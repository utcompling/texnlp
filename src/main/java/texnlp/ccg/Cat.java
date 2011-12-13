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

import java.util.Set;

/**
 * Abstract category class to group properties shared by subtypes of categories,
 * like atomic and complex categories.
 * 
 * @author Jason Baldridge
 * @version $Revision: 1.53 $, $Date: 2006/10/12 21:20:44 $
 */
public abstract class Cat {

    public static char S = 's';
    public static char N = 'n';

    public int arity = 0;

    public abstract boolean equals(Object c);

    public abstract boolean unifies(Object c);

    public abstract int collectCats(Set<AtomCat> atomcats, Set<Cat> allcats);

    public abstract String toString();

    public abstract AtomCat getRootCat();

    public boolean notSeekingDir(boolean dir) {
        return true;
    }

    public int hashCode() {
        return toString().hashCode();
    }

    public static String changeNonCats(String c) {
        if (c.equals(","))
            return "NP[comma]\\NP";
        if (c.equals("."))
            return "S[punc]\\S";
        if (c.equals(";"))
            return "(S[punc]\\S)/S";
        // if (c.equals("LRB"))
        // return "S[punc]/S";
        // if (c.equals("RRB"))
        // return "S[RRB]\\S";
        return c;
    }

}
