package dianaAlgorithm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MyTreeNode<T> implements Serializable{
    private T data = null;
    private List<MyTreeNode> children = new ArrayList<>();
    private MyTreeNode parent = null;
    private Elem distHoeff;
    private boolean pruned = false;
    

    public MyTreeNode(T data) {
        this.data = data;
    }
    



    public MyTreeNode() {
        
    }


    public void setHoeff(Elem distHoeff) {
    	this.distHoeff=distHoeff;
    }
    
    public Elem getHoeff(){
    	return this.distHoeff;
    }

    public void addChild(MyTreeNode child) {
        child.setParent(this);
        this.children.add(child);
    }

    public void addChild(T data) {
        MyTreeNode<T> newChild = new MyTreeNode<>(data);
        newChild.setParent(this);
        children.add(newChild);
    }

    public void addChildren(List<MyTreeNode> children) {
        for(MyTreeNode t : children) {
            t.setParent(this);
        }
        this.children.addAll(children);
    }

    public List<MyTreeNode> getChildren() {
        return children;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    private void setParent(MyTreeNode parent) {
        this.parent = parent;
    }

    public MyTreeNode getParent() {
        return parent;
    }
    
    public void deleteChildren() {
        children.clear();
    }
    
    public boolean getPruned() {
        return pruned;
    }
    
    public void setPruned(boolean pruned) {
        this.pruned = pruned;
    }
    
    
    
    
}