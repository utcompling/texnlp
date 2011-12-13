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
 * An atomic category, like S or NP.
 * 
 * @author Jason Baldridge
 * @version $Revision: 1.53 $, $Date: 2006/10/12 21:20:44 $
 */
public class AtomCat extends Cat {

    public String val;
    public String feature = "";

    public AtomCat(String val) {
        this.val = val;
    }

    public AtomCat(String val, String feature) {
        this.val = val;
        this.feature = feature;
    }

    public AtomCat(char val) {
        this.val = Character.toString(val);
    }

    public AtomCat getRootCat() {
        return this;
    }

    public int collectCats(Set<AtomCat> atomcats, Set<Cat> allcats) {
        atomcats.add(this);
        allcats.add(this);
        return 1;
    }

    public boolean equals(Object c) {
        if (c instanceof AtomCat)
            return (val.equals(((AtomCat) c).val) && feature.equals(((AtomCat) c).feature));
        return false;
    }

    public boolean unifies(Object c) {
        if (c instanceof AtomCat) {
            if (matches(val, ((AtomCat) c).val)) {
                if (feature.isEmpty() || ((AtomCat) c).feature.isEmpty())
                    return true;
                else
                    return feature.equals(((AtomCat) c).feature);
            }
        }
        return false;
    }

    private static boolean matches(String s1, String s2) {
        return (s1.equals(s2) || (s1.equals("NP") && s2.equals("N")));
        // || (s1.equals("N") && s2.equals("NP")));
    }

    public String toString() {
        if (feature.isEmpty())
            return val;
        else
            return val + "[" + feature + "]";
    }

}
