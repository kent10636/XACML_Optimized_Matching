package reqGen.ncsu.margrave;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import reqGen.com.sun.xacml.ctx.RequestCtx;
import reqGen.ncsu.util.Util;
import reqGen.ncsu.xacml.MrgrvRequestFactory;



public class MrgrvSmartStreamGobbler extends StreamGobbler {

	private MrgrvRequestFactory factory;
	private File requestDir;
	private static final int MAX_REQ = 10;
	private int numReq;
	
	public MrgrvSmartStreamGobbler(InputStream is, File requestOutputDir) {
		super(is);
		setName("ncsu.margrave.MrgrvSmartStreamGobbler");
		factory = new MrgrvRequestFactory();
		requestDir = requestOutputDir;
		numReq = 0;
		this.start();
	}
	
	protected boolean processLine(String line) {	
		try {	
			// terminating condition to stop long lists of counter-examples
			if (numReq >= MAX_REQ) {
				return true;
			}		
			
			// write request
			RequestCtx request = factory.processLine(line);
			if (request != null) {
				String[] rs = requestDir.list();
				int r = 1;
				if (rs != null) {
					r = rs.length + 1;
				}
				File reqF = new File(requestDir + "/" + (r) + ".xml");
//				request.encode(System.out);
				Util.createFile(reqF, MrgrvExec.logger);
				request.encode(new FileOutputStream(reqF));
				numReq++;
			}
		} catch (Exception e) {
			MrgrvExec.logger.error("Failed to write request", e);
		}		
		return false;
	}

}
