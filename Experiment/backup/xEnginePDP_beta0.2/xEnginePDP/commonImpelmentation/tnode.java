/**
 * @author Fei Chen
 * feichen@cse.msu.edu
 * Computer Science and Engineering
 * Michigan State University
 * 12/03/2008
 */

package commonImpelmentation;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sun.xacml.AbstractPolicy;
import com.sun.xacml.Policy;
import com.sun.xacml.PolicySet;
import com.sun.xacml.Target;

public class tnode {

	Func func;
	TargetFunc targetFunc;
	

	private int _left;
	
	private int _right;

	private ArrayList _children;

	private ArrayList _numericRules;

	private boolean _isLeaf;

	private String name;

	private String algorithm;

	private String type;

	private int effect;

	private int seq;

	private Target target;

	public tnode(Func func, TargetFunc targetFunc) {

		_children = new ArrayList();
		_numericRules = new ArrayList();
		this.func = func;
		this.targetFunc = targetFunc;

	}

	public void addChild(int i, tnode son) {
		_children.add(i, son);
	}

	public void addRule(int i, trule son) {
		_numericRules.add(i, son);
	}

	public ArrayList get_childrenList() {
		return _children;
	}

	public ArrayList get_numericRulesList() {
		return _numericRules;
	}

	public int get_numericRulesNum() {
		return _numericRules.size();
	}

	public String getAlg() {
		return algorithm;
	}

	public ArrayList getAllChild() {
		return _children;
	}

	public List getAlldenyRules() {
		List returnRule = new ArrayList();

		for (int i = 0; i < this.get_numericRulesNum(); i++) {
			trule trule = (trule) this.getRule(i);

			// Deny

			if (trule.getEffect() == 0) {
				returnRule.add(trule);
			}
		}
		return returnRule;
	}

	public List getAllPermitRules() {
		List returnRule = new ArrayList();

		for (int i = 0; i < this.get_numericRulesNum(); i++) {
			trule trule = (trule) this.getRule(i);

			// Permit

			if (trule.getEffect() == 1) {
				returnRule.add(trule);
			}
		}
		return returnRule;

	}

	public ArrayList getAllRules() {
		return _numericRules;
	}

	public tnode getChild(int i) {
		return (tnode) _children.get(i);
	}

	public int getChildrenNum() {
		return _children.size();
	}

	public List getDenyPermitSorted() {
		List returnRule = new ArrayList();
		returnRule.addAll(getAlldenyRules());
		returnRule.addAll(getAllPermitRules());
		return returnRule;
	}

	public int getEffect() {
		return this.effect;
	}

	public List getFirstApplicableSorted() {
		List returnRule = new ArrayList();

		for (int i = 0; i < this.get_numericRulesNum(); i++) {
			trule trule = (trule) this.getRule(i);
			returnRule.add(trule);
		}
		return returnRule;
	}

	public int getLeft() {
		return _left;
	}

	public String getName() {
		return name;
	}

	public List getPermitDenySorted() {
		List returnRule = new ArrayList();
		returnRule.addAll(getAllPermitRules());
		returnRule.addAll(getAlldenyRules());
//		return Collections.unmodifiableList(new ArrayList(returnRule));
		return returnRule;
	}

	public int getRight() {
		return _right;
	}

	public trule getRule(int i) {
		return (trule) _numericRules.get(i);
	}

	public int getSeqNum() {
		return this.seq;
	}

	public Target getTarget() {
		return this.target;
	}

	public String getType() {
		return type;
	}


	public void printOutNodeInfo(int spaceGap, PrintStream outS) {
		// TODO Auto-generated method stub

		String indent = func.makeindent(spaceGap);

		if (type.equals("PolicySet") || type.equals("Policy")) {
//			System.out.println(indent + getType() + " : " + getName() + " : "
//					+ getAlg() + " size : " + getChildrenNum());
			
			outS.println(indent + getType() + " : " + getName() + " : "
					+ getAlg() + " size : " + getChildrenNum() + "["+getLeft() +","+ getRight()+"]");
			
			targetFunc.printTarget(this.target, spaceGap);
		} else if (type.equals("Rule")) {

			String decision = null;
			if (effect == 0) {
				decision = "Deny";
			} else if (effect == 1) {
				decision = "Permit";
			}

//			System.out.println(indent + getType() + " : " + getName() + " : "
//					+ decision + " @ " + this.getSeqNum());
			
			outS.println(indent + getType() + " : " + getName() + " : "
					+ decision + " @ " + this.getSeqNum());
			
			targetFunc.printTarget(this.target, spaceGap);
			this.printOutNumericRules(spaceGap, outS);

		} else {
			System.out.println("No valid Type");
		}

//		System.out.println();
		outS.println();
	}

	public void printOutNodeInfo2String(int spaceGap) {
		// TODO Auto-generated method stub

		String indent = func.makeindent(spaceGap);

		if (type.equals("PolicySet") || type.equals("Policy")) {
			System.out.println(indent + getType() + " : " + getName() + " : "
					+ getAlg() + " size : " + getChildrenNum());
			
			targetFunc.printTarget(this.target, spaceGap);
		} else if (type.equals("Rule")) {

			String decision = null;
			if (effect == 0) {
				decision = "Deny";
			} else if (effect == 1) {
				decision = "Permit";
			}

			System.out.println(indent + getType() + " : " + getName() + " : "
					+ decision + " @ " + this.getSeqNum());
			
			targetFunc.printTarget(this.target, spaceGap);
//			this.printOutNumericRules(spaceGap, outS);

		} else {
			System.out.println("No valid Type");
		}

		System.out.println();
	}
	
	public void printOutNumericRules(int spaceGap, PrintStream outS) {

		String indent = func.makeindent(spaceGap);
		
		for (int i = 0; i < this.get_numericRulesNum(); i++) {
			trule trule = (trule) this.getRule(i);

			trule.printOutRuleInfo(indent, outS);
			
		}
	}

	
	public void set_childrenList(ArrayList a) {
		_children = a;
	}

	public void set_numericRulesList(ArrayList a) {
		_numericRules = a;
	}

	public void setAlg(String a) {
		algorithm = a;
	}

	public void setEffect(int i) {
	
		if (i == 0){this.effect = 1;} 
		else if (i == 1){this.effect = 0;}
		else if (i == -1){this.effect = -1;}		
//		this.effect = i;
	}

	public void setInfo(AbstractPolicy p) {
		// TODO Auto-generated method stub
		setName(p.getId().toString());

		String split[] = p.getCombiningAlg().getIdentifier().toString().split(
				":");
		if (split.length > 0) {
			setAlg(split[split.length - 1]);
		}

		if (p instanceof PolicySet) {
			setType("PolicySet");
		} else if (p instanceof Policy) {
			setType("Policy");
		}

		// System.out.println(p.getId() + " : " +
		// p.getCombiningAlg().getIdentifier());

	}

	public void setLeft(int i) {
		_left = i;
	}

	public void setName(String a) {
		name = a;
	}

	public void setRight(int i) {
		_right = i;
	}

	public void setRuleInfo(String type, String name, int effect,
			Target target, int rule_seq) {
		// TODO Auto-generated method stub
		this.setType(type);
		this.setName(name);
//		this.effect = effect;
		this.setEffect(effect);
		this.setTarget(target);
		this.setSeqNum(rule_seq);

	}

	public void setSeqNum(int i) {
		this.seq = i;
	}

	public void setTarget(Target b) {
		this.target = b;
	}
	

	
	public void setType(String a) {
		type = a;
	}


}
