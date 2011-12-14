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

import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.THashSet;

import java.util.Set;

import texnlp.ccg.AtomCat;
import texnlp.ccg.Cat;
import texnlp.ccg.Rules;
import texnlp.ccg.Slash;
import texnlp.ccg.parse.CategoryParser;
import texnlp.util.MathUtil;

/**
 * 
 * 
 * @author Jason Baldridge
 * @version $Revision: 1.53 $, $Date: 2006/10/12 21:20:44 $
 */
public class TransitionPrior {
    public double[][] tt;
    public double[] tInitial;
    public double[] tFinal;

    private static final double sigma = .95;

    public TransitionPrior(String[] stateNames, TObjectIntHashMap<String> states) {
        this(stateNames, states, false);
    }

    public TransitionPrior(String[] stateNames, TObjectIntHashMap<String> states, boolean uniformUnary) {

        int numTags = stateNames.length;

        CategoryParser catp = new CategoryParser();
        Cat[] cats = new Cat[numTags];
        Set<AtomCat> atomcats = new THashSet<AtomCat>();
        int[] catComplexities = new int[numTags];
        for (int stateID = 0; stateID < numTags; stateID++) {
            String catString = Cat.changeNonCats(stateNames[stateID]);
            // String catString = stateNames[stateID];
            Cat c = catp.parse(catString);
            cats[stateID] = c;

            Set<Cat> uniquecats = new THashSet<Cat>();
            if (uniformUnary) {
                // make a uniform lexprior
                catComplexities[stateID] = 1;
            }
            else {
                // measure "complexity" as the total number of subcategories
                catComplexities[stateID] = c.collectCats(atomcats, uniquecats);
            }
        }

        double[] prob_cat_unary = new double[states.size()];
        for (int stateID = 0; stateID < numTags; stateID++)
            prob_cat_unary[stateID] = 1.0 / catComplexities[stateID];

        double allUnary = MathUtil.sum(prob_cat_unary);
        for (int stateID = 0; stateID < numTags; stateID++)
            prob_cat_unary[stateID] /= allUnary;

        // handle transition prior
        tt = new double[numTags][numTags];
        for (int i = 0; i < numTags; i++) {
            double totalCombiners = 0;
            for (int j = 0; j < numTags; j++)
                if (Rules.canCombine(cats[i], cats[j]))
                    totalCombiners += prob_cat_unary[j];

            for (int j = 0; j < numTags; j++) {
                tt[i][j] = (1 - sigma) * prob_cat_unary[j];
                if (Rules.canCombine(cats[i], cats[j]))
                    tt[i][j] += sigma * prob_cat_unary[j] / totalCombiners;
            }
        }

        // handle initial prior
        double totalStarters = 0.0;
        for (int i = 0; i < numTags; i++)
            if (cats[i].notSeekingDir(Slash.L))
                totalStarters += prob_cat_unary[i];

        tInitial = new double[numTags];
        for (int i = 0; i < numTags; i++) {
            tInitial[i] = (1 - sigma) * prob_cat_unary[i];
            if (cats[i].notSeekingDir(Slash.L))
                tInitial[i] += sigma * prob_cat_unary[i] / totalStarters;
        }

        // handle final prior
        double totalEnders = 0.0;
        for (int i = 0; i < numTags; i++)
            if (cats[i].notSeekingDir(Slash.R))
                totalEnders += 1.0;

        tFinal = new double[numTags];
        for (int i = 0; i < numTags; i++) {
            tFinal[i] = (1 - sigma) * 1.0 / numTags;
            if (cats[i].notSeekingDir(Slash.R))
                tFinal[i] += sigma * 1.0 / totalEnders;
        }
    }

}
