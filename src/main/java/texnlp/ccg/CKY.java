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

/**
 * A parser that uses the CKY algorithm.
 * 
 * @author Jason Baldridge
 * @version $Revision: 1.53 $, $Date: 2006/10/12 21:20:44 $
 */
public class CKY {

    Lexicon lexicon;

    public Cell[][] chart;

    public CKY(Lexicon lexicon) {
        this.lexicon = lexicon;
    }

    public Cell parse(String sentence) {
        return parse(sentence.split(" "));
    }

    public Cell parse(String[] words) {
        final int numItems = words.length;

        chart = new Cell[numItems][numItems];

        for (int j = 0; j < numItems; j++) {
            chart[j][j] = new Cell(lexicon.getEntries(words[j]));

            for (int i = j - 1; i >= 0; i--) {
                Cell fill = new Cell();

                for (int k = i; k <= j - 1; k++) {

                    for (Sign left : chart[i][k].items) {

                        for (Sign right : chart[k + 1][j].items) {

                            // Forward application
                            combine(left, right, Slash.R, fill);

                            // Backward application
                            combine(right, left, Slash.L, fill);

                        }
                    }
                }
                chart[i][j] = fill;
            }
        }

        return chart[0][numItems - 1];
    }

    public void combine(Sign functor, Sign arg, boolean dir, Cell items) {
        // System.out.println("!!! " + functor + " -> " + arg);

        if (functor.cat instanceof ComplexCat && ((ComplexCat) functor.cat).arg.equals(arg.cat)
                && ((ComplexCat) functor.cat).sl.hasDir(dir)) {

            String words = "";
            if (dir == Slash.R)
                words = functor.lex + " " + arg.lex;
            else
                words = arg.lex + " " + functor.lex;

            items.addItem(new Sign(words, ((ComplexCat) functor.cat).res));
            // System.out.println("::: " + items);
        }
    }

}
