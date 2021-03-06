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
package texnlp.io;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Wrapper for reading in information from a file which has one word
 * per line with associated tag separated by "/". E.g.,
 *
 * ###/###
 * When/W
 * such/J
 * claims/N
 * and/C
 * litigation/N
 * extend/V
 * beyond/I
 * the/D
 * period/N
 * ,/,
 * the/D
 * syndicates/N
 * can/M
 * extend/V
 * their/P
 * accounting/N
 * deadlines/N
 * ./.
 * ###/###
 *
 * @author  Jason Baldridge
 * @version $Revision: 1.53 $, $Date: 2006/10/12 21:20:44 $
 */
public class HashSlashReader extends DataReader {

    public HashSlashReader(File f) throws IOException {
        super(f);
    }

    // Read the first ###/### line to get started.
    protected void prepare() throws IOException {
        inputReader.readLine();
    }

    public String[] nextToken() throws IOException, EOFException {
        String line = inputReader.readLine();
        if (line == null)
            throw new EOFException();
        line = line.trim();
        if (line.equals("###/###"))
            return nextToken();
        else
            return line.split("/");
    }

    public String[][] nextSequence() throws IOException, EOFException {

        ArrayList<String[]> sequence = new ArrayList<String[]>();

        String line = inputReader.readLine();
        if (line == null)
            throw new EOFException();
        line = line.trim();

        while (!(line.equals("###/###"))) {
            sequence.add(line.split("/"));
            line = inputReader.readLine();
            if (line == null)
                line = "###/###";
            line = line.trim();
        }

        String[][] sequenceFixed = new String[sequence.size()][];
        sequence.toArray(sequenceFixed);
        return sequenceFixed;
    }

    public String[] nextOutputSequence() throws IOException, EOFException {

        ArrayList<String> sequence = new ArrayList<String>();

        String line = inputReader.readLine();
        if (line == null)
            throw new EOFException();
        line = line.trim();

        while (!(line.equals("###/###"))) {
            sequence.add(line.substring(0, line.indexOf('/')));
            line = inputReader.readLine();
            if (line == null)
                line = "###/###";
            line = line.trim();
        }

        String[] sequenceFixed = new String[sequence.size()];
        sequence.toArray(sequenceFixed);
        return sequenceFixed;
    }

}
