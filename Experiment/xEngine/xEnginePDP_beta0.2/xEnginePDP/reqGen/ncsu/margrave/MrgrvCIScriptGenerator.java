/*
 * Created on Jan 26, 2006
 *
 */
package reqGen.ncsu.margrave;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;


import org.apache.log4j.Logger;

import reqGen.ncsu.util.Util;

/**
 * @author eemartin
 *
 */
public class MrgrvCIScriptGenerator {
	
	static Logger logger = Logger.getLogger(MrgrvCIScriptGenerator.class);
	
	private MrgrvCIScriptGenerator() {}
	
	public static String getScriptName() {
		return "./scripts/mrgv-script.scm";
	}
	
	public static boolean generateScript(String firstPolicy, String secondPolicy) {
		boolean success = true;
		String script = getScriptName();
		BufferedWriter out = null;
		File outputF;
		try {
			outputF = new File(script);
			if (!outputF.exists()) {
				if (!outputF.createNewFile()) {
					MrgrvExec.logger.error("Could not create file " + script);
				}
			}
			out = new BufferedWriter(new FileWriter(outputF));
			out.write("#! /bin/sh"); out.newLine();
			out.write("#|"); out.newLine();
//			out.write("PATH=$PATH:/Applications/PLT\\ Scheme\\ v299.200/bin"); out.newLine();
			out.write("exec mzscheme -qr \"$0\" ${1+\"$@\"}"); out.newLine();
			out.write("|#"); out.newLine();
			out.write("(require \"../../../../../usr/margrave/analysis/margrave.scm\")"); out.newLine();
//			out.write("(require \"../ncsu/margrave/analysis/margrave.scm\")"); out.newLine();
			out.write("(let-xacml-policies [ [firstPol \"" + firstPolicy + "\"]"); out.newLine();
			out.write("                      [secondPol \"" + secondPolicy + "\"] ]"); out.newLine();                    
			out.write("                    (let [ [comp1to2 (compare-policies firstPol secondPol)] ]"); out.newLine();
			out.write("                    (print-comparison-changes comp1to2)))"); out.newLine();
		} catch (Exception e) {
			logger.error("Error generating change-impact script", e);
			success = false;
		} finally {
			if (out != null) {
				try {
					out.flush();
					out.close();
				} catch (Exception e) {
					logger.error("Failed to close out stream.", e);
				}
			}
		}
		return success;
	}

	public static void main(String[] args) {
		Util.setupLogger();
		String first = "ncsu/margrave/examples/tutorial/xacml-code/empty/RPSlist.xml";
		String second = "ncsu/margrave/examples/tutorial/xacml-code/simple/simple-policy.xml";
		if (generateScript(first, second)) {
			logger.info("Script generated successfully");
		} else {
			logger.info("Script generation failed");
		}
	}
}
