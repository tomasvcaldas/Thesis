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

package pt.up.fe.specs.contextwa.classifiers.ensemble.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.testng.mustache.Model;

import pt.up.fe.specs.contextwa.data.handlers.DataHandler;

public class Prediction {
    private int real;
    private int predictedEnsembled;
    private List<Integer> predictedPerClassifier;

    public Prediction(int real) {
        this.real = real;
        this.predictedPerClassifier = new ArrayList<>();
    }
    public Prediction(int real, int predictedEnsembled) {
        super();
        this.real = real;
        this.predictedEnsembled = predictedEnsembled;
        this.predictedPerClassifier = new ArrayList<>();
    }

    public List<Integer> getPredictedPerClassifier() {
        return predictedPerClassifier;
    }

    public void setPredictedPerClassifier(List<Integer> predictedPerClassifier) {
        this.predictedPerClassifier = predictedPerClassifier;
    }

    public void addClassifierPrediction(int predicted) {
        predictedPerClassifier.add(predicted);
    }

    public int getPredictedEnsembled() {
        return predictedEnsembled;
    }

    public void setPredictedEnsembled(int predictedEnsembled) {
        this.predictedEnsembled = predictedEnsembled;
    }

    public int getReal() {
        return real;
    }

    public void setReal(int real) {
        this.real = real;
    }

    @Override
    public String toString() {
        return "real: " + getReal()
                + ", predicted: " + getPredictedEnsembled()
                + ", p/classif: " + getPredictedPerClassifier();
    }

    public String toString(DataHandler handler) {
        handler.getClassification(predictedEnsembled);
        return "real: " + handler.getClassification(getReal())
                + ", predicted: " + handler.getClassification(getPredictedEnsembled())
                + ", p/classif: " + predictedToString(handler);
    }

    public String toCSV(DataHandler handler) {
        handler.getClassification(predictedEnsembled);
        return handler.getClassification(getReal())
                + ", " + handler.getClassification(getPredictedEnsembled())
                + ", "
                + predictedToString(handler);
    }

    public String predictedToString(DataHandler handler) {
        return getPredictedPerClassifier().stream().map(handler::getClassification).collect(Collectors.joining(", "));
    }

    public String toCSV() {
        return getReal()
                + ", " + getPredictedEnsembled()
                + ", " + getPredictedPerClassifier();
    }
}
