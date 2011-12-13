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
package texnlp.ccg;

import gnu.trove.THashMap;
import gnu.trove.THashSet;

import java.util.Map;
import java.util.Set;

/**
 * A lexicon mapping each word to a set of categories.
 * 
 * @author Jason Baldridge
 * @version $Revision: 1.53 $, $Date: 2006/10/12 21:20:44 $
 */
public class Lexicon {

    Set<Cat> defaultCats;

    Map<String, Set<Sign>> words2cats = new THashMap<String, Set<Sign>>();

    public Lexicon() {
    }

    public Lexicon(Set<Cat> defaultCats) {
        this.defaultCats = defaultCats;
    }

    public void addEntry(String word, Cat c) {

        if (!words2cats.containsKey(word))
            words2cats.put(word, new THashSet<Sign>());

        words2cats.get(word).add(new Sign(word, c));
    }

    public Set<Sign> getEntries(String word) {
        if (!words2cats.containsKey(word))
            return getDefaultEntries(word);
        else
            return words2cats.get(word);

    }

    public Set<Sign> getDefaultEntries(String word) {
        Set<Sign> entries = new THashSet<Sign>();
        for (Cat c : defaultCats)
            entries.add(new Sign(word, c));

        return entries;
    }

}
