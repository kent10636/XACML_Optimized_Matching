/*
 * Created on Apr 11, 2006
 *
 */
package reqGen.ncsu;

import java.io.File;


import org.apache.log4j.Logger;

import reqGen.ncsu.margrave.MrgrvPoco;
import reqGen.ncsu.util.Util;
import reqGen.ncsu.xacml.mutator.Mutator;
import reqGen.ncsu.xacml.mutator.ReqEvaluator;
import reqGen.ncsu.xacml.mutator.ReqGenerator;

/**
 * @author eemartin
 *
 */
public class SigsoftCirgExpDriver {
	
	static Logger logger = Logger.getLogger(SigsoftCirgExpDriver.class);
	
	private String outputRoot;
	
	public SigsoftCirgExpDriver(String outputRoot) {
		this.outputRoot = outputRoot;
	}
	
	public void go(String[] names, String[] policies) {

		for (int i = 0; i < policies.length; i++) {
			try {
				// copy policy to output directory and use it to create mutants
				File policy = copyAndMutate(policies[i], names[i]);

				// generate and evaluate requests
				genAndEvalReq(policy);
			} catch (Exception e) {
				logger.error("Failed on " + names[i], e);
			}
		}
	}
	
	private File copyAndMutate(String policy, String name) throws Exception {
		Mutator mutator = new Mutator(policy, name, true);
		
		// all rule ops
		mutator.mutate("RTT");
		mutator.mutate("RTF");
		mutator.mutate("RCT");
		mutator.mutate("RCF");
		mutator.mutate("CRE");
//		mutator.mutate("RMR");
		
		// all policy ops
		mutator.mutate("PTT");
		mutator.mutate("PTF");
		mutator.mutate("CRC");
//		mutator.mutate("CRO");
//		mutator.mutate("RMP");
		
		// policy set ops
		mutator.mutate("PSTT");
		mutator.mutate("PSTF");
		mutator.mutate("CPC");
//		mutator.mutate("CPO");
//		mutator.mutate("RMPS");
		
		mutator.outputTotals();
		return mutator.getPolicyFile();
	}
	
//	private void makeCirgReq(File policy) throws Exception {
//		ReqGenerator generator = new ReqGenerator();
//		generator.setPolicy(policy);
//		generator.makeCirgReq();
//	}
	
	private void genAndEvalReq(File policy) {
		ReqEvaluator evaluator;
		boolean useCirg = true;

		evaluator = new ReqEvaluator();
		evaluator.generateEvaluate(policy.getParentFile(), policy, !useCirg, outputRoot);
		
		evaluator = new ReqEvaluator();
		evaluator.generateEvaluate(policy.getParentFile(), policy, useCirg, outputRoot);		
	}
	
	public static void main(String[] args) {
		Util.setupLogger();
		if (args.length == 0) {
			logger.info("Running on default subjects.");
			String[] names = {
//					"simple-policy", 
//					"codeA",
//					"conference",
//					"default-2",
//					"mod-fedora",
//					"demo-5",
//					"demo-11",
//					"demo-26",
//					"continue-a",
//					"codeB",
//					"codeC",
//					"codeD",
//					"continue-b",
//					"generated",
//					"obligation",
//					"selector",
					"pluto",
					"continue-a"
			};
			String[] policies = {
//					"../xacml-subjects/margrave/simple/simple-policy.xml", 
//					"../xacml-subjects/margrave/codeA/RPSlist.xml",
//					"../xacml-subjects/conference/conference.xml",
//					"../xacml-subjects/fedora/default-2/RPSlist.xml",
//					"../xacml-subjects/fedora/modifedFedora/MyPolicySet.xml",
//					"../xacml-subjects/fedora/demo-5/demo-5.xml",
//					"../xacml-subjects/fedora/demo-11/demo-11.xml",
//					"../xacml-subjects/fedora/demo-26/demo-26.xml",
//					"../xacml-subjects/margrave/continue-a/RPSlist.xml",
//					"../xacml-subjects/margrave/codeB/RPSlist.xml",
//					"../xacml-subjects/margrave/codeC/RPSlist.xml",
//					"../xacml-subjects/margrave/codeD/RPSlist.xml",
//					"../xacml-subjects/margrave/continue-b/RPSlist.xml",
//					"../xacml-subjects/sun-samples/generated/generated.xml",
//					"../xacml-subjects/sun-samples/obligation/obligation.xml",
//					"../xacml-subjects/sun-samples/selector/selector.xml",
					"../xacml-subjects/archon/pluto.xml",
					"../xacml-subjects/margrave/continue-a/RPSlist.xml"
			};
			SigsoftCirgExpDriver driver = new SigsoftCirgExpDriver("../output");
			driver.go(names, policies);
		} else if (args.length == 2) {
			logger.info("Running on " + args[0]);
			String[] names = {args[0]};
			String[] policies = {args[1]};
			SigsoftCirgExpDriver driver = new SigsoftCirgExpDriver("../output");
			driver.go(names, policies);
		} else {
			
			// special code
			logger.info("Running special...");
			
//			ReqEvaluator evaluator;
//			boolean useCirg = true;
			File policy = new File("../fse06-output-continue/policies/continue-a/copy.xml");				
//			evaluator = new ReqEvaluator();
//			evaluator.generateEvaluate(policy.getParentFile(), policy, useCirg);
			
			SigsoftCirgExpDriver driver = new SigsoftCirgExpDriver("../output");
			driver.genAndEvalReq(policy);
			
			logger.error("Usage:");
			logger.error("No arguments: runs default.");
			logger.error("Two arguments: <name> <policy>");
			logger.error("Exiting...");
			System.exit(1);			
		}
		logger.info("Done.");
	}
}
