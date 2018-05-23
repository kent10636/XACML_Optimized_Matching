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

public class TargetFunc {

	protected static Logger logger = Logger.getLogger("TargetFunc.class");

	Func func ;	
	PrintStream outS = null;


	public TargetFunc(Func func, PrintStream outS) {
		this.func = func;
		this.outS = outS;
	}

	private List cloneArrayList(List list) {
		return Collections.unmodifiableList(new ArrayList(list));
	}


	
	private List getCommonAttributesInTwoLists(List list1, List list2){
		// TODO Auto-generated method stub
		
		list1 = Collections.synchronizedList(new ArrayList(list1)); 
		list2 = Collections.synchronizedList(new ArrayList(list2)); 
		
		Iterator it1 = list1.iterator();
		List matches = new ArrayList();
		List tmlist = new ArrayList();	
		

		while (it1.hasNext()) {

			List items1 = (List) (it1.next());
			Iterator it2 = list2.iterator();
			
			while (it2.hasNext()) {
				List items2 = (List) (it2.next());
				if (isSubsetOf(items1, items2)){
					it1.remove();
					it2.remove();
					matches.add(items2);						
				} else if (isSubsetOf(items2, items1)){
					it1.remove();
					it2.remove();
					matches.add(items1);
				} 
			}	
		}
		
		it1 = list1.iterator();

		while (it1.hasNext()) {

			List items1 = (List) (it1.next());
			Iterator it2 = list2.iterator();
			
			while (it2.hasNext()) {
				List items2 = (List) (it2.next());
				List tempList = new ArrayList();
				tempList.addAll(items1);
				tempList.addAll(items2);
				matches.add(tempList);
 
			}	
		}		

		return cloneArrayList(matches);

	}

	// isSubsetOf (List A, List B) B is subset of A
	
	private boolean isSubsetOf (List list1, List list2) {
		
		Iterator iter = list2.iterator(); // and
		while (iter.hasNext()) {
			TargetMatch tm = (TargetMatch) (iter.next());
			if (!containTargetMatch(list1, tm)) {
				return false;
			} 
		}
			return true;
	}

	private boolean containTargetMatch(List list, TargetMatch tm) {
		// TODO Auto-generated method stub\
		Iterator matchIterator = list.iterator();

			while (matchIterator.hasNext()) {
				TargetMatch tm2 = (TargetMatch) (matchIterator.next());
				if (isEqual(tm,tm2)) return true;			
			}

		return false;
	}
	
	private boolean isEqual(TargetMatch tm1, TargetMatch tm2) {
		// TODO Auto-generated method stub\

		if (tm1.getType() == tm2.getType()
				&& tm1.getMatchValue().toString().equals(
						tm2.getMatchValue().toString())
				&& ((AttributeDesignator) tm1.getMatchEvaluatable())
						.getId().toString().equals(
								((AttributeDesignator) tm2
										.getMatchEvaluatable()).getId()
										.toString())) {
			return true;
		}

		return false;
	}		
/*	
	private boolean containTargetMatch(List list, TargetMatch tm) {
		// TODO Auto-generated method stub\
		Iterator it = list.iterator();

		while (it.hasNext()) {
			List items = (List) (it.next());
			Iterator matchIterator = items.iterator();
			while (matchIterator.hasNext()) {
				TargetMatch tm2 = (TargetMatch) (matchIterator.next());

				if (tm.getType() == tm2.getType()
						&& tm.getMatchValue().toString().equals(
								tm2.getMatchValue().toString())
						&& ((AttributeDesignator) tm.getMatchEvaluatable())
								.getId().toString().equals(
										((AttributeDesignator) tm2
												.getMatchEvaluatable()).getId()
												.toString())) {
					return true;
				}

			}
		}

		return false;
	}
*/
	private List insersectAttribute(List arr1, List arr2) {
		// TODO Auto-generated method stub

		// hwang

		if (arr1 == null && arr2 == null) {
			return null;
		} else if (arr1 == null) {
			return cloneArrayList(arr2);
		} else if (arr2 == null) {
			return cloneArrayList(arr1);
		}
		return getCommonAttributesInTwoLists(arr1, arr2);

	}

	public Target insersectTarget(Target t1, Target t2) {

		if (t1 == null || t2 == null) {
			return null;
		}


		List subjects = insersectAttribute(t1.getSubjects(), t2.getSubjects());
		List resources = insersectAttribute(t1.getResources(), t2
				.getResources());
		List actions = insersectAttribute(t1.getActions(), t2.getActions());

		Target tg = new Target(subjects, resources, actions);
		


		return tg;

	}

	public void printTarget(Target target, int spaceGap) {
		// TODO Auto-generated method stub
		if (target == null) {
			System.out.println("This target is null");
			return;
		}

		printAttr("subjects", target.getSubjects(), spaceGap);
		printAttr("resources", target.getResources(), spaceGap);
		printAttr("actions", target.getActions(), spaceGap);
	}

	public boolean AnyAttributesAllowable(List list) {

		if (list == null) {
			return true;
		}

		return false;
	}

	public boolean NoAttributesAllowable(List list) {

		if (list == null) {
			return false;
		}

		if (list.size() == 0) {
			return true;
		}
		if (list.size() == 1) {
			if (((List) (list.get(0))).size() == 0)
				return true;
		}
		return false;

	}

	private void printAttr(String string, List list, int spaceGap) {
		// TODO Auto-generated method stub

		String indent = func.makeindent(spaceGap);
		if (AnyAttributesAllowable(list)) {
//			System.out.println(indent + "  + " + "Any " + string);
			outS.println(indent + "  + " + "Any " + string);
			return;
		}
		if (NoAttributesAllowable(list)) {
//			System.out.println(indent + "  + " + "No " + string);
			outS.println(indent + "  + " + "No " + string);
			return;
		}

		Iterator it = list.iterator();

		while (it.hasNext()) {
			List items = (List) (it.next());

			Iterator matchIterator = items.iterator();
			while (matchIterator.hasNext()) {
				TargetMatch tm = (TargetMatch) (matchIterator.next());

				String str = indent
						+ "  + "
						+ string
						+ ": "
						+ tm.getMatchValue().toString()
						+ " : "
						+ ((AttributeDesignator) tm.getMatchEvaluatable())
								.getId().toString();
//				System.out.print(str + "&&");
				outS.print(str);
				if (matchIterator.hasNext()) {outS.print(" && ");};
			}
//			System.out.println();
			outS.println();
		}
		return;
	}


 






}
