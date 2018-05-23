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

public class RequestParser {

	Func func ;
	Mapper mapper;
	

	public RequestParser(Func func, Mapper mapper) {
		this.func = func;
		this.mapper = mapper;
	}
 

	public void searchReqsDir (File requestDir, PrintStream outS) {

		System.out.println("Fwr alreay converted is not working now. see requestparser java file..");
		
/*		
		File[] Tarrequests = requestDir.listFiles();
		long nnn[][];
		String str="";

//		Tarrequests.length
 		for (int t = 0; t < 5; t++) {
// 	 		for (int t = 0; t < Tarrequests.length ; t++) {
// 	 			for (int t = 0; t < 1000; t++) {
 			str="";
 			nnn = parseSingleReq (Tarrequests[t]);	
 			
 			
 			for (int j = 0 ; j < nnn.length ; j++){
 				
 				if (!str.equals("")){str=str+"&";}
 				str= str+nnn[j][0] +","+ nnn[j][1]+","+nnn[j][2];
 			}
 			
			System.out.println(t+" : "+ Tarrequests[t].getName() +" : "+str);
 			outS.println(t+" : "+ Tarrequests[t].getName() +" : "+ nnn.length +" : "+str);
		}
*/						
	}

	private void parse_getMatchNumList(BufferedReader br, List subjects, List resources, List actions) {
		// TODO Auto-generated method stub

		String line = null;
		List selectedList = null;
		Hashtable selectedHash = null;
				
	
		// input initial value..........
		/*
		((List) mapper.getHashList().get(0)).add(0);
		((List) mapper.getHashList().get(1)).add(0);
		((List) mapper.getHashList().get(2)).add(0);
		*/
/*
		subjects.add((long)0);
		resources.add((long)0);
		actions.add((long)0);
*/		
		
		// flag  0 : off , 0 : subjects, 1 : resources, 2 : actions
		int flag = -1;
				
		try {
			line = br.readLine().trim();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		
		while ( line!= null ){
	    	
		 
				if (line.contains("<Subject")){
					flag = 0;
				}else if (line.contains("</Subject>")){
					flag = -1;	
				}else if (line.contains("<Resource>")){
					flag = 1;
				}else if (line.contains("</Resource>")){
					flag = -1;
				}else if (line.contains("<Action>")){
					flag = 2;
				}else if (line.contains("</Action>")){
					flag = -1;
				}
			
 
			
			if (line.contains("<AttributeValue>") && line.contains("AttributeId") ){
	    		
				String atrval = "<AttributeValue>";
	    		int sp = line.indexOf("<AttributeValue>") + atrval.length();
	    		int lp = line.indexOf("</AttributeValue>");				
	    		 

	    		String atrval2 = "AttributeId=";
	    		int sp2 = line.indexOf("AttributeId=") + atrval2.length();
	    		int lp2 = line.indexOf("\"", sp2+1);
	    		
	    		
	    		String element = line.substring(sp, lp) + " : " + line.substring(sp2+1, lp2);
	    		element = element.trim();
	    		
	  		if (!element.startsWith("DEFAULT ")){
	    			
	    			switch (flag){
	    			
	    			case 0: selectedList =  subjects ; 
	    			selectedHash =(Hashtable) mapper.getHashList().get(0) ;
	    			break;	    				
	    			case 1:  selectedList =  resources ; 
	    			selectedHash = (Hashtable) mapper.getHashList().get(1) ;
	    			break; 
	    			case 2:   selectedList =  actions ; 
	    			selectedHash = (Hashtable) mapper.getHashList().get(2) ;
	    			break; 
	    			default : break;	
	    			
	    			}
	    			
//	    			System.out.println("Subjects: " + flag + line + element);
	    //			System.out.println(element);
	    			
	    			
	    			
//	    			System.out.println("element : "+element);
	    			
	    		 
	    			long elementNum = (long) mapper.mapOneStr2key(selectedHash, element);
	    			
	    			
	    			
	   // 			long elementNum = (long)(Integer) mapper.mapOneStr2key(selectedHash, "&&1&&2");
	   // 			System.out.println("element Num "+ flag +" "+ element +" " +elementNum);
	    			
	    			
//	    			int elementNum = (Integer) selectedHash.get(element);
//	    			long elementNum = (Long) selectedHash.get(element);
	    			
//	    			System.out.println(selectedHash.get(element));
		    		if (!selectedList.contains(elementNum)){
	    			
		    			selectedList.add(elementNum);
		    		}	    		
		    		
	    		} // if (!element.startsWith("DEFAULT ") 
			}
			
	    	try {
				line = br.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	}
		
		if (subjects.size()==0){subjects.add((long)0);}
		if (resources.size()==0){resources.add((long)0);}	    	
		if (actions.size()==0){actions.add((long)0);}
		}

	private List getOnlyMatchedList(List s, List t, Hashtable h) {
		
		List newAddedList = new ArrayList();
		for (int i=0; i < t.size(); i++){
			
			List t2 = (List)t.get(i);
			
			int flagged = 1;
			String str="";
			// j=0 is already checked..
			for (int j=0 ; j < t2.size(); j++){
				
//				System.out.println("s "+s + " t"+t2.get(j));
				if (!s.contains(t2.get(j))){
					flagged = 0; break;
				}
				str = str + "&&"+t2.get(j);
			}
			
//			System.out.println("flagged"+flagged);
			if (flagged == 1){
				
				
				Long val = (Long) h.get(str);
				if (val != null){
					s.add(val);
//					System.out.println("Matched Multi str"+str);
//					System.out.println(s);
				}
//				newAddedList.add(str);
			}
			
		}
		
		return newAddedList;

	}
	
	private void chk_MultiHashListAndAdd(List subjects, List resources, List actions) {
	
//		System.out.println("chk_MultiHashListAndAdd(");
		
//		System.out.println(subjects);
//		System.out.println(resources);
//		System.out.println(actions);
		Hashtable selectedHash = null;
		Hashtable selectedMultiHash = null;
		List selectedList = null;
		
		for (int selected=0; selected < 3; selected++){

			selectedHash = (Hashtable) mapper.getHashList().get(selected) ;
			selectedMultiHash = (Hashtable) mapper.getMultiHashList().get(selected) ;			
			
			switch (selected){
				case 0: selectedList = subjects; break;
				case 1: selectedList = resources; break;
				case 2: selectedList = actions; break;
			default : break;
			}
			
			Collections.sort(selectedList);
//			System.out.println("---not null"+selectedList);
			
			if (selectedMultiHash != null){
				
				for (int i=0; i < selectedList.size(); i++){

					Long kkk = (Long) selectedList.get(i);
//					System.out.println(kkk);
					if (kkk != null){
					List IndMultiHash = (List) selectedMultiHash.get(kkk);
					
					// compare value to set
						if (IndMultiHash != null){
							
							// getAddedList && && format...
							List getAddedList = getOnlyMatchedList(selectedList, IndMultiHash, selectedHash);
//							System.out.println("---not null"+ selectedList.get(i) +IndMultiHash + "--"+getAddedList);
							
							/*
							for (int j=i+1; j < selectedList.size(); j++){
							
								
								
								System.out.println("---not null"+ selectedList.get(i) +IndMultiHash);
								
							}
							*/
							
							
						}
					}
					
					

					
				}
			}
			
		}
		

		
		
		for (int i=0; i < selectedList.size(); i++){
			
//			long value = selectedList.get(i);
		
//		selectedMultiHash.get(key)
//		System.out.println(actions);
		}
		
	}
	
	



	public long[][] parseSingleReq (File reqFile) {

		List subjects = new ArrayList();
	    List resources = new ArrayList();
	    List actions = new ArrayList();

	    if (func == null) System.out.println("Ooops..func is null in Request Parser");
		BufferedReader br = func.getBufferedReader(reqFile) ;
		
		parse_getMatchNumList(br, subjects, resources, actions);
		chk_MultiHashListAndAdd (subjects, resources, actions);

		
// Any subjects not ready for...
		
// if size == 0 added 0 since if no elements in it should be consumed in any

		if (subjects.size() == 0){subjects.add((long)0);}
		if (resources.size() == 0){resources.add((long)0);}
		if (actions.size() == 0){actions.add((long)0);}	
//
		
		int LongSize = subjects.size() * resources.size() * actions.size();
		
		long[][] query = new long[LongSize][3];

		
//		System.out.println("reqparser in common : longsize " + LongSize);
		
		
		
		int fillerIndex = 0;
		
		for (int i=0; i < subjects.size() ; i++){
			
			for (int j=0; j < resources.size() ; j++){
				
				for (int k=0; k < actions.size() ; k++){
					
//					System.out.println(LongSize+" "+ i + subjects.get(i).toString());
					
					query[fillerIndex][0] = (Long) subjects.get(i);
					query[fillerIndex][1] = (Long) resources.get(j);					
					query[fillerIndex][2] = (Long) actions.get(k);

					fillerIndex++;
					
				}				
				
			}			
			
		}
		
//		System.out.println(fillerIndex);
		
//		long[][] query2 = new long[10][3];
//		System.out.println(query2.length);
// Long Array Converter................hmhm........		
		
//		matchingInHashMap(subjects, resources, actions);
		
//		printListedAttr(subjects, resources, actions);
	 		
		return query;
 
//		return null;
	}
	
}
