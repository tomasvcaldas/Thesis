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

package pt.up.fe.specs.contextwa;

import static java.lang.Math.toIntExact;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.text.AbstractDocument.LeafElement;

import com.xkynar.harossl.classifier.HarosslMajorityClassifier;
import com.xkynar.harossl.data.PamapDataFetcher;
import com.xkynar.harossl.data.PamapDataHandler;
import com.xkynar.harossl.util.ProgressBarString;
import com.yahoo.labs.samoa.instances.Attribute;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Instances;

import dianaAlgorithm.MyTreeNode;
import pt.up.fe.specs.contextwa.classifiers.ensemble.SSLClassifier;
import pt.up.fe.specs.contextwa.classifiers.ensemble.util.Prediction;
import pt.up.fe.specs.contextwa.data.fetch.ConditionalDataFetcher;
import pt.up.fe.specs.contextwa.data.fetch.DataFetcher;
import pt.up.fe.specs.contextwa.data.handlers.DataHandler;

/**
 * Framework connecting the classifier with data fetch and data handling for testing purposes. In this framework one
 * defines the classifier to use, how to fetch the training data and the test data and the class that is responsible to
 * handle with raw values (list of {@link Instance}).
 * 
 * @author tdrc
 *
 */
public class EnsembleModel {
    private SSLClassifier classifier;
    private DataHandler dataHandler;
    private DataFetcher trainDataFetcher;
    private DataFetcher testDataFetcher;
    private int numtrain;
    private int totalNumSamples;
    private boolean printBar = true;

    private double[][] acc_class; // Two positions: 0: true classification, 1: guessed classification
    private List<Prediction> predictions;
    private boolean savePredictions;
    private int[] acc_ensemble;

    private List<String> classifyInfo;
    private int numSamplesCorrect;
    private String[] classifiernome = {"kNN","Naive","Hoeff"};
    private ArrayList<Integer> classifierID = new ArrayList<Integer>();
    private List <List <Instance>> Instancias;
    
    public static int numCorrect = 0;
    public int NumeroInstancias;
    public int NumeroInstanciasNotEvaluated = 0;
    public ArrayList<Integer> accuracy = new ArrayList<Integer>();
    public ArrayList<Integer> NumberOfInstances = new ArrayList<Integer>();
    public static Map<String, Integer> leafCorrectValues = new HashMap<String, Integer>();
    public static Map<String, Integer> leafNumberOfValues = new HashMap<String, Integer>();
    

    public EnsembleModel(SSLClassifier classifier, DataHandler dataHandler,
            DataFetcher trainDataFetcher,
            DataFetcher testDataFetcher) {
    		this.classifier = classifier;
    		
        String c = classifier.getModelDescription();
        int j = 0;
        for(int i = 0; i < classifiernome.length;i++) {
        	if(c.contains(classifiernome[i])) {
        		classifierID.add(i);
        	}
        		
        }

        this.dataHandler = dataHandler;
        this.trainDataFetcher = trainDataFetcher;
        this.testDataFetcher = testDataFetcher;
        assertNotNull();
        this.classifyInfo = new ArrayList<>();
        acc_class = new double[2][dataHandler.getNumClasses()]; // matrix for accuracy per class
        acc_ensemble = new int[classifier.getNumClassifiers()];
       // confusionmatrixHoeff = new int[dataHandler.getNumClasses()][dataHandler.getNumClasses()];
        predictions = new ArrayList<>();
        savePredictions = true;
    }

    public void init() {
        classifier.prepareForUse();
    }
    
    public void init(EnsembleModel model) {
        model.classifier.prepareForUse();
    }

    public void train() {
        trainDataFetcher.restart();
        int percentage = 0;
        long totalNumberOfSamples = trainDataFetcher.getTotalNumberOfSamples();

        while (trainDataFetcher.hasNext()) {
            trainSupervised(trainDataFetcher.next());
            if (printBar && totalNumberOfSamples > 0) {
                long numInstancesRead = trainDataFetcher.getNumberSamplesRead();
                percentage = ProgressBarString.printBar(percentage, numInstancesRead,
                        totalNumberOfSamples);
            }
        }

        if (printBar) {
            ProgressBarString.printFullBar();
        }
        trainDataFetcher.close();
    }

    public void test() {
        testDataFetcher.restart();
        int percentage = 0;
        long totalNumberOfSamples = testDataFetcher.getTotalNumberOfSamples();
       
        while (testDataFetcher.hasNext()) {
        		
            List<Instance> window = testDataFetcher.next();
            Instance finalInstance = dataHandler.extractFeatures(window);
            test(finalInstance);
            if (printBar && totalNumberOfSamples > 0) {
                long numInstancesRead = testDataFetcher.getNumberSamplesRead();
                percentage = ProgressBarString.printBar(percentage, numInstancesRead,
                        totalNumberOfSamples);
            }
            
        }

        if (printBar) {
            ProgressBarString.printFullBar();
        }
        
        testDataFetcher.close();
    }

    /////////////////////////////////////////////////////////////////////////////
    /////////////////////////// TEST METHODS//////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////
    /**
     * normal test method - for raw instances
     * 
     * @param window
     */
    public void test(List<Instance> window) {

        // Instance instance = createInstance(window);
        Instance instance = dataHandler.extractFeatures(window);
        test(instance);
    }

    /**
     * 
     * @param root 
     * @param Sensors 
     * @param model 
     * @param instance
     * @param ensemble 
     */
    
	public void testTree(MyTreeNode<String> root, boolean[] Sensors) {
		 testDataFetcher.restart();
	        int percentage = 0;
	        long totalNumberOfSamples = testDataFetcher.getTotalNumberOfSamples();
	      
	        while (testDataFetcher.hasNext()) {
	        		
	            List<Instance> window = testDataFetcher.next();
	            Instance finalInstance = dataHandler.extractFeatures(window);
	            
	            countInstances(dataHandler.getRealClass(),root);
	            testTree(finalInstance, root,window, dataHandler.getRealClass());
	            if (printBar && totalNumberOfSamples > 0) {
	                long numInstancesRead = testDataFetcher.getNumberSamplesRead();
	                percentage = ProgressBarString.printBar(percentage, numInstancesRead,
	                        totalNumberOfSamples);
	            }
	            
	        }
	        
	        NumeroInstancias = ChildNode1.size() + ChildNode2.size();
	        
	        if(!IsLeaf(root.getChildren().get(0)))
	        testTreeIteration(root.getChildren().get(0),ChildNode1,Sensors);
	        else
	        sumCorrectInstances(ChildNode1);
	        if(!IsLeaf(root.getChildren().get(1)))
	        testTreeIteration(root.getChildren().get(1),ChildNode2,Sensors);
	        else
	        sumCorrectInstances(ChildNode2);

	        if (printBar) {
	            ProgressBarString.printFullBar();
	        }
	        
	        testDataFetcher.close();
	        
	        System.out.println("Instances values: ");
	        
	        for (String key : leafNumberOfValues.keySet()) {
	           // System.out.println(key + ": total -> " + leafNumberOfValues.get(key) + " | real -> " + leafCorrectValues.getOrDefault(key, 0));
	            double correctInstancePercentage = leafCorrectValues.getOrDefault(key, 0) / leafNumberOfValues.get(key);
	            System.out.println(key + " : " + correctInstancePercentage );
	        }
	        
            
	        
	        
	        
	        
		
	}
	
	private void countInstances(String realClass, MyTreeNode<String> root) {
		for(int i = 0;i < root.getHoeff().getActivityList().length;i++) {
			NumberOfInstances.add(0);
		}
		
		for(int i = 0;i < root.getHoeff().getActivityList().length;i++) {
			if(root.getHoeff().getActivityList()[i].equals(realClass)) {
				NumberOfInstances.set(i, NumberOfInstances.get(i) + 1);
				return;
			}
			
		}
		
	}

	private void sumCorrectInstances(List<InstanceModified> ListaInstancias) {
			int acc = 0;
		for(int i = 0;i <ListaInstancias.size(); i++) {
			double temp = ListaInstancias.get(i).getInstance().value(0);
			String temp2 = dataHandler.getClassification((int) temp);
			if(ListaInstancias.get(i).getRealClass().equals(temp2)) {
				numCorrect++;
				acc++;
			}
			
		}
		
		accuracy.add(acc);
		
	}
	
	private void sumCorrectInstances(List<InstanceModified> ListaInstancias, EnsembleModel model) {
	    
		int acc = 0;
		for(int i = 0;i <ListaInstancias.size(); i++) {
			double temp = ListaInstancias.get(i).getInstance().value(0);
			String temp2 = model.dataHandler.getClassification((int) temp);
			if(ListaInstancias.get(i).getRealClass().equals(temp2)) {
				numCorrect++;
				acc++;
			}	
		}
		System.out.println(acc);
		accuracy.add(acc);
		
	}

	private void testTreeIteration(MyTreeNode<String> Node, List<InstanceModified> childNode12, boolean[] Sensors) {
		
		PamapDataHandler dataHandler2 = null;
	    //	if(IterationNode.getData().equals("Root"))
	    String[] temp = {Node.getChildren().get(0).getData().toString(),Node.getChildren().get(1).getData().toString()};
	    	
	    	dataHandler2 = new PamapDataHandler(temp);
	    	dataHandler2.setActiveSensors(Sensors[0], Sensors[1], Sensors[2]);
	    	
	    	HarosslMajorityClassifier classifier = (HarosslMajorityClassifier) this.classifier;
	        DataFetcher trainDataFetcher = this.trainDataFetcher;
	        DataFetcher testDataFetcher = this.testDataFetcher;
	        EnsembleModel model = new EnsembleModel(classifier, dataHandler2, trainDataFetcher, testDataFetcher);
	        
	        List<InstanceModified> Path1 = new ArrayList<InstanceModified>();
			List<InstanceModified> Path2 = new ArrayList<InstanceModified>();
			
	    	 model.init();
	    	 model.train(model);
	    	// model.train();
	    	 model.test(childNode12, model, Path1, Path2);
	    	 
	    	if(!IsLeaf(Node.getChildren().get(0)))
	    		testTreeIteration(Node.getChildren().get(0), Path1, Sensors);
	    	else {
	    	    sumCorrectInstances(Path1,model);
	    	}
	    		
	    	
	    	if(!IsLeaf(Node.getChildren().get(1)))
	    		testTreeIteration(Node.getChildren().get(1), Path2, Sensors);
	    	else {
	    	    sumCorrectInstances(Path2,model);
	    	}
	    		
	    		
		
	}
	
	public void updateLeafCorrectValue(String instance) {
	    if(leafCorrectValues.containsKey(instance) ) {
	        leafCorrectValues.put(instance, leafCorrectValues.get(instance)+1);
	    } else {
	        leafCorrectValues.put(instance, 1);
	    }
	}
	
	public void updateLeafNumberOfValues(String instance) {
	    if(leafNumberOfValues.containsKey(instance) ) {
	        leafNumberOfValues.put(instance, leafNumberOfValues.get(instance)+1);
        } else {
            leafNumberOfValues.put(instance, 1);
        }
	}

	private void test(List<InstanceModified> childNode12, EnsembleModel model, List<InstanceModified> Path1, List<InstanceModified> Path2)
	{
		
		for(int i = 0;i < childNode12.size(); i++)
		{
			test(childNode12.get(i),model, Path1, Path2);
			
		}
		
	
		
	}

	private void test(InstanceModified instance, EnsembleModel model, List<InstanceModified> Path1, List<InstanceModified> Path2) {
		 model.totalNumSamples++;
	        int classificationIndex = model.classifier.classify(instance.getInstance());
	        // matrix
	        //int realClassValue = getClassValue(instance);
	        String givenClass = model.dataHandler.getClassification(classificationIndex); // classifier opinion
	        String realClass = instance.getRealClass();
	        List<String> newClasses = new ArrayList<String>();
	        newClasses.add(model.dataHandler.getClassification(0));
	        newClasses.add(model.dataHandler.getClassification(1));

	        
	        Attribute att = new Attribute("class", newClasses);
	        Instances temp = instance.getInstance().dataset();
	        
	        
	        Instances replace = changeInstance(instance,att);
	        
	        instance.getInstance().setDataset(replace);
	        instance.getInstance().setValue(0, classificationIndex);
	        InstanceModified newInstance = new InstanceModified(instance.getInstance(),instance.getRealClass());
	        
	        if(classificationIndex == 0)
	        Path1.add(newInstance);
	        else
	        Path2.add(newInstance);
	        
		
	}

	private Instances changeInstance(InstanceModified instance, Attribute att) {
		
		List<Attribute> attributes = new ArrayList<Attribute>();

        attributes.add(att);
        for (int i = 0; i < instance.getInstance().numAttributes() - 1; i++) {
            attributes.add(new Attribute("attr_" + i));
        }
		
		
		Instances dataInfo = new Instances("har", attributes, 0);
        dataInfo.setClassIndex(0);
        
        return dataInfo;
	}

	private void train(EnsembleModel model) {
		/*
		for(int i = 0;i < instances.size(); i++)
		{
			trainSupervised(instances.get(i),model);
                percentage = ProgressBarString.printBar(percentage, i,
                        instances.size());
            
			
		}
		*/
		 trainDataFetcher.restart();
	        int percentage = 0;
	        long totalNumberOfSamples = trainDataFetcher.getTotalNumberOfSamples();

	        while (trainDataFetcher.hasNext()) {
	            trainSupervisedModified(trainDataFetcher.next(),model);
	            if (printBar && totalNumberOfSamples > 0) {
	                long numInstancesRead = model.trainDataFetcher.getNumberSamplesRead();
	                percentage = ProgressBarString.printBar(percentage, numInstancesRead,
	                        totalNumberOfSamples);
	            }
	        }

	        if (printBar) {
	            ProgressBarString.printFullBar();
	        }
	        trainDataFetcher.close();
		
		 if (printBar) {
	            ProgressBarString.printFullBar();
	        }
			
		
	}

	private void trainSupervisedModified(List<Instance> next, EnsembleModel model) {
		Instance inst = model.dataHandler.extractFeatures(next);
        if(inst != null)
        model.classifier.trainSupervised(inst);
		
	}

	private void trainSupervised(Instance instance, EnsembleModel model) {
		
		numtrain++;
        // create instances
      //  Instance inst = dataHandler.extractFeatures(window);
        model.classifier.trainSupervised(instance);
		
	}

	List<InstanceModified> ChildNode1 = new ArrayList<InstanceModified>();
	List<InstanceModified> ChildNode2 = new ArrayList<InstanceModified>();
	
    private void testTree(Instance instance,MyTreeNode<String> root, List<Instance> window, String RealClass) {
		// TODO Auto-generated method stub
    	MyTreeNode<String> IteratingNode = root;
    	if(instance == null)
    	{
    		NumeroInstanciasNotEvaluated++;
    		return;
    	}
    	
    	totalNumSamples++;
    	//int realClassValue = getClassValue(instance);
    	
    	//Onde passa um 
    	int classificationIndex = classifier.classifyTree(instance,root);
    	
    	if(classificationIndex == 0) {
    		InstanceModified temp = new InstanceModified(instance,RealClass);
    		ChildNode1.add(temp);
    	}
    	else {
    		InstanceModified temp = new InstanceModified(instance,RealClass);
    		ChildNode2.add(temp);
    	}
    	     	
    	
  
    /*	
    	while(!IsLeaf(IteratingNode)) {
    	 double[][] votes = classifier.classifyTree(instance);
    	 
    	 MyTreeNode<String> Child1 = root.getChildren().get(0);
    	 MyTreeNode<String> Child2 = root.getChildren().get(1);
    	 
    	// CompareBothVotes(Child1,Child2);
    	 
    	 
    	}
    	*/ 
    	 
		
	}
    
    private boolean IsLeaf(MyTreeNode<String> IteratingNode) {
    	if(IteratingNode.getData().equals("Root"))
    		return false;
    	
    	String temp = IteratingNode.getData();
    	String[] i = temp.split("-");

    	if(i.length > 1)
    		return false;
    	else
    		return true;
    	
    }

	public void test(Instance instance) {
        totalNumSamples++;
        int classificationIndex = classifier.classify(instance);
        // matrix
        int classValue = getClassValue(instance);
        acc_class[0][classValue] += 1; // first line: total number of instances per class
        int[] num_ensemble = classifier.getClassificationPerClass(); // second line: number of instances right
                                                                    // classified per class
        if (classificationIndex == -1) {
        	int realClassValue = getClassValue(instance);
        	Prediction prediction = new Prediction(realClassValue);
        	 for (int k = 0; k < num_ensemble.length; k++) {
                 prediction.addClassifierPrediction(num_ensemble[k]);
             }
        	 predictions.add(prediction);
            return;
        }
        
        int realClassValue = getClassValue(instance);

        // classify information per instance
        String givenClass = dataHandler.getClassification(classificationIndex); // classifier opinion
        String realClass = dataHandler.getClassification(realClassValue); // instance class

        realClass = realClass + " - " + givenClass;
        classifyInfo.add(realClass);

        Prediction prediction = new Prediction(realClassValue, classificationIndex);
        if (classifier.correctlyClassifies(instance)) {
            numSamplesCorrect++;
            // count for accuracy per class
            acc_class[1][classificationIndex] += 1;
            // count accuracy per classifier
            for (int k = 0; k < num_ensemble.length; k++) {
                if (num_ensemble[k] == classificationIndex) { // if classifier guesses correctly
                    acc_ensemble[k] += 1;
                }
                prediction.addClassifierPrediction(num_ensemble[k]);
            }
        } else {
            for (int k = 0; k < num_ensemble.length; k++) {
                prediction.addClassifierPrediction(num_ensemble[k]);
            }
        }
        predictions.add(prediction);
    }

    public static int getClassValue(Instance instance) {
        return (int) instance.classValue();
    }

    // normal test method - for processed instances
    public void testPreprocessedWindow(List<Instance> window) {

        for (Instance inst : window) {
            // total of instances
            totalNumSamples++;

            test(inst);
        }

    }

    /////////////////////////////////////////////////////////////////////////////
    ////////////////////////// TRAINING METHODS///////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////

    /**
     * train with raw instance window
     * 
     * @param window
     * @throws IOException
     */
    public void trainSupervised(List<Instance> window) {
        // number of instances used in train phase
        numtrain++;
        // create instances
        Instance inst = dataHandler.extractFeatures(window);
        if(inst != null)
        classifier.trainSupervised(inst);

    }

    /**
     * train with processed instances
     * 
     * @param window
     * @throws IOException
     */
    public void trainSupervisedPreprocessedWindow(List<Instance> window) {
        // processed data
        for (Instance inst : window) {
            classifier.trainSupervised(inst);
        }
    }

    /**
     * simple semi-supervised strategy with raw instances
     * 
     * @param window
     */
    public void trainUnsupervised(List<Instance> window) {
        // number of instances used in train phase
        numtrain++;
        // create instances
        Instance inst = dataHandler.extractFeatures(window);
        classifier.trainUnsupervised(inst);

    }

    /**
     * simple semi-supervised strategy with processed instances
     * 
     * @param window
     */
    public void trainUnsupervisedPreprocessedWindow(List<Instance> window) {
        for (Instance inst : window) {
            classifier.trainUnsupervised(inst);
        }
    }

    /**
     * Guarantee that nothing that is crucial is missing
     */
    private void assertNotNull() {
        if (classifier == null) {
            throw new NullPointerException("The ensemble classifier must be defined");
        }
        if (dataHandler == null) {
            throw new NullPointerException("The data handler class must be defined");
        }
        if (trainDataFetcher == null) {
            throw new NullPointerException("The training data fetcher must be defined");
        }
        if (testDataFetcher == null) {
            throw new NullPointerException("The test data fetcher must be defined");
        }
    }

    public int getNumtrain() {
        return numtrain;
    }

    public double[][] getAcc_class() {
        return acc_class;
    }

    public int[] getCorrectClassifPerClassifier() {
        return acc_ensemble;
    }

    public List<String> getClassifyInfo() {
        return classifyInfo;
    }

    public int getTotalNumSamples() {
        return totalNumSamples;
    }

    public int numberOfCorrectClassifications() {
        return numSamplesCorrect;
    }

    /**
     * get number of instances used in training
     * 
     * @return
     */
    public int getNumTrain() {
        return numtrain;
    }

    public String getModelDescription() {
        return classifier.getModelDescription();
    }

    public EnsembleModel setClassifier(SSLClassifier classifier) {
        this.classifier = classifier;
        return this;
    }

    public EnsembleModel setDataHandler(DataHandler dataHandler) {
        this.dataHandler = dataHandler;
        return this;
    }

    public EnsembleModel setTrainDataFetcher(ConditionalDataFetcher trainDataFetcher) {
        this.trainDataFetcher = trainDataFetcher;
        return this;
    }

    public EnsembleModel setTestDataFetcher(ConditionalDataFetcher testDataFetcher) {
        this.testDataFetcher = testDataFetcher;
        return this;
    }

    public boolean isPrintBar() {
        return printBar;
    }

    public EnsembleModel setPrintBar(boolean printBar) {
        this.printBar = printBar;
        return this;
    }

    public List<Prediction> getPredictions() {
        return predictions;
    }

    public boolean savePredictions() {
        return savePredictions;
    }

    public EnsembleModel setSavePredictions(boolean savePredictions) {
        this.savePredictions = savePredictions;
        return this;
    }

    public DataHandler getDataHandler() {
        return dataHandler;
    }
    
    public ArrayList<Integer> getclassifierID(){
    	return classifierID;
    }


}
