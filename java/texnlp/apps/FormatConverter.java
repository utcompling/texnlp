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

import java.io.*;
import org.apache.commons.cli.*;
import texnlp.io.*;
import texnlp.util.*;

/**
 * Convert between formats.
 *
 * @author  Jason Baldridge
 * @version $Revision: 1.53 $, $Date: 2006/10/12 21:20:44 $
 */
public class FormatConverter {

    public static void convertToCandc (String file) {

	try {
	    DataReader inputReader = new Conll2kReader(new File(file));
	    try {
		while (true) {
		    String[][] sequence = inputReader.nextSequence();
		    if (sequence.length > 0) {
			System.out.print(StringUtil.join("|", sequence[0]));
			for (int i=1; i<sequence.length; i++)
			    System.out.print(" " + StringUtil.join("|", sequence[i]));
			System.out.println();
		    }
		}
	    } catch (EOFException e) {
		inputReader.close();
	    }
	    
	} catch (IOException e) {
	    System.out.println("Error reading file: " + file);
	    System.out.println(e);
	}
    }

    public static void convertToConll (String file) {

	try {
	    DataReader inputReader = new PipeSepReader(new File(file));
	    try {
		while (true) {
		    String[][] sequence = inputReader.nextSequence();
		    if (sequence.length > 0) {
			System.out.print(StringUtil.join("\t", sequence[0]));
			for (int i=1; i<sequence.length; i++)
			    System.out.print("\n" + StringUtil.join("\t", sequence[i]));
			System.out.println();
			System.out.println();
		    }
		}
	    } catch (EOFException e) {
		inputReader.close();
	    }
	    
	} catch (IOException e) {
	    System.out.println("Error reading file: " + file);
	    System.out.println(e);
	}
    }

    public static void main (String[] args) {

	CommandLineParser optparse = new PosixParser();

	// create the Options
	Options options = new Options();
	options.addOption( "o", "output", true, "the output format" );
	options.addOption( "h", "help", false, "help" );

	try {
	    CommandLine cline = optparse.parse(options, args);
	    
	    if (cline.hasOption('h')) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(
			"java texnlp.apps.FormatConverter options filename", 
			options);
		System.exit(0);
		
	    }

	    String outputFormat = "candc";
	    if (cline.hasOption('o'))
		outputFormat = cline.getOptionValue('o');


	    if (outputFormat.equals("conll"))
		convertToConll(cline.getArgs()[0]);
	    else
		convertToCandc(cline.getArgs()[0]);

	} catch(ParseException exp ) {
	    System.out.println( "Unexpected exception parsing command line options:" + exp.getMessage() );
	}
	
    }


}
