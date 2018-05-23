/*
 * Created on Jan 23, 2006
 *
 */
package reqGen.ncsu.xacml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;

import reqGen.com.sun.xacml.EvaluationCtx;
import reqGen.com.sun.xacml.attr.AttributeValue;
import reqGen.com.sun.xacml.attr.StringAttribute;
import reqGen.com.sun.xacml.ctx.Attribute;
import reqGen.com.sun.xacml.ctx.RequestCtx;
import reqGen.com.sun.xacml.ctx.Subject;
import reqGen.ncsu.margrave.MrgrvChangeParser;



/**
 * @author eemartin
 *
 */
public class MrgrvRequestFactory implements RequestFactoryIntf {
	
	private static final int SUBJECT = 0;
	private static final int RESOURCE = 1;
	private static final int ACTION = 2;
	private static final String subjIdURI = "urn:oasis:names:tc:xacml:1.0:subject:subject-id";	
	private static final String resIdURI = EvaluationCtx.RESOURCE_ID;
	
//	private int i;
	private ArrayList list;
	private HashSet subject, resource, action, environment;
	private BufferedReader in;
	private MrgrvChangeParser parser;
	
	public MrgrvRequestFactory() {
		parser = new MrgrvChangeParser(); 
	}
	
	public MrgrvRequestFactory(File f) {
		try {
			in = new BufferedReader(new FileReader(f));
			if (!in.ready()) {
				return;
			}
		} catch (Exception e) {
			logger.error("Error creating file reader " + f, e);
		}
		parser = new MrgrvChangeParser();
		initList();
	}
	
	public RequestCtx processLine(String line) {
		if (line == null) {
			return null;
		}
		if (parser.isParsingList()) {
			parser.parseLine(line);
			return null;
		}
		if (list == null) {
			list = new ArrayList(parser.getList());
		}
		if (parser.isParsingComplete()) {
			return null;
		}
		RequestCtx request = null;
		try {
			char[] r = parser.parseExample(line); 
			if (r == null) {
				return null;
			}
			createAttributeSets(r);
			request = new RequestCtx(
					subject, 
					resource, 
					action, 
					environment);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return request;
	}
	
	public boolean isInitialized() {
		if (parser == null) {
			return false;
		}
		return !parser.isParsingList();
	}
		
	private void initList() {
		while (parser.isParsingList()) {
			parser.parseLine(readLine());
		}
		list = new ArrayList(parser.getList());
	}
	
	private String readLine() {
		String line = "";
		try {
			line = in.readLine();
		} catch (Exception e) {
			logger.error("Error reading line.", e);
		}
		return line;
	}
	
//	public MrgrvRequestFactory(Collection list, Collection requests) {
//		this.list = new ArrayList(list);
//		this.req = new ArrayList(requests);
//		i = 0;
//	}
	
	/* (non-Javadoc)
	 * @see ncsu.xacml.RequestFactory#nextRequest()
	 */
	public RequestCtx nextRequest() throws Exception {
		if (parser == null) {
			return null;
		}
		RequestCtx request = null;
		try {
			char[] r = parser.parseExample(readLine()); //(char[]) req.get(i++);
			if (r == null) {
				return null;
			}
			createAttributeSets(r);
			request = new RequestCtx(
					subject, 
					resource, 
					action, 
					environment);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return request;
	}

	/* (non-Javadoc)
	 * @see ncsu.xacml.RequestFactory#hasNext()
	 */
	public boolean hasNext() {
		if (parser == null) {
			return false;
		}
		return !parser.isParsingComplete();
	}
	
	private void createAttributeSets(char[] r) throws URISyntaxException {
		// initialize
		boolean hasSubjId = false;
		boolean hasResourceId = false;
		subject = new HashSet();  
		resource = new HashSet();
		action = new HashSet();
		environment = new HashSet();
		
		
		for (int i = 0; i < r.length; i++) {
			String id = null;
			// show it or not?
			if (r[i] == '0') {
				continue; // maybe do a ! here
			} else if (r[i] == '1') {
				Attribute attribute = createAttribute((String) list.get(i)); 
				// a little checking
				if (attribute != null) {
					id = attribute.getId().toString();
					if (subjIdURI.equals(id)) {
						hasSubjId = true;
					} else if (resIdURI.equals(id)) {
						hasResourceId = true;
					}
				}
			} else if (r[i] == '-') {
				continue;
			} else {
				logger.warn("How did I get here?");
			}
		}
		// add a default subject
		if (!hasSubjId) {
			StringAttribute value = new StringAttribute("DEFAULT SUBJECT");
			subject.add(new Attribute(new URI(subjIdURI), null, null, value));
		}
		// default resource
		if (!hasResourceId) {
			StringAttribute value = new StringAttribute("DEFAULT RESOURCE");
			resource.add(new Attribute(new URI(resIdURI), null, null, value));
		}
		// bundle the attributes in a Subject with the default category
		Subject s = new Subject(subject);
		subject = new HashSet();
		subject.add(s);
	}
	
	private Attribute createAttribute(String line) throws URISyntaxException {
		// split out the parts
		String[] token = line.split(",");
		if (token.length != 3) {
			logger.error("Incorrect tokens for: " + line);
			return null;
		}
		for (int i = 0; i < token.length; i++) {
			token[i] = token[i].replaceAll("/","").trim();
		}
		// create the attribute
		URI id = new URI(token[1]);
		AttributeValue value = new StringAttribute(token[2]);
		Attribute attr = new Attribute(id, null, null, value);
		// where to put it
		if (token[0].indexOf("Subject") != -1) {
			subject.add(attr);
		} else if (token[0].indexOf("Resource") != -1) {
			resource.add(attr);
		} else if (token[0].indexOf("Action") != -1) {
			action.add(attr);
		} else {
			logger.error("Error when creating attribute from: " + line);
		}
		return attr;
	}
	
//	public static void main(String[] args) {
//		File f = new File("mrgrv-out-temp-continue.txt");
//		RequestFactoryIntf factory = new MrgrvRequestFactory(f);
//		int i = 1;
//		while (factory.hasNext()) {
//			try {
//				RequestCtx req = factory.nextRequest();
//				String reqF = path + (i++) + "-req.xml";
//				writeRequest(req, reqF);
//			} catch (Exception e) {
//				logger.error("Error on request " + path + i, e);
//			}
//		}
//		return i-1;
//	}
}
