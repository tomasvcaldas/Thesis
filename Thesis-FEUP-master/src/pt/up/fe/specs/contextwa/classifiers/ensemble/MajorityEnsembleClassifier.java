package pt.up.fe.specs.contextwa.classifiers.ensemble;

import java.util.List;

import com.yahoo.labs.samoa.instances.Instance;

import dianaAlgorithm.MyTreeNode;
import moa.classifiers.Classifier;
import moa.core.Utils;

public class MajorityEnsembleClassifier extends EnsembleClassifier {

    public int[] classifierInfo;
    public double votes;
    private boolean[] ensemble;

    public MajorityEnsembleClassifier(Classifier... classifiers) {
        super(classifiers);
        init();
    }

    public MajorityEnsembleClassifier(List<Classifier> classifiers, boolean[] ensemble) {
        super(classifiers);
        this.ensemble = ensemble;
        init();
    }

    private void init() {
        classifierInfo = new int[getClassifiers().size()];
    }

    // ensemble votes per instance
    @Override
    public int classify(Instance instance) {
        int numClasses = instance.numClasses();// ActivityType.values().length;
        int numClassifiers = getClassifiers().size();
        double[][] votesPerClassifier = getVotesPerClassifier(instance); // raw values
        double[] votes = new double[numClasses];
        for (int i = 0; i < numClassifiers; i++) {
            double[] votesPerInstance = votesPerClassifier[i];
            double votesSum = Utils.sum(votesPerInstance);
            classifierInfo[i] = Utils.maxIndex(votesPerInstance);
            // System.out.println(votes.length + " vs " + votesPerInstance.length);
            for (int j = 0; j < numClasses; j++) {
                votes[j] += (votesPerInstance[j] / votesSum);
            }
        }
        for (int j = 0; j < numClasses; j++) {
            votes[j] = votes[j] / numClassifiers * 100f;
        }

        int maxIndex = Utils.maxIndex(votes);
        // int real = (int) instance.classValue();
        // System.out.print(maxIndex + " vs " + real);

        if (votes[maxIndex] <= 40) {
            // System.out.println(" = -1");
            return -1;
        }

        this.votes = maxIndex;
        // System.out.println(" = " + maxIndex);

        return maxIndex;

    }
    
    @Override
    public int classifyTree(Instance instance, MyTreeNode<String> root) {
        int numClasses = instance.numClasses();// ActivityType.values().length;
        int numClassifiers = getClassifiers().size();
        double[][] votesPerClassifier = getVotesPerClassifier(instance); // raw values
       
        int index = 0;
        /*int ramo = 1;
        String cenas = root.getChildren().get(0).getHoeff().getPickedClassifier();
        
        if(cenas != null)
        	ramo = 0;
        	
        if(root.getChildren().get(ramo).getHoeff().getPickedClassifier().equals("Hoeff"))
        	index = getClassifiers().size() - 1;
        else if(root.getChildren().get(ramo).getHoeff().getPickedClassifier().equals("Naive"))
        {
        	if(ensemble[0] == false)
        		index = 0;
        	else
        		index = 1;
        }
        else if(root.getChildren().get(ramo).getHoeff().getPickedClassifier().equals("KNN"))
        {
        	index = 0;
        }*/
        
        
        for(int i = 0; i < root.getChildren().size(); i++) {
            if(root.getChildren().get(i).getHoeff().getPickedClassifier() != null) {
 
             if(root.getChildren().get(i).getHoeff().getPickedClassifier().equals("Hoeff"))
                 index = getClassifiers().size() - 1;
             else if(root.getChildren().get(i).getHoeff().getPickedClassifier().equals("Naive")){
                 if(ensemble[0] == false)
                     index = 0;
                 else
                     index = 1;
                 } else if(root.getChildren().get(i).getHoeff().getPickedClassifier().equals("KNN")) {
                     index = 0;
                 }
            }
        }
        
        double[] votesPerInstance = votesPerClassifier[index];
        int maxIndex = Utils.maxIndex(votesPerInstance);
        
        return maxIndex;
        
        
        /*

        int maxIndex = Utils.maxIndex(votes);
        // int real = (int) instance.classValue();
        // System.out.print(maxIndex + " vs " + real);

        if (votes[maxIndex] <= 40) {
            // System.out.println(" = -1");
            return -1;
        }

        this.votes = maxIndex;
        // System.out.println(" = " + maxIndex);

        return maxIndex;
*/
    }

    @Override
    public boolean correctlyClassifies(Instance instance) {
        int prediction = classify(instance);
        int real = (int) instance.classValue();
        return prediction == real;
    }

    @Override
    public int[] getClassificationPerClass() {
        // System.out.println(Arrays.toString(classifierInfo));
        return classifierInfo;
    }

    public double getVotes() {
        return votes;
    }

    public boolean[] getensemble() {
    	return ensemble;
    }

}
