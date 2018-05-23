/*
 * Created on Nov 8, 2005
 *
 */
package reqGen.ncsu.xacml;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import reqGen.com.sun.xacml.AbstractPolicy;
import reqGen.com.sun.xacml.Policy;
import reqGen.com.sun.xacml.PolicyReference;
import reqGen.com.sun.xacml.PolicySet;
import reqGen.com.sun.xacml.PolicyTreeElement;
import reqGen.com.sun.xacml.Rule;
import reqGen.com.sun.xacml.Target;
import reqGen.com.sun.xacml.TargetMatch;
import reqGen.com.sun.xacml.attr.AttributeDesignator;
import reqGen.com.sun.xacml.attr.AttributeValue;
import reqGen.com.sun.xacml.cond.Apply;
import reqGen.com.sun.xacml.cond.Evaluatable;
import reqGen.com.sun.xacml.ctx.RequestCtx;


/**
 * @author eemartin
 *
 */
public abstract class RequestFactory implements RequestFactoryIntf {

	public static Logger logger = Logger.getLogger(RequestFactory.class);
	
	// AttributeDesignator.getID().toString() -> AttributeValue for each 
	protected Hashtable subjectAtts;
	protected Hashtable resourceAtts;
	protected Hashtable actionAtts;
	// encoded AttributeValue -> AttributeDesignator.getID().toString()
	protected Hashtable revSubjectAtts;
	protected Hashtable revResourceAtts;
	protected Hashtable revActionAtts;
	
	public abstract RequestCtx nextRequest() throws Exception;
	public abstract boolean hasNext();
	
	public RequestFactory() {
		// just to resolve compilation errors
	}
	
	public RequestFactory(Set p) {
		subjectAtts = new Hashtable();
		resourceAtts = new Hashtable();
		actionAtts = new Hashtable();
		revSubjectAtts = new Hashtable();
		revResourceAtts = new Hashtable();
		revActionAtts = new Hashtable();
		loadPolicies(p);
	}
	
	private void loadPolicies(Set p) {
		logger.debug("Loading policies...");
		subjectAtts.clear();
		resourceAtts.clear();
		actionAtts.clear();
		revSubjectAtts.clear();
		revResourceAtts.clear();
		revActionAtts.clear();
		Iterator it = p.iterator();
		while (it.hasNext()) {
			Object o = it.next();
			AbstractPolicy policy = (AbstractPolicy) o;
			processPolicyTreeElements(policy, null);
		}
	}
	
	private void processPolicyTreeElements(PolicyTreeElement pe, PolicyTreeElement parentPE) {
		if (pe instanceof reqGen.com.sun.xacml.Rule) {
			logger.debug("Rule# " + pe.getId().toString());
			Rule r = (Rule)pe;
			
			// handle rule target
			Target rule_target = r.getTarget();
			if (rule_target != null) {
				processTargetMatchList(rule_target.getActions());
				processTargetMatchList(rule_target.getResources());
				processTargetMatchList(rule_target.getSubjects());
			}
			
			// TODO handle condition
			Apply condition = r.getCondition();
			if (condition != null) {
				logger.debug("Condition# " + pe.getId().toString()+"#" + condition.getFunction().getIdentifier().toString());
				processApplyList(condition.getChildren(), null);
			}
			return;
		} else if (pe instanceof reqGen.com.sun.xacml.Policy) {
			Policy p = (Policy) pe;
			logger.debug("Processing policy# " + p.getId().toString());
			Target t = p.getTarget(); // target of the policy
			if (t != null) {
				processTargetMatchList(t.getActions()); 
				processTargetMatchList(t.getResources());
				processTargetMatchList(t.getSubjects());
			}
		} else if (pe instanceof PolicySet) {
			PolicySet p = (PolicySet) pe;
			logger.debug("Processing policy set " + p.getId().toString());
			Target t = pe.getTarget();
			if (t != null) {
				processTargetMatchList(t.getActions()); 
				processTargetMatchList(t.getResources());
				processTargetMatchList(t.getSubjects());
			}
		} else if (pe instanceof PolicyReference) {
			PolicyReference ref = (PolicyReference) pe;
			processPolicyTreeElements(ref.resolvePolicy(), parentPE);
		} else {
			logger.warn("Wrong policy tree elements: " + pe.toString() + "!!!!");
		}
		
		List children = pe.getChildren();
		Iterator itChildren = children.iterator();
		while (itChildren.hasNext()) {
			PolicyTreeElement element = (PolicyTreeElement)(itChildren.next());
			processPolicyTreeElements(element, pe);
		}	
	}
	
	private void processTargetMatchList(List l) {
		if (l == null) {
			return;
		}
		Iterator iter1 = l.iterator();
		while(iter1.hasNext()) {
			Iterator iter2 = ((List) iter1.next()).iterator();
			while (iter2.hasNext()) {
				TargetMatch tm = (TargetMatch) iter2.next();
				Hashtable ht = null;
				switch (tm.getType()) {
				case TargetMatch.ACTION:
					logger.debug("Action# ");
					ht = actionAtts;
					break;
				case TargetMatch.RESOURCE:
					logger.debug("Resource# ");
					ht = resourceAtts;
					break;
				case TargetMatch.SUBJECT:
					logger.debug("Subject# ");
					ht = subjectAtts;
					break;
				default:
					logger.error("Unknown TargetMatch type.");
					continue;
				}
				if (ht != null) {
					Evaluatable e = tm.getMatchEvaluatable();
					if (e instanceof AttributeDesignator) {
						AttributeDesignator ad = (AttributeDesignator) e;
//						Tuple id = new Tuple(ad.getId().toString(), ad.getType().toString());
//						logger.debug("id# " + id);
						addToSet(ht, ad.getId().toString(), tm.getMatchValue());
						
					} else {
						logger.warn("What is e? " + e.getClass().toString());
					}
				} else {
					logger.error("How did I get here?");
				}
			}
		}
	}
	
	private void addToSet(Hashtable ht, String id, AttributeValue val) {
		logger.debug("Adding: " + id + "->" + val.encode());
		if (ht == null || id == null || val == null) {
			logger.warn("Error adding to set.", new Exception("NPE"));
			return;
		} 
		if (ht.containsKey(id)) {
			HashSet set = (HashSet) ht.get(id);
			set.add(val);
		} else {
			HashSet set = new HashSet();
			set.add(val);
			ht.put(id, set);
		}
		addToReverseSet(getReverseHashtable(ht), val.encode(), id);
	}
	
	private void addToReverseSet(Hashtable ht, String attrValEncoded, String id) {
		if (ht == null || attrValEncoded == null || id == null) {
			logger.warn("Error adding to set.", new Exception("NPE"));
			return;
		}
		if (ht.containsKey(attrValEncoded)) {
			HashSet set = (HashSet) ht.get(attrValEncoded);
			set.add(id);
		} else {
			HashSet set  = new HashSet();
			set.add(id);
			ht.put(attrValEncoded, set);
		}
	}
	
	private Hashtable getReverseHashtable(Hashtable ht) {
		if (ht == subjectAtts) {
			return revSubjectAtts;
		} else if (ht == resourceAtts) {
			return revResourceAtts;
		} else if (ht == actionAtts) {
			return revActionAtts;
		} else {
			logger.warn("Error getting reverse hashtable.", new Exception(""));
			return new Hashtable();
		}
	}
	
	private void processApplyList(List l, AttributeDesignator designator) {
		if (l.size() == 0) {
			// empty set
			return;
		}
		if (designator != null) {
			int i = 0;
			while (i < l.size() && !(l.get(i) instanceof AttributeDesignator)) {
				if (l.get(i) instanceof Apply) {
					// tunnel deeper
					processApplyList(((Apply) l.get(i)).getChildren(), designator);
				} else if (l.get(i) instanceof AttributeValue) {
					// found a value
					AttributeValue val = (AttributeValue) l.get(i);	
					Hashtable ht = getTableByDesignator(designator);
					if (ht != null) {
						addToSet(ht, designator.getId().toString(), val);
					}
				} else {
					logger.info("what do I have? " + l.get(i).getClass());
				}
				i++;
			}
			processApplyList(l.subList(i, l.size()), null);
		} else if (l.get(0) instanceof AttributeDesignator) {
			// found an id
			AttributeDesignator ad = (AttributeDesignator) l.get(0);
			
			processApplyList(l.subList(1, l.size()), ad);
		} else if (l.get(0) instanceof Apply) {
			// tunnel deeper
			for (int i = 0; i < l.size(); i++) {
				processApplyList(((Apply) l.get(0)).getChildren(), null);
			}
		}	
	}
	
	private Hashtable getTableByDesignator(AttributeDesignator d) {
		switch (d.getDesignatorType()) {
		case AttributeDesignator.ACTION_TARGET:
			return actionAtts;
		case AttributeDesignator.RESOURCE_TARGET:
			return resourceAtts;
		case AttributeDesignator.SUBJECT_TARGET:
			return subjectAtts;
		}
		logger.warn("No table found for designator " + d.getDesignatorType());
		return null;
	}
	
	public String[] subjects() {
		Object[] objArr = revSubjectAtts.keySet().toArray();
		String[] strArr = new String[objArr.length];
		for (int i = 0; i < strArr.length; i++) {
			strArr[i] = (String) objArr[i];
		}
		return strArr;
	}
	
	public String[] resources() {
		Object[] objArr = revResourceAtts.keySet().toArray();
		String[] strArr = new String[objArr.length];
		for (int i = 0; i < strArr.length; i++) {
			strArr[i] = (String) objArr[i];
		}
		return strArr;
	}
	
	public String[] actions() {
		Object[] objArr = revActionAtts.keySet().toArray();
		String[] strArr = new String[objArr.length];
		for (int i = 0; i < strArr.length; i++) {
			strArr[i] = (String) objArr[i];
		}
		return strArr;
	}
	
//	public Collection subjectCollection() {
//		Vector v = new Vector();
//		Object[] keys = subjectAtts.keySet().toArray();
//		for (int i = 0; i < keys.length; i++) {
//			v.add(keys[i]);
//		}
//		return v;
//	}
//	
//	public Collection resourceCollection() {
//		Vector v = new Vector();
//		Object[] keys = resourceAtts.keySet().toArray();
//		for (int i = 0; i < keys.length; i++) {
//			v.add(keys[i]);
//		}
//		return v;
//	}
//	
//	public Collection actionCollection() {
//		Vector v = new Vector();
//		Object[] keys = actionAtts.keySet().toArray();
//		for (int i = 0; i < keys.length; i++) {
//			v.add(keys[i]);
//		}
//		return v;
//	}
		
//	class Tuple {
//		private String id;
//		private String type;
//		private String descrip;
//		
//		public Tuple(String id, String type) {
//			this.id = id;
//			this.type = type;
//			this.descrip = "(id# " + id + ",type# " + type + ")";
//		}
//		
//		public String getId() {
//			return id;
//		}
//		
//		public String getType() {
//			return type;
//		}
//		
//		public String toString() {
//			return descrip;
//		}
//
//		public boolean equals(Object obj) {
//			if (obj.getClass() == this.getClass()) {
//				return this.toString().equals(obj.toString());
//			} else {
//				return false;
//			}
//		}
//
//		public int hashCode() {
//			return toString().hashCode();
//		}
//	}
}
