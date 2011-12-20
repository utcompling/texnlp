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
    private int numIterations = 0;
    private int numMachines = 1;
    private double priorAmount = 0.0;
    private double logBeta = Math.log(0.001);
    private double cutoffTD = 0.0;
    private String modelType = "HMM";
    private String format = "pipe";
    private String taggedFile = "";
    private String rawFile = "";
    private String devFile = "";
    private String evalFile = "";
    private String machineFile = "";
    private File outputDir = null;
    private String tagsetFile = "";
    private boolean tagdictTraining = false;
    private boolean unconstrainedByTagdict = false;
    private boolean multitag = false;
    private boolean dirichletTransition = false;
    private boolean dirichletEmission = false;
    private String contextGen = "word";
    private Double lambda = 0.0;
    private double tolerance = .0001;
    private int validTagsForUnknownsMinCount = 1;
    private int maxValidTagsForUnknows = Integer.MAX_VALUE;

    public TaggerOptions(CommandLine cline) throws IOException {

        boolean outputDirSet = false;
        for (Option option : cline.getOptions()) {
            String value = option.getValue();
            switch (option.getOpt().charAt(0)) {
            case 'a':
                setNumMachines(Integer.parseInt(value));
                break;
            case 'b':
                setBeta(Double.parseDouble(value));
                break;
            case 'c':
                setCutoffTD(Double.parseDouble(value));
                break;
            case 'd':
                setDevFile(value);
                break;
            case 'e':
                setEvalFile(value);
                break;
            case 'f':
                setFormat(value);
                break;
            case 'g':
                setTagdictTraining(true);
                break;
            case 'i':
                setNumIterations(Integer.parseInt(value));
                break;
            case 'j':
                setDirichletTransition(true);
                break;
            case 'k':
                setDirichletEmission(true);
                break;
            case 'l':
                setMachineFile(value);
                break;
            case 'm':
                setModelType(value);
                break;
            case 'n':
                setMultitag(true);
                break;
            case 'o':
                setOutputDir(value);
                outputDirSet = true;
                break;
            case 'p':
                setPriorAmount(Double.parseDouble(value));
                break;
            case 'q':
                setLambda(Double.parseDouble(value));
                break;
            case 'r':
                setRawFile(value);
                break;
            case 's':
                setTagsetFile(value);
                break;
            case 't':
                setTaggedFile(value);
                break;
            case 'u':
                setUnconstrainedByTagdict(true);
                break;
            case 'v':
                setValidTagsForUnknownsMinCount(Integer.parseInt(value));
                break;
            case 'w':
                setContextGen(value);
                break;
            case 'z':
            	setTolerance(Double.parseDouble(value));
            	break;
            }
        }

        if (!outputDirSet)
            createOutputDir();
    }

	public void createOutputDir() throws IOException {
        if (outputDir == null) {
            outputDir = File.createTempFile("tag", null);
            // This removes the created file so we can recreate it as a
            // directory.
            outputDir.delete();
            // System.out.println("Results will be saved to: " + outputDir +
            // "\nMake sure to remove such output directories periodically.");
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

    public void setFiles(String model, String taggedFile, String devFile, File outputDir) throws IOException {
        setModelType(model);
        setOutputDir(outputDir);
        setTaggedFile(taggedFile);
        setDevFile(devFile);
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

    public void setFormat(String format) {
        this.format = format;
    }

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

    public double getTolerance() {
		return tolerance;
	}

    public int getValidTagsForUnknownsMinCount() {
        return validTagsForUnknownsMinCount;
    }

	public int getMaxValidTagsForUnknows() {
		return maxValidTagsForUnknows;
	}

	public void setNumIterations(int numIterations) {
        this.numIterations = numIterations;
    }

    public void setNumMachines(int numMachines) {
        this.numMachines = numMachines;
    }

    public void setPriorAmount(double priorAmount) {
        this.priorAmount = priorAmount;
    }

    public void setBeta(double beta) {
        this.logBeta = Math.log(beta);
    }

    public void setCutoffTD(double cutoffTD) {
        this.cutoffTD = cutoffTD;
    }

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

    public void setTaggedFile(String taggedFile) {
        this.taggedFile = taggedFile;
    }

    public void setRawFile(String rawFile) {
        this.rawFile = rawFile;
    }

    public void setDevFile(String devFile) {
        this.devFile = devFile;
    }

    public void setEvalFile(String evalFile) {
        this.evalFile = evalFile;
    }

    public void setMachineFile(String machineFile) {
        this.machineFile = machineFile;
    }

    public void setOutputDir(String outputDir) throws IOException {
        this.outputDir = new File(outputDir);
        createOutputDir();
    }

    public void setOutputDir(File outputDir) throws IOException {
        this.outputDir = outputDir;
        createOutputDir();
    }

    public void setTagsetFile(String tagsetFile) {
        this.tagsetFile = tagsetFile;
    }

    public void setTagdictTraining(boolean tagdictTraining) {
        this.tagdictTraining = tagdictTraining;
    }

    public void setUnconstrainedByTagdict(boolean unconstrainedByTagdict) {
        this.unconstrainedByTagdict = unconstrainedByTagdict;
    }

    public void setMultitag(boolean multitag) {
        this.multitag = multitag;
    }

    public void setDirichletTransition(boolean dirichletTransition) {
        this.dirichletTransition = dirichletTransition;
    }

    public void setDirichletEmission(boolean dirichletEmission) {
        this.dirichletEmission = dirichletEmission;
    }

    public void setLambda(Double lambda) {
        this.lambda = lambda;
    }

	public void setTolerance(double tolerance) {
		this.tolerance = tolerance;
	}

    public void setValidTagsForUnknownsMinCount(int validTagsForUnknownsMinCount) {
        this.validTagsForUnknownsMinCount = validTagsForUnknownsMinCount;
    }

	public void setMaxValidTagsForUnknows(int maxValidTagsForUnknows) {
		this.maxValidTagsForUnknows = maxValidTagsForUnknows;
	}

}
