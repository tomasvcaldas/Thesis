package pt.up.fe.specs.contextwa.classifiers.ensemble;

import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.InstancesHeader;

import dianaAlgorithm.MyTreeNode;

/**
 * A semi-supervised learning classifier must follow this interface
 * 
 * @author tdrc
 *
 */
public interface SSLClassifier {
    void setModelContext(InstancesHeader header);

    void prepareForUse();

    void trainSupervised(Instance instance);

    void trainUnsupervised(Instance instance);

    double[] getVotesForInstance(Instance instance);

    int classify(Instance instance, int numClasses);
    
    int classify(Instance instance);

    boolean correctlyClassifies(Instance instance);

    int[] getClassificationPerClass();

    String getModelDescription();

    int getNumClassifiers();

	int classifyTree(Instance instance, MyTreeNode<String> root);
    
}
