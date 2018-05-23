/**
 * @author Fei Chen
 * feichen@cse.msu.edu
 * Computer Science and Engineering
 * Michigan State University
 * 12/03/2008
 */

package xEngineVerifier;

import java.io.*;

import org.apache.log4j.Logger;

import commonImpelmentation.Func;
import commonImpelmentation.Mapper;
import commonImpelmentation.RequestParser;
import commonImpelmentation.TargetFunc;

public class xReqNode{
	private static Logger logger = Logger.getLogger(xReqNode.class);
	
	//long query[] = new long[3];
	
	int decision = 0;
	int xacml_rule_num = 0;

	public xReqNode(int dec, int xacml_rule_no) {//long query[], 
		decision = dec;
		xacml_rule_num = xacml_rule_no;

		//for (int i = 0; i < 3; i++) {
		//	this.query[i] = query[i];
		//}
	}

	public int get_decision() {
		return decision;
	}
	
	public void put_xacml_rule_num(int a) {
		this.xacml_rule_num = a;
	}

	public int get_xacml_rule_num() {
		return this.xacml_rule_num;
	}

	/*
	public long[] get_query_by_long() {
		return this.query;
	}

	public String get_query_by_String() {
		return (""+query[0]+ "." + query[1]+ "." + query[2]);
	}
	
	
	public int getRuleNum() {
		return this.rule_num;
	}
	*/
	public void printReqInfo() {

		//String StrInfo = " xReqNode Info : Query : " + query[0] + "."
		//		+ query[1] + "." + query[2] + "  rule_seq : " + rule_num
		//		+ " & decison :" + decision + " & xacml_rule_num :" + xacml_rule_num;
		String StrInfo = " xacml_rule_num : " + xacml_rule_num + " & decison :" + decision;
		System.out.println(StrInfo);
	}
}
