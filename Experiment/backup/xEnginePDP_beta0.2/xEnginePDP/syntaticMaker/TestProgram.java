package syntaticMaker;


import java.io.*;

import ncsu.util.Util;


import com.sun.xacml.ParsingException;
import com.sun.xacml.finder.PolicyFinder;
import commonImpelmentation.*;

/**
 * Test the conversion program by calling MainClass with "xacml" file as parameter
 * @author JeeHyun Hwang 02/09/2007
 *
 */


public class TestProgram{


	public synchronized static void main(String[] args) throws Throwable{
		
		Util.setupLogger();

		ConfigInfo c = new ConfigInfo();	
		int selected = 0;
		if (args.length == 1) {
			selected = Integer.parseInt(args[0]);
		} else {
			System.err.println("err: ....");
		}
		syntaticRuleMaker syntaticTree = new syntaticRuleMaker(c.logDir + c.xacml_policy[selected]);
		syntaticTree.generatePolicy();

	}

}
