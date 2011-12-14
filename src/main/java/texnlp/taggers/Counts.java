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

import gnu.trove.iterator.TObjectDoubleIterator;
import gnu.trove.list.linked.TLinkedList;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.THashSet;
import texnlp.util.IntDoublePair;
import texnlp.util.MathUtil;

/**
 * Count events for HMMs. Doubles are used so that partial counts obtained from
 * Baum-Welch can be added.
 * 
 * @author Jason Baldridge
 * @version $Revision: 1.53 $, $Date: 2006/10/12 21:20:44 $
 */
public class Counts {
    protected boolean useTransitionPrior = false;

    protected int numTags;
    protected THashSet<String> seenWords;

    protected TObjectDoubleHashMap<String> c_w;
    protected double[] c_t;

    protected double[] c_tInitial;
    protected double[] c_tFinal;

    protected double[][] c_tt;
    protected double[] lambda_tt;
    protected double[] lambda_tw;

    protected TransitionPrior tprior;
    // protected double[][] tprior.tt;
    // protected double[] tprior.tInitial;
    // protected double[] tprior.tFinal;

    protected boolean tagdictTraining = false;
    protected boolean useDirichletTransition = false;
    protected boolean useDirichletEmission = false;
    // protected final double dirichletAlpha = .1;
    protected final double dirichletAlpha = .005;

    protected TObjectDoubleHashMap<String>[] c_tw;

    public Counts(int _numTags, boolean tagdictTraining, boolean dirichletTransition, boolean dirichletEmission) {
        this(_numTags);
        this.tagdictTraining = tagdictTraining;
        useDirichletTransition = dirichletTransition;
        useDirichletEmission = dirichletEmission;
    }

    // Creating a simple class that immediately subclasses
    // TObjectDoubleHashMap<String> so as to be able to stick these into the
    // c_tw array
    private static class TWCounter extends TObjectDoubleHashMap<String> {
    };

    public Counts(int _numTags) {
        numTags = _numTags;
        seenWords = new THashSet<String>();

        c_t = new double[numTags];
        c_tInitial = new double[numTags];
        c_tFinal = new double[numTags];
        c_tt = new double[numTags][numTags];

        c_w = new TObjectDoubleHashMap<String>();
        c_tw = new TWCounter[numTags];
        for (int i = 0; i < numTags; i++) {
            c_tw[i] = new TWCounter();
        }

    }

    public Counts copy() {
        // return new Counts(numTags, tagdictTraining, useDirichletTransition,
        // useDirichletEmission);

        Counts ccopy = new Counts(numTags, tagdictTraining, useDirichletTransition, useDirichletEmission);

        // ccopy.c_w = c_w = new TObjectDoubleHashMap<String>();
        ccopy.c_w = clone(c_w);

        // ccopy.c_t = new double[numTags];
        System.arraycopy(c_t, 0, ccopy.c_t, 0, numTags);

        // ccopy.c_tInitial = new double[numTags];
        System.arraycopy(c_tInitial, 0, ccopy.c_tInitial, 0, numTags);
        //
        // ccopy.c_tFinal = new double[numTags];
        System.arraycopy(c_tFinal, 0, ccopy.c_tFinal, 0, numTags);
        //
        ccopy.c_tt = new double[numTags][numTags];
        ccopy.c_tw = new TObjectDoubleHashMap[numTags];
        for (int i = 0; i < numTags; i++) {
            System.arraycopy(c_tt[i], 0, ccopy.c_tt[i], 0, numTags);
            ccopy.c_tw[i] = new TObjectDoubleHashMap<String>();
            ccopy.c_tw[i] = clone(c_tw[i]);
        }

        ccopy.lambda_tt = lambda_tt;
        ccopy.lambda_tw = lambda_tw;

        ccopy.useTransitionPrior = useTransitionPrior;

        if (useTransitionPrior)
            ccopy.tprior = tprior;

        ccopy.seenWords = seenWords;

        return ccopy;
    }

    private TObjectDoubleHashMap<String> clone(TObjectDoubleHashMap<String> in) {
        TObjectDoubleHashMap<String> out = new TObjectDoubleHashMap<String>();
        out.putAll(in);
        return out;
    }

    public final void increment(String w) {
        c_w.adjustOrPutValue(w, 1.0, 1.0);
        seenWords.add(w);
    }

    public final void addToSeenWords(String w) {
        seenWords.add(w);
    }

    public final void increment(String w, double amount) {
        c_w.adjustOrPutValue(w, amount, amount);
        seenWords.add(w);
    }

    public final void increment(int t_i, String w) {
        increment(t_i, w, 1.0);
    }

    public double probTagGivenWord(int t_i, String w) {
        return c_tw[t_i].get(w) / c_w.get(w);
    }

    public final void increment(int t_i, String w, double amount) {
        c_tw[t_i].adjustOrPutValue(w, amount, amount);
    }

    public final int getNumEmissions(int t_i) {
        return c_tw[t_i].size();
    }

    public final void increment(int t_i) {
        c_t[t_i] += 1.0;
    }

    public final void increment(int t_i, double amount) {
        c_t[t_i] += amount;
    }

    public final void increment(int t_i, int t_j) {
        increment(t_i, t_j, 1.0);
    }

    public final void increment(int t_i, int t_j, double amount) {
        c_tt[t_i][t_j] += amount;
    }

    public final void incrementInitial(int t_i) {
        c_tInitial[t_i] += 1.0;
    }

    public final void incrementInitial(int t_i, double amount) {
        c_tInitial[t_i] += amount;
    }

    public final void incrementFinal(int t_i) {
        c_tFinal[t_i] += 1.0;
    }

    public final void incrementFinal(int t_i, double amount) {
        c_tFinal[t_i] += amount;
    }

    public void prepare() {

        int[] mult_tt = new int[numTags];
        int[] sing_tt = new int[numTags];
        int[] sing_tw = new int[numTags];

        for (int i = 0; i < numTags; i++) {
            for (int j = 0; j < numTags; j++)
                if (c_tt[i][j] > 0.0)
                    if (c_tt[i][j] <= 1.0)
                        sing_tt[i]++;
                    else
                        mult_tt[i]++;

            for (TObjectDoubleIterator<String> iter = c_tw[i].iterator(); iter.hasNext();) {
                iter.advance();
                final double val = iter.value();
                // System.out.println(iter.key() + " :: " + val);
                if (val > 0.0 && val <= 1.0)
                    sing_tw[i]++;
            }
        }

        // Take care of the lambdas for transitions
        lambda_tt = new double[numTags];

        int max_sing_tt = 0;
        for (int i = 0; i < numTags; i++)
            if (sing_tt[i] > max_sing_tt)
                max_sing_tt = sing_tt[i];

        for (int i = 0; i < numTags; i++) {
            // lambda_tt[i] = Math.max(sing_tt[i]+1e-100,
            // max_sing_tt * ((1.0 + sing_tt[i])/(1.0+mult_tt[i])));
            lambda_tt[i] = sing_tt[i] + 1e-100;
        }

        // System.out.println("singtt: "+ lambda_tt[0]);

        // Take care of the lambdas/dirichlet for emissions
        lambda_tw = new double[numTags];
        for (int i = 0; i < numTags; i++)
            lambda_tw[i] = c_tw[i].size() + 1e-100;
        // lambda_tw[i] = sing_tw[i]+1e-100;

        // Get the dirichlet alphas for each state
        // if (true || useDirichletEmission) {
        if (useDirichletEmission) {
            double numTypes = (double) seenWords.size();
            for (int i = 0; i < numTags; i++)
                lambda_tw[i] = Math.max(1.0, lambda_tw[i]) / numTypes;
        }

    }

    public final double[] getInitialLogDist() {
        return getInitialLogDist(false);
    }

    public final double[] getInitialLogDist(boolean startWithPrior) {
        double[] real_c_t = new double[numTags];
        for (int i = 0; i < numTags; i++)
            for (int j = 0; j < numTags; j++)
                real_c_t[i] += c_tt[i][j];

        double[] backoff = normalize(real_c_t);
        double total = MathUtil.sum(c_tInitial);
        double[] probs = new double[numTags];

        if (startWithPrior) {
            for (int i = 0; i < numTags; i++) {
                final double priorMultiplier = c_tw[i].size();
                if (useDirichletTransition)
                    probs[i] = MathUtil.elog(vbF(priorMultiplier * tprior.tInitial[i] + dirichletAlpha)
                            / vbF(priorMultiplier + dirichletAlpha * numTags));
                else
                    probs[i] = MathUtil.elog(tprior.tInitial[i]);
            }
        }
        else {
            for (int i = 0; i < numTags; i++) {
                if (useDirichletTransition)
                    probs[i] = MathUtil.elog(vbF(c_tInitial[i] + dirichletAlpha)
                            / vbF(total + dirichletAlpha * numTags));
                else
                    probs[i] = MathUtil.elog((c_tInitial[i] + lambda_tt[i] * backoff[i]) / (total + lambda_tt[i]));
            }
        }

        return probs;
    }

    public final double[] getFinalLogDist() {
        return getFinalLogDist(false);
    }

    public final double[] getFinalLogDist(boolean startWithPrior) {
        double[] real_c_t = new double[numTags];
        for (int i = 0; i < numTags; i++)
            for (int j = 0; j < numTags; j++)
                real_c_t[i] += c_tt[i][j];

        double[] backoff = normalize(real_c_t);
        double total = MathUtil.sum(c_tFinal);
        double[] probs = new double[numTags];

        if (startWithPrior) {
            for (int i = 0; i < numTags; i++) {
                final double priorMultiplier = c_tw[i].size();
                if (useDirichletTransition)
                    probs[i] = MathUtil.elog(vbF(priorMultiplier * tprior.tFinal[i] + dirichletAlpha)
                            / vbF(priorMultiplier + dirichletAlpha * numTags));
                else
                    probs[i] = MathUtil.elog(tprior.tFinal[i]);
            }
        }
        else {
            for (int i = 0; i < numTags; i++) {
                if (useDirichletTransition)
                    probs[i] = MathUtil.elog(vbF(c_tFinal[i] + dirichletAlpha) / vbF(total + dirichletAlpha * numTags));
                else
                    probs[i] = MathUtil.elog((c_tFinal[i] + lambda_tt[i] * backoff[i]) / (total + lambda_tt[i]));
            }
        }

        return probs;
    }

    public final double[][] getTransitionLogDist() {
        return getTransitionLogDist(false);
    }

    public final double[][] getTransitionLogDist(boolean startWithPrior) {
        double[][] probs = new double[numTags][numTags];

        if (startWithPrior) {
            for (int i = 0; i < numTags; i++) {
                for (int j = 0; j < numTags; j++) {
                    final double priorMultiplier = c_tw[i].size();
                    if (useDirichletTransition)
                        probs[i][j] = MathUtil.elog(vbF(priorMultiplier * tprior.tt[i][j] + dirichletAlpha)
                                / vbF(priorMultiplier + dirichletAlpha * numTags));
                    else
                        probs[i][j] = MathUtil.elog(tprior.tt[i][j]);
                }
            }
        }
        else {

            double[] real_c_t = new double[numTags];
            for (int i = 0; i < numTags; i++)
                real_c_t[i] = MathUtil.sum(c_tt[i]);
            // real_c_t[i] = MathUtil.sum(c_tt[i]) + c_tFinal[i];

            double[] backoff = normalize(real_c_t);
            for (int i = 0; i < numTags; i++) {
                for (int j = 0; j < numTags; j++) {
                    if (useDirichletTransition)
                        probs[i][j] = MathUtil.elog(vbF(c_tt[i][j] + dirichletAlpha)
                                / vbF(real_c_t[i] + dirichletAlpha * numTags));
                    else {
                        final double rawCount = c_tt[i][j];
                        final double lambdaVal = lambda_tt[i];
                        final double backoffVal = backoff[j];
                        final double tagCntSum = real_c_t[i];
                        final double nom = rawCount + lambdaVal * backoffVal;
                        final double denom = tagCntSum + lambdaVal;
                        final double transLogProb = MathUtil.elog(nom / denom);
                        probs[i][j] = transLogProb;
                        // System.out.println(i +
                        // "::"+j+": "+Math.exp(probs[i][j]));
                    }
                }
            }
        }
        return probs;
    }

    public final EmissionProbs getEmissionLogDist() {
        return getEmissionLogDist(null);
    }

    public final EmissionProbs getEmissionLogDist(TIntSet validTagsForUnknowns) {
        TObjectDoubleHashMap<String> unigramSmoothed = new TObjectDoubleHashMap<String>(c_w.size());
        final double numWords = MathUtil.sum(c_w.values());
        final int numTypes = seenWords.size() + 1;

        // System.out.println(numWords + " :: " + numTypes);
        TLinkedList<IntDoublePair> unknownEmission = new TLinkedList<IntDoublePair>();

        THashMap<String, TLinkedList<IntDoublePair>> probs = new THashMap<String, TLinkedList<IntDoublePair>>(
                c_w.size());

        for (TObjectDoubleIterator<String> iter = c_w.iterator(); iter.hasNext();) {
            iter.advance();
            unigramSmoothed.put(iter.key(), (iter.value() + 1.0) / (numWords + numTypes));
            probs.put(iter.key(), new TLinkedList<IntDoublePair>());
        }

        double[] emissionTotals = new double[numTags];
        for (int i = 0; i < numTags; i++) {
            TObjectDoubleIterator<String> iter = c_tw[i].iterator();
            for (int j = c_tw[i].size(); j-- > 0;) {
                iter.advance();
                emissionTotals[i] += iter.value();
            }
        }

        for (int i = 0; i < numTags; i++) {
            // final double localDA = dirichletAlpha;
            final double localDA = lambda_tw[i];

            if (validTagsForUnknowns == null || validTagsForUnknowns.contains(i)) {
                double unknownProb;
                if (useDirichletEmission)
                    unknownProb = MathUtil.elog(vbF(localDA) / vbF(emissionTotals[i] + localDA * numTypes));
                else
                    unknownProb = MathUtil.elog((lambda_tw[i] / (numWords + numTypes))
                            / (emissionTotals[i] + lambda_tw[i]));

                // System.out.println(i + " => " + MathUtil.eexp(unknownProb) +
                // " :: " + numEmissions + " :: " + emissionTotals[i] + "\t:: "
                // + lambda_tw[i]);

                unknownEmission.add(new IntDoublePair(i, unknownProb));
            }

            for (TObjectDoubleIterator<String> iter = c_tw[i].iterator(); iter.hasNext();) {
                iter.advance();
                double p;
                if (useDirichletEmission)
                    p = MathUtil.elog(vbF(iter.value() + localDA) / vbF(emissionTotals[i] + localDA * numTypes));
                else
                    p = MathUtil.elog((iter.value() + lambda_tw[i] * unigramSmoothed.get(iter.key()))
                            / (emissionTotals[i] + lambda_tw[i]));

                probs.get(iter.key()).add(new IntDoublePair(i, p));
            }
        }

        return new EmissionProbs(probs, unknownEmission);
    }

    private final double vbF(double v) {
        return Math.exp(digamma(v));
    }

    private final double digamma(double v) {
        if (v > 7)
            return digammaApproximation(v);
        else
            return digamma(v + 1.0) - 1 / v;
    }

    private final double digammaApproximation(double v) {
        return Math.log(v) + 0.04167 * Math.pow(v, -2) - 0.00729 * Math.pow(v, -4) + 0.00384 * Math.pow(v, -6)
                - 0.00413 * Math.pow(v, -8);
    }

    private final double[] normalize(double[] vals) {
        double[] probs = new double[vals.length];
        double total = MathUtil.sum(vals);
        if (total == 0.0)
            for (int i = 0; i < vals.length; i++)
                probs[i] = 1.0 / vals.length;
        else
            for (int i = 0; i < vals.length; i++)
                probs[i] = vals[i] / total;
        return probs;
    }

    public void createTransitionPrior(String[] stateNames, TObjectIntHashMap<String> states, double amount) {
        useTransitionPrior = true;

        tprior = new TransitionPrior(stateNames, states);

    }

}
