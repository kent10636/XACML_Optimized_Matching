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

import commonImpelmentation.Func;
import commonImpelmentation.Mapper;
import commonImpelmentation.RequestParser;
import commonImpelmentation.TargetFunc;


public class xFirFileParser {

	static final Comparator DISPLAYNAME_ORDER =
		 new Comparator() {
		   public int compare(Object obj1, Object obj2) {
	            int i1 = ((xReqNode)obj1).get_xacml_rule_num();
	            int i2 = ((xReqNode)obj2).get_xacml_rule_num();

	            return Math.abs(i1) - Math.abs(i2);
		   }
		 }; 
		 
private static Logger logger = Logger.getLogger(xFirFileParser.class);

xAclFunc xaclfunc;
resolutionTree restree;
Mapper mapper;
RequestParser reqParser ;

public static int mode =0;	

public static int numOfSubrequest = 0;
public static int numOfRequests = 0;

List reqFactory = new ArrayList();

public static void initialize(int modeIn) {

	mode =modeIn;
	numOfSubrequest = 0;
	numOfRequests = 0;
	
}

public void setResolutionTree(resolutionTree resTree2) {
	// TODO Auto-generated method stub

	this.restree = resTree2;
}

public void setMapper(Mapper mapper) {
	// TODO Auto-generated method stub

	this.mapper = mapper;
}
	

public xFirFileParser(xAclFunc xaclfunc) {
	// TODO Auto-generated constructor stub

	this.xaclfunc = xaclfunc;
}


public xReqNode searchResolutionTree(List xReqList){
	
	if (xReqList == null) return null;
	if (xReqList.size()== 1) {return (xReqNode) xReqList.get(0);}
	
	xReqNode xreq = null;
	
 
	// locate Req Information in Resolution Tree

  /*	
	for (int i=0; i < xReqList.size();i++){
	xreq = (xReqNode) xReqList.get(i);
	xreq.printReqInfo();
	}
 */

	for (int i=0; i < xReqList.size();i++){
		xreq = (xReqNode) xReqList.get(i);
		

		xreq = restree.locateFindxReqNode(restree.getRootR(), xreq);

		if (xreq != null) {	
			restree.clearResTree(restree.getRootR());
			return xreq;}
	}

//		System.out.println("final..\n\n.");
		// try to find Req Information in Resolution Tree by Traverse...
		xreq = restree.getFinalReqNode(restree.getRootR());	
		
	return xreq;
	
}

public void queryDir_processing_pre (File requestDir, File ref){
	
//	System.out.println("--------1------");
	mapper.putMapperFromLogFile(ref);
//	System.out.println("--------2------");
	File[] Tarrequests = requestDir.listFiles();
	
	
	
long start = System.currentTimeMillis();	 	
 	
	for (int t = 0; t < Tarrequests.length; t++) {
		long localquery[][] = reqParser.parseSingleReq(Tarrequests[t]);
		reqFactory.add(localquery);
	}
	
	long stop = System.currentTimeMillis();	 	
	long time = stop - start;	 	
	System.out.println("Total Request Loading/Convert Time:: "+time+ "\n");
	BufferedWriter bw = null;
}
 
public void queryDir_processing (int OutType){

	xReqNode newReqNode;
	Set<Integer[]> origins = new HashSet<Integer[]>();
	ArrayList<xReqNode> reqNodeset = new ArrayList<xReqNode>();
	//ArrayList<xReqNode> reqNodesetTemp = new ArrayList<xReqNode>();
	//int arraySize=0;
	numOfSubrequest = 0;
	numOfRequests = reqFactory.size();
	
	int dec=-2;//feichen
	
	for (int t = 0; t < numOfRequests; t++)
	{
		reqNodeset.clear();
		origins.clear();
		
		long localquery[][] = (long[][]) reqFactory.get(t);
		numOfSubrequest = localquery.length;
		
		for (int k = 0; k < numOfSubrequest ; k++)
		{
			Set<Integer[]> org = new HashSet<Integer[]>();
			dec = xaclfunc.run_single_query(localquery[k], org);
			origins.addAll(org);
			Iterator<Integer[]> it = origins.iterator();
			while(it.hasNext())//while
			{
				Integer tmp[];
				tmp = it.next();//tmp[0] rule num, tmp[1] decision
				xReqNode xreq = new xReqNode(tmp[1], tmp[0]);//localquery[k],
				reqNodeset.add(xreq);
			}  
		}
		
		/*
		int strVal = -1;
		int state = 0; 		
		numOfSubrequest = numOfSubrequest + arraySize;			
		reqNodesetTemp.clear();
		for (int j = 0; j < arraySize ; j++)
		{
			newReqNode = (xReqNode)reqNodeset.get(j);
			strVal = ((xReqNode)reqNodeset.get(j)).getRuleNum();		
				
			if (mode == 0)
			{				
				ArrayList stateSet = xaclfunc.getStateHash(strVal);				
				if (stateSet == null){					
					newReqNode.put_xacml_rule_num(-1);
					reqNodesetTemp.add(newReqNode);
				}
				else
				{			 
					newReqNode.put_xacml_rule_num((Integer)stateSet.get(0));						
					reqNodesetTemp.add(newReqNode);						
					for (int si=1; si < stateSet.size(); si++)
					{
		//				xReqNode xreq88 = new xReqNode(query, decision, rule_num);
						xReqNode xreq88 = new xReqNode(newReqNode.get_query_by_long(), newReqNode.get_decision(), newReqNode.get_xacml_rule_num());
						xreq88.put_xacml_rule_num((Integer)stateSet.get(si));
						reqNodesetTemp.add(xreq88);						
						//newReqNode.put_xacml_rule_num((Integer)stateSet.get(si));
					}				
				}				
			}	// end mod == 0
			else
			{
				reqNodesetTemp.add(newReqNode);
			}
		}
		*/
			
		//if (mode == 0){							
			if (reqNodeset.size()== 0) 
			{
				newReqNode = null;
			}
			else if (reqNodeset.size()== 1) 
			{
	//				System.out.println("1");
				newReqNode = (xReqNode) reqNodeset.get(0);
			}
			else 
			{
				Collections.sort(reqNodeset, DISPLAYNAME_ORDER);
				newReqNode = searchResolutionTree(reqNodeset);
			}
				
			if (OutType == 1){
				xaclfunc.print_output(newReqNode);
			}
			else if (OutType == 2){
					// no write..
			}
		//}	// mode == 0
	}
//		System.out.println("numOfSubrequest"+numOfSubrequest);
}

/*
public void queryDir_processing (int OutType){

	xReqNode newReqNode;
	Set<Integer[]> origins = new HashSet<Integer[]>();
	ArrayList<xReqNode> reqNodeset = new ArrayList<xReqNode>();
	ArrayList<xReqNode> reqNodesetTemp = new ArrayList<xReqNode>();
	int arraySize=0;
	numOfSubrequest = 0;
	numOfRequests = reqFactory.size();
	
	for (int t = 0; t < reqFactory.size(); t++) 
	{
		reqNodeset.clear();
		long localquery[][] = (long[][]) reqFactory.get(t);
		arraySize = localquery.length;
		
		for (int k = 0; k <arraySize ; k++)
		{
			Set<Integer[]> org = new HashSet<Integer[]>();
			xaclfunc.run_single_query(localquery[k], org);
			origins.addAll(org);
			Iterator<Integer[]> it = origins.iterator();
			while(it.hasNext())
			{
				Integer tmp[];
				tmp = it.next();//tmp[0] rule num, tmp[1] decision
				xReqNode xreq = new xReqNode(tmp[1], tmp[0]);//localquery[k],
				reqNodeset.add(xreq);
			}  
		}
		
		int strVal = -1;
		int state = 0; 		
		numOfSubrequest = numOfSubrequest + arraySize;			
		reqNodesetTemp.clear();
		for (int j = 0; j < arraySize ; j++)
		{
			newReqNode = (xReqNode)reqNodeset.get(j);
			strVal = ((xReqNode)reqNodeset.get(j)).getRuleNum();		
				
			if (mode == 0)
			{				
				ArrayList stateSet = xaclfunc.getStateHash(strVal);				
				if (stateSet == null){					
					newReqNode.put_xacml_rule_num(-1);
					reqNodesetTemp.add(newReqNode);
				}
				else
				{			 
					newReqNode.put_xacml_rule_num((Integer)stateSet.get(0));						
					reqNodesetTemp.add(newReqNode);						
					for (int si=1; si < stateSet.size(); si++)
					{
		//				xReqNode xreq88 = new xReqNode(query, decision, rule_num);
						xReqNode xreq88 = new xReqNode(newReqNode.get_query_by_long(), newReqNode.get_decision(), newReqNode.get_xacml_rule_num());
						xreq88.put_xacml_rule_num((Integer)stateSet.get(si));
						reqNodesetTemp.add(xreq88);						
						//newReqNode.put_xacml_rule_num((Integer)stateSet.get(si));
					}				
				}				
			}	// end mod == 0
			else
			{
				reqNodesetTemp.add(newReqNode);
			}
		}
		
			
		if (mode == 0){							
			if (reqNodesetTemp.size()== 0) {
				newReqNode = null;}
			else if (reqNodesetTemp.size()== 1) {
	//				System.out.println("1");
				newReqNode = (xReqNode) reqNodesetTemp.get(0);}
			else {
				Collections.sort(reqNodesetTemp, DISPLAYNAME_ORDER);
				newReqNode = searchResolutionTree(reqNodesetTemp);
			}
				
			if (OutType == 1){
				// write..
				xaclfunc.print_output(newReqNode);
			}
			else if (OutType == 2){
					// no write..
			}
		}	// mode == 0
	}
//		System.out.println("numOfSubrequest"+numOfSubrequest);
}
*/

public void setreqParser(RequestParser reqParser) {
	// TODO Auto-generated method stub
	this.reqParser = reqParser;
}

}
