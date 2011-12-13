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

import gnu.trove.THashSet;

import java.util.Arrays;

import texnlp.estimate.AggressiveWordContextGenerator;
import texnlp.estimate.CCWordContextGenerator;
import texnlp.estimate.WordContextGenerator;
import texnlp.util.TaggerOptions;

/**
 * A Maximum Entropy tagger.
 * 
 * @author Jason Baldridge
 * @version $Revision: 1.53 $, $Date: 2006/10/12 21:20:44 $
 */
public abstract class MaxentTagger extends Tagger {
    protected int maxIterations = 0;
    protected int numMachines;
    protected double variance;

    protected WordContextGenerator wcgen;

    // protected WordContextGenerator wcgen = new CCWordContextGenerator();
    // protected WordContextGenerator wcgen = new
    // AggressiveWordContextGenerator();

    public MaxentTagger(TaggerOptions taggerOptions) {
        super(taggerOptions);
        this.maxIterations = taggerOptions.getNumIterations();
        this.numMachines = taggerOptions.getNumMachines();
        final double variance = taggerOptions.getPriorAmount();
        if (variance == 0.0) {
            this.variance = 1000.0;
        }
        else {
            this.variance = variance;
        }

        final String contextGenOption = taggerOptions.getContextGen();
        if (contextGenOption.equals("word"))
            this.wcgen = new CCWordContextGenerator();
        else if (contextGenOption.equals("aggressive"))
            this.wcgen = new AggressiveWordContextGenerator();
        else
            throw new RuntimeException("Unknown word context generator: " + contextGenOption);
    }

    public void setWordContextGenerator(WordContextGenerator wcg) {
        wcgen = wcg;
    }

    protected String[][] prepareForSentence(String[] tokens) {
        String[][] wordContexts = new String[tokens.length][];
        for (int tokenID = 0; tokenID < tokens.length; tokenID++) {
            // if (highFrequencyWords.contains(tokens[tokenID])) {
            // String[] singleWordContext = { tokens[tokenID] };
            // wordContexts[tokenID] = singleWordContext;
            // } else {
            // wordContexts[tokenID] = wcgen.getWordContexts(tokens[tokenID]);
            // }
            wordContexts[tokenID] = wcgen.getWordContexts(tokens[tokenID]);
        }

        String[][] cachedWordFeatures = new String[tokens.length][];
        for (int tokenID = 0; tokenID < tokens.length; tokenID++) {
            int index = wordContexts[tokenID].length;
            // String[] contexts = Arrays.copyOf(wordContexts[tokenID],
            // index+2);
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
        return cachedWordFeatures;
    }

    protected void addSpecificContexts(String identifier, String[] wordContexts, THashSet<String> fullContexts) {
        for (int contextID = 0; contextID < wordContexts.length; contextID++)
            fullContexts.add(identifier + wordContexts[contextID]);
    }

}
