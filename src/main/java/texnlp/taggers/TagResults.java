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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;

/**
 * Write various aspects of a tagging experiment to appropriate files.
 * 
 * @author Jason Baldridge
 * @version $Revision: 1.53 $, $Date: 2006/10/12 21:20:44 $
 */
public class TagResults {

    protected File input;
    protected File outputFile;
    protected BufferedWriter taggedOutput;
    protected BufferedWriter perplexity;
    protected BufferedWriter entropy;
    protected boolean savePerplexity = false;
    protected boolean saveEntropy = false;

    protected enum OutputType {
        PIPE, CONLL, HASH;
    };

    protected OutputType format = OutputType.PIPE;

    public TagResults(String name, File input, File outputDir, boolean savePerplexity, boolean saveEntropy,
            String format) {

        if (format.equals("tab"))
            this.format = OutputType.CONLL;
        else if (format.equals("pipe"))
            this.format = OutputType.PIPE;
        else if (format.equals("hashslash"))
            this.format = OutputType.HASH;
        else
            throw new RuntimeException("Unknown format " + format);

        this.input = input;
        this.savePerplexity = savePerplexity;
        this.saveEntropy = saveEntropy;

        try {
            if (input.getName().endsWith(".gz")) {
                outputFile = new File(outputDir, name + ".tagged.txt.gz");
                taggedOutput = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(
                        outputFile))));

            }
            else {
                outputFile = new File(outputDir, name + ".tagged.txt");
                taggedOutput = new BufferedWriter(new FileWriter(outputFile));
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Unable to open file to save tagged results:" + name, e);
        }

        if (savePerplexity) {
            try {
                if (input.getName().endsWith(".gz"))
                    perplexity = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(
                            new File(outputDir, name + ".perplexity.txt.gz")))));
                else
                    perplexity = new BufferedWriter(new FileWriter(new File(outputDir, name + ".perplexity.txt")));

            }
            catch (IOException e) {
                throw new RuntimeException("Unable to open file to save perplexity:" + name, e);
            }
        }

        if (saveEntropy) {
            try {
                if (input.getName().endsWith(".gz"))
                    entropy = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(
                            new File(outputDir, name + ".entropy.txt.gz")))));
                else
                    entropy = new BufferedWriter(new FileWriter(new File(outputDir, name + ".entropy.txt")));

            }
            catch (IOException e) {
                throw new RuntimeException("Unable to open file to save entropy:" + name, e);
            }
        }

    }

    public File getInput() {
        return input;
    }

    public File getOutput() {
        return outputFile;
    }

    public void addTags(String[] toks, String[] tags) throws IOException {
        if (format == OutputType.CONLL) {
            for (int i = 0; i < toks.length; i++) {
                taggedOutput.write(toks[i]);
                taggedOutput.write('\t');
                taggedOutput.write(tags[i]);
                taggedOutput.write('\n');
            }
            taggedOutput.write('\n');
        }
        else if (format == OutputType.PIPE) {
            for (int i = 0; i < toks.length; i++) {
                taggedOutput.write(toks[i]);
                taggedOutput.write('|');
                taggedOutput.write(tags[i]);
                if (i + 1 < toks.length)
                    taggedOutput.write(' ');
            }
            taggedOutput.write('\n');
        }
        else if (format == OutputType.HASH) {
            throw new RuntimeException("Hash type not implemented yet");
        }
        else {
            throw new RuntimeException("Unhandled OutputType");
        }
    }

    public String[][] addTagsIte(String[] toks, Vector<String[]> tags) throws IOException {
        String[][] sentence = new String[toks.length][];
        for (int i = 0; i < toks.length; i++) {
            sentence[i] = new String[1 + tags.get(i).length];
            sentence[i][0] = toks[i];
            for (int j = 0; j < tags.get(i).length; j++) {
                sentence[i][j + 1] = (tags.get(i))[j];
            }
        }
        return sentence;
    }

    public void addPerplexity(double value) {
        if (savePerplexity) {
            try {
                perplexity.write(Double.toString(value));
                perplexity.write('\n');
            }
            catch (IOException e) {
                throw new RuntimeException("Error saving perplexity:", e);
            }
        }
    }

    public void addEntropy(double value) {
        if (saveEntropy) {
            try {
                entropy.write(Double.toString(value));
                entropy.write('\n');
            }
            catch (IOException e) {
                throw new RuntimeException("Error saving entropy:", e);
            }
        }
    }

    public void addEntropyBlank() {
        if (saveEntropy) {
            try {
                entropy.write('\n');
            }
            catch (IOException e) {
                throw new RuntimeException("Error saving entropy:", e);
            }
        }
    }

    public void close() throws IOException {
        taggedOutput.flush();
        taggedOutput.close();
        if (perplexity != null) {
            perplexity.flush();
            perplexity.close();
        }
        if (entropy != null) {
            entropy.flush();
            entropy.close();
        }
    }

}
