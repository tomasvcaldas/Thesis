package dianaAlgorithm;

import java.io.Serializable;
import java.util.Hashtable;

public class Elem implements Serializable {
	
	double[][] distHoeff;
	double[][] distNaive;
	String[] activity;
	String PickedClassifier;
	
	public Elem(double[][] distHoeff,double[][] distNaive, String[] activity,String PickedClassifier) {
    	this.distHoeff=distHoeff;
    	this.distNaive=distNaive;
    	this.activity=activity;
    	this.PickedClassifier = PickedClassifier;
	}
	
	public Elem(double[][] distHoeff,double[][] distNaive, String[] activity) {
    	this.distHoeff=distHoeff;
    	this.distNaive=distNaive;
    	this.activity=activity;
	}
	
	  public void setHoeff(double[][] distHoeff) {
	    	this.distHoeff=distHoeff;
	    }
	  
	  public String getPickedClassifier() {
	    	return PickedClassifier;
	    }
	  
	  public void setNaive(double[][] distNaive) {
	    	this.distNaive=distNaive;
	    }
	  
	  public void setActivity(String[] activity) {
	    	this.activity=activity;
	    }
	  
	  public double[][] getMatrix(){
		  return distHoeff;
	  }
	  
	  public double[][] getMatrixNaive(){
		  return distNaive;
	  } 
	  
	  public String[] getActivityList(){
		  return activity;
	  }
	  
}
