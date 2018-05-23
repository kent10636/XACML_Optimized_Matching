/**
 * @author Fei Chen
 * feichen@cse.msu.edu
 * Computer Science and Engineering
 * Michigan State University
 * 12/03/2008
 */

package xacml_request_verifier;

import xEngineConverter.conversionTree;
import ncsu.util.Util;


import com.sun.xacml.ParsingException;
import com.sun.xacml.finder.PolicyFinder;
import commonImpelmentation.ConfigInfo;

public class TestProgram{
	public synchronized static void main(String[] args) throws Throwable{
		
		Util.setupLogger();

		ConfigInfo c = new ConfigInfo();
		
		int selected =4;
		int mode =1;
		String Outoption = "writeFile";
		
		if (args.length == 3) {
			selected = Integer.parseInt(args[0]);
			mode = Integer.parseInt(args[1]);
			Outoption = args[2].trim();
		}	 
		
		if (mode == 1){

			RequestVerifierOfTwoPolicies.main(new String[] { c.logDir + c.xacml_policy[selected], c.logDir+c.SoDreqDir [selected], c.SOD_xacml_response[selected], Outoption});	
			
			
		} else if (mode ==2){

			RequestVerifierOfTwoPolicies.main(new String[] { c.logDir + c.xacml_policy[selected], c.logDir+c.nonSoDreqDir [selected], c.nonSOD_xacml_response[selected], Outoption});	

		}
		
	}

}
