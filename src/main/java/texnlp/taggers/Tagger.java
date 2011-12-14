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
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.THashSet;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import texnlp.io.Conll2kReader;
import texnlp.io.DataReader;
import texnlp.io.HashSlashReader;
import texnlp.io.PipeSepReader;
import texnlp.util.TaggerOptions;

/**
 * A Tagger (possible sequence based or just a labeler).
 * 
 * @author Jason Baldridge
 * @version $Revision: 1.53 $, $Date: 2006/10/12 21:20:44 $
 */
public abstract class Tagger {

    public static final String UNLABELED_TAG = "???";
    protected File outputDir;
    protected String dataIOType = "pipe";
    protected TagDictionary tagDictionary = new TagDictionary();
    protected TObjectIntHashMap<String> states;
    protected String[] stateNames;
    protected int numStates = 0;
    protected TObjectIntHashMap<String> typeFrequencies = new TObjectIntHashMap<String>();
    protected THashSet<String> highFrequencyWords;
    protected int numTokens = 0;
    protected boolean useMultitags = false;

    protected Tagger(TaggerOptions taggerOptions) {
        this.outputDir = taggerOptions.getOutputDir();
        final String tagsetFile = taggerOptions.getTagsetFile();
        setDataIOType(taggerOptions.getFormat());

        if (!tagsetFile.equals("")) {
            states = new TObjectIntHashMap<String>();

            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(tagsetFile)));

                int stateID = 0;
                String line = null;
                while ((line = br.readLine()) != null)
                    states.put(line.trim(), stateID++);

                states.trimToSize();

                numStates = stateID;
                stateNames = new String[numStates];
                for (TObjectIntIterator<String> it = states.iterator(); it.hasNext();) {
                    it.advance();
                    stateNames[it.value()] = it.key();
                }

            }
            catch (IOException e) {
                throw new RuntimeException("Could not open tagset file: " + tagsetFile, e);
            }
        }
    }

    public abstract void train();

    public abstract String[] tagSentence(String[] tokens, TagResults results);

    public Vector<String[]> tagSentenceIte(String[] tokens, TagResults results) {
        throw new RuntimeException(getClass().getName() + " has not yet been implemented");
    }

    // Can be overridden to add in unknown tokens, etc.
    protected String[] normalizeTokens(String[] tokens) {
        return tokens;
    }

    public void setDataIOType(String type) {
        dataIOType = type;
    }

    public void setTagDictionary(String dictfile) {
        try {
            tagDictionary = new TagDictionary(getDataReader(new File(dictfile)), states);
        }
        catch (IOException e) {
            throw new RuntimeException("Unable to load tag dictionary from: " + dictfile, e);
        }

    }

    protected DataReader getDataReader(File f) throws IOException {
        if (dataIOType.equals("hashslash"))
            return new HashSlashReader(f);

        else if (dataIOType.equals("pipe"))
            return new PipeSepReader(f);

        else if (dataIOType.equals("tab"))
            return new Conll2kReader(f);
        
        else
            throw new RuntimeException("Unexpected data format: " + dataIOType);
    }

    // Tag an entire file
    public void tagFile(TagResults results) {
        try {
            DataReader inputReader = getDataReader(results.getInput());
            try {
                while (true) {
                    String[] toks = inputReader.nextOutputSequence();
                    // LOG.debug(StringUtil.join(toks));
                    String[] tags = tagSentence(toks, results);
                    results.addTags(toks, tags);

                }
            }
            catch (EOFException e) {
                inputReader.close();
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Error reading file for tagging:", e);
        }
    }

    // Tag an entire file for use in ITE
    public Vector<String[][]> tagFileIte(TagResults results) {
        Vector<String[][]> sentences = new Vector<String[][]>();
        try {
            DataReader inputReader = getDataReader(results.getInput());
            try {
                while (true) {
                    String[] toks = inputReader.nextOutputSequence();
                    String[] tags = tagSentence(toks, results);
                    // generate output for active learning code
                    results.addTags(toks, tags);
                    // generate output for ite
                    Vector<String[]> tagsIte = tagSentenceIte(toks, results);
                    sentences.add(results.addTagsIte(toks, tagsIte));
                }
            }
            catch (EOFException e) {
                inputReader.close();
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Error reading file for tagging:", e);
        }
        return sentences;
    }

    /**
     * Initialize global data structures based on first scan of the input.
     */
    protected void scanFile(File events) throws IOException {

        DataReader inputReader = getDataReader(events);
        try {
            String[] token = inputReader.nextToken();

            // populate the tagset if one hasn't been supplied, otherwise use
            // the given one
            if (numStates == 0) {

                states = new TObjectIntHashMap<String>();
                int stateIndex = 0;

                try {

                    while (true) {
                        if (!useMultitags && token.length > 2 && Character.isDigit(token[1].charAt(0)))

                            useMultitags = true;

                        int firstTagIndex = 1;
                        if (useMultitags)
                            firstTagIndex = 2;

                        boolean wasTagged = false;
                        for (int i = firstTagIndex; i < token.length; i += 2) {
                            String tag = token[i];

                            if (!tag.equals(Tagger.UNLABELED_TAG)) {
                                wasTagged = true;
                                if (!states.containsKey(tag)) {
                                    states.put(tag, stateIndex);
                                    stateIndex++;
                                }

                                int stateID = states.get(tag);
                                tagDictionary.addTagForWord(token[0], stateID);
                                // LOG.debug(token[0]+"::"+stateID+" ");
                            }
                        }
                        if (wasTagged) {
                            typeFrequencies.adjustOrPutValue(token[0], 1, 1);
                            numTokens++;
                        }
                        token = inputReader.nextToken();
                    }

                }
                catch (EOFException e) {
                    inputReader.close();
                }

                numStates = stateIndex;

                states.trimToSize();
                stateNames = new String[numStates];
                for (TObjectIntIterator<String> it = states.iterator(); it.hasNext();) {
                    it.advance();
                    stateNames[it.value()] = it.key();
                }

            }
            else {

                try {
                    while (true) {
                        if (!useMultitags && token.length > 2 && Character.isDigit(token[1].charAt(0)))

                            useMultitags = true;

                        int firstTagIndex = 1;
                        if (useMultitags)
                            firstTagIndex = 2;

                        boolean wasTagged = false;
                        for (int i = firstTagIndex; i < token.length; i += 2) {
                            String tag = token[i];

                            if (!tag.equals(Tagger.UNLABELED_TAG)) {
                                wasTagged = true;
                                if (states.containsKey(tag))
                                    tagDictionary.addTagForWord(token[0], states.get(tag));
                            }
                        }
                        if (wasTagged) {
                            typeFrequencies.adjustOrPutValue(token[0], 1, 1);
                            numTokens++;
                        }

                        token = inputReader.nextToken();
                    }

                }
                catch (EOFException e) {
                    inputReader.close();
                }
            }
        }
        catch (EOFException e) {
            inputReader.close();
        }
    }

    // handle frequency relative cutoffs for feature extraction, finalize the
    // tag dictionary
    protected void prepare() {

        final int numTypes = typeFrequencies.size();
        final double averageFreq = (double) numTokens / numTypes;

        highFrequencyWords = new THashSet<String>(numTypes);
        TObjectIntIterator<String> it = typeFrequencies.iterator();
        for (int i = numTypes; i-- > 0;) {
            it.advance();
            if (it.value() > averageFreq)
                highFrequencyWords.add(it.key());
        }

        typeFrequencies = null;
        highFrequencyWords.trimToSize();

        tagDictionary.finalize(numStates);
    }

    public class PairIte implements Comparable {

        public String token;
        public double prob;

        public int compareTo(Object other) {
            if (prob == ((PairIte) other).prob) {
                return token.compareToIgnoreCase(((PairIte) other).token);
            }
            else if (prob > ((PairIte) other).prob) {
                return -1;
            }
            else {
                return 1;
            }
        }
    }

}
