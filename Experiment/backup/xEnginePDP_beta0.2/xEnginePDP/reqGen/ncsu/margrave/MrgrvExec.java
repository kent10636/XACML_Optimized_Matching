/*
 * Created on Sep 28, 2005
 *
 */
package reqGen.ncsu.margrave;

import java.io.File;


import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import reqGen.com.sun.xacml.Indenter;
import reqGen.com.sun.xacml.ctx.RequestCtx;
import reqGen.ncsu.util.Util;
import reqGen.ncsu.xacml.RequestFactoryIntf;
import reqGen.ncsu.xacml.poco.RuntimeCoverage;


/**
 * @author eemartin
 */
public class MrgrvExec {

	static final int TIMEOUT = 5*60*1000;
	static Logger logger = Logger.getLogger(MrgrvExec.class);
	
	static Process proc;
	
	static Thread timer;
	static StreamGobbler errorGobbler;
	static StreamGobbler outputGobbler;
	
	static RequestFactoryIntf factory;
	static boolean destroyed;
	
	private MrgrvExec() {}
	
	public static void execScriptSmart(String script, File requestOutput) {
		try {
			Runtime rt = Runtime.getRuntime();	
			logger.debug("Executing " + script);
			proc = rt.exec(script);
			errorGobbler = new MrgrvErrorStreamGobbler(proc.getErrorStream());
			outputGobbler = new MrgrvSmartStreamGobbler(proc.getInputStream(), requestOutput);
			while (errorGobbler.isAlive() && outputGobbler.isAlive()) {
				Thread.sleep(500);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} 
	}
	
	public static RequestFactoryIntf execScript(String script) {
		try {
			destroyed = false;
			Runtime rt = Runtime.getRuntime();	
			logger.debug("Executing " + script);
			proc = rt.exec(script);
			errorGobbler = new MrgrvErrorStreamGobbler(proc.getErrorStream());
			outputGobbler = new MrgrvStreamGobbler(proc.getInputStream());
			errorGobbler.start();
			outputGobbler.start();		
//			startTimer();
			logger.info("return code: " + proc.waitFor());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			destroyProcess();
		}
		return factory;
	}
	
	private static void startTimer() {
		timer = new Thread() {
			public void run() {
				try {
					long startTime = System.currentTimeMillis();
					while (startTime + TIMEOUT > System.currentTimeMillis()) {
						if (destroyed) {
							return;
						}
						sleep(100);
					}
					if (!destroyed) {
						logger.error("Timer expired...destroying process.");
						destroyProcess();
					} else {
						logger.info("Timer expired...process already destroyed.");
					}
				} catch (InterruptedException e) {
					logger.error("Error executing thread.", e);
				}
			}
		};
		timer.start();
	}
	
	public static synchronized void destroyProcess() {
		if (outputGobbler != null && outputGobbler instanceof MrgrvStreamGobbler) {
			((MrgrvStreamGobbler) outputGobbler).close();
			factory = ((MrgrvStreamGobbler) outputGobbler).getRequestFactory();
		}
		if (destroyed) {
			return;
		} else {
			proc.destroy();
//			errorGobbler.stop();
//			outputGobbler.stop();;
//			timer.stop();
			destroyed = true;
		}
	}
	
//	public static RequestFactoryIntf execScript(String script) {
//		RequestFactoryIntf factory = null;
//		try {
//			Runtime rt = Runtime.getRuntime();	
//			logger.debug("Executing " + script);
//			proc = rt.exec(script);
//			errorGobbler = new MrgrvErrorStreamGobbler(proc.getErrorStream());
//			outputGobbler = new MrgrvStreamGobbler(proc.getInputStream());
//			errorGobbler.start();
//			outputGobbler.start();
//		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
//		} finally {
//			outputGobbler.close();
//			if (outputGobbler != null) {
//				factory = outputGobbler.getRequestFactory();
//			}
//			errorGobbler = null;
//			outputGobbler = null;
//		}
//		return factory;
//	}
	
	/**
	 * A test of sorts
	 * @param args
	 */
	public static void main(String[] args) {
		Util.setupLogger(Level.DEBUG);
		RuntimeCoverage.coverageFileName = "MrgrvExecTest";
		String script = null;
		String policy = null;
		if (args.length != 2) {
			logger.warn("Usage: java MgvExec <script file> <policy file>");
			script = "./scripts/mrgv-script-constrained.scm";
			policy = "./ncsu/margrave/examples/tutorial/xacml-code/xacmlA/2/RPSlist.xml";
			logger.info("Using default script: " + script);
			logger.info("Using default policy: " + policy);
		} else {
			script = args[0];
			policy = args[1];
		}

		// create the PDP
//		MrgrvPoco simplePDP = null;
//		try {
//			simplePDP = new MrgrvPoco(policy);
//		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
//			System.exit(1);
//		}
		// script performs change impact analysis and creates a request factory
		RequestFactoryIntf factory = execScript(script);
		if (factory == null) {
			return;
		}
		// to avoid exceptions
//		RuntimeCoverage.writeCovInfo();
		int i = 20;
		while (factory.hasNext()) {
			try {
				RequestCtx req = factory.nextRequest();
				if (req == null) {
					break;
				}
				RuntimeCoverage.setRequestFile(Integer.toString(i++));
		        // evaluate the request
//		        ResponseCtx response = simplePDP.evaluate(req);
		        req.encode(System.out, new Indenter());
//		        response.encode(System.out, new Indenter());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		RuntimeCoverage.outputCoverageStatistics(System.out);
	}
}