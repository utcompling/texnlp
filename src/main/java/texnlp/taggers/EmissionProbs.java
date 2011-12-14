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

import gnu.trove.list.linked.TLinkedList;
import gnu.trove.map.hash.THashMap;
import texnlp.util.IntDoublePair;

/**
 * HMM emission probabilities. Also doubles as a tag dictionary.
 * 
 * @author Jason Baldridge
 * @version $Revision: 1.53 $, $Date: 2006/10/12 21:20:44 $
 */
public class EmissionProbs {
    protected THashMap<String, TLinkedList<IntDoublePair>> pEmission;
    protected TLinkedList<IntDoublePair> unknownEmission;

    public EmissionProbs(THashMap<String, TLinkedList<IntDoublePair>> _pEmission,
            TLinkedList<IntDoublePair> _unknownEmission) {

        pEmission = _pEmission;
        unknownEmission = _unknownEmission;
    }

    public TLinkedList<IntDoublePair> get(String word) {
        if (pEmission.containsKey(word))
            return pEmission.get(word);
        else
            return unknownEmission;
    }

    public TLinkedList<IntDoublePair> getStrict(String word) {
        if (pEmission.containsKey(word))
            return pEmission.get(word);
        else
            return null;
    }

    public TLinkedList<IntDoublePair> getUnknown() {
        return unknownEmission;
    }

}
