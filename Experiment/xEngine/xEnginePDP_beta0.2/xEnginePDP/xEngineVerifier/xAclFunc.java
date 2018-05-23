/**
 * @author Fei Chen
 * feichen@cse.msu.edu
 * Computer Science and Engineering
 * Michigan State University
 * 12/03/2008
 */

package xEngineVerifier;

import java.io.*;
import java.util.*;
//import java.util.Hashtable;

import PddOriginblock.*;

import org.apache.log4j.Logger;


// import xEngineVerifier.diversity.*;
//import xEngineVerifier.diversity_with_rule_tracer.*;


import commonImpelmentation.Func;
import commonImpelmentation.Mapper;
import commonImpelmentation.RequestParser;
import commonImpelmentation.TargetFunc;

public class xAclFunc {

	
	private static Logger logger = Logger.getLogger(xAclFunc.class);


	PrintStream outS = null;
	
	Acl t1 = null;
	//xEngineVerifier.diversity.Acl t1 = null;
	//xEngineVerifier.diversity_with_rule_tracer.Acl t2 = null;	
 	
	Func func = null;
	
	private Hashtable stateHash = new Hashtable(100);
	long start_milliseconds = 0;
	long stop_milliseconds = 0;


	public static int mode =0;	
	
	public static void initialize(int modeIn) {
		mode =modeIn;
	}
	
	public xAclFunc(PrintStream outS, Acl t1,Func func) {
		// TODO Auto-generated constructor stub
		this.outS = outS;
		this.t1 = t1;
		this.func = func;
	}
	
	/*
	public xAclFunc(PrintStream outS, xEngineVerifier.diversity.Acl t1, xEngineVerifier.diversity_with_rule_tracer.Acl t2,Func func) {
		// TODO Auto-generated constructor stub
		this.outS = outS;
		this.t1 = t1;
		this.t2 = t2;
		this.func = func;
	}
	*/


	public void set_start_milliseconds(){		
		start_milliseconds = System.currentTimeMillis();		
	}

	public void set_stop_milliseconds(){		
		stop_milliseconds = System.currentTimeMillis();		
	}	

	public long show_result(){

		long time = stop_milliseconds - start_milliseconds;

/*		
		System.out.println("\n\n----------------------");		 
		System.out.println("\n\nxEngine Verification Time");		 
		System.out.println("Elapsed Time :: "+time);	
		System.out.println("----------------------\n\n");
*/		
		return time;
	}	

	public void print_output(String StrInfo){

		outS.println(StrInfo);	
	}

	public void print_output(xReqNode newReqNode) {
/*
		if (newReqNode != null){
			
			newReqNode.printReqInfo();
			
		}else {System.out.println("not applicable");}
*/		
		
		// -1 not-applicable
		// 0 deny
		// 1 permit
		
		String printOut = null;
		if (newReqNode != null){
			
			int xacmlstate = newReqNode.get_xacml_rule_num();
		
			printOut = " rule. "+xacmlstate;//" query." + newReqNode.get_query_by_String() + 
			if (xacmlstate == -1){
			
				printOut = "xacmlstate is -1, not applicable decision made by query :-1";
			}else if (xacmlstate == -2){
				printOut = "xacmlstate -2, OutOfRange-i.e. -1.-1.0 query :-2";
			} else {
				printOut = printOut + ":"+newReqNode.get_decision();
			}		
		}
		else {printOut = "xacmlstate = null, No decision made by queries..every ReqNode is Null :-3";}
		outS.println(printOut);
	}
	
	public int run_single_query(long query[], Set<Integer[]> origins)
	{		
		int v = 0;		
		int decision = 0;
		int rule_num = 0;
		//ArrayList<xReqNode> reqList = new ArrayList<xReqNode>();
		
		if (mode == 0 || mode == 1){				
			decision = t1.acl_query_single(query, origins);
		} else if (mode == 2){
//			v = t1.acl_query_single_with_super_saver_memory(query);
		}else if (mode == 3){
			decision = t1.acl_query_single_with_quick_table(query, origins);
		}else if (mode == 4){
//			v = t1.acl_query_single_with_binary_query(query);
		}
		  
		/*
		if (mode == 2 || mode ==3 || mode ==4){
			rule_num = -100;		  
			if (v==1)  {decision = 1;}
			else if (v==0)  {decision = 0;}
			else if (v==2)  {decision = -1;}	
			else if (v==-1) {decision = -2;}				
		}
		*/
		return decision;
	}

	public void set_state_mapping(String ref) {
		putStateMapperFromLogFile(new File(ref));
	}
	
	public ArrayList getStateHash(int a) {
		
		
//		System.out.println("state"+stateHash.get(str));
//		System.out.println("crash"+a);
//		stateHash.get(arg0)
		if (!stateHash.containsKey(a)) return null;
		return (ArrayList) stateHash.get(a);	
	}
	
	public void putStateMapperFromLogFile(File logFile) {
		// TODO Auto-generated method stub
		BufferedReader br = func.getBufferedReader(logFile);

		if (br == null) {
			System.out.println("Set up reference failed ... check file name");
		}

		String line = func.readNextLine(br);

		int flag = -1;

		String element = null;
		int value = 0;
		int startpoint = 0;
		
		
		while (line != null) {

			// flag setting

			String trimline = line.trim();

			if (trimline.startsWith("####State Info Start#####")) {
				flag = 1;
			} 

			if (flag == 1 && trimline.startsWith("####State Info End#####")) {
				break;
			}

			if (flag ==1) {
				startpoint = line.indexOf(":");

				if (startpoint != -1) {

					element = line.substring(0, startpoint).trim();
					int elem = Integer.parseInt(element.trim());
					String othernums = line.substring(startpoint+1, line.length()).trim();
					String[] result = othernums.split("\\,");
					ArrayList valueList = new ArrayList();
//					System.out.println(othernums);
					for (int jj=0; jj< result.length; jj++){
//						System.out.println("  PP "+result[jj]);
						if (result[jj] != null && !result[jj].trim().equals(""))
						valueList.add(Integer.parseInt(result[jj].trim()));
					}
					
					
//					value = Integer.parseInt(line.substring(startpoint+1, line.length()).trim());			
					//stateHash.put(elem, value);
					
					stateHash.put(elem, valueList);
					
//					System.out.println(element + "==>" +  value);
				}
			}

			line = func.readNextLine(br);
		}

		
//	  1 : permit decision
//	  0 : deny decision
//	 -1 : It's not applicable decision (From xSearch) and State is -1
//	 -2 : Domain is out of range, so it's same to non applicable decision (From xSearch) State is -2
//	 -3 : Null , no req get a applicable ruleset. So it's non applicable decision (From meging requests) Non States Finally !!
	
		int tempn = Integer.parseInt(element) + 1 ;
		
		ArrayList valueList1 = new ArrayList();
		ArrayList valueList2 = new ArrayList();
		valueList1.add(tempn);
		valueList2.add(0);		
		stateHash.put(valueList1, -1); 
		stateHash.put(valueList2, -2); 
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
