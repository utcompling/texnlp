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
package texnlp.apps;

import gnu.trove.THashMap;
import gnu.trove.THashSet;
import gnu.trove.TObjectIntHashMap;
import gnu.trove.TObjectIntIterator;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import texnlp.io.Conll2kReader;
import texnlp.io.DataReader;
import texnlp.io.PipeSepReader;
import texnlp.util.Constants;
import texnlp.util.IntStringPair;

/**
 * Score tagger output
 * 
 * @author Jason Baldridge
 * @version $Revision: 1.53 $, $Date: 2006/10/12 21:20:44 $
 */
public class Score {

    public static void score(String modelFile, String goldFile, String trainFile, String format, int column) {
        ScoreResults scoreResults = getScore(modelFile, goldFile, trainFile, format, column);
        System.out.println();
        System.out.println(scoreResults);
    }

    public static ScoreResults getScore(String modelFile, String goldFile, String trainFile, String format, int column) {

        List<String[][]> modelTags = slurpFile(modelFile, format);
        List<String[][]> goldTags = slurpFile(goldFile, format);
        // List<String[][]> trainTags = slurpFile(trainFile, format);

        THashSet<String> knownWords = getWords(trainFile, format);
        THashMap<String, Set<String>> trainLexicon = getActualLexicon(trainFile, format, column);

        THashSet<String> ambigWords = new THashSet<String>();
        THashSet<String> ignoreCats = new THashSet<String>();

        ignoreCats.add(".");
        ignoreCats.add(";");
        ignoreCats.add(":");
        ignoreCats.add(",");
        ignoreCats.add("LRB");
        ignoreCats.add("RRB");
        ignoreCats.add("");

        for (String word : trainLexicon.keySet()) {
            if (trainLexicon.get(word).size() > 1)
                ambigWords.add(word);
        }

        ScoreResults scoreResults = new ScoreResults();

        for (int i = 0; i < goldTags.size(); i++) {

            int sentValue = 0;
            String[][] goldSentence = goldTags.get(i);
            String[][] modelSentence = modelTags.get(i);

            for (int j = 0; j < goldSentence.length; j++) {
                if (!ignoreCats.contains(goldSentence[j][1])) {
                    int value = 0;
                    String gold = goldSentence[j][column];
                    String model = modelSentence[j][column];
                    if (gold.equals(model)) {
                        value = 1;
                        sentValue++;
                    }
                    else {
                        scoreResults.incErrors(goldSentence[j][column] + "\t" + modelSentence[j][column], 1, 1);
                    }

                    if (knownWords.contains(goldSentence[j][0])) {
                        scoreResults.incKnownCorrect(value);
                        scoreResults.incKnownTotal(1);
                    }
                    else {
                        scoreResults.incUnseenCorrect(value);
                        scoreResults.incUnseenTotal(1);
                    }

                    if (ambigWords.contains(goldSentence[j][0])) {
                        scoreResults.incAmbigCorrect(value);
                        scoreResults.incAmbigTotal(1);
                    }

                    scoreResults.incCorrect(value);
                    scoreResults.incTotal(1);
                }

            }

            if (sentValue == goldSentence.length)
                scoreResults.incSentenceCorrect(1);
            scoreResults.incSentenceTotal(1);
        }

        // scoreLexicon(modelFile, goldFile, trainFile, format, column);

        return scoreResults;
    }

    public static class ScoreResults {
        private int total = 0;
        private int correct = 0;
        private int unseenTotal = 0;
        private int unseenCorrect = 0;
        private int knownTotal = 0;
        private int knownCorrect = 0;
        private int sentenceTotal = 0;
        private int sentenceCorrect = 0;
        private int ambigCorrect = 0;
        private int ambigTotal = 0;
        private TObjectIntHashMap<String> errors = new TObjectIntHashMap<String>();

        public void incTotal(int i) {
            this.total += i;
        }

        public void incCorrect(int i) {
            this.correct += i;
        }

        public void incUnseenTotal(int i) {
            this.unseenTotal += i;
        }

        public void incUnseenCorrect(int i) {
            this.unseenCorrect += i;
        }

        public void incKnownTotal(int i) {
            this.knownTotal += i;
        }

        public void incKnownCorrect(int i) {
            this.knownCorrect += i;
        }

        public void incSentenceTotal(int i) {
            this.sentenceTotal += i;
        }

        public void incSentenceCorrect(int i) {
            this.sentenceCorrect += i;
        }

        public void incAmbigCorrect(int i) {
            this.ambigCorrect += i;
        }

        public void incAmbigTotal(int i) {
            this.ambigTotal += i;
        }

        public void incErrors(String s, int adjust_amount, int put_amount) {
            this.errors.adjustOrPutValue(s, adjust_amount, put_amount);
        }

        public double totalAccuracy() {
            return getAccuracy(correct, total);
        }

        public double sentenceAccuracy() {
            return getAccuracy(sentenceCorrect, sentenceTotal);
        }

        public double knownAccuracy() {
            return getAccuracy(knownCorrect, knownTotal);
        }

        public double unseenAccuracy() {
            return getAccuracy(unseenCorrect, unseenTotal);
        }

        public double ambigAccuracy() {
            return getAccuracy(ambigCorrect, ambigTotal);
        }

        private double getAccuracy(int correct, int total) {
            double accuracy = 0.0;
            if (total > 0)
                accuracy = correct / (double) total;
            return accuracy;
        }

        public int getTotal() {
            return total;
        }

        public int getCorrect() {
            return correct;
        }

        public int getUnseenTotal() {
            return unseenTotal;
        }

        public int getUnseenCorrect() {
            return unseenCorrect;
        }

        public int getKnownTotal() {
            return knownTotal;
        }

        public int getKnownCorrect() {
            return knownCorrect;
        }

        public int getSentenceTotal() {
            return sentenceTotal;
        }

        public int getSentenceCorrect() {
            return sentenceCorrect;
        }

        public int getAmbigCorrect() {
            return ambigCorrect;
        }

        public int getAmbigTotal() {
            return ambigTotal;
        }

        public TObjectIntHashMap<String> getErrors() {
            return errors;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append("Performance:\n");
            sb.append("-----------------------\n");
            // sb.append("\nK: "+getAccuracy(knownCorrect,knownTotal)
            // + "\tU: " + getAccuracy(unseenCorrect,unseenTotal)
            // + "\tT: " + getAccuracy(correct,total)
            // + "\tS: " + getAccuracy(sentenceCorrect,sentenceTotal));

            sb.append("Total:     " + getAccuracyString(correct, total) + "\n");
            sb.append("Sentence:  " + getAccuracyString(sentenceCorrect, sentenceTotal) + "\n");
            sb.append("-----------------------\n");
            sb.append("Knowns:    " + getAccuracyString(knownCorrect, knownTotal) + "\n");
            sb.append("Unknowns:  " + getAccuracyString(unseenCorrect, unseenTotal) + "\n");
            sb.append("Ambiguous: " + getAccuracyString(ambigCorrect, ambigTotal) + "\n");

            IntStringPair[] sortedErrors = new IntStringPair[errors.size()];
            int index = 0;
            for (TObjectIntIterator<String> it = errors.iterator(); it.hasNext();) {
                it.advance();
                sortedErrors[index++] = new IntStringPair(it.value(), it.key());
            }
            Arrays.sort(sortedErrors);
            sb.append("\nMost common errors:\n");
            sb.append("#Err\tGold\tModel\n-----------------------\n");
            for (int i = 0; i < 5 && i < sortedErrors.length; i++)
                sb.append(sortedErrors[i].intValue + "\t" + sortedErrors[i].stringValue + "\n");

            return sb.toString();
        }
    }

    public static String[][][] scoreIte(String modelFile, String goldFile, String trainFile, String format, int column) {

        List<String[][]> modelTags = slurpFile(modelFile, format);
        List<String[][]> goldTags = slurpFile(goldFile, format);

        THashSet<String> knownWords = getWords(trainFile, format);
        THashMap<String, Set<String>> trainLexicon = getActualLexicon(trainFile, format, column);

        THashSet<String> ambigWords = new THashSet<String>();
        THashSet<String> ignoreCats = new THashSet<String>();
        ignoreCats.add(".");
        ignoreCats.add(";");
        ignoreCats.add(":");
        ignoreCats.add(",");
        ignoreCats.add("LRB");
        ignoreCats.add("RRB");
        ignoreCats.add("");

        for (String word : trainLexicon.keySet()) {
            if (trainLexicon.get(word).size() > 1)
                ambigWords.add(word);
        }

        TObjectIntHashMap<String> errors = new TObjectIntHashMap<String>();

        int total = 0;
        int correct = 0;
        int unseenTotal = 0;
        int unseenCorrect = 0;
        int knownTotal = 0;
        int knownCorrect = 0;
        int sentenceTotal = 0;
        int sentenceCorrect = 0;

        int ambigCorrect = 0;
        int ambigTotal = 0;

        for (int i = 0; i < goldTags.size(); i++) {
            int sentValue = 0;
            String[][] goldSentence = goldTags.get(i);
            String[][] modelSentence = modelTags.get(i);

            for (int j = 0; j < goldSentence.length; j++) {
                if (!ignoreCats.contains(goldSentence[j][1])) {
                    int value = 0;
                    String gold = goldSentence[j][column];
                    String model = modelSentence[j][column];
                    if (gold.equals(model)) {
                        value = 1;
                        sentValue++;
                    }
                    else {
                        errors.adjustOrPutValue(goldSentence[j][column] + "\t" + modelSentence[j][column], 1, 1);
                    }

                    if (knownWords.contains(goldSentence[j][0])) {
                        knownCorrect += value;
                        knownTotal++;
                    }
                    else {
                        unseenCorrect += value;
                        unseenTotal++;
                    }

                    if (ambigWords.contains(goldSentence[j][0])) {
                        ambigCorrect += value;
                        ambigTotal++;
                    }

                    correct += value;
                    total++;
                }

            }

            if (sentValue == goldSentence.length)
                sentenceCorrect++;
            sentenceTotal++;
        }

        System.out.println("doing performance");
        String[][] Performance = new String[][] { getAccuracyIte(knownCorrect, knownTotal), getAccuracyIte(unseenCorrect, unseenTotal),
                getAccuracyIte(ambigCorrect, ambigTotal), getAccuracyIte(correct, total), getAccuracyIte(sentenceCorrect, sentenceTotal) };

        System.out.println("doing errors");
        IntStringPair[] sortedErrors = new IntStringPair[errors.size()];
        int index = 0;
        for (TObjectIntIterator<String> it = errors.iterator(); it.hasNext();) {
            it.advance();
            sortedErrors[index++] = new IntStringPair(it.value(), it.key());
        }
        Arrays.sort(sortedErrors);
        System.out.println("doing lex");
        String[][] Errors = new String[5][];
        for (int i = 0; i < 5 && i < sortedErrors.length; i++)
            Errors[i] = new String[] { "" + sortedErrors[i].intValue, sortedErrors[i].stringValue };

        String[][] lexScore = scoreLexiconIte(modelFile, goldFile, trainFile, format, column);

        return new String[][][] { Performance, Errors, lexScore };

    }

    public static String[][] scoreLexiconIte(String modelFile, String goldFile, String trainFile, String format, int column) {

        THashSet<String> modelLexicon = getLexicon(modelFile, format, column);
        THashSet<String> goldLexicon = getLexicon(goldFile, format, column);
        THashSet<String> trainLexicon = getLexicon(trainFile, format, column);

        modelLexicon.removeAll(trainLexicon);
        goldLexicon.removeAll(trainLexicon);

        int truePositives = 0;
        for (Iterator<String> it = modelLexicon.iterator(); it.hasNext();) {
            String entry = it.next();
            if (goldLexicon.contains(entry))
                truePositives++;
        }

        return new String[][] { getAccuracyIte(truePositives, modelLexicon.size()), getAccuracyIte(truePositives, goldLexicon.size()) };
    }

    public static void scoreLexicon(String modelFile, String goldFile, String trainFile, String format, int column) {

        THashSet<String> modelLexicon = getLexicon(modelFile, format, column);
        THashSet<String> goldLexicon = getLexicon(goldFile, format, column);
        THashSet<String> trainLexicon = getLexicon(trainFile, format, column);

        modelLexicon.removeAll(trainLexicon);
        goldLexicon.removeAll(trainLexicon);

        int truePositives = 0;
        for (Iterator<String> it = modelLexicon.iterator(); it.hasNext();) {
            String entry = it.next();
            if (goldLexicon.contains(entry))
                truePositives++;
        }

        System.out.println("\nLexicon performance:\n----------------------");
        System.out.println("P: " + getAccuracyString(truePositives, modelLexicon.size()));
        System.out.println("R: " + getAccuracyString(truePositives, goldLexicon.size()));

    }

    public static String getAccuracyString(int correct, int total) {
        double accuracy = 0.0;
        if (total > 0)
            accuracy = correct / (double) total;
        return Constants.PERCENT_FORMAT.format(accuracy) + "\t(" + correct + "/" + total + ")";
    }

    public static String[] getAccuracyIte(int correct, int total) {
        double accuracy = 0.0;
        if (total > 0)
            accuracy = correct / (double) total;
        return new String[] { Constants.PERCENT_FORMAT.format(accuracy), "(" + correct + "/" + total + ")" };
    }

    public static List<String[][]> slurpFile(String taggedFile, String format) {

        List<String[][]> items = new ArrayList<String[][]>();

        try {
            DataReader inputReader;
            if (format.toLowerCase().equals("pipe"))
                inputReader = new PipeSepReader(new File(taggedFile));
            else if (format.toLowerCase().equals("tab"))
                inputReader = new Conll2kReader(new File(taggedFile));
            else
                throw new RuntimeException("Unexpected format: " + format);
            try {
                while (true)
                    items.add(inputReader.nextSequence());
            }
            catch (EOFException e) {
                inputReader.close();
            }

        }
        catch (IOException e) {
            System.out.println("Error reading file: " + taggedFile);
            System.out.println(e);
        }
        return items;
    }

    public static THashSet<String> getWords(String file, String format) {

        THashSet<String> words = new THashSet<String>();

        try {
            DataReader inputReader;
            if (format.toLowerCase().equals("pipe"))
                inputReader = new PipeSepReader(new File(file));
            else if (format.toLowerCase().equals("tab"))
                inputReader = new Conll2kReader(new File(file));
            else
                throw new RuntimeException("Unexpected format: " + format);

            try {
                while (true)
                    words.add(inputReader.nextToken()[0]);
            }
            catch (EOFException e) {
                inputReader.close();
            }

        }
        catch (IOException e) {
            System.out.println("Error reading file: " + file);
            System.out.println(e);
        }
        return words;
    }

    public static THashMap<String, Set<String>> getActualLexicon(String file, String format, int column) {

        THashMap<String, Set<String>> lexicon = new THashMap<String, Set<String>>();

        try {
            DataReader inputReader;

            if (format.toLowerCase().equals("pipe"))
                inputReader = new PipeSepReader(new File(file));
            else if (format.toLowerCase().equals("tab"))
                inputReader = new Conll2kReader(new File(file));
            else
                throw new RuntimeException("Unexpected format: " + format);

            try {
                while (true) {
                    String[] token = inputReader.nextToken();

                    if (!lexicon.contains(token[0]))
                        lexicon.put(token[0], new THashSet<String>());
                    lexicon.get(token[0]).add(token[column]);
                }
            }
            catch (EOFException e) {
                inputReader.close();
            }

        }
        catch (IOException e) {
            System.out.println("Error reading file: " + file);
            System.out.println(e);
        }
        return lexicon;
    }

    public static THashSet<String> getLexicon(String file, String format, int column) {

        THashSet<String> lexicon = new THashSet<String>();

        try {
            DataReader inputReader;
            if (format.toLowerCase().equals("pipe"))
                inputReader = new PipeSepReader(new File(file));
            else if (format.toLowerCase().equals("tab"))
                inputReader = new Conll2kReader(new File(file));
            else
                throw new RuntimeException("Unexpected format: " + format);

            try {
                while (true) {
                    String[] token = inputReader.nextToken();
                    lexicon.add(token[0] + "/" + token[column]);
                }
            }
            catch (EOFException e) {
                inputReader.close();
            }

        }
        catch (IOException e) {
            System.out.println("Error reading file: " + file);
            System.out.println(e);
        }
        return lexicon;
    }

    public static void main(String[] args) {

        CommandLineParser optparse = new PosixParser();

        // create the Options
        Options options = new Options();
        options.addOption("m", "model", true, "the output file created by the model");
        options.addOption("r", "raw", true, "the raw training file (for determining seens)");
        options.addOption("t", "train", true, "the training file (for determining knowns)");
        options.addOption("g", "gold", true, "the gold file");
        options.addOption("f", "format", true, "the format to use (Tab, Pipe) [default: Tab]");
        options.addOption("c", "column", true, "the tag column to score (default: 1, which is the first tag after the token)");
        options.addOption("h", "help", false, "help");

        try {
            CommandLine cline = optparse.parse(options, args);

            if (cline.hasOption('h')) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("java texnlp.apps.Score", options);
                System.exit(0);
            }

            String modelFile = "";
            if (cline.hasOption('m'))
                modelFile = cline.getOptionValue('m');

            String goldFile = "";
            if (cline.hasOption('g'))
                goldFile = cline.getOptionValue('g');

            // String rawFile = "";
            // if (cline.hasOption('r'))
            // rawFile = cline.getOptionValue('r');

            String trainFile = "";
            if (cline.hasOption('t'))
                trainFile = cline.getOptionValue('t');

            String format = "Pipe";
            if (cline.hasOption('f'))
                format = cline.getOptionValue('f');

            int column = 1;
            if (cline.hasOption('c'))
                column = Integer.parseInt(cline.getOptionValue('c'));

            score(modelFile, goldFile, trainFile, format, column);

        }
        catch (ParseException exp) {
            System.out.println("Unexpected exception parsing command line options:" + exp.getMessage());
        }

    }

}