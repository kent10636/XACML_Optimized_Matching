/**
 * Normalize an XACML policy
 * @author Fei Chen
 * feichen@cse.msu.edu
 * Computer Science and Engineering
 * Michigan State University
 * 12/03/2008
 */

package xEngineConverter;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.xml.parsers.*;


import org.apache.log4j.Logger;

import PddOriginblock.Edge;
import PddOriginblock.Interval;
import PddOriginblock.Rule;
import PddOriginblock.Sequence;
import PddOriginblock.Node;
//import XACML2Firewall.diversity.Edge;
//import XACML2Firewall.diversity.Interval;
//import XACML2Firewall.diversity.Rule;
//import XACML2Firewall.diversity.Sequence;
//import XACML2Firewall.diversity.Node;

import com.sun.xacml.*;
import com.sun.xacml.attr.AttributeDesignator;
import com.sun.xacml.attr.AttributeFactory;
import com.sun.xacml.attr.AttributeValue;
import com.sun.xacml.cond.Evaluatable;
import com.sun.xacml.cond.Function;
import com.sun.xacml.cond.FunctionFactory;
import com.sun.xacml.cond.FunctionTypeException;
import com.sun.xacml.ctx.Result;
import com.sun.xacml.ctx.Status;
import com.sun.xacml.finder.PolicyFinder;
import com.sun.xacml.finder.PolicyFinderModule;
import com.sun.xacml.finder.PolicyFinderResult;
import commonImpelmentation.*;

public class conversionTree {

protected static Logger logger = Logger.getLogger("conversionTree.class");
protected PolicyFinder finder;

int Rule_Sequence = 1;

public static int numOFsets = 0;
public static int numOFpolicies = 0;
public static int numOFrules = 0;
public static int numOfconvertedrangerules = 0;

public static int static_NumOf_numOFsubjects = 0;
public static int static_NumOf_numOFresources = 0;
public static int static_NumOf_actions = 0;


public static String subjectsInfo = "";
public static String resourcesInfo = "";
public static String actionsInfo = "";


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

// PDD
public Sequence seq;

public static void initialize() {
	// TODO Auto-generated method stub
	numOFsets = 0;
	numOFpolicies = 0;
	numOFrules = 0;
	numOfconvertedrangerules = 0;	
	subjectsInfo = "";
	resourcesInfo = "";
	actionsInfo = "";	
}

public conversionTree(String listFile, String log, String outFWR) {	
	this.listFile = new File(listFile);
//	outS = new PrintStream (System.out);
  	
	try {
			outS = new PrintStream (new FileOutputStream (log));
	} catch (FileNotFoundException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
  	
	 
//	System.out.println("ourFWRfile"+outFWR);
	try {
		outFWRfile = new PrintStream (new FileOutputStream (outFWR));
	} catch (FileNotFoundException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}	
	
	this.func = new Func(outS);
	this.targetFunc = new TargetFunc(func, outS);
	this.mapper = new Mapper(targetFunc, outS, 15);
	func.setMapper(mapper);
	mapper.setFunc(func);
	
	policyTree = new MrgrvPolicyFinderModule(listFile);
	
}

public void init(PolicyFinder finder) {
	this.finder = finder;
	policyTree.init(finder);
	loadAll();

}
private void loadAll() {
	policyRoot = policyTree.getPolicyRoot();	
	tnode rootR = new tnode(func, targetFunc);
	
	rootR.setLeft(1);
	rootR.setRight(1);
	
	AbstractPolicy parentp = (AbstractPolicy) policyRoot.getUserObject();
	rootR.setInfo(parentp);
	
	Target commonTarget = parentp.getTarget();
	rootR.setTarget(commonTarget);
	 
//	generateResolutionTree(rootR, policyRoot, 1, parentp.getTarget());
//	printResolutionTree(rootR, 0);

//	System.out.println("----Parse Testing Starting--------");
	parseXACMLPolicy(rootR, policyRoot, 1, parentp.getTarget());	 
//	printResolutionTree(rootR, 0);
//	System.out.println("----Parse Testing End-------");

 
	
	// add hash table to other subject, other resource, other actions
	//mapper.MapperAddDefaultOtherValue(); //feichen
	
	mapper.putEnumerateHashList(rootR);
	mapper.mapEnumerateOnlyRule2Fwr(rootR);
	
	ArrayList<Rule> firewallRules = xacml2firewallrule(rootR);
	
	outS.println("\n1. All used element with numeric identifier in attributes (Subjects, Resources,and Actions)\n\n");
	outS.println("####Element Set Start#####");
	mapper.printOutHashList();
	static_NumOf_numOFsubjects = mapper.subjectHash.size()-1;
	static_NumOf_numOFresources = mapper.resourceHash.size()-1;
	static_NumOf_actions = mapper.actionHash.size() - 1;	
	
	outS.println("####Element Set Endt#####");
	
	outS.println("\n2. Policy Hierarchy (includes attribute's elements, and assigned numeric rules)\n\n");
	printResolutionTree(rootR, 0);	

 
	outS.println("\n\n3. Converted Firewall Rules using XACML's combining algorithm\n\n");
	//test
	//printOutNumericRules(firewallRules, System.out);
	
	printOutNumericRules(firewallRules, outS);				//// print into log file
	printOutNumericRules(firewallRules, outFWRfile);		// print into firewall policy file
		
	outS.println("####State Info Start#####");	
	printOutSeqStateChange(firewallRules, outS);	// print into log file
	outS.println("####State Info End#####");

	
//	printOutNumericRules(firewallRules, System.out);
//	printOutSeqStateChange(firewallRules, System.out);	// print into log file
	
 
/* 
	// numOFsets added 1 since root //
	System.out.println("note that pluto and conference does not have #PolicySet=0 ");
	System.out.println("numOFsets: "+ numOFsets);	
	System.out.println("numOFpolicies: "+numOFpolicies);	
	System.out.println("numOFrules: "+numOFrules);

	System.out.println("Subjects : "+mapper.getHashElementsNum(mapper.subjectHash));
	System.out.println("Resources : "+mapper.getHashElementsNum(mapper.resourceHash));
	
	System.out.println("Actions "+mapper.getHashElementsNum(mapper.actionHash));
*/	
	subjectsInfo = mapper.getHashElementsNum(mapper.subjectHash);
	resourcesInfo = mapper.getHashElementsNum(mapper.resourceHash);
	actionsInfo = mapper.getHashElementsNum(mapper.actionHash);
	numOfconvertedrangerules = firewallRules.size();
}

public void printOutSeqStateChange(ArrayList<Rule> numRules, PrintStream outS) {
	// TODO Auto-generated method stub
	int seqStateTracer = 1 ;
	
	if (numRules == null){return;}
	
	for (int i=0; i < numRules.size(); i++){
		Rule rule = (Rule) numRules.get(i);
		/*
		if (trule._rule_seq_collection == null){
			outS.println(seqStateTracer + " : " + trule.getRule_seq());}
		else {
			outS.print(seqStateTracer + " : ");
			trule.printOutRule_rule_seq_collection(",", outS);
			outS.println();
		}*/
		outS.println(seqStateTracer + " : "+ seqStateTracer);
		seqStateTracer++;
	}
}

private void printOutNumericRules(ArrayList<Rule> numRules, PrintStream outS) {
	// TODO Auto-generated method stub
	
	if (numRules == null){return;}
	
	for (int i=0; i < numRules.size(); i++){
		numRules.get(i).print_file(outS);
		//((trule) numRules.get(i)).printOutRuleInfo("", outS);
	}	
}

private ArrayList<Rule> xacml2firewallrule(tnode tnode) {	
	// TODO Auto-generated method stub
	
	ArrayList<Rule> allrule = new ArrayList<Rule>();
//	List DenyRule = new ArrayList();
//	List PermitRule = new ArrayList();
		
	if (tnode.getType() == "PolicySet" || tnode.getType() == "Policy"){
		String alg = tnode.getAlg();
		for (int i=0; i<tnode.getChildrenNum(); i++)
		{
			tnode childnode = (tnode) tnode.getChild(i);
			ArrayList<Rule> templist = xacml2firewallrule(childnode);
			allrule.addAll(templist);
		}
		return AllMatch2FirstMatch(allrule, alg);
		/*
		if (tnode.getAlg().equals("first-applicable")){

			List PrevRule = new ArrayList();
			List LaterRule = new ArrayList();
			
			for (int i=0; i < tnode.getChildrenNum() ; i++){
				tnode childnode = (tnode)tnode.getChild(i);
				List TempList = xacml2firewallrule (childnode);
				AllRule.addAll(TempList);

				return AllRule;
			}
		} else if (tnode.getAlg().equals("deny-overrides")){
			for (int i=0; i < tnode.getChildrenNum() ; i++){
				tnode childnode = (tnode)tnode.getChild(i);
				List TempList = xacml2firewallrule (childnode);
				
				List TempDenyRule = new ArrayList();
				List TempPermitRule = new ArrayList();
				
				if (tnode.getType() == "PolicySet"){DenyPermitOrdered (TempPermitRule, TempDenyRule ,TempList);}
				else if (tnode.getType() == "Policy"){DenyPermitSeparated(TempPermitRule, TempDenyRule ,TempList);}
				DenyRule.addAll(TempDenyRule);
				PermitRule.addAll(TempPermitRule);
				
				TempDenyRule.clear();
				TempPermitRule.clear();
				
			}

//			lazy evaluation			
// 			Fix for counter-example 1 that JH found. - make nonactive			
//			System.out.println("CommonMergeRule STARTING");
//			DenyRule = CommonMergeRule(DenyRule);
//			PermitRule = CommonMergeRule(PermitRule);
//			System.out.println("CommonMergeRule Ending");
			
			AllRule.addAll(DenyRule);
			AllRule.addAll(PermitRule);
				
				
				return AllRule;
		} else if (tnode.getAlg().equals("permit-overrides")){
			for (int i=0; i < tnode.getChildrenNum() ; i++){
				tnode childnode = (tnode)tnode.getChild(i);
				List TempList = xacml2firewallrule (childnode);
				List TempDenyRule = new ArrayList();
				List TempPermitRule = new ArrayList();
				
				if (tnode.getType() == "PolicySet"){PermitDenyOrdered (TempPermitRule , TempDenyRule ,TempList);}
				else if (tnode.getType() == "Policy"){DenyPermitSeparated (TempPermitRule , TempDenyRule ,TempList);}			

				DenyRule.addAll(TempDenyRule);
				PermitRule.addAll(TempPermitRule);
				
				TempDenyRule.clear();
				TempPermitRule.clear();		
			}
//			lazy evaluation
// 			Fix for counter-example 1 that JH found. - make nonactive				
//System.out.println("CommonMergeRule STARTING");
//			PermitRule = CommonMergeRule(PermitRule);	
//			DenyRule = CommonMergeRule(DenyRule);
//System.out.println("CommonMergeRule Ending");	
			
				AllRule.addAll(PermitRule);
				AllRule.addAll(DenyRule);			
				return AllRule;
		}
	*/	
	} else if (tnode.getType() == "Rule"){
		List<trule> templist = tnode.getFirstApplicableSorted();
		return CreateRuleFromString(templist);
	}
//System.out.println("null");
	return null;
}

private ArrayList<Rule> CreateRuleFromString(List<trule> truleList)
{
	ArrayList<Rule> ruleList = new ArrayList<Rule>();
	String splitString = "\\[|\\]|\\^|\\,";
	String splitResult[];
	long low, high;
	
	for (int i=0; i<truleList.size(); i++)
	{
		trule tr = (trule) truleList.get(i);
		
		splitResult = tr.getRule().trim().split(splitString);
		Rule r = new Rule();
		// subjects
        low = Long.parseLong(splitResult[1]);
        high = Long.parseLong(splitResult[2]);
        r.add(new Edge(0, new Interval(low, high)));
        //resources
        low = Long.parseLong(splitResult[5]);
        high = Long.parseLong(splitResult[6]);
        r.add(new Edge(1, new Interval(low, high)));
        //actions
        low = Long.parseLong(splitResult[9]);
        high = Long.parseLong(splitResult[10]);
        r.add(new Edge(2, new Interval(low, high)));
        
        //set decision
        r.setDecision(tr.getEffect());
        
        //set the sequence number of XACML rules
        r.setOrigin(tr.getRule_seq(), tr.getEffect());
        
        ruleList.add(r);
	}
	return ruleList;
}

private ArrayList<Rule> AllMatch2FirstMatch(ArrayList<Rule> allrule, String alg)
{
	seq = new Sequence();
	seq.setRules(allrule);
	Node root = seq.buildAllMatchFDD(alg);
	root.simplify();
	
	Rule tmpRule = new Rule();
	seq.generateRules(root, tmpRule, 0);
	return seq.getFirstMatchRules();
}

private void RulePrint(List tList) {
	
	for (int i=0; i < tList.size(); i++){
		System.out.println(i+" :  "+((trule) tList.get(i)).getRule()+"->"+((trule) tList.get(i)).getEffect());
		
	}
	
}

private void printResolutionTree(tnode tnode, int spaceGap){
	

	tnode.printOutNodeInfo(spaceGap, outS);	
	for (int i=0; i < tnode.getChildrenNum() ; i++){
		printResolutionTree((tnode)tnode.getChild(i), spaceGap+1); 
	}	
}

private tnode addStructurenode(tnode pStructurenode, DefaultMutableTreeNode cPolicynode, Target ptarget){

	AbstractPolicy cPolicy = (AbstractPolicy) cPolicynode.getUserObject();
	
	// derive the information
	tnode childStructureNode = new tnode(func, targetFunc);
	childStructureNode.setInfo(cPolicy);	
	Target commonTarget = targetFunc.insersectTarget(ptarget, cPolicy.getTarget());
	childStructureNode.setTarget(commonTarget);
	
	// add to the tree...
	pStructurenode.addChild(pStructurenode.getChildrenNum(), childStructureNode);

	return childStructureNode;
		
}


private int parseXACMLPolicy (tnode pStructurenode, DefaultMutableTreeNode pPolicynode, int spaceGap, Target ptarget) {

	AbstractPolicy pPolicy = (AbstractPolicy) pPolicynode.getUserObject();

	if (pPolicy instanceof PolicySet) {	

//		System.out.println("policyset");
		numOFsets++;
		int enumerator = 0;
		
		for (Enumeration e = pPolicynode.children(); e.hasMoreElements();) {
			 
			DefaultMutableTreeNode cPolicynode = (DefaultMutableTreeNode) e.nextElement();
			tnode childStructureNode = addStructurenode(pStructurenode, (DefaultMutableTreeNode) cPolicynode, ptarget);
			
			//			 set left
			if (enumerator == 0){
				childStructureNode.setLeft(pStructurenode.getLeft());	
			} else {
				childStructureNode.setLeft(pStructurenode.getRight()+1);	
			}
			
			parseXACMLPolicy (childStructureNode, cPolicynode, spaceGap+1, childStructureNode.getTarget());

			//	set right			
			pStructurenode.setRight(childStructureNode.getRight());
			//
			enumerator++;
			
		}
		
	} else if (pPolicy instanceof Policy) {
		
		numOFpolicies++;
		
		List children = pPolicy.getChildren();
		
		for (int i = 0; i < children.size(); i++) {

        	numOFrules++;
        	
    		tnode ruleNode = new tnode(func, targetFunc);
    		pStructurenode.addChild(pStructurenode.getChildrenNum(), ruleNode);
    		
        	com.sun.xacml.Rule rule = (com.sun.xacml.Rule)children.get(i);
        	Target commonRuleTarget = targetFunc.insersectTarget(ptarget, rule.getTarget());
        	ruleNode.setRuleInfo("Rule", rule.getId().toString(), rule.getEffect(), commonRuleTarget, Rule_Sequence);
        	Rule_Sequence++;
		 }
		
		pStructurenode.setRight(Rule_Sequence-1);
		 
	}		
	// return value.. important...
	return 1;	
}

}

