package pt.up.fe.specs.contextwa.classifiers.ensemble;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.InstancesHeader;

import moa.classifiers.Classifier;
import moa.core.Utils;

public abstract class EnsembleClassifier implements SSLClassifier {
    private List<Classifier> classifiers;
    public int[] classifierInfo;

    private Map<Classifier, Function<double[], double[]>> voteConverter;

    public EnsembleClassifier(Classifier... classifiers) {
        this(Arrays.asList(classifiers));
    }

    public EnsembleClassifier(List<Classifier> classifiers) {
        this.setClassifiers(classifiers);
        this.voteConverter = new HashMap<>();
    }

    @Override
    public void setModelContext(InstancesHeader header) {
        getClassifiers().forEach(c -> c.setModelContext(header));
    }

    @Override
    public void prepareForUse() {
        getClassifiers().forEach(Classifier::prepareForUse);
    }

    /**
     * Votes in raw format
     * 
     * @param instance
     * @return
     */
    protected double[][] getVotesPerClassifier(Instance instance, int numClasses) {
        // int numClasses = instance.numClasses();
        int ensembleSize = this.getClassifiers().size();
        double[][] votePerClassifier = new double[ensembleSize][];

        for (int i = 0; i < ensembleSize; i++) {
            Classifier cl = getClassifiers().get(i);
            double[] clVotes = cl.getVotesForInstance(instance);
            /* *System.out.println("[ENSEMBLE] SIZE: " + clVotes.length);/* */
            clVotes = Arrays.copyOf(clVotes ,numClasses);
            /* *System.out.println("[ENSEMBLE] NEWS: " + clVotes.length);/* */
            votePerClassifier[i] = clVotes;
        }
        return votePerClassifier;
    }
    
    
    /**
     * Votes in raw format
     * 
     * @param instance
     * @return
     */
    protected double[][] getVotesPerClassifier(Instance instance) {
        // int numClasses = instance.numClasses();
        int ensembleSize = this.getClassifiers().size();
        double[][] votePerClassifier = new double[ensembleSize][];

        for (int i = 0; i < ensembleSize; i++) {
            Classifier cl = getClassifiers().get(i);
            double[] clVotes = cl.getVotesForInstance(instance);
            /* *System.out.println("[ENSEMBLE] SIZE: " + clVotes.length);/* */
            clVotes = Arrays.copyOf(clVotes, instance.numClasses());
            /* *System.out.println("[ENSEMBLE] NEWS: " + clVotes.length);/* */
            votePerClassifier[i] = clVotes;
        }
        return votePerClassifier;
    }

    @Override
    public double[] getVotesForInstance(Instance instance) {
        int numClasses = instance.numClasses();
        int ensembleSize = this.getClassifiers().size();
        double[] votes = new double[numClasses];
        double[][] votePerClassifier = new double[ensembleSize][];

        for (int i = 0; i < ensembleSize; i++) {
            Classifier cl = getClassifiers().get(i);
            double[] clVotes = cl.getVotesForInstance(instance);
            /* */System.out.println("[ENSEMBLE] SIZE: " + clVotes.length);/* */
            clVotes = Arrays.copyOf(clVotes, numClasses);
            /* */System.out.println("[ENSEMBLE] NEWS: " + clVotes.length);/* */

            if (voteConverter.containsKey(cl)) {
                votePerClassifier[i] = voteConverter.get(cl).apply(clVotes);
            } else {
                double sumVotes = Utils.sum(clVotes);
                for (int j = 0; j < numClasses; j++) {
                    clVotes[j] += (clVotes[j] / sumVotes) * 100f;
                }
            }
        }

        for (int i = 0; i < numClasses; i++) {

            double sumVotes = 0;
            for (int j = 0; j < ensembleSize; j++) {
                sumVotes += votePerClassifier[j][i];
            }
            votes[i] = sumVotes / ensembleSize;
        }
        return votes;
    }

    @Override
    public void trainSupervised(Instance instance) {
        // knnClassifier.
        // trainingHasStarted();
        trainOnInstance(instance);
    }

    // default method
    @Override
    public void trainUnsupervised(Instance instance) {
        double[] votes = getVotesForInstance(instance);
        int maxIndex = Utils.maxIndex(votes);

        if (votes[maxIndex] >= 99.9) {
            instance.setClassValue(maxIndex);
            trainOnInstance(instance);
        }
    }

    // Utility
    private void trainOnInstance(Instance instance) {
        // System.out.println("instance-->" + instance);
        // System.out.println("class-->" + instance.classValue());
        getClassifiers().forEach(c -> c.trainOnInstance(instance));
    }

    @Override
    public String getModelDescription() {
        StringBuilder sb = new StringBuilder("-== Ensemble Classifier ==-\n");
        getClassifiers().forEach(c -> c.getDescription(sb, 1));
        return sb.toString();
    }

    public List<Classifier> getClassifiers() {
        return classifiers;
    }

    public void setClassifiers(List<Classifier> classifiers) {
        this.classifiers = classifiers;
    }

    public void addVotesConverter(Classifier cl, Function<double[], double[]> converter) {
        this.voteConverter.put(cl, converter);
    }

    @Override
    public int getNumClassifiers() {
        return classifiers.size();
    }
    
}
