/**
 * @author Fei Chen
 * feichen@cse.msu.edu
 * Computer Science and Engineering
 * Michigan State University
 * 12/03/2008
 */

package commonImpelmentation;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.tree.*;
import javax.xml.parsers.*;


import org.apache.log4j.Logger;
import org.w3c.dom.*;
import org.xml.sax.*;

import ncsu.util.Util;

import com.sun.xacml.*;
import com.sun.xacml.attr.*;
import com.sun.xacml.cond.*;
import com.sun.xacml.ctx.*;
import com.sun.xacml.finder.*;

// public class resolutionTree extends PolicyFinderModule implements
// ErrorHandler{
public class Func {

	protected static Logger logger = Logger.getLogger("Func.class");

	PrintStream outS = null;
	Mapper mapper = null;
	
	public Func(PrintStream outS) {
		this.outS = outS;
	}

	public void setMapper(Mapper mapper) {
		this.mapper = mapper;
	}
	
	public String makeindent(int val) {

		String indent = "";
		for (int i = 0; i < val; i++) {
			indent = indent + "    ";
		}

		return indent;

	}
	

	
//	inpute : [5,4,3][12][0]
//	rule : [5,5]&[4,4]&[3,3]*[12,12]*[0,0]
	                               
//	inpute : [1][12][3]
//	rule : [1,1]*[12,12]*[3,3]	

// two types are available,   one is Atomic and the other is Merge
/*Atomic Type
[0,0][0,0][0,0]
[0,0][0,0][1,1]
[0,0][1,1][0,0]
[0,0][1,1][1,1]
[4,4][0,0][0,0]
[4,4][0,0][1,1]
[4,4][1,1][0,0]
[4,4][1,1][1,1]
[5,5][0,0][0,0]
[5,5][0,0][1,1]
[5,5][1,1][0,0]
[5,5][1,1][1,1]
[21,21][0,0][0,0]
[21,21][0,0][1,1]
[21,21][1,1][0,0]
[21,21][1,1][1,1]
Merge Type
[0,0][0,1][0,1]
[0,0][5,5][0,1]
[0,0][7,8][0,1]
[4,5][0,1][0,1]
[4,5][5,5][0,1]
[4,5][7,8][0,1]
[21,21][0,1][0,1]
[21,21][5,5][0,1]
[21,21][7,8][0,1]
*/	
	
	public List conv(String numericrule, String type) {
		// TODO Auto-generated method stub
		
		String split[] = numericrule.split("\\[|\\]");	
		String rule = "";
		
		for (int i=0; i < split.length ; i++){
			String subrule = conv2(split[i], type);
			if (subrule.startsWith("No")){return null;}
			if (subrule != null && !subrule.equals("")){
				if (rule.equals("")){rule = rule + subrule;}
				else {rule = rule + "*" + subrule;}}
		}
	               
		return multiconv(rule, type);
	}


//	 multiconv
//	 input [2,2]&[4,4]*[6,6]*[0,4]
//	 output as List Type : 1st, [2,2][6,6][0,4] 2nd,  [4,4][6,6][0,4]
	
	public List multiconv(String rule, String type) {
		// TODO Auto-generated method stub
		if (rule == null) return null;

// 		System.out.println("rule:"+rule);
		String split[] = rule.split("\\*");
 
//		System.out.println(split[0]);
//		System.out.println(split[1]);
//		System.out.println(split[2]);
	 	
		List returnRule = new ArrayList();
		
	 
		
		String subjectssplit[] = split[0].split("\\&");
		String resourcessplit[] = split[1].split("\\&");
		String actionssplit[] = split[2].split("\\&");
		
//		System.out.println(subjectssplit[0]);
		
		for (int i=0; i < subjectssplit.length ; i++){
			for (int j=0; j < resourcessplit.length ; j++){
				for (int k=0; k < actionssplit.length ; k++){

					String tempRule = subjectssplit[i]+"^"+resourcessplit[j]+"^"+actionssplit[k];
					returnRule.add(tempRule);				
				}	
			}	
		}		
		return returnRule;
	}


	
//	inpute : [5,4]
//	rule : [5,5]&[4,4]    or Anysubjects or No

// input 	Anysubjects
// output : [0, last_num]
	
//	
	public String conv2(String string, String type) {
		
		ArrayList aa = new ArrayList();
		if (string == null) return null;

		String split[] = string.split(",");	
		String rule = "";
		String tmp_str = null;
//		int prev = -1;
//		int temp = -1;
//		int start = -1;
//		int end = -1;
		
		
		// extract data
		for (int i=0; i < split.length ; i++){
			
			tmp_str = split[i].trim();

/* 		
			// any data
			if (tmp_str.startsWith("Anysubjects")){
				return "[-1," + (((Hashtable) mapper.getHashList().get(0)).size()-1) +"]";
			}else if (tmp_str.startsWith("Anyresources")){
				return "[-1," + (((Hashtable) mapper.getHashList().get(1)).size()-1)+"]";
			}else if (tmp_str.startsWith("Anyactions")){
				return "[-1," + (((Hashtable) mapper.getHashList().get(2)).size() -1) +"]";
			}
*/ 
			if (tmp_str.startsWith("Anysubjects")){
				return "[0," + ((((Hashtable) mapper.getHashList().get(0)).size())-1) +"]";
			}else if (tmp_str.startsWith("Anyresources")){
				return "[0," + ((((Hashtable) mapper.getHashList().get(1)).size())-1)+"]";
			}else if (tmp_str.startsWith("Anyactions")){
				return "[0," + ((((Hashtable) mapper.getHashList().get(2)).size())-1) +"]";
			}
 		
			// no data
			if (tmp_str.startsWith("No")){
				return "No";
			}		
			
			if (!tmp_str.equals("")){
					Integer intvalue = Integer.parseInt(tmp_str.trim());
					if (!aa.contains(intvalue)){
					aa.add(intvalue);}				
			}
				

			
		}

		// collection
		
		Collections.sort(aa);

		
		if (type.equals("Atomic")){
		
		if (aa.size()>0){
			
			for (int i=0; i < aa.size() ; i++){
				if (!rule.equals("")){rule = rule + "&";}
				rule = rule + "[" + aa.get(i) +","+ aa.get(i)+ "]";
			}
			
		}
		} else if (type.equals("Merge")){
			
			int prev = -1;
			int temp = -1;
			int start = -1;
			int end = -1;
			 
				
				
				for (int i=0; i < aa.size() ; i++){
					
					temp = (int)((Integer)aa.get(i));
					if (prev == -1){
						start = temp;
						end = temp;
						prev = temp;
					} else if ((prev+1) == temp){
						end = temp;
						prev = temp;
					} else{
						if (rule.length() == 0){rule = rule + "[" +start +"," + end + "]";}
						else {rule = rule + "&" +"[" +start +"," + end + "]";}
						start = temp;
						end = temp;
						prev = temp;			
					}
					
					// add last Rule
					if (i == (split.length-1)){
						if (rule.length() == 0){rule = rule + "[" +start +"," + end + "]";}
						else {rule = rule + "&" +"[" +start +"," + end + "]";}	
					}
				
				}
		 
		}
				
		
		
		
		
	
	
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
//		String lastRule = 
//		if (rule.length() != 0){}
//		if (start != -1 || end != -1){rule = rule + "[" +start +"," + end + "]";}
		
		// TODO Auto-generated method stub
		return rule;
	}
	
	//Atomic and the other is Merge
	
	public synchronized static void main(String[] args) throws Throwable{
		
		Util.setupLogger();

		Func func = new Func(null);
		List result = func.conv("[21, 0, 5,4][0,1][0,1]", "Atomic");

		System.out.println("Atomic Type");
		for (int i=0; i < result.size() ; i++){
			System.out.println(result.get(i));
		}

		System.out.println("Merge Type");
		List result2 = func.conv("[21, 0, 5,4][0,1,5,7,8][0,1]", "Merge");
		for (int i=0; i < result2.size() ; i++){
			System.out.println(result2.get(i));
		}
	}

	public String deleteStrAttributeQuot(String string) {
		// TODO Auto-generated method stub
		
    	if (string.trim().startsWith(("StringAttribute:"))){
    		
//    		System.out.println(string);
    		int sp = string.indexOf("\"");
    		int lp = string.indexOf("\"", sp+1);
    		
    		if (sp != -1 && lp != -1)
    		string = string.substring(sp+1, lp);
    		
//    		System.out.println(sp + " " + lp +  string);
    		
    	}
		return string;
	}
	
	
	
	
	
	
	
    public BufferedReader getBufferedReader(File File) {
		
		try {
			return (new BufferedReader (new InputStreamReader (new FileInputStream (File))));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return null;
    }

    public String readNextLine(BufferedReader br) {
		
		try {
			return br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
    }


}
