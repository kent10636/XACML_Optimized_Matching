/*
 * Created on Oct 3, 2005
 */
package reqGen.ncsu.margrave;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author eemartin
 */
public abstract class StreamGobbler extends Thread {
	
	private InputStream is;
	
	StreamGobbler(InputStream is) {
		super("policy.margrave.StreamGobbler");
		this.is = is;
	}
	
	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;

			do {			
				line = br.readLine();
//				MrgrvExec.logger.info(line);
				if (processLine(line)) {
					MrgrvExec.destroyProcess();
					return;
				}
				
			} while (line != null);
			
		} catch (Exception ioe) {
			MrgrvExec.logger.error(getName() + ": " + ioe.getMessage(), ioe);
		}
	}
	
	protected abstract boolean processLine(String line);
}
