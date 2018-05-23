// Update SoD request.. 


package reqGen.ncsu.xacml;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import reqGen.com.sun.xacml.EvaluationCtx;
import reqGen.com.sun.xacml.attr.AttributeValue;
import reqGen.com.sun.xacml.attr.StringAttribute;
import reqGen.com.sun.xacml.ctx.Attribute;
import reqGen.com.sun.xacml.ctx.RequestCtx;
import reqGen.com.sun.xacml.ctx.Subject;

public class AllComboReqFactory extends RequestFactory {
	
	protected BitSet map;
		
	protected long count;
	
	protected long maxCount;
		
	private Vector s, r, a;
	
	// if 1 ; SoD mode
	// if 2 ; nonSoD mode
	// if 3 ; control number of subjects, objects, actions..
	
	public static int SoDmode = 1;
	public static int numOfsubjects = 0;	
	public static int numOfresources = 0;	
	public static int numOfactions = 0;
	
	static public void setReqParameter(int mode, int subs, int res, int acts) {
		SoDmode = mode;
		numOfsubjects = subs;
		numOfresources = res;
		numOfactions = acts;
	}
	
	public AllComboReqFactory(Set policies) {
		this(policies, 0, 0);
	}

	public AllComboReqFactory(Set policies, long start, long stop) {
		// load policies
		super(policies);
		// init members
		map = new BitSet();
		map.clear();
		count = start;
		s = new Vector();
		r = new Vector();
		a = new Vector();
		processHashtable(subjectAtts, s);
		processHashtable(resourceAtts, r);
		processHashtable(actionAtts, a);
		// compute max count
		if (stop == 0) {
			maxCount = (long) Math.ceil(Math.pow(2, lengthOfMap()));
		} else {
			maxCount = stop;
		}
	}
	
	protected int lengthOfMap() {
		return s.size() + r.size() + a.size();
	}
	
	public boolean hasNext() {
		return count < maxCount;
	}

	public RequestCtx nextRequest() throws Exception {
		RequestCtx request = null;
		try {
			if (SoDmode ==1) {
			Set reqSubj = getSubjectSet_SOD();
			Set reqRes = getResourceSet_SOD();
			Set reqAct = getActionSet_SOD();
			request = new RequestCtx(reqSubj, reqRes, reqAct, new HashSet());
			}else if (SoDmode ==2) {
				Set reqSubj = getSubjectSet_nonSOD();
				Set reqRes = getResourceSet_nonSOD();
				Set reqAct = getActionSet_nonSOD();
				request = new RequestCtx(reqSubj, reqRes, reqAct, new HashSet());
			}else if (SoDmode ==3) {				
				Set reqSubj = getSubjectSet_Numbered (numOfsubjects);
				Set reqRes = getResourceSet_Numbered(numOfresources);
				Set reqAct = getActionSet_Numbered(numOfactions);
				request = new RequestCtx(reqSubj, reqRes, reqAct, new HashSet());
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		updateMaps();
		return request;
	}

	private Set getSubjectSet_Numbered(int num) throws URISyntaxException {
		boolean hasSubjId = false;
		String subjIdURI = "urn:oasis:names:tc:xacml:1.0:subject:subject-id";		
		HashSet attributes = new HashSet();		
		BitSet subMap = map.get(0, s.size());
	
		Random rand = new Random();

		if (s.size() == 0){
		StringAttribute value = new StringAttribute("DEFAULT SUBJECT");
		attributes.add(new Attribute(new URI(subjIdURI), null, null, value));		
		HashSet newSubjects = new HashSet();
		newSubjects.add(new Subject(attributes));
		return newSubjects;	}
		
		
		List dupcheck = new ArrayList();
		if (s.size() < num ){
		System.out.println("error. The generated request - numbere of subjects is out of scope");
		return null;
		}
		
		for (int i=0; i < num; i++){
			int selected = rand.nextInt();
			if (selected < 0){selected = - selected;}
			selected = selected%s.size();
			
			if (!dupcheck.contains(selected)){
				dupcheck.add(selected);			
				Tuple t = (Tuple) s.get(selected);
				URI id = t.designator;
				AttributeValue value = t.value;
				attributes.add(new Attribute(id, null, null, value));
			} else {				
				i--;
			}
		}
		
		// bundle the attributes in a Subject with the default category    
		HashSet newSubjects = new HashSet();
		newSubjects.add(new Subject(attributes));
		return newSubjects;			
	}

	
	private Set getResourceSet_Numbered(int num) throws URISyntaxException {
		boolean hasResourceId = false;
		String resId = EvaluationCtx.RESOURCE_ID;		
		HashSet attributes = new HashSet();
		
  		
		Random rand = new Random();

		List dupcheck = new ArrayList();
		if (r.size() < num ){
		System.out.println("error. The generated request - numbere of subjects is out of scope");
		return null;
		}
		
		for (int i=0; i < num; i++){
			int selected = rand.nextInt();
			if (selected < 0){selected = - selected;}
			selected = selected%r.size();
			
			if (!dupcheck.contains(selected)){
				dupcheck.add(selected);			
				Tuple t = (Tuple) r.get(selected);
				URI id = t.designator;
				AttributeValue value = t.value;
				attributes.add(new Attribute(id, null, null, value));
				if (resId.equals(id.toString())) {
					hasResourceId = true;
				}
			} else {				
				i--;
			}
		}
		

		if (!hasResourceId) {
			StringAttribute value = new StringAttribute("DEFAULT RESOURCE");
			attributes.add(new Attribute(new URI(resId), null, null, value));
		}		

		return attributes;		
	}
	
	private Set getActionSet_Numbered(int num) throws URISyntaxException {	
		HashSet attributes = new HashSet();
		
		Random rand = new Random();
		List dupcheck = new ArrayList();
		if (a.size() < num ){
		System.out.println("error. The generated request - numbere of subjects is out of scope");
		return null;
		}
		
		for (int i=0; i < num; i++){
			int selected = rand.nextInt();
			if (selected < 0){selected = - selected;}
			selected = selected%a.size();
			
			if (!dupcheck.contains(selected)){
				dupcheck.add(selected);			
				Tuple t = (Tuple) a.get(selected);
				URI id = t.designator;
				AttributeValue value = t.value;
				attributes.add(new Attribute(id, null, null, value));
			} else {				
				i--;
			}
		}


		int start = s.size() + r.size();
		BitSet subMap = map.get(start, start + a.size());
		

		return attributes;
	}

	
	/**
	 * Sets up the Subject section of the request. This Request only has
	 * one Subject section, and it uses the default category. To create a
	 * Subject with a different category, you simply specify the category
	 * when you construct the Subject object.
	 *
	 * @return a Set of Subject instances for inclusion in a Request
	 *
	 * @throws URISyntaxException if there is a problem with a URI
	 */
	private Set getSubjectSet_SOD() throws URISyntaxException {
		boolean hasSubjId = false;
		String subjIdURI = "urn:oasis:names:tc:xacml:1.0:subject:subject-id";		
		HashSet attributes = new HashSet();		
		BitSet subMap = map.get(0, s.size());
	
		Random rand = new Random();

		int selected = rand.nextInt();


		
		if (selected < 0){selected = - selected;}

		// s.size means the total number of subjects
		// if not subject in policy, it should be Default Subject... OK?
		if (s.size() == 0){
		StringAttribute value = new StringAttribute("DEFAULT SUBJECT");
		attributes.add(new Attribute(new URI(subjIdURI), null, null, value));		
		HashSet newSubjects = new HashSet();
		newSubjects.add(new Subject(attributes));
		return newSubjects;	}
		
		selected = selected%s.size();
		
		
		
//		System.out.println(s.size());
//		System.out.println(selected);
		
		Tuple t = (Tuple) s.get(selected);
		URI id = t.designator;
		AttributeValue value = t.value;
		attributes.add(new Attribute(id, null, null, value));
		
		 /*
		for (int i = 0; i < s.size(); i++) {
			// show it or not?
			if (!subMap.get(i)) {
				continue;
			}
			Tuple t = (Tuple) s.get(i); 
			URI id = t.designator;
			AttributeValue value = t.value;
			attributes.add(new Attribute(id, null, null, value));
			if (subjIdURI.equals(id.toString())) {
				hasSubjId = true;
			}
		}
		if (!hasSubjId) {
			StringAttribute value = new StringAttribute("DEFAULT SUBJECT");
			attributes.add(new Attribute(new URI(subjIdURI), null, null, value));
		}
		 */
		
		// bundle the attributes in a Subject with the default category    
		HashSet newSubjects = new HashSet();
		newSubjects.add(new Subject(attributes));
		return newSubjects;			
	}
 
	private Set getSubjectSet_nonSOD() throws URISyntaxException {
		boolean hasSubjId = false;
		String subjIdURI = "urn:oasis:names:tc:xacml:1.0:subject:subject-id";		
		HashSet attributes = new HashSet();		
		BitSet subMap = map.get(0, s.size());
		for (int i = 0; i < s.size(); i++) {
			// show it or not?
			if (!subMap.get(i)) {
				continue;
			}
			Tuple t = (Tuple) s.get(i); 
			URI id = t.designator;
			AttributeValue value = t.value;
			attributes.add(new Attribute(id, null, null, value));
			if (subjIdURI.equals(id.toString())) {
				hasSubjId = true;
			}
		}
		if (!hasSubjId) {
			StringAttribute value = new StringAttribute("DEFAULT SUBJECT");
			attributes.add(new Attribute(new URI(subjIdURI), null, null, value));
		}

		// bundle the attributes in a Subject with the default category    
		HashSet newSubjects = new HashSet();
		newSubjects.add(new Subject(attributes));
		return newSubjects;			
	}
	 
	/**
	 * Creates a Resource specifying the resource-id, a required attribute.
	 *
	 * @return a Set of Attributes for inclusion in a Request
	 *
	 * @throws URISyntaxException if there is a problem with a URI
	 */
	private Set getResourceSet_SOD() throws URISyntaxException {
		boolean hasResourceId = false;
		String resId = EvaluationCtx.RESOURCE_ID;		
		HashSet attributes = new HashSet();
		
  		
		Random rand = new Random();
		
//		System.out.println( "r.size() " + r.size());
		{	
			int selected = rand.nextInt();
			if (selected < 0){selected = - selected;}
			selected = selected%r.size();
	
	//		System.out.println( "selected " + selected);		
			
			Tuple t = (Tuple) r.get(selected);
			URI id = t.designator;
			AttributeValue value = t.value;
			attributes.add(new Attribute(id, null, null, value));
			if (resId.equals(id.toString())) {
				hasResourceId = true;
			}
	    }

		if (!hasResourceId) {
			StringAttribute value = new StringAttribute("DEFAULT RESOURCE");
			attributes.add(new Attribute(new URI(resId), null, null, value));
		}		
		/*
		
		int start = s.size();
		BitSet subMap = map.get(start, start + r.size());
		for (int i = 0; i < r.size(); i++) {
			if (!subMap.get(i)) {
				continue;
			}
			Tuple t = (Tuple) r.get(i);
			URI id = t.designator;
			AttributeValue value = t.value;
			attributes.add(new Attribute(id, null, null, value));
			if (resId.equals(id.toString())) {
				hasResourceId = true;
			}
		}
		if (!hasResourceId) {
			StringAttribute value = new StringAttribute("DEFAULT RESOURCE");
			attributes.add(new Attribute(new URI(resId), null, null, value));
		}
		
		*/
		return attributes;		
	}

	/**
	 * Creates a Resource specifying the resource-id, a required attribute.
	 *
	 * @return a Set of Attributes for inclusion in a Request
	 *
	 * @throws URISyntaxException if there is a problem with a URI
	 */
 	private Set getResourceSet_nonSOD() throws URISyntaxException {
		boolean hasResourceId = false;
		String resId = EvaluationCtx.RESOURCE_ID;		
		HashSet attributes = new HashSet();
		int start = s.size();
		BitSet subMap = map.get(start, start + r.size());
		for (int i = 0; i < r.size(); i++) {
			if (!subMap.get(i)) {
				continue;
			}
			Tuple t = (Tuple) r.get(i);
			URI id = t.designator;
			AttributeValue value = t.value;
			attributes.add(new Attribute(id, null, null, value));
			if (resId.equals(id.toString())) {
				hasResourceId = true;
			}
		}
		if (!hasResourceId) {
			StringAttribute value = new StringAttribute("DEFAULT RESOURCE");
			attributes.add(new Attribute(new URI(resId), null, null, value));
		}
		return attributes;		
	}
 
	/**
	 * Creates an Action specifying the action-id, an optional attribute.
	 *
	 * @return a Set of Attributes for inclusion in a Request
	 *
	 * @throws URISyntaxException if there is a problem with a URI
	 */
	private Set getActionSet_SOD() throws URISyntaxException {	
		HashSet attributes = new HashSet();
		
		Random rand = new Random();
//		int selected = rand.nextInt()%a.size();
		int selected = rand.nextInt();
		if (selected < 0){selected = - selected;}
		selected = selected%a.size();
	 
		Tuple t = (Tuple) a.get(selected);
		URI id = t.designator;
		AttributeValue value = t.value;
		attributes.add(new Attribute(id, null, null, value));
	 
		
		int start = s.size() + r.size();
		BitSet subMap = map.get(start, start + a.size());
		
		/*
		for (int i = 0; i < a.size(); i++) {
			if (!subMap.get(i)) {
				continue;
			}
			Tuple t = (Tuple) a.get(i);
			URI id = t.designator;
			AttributeValue value = t.value;
			attributes.add(new Attribute(id, null, null, value));
		}
		*/
		return attributes;
	}
 
	private Set getActionSet_nonSOD() throws URISyntaxException {	
		HashSet attributes = new HashSet();
		int start = s.size() + r.size();
		BitSet subMap = map.get(start, start + a.size());
		for (int i = 0; i < a.size(); i++) {
			if (!subMap.get(i)) {
				continue;
			}
			Tuple t = (Tuple) a.get(i);
			URI id = t.designator;
			AttributeValue value = t.value;
			attributes.add(new Attribute(id, null, null, value));
		}
		return attributes;
	}
 	
	protected void updateMaps() {
		// increment count
		count++;
		// set map to count
		map.clear();
		char[] digits = (new StringBuffer(Long.toBinaryString(count))).reverse().toString().toCharArray();
		for (int i = 0; i < digits.length; i++) {
			if (digits[i] == '1') {
				map.set(i);
			}
		}
	}
	
	private void processHashtable(Hashtable t, Vector v) {
		v.removeAllElements();
		for (Enumeration keys = t.keys(); keys.hasMoreElements();) {
			String d = (String) keys.nextElement();
			HashSet valSet = (HashSet) t.get(d);
			for (Iterator i = valSet.iterator(); i.hasNext();) {
				AttributeValue val = (AttributeValue) i.next();
				try {
					v.add(new Tuple(d, val));
				} catch (Exception e) {
					logger.error("Invalid URI!!" , e);
				}
			}
		}
//		
//		Object[] oArr = tuples.toArray();
//		tArr = new Tuple[oArr.length];
//		for (int i = 0; i < tArr.length; i++) {
//			tArr[i] = (Tuple) oArr[i];
//		}
	}
	
	private class Tuple {
		
		URI designator;
		AttributeValue value;
		
		public Tuple(String d, AttributeValue v) throws Exception {			
			designator = new URI(d);
			value = v;
		}
		
		public String toString() {
			return designator.toString() + "->" + value.encode();
		}
	}
}