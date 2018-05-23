package reqGen.src.requestGenerator;


import java.io.IOException;

import commonImpelmentation.ConfigInfo;

import reqGen.com.sun.xacml.ParsingException;
import reqGen.ncsu.*;
import reqGen.ncsu.util.Util;
import reqGen.ncsu.xacml.mutator.Mutator;



/**
 * Test the conversion program by calling MainClass with "xacml" file as parameter
 * @author JeeHyun Hwang 02/09/2007
 *
 */


public class TestProgram{


	public static void main(String[] args) throws Throwable{
		
		Util.setupLogger();
		
		ConfigInfo c = new ConfigInfo();

		int selected=0;
		int mode=0;
		int maxReqNum = 1000;
		
		if (args.length == 3) {
			selected = Integer.parseInt(args[0]);
			mode = Integer.parseInt(args[1]);
			maxReqNum = Integer.parseInt(args[2]);
		} else {
			System.err.println("err: ....");
		}
		
		// Change Argument in /xacml2firewall/ncsu/xacml/AllComboReqFactory.java
		// if 1 ; SoD mode
		// if 2 ; nonSoD mode
		System.out.println("1.err: ....");
		if (mode == 1){
			System.out.println("12.err: ....");
			RequestGenerator.random(c.logDir + c.xacml_policy [selected], c.logDir+ c.SoDreqDir[selected], maxReqNum);
		} else if (mode == 2){
			System.out.println("22.err: ....");
			RequestGenerator.random(c.logDir + c.xacml_policy [selected], c.logDir+ c.nonSoDreqDir[selected], maxReqNum);	
		}
		System.out.println("2.err: ....");
		
		
	}

}
