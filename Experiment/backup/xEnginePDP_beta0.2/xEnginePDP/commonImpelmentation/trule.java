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
import java.util.Iterator;
import java.util.List;

import com.sun.xacml.AbstractPolicy;
import com.sun.xacml.Policy;
import com.sun.xacml.PolicySet;
import com.sun.xacml.Target;

//import diversity.Node;

// Referenced classes of package diversity:
//            Edge, Interval, Rule, Sequence

public class trule
{
    public List _rule_seq_collection;	
    private String rule;
    private int effect;
    private int rule_seq;

    public trule(String rule, int effect, int rule_seq) {
    	this.rule = rule;
    	
    	// effect change 0 -> 1 & 1 -> 0 & others -> -1
    	setEffect(effect);
    	this.rule_seq = rule_seq;    	     	
    } 
 
    public void setRule(String a)
    {
    	this.rule = a;
    }
    
    public String getRule()
    {
        return this.rule;
    }
 
	// effect change 0 -> 1 & 1 -> 0 & others -> -1
    
    public void setEffect(int a)
    {
 /*
    	if (a == 0){this.effect = 1;} else if (a == 1){
    		this.effect = 0;
    	} else {
    		this.effect = -1;
    	}
*/    		
    	this.effect = a;
    }
    public int getEffect()
    {
        return this.effect;
    }    
    
    public void setRule_seq(int a)
    {
    	this.rule_seq = a;
    }
 

    public int getRule_seq()
    {
        return this.rule_seq;
    }

	public void printOutRuleInfo(String indent, PrintStream outS) {
		// TODO Auto-generated method stub

		
//		System.out.println(indent + "  + " + getRule() + " -> " + getEffect() + " @ " + getRule_seq());
		
		outS.println(indent + getRule() + "->"+ getEffect());
		
	}    

	public void printOutRule_rule_seq_collection(String indent, PrintStream outS) {
		// TODO Auto-generated method stub

		if (_rule_seq_collection == null) return;
		
		Iterator iter = _rule_seq_collection.iterator();
		
		while (iter.hasNext()){
			int num = (Integer) iter.next();
			outS.print(indent + num);
		}	
	}
}
