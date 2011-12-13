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

import gnu.trove.THashSet;

/**
 * A small, pre-defined lexicon for testing purposes.
 * 
 * @author Jason Baldridge
 * @version $Revision: 1.53 $, $Date: 2006/10/12 21:20:44 $
 */
public class TestLexicon extends Lexicon {

    public TestLexicon() {

        Cat s = new AtomCat(Cat.S);
        Cat n = new AtomCat(Cat.N);
        Cat intrans = new ComplexCat(s, new Slash(Slash.L), n);
        Cat trans = new ComplexCat(intrans, new Slash(Slash.R), n);
        Cat sentcomp = new ComplexCat(intrans, new Slash(Slash.R), s);
        Cat postnmod = new ComplexCat(n, new Slash(Slash.L), n);
        Cat subjrel = new ComplexCat(postnmod, new Slash(Slash.R), intrans);

        defaultCats = new THashSet<Cat>();
        defaultCats.add(intrans);
        defaultCats.add(trans);
        defaultCats.add(n);

        addEntry("Calvin", n);
        addEntry("Hobbes", n);
        addEntry("Susie", n);
        addEntry("boy", n);
        addEntry("girl", n);
        addEntry("tiger", n);

        addEntry("walks", intrans);
        addEntry("sees", trans);
        addEntry("thinks", sentcomp);

        addEntry("who", subjrel);

    }

}
