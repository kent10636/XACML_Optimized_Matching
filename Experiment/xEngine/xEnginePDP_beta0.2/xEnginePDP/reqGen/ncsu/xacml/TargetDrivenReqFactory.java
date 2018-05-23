package reqGen.ncsu.xacml;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import reqGen.com.sun.xacml.AbstractPolicy;
import reqGen.com.sun.xacml.EvaluationCtx;
import reqGen.com.sun.xacml.Policy;
import reqGen.com.sun.xacml.PolicySet;
import reqGen.com.sun.xacml.PolicyTreeElement;
import reqGen.com.sun.xacml.Rule;
import reqGen.com.sun.xacml.Target;
import reqGen.com.sun.xacml.TargetMatch;
import reqGen.com.sun.xacml.attr.AttributeDesignator;
import reqGen.com.sun.xacml.attr.AttributeValue;
import reqGen.com.sun.xacml.attr.StringAttribute;
import reqGen.com.sun.xacml.cond.Apply;
import reqGen.com.sun.xacml.cond.Evaluatable;
import reqGen.com.sun.xacml.ctx.Attribute;
import reqGen.com.sun.xacml.ctx.RequestCtx;
import reqGen.com.sun.xacml.ctx.ResponseCtx;
import reqGen.com.sun.xacml.ctx.Result;
import reqGen.com.sun.xacml.ctx.Subject;
import reqGen.ncsu.util.Util;
import reqGen.ncsu.xacml.mutator.Mutator;
import reqGen.ncsu.xacml.poco.RuntimeCoverage;



public class TargetDrivenReqFactory implements RequestFactoryIntf {
	
	private static final String subjIdURI = "urn:oasis:names:tc:xacml:1.0:subject:subject-id";	
	private static final String resIdURI = EvaluationCtx.RESOURCE_ID;
	
	private int index;
	private ArrayList requests;
	private DefaultMutableTreeNode root;
	
	public TargetDrivenReqFactory(AbstractPolicy policy) {
		if (policy instanceof Policy || policy instanceof PolicySet) {
			requests = new ArrayList();
			index = 0;
			root = new DefaultMutableTreeNode();
			init(policy, root);
			walkTree(root);
		} else {
			throw new IllegalArgumentException("Argument must be a Policy or PolicySet." + policy.getClass());
		}
	}
	
	private void init(PolicyTreeElement pte, DefaultMutableTreeNode node) {
		node.setUserObject(pte);
		List childList = pte.getChildren();
		if (childList != null) {
			for (Iterator i = childList.iterator(); i.hasNext();) {
				DefaultMutableTreeNode next = new DefaultMutableTreeNode();
				node.add(next);
				init((PolicyTreeElement) i.next(), next);
			}
		}
	}
	
	private void walkTree(DefaultMutableTreeNode root) {
		for (Enumeration e = root.depthFirstEnumeration(); e.hasMoreElements();) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
			if (node.isLeaf()) {
				PolicyTreeElement pte = (PolicyTreeElement) node.getUserObject();
				if (pte instanceof Rule) {
					ArrayList targets = collectTargets(node.getPath());
					makeRequests(targets, (Rule) pte);
				} else {
					logger.error(pte + " not a rule");
				}				
			}
		}
	}
	
	private ArrayList collectTargets(TreeNode[] node) {
		ArrayList targets = new ArrayList();
		for (int i = 0; i < node.length; i++) {
			DefaultMutableTreeNode n = (DefaultMutableTreeNode) node[i];
			PolicyTreeElement pte = (PolicyTreeElement) n.getUserObject();
			Target target = pte.getTarget();
			if (target != null) {
				targets.add(target);
			}
		}
		return targets;
	}
	
	private void handleCondition(Apply condition, Set subjects, Set resources, Set actions, Set environments, AttributeDesignator designator, Set values) {
		if (condition == null) {
			return;
		}
		List children = condition.getChildren();
		if (children == null) {
			return;
		}
		for (Iterator i = children.iterator(); i.hasNext();) {
			Evaluatable eval = (Evaluatable) i.next();
			if (eval instanceof AttributeDesignator) {
				if (designator != null) {
					logger.warn("Found duplicate attribute designators: " + designator + " , " + eval);
				} else {
					designator = (AttributeDesignator) eval;
					// apply previously found values
					for (Iterator v = values.iterator(); v.hasNext();) {
						AttributeValue value = (AttributeValue) v.next();
						URI id = designator.getId();	                
						Attribute attr = new Attribute(id, null, null, value);
						// TODO this should be smarter
						Attribute notAttr = new Attribute(id, null, null, new StringAttribute("FALSE-ATTRIBUTE"));
						switch(designator.getDesignatorType()) {
						case AttributeDesignator.SUBJECT_TARGET:
							subjects.add(attr);
							subjects.add(notAttr);
							break;
						case AttributeDesignator.RESOURCE_TARGET:
							resources.add(attr);
							resources.add(notAttr);
							break;
						case AttributeDesignator.ACTION_TARGET:
							actions.add(attr);
							actions.add(notAttr);
							break;
						case AttributeDesignator.ENVIRONMENT_TARGET:
							environments.add(attr);
							environments.add(notAttr);
							break;
						}	
					}
					// clear it
					values.clear();
				}
			} else if (eval instanceof AttributeValue) {
				if (designator == null) {
					values.add(eval);
				} else {
					// have designator and value - add to appropriate set
					AttributeValue value = (AttributeValue) eval;
					URI id = designator.getId();	                
					Attribute attr = new Attribute(id, null, null, value);
					// TODO this should be smarter
					Attribute notAttr = new Attribute(id, null, null, new StringAttribute("FALSE-ATTRIBUTE"));
					switch(designator.getDesignatorType()) {
					case AttributeDesignator.SUBJECT_TARGET:
						subjects.add(attr);
						subjects.add(notAttr);
						break;
					case AttributeDesignator.RESOURCE_TARGET:
						resources.add(attr);
						resources.add(notAttr);
						break;
					case AttributeDesignator.ACTION_TARGET:
						actions.add(attr);
						actions.add(notAttr);
						break;
					case AttributeDesignator.ENVIRONMENT_TARGET:
						environments.add(attr);
						environments.add(notAttr);
						break;
					}
				}
			} else if (eval instanceof Apply) {
				handleCondition((Apply) eval, subjects, resources, actions, environments, designator, values);
			}
		}
	}
	
	private void makeRequests(ArrayList targets, Rule rule) {
//		if (!rule.getId().toString().equals("RPSlist.4.0.3.1.r.1")) {
//			return;
//		}
		// collect targets
		HashSet allSubjects = new HashSet();
		HashSet allResources = new HashSet();
		HashSet allActions = new HashSet();
		HashSet allEnvironments = new HashSet();
		for (Iterator i = targets.iterator(); i.hasNext();) {
			Target t = (Target) i.next();
			handleTargetList(t.getSubjects(), allSubjects);
			handleTargetList(t.getResources(), allResources);
			handleTargetList(t.getActions(), allActions);
		}
		
		// conditions
		handleCondition(rule.getCondition(), allSubjects, allResources, allActions, allEnvironments, null, new HashSet());
			
		// first a request with all of them
		HashSet subject = new HashSet();
		HashSet resource = new HashSet();
		HashSet action = new HashSet();
		HashSet environment = new HashSet();
		subject.addAll(allSubjects);
		resource.addAll(allResources);
		action.addAll(allActions);
		environment.addAll(allEnvironments);
        if (subject.size() <= 1 && resource.size() <= 1 && action.size() <= 1 && environment.size() <= 1) {
        	// will cover below
        } else {        	
        	addRequest(subject, resource, action, environment);
        }
		
		// one of each subject, resource
        Object[] sArr = (allSubjects.size() == 0) ? new Object[1] : allSubjects.toArray();
        Object[] rArr = (allResources.size() == 0) ? new Object[1] : allResources.toArray();
        Object[] aArr = (allActions.size() == 0) ? new Object[1] : allActions.toArray();
        Object[] eArr = (allEnvironments.size() == 0) ? new Object[1] : allEnvironments.toArray();
        for (int s = 0; s < sArr.length; s++) {
			for (int r = 0; r < rArr.length; r++) {
				for (int a = 0; a < aArr.length; a++) {
					for (int e = 0; e < eArr.length; e++) {
						subject.clear();
						resource.clear();
						action.clear();
						environment.clear();
						if (sArr[s] != null) {
							subject.add(sArr[s]);
						}
						if (rArr[r] != null) {
							resource.add(rArr[r]);
						}
						if (aArr[a] != null) {
							action.add(aArr[a]);
						}
						if (eArr[e] != null) {
							environment.add(eArr[e]);
						}
						addRequest(subject, resource, action, environment);
					}
				}
			}
		}
	}
	
	private void addRequest(Set subject, Set resource, Set action, Set environment) {
		// add a default subject?	
		boolean hasSubjId = false;
		for (Iterator i = subject.iterator(); i.hasNext();) {
			Attribute a = (Attribute) i.next();
			if (subjIdURI.equals(a.getId().toString())) {
				hasSubjId = true;
				break;
			}
		}
		if (!hasSubjId) {
			StringAttribute value = new StringAttribute("DEFAULT SUBJECT");
			try {
				subject.add(new Attribute(new URI(subjIdURI), null, null, value));
			} catch (URISyntaxException e) {
				logger.error(e.getMessage(), e);
			}
		}
		boolean hasResourceId = false;
		for (Iterator i = resource.iterator(); i.hasNext();) {
			Attribute a = (Attribute) i.next();
			if (resIdURI.equals(a.getId().toString())) {
				hasResourceId = true;
				break;
			}
		}
		// default resource?
		if (!hasResourceId) {
			StringAttribute value = new StringAttribute("DEFAULT RESOURCE");
			try {
				resource.add(new Attribute(new URI(resIdURI), null, null, value));
			} catch (URISyntaxException e) {
				logger.error(e.getMessage(), e);
			}
		}
		
		HashSet s = new HashSet();
		s.add(new Subject(subject));
		RequestCtx request = new RequestCtx(s, resource, action, environment);
        requests.add(request);
	}

	private void handleTargetList(List list, HashSet set) {
		if (list == null) {
			return;
		}
		for (Iterator i = list.iterator(); i.hasNext();) {
			List l2 = (List) i.next();
			for (Iterator i2 = l2.iterator(); i2.hasNext();) {
				TargetMatch tm = (TargetMatch) i2.next();
				Evaluatable eval = tm.getMatchEvaluatable();
				if (eval instanceof AttributeDesignator) {
					URI id = ((AttributeDesignator) eval).getId();
					AttributeValue value = tm.getMatchValue();	                
					Attribute attr = new Attribute(id, null, null, value);
					set.add(attr);
				}
			}
		}		
	}
	
	public RequestCtx nextRequest() throws Exception {
		return (RequestCtx) requests.get(index++);
	}

	public boolean hasNext() {
		return requests.size() > index;
	}

	public static void main(String[] args) {
		Util.setupLogger();
		if (args.length != 1) {
			logger.error("Usage: java TargetDrivenReqFactory <Policy File>");
			System.exit(1);
		}	
		logger.info("Loading policy...");
		try {
			String out = "target-test";
			File outF = new File(out + ".cov");
			outF.delete();
			Mutator mutator = new Mutator(args[0], out, false);
			TargetDrivenReqFactory factory = new TargetDrivenReqFactory(mutator.getRootPolicy());
			HashSet policies = new HashSet();
			policies.add(mutator.getRootPolicy());
			RuntimeCoverage.collectAllPolicies(policies);
			RuntimeCoverage.reset(out);
			int i = 1;
			while (factory.hasNext()) {			
				try {
					RequestCtx request = factory.nextRequest();
//					request.encode(System.out);
					ResponseCtx resp = mutator.evaluate(request);
//					resp.encode(System.out);
					boolean print = false;
					for (Iterator iter = resp.getResults().iterator(); iter.hasNext();) {
						Result r = (Result) iter.next();
						if (r.getDecision() != Result.DECISION_PERMIT && r.getDecision() != Result.DECISION_DENY) {
							print = true;
							break;
						}
					}
					if (print) {
						request.encode(System.out);
						resp.encode(System.out);
						System.out.println("***********");
					}
					RuntimeCoverage.setRequestFile(Integer.toString(i));
					i++;
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		} catch (Exception e) {
			logger.error("Failed to load policy.", e);
		}
		RuntimeCoverage.writeCovInfo();
		logger.info("Done");
	}
}
