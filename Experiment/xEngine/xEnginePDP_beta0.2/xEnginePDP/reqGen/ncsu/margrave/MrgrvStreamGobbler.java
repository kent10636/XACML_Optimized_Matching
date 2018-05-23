/*
 * Created on Oct 3, 2005
 */
package reqGen.ncsu.margrave;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;

import reqGen.ncsu.xacml.MrgrvRequestFactory;
import reqGen.ncsu.xacml.RequestFactoryIntf;

/**
 * @author eemartin
 */
class MrgrvStreamGobbler extends StreamGobbler {

	private boolean lastLine;
	private File outputF;
	private BufferedWriter out;
	
	public MrgrvStreamGobbler(InputStream is) {
		this(is, "mrgrv-output-temp.txt");
	}
	
	public MrgrvStreamGobbler(InputStream is, String file) {
		super(is);
		lastLine = false;
		this.setName("ncsu.margrave.MrgrvStreamGobbler");
		try {
			outputF = new File(file);
			if (!outputF.exists()) {
				if (!outputF.createNewFile()) {
					MrgrvExec.logger.error("Could not create file " + file);
				}
			}
			out = new BufferedWriter(new FileWriter(outputF));
		} catch (Exception e) {
			MrgrvExec.logger.error("Error instantiating MrgrvStreamGobbler", e);
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.ncsu.policy.margrave.StreamGobbler#processLine(java.lang.String)
	 */
	/**
	 * processLine of Margrave command line output
	 */
	protected boolean processLine(String line) {
//		parser.parseLine(line);
//		MrgrvExec.request = parser.getRequest();
//		if (MrgrvExec.request != null) {
//			MrgrvExec.destroyProcess();
//		}
		try {
			out.write(line);
			out.newLine();
			if (lastLine) {
				if (line.trim().length() > 0) {
					out.write("}");
					out.newLine();
					out.flush();
					MrgrvExec.logger.info("Found one counter-example...destroying process.");
					return true;
				}
			}
			if (line.indexOf("{") != -1) {
				lastLine = true;
			}
		} catch (Exception e) {
			MrgrvExec.logger.error("Error writing line " + line + " to " + outputF, e);
		}
		return false;
	}
	
	public void close() {
		try {
			out.flush();
			out.close();
		} catch (Exception e) {
			MrgrvExec.logger.error("Error closing output stream.");
			MrgrvExec.logger.debug(e);
		}
	}
	
	RequestFactoryIntf getRequestFactory() {	
		MrgrvRequestFactory factory = new MrgrvRequestFactory(outputF);
		if (factory.isInitialized()) {
			return factory;
		} else {
			return null;
		}
	}
}
