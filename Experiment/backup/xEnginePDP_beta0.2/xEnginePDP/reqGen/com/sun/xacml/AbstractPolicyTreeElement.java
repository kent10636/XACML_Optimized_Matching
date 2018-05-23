package reqGen.com.sun.xacml;

import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


import org.apache.log4j.Logger;

import reqGen.com.sun.xacml.attr.AttributeDesignator;
import reqGen.com.sun.xacml.attr.AttributeValue;
import reqGen.com.sun.xacml.cond.Function;
import reqGen.com.sun.xacml.cond.FunctionFactory;
import reqGen.ncsu.util.MethodLocator;


/**
 * This class handles most of the mutation stuff
 * @author eemartin
 *
 */
public abstract class AbstractPolicyTreeElement implements PolicyTreeElement {
	
	protected String mutantType;
	
	private MethodLocator locator;
	
	protected Logger logger4j = Logger.getLogger(AbstractPolicyTreeElement.class);
	
	/**
     * Encodes this <code>AbstractPolicyTreeElement</code> into its XML representation and
     * writes this encoding to the given <code>OutputStream</code> with no
     * indentation.
     *
     * @param output a stream into which the XML-encoded data is written
     */
    public void encode(OutputStream output) {
        encode(output, new Indenter(0));
    }
    
	public boolean encodeMutant(OutputStream output, Indenter indenter) {
		if (mutantType == null) {
			encode(output, indenter);
			return true;
		}
		Object[] args = {output, indenter};
		try {		
			// find method
			Method m = getLocator().findMethod(mutantType, args);
			logger4j.debug("Encoding " + mutantType);
			// now create mutants
			Object ret = m.invoke(this, args);
			return ((Boolean) ret).booleanValue();
		} catch (IllegalArgumentException e) {
			logger4j.error("Illegal argument for " + mutantType, e);
		} catch (IllegalAccessException e) {
			logger4j.error("Illegal access for " + mutantType, e);
		} catch (InvocationTargetException e) {
			logger4j.error("Error invoking target for " + mutantType, e);
		} catch (NoSuchMethodException e) {
			logger4j.error("No such method for " + mutantType, e);
		}
		return false;
	}
	
	public abstract boolean PTT(OutputStream output, Indenter indenter);
	
	public abstract boolean PTF(OutputStream output, Indenter indenter);
	
	public abstract boolean RTT(OutputStream output, Indenter indenter);
	
	public abstract boolean RTF(OutputStream output, Indenter indenter);

	public abstract boolean RCT(OutputStream output, Indenter indenter);
	
	public abstract boolean RCF(OutputStream output, Indenter indenter);
	
//	public abstract boolean CPC(OutputStream output, Indenter indenter);
	
//	public abstract boolean CRC(OutputStream output, Indenter indenter);
	
//	public abstract boolean CPO(OutputStream output, Indenter indenter);
	
//	public abstract boolean CRO(OutputStream output, Indenter indenter);
	
	public abstract boolean CRE(OutputStream output, Indenter indenter);
	
	public abstract boolean RMP(OutputStream output, Indenter indenter);
	
	public abstract boolean RMR(OutputStream output, Indenter indenter);
	
	public abstract boolean PSTT(OutputStream output, Indenter indenter);
	
	public abstract boolean PSTF(OutputStream output, Indenter indenter);
	
	public abstract boolean RMPS(OutputStream output, Indenter indenter);
	
	public abstract boolean oneToEmpty(OutputStream output, Indenter indenter);
	
	public void setMutantType(String type) {
		mutantType = type;
	}
	
	private MethodLocator getLocator() {
		if (locator == null) {
			locator = new MethodLocator(AbstractPolicyTreeElement.class);
		}
		return locator;
	}

    /**
     * Simple helper routine that creates a TargetMatch instance.
     *
     * @param type the type of match
     * @param functionId the matching function identifier
     * @param designator the AttributeDesignator used in this match
     * @param value the AttributeValue used in this match
     *
     * @return the matching element
     */
    public static TargetMatch createTargetMatch(int type, String functionId,
                                                AttributeDesignator designator,
                                                AttributeValue value) {
        try {
            // get the factory that handles Target functions and get an
            // instance of the right function
            FunctionFactory factory = FunctionFactory.getTargetInstance();
            Function function = factory.createFunction(functionId);
        
            // create the TargetMatch
            return new TargetMatch(type, function, designator, value);
        } catch (Exception e) {
            // note that in this example, we should never hit this case, but
            // in the real world you need to worry about exceptions, especially
            // from the factory
            return null;
        }
    }
}
