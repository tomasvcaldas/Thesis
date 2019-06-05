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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.text.AbstractDocument.LeafElement;

import com.xkynar.harossl.classifier.HarosslMajorityClassifier;
import com.xkynar.harossl.data.PamapDataFetcher;
import com.xkynar.harossl.data.PamapDataHandler;
import com.xkynar.harossl.util.ProgressBarString;
import com.xkynar.harossl.util.PruningMethods;
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
    public static Map<String, Integer> nodesCount = new HashMap<String, Integer>();
    public static Map<String, Integer> nodesGuessCount = new HashMap<String, Integer>();
    public static Map<String, Integer> nodesWrong = new HashMap<String, Integer>();
    
    public static Map<String, int[]> confusionMatrixes = new HashMap<String, int[]>();
    
    public static Map<String, int[]> finalConfusionMatrixes = new HashMap<String, int[]>();
    
    public PruningMethods pruningMethods = new PruningMethods();
    
    
    public EnsembleModel() {};
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
	        
	        updateFinalTrueNegatives(NumeroInstancias);
	        
	        if (printBar) {
	            ProgressBarString.printFullBar();
	        }
	        
	        testDataFetcher.close();
	        
	        System.out.println("Confusion Matrixes Starting Accuracy: ");
            Iterator<Map.Entry <String, int[]> > it = confusionMatrixes.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, int[]> pair = it.next();
                double accuracy = (double)(pair.getValue()[0] + pair.getValue()[3] )/(pair.getValue()[0] + pair.getValue()[1] + pair.getValue()[2] + pair.getValue()[3]);
                System.out.println(pair.getKey() +": " +  accuracy );
            }
	        
	        System.out.println("Final Confusion Matrixes Accuracy: ");
            Iterator<Map.Entry <String, int[]> > it2 = finalConfusionMatrixes.entrySet().iterator();
            while (it2.hasNext()) {
                Map.Entry<String, int[]> pair = it2.next();
                double accuracy = (double)( pair.getValue()[0] + pair.getValue()[3] ) / (pair.getValue()[0] + pair.getValue()[1] + pair.getValue()[2] + pair.getValue()[3]);
                System.out.println(pair.getKey() +": " +  accuracy );
            }
            
           /* System.out.println("Nodes count: ");
            Iterator<Map.Entry <String, Integer> > it3 = nodesCount.entrySet().iterator();
            while (it3.hasNext()) {
                Map.Entry<String, Integer> pair = it3.next();
                System.out.println(pair.getKey() +": " +  pair.getValue() );
            }
            
            System.out.println("Nodes guess count: ");
            Iterator<Map.Entry <String, Integer> > it4 = nodesGuessCount.entrySet().iterator();
            while (it4.hasNext()) {
                Map.Entry<String, Integer> pair = it4.next();
                System.out.println(pair.getKey() +": " +  pair.getValue() );
            }*/
             
           MyTreeNode<String> nodeToPrun = pruningMethods.getNodeToPrune(root);
           
           MyTreeNode<String> prunnedNode = pruningMethods.pruneNode(nodeToPrun);
           
 
         
            
            
            
            
	         
	        System.out.println("ACABOU");
	        
	        /*for (String key : leafNumberOfValues.keySet()) {
	            System.out.println(key + ": total -> " + leafNumberOfValues.get(key) + " | real -> " + leafCorrectValues.getOrDefault(key, 0));
	            //double correctInstancePercentage = leafCorrectValues.getOrDefault(key, 0) / leafNumberOfValues.get(key);
	            //System.out.println(key + " : " + correctInstancePercentage );
	        }*/
		
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
			createFinalConfusionMatrixes(ListaInstancias.get(i).getRealClass(), temp2);
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
			createFinalConfusionMatrixes(ListaInstancias.get(i).getRealClass(), temp2);
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
	
	/*public void updateLeafCorrectValue(String instance) {
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
	}*/
	
	public void updateCorrectNodeCount(String node) {
	    if(nodesCount.containsKey(node) ) {
	        nodesCount.put(node, nodesCount.get(node)+1);
        } else {
            nodesCount.put(node, 1);
        }
	}
	
	public void updateGuessedNodeCount(String node) {
        if(nodesGuessCount.containsKey(node) ) {
            nodesGuessCount.put(node, nodesGuessCount.get(node)+1);
        } else {
            nodesGuessCount.put(node, 1);
        }
    }
	
	public void updateWrongNodes(String node) {
        if(nodesWrong.containsKey(node) ) {
            nodesWrong.put(node, nodesWrong.get(node)+1);
        } else {
            nodesWrong.put(node, 1);
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
	        
	        /*if (model.dataHandler.getClassification(0).contains(realClass)) {
	            updateCorrectNodeCount(model.dataHandler.getClassification(0));
	        } else if (model.dataHandler.getClassification(1).contains(realClass)) {
	            updateCorrectNodeCount(model.dataHandler.getClassification(1));
	        } 
	        
	        if(classificationIndex == 0) {
	            updateGuessedNodeCount(model.dataHandler.getClassification(0));
	        } else if (classificationIndex == 1) {
	            updateGuessedNodeCount(model.dataHandler.getClassification(1));
	        }*/
	        
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
    	
    	if(root.getChildren().get(0).getData().toString().contains(RealClass)) {
    	    updateCorrectNodeCount(root.getChildren().get(0).getData().toString());
    	} else if (root.getChildren().get(1).getData().toString().contains(RealClass)) {
    	    updateCorrectNodeCount(root.getChildren().get(1).getData().toString());
    	} else {
    	    updateWrongNodes(root.getData().toString());
    	}
    	
    	
    	if(classificationIndex == 0) {
    	    updateGuessedNodeCount(root.getChildren().get(0).getData().toString());
    		InstanceModified temp = new InstanceModified(instance,RealClass);
    		ChildNode1.add(temp);
    	}  
    	else {
    	    updateGuessedNodeCount(root.getChildren().get(1).getData().toString());
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
    
    
    
    public boolean IsLeaf(MyTreeNode<String> IteratingNode) {
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
    
    public void createConfusionMatrixes(String real, String predicted) {
        
        if(real == predicted) {
            addTruePositiveToConfusionMatrix(predicted);
        } else if (real != predicted ) {
            addFalsePositiveToConfusionMatrix(predicted);
            addFalseNegativeToConfusionMatrix(real);
        }
        
    }
    
    public void updateTrueNegatives(int size) {
        Iterator<Map.Entry <String, int[]> > it = confusionMatrixes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, int[]> pair = it.next();
            int trueNegatives = pair.getValue()[0] + pair.getValue()[1] + pair.getValue()[2];
            pair.getValue()[3] = size - trueNegatives;
            pair.setValue(pair.getValue());
        }
        
        
    }
    
    public void addTruePositiveToConfusionMatrix(String activity) {
        
        if(confusionMatrixes.containsKey(activity) ) {
            confusionMatrixes.get(activity)[0]++;
            confusionMatrixes.put(activity, confusionMatrixes.get(activity));
        } else {
            int[] array =  new int[] {1,0,0,0};
            confusionMatrixes.put(activity, array );
        }
    }
   
   
    public void addFalsePositiveToConfusionMatrix(String activity) {
        
        if(confusionMatrixes.containsKey(activity) ) {
            confusionMatrixes.get(activity)[1]++;
            confusionMatrixes.put(activity, confusionMatrixes.get(activity));
        } else {
            int[] array =  new int[] {0,1,0,0};
            confusionMatrixes.put(activity, array );
        }
    }

    public void addFalseNegativeToConfusionMatrix(String activity) {
    
        if(confusionMatrixes.containsKey(activity) ) {
            confusionMatrixes.get(activity)[2]++;
            confusionMatrixes.put(activity, confusionMatrixes.get(activity));
        } else {
            int[] array =  new int[] {0,0,1,0};
            confusionMatrixes.put(activity, array );
        }
    }

    public void addTrueNegativeToConfusionMatrix(String activity) {
    
        if(confusionMatrixes.containsKey(activity) ) {
            confusionMatrixes.get(activity)[3]++;
            confusionMatrixes.put(activity, confusionMatrixes.get(activity));
        } else {
            int[] array =  new int[] {0,0,0,1};
            confusionMatrixes.put(activity, array );
        }
    }
    
    public void createFinalConfusionMatrixes(String real, String predicted) {
        
        if(real.equals(predicted)) {
            addFinalTruePositiveToConfusionMatrix(predicted);
        } else if (real != predicted ) {
            addFinalFalsePositiveToConfusionMatrix(predicted);
            addFinalFalseNegativeToConfusionMatrix(real);
        }
        
    }
    
    public void updateFinalTrueNegatives(int size) {
        Iterator<Map.Entry <String, int[]> > it = finalConfusionMatrixes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, int[]> pair = it.next();
            int trueNegatives = pair.getValue()[0] + pair.getValue()[1] + pair.getValue()[2];
            pair.getValue()[3] = size - trueNegatives;
            pair.setValue(pair.getValue());
        }
        
        
    }
    
    public void addFinalTruePositiveToConfusionMatrix(String activity) {
        
        if(finalConfusionMatrixes.containsKey(activity) ) {
            finalConfusionMatrixes.get(activity)[0]++;
            finalConfusionMatrixes.put(activity, finalConfusionMatrixes.get(activity));
        } else {
            int[] array =  new int[] {1,0,0,0};
            finalConfusionMatrixes.put(activity, array );
        }
    }
    
   
    public void addFinalFalsePositiveToConfusionMatrix(String activity) {
        
        if(finalConfusionMatrixes.containsKey(activity) ) {
            finalConfusionMatrixes.get(activity)[1]++;
            finalConfusionMatrixes.put(activity, finalConfusionMatrixes.get(activity));
        } else {
            int[] array =  new int[] {0,1,0,0};
            finalConfusionMatrixes.put(activity, array );
        }
    }

    public void addFinalFalseNegativeToConfusionMatrix(String activity) {
    
        if(finalConfusionMatrixes.containsKey(activity) ) {
            finalConfusionMatrixes.get(activity)[2]++;
            finalConfusionMatrixes.put(activity, finalConfusionMatrixes.get(activity));
        } else {
            int[] array =  new int[] {0,0,1,0};
            finalConfusionMatrixes.put(activity, array );
        }
    }

    public void addFinalTrueNegativeToConfusionMatrix(String activity) {
    
        if(finalConfusionMatrixes.containsKey(activity) ) {
            finalConfusionMatrixes.get(activity)[3]++;
            finalConfusionMatrixes.put(activity, finalConfusionMatrixes.get(activity));
        } else {
            int[] array =  new int[] {0,0,0,1};
            finalConfusionMatrixes.put(activity, array );
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
    
    
    public Map<String, int[]> getConfusionMatrixes() {
        return confusionMatrixes;
    }


}
