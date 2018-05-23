package reqGen.ncsu.xacml.mutator;

import java.io.File;

public class Sandbox {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String path = "../xacml-subjects/fedora/default-2/";
		File f = new File(path);
		File[] files = f.listFiles();
		
//		<?xml version="1.0" encoding="UTF-8"?>
//		<PolicySet   xmlns="urn:oasis:names:tc:xacml:1.0:policy" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="urn:oasis:names:tc:xacml:1.0:policy /pro/xacml/summer2004/xacml/schema/cs-xacml-schema-policy-01.xsd" 
//		  PolicySetId="RPSlist"
//		  PolicyCombiningAlgId="urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:permit-overrides">
//		    <Target>
//		      <Subjects><AnySubject/></Subjects>
//		      <Resources><AnyResource/></Resources>
//		      <Actions><AnyAction/></Actions>
//		    </Target>
//		   
//		    <PolicySetIdReference>RPS_Faculty</PolicySetIdReference>
//		    <PolicySetIdReference>RPS_Student</PolicySetIdReference>
//		   
//		</PolicySet>
		
		for (int i = 0; i < files.length; i++) {
//			if (files[i].getName().contains("PPS")) {
//				System.out.println("\"" + files[i] + "\",");
//				System.out.println("\"continue-b-" + i + "\",");
//			}
			System.out.println("<PolicySetIdReference>" + files[i].getName().replace(".xml", "") + "</PolicySetIdReference>");
		}
	}

}
