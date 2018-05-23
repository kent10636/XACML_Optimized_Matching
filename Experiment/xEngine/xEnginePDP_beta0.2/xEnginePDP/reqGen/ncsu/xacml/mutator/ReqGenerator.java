/*
 * Created on Mar 27, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package reqGen.ncsu.xacml.mutator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashSet;


import org.apache.log4j.Logger;

import reqGen.com.sun.xacml.Indenter;
import reqGen.com.sun.xacml.ctx.RequestCtx;
import reqGen.com.sun.xacml.ctx.ResponseCtx;
import reqGen.ncsu.margrave.MrgrvCIScriptGenerator;
import reqGen.ncsu.margrave.MrgrvExec;
import reqGen.ncsu.margrave.MrgrvPolicySplitter;
import reqGen.ncsu.util.Util;
import reqGen.ncsu.xacml.RandomRequestFactory;
import reqGen.ncsu.xacml.RequestFactoryIntf;
import reqGen.ncsu.xacml.poco.RuntimeCoverage;


/**
 * @author eemartin
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ReqGenerator {

	private static Logger logger = Logger.getLogger(ReqGenerator.class);
	
	private static final String denyAll = "../xacml-subjects/deny/RPSlist.xml";
	private static final String permitAll = "../xacml-subjects/permit/RPSlist.xml";

	private File policy;
	
	public ReqGenerator() {}
	
	public ReqGenerator(File policy) {
		setPolicy(policy);
	}
	
	public void setPolicy(File policy) {
		this.policy = policy;
	}
	
//	public File[] getRequests() {
//		return getRequestDirectory().listFiles();
//	}
//	
//	public File getRequestDirectory() {
//		return new File(policy.getParent() + "/requests/");
//	}	
	
	public File[] makeCirgReq(String op, String type) {
		logger.info("Generating requests for " + policy.getName());
		File copy = copyPolicy();
		if (copy == null) {
			logger.error("Failed to copy " + policy);
			return new File[0];
		}
		return makeCirgReq(copy, op, type);		
	}
	
	private File[] makeCirgReq(File copy, String op, String type) {
		File[] versions = null;
		File dir = new File(copy.getParent() + "/versions/" + type);
		
		try {
			Mutator mutator = new Mutator(copy.getPath(), policy.getName(), false/*mutate*/);			
			mutator.createVersions(op, dir);
			versions = dir.listFiles();
		} catch (Exception e) {
			logger.error("Failed to create versions for " + copy);
		}
		
		if (versions == null) {
			logger.error("Failed to create versions of " + copy);
			return new File[0];
		}
		
		File reqDir = new File(policy.getParent() + "/requests/" + type);
		for (int i = 0; i < versions.length; i++) {
			// generate and execute script
			logger.info("Executing script on " + copy + "..." + i);

			if ("oneToEmpty".equals(op)) {
				if (isDecisionDeny(versions[i].getPath())) {
					// decision is deny
					MrgrvCIScriptGenerator.generateScript(permitAll, versions[i].getPath());
				} else {
					// decision is permit
					MrgrvCIScriptGenerator.generateScript(denyAll, versions[i].getPath());
				}
				MrgrvExec.execScriptSmart(MrgrvCIScriptGenerator.getScriptName(), reqDir);
			} else {
				MrgrvCIScriptGenerator.generateScript(copy.getPath(), versions[i].getPath());
				MrgrvExec.execScriptSmart(MrgrvCIScriptGenerator.getScriptName(), reqDir);
			}
		}
		File[] requests = reqDir.listFiles();
		return (requests != null) ? requests : new File[0];
	}
	
	private boolean isDecisionDeny(String p) {
		try {
			Mutator m = new Mutator(p, "test", false);
			return m.isDecisionDeny();
		} catch (Exception e){
			logger.error("Could not determine decision" + p, e);
		}
		return false;
	}
	
	private File copyPolicy() {
		try {
			File copy = new File(policy.getParent() + "/copy.xml");
			Mutator mutator = new Mutator(policy.getPath(), policy.getName(), false/*mutate*/);
			mutator.copyRmAllConditions(copy);
			logger.info("Copy at " + copy);
			return copy;
		} catch (Exception e) {
			logger.error("Failed to copy policy " + policy, e);
			return null;
		}
	}
	
	public static void main(String[] args) {
		Util.setupLogger();
		ReqGenerator generator = new ReqGenerator();
		File[] dirs = new File("../ase06-output/policies/").listFiles();
		for (int d = 0; d < dirs.length; d++) {
			File[] files = dirs[d].listFiles();
			if (files == null) {
				continue;
			}
			for (int f = 0; f < files.length; f++) {
				if (files[f].isDirectory()) {
					continue;
				} else {
					generator.setPolicy(files[f]);
				}
			}
		}
		logger.info("Done");
	}
}
