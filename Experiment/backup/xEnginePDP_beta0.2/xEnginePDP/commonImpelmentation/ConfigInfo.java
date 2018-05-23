/**
 * @author Fei Chen
 * feichen@cse.msu.edu
 * Computer Science and Engineering
 * Michigan State University
 * 12/03/2008
 */

package commonImpelmentation;

import ncsu.util.Util;

import com.sun.xacml.ParsingException;
import com.sun.xacml.finder.PolicyFinder;

public class ConfigInfo{

	// Directory
	
	public static final String logDir = "XACMLPolicies";
//	public static final String logDir = "XACMLsubjects_requests";	
	
//	public static final String logDir = "subjects";
	
	// the results for single-valued requests
	public static final String SoDDir = "Reponse/single/";

	// the results for multi-valued requests
	public static final String nonSoDDir = "Reponse/multi22/";
	
	// log
	public static final String logWriter = "mr_log.txt";
	
	// policies
	public static final String[] xacml_policy = {"/continue-a.xml"};

	public static final String[] fwr_policy = {"Reponse/fwr_policy/continue-a.txt"};
	
	// policy reference 
	public static final String[] policy_reference = {"/continue-a.xml.fwr.log"};

	// Generated Requests_file
	
	public static final String[] SoDreqDir = {"/requests/single/"};		

	public static final String[] nonSoDreqDir = {"/requests/multi22/"};	

		public static final String[] SOD_fwr_response = {
				SoDDir+"fwr_continue-a.txt"
		};		

		public static final String[] SOD_xacml_response = {
			SoDDir+"xacml_continue-a.txt"
		};	

		public static final String[] SOD_compare_result = {
			SoDDir+"comparion_result/comp_continue-a.txt"
		};		
		
		public static final String[] SOD_fwr_converted_query = {
				SoDDir+"continue-a_query.txt"
		}; 	

		public static final String[] nonSOD_fwr_response = {
				nonSoDDir+"fwr_continue-a.txt"
		};		

		public static final String[] nonSOD_xacml_response = {
			nonSoDDir+"xacml_continue-a.txt"
		};	

		public static final String[] nonSOD_compare_result = {
			nonSoDDir+"comparion_result/continue-a.txt"
		};		
		
		public static final String[] nonSOD_fwr_converted_query = {
				nonSoDDir+"continue-a_query.txt"
		};
}
