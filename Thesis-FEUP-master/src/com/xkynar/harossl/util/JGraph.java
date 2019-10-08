/**
 *  Copyright 2019 SPeCS.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package com.xkynar.harossl.util;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JApplet;
import javax.swing.JFrame;

import org.jgrapht.Graph;
import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;

import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxConstants;

import dianaAlgorithm.MyTreeNode;


public final class JGraph {
    
    public DefaultDirectedGraph<String, DefaultEdge> g;
    
    public JGraph() {}
    
    public void createGraph(MyTreeNode<String> Node, String fileName, int user) {
        
        
        new File("src/test/resources/user" + user).mkdirs();
        File imgFile = new File("src/test/resources/user" + user + "/" + fileName + ".png");
        try {
            imgFile.createNewFile();
        } catch (IOException e) {
            System.out.println("Error message: " +  e);
        }
     
        g = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
        
     
       
        g.addVertex(Node.getData().toString());
        addGraphVertexes(Node);
        addGraphEdges(Node);
        
        JGraphXAdapter<String, DefaultEdge> graphAdapter = new JGraphXAdapter<String, DefaultEdge>(g);
        mxIGraphLayout layout = new mxHierarchicalLayout(graphAdapter);
        layout.execute(graphAdapter.getDefaultParent());
        graphAdapter.getStylesheet().getDefaultEdgeStyle().put(mxConstants.STYLE_NOLABEL, "1");
               
        BufferedImage image = mxCellRenderer.createBufferedImage(graphAdapter, null, 2, Color.WHITE, true, null);
        
        try {
            ImageIO.write(image, "PNG", imgFile);
            System.out.println("Tree created!"); 
        } catch (IOException e) {
            System.out.println("Error message: " +  e);
        }
    }
           
    
    
    private void addGraphVertexes(MyTreeNode<String> Node) {
        
        for(int i = 0; i < Node.getChildren().size(); i++) {
            g.addVertex(Node.getChildren().get(i).getData().toString());
        }
       
      
        for(int i = 0; i < Node.getChildren().size(); i++) {
            if(!IsLeaf(Node.getChildren().get(i))) {
                addGraphVertexes(Node.getChildren().get(i));
            }
        }
       
         
    }
    
    private void addGraphEdges(MyTreeNode<String> Node) {
        
        for(int i = 0; i < Node.getChildren().size(); i++) {
            g.addEdge(Node.getData().toString(), Node.getChildren().get(i).getData().toString());
        }
        
     
        for(int i = 0; i < Node.getChildren().size(); i++) {
            if(!IsLeaf(Node.getChildren().get(i))) {
                addGraphEdges(Node.getChildren().get(i));
            }
        }
        
        
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
    
 
}
    
 
