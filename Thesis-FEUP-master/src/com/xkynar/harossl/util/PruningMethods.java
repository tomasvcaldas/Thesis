

package com.xkynar.harossl.util;




import dianaAlgorithm.MyTreeNode;
import pt.up.fe.specs.contextwa.EnsembleModel;
import pt.up.fe.specs.contextwa.classifiers.ensemble.EnsembleClassifier;


public final class PruningMethods {
    
    private EnsembleModel model = new EnsembleModel();
    
    public PruningMethods() {
        // TODO Auto-generated constructor stub
    }
    
    
    @SuppressWarnings("unchecked")
    public MyTreeNode<String> getNodeToPrune(MyTreeNode<String> root) {
        
        MyTreeNode<String> returnNode = null;
        
        MyTreeNode<String> rightNode = root.getChildren().get(1);
        
        MyTreeNode<String> leftNode = root.getChildren().get(0);
        
        
        if(model.IsLeaf(rightNode) && !model.IsLeaf(leftNode) && model.IsLeaf(leftNode.getChildren().get(0)) && model.IsLeaf(leftNode.getChildren().get(1)) ||
                model.IsLeaf(leftNode) && !model.IsLeaf(rightNode) && model.IsLeaf(rightNode.getChildren().get(1)) && model.IsLeaf(rightNode.getChildren().get(0))  ) {
            return root;
        } else
        
        if( !model.IsLeaf(rightNode)) {
           returnNode = getNodeToPrune(rightNode);
        } if(!model.IsLeaf(leftNode)) {
            returnNode = getNodeToPrune(leftNode);
        }
        
        return returnNode;
        
    }


    public MyTreeNode<String> pruneNode(MyTreeNode<String> nodeToPrun) {
        MyTreeNode<String> newNode = null;
        
        MyTreeNode<String> rightNode = nodeToPrun.getChildren().get(1);
        
        MyTreeNode<String> leftNode = nodeToPrun.getChildren().get(0);
        
        if(model.IsLeaf(rightNode)) {
            newNode.addChild(rightNode);
        } else {
            newNode.addChild(rightNode.getChildren().get(0));
            newNode.addChild(rightNode.getChildren().get(1));
        }
        
        if(model.IsLeaf(leftNode)) {
            newNode.addChild(leftNode);
        } else {
            newNode.addChild(leftNode.getChildren().get(0));
            newNode.addChild(leftNode.getChildren().get(1));
        }
        
        return newNode;
      
    }


  
 
}
    
 
