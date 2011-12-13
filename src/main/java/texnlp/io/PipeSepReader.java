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

/**
 * Wrapper for reading in information from a file which has one
 * sentence per line with each word and associated tags separated by
 * the pipe |. E.g., the native format for the C&C tools:
 *
 * Pierre|NNP|N/N Vinken|NNP|N ,|,|, 61|CD|N/N years|NNS|N old|JJ|(S[adj]\NP)\NP ,|,|, will|MD|(S[dcl]\NP)/(S[b]\NP) join|VB|((S[b]\NP)/PP)/NP the|DT|NP[nb]/N board|NN|N as|IN|PP/NP a|DT|NP[nb]/N nonexecutive|JJ|N/N director|NN|N Nov.|NNP|((S\NP)\(S\NP))/N[num] 29|CD|N[num] .|.|.

 *
 * @author  Jason Baldridge
 * @version $Revision: 1.53 $, $Date: 2006/10/12 21:20:44 $
 */
public class PipeSepReader extends DataReader {

    private String[][] currentSequence = null;
    private int currentIndex = 0;

    public PipeSepReader(File f) throws IOException {
        super(f);
    }

    public String[] nextToken() throws IOException, EOFException {
        if (currentSequence == null || currentIndex == currentSequence.length) {
            currentSequence = nextSequence();
            currentIndex = 0;
        }

        return currentSequence[currentIndex++];
    }

    public String[][] nextSequence() throws IOException, EOFException {

        String[][] sequence;

        String line = inputReader.readLine();
        if (line == null)
            throw new EOFException();
        line = line.trim();

        if (line.length() == 0)
            return nextSequence();

        String[] items = line.split(" ");
        sequence = new String[items.length][];
        for (int i = 0; i < items.length; i++)
            sequence[i] = items[i].split("\\|");

        return sequence;
    }

    public String[] nextOutputSequence() throws IOException, EOFException {

        String[] sequence;

        String line = inputReader.readLine();

        if (line == null)
            throw new EOFException();
        line = line.trim();

        if (line.length() == 0)
            return nextOutputSequence();

        String[] items = line.split(" ");
        sequence = new String[items.length];
        for (int i = 0; i < items.length; i++)
            sequence[i] = items[i].split("\\|")[0];

        return sequence;

    }

}
