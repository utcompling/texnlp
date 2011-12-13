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

import java.util.Comparator;

/**
 * A wrapper class storing a double and a Cat for sorting purposes.
 * 
 * @author Jason Baldridge
 * @version $Revision: 1.53 $, $Date: 2006/10/12 21:20:44 $
 */
public class PairDoubleCat {
    public double doubleValue;
    public Cat cat;

    public PairDoubleCat(double d, Cat c) {
        doubleValue = d;
        cat = c;
    }

    public String toString() {
        return String.valueOf(doubleValue) + " :: " + cat.toString();
    }

    public static Comparator<PairDoubleCat> CMP = new Comparator<PairDoubleCat>() {
        public int compare(PairDoubleCat x, PairDoubleCat y) {
            return Double.compare(x.doubleValue, y.doubleValue);
        }
    };
}
