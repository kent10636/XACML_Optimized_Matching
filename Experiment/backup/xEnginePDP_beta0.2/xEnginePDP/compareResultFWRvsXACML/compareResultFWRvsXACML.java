/**
 * @author Fei Chen
 * feichen@cse.msu.edu
 * Computer Science and Engineering
 * Michigan State University
 * 12/03/2008
 */

package compareResultFWRvsXACML;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
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
import com.sun.xacml.cond.FunctionFactory;
import com.sun.xacml.cond.FunctionFactoryProxy;
import com.sun.xacml.cond.StandardFunctionFactory;
import com.sun.xacml.ctx.RequestCtx;
import com.sun.xacml.finder.AttributeFinder;
import com.sun.xacml.finder.PolicyFinder;
import com.sun.xacml.finder.impl.CurrentEnvModule;
import com.sun.xacml.finder.impl.FilePolicyModule;
import com.sun.xacml.finder.impl.SelectorModule;

public class compareResultFWRvsXACML {
	
	private static Logger logger = Logger.getLogger(compareResultFWRvsXACML.class);
	
	
	
	
	private String nextFDDdecision(BufferedReader br){
		
		try {
			 
			return br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	private String nextXACMLdecision(BufferedReader br){
		
		String result = "filler";
		String seqnum = "";
		
	
		while (!result.startsWith("<Decision>")){

			if (!result.startsWith("<")){
					seqnum = result;
			}
				
			try {
				result =   br.readLine();
				// end of line
				if (result == null) {return null;}				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				
				e.printStackTrace();
			}
		}
		
		return seqnum + ":" + result;
	}
	
//	private int compare(File pol1, File pol2, File saved_file){
	private int compare(String pol1, String pol2, String saved_file){		
		System.out.println("compared....the two results");
		
		BufferedReader br1 = null, br2 = null;
		String line1=null, line2 = null;
		
		try {
			br1 = new BufferedReader (new InputStreamReader (new FileInputStream (pol1)));
			br2 = new BufferedReader (new InputStreamReader (new FileInputStream (pol2)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			 
		int count = 0;
		int diff = 0;
		int permit= 0;
		int deny = 0;
		int notapp = 0;
		int total = 0;
		
		String tline = null;
	
		String a = "s";
		String b = "s";
		while ( a!= null && b != null){		
			a= nextFDDdecision(br1);
			b= nextXACMLdecision(br2);
			
			
			if (a== null || b == null){
				break;
			}
			
			total++;
			
			String[] a_decision = a.split(":");
			String[] b_decision = b.split(":");

//		  1 : permit decision
//		  0 : deny decision
//		 -1 : It's not applicable decision (From xSearch)
//		 -2 : Domain is out of range, so it's same to non applicable decision (From xSearch)
//		 -3 : Null , no req get a applicable ruleset. So it's non applicable decision (From meging requests)
			
			
			if (a_decision[1].equals("1")){				
				if (b_decision[1].equals("<Decision>Permit</Decision>")){
					permit++;	
				} else {
					diff++;
					System.out.println("case 1 :" + a + "====" +b);	
				}				
			} else if (a_decision[1].equals("0")){				
				if (b_decision[1].equals("<Decision>Deny</Decision>")){
					deny++;	
				} else {
					diff++;
					System.out.println("case 2 :" + a + "====" +b);	
				}				
			}else if (a_decision[1].equals("-1") || 
					a_decision[1].equals("-2") ||
					a_decision[1].equals("-3")
					
			){				
				if (b_decision[1].equals("<Decision>NotApplicable</Decision>")){
					notapp++;	
				} else {
					diff++;
					System.out.println("case 3 :" + a + "====" +b);	
				}				
			} 	
				
		}

		System.out.println("Total Diff is (0 is good):: "+diff);
		System.out.println("match-permit is :: "+permit);
		System.out.println("match-deny is :: "+deny);
		System.out.println("match-notapp is :: "+notapp);
		
		
		System.out.println("Total correctly matched Number is (/out of):: "+(permit+deny+notapp)+"/"+total);		
		if (line1== null && line2 == null){
			
		}else {
			System.out.println("WARNING :: both policies has not same number of lines ");
		}
	
	
		return 1;
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		Util.setupLogger();
		if (args.length != 3) {
			System.out.println(args.length);
			logger.info("1Usage: java RequestVerifierOfTwoPolicies <policie> <policie> <requestdir>");
			System.exit(1);
		} else if (args.length == 3){
/*			
			File policies1 = new File(args[0]);
			File policies2 = new File(args[1]);
			File saved_file = new File(args[2]);
*/
			String policies1 = args[0];
			String policies2 = args[1];
			String saved_file = args[2];
			
//			System.out.println(policies1.getPath());
//			System.out.println(policies1.getAbsoluteFile());
			compareResultFWRvsXACML rp = new compareResultFWRvsXACML();
			rp.compare(policies1, policies2, saved_file);

		}
		logger.info("Done");
	}
}
