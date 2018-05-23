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

import org.apache.log4j.Logger;

import resolutionTree.resolutionTree;
//import xEngineVerifier.diversity.*;
//import xEngineVerifier.diversity_with_rule_tracer.*;

import PddOriginblock.*;

import com.sun.xacml.finder.PolicyFinder;
import commonImpelmentation.Func;
import commonImpelmentation.Mapper;
import commonImpelmentation.RequestParser;
import commonImpelmentation.TargetFunc;

public class xxAclQuery {

	private static Logger logger = Logger.getLogger(xxAclQuery.class);

	Func func;
	TargetFunc targetFunc;
	Mapper mapper;
	xAclFunc xaclfunc;
	RequestParser reqParser;
	PrintStream outS = null;

	static int query_size = 10000;
	public static int mode =0;	
	public static float static_total_time =0;
	
	public static float conversion_time =0;
	public static long memory_size=0;
	
	long[][] query = new long[query_size][3];

	int extracted_query_size = 0;

	Acl t1 = new Acl();

	public static void initialize(int modeIn) {

		mode =modeIn;	
		static_total_time =0;
	}
	
	
	public xxAclQuery(String log) {

		try {
			this.outS = new PrintStream(new FileOutputStream(log));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		this.func = new Func(outS);
		this.targetFunc = new TargetFunc(func, outS);
		this.mapper = new Mapper(targetFunc, outS, 15);
		func.setMapper(mapper);
		mapper.setFunc(func);
		reqParser = new RequestParser(func, mapper);
		//xaclfunc = new xAclFunc(outS, t1, t2, func);
		xaclfunc = new xAclFunc(outS, t1, func);
	}

	public void xacmDirParsing(String fwrPolicy, String ref, String queryDir,
			int OutType, String xacmlpolicy) 
	{
		if (mode == 0){
			//t1.test_acl_query(fwrPolicy);
			t1.acl_build(fwrPolicy);
		}else if (mode == 1){
			t1.build_quick_table(fwrPolicy);
			//t1.test_quick_table();
		}
		
		conversion_time = t1.conversiontime;
		memory_size = t1.memorysize;
	
	
//		 it's setting..		
		xFirFileParser xfirparser = new xFirFileParser(xaclfunc);
		
// create resolution tree...		
		resolutionTree resTree = new resolutionTree(xacmlpolicy);
		PolicyFinder policyFinder = new PolicyFinder();
		resTree.init(policyFinder);
		
// set resolution tree		
		xfirparser.setResolutionTree(resTree);
		
		
		// mapper....
		xfirparser.setMapper(mapper);
		xfirparser.setreqParser(reqParser);


		xaclfunc.set_state_mapping(ref);
		
		xfirparser.queryDir_processing_pre(new File(queryDir), new File(ref));

		long totaltime = 0;
		
		long start = System.currentTimeMillis();
		xfirparser.queryDir_processing(OutType);

		long end = System.currentTimeMillis();
		totaltime = end - start;

		static_total_time = totaltime;
	}



	public static void main(String[] args) {
		if (args.length != 8) {
			logger.info("Usage: java AclQuery  <policie> <policie> <requestdir>");
			System.exit(1);
		} else if (args.length == 8) {

			/*
			 * 0 : fwr policy 1 : log file 2 : xacml request dir 3 : fwr
			 * formated request file (already converted) 4 : file to write... 5 :
			 * option 1, xacmParsing or fwrParsing 6 : option 2, writeFile or
			 * noWrite
			 */

			String outS = args[4].trim();
			String Outoption = args[6].trim();
			int OutType = 1;
			if (Outoption.equals("writeFile")){
				OutType = 1;				
			}else if (Outoption.equals("noWrite")){
				OutType = 2;				
			}
			
			xxAclQuery acler = new xxAclQuery(outS);

			if (args[5].trim().startsWith("xacmDirParsing")) {

				acler.xacmDirParsing(args[0], args[1], args[2], OutType,
						args[7]);
				// test
				System.out.println("total processing time: "+static_total_time);
			}
		}
	}
}