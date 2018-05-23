/**
 * @author Fei Chen
 * feichen@cse.msu.edu
 * Computer Science and Engineering
 * Michigan State University
 * 12/03/2008
 */

package xEngineConverter;

import ncsu.util.Util;
import com.sun.xacml.ParsingException;
import com.sun.xacml.finder.PolicyFinder;
import commonImpelmentation.*;

public class TestProgram{


	public static long conversionTime = 0;
	
	public synchronized static void main(String[] args) throws Throwable{
		
		Util.setupLogger();
		ConfigInfo c = new ConfigInfo();
		int selected =4;

		if (args.length == 1) {
			selected = Integer.parseInt(args[0]);
		}
			
//			System.out.println("XACML2Firewall Conversion ");
//		 	System.out.println("XACML target policy : " + c.xacml_policy[selected]);
//		 	System.out.println("log-reference file : " + c.policy_reference[selected]);
		
		long start = System.currentTimeMillis();
					
		
		// main func...
		// load policy
		//XACML2Firewall.TestProgram.conversionTime = 0;
		//XACML2Firewall.conversionTree.initialize();

		conversionTree resTree = new conversionTree(c.logDir + c.xacml_policy[selected], c.logDir+c.policy_reference[selected], c.fwr_policy[selected]);
		PolicyFinder policyFinder = new PolicyFinder();
		resTree.init(policyFinder);
		
		long stop = System.currentTimeMillis();	 	
		conversionTime = stop - start;
		System.out.println(conversionTime);
	}

}
