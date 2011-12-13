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

import gnu.trove.TObjectDoubleHashMap;
import gnu.trove.TObjectDoubleIterator;

import java.util.Set;

import texnlp.util.MathUtil;

/**
 * Maps objects to logarithmic values (of probabilities).
 * 
 * @author Jason Baldridge
 * @version $Revision: 1.53 $, $Date: 2006/10/12 21:20:44 $
 */
public class LogDistribution extends TObjectDoubleHashMap<String> {

    public LogDistribution() {
    }

    public LogDistribution(String firstObs) {
        observe(firstObs);
    }

    public double getLogProb(String s) {
        if (containsKey(s))
            return get(s);
        else
            return MathUtil.LOG_ZERO;
    }

    public void observe(String s) {
        adjustOrPutValue(s, 1.0, 1.0);
    }

    public void resetValue(String key, double val) {
        put(key, val);
    }

    public void makeUniform(Set<String> values) {
        double uniformProb = MathUtil.elog(1.0 / values.size());
        for (String key : values)
            put(key, uniformProb);
    }

    public void doneObserving() {
        double total = MathUtil.sum(_values);
        for (TObjectDoubleIterator<String> it = iterator(); it.hasNext();) {
            it.advance();
            it.setValue(MathUtil.elog(it.value() / total));
        }

    }

    public void checkSum() {
        double sum = 0.0;

        for (TObjectDoubleIterator<String> it = iterator(); it.hasNext();) {
            it.advance();
            sum += MathUtil.eexp(it.value());
        }
        System.out.println("Sums to: " + sum);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (TObjectDoubleIterator<String> it = iterator(); it.hasNext();) {
            it.advance();
            double val = MathUtil.eexp(it.value());
            if (val != 0.0)
                sb.append("\t" + it.key() + " -> " + val + "\n");
        }
        // checkSum();
        return sb.toString();
    }

}
