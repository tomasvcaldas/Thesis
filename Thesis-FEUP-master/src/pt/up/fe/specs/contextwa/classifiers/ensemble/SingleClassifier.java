/**
 * Copyright 2018 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

package pt.up.fe.specs.contextwa.classifiers.ensemble;

import java.util.Arrays;
import java.util.function.Function;

import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.InstancesHeader;

import dianaAlgorithm.MyTreeNode;
import moa.classifiers.Classifier;
import moa.core.Utils;

public class SingleClassifier implements SSLClassifier {

    private Classifier classifier;
    private Function<double[], double[]> voteConverter;
    private int[] classifierInfo;

    public SingleClassifier(Classifier classifier) {
        this(classifier, null);
    }

    public SingleClassifier(Classifier classifier, Function<double[], double[]> voteConverter) {
        this.classifier = classifier;
        this.voteConverter = voteConverter;
        classifierInfo = new int[1];
    }

    @Override
    public void setModelContext(InstancesHeader header) {
        this.classifier.setModelContext(header);
    }

    @Override
    public void prepareForUse() {
        this.classifier.prepareForUse();

    }

    @Override
    public void trainSupervised(Instance instance) {
        trainOnInstance(instance);

    }

    public void trainOnInstance(Instance instance) {
        this.classifier.trainOnInstance(instance);
    }

    @Override
    public void trainUnsupervised(Instance instance) {
        double[] votes = getVotesForInstance(instance);

        if (votes[0] >= 99.9) {
            instance.setClassValue(0);
            trainOnInstance(instance);
        }
    }

    @Override
    public double[] getVotesForInstance(Instance instance) {
        int numClasses = instance.numClasses();

        double[] clVotes = classifier.getVotesForInstance(instance);
        /* */System.out.println("[ENSEMBLE] SIZE: " + clVotes.length);/* */
        clVotes = Arrays.copyOf(clVotes, numClasses);
        /* */System.out.println("[ENSEMBLE] NEWS: " + clVotes.length);/* */

        if (voteConverter != null) {
            clVotes = voteConverter.apply(clVotes);
        } else {
            double sumVotes = Utils.sum(clVotes);
            for (int j = 0; j < numClasses; j++) {
                clVotes[j] += (clVotes[j] / sumVotes) * 100f;
            }
        }

        return clVotes;
    }

    @Override
    public int classify(Instance instance) {
        double[] votes = classifier.getVotesForInstance(instance);

        int maxIndex = Utils.maxIndex(votes);
        if (votes[maxIndex] <= 40) {
            return -1;
        }
        classifierInfo[0] = maxIndex;

        return maxIndex;

    }
    
   
    public int classifyTree(Instance instance,  MyTreeNode<String> root) {
        double[] votes = classifier.getVotesForInstance(instance);
        
        double[][] bait = null;
        return 0;
/*
        int maxIndex = Utils.maxIndex(votes);
        if (votes[maxIndex] <= 40) {
            return -1;
        }
        classifierInfo[0] = maxIndex;

        return maxIndex;
*/
    }


    @Override
    public boolean correctlyClassifies(Instance instance) {
        return classifier.correctlyClassifies(instance);
    }

    @Override
    public int[] getClassificationPerClass() {
        return classifierInfo;
    }

    @Override
    public String getModelDescription() {
        StringBuilder sb = new StringBuilder("-== Classifier ==-\n");
        this.classifier.getDescription(sb, 1);
        return sb.toString();
    }

    @Override
    public int getNumClassifiers() {
        return 1;
    }



}
