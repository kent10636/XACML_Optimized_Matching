/**
 * 
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

public class Mapper {

	private static final int IntegerVal = 0;

	private static final int List = 0;

	PrintStream outS = null;

	TargetFunc targetFunc;

	Func func;

	protected static Logger logger = Logger.getLogger("Mapper.class");

	int HashSize = 15;

	private ArrayList hashList;
	private ArrayList MultihashList;

	public Hashtable subjectHash = new Hashtable(HashSize);

	public  Hashtable resourceHash = new Hashtable(HashSize);

	public Hashtable actionHash = new Hashtable(HashSize);
	
	public Hashtable MultisubjectHash = new Hashtable(HashSize);
	public  Hashtable MultiresourceHash = new Hashtable(HashSize);	
	public Hashtable MultiactionHash = new Hashtable(HashSize);
	
	public Mapper(TargetFunc targetFunc, PrintStream outS, int HashSize) {

		this.targetFunc = targetFunc;
		this.outS = outS;
		this.HashSize = HashSize;

		subjectHash = new Hashtable(HashSize);
		resourceHash = new Hashtable(HashSize);
		actionHash = new Hashtable(HashSize);
		
		hashList = new ArrayList();
		MultihashList = new ArrayList();

		hashList.add(subjectHash);
		hashList.add(resourceHash);
		hashList.add(actionHash);
		
		MultihashList = new ArrayList();
		
	
		MultihashList.add(MultisubjectHash);
		MultihashList.add(MultiresourceHash);
		MultihashList.add(MultiactionHash);	 
		
		
	}
	
	public void MapperAddDefaultOtherValue(){
		subjectHash.put("Other subjects", (long)0);
		resourceHash.put("Other resources",(long) 0);
		actionHash.put("Other actions", (long) 0);			
	}


	private void printOutHashTable(Hashtable ht) {
		Enumeration e = ht.keys();

		String[] sorted = new String[ht.size()];

		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			
//				System.out.println(ht.get(key)+key);
			long value = (Long) ht.get(key);
			
			// System.out.println ("{ " + key + ", " + value + " }");
			int index = Integer.parseInt(Long.toString(value));
			if (index < sorted.length) {				
				sorted[index] = key;
			}

		}

		for (int i = 0; i < sorted.length; i++) {
//			System.out.println(i + " : " + sorted[i]);
			outS.println(i + " : " + sorted[i]);
		}

	}
	
	
	public String getHashElementsNum (Hashtable ht) {
		
		int multi=0;
		int single=0;
		
		Enumeration e = ht.keys();
		String[] sorted = new String[ht.size()];

		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			
//				System.out.println(ht.get(key)+key);
			long value = (Long) ht.get(key);
			
			// System.out.println ("{ " + key + ", " + value + " }");
			int index = Integer.parseInt(Long.toString(value));
			if (index < sorted.length) {				
				sorted[index] = key;
			}

		}

		for (int i = 0; i < sorted.length; i++) {
			
			if (sorted[i].startsWith("&&")){
				multi++;	
			} else{
				single++;	
			}
		}
		
		// substract Others...
		single--;
		String ss = "total :" + (multi + single) + "  single :" + single + "  multi : " + multi;
		return ss;

	}


	public void printOutHashList() {
		// TODO Auto-generated method stub
//		System.out.println("Subjects Set");
		outS.println("Subjects Set");
		printOutHashTable(this.subjectHash);

//		System.out.println("Resources Set");
		outS.println("Resources Set");
		printOutHashTable(this.resourceHash);

//		System.out.println("Actions Set");
		outS.println("Actions Set");
		printOutHashTable(this.actionHash);
	}

	public ArrayList getHashList() {
		return hashList;
	}

	public ArrayList getMultiHashList() {
		return MultihashList;
	}
	
	public void putEnumerateHashList(tnode tnode) {
		// TODO Auto-generated method stub

 
		
		
		// setting up mapper.......

		if (tnode.getType().equals("Rule")){
		putHashList(this.getHashList(), tnode.getTarget());
		}

		for (int i = 0; i < tnode.getChildrenNum(); i++) {
			putEnumerateHashList((tnode) tnode.getChild(i));
		}

	}

	/*
	 * private void getSingleAttrMapperInfo(ArrayList hashList2, Target target) { //
	 * TODO Auto-generated method stub }
	 */

	private void putHashList(ArrayList hashList, Target target) {
		// TODO Auto-generated method stub
		if (target == null) {
			System.out.println("This target is null");
			return;
		}

		putOneHashTable((Hashtable) hashList.get(0), target.getSubjects());
		putOneHashTable((Hashtable) hashList.get(1), target.getResources());
		putOneHashTable((Hashtable) hashList.get(2), target.getActions());

	}

	// put HashTable from a single Target's subject/resources/action List

	private void putOneHashTable(Hashtable hash_t, List list) {
		// TODO Auto-generated method stub

		if (targetFunc.AnyAttributesAllowable(list)) {
			return;
		}
		if (targetFunc.NoAttributesAllowable(list)) {
			return;
		}

		Iterator it = list.iterator();
		List<Long> sortHashnumAnd = new ArrayList();

		while (it.hasNext()) {
			List items = (List) (it.next());

			Iterator matchIterator = items.iterator();
			
			int count = 0;

			sortHashnumAnd.clear();
			while (matchIterator.hasNext()) {
				
				count++;
				TargetMatch tm = (TargetMatch) (matchIterator.next());

				String str = func.deleteStrAttributeQuot(tm.getMatchValue()
						.toString().trim())
						+ " : "
						+ ((AttributeDesignator) tm.getMatchEvaluatable())
								.getId().toString().trim();

				long index = putString2OneHashTable(hash_t, str);
				
				
				if (index != -1 && !sortHashnumAnd.contains(index)) {sortHashnumAnd.add(index);}
				else {
					count--;
				}

			}
			if (count > 1){
				Collections.sort(sortHashnumAnd);
				Iterator iter = sortHashnumAnd.iterator();
				String str = "";
// hwang				
				while (iter.hasNext()) {
					Long tm = (Long) (iter.next());
					str = str + "&&" + tm;
				}
				if (!hash_t.containsKey(str)) {
					long value = hash_t.size();
					// System.out.println("Input: " +str + ": " +value);
//					System.out.println("multi: "+str + " size"+sortHashnumAnd.size());
					hash_t.put(str, value);
				}
			}
			
		}
		return;
	}
	
	// return -1 : error
	// number : hash number..
	public long putString2OneHashTable(Hashtable hash_t, String str) {
		// TODO Auto-generated method stub

		if (str == null || str.equals("")) {
			return -1;
		}
				if (!hash_t.containsKey(str)) {
					long value = hash_t.size();
					// System.out.println("Input: " +str + ": " +value);
					hash_t.put(str, value);
					return value;
				} else {
				
					return (Long) hash_t.get(str);
				}
//		return -1;
	}

	private String mapTarget2Fwr(ArrayList hashList, Target target) {
		// TODO Auto-generated method stub

		if (target == null) {
			System.out
					.println(" TargetFunc:: mappingRule : The target is null !!");
			return null;
		}

		String str1 = mapOneEachAttr2Fwr("subjects", (Hashtable) hashList
				.get(0), target.getSubjects());
		String str2 = mapOneEachAttr2Fwr("resources", (Hashtable) hashList
				.get(1), target.getResources());
		String str3 = mapOneEachAttr2Fwr("actions",
				(Hashtable) hashList.get(2), target.getActions());

//		 System.out.println("returned  "+str1+str2+str3);
		return (str1 + str2 + str3);

	}

	public void mapEnumerateOnlyRule2Fwr(tnode tnode) {
		// TODO Auto-generated method stub

		if (tnode.getType().equals("Rule")) {

			String Numericrule = mapTarget2Fwr(getHashList(), tnode.getTarget());
//			System.out.println("numerical rule--"+Numericrule);
			
			
			
			List aa = null;
			if (Numericrule != null){
			aa = func.conv(Numericrule, "Merge");}

 
//				System.out.println(aa + "null check of aa");
	 
			
			if (aa != null) {
				for (int i = 0; i < aa.size(); i++) {

					trule newRule = new trule((String) aa.get(i), tnode
							.getEffect(), tnode.getSeqNum());
					tnode.addRule(tnode.get_numericRulesNum(), newRule);


				}
			}

		}

		for (int i = 0; i < tnode.getChildrenNum(); i++) {
			mapEnumerateOnlyRule2Fwr((tnode) tnode.getChild(i));
		}

	}

	private String mapOneEachAttr2Fwr(String string, Hashtable hash_t, List list) {
		// TODO Auto-generated method stub

		// any wrong..

		if (targetFunc.AnyAttributesAllowable(list)) {
			/*
			 * String subr = "[Any" + string; for (int i=0; i < hash_t.size();
			 * i++){ } subr = subr+ "]"; return subr;
			 */
			return "[Any" + string + "]";

		}
		if (targetFunc.NoAttributesAllowable(list)) {
			return "[No" + string + "]";
		}

		String r = "[";

		Iterator it = list.iterator();
		List<Long> sortHashnumAnd = new ArrayList();
		
		
		while (it.hasNext()) {
			List items = (List) (it.next());
			Iterator matchIterator = items.iterator();
			
			int count = 0;
			
//			System.out.println("items size "+items.size());
			sortHashnumAnd.clear();
			
			while (matchIterator.hasNext()) {
				count++;
				TargetMatch tm = (TargetMatch) (matchIterator.next());
				String str = func.deleteStrAttributeQuot(tm.getMatchValue()
						.toString().trim())
						+ " : "
						+ ((AttributeDesignator) tm.getMatchEvaluatable())
								.getId().toString().trim();
				
				long index = mapOneStr2key(hash_t, str);

				if (index != -1 && !sortHashnumAnd.contains(index)) {sortHashnumAnd.add(index);}
				else {
					count--;
				}
			}
			
			if (count == 1){
				long Num = sortHashnumAnd.get(0);	
				if (r.equals("[")) {
					r = r + Num;
				} else {
					r = r + "," + Num;
				}
			}
			
			if (count > 1){
				Collections.sort(sortHashnumAnd);
				Iterator iter = sortHashnumAnd.iterator();
				String str = "";
				
				while (iter.hasNext()) {
					long tm = (Long) (iter.next());
					str = str + "&&" + tm;
				}
				long Num = (Long) hash_t.get(str);
				if (r.equals("[")) {
					r = r + Num;
				} else {
					r = r + "," + Num;
				}
			}

		}
		r = r + "]";
		return r;
	}

	public long mapOneStr2key(Hashtable hash_t, String str) {
		// TODO Auto-generated method stub

		if (str == null || str.equals("")) {
			return 0;
		}
		long Num = 0;
		if (hash_t.containsKey(str))
		Num = (Long) hash_t.get(str);
		
		return Num;
	}
	
	
	public void setFunc(Func func) {
		// TODO Auto-generated method stub
		this.func = func;

	}

	public void putMapperFromLogFile(File logFile) {
		// TODO Auto-generated method stub
		BufferedReader br = func.getBufferedReader(logFile);

		if (br == null) {
			System.out.println("Set up reference failed ... check file name");
		}

		String line = func.readNextLine(br);

		// flag -1 : none , flag 0 : subject, flag 1 : resor, flag 2 : actoins
		int flag = -2;
		int index = 0;

		while (line != null) {

			// flag setting

			String trimline = line.trim();

			if (trimline.startsWith("####Element Set Start#####")) {
				flag = -1;
			} else if (flag == -1 && trimline.startsWith("Subjects Set")) {
				flag = 0;
			} else if (flag == 0 && trimline.startsWith("Resources Set")) {
				flag = 1;
			} else if (flag == 1 && trimline.startsWith("Actions Set")) {
				flag = 2;
			}

			if (flag == 2 && trimline.startsWith("####Element Set Endt#####")) {
				break;
			}

			// seeting end

			if (flag >= 0 && flag <=2) {

//				System.out.println("a" + trimline);

				String partialline = null;
				String element = null;
				
				int startpoint = line.indexOf(":");

				if (startpoint != -1) {
					element = line.substring(startpoint + 1, line.length())
							.trim();

					String valueStr = line.substring(0, startpoint).trim();
					long value = Long.parseLong(valueStr);

//					System.out.println(element + " "+valueStr);
					((Hashtable) hashList.get(flag)).put(element, value);
	
					
// ArrayList input ...					
					if (element.startsWith("&&")){
						String split[] = element.split("\\&&");
						List seqNum = new ArrayList();
						
						;
						long hashkey = Long.parseLong(split[1]);
						
						for (int i=1; i < split.length; i++){
							
							seqNum.add(Long.parseLong(split[i]));
						}
						
						List getTopList = (List)((Hashtable) MultihashList.get(flag)).get(hashkey);
						
						if (getTopList == null){
							getTopList = new ArrayList();
				
							((Hashtable) MultihashList.get(flag)).put(hashkey, (List) getTopList);
							
						}
						
						getTopList.add(seqNum);
				}
					
				}
				
				
			}

			line = func.readNextLine(br);
		}

		
		
		
//		this.printOutHashList();
		
		
		
	}

}
