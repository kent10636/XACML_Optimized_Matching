package reqGen.ncsu.margrave;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Set;


import org.apache.log4j.Logger;

import reqGen.com.sun.xacml.PolicySet;
import reqGen.com.sun.xacml.Target;
import reqGen.com.sun.xacml.combine.PolicyCombiningAlgorithm;
import reqGen.ncsu.util.Util;


/**
 * Converts a given policy or list of policies into the RPSList format expected by Margrave.
 * It also ensures unique identifiers are used.
 * @author eemartin
 *
 */
public class MrgrvConverter {
	static Logger logger = Logger.getLogger(MrgrvConverter.class);
	
	private String[] policies;
	private String combiningAlg;
	private String description;
	private String outputDirectory;
	private String outputRoot;
	
	public MrgrvConverter(String[] policies, String combiningAlg) {
		this(policies, combiningAlg, null);
	}
	
	public MrgrvConverter(String[] policies, String combiningAlg, String description) {
		this.policies = policies;
		this.combiningAlg = combiningAlg;
		this.description = description;
		outputRoot = "../output";
	}
	
	public void setOutputDir(String dir) {
		outputDirectory = dir;
	}
	
	public void convert() {
		logger.info("Using policy combining algorithm: " + combiningAlg);
		
		if (description == null) {
			// use a counter instead
			int filecount = 0;
			String[] polDirList = new File(Util.policiesDir(outputRoot)).list();
			if (polDirList == null) {
				filecount = 0;
			} else {
				filecount = polDirList.length;
			}
			description = Integer.toString(filecount);
		}
		outputDirectory = Util.policiesDir(outputRoot) + "/" + description + "/";
		File dir = new File(outputDirectory);
		if (dir.exists()) {
			logger.error("Attempting to write to existing directory = " + outputDirectory);
			logger.error("Processing halted.");
			return;
		}
		if (!dir.mkdirs()) {
			logger.error("Failed to create directory = " + outputDirectory);
			logger.error("Processing halted.");
			return;
		}
		
		if (!copy()) {
			logger.error("Failed to copy files over.");
			logger.error("Processing halted.");
			return;
		}
		
		if (!writeRPSList()) {
			logger.error("Failed to write RPSList.");
			logger.error("Processing halted.");
			return;
		}
		
		if (!checkUniqueIds()) {
			logger.error("Failed while checking unique identifiers.");
			logger.error("Processing halted.");
			return;
		}
		
		logger.info("Processing complete.");
	}
	
	private boolean copy() {
		logger.info("Copying files...");
		for (int i = 0; i < policies.length; i++) {
			File src = new File(policies[i]);
			if (!src.exists()) {
				return false;
			}
			if (!src.isFile()) {
				return false;
			}
			File dst = new File(outputDirectory + src.getName());
			try {
				copy(src, dst);
			} catch (IOException ioe) {
				logger.error("Error copying from '" + src + "' to '" + dst + "'", ioe);
				return false;
			}
			logger.info("Copied from '" + src + "' to '" + dst + "'");
		}		
		return true;
	}
	
	private void copy(File src, File dst) throws IOException {
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dst);
		
		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}
	
	private boolean writeRPSList() {
		logger.info("Creating policy set...");
//		URI id;
//		PolicyCombiningAlgorithm combiningAlg;
//        String description; 
//        Target target; 
//        List policies;
//        String defaultVersion; 
//        Set obligations;
//		PolicySet policySet = new PolicySet(id, combiningAlg,  description, target, policies, defaultVersion, obligations);
		
		return false;
	}
	
	private boolean checkUniqueIds() {
		logger.info("Checking for unique identifiers...");
		return false;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Util.setupLogger();	
		if (args.length == 0) {
			logger.info("Usage: <policy list> <combining alg>");
		}
		// clean directory
		Util.cleanDir(new File("../output"), logger);
		
		// loading arguments
		String[] policies = new String[args.length - 1];
		for (int i = 0; i < args.length - 1; i++) {
			policies[i] = args[i];
		}
		String combiningAlg = args[args.length - 1];
		
		// writing RPSList
		MrgrvConverter converter = new MrgrvConverter(policies, combiningAlg);
		converter.convert();
		
		logger.info("Done");
	}
}
