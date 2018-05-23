/*
 * Created on Jan 23, 2006
 *
 */
package reqGen.ncsu.margrave;

import reqGen.ncsu.util.Util;

/**
 * @author eemartin
 *
 */
public class MrgrvPolicySplitter extends MrgrvPoco {

	public MrgrvPolicySplitter(String listFile) throws Exception {
		super(listFile);
	}
	
//	public int splitPolicyOneAtTime() {
//		return mrgrvPolicyModule.splitPolicyOneAtTime();
//	}
//	
//	public int splitPolicyNAtTime() {
//		return mrgrvPolicyModule.splitPolicyNAtTime();
//	}
//	
//	public int splitPolicyMinusOneAtTime() {
//		return mrgrvPolicyModule.splitPolicyMinusOneAtTime();
//	}
//	
//	public void copyPolicy(){
//		mrgrvPolicyModule.copyPolicy();
//	}
	
	public boolean isDecisionDeny() throws Exception {
		return mrgrvPolicyModule.isDecisionDeny();
	}
	
//	public static void main(String[] args) {
//		Util.setupLogger();
////		String p = "./ncsu/margrave/examples/tutorial/xacml-code/fedora2/fedora2.xml";
//		String p = Util.SUBJECTS_DIR + "/margrave/simple/simple-policy.xml";
//		try {
//			MrgrvPolicySplitter splitter = new MrgrvPolicySplitter(p);
//			splitter.copyPolicy();
////			splitter.splitPolicy();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
}
