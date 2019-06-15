
/*
package com.xkynar.harossl.util;




import java.util.ArrayList;
import java.util.List;

import dianaAlgorithm.MyTreeNode;
import pt.up.fe.specs.contextwa.EnsembleModel;
import pt.up.fe.specs.contextwa.classifiers.ensemble.EnsembleClassifier;


public final class PruningMethods {
    
    //private EnsembleModel model = new EnsembleModel();
    
    public PruningMethods() {
        // TODO Auto-generated constructor stub
    }
    
    
    @SuppressWarnings("unchecked")
    public void getNodeToPrune(MyTreeNode<String> root) {
        
        MyTreeNode<String> returnNode = null;
        
        MyTreeNode<String> rightNode = root.getChildren().get(1);
        
        MyTreeNode<String> leftNode = root.getChildren().get(0);
        
        
        if(model.IsLeaf(rightNode) && !model.IsLeaf(leftNode) && model.IsLeaf(leftNode.getChildren().get(0)) && model.IsLeaf(leftNode.getChildren().get(1)) ||
                model.IsLeaf(leftNode) && !model.IsLeaf(rightNode) && model.IsLeaf(rightNode.getChildren().get(1)) && model.IsLeaf(rightNode.getChildren().get(0))  ) {
            pruneNode(root);
        } else
        
        if( !model.IsLeaf(rightNode)) {
           getNodeToPrune(rightNode);
        } if(!model.IsLeaf(leftNode)) {
            getNodeToPrune(leftNode);
        }
                
    }


    @SuppressWarnings({ "unchecked" })
    public void pruneNode(MyTreeNode<String> nodeToPrun) {
        List<MyTreeNode> newChildList = new ArrayList<>();
         
        MyTreeNode<String> rightNode = nodeToPrun.getChildren().get(1);
        MyTreeNode<String> leftNode = nodeToPrun.getChildren().get(0);

        if(model.IsLeaf(rightNode)) {
            newChildList.add(rightNode);
        } else {
            newChildList.add(rightNode.getChildren().get(0));
            newChildList.add(rightNode.getChildren().get(1));
        }
        
        if(model.IsLeaf(leftNode)) {
            newChildList.add(leftNode);
        } else {
            newChildList.add(leftNode.getChildren().get(0));
            newChildList.add(leftNode.getChildren().get(1));
        }
        nodeToPrun.deleteChildren();
        
        nodeToPrun.addChildren(newChildList);
        
        //return newNode;
      
    }


   


  
 
}
    
 
*/