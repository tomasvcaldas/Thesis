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

package com.xkynar.harossl.classifier;

import java.util.ArrayList;
import java.util.List;

import moa.classifiers.Classifier;
import moa.classifiers.bayes.NaiveBayes;
import moa.classifiers.trees.HoeffdingTree;
import pt.up.fe.specs.contextwa.classifiers.ensemble.MajorityEnsembleClassifier;

public class HarosslMajorityClassifier extends MajorityEnsembleClassifier {

    // protected NaiveBayes bayesClassifier;
    // protected HoeffdingTree hoeffdingClassifier;
    // protected kNN knnClassifier;
    // private boolean[] ensemble;
    private static int DEFAULT_kNN_LIMIT = 2000; 
    

    public HarosslMajorityClassifier(int kValue, boolean[] ensemble) {
        super(init(kValue, DEFAULT_kNN_LIMIT, ensemble),ensemble);
        
        
         
    }

    public HarosslMajorityClassifier(int kValue, int kNNSize, boolean[] ensemble) {
        super(init(kValue, kNNSize, ensemble),ensemble);
         
    }

    public static List<Classifier> init(int kValue, int kNNSize, boolean[] ensemble) {
        List<Classifier> classifiers = new ArrayList<>();
        if (ensemble[0]) {
            classifiers.add(new kNN(kValue, kNNSize));
        }
        if (ensemble[1]) {
            classifiers.add(new NaiveBayes());
        }
        if (ensemble[2]) {
            classifiers.add(new HoeffdingTree());
        }
        return classifiers;
    }

    public static String activeClassifiers(boolean[] ensemble) {
        String classifiers = "";
        if (ensemble[0]) {
            classifiers += "kNN";
        }
        if (ensemble[1]) {
            if (!classifiers.isEmpty()) {
                classifiers += ",";
            }
            classifiers += "Naive Bayes";
        }
        if (ensemble[2]) {
            if (!classifiers.isEmpty()) {
                classifiers += ",";
            }
            classifiers += "Hoeffding Tree";
        }
        return classifiers;
    }
    

    

}
