///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2007 Jason Baldridge, The University of Texas at Austin
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//////////////////////////////////////////////////////////////////////////////
package texnlp.taggers;

import gnu.trove.set.hash.THashSet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;

import texnlp.estimate.AggressiveWordContextGenerator;
import texnlp.estimate.CCWordContextGenerator;
import texnlp.estimate.Context;
import texnlp.estimate.TadmClassifier;
import texnlp.estimate.WordContextGenerator;
import texnlp.io.DataReader;
import texnlp.util.TaggerOptions;

/**
 * A Maximum Entropy Markov Model, trained on words and tags.
 * 
 * @author Jason Baldridge
 * @version $Revision: 1.53 $, $Date: 2006/10/12 21:20:44 $
 */
public class MEMM extends MarkovModel {
    private int maxIterations = 0;
    private String taggedFile;
    private String labeledFile;
    private int numMachines;
    private double variance;

    private TadmClassifier mInitial;
    private TadmClassifier mFinal;
    private TadmClassifier mTransition;

    // private WordContextGenerator wcgen = new
    // AggressiveWordContextGenerator();
    // private WordContextGenerator wcgen = new CCWordContextGenerator();
    // private StateContextGenerator scgen;

    private final WordContextGenerator wcgen;

    String[][] cachedWordFeatures;

    public MEMM(TaggerOptions taggerOptions) {
        super(taggerOptions);
        this.maxIterations = taggerOptions.getNumIterations();
        this.taggedFile = taggerOptions.getTaggedFile();
        this.numMachines = taggerOptions.getNumMachines();
        this.labeledFile = taggerOptions.getMachineFile();
        final double variance = taggerOptions.getPriorAmount();

        if (variance == 0.0)
            this.variance = 1000.0;
        else
            this.variance = variance;

        final String contextGenOption = taggerOptions.getContextGen();
        if (contextGenOption.equals("word"))
            wcgen = new CCWordContextGenerator();
        else if (contextGenOption.equals("aggressive"))
            wcgen = new AggressiveWordContextGenerator();
        else
            throw new RuntimeException("Unknown word context generator: " + contextGenOption);
    }

    protected final double[][] getTransitionLogProbs(int[] iStates, int[] jStates, int tokenID, String[] tokens) {

        // String[] prevStateContexts = scgen.getStateContexts(prevStateID);
        // for (int i=0; i<prevStateContexts.length; i++)
        // contexts.add(prevStateContexts[i]);

        double[][] probs = mTransition.getLogDistributions(cachedWordFeatures[tokenID], iStates, jStates);

        return probs;
    }

    protected final double[] getInitialLogProbs(String[] tokens) {
        return mInitial.getLogDistribution(cachedWordFeatures[0]);
    }

    protected final double[] getFinalLogProbs(String[] tokens) {
        return mFinal.getLogDistribution(cachedWordFeatures[tokens.length - 1]);
    }

    protected void prepareForSentence(String[] tokens) {
        String[][] wordContexts = new String[tokens.length][];
        for (int tokenID = 0; tokenID < tokens.length; tokenID++) {
            if (highFrequencyWords.contains(tokens[tokenID])) {
                String[] singleWordContext = { tokens[tokenID] };
                wordContexts[tokenID] = singleWordContext;
            }
            else {
                wordContexts[tokenID] = wcgen.getWordContexts(tokens[tokenID]);
            }
        }

        cachedWordFeatures = new String[tokens.length][];
        for (int tokenID = 0; tokenID < tokens.length; tokenID++) {
            int index = wordContexts[tokenID].length;
            String[] contexts = Arrays.copyOf(wordContexts[tokenID], index + 4);

            if (tokenID > 1) {
                contexts[index] = "prev:" + tokens[tokenID - 1];
                if (tokenID > 2) {
                    contexts[index + 1] = "prev2:" + tokens[tokenID - 2];
                }
                else {
                    contexts[index + 1] = "prev2:**BOUNDARY**";
                }
            }
            else {
                contexts[index] = "prev:**BOUNDARY**";
                contexts[index + 1] = "prev2:**BOUNDARY**";
            }

            if (tokenID < tokens.length - 1) {
                contexts[index + 2] = "next:" + tokens[tokenID + 1];
                if (tokenID < tokens.length - 2) {
                    contexts[index + 3] = "next2:" + tokens[tokenID + 2];
                }
                else {
                    contexts[index + 3] = "next2:**BOUNDARY**";
                }
            }
            else {
                contexts[index + 2] = "next:**BOUNDARY**";
                contexts[index + 3] = "next2:**BOUNDARY**";
            }

            cachedWordFeatures[tokenID] = contexts;
            // cachedWordFeatures[tokenID] = wordContexts[tokenID];
        }
    }

    protected void addSpecificContexts(String identifier, String[] wordContexts, THashSet<String> fullContexts) {
        for (int contextID = 0; contextID < wordContexts.length; contextID++)
            fullContexts.add(identifier + wordContexts[contextID]);

    }

    public void train() {

        try {

            File taggedEvents;
            if (!this.labeledFile.equals("")) {

                /* Create combined events file */
                taggedEvents = File.createTempFile("events", "txt");

                /* Set up output stream for combined events file */
                Writer outputStream = new BufferedWriter(new FileWriter(taggedEvents));

                /* Open taggedFile and write it into combined events file */
                Reader inputStream = new BufferedReader(new FileReader(this.taggedFile));

                int c;
                while ((c = inputStream.read()) != -1)
                    outputStream.write(c);

                inputStream.close();

                /* Open labeledFile and write it into combined events file */
                inputStream = new BufferedReader(new FileReader(this.labeledFile));

                while ((c = inputStream.read()) != -1)
                    outputStream.write(c);

                inputStream.close();

                /* Close up output stream -- should be good to go */
                outputStream.close();

            }
            else {
                taggedEvents = new File(taggedFile);
            }

            // Set things up
            scanFile(taggedEvents);
            prepare();

            // scgen = new StateContextGenerator(stateNames);

            // now observe events
            mInitial = new TadmClassifier("initial", "tao_lmvm", numStates, maxIterations, variance, numMachines);
            mFinal = new TadmClassifier("final", "tao_lmvm", numStates, maxIterations, variance, numMachines);
            mTransition = new TadmClassifier("transition", "tao_lmvm", numStates, maxIterations, variance, numMachines);

            DataReader inputReader = getDataReader(taggedEvents);

            try {
                String[][] sequence = inputReader.nextSequence();

                while (true) {

                    int prevStateID = -1;

                    String[] tokens = new String[sequence.length];
                    for (int tokenID = 0; tokenID < sequence.length; tokenID++)
                        tokens[tokenID] = sequence[tokenID][0];

                    prepareForSentence(tokens);

                    for (int tokenID = 0; tokenID < sequence.length; tokenID++) {

                        // String output = sequence[tokenID][0];

                        int t = states.get(sequence[tokenID][1]);

                        String[] contexts = cachedWordFeatures[tokenID];

                        if (tokenID == 0) {
                            mInitial.addEvent(t, new Context(contexts));
                        }
                        else {
                            String[] contexts2 = Arrays.copyOf(contexts, contexts.length + 1);
                            contexts2[contexts.length] = "prevtag:" + Integer.toString(prevStateID);
                            // String[] prevStateContexts =
                            // scgen.getStateContexts(prevStateID);
                            // for (int i=0; i<prevStateContexts.length; i++)
                            // contexts.add(prevStateContexts[i]);

                            mTransition.addEvent(t, new Context(contexts2));
                        }

                        prevStateID = t;
                    }

                    mFinal.addEvent(prevStateID, new Context(cachedWordFeatures[sequence.length - 1]));

                    sequence = inputReader.nextSequence();
                }

            }
            catch (EOFException e) {
                inputReader.close();
            }

        }
        catch (IOException e) {
            System.out.println(e);
        }

        mTransition.train();
        mInitial.train();
        mFinal.train();

    }

}
