package reqGen.ncsu.xacml.mutator;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;


import org.apache.log4j.Logger;

import reqGen.com.sun.xacml.Indenter;
import reqGen.com.sun.xacml.ParsingException;
import reqGen.com.sun.xacml.ctx.RequestCtx;
import reqGen.com.sun.xacml.ctx.ResponseCtx;
import reqGen.com.sun.xacml.ctx.Result;
import reqGen.ncsu.util.Util;
import reqGen.ncsu.xacml.RandomRequestFactory;
import reqGen.ncsu.xacml.RequestFactoryIntf;
import reqGen.ncsu.xacml.TargetDrivenReqFactory;
import reqGen.ncsu.xacml.poco.RuntimeCoverage;


public class ReqEvaluator {
	
	private static Logger logger = Logger.getLogger(ReqEvaluator.class);
	
	public static void generateEvaluate_ccs(File[] policies, String outputRoot) {
		for (int i = 0; i < policies.length; i++) {
			generateEvaluate_ccs(new File(policies[i].getPath() + "/" + policies[i].getName() + ".xml"), outputRoot);
		}
	}
	
	public static void generateEvaluate_ccs(File policy, String outputRoot) {
		logger.info("Processing " + policy.getName());
		// generate targeted requests
		HashSet greedySet; 
		double[] covInfo;
		File[] reqs;
		
//		greedySet = new HashSet();
//		covInfo = new double[4];
//		reqs = generateTargetedRequests(policy.getParentFile(), policy, greedySet, covInfo);
//		evaluate(policy.getParentFile(), policy, reqs, greedySet, covInfo, "targeted");
		
		// generate targeted-cirg requests (if any) TODO
//		greedySet = new HashSet();
//		covInfo = new double[4];
//		reqs = generateTargetedCirgRequests(policy, greedySet, covInfo, rules);
//		evaluate(policy.getParentFile(), policy, reqs, greedySet, covInfo, "targeted-cirg");
		
		// generate random requests
//		int maxReq = reqs.length;
		int maxReq = 555;
		greedySet = new HashSet();
		covInfo = new double[4];
		reqs = generateRandomRequests(policy.getParentFile(), policy, greedySet, covInfo, maxReq, outputRoot);
		evaluate(policy.getParentFile(), policy, reqs, greedySet, covInfo, "random", outputRoot);
	}	
	
	public static void generateEvaluate(File policyDir, File policyFile, boolean useCirg, String outputRoot) {
		if (useCirg) {
			String[] ops = {
					"oneToEmpty",
					"CRE", 
//					"RMR", 
//					"RMP"
					};
			String[] type = {
					"one-to-empty",
					"all-to-negate-one-r", 
//					"all-to-minus-one-r", 
//					"all-to-minus-one-p"
					};
			
			for (int i = 0; i < ops.length; i++) {
				// generate cirg requests
				HashSet greedySet = new HashSet();
				double[] covInfo = new double[4];
				File[] reqs = generateCirgRequests(policyDir, policyFile, ops[i], type[i], greedySet, covInfo, outputRoot);
				// evaluate mutants
				evaluate(policyDir, policyFile, reqs, greedySet, covInfo, type[i], outputRoot);
			}
		} else {
			// generate random requests
			HashSet greedySet = new HashSet();
			double[] covInfo = new double[4];
			File[] reqs = generateRandomRequests(policyDir, policyFile, greedySet, covInfo, 50, outputRoot);
			// evaluate mutants
			evaluate(policyDir, policyFile, reqs, greedySet, covInfo, "random", outputRoot);
		}
	}

	/**
	 * This method cleans the directory and generates random requests for evaluation
	 * Use with caution
	 * @param topDir
	 */
	public static void cleanGenerateEvaluate(File topDir, boolean useCirg, String outputRoot) {
		File[] subjects = topDir.listFiles();
		// iterate over subjects here
		for (int i = 0; i < subjects.length; i++) {
			// extract directory and file
			File policyDir = subjects[i];
			File policyFile = null;
			File[] list = policyDir.listFiles();
			for (int l = 0; l < list.length; l++) {
				if (list[l].isDirectory()) {
					continue;
				}
				if (list[l].isFile()) {
					if (policyFile == null) { 
						policyFile = list[l];
					} else {
						logger.warn("Policy file already set to " + policyFile);
						logger.warn("Ignoring unexpected file " + list[l]);
					}
				}
			}
			// cleans the directory
			clean(policyDir, outputRoot);
			
			generateEvaluate(policyDir, policyFile, useCirg, outputRoot);
		}
		outputTotalStats();
	}	

	public static File[] generateTargetedRequests(File policyDir, File policyFile, HashSet greedySet, double[] covInfo, String outputRoot) {
		logger.info("Generating targeted requests for " + policyDir.getName());
		double polCov = 0;
		double ruleCov = 0;
		double condCov = 0;
		double genTime = 0;
		Mutator pdp = null;
		try {
			pdp = new Mutator(policyFile.getPath(), null, false/*copy*/);
		} catch (Exception e1) {
			logger.error("Error creating pdp", e1);
		}
		File reqDir = new File(policyDir.getPath() + "/" + Util.REQ_DIR + "/targeted/");
		if (reqDir.exists()) {
			Util.cleanDir(reqDir, logger);
		} else {
			reqDir.mkdirs();
		}
		HashSet policies = new HashSet();
		policies.add(pdp.getRootPolicy());
		RuntimeCoverage.collectAllPolicies(policies);
		RuntimeCoverage.reset(Util.tablesDir(outputRoot) + "/" + policyDir.getName() + "/coverage-targeted");
		RequestFactoryIntf factory = new TargetDrivenReqFactory(pdp.getRootPolicy());			
		int i = 1;
		double prevPolCov = 0;
		double prevRuleCov = 0;
		double prevCondCov = 0;
		Vector files = new Vector();
		long start = System.currentTimeMillis();
		while (factory.hasNext()) {
			try {
				// make it
				RequestCtx req = factory.nextRequest();
				
				// write it
				String file = reqDir.getPath() + "/" + (i++) + "-req.xml";
				files.add(file);
				if (req == null || file == null) {
					continue;
				}
				Util.createFile(file, logger);
				PrintStream p = new PrintStream(new FileOutputStream(file));
				req.encode(p, new Indenter());
			} catch (Exception e) {
				logger.error("Error on request " + reqDir.getPath() + i, e);
			}
		}
		long stop = System.currentTimeMillis();
		genTime = (double) (stop - start) / (double) 1000;
		Iterator iter = files.iterator();
		while (iter.hasNext()) {
			String file = (String) iter.next();
			// evaluate it					
			RuntimeCoverage.setRequestFile(file);
			RequestCtx request;
			try {
				request = RequestCtx.getInstance(new FileInputStream(file));
				ResponseCtx response = pdp.evaluate(request);
			} catch (FileNotFoundException e) {
				logger.error("Error on " + file, e);
			} catch (ParsingException e) {
				logger.error("Error on " + file, e);
			}
			polCov = RuntimeCoverage.getPolicyCovPercent();
			ruleCov = RuntimeCoverage.getRuleCovPercent();
			condCov = RuntimeCoverage.getCondCovPercent();
			if (prevPolCov < polCov
					|| prevRuleCov < ruleCov
					|| prevCondCov < condCov) {
				// add to greedy set
				greedySet.add(file);
			}
			prevPolCov = polCov;
			prevRuleCov = ruleCov;
			prevCondCov = condCov;	
		}
		RuntimeCoverage.writeCovInfo();
		
		covInfo[0] = polCov;
		covInfo[1] = ruleCov;
		covInfo[2] = condCov;
		covInfo[3] = genTime;
		return reqDir.listFiles();
	}
	
	public static File[] generateRandomRequests(File policyDir, File policyFile, HashSet greedySet, double[] covInfo, int maxReq, String outputRoot) {
		logger.info("Generating random requests for " + policyDir.getName());
		double polCov = 0;
		double ruleCov = 0;
		double condCov = 0;
		double genTime = 0;
		Mutator pdp = null;
		try {
			pdp = new Mutator(policyFile.getPath(), null, false/*copy*/);
		} catch (Exception e1) {
			logger.error("Error creating pdp", e1);
		}
		File reqDir = new File(policyDir.getPath() + "/" + Util.REQ_DIR + "/random/");
		reqDir.mkdirs();
		try {
			HashSet policies = new HashSet();
			policies.add(pdp.getRootPolicy());
			RuntimeCoverage.collectAllPolicies(policies);
			RuntimeCoverage.reset(Util.tablesDir(outputRoot) + "/" + policyDir.getName() + "/coverage-random");
			RequestFactoryIntf factory = new RandomRequestFactory(policies, maxReq);			
			int i = 1;
			double prevPolCov = 0;
			double prevRuleCov = 0;
			double prevCondCov = 0;
			Vector files = new Vector();
			long start = System.currentTimeMillis();
			while (factory.hasNext()) {
				try {
					// make it
					RequestCtx req = factory.nextRequest();
					
					// write it
					String file = reqDir.getPath() + "/" + (i++) + "-req.xml";
					files.add(file);
					if (req == null || file == null) {
						continue;
					}
					Util.createFile(file, logger);
					PrintStream p = new PrintStream(new FileOutputStream(file));
					req.encode(p, new Indenter());
				} catch (Exception e) {
					logger.error("Error on request " + reqDir.getPath() + i, e);
				}
			}
			long stop = System.currentTimeMillis();
			genTime = (double) (stop - start) / (double) 1000;
			Iterator iter = files.iterator();
			while (iter.hasNext()) {
				String file = (String) iter.next();
				// evaluate it					
				RuntimeCoverage.setRequestFile(file);
				RequestCtx request = RequestCtx.getInstance(new FileInputStream(file));
				ResponseCtx response = pdp.evaluate(request);
				polCov = RuntimeCoverage.getPolicyCovPercent();
				ruleCov = RuntimeCoverage.getRuleCovPercent();
				condCov = RuntimeCoverage.getCondCovPercent();
				if (prevPolCov < polCov
						|| prevRuleCov < ruleCov
						|| prevCondCov < condCov) {
					// add to greedy set
					greedySet.add(file);
				}
				prevPolCov = polCov;
				prevRuleCov = ruleCov;
				prevCondCov = condCov;	
			}
			RuntimeCoverage.writeCovInfo();
//			logger.info("Created " + reqDir.listFiles().length + " requests in " + reqDir);
		} catch (Exception e) {
			logger.error("Error instantiating MrgrvPoco", e);
		}
		covInfo[0] = polCov;
		covInfo[1] = ruleCov;
		covInfo[2] = condCov;
		covInfo[3] = genTime;
		return reqDir.listFiles();
	}
	
	public static File[] generateCirgRequests(File policyDir, File policyFile, String op, String type, HashSet greedySet, double[] covInfo, String outputRoot) {		
		logger.info("Generating cirg requests for " + policyDir.getName());
		
		double polCov = 0;
		double ruleCov = 0;
		double condCov = 0;
		double genTime = 0;
		Mutator pdp = null;
		try {
			pdp = new Mutator(policyFile.getPath(), null, false/* copy */);
		} catch (Exception e1) {
			logger.error("Error creating pdp", e1);
		}
//		File reqDir = new File(policyDir.getPath() + "/" + Util.REQ_DIR);
		HashSet policies = new HashSet();
		policies.add(pdp.getRootPolicy());
		RuntimeCoverage.collectAllPolicies(policies);
		RuntimeCoverage.reset(Util.tablesDir(outputRoot) + "/" + policyDir.getName()
				+ "/coverage-" + type);
		double prevPolCov = 0;
		double prevRuleCov = 0;
		double prevCondCov = 0;
		
		ReqGenerator generator = new ReqGenerator();
		generator.setPolicy(policyFile);
		long start = System.currentTimeMillis();
		File[] files = generator.makeCirgReq(op, type);		
		long stop = System.currentTimeMillis();
		genTime = (double) (stop - start) / (double) 1000;
		
		if (files == null) {
			logger.info("No requests.");
			files = new File[0];
		}
		for (int r = 0; r < files.length; r++) {
			String file = files[r].getPath();
			// evaluate it
			RuntimeCoverage.setRequestFile(file);
			try {
				RequestCtx request = RequestCtx.getInstance(new FileInputStream(file));
				ResponseCtx response = pdp.evaluate(request);
				polCov = RuntimeCoverage.getPolicyCovPercent();
				ruleCov = RuntimeCoverage.getRuleCovPercent();
				condCov = RuntimeCoverage.getCondCovPercent();
				if (prevPolCov < polCov || prevRuleCov < ruleCov
						|| prevCondCov < condCov) {
					// add to greedy set
					greedySet.add(file);
				}
				prevPolCov = polCov;
				prevRuleCov = ruleCov;
				prevCondCov = condCov;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// terminates at 100% coverage
			//					if (RuntimeCoverage.getPolicyCount() == 0 || prevPolCov == 1) {
			//						if (RuntimeCoverage.getRuleCount() == 0 || prevRuleCov == 1) {
			//							if (RuntimeCoverage.getCondCount() == 0 || prevCondCov == 1) {
			//								// met coverage
			//								break;
			//							}
			//						}
			//					}
			
		}
		RuntimeCoverage.writeCovInfo();
		covInfo[0] = polCov;
		covInfo[1] = ruleCov;
		covInfo[2] = condCov;
		covInfo[3] = genTime;
		return files;
	}
	
	public static void evaluate(String policyDir, String policyName, String requestDir, String outputDir) {
		
	}
	
	public static void evaluate(File policyDir, File policyFile, File[] reqs, HashSet greedySet, double[] covInfo, String type, String outputRoot) {		
		logger.info("Evaluating " + policyDir.getName());
		double t_totKill = 0; 
		double t_totLive = 0; 
		double t_redKill = 0;	
		long t_startT = System.currentTimeMillis();
		// original evaluator
		Mutator pdp = null;
		try {
			pdp = new Mutator(policyFile.getPath(), null, false/*copy*/);
		} catch (Exception e1) {
			logger.error("Error creating pdp", e1);
		}
		// get ops
		File[] ops = new File(policyDir.getPath() + "/" + Util.MUTANT_DIR).listFiles();
		HashSet liveMutants = new HashSet();
		HashSet deadMutants = new HashSet();
		File[] mutants;
		// iterate over ops
		for (int op = 0; op < ops.length; op++) {
			logger.info("Evaluating " + ops[op]);
			long startT = System.currentTimeMillis();
			// get mutants for op
			liveMutants.clear();
			mutants = ops[op].listFiles();
			for (int mut = 0; mut < mutants.length; mut++) {
				try {
					liveMutants.add(mutants[mut]);//new Mutator(mutants[mut].getPath(), null, false/*copy*/));
				} catch (Exception e) {
					logger.error("Error creating mutant pdp", e);
				}
			}
			// none killed yet
			deadMutants.clear();
			double redKill = 0;
			// iterate over greedy set first
			Iterator reqIter = greedySet.iterator();
			while (reqIter.hasNext()) {
				try {
					RequestCtx r = RequestCtx.getInstance(new FileInputStream(reqIter.next().toString()));
					// evaluate against original and remember decision
					Set origResults = pdp.evaluate(r).getResults();	
					// evaluate against all live mutants
					Iterator living = liveMutants.iterator();
					while (living.hasNext()) {
						try {
							File f = (File) living.next();
							Mutator m = new Mutator(f.getPath(), null, false/*copy*/);//(Mutator) living.next();
							Set results = m.evaluate(r).getResults();
							if (isKilled(origResults, results)) {
//								deadMutants.add(m);
								deadMutants.add(f);
								redKill++;
							}
						} catch (Exception e) {
							logger.debug("Failed to evaluate.", e);
						}
					}
					liveMutants.removeAll(deadMutants);
				} catch (FileNotFoundException e) {
					logger.error("File not found", e);
				} catch (ParsingException e) {
					logger.error("Parsing exception", e);
				}
				
			}
			
			// iterate over requests
			for (int req = 0; req < reqs.length; req++) {
				if (greedySet.contains(reqs[req].toString())) {
					continue; // already evaluated
				}
				try {
					RequestCtx r = RequestCtx.getInstance(new FileInputStream(reqs[req]));
					// evaluate against original and remember decision
					Set origResults = pdp.evaluate(r).getResults();	
					// evaluate against all live mutants
					Iterator living = liveMutants.iterator();
					while (living.hasNext()) {
						try {
							File f = (File) living.next();
							Mutator m = new Mutator(f.getPath(), null, false/*copy*/);
							Set results = m.evaluate(r).getResults();
							if (isKilled(origResults, results)) {
								deadMutants.add(f);
							}
						} catch (Exception e) {
							logger.debug("Failed to evaluate.", e);
						}
					}
					liveMutants.removeAll(deadMutants);
				} catch (FileNotFoundException e) {
					logger.error("File not found", e);
				} catch (ParsingException e) {
					logger.error("Parsing exception", e);
				}
			}
			long stopT = System.currentTimeMillis();
			double genTime = (double) (stopT - startT) / (double) 1000;
			// output op results
			outputOpStats(ops[op].getName(), policyDir.getName(), 
					deadMutants.size(), liveMutants.size(), redKill, genTime, outputRoot);
			t_totKill = t_totKill + deadMutants.size();
			t_totLive = t_totLive + liveMutants.size();
			t_redKill = t_redKill + redKill;
		}
		long t_stopT = System.currentTimeMillis();
		double evalTime = (double) (t_stopT - t_startT) / (double) 1000;
		outputSubjectStats(type, policyDir.getName(), 
				reqs.length, greedySet.size(), t_totKill, 
				t_totLive, t_redKill, covInfo[3], evalTime,
				covInfo[0] * (double) 100, covInfo[1] * (double) 100, covInfo[2] * (double) 100, outputRoot);
	}

	private static boolean isKilled(Set orig, Set result) {
		if (orig.size() > 1) {
			logger.warn("Original result set contains multiple results. " + orig.size());
		}
		if (result.size() > 1) {
			logger.warn("Mutant result set contains multiple results. " + result.size());
		}
		Iterator o = orig.iterator();
		Iterator r = result.iterator();
		if (o.hasNext()) {
			Result ro = (Result) o.next();
			if (r.hasNext()) {
				Result rr = (Result) r.next();
				return (ro.getDecision() != rr.getDecision());
			} else {
				logger.warn("Mutant result set is empty.");
				return false;
			}
		} else {
			logger.warn("Original result set is empty.");
			return false;
		}
	}
	
	// statistics by op for each subject
	private static void outputOpStats(String op, String subject, 
			double totKill, double totLive, double redKill, double genTime, String outputRoot) {
		File file = new File(Util.tablesDir(outputRoot) + "/" + subject + Util.FAULT_REDUCE_OP);
		try {
			boolean firstRun = !file.exists();
			if (firstRun) {
				Util.createFile(file, logger);
			}
			PrintWriter out = new PrintWriter(new BufferedOutputStream(new FileOutputStream(file, true/*append*/)));
			if (firstRun) {
				out.write("Operator\tMutants\tTotKill\tTotKill\\%\tRedKill\tRedKill\\%\tCapRed\tGenTime");
				out.write(Util.SEP);
			}
			double numMutants = totKill + totLive;
			double totPercent = (numMutants != 0) ? (totKill / numMutants) * (double) 100 : 0;
			double redPercent = (numMutants != 0) ? (redKill / numMutants) * (double) 100 : 0;
			double ratio = (totKill != 0) ? (redKill / totKill) : 0;
			double capRed = ((double) 1 - ratio) * (double) 100;
			logger.info(
					"OP=" + op + "," + 
					"TKill%=" + totPercent + "," + 
					"RKill%=" + redPercent + "," +
					"CapRed=" + capRed);
				
			out.write(
					op + "\t" + 
					numMutants + "\t" + 
					totKill + "\t" + 
					totPercent + "\t" + 
					redKill + "\t" + 
					redPercent + "\t" + 
					capRed + "\t" + 
					genTime);
			out.write(Util.SEP);
			out.close();
		} catch (FileNotFoundException e) {
			logger.error("Could not create writer for " + file, e);
		}	
	}
	
	private static void outputSubjectStats(String type, String subject, 
			double numReq, double numRedReq, double totKill, 
			double totLive, double redKill, double genTime, double evalTime,
			double polCov, double ruleCov, double condCov, String outputRoot) {
		File file = new File(Util.tablesDir(outputRoot) + "/" + Util.FAULT_REDUCE);
		try {
			boolean firstRun = !file.exists();
			if (firstRun) {
				Util.createFile(file, logger);
			}
			PrintWriter out = new PrintWriter(new BufferedOutputStream(new FileOutputStream(file, true/*append*/)));
			if (firstRun) {
				out.write("Type\tSubject\tNumReq\tNumRedReq\tMutants\tTotKill\tTotKill\\%\tRedKill\tRedKill\\%\tCapRed\tSizeRed\tPolCov\tRuleCov\tCondCov\tGenTime\tEvalTime");
				out.write(Util.SEP);
			}
			double numMutants = totKill + totLive;
			double totKillP = (numMutants != 0) ? (totKill / numMutants) * (double) 100 : 0;
			double redKillP = (numMutants != 0) ? (redKill / numMutants) * (double) 100 : 0;
			double ratio1 = (numReq != 0) ? (numRedReq / numReq) : 0;
			double sizeRed = ((double) 1 - ratio1) * (double) 100;
			double ratio2 = (totKill != 0) ? (redKill / totKill) : 0;
			double capRed = ((double) 1 - ratio2) * (double) 100;
			logger.info(
					"Subj=" + subject + "," + 
					"SizeRed%=" + sizeRed + "," + 
					"CapRed=" + capRed);
				
			out.write(
					type + "\t" + 
					subject + "\t" + 
					numReq + "\t" + 
					numRedReq + "\t" + 
					numMutants + "\t" + 
					totKill + "\t" + 
					totKillP + "\t" + 
					redKill + "\t" + 
					redKillP + "\t" + 
					capRed + "\t" + 
					sizeRed + "\t" +
					polCov + "\t" + 					
					ruleCov + "\t" + 					
					condCov + "\t" + 					
					genTime + "\t" + 
					evalTime);
			out.write(Util.SEP);
			out.close();
		} catch (FileNotFoundException e) {
			logger.error("Could not create writer for " + file, e);
		}	
	}
	
	private static void outputTotalStats() {
		//TODO 
	}
	
//	private void clean() {
//		File file = new File(Util.TABLES_DIR + Util.FAULT_REDUCE);
//		delete(file);		
//	}
	
	private static void clean(File policyDir, String outputRoot) {
		File file;
		file = new File(Util.tablesDir(outputRoot) + "/" + policyDir.getName() + Util.FAULT_REDUCE);
		delete(file);
		file = new File(policyDir.getPath() + "/" + Util.REQ_DIR);
		delete(file);
		file = new File(Util.tablesDir(outputRoot) + "/" + policyDir.getName() + "/coverage.cov");
		delete(file);
	}
	
	private static void delete(File file) {
		if (file.exists()) {
			if (file.isDirectory()) {
				Util.cleanDir(file, logger);
				logger.info("Removed directory " + file);
			}else if (file.delete()) {
				logger.info("Removed file " + file);
			} else {
				logger.warn("Could not delete " + file);
			}
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Util.setupLogger();
		String outputRoot = "../output";
		if (args.length == 0) {
			logger.info("Initializing evaluator...");
			ReqEvaluator.cleanGenerateEvaluate(new File(Util.policiesDir(outputRoot)), false/*cirg*/, outputRoot);
//			File policyDir = new File(Util.POLICIES_DIR + "/demo-5/");
//			File policyFile = new File(Util.POLICIES_DIR + "/demo-5/RPSlist.xml");
//			File policyDir = new File(Util.POLICIES_DIR + "/default-2/");
//			File policyFile = new File(Util.POLICIES_DIR + "/default-2/default-2.xml");
//			File policyDir = new File(Util.POLICIES_DIR + "/simple-policy/");
//			File policyFile = new File(Util.POLICIES_DIR + "/simple-policy/college.xml");
//			evaluator.clean(policyDir);
//			evaluator.generateRandomRequests(policyDir, policyFile);
//			evaluator.evaluate(policyDir, policyFile);
		} else if (args.length == 1 && "ccs".equalsIgnoreCase(args[0])) {
			File pDir = new File(Util.policiesDir(outputRoot));
			ReqEvaluator.generateEvaluate_ccs(pDir.listFiles(), outputRoot);
		}
		logger.info("Done");
	}
	
}
