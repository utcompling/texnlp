package texnlp.estimate;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;

import texnlp.util.Constants;
import texnlp.util.IOUtil;
import texnlp.util.MathUtil;

public class TadmClassifier {

    public static String PATH_TO_ESTIMATE_EXECUTABLE = Constants.TADM_HOME + "/bin/tadm";

    protected static File MODEL_DIR = new File(Constants.TEXNLP_HOME + "/tmp/");
    static {
        MODEL_DIR.mkdir();
    }

    // For training
    // private TLinkedList<Event> events = new TLinkedList<Event>();
    private THashMap<Context, int[]> events = new THashMap<Context, int[]>();
    // new THashMap<Context,int[]>(new TObjectHashingStrategy<Context>() {
    // int computeHashCode(Context c) {
    // return c.hashCode();
    // }
    // boolean equals (Context c1, Context c2) {
    // return c1.equals(c2);
    // }
    //
    // });

    // these are for the model usage phase
    private FeatureMap fmap;
    private double[] params;
    private int numLabels;
    protected int numParams;

    // options to TADM
    protected File paramsFile, eventsFile;
    protected String name;
    protected String method;
    protected int iterations;
    protected double variance;
    protected int numMachines;

    public TadmClassifier(String _name, String _method, int _numLabels, int _iterations, double _variance,
            int _numMachines) {
        name = _name;
        method = _method;
        numLabels = _numLabels;
        iterations = _iterations;
        variance = _variance;
        numMachines = _numMachines;

        fmap = new FeatureMap(numLabels);

        try {
            if (numMachines > 1) {
                paramsFile = File.createTempFile(name, ".params", new File(Constants.CWD));
                eventsFile = File.createTempFile(name, ".events.gz", new File(Constants.CWD));
            }
            else {
                paramsFile = File.createTempFile(name, ".params");
                eventsFile = File.createTempFile(name, ".events.gz");
            }

            paramsFile.deleteOnExit();
            eventsFile.deleteOnExit();
        }
        catch (java.io.IOException e) {
            System.out.println("Unable to create tmp files for TADM events and parameters!");
            System.out.println(e);
            System.exit(0);
        }
    }

    public double getLogProb(int candidate, String[] contexts) {
        return getLogDistribution(contexts)[candidate];
    }

    public double[] getLogDistribution(THashSet<String> contexts) {
        String[] contextsSA = new String[contexts.size()];
        contexts.toArray(contextsSA);
        return getLogDistribution(contextsSA);
    }

    public double[] getLogDistribution(String[] contexts) {

        double[] probs = new double[numLabels];

        for (int m = 0; m < contexts.length; m++) {
            final int featureID = fmap.get(contexts[m]);
            if (featureID > 0)
                for (int label = 0; label < numLabels; label++)
                    probs[label] += params[featureID - label];
        }

        // make it a distribution and turn the probs into log probs
        MathUtil.exponentiate(probs);
        MathUtil.normalize(probs);
        MathUtil.takeLogarithm(probs);

        return probs;
    }

    public double[][] getLogDistributions(String[] contexts, int[] iStates, int[] jStates) {

        double[][] probs = new double[numLabels][numLabels];

        double[] baseWeights = new double[numLabels];
        for (int m = 0; m < contexts.length; m++) {
            final int featureID = fmap.get(contexts[m]);
            if (featureID > 0)
                for (int j = 0; j < jStates.length; j++)
                    baseWeights[jStates[j]] += params[featureID - jStates[j]];
        }

        for (int i = 0; i < iStates.length; i++) {
            final int featureID = fmap.get("prevtag:" + Integer.toString(iStates[i]));
            if (featureID > 0)
                for (int j = 0; j < jStates.length; j++)
                    probs[iStates[i]][jStates[j]] = baseWeights[jStates[j]] + params[featureID - jStates[j]];
        }

        // make distributions and turn the probs into log probs
        for (int i = 0; i < iStates.length; i++) {
            double total = 0.0;
            for (int j = 0; j < jStates.length; j++) {
                probs[iStates[i]][jStates[j]] = Math.exp(probs[iStates[i]][jStates[j]]);
                total += probs[iStates[i]][jStates[j]];
            }

            for (int j = 0; j < jStates.length; j++)
                probs[iStates[i]][jStates[j]] = Math.log(probs[iStates[i]][jStates[j]] / total);

        }

        return probs;
    }

    public void train() {
        System.out.println("Saving events");
        saveEvents();
        System.out.println("Events saved");

        String varianceOption = "";
        if (variance > 0)
            varianceOption = " -l2 " + variance;

        String iterationOption = "";
        if (iterations > 0)
            iterationOption = " -max_it " + iterations;

        String mpiOption = "";
        if (numMachines > 1)
            mpiOption = "mpiexec -n " + numMachines + " ";

        IOUtil.runCommand(mpiOption + PATH_TO_ESTIMATE_EXECUTABLE + " -monitor" + " -events_in "
                + eventsFile.getAbsolutePath() + " -params_out " + paramsFile.getAbsolutePath() + iterationOption
                + " -method " + method + varianceOption + " -frtol 1e-10");

        params = new double[fmap.getNumFeatures()];

        try {
            BufferedReader paramReader = new BufferedReader(new InputStreamReader(new FileInputStream(paramsFile)));

            int featureIndex = 0;
            String line = null;
            while ((line = paramReader.readLine()) != null)
                params[featureIndex++] = Double.parseDouble(line);

        }
        catch (IOException e) {
            System.out.println("Could not open parameter file. Check the options for training the model.");
        }

    }

    public void addEvent(int label, Context c) {
        if (events.containsKey(c)) {
            // System.out.println(StringUtil.join(c.contexts));
            events.get(c)[label]++;
        }
        else {
            int[] labels = new int[numLabels];
            labels[label]++;
            events.put(c, labels);
        }
    }

    private void saveEvents() {

        try {
            BufferedWriter eventsWriter = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(
                    new FileOutputStream(eventsFile))));

            // TODO rm
            int testCounter = 0;

            THashSet<String> seenOnce = new THashSet<String>();
            THashSet<String> seenTwice = new THashSet<String>();
            // THashSet<String> seenThrice = new THashSet<String>();

            for (Context c : events.keySet()) {
                for (int m = 0; m < c.contexts.length; m++)
                    if (!seenOnce.add(c.contexts[m]))
                        seenTwice.add(c.contexts[m]);
                // if (!seenTwice.add(c.contexts[m]))
                // seenThrice.add(c.contexts[m]);
            }
            seenOnce = null;
            // seenTwice = null;

            for (Context c : events.keySet()) {
                FeatureCounts fcounts = new FeatureCounts(fmap, numLabels);
                for (int m = 0; m < c.contexts.length; m++)
                    // if (seenThrice.contains(c.contexts[m]))
                    if (seenTwice.contains(c.contexts[m]))
                        fcounts.observeFeature(c.contexts[m]);

                eventsWriter.write(fcounts.toString(events.get(c)));

                // TODO rm
                if ((testCounter++ % 1000) == 0)
                    System.out.print(";");

            }

            // TODO rm
            System.out.println();

            System.out.println(name + " -- " + fmap.getNumFeatures());
            eventsWriter.flush();
            eventsWriter.close();
            events = null;
            fmap.cap();
        }
        catch (IOException e) {
            System.out.println("Unable to save events to " + eventsFile);
            System.out.println(e);
        }
    }

    public String toString() {
        return name;
    }

}
