/**
 * @author Fei Chen
 * feichen@cse.msu.edu
 * Computer Science and Engineering
 * Michigan State University
 * 12/03/2008
 */

package resolutionTree;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;

import com.sun.xacml.AbstractPolicy;
import com.sun.xacml.Policy;
import com.sun.xacml.PolicySet;

//import diversity.Node;
import xEngineVerifier.*;

// Referenced classes of package diversity:
//            Edge, Interval, Rule, Sequence

public class tnode
{
    private int _left;
    private int _right;
    private ArrayList _children = new ArrayList();;
    private boolean _isLeaf;
    private String name;
    private String algorithm;
    private String type;
    private xReqNode xreq = null;
    private boolean isstored = false;
    private boolean finalized = false;
 
    public void setReqFlagBool(boolean b)
    {
    	isstored = b;
    }
    
    public boolean getReqFlagBool()
    {
    	return this.isstored ;
    }   

    public void setFinalizedFlagBool(boolean b)
    {
    	finalized = b;
    }  
 
    public boolean getFinalizedFlagBool()
    {
    	return this.finalized;
    }  
    
    public void setxReqNode(xReqNode xreq)
    {
    	this.xreq = xreq;
    }  
    public void emptyxReqNode()
    {
    	setReqFlagBool(false);
    	setFinalizedFlagBool(false);
    	this.xreq = null;
    }  
    
    public xReqNode getxReqNode()
    {
        return xreq;
    }   

    public String getType()
    {
        return type;
    }
 
    public void setType(String a)
    {
    	type = a;
    }
    public String getAlg()
    {
        return algorithm;
    }
 
    public void setAlg(String a)
    {
    	algorithm = a;
    }
    
    public String getName()
    {
        return name;
    }
 
    public void setName(String a)
    {
    	name = a;
    }
    
    public int getLeft()
    {
        return _left;
    }

    public int getRight()
    {
        return _right;
    }

    public void setLeft(int i)
    {
        _left = i;
    }

    public void setRight(int i)
    {
        _right = i;
    }
    
    public ArrayList get_childrenList()
    {
        return _children;
    }

    public void set_childrenList(ArrayList a)
    {
    	_children = a;
    }

    public void addChild(int i, tnode son)
    {
        _children.add(i, son);
    }

    public tnode getChild(int i)
    {
        return (tnode)_children.get(i);
    }

    public ArrayList getAllChild()
    {
        return _children;
    }

    public int getChildrenNum()
    {
        return _children.size();
    }

	public void setInfo(AbstractPolicy p) {
		// TODO Auto-generated method stub
		setName(p.getId().toString());
		
		String split[] = p.getCombiningAlg().getIdentifier().toString().split(":");
		if (split.length > 0) {setAlg(split[split.length-1]);}

		if (p instanceof PolicySet){setType("PolicySet");}
		else if (p instanceof Policy){setType("Policy");}
//		System.out.println(p.getId() + " : " + p.getCombiningAlg().getIdentifier());	
	}

	public void printInfo(String indent) {
		// TODO Auto-generated method stub
	
//		System.out.println(indent + getName() + getAlg() + getType() + " : " + pgetChildren().size() + e.hasMoreElements());
//		System.out.println(indent + getName() + getAlg() + getType() + " : "  + "  [" + getLeft() + ","+ getRight() + "]  size : " + getChildrenNum());
		System.out.println(indent + getType() + " : " + getName() + " : "+getAlg() + "  [" + getLeft() + ","+ getRight() + "]  size : " + getChildrenNum());
	}
}
