package reqGen.ncsu.xacml.mutator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import reqGen.com.sun.xacml.AbstractPolicy;
import reqGen.com.sun.xacml.Indenter;
import reqGen.com.sun.xacml.Policy;
import reqGen.com.sun.xacml.PolicySet;
import reqGen.com.sun.xacml.Rule;
import reqGen.com.sun.xacml.combine.CombiningAlgorithm;
import reqGen.com.sun.xacml.combine.PolicyCombiningAlgorithm;
import reqGen.com.sun.xacml.combine.RuleCombiningAlgorithm;
import reqGen.com.sun.xacml.combine.StandardCombiningAlgFactory;
import reqGen.com.sun.xacml.ctx.RequestCtx;
import reqGen.ncsu.margrave.MrgrvCIScriptGenerator;
import reqGen.ncsu.margrave.MrgrvExec;
import reqGen.ncsu.margrave.MrgrvPolicyFinderModule;
import reqGen.ncsu.util.Util;
import reqGen.ncsu.xacml.RequestFactoryIntf;



public class MutatorPolicyFinderModule extends MrgrvPolicyFinderModule {
	
	private double numMutants, numEquiv;
	private String lastOp;

	public MutatorPolicyFinderModule(String listFile) {
		super(listFile);
		clearMetrics();
	}
	
	public AbstractPolicy getRootPolicy() {
		return (AbstractPolicy) policyRoot.getUserObject();
	}

	public void PSTT(File dir) {
		clearMetrics();
		lastOp = "PSTT";
		HashSet allPolicies = collectPolicySets();
		int i = 1;
		for (Iterator iter = allPolicies.iterator(); iter.hasNext();) {
			// set up the file and output stream
			File mutantF = new File(dir.getPath() + "/" + (i++) + ".xml");
			Util.createFile(mutantF, logger);
			PrintStream outS = null;
			try {
				outS = new PrintStream (new FileOutputStream (mutantF));
			} catch (FileNotFoundException e) {
				logger.error("File not found. " + mutantF, e);
			}
			// set mutant type, encode, and unset mutant type
			PolicySet p = (PolicySet) iter.next();
			p.setMutantType("PSTT");
			AbstractPolicy policy = (AbstractPolicy) policyRoot.getUserObject();
			boolean equivalent = !policy.encode(outS, new Indenter(), p);
			outS.close();
			handleMetrics(equivalent, mutantF);			
			p.setMutantType(null);
		}
		checkCount(dir);
	}
	
	public void PSTF(File dir) {
		clearMetrics();
		lastOp = "PSTF";
		HashSet allPolicies = collectPolicySets();
		int i = 1;
		for (Iterator iter = allPolicies.iterator(); iter.hasNext();) {
			// set up the file and output stream
			File mutantF = new File(dir.getPath() + "/" + (i++) + ".xml");
			Util.createFile(mutantF, logger);
			PrintStream outS = null;
			try {
				outS = new PrintStream (new FileOutputStream (mutantF));
			} catch (FileNotFoundException e) {
				logger.error("File not found. " + mutantF, e);
			}
			// set mutant type, encode, and unset mutant type
			PolicySet p = (PolicySet) iter.next();
			p.setMutantType("PSTF");
			AbstractPolicy policy = (AbstractPolicy) policyRoot.getUserObject();
			boolean equivalent = !policy.encode(outS, new Indenter(), p);
			outS.close();
			handleMetrics(equivalent, mutantF);			
			p.setMutantType(null);
		}
		checkCount(dir);
	}
	
	public void PTT(File dir) {
		clearMetrics();
		lastOp = "PTT";
		HashSet allPolicies = collectPolicies();
		int i = 1;
		for (Iterator iter = allPolicies.iterator(); iter.hasNext();) {
			// set up the file and output stream
			File mutantF = new File(dir.getPath() + "/" + (i++) + ".xml");
			Util.createFile(mutantF, logger);
			PrintStream outS = null;
			try {
				outS = new PrintStream (new FileOutputStream (mutantF));
			} catch (FileNotFoundException e) {
				logger.error("File not found. " + mutantF, e);
			}
			// set mutant type, encode, and unset mutant type
			Policy p = (Policy) iter.next();
			p.setMutantType("PTT");
			AbstractPolicy policy = (AbstractPolicy) policyRoot.getUserObject();
			boolean equivalent = !policy.encode(outS, new Indenter(), p);
			outS.close();
			handleMetrics(equivalent, mutantF);			
			p.setMutantType(null);
		}
		checkCount(dir);
	}
	
	public void PTF(File dir) {
		clearMetrics();
		lastOp = "PTF";
		HashSet allPolicies = collectPolicies();
		int i = 1;
		for (Iterator iter = allPolicies.iterator(); iter.hasNext();) {
			// set up the file and output stream
			File mutantF = new File(dir.getPath() + "/" + (i++) + ".xml");
			Util.createFile(mutantF, logger);
			PrintStream outS = null;
			try {
				outS = new PrintStream (new FileOutputStream (mutantF));
			} catch (FileNotFoundException e) {
				logger.error("File not found. " + mutantF, e);
			}
			// set mutant type, encode, and unset mutant type
			Policy p = (Policy) iter.next();
			p.setMutantType("PTF");
			AbstractPolicy policy = (AbstractPolicy) policyRoot.getUserObject();
			boolean equivalent = !policy.encode(outS, new Indenter(), p);
			outS.close();
			handleMetrics(equivalent, mutantF);			
			p.setMutantType(null);
		}
		checkCount(dir);
	}
	
	public void RTT(File dir) {
		clearMetrics();
		lastOp = "RTT";
		HashSet allRules = collectRules();
		int i = 1;
		for (Iterator iter = allRules.iterator(); iter.hasNext();) {
			// set up the file and output stream
			File mutantF = new File(dir.getPath() + "/" + (i++) + ".xml");
			Util.createFile(mutantF, logger);
			PrintStream outS = null;
			try {
				outS = new PrintStream (new FileOutputStream (mutantF));
			} catch (FileNotFoundException e) {
				logger.error("File not found. " + mutantF, e);
			}
			// set mutant type, encode, and unset mutant type
			Rule r = (Rule) iter.next();
			r.setMutantType("RTT");
			AbstractPolicy policy = (AbstractPolicy) policyRoot.getUserObject();
			boolean equivalent = !policy.encode(outS, new Indenter(), r);
			outS.close();
			handleMetrics(equivalent, mutantF);			
			r.setMutantType(null);
		}
		checkCount(dir);
	}
	
	public void RTF(File dir) {
		clearMetrics();
		lastOp = "RTF";
		HashSet allRules = collectRules();
		int i = 1;
		for (Iterator iter = allRules.iterator(); iter.hasNext();) {
			// set up the file and output stream
			File mutantF = new File(dir.getPath() + "/" + (i++) + ".xml");
			Util.createFile(mutantF, logger);
			PrintStream outS = null;
			try {
				outS = new PrintStream (new FileOutputStream (mutantF));
			} catch (FileNotFoundException e) {
				logger.error("File not found. " + mutantF, e);
			}
			// set mutant type, encode, and unset mutant type
			Rule r = (Rule) iter.next();
			r.setMutantType("RTF");
			AbstractPolicy policy = (AbstractPolicy) policyRoot.getUserObject();
			boolean equivalent = !policy.encode(outS, new Indenter(), r);
			outS.close();
			handleMetrics(equivalent, mutantF);			
			r.setMutantType(null);
		}
		checkCount(dir);
	}
	
	public void copyRmRules(HashSet ruleIds, File output) {
		HashSet allRules = collectRules();
		for (Iterator iter = allRules.iterator(); iter.hasNext();) {
			Rule r = (Rule) iter.next();
			if (ruleIds.contains(r.getId().toString())) {
				r.setMutantType("RMR");
			}
		}
		
		// set up the file and output stream
		Util.createFile(output, logger);
		PrintStream outS = null;
		try {
			outS = new PrintStream (new FileOutputStream(output));
		} catch (FileNotFoundException e) {
			logger.error("File not found. " + output, e);
		}
		AbstractPolicy policy = (AbstractPolicy) policyRoot.getUserObject();
		policy.setMutantType("oneToEmpty");
		policy.encode(outS, new Indenter(), policy);
		outS.close();
		
		policy.setMutantType(null);
		for (Iterator iter = allRules.iterator(); iter.hasNext();) {
			Rule r = (Rule) iter.next();
			if (ruleIds.contains(r.getId().toString())) {
				r.setMutantType(null);
			}
		}
	}

	public void copyRmAllConditions(File output) {
		HashSet allRules = collectRules();
		for (Iterator iter = allRules.iterator(); iter.hasNext();) {
			// set mutant type, encode, and unset mutant type
			Rule r = (Rule) iter.next();
			r.setMutantType("RCT");
		}
		
		// set up the file and output stream
		Util.createFile(output, logger);
		PrintStream outS = null;
		try {
			outS = new PrintStream (new FileOutputStream(output));
		} catch (FileNotFoundException e) {
			logger.error("File not found. " + output, e);
		}
		AbstractPolicy policy = (AbstractPolicy) policyRoot.getUserObject();
		policy.setMutantType("RCT");
		boolean equivalent = !policy.encode(outS, new Indenter(), policy);
		outS.close();
		
		policy.setMutantType(null);
		for (Iterator iter = allRules.iterator(); iter.hasNext();) {
			// set mutant type, encode, and unset mutant type
			Rule r = (Rule) iter.next();
			r.setMutantType(null);
		}
	}
	
	public void RCT(File dir) {
		clearMetrics();
		lastOp = "RCT";
		HashSet allRules = collectRules();
		int i = 1;
		for (Iterator iter = allRules.iterator(); iter.hasNext();) {
			// set up the file and output stream
			File mutantF = new File(dir.getPath() + "/" + (i++) + ".xml");
			Util.createFile(mutantF, logger);
			PrintStream outS = null;
			try {
				outS = new PrintStream (new FileOutputStream (mutantF));
			} catch (FileNotFoundException e) {
				logger.error("File not found. " + mutantF, e);
			}
			// set mutant type, encode, and unset mutant type
			Rule r = (Rule) iter.next();
			r.setMutantType("RCT");
			AbstractPolicy policy = (AbstractPolicy) policyRoot.getUserObject();
			boolean equivalent = !policy.encode(outS, new Indenter(), r);
			outS.close();
			handleMetrics(equivalent, mutantF);			
			r.setMutantType(null);
		}
		checkCount(dir);
	}
	
	public void RCF(File dir) {
		clearMetrics();
		lastOp = "RCF";
		HashSet allRules = collectRules();
		int i = 1;
		for (Iterator iter = allRules.iterator(); iter.hasNext();) {
			// set up the file and output stream
			File mutantF = new File(dir.getPath() + "/" + (i++) + ".xml");
			Util.createFile(mutantF, logger);
			PrintStream outS = null;
			try {
				outS = new PrintStream (new FileOutputStream (mutantF));
			} catch (FileNotFoundException e) {
				logger.error("File not found. " + mutantF, e);
			}
			// set mutant type, encode, and unset mutant type
			Rule r = (Rule) iter.next();
			r.setMutantType("RCF");
			AbstractPolicy policy = (AbstractPolicy) policyRoot.getUserObject();
			boolean equivalent = !policy.encode(outS, new Indenter(), r);
			outS.close();
			handleMetrics(equivalent, mutantF);			
			r.setMutantType(null);
		}
		checkCount(dir);
	}
	
	public void CPC(File dir) {
		clearMetrics();
		lastOp = "CPC";
		HashSet allPolicies = collectPolicySets();
		int i = 1;
		for (Iterator iter = allPolicies.iterator(); iter.hasNext();) {
			// this one's a bit different
			PolicySet p = (PolicySet) iter.next();
			CombiningAlgorithm oldAlg = p.getCombiningAlg();
			// nested loops iterates over possible combination algorithms			
			for (Iterator algs = StandardCombiningAlgFactory.getFactory().getStandardAlgorithms().iterator(); algs.hasNext();) {
				CombiningAlgorithm alg = (CombiningAlgorithm) algs.next();
				if (alg.getIdentifier().toString().equals(oldAlg.getIdentifier().toString())) {
					// this would result in a copy
					continue;
				}
				if (alg instanceof PolicyCombiningAlgorithm) {
					// set up the file and output stream
					File mutantF = new File(dir.getPath() + "/" + (i++) + ".xml");
					Util.createFile(mutantF, logger);
					PrintStream outS = null;
					try {
						outS = new PrintStream (new FileOutputStream (mutantF));
					} catch (FileNotFoundException e) {
						logger.error("File not found. " + mutantF, e);
					}					
					p.setCombiningAlg(alg);
					AbstractPolicy policy = (AbstractPolicy) policyRoot.getUserObject();
					policy.encode(outS, new Indenter());
					outS.close();
					handleMetrics(false/*not equivalent*/, mutantF);
				}
			}	
			p.setCombiningAlg(oldAlg);			
		}
		checkCount(dir);
	}
	
	public void CRC(File dir) {
		clearMetrics();
		lastOp = "CRC";
		HashSet allPolicies = collectPolicies();
		int i = 1;
		for (Iterator iter = allPolicies.iterator(); iter.hasNext();) {
			// this one's a bit different
			Policy p = (Policy) iter.next();
			CombiningAlgorithm oldAlg = p.getCombiningAlg();
			// nested loops iterates over possible combination algorithms			
			for (Iterator algs = StandardCombiningAlgFactory.getFactory().getStandardAlgorithms().iterator(); algs.hasNext();) {
				CombiningAlgorithm alg = (CombiningAlgorithm) algs.next();
				if (alg.getIdentifier().toString().equals(oldAlg.getIdentifier().toString())) {
					// this would result in a copy
					continue;
				}
				if (alg instanceof RuleCombiningAlgorithm) {
					// set up the file and output stream
					File mutantF = new File(dir.getPath() + "/" + (i++) + ".xml");
					Util.createFile(mutantF, logger);
					PrintStream outS = null;
					try {
						outS = new PrintStream (new FileOutputStream (mutantF));
					} catch (FileNotFoundException e) {
						logger.error("File not found. " + mutantF, e);
					}					
					p.setCombiningAlg(alg);
					AbstractPolicy policy = (AbstractPolicy) policyRoot.getUserObject();
					policy.encode(outS, new Indenter());
					outS.close();
					handleMetrics(false/*not equivalent*/, mutantF);
				}
			}	
			p.setCombiningAlg(oldAlg);			
		}
		checkCount(dir);
	}
	
	public void CPO(File dir) {
		clearMetrics();
		lastOp = "CPO";
//		HashSet allPolicies = collectPolicySets();
//		int i = 1;
//		for (Iterator iter = allPolicies.iterator(); iter.hasNext();) {
//			// set up the file and output stream
//			File mutantF = new File(dir.getPath() + "/" + (i++) + ".xml");
//			Util.createFile(mutantF, logger);
//			PrintStream outS = null;
//			try {
//				outS = new PrintStream (new FileOutputStream (mutantF));
//			} catch (FileNotFoundException e) {
//				logger.error("File not found. " + mutantF, e);
//			}
//			// set mutant type, encode, and unset mutant type
//			PolicySet p = (PolicySet) iter.next();
//			p.setMutantType("CPO");
//			AbstractPolicy policy = (AbstractPolicy) policyRoot.getUserObject();
//			boolean equivalent = !policy.encode(outS, new Indenter(), p);
//			outS.close();
//			handleMetrics(equivalent, mutantF);			
//			p.setMutantType(null);
//		}
		checkCount(dir);
	}
	
	public void CRO(File dir) {
		clearMetrics();
		lastOp = "CRO";
//		HashSet allPolicies = collectPolicies();
//		int i = 1;
//		for (Iterator iter = allPolicies.iterator(); iter.hasNext();) {
//			// set up the file and output stream
//			File mutantF = new File(dir.getPath() + "/" + (i++) + ".xml");
//			Util.createFile(mutantF, logger);
//			PrintStream outS = null;
//			try {
//				outS = new PrintStream (new FileOutputStream (mutantF));
//			} catch (FileNotFoundException e) {
//				logger.error("File not found. " + mutantF, e);
//			}
//			// set mutant type, encode, and unset mutant type
//			Policy p = (Policy) iter.next();
//			p.setMutantType("CRO");
//			AbstractPolicy policy = (AbstractPolicy) policyRoot.getUserObject();
//			boolean equivalent = !policy.encode(outS, new Indenter(), p);
//			outS.close();
//			handleMetrics(equivalent, mutantF);			
//			p.setMutantType(null);
//		}
//		checkCount(dir);
	}
	
	public Hashtable CRE(HashSet rules, File dir) {
		int i = 1;
		Hashtable versionRuleMap = new Hashtable();
		HashSet allRules = collectRules();
		for(Iterator iter = allRules.iterator(); iter.hasNext();) {
			Rule r = (Rule) iter.next();
			if (rules.contains(r.getId().toString())) {
				File mutantF = new File(dir.getPath() + "/" + (i++) + ".xml");
				Util.createFile(mutantF, logger);
				PrintStream outS = null;
				try {
					outS = new PrintStream (new FileOutputStream (mutantF));
				} catch (FileNotFoundException e) {
					logger.error("File not found. " + mutantF, e);
				}
				// set mutant type, encode, and unset mutant type
				r.setMutantType("CRE");
				AbstractPolicy policy = (AbstractPolicy) policyRoot.getUserObject();
				policy.encode(outS, new Indenter(), r);
				outS.close();		
				r.setMutantType(null);
				versionRuleMap.put(mutantF.getName(), r);
			}
		}
		return versionRuleMap;
	}
	
	public void CRE(File dir) {
		clearMetrics();
		lastOp = "CRE";	
		HashSet allRules = collectRules();
		int i = 1;
		for (Iterator iter = allRules.iterator(); iter.hasNext();) {
			// set up the file and output stream
			File mutantF = new File(dir.getPath() + "/" + (i++) + ".xml");
			Util.createFile(mutantF, logger);
			PrintStream outS = null;
			try {
				outS = new PrintStream (new FileOutputStream (mutantF));
			} catch (FileNotFoundException e) {
				logger.error("File not found. " + mutantF, e);
			}
			// set mutant type, encode, and unset mutant type
			Rule r = (Rule) iter.next();
			r.setMutantType("CRE");
			AbstractPolicy policy = (AbstractPolicy) policyRoot.getUserObject();
			boolean equivalent = !policy.encode(outS, new Indenter(), r);
			outS.close();
			handleMetrics(equivalent, mutantF);			
			r.setMutantType(null);
		}
		checkCount(dir);
	}

	public void RMPS(File dir) {
		clearMetrics();
		lastOp = "RMPS";
		HashSet allPolicies = collectPolicySets();
		int i = 1;
		for (Iterator iter = allPolicies.iterator(); iter.hasNext();) {
			// set up the file and output stream
			File mutantF = new File(dir.getPath() + "/" + (i++) + ".xml");
			Util.createFile(mutantF, logger);
			PrintStream outS = null;
			try {
				outS = new PrintStream (new FileOutputStream (mutantF));
			} catch (FileNotFoundException e) {
				logger.error("File not found. " + mutantF, e);
			}
			// set mutant type, encode, and unset mutant type
			PolicySet p = (PolicySet) iter.next();
			p.setMutantType("RMPS");
			AbstractPolicy policy = (AbstractPolicy) policyRoot.getUserObject();
			boolean equivalent = !policy.encode(outS, new Indenter(), p);
			outS.close();
			handleMetrics(equivalent, mutantF);			
			p.setMutantType(null);
		}
		checkCount(dir);
	}
	
	public void RMP(File dir) {
		clearMetrics();
		lastOp = "RMP";
		HashSet allPolicies = collectPolicies();
		int i = 1;
		for (Iterator iter = allPolicies.iterator(); iter.hasNext();) {
			// set up the file and output stream
			File mutantF = new File(dir.getPath() + "/" + (i++) + ".xml");
			Util.createFile(mutantF, logger);
			PrintStream outS = null;
			try {
				outS = new PrintStream (new FileOutputStream (mutantF));
			} catch (FileNotFoundException e) {
				logger.error("File not found. " + mutantF, e);
			}
			// set mutant type, encode, and unset mutant type
			Policy p = (Policy) iter.next();
			p.setMutantType("RMP");
			AbstractPolicy policy = (AbstractPolicy) policyRoot.getUserObject();
			boolean equivalent = !policy.encode(outS, new Indenter(), p);
			outS.close();
			handleMetrics(equivalent, mutantF);			
			p.setMutantType(null);
		}
		checkCount(dir);
	}
	
	public void RMR(File dir) {
		clearMetrics();
		lastOp = "RMR";		
		HashSet allRules = collectRules();
		int i = 1;
		for (Iterator iter = allRules.iterator(); iter.hasNext();) {
			// set up the file and output stream
			File mutantF = new File(dir.getPath() + "/" + (i++) + ".xml");
			Util.createFile(mutantF, logger);
			PrintStream outS = null;
			try {
				outS = new PrintStream (new FileOutputStream (mutantF));
			} catch (FileNotFoundException e) {
				logger.error("File not found. " + mutantF, e);
			}
			// set mutant type, encode, and unset mutant type
			Rule r = (Rule) iter.next();
			r.setMutantType("RMR");
			AbstractPolicy policy = (AbstractPolicy) policyRoot.getUserObject();
			boolean equivalent = !policy.encode(outS, new Indenter(), r);
			outS.close();
			handleMetrics(equivalent, mutantF);			
			r.setMutantType(null);
		}
		checkCount(dir);
	}
	
	public void oneToEmpty(File dir) {
		clearMetrics();
		lastOp = "all-to-empty";
		HashSet allRules = collectRules();
		int i = 1;
		AbstractPolicy policy = (AbstractPolicy) policyRoot.getUserObject();
		policy.setMutantType("oneToEmpty");
		for (Iterator iter = allRules.iterator(); iter.hasNext();) {
			// the rule to show
			Rule theRule = (Rule) iter.next();
			// set up the file and output stream
			File mutantF = new File(dir.getPath() + "/" + (i++) + ".xml");
			Util.createFile(mutantF, logger);
			PrintStream outS = null;
			try {
				outS = new PrintStream (new FileOutputStream (mutantF));
			} catch (FileNotFoundException e) {
				logger.error("File not found. " + mutantF, e);
			}
			// set mutant type for all rules but theRule
			for (Iterator iter2 = allRules.iterator(); iter2.hasNext();) {
				Rule nextRule = (Rule) iter2.next();
				if (theRule != nextRule) {					
					nextRule.setMutantType("RMR");
				}
			}
			// encode
			boolean equivalent = !policy.encode(outS, new Indenter(), policy);
			outS.close();
			handleMetrics(equivalent, mutantF);	
			// unset mutant type
			for (Iterator iter2 = allRules.iterator(); iter2.hasNext();) {
				Rule nextRule = (Rule) iter2.next();
				nextRule.setMutantType(null);
			}
		}
		policy.setMutantType(null);
		checkCount(dir);
	}
	
	private HashSet collectRules() {
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
		return rules;
	}
	
	private HashSet collectPolicySets() {
		HashSet policies = new HashSet();
		for (Enumeration e = policyRoot.breadthFirstEnumeration(); e.hasMoreElements();) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
			if (node.getUserObject() instanceof PolicySet) {
				PolicySet policy = (PolicySet) node.getUserObject();
				policies.add(policy);	
			}
		}
		return policies;
	}
	
	private HashSet collectPolicies() {
		HashSet policies = new HashSet();
		for (Enumeration e = policyRoot.breadthFirstEnumeration(); e.hasMoreElements();) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
			if (node.getUserObject() instanceof Policy) {
				Policy policy = (Policy) node.getUserObject();
				policies.add(policy);	
			}
		}
		return policies;
	}
	
	// metrics
	
	private void clearMetrics() {
		numMutants = 0;
		numEquiv = 0;
		lastOp = null;
	}
	
	public String getLastOp() {
		return lastOp;
	}
	
	public double getNumMutants() {
		return numMutants;
	}
	
	public double getNumEquivalent() {
		return numEquiv;
	}
	
	public void markEquivalent(File dir) {
		logger.info("Finding equivalent mutants...");
		File original = listFile;
		File[] mutants = dir.listFiles();
		for (int i = 0; i < mutants.length; i++) {
			if (mutants[i].getName().indexOf("equiv") != -1) {
				// skip preidentified equivalent mutants
				continue;
			}
			MrgrvCIScriptGenerator.generateScript(original.getPath(), mutants[i].getPath());
			RequestFactoryIntf factory = MrgrvExec.execScript(MrgrvCIScriptGenerator.getScriptName());
			logger.debug("for " + mutants[i]);
			try {
				if (factory == null) {
					System.out.print("-");
					continue;
				}
				RequestCtx request = factory.nextRequest();
				if (request == null) {
					System.out.print("+");
					// equivalent mutant detected
					numEquiv++;
					String path = mutants[i].getParent();
					String name = "/equiv-" + mutants[i].getName();
					if (!mutants[i].renameTo(new File(path + name))) {
						logger.error("Failed to rename " + mutants[i] + " to " + path + name);	
					}
				}
				System.out.print("-");
			} catch (Exception e) {
				logger.error("Exception thrown while detecting equivalent mutants for " + mutants[i], e);
			}
		}	
		System.out.println();
	}
	private void handleMetrics(boolean equivalent, File mutantF) {                   
		if (equivalent) {
//			numEquiv++;
//			String path = mutantF.getParent();
//			String name = "/equiv-" + mutantF.getName();
//			if (!mutantF.renameTo(new File(path + name))) {
//				logger.error("Failed to rename " + mutantF + " to " + path + name);	
//			}
			if (!mutantF.delete()) {
				logger.error("Failed to delete " + mutantF);
			}
		} else {
			numMutants++;
		}
	}
	
	private void checkCount(File dir) {		
		// indirect way of counting number of mutants
		
		if (dir.list() == null || numMutants != dir.list().length) {
			logger.error("Mismatch in counter and number of mutant policies in " + dir);
		}
	}
	
	public File getPolicyFile() {
		return listFile;
	}
}
