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

import java.util.Arrays;

import texnlp.util.MathUtil;
import texnlp.util.TaggerOptions;

/**
 * An abstract bigram Markov Model -- could be an HMM or MEMM.
 * 
 * @author Jason Baldridge
 * @version $Revision: 1.53 $, $Date: 2006/10/12 21:20:44 $
 */
public abstract class MarkovModel extends Tagger {

    protected double logBeta;

    protected MarkovModel(TaggerOptions options) {
        super(options);
        this.logBeta = options.getLogBeta();
    }

    protected abstract double[][] getTransitionLogProbs(int[] iStates, int[] jStates, int tokenID, String[] tokens);

    protected abstract double[] getInitialLogProbs(String[] tokens);

    protected abstract double[] getFinalLogProbs(String[] tokens);

    // Can be overridden to cache features
    protected void prepareForSentence(String[] tokens) {
        return;
    }

    // Can be overridden to add in unknown tokens, etc.
    protected String[] normalizeTokens(String[] tokens) {
        return tokens;
    }

    // Split a string to pass on to viterbiTag(String[])
    public String[] tagSentence(String sentence, TagResults results) {
        return tagSentence(sentence.split(" "), results);
    }

    // Run the viterbi algorithm to find the most probable tag sequence.
    public String[] tagSentence(String[] tokens, TagResults results) {
        // System.out.print(".");

        final int numTokens = tokens.length;

        // Make sure there is something to tag
        if (numTokens < 1)
            return new String[0];

        // Normalize the raw tokens, e.g. replace unknown words with UNKNOWN
        // values
        tokens = normalizeTokens(tokens);
        prepareForSentence(tokens);

        int[][] validTags = new int[numTokens][];
        for (int tokenID = 0; tokenID < numTokens; tokenID++)
            validTags[tokenID] = tagDictionary.getTags(tokens[tokenID]);

        // Now get ready to run Viterbi and do it
        double[][] viterbi = new double[numTokens + 1][numStates];
        int[][] backtraces = new int[numTokens][numStates];

        viterbi[0] = getInitialLogProbs(tokens);

        for (int tokenID = 1; tokenID < numTokens; tokenID++) {

            int[] iStates = validTags[tokenID - 1];
            int[] jStates = validTags[tokenID];

            // cache the transition probabilities (important for MEMM)
            double[][] transitions = getTransitionLogProbs(iStates, jStates, tokenID, tokens);

            double maxViterbi = MathUtil.LOG_ZERO;
            for (int j = 0; j < jStates.length; j++) {
                final int jStateID = jStates[j];

                int bestPrevState = -1;
                double max = MathUtil.LOG_ZERO;

                for (int i = 0; i < iStates.length; i++) {
                    final int iStateID = iStates[i];

                    final double pathProb = viterbi[tokenID - 1][iStateID] + transitions[iStateID][jStateID];

                    // We can assume that these are not LOG_ZERO
                    // because we are restricted to states which have
                    // non-zero emission probs. Otherwise we would need:
                    //
                    // MathUtil.elogProduct(viterbi[tokenID-1][iStateID],
                    // transitions[iStateID][jStateID]);

                    if (pathProb > max) {
                        max = pathProb;
                        bestPrevState = iStateID;
                    }
                }
                viterbi[tokenID][jStateID] = max;
                backtraces[tokenID][jStateID] = bestPrevState;

                // System.out.println(stateNames[bestPrevState] + "->" +
                // stateNames[jStateID] + ": " + max);

                if (max > maxViterbi)
                    maxViterbi = max;
            }

            if (validTags[tokenID].length > 1)
                validTags[tokenID] = applyBeta(validTags[tokenID], viterbi[tokenID], logBeta + maxViterbi);

        }

        int endLabel = -1;
        double bestTotalProb = MathUtil.LOG_ZERO;

        double[] finalProbs = getFinalLogProbs(tokens);
        int[] lastStates = validTags[numTokens - 1];
        for (int i = 0; i < lastStates.length; i++) {
            int stateID = lastStates[i];

            final double finalProb = viterbi[numTokens - 1][stateID] + finalProbs[stateID];

            // See comment in above loop about LOG_ZERO and not needing
            // elogProduct.
            // MathUtil.elogProduct(viterbi[numTokens-1][stateID],
            // finalProbs[stateID]);

            if (finalProb > bestTotalProb) {
                bestTotalProb = finalProb;
                endLabel = stateID;
            }
        }

        // double perplexity = Math.exp(-1*(bestTotalProb/numTokens));
        // System.out.println("Log prob: " + bestTotalProb);
        // System.out.println("Perplexity per tagged word: " + perplexity);
        results.addPerplexity(Math.exp(-1 * (bestTotalProb / numTokens)));

        String[] labels = new String[numTokens];

        for (int i = numTokens - 1; i > 0; i--) {
            labels[i] = stateNames[endLabel];
            endLabel = backtraces[i][endLabel];
            // System.out.println("Log prob: " + i + " " + endLabel + ":" +
            // viterbi[i][endLabel]);
        }
        labels[0] = stateNames[endLabel];

        // System.out.println(StringUtil.mergeJoin("/", tokens, labels));

        return labels;
    }

    /*
     * private int[] applyBeam (int[] tags, double[] probs, int beamWidth) {
     * 
     * IntDoublePair[] sorted = new IntDoublePair[tags.length]; for (int i=0;
     * i<tags.length; i++) sorted[i] = new IntDoublePair(tags[i],
     * probs[tags[i]]); Arrays.sort(sorted);
     * 
     * int[] beamed = new int[beamWidth]; for (int i=0; i<beamWidth; i++)
     * beamed[i] = sorted[i].intValue; return beamed; }
     */
    protected int[] applyBeta(int[] tags, double[] probs, double threshhold) {

        int[] betaCut = new int[tags.length];
        int numKept = 0;
        for (int i = 0; i < tags.length; i++) {
            if (probs[tags[i]] > threshhold) {
                betaCut[numKept] = tags[i];
                numKept++;
            }
        }

        return Arrays.copyOf(betaCut, numKept);
    }

}
