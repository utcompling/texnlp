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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collections;
import java.util.Vector;

import texnlp.estimate.Context;
import texnlp.estimate.TadmClassifier;
import texnlp.io.DataReader;
import texnlp.util.MathUtil;
import texnlp.util.TaggerOptions;

/**
 * A Maximum Entropy Labeler (no sequence info), trained on words and tags.
 * Extends MarkovModel, but doesn't actually do sequence labeling.
 * 
 * @author Jason Baldridge
 * @version $Revision: 1.53 $, $Date: 2006/10/12 21:20:44 $
 */
public class MEL extends MaxentTagger {
    private String taggedFile;
    private String labeledFile = null;
    private double logBeta;
    private boolean unconstrainedByTagdict;
    private boolean multitag;

    private TadmClassifier mTagGivenContext;

    public MEL(TaggerOptions topt) {
        super(topt);
        this.taggedFile = topt.getTaggedFile();
        this.labeledFile = topt.getMachineFile();
        this.logBeta = topt.getLogBeta();
        this.unconstrainedByTagdict = topt.isUnconstrainedByTagdict();
        this.multitag = topt.isMultitag();
    }

    public String[] tagSentence(String[] tokens, TagResults results) {
        final int numTokens = tokens.length;

        // double chanceProb = 1.0/numStates;

        // Make sure there is something to tag
        if (numTokens < 1)
            return new String[0];

        String[][] cachedWordFeatures = prepareForSentence(tokens);

        String[] labels = new String[numTokens];

        double totalProb = MathUtil.elog(1.0);
        for (int tokenID = 0; tokenID < numTokens; tokenID++) {

            double[] probs = mTagGivenContext.getLogDistribution(cachedWordFeatures[tokenID]);

            results.addEntropy(MathUtil.entropyOfLogDistribution(probs));

            int[] validTags = tagDictionary.getTags(tokens[tokenID]);

            int bestState = -1;
            double max = MathUtil.LOG_ZERO;

            if (multitag) {

                StringBuffer label = new StringBuffer();
                int numAboveThreshold = 0;
                if (unconstrainedByTagdict) {
                    for (int iStateID = 0; iStateID < numStates; iStateID++) {
                        if (probs[iStateID] > max) {
                            max = probs[iStateID];
                            bestState = iStateID;
                        }
                    }

                    final double threshold = logBeta + max;
                    for (int iStateID = 0; iStateID < numStates; iStateID++) {
                        if (probs[iStateID] > threshold) {
                            // || (tagDictionary.containsWord(tokens[tokenID])
                            // && Arrays.binarySearch(validTags, iStateID) >
                            // -1)) {
                            numAboveThreshold++;
                            label.append(stateNames[iStateID]).append('\t');
                            label.append(Math.exp(probs[iStateID])).append('\t');
                        }
                    }

                }
                else {

                    for (int i = 0; i < validTags.length; i++) {
                        final int iStateID = validTags[i];
                        if (probs[iStateID] > max) {
                            bestState = iStateID;
                            max = probs[iStateID];
                        }
                    }
                    final double threshold = logBeta + max;
                    for (int i = 0; i < validTags.length; i++) {
                        final int iStateID = validTags[i];
                        if (probs[iStateID] > threshold) {
                            numAboveThreshold++;
                            label.append(stateNames[iStateID]).append('\t');
                            label.append(Math.exp(probs[iStateID])).append('\t');
                        }
                    }

                }
                labels[tokenID] = numAboveThreshold + "\t" + label.toString().trim();

            }
            else {

                if (unconstrainedByTagdict) {
                    for (int iStateID = 0; iStateID < numStates; iStateID++) {
                        if (probs[iStateID] > max) {
                            bestState = iStateID;
                            max = probs[iStateID];
                        }
                    }
                }
                else {
                    for (int i = 0; i < validTags.length; i++) {
                        final int iStateID = validTags[i];
                        if (probs[iStateID] > max) {
                            bestState = iStateID;
                            max = probs[iStateID];
                        }
                    }
                }
                labels[tokenID] = stateNames[bestState];
            }

            totalProb = MathUtil.elogProduct(totalProb, max);
        }

        results.addPerplexity(Math.exp(-1 * (totalProb / numTokens)));
        results.addEntropyBlank();

        // System.out.println(StringUtil.mergeJoin("/", tokens, labels));

        return labels;
    }

    @Override
    public Vector<String[]> tagSentenceIte(String[] tokens, TagResults results) {
        numTokens = tokens.length;

        // double chanceProb = 1.0/numStates;

        Vector<String[]> labels = new Vector<String[]>();
        // Make sure there is something to tag

        String[][] cachedWordFeatures = prepareForSentence(tokens);

        double totalProb = MathUtil.elog(1.0);
        for (int tokenID = 0; tokenID < numTokens; tokenID++) {

            double[] probs = mTagGivenContext.getLogDistribution(cachedWordFeatures[tokenID]);

            // results.addEntropy(MathUtil.entropyOfLogDistribution(probs));

            Vector<PairIte> tagpairs = new Vector<PairIte>();

            double max = MathUtil.LOG_ZERO;

            int numAboveThreshold = 0;
            for (int iStateID = 0; iStateID < numStates; iStateID++) {
                if (probs[iStateID] > max) {
                    max = probs[iStateID];
                }
            }

            final double threshold = logBeta + max;
            for (int iStateID = 0; iStateID < numStates; iStateID++) {
                if (probs[iStateID] > threshold) {
                    PairIte tagpair = new PairIte();
                    // || (tagDictionary.containsWord(tokens[tokenID])
                    // && Arrays.binarySearch(validTags, iStateID) > -1)) {
                    numAboveThreshold++;
                    tagpair.prob = Math.exp(probs[iStateID]);
                    tagpair.token = stateNames[iStateID];
                    tagpairs.add(tagpair);
                }
            }

            Collections.sort(tagpairs);

            // Section for getting rid of tags which have less than half the
            // prob of the previous tag
            int counter = 0;
            for (PairIte pi : tagpairs) {
                try {
                    if (pi.prob / 2 > tagpairs.get(counter + 1).prob) {
                        counter++;
                        break;
                    }
                    else {
                        counter++;
                    }
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    counter++;
                }
            }

            String[] tstring = new String[counter];
            labels.add(tstring);
            for (int i = 0; i < counter; i++) {
                tstring[i] = tagpairs.get(i).token;
            }
            // Alternative with no culling
            // String[] tstring = new String[tagpairs.size()];
            // labels.add(tstring);
            // int counter = 0;
            // for(PairIte pi: tagpairs){
            // tstring[counter++] = pi.token;
            // }
            totalProb = MathUtil.elogProduct(totalProb, max);
        }

        // results.addPerplexity(Math.exp(-1 * (totalProb / numTokens)));
        // results.addEntropyBlank();

        // System.out.println(StringUtil.mergeJoin("/", tokens, labels));

        return labels;
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

            // now observe events
            mTagGivenContext = new TadmClassifier("initial", "tao_lmvm", numStates, maxIterations, variance,
                    numMachines);

            DataReader inputReader = getDataReader(taggedEvents);
            try {
                String[][] sequence = inputReader.nextSequence();

                // TODO rm
                int testCounter = 0;

                while (true) {

                    String[] tokens = new String[sequence.length];
                    for (int tokenID = 0; tokenID < sequence.length; tokenID++)
                        tokens[tokenID] = sequence[tokenID][0];

                    String[][] cachedWordFeatures = prepareForSentence(tokens);

                    for (int tokenID = 0; tokenID < sequence.length; tokenID++) {

                        String tag = sequence[tokenID][1];

                        if (!tag.equals(Tagger.UNLABELED_TAG)) {

                            // String output = sequence[tokenID][0];

                            if (states.containsKey(sequence[tokenID][1])) {
                                int t = states.get(sequence[tokenID][1]);
                                mTagGivenContext.addEvent(t, new Context(cachedWordFeatures[tokenID]));
                            }
                        }

                        // TODO rm
                        if ((testCounter++ % 1000) == 0)
                            System.out.print('.');
                    }
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

        // TODO rm
        System.out.println();

        mTagGivenContext.train();
    }

}
