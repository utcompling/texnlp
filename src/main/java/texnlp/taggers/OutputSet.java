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

/**
 * A Hashset that keeps track of outputs, and handles unknowns.
 * 
 * @author Jason Baldridge
 * @version $Revision: 1.53 $, $Date: 2006/10/12 21:20:44 $
 */
public class OutputSet extends THashSet<String> {
    protected static final String UNKNOWN = "__UNKNOWN__";
    protected static final String UNKNOWN_LC = "__UNKNOWN_LC__";
    protected static final String UNKNOWN_UC = "__UNKNOWN_UC__";
    protected static final String UNKNOWN_DIG = "__UNKNOWN_DIG__";

    public OutputSet() {
        add(UNKNOWN);
        add(UNKNOWN_LC);
        add(UNKNOWN_UC);
        add(UNKNOWN_DIG);
    }

    public String get(String s) {
        if (contains(s)) {
            return s;
        }

        // do a *rough* characterization of what kind of unknown we have
        char firstChar = s.charAt(0);
        if (Character.isLowerCase(firstChar)) {
            return UNKNOWN_LC;
        }
        else if (Character.isUpperCase(firstChar)) {
            return UNKNOWN_UC;
        }
        else if (Character.isDigit(firstChar)) {
            return UNKNOWN_DIG;
        }
        else {
            return UNKNOWN;
        }
    }

}
