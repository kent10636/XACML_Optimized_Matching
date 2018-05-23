/**
 * @author Fei Chen
 * feichen@cse.msu.edu
 * Computer Science and Engineering
 * Michigan State University
 * 12/03/2008
 */

package xEngineVerifier;
 
import ncsu.util.Util;

import com.sun.xacml.ParsingException;
import commonImpelmentation.ConfigInfo;


/**
 * Test the conversion program by calling MainClass with "xacml" file as
 * parameter
 * 
 * @author JeeHyun Hwang 02/09/2007
 * 
 */

public class TestProgram {
	
	public static long conversionTime = 0;
	public static long memorySize = 0;
	
	public synchronized static void main(String[] args) throws Throwable {

		Util.setupLogger();
		
		ConfigInfo c = new ConfigInfo();

		int policyid=0;
		int requestMode=1;
		String option = "xacmDirParsing";
		// String option = "fwrFileParsing";
		String Outoption = "writeFile";
		// String Outoption = "noWrite";
		
// setup arguments..
		
		if (args.length == 4) {
			policyid = Integer.parseInt(args[0]);
			requestMode = Integer.parseInt(args[1]);
			option = args[2];
			Outoption = args[3];
		}

		if (requestMode == 1) {// single-valued requests

			xxAclQuery.main(new String[] { 
					c.fwr_policy[policyid],
					c.logDir + c.policy_reference[policyid],
					c.logDir + c.SoDreqDir[policyid],
					c.SOD_fwr_converted_query[policyid],
					c.SOD_fwr_response[policyid], option, Outoption,
					c.logDir + c.xacml_policy[policyid] });
			conversionTime = (long)xxAclQuery.conversion_time;
			memorySize = xxAclQuery.memory_size;

		} else if (requestMode == 2) {// multi-valued requests
			xxAclQuery.main(new String[] { 
					c.fwr_policy[policyid],
					c.logDir + c.policy_reference[policyid],
					c.logDir + c.nonSoDreqDir[policyid],
					c.nonSOD_fwr_converted_query[policyid],
					c.nonSOD_fwr_response[policyid], option, Outoption,
					c.logDir + c.xacml_policy[policyid] });
		}
	}
}
