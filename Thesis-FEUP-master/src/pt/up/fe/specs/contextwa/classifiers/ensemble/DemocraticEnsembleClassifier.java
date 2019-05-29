package pt.up.fe.specs.contextwa.classifiers.ensemble;

import java.util.List;

import com.yahoo.labs.samoa.instances.Instance;

import dianaAlgorithm.MyTreeNode;
import moa.classifiers.Classifier;
import moa.core.Utils;

public abstract class DemocraticEnsembleClassifier extends EnsembleClassifier {
    private static final float DEFAULT_THRESHOLD = 0.5f;
    private float threshold = 0.5f;

    public DemocraticEnsembleClassifier(Classifier... classifiers) {
        this(DEFAULT_THRESHOLD, classifiers);
    }

    public DemocraticEnsembleClassifier(float threshold, Classifier... classifiers) {
        super(classifiers);
        this.threshold = threshold;
    }

    public DemocraticEnsembleClassifier(List<Classifier> classifiers) {
        this(DEFAULT_THRESHOLD, classifiers);
    }

    public DemocraticEnsembleClassifier(float threshold, List<Classifier> classifiers) {
        super(classifiers);
        this.threshold = threshold;
    }
    
    @Override
    public int classifyTree(Instance instance, MyTreeNode<String> root) {

    	int numClasses = instance.numClasses();// ActivityType.values().length;
        int numClassifiers = getClassifiers().size();
        double[][] votesPerClassifier = getVotesPerClassifier(instance); // raw values
       
        int index = 0;
       
        
        if(root.getChildren().get(0).equals("Hoeff") || root.getChildren().get(0).equals("Hoeff"))
        	index = getClassifiers().size() - 1;
        else if(root.getChildren().get(0).equals("Naive") || root.getChildren().get(0).equals("Naive"))
        {
        	//Incompleto
        //	if(ensemble[0] == false)
        //		index = 0;
        //	else
        //		index = 1;
        }
        else if(root.getChildren().get(0).equals("KNN") || root.getChildren().get(0).equals("KNN"))
        {
        	index = 0;
        }
        
        double[] votesPerInstance = votesPerClassifier[index];
        int maxIndex = Utils.maxIndex(votesPerInstance);
        
        return maxIndex;
        /*
        int numClasses = instance.numClasses();
        int numClassifiers = getClassifiers().size();
        int[] votes = new int[numClasses];
        double[][] votesPerClassifier = getVotesPerClassifier(instance); // raw values
        for (int i = 0; i < numClassifiers; i++) {
            double[] ds = votesPerClassifier[i];
            int prediction = Utils.maxIndex(ds);
            votes[prediction]++;
        }
        
        return 0;
        
        int maxIndex = Utils.maxIndex(votes);
        int value = votes[maxIndex];
        float f = value / (float) numClassifiers;
        if (f > this.threshold) { // only if more than half votes
            return maxIndex;
        }

        return -1;
*/
        //
        //
        // int bayesPred = Utils.maxIndex(bayesClassifier.getVotesForInstance(instance));
        // int hoeffPred = Utils.maxIndex(hoeffdingClassifier.getVotesForInstance(instance));
        // int knnPred = Utils.maxIndex(knnClassifier.getVotesForInstance(instance));
        //
        // // if at least two agree
        // if (bayesPred == hoeffPred) {
        // finalPred = bayesPred;
        // }
        // if (bayesPred == knnPred) {
        // finalPred = bayesPred;
        // }
        // if (hoeffPred == knnPred) {
        // finalPred = hoeffPred;
        // }
        //
        // return finalPred;
    }

    @Override
    public int classify(Instance instance) {

        int numClasses = instance.numClasses();
        int numClassifiers = getClassifiers().size();
        int[] votes = new int[numClasses];
        double[][] votesPerClassifier = getVotesPerClassifier(instance); // raw values
        for (int i = 0; i < numClassifiers; i++) {
            double[] ds = votesPerClassifier[i];
            int prediction = Utils.maxIndex(ds);
            votes[prediction]++;
        }
        int maxIndex = Utils.maxIndex(votes);
        int value = votes[maxIndex];
        float f = value / (float) numClassifiers;
        if (f > this.threshold) { // only if more than half votes
            return maxIndex;
        }

        return -1;

        //
        //
        // int bayesPred = Utils.maxIndex(bayesClassifier.getVotesForInstance(instance));
        // int hoeffPred = Utils.maxIndex(hoeffdingClassifier.getVotesForInstance(instance));
        // int knnPred = Utils.maxIndex(knnClassifier.getVotesForInstance(instance));
        //
        // // if at least two agree
        // if (bayesPred == hoeffPred) {
        // finalPred = bayesPred;
        // }
        // if (bayesPred == knnPred) {
        // finalPred = bayesPred;
        // }
        // if (hoeffPred == knnPred) {
        // finalPred = hoeffPred;
        // }
        //
        // return finalPred;
    }

    @Override
    public boolean correctlyClassifies(Instance instance) {
        int votes = 0;

        List<Classifier> classifiers = getClassifiers();
        int numClassifiers = classifiers.size();
        for (Classifier classifier : classifiers) {
            if (classifier.correctlyClassifies(instance)) {
                votes++;
            }
        }

        return (votes / (float) numClassifiers) > threshold;
    }

    public float getThreshold() {
        return threshold;
    }

    public void setThreshold(float threshold) {
        this.threshold = threshold;
    }
}
