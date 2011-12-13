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

import java.util.*;

/**
 * A complex category, like (s\np)/np.
 *
 * @author  Jason Baldridge
 * @version $Revision: 1.53 $, $Date: 2006/10/12 21:20:44 $
 */
public class ComplexCat extends Cat {

    public Cat res;
    public Slash sl;
    public Cat arg;

    public ComplexCat (Cat res, Slash sl, Cat arg) {
	this.res = res;
	this.sl = sl;
	this.arg = arg;
	arity = res.arity+1;
    }

    public int collectCats(Set<AtomCat> atomcats, Set<Cat> allcats) {
	allcats.add(this);

	int resSubcats = res.collectCats(atomcats, allcats);
	int argSubcats = arg.collectCats(atomcats, allcats);
	return 1 + resSubcats + argSubcats;
    }

    public AtomCat getRootCat() {
	if (res instanceof AtomCat)
	    return (AtomCat)res;
	else 
	    return ((ComplexCat)res).getRootCat();
    }


    public boolean notSeekingDir(boolean dir) {
	if (sl.hasDir(dir)) {
	    return false;
	} else {
	    return res.notSeekingDir(dir);
	}
    }

    public boolean equals (Object c) {
	if (c instanceof ComplexCat)
	    return (sl.equals(((ComplexCat)c).sl)
		    && arg.equals(((ComplexCat)c).arg)
		    && res.equals(((ComplexCat)c).res));
	else
	    return false;
    }

    public boolean unifies (Object c) {
	if (c instanceof ComplexCat)
	    return (sl.equals(((ComplexCat)c).sl)
		    && arg.unifies(((ComplexCat)c).arg)
		    && res.unifies(((ComplexCat)c).res));
	else
	    return false;
    }

    public String toString () {
	StringBuilder sb = new StringBuilder();
	if (res instanceof ComplexCat)
	    sb.append("("+res.toString()+")");
	else
	    sb.append(res.toString());

	sb.append(sl.toString());

	if (arg instanceof ComplexCat)
	    sb.append("("+arg.toString()+")");
	else
	    sb.append(arg.toString());

	return sb.toString();
    }

}
