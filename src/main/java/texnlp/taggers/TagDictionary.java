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

import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectObjectProcedure;

import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import texnlp.io.DataReader;

/**
 * A tag dictionary.
 * 
 * @author Jason Baldridge
 * @version $Revision: 1.53 $, $Date: 2006/10/12 21:20:44 $
 */
// public class TagDictionary extends THashMap<String, TIntDoubleHashMap> {
public class TagDictionary {
    // private double threshold = 0.1;
    private double threshold = 0.0;

    THashMap<String, TIntDoubleHashMap> collectDict = new THashMap<String, TIntDoubleHashMap>();

    int[] defaults;
    THashMap<String, int[]> dict;

    public TagDictionary() {
    }

    public TagDictionary(DataReader inputReader, TObjectIntHashMap<String> states) throws IOException {

        String[] token = inputReader.nextToken();
        try {
            while (true) {
                String tag = token[1];
                if (!tag.equals(Tagger.UNLABELED_TAG))
                    addTagForWord(token[0], states.get(tag));
                token = inputReader.nextToken();
            }
        }
        catch (EOFException e) {
            inputReader.close();
        }

        finalize(states.size());
    }

    public void finalize(int numTags) {
        // Don't do anything if we already finalized it (not clean,
        // but no time to make it pretty for all tagging models now.
        if (defaults != null)
            return;

        // applyThreshold();

        defaults = new int[numTags];
        for (int i = 0; i < numTags; i++)
            defaults[i] = i;

        int numTypes = collectDict.size();
        dict = new THashMap<String, int[]>(numTypes);
        collectDict.forEachEntry(new TObjectObjectProcedure<String, TIntDoubleHashMap>() {
            public boolean execute(String s, TIntDoubleHashMap m) {
                Arrays.sort(m.keys());
                dict.put(s, m.keys());
                return true;
            }
        });
        collectDict = null;
        dict.trimToSize();
    }

    public void addTagForWord(String word, int tag) {
        if (!collectDict.containsKey(word))
            collectDict.put(word, new TIntDoubleHashMap());
        collectDict.get(word).adjustOrPutValue(tag, 1.0, 1.0);
    }

    public void addTagForWord(String word, int tag, double amount) {
        if (!collectDict.containsKey(word))
            collectDict.put(word, new TIntDoubleHashMap());
        collectDict.get(word).adjustOrPutValue(tag, amount, amount);
    }

    public boolean containsWord(String word) {
        return dict.containsKey(word);
    }

    public int[] getTags(String word) {
        int[] tags = dict.get(word);
        if (null == tags)
            return defaults;
        return tags;
    }

    public Set<String> getWords() {
        return dict.keySet();
    }

    public void setThreshold(double cutoff) {
        threshold = cutoff;
    }

    public int[] getNumWordsForTags(int numStates) {
        int[] numWordsForTag = new int[numStates];

        for (Iterator<String> it = collectDict.keySet().iterator(); it.hasNext();) {
            String word = it.next();
            for (TIntDoubleIterator tagit = collectDict.get(word).iterator(); tagit.hasNext();) {
                tagit.advance();
                numWordsForTag[tagit.key()]++;
            }
        }
        return numWordsForTag;
    }

    public THashMap<String, TIntDoubleHashMap> getCollectionDictionary() {
        return collectDict;
    }

    public void applyThreshold() {
        if (threshold == 0.0)
            return;

        for (Iterator<String> it = collectDict.keySet().iterator(); it.hasNext();) {
            final String word = it.next();

            double total = 0.0;
            for (TIntDoubleIterator tagit = collectDict.get(word).iterator(); tagit.hasNext();) {
                tagit.advance();
                total += tagit.value();
            }

            for (TIntDoubleIterator tagit = collectDict.get(word).iterator(); tagit.hasNext();) {
                tagit.advance();
                if (tagit.value() / total < threshold) {
                    tagit.remove();
                }
            }
        }
    }

    public void applyThresholdFair() {
        if (threshold == 0.0)
            return;

        TIntIntHashMap numWordsOccurredWith = new TIntIntHashMap();
        for (Iterator<String> it = collectDict.keySet().iterator(); it.hasNext();) {
            final String word = it.next();
            for (TIntDoubleIterator tagit = collectDict.get(word).iterator(); tagit.hasNext();) {
                tagit.advance();
                numWordsOccurredWith.adjustOrPutValue(tagit.key(), 1, 1);
            }
        }

        int numRemoved = 0;
        for (Iterator<String> it = collectDict.keySet().iterator(); it.hasNext();) {
            final String word = it.next();

            for (TIntDoubleIterator tagit = collectDict.get(word).iterator(); tagit.hasNext();) {
                tagit.advance();
                if (numWordsOccurredWith.get(tagit.key()) < threshold && collectDict.get(word).size() > 1) {
                    tagit.remove();
                    numRemoved++;
                }
            }
        }
        System.out.println("** " + numRemoved);

    }

}
