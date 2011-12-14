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
package texnlp.apps;

import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import texnlp.ccg.Cat;
import texnlp.ccg.Rules;
import texnlp.ccg.Slash;
import texnlp.ccg.parse.CategoryParser;
import texnlp.taggers.TransitionPrior;

/**
 * Given a list of categories, say whether every possible pair can combine.
 * 
 * @author Jason Baldridge
 * @version $Revision: 1.53 $, $Date: 2006/10/12 21:20:44 $
 */
public class CatCombine {

    public static void main(String[] args) {

        CommandLineParser optparse = new PosixParser();

        Options options = new Options();
        options.addOption("s", "tagset", true, "the file containing the categories");
        options.addOption("o", "output", true, "the file containing the categories");

        try {
            CommandLine cline = optparse.parse(options, args);

            String tagsetFile = "";
            String outputType = "boolean";
            for (Option option : cline.getOptions()) {
                String value = option.getValue();
                switch (option.getOpt().charAt(0)) {
                case 's':
                    tagsetFile = value;
                    break;
                case 'o':
                    outputType = value;
                    break;
                }
            }

            TObjectIntHashMap<String> states = new TObjectIntHashMap<String>();
            String[] stateNames;
            int numStates = 0;

            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(tagsetFile)));

            int stateID = 0;
            String line = null;
            while ((line = br.readLine()) != null)
                states.put(line.trim(), stateID++);

            states.trimToSize();

            numStates = stateID;
            stateNames = new String[numStates];
            for (TObjectIntIterator<String> it = states.iterator(); it.hasNext();) {
                it.advance();
                stateNames[it.value()] = it.key();
            }

            int numTags = numStates;
            CategoryParser catp = new CategoryParser();
            Cat[] cats = new Cat[numTags];

            for (int i = 0; i < numTags; i++) {
                String catString = Cat.changeNonCats(stateNames[i]);
                // String catString = stateNames[i];
                Cat c = catp.parse(catString);
                cats[i] = c;
            }

            TransitionPrior tprior = new TransitionPrior(stateNames, states);

            for (int i = 0; i < numTags; i++) {
                for (int j = 0; j < numTags; j++) {
                    String outputVal = "0";
                    if (outputType.equals("weighted"))
                        outputVal = "1";

                    if (Rules.canCombine(cats[i], cats[j])) {
                        outputVal = "1";
                        if (outputType.equals("weighted"))
                            outputVal = "10";
                    }
                    if (outputType.equals("prior"))
                        outputVal = Double.toString(tprior.tt[i][j]);

                    System.out.println(outputVal + " " + stateNames[i] + " " + stateNames[j]);
                }

                String outputForEOS = "0";
                if (outputType.equals("weighted"))
                    outputForEOS = "1";

                if (cats[i].notSeekingDir(Slash.R)) {
                    outputForEOS = "1";
                    if (outputType.equals("weighted"))
                        outputForEOS = "10";
                }

                if (outputType.equals("prior"))
                    outputForEOS = Double.toString(tprior.tFinal[i]);

                System.out.println(outputForEOS + " " + stateNames[i] + " EOS");

            }

            for (int j = 0; j < numTags; j++) {
                String outputForBOS = "0";
                if (outputType.equals("weighted"))
                    outputForBOS = "1";

                if (cats[j].notSeekingDir(Slash.L)) {
                    outputForBOS = "1";
                    if (outputType.equals("weighted"))
                        outputForBOS = "10";
                }

                if (outputType.equals("prior"))
                    outputForBOS = Double.toString(tprior.tInitial[j]);

                System.out.println(outputForBOS + " BOS " + stateNames[j]);
            }

        }
        catch (ParseException exp) {
            System.out.println("Unexpected exception parsing command line options:" + exp.getMessage());
        }
        catch (IOException exp) {
            System.out.println("IOException:" + exp.getMessage());
            System.exit(0);
        }

    }

}
