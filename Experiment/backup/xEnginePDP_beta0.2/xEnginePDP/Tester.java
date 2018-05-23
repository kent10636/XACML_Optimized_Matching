/**
 * @author Fei Chen
 * feichen@cse.msu.edu
 * Computer Science and Engineering
 * Michigan State University
 * 12/03/2008
 */

import ncsu.util.Util;
import xacml_request_verifier.RequestVerifierOfTwoPolicies;
import com.sun.xacml.ParsingException;
import com.sun.xacml.finder.PolicyFinder;
import commonImpelmentation.*;
 
public class Tester{
	public synchronized static void main(String[] args) throws Throwable{
		request.generate_request(null);  //request.java中的generate_request()方法生成待匹配的策略，需要输入生成数量

		Util.setupLogger();
		
		String Outoption = "writeFile";	// Write file option is to compare the results between xEngine and Sun's PDP
		String requestOption = "1"; // 1, Single-Valued requests; 2, multi-valued requests
		String policyid = "0"; // Define the index of a xacml policy. 
		                            // Check ConfigInfo.java for the detailed information. 
		int mode = 1;// 0, the PDD approach; 1, the forwarding table approach
		
		// Normalize an XACML policy
		InitializeConversion();
		xEngineConverter.TestProgram.main(new String[] {policyid});
		
		// Process a request
		InitializexEngine(mode);
		xEngineVerifier.TestProgram.main(new String[] {policyid, requestOption, "xacmDirParsing", Outoption});
		
		// Process the request using Sun's PDP
		xacml_request_verifier.TestProgram.main(new String[] {policyid, requestOption, Outoption});
		
		// Compare two results
		compareResultFWRvsXACML.TestProgram.main(new String[] {policyid, requestOption});		
	}

	
	private static void InitializeConversion() {
		// TODO Auto-generated method stub
		xEngineConverter.TestProgram.conversionTime = 0;
		xEngineConverter.conversionTree.initialize();
	}
	
	private static void InitializexEngine(int mode) {
		// TODO Auto-generated method stub
		xEngineVerifier.xxAclQuery.initialize(mode);
		xEngineVerifier.xAclFunc.initialize(mode);
		xEngineVerifier.xFirFileParser.initialize(mode);
	}
}