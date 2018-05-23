/*
 * Created on Nov 8, 2005
 *
 */
package reqGen.ncsu.xacml;

import org.apache.log4j.Logger;

import reqGen.com.sun.xacml.ctx.RequestCtx;


/**
 * @author eemartin
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface RequestFactoryIntf {

	public static Logger logger = Logger.getLogger(RequestFactoryIntf.class);
	public RequestCtx nextRequest() throws Exception;
	public boolean hasNext();
}
