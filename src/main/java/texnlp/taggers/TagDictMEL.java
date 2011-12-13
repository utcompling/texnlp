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

import java.io.EOFException;
import java.io.File;
import java.io.IOException;

import texnlp.estimate.Context;
import texnlp.estimate.TadmClassifier;
import texnlp.io.DataReader;
import texnlp.util.MathUtil;
import texnlp.util.TaggerOptions;

/**
 * A Maximum Entropy Labeler (no sequence info) that models the probability of a
 * tag given only the word (and no surrounding context, either pos or words).
 * 
 * @author Jason Baldridge
 * @version $Revision: 1.53 $, $Date: 2006/10/12 21:20:44 $
 */
public class TagDictMEL extends MaxentTagger {
    private String taggedFile;
    private double logBeta;
    private boolean unconstrainedByTagdict;
    private boolean multitag;

    private TadmClassifier mTagGivenContext;

    // private WordContextGenerator wcgen = new CCWordContextGenerator();
    // private WordContextGenerator wcgen = new
    // AggressiveWordContextGenerator();

    public TagDictMEL(TaggerOptions options) {
        super(options);
        this.taggedFile = options.getTaggedFile();
        this.logBeta = options.getLogBeta();
        this.unconstrainedByTagdict = options.isUnconstrainedByTagdict();
        this.multitag = options.isMultitag();
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

        // System.out.println(StringUtil.mergeJoin("/", tokens, labels));

        return labels;
    }

    public void train() {

        try {

            File taggedEvents = new File(taggedFile);

            // Set things up
            scanFile(taggedEvents);
            prepare();

            // now observe events
            mTagGivenContext = new TadmClassifier("initial", "tao_lmvm", numStates, maxIterations, variance,
                    numMachines);

            // Set<String> words = tagDictionary.getWords();
            // for (String w: words) {
            // String[] features = wcgen.getWordContexts(w);
            // int[] tags = tagDictionary.getTags(w);
            // for (int i=0; i<tags.length; i++)
            // mTagGivenContext.addEvent(tags[i], new Context(features));
            // }

            DataReader inputReader = getDataReader(taggedEvents);
            try {
                String[][] sequence = inputReader.nextSequence();

                while (true) {

                    String[] tokens = new String[sequence.length];
                    for (int tokenID = 0; tokenID < sequence.length; tokenID++)
                        tokens[tokenID] = sequence[tokenID][0];

                    // String[][] cachedWordFeatures =
                    // prepareForSentence(tokens);

                    for (int tokenID = 0; tokenID < sequence.length; tokenID++) {

                        String tag = sequence[tokenID][1];

                        if (!tag.equals(Tagger.UNLABELED_TAG)) {

                            String output = sequence[tokenID][0];

                            // if (states.containsKey(sequence[tokenID][1])) {
                            // int t = states.get(sequence[tokenID][1]);
                            // mTagGivenContext.addEvent(t,
                            // new Context(cachedWordFeatures[tokenID]));
                            // }

                            String[] features = wcgen.getWordContexts(output);
                            int[] tags = tagDictionary.getTags(output);
                            for (int i = 0; i < tags.length; i++)
                                mTagGivenContext.addEvent(tags[i], new Context(features));

                        }
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

        mTagGivenContext.train();
    }

}
