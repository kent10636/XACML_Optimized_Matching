package reqGen.ncsu;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;


import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import reqGen.ncsu.util.Util;
import reqGen.ncsu.xacml.mutator.ReqEvaluator;
import reqGen.ncsu.xacml.CopyAndMutate;

public class ExperimentRunner {

	static Logger logger = Logger.getLogger(ExperimentRunner.class);
	
	public static final String POLICY_LIST = "PolicyList";
	public static final String POLICY_NAME_LIST = "PolicyNameList";
	public static final String REQUEST_DIR_LIST = "RequestDirList";
	public static final String OUTPUT_DIR = "OutputDir";
	public static final String RANDOM_NUMBER = "RandomNumber";
	public static final String RANDOM_DIR = "RandomDir";
	public static final String TARGEN_NUMBER = "TargenNumber";
	public static final String TARGEN_DIR = "TargenDir";
	
	public String[] policyList;
	public String[] policyNameList;
	public String[] requestDirList;
	public String outputDir;
	public int randomNumber;
	public String randomDir;
	public int targenNumber;
	public String targenDir;
	
	public ExperimentRunner(Properties p) {
		policyList = p.getProperty(POLICY_LIST).split(",");
		policyNameList = p.getProperty(POLICY_NAME_LIST).split(",");
		requestDirList = p.getProperty(REQUEST_DIR_LIST).split(",");
		outputDir = p.getProperty(OUTPUT_DIR);
		randomNumber = Integer.parseInt(p.getProperty(RANDOM_NUMBER));
		randomDir = p.getProperty(RANDOM_DIR);
		targenNumber = Integer.parseInt(p.getProperty(TARGEN_NUMBER));
		targenDir = p.getProperty(TARGEN_DIR);
		if (policyList.length != policyNameList.length) {
			throw new IllegalArgumentException("Number of elements in PolicyList must equal the number of elements in the PolicyNameList. Check the config file.");
		}
		Util.cleanDir(new File(outputDir), logger);
	}
	
	public void run() {
		logger.info("Executing ExperimentRunner...");
		
		// first copy policy over and mutate
		CopyAndMutate.copyAndMutate(this);
		
		// generate random
		if (randomNumber > 0) {
			
		}
		
		// generate targen
		if (targenNumber != 0) {
			
		}
		
		// now begin executing requests against policies and mutants
		for (int p = 0; p < policyList.length; p++) {
			for (int r = 0; r < requestDirList.length; r++) {
				String policyDir = policyList[p];
				String policyName = policyNameList[p];
				String requestDir = requestDirList[r];
				ReqEvaluator.evaluate(policyDir, policyName, requestDir, outputDir);
			}
		}
		
		logger.info("ExperimentRunner complete.");
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Util.setupLogger(Level.INFO);
		if (args.length != 1) {
			logger.info("Usage: ExperimentRunner <config-file>");
			return;
		} 
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(args[0]));
		} catch (Exception e) {
			logger.error("Unable to load custom config properties.", e);
		}
		
		ExperimentRunner runner = new ExperimentRunner(properties);
		runner.run();
	}

}
