package reqGen.ncsu.margrave;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import reqGen.com.sun.xacml.AbstractPolicy;
import reqGen.com.sun.xacml.EvaluationCtx;
import reqGen.com.sun.xacml.Indenter;
import reqGen.com.sun.xacml.MatchResult;
import reqGen.com.sun.xacml.Policy;
import reqGen.com.sun.xacml.PolicyReference;
import reqGen.com.sun.xacml.PolicySet;
import reqGen.com.sun.xacml.Rule;
import reqGen.com.sun.xacml.ctx.Result;
import reqGen.com.sun.xacml.ctx.Status;
import reqGen.com.sun.xacml.finder.PolicyFinder;
import reqGen.com.sun.xacml.finder.PolicyFinderModule;
import reqGen.com.sun.xacml.finder.PolicyFinderResult;
import reqGen.ncsu.util.Util;
import reqGen.ncsu.xacml.poco.RuntimeCoverage;


public class MrgrvPolicyFinderModule extends PolicyFinderModule implements
		ErrorHandler {

	protected static Logger logger = Logger.getLogger("MrgrvPolicyFinderModule.class");

	protected PolicyFinder finder;

	// the root policy
	protected File listFile;

	// policy hierarchy
	protected DefaultMutableTreeNode policyRoot;

	// maps URI.toString() --> policy for fast retrieval
//	protected Hashtable policies;

	public MrgrvPolicyFinderModule(String listFile) {
		this.listFile = new File(listFile);
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
		loadAll();
	}

	private void loadAll() {
//		policies = new Hashtable();
		policyRoot = new DefaultMutableTreeNode();
		AbstractPolicy policy = loadPolicy(listFile);
		if (policy == null) {
			logger.error("Failed to load root policy!!!");
		} else {
			loadNode(policyRoot, policy);
		}
		makeUnique();
	}

	private void makeUnique() {
		for (Enumeration e = policyRoot.depthFirstEnumeration(); e.hasMoreElements();) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
			AbstractPolicy p = (AbstractPolicy) node.getUserObject();
			if (p instanceof PolicySet) {	
				if (p != policyRoot.getUserObject()) {
					p.setId(getUniqueId(node));
				}
				// collect abstract policies
				Vector pSet = new Vector();
				for (Enumeration c = node.children(); c.hasMoreElements();) {
					DefaultMutableTreeNode nextC = (DefaultMutableTreeNode) c.nextElement();
					Object o = nextC.getUserObject();
					// check type
					if (! (o instanceof AbstractPolicy))
	                    throw new IllegalArgumentException("non-AbstractPolicy " + "in policies");
					pSet.add(o);
				}
				p.setChildren(pSet); // replaces policy references with policies
			} else if (p instanceof Policy) {
				if (p != policyRoot.getUserObject()) {				
					p.setId(getUniqueId(node));
				}
				// need to ensure rule identifiers are unique as well
				int i = 1;
				for (Iterator children = p.getChildren().iterator(); children.hasNext();) {
					((Rule) children.next()).setId(p.getId() + ".r." + (i++));
				}
				
			} else {
				logger.error("MrgrvPolicyFinder.makeUnique() - What do we have??" + p);
			}
		}
	}
	
	public String getUniqueId(DefaultMutableTreeNode node) {
		TreeNode[] path = node.getPath();
		if (path.length == 1) {
			logger.error("getUniqueId() should not be called on root.");
			return ((AbstractPolicy) node.getUserObject()).getId().toString();
		}
		StringBuffer id = new StringBuffer();
		id.append(((AbstractPolicy) policyRoot.getUserObject()).getId().toString());
		for (int i = 1; i < path.length; i++) {
			id.append("." + path[i].getParent().getIndex(path[i]));
		}
		return id.toString();
		
//		AbstractPolicy p = (AbstractPolicy) node.getUserObject();
//		TreeNode tNode = node.getParent();
//		if (tNode == null) {
//			logger.error("Error at getUniqueId() - node not rooted. " + p.getId());
//		} else {
//			DefaultMutableTreeNode grandParent = (DefaultMutableTreeNode) tNode;
//			if (grandParent.getUserObject() == null || !(grandParent.getUserObject() instanceof PolicySet)) {
//				logger.error("Error at getUniqueId() - PolicySet's parent is not a PolicySet. " + grandParent.getUserObject());
//			} else {
//				PolicySet grandSet = (PolicySet) grandParent.getUserObject();
//				return (p.getId() + "-" + grandSet.getId().toString() + "-" + (grandParent.getIndex(node) + 1));
//			}
//		}
//		return p.getId().toString();
	}
	
	private void loadNode(DefaultMutableTreeNode parent, AbstractPolicy policy) {

		
//		// ensure unique identifers
//		if (parent == policyRoot) {
//			// skip - have to start somewhere
//			logger.info("Skipped " + policy.getId());
//		} else if (policy instanceof PolicySet) {
//			TreeNode tNode = parent.getParent();
//			if (tNode == null) {
//				logger.error("Error at loadNode() - node not rooted. " + policy.getId());
//			} else {
//				DefaultMutableTreeNode grandParent = (DefaultMutableTreeNode) tNode;
//				if (grandParent.getUserObject() == null || !(grandParent.getUserObject() instanceof PolicySet)) {
//					logger.error("Error at loadNode() - PolicySet's parent is not a PolicySet. " + grandParent.getUserObject());
//				} else {
//					PolicySet grandSet = (PolicySet) grandParent.getUserObject();
//					policy.setId(policy.getId() + "-" + grandSet.getId().toString() + "-PolicySet-" + (grandParent.getIndex(parent) + 1));
//				}
//			}
//		} else if (policy instanceof Policy) {
//			TreeNode tNode = parent.getParent();
//			if (tNode == null) {
//				logger.error("Error at loadNode() - node not rooted. " + tNode);
//			} else {
//				DefaultMutableTreeNode grandParent = (DefaultMutableTreeNode) tNode;
//				if (grandParent.getUserObject() == null || !(grandParent.getUserObject() instanceof PolicySet)) {
//					logger.error("Error at loadNode() - Policy's parent is not a PolicySet. " + grandParent.getUserObject());
//				} else {
//					PolicySet grandSet = (PolicySet) grandParent.getUserObject();
//					policy.setId(policy.getId() + "-" + grandSet.getId().toString() + "-Policy-" + (grandParent.getIndex(parent) + 1));
//					// need to ensure rule identifiers are unique as well - hack for issta experiments
//					int i = 1;
//					for (Iterator children = policy.getChildren().iterator(); children.hasNext();) {
//						((Rule) children.next()).setId(policy.getId() + "-Rule-" + (i++));
//					}
//				}
//			}
//		}
		
		// load it
		if (policy instanceof PolicyReference) {
			// don't store references - load the policy or policy set from file
			File f = new File(listFile.getParent() + "/" + ((PolicyReference) policy).getReference().toString() + ".xml");
			AbstractPolicy realPolicy = loadPolicy(f);
			loadNode(parent, realPolicy);
		} else if (policy instanceof PolicySet) {
			// store the policy set
			parent.setUserObject(policy);
//			policies.put(policy.getId().toString(), policy);
			// traverse children of set since they are policies
			Iterator children = policy.getChildren().iterator();
			while (children.hasNext()) {
				Object o = children.next();
				if (o instanceof AbstractPolicy) {
					DefaultMutableTreeNode child = new DefaultMutableTreeNode();
					parent.insert(child, parent.getChildCount());
					// recurse child
					loadNode(child, (AbstractPolicy) o);
				} else {
					logger.info(o.toString());
				}
			}
		} else if (policy instanceof Policy) {			
			parent.setUserObject(policy);
//			policies.put(policy.getId().toString(), policy);
		} else {
			// shouldn't get here
			logger.error("MrgrvPolicyFinder.loadNode() - What do we have??");
		}
	}

	private AbstractPolicy loadPolicy(File file) {
		try {
			// create the factory
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringComments(true);

			DocumentBuilder db = null;

			// as of 1.2, we always are namespace aware
			factory.setNamespaceAware(true);

			// set the factory to work the way the system requires
			//            if (schemaFile == null) {
			// we're not doing any validation
			factory.setValidating(false);

			db = factory.newDocumentBuilder();
			//            } else {
			//                // we're using a validating parser
			//                factory.setValidating(true);
			//
			//                factory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
			//                factory.setAttribute(JAXP_SCHEMA_SOURCE, schemaFile);
			//                
			//                db = factory.newDocumentBuilder();
			//                db.setErrorHandler(handler);
			//            }

			// try to load the policy file
			Document doc = db.parse(new FileInputStream(file));

			// handle the policy, if it's a known type
			Element root = doc.getDocumentElement();
			String name = root.getTagName();

			if (name.equals("Policy")) {
				return Policy.getInstance(root);
			} else if (name.equals("PolicySet")) {
				return PolicySet.getInstance(root, finder);
			} else {
				// this isn't a root type that we know how to handle
				throw new Exception("Unknown root document type: " + name);
			}
		} catch (Exception e) {
			logger.error("Error reading policy from file " + file, e);
		}
		return null;
	}

	/**
	 * Always return true, since indeed this class supports references.
	 *
	 * @return true
	 */
	public boolean isIdReferenceSupported() {
		return true;
	}

	public boolean isRequestSupported() {
		return true;
	}

	/**
	 * This method is invoked to find
	 * a policy reference. In this case, we first check that we haven't 
	 * previously cached the policy, then we fetch the policy, and if needed,
	 * we parse the XML and create a new AbstractPolicy handling errors
	 * correctly. 
	 *
	 * @param idReference an identifier specifying some policy
	 * @param type type of reference (policy or policySet) as identified by
	 *             the fields in <code>PolicyReference</code>
	 *
	 * @return the result of looking for the referenced policy
	 */
	public PolicyFinderResult findPolicy(URI idReference, int type) {
		String key = idReference.toString();
		Vector policies = new Vector();
		for (Enumeration e = policyRoot.depthFirstEnumeration(); e.hasMoreElements();) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
			AbstractPolicy p = (AbstractPolicy) node.getUserObject();
			if (p != null && p.getId().toString().equals(key)) {
				policies.add(node.getUserObject());
			}
		}
		if (policies.size() > 1) {
			logger.error("Found " + policies.size() + " policies for " + idReference);
			ArrayList code = new ArrayList();
			code.add(Status.STATUS_PROCESSING_ERROR);
			Status status = new Status(code, "too many applicable top-level policies");
			return new PolicyFinderResult(status);
		} else if (policies.size() == 1){
			return new PolicyFinderResult((AbstractPolicy) policies.firstElement());
		} else {
			File f = new File(listFile.getParent() + "/" + idReference.toString() + ".xml");
			AbstractPolicy policy = loadPolicy(f);
			return new PolicyFinderResult(policy);
		}
	}

	/**
	 * Finds a policy based on a request's context. This may involve using
	 * the request data as indexing data to lookup a policy. This will always
	 * do a Target match to make sure that the given policy applies. If more
	 * than one applicable policy is found, this will return an error.
	 * NOTE: this is basically just a subset of the OnlyOneApplicable Policy
	 * Combining Alg that skips the evaluation step. See comments in there
	 * for details on this algorithm.
	 *
	 * @param context the representation of the request data
	 *
	 * @return the result of trying to find an applicable policy
	 */
	public PolicyFinderResult findPolicy(EvaluationCtx context) {
		HashSet policies = new HashSet();
		for (Enumeration e = policyRoot.depthFirstEnumeration(); e.hasMoreElements();) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
			policies.add(node.getUserObject());
		}
//		RuntimeCoverage.collectAllPolicies(policies);
		ArrayList matched = new ArrayList();
		// TODO may be able to use one of those enumerations here
		// recursively search policy hierarchy
		try {
			findPolicy(context, policyRoot, matched);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.debug("NPE", e);
		}
		if (matched.size() == 1) {
			// found exactly one
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) matched.get(0); 
			AbstractPolicy policy = (AbstractPolicy) node.getUserObject();
			return new PolicyFinderResult(policy);
		} else if (matched.size() > 1) {
			// found more than one - need to resolve with combining alg
			PolicySet commonAncestor = findFirstAncestor(matched);
			if (commonAncestor != null) {
				return new PolicyFinderResult(commonAncestor);
			}
			// previous impl throws error
			ArrayList code = new ArrayList();
			code.add(Status.STATUS_PROCESSING_ERROR);
			Status status = new Status(code, "too many applicable top-level policies");
			return new PolicyFinderResult(status);
		} else {
			// found none?
			logger.debug("No applicable policy found.");
			return new PolicyFinderResult();
		}
	}

	private void findPolicy(EvaluationCtx context, DefaultMutableTreeNode node,
			ArrayList matched) throws Exception {

		AbstractPolicy policy = (AbstractPolicy) node.getUserObject();
		int result = MatchResult.INDETERMINATE;
		try {
			MatchResult match = policy.match(context);
			result = match.getResult();
		} catch (Exception e) {
			throw e;
		}		

		// if there was an error, we stop right away
		if (result == MatchResult.INDETERMINATE) {
			return;
		}

		if (result == MatchResult.MATCH) {
			// matched something
			if (policy instanceof Policy) {
				// matched policy - good
//				RuntimeCoverage.coverPolicy(policy); // TODO do we want to cover here or if we really use the policy
				// add the node to preserve the hierarchy
				matched.add(node);
			} else if (policy instanceof PolicySet) {
				// matched policy set so traverse hierarchy
				for (int i = 0; i < node.getChildCount(); i++) {
					findPolicy(context, (DefaultMutableTreeNode) node.getChildAt(i), matched);
				}
			} else {
				// should be here
				logger.error("don't know...");
			}
		}
	}
	
	private PolicySet findFirstAncestor(ArrayList matched) {
		// setup a vector of paths and remember the shortest path
		// traverse this column by column to find commonAncestor
		ArrayList paths = new ArrayList();
		int shortestPath = -1;
		for (int i = 0; i < matched.size(); i++) {
			TreeNode[] path = ((DefaultMutableTreeNode) matched.get(i)).getPath();
			paths.add(i,path);
			if (shortestPath == -1) {
				shortestPath = path.length;
			} else if (shortestPath > path.length) {
				shortestPath = path.length;
			}
		}
		
		TreeNode commonAncestor = policyRoot;
		// count backwards for columns
		for (int c = (shortestPath - 1); c >= 0; c--) {
			// get the upper right node
			TreeNode next = ((TreeNode[]) paths.get(0))[c];
			// compare down the rows
			boolean common = true;
			for (int r = 1; r < paths.size(); r++) {
				TreeNode thisNode = ((TreeNode[]) paths.get(r))[c];
				if (!next.equals(thisNode)) {
					common = false;
					break;
				}
			}
			if (common) {
				commonAncestor = next;
				break;
			}
		}

		return (PolicySet) ((DefaultMutableTreeNode) commonAncestor).getUserObject();
	}

	/**
	 * Standard handler routine for the XML parsing.
	 *
	 * @param exception information on what caused the problem
	 */
	public void warning(SAXParseException exception) throws SAXException {
		logger.error("Warning on line " + exception.getLineNumber()
					+ ": " + exception.getMessage(), exception);
	}

	/**
	 * Standard handler routine for the XML parsing.
	 *
	 * @param exception information on what caused the problem
	 *
	 * @throws SAXException always to halt parsing on errors
	 */
	public void error(SAXParseException exception) throws SAXException {
		logger.error("Error on line " + exception.getLineNumber() + ": "
					+ exception.getMessage() + " ... "
					+ "Policy will not be available", exception);
		throw new SAXException("error parsing policy");
	}

	/**
	 * Standard handler routine for the XML parsing.
	 *
	 * @param exception information on what caused the problem
	 *
	 * @throws SAXException always to halt parsing on errors
	 */
	public void fatalError(SAXParseException exception) throws SAXException {
		logger.error("Fatal error on line " + exception.getLineNumber()
					+ ": " + exception.getMessage() + " ... "
					+ "Policy will not be available", exception);

		throw new SAXException("fatal error parsing policy");
	}
	
	public boolean isDecisionDeny() throws Exception {
		HashSet rules = new HashSet();
		for (Enumeration e = policyRoot.breadthFirstEnumeration(); e.hasMoreElements();) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
			if (node.getUserObject() instanceof Policy) {
				// since only policies have rules not policy sets
				Policy policy = (Policy) node.getUserObject();
				List ruleList = policy.getChildren();
				rules.addAll(ruleList);	
			}
		}
		if (rules.size() != 1) {
			throw new Exception("Too many or too few rules to determine if decision is deny.");
		} else {
			Rule r = (Rule) rules.toArray()[0];
			return r.getEffect() == Result.DECISION_DENY;
		}
	}
	
//	public void copyPolicy() {
////		 read in and write out - good for removing AnySubject and/or modifying identifiers
//		outputNode(policyRoot, null, "copy");
//	}
	
	public String copyPolicy(String dir, String name) {
		AbstractPolicy policy = (AbstractPolicy) policyRoot.getUserObject();
		String newPolicyPath = dir + name + ".xml"; 
		File f = new File(newPolicyPath);
		Util.createFile(f, logger);
		// write file
		PrintStream outS = null;
		try {
			outS = new PrintStream (new FileOutputStream (f));
		} catch (FileNotFoundException e) {
			logger.error("File not found. " + f, e);
		}		
		policy.encode(outS, new Indenter());
		return newPolicyPath;
	}
	
	public String copyPolicy(String dir) {
		AbstractPolicy policy = (AbstractPolicy) policyRoot.getUserObject();
		String fileName = policy.getId().toString();
		String newPolicyPath = dir + fileName + ".xml"; 
		File f = new File(newPolicyPath);
		Util.createFile(f, logger);
		// write file
		PrintStream outS = null;
		try {
			outS = new PrintStream (new FileOutputStream (f));
		} catch (FileNotFoundException e) {
			logger.error("File not found. " + f, e);
		}		
		policy.encode(outS, new Indenter());
		return newPolicyPath;
	}
	
//	public int splitPolicyNAtTime() {
////		 count the rules and collect them
//		// override encode() to take the collection and a counter
//		HashSet rules = new HashSet();
//		for (Enumeration e = policyRoot.breadthFirstEnumeration(); e.hasMoreElements();) {
//			DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
//			if (node.getUserObject() instanceof Policy) {
//				// since only policies have rules not policy sets
//				Policy policy = (Policy) node.getUserObject();
//				List ruleList = policy.getChildren();
//				rules.addAll(ruleList);	
//			}
//		}
//		
//		// adds 1 rule, then 2 rules, then 3 rules, ...
//		Object[] rulesArr = rules.toArray();
//		for (int i = 0; i < rules.size(); i++) {
//			Rule[] subset = new Rule[i+1];
//			for (int j = 0; j <=i; j++) {
//				subset[j] = (Rule) rulesArr[j];
//			}
//			outputNode(policyRoot, subset, Integer.toString(i+1));
//		}
//		return rulesArr.length;	
//	}
	
//	public int splitPolicyMinusOneAtTime() {
////		 count the rules and collect them
//		// override encode() to take the collection and a counter
//		HashSet rules = new HashSet();
//		for (Enumeration e = policyRoot.breadthFirstEnumeration(); e.hasMoreElements();) {
//			DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
//			if (node.getUserObject() instanceof Policy) {
//				// since only policies have rules not policy sets
//				Policy policy = (Policy) node.getUserObject();
//				List ruleList = policy.getChildren();
//				rules.addAll(ruleList);	
//			}
//		}
//		
//		// remove 1 at a time
//		Object[] rulesArr = rules.toArray();
//		for (int i = 0; i < rules.size(); i++) {
//			Rule[] subset = new Rule[rules.size() - 1];
//			int index = 0;
//			for (int j = 0; j < rules.size(); j++) {
//				if (i != j) {
//					subset[index++] = (Rule) rulesArr[j];
//				}
//			}
//			outputNode(policyRoot, subset, Integer.toString(i+1));
//		}
//		return rulesArr.length;	
//	}
	
//	public int splitPolicyOneAtTime() {
//		// count the rules and collect them
//		// override encode() to take the collection and a counter
//		HashSet rules = new HashSet();
//		for (Enumeration e = policyRoot.breadthFirstEnumeration(); e.hasMoreElements();) {
//			DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
//			if (node.getUserObject() instanceof Policy) {
//				// since only policies have rules not policy sets
//				Policy policy = (Policy) node.getUserObject();
//				List ruleList = policy.getChildren();
//				rules.addAll(ruleList);	
//			}
//		}
//	
//		// add 1 rule at a time
//		Object[] rulesArr = rules.toArray();
//		for (int i = 0; i < rulesArr.length; i++) {
//			Rule[] subset = {(Rule) rulesArr[i]};
//			outputNode(policyRoot, subset, Integer.toString(i+1), false);
//		}	
//		return rulesArr.length;
//	}
	
//	protected void outputNode(DefaultMutableTreeNode root, Object[] rules, String dir) {
//		outputNode(root, rules, dir, false);
//	}
	
//	protected void outputNode(DefaultMutableTreeNode root, Object[] rules, String dir, boolean fullPath) {
//		try {
//			outputPolicy((AbstractPolicy) root.getUserObject(), rules, dir, fullPath);
//		} catch (Exception e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		
////		for (Enumeration e = root.breadthFirstEnumeration(); e.hasMoreElements();) {
////			DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
////			AbstractPolicy p = (AbstractPolicy) node.getUserObject();
////			try { 
////				outputPolicy(p, rules, dir, fullPath);
////			} catch (Exception ex) {
////				logger.error("Failed to output policy " + p.getId().toString(), ex);
////			}
////		}
//	}
	
//	protected void outputPolicy(AbstractPolicy policy, Object[] rules, String dir, boolean fullPath) throws Exception {
//		String path = dir;
//		if (!fullPath) {
//			path = listFile.getParent() + "/" + dir;
//		} 
//		
//		// create directory
//		File pathF = new File(path);
//		if (!pathF.exists()) {
//			if (!pathF.mkdirs()) {
//				logger.error("Failed to create " + path);
//				return;
//			}
//		}
//		// create file
//		File outFile = new File(path + "/" + policy.getId().toString() + ".xml");
//		if (!outFile.exists()) {
//			if (!outFile.createNewFile()) {
//				logger.error("Failed to create " + outFile);
//				return;
//			}
//		}
//		// write file
//		PrintStream out = new PrintStream (new FileOutputStream (outFile));
//		policy.encode(out, new Indenter(), rules);
//	}
	
	public Set getPolicySet() {
		HashSet policies = new HashSet();
		for (Enumeration e = policyRoot.depthFirstEnumeration(); e.hasMoreElements();) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
			policies.add(node.getUserObject());
		}
		return policies;
	}
}