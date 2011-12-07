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

import gnu.trove.*;
import texnlp.util.*;

/**
 * A simple log-space counter for counting emission probabilities
 * during the maximization step of the forward-backward algorithm.
 *
 * @author  Jason Baldridge
 * @version $Revision: 1.53 $, $Date: 2006/10/12 21:20:44 $
 */
public class OutputCounter extends TObjectDoubleHashMap<String> {

    public OutputCounter () {}

    public double getCount (String s) {
	if (containsKey(s))
	    return get(s);
	else
	    return MathUtil.LOG_ZERO;
    }

    public void increment (String s, double val) {
	if (containsKey(s))
	    put(s, MathUtil.elogSum(get(s), val));
	else
	    put(s, val);
    }

    public String toString () {
	StringBuffer sb = new StringBuffer();
	for (TObjectDoubleIterator<String> it = iterator(); it.hasNext();) {
	    it.advance();
	    sb.append("\t" + it.key() + " -> " + MathUtil.eexp(it.value()) + "\n");
	}
	return sb.toString();
    }

}
