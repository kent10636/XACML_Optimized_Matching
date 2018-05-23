/**
 * @author Fei Chen
 * feichen@cse.msu.edu
 * Computer Science and Engineering
 * Michigan State University
 * 12/03/2008
 */

package resolutionTree;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.xml.parsers.*;

import org.apache.log4j.Logger;
import org.w3c.dom.*;
import org.xml.sax.*;

import com.sun.xacml.*;
import com.sun.xacml.ctx.Result;
import com.sun.xacml.ctx.Status;
import com.sun.xacml.finder.PolicyFinder;
import com.sun.xacml.finder.PolicyFinderModule;
import com.sun.xacml.finder.PolicyFinderResult;
import commonImpelmentation.*;
import xEngineVerifier.xReqNode;

//public class resolutionTree extends PolicyFinderModule implements ErrorHandler{

public class resolutionTree {
	protected static Logger logger = Logger.getLogger("resolutionTree.class");
	protected PolicyFinder finder;
	int Rule_Sequence = 0;

	Func func ;
	TargetFunc targetFunc ;
	Mapper mapper;
	
	PrintStream outS = null;
	PrintStream outFWRfile = null;
	
	// the root policy
	protected File listFile;

	// policy hierarchy
	protected DefaultMutableTreeNode policyRoot;
	MrgrvPolicyFinderModule policyTree ;

	tnode rootR ;

	public tnode getRootR() {
		return this.rootR;
	}
	
	public void clearResTree(tnode tnode) {
		// TODO Auto-generated method stub
		if (tnode == null) return;
		for (int i=0; i < tnode.getChildrenNum() ; i++){
			tnode childnode = (tnode)tnode.getChild(i);	
			
			if (childnode.getFinalizedFlagBool() == true || childnode.getReqFlagBool() == true){		
				clearResTree(childnode);
			}
		}
		tnode.emptyxReqNode();
	}
	
	
	public xReqNode getFinalReqNode(tnode tnode) {
		// TODO Auto-generated method stub
		
		if (tnode == null) return null;
		
		if (tnode.getFinalizedFlagBool() == true){
			xReqNode returnxreq = tnode.getxReqNode();
			this.clearResTree(tnode);
			return returnxreq;		
		}
		
		for (int i=0; i < tnode.getChildrenNum() ; i++){
			 
			
			tnode childnode = (tnode)tnode.getChild(i);
			
			if (childnode.getReqFlagBool() == true){
				
				xReqNode new_xreq = getFinalReqNode((tnode)childnode);
				
				if (new_xreq  != null){
					
					xReqNode old_xreq= tnode.getxReqNode();
					
					if (old_xreq != null){
						
						tnode.setxReqNode(getProperxreq (tnode.getAlg(), new_xreq , old_xreq));				
					} else {
						tnode.setxReqNode(new_xreq)	;			
							
					}
					
				}
		}
		}
		
			
		xReqNode returnxreq = tnode.getxReqNode();
		this.clearResTree(tnode);
		return returnxreq;
	}
	 
	// setFinalizedFlagBool(boolean b)
	
	public xReqNode locateFindxReqNode(tnode tnode, xReqNode xreq) {
		// TODO Auto-generated method stub
		if (xreq == null) {return null;}
		if (xreq.get_decision()==-1 || xreq.get_xacml_rule_num() == -1) {return null;}
		
	
		for (int i=0; i < tnode.getChildrenNum() ; i++){
			int val = xreq.get_xacml_rule_num();
			
			tnode childnode = (tnode)tnode.getChild(i);
				
			
			if (childnode.getFinalizedFlagBool() == false && val <= childnode.getRight() && val >= childnode.getLeft()){
				xReqNode xNode = locateFindxReqNode((tnode)childnode, xreq);
				if (xNode != null){
	
	//				System.out.println("policySet located----------");
	//				tnode.printInfo(" ");				
	//				xNode.printReqInfo();
	//				System.out.println("policySet located--end--------");	
					
					// This is applicable, return it....
					if (isFinalNode(tnode.getAlg(), xNode, tnode)){
	
						this.clearResTree(tnode);
						tnode.setFinalizedFlagBool(true);
						return xNode;
					}
					else {
						//if (tnode.getReqFlagBool() == false){
						tnode.setReqFlagBool(true);
						if (tnode.getxReqNode() == null) tnode.setxReqNode(xNode);
						//}
						return null;
					}				
				}
				else {
					tnode.setReqFlagBool(true);
					return null;
				}
			}
		}
		
		if (tnode.getType().equals("Policy")){
			xReqNode xNode = xreq;
	//		tnode.printInfo(" ");
	//		System.out.println("policy located");xreq.printReqInfo();
			if (isFinalNode(tnode.getAlg(), xNode, tnode)){
				
				tnode.setReqFlagBool(false);
				tnode.setFinalizedFlagBool(true);
				tnode.setxReqNode(null);
				return xNode;
			}
			else {
				//if (tnode.getReqFlagBool() == false){
				tnode.setReqFlagBool(true);
				if (tnode.getxReqNode() == null) tnode.setxReqNode(xNode);
				//}
					return null;
			}		
		}
	
			
		return null;
	}
	
	
	private boolean isFinalNode(String alg, xReqNode xreq, tnode tnode) {
		// TODO Auto-generated method stub
		
		if (alg.equals("permit-overrides") && xreq.get_decision()==1){
			return true;
		}else if (alg.equals("deny-overrides") && xreq.get_decision()==0){
			return true;
		}else if (alg.equals("first-applicable")){
			
			if (tnode.getReqFlagBool() == true){
				return false;
			} else {return true;}
		}
	
		return false;
	}
	
	public boolean locatexReqNode(tnode tnode, xReqNode xreq){
		
	//	String indent = makeindent(spaceGap);
	//	tnode.printInfo(indent);
		
	//	System.out.println(indent + tnode.getName() + "  [" + tnode.getLeft() + ","+ tnode.getRight() + "]  size : " + tnode.getChildrenNum());
		
		if (xreq == null) return false;
		if (xreq.get_decision()==-1) return false;
		
		for (int i=0; i < tnode.getChildrenNum() ; i++){
			int val = xreq.get_xacml_rule_num();
			
			tnode childnode = (tnode)tnode.getChild(i);
			
			if (val <= childnode.getRight() && val >= childnode.getLeft()){
			
				if ( locatexReqNode((tnode)childnode, xreq) == true){
					tnode.setReqFlagBool(true);
					return true;};
			 
				
			} 
		}
		
			if (tnode.getType().equals("Policy")){
				
				xReqNode old_xreq= tnode.getxReqNode();
				
				
				
				
				if (old_xreq != null){
					
					tnode.setxReqNode(getProperxreq (tnode.getAlg(), xreq, old_xreq));				
				} else {
					tnode.setxReqNode(xreq)	;			
						
				}
				
	//			System.out.println("located");
	//			tnode.getxReqNode().printReqInfo();
				
				tnode.setReqFlagBool(true);
				return true;
			}
			
			return false;
	}
	
	private xReqNode getProperxreq(String alg, xReqNode xreq, xReqNode old_xreq) {
		// TODO Auto-generated method stub
		// guess what situation, it should be changed...
		if (alg.equals("permit-overrides")){
			
			if (xreq.get_decision()==1){
				return xreq;
			} else{
				return old_xreq;
			}
			
		} else if (alg.equals("deny-overrides")){	
	
			if (xreq.get_decision()==0){
				return xreq;
			} else{
				return old_xreq;
			}
			
		} else {
			if (xreq.get_xacml_rule_num() < old_xreq.get_xacml_rule_num()){
				return xreq;
			} 	
		}
		
		return old_xreq;
	}
	
	
	
	public resolutionTree(String listFile) {
		this.listFile = new File(listFile);
		
	 
	/*
		try {
			outS = new PrintStream (new FileOutputStream (log));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	
	
		try {
			outFWRfile = new PrintStream (new FileOutputStream (outFWR));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	
	*/	
		this.func = new Func(outS);
		this.targetFunc = new TargetFunc(func, outS);
		this.mapper = new Mapper(targetFunc, outS, 15);
		func.setMapper(mapper);
		mapper.setFunc(func);
		
		policyTree = new MrgrvPolicyFinderModule(listFile);
	}
	
	
	/**
	 * Called when the <code>PolicyFinder</code> initializes. This lets your
	 * code keep track of the finder, which is especially useful if you have
	 * to create policy sets.
	 *
	 * @param finder the <code>PolicyFinder</code> that's using this module
	 */
	public void init(PolicyFinder finder) {
		// a second initializer that lets you keep track of the finder that
		// is using this class...this information is needed when you try
		// to instantiate a policy set, but may be useful for other tasks
		this.finder = finder;
		policyTree.init(finder);
		loadAll();
	}
	
	private void loadAll() {
		
		System.out.println("load all");
		
		/*
	//	policies = new Hashtable();
		policyRoot = new DefaultMutableTreeNode();
		AbstractPolicy policy = policyTree.loadPolicy(listFile);
		
	 
	 
		if (policy == null) {
			logger.error("Failed to load root policy!!!");
		} else {
			policyTree.loadNode(policyRoot, policy);
		}
		*/
		policyRoot = policyTree.getPolicyRoot();
	//	makeUnique();
		
		rootR = new tnode();
		
		rootR.setLeft(1);
		rootR.setRight(1);
		AbstractPolicy parentp = (AbstractPolicy) policyRoot.getUserObject();
		rootR.setInfo(parentp);
	 
		
		
	 
	
		generateResolutionTree(rootR, policyRoot, 0);
		
		
	//	System.out.println("-----------------------------------------");
	//	printResolutionTree(rootR, 0);
	 
	}
	private String makeindent(int val){
	
		String indent = "";
		for (int i=0; i < val ; i++){indent = indent + "    ";	}
		
		return indent;
		
	}
	
	private void printResolutionTree(tnode tnode, int spaceGap){
		
		String indent = makeindent(spaceGap);
		tnode.printInfo(indent);
		
	//	System.out.println(indent + tnode.getName() + "  [" + tnode.getLeft() + ","+ tnode.getRight() + "]  size : " + tnode.getChildrenNum());
		
		for (int i=0; i < tnode.getChildrenNum() ; i++){
			printResolutionTree((tnode)tnode.getChild(i), spaceGap+1); 
		}
		
		
	}
	//hwang
	
	private int generateResolutionTree(tnode nodeElement, DefaultMutableTreeNode policyNode, int spaceGap) {
		
		String indent = makeindent(spaceGap);
		
		int NleftVal = nodeElement.getLeft();
		
		
		
		for (Enumeration e = policyNode.children(); e.hasMoreElements();) {
	
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
			AbstractPolicy p = (AbstractPolicy) node.getUserObject();
			
			tnode newNode = new tnode();
			nodeElement.addChild(nodeElement.getChildrenNum(), newNode);
			newNode.setLeft(NleftVal);
			
			newNode.setInfo(p);
	//		newNode.printInfo(indent);
			
	
			
			if (p instanceof PolicySet) {	
				
				generateResolutionTree(newNode, node, spaceGap+1);	
				
			} else if (p instanceof Policy) {
		
				newNode.setRight(newNode.getLeft()+p.getChildren().size()-1);
				
			} else {
				logger.error("MrgrvPolicyFinder.makeUnique() - What do we have??" + p);
			}
			
			nodeElement.setRight(newNode.getRight());
			
			if (e.hasMoreElements()) {NleftVal = newNode.getRight()+1;}
			
		}		
			
		return NleftVal;
			
	}
}

