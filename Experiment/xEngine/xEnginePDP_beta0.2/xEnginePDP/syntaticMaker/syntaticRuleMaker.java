package syntaticMaker;

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
import com.sun.xacml.attr.AttributeDesignator;
import com.sun.xacml.attr.AttributeFactory;
import com.sun.xacml.attr.AttributeValue;
import com.sun.xacml.combine.*;
import com.sun.xacml.cond.Apply;
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




public class syntaticRuleMaker {

protected static Logger logger = Logger.getLogger("syntaticRuleMaker.class");


private Random rand = new Random();


static public int numOFpolicySets1 = 0;	// num of policysets	// first level (if any)
static public int numOFpolicySets2 = 0;	// num of policysets	// second level (if any)
static public int numOFpolicySets3 = 0;	// num of policysets	// third level (if any)
static public int numOFpolicySets4 = 0;	// num of policysets	// 4th level (if any)
static public int numOFpolicySets5 = 0;	// num of policysets	// 5th level (if any)
static public int numOFpolicySets6 = 0;	// num of policysets	// 6th level (if any)
static public int numOFpolicySets7 = 0;	// num of policysets	// 7th level (if any)
static public int numOFpolicySets8 = 0;	// num of policysets	// 8th level (if any)
static public int numOFpolicySets9 = 0;	// num of policysets	// 9th level (if any)
static public int numOFpolicies = 0;	// num of policies
static public int numOFrules = 0;

// totol available attribute values in a policy.
static public int numOFsubjects = 0;
static public int numOFresources = 0;
static public int numOFactions = 0;

// each rule has the following number of subjects, resources, actions, respectively.
static public int RuleNumOFsubjects = 0;
static public int RuleNumOFresources = 0;
static public int RuleNumOFactions = 0;

// each attribute has following number of multi-attributies 
static public int RuleNumOFMultisubjects = 0;
static public int RuleNumOFMultiresources = 0;
static public int RuleNumOFMutltiactions = 0;


PrintStream outS = null;
PrintStream outFWRfile = null;
PrintStream outReport = null;


static public void setPoiicyTreeStructure (int sets1, int sets2, int sets3, int pols, int rules) {
	// TODO Auto-generated method stub
	numOFpolicySets1 = sets1;	// num of policysets	// first level (if any)
	numOFpolicySets2 = sets2;	// num of policysets	// second level (if any)
	numOFpolicySets3 = sets3;	// num of policysets	// third level (if any)
	numOFpolicies = pols;	// num of policies
	numOFrules = rules;
}


static public void setPoiicyTreeStructure (int sets1, int sets2, int sets3, 
		int sets4, int sets5, int sets6,int sets7, int sets8, int sets9,
		int pols, int rules) {
	// TODO Auto-generated method stub
	numOFpolicySets1 = sets1;	// num of policysets	// first level (if any)
	numOFpolicySets2 = sets2;	// num of policysets	// second level (if any)
	numOFpolicySets3 = sets3;	// num of policysets	// third level (if any)
	numOFpolicySets4 = sets4;	// num of policysets	// first level (if any)
	numOFpolicySets5 = sets5;	// num of policysets	// second level (if any)
	numOFpolicySets6 = sets6;	// num of policysets	// third level (if any)
	numOFpolicySets7 = sets7;	// num of policysets	// first level (if any)
	numOFpolicySets8 = sets8;	// num of policysets	// second level (if any)
	numOFpolicySets9 = sets9;	// num of policysets	// third level (if any)	
	
	numOFpolicies = pols;	// num of policies
	numOFrules = rules;
}


static public void setAttrParameters (int subs, int res, int acts) {
	// TODO Auto-generated method stub
	numOFsubjects = subs;
	numOFresources = res;
	numOFactions = acts;	
}

static public void setRuleAttrParameters (int subs, int subsMulti,int res, int resMulti, int acts, int actsMulti) {
	// TODO Auto-generated method stub

	RuleNumOFsubjects = subs;
	RuleNumOFresources = res;
	RuleNumOFactions = acts;
	RuleNumOFMultisubjects = subsMulti;
	RuleNumOFMultiresources = resMulti;
	RuleNumOFMutltiactions = actsMulti;
}

public void setOutReport(PrintStream outReport) {
	// TODO Auto-generated method stub
	this.outReport = outReport;	
}

public syntaticRuleMaker(String outFWR) {
		
	try {		
		outFWRfile = new PrintStream (new FileOutputStream (outFWR));
	} catch (FileNotFoundException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
}

private TargetMatch getTargetMatchInstance (int type, String attr_id, String attr_value){

//	 create eval  
	    Evaluatable eval = null;
	    try {
			eval = new AttributeDesignator(type, new URI("http://www.w3.org/2001/XMLSchema#string"), new URI(attr_id), false);
		} catch (URISyntaxException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
	    
//		 create attribute_value      
	    AttributeFactory attrFactory = AttributeFactory.getInstance();
	    AttributeValue attrValue = null;
	    
		try {
			attrValue = attrFactory.createValue(new URI("http://www.w3.org/2001/XMLSchema#string"),attr_value);
		} catch (UnknownIdentifierException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (ParsingException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (URISyntaxException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

//		 create attribute_function  	
	    URI funcId = null;
		try {
			funcId = new URI("urn:oasis:names:tc:xacml:1.0:function:string-equal");
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    FunctionFactory factory = FunctionFactory.getTargetInstance();
	    Function function=null;
		try {
			function = factory.createFunction(funcId);
		} catch (UnknownIdentifierException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FunctionTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
		return new TargetMatch(type, function, eval, attrValue);
		
}


//TargetMatch.SUBJECT type

private List getMatchesList (int type, int Matchcount){

    List matches = new ArrayList();
    List dupChk = new ArrayList();
    
    int selected = 0;
    String attr_id = "";  
    String attr_value = "";
       
    for (int i=0; i<Matchcount ; i++){
    	
        if (type == TargetMatch.SUBJECT){
        	selected = Math.abs(rand.nextInt())%numOFsubjects;
            attr_id = "role";  
            attr_value = "subject_"+selected;
        }else if (type == TargetMatch.RESOURCE){
        	selected = Math.abs(rand.nextInt())%numOFresources;
            attr_id = "resource-class";  
            attr_value = "resource_"+selected;
        }else if (type == TargetMatch.ACTION){
        	selected = Math.abs(rand.nextInt())%numOFactions;
            attr_id = "action-type";  
            attr_value = "action_"+selected;    	
        }   	
   		if (!dupChk.contains(selected)){
		    TargetMatch tm = getTargetMatchInstance (type, attr_id, attr_value);
		    matches.add(tm);
		    dupChk.add(selected);
   		}else{
   			i--;	// duplicate found
   		}
    }
    
    return matches;
}


private boolean IsAttrMatchequal(List matchAttr, List matches) {
	// TODO Auto-generated method stub
	
	int dupNum = 0;
 	
	for (int i=0; i <matchAttr.size(); i++ ){
		TargetMatch tm = (TargetMatch) matchAttr.get(i);	 
		for (int j=0; j <matchAttr.size(); j++ ){
			TargetMatch tm2 = (TargetMatch) matches.get(j);
			if (tm.getMatchValue().toString().equals(tm2.getMatchValue().toString())){
//				System.out.println("dup.."+tm.getMatchValue().toString()+tm2.getMatchValue().toString());
				dupNum++;
			}		
		}
	}
	
	if (dupNum == matchAttr.size()){
//		System.out.println("dup..");
		return true;
	}
	return false;
}

private boolean IsDuplicateAttrValue(List attribute, List matches) {
	// TODO Auto-generated method stub
	
	for (int i=0; i <attribute.size(); i++ ){
		
		List matchAttr = (List) attribute.get(i);
		if (IsAttrMatchequal(matchAttr, matches)) {return true;}{
		}
	}
	
	return false;
}





private List getAttributeList (int type, int Attrcount, int Matchcount){

    List attribute = new ArrayList();
    
    // just temporalilry to check duplicate. my tool cannot handle duplciate..in stirng.conversion.
    List DupStore = new ArrayList();
    
    for (int i=0; i<Attrcount ; i++){
    	List matches = getMatchesList (type, Matchcount);
    	if (!IsDuplicateAttrValue(attribute, matches)){
    		attribute.add(matches);} 
    	else {
    		i--;
    	}
    }
    
    return attribute;
}



private Target getRandomTarget(){

	List subject = getAttributeList (TargetMatch.SUBJECT, RuleNumOFsubjects, RuleNumOFMultisubjects);
	List resources = getAttributeList (TargetMatch.RESOURCE, RuleNumOFresources, RuleNumOFMultiresources);
	List action = getAttributeList (TargetMatch.ACTION, RuleNumOFactions, RuleNumOFMutltiactions);
	
    return new Target(subject,resources,action);
}



private Rule getRandomRule(URI id){
	
	Target target = getRandomTarget();
	

	int selected = Math.abs(rand.nextInt())%2;
	
	int effect = 0;
	
	if (selected==0){
		effect = Result.DECISION_DENY;
	}if (selected==1){
		effect = Result.DECISION_PERMIT;
	}
	
	return new Rule(id, effect, "rule description"+id.toString(),  target, null);	
}


private Policy getRandomPolicy(URI id){
	
	Policy pset = null;
	
	int selected = Math.abs(rand.nextInt())%3;
	
	if (selected == 0){
		pset = new Policy(id, new FirstApplicableRuleAlg(), new Target(null,null,null), new ArrayList());		
	}else if (selected == 1){
		pset = new Policy(id, new DenyOverridesRuleAlg(), new Target(null,null,null), new ArrayList());
	}else if (selected == 2){
		pset = new Policy(id, new PermitOverridesRuleAlg(), new Target(null,null,null), new ArrayList());
	}
	
	return pset;
}

private PolicySet getRandomPolicySet(URI id){

	PolicySet pset = null;
	
	int selected = Math.abs(rand.nextInt())%3;

	if (selected == 0){
		pset = new PolicySet(id, new FirstApplicablePolicyAlg(), new Target(null,null,null), new ArrayList());		
	}else if (selected == 1){
		pset = new PolicySet(id, new DenyOverridesPolicyAlg(), new Target(null,null,null), new ArrayList());
	}else if (selected == 2){
		pset = new PolicySet(id, new PermitOverridesPolicyAlg(), new Target(null,null,null), new ArrayList());
	}
	
	return pset;
}


private AbstractPolicy genRandomTreePolicyIterFunc(AbstractPolicy root, int select) {
	// TODO Auto-generated method stub


	int num = 0;
	
	if (select <= 8){ 				//	if policysets...
		
		if (select == 0){num = numOFpolicySets1;
		} else if (select == 1){num = numOFpolicySets2;
		} else if (select == 2){num = numOFpolicySets3;		
		}else if (select == 3){num = numOFpolicySets4;
		} else if (select == 4){num = numOFpolicySets5;}
		else if (select == 5){num = numOFpolicySets6;
		} else if (select == 6){num = numOFpolicySets7;}
		else if (select == 7){num = numOFpolicySets8;
		} else if (select == 8){num = numOFpolicySets9;}
		if (num == 0){genRandomTreePolicyIterFunc (root, select+1);}
		
		for (int i=0; i < num ; i++){		
			PolicySet pset = null;
			try {
				pset = getRandomPolicySet(new URI(root.getId().toString()+"."+i));
				genRandomTreePolicyIterFunc (pset, select+1);			
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (pset != null){
				root.getChildren().add(pset);
			}
		}
	} else if (select == 9){		//	if policies...

			num = numOFpolicies;
			
			if (num == 0){genRandomTreePolicyIterFunc (root, select+1);}			
			for (int i=0; i < num ; i++){	
			Policy pset = null;
			try {
				pset = getRandomPolicy(new URI(root.getId().toString()+"."+i));
				genRandomTreePolicyIterFunc (pset, select+1);			
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (pset != null){
				root.getChildren().add(pset);
			}		
		}
	}  else if (select == 10){		//	if rules...
				
				num = numOFrules;
		
				for (int l=0; l < num ; l++){
					Rule rule = null;
					try {
						rule = getRandomRule(new URI(root.getId().toString()+"."+l));
					} catch (URISyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (rule != null){
						root.getChildren().add(rule);								
					}
		}	// for l
	}		
		
	return root;
}

void generatePolicy() throws URISyntaxException, UnknownIdentifierException, Exception {

	AbstractPolicy Policyrootp = null;
	
	if (numOFpolicies == 0){
		Policy rootp = getRandomPolicy(new URI("RPlist"));
		Policyrootp = rootp;
	} else {
		PolicySet rootp = getRandomPolicySet(new URI("RPlist"));
		Policyrootp = rootp;
	}
	
	genRandomTreePolicyIterFunc (Policyrootp, 0);
	

//	Policyrootp.encode(System.out, new Indenter(4));
	Policyrootp.encode(outFWRfile, new Indenter(4));
	
	
//	System.out.println("ending....");

	
}

public static int getRuleNum() {
	
	int a = 0, b=0, c=0, d=0, e=0;
	
	if (numOFpolicySets1 == 0){a = 1;}
	else {a = numOFpolicySets1;}

	if (numOFpolicySets2 == 0){b = 1;}
	else {b = numOFpolicySets2;}

	if (numOFpolicySets3 == 0){c = 1;}
	else {c = numOFpolicySets3;}

	if (numOFpolicies == 0){d = 1;}
	else {d = numOFpolicies;}

	if (numOFrules == 0){e = 1;}
	else {e = numOFrules;}
	
	return a*b*c*d*e;
}

public static int getTargetNum() {
	
	int a = 0, b=0, c=0, d=0, e=0;
	
	if (numOFpolicySets1 == 0){a = 1;}
	else {a = numOFpolicySets1;}

	if (numOFpolicySets2 == 0){b = 1;}
	else {b = numOFpolicySets2;}

	if (numOFpolicySets3 == 0){c = 1;}
	else {c = numOFpolicySets3;}

	if (numOFpolicies == 0){d = 1;}
	else {d = numOFpolicies;}

	if (numOFrules == 0){e = 1;}
	else {e = numOFrules;}
	
	return numOFpolicySets1+ a*numOFpolicySets2+ a*b*numOFpolicySets3+a*b*c*numOFpolicies+a*b*c*d*e;
}

public static void writeBasicInfo(PrintStream outReport) {
	// TODO Auto-generated method stub
	outReport.println("1: Generated Policy : ");
	outReport.println("1: Total num of Rules : "+ getRuleNum() );
	outReport.println("1: Total num of Targets : " + getTargetNum());
	outReport.println();		
	outReport.println("1: numOFpolicySets1 : " + numOFpolicySets1);
	outReport.println("1: numOFpolicySets2 : " + numOFpolicySets2);
	outReport.println("1: numOFpolicySets3 : " + numOFpolicySets3);
	outReport.println("1: numOFpolicies : " + numOFpolicies);
	outReport.println("1: numOFrules : " + numOFrules);	
	outReport.println();	
	outReport.println("1: numOFsubjects : " + numOFsubjects);	
	outReport.println("1: numOFresources : " + numOFresources);	
	outReport.println("1: numOFactions : " + numOFactions);	
	outReport.println();	
	outReport.println("1: RuleNumOFsubjects : " + RuleNumOFsubjects);	
	outReport.println("1: RuleNumOFresources : " + RuleNumOFresources);	
	outReport.println("1: RuleNumOFactions : " + RuleNumOFactions);	
	outReport.println();	
	outReport.println("1: RuleNumOFMultisubjects : " + RuleNumOFMultisubjects);	
	outReport.println("1: RuleNumOFMultiresources : " + RuleNumOFMultiresources);	
	outReport.println("1: RuleNumOFMutltiactions : " + RuleNumOFMutltiactions);
}









}

