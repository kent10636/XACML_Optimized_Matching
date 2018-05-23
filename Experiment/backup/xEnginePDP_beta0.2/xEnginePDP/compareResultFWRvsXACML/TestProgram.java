/**
 * @author Fei Chen
 * feichen@cse.msu.edu
 * Computer Science and Engineering
 * Michigan State University
 * 12/03/2008
 */

package compareResultFWRvsXACML;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import ncsu.util.Util;


import com.sun.xacml.ParsingException;
import commonImpelmentation.ConfigInfo;

public class TestProgram{


	public static void main(String[] args) throws Throwable{
		
		Util.setupLogger();


		ConfigInfo c = new ConfigInfo();
		
		int selected =4;
		int mode =1;
		
		if (args.length == 2) {
			selected = Integer.parseInt(args[0]);
			mode = Integer.parseInt(args[1]);
		}	 
		
		if (mode == 1){
			compareResultFWRvsXACML.main(new String[]{c.SOD_fwr_response[selected],  c.SOD_xacml_response[selected], c.SOD_compare_result[selected]});
			
		} else if (mode ==2){

			compareResultFWRvsXACML.main(new String[]{c.nonSOD_fwr_response[selected],  c.nonSOD_xacml_response[selected], c.nonSOD_compare_result[selected]});
		}
			
	}

}
