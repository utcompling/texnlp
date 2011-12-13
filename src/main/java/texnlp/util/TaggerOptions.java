///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2008 Elias Ponvert, The University of Texas at Austin
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

package texnlp.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import texnlp.taggers.Tagger;

/**
 * Options to configure a {@link Tagger} from {@link CommandLine} arguments
 * 
 * @author ponvert@mail.utexas.edu (Elias Ponvert)
 */
public class TaggerOptions {
    public int numIterations = 0;
    public int numMachines = 1;
    public double priorAmount = 0.0;
    public double logBeta = Math.log(0.001);
    public double cutoffTD = 0.0;
    public String modelType = "HMM";
    public String format = "pipe";
    public String taggedFile = "";
    public String rawFile = "";
    public String devFile = "";
    public String evalFile = "";
    public String machineFile = "";
    public File outputDir = null;
    public String tagsetFile = "";
    public boolean tagdictTraining = false;
    public boolean unconstrainedByTagdict = false;
    public boolean multitag = false;
    public boolean dirichletTransition = false;
    public boolean dirichletEmission = false;
    public String contextGen = "word";
    public Double lambda = 0.0;

    public TaggerOptions(CommandLine cline) throws IOException {

        for (Option option : cline.getOptions()) {
            String value = option.getValue();
            switch (option.getOpt().charAt(0)) {
            case 'f':
                setFormat(value);
                break;
            case 'w':
                setContextGen(value);
                break;
            case 'i':
                numIterations = Integer.parseInt(value);
                break;
            case 'a':
                numMachines = Integer.parseInt(value);
                break;
            case 'p':
                priorAmount = Double.parseDouble(value);
                break;
            case 'b':
                logBeta = Math.log(Double.parseDouble(value));
                break;
            case 'c':
                cutoffTD = Double.parseDouble(value);
                break;
            case 'q':
                lambda = Double.parseDouble(value);
                break;
            case 'm':
                modelType = value;
                break;
            case 't':
                taggedFile = value;
                break;
            case 'r':
                rawFile = value;
                break;
            case 'd':
                devFile = value;
                break;
            case 'e':
                evalFile = value;
                break;
            case 'l':
                machineFile = value;
                break;
            case 'o':
                outputDir = new File(value);
                break;
            case 's':
                tagsetFile = value;
                break;
            case 'g':
                tagdictTraining = true;
                break;
            case 'u':
                unconstrainedByTagdict = true;
                break;
            case 'n':
                multitag = true;
                break;
            case 'j':
                dirichletTransition = true;
                break;
            case 'k':
                dirichletEmission = true;
                break;
            }
        }

        // Create the output directory
        if (outputDir == null) {
            outputDir = File.createTempFile("tag", null);
            // This removes the created file so we can recreate it as a
            // directory.
            outputDir.delete();
            System.out.println("Results will be saved to: " + outputDir
                    + "\nMake sure to remove such output directories periodically.");
        }

        if (!outputDir.mkdirs()) {
            System.out.println("************************************************************************");
            System.out.println("Unable to create directory \"" + outputDir
                    + "\" for output.\nIt probably already exists -- choose a unique name.");
            System.out.println("************************************************************************");
            System.exit(0);
        }
    }

    public TaggerOptions() {

    }

    public void setFiles(String model, String taggedFile, String devFile, File outputDir) {
        this.modelType = model;
        this.outputDir = outputDir;
        this.taggedFile = taggedFile;
        this.devFile = devFile;
    }

    public int getNumIterations() {
        return numIterations;
    }

    public int getNumMachines() {
        return numMachines;
    }

    public double getPriorAmount() {
        return priorAmount;
    }

    public double getLogBeta() {
        return logBeta;
    }

    public double getCutoffTD() {
        return cutoffTD;
    }

    public String getModelType() {
        return modelType;
    }

    public String getTaggedFile() {
        return taggedFile;
    }

    public String getRawFile() {
        return rawFile;
    }

    public String getDevFile() {
        return devFile;
    }

    public String getEvalFile() {
        return evalFile;
    }

    public String getMachineFile() {
        return machineFile;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public String getTagsetFile() {
        return tagsetFile;
    }

    public boolean isTagdictTraining() {
        return tagdictTraining;
    }

    public boolean isUnconstrainedByTagdict() {
        return unconstrainedByTagdict;
    }

    public boolean isMultitag() {
        return multitag;
    }

    public boolean isDirichletTransition() {
        return dirichletTransition;
    }

    public boolean isDirichletEmission() {
        return dirichletEmission;
    }

    /**
     * @param format
     *            the format to set
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * @return the format
     */
    public String getFormat() {
        return format;
    }

    public String getContextGen() {
        return contextGen;
    }

    public void setContextGen(String contextGen) {
        this.contextGen = contextGen;
    }

    public Double getLambda() {
        return lambda;
    }
}
