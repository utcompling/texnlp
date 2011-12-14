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

import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.list.linked.TLinkedList;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import texnlp.io.DataReader;
import texnlp.util.IntDoublePair;
import texnlp.util.MathUtil;
import texnlp.util.TaggerOptions;

/**
 * A Hidden Markov Model, trained on tagged and untagged sentences. Constructs a
 * tag dictionary from the tagged data.
 * 
 * @author Jason Baldridge
 * @version $Revision: 1.53 $, $Date: 2006/10/12 21:20:44 $
 */
public class HMM extends MarkovModel {
    private static Log LOG = LogFactory.getLog(HMM.class);

    private int numIterations;
    protected String taggedFile;
    protected String machineFile;
    protected String rawFile;
    protected String devFile;

    protected EmissionProbs pEmission;
    protected double[] pInitial;
    protected double[] pFinal;
    protected double[][] pTransition;

    protected boolean tagdictTraining;
    protected double priorAmount;
    protected double cutoffTD;

    protected boolean dirichletTransition;
    protected boolean dirichletEmission;

    // Scaler to apply to soft counts from machine labeled files, and in EM
    protected double lambda;

    // The minimum change in log probability between iterations for EM to keep
    // going.
    private final double tolerance = .0001;

    public HMM(TaggerOptions taggerOptions) {
        super(taggerOptions);
        numIterations = taggerOptions.getNumIterations();
        taggedFile = taggerOptions.getTaggedFile();
        machineFile = taggerOptions.getMachineFile();
        rawFile = taggerOptions.getRawFile();
        devFile = taggerOptions.getDevFile();
        tagdictTraining = taggerOptions.isTagdictTraining();
        priorAmount = taggerOptions.getPriorAmount();
        dirichletTransition = taggerOptions.isDirichletTransition();
        dirichletEmission = taggerOptions.isDirichletEmission();
        tagDictionary.setThreshold(taggerOptions.getCutoffTD());
        lambda = taggerOptions.getLambda();
    }

    // Run the viterbi algorithm to find the most probable tag sequence.
    // MarkovModel method is overridden so that it can be faster for the HMM.
    public final String[] tagSentence(String[] tokens, TagResults results) {
        final int numTokens = tokens.length;

        // Make sure there is something to tag
        if (numTokens < 1)
            return new String[0];

        // Normalize the raw tokens, e.g. replace unknown words with UNKNOWN
        // values
        tokens = normalizeTokens(tokens);

        int[][] validTags = new int[numTokens][];
        // for (int tokenID=0; tokenID<numTokens; tokenID++)
        // validTags[tokenID] = tagDictionary.getTags(tokens[tokenID]);

        // Now get ready to run Viterbi and do it
        double[][] viterbi = new double[numTokens + 1][numStates];
        int[][] backtraces = new int[numTokens][numStates];

        viterbi[0] = new double[numStates];
        Arrays.fill(viterbi[0], MathUtil.LOG_ZERO);

        TLinkedList<IntDoublePair> validFirstStates = pEmission.get(tokens[0]);
        validTags[0] = new int[validFirstStates.size()];
        IntDoublePair currentFirst = validFirstStates.getFirst();
        double maxFirst = MathUtil.LOG_ZERO;
        for (int i = 0; i < validTags[0].length; i++) {
            int stateID = currentFirst.intValue;
            validTags[0][i] = stateID;
            viterbi[0][stateID] = MathUtil.elogProduct(pInitial[stateID], currentFirst.doubleValue);
            if (viterbi[0][stateID] > maxFirst)
                maxFirst = viterbi[0][stateID];
            currentFirst = (IntDoublePair) currentFirst.getNext();
        }

        if (validTags[0].length > 1)
            validTags[0] = applyBeta(validTags[0], viterbi[0], logBeta + maxFirst);

        for (int tokenID = 1; tokenID < numTokens; tokenID++) {

            int[] iStates = validTags[tokenID - 1];

            double maxViterbi = MathUtil.LOG_ZERO;
            final String token = tokens[tokenID];
            TLinkedList<IntDoublePair> validStates = pEmission.get(token);
            IntDoublePair current = validStates.getFirst();
            int[] jStates = new int[validStates.size()];
            for (int j = 0; j < jStates.length; j++) {
                final int jStateID = current.intValue;
                jStates[j] = jStateID;

                int bestPrevState = -1;
                double max = MathUtil.LOG_ZERO;

                for (int iStateID : iStates) {
                    final double prevPathLogProb = viterbi[tokenID - 1][iStateID];
                    final double transitionLogProb = pTransition[iStateID][jStateID];
                    final double emissionLogProb = current.doubleValue;
                    final double pathLogProb = prevPathLogProb + transitionLogProb + emissionLogProb;

                    // We can skip the use of MathUtil.elogProduct since
                    // neither of these values can be zero. This is what it
                    // would be otherwise:
                    // MathUtil.elogProduct(viterbi[tokenID-1][iStateID],pTransition[iStates[i]][stateID],
                    // current.doubleValue);
                    if (pathLogProb > max) {
                        max = pathLogProb;
                        bestPrevState = iStateID;
                    }
                }
                viterbi[tokenID][jStateID] = max;
                backtraces[tokenID][jStateID] = bestPrevState;

                if (max > maxViterbi)
                    maxViterbi = max;

                current = (IntDoublePair) current.getNext();
            }

            if (jStates.length > 1) {
                validTags[tokenID] = applyBeta(jStates, viterbi[tokenID], logBeta + maxViterbi);
            }
            else {
                validTags[tokenID] = jStates;
            }
            // LOG.debug(tokens[tokenID] + ": " +
            // StringUtil.join(validTags[tokenID]));

        }

        int endLabel = -1;
        // double[] finalProbs = getFinalLogProbs(tokens);
        final int lastTokenID = numTokens - 1;
        double bestTotalProb = MathUtil.LOG_ZERO;
        double finalViterbi;
        double finalDistLogProb;
        double finalProb;

        if (validTags[lastTokenID].length != 1) {

            int[] lastStates = validTags[lastTokenID];

            for (int stateID : lastStates) {
                finalViterbi = viterbi[lastTokenID][stateID];
                finalDistLogProb = pFinal[stateID];
                finalProb = finalViterbi + finalDistLogProb;

                // See comment in above loop about LOG_ZERO and not needing
                // elogProduct.
                // MathUtil.elogProduct(viterbi[numTokens-1][stateID],
                // finalProbs[stateID]);

                // LOG.debug(bestTotalProb + "\t" + finalProb);

                if (finalProb > bestTotalProb) {
                    bestTotalProb = finalProb;
                    endLabel = stateID;
                }
            }
        }
        else {
            int stateID = validTags[lastTokenID][0];
            finalViterbi = viterbi[lastTokenID][stateID];
            finalDistLogProb = pFinal[stateID];

            if (finalViterbi != MathUtil.LOG_ZERO)
                finalProb = finalViterbi + finalDistLogProb;
            else
                finalProb = finalDistLogProb;

            endLabel = stateID;
        }

        // double perplexity = Math.exp(-1*(bestTotalProb/numTokens));
        // LOG.debug("Log prob: " + bestTotalProb);
        // LOG.debug("Perplexity per tagged word: " + perplexity);
        results.addPerplexity(Math.exp(-1 * (bestTotalProb / numTokens)));

        String[] labels = new String[numTokens];

        for (int i = lastTokenID; i > 0; i--) {
            labels[i] = stateNames[endLabel];
            endLabel = backtraces[i][endLabel];
            // LOG.debug("Log prob: " + i + " " + endLabel + ":" +
            // viterbi[i][endLabel]);
        }

        try {
            labels[0] = stateNames[endLabel];
        }
        catch (java.lang.ArrayIndexOutOfBoundsException e) {
            StringBuilder sb = new StringBuilder("Issue tagging:\n");
            for (int k = 0; k < tokens.length; k++) {
                sb.append(tokens[k]);
                sb.append(" ");
            }
            sb.append("\n");
            throw new RuntimeException(sb.toString(), e);
        }

        // LOG.debug(StringUtil.mergeJoin("/", tokens, labels));

        return labels;
    }

    protected double[][] getTransitionLogProbs(int[] iStates, int[] jStates, int tokenID, String[] tokens) {

        double[][] probs = new double[numStates][numStates];
        for (int i = 0; i < numStates; i++)
            Arrays.fill(probs[i], MathUtil.LOG_ZERO);

        TLinkedList<IntDoublePair> validStates = pEmission.get(tokens[tokenID]);
        IntDoublePair current = validStates.getFirst();
        for (int j = validStates.size(); j > 0; j--) {
            final int stateID = current.intValue;

            for (int i = 0; i < iStates.length; i++)
                probs[iStates[i]][stateID] = pTransition[iStates[i]][stateID] + current.doubleValue;

            // We can skip the use of MathUtil.elogProduct since
            // neither of these values can be zero. This is what it
            // would be otherwise:
            // MathUtil.elogProduct(pTransition[iStates[i]][stateID],
            // current.doubleValue);

            current = (IntDoublePair) current.getNext();
        }

        return probs;
    }

    protected double[] getInitialLogProbs(String[] tokens) {
        double[] probs = new double[numStates];
        Arrays.fill(probs, MathUtil.LOG_ZERO);

        TLinkedList<IntDoublePair> validStates = pEmission.get(tokens[0]);
        IntDoublePair current = validStates.getFirst();
        for (int i = validStates.size(); i > 0; i--) {
            int stateID = current.intValue;
            probs[stateID] = MathUtil.elogProduct(pInitial[stateID], current.doubleValue);
            current = (IntDoublePair) current.getNext();
        }

        return probs;
    }

    protected double[] getFinalLogProbs(String[] tokens) {
        double[] probs = new double[numStates];
        for (int stateID = 0; stateID < numStates; stateID++)
            probs[stateID] = pFinal[stateID];
        return probs;
    }

    public void train() {

        try {

            File taggedEvents = new File(taggedFile);

            // Set things up
            scanFile(taggedEvents);

            if (!machineFile.equals(""))
                scanFile(new File(machineFile));

            // now do the actual counts
            Counts cEmpty = new Counts(numStates, tagdictTraining, dirichletTransition, dirichletEmission);
            Counts cOrig = new Counts(numStates, tagdictTraining, dirichletTransition, dirichletEmission);
            Counts cTotal;

            if (tagdictTraining) {
                // Do semi-supervised training vith a tag dictionary,
                // possibly using frequencies from the tag dictionary
                // to cut out noise (via -c option). See Banko and
                // Moore 2004.

                tagDictionary.applyThreshold();

                tagDictionary.finalize(numStates);

                // for (String word: tagDictionary.getWords()) {
                // int[] tagsForWord = tagDictionary.getTags(word);
                // final int numTagsForWord = tagsForWord.length;
                // cOrig.increment(word);
                // for (int j=0; j<numTagsForWord; j++) {
                // cOrig.increment(tagsForWord[j], word);
                // cOrig.increment(tagsForWord[j]);
                // }
                // }

                for (int i = 0; i < numStates; i++) {
                    cOrig.incrementInitial(i, 1.0 / (double) numStates);
                    cOrig.incrementFinal(i, 1.0 / (double) numStates);
                    final double amount = cOrig.c_t[i] / (double) numStates;
                    for (int j = 0; j < numStates; j++) {
                        cOrig.increment(i, j, amount);
                    }
                }

                // LOG.debug(StringUtil.join(c.c_t));

                // The next block of code adds pseudo-counts to the
                // emission counts by looking at each word, and using
                // as its count for a tag's emission that word's
                // overall frequency divided by the number of tags for
                // the word in the tag dictionary.
                DataReader rawinputReader = getDataReader(new File(rawFile));
                TObjectIntHashMap<String> wordFreqs = new TObjectIntHashMap<String>();
                try {
                    while (true) {
                        String[] words = rawinputReader.nextOutputSequence();
                        for (int i = 0; i < words.length; i++) {
                            wordFreqs.adjustOrPutValue(words[i], 1, 1);
                        }
                    }
                }
                catch (EOFException e) {
                    rawinputReader.close();
                }
                TObjectIntIterator<String> iterator = wordFreqs.iterator();
                for (int i = wordFreqs.size(); i-- > 0;) {
                    iterator.advance();
                    String word = iterator.key();

                    if (tagDictionary.containsWord(word)) {
                        final int freqOfWord = iterator.value();
                        cOrig.increment(word, (double) freqOfWord);
                        int[] tagsForWord = tagDictionary.getTags(word);
                        final int numTagsForWord = tagsForWord.length;
                        double amount = freqOfWord / (double) numTagsForWord;
                        // double amount = freqOfWord;
                        for (int j = 0; j < numTagsForWord; j++) {
                            cOrig.increment(tagsForWord[j], word, amount);
                            cOrig.increment(tagsForWord[j], amount);
                        }
                    }
                }

                cTotal = cOrig.copy();

            }
            else {
                // Do standard supervised training
                addToCounts(taggedEvents, cOrig);

                // Make a copy of the original counts.
                // The originals will be used to seed EM
                cTotal = cOrig.copy();

                if (!machineFile.equals(""))
                    addToCounts(new File(machineFile), cTotal, lambda);

                if (!rawFile.equals("")) {
                    DataReader rawinputReader = getDataReader(new File(rawFile));
                    try {
                        while (true) {
                            String[] words = rawinputReader.nextOutputSequence();
                            for (String word : words)
                                cOrig.addToSeenWords(word);
                        }
                    }
                    catch (EOFException e) {
                        rawinputReader.close();
                    }
                }
            }

            // Use the CCG priors
            boolean startWithPrior = false;
            if (priorAmount > 0) {
                cTotal.createTransitionPrior(stateNames, states, priorAmount);
                startWithPrior = tagdictTraining;
            }

            prepare();
            highFrequencyWords = null;
            tagDictionary = null;

            cTotal.prepare();

            // for (int i=0; i<stateNames.length; i++)
            // LOG.debug(i + " :: " + stateNames[i]);

            pInitial = cTotal.getInitialLogDist(startWithPrior);
            pFinal = cTotal.getFinalLogDist(startWithPrior);
            pTransition = cTotal.getTransitionLogDist(startWithPrior);
            pEmission = cTotal.getEmissionLogDist();

            if (numIterations > 0) {
                forwardBackward(cOrig, cEmpty);
            }

        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void addToCounts(File file, Counts c) throws IOException {
        addToCounts(file, c, 1.0);
    }

    private void addToCounts(File file, Counts c, double amount) throws IOException {
        DataReader inputReader = getDataReader(file);

        try {
            String[][] sequence = inputReader.nextSequence();

            while (true) {

                // Do first block if there are multitags, otherwise do the
                // normal thing
                if (sequence[0].length > 2 && Character.isDigit(sequence[0][1].charAt(0))) {
                    int[] prevTags = new int[0];
                    double[] prevProbs = new double[0];

                    // TODO use amount in this block
                    // TODO multiply all the multitag values by amount, right?
                    for (int tokenID = 0; tokenID < sequence.length; tokenID++) {

                        final String output = sequence[tokenID][0];
                        c.increment(output);

                        final int numTags = Integer.parseInt(sequence[tokenID][1]);

                        int[] tokenTags = new int[numTags];
                        double[] tokenProbs = new double[numTags];

                        for (int i = 0; i < numTags; i++) {
                            final int index = 2 * i + 2;

                            final int t = states.get(sequence[tokenID][index]);
                            final double prob = Double.parseDouble(sequence[tokenID][index + 1]);

                            tokenTags[i] = t;
                            tokenProbs[i] = prob;

                            c.increment(t, output, prob);
                            c.increment(t, prob);

                            // If first token, add to initial transition probs,
                            // otherwise add to transition probs.
                            if (tokenID == 0) {
                                c.incrementInitial(t, prob);
                            }
                            else {
                                for (int j = 0; j < prevTags.length; j++)
                                    c.increment(prevTags[j], t, prob * prevProbs[j]);
                            }

                            if (tokenID == sequence.length - 1)
                                c.incrementFinal(t, prob);

                        }
                        prevTags = tokenTags;
                        prevProbs = tokenProbs;
                    }

                }
                else {
                    int prevTag = -1;
                    for (int tokenID = 0; tokenID < sequence.length; tokenID++) {
                        String output = sequence[tokenID][0];
                        c.increment(output, amount);

                        int t = states.get(sequence[tokenID][1]);
                        c.increment(t, output, amount);
                        c.increment(t, amount);

                        // If first token, add to initial transition probs,
                        // otherwise add to transition probs.
                        if (tokenID == 0)
                            c.incrementInitial(t, amount);
                        else
                            c.increment(prevTag, t, amount);

                        prevTag = t;
                    }

                    if (prevTag > -1)
                        c.incrementFinal(prevTag, amount);
                }

                sequence = inputReader.nextSequence();
            }
        }
        catch (EOFException e) {
            inputReader.close();
        }
    }

    // Train with the forward-backward algorithm. For each iteration,
    // runs each sentence (sequence) separately, collating the results
    // over all sequences. See Rabiner tutorial for details.
    public void forwardBackward(Counts c, Counts cEmpty) {
        LOG.info("\tRunning forward-backward...");

        try {

            File rawEvents = new File(rawFile);

            // Go through the data and bring it all into memory.
            DataReader inputReader = getDataReader(rawEvents);

            List<String[]> sequenceAccumulator = new ArrayList<String[]>();
            try {
                while (true) {
                    sequenceAccumulator.add(inputReader.nextOutputSequence());
                }
            }
            catch (EOFException e) {
                inputReader.close();
            }

            String[][] allSequences = new String[sequenceAccumulator.size()][];
            sequenceAccumulator.toArray(allSequences);
            sequenceAccumulator = null;

            int numSequences = allSequences.length;

            // Bring possible tags and p(w|t) into memory for faster
            // forward-backward. The downside is that this requires a
            // fair amount of memory.

            // First get values for unknown words
            int[] unknownTags = new int[numStates];
            double[] unknownProbs = new double[numStates];
            IntDoublePair unknownInfo = pEmission.getUnknown().getFirst();
            for (int counter = 0; counter < numStates; counter++) {
                unknownTags[counter] = unknownInfo.intValue;
                unknownProbs[counter] = unknownInfo.doubleValue;
                unknownInfo = (IntDoublePair) unknownInfo.getNext();
            }

            // Now get values for all the rest, leaving empty lists
            // where there are unknowns, for which we'll later use the
            // unknownTags and unknownProbs arrays. This saves a lot
            // of memory, especially in supertagging.
            int[][][] possibleTags = new int[numSequences][][];
            double[][][] probWordGivenTag = new double[numSequences][][];
            for (int itemID = 0; itemID < numSequences; itemID++) {
                String[] tokens = allSequences[itemID];
                possibleTags[itemID] = new int[tokens.length][];
                probWordGivenTag[itemID] = new double[tokens.length][];
                for (int tokenID = 0; tokenID < tokens.length; tokenID++) {
                    TLinkedList<IntDoublePair> valid = pEmission.getStrict(tokens[tokenID]);
                    if (null != valid) {
                        final int numTags = valid.size();
                        possibleTags[itemID][tokenID] = new int[numTags];
                        probWordGivenTag[itemID][tokenID] = new double[numTags];
                        IntDoublePair info = valid.getFirst();
                        for (int counter = 0; counter < numTags; counter++) {
                            possibleTags[itemID][tokenID][counter] = info.intValue;
                            probWordGivenTag[itemID][tokenID][counter] = info.doubleValue;
                            info = (IntDoublePair) info.getNext();
                        }
                    }
                    else {
                        possibleTags[itemID][tokenID] = unknownTags;
                        probWordGivenTag[itemID][tokenID] = unknownProbs;
                    }
                }
            }

            // Start iterating EM
            double previousLogProb = MathUtil.LOG_ZERO;
            for (int iter = 0; iter < numIterations; iter++) {

                // Counts cnew = c.copy();
                Counts cnew = cEmpty.copy();

                double totalProbForK = 0.0;
                for (int itemID = 0; itemID < numSequences; itemID++) {
                    totalProbForK += emForSequence(allSequences[itemID], cnew, possibleTags[itemID],
                            probWordGivenTag[itemID]);
                }

                double averageLogProb = totalProbForK / numSequences;
                // LOG.debug("\t"+ iter + ": " + averageLogProb);
                LOG.info("\t" + iter + ": " + averageLogProb);

                // Reset model parameters based on the maximization
                cnew.prepare();
                pInitial = cnew.getInitialLogDist();
                pFinal = cnew.getFinalLogDist();
                pTransition = cnew.getTransitionLogDist();
                pEmission = cnew.getEmissionLogDist();

                if (averageLogProb - previousLogProb < tolerance) {
                    if (averageLogProb < previousLogProb) {
                        LOG.info("DIVERGED: log probability decreased!!");
                    }
                    else {
                        LOG.info("DONE: Change in average log probability is less than " + tolerance);
                    }
                    break;
                }
                previousLogProb = averageLogProb;

                // First get values for unknown words
                unknownProbs = new double[numStates];
                unknownInfo = pEmission.getUnknown().getFirst();
                for (int counter = 0; counter < numStates; counter++) {
                    unknownProbs[counter] = unknownInfo.doubleValue;
                    unknownInfo = (IntDoublePair) unknownInfo.getNext();
                }

                for (int itemID = 0; itemID < numSequences; itemID++) {
                    String[] tokens = allSequences[itemID];
                    for (int tokenID = 0; tokenID < tokens.length; tokenID++) {
                        // TLinkedList<IntDoublePair> valid =
                        // pEmission.get(tokens[tokenID]);
                        TLinkedList<IntDoublePair> valid = pEmission.getStrict(tokens[tokenID]);
                        if (null != valid) {
                            final int numTags = probWordGivenTag[itemID][tokenID].length;
                            IntDoublePair info = valid.getFirst();
                            for (int counter = 0; counter < numTags; counter++) {
                                probWordGivenTag[itemID][tokenID][counter] = info.doubleValue;
                                info = (IntDoublePair) info.getNext();
                            }
                        }
                        else {
                            probWordGivenTag[itemID][tokenID] = unknownProbs;
                        }
                    }
                }

            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private double emForSequence(String[] tokens, Counts cnew, int[][] possibleTags, double[][] pWGT) {

        final int numTokens = tokens.length;

        // /////////////////////////////////////////////////////////////////////////////
        // E Step
        // /////////////////////////////////////////////////////////////////////////////

        // /////////////////////////////////////////////////////////////////////////////
        // calculate forward probabilities
        // /////////////////////////////////////////////////////////////////////////////
        double[][] forward = new double[numTokens][numStates];

        final int lastTokenID = numTokens - 1;

        Arrays.fill(forward[0], MathUtil.LOG_ZERO);

        for (int jCounter = 0; jCounter < possibleTags[0].length; jCounter++) {
            final int jStateID = possibleTags[0][jCounter];
            forward[0][jStateID] = MathUtil.elogProduct(pInitial[jStateID], pWGT[0][jCounter]);
        }

        for (int tokenID = 1; tokenID < numTokens; tokenID++) {

            Arrays.fill(forward[tokenID], MathUtil.LOG_ZERO);
            final int iTokenID = tokenID - 1;

            for (int jCounter = 0; jCounter < possibleTags[tokenID].length; jCounter++) {
                final int jStateID = possibleTags[tokenID][jCounter];
                for (int iCounter = 0; iCounter < possibleTags[iTokenID].length; iCounter++) {
                    final int iStateID = possibleTags[iTokenID][iCounter];
                    forward[tokenID][jStateID] = MathUtil.elogSum(forward[tokenID][jStateID], MathUtil.elogProduct(
                            forward[iTokenID][iStateID], pTransition[iStateID][jStateID], pWGT[tokenID][jCounter]));
                }
            }
        }

        double logForwardProb = MathUtil.LOG_ZERO;
        for (int jCounter = 0; jCounter < possibleTags[lastTokenID].length; jCounter++) {
            final int jStateID = possibleTags[lastTokenID][jCounter];
            logForwardProb = MathUtil.elogSum(logForwardProb,
                    MathUtil.elogProduct(forward[lastTokenID][jStateID], pFinal[jStateID]));
        }

        // /////////////////////////////////////////////////////////////////////////////
        // calculate backward probabilities
        // /////////////////////////////////////////////////////////////////////////////

        double[][] backward = new double[numTokens][numStates];

        Arrays.fill(backward[lastTokenID], MathUtil.LOG_ZERO);
        for (int jCounter = 0; jCounter < possibleTags[lastTokenID].length; jCounter++) {
            final int jStateID = possibleTags[lastTokenID][jCounter];
            backward[lastTokenID][jStateID] = pFinal[jStateID];

        }

        for (int jCounter = 0; jCounter < possibleTags[lastTokenID].length; jCounter++) {
            final int jStateID = possibleTags[lastTokenID][jCounter];
            final double prob_t_at_final = MathUtil.eexp(MathUtil.elogProduct(forward[lastTokenID][jStateID],
                    backward[lastTokenID][jStateID], -logForwardProb, lambda));
            cnew.incrementFinal(jStateID, prob_t_at_final);
        }

        for (int tokenID = numTokens - 2; tokenID >= 0; tokenID--) {
            Arrays.fill(backward[tokenID], MathUtil.LOG_ZERO);

            final int jTokenID = tokenID + 1;
            for (int jCounter = 0; jCounter < possibleTags[jTokenID].length; jCounter++) {
                final int jStateID = possibleTags[jTokenID][jCounter];

                final double prob_t_at_j = MathUtil.eexp(MathUtil.elogProduct(forward[jTokenID][jStateID],
                        backward[jTokenID][jStateID], -logForwardProb, lambda));
                cnew.increment(jStateID, tokens[jTokenID], prob_t_at_j);
                cnew.increment(jStateID, prob_t_at_j);
                cnew.increment(tokens[jTokenID], prob_t_at_j);

                for (int iCounter = 0; iCounter < possibleTags[tokenID].length; iCounter++) {
                    final int iStateID = possibleTags[tokenID][iCounter];

                    final double prob = MathUtil.elogProduct(pTransition[iStateID][jStateID], pWGT[jTokenID][jCounter]);

                    backward[tokenID][iStateID] = MathUtil.elogSum(backward[tokenID][iStateID],
                            MathUtil.elogProduct(backward[jTokenID][jStateID], prob));

                    cnew.increment(iStateID, jStateID, MathUtil.eexp(MathUtil.elogProduct(forward[tokenID][iStateID],
                            prob, backward[jTokenID][jStateID], -logForwardProb, lambda)));
                }
            }
        }

        double logBackwardProb = MathUtil.LOG_ZERO;

        for (int jCounter = 0; jCounter < possibleTags[0].length; jCounter++) {
            final int jStateID = possibleTags[0][jCounter];

            logBackwardProb = MathUtil.elogSum(logBackwardProb,
                    MathUtil.elogProduct(pInitial[jStateID], pWGT[0][jCounter], backward[0][jStateID]));

            final double prob_t_at_initial = MathUtil.eexp(MathUtil.elogProduct(forward[0][jStateID],
                    backward[0][jStateID], -logForwardProb, lambda));
            cnew.incrementInitial(jStateID, prob_t_at_initial);
            cnew.increment(jStateID, tokens[0], prob_t_at_initial);
            cnew.increment(tokens[0], prob_t_at_initial);

        }

        // double backwardProb = MathUtil.eexp(logBackwardProb);

        // /////////////////////////////////////////////////////////////////////////////
        // Check that forward and backward probs are within a small tolerance.
        // /////////////////////////////////////////////////////////////////////////////
        if (Math.abs(logForwardProb - logBackwardProb) > .0001) {
            LOG.info("WARNING: forward and backward probs different!");
            LOG.info("\tForward: " + logForwardProb);
            LOG.info("\tBackward: " + logBackwardProb);
        }

        // LOG.debug(StringUtil.join(tokens));
        // LOG.debug("\tForward: " + logForwardProb);

        // double perplexity = Math.exp(-1*(logForwardProb/(numTokens)));
        // LOG.debug("\tPerplexity per word: " + perplexity);

        return logForwardProb;
    }

}
