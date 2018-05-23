/*
 * Created on Mar 26, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package reqGen.ncsu.margrave;

import java.io.InputStream;

/**
 * @author eemartin
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class MrgrvErrorStreamGobbler extends StreamGobbler {

	public MrgrvErrorStreamGobbler(InputStream is) {
		super(is);
		this.setName("ncsu.margrave.MrgrvErrorStreamGobbler");
		this.start();
	}
	
	protected boolean processLine(String line) {
		if (line == null) {
			return false;
		}
		if (line.length() == 0) {
			return false;
		}
		MrgrvExec.logger.error(line);
		MrgrvExec.logger.error("Error occured...destroying process.");
		return true;
	}
}
