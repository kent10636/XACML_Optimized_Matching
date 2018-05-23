/*
 * Created on Jan 23, 2006
 */
package reqGen.ncsu.margrave;

import java.util.ArrayList;
import java.util.Vector;

import reqGen.ncsu.util.Util;
import reqGen.ncsu.xacml.MrgrvRequestFactory;
import reqGen.ncsu.xacml.RequestFactoryIntf;


/**
 * @author eemartin
 */
public class MrgrvChangeParser {

	public static final int LIST = 1;
	public static final int EXAMPLES = 2;
	public static final int DONE = 3;
	
	private int currentState;
	private ArrayList list;
//	private Vector requests;
	private RequestFactoryIntf reqFactory;
	
	public MrgrvChangeParser() {		
		reset();
	}
	/**
	 * 
	 * As an example here is some output
	 * 
	 1:/Action, command, Receive/
	 2:/Action, command, OTHER/
	 3:/Resource, resource-class, ExternalGrades/
	 4:/Resource, resource-class, OTHER/
	 5:/Subject, role, Student/
	 6:/Subject, role, OTHER/
	 7:/Action, command, View/
	 8:/Action, command, Assign/
	 9:/Resource, resource-class, InternalGrades/
	 10:/Subject, role, Faculty/
	 11:/Subject, role, TA/
	 
	 1         
	 12345678901
	 {
	 00000-01101  N->P
	 00000-10101  N->P
	 00001-01101  N->P
	 00001-10101  N->P
	 00100-01001  N->P
	 00100-10001  N->P
	 00101-01001  N->P
	 00101-10001  N->P
	 
	 }	 
	 *
	 *  @param line
	 */
	public void parseLine(String line) {
		if (line == null) {
			return;
		}
		if ("".equals(line)) {
			return;
		}
		
		try {
			switch (currentState) {
			case LIST:
				parseList(line);
				break;
			case EXAMPLES:
				parseExample(line);
				break;
			default:
				throw new IllegalStateException("Invalid parsing state");
			}
		} catch (IllegalStateException ise) {
			MrgrvExec.logger.error("Reseting parser", ise);
			MrgrvExec.logger.debug(this.toString());
			reset();
		} catch (Exception e) {
			MrgrvExec.logger.error("Error parsing line \'" + line + "\'", e);
		}
	}
	
	public boolean isParsingList() {
		return currentState == LIST;
	}
	
	public boolean isParsingExamples() {
		return currentState == EXAMPLES;
	}
	
	public boolean isParsingComplete() {
		return currentState == DONE;
	}
	
	public ArrayList getList() {
		return list;
	}
	
	private void nextState() throws IllegalStateException {
		switch (currentState) {
		case LIST:
			currentState = EXAMPLES;
			break;
		case EXAMPLES:
			currentState = DONE;
			break;
		default:
			throw new IllegalStateException("Invalid parsing state");
		}
	}
	
	private void parseList(String line) throws Exception {
		String[] token = line.split(":");
		
		if (token.length > 2) {
			StringBuffer buff = new StringBuffer(token[1]);
			for (int i = 2; i < token.length; i++) {
				buff.append(":" + token[i]);
			}
			String id = token[0];
			token = new String[2];
			token[0] = id;
			token[1] = buff.toString();
		}
		
		for (int i = 0; i < token.length; i++) {
			token[i] = token[i].trim();
		}
		
		if (token.length == 1) {
			if ("{".equals(token[0])) {
				nextState();
			} 
		} else if (token.length == 2) {
			// else just read the lines
			int id = Integer.parseInt(token[0]);
			parseListItem(id, token[1]);
		} else {
			error("Error parsing list at line " + line);
		}
	}
	
	/**
	 * Parses list item of the form 
	 * 9:/Resource, resource-class, InternalGrades/
	 * @param id
	 * @pre 1 <= id
	 * @param item
	 */
	private void parseListItem(int id, String item) throws Exception {
		list.add(id-1, item);
	}
	
	public char[] parseExample(String line) throws Exception {
		if (currentState != EXAMPLES) {
			throw new IllegalStateException("Invalid parsing state");
		}
		char[] req = new char[list.size()];
		if (line.indexOf("}") != -1) {
			nextState();
			MrgrvExec.logger.debug("End of change output found");
			return null;
		}
		line = line.trim();
		if (line.equals("")) {
			return null;
		}
		char[] chars = line.toCharArray();
		for (int i = 0; i < chars.length && i < list.size(); i++) {
			if (chars[i] == ' ') {
				return null;
			}
			req[i] = chars[i];
		}
		return req;
	}
	
	private void reset() {
		list = new ArrayList();
//		requests = new Vector();
		currentState = LIST;
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("MrgrvChangeParser");
		buffer.append(Util.SEP);
		
		buffer.append("currentState=");
		buffer.append(currentState);
		buffer.append(Util.SEP);
		
		return buffer.toString();
	}
	
	private void error(String msg) throws Exception {
		throw new Exception(msg);
	}
}