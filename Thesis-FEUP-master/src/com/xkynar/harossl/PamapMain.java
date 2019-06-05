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

package com.xkynar.harossl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;


import com.xkynar.harossl.classifier.HarosslMajorityClassifier;
import com.xkynar.harossl.data.PamapDataFetcher;
import com.xkynar.harossl.data.PamapDataHandler;
import com.xkynar.harossl.util.ActivityType;
import com.xkynar.harossl.util.PamapOptions;
import com.xkynar.harossl.util.PamapReport;
import com.xkynar.harossl.util.JGraph;

import pt.up.fe.specs.contextwa.EnsembleModel;
import pt.up.fe.specs.contextwa.classifiers.ensemble.util.Prediction;
import pt.up.fe.specs.contextwa.data.fetch.DataFetcher;
import pt.up.fe.specs.contextwa.data.handlers.DataHandler;
import dianaAlgorithm.Elem;
import dianaAlgorithm.MyTreeNode;


public class PamapMain extends PamapOptions {

    private static PamapDataHandler dataHandler = new PamapDataHandler();
    private static List<PamapReport> reports = new ArrayList<>();

    private static long totalNumInstances;
    private static double averageAccuracy;
    
    static int [][] confHoeff;
    static int [][] confNaive;
    static int [][] confKNN;
    static double [][] distHoeff;
    static double [][] distNaive;
    static double [][] distKNN;
    
    static MyTreeNode<String> root = new MyTreeNode<>("Root");
	private static double[][] initialHoeff;
	private static double[][] initialNaive;
	private static double[][] initialKNN;
	
	private static ArrayList<Integer> previousline= new ArrayList<Integer>();
	private static boolean splinter = false;
	
	private static JGraph jgraph = new JGraph();

    public static void main(String[] args) throws IOException {
      
        processArguments(args);
        dataHandler.setActiveSensors(SENSORS[0], SENSORS[1], SENSORS[2]);

        long startTime = System.currentTimeMillis();
        run();
        long elapsed = System.currentTimeMillis() - startTime;

        finalReport(elapsed);
        
        for (PamapReport report : reports) {

            createConfusionMatrix(report.getModel());
            
        }
    
    }

    private static void createConfusionMatrix(EnsembleModel model) {
    	confHoeff = new int[dataHandler.getNumClasses()][dataHandler.getNumClasses()];
    	confNaive = new int[dataHandler.getNumClasses()][dataHandler.getNumClasses()];
    	confKNN = new int[dataHandler.getNumClasses()][dataHandler.getNumClasses()];
    	
    	for(int i = 0; i < model.getPredictions().size();i++) {
    		int real = model.getPredictions().get(i).getReal();
    		
    		for(int k = 0; k < model.getclassifierID().size();k++) {
    			if(model.getclassifierID().get(k) == 0) {
        		confKNN[real][model.getPredictions().get(i).getPredictedPerClassifier().get(k)] += 1;
    			}
    			else if(model.getclassifierID().get(k) == 1) {
    			confNaive[real][model.getPredictions().get(i).getPredictedPerClassifier().get(k)] += 1;
    			
    			}else if (model.getclassifierID().get(k) == 2)
    			confHoeff[real][model.getPredictions().get(i).getPredictedPerClassifier().get(k)] += 1;
    		}
    			
    	}
    	
    	String[] act = getElemInit(confHoeff);
    	act = removeActivityStringNotUsed(confHoeff,act);
    	confHoeff = removeActiviesNotUsed(confHoeff);
    	confNaive = removeActiviesNotUsed(confNaive);
    	confKNN = removeActiviesNotUsed(confKNN);
    	
        distHoeff = new double[confHoeff.length][confHoeff.length];
        double[][] distHoeff2 = new double[confHoeff.length][confHoeff.length];
        int incremental = 0;
        
        for(int i=0;i < confHoeff.length;i++) {
        	//for(int j = incremental; j<confHoeff[i].length;j++) {
        	for(int j = 0; j<confHoeff[i].length;j++) {
    			distHoeff2[i][j] = ((float) confHoeff[i][i] + (float) confHoeff[j][j])/((float) confHoeff[i][i] + (float) confHoeff[i][j] + (float) confHoeff[j][i] + (float) confHoeff[j][j]);

        		if(confHoeff[i][i] != 0) {
        			if(incremental == j)
        				distHoeff[i][j] = 0;
        				else
        			distHoeff[i][j] = ((float) confHoeff[i][i] + (float) confHoeff[j][j])/((float) confHoeff[i][i] + (float) confHoeff[i][j] + (float) confHoeff[j][i] + (float) confHoeff[j][j]);
        			
        		}
        		if((j + 1) >= confHoeff[i].length)
        		incremental++;
        	}
        	
        }
        
        distNaive = new double[confNaive.length][confNaive.length];
        double[][] distNaive2 = new double[confNaive.length][confNaive.length];
        int incrementalNaive = 0;
        
        for(int i=0;i < confNaive.length;i++) {
        	//for(int j = incremental; j<confHoeff[i].length;j++) {
        	for(int j = 0; j<confNaive[i].length;j++) {
    			distNaive2[i][j] = ((float) confNaive[i][i] + (float) confNaive[j][j])/((float) confNaive[i][i] + (float) confNaive[i][j] + (float) confNaive[j][i] + (float) confNaive[j][j]);

        		if(confNaive[i][i] != 0) {
        			if(incrementalNaive == j)
        				distNaive[i][j] = 0;
        				else
        			distNaive[i][j] = ((float) confNaive[i][i] + (float) confNaive[j][j])/((float) confNaive[i][i] + (float) confNaive[i][j] + (float) confNaive[j][i] + (float) confNaive[j][j]);
        			
        		}
        		if((j + 1) >= confNaive[i].length)
        		incrementalNaive++;
        	}
        	
        }
        
        distKNN = new double[confKNN.length][confKNN.length];
        double[][] distKNN2 = new double[confKNN.length][confKNN.length];
        int incrementalKNN = 0;
        
        for(int i=0;i < confKNN.length;i++) {
        	//for(int j = incremental; j<confHoeff[i].length;j++) {
        	for(int j = 0; j<confKNN[i].length;j++) {
    			distNaive2[i][j] = ((float) confKNN[i][i] + (float) confKNN[j][j])/((float) confKNN[i][i] + (float) confKNN[i][j] + (float) confKNN[j][i] + (float) confKNN[j][j]);

        		if(confKNN[i][i] != 0) {
        			if(incrementalKNN == j)
        				distKNN[i][j] = 0;
        				else
        				distKNN[i][j] = ((float) confKNN[i][i] + (float) confKNN[j][j])/((float) confKNN[i][i] + (float) confKNN[i][j] + (float) confKNN[j][i] + (float) confKNN[j][j]);
        			
        		}
        		if((j + 1) >= confKNN[i].length)
        			incrementalKNN++;
        	}
        	
        }
        
        initialKNN = distKNN;
        initialHoeff = distHoeff;
        initialNaive = distNaive;
       // dianaiteration(0,distHoeff);
        //String[] act = getElemInit(distHoeff);
        
        //removeActiviesNotUsed(distHoeff,act);
        Elem elem = new Elem(distHoeff,distNaive,act);
        root.setHoeff(elem);
        dianaiteration(root,elem);
        jgraph.createGraph(root);
        testTree(root);
        System.out.println("wait");	
        }
    
    private static String[] getElemInit(int[][] confHoeff2) {
		
    	   String[] act = new String[confHoeff2.length];
     	   int i = 0;
     		
     			for (ActivityType activity : ActivityType.values()) {
     				act[i] = activity.toString();
     				i++;
     			}
     			
     			return act;
     			
	}

	private static int[][] removeActiviesNotUsed(int[][] confHoeff2) {
		
    	for(int i = 0;i < confHoeff2.length;i++)
    	{
    		if(confHoeff2[i][i] == 0) {
    			confHoeff2 = ArrayUtils.remove(confHoeff2, i);
    			confHoeff2 = removeColumn(confHoeff2,i);
    			i--;
    		}
    		
    	}
    	
    	return confHoeff2;
		
	}
	
	private static String[] removeActivityStringNotUsed(int[][] confHoeff2, String[] act) {
		
    	for(int i = 0;i < confHoeff2.length;i++)
    	{
    		if(confHoeff2[i][i] == 0) {
    			act[i] = "ERR";
    		}
    		
    	}
    	
    	for(int i = 0;i < act.length;i++)
    	{
    		if(act[i].equals("ERR")) {
    			act = ArrayUtils.remove(act,i);
    			i--;
    		}
    		
    	}
    	
    	
    	return act;
		
	}

	private static int[][] removeColumn(int[][] distHoeff, int line) {
		for(int i=0;i < distHoeff.length;i++) {
			distHoeff[i] = ArrayUtils.remove(distHoeff[i], line);
		}
		
		return distHoeff;
	}

	private static void testTree(MyTreeNode<String> IterationNode) {
    	PamapDataHandler dataHandler2 =  null;
    //	if(IterationNode.getData().equals("Root"))
    	String[] temp = {IterationNode.getChildren().get(0).getData().toString(),IterationNode.getChildren().get(1).getData().toString()};
    	
    	 dataHandler2 = new PamapDataHandler(temp);
    	
    //	dataHandler2 = new PamapDataHandler(ActivityType.toStringArray());
    	
    	 dataHandler2.setActiveSensors(SENSORS[0], SENSORS[1], SENSORS[2]);
    	 EnsembleModel model = newEnsembledModel(6,dataHandler2);
    	 model.init();
    	 model.train();
    	 model.testTree(root,SENSORS);
    	 
    	 int temp2 = model.numCorrect;
    	 /*
    	    List<Prediction> temp = model.getPredictions();
    	
    	    int[][] tempHoeff = new int[dataHandler2.getNumClasses()][dataHandler2.getNumClasses()];
    	    int[][] tempNaive = new int[dataHandler2.getNumClasses()][dataHandler2.getNumClasses()];
    	
    	    for(int i = 0; i < model.getPredictions().size();i++) {
    		    int real = model.getPredictions().get(i).getReal();
    		
    		    for(int k = 0; k < model.getclassifierID().size();k++) {
    			    if(model.getclassifierID().get(k) == 1) {
    			    tempNaive[real][model.getPredictions().get(i).getPredictedPerClassifier().get(k)] += 1;
    			
    			    }else if (model.getclassifierID().get(k) == 2)
    			    tempHoeff[real][model.getPredictions().get(i).getPredictedPerClassifier().get(k)] += 1;
    		    } 	
    	    } 
    	*/
    	
	}
	
	public static void testMultiTree(MyTreeNode<String> IterationNode) {
	    
	    
	    return null;
	}
	

	private static String[] getElemInit(double[][] distHoeff) {
 	   String[] act = new String[distHoeff.length];
 	   int i = 0;
 		
 			for (ActivityType activity : ActivityType.values()) {
 				act[i] = activity.toString();
 				i++;
 			}
 			
 			return act;
 	
 }
    
    private static void dianaiteration(MyTreeNode<String> PNode, Elem Matrix) {
		
			if(splinter) {
				double[] Avg = null;
				
				if(Matrix.getPickedClassifier().equals("Hoeff"))
				Avg = getAverageDiss(Matrix.getMatrix());
				else if(Matrix.getPickedClassifier().equals("Naive"))
				Avg = getAverageDiss(Matrix.getMatrixNaive());
				
				double[] Avg2 = calc(PNode,Matrix);
				double[] Final = calcDiff(Avg,Avg2);
				
				int newline = checkforpositive(Final);
				
				if(newline == -1) {
					String line = getOtherLines(PNode);
   		    	MyTreeNode<String> child1 = new MyTreeNode<>(line);
   		    	
   		    	PNode.getParent().addChild(child1);
   		    	child1.setHoeff(Matrix);
   		    	
   		    	double SecondMatrix[][] = getSecondMatrix(PNode);
   		    	double SecondMatrixNaive[][] = getSecondMatrixNaive(PNode);
   		    	String[] SecondList = getSecondList(PNode);
   		    	
   		    	Elem newElem = new Elem(SecondMatrix,SecondMatrixNaive,SecondList);	
   		    	PNode.setHoeff(newElem);
   		    	splinter = false;
   		    	MyTreeNode<String> NextN = findNextNode();
   		    	
   		    	if(NextN == null)
   		    		return;
   		    	
   		    	dianaiteration(NextN,NextN.getHoeff());
   		    	
					
				}
				else {
					String deleted = Matrix.getActivityList()[newline];
   		    	
		   		    //	double[][] Tempo = Substitute(PNode,line);
		   	    		
		   		    	double[][] Tempo = ArrayUtils.remove(Matrix.getMatrix(), newline);
		   		    	Tempo = removeColumn(Tempo,newline);
		   		    	
		   		    	double[][] Tempo2 = ArrayUtils.remove(Matrix.getMatrixNaive(), newline);
		   		    	Tempo2 = removeColumn(Tempo2,newline);
		   		    	String[] DeleteActivity = ArrayUtils.remove(Matrix.getActivityList(), newline);
		   		    	
		   		    	Elem Temp = new Elem(Tempo,Tempo2,DeleteActivity,Matrix.getPickedClassifier());
		   		    	PNode.setData(PNode.getData() + "-" + deleted);
		   		    	dianaiteration(PNode,Temp);
					
				}
				
				
			}
			else {
				double sum = 0;
		    	int line = -1;
		    	String MatrixPick = "Hoeff";
		    	
		    	double[] sumList = new double[PNode.getHoeff().getMatrix().length];
		    	
		    	for(int i=0;i < PNode.getHoeff().getMatrix().length;i++) {
		    		double sumLine = 0;

		    		for(int j=0;j<PNode.getHoeff().getMatrix()[i].length;j++) {
		    			sumLine += PNode.getHoeff().getMatrix()[i][j];
		    			
		    		}
		    		
		    		sumLine = sumLine / (PNode.getHoeff().getMatrix().length - 1);
		    		
		    		sumList[i]=sumLine;
		    		
		    		if(sumLine > sum) {
		    			sum = sumLine;
		    			line = i;
		    		}
		    		
		    	}
		    	
		    	for(int i=0;i < PNode.getHoeff().getMatrixNaive().length;i++) {
		    		double sumLine = 0;

		    		for(int j=0;j<PNode.getHoeff().getMatrixNaive()[i].length;j++) {
		    			sumLine += PNode.getHoeff().getMatrixNaive()[i][j];
		    			
		    		}
		    		
		    		sumLine = sumLine / (PNode.getHoeff().getMatrix().length - 1);
		    		
		    		sumList[i]=sumLine;
		    		
		    		if(sumLine > sum) {
		    			sum = sumLine;
		    			line = i;
		    			MatrixPick = "Naive";
		    		}
		    		
		    	}
		    	
		    	if(line == -1)
		    		return;
		    	
		    	String deleted = PNode.getHoeff().getActivityList()[line];
		    	
		    //	double[][] Tempo = Substitute(PNode,line);
	    		
		    	double[][] Tempo = ArrayUtils.remove(PNode.getHoeff().getMatrix(), line);
		    	Tempo = removeColumn(Tempo,line);
		    	
		    	double[][] Tempo2 = ArrayUtils.remove(PNode.getHoeff().getMatrixNaive(), line);
		    	Tempo2 = removeColumn(Tempo2,line);
	    		String[] DeleteActivity = ArrayUtils.remove(PNode.getHoeff().getActivityList(), line);
		    	
	    		Elem Temp = new Elem(Tempo,Tempo2,DeleteActivity,MatrixPick);
	    		
	    		MyTreeNode<String> child1 = new MyTreeNode<>(deleted);
		    	PNode.addChild(child1);
		    	//child1.setHoeff(Tempo);
		    	splinter = true;
		    	dianaiteration(child1,Temp);
		    	
				
			}



}
    private static MyTreeNode<String> calcDiam(List<MyTreeNode> listNode) {

		double max = 0;
		int index = 0;
		
		for(int i = 0;i < listNode.size();i++) {
			double newmax = calcMax(listNode.get(i));
			double newmax2 = calcMaxN(listNode.get(i));
			if(newmax > max) {
				max = newmax;
				index = i;
			}
			
			if(newmax2 > max) {
				max = newmax2;
				index = i;
			}
			
		}
		
		return listNode.get(index);
}

private static double calcMaxN(MyTreeNode myTreeNode) {
double maxValue = 0;

for(int i = 0;i < myTreeNode.getHoeff().getMatrixNaive().length;i++) {
	for(int j = 0;j < myTreeNode.getHoeff().getMatrixNaive()[i].length;j++) {
		if(myTreeNode.getHoeff().getMatrixNaive()[i][j] > maxValue)
			maxValue = myTreeNode.getHoeff().getMatrixNaive()[i][j];
		
	}
}

return maxValue;
}

private static double calcMax(MyTreeNode myTreeNode) {
double maxValue = 0;

for(int i = 0;i < myTreeNode.getHoeff().getMatrix().length;i++) {
	for(int j = 0;j < myTreeNode.getHoeff().getMatrix()[i].length;j++) {
		if(myTreeNode.getHoeff().getMatrix()[i][j] > maxValue)
			maxValue = myTreeNode.getHoeff().getMatrix()[i][j];
		
	}
}

return maxValue;

}


    private static MyTreeNode<String> findNextNode() {
    	List<MyTreeNode> ListNode = new ArrayList<MyTreeNode>();
    	
    	
    	loop(root);
    	ListNode = FNodes;
    	
    	if(ListNode.size() == 0)
    		return null;
    	
    	
    	MyTreeNode<String> Final = calcDiam(ListNode);
    	FNodes.clear();
    	
    	return Final;
    }
    
    static List<MyTreeNode> FNodes = new ArrayList<MyTreeNode>();

    private static void loop(MyTreeNode<String> root2) {
    	List<MyTreeNode> fp = root2.getChildren();
    	
    	if(fp.size() == 0) {
    		String temp = root2.getData();
    		String[] i = temp.split("-");
    		
    		if(i.length >= 2)
    		FNodes.add(root2);
    		
    	}
    	else {
    			for(int i = 0;i < fp.size(); i++) {
    				loop(fp.get(i));
    			}
    	}
    			
    }

    private static String[] getSecondList(MyTreeNode<String> pNode) {
    	String temp = pNode.getData();
    	String[] i = temp.split("-");

    	return i;
    	
    }
    
    private static double[][] getSecondMatrixNaive(MyTreeNode<String> pNode) {
    	String temp = pNode.getData();
    	String[] i = temp.split("-");
    	
    	int[] number = new int[i.length];
    	
    	// retorn index correpondente
    	for(int p = 0;p < i.length;p++) {
    	for(int j = 0;j < root.getHoeff().getActivityList().length;j++) {
    		if(root.getHoeff().getActivityList()[j].equals(i[p]))
    			number[p] = j;

    	}
    	}
    	
    	double[][] Temp = new double[i.length][i.length];
    	/*
    	int index2 = 0;
    	
    	
    	for(int k = 0;k<root.getHoeff().getMatrix().length;k++) {
    		int index = 0;
    		for(int j=0;j < root.getHoeff().getMatrix()[k].length;j++) {
    			if(contain(k,number) && contain(j,number))
    			{
    				Temp[index][index2] = root.getHoeff().getMatrix()[k][j];
    				index++;
    			}
    		}
    		
    		if(index != 0)
    			index2++;
    	}
    	
    	*/
    	for(int t = 0;t < number.length;t++) {
    	for(int k = 0;k < number.length;k++) {
    		Temp[t][k] = root.getHoeff().getMatrixNaive()[number[t]][number[k]]; 
    	}
    	}
    	
    	return Temp;
    }
    
    private static double[][] getSecondMatrix(MyTreeNode<String> pNode) {
    	String temp = pNode.getData();
    	String[] i = temp.split("-");
    	
    	int[] number = new int[i.length];
    	
    	// retorn index correpondente
    	for(int p = 0;p < i.length;p++) {
    	for(int j = 0;j < root.getHoeff().getActivityList().length;j++) {
    		if(root.getHoeff().getActivityList()[j].equals(i[p]))
    			number[p] = j;

    	}
    	}
    	
    	double[][] Temp = new double[i.length][i.length];
    	/*
    	int index2 = 0;
    	
    	
    	for(int k = 0;k<root.getHoeff().getMatrix().length;k++) {
    		int index = 0;
    		for(int j=0;j < root.getHoeff().getMatrix()[k].length;j++) {
    			if(contain(k,number) && contain(j,number))
    			{
    				Temp[index][index2] = root.getHoeff().getMatrix()[k][j];
    				index++;
    			}
    		}
    		
    		if(index != 0)
    			index2++;
    	}
    	
    	*/
    	for(int t = 0;t < number.length;t++) {
    	for(int k = 0;k < number.length;k++) {
    		Temp[t][k] = root.getHoeff().getMatrix()[number[t]][number[k]]; 
    	}
    	}
    	
    	return Temp;
    }
    
    private static String getOtherLines(MyTreeNode<String> pNode) {
    	
    	String p = pNode.getData();
    	String[] i = p.split("-");
    	String fin = "";
    	
    	
    	for(int g = 0;g < pNode.getParent().getHoeff().getMatrix().length;g++) {
    		
    		if(!contain(pNode.getParent().getHoeff().getActivityList()[g],i))
    			fin += "-" + pNode.getParent().getHoeff().getActivityList()[g];
    	}
    	
    	
    	fin = fin.replaceFirst("-", "");
    	
    	return fin;
    	
    	/*String temp = pNode.getData();
    	String[] i = temp.split("-");
    	int[] fin = StringArrToIntArr(i);
    	int[] fin2;
    	
    	String temp2 = (String) pNode.getParent().getData();
    	if(temp2 == "Root")
    		fin2 = createVector();
    	else {
    	String[] i2 = temp2.split("-");
    	fin2 = StringArrToIntArr(i2);
    	}
    	
    	
    	String node = "";
    	
    	for(int j=0;j<fin2.length;j++) {
    		if(!contain(fin2[j],fin)) {
    			node += "-" + fin2[j];
    		}
    		
    	}
    	
    	node = node.replaceFirst("-", "");
    	
    	return node;
    	*/
    }
    
    private static int checkforpositive(double[] final1) {
    	double max = -999;
    	int line = -5;
    	
    	for(int i=0;i<final1.length;i++) {
    		
    		if(max < final1[i]) {
    			max = final1[i];
    			line = i;
    		}
    		
    	}
    	
    	if(max <= 0)
    		return -1;
    	
    	
    	return line;
    	
    }
    
    private static double[] calc(MyTreeNode<String> pNode, Elem matrix) {
    	
    	String p = pNode.getData();
    	String[] i = p.split("-");
    	int[] fin = new int[i.length];
    	
    	/*
    	 *	for(int g = 0;g < fin.length;g++) {
    		for(int p2 = 0;p2 < pNode.getParent().getHoeff().getActivityList().length;p2++) {
    		if(pNode.getParent().getHoeff().getActivityList()[p2].equals(i[g])) {
    			fin[g] = p2;
    		}
    	}
    }
    	 */
    	
    				for(int g = 0;g < fin.length;g++) {
    					for(int p2 = 0;p2 < root.getHoeff().getActivityList().length;p2++) {
    					if(root.getHoeff().getActivityList()[p2].equals(i[g])) {
    						fin[g] = p2;
    					}
    				}
    			}
    	double[] media = null;
    	
    	if(matrix.getPickedClassifier().equals("Hoeff")) {
    	media = new double[initialHoeff.length];
    	
    	for(int j=0;j< fin.length;j++) {
    		
    		for(int k=0;k<initialHoeff.length ;k++) {
    			
    			media[k] += initialHoeff[k][fin[j]];
    			
    			
    		}
    		
    	}
    	}
    	else if(matrix.getPickedClassifier().equals("Naive")) {
    		media = new double[initialNaive.length];
    		
    		for(int j=0;j< fin.length;j++) {
    			
    			for(int k=0;k<initialNaive.length ;k++) {
    				
    				media[k] += initialNaive[k][fin[j]];
    				
    				
    			}
    			
    		}
    		
    	}
    	
    	
    	for(int t = 0;t < media.length; t++) {
    		if(!contain(root.getHoeff().getActivityList()[t],pNode.getParent().getHoeff().getActivityList()))
    				{
    			//media = ArrayUtils.remove(media, t);
    			media[t] = -99;
    				}
    		
    	}
    	
    	for(int t = 0;t < fin.length;t++) {
    		//media = ArrayUtils.remove(media, fin[t]);
    		media[fin[t]] = -99;
    		
    	}
    	
    	for(int t = 0;t < media.length;t++) {
    		if(media[t] == -99) {
    			media = ArrayUtils.remove(media, t);
    			t--;
    		}
    	}
    	
    	for(int t = 0;t < media.length;t++) {
    		media[t] = media[t]/fin.length;
    	}
    	
    	
    	//remove ones 
    	
    	return media;
    }
    
    private static double[] calcDiff(double[] avg, double[] avg2) {
    	
    	double[] diff = new double[avg.length];
    	
    	if(avg.length != avg2.length)
    		return null;
    	
    	for(int i=0;i< avg.length;i++) {
    		diff[i] =avg[i] - avg2[i];
    	}
    	
    	return diff;
    }


    public static int[] StringArrToIntArr(String[] s) {
    	   int[] result = new int[s.length];
    	   for (int i = 0; i < s.length; i++) {
    	      result[i] = Integer.parseInt(s[i]);
    	   }
    	   return result;
    }
    
    private static boolean contain(int i, int[] fin) {
    	for(int k = 0;k < fin.length;k++) {
    		if(fin[k] == i)
    			return true;
    	}
    	return false;
    }

    private static boolean contain(String i, String[] fin) {
    	for(int k = 0;k < fin.length;k++) {
    		if(fin[k].equals(i))
    			return true;
    	}
    	return false;
    }
    
    private static double[][] removeColumn(double[][] distHoeff,int line) {
		for(int i=0;i < distHoeff.length;i++) {
			distHoeff[i] = ArrayUtils.remove(distHoeff[i], line);
		}
		
		return distHoeff;
}
    
    private static double[] getAverageDiss(double[][] distHoeff) {
    	
	  	
    	double[] avg =  new double[distHoeff.length];
    	
    	for(int i=0;i<distHoeff.length;i++) {
		for(int j=0;j < distHoeff.length;j++) {
			//if(distHoeff[i][j] != -99)
			avg[i] += distHoeff[i][j];
			
			
		}
		
		avg[i] = avg[i]/(distHoeff.length - 1);
		
    	}
    	return avg;
    	
}

	private static void run() {
        List<Integer> persons = USERS_LIST;
        System.out.println("List of participants: " + persons);

        long currentTimeMillis = System.currentTimeMillis();
        // First list all the available users in the file
        int numberOfSubjects = persons.size();
        System.out.println("-== Verifying #Samples ==-");

        //DataFetcher dataFetcher = PamapDataFetcher.fetchAll(100, 0); // Just to count number of samples
        //totalNumInstances = dataFetcher.getTotalNumberOfSamples();
        //System.out.println(
        //         "#instances: " + totalNumInstances + "\nTime: " + (System.currentTimeMillis() - currentTimeMillis)
        //              + "ms");
        // numberOfSubjects = 9;
        averageAccuracy = 0;

        // for each person -> leave on participant out
        for (int person : persons) {
            // this.currentPerson = person;

            printInitialMessage(person);
            currentTimeMillis = System.currentTimeMillis();

            averageAccuracy += leaveOneOut(person);

            printCurrentTestEndMessage(person, currentTimeMillis);
        }
        averageAccuracy /= numberOfSubjects;
    }

    private static double leaveOneOut(int person) {
        EnsembleModel model = newEnsembledModel(person);
        model.init();
        System.out.println("1. Supervised train phase");
        // long begin = System.currentTimeMillis();
        model.train();
        System.out.println("2. Test phase");
        long begin = System.currentTimeMillis();
        model.test();
       // int[][] temp = model.getconfusionmatrixHoeff();
        long end = System.currentTimeMillis() - begin;
      
        System.out.println("Test Time: " + end);
        reports.add(new PamapReport(person, model, end));
        return model.numberOfCorrectClassifications() / (double) model.getTotalNumSamples();
    }

    public static EnsembleModel newEnsembledModel(int person) {
        // DataHandler declared as a field (will be the same for all people)
        HarosslMajorityClassifier classifier = new HarosslMajorityClassifier(kValue, ENSEMBLE);
        DataFetcher trainDataFetcher = PamapDataFetcher.fetchIgnore(person, WINDOWSIZE, OVERLAPFACTOR);// SAMPLING_JUMP);
        DataFetcher testDataFetcher = PamapDataFetcher.fetchOnly(person, WINDOWSIZE, OVERLAPFACTOR);// SAMPLING_JUMP);
        EnsembleModel model = new EnsembleModel(classifier, dataHandler, trainDataFetcher, testDataFetcher);
        return model;
    }
    
    public static EnsembleModel newEnsembledModel(int person,DataHandler dataHandler) {
        // DataHandler declared as a field (will be the same for all people)
        HarosslMajorityClassifier classifier = new HarosslMajorityClassifier(kValue, ENSEMBLE);
        DataFetcher trainDataFetcher = PamapDataFetcher.fetchIgnore(person, WINDOWSIZE, OVERLAPFACTOR);// SAMPLING_JUMP);
        DataFetcher testDataFetcher = PamapDataFetcher.fetchOnly(person, WINDOWSIZE, OVERLAPFACTOR);// SAMPLING_JUMP);
        EnsembleModel model = new EnsembleModel(classifier, dataHandler, trainDataFetcher, testDataFetcher);
        return model;
    }

    private static void finalReport(long time) {
        System.out.println("-== Report ==-");
        System.out.printf("Global accuracy: %.2f%%\n", averageAccuracy * 100);
        System.out.println("Time (ms): " + time);
        for (PamapReport report : reports) {

            System.out.println(" -= Person " + report.getUser() + " =- ");
            System.out.println(report.report(REPORT_PREDICTIONS));
        }
    }

    public static void printInitialMessage(int person) {
        System.out.println("-== Person " + person + " as a participant ==-");
        System.out.println(" -= Configuration =-");
        System.out.println(" kNN: " + kValue);
        System.out.println(" Window: " + WINDOWSIZE);
        System.out.println(" Overlap: " + (int) (OVERLAPFACTOR * 100) + "%");
        System.out.println(" Classifiers: " + HarosslMajorityClassifier.activeClassifiers(ENSEMBLE));
        System.out.println(" Sensors: " + dataHandler.activeSensors());
    }

    public static void printCurrentTestEndMessage(int person, long currentTimeMillis) {
        System.out.println(
                "-== Person " + person + " time:  " + (System.currentTimeMillis() - currentTimeMillis) + "ms ==-");
    }
    
    

}