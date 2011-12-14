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

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import texnlp.estimate.SimpleWordContextGenerator;
import texnlp.taggers.HMM;
import texnlp.taggers.MEL;
import texnlp.taggers.MEMM;
import texnlp.taggers.TagResults;
import texnlp.taggers.Tagger;
import texnlp.util.IOUtil;
import texnlp.util.TaggerOptions;

/**
 * Train and test a tagger.
 * 
 * @author Jason Baldridge
 * @version $Revision: 1.53 $, $Date: 2006/10/12 21:20:44 $
 */
public class Tag {
    private static Log LOG = LogFactory.getLog(Tag.class);

    public static void testTagger(TaggerOptions taggerOptions) {
        Tagger tagger;
        String modelType = taggerOptions.getModelType();

        if (modelType.equals("MEMM")) {
            tagger = new MEMM(taggerOptions);
        }
        else if (modelType.equals("MEL")) {
            tagger = new MEL(taggerOptions);
        }
        else if (modelType.equals("Super")) {
            tagger = new MEL(taggerOptions);
            ((MEL) tagger).setWordContextGenerator(new SimpleWordContextGenerator());
        }
        else {
            tagger = new HMM(taggerOptions);
        }

        LOG.info("Training...");

        tagger.train();

        String devFile = taggerOptions.getDevFile();
        File outputDir = taggerOptions.getOutputDir();

        try {
            if (!devFile.equals("")) {
                LOG.info("Tagging devel file...");
                TagResults results = new TagResults("dev", new File(devFile), outputDir, true, true,
                        taggerOptions.getFormat());
                tagger.tagFile(results);
                results.close();
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Exception tagging dev file.", e);
        }

        try {
            String evalFile = taggerOptions.getEvalFile();
            String trainingFile = taggerOptions.getTaggedFile();

            if (!evalFile.equals("")) {
                LOG.info("Tagging eval file...");
                TagResults results = new TagResults("eval", new File(evalFile), outputDir, false, false,
                        taggerOptions.getFormat());
                tagger.tagFile(results);
                results.close();
                final String resultsOutput = results.getOutput().toString();
                Score.score(resultsOutput, evalFile, trainingFile, taggerOptions.getFormat(), 1);
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Exception tagging eval file.", e);
        }
    }

    public static void main(String[] args) throws ParseException, IOException {

        CommandLineParser optparse = new PosixParser();

        // create the Options
        Options options = new Options();
        options.addOption("w", "context-gen", true, "context generator strategy [word, aggressive; default = word]");
        options.addOption("f", "format", true, "input/output formats [tab,pipe,hashslash, default = pipe]");
        options.addOption("d", "dev", true, "the development file");
        options.addOption("t", "train", true, "the training file");
        options.addOption("l", "labeled", true, "the machine labeled file");
        options.addOption("r", "raw", true, "the raw training file");
        options.addOption("i", "iterations", true, "number of forward-backward iterations");
        options.addOption("e", "eval", true, "the evaluation file");
        options.addOption("o", "out", true, "the output directory");
        options.addOption("m", "model", true, "the model type (default HMM)");
        options.addOption("s", "tagset", true,
                "a prespecified tagset (e.g., for use with cutoffs to ignore low frequency tags)");
        options.addOption("g", "tag-dict", false, "use training file to build tag dictionary");
        options.addOption("p", "prior", true, "use prior with value specified (what this means depends on the model)");
        options.addOption("a", "mpi", true, "use MPI with n machines (for MEMM and MEL)");
        options.addOption("b", "beta", true,
                "use variable width beam during viterbi decoding using beta*maxprob as a threshhold (default = .001)");
        options.addOption("c", "td-cutoff", true, "use variable width cutoff in tag dictionary (default = 0.0)");
        options.addOption("u", "unconstrained", false,
                "don't be constrained by tag dictionary when outputting multiple tags (works for MEL only)");
        options.addOption("n", "multitag", false, "output multiple tags (works for MEL only)");
        options.addOption("j", "dirichlet-transition", false, "use dirichlet prior on transition probabilities");
        options.addOption("k", "dirichlet-emission", false, "use dirichlet prior on emission probabilities");
        options.addOption("q", "lambda", true, "scaler for soft counts in HMM EM and cotraining");
        options.addOption("h", "help", false, "print help");

        CommandLine cline = optparse.parse(options, args);

        if (cline.hasOption('h')) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java Tag", options);
            System.exit(0);
        }

        TaggerOptions taggerOptions = new TaggerOptions(cline);
        testTagger(taggerOptions);

        if (taggerOptions.getNumMachines() > 1) {
            IOUtil.runCommand("mpdallexit");
        }

    }

}
