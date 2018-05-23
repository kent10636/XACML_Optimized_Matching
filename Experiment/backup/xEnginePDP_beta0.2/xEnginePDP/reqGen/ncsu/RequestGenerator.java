package reqGen.ncsu;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;


import org.apache.log4j.Logger;

import reqGen.com.sun.xacml.Indenter;
import reqGen.com.sun.xacml.ParsingException;
import reqGen.com.sun.xacml.Rule;
import reqGen.com.sun.xacml.ctx.RequestCtx;
import reqGen.ncsu.margrave.MrgrvCIScriptGenerator;
import reqGen.ncsu.margrave.MrgrvExec;
import reqGen.ncsu.util.Util;
import reqGen.ncsu.xacml.RandomRequestFactory;
import reqGen.ncsu.xacml.RequestFactoryIntf;
import reqGen.ncsu.xacml.TargetDrivenReqFactory;
import reqGen.ncsu.xacml.mutator.Mutator;
import reqGen.ncsu.xacml.poco.RuntimeCoverage;


public class RequestGenerator {
	
	private static Logger logger = Logger.getLogger(RequestGenerator.class);
	
	private static int generate(RequestFactoryIntf factory, String output) throws Exception {
		int i = 1;
		while (factory.hasNext()) {
			
			// make it
			RequestCtx req = factory.nextRequest();
			
			// write it
			String file = output + (i++) + "-req.xml";
			Util.createFile(file, logger);
			PrintStream p = new PrintStream(new FileOutputStream(file));
			req.encode(p, new Indenter());
		}
		return i;
	}
	
	private static File cleanIt(String output) {
		File outputF = new File(output);
		File[] contents = outputF.listFiles();
		if (contents != null && contents.length != 0) {
			logger.warn("Deleting existing files.");
			for (int i = 0; i < contents.length; i++) {
				if (!contents[i].delete()) {
					logger.warn("Could not delete file " + contents[i]);
				}
			}
		}
		return outputF;
	}
	
	public static File[] random(String policy, String output, int max) {
	
		File[] requests = {};
		try {
			logger.info("Generating  random requests for " + policy);
			File outputF = cleanIt(output);
			Mutator mutator = new Mutator(policy, "TargetDriveReqFactoryTest", false);
			HashSet policies = new HashSet();
			policies.add(mutator.getRootPolicy());
			RandomRequestFactory factory = new RandomRequestFactory(policies, max);
			int i = generate(factory, output);
			requests = outputF.listFiles();
			if (requests.length != (i-1)) {
				logger.warn("Number generated requests mismatch.");
			}
			logger.info("Generated " + requests.length + " requests");
		} catch (Exception e) {
			logger.error("Error on " + policy, e);
		}
		return requests;
	}
	
	public static File[] targeted(String policy, String output) {
		File[] requests = {};
		try {
			logger.info("Generating targeted requests for " + policy);
			File outputF = cleanIt(output);
			Mutator mutator = new Mutator(policy, "TargetDriveReqFactoryTest", false);
			TargetDrivenReqFactory factory = new TargetDrivenReqFactory(mutator.getRootPolicy());
			int i = generate(factory, output);
			requests = outputF.listFiles();
			if (requests.length != (i-1)) {
				logger.warn("Number generated requests mismatch.");
			}
			logger.info("Generated " + requests.length + " requests");
		} catch (Exception e) {
			logger.error("Error on " + policy, e);
		}
		return requests;
	}
	
	public static File[] targetedPlusCirg(String policy, String output, HashSet ruleIds) {
		File[] requests = {};
		logger.info("Generating targeted-cirg requests for " + policy);
		
		File outputF = cleanIt(output);		
		
		// copy to remove conditions
		File policyF = new File(policy);
		File copy = new File(policyF.getParent() + "/rmCond.xml");
		Mutator mutator;
		try {
			mutator = new Mutator(policy, policyF.getName(), false/*mutate*/);
			mutator.copyRmAllConditions(copy);
			mutator = null;
		} catch (Exception e) {
			logger.error("Failed to remove conditions for " + policyF.getName(), e);
		}
		
		// create versions of specific rules
		File[] versions = null;
		Hashtable versionRuleMap = null;
		File dir = new File(copy.getParent() + "/versions/");
		
		try {
			mutator = new Mutator(copy.getPath(), policyF.getName(), false/*mutate*/);			
			versionRuleMap = mutator.createVersions(ruleIds, dir);
			versions = dir.listFiles();
			mutator = null;
		} catch (Exception e) {
			logger.error("Failed to create versions for " + policyF.getName(), e);
		}
		
		if (versions == null) {
			logger.error("Failed to create versions of " + policyF.getName());
			return new File[0];
		}
		
		// make requests and identify redundant rules
		File reqDir = new File(output);
		int numReq = 0;
		HashSet redundantRules = new HashSet();
		for (int i = 0; i < versions.length; i++) {
			// generate and execute script
			logger.info("Executing script on " + versions[i].getName() + "...");
			MrgrvCIScriptGenerator.generateScript(copy.getPath(), versions[i].getPath());
			MrgrvExec.execScriptSmart(MrgrvCIScriptGenerator.getScriptName(), reqDir);
			Rule r = (Rule) versionRuleMap.get(versions[i].getName());
			String[] ls = reqDir.list();
			if (ls != null) {
				if (numReq == ls.length) {
					logger.info("Found redundant rule: " + r.getId().toString());
					redundantRules.add(r);
				} else {
					logger.info("Found counter-examples for rule: " + r.getId().toString());
				}
				numReq = ls.length;
			} else {
				logger.info("Found redundant rule: " + r.getId().toString());
				redundantRules.add(r);
			}
		}
		requests = reqDir.listFiles();
		logger.info("Generated " + requests.length + " requests");
		logger.info("Found " + redundantRules.size() + " redundant rules");
		for (Iterator iter = redundantRules.iterator(); iter.hasNext();) {
			logger.info(((Rule) iter.next()).getId().toString());
		}
		
//		logger.info("Removing redundant rules.");
//		try {
//			File f = new File(policyF.getParent() + "/rmRedRules.xml");
//			mutator = new Mutator(policy, policyF.getName(), false/*mutate*/);
//			mutator.copyRmRules(redundantRules, f);
//			mutator = null;
//		} catch (Exception e) {
//			logger.error("Failed to remove redundant rules for " + policyF.getName(), e);
//		}
		
		return (requests != null) ? requests : new File[0];
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Util.setupLogger();
		if (args.length != 1) {
			logger.info("Usage: java RequestGenerator <policiesDir>");
			System.exit(1);
		} else {
			File policiesDir = new File(args[0]);
			File stats = new File(Util.tablesDir("../output") + "/req-gen-eval.txt");
			boolean firstRun = !stats.exists();
			if (firstRun) {
				Util.createFile(stats, logger);
			}
			PrintWriter out = null;			
			try {
				out = new PrintWriter(new BufferedOutputStream(new FileOutputStream(stats, true/*append*/)));
			} catch (FileNotFoundException e) {
				logger.error("Could not open out file.", e);
			}
			
			if (firstRun) {
				out.write("Policy\tMethod\tRequests\tMin-Requests\tGenTime\tPolicyCov\tRuleCov\tCondCov\tKill\tMin-Kill");
				out.write(Util.SEP);
			}
			File[] policies = policiesDir.listFiles();
			
	 
			for (int oo=0; oo < policies.length ; oo++){
//			System.out.println("="+policies[oo].getPath() + " "+policies[oo].getName() );
			}
 	
			
			
			
//			File[] policies = {new File("../ccs06-output/policies/mod-fedora/")};
			String policy, output;
			for (int i = 0; i < policies.length; i++) {				
				policy = policies[i].getPath() + "/" + policies[i].getName() + ".xml";
				int numReq = 0;
				
				output = policies[i].getPath() + "/requests/targeted/";
				long start = System.currentTimeMillis();
//				File[] targeted = {};
				File[] targeted = targeted(policy, output); 
				long stop = System.currentTimeMillis();
				long time = stop - start;
				numReq = targeted.length;
				HashSet greedySet = new HashSet();
				// evaluate targeted to get uncovered rules				
				if (targeted.length > 0) {
					Mutator mutator = null;
					try {
						mutator = new Mutator(policy, "targeted", false);
					} catch (Exception e) {
						logger.error("Could not create mutator for " + policy, e);
					}
					HashSet policySet = new HashSet();
					policySet.add(mutator.getRootPolicy());
					RuntimeCoverage.collectAllPolicies(policySet);						
					RuntimeCoverage.reset(Util.tablesDir("../output") + "/" + policies[i].getName() + "/coverage-targeted");
					double prevPolCov = 0;
					double prevRuleCov = 0;
					double prevCondCov = 0;
					/*
					for (int t = 0; t < targeted.length; t++) {
						try {
							RuntimeCoverage.setRequestFile(targeted[t].getName());
							RequestCtx request = RequestCtx.getInstance(new FileInputStream(targeted[t]));
							mutator.evaluate(request);
							double polCov = RuntimeCoverage.getPolicyCovPercent();
							double ruleCov = RuntimeCoverage.getRuleCovPercent();
							double condCov = RuntimeCoverage.getCondCovPercent();
							if (prevPolCov < polCov
									|| prevRuleCov < ruleCov
									|| prevCondCov < condCov) {
								// add to greedy set
								greedySet.add(targeted[t]);
							}
							prevPolCov = polCov;
							prevRuleCov = ruleCov;
							prevCondCov = condCov;
						} catch (FileNotFoundException e) {
							logger.error("Error on " + targeted[t], e);
						} catch (ParsingException e) {
							logger.error("Error on " + targeted[t], e);
						}							
					}
					RuntimeCoverage.writeCovInfo();
					*/
				}
				
				
				out.write(policies[i].getName());
				out.write("\t");
				out.write("targeted");
				out.write("\t");
				out.write(Util.getNumberFormat().format(numReq));
				out.write("\t");
				out.write(Util.getNumberFormat().format(greedySet.size()));
				out.write("\t");
				out.write(Util.getNumberFormat().format(time));		
				out.write("\t");
				out.write(Util.getNumberFormat().format(RuntimeCoverage.getPolicyCovPercent()));
				out.write("\t");
				out.write(Util.getNumberFormat().format(RuntimeCoverage.getRuleCovPercent()));
				out.write("\t");
				out.write(Util.getNumberFormat().format(RuntimeCoverage.getCondCovPercent()));
				out.write(Util.SEP);
				
				output = policies[i].getPath() + "/requests/targeted-cirg/";
				start = System.currentTimeMillis();
//				File[] targetedPlusCirg = {};
				File[] targetedPlusCirg = targetedPlusCirg(policy, output, RuntimeCoverage.getUnCoveredRules());
				stop = System.currentTimeMillis();
				numReq = numReq + targetedPlusCirg.length;
				time = time + (stop - start);					
				
				if (targetedPlusCirg.length > 0) {
					Mutator mutator = null;
					try {
						mutator = new Mutator(policy, "targeted-cirg", false);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					HashSet policySet = new HashSet();
					policySet.add(mutator.getRootPolicy());
					RuntimeCoverage.collectAllPolicies(policySet);						
					RuntimeCoverage.reset(targetedPlusCirg[0].getParent()+"/coverage");
					for (int t = 0; t < targeted.length; t++) {
						RequestCtx request;
						try {
							request = RequestCtx.getInstance(new FileInputStream(targeted[t]));
							mutator.evaluate(request);
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ParsingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}							
					}
					
					/*
					for (int t = 0; t < targetedPlusCirg.length; t++) {
						RequestCtx request;
						try {
							request = RequestCtx.getInstance(new FileInputStream(targetedPlusCirg[t]));
							mutator.evaluate(request);	
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ParsingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}						
					}
					RuntimeCoverage.writeCovInfo();
					*/
				}
				
				out.write(policies[i].getName());
				out.write("\t");
				out.write("targeted-cirg");
				out.write("\t");
				out.write(Util.getNumberFormat().format(numReq));
				out.write("\t");
				out.write(Util.getNumberFormat().format(time));
				out.write("\t");
				out.write(Util.getNumberFormat().format(RuntimeCoverage.getPolicyCovPercent()));
				out.write("\t");
				out.write(Util.getNumberFormat().format(RuntimeCoverage.getRuleCovPercent()));
				out.write("\t");
				out.write(Util.getNumberFormat().format(RuntimeCoverage.getCondCovPercent()));
				out.write(Util.SEP);
				
				output = policies[i].getPath() + "/requests/random/";
				start = System.currentTimeMillis();
				File[] random = random(policy, output, numReq);
//				File[] random = {};
				numReq = random.length;
				stop = System.currentTimeMillis();
				time = stop - start;
				
				if (random.length > 0) {
					Mutator mutator = null;
					try {
						mutator = new Mutator(policy, "random", false);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					HashSet policySet = new HashSet();
					policySet.add(mutator.getRootPolicy());
					RuntimeCoverage.collectAllPolicies(policySet);						
					RuntimeCoverage.reset(random[0].getParent()+"/coverage");
					
					/*
					for (int t = 0; t < random.length; t++) {
						RequestCtx request;
						try {
							request = RequestCtx.getInstance(new FileInputStream(random[t]));
							mutator.evaluate(request);	
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ParsingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}						
					}
					RuntimeCoverage.writeCovInfo();
					*/
				}
				
				
				out.write(policies[i].getName());
				out.write("\t");
				out.write("random");
				out.write("\t");
				out.write(Util.getNumberFormat().format(numReq));
				out.write("\t");
				out.write(Util.getNumberFormat().format(time));
				out.write("\t");
				out.write(Util.getNumberFormat().format(RuntimeCoverage.getPolicyCovPercent()));
				out.write("\t");
				out.write(Util.getNumberFormat().format(RuntimeCoverage.getRuleCovPercent()));
				out.write("\t");
				out.write(Util.getNumberFormat().format(RuntimeCoverage.getCondCovPercent()));
				out.write(Util.SEP);			
			}
			out.close();
		}
		logger.info("Done");
	}
}
