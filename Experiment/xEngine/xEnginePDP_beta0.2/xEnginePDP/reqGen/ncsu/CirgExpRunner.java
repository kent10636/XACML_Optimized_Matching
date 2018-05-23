/*
 * Created on Jan 24, 2006
 *
 */
package reqGen.ncsu;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Set;


import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import reqGen.com.sun.xacml.Indenter;
import reqGen.com.sun.xacml.ctx.RequestCtx;
import reqGen.com.sun.xacml.ctx.ResponseCtx;
import reqGen.ncsu.margrave.MrgrvCIScriptGenerator;
import reqGen.ncsu.margrave.MrgrvExec;
import reqGen.ncsu.margrave.MrgrvPoco;
import reqGen.ncsu.margrave.MrgrvPolicySplitter;
import reqGen.ncsu.util.Util;
import reqGen.ncsu.xacml.MrgrvRequestFactory;
import reqGen.ncsu.xacml.RandomRequestFactory;
import reqGen.ncsu.xacml.RequestFactoryIntf;
import reqGen.ncsu.xacml.poco.RuntimeCoverage;


/**
 * @author eemartin
 *
 */
public class CirgExpRunner {

//	static Logger logger = Logger.getLogger(CirgExpRunner.class);
//	
//	private static final int MAX_RAND_REQ = 501;
//	private static final int MAX_CIRG_REQ = 501;
//	
//	private static final String denyAll = "ncsu/margrave/examples/tutorial/xacml-code/deny/RPSlist.xml";
//	private static final String permitAll = "ncsu/margrave/examples/tutorial/xacml-code/permit/RPSlist.xml";
//
//	private static final int RAND = 0;
//	private static final int CIRG1 = 1;
//	private static final int CIRG2 = 2;
//	private static final int CIRG3 = 3;
//	private static final int CIRG4 = 4;
//	private static final int GREEDY_RANDOM = 5;
//	private static final int GREEDY_CIRG1 = 6;
//	private static final int GREEDY_CIRG2 = 7;
//	private static final int GREEDY_CIRG3 = 8;
//	private static final int GREEDY_CIRG4 = 9;
//	private static final int TOTAL = 10;
//	
//	private static final String[] DIRS = {
//			"/rand/",
//			"/cirg1/",
//			"/cirg2/",
//			"/cirg3/",
//			"/cirg4/",
//			"/greedy-rand/",
//			"/greedy-cirg1/",
//			"/greedy-cirg2/",
//			"/greedy-cirg3/",
//			"/greedy-cirg4/"
//			}; 
//	
//	private static final String[] HEADERS = {
//		"Description",
//		"Random",
//		"All-to-Empty",
//		"One-to-Empty",
//		"One-Increment",
//		"All-to-Minus-One",
//		"Greedy Random",
//		"Greedy All-to-Empty",
//		"Greedy One-to-Empty",
//		"Greedy One-Increment",
//		"Greedy All-to-Minus-One"
//	};
//								
//	private String originalPolicy, margravePolicy, outputD;
//	private long[] genStart, genStop, evalStart, evalStop;
//	private int[] genNum;
//	private int[] policyNum, ruleNum, condNum;
//	private double[] policyCov, ruleCov, condCov;
//	
//	public CirgExpRunner(String policyList, String outputDir) {
//		margravePolicy = policyList;
//		File temp = new File(margravePolicy);
//		originalPolicy = temp.getParent() + "/original/" + temp.getName();
//		outputD = outputDir;
//		genStart = new long[TOTAL];
//		genStop = new long[TOTAL];
//		evalStart = new long[TOTAL];
//		evalStop = new long[TOTAL];
//		genNum = new int[TOTAL];
//		policyNum = new int[TOTAL];
//		ruleNum = new int[TOTAL];
//		condNum = new int[TOTAL];
//		policyCov = new double[TOTAL];
//		ruleCov = new double[TOTAL];
//		condCov = new double[TOTAL];
//		
//		// create output directories
//		createOrClean(outputD);
//		for (int i = 0; i < DIRS.length; i++) {
//			createOrClean(outputD + DIRS[i]);
//			createOrClean(outputD + DIRS[i] + "req/");
//			createOrClean(outputD + DIRS[i] + "resp/");
//		}
//	}
//	
//	private void createOrClean(String dir) {
//		File f = new File(dir);
//		if (!f.exists()) {
//			if (!f.mkdirs()) {
//				logger.error("Failed to create " + f.getPath());
//				return;
//			}
//		} else {
//			clean(f);
//		}
//	}
//	
//	private void clean(File f) {
//		File[] files = f.listFiles();
//		for (int i = 0; i < files.length; i++) {
//			if (files[i].isDirectory()) {
//				continue;
//			} else if (!files[i].delete()) {
//				logger.error("Could not delete file " + files[i].getPath());
//			}
//		}
//	}
//	
//	public void startExp() {
//		int start = 0; // for everyone else
////		if (originalPolicy.indexOf("continue") != -1) {
////			start = 1; // for continue skip CIRG
////		}
//		for (int type = start; type < TOTAL; type++) { 
//			logger.info("Generating requests for type " + type);
//			generateRequests(type);
//			logger.info("Evaluating requests for type " + type);
//			evaluateRequests(type);
//			outputFile(outputD + "output_type" + type + ".txt");
//		}
////		logger.info("Generating requests for type " + 6);
////		generateRequests(6);
////		logger.info("Evaluating requests for type " + 6);
////		evaluateRequests(6);
////		logger.info("Generating requests for type " + 7);
////		generateRequests(7);
////		logger.info("Evaluating requests for type " + 7);
////		evaluateRequests(7);
////		logger.info("Writing results to " + outputD);
//		outputFile();
//	}
//	
//	private void generateRequests(int type) {
//		switch (type) {
//		case CIRG1:
////			generateCirg1Requests();
//			return;
//		case RAND:
//			generateRandRequests();
//			return;
//		case CIRG2:
//			generateCirg2Requests();
//			return;
//		case GREEDY_CIRG1:
//			generateGreedyRequests(CIRG1, GREEDY_CIRG1);
//			return;
//		case GREEDY_RANDOM:
//			generateGreedyRequests(RAND, GREEDY_RANDOM);
//			return;
//		case GREEDY_CIRG2:
//			generateGreedyRequests(CIRG2, GREEDY_CIRG2);
//			return;
//		case CIRG3:
//			generateCirg3Requests();
//			return;
//		case GREEDY_CIRG3:
//			generateGreedyRequests(CIRG3, GREEDY_CIRG3);
//			return;
//		case CIRG4:
//			generateCirg4Requests();
//			return;
//		case GREEDY_CIRG4:
//			generateGreedyRequests(CIRG4, GREEDY_CIRG4);
//			return;
//		default:
//			logger.error("Invalid type " + type);
//		}
//	}
//	
//	private void evaluateRequests(int type) {
//		switch (type) {
//		case CIRG1:
//			evalRequests(CIRG1);
//			return;
//		case RAND:
//			evalRequests(RAND);
//			return;
//		case CIRG2:
//			evalRequests(CIRG2);
//			return;
//		case GREEDY_CIRG1:
//			evalRequests(GREEDY_CIRG1);
//			return;
//		case GREEDY_RANDOM:
//			evalRequests(GREEDY_RANDOM);
//			return;
//		case GREEDY_CIRG2:
//			evalRequests(GREEDY_CIRG2);
//			return;
//		case CIRG3:
//			evalRequests(CIRG3);
//			return;
//		case GREEDY_CIRG3:
//			evalRequests(GREEDY_CIRG3);
//			return;
//		case CIRG4:
//			evalRequests(CIRG4);
//			return;
//		case GREEDY_CIRG4:
//			evalRequests(GREEDY_CIRG4);
//			return;
//		default:
//			logger.error("Invalid type " + type);
//		}
//	}
//	
//	private void generateCirg1Requests() {
//		genStart[CIRG1] = System.currentTimeMillis();
//	
//		MrgrvCIScriptGenerator.generateScript(denyAll, margravePolicy);
//		RequestFactoryIntf factory = MrgrvExec.execScript(MrgrvCIScriptGenerator.getScriptName());
//		int num = writeRequests(factory, outputD + DIRS[CIRG1] + "req/", 1);
//
//		MrgrvCIScriptGenerator.generateScript(permitAll, margravePolicy);
//		factory = MrgrvExec.execScript(MrgrvCIScriptGenerator.getScriptName());
//		num = writeRequests(factory, outputD + DIRS[CIRG1] + "req/", (num + 1));
//		
//		genNum[CIRG1] = num;
//		
//		genStop[CIRG1] = System.currentTimeMillis();
//	}
//	
//	private void generateCirg2Requests() {
//		genStart[CIRG2] = System.currentTimeMillis();
//	
//		int num = 0;
//		try {
//			// split policy
//			MrgrvPolicySplitter splitter = new MrgrvPolicySplitter(margravePolicy);
//			int count = splitter.splitPolicyOneAtTime();
//			File policyFile = new File(margravePolicy);
//			String policyName = policyFile.getName();
//			String policyPath = policyFile.getParent();
//			// iterate over pieces
//			for (int i = 1; i <= count; i++) {
//				String next = policyPath + "/" + Integer.toString(i) + "/" + policyName;
//				if (isDecisionDeny(next)) {
//					// decision is deny
//					MrgrvCIScriptGenerator.generateScript(permitAll, next);
//				} else {
//					// decision is permit
//					MrgrvCIScriptGenerator.generateScript(denyAll, next);
//				}
//				RequestFactoryIntf factory = MrgrvExec.execScript(MrgrvCIScriptGenerator.getScriptName());
//				num = writeRequests(factory, outputD + DIRS[CIRG2] + "req/", (num + 1));
//				
//				logger.info("Wrote " + num + " requests...");
//				if (num >= MAX_CIRG_REQ-1) {
//					break;
//				}
//			}	
//		} catch (Exception e) {
//			logger.error("Could not split policy.", e);
//		}
//		genNum[CIRG2] = num;
//		
//		genStop[CIRG2] = System.currentTimeMillis();
//	}
//	
//	private boolean isDecisionDeny(String p) {
//		try {
//			MrgrvPolicySplitter mps = new MrgrvPolicySplitter(p);
//			return mps.isDecisionDeny();
//		} catch (Exception e){
//			logger.error("Could not determine decision" + p, e);
//		}
//		return false;
//	}
//	
//	private void generateCirg3Requests() {
//		genStart[CIRG3] = System.currentTimeMillis();
//	
//		int num = 0;
//		try {
//			// split policy
//			MrgrvPolicySplitter splitter = new MrgrvPolicySplitter(margravePolicy);
//			int count = splitter.splitPolicyNAtTime();
//			File policyFile = new File(margravePolicy);
//			String policyName = policyFile.getName();
//			String policyPath = policyFile.getParent();
//			String prev = null;
//			if (isDecisionDeny(policyPath + "/1/" + policyName)) {
//				// decision is deny
//				prev = permitAll;
//			} else {
//				// decision is permit
//				prev = denyAll;
//			}
//			// iterate over pieces
//			for (int i = 1; i < count + 1; i++) {
//				// create script
//				String next = policyPath + "/" + Integer.toString(i) + "/" + policyName;
//				MrgrvCIScriptGenerator.generateScript(prev, next);
//				// script performs change impact analysis and creates a request factory
//				RequestFactoryIntf factory = MrgrvExec.execScript(MrgrvCIScriptGenerator.getScriptName());
//				num = writeRequests(factory, outputD + DIRS[CIRG3] + "req/", (num + 1));
//				prev = next;
//				logger.info("Wrote " + num + " requests...");
//				if (num >= MAX_CIRG_REQ-1) {
//					break;
//				}
//			}	
//		} catch (Exception e) {
//			logger.error("Could not split policy." + margravePolicy, e);
//		}
//		genNum[CIRG3] = num;
//		
//		genStop[CIRG3] = System.currentTimeMillis();
//	}
//	
//	private void generateCirg4Requests() {
//		genStart[CIRG4] = System.currentTimeMillis();
//	
//		int num = 0;
//		try {
//			// split policy
//			MrgrvPolicySplitter splitter = new MrgrvPolicySplitter(margravePolicy);
//			int count = splitter.splitPolicyMinusOneAtTime();
//			File policyFile = new File(margravePolicy);
//			String policyName = policyFile.getName();
//			String policyPath = policyFile.getParent();
//			// iterate over pieces
//			for (int i = 1; i < count + 1; i++) {
//				// create script
//				String next = policyPath + "/" + Integer.toString(i) + "/" + policyName;
//				MrgrvCIScriptGenerator.generateScript(margravePolicy, next);
//				RequestFactoryIntf factory = MrgrvExec.execScript(MrgrvCIScriptGenerator.getScriptName());
//				num = writeRequests(factory, outputD + DIRS[CIRG4] + "req/", (num + 1));
//				logger.info("Wrote " + num + " requests...");
//				if (num >= MAX_CIRG_REQ-1) {
//					break;
//				}
//			}	
//		} catch (Exception e) {
//			logger.error("Could not split policy.", e);
//		}
//		genNum[CIRG4] = num;
//		
//		genStop[CIRG4] = System.currentTimeMillis();
//	}
//	
//	private void generateRandRequests() {
//		genStart[RAND] = System.currentTimeMillis();
//		
//		try {
//			// use MrgrvPoco and indirectly MrgrvPolicyFinderModule to get set of policies
//			MrgrvPoco poco = new MrgrvPoco(margravePolicy);
//			Set policies = poco.getPolicySet();
//			RequestFactoryIntf factory = new RandomRequestFactory(policies, MAX_RAND_REQ);
//			int num = writeRequests(factory, outputD + DIRS[RAND] + "req/", 1);
//			genNum[RAND] = num;
//		} catch (Exception e) {
//			logger.error("Error instantiating MrgrvPoco", e);
//		}
//		
//		genStop[RAND] = System.currentTimeMillis();
//	}
//	
//	/**
//	 * 
//	 * @pre This should be called after generateXXXRequests()
//	 * for type XXX
//	 */
//	private void generateGreedyRequests(int baseType, int greedyType) {
//		genStart[greedyType] = System.currentTimeMillis();
//		logger.info("Start:" + genStart[greedyType]);
//		// reset coverage
//		RuntimeCoverage.reset("");
//		// get the full set of requests
//		File reqDir = new File(outputD + DIRS[baseType] + "req/");
//		File[] requests = reqDir.listFiles();
//		// create the PDP
//		MrgrvPoco pdp = null;
//		try {
//			pdp = new MrgrvPoco(originalPolicy);
//		} catch (Exception e) {
//			logger.error("Error evalRequests.", e);
//			return;
//		}
//		double polCov = 0;
//		double ruleCov = 0;
//		double condCov = 0;
//		double prevPolCov = 0;
//		double prevRuleCov = 0;
//		double prevCondCov = 0;
//		for (int i = 0; i < requests.length; i++) {
//			try {
//				if (prevPolCov == 1 && prevRuleCov == 1) { // && prevCondCov == 1) {
//					if (RuntimeCoverage.getCondCount() > 0 && prevCondCov == 1) { // handles 0 conditions
//						// met coverage
//						break;
//					}
//				}
//				RuntimeCoverage.setRequestFile(requests[i].getName());
//				RequestCtx request = RequestCtx.getInstance(new FileInputStream(requests[i]));
//				ResponseCtx response = pdp.evaluate(request);
//				polCov = RuntimeCoverage.getPolicyCovPercent();
//				ruleCov = RuntimeCoverage.getRuleCovPercent();
//				condCov = RuntimeCoverage.getCondCovPercent();
//				if (prevPolCov < polCov
//						|| prevRuleCov < ruleCov
//						|| prevCondCov < condCov) {
//					// add to greedy set
//					String reqF = outputD + DIRS[greedyType] + "req/" + requests[i].getName();
//					writeRequest(request, reqF);
//				}
//				prevPolCov = polCov;
//				prevRuleCov = ruleCov;
//				prevCondCov = condCov;
//			} catch (Exception e) {
//				logger.error("Error evaluating " + requests[i].getPath(), e);
//			}
//		}	
//		genNum[greedyType] = (new File(outputD + DIRS[greedyType] + "req/")).listFiles().length;
//		genStop[greedyType] = System.currentTimeMillis();
//		logger.info("Stop:" + genStop[greedyType]);
//	}
//	
//	private int writeRequests(RequestFactoryIntf factory, String path, int i) {
//		while (factory.hasNext() && i < MAX_CIRG_REQ) {
//			try {
//				RequestCtx req = factory.nextRequest();
//				String reqF = path + (i++) + "-req.xml";
//				writeRequest(req, reqF);
//			} catch (Exception e) {
//				logger.error("Error on request " + path + i, e);
//			}
//		}
//		return (new File(path)).listFiles().length;
//	}
//	
//	private void writeRequest(RequestCtx req, String file) {
//		if (req == null || file == null) {
//			return;
//		}
//		try {
//			PrintStream p = new PrintStream(new FileOutputStream(file));
//			req.encode(p, new Indenter());
//		} catch (Exception e) {
//			logger.error("Failed to write requests to " + file);
//		}
//	}
//	
//	private void evalRequests(int type) {
//		evalStart[type] = System.currentTimeMillis();
//		
//		String path = outputD + DIRS[type];
//		evalRequests(path);
//		
//		evalStop[type] = System.currentTimeMillis();
//		
//		collectMetrics(type);
//	}
//	
//	private void evalRequests(String path) {
//		RuntimeCoverage.reset(path + "Coverage");
//		File reqDir = new File(path + "req/");
//		File[] requests = reqDir.listFiles();
//		// create the PDP
//		MrgrvPoco pdp = null;
//		try {
//			pdp = new MrgrvPoco(originalPolicy);
//		} catch (Exception e) {
//			logger.error("Error evalRequests.", e);
//			return;
//		}
//		for (int i = 0; i < requests.length; i++) {
//			try {
//				String reqFileName = path + "resp/" + requests[i].getName() + "-resp.xml";
//				RuntimeCoverage.setRequestFile(reqFileName);
//				ResponseCtx response = pdp.evaluate(requests[i].getPath());
//				PrintStream p = new PrintStream(new FileOutputStream(reqFileName));
//				response.encode(p);
//			} catch (Exception e) {
//				logger.error("Error evaluating " + requests[i].getPath(), e);
//			}
//		}	
//		RuntimeCoverage.writeCovInfo();
//	}
//	
//	private void collectMetrics(int type) {
//		policyNum[type] = RuntimeCoverage.getPolicyCount();
//		ruleNum[type] = RuntimeCoverage.getRuleCount();
//		condNum[type] = RuntimeCoverage.getCondCount();
// 		policyCov[type] = RuntimeCoverage.getPolicyCovPercent();
//		ruleCov[type] = RuntimeCoverage.getRuleCovPercent();
//		condCov[type] = RuntimeCoverage.getCondCovPercent();
//	}
//	
//	private void outputFile(String file) {
//		PrintWriter p = null;
//		try {
//			File outFile = new File(file);
//			if (!outFile.exists()) {
//				if (!outFile.createNewFile()) {
//					logger.error("Failed to create " + outFile);
//					return;
//				}
//			}
//			p = new PrintWriter(new BufferedWriter(new FileWriter(outFile)));
//			StringBuffer lineBuff = new StringBuffer();
//			// header
//			for (int i = 0; i < HEADERS.length; i++) {
//				lineBuff.append(HEADERS[i]);
//				lineBuff.append(",");
//			}
//			lineBuff.deleteCharAt(lineBuff.length() - 1); // last comma
//			p.println(lineBuff.toString());
//			
//			// generation time
//			outputDiffLine("Generation time (ms)", genStart, genStop, p);
//			
//			// number generated
//			outputLine("# Requests generated", genNum, p);
//			
//			// eval
//			outputDiffLine("Evaluation time (ms)", evalStart, evalStop, p);
//			
//			// coverage metrics
//			outputLine("# Policies", policyNum, p);
//			outputLine("PolicyCoverage (%)", policyCov, p);
//			outputLine("# Rules", ruleNum, p);
//			outputLine("Rule coverage (%)", ruleCov, p);
//			outputLine("# Conditions", condNum, p);
//			outputLine("Condition coverage (%)", condCov, p);
//			
//		} catch (Exception e) {
//			logger.error("Error writing output file " + outputD, e);
//		} finally {
//			if (p != null) {
//				p.flush();
//				p.close();
//			}
//		}		
//	}
//	
//	private void outputFile() {
//		outputFile(outputD + "output.txt");
//	}
//	
//	private void outputDiffLine(String descrip, long[] start, long[] stop, PrintWriter p) {
//		StringBuffer lineBuff = new StringBuffer(descrip + ",");
//		for (int i = 0; i < TOTAL; i++) {
//			long delta = stop[i] - start[i];
////			if (delta < 0) {
////				delta = 0;
////			}
//			lineBuff.append(Long.toString(delta));
//			lineBuff.append(",");
//		}
//		lineBuff.deleteCharAt(lineBuff.length() - 1); // last comma
//		p.println(lineBuff.toString());
//	}
//	
//	private void outputLine(String descrip, double[] data, PrintWriter p) {
//		StringBuffer lineBuff = new StringBuffer(descrip + ",");
//		for (int i = 0; i < TOTAL; i++) {
//			lineBuff.append(Double.toString(data[i]));
//			lineBuff.append(",");
//		}
//		lineBuff.deleteCharAt(lineBuff.length() - 1); // last comma
//		p.println(lineBuff.toString());
//	}
//	
//	private void outputLine(String descrip, int[] data, PrintWriter p) {
//		StringBuffer lineBuff = new StringBuffer(descrip + ",");
//		for (int i = 0; i < TOTAL; i++) {
//			lineBuff.append(Integer.toString(data[i]));
//			lineBuff.append(",");
//		}
//		lineBuff.deleteCharAt(lineBuff.length() - 1); // last comma
//		p.println(lineBuff.toString());
//	}
//	
//	private static void runSpecial() {
//		RuntimeCoverage.reset("CirgTestCoverage");
//		int total = 0;
//		int greedy = 0;
//		double polCov = 0;
//		double ruleCov = 0;
//		double condCov = 0;
//		double prevPolCov = 0;
//		double prevRuleCov = 0;
//		double prevCondCov = 0;
//		try {
//			MrgrvPoco pdp = new MrgrvPoco("./ncsu/margrave/examples/tutorial/xacml-code/continue/RPSlist.xml");
//			File f = new File("mrgrv-out-temp-continue.txt");
//			RequestFactoryIntf factory = new MrgrvRequestFactory(f);
//			while (factory.hasNext()) {
//				total++;
//				RequestCtx req = factory.nextRequest();
//				pdp.evaluate(req);
//				polCov = RuntimeCoverage.getPolicyCovPercent();
//				ruleCov = RuntimeCoverage.getRuleCovPercent();
//				condCov = RuntimeCoverage.getCondCovPercent();
//				if (prevPolCov < polCov
//						|| prevRuleCov < ruleCov
//						|| prevCondCov < condCov) {
//					// add to greedy set
//					greedy++;
//				}
//				prevPolCov = polCov;
//				prevRuleCov = ruleCov;
//				prevCondCov = condCov;
//				if (total%200 == 0) {
//					logger.info("Evaluated "  + total + " requests.");
//				}
//			}
//		} catch (Exception e) {
//			logger.error("Error evalRequests.", e);
//			return;
//		}
//		logger.info("Number requests " + total);
//		logger.info("Number greedy requests " + greedy);
//		logger.info("Policy " + RuntimeCoverage.getPolicyCovPercent());
//		logger.info("Rule " + RuntimeCoverage.getRuleCovPercent());
//		logger.info("Cond " + RuntimeCoverage.getCondCovPercent());
//	}
//	
//	public static void main(String[] args) {
//		Util.setupLogger(Level.INFO);
////		runSpecial();
//		if (args.length != 2) {
//			logger.error("Usage: java ncsu.CirgExpRunner <policy> <output directory>");
//			System.exit(1);
//		} 
//		logger.info("Starting experiment with policy:" + args[0] + " output:" + args[1]);
//		CirgExpRunner runner = new CirgExpRunner(args[0], args[1]);
//		runner.startExp();
////		runner.generateCirgIterRequests();
//		logger.info("Done");
//	}
}
