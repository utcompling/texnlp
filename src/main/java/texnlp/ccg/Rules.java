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

import texnlp.ccg.parse.CategoryParser;

/**
 * A simple implementation of CCG rules.
 * 
 * @author Jason Baldridge
 * @version $Revision: 1.53 $, $Date: 2006/10/12 21:20:44 $
 */
public class Rules {

    public static boolean forwardApplication(Cat left, Cat right) {
        return application(left, right, Slash.R);
    }

    public static boolean backwardApplication(Cat left, Cat right) {
        return application(right, left, Slash.L);
    }

    public static boolean forwardComposition(Cat left, Cat right) {
        return composition(left, right, Slash.R);
    }

    public static boolean backwardComposition(Cat left, Cat right) {
        return composition(right, left, Slash.L);
    }

    public static boolean forwardCrossedComposition(Cat left, Cat right) {
        return crossedComposition(left, right, Slash.R);
    }

    public static boolean backwardCrossedComposition(Cat left, Cat right) {
        return crossedComposition(right, left, Slash.L);
    }

    public static boolean application(Cat primary, Cat secondary, boolean ruleDir) {

        if (primary instanceof ComplexCat) {
            if (((ComplexCat) primary).sl.hasDir(ruleDir)) {
                Cat pArg = ((ComplexCat) primary).arg;
                if (pArg.unifies(secondary))
                    return true;
            }
            else {
                return application(((ComplexCat) primary).res, secondary, ruleDir);
            }
        }
        return false;
    }

    public static boolean composition(Cat primary, Cat secondary, boolean ruleDir) {

        if (primary instanceof ComplexCat && secondary instanceof ComplexCat) {

            if (((ComplexCat) primary).sl.hasDir(ruleDir) && ((ComplexCat) secondary).sl.hasDir(ruleDir)) {

                Cat pArg = ((ComplexCat) primary).arg;
                Cat sFunc = ((ComplexCat) secondary).res;

                if (pArg.unifies(sFunc))
                    return true;
                else
                    return composition(primary, sFunc, ruleDir);
            }
            return composition(((ComplexCat) primary).res, secondary, ruleDir);
        }
        return false;
    }

    public static boolean crossedComposition(Cat primary, Cat secondary, boolean ruleDir) {

        if (primary instanceof ComplexCat && secondary instanceof ComplexCat) {

            if (((ComplexCat) primary).sl.hasDir(ruleDir) && !((ComplexCat) secondary).sl.hasDir(ruleDir)) {

                Cat pArg = ((ComplexCat) primary).arg;
                Cat sFunc = ((ComplexCat) secondary).res;

                if (pArg.unifies(sFunc) && pArg.getRootCat().val.equals("S"))
                    return true;
                else
                    return crossedComposition(primary, sFunc, ruleDir);
            }
            return crossedComposition(((ComplexCat) primary).res, secondary, ruleDir);
        }
        return false;
    }

    public static boolean canCombine(Cat left, Cat right) {
        // System.out.println(left + " ## " + right);

        boolean combinable = false;
        // String rule = "NO RULE";

        if (forwardApplication(left, right)) {
            // rule = "FA";
            combinable = true;
        }

        if (backwardApplication(left, right)) {
            // rule = "BA";
            combinable = true;
        }

        if (forwardComposition(left, right)) {
            // rule = "FC";
            combinable = true;
        }

        if (backwardComposition(left, right)) {
            // rule = "BC";
            combinable = true;
        }

        if (backwardCrossedComposition(left, right)) {
            // rule = "BxC";
            combinable = true;
        }

        // if (combinable)
        // System.out.println(rule + ":\t" + left + "\t" + right);

        return combinable;
    }

    public static void main(String[] args) {
        CategoryParser parser = new CategoryParser();
        Cat c1 = parser.parse("((S\\NP)/S)/NP");
        Cat c2 = parser.parse("N");
        Cat c3 = parser.parse("NP\\NP");
        Cat c4 = parser.parse("S/(S\\NP)");
        Cat c5 = parser.parse("N/N");
        Cat c6 = parser.parse("NP");
        Cat c7 = parser.parse("((S\\NP)\\(S\\NP))/NP");
        Cat c8 = parser.parse("(S\\NP)/NP");

        System.out.println(Rules.forwardApplication(c1, c2));
        System.out.println(Rules.backwardApplication(c1, c2));
        System.out.println(Rules.backwardApplication(c2, c1));
        System.out.println(Rules.forwardComposition(c1, c3));

        System.out.println(canCombine(c1, c2));
        System.out.println(canCombine(c2, c1));
        System.out.println(canCombine(c3, c1));
        System.out.println(canCombine(c1, c4));
        System.out.println(canCombine(c4, c1));
        System.out.println(canCombine(c2, c1));
        System.out.println(canCombine(c5, c6));
        System.out.println(canCombine(c6, c7));
        System.out.println(canCombine(c8, c7));

    }

}
