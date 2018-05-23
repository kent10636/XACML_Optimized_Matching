package reqGen.ncsu.xacml;

import java.io.File;


import org.apache.log4j.Logger;

import reqGen.ncsu.ExperimentRunner;
import reqGen.ncsu.util.Util;
import reqGen.ncsu.xacml.mutator.Mutator;

public class CopyAndMutate {
	
	private static Logger logger = Logger.getLogger(CopyAndMutate.class);

	public static void copyAndMutate(ExperimentRunner runner) {
		for (int i = 0; i < runner.policyList.length; i++) {
			try {
				copyAndMutate(runner.policyList[i], runner.policyNameList[i], runner.outputDir);
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}
	
	public static File copyAndMutate(String policy, String name, String outputDir) throws Exception {
		logger.info("Mutating " + policy);
		Mutator mutator = new Mutator(policy, name, true, outputDir);
		
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
	
//	/**
//	 * @param args
//	 */
	public static void main(String[] args) {
		Util.setupLogger();
		if (args.length == 0) {
			logger.info("Usage: java CopyAndMutate <name1> <policy1> <name2> <policy2> ...");
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
//					"pluto",
////					"continue",
//					"continue-a",
////					"codeB",
////					"codeC",
////					"codeD",
//					"continue-b",
//					"generated",
//					"obligation",
//					"selector",
//					"default-2",
//					"demo-5",
//					"demo-11",
//					"demo-26",					
					"modifedFedora"	
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
//					"../xacml-subjects/archon/pluto.xml",
////					"../xacml-subjects/margrave/continue/continue.xml",
//					"../xacml-subjects/margrave/continue-a/RPSlist.xml",
////					"../xacml-subjects/margrave/codeB/RPSlist.xml",
////					"../xacml-subjects/margrave/codeC/RPSlist.xml",
////					"../xacml-subjects/margrave/codeD/RPSlist.xml",
//					"../xacml-subjects/margrave/continue-b/RPSlist.xml",
//					"../xacml-subjects/sun-samples/generated/generated.xml",
//					"../xacml-subjects/sun-samples/obligation/obligation.xml",
//					"../xacml-subjects/sun-samples/selector/selector.xml",
//					"../xacml-subjects/fedora/default-2/RPSlist.xml",
//					"../xacml-subjects/fedora/demo-5/demo-5.xml",
//					"../xacml-subjects/fedora/demo-11/demo-11.xml",
//					"../xacml-subjects/fedora/demo-26/demo-26.xml",
					"../xacml-subjects/fedora/modifedFedora/MyPolicySet.xml"
										
					
			};
			
			
			String outputDir = "../xacml-subjects/sun-samples/";
			
			for (int i = 0; i < names.length; i++) {
				try {
					copyAndMutate(policies[i], names[i], outputDir);
				} catch (Exception e) {
					logger.error("Error on " + names[i], e);
				}
			}
		} else {
			for (int i = 1; i < args.length; i++) {
				try {
					copyAndMutate(args[i], args[i-1], args[i-1]);
				} catch (Exception e) {
					logger.error("Error on " + args[i-1], e);
				}
			}
		}
		logger.info("Done");
	}
}
