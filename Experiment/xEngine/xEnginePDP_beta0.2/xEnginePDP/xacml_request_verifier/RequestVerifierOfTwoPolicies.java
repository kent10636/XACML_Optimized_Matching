/**
 * @author Fei Chen
 * feichen@cse.msu.edu
 * Computer Science and Engineering
 * Michigan State University
 * 12/03/2008
 */

package xacml_request_verifier;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.sun.xacml.Indenter;
import com.sun.xacml.PDP;
import com.sun.xacml.PDPConfig;
import com.sun.xacml.ParsingException;
import com.sun.xacml.Rule;
import com.sun.xacml.Target;
import com.sun.xacml.cond.FunctionFactory;
import com.sun.xacml.cond.FunctionFactoryProxy;
import com.sun.xacml.cond.StandardFunctionFactory;
import com.sun.xacml.ctx.RequestCtx;
import com.sun.xacml.finder.AttributeFinder;
import com.sun.xacml.finder.PolicyFinder;
import com.sun.xacml.finder.impl.CurrentEnvModule;
import com.sun.xacml.finder.impl.FilePolicyModule;
import com.sun.xacml.finder.impl.SelectorModule;

public class RequestVerifierOfTwoPolicies {
	
	private static Logger logger = Logger.getLogger(RequestVerifierOfTwoPolicies.class);
	

	int maxHandlingSize = 500;
	private	File[] Tarrequests ;
	private	RequestCtx[] request;
	static public long static_total_time = 0;
	static public long static_total_using_median = 0;	
	
	static public long static_total_processing_time = 0;
	static public long static_total_requests_num = 0;	
	static public long static_avg_processing_time_per_requests_set = 0;	
	static public String static_processing_time_Iterators = "";	
	static public int static_maxIterators = 2;

	public static void initialize(int max) {
		static_total_time = 0;
		static_total_processing_time = 0;
		static_avg_processing_time_per_requests_set = 0;	
		static_processing_time_Iterators = "";	
		static_maxIterators = max;
	}
	
	private static File cleanIt(String output) {
		File outputF = new File(output);
		File[] contents = outputF.listFiles();
		if (contents != null && contents.length != 0) {
			logger.warn("Deleting existing files.");
			for (int i = 0; i < contents.length; i++) {
				if (!contents[i].delete()) {
					logger.warn("Could not delete file " + contents[i]);
				}
			}
		}
		return outputF;
	}
	

	PDP pdp = null;
	
	private int setPDP(File Policy){
		
	    FilePolicyModule policyModule = new FilePolicyModule();
	    policyModule.addPolicy(Policy.getPath());
	    
	    CurrentEnvModule envModule = new CurrentEnvModule();
	    
	    PolicyFinder policyFinder = new PolicyFinder();
	    Set policyModules = new HashSet();
	    policyModules.add(policyModule);
	    policyFinder.setModules(policyModules);

	    AttributeFinder attrFinder = new AttributeFinder();
	    List attrModules = new ArrayList();
	    attrModules.add(envModule);
	    attrFinder.setModules(attrModules);
	    
	    pdp = new PDP(new PDPConfig(attrFinder, policyFinder, null));
	    
    return 1;
}
 

 
	
	private int validationofPDP(File Policy, File requestDir, String outWriter, int OutType){
	 
		String policystr = Policy.getPath();
		setPDP(Policy);

		Tarrequests = requestDir.listFiles();
		

		PrintStream outS = null;
		try {
			outS = new PrintStream (new FileOutputStream (outWriter));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
 
		
		
/*		
		
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter("evaluationtime.txt", true));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			bw.newLine();
			bw.newLine();
			bw.write("START: XACML processsing time ..:xacmlpolicy"+requestDir);
			bw.newLine();
			bw.flush();	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
*/		
		
		
		
		
		request = new RequestCtx[maxHandlingSize];
		int iterSize = (int) (Tarrequests.length-1)/maxHandlingSize;
		
 
		
		long totaltime = 0;
		long totalprocessingtime = 0;
		
		long start = 0;
//		long precurrent = 0;
//		long current = start;
			
//		int max = 1;
		String result = "";

		long time_spents = 0;
		long loadtime =0;
		ArrayList valueArray = new ArrayList();


for (int j=0; j < static_maxIterators ; j++){
	
	if (j==1) {start = System.currentTimeMillis();}
	
	time_spents = 0;
	
		for (int i=0; i <= iterSize; i++){
			
			int startNum = maxHandlingSize * i;
			int endNum = maxHandlingSize * (i+1);
			
			if (endNum > Tarrequests.length){
				endNum = Tarrequests.length;
			}
	
			if (startNum+1 <= endNum){		
			loadtime = loadtime + loadReq (startNum, endNum);
			long time = processReq (startNum, endNum, outS, OutType);
			time_spents = time_spents + time;
			}
		}
		
		if (j>0) {totalprocessingtime = totalprocessingtime + time_spents;
		
		valueArray.add(time_spents);
		}
		result = result + "  "+ j +"th : "+ (time_spents);		
		
}		
		
long end = System.currentTimeMillis();
totaltime = end - start;

static_total_time = totaltime;
static_total_processing_time = totalprocessingtime;
static_avg_processing_time_per_requests_set = (totalprocessingtime/(static_maxIterators-1));
static_processing_time_Iterators = result;
static_total_requests_num = Tarrequests.length * (static_maxIterators-1);

/*
if (valueArray.size()>=6 && valueArray.size()<10){
	Collections.sort(valueArray);
	long mediTotal = 0;
	for (int u=2 ; u < (valueArray.size()-2) ;u++){
		mediTotal = mediTotal + (Long) valueArray.get(u);		
	}
	static_total_using_median = (mediTotal / (valueArray.size()-4)) * valueArray.size();
	
}
*/

int ASize = valueArray.size();

if (ASize>=10){
	Collections.sort(valueArray);
	long mediTotal = 0;
	for (int u=3 ; u < (ASize-3) ;u++){
		mediTotal = mediTotal + (Long) valueArray.get(u);		
	}
	static_total_using_median = (long)(((float)mediTotal / (float)(ASize-6)) * (float)ASize);		
} else if (ASize >= 5){
	Collections.sort(valueArray);
	long mediTotal = 0;
	for (int u=0 ; u < (ASize-2) ;u++){
		mediTotal = mediTotal + (Long) valueArray.get(u);		
	}
	static_total_using_median = (long)(((float)mediTotal / (float)(ASize-2)) * (float)ASize);	
} else {
	static_total_using_median = static_total_processing_time;
}

/*
try {
	bw.write("\ntotaltime : "+totaltime);
	bw.newLine();
	bw.write("\ntotal processing time : "+totalprocessingtime);
	bw.newLine();
	bw.write("\navgtime : "+((float) totalprocessingtime/(max-1)));
	bw.newLine();
	bw.write(result);
	bw.write("\nEND: xacmlpolicy"+requestDir);
	bw.newLine();
	bw.flush();	
} catch (IOException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
*/

		return 1;
	}

	
 


	private long processReq(int startNum, int endNum, PrintStream outS, int OutType) {
		// TODO Auto-generated method stub
		
		long start = System.currentTimeMillis();
		int finishNum = endNum - startNum;
		
		for (int t = 0; t < finishNum; t++) {
			
			if (OutType == 1){
			outS.println(Tarrequests[t+ startNum].getName().toString());
			pdp.evaluate(request[t]).encode(outS);
			} else if (OutType == 2) {
			pdp.evaluate(request[t]);	
			}		
		}
		
		long stop = System.currentTimeMillis();
		long time = stop - start;
		
		return time;
		
	}


	private long loadReq(int startNum, int endNum) {
		// TODO Auto-generated method stub

		long start = System.currentTimeMillis();
		
		for (int t = startNum; t < endNum; t++) {
//			for (int t = 0; t < 800; t++) {
			try {
//				System.out.println(t+"----------");
				request[t%maxHandlingSize] = RequestCtx.getInstance(new FileInputStream(Tarrequests[t]));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParsingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}							
		}
		
		long stop = System.currentTimeMillis();
		long time = stop - start;
		
		return time;
	}


	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		Util.setupLogger();
		if (args.length != 4) {
			logger.info("Usage: java RequestVerifierOfTwoPolicies <policie> <policie> <requestdir>");

			logger.info("Usage: java RequestVerifierOfTwoPolicies <policie> <requestdir> <outS> ");			
			System.exit(1);
		} else if (args.length == 4){
			
			File policies1 = new File(args[0]);
			File tarDir = new File(args[1]);
			String outWriter = args[2].trim();
			String Outoption = args[3].trim();
			int OutType = 1;
			
			if (Outoption.equals("writeFile")){
				OutType = 1;				
			}else if (Outoption.equals("noWrite")){
				OutType = 2;				
			}
			
		
			RequestVerifierOfTwoPolicies rp = new RequestVerifierOfTwoPolicies();

			
			
			System.out.println("Policy :: " + policies1 + " ::" + " saved in " + outWriter + "OutOption" + Outoption);			
			
			
			
			
			rp.validationofPDP(policies1, tarDir, outWriter, OutType );	
		}
		else {
/*	
			File policies1 = new File(args[0]);
			File policies2 = new File(args[1]);
			File tarDir = new File(args[2]);
//			File ranDir = new File(args[2] + "/random/");
			
			
//			File tarDir = new File(args[2] + "/targeted/");
//			File ranDir = new File(args[2] + "/random/");

			File[] Tarrequests = tarDir.listFiles();
//			File[] Ranrequests = ranDir.listFiles();		
			
			RequestVerifierOfTwoPolicies rp = new RequestVerifierOfTwoPolicies();
			RequestVerifierOfTwoPolicies rp2 = new RequestVerifierOfTwoPolicies();
			
			System.out.println("Original :: " + policies2 + " ::");
			rp2.validationofPDP(policies2, tarDir);
			
			System.out.println("Revised :: " + policies1 + " ::");			
			rp.validationofPDP(policies1, tarDir);			
			
			rp.comparetworesults(policies1, policies2, tarDir);
*/
		}
		
	 
		logger.info("Done");
	}
}
