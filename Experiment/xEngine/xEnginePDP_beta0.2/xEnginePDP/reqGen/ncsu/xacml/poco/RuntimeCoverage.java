package reqGen.ncsu.xacml.poco;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import reqGen.com.sun.xacml.AbstractPolicy;
import reqGen.com.sun.xacml.Indenter;
import reqGen.com.sun.xacml.Policy;
import reqGen.com.sun.xacml.PolicyReference;
import reqGen.com.sun.xacml.PolicySet;
import reqGen.com.sun.xacml.PolicyTreeElement;
import reqGen.com.sun.xacml.Rule;
import reqGen.com.sun.xacml.cond.Apply;


public class RuntimeCoverage {
	static private Hashtable policies = new Hashtable();
	static private Hashtable policyCoveringRequests = new Hashtable();
	static private Hashtable rules = new Hashtable();
	static private Hashtable ruleCoveringRequests = new Hashtable();
	static private Hashtable conditions = new Hashtable();
	static private Hashtable conditionCoveringRequests = new Hashtable();
	static private HashSet requests = new HashSet();
	static private Hashtable policySets = new Hashtable();
	static private boolean hasLoadedCoverage = false;
	static public String coverageFileName = null;
	static private String requestFile = null;
	static private Logger logger = Logger.getLogger(RuntimeCoverage.class);

	public static void reset(String covFileName) {
//		rulePolMap = new Hashtable();
		policies = new Hashtable();
		policyCoveringRequests = new Hashtable();
		rules = new Hashtable();
		ruleCoveringRequests = new Hashtable();
		conditions = new Hashtable();
		conditionCoveringRequests = new Hashtable();
		
		requests = new HashSet();
		
		policySets = new Hashtable();
		
		hasLoadedCoverage = false;
		coverageFileName = covFileName;
		requestFile = null;
	}
	
	public static void setRequestFile(String fName) {
		requestFile = fName;
		requests.add(fName);
	}

	private static void processPolicyTreeElements(PolicyTreeElement pe,
			PolicyTreeElement parentPE) {
		if (pe instanceof reqGen.com.sun.xacml.Rule) {
			// add to rule list
			rules.put(/* parentPE.getId() + ":" + */pe.getId().toString(),
					new Integer(0));
			ruleCoveringRequests.put(pe.getId().toString(), new HashSet());
			Rule r = (Rule) pe;
			Apply condition = r.getCondition();
			if (condition != null) {
				// add to condition list
				conditions.put(pe.getId().toString() + ":"
						+ condition.getFunction().getIdentifier().toString()
						+ "-true", new Integer(0));
				conditions.put(pe.getId().toString() + ":"
						+ condition.getFunction().getIdentifier().toString()
						+ "-false", new Integer(0));
				conditionCoveringRequests.put(pe.getId().toString() + ":"
						+ condition.getFunction().getIdentifier().toString()
						+ "-true", new HashSet());
				conditionCoveringRequests.put(pe.getId().toString() + ":"
						+ condition.getFunction().getIdentifier().toString()
						+ "-false", new HashSet());
			}
			return;
		} else if (pe instanceof reqGen.com.sun.xacml.Policy) {
			// add to policy list
			String policyStr = pe.getId().toString();
			policies.put(pe.getId().toString(), new Integer(0));
			policyCoveringRequests.put(pe.getId().toString(), new HashSet());
			if (parentPE != null) {
				// add to policy set list
				String parentPEStr = parentPE.getId().toString();
				HashSet childPolicies = (HashSet) policySets.get(parentPEStr);
				if (childPolicies == null) {
					childPolicies = new HashSet();
					policySets.put(parentPEStr, childPolicies);
				}
				childPolicies.add(pe);
			}
		} else if (pe instanceof PolicySet) {
			// add to policy set list, we'll traverse children shortly
			policySets.put(pe.getId().toString(), new HashSet());
		} else if (pe instanceof PolicyReference) {
			// if reference get the policy
			PolicyReference ref = (PolicyReference) pe;
			processPolicyTreeElements(ref.resolvePolicy(), parentPE);
		} else {
			logger.error("Wrong policy tree elements: " + pe.toString() + "!!!!");
		}

		// process children
		List children = pe.getChildren();
		Iterator itChildren = children.iterator();
		while (itChildren.hasNext()) {
			PolicyTreeElement element = (PolicyTreeElement) (itChildren.next());
			processPolicyTreeElements(element, pe);
			//        	if (element instanceof com.sun.xacml.Rule) {
			//        		rules.put(/*parentPE.getId() + ":" +
			// */element.getId().toString(), new Integer(0));
			//        		ruleCoveringRequests.put(element.getId().toString(), new
			// HashSet());
			//        		Rule r = (Rule)element;
			//        		Apply condition = r.getCondition();
			//        		if (condition != null) {
			//        			conditions.put(element.getId().toString()+":" +
			// condition.getFunction().getIdentifier().toString() + "-true", new
			// Integer(0));
			//        			conditions.put(element.getId().toString()+":" +
			// condition.getFunction().getIdentifier().toString() + "-false",
			// new Integer(0));
			//        			conditionCoveringRequests.put(element.getId().toString()+":" +
			// condition.getFunction().getIdentifier().toString() + "-true", new
			// HashSet());
			//        			conditionCoveringRequests.put(element.getId().toString()+":" +
			// condition.getFunction().getIdentifier().toString() + "-false",
			// new HashSet());
			//        		}
			//        	} else if (element instanceof com.sun.xacml.Policy) {
			//        		processPolicyTreeElements(element, pe);
			//        	} else {
			//        	    System.out.println("Wrong policy tree elements: " +
			// element.toString() + "!!!!");
			//        	    processPolicyTreeElements(element, pe);
			//        	}
		}
	}

	/*
	 * invoked from
	 * com.sun.xacml.finder.impl.FilePolicyModule.findPolicy(EvaluationCtx
	 * context)
	 */
	public static void collectAllPolicies(Set p) {
		if (policies.size() == 0) {
			Iterator it = p.iterator();
			while (it.hasNext()) {
				AbstractPolicy policy = (AbstractPolicy) (it.next());
				processPolicyTreeElements(policy, null);
			}
		}
		if (policies.size() != 0) {
			Enumeration IDs = policies.keys();
			while (IDs.hasMoreElements()) {
				String id = (String) IDs.nextElement();
				if (coverageFileName == null) {
					coverageFileName = id;
					coverageFileName = coverageFileName.replaceAll("\\:", "_");
					coverageFileName = coverageFileName.replaceAll("\\.", "_");
				} 
//				else if (id.compareTo(coverageFileName) < 0) {
//					coverageFileName = id;
//					coverageFileName = coverageFileName.replaceAll("\\:", "_");
//					coverageFileName = coverageFileName.replaceAll("\\.", "_");
//				}
			}
			coverageFileName = coverageFileName.replaceAll(" ", "");
			//System.out.println("cov:"+coverageFileName+";");
			if (coverageFileName.equals("")) {
				coverageFileName = "policy";
			}
			try {
				getCovInfo(coverageFileName + ".cov");
			} catch (Exception e) {
				logger.error("Execption thrown when reading coverage info from file: "
								+ coverageFileName + ".cov " + e.toString(), e);
			}
		}
	}

	/*
	 * invoked from
	 * com.sun.xacml.finder.impl.FilePolicyModule.PolicyFinderResult
	 * findPolicy(EvaluationCtx context)
	 */
	public static void coverPolicy(AbstractPolicy p) {
		if (p instanceof PolicySet) {
			String policySetStr = p.getId().toString();
			HashSet childPolicies = (HashSet) policySets.get(policySetStr);
			Iterator childPoliciesList = childPolicies.iterator();
			while (childPoliciesList.hasNext()) {
				AbstractPolicy policy = (AbstractPolicy) childPoliciesList
						.next();
				hitPolicy(policy);
			}
			return;
		} else if (!(p instanceof Policy)) {
			return;
		}

		hitPolicy(p);
	}

	private static void hitPolicy(AbstractPolicy p) {
		String policyStr = p.getId().toString();
		Integer count = (Integer) policies.get(policyStr);
		if (count == null) {
			logger.warn("Cannot find policy in coverage collection!!!! ");
		} else {
			policies.put(policyStr, new Integer(count.intValue() + 1));
			HashSet requests = (HashSet) policyCoveringRequests.get(policyStr);
			if (requests != null) {
				requests.add(requestFile);
			} else {
				logger.warn("Cannot find associated request set: "
						+ policyStr);
			}
		}
		logger.debug("Cover policy: ");
		logger.debug("===============================");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		p.encode(out, new Indenter());
		logger.debug(out.toString());
		logger.debug("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	}

	/* invoked from com.sun.xacml.Rule.evaluate(EvaluationCtx context) */
	public static void coverRule(Rule r) {
		String ruleStr = r.getId().toString();
		Integer count = (Integer) rules.get(ruleStr);
		if (count == null) {
			logger.warn("Cannot find rule in coverage collection!!!! ");
		} else {
			rules.put(ruleStr, new Integer(count.intValue() + 1));
			HashSet requests = (HashSet) ruleCoveringRequests.get(ruleStr);
			if (requests != null) {
				requests.add(requestFile);
			} else {
				logger.warn("Cannot find associated request set: "
						+ ruleStr);
			}
		}
		logger.debug("Cover rule: ");
		logger.debug("========================");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		r.encode(out, new Indenter());
		logger.debug(out.toString());
		logger.debug("~~~~~~~~~~~~~~~~~~~~~~~~");
	}

	/* invoked from com.sun.xacml.Rule.evaluate(EvaluationCtx context) */
	public static void coverConditionofRule(Rule r, Apply condition,
			boolean branch) {
		String branchStr = "-true";
		if (!branch)
			branchStr = "-false";
		String condtionStr = r.getId().toString() + ":"
				+ condition.getFunction().getIdentifier().toString()
				+ branchStr;
		Integer count = (Integer) conditions.get(condtionStr);
		if (count == null) {
			logger.warn("Cannot find rule condition in coverage collection!!!! ");
		} else {
			conditions.put(condtionStr, new Integer(count.intValue() + 1));
			HashSet requests = (HashSet) conditionCoveringRequests
					.get(condtionStr);
			if (requests != null) {
				requests.add(requestFile);
			} else {
				logger.warn("Cannot find associated request set: "
						+ condtionStr);
			}
		}

		logger.debug("Cover rule condition: " + branch);
		logger.debug("========");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		condition.encode(out, new Indenter());
		logger.debug(out.toString());
		out = new ByteArrayOutputStream();
		r.encode(out, new Indenter());
		logger.debug(out.toString());
		logger.debug("~~~~~~~~");
	}

	/* invoked from com.sun.xacml.tests.SimplePDP.main(String[]) */
	public static void outputCoverageStatistics(PrintStream o) {
		o.println("####################");
		o.println("Residual coverage details:");
		outputCoverageStatisticsForOneType(o, policies, "Policy", true);
		outputCoverageStatisticsForOneType(o, rules, "Rule", true);
		outputCoverageStatisticsForOneType(o, conditions, "ConditionBranch",
				true);
		o.println("####################");
		o.println("Coverage statistics:");
		outputCoverageStatisticsForOneType(o, policies, "Policy", false);
		outputCoverageStatisticsForOneType(o, rules, "Rule", false);
		outputCoverageStatisticsForOneType(o, conditions, "ConditionBranch",
				false);

		writeCovInfo();

	}
	
	public static int getPolicySetCount() {
		return getCount(policySets);
	}
	
	public static int getPolicyCount() {
		return getCount(policies);
	}
	
	public static int getRuleCount() {
		return getCount(rules);
	}
	
	public static int getCondCount() {
		return getCount(conditions);
	}
	
	public static int getCount(Hashtable h) {
		return h.size();
	}
	
	public static double getPolicyCovPercent() {
		return getCovPercent(policies);
	}
	
	public static double getRuleCovPercent() {
		return getCovPercent(rules);
	}
	
	public static double getCondCovPercent() {
		return getCovPercent(conditions);
	}
	
	private static double getCovPercent(Hashtable h) {
		if (h.size() == 0) {
			return 0;
		}
		double covered = 0;
		for (Enumeration keys = h.keys(); keys.hasMoreElements();) {
			Integer count = (Integer) h.get(keys.nextElement());
			if (count.intValue() != 0) {
				covered++;
			}
		}
		double total = h.size();
		return covered / total;
	}

	private static void outputCoverageStatisticsForOneType(PrintStream o,
			Hashtable h, String typeName, boolean outputDetails) {
		o.println("#Total" + typeName + ": " + h.size());
		Enumeration IDs = h.keys();
		int uncoveredCount = 0;
		while (IDs.hasMoreElements()) {
			String id = (String) IDs.nextElement();
			Integer count = (Integer) h.get(id);
			if (count.intValue() == 0) {
				uncoveredCount++;
				if (outputDetails) {
					o.println("Uncovered" + typeName + ": " + id);
				}
			} else {
				if (outputDetails) {
					o.println("Covered" + typeName + ": " + id + " "
							+ count.toString() + " time(s)");
				}
			}
		}
		o.println("#Covered" + typeName + ": " + (h.size() - uncoveredCount));
		if (h.size() != 0) {
			o.println("%Covered" + typeName + ": "
					+ (h.size() - uncoveredCount) * 100 / (h.size()) + "%");
		}
	}

	/**
	 * This method expands any instances of "$home" in the input string to the
	 * string in the "user.home" property as defined by System.getProperty.
	 */
	/*
	 * private static String expand(String s) { int i =
	 * s.toLowerCase().indexOf("$home"); if (i < 0) return s;
	 * 
	 * StringBuffer result = new StringBuffer(); result.append(s.substring(0,
	 * i)); result.append(System.getProperty("user.home"));
	 * result.append(expand(s.substring(i + 5)));
	 * 
	 * return result.toString(); } // expand
	 */

	private static void outputCoverageStatisticsForOneTypeToFile(PrintWriter o,
			Hashtable h, String typeName, boolean outputDetails) {
		o.println("#Total" + typeName + ": " + h.size());
		Enumeration IDs = h.keys();
		int uncoveredCount = 0;
		while (IDs.hasMoreElements()) {
			String id = (String) IDs.nextElement();
			Integer count = (Integer) h.get(id);
			if (count.intValue() == 0) {
				uncoveredCount++;
				if (outputDetails) {
					o.println("Uncovered" + typeName + ": " + id);
				}
			} else {
				if (outputDetails) {
					o.println("Covered" + typeName + ": " + id + " "
							+ count.toString() + " time(s)");
				}
			}
		}
		o.println("#Covered" + typeName + ": " + (h.size() - uncoveredCount));
		if (h.size() != 0) {
			o.println("%Covered" + typeName + ": "
					+ (h.size() - uncoveredCount) * 100 / (h.size()) + "%");
		}
	}

	private static void outputCoverageRequestsForOneTypeToFile(PrintWriter o,
			Hashtable h, String typeName) {
		Enumeration IDs = h.keys();
		while (IDs.hasMoreElements()) {
			String id = (String) IDs.nextElement();
			HashSet requests = (HashSet) h.get(id);
			if (requests != null) {
				Iterator it = requests.iterator();
				while (it.hasNext()) {
					String request = (String) (it.next());
					o.println("Covered" + typeName + "Request: " + id + " "
							+ request);
				}
			}
		}
	}

	/**
	 * This method provides the ability to read the .cov file Use the
	 * corresponding writeNames() method to store changes back into the .config
	 * file.
	 */
	public synchronized static void getCovInfo(String covFileName)
			throws IOException {
		hasLoadedCoverage = true;
		if (covFileName == null) {
			return;
		}
		File f = new File(covFileName);
		if (!f.exists()) {
			return;
		}
		
		Reader r = new BufferedReader(new FileReader(f));

		StreamTokenizer tok = new StreamTokenizer(r);

		tok.wordChars('!', '~');
		tok.ordinaryChar('"');
		tok.quoteChar('"');

		do {
			tok.nextToken();

			if (tok.ttype == StreamTokenizer.TT_WORD) {
				String word = tok.sval;
				tok.nextToken();
				if (word.equalsIgnoreCase("CoveredPolicy:")) {
					if (policies.containsKey(tok.sval)) {
						String name = tok.sval;
						tok.nextToken();
						int count = (int) tok.nval;
						policies.put(name, new Integer(count));
						tok.nextToken();//"time(s)"
					} else {
						logger.warn("Cannot find covered policy name in loaded policies: "
										+ tok.sval);
					}
				} else if (word.equalsIgnoreCase("CoveredPolicyRequest:")) {
					if (policyCoveringRequests.containsKey(tok.sval)) {
						String name = tok.sval;
						tok.nextToken();
						String requestName = (String) tok.sval;
						HashSet request = (HashSet) policyCoveringRequests
								.get(name);
						request.add(requestName);
					} else {
						logger.warn("Cannot find covered policy name in loaded policies: "
										+ tok.sval);
					}
				} else if (word.equalsIgnoreCase("UncoveredPolicy:")) {
					if (!policies.containsKey(tok.sval)) {
						logger.warn("Cannot find uncovered policy name in loaded policies: "
										+ tok.sval);
					}
				} else if (word.equalsIgnoreCase("CoveredRule:")) {
					if (rules.containsKey(tok.sval)) {
						String name = tok.sval;
						tok.nextToken();
						int count = (int) tok.nval;
						rules.put(name, new Integer(count));
						tok.nextToken();//"time(s)"
					} else {
						logger.warn("Cannot find covered rule name in loaded rules: "
										+ tok.sval);
					}
				} else if (word.equalsIgnoreCase("CoveredRuleRequest:")) {
					if (ruleCoveringRequests.containsKey(tok.sval)) {
						String name = tok.sval;
						tok.nextToken();
						String requestName = (String) tok.sval;
						HashSet request = (HashSet) ruleCoveringRequests
								.get(name);
						request.add(requestName);
					} else {
						logger.warn("Cannot find covered rule name in loaded rules: "
										+ tok.sval);
					}
				} else if (word.equalsIgnoreCase("UncoveredRule:")) {
					if (!rules.containsKey(tok.sval)) {
						logger.warn("Cannot find uncovered rule name in loaded rules: "
										+ tok.sval);
					}
				} else if (word.equalsIgnoreCase("CoveredConditionBranch:")) {
					if (conditions.containsKey(tok.sval)) {
						String name = tok.sval;
						tok.nextToken();
						int count = (int) tok.nval;
						conditions.put(name, new Integer(count));
						tok.nextToken();//"time(s)"
					} else {
						logger.warn("Cannot find covered condition branch name in loaded conditions: "
										+ tok.sval);
					}
				} else if (word
						.equalsIgnoreCase("CoveredConditionBranchRequest:")) {
					if (conditionCoveringRequests.containsKey(tok.sval)) {
						String name = tok.sval;
						tok.nextToken();
						String requestName = (String) tok.sval;
						HashSet request = (HashSet) conditionCoveringRequests
								.get(name);
						request.add(requestName);
					} else {
						logger.warn("Cannot find covered rule name in loaded rules: "
										+ tok.sval);
					}
				} else if (word.equalsIgnoreCase("UncoveredConditionBranch:")) {
					if (!conditions.containsKey(tok.sval)) {
						logger.warn("Cannot find uncovered condition branch name in loaded conditions: "
										+ tok.sval);
					}
				} else if (word.equalsIgnoreCase("RequestName:")) {
					String requestName = (String) tok.sval;
					requests.add(requestName);
				}
			}
		} while (tok.ttype != StreamTokenizer.TT_EOF);

		r.close();
	} // getCovInfo

	/**
	 * Writes out the current internal representation of the contents of the
	 * configuration file. Throws an IllegalStateException if the contents
	 * haven't been set or read in.
	 */
	public synchronized static void writeCovInfo() {
		String covFileName = coverageFileName;
		try {
			if (covFileName == null)
				return;
			PrintWriter p = new PrintWriter(new BufferedWriter(new FileWriter(
					covFileName + ".cov")));

			Iterator rqs = requests.iterator();
			while (rqs.hasNext()) {
				String rq = (String) rqs.next();
				p.println("RequestName: " + rq);
			}
			p.println("#Requests: " + requests.size());

			p.println("####################");
			p.println("Residual coverage details:");
			outputCoverageStatisticsForOneTypeToFile(p, policies, "Policy",
					true);
			outputCoverageStatisticsForOneTypeToFile(p, rules, "Rule", true);
			outputCoverageStatisticsForOneTypeToFile(p, conditions,
					"ConditionBranch", true);
			p.println("####################");
			p.println("Covering request details:");
			outputCoverageRequestsForOneTypeToFile(p, policyCoveringRequests,
					"Policy");
			outputCoverageRequestsForOneTypeToFile(p, ruleCoveringRequests,
					"Rule");
			outputCoverageRequestsForOneTypeToFile(p,
					conditionCoveringRequests, "ConditionBranch");

			p.println("####################");
			p.println("Coverage statistics:");
			outputCoverageStatisticsForOneTypeToFile(p, policies, "Policy",
					false);
			outputCoverageStatisticsForOneTypeToFile(p, rules, "Rule", false);
			outputCoverageStatisticsForOneTypeToFile(p, conditions,
					"ConditionBranch", false);

			p.close();
		} catch (IOException e) {
			logger.error("Exception thrown during writing the configuration file "
							+ covFileName + ":" + e, e);
		}
	} // writeNames	
	
	public static HashSet getUnCoveredRules() {
		HashSet ruleIds = new HashSet();
		for (Enumeration keys = rules.keys(); keys.hasMoreElements();) {
			Object key = keys.nextElement();
			Integer count = (Integer) rules.get(key);
			if (count.intValue() == 0) {
				ruleIds.add(key);
			}
		}
		return ruleIds;
	}
}