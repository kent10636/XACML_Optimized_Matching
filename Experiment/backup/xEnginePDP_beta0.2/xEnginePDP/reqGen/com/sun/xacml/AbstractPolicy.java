
/*
 * @(#)AbstractPolicy.java
 *
 * Copyright 2003-2004 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   1. Redistribution of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * 
 *   2. Redistribution in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for use in
 * the design, construction, operation or maintenance of any nuclear facility.
 */

package reqGen.com.sun.xacml;

import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import reqGen.com.sun.xacml.attr.AnyURIAttribute;
import reqGen.com.sun.xacml.attr.AttributeDesignator;
import reqGen.com.sun.xacml.combine.CombiningAlgFactory;
import reqGen.com.sun.xacml.combine.CombiningAlgorithm;
import reqGen.com.sun.xacml.combine.PolicyCombiningAlgorithm;
import reqGen.com.sun.xacml.combine.RuleCombiningAlgorithm;
import reqGen.com.sun.xacml.ctx.Result;



/**
 * Represents an instance of an XACML policy. 
 *
 * @since 1.0
 * @author Seth Proctor
 * @author Marco Barreno
 */
public abstract class AbstractPolicy extends AbstractPolicyTreeElement
{

    /**
     * XPath 1.0 identifier, the only version we support right now
     */
    public static final String XPATH_1_0_VERSION =
        "http://www.w3.org/TR/1999/Rec-xpath-19991116";

    // atributes associated with this policy
    protected URI idAttr;
    private CombiningAlgorithm combiningAlg;

    // the elements in the policy
    private String description;
    private Target target;

    // the value in defaults, or null if there was no default value
    private String defaultVersion;

    // the elements we run through the combining algorithm
    private List children;

    // any obligations held by this policy
    private Set obligations;

    // the logger we'll use for all messages
    private static final Logger logger =
        Logger.getLogger(AbstractPolicy.class.getName());

    /**
     * Constructor used by <code>PolicyReference</code>, which supplies
     * its own values for the methods in this class.
     */
    protected AbstractPolicy() {

    }

    /**
     * Constructor used to create a policy from concrete components.
     *
     * @param id the policy id
     * @param combiningAlg the combining algorithm to use
     * @param description describes the policy or null if there is none
     * @param target the policy's target
     */
    protected AbstractPolicy(URI id, CombiningAlgorithm combiningAlg,
                             String description, Target target) {
        this(id, combiningAlg, description, target, null);
    }

    /**
     * Constructor used to create a policy from concrete components.
     *
     * @param id the policy id
     * @param combiningAlg the combining algorithm to use
     * @param description describes the policy or null if there is none
     * @param target the policy's target
     * @param defaultVersion the XPath version to use for selectors
     */
    protected AbstractPolicy(URI id, CombiningAlgorithm combiningAlg,
                             String description, Target target,
                             String defaultVersion) {
        this(id, combiningAlg, description, target, defaultVersion, null);
    }

    /**
     * Constructor used to create a policy from concrete components.
     *
     * @param id the policy id
     * @param combiningAlg the combining algorithm to use
     * @param description describes the policy or null if there is none
     * @param target the policy's target
     * @param defaultVersion the XPath version to use for selectors
     * @param obligations the policy's obligations
     */
    protected AbstractPolicy(URI id, CombiningAlgorithm combiningAlg,
                             String description, Target target,
                             String defaultVersion, Set obligations) {
        idAttr = id;
        this.combiningAlg = combiningAlg;
        this.description = description;
        this.target = target;
        this.defaultVersion = defaultVersion;

        if (obligations == null)
            this.obligations = Collections.EMPTY_SET;
        else
            this.obligations = Collections.
                unmodifiableSet(new HashSet(obligations));
    }

    /**
     * Constructor used by child classes to initialize the shared data from
     * a DOM root node.
     *
     * @param root the DOM root of the policy
     * @param policyPrefix either "Policy" or "PolicySet"
     * @param combiningName name of the field naming the combining alg
     *
     * @throws ParsingException if the policy is invalid
     */
    protected AbstractPolicy(Node root, String policyPrefix,
                             String combiningName) throws ParsingException {
        // get the attributes, all of which are common to Policies
        NamedNodeMap attrs = root.getAttributes();

        try {
            // get the attribute Id
            idAttr = new URI(attrs.getNamedItem(policyPrefix + "Id").
                             getNodeValue());
        } catch (Exception e) {
            throw new ParsingException("Error parsing required attribute " +
                                       policyPrefix + "Id", e);
        }
        
        // now get the combining algorithm...
        try {
            URI algId = new URI(attrs.getNamedItem(combiningName).
                                getNodeValue());
            CombiningAlgFactory factory = CombiningAlgFactory.getInstance();
            combiningAlg = factory.createAlgorithm(algId);
        } catch (Exception e) {
            throw new ParsingException("Error parsing combining algorithm" +
                                       " in " + policyPrefix, e);
        }
        
        // ...and make sure it's the right kind
        if (policyPrefix.equals("Policy")) {
            if (! (combiningAlg instanceof RuleCombiningAlgorithm))
                throw new ParsingException("Policy must use a Rule " +
                                           "Combining Algorithm");
        } else {
            if (! (combiningAlg instanceof PolicyCombiningAlgorithm))
                throw new ParsingException("PolicySet must use a Policy " +
                                           "Combining Algorithm");
        }

        obligations = new HashSet();

        // now read the policy elements
        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String cname = child.getNodeName();

            if (cname.equals("Description")) {
                description = child.getFirstChild().getNodeValue();
            } else if (cname.equals("Target")) {
                target = Target.getInstance(child, defaultVersion);
            } else if (cname.equals("Obligations")) {
                parseObligations(child);
            } else if (cname.equals(policyPrefix + "Defaults")) {
                handleDefaults(child);
            }
        }

        // finally, make sure the set of obligations is immutable
        obligations = Collections.unmodifiableSet(obligations);
    }

    /**
     * Helper routine to parse the obligation data
     */
    private void parseObligations(Node root) throws ParsingException {
        NodeList nodes = root.getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeName().equals("Obligation"))
                obligations.add(Obligation.getInstance(node));
        }
    }

    /**
     * There used to be multiple things in the defaults type, but now
     * there's just the one string that must be a certain value, so it
     * doesn't seem all that useful to have a class for this...we could
     * always bring it back, however, if it started to do more
     */
    private void handleDefaults(Node root) throws ParsingException {
        defaultVersion = null;
        NodeList nodes = root.getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeName().equals("XPathVersion")) {
                defaultVersion = node.getFirstChild().getNodeValue();
                if (! defaultVersion.equals(XPATH_1_0_VERSION)) {
                    throw new ParsingException("Unknown XPath version");
                }
            }
        }
    }

    /**
     * Returns the id of this policy
     *
     * @return the policy id
     */
    public URI getId() {
        return idAttr;
    }

    /**
     * Returns the combining algorithm used by this policy
     *
     * @return the combining algorithm
     */
    public CombiningAlgorithm getCombiningAlg() {
        return combiningAlg;
    }
    
    public void setCombiningAlg(CombiningAlgorithm alg) {
    	combiningAlg = alg;
    }

    /**
     * Returns the given description of this policy or null if there is no
     * description
     *
     * @return the description or null
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the target for this policy
     *
     * @return the policy's target
     */
    public Target getTarget() {
        return target;
    }

    /**
     * Returns the XPath version to use or null if none was specified
     *
     * @return XPath version or null
     */
    public String getDefaultVersion() {
        return defaultVersion;
    }

    /**
     * Returns the <code>List</code> of children under this node in the
     * policy tree. Depending on what kind of policy this node represents
     * the children will either be <code>AbstractPolicy</code> objects
     * or <code>Rule</code>s.
     *
     * @return a <code>List</code> of child nodes
     */
    public List getChildren() {
        return children;
    }

    /**
     * Returns the Set of obligations for this policy, which may be empty
     *
     * @return the policy's obligations
     */
    public Set getObligations() {
        return obligations;
    }

    /**
     * Given the input context sees whether or not the request matches this
     * policy. This must be called by combining algorithms before they
     * evaluate a policy. This is also used in the initial policy finding
     * operation to determine which top-level policies might apply to the
     * request.
     *
     * @param context the representation of the request
     *
     * @return the result of trying to match the policy and the request
     */
    public MatchResult match(EvaluationCtx context) {
        return target.match(context);
    }

    /**
     * Sets the child policy tree elements for this node, which are passed
     * to the combining algorithm on evaluation. The <code>List</code> must
     * contain <code>Rule</code>s or <code>AbstractPolicy</code>s, but may
     * not contain both types of elements.
     *
     * @param children the child elements used by the combining algorithm
     */
    public void setChildren(List children) {
        // we always want a concrete list, since we're going to pass it to
        // a combiner that expects a non-null input
        if (children == null) {
            this.children = Collections.EMPTY_LIST;
        } else {
            // NOTE: since this is only getting called by known child
            // classes we don't check that the types are all the same
            this.children = Collections.unmodifiableList(children);
        }
    }

    /**
     * Tries to evaluate the policy by calling the combining algorithm on
     * the given policies or rules. The <code>match</code> method must always
     * be called first, and must always return MATCH, before this method
     * is called.
     *
     * @param context the representation of the request
     *
     * @return the result of evaluation
     */
    public Result evaluate(EvaluationCtx context) {
        // evaluate
        Result result = combiningAlg.combine(context, children);

        System.out.println("Result evaluate(EvaluationCtx context)	");
        
        // if we have no obligations, we're done
        if (obligations.size() == 0)
            return result;

        System.out.println("Result evaluate(EvaluationCtx context)	q");      
        
        // now, see if we should add any obligations to the set
        int effect = result.getDecision();

        if ((effect == Result.DECISION_INDETERMINATE) ||
            (effect == Result.DECISION_NOT_APPLICABLE)) {
            // we didn't permit/deny, so we never return obligations
            return result;
        }

        Iterator it = obligations.iterator();
        while (it.hasNext()) {
            Obligation obligation = (Obligation)(it.next());
            if (obligation.getFulfillOn() == effect)
                result.addObligation(obligation);
        }

        // finally, return the result
        return result;
    }

    /**
     * Routine used by <code>Policy</code> and <code>PolicySet</code> to
     * encode some common elements.
     *
     * @param output a stream into which the XML-encoded data is written
     * @param indenter an object that creates indentation strings
     */
    protected void encodeCommonElements(OutputStream output, Indenter indenter) {
    	if (target != null)
    		target.encode(output, indenter);
    	
    	Iterator it = children.iterator();
    	while (it.hasNext()) {
    		PolicyTreeElement pte = (PolicyTreeElement) it.next();
    		pte.encode(output, indenter);
    	}
    	
    	if (obligations.size() != 0) {
    		PrintStream out = new PrintStream(output);
    		String indent = indenter.makeString();
    		
    		out.println(indent + "<Obligations>");
    		indenter.in();
    		
    		it = obligations.iterator();
    		while (it.hasNext()) {
    			((Obligation)(it.next())).encode(output, indenter);
    		}
    		
    		out.println(indent + "</Obligations>");
    		indenter.out();
    	}
    }    
    
    /**
     * for forwarding op down the tree
     * @param output
     * @param indenter
     * @param op
     */
    protected void encodeCommonElements(OutputStream output,
			Indenter indenter, String op) {
		if (target != null)
			target.encode(output, indenter);

		Iterator it = children.iterator();
		while (it.hasNext()) {
			PolicyTreeElement pte = (PolicyTreeElement) it.next();
			pte.setMutantType(op);
			pte.encode(output, indenter, pte);
			pte.setMutantType(null); 
		}

		if (obligations.size() != 0) {
			PrintStream out = new PrintStream(output);
			String indent = indenter.makeString();

			out.println(indent + "<Obligations>");
			indenter.in();

			it = obligations.iterator();
			while (it.hasNext()) {
				((Obligation) (it.next())).encode(output, indenter);
			}

			out.println(indent + "</Obligations>");
			indenter.out();
		}
	}
    
    protected boolean encodeCommonElements(OutputStream output, Indenter indenter, PolicyTreeElement mutant) {
    	boolean success = false;
    	
    	if (target != null)
    		target.encode(output, indenter);
    	
    	Iterator it = children.iterator();
    	while (it.hasNext()) {
    		PolicyTreeElement pte = (PolicyTreeElement) it.next();
    		if (pte.encode(output, indenter, mutant)) {
    			success = true;
    		}
    	}
    	
    	if (obligations.size() != 0) {
    		PrintStream out = new PrintStream(output);
    		String indent = indenter.makeString();
    		
    		out.println(indent + "<Obligations>");
    		indenter.in();
    		
    		it = obligations.iterator();
    		while (it.hasNext()) {
    			((Obligation)(it.next())).encode(output, indenter);
    		}
    		
    		out.println(indent + "</Obligations>");
    		indenter.out();
    	}
    	return success;
    }
    
    public void setId(String id) {
    	try {
    		idAttr = new URI(id);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
        
	public boolean PTF(OutputStream output, Indenter indenter) {
		if (!(this instanceof Policy)) {
    		logger4j.error(this.getClass() + " Can not set policy target false on this type " + getId().toString());
    		return false;
    	}    	
		PrintStream out = new PrintStream(output);
		Target oldTarget = target;
		out.println("<!--PTF: Mutated target " + idAttr.toString() + "-->");
		
		// create a bogus Resource section
		List resource = new ArrayList();
		try {
			String resourceMatchId = "urn:oasis:names:tc:xacml:1.0:function:anyURI-equal";
			URI resourceDesignatorType;
			resourceDesignatorType = new URI("http://www.w3.org/2001/XMLSchema#anyURI");
			
			URI resourceDesignatorId =
				new URI("urn:oasis:names:tc:xacml:1.0:resource:resource-id");
			AttributeDesignator resourceDesignator =
				new AttributeDesignator(AttributeDesignator.RESOURCE_TARGET,
						resourceDesignatorType,
						resourceDesignatorId, false);
			
			AnyURIAttribute resourceValue =
				new AnyURIAttribute(new URI("http://127.0.0.1.abcdefh.mutated.hopefully.not.com"));
			
			resource.add(createTargetMatch(TargetMatch.RESOURCE, resourceMatchId,
					resourceDesignator, resourceValue));
		} catch (URISyntaxException e) {
			logger4j.error("Error mutating "  + getId().toString());
			return false;
		}
		
		// put the Resource sections into their lists
		List resources = new ArrayList();
		resources.add(resource);
		
		// create a bogus target
		target = new Target(null, resources, null);
		encode(output, indenter);
		
		// restore
		target = oldTarget;	
		return true;
	}

	public boolean PTT(OutputStream output, Indenter indenter) {
		if (!(this instanceof Policy)) {
			logger4j.error(this.getClass() + " Can not set policy target true on this type " + getId().toString());
    		return false;
    	}		
		PrintStream out = new PrintStream(output);
		Target oldTarget = target;
		if (target == null) {
			// doesn't do any good
			logger4j.debug("PTT: EQUIVALENT MUTANT! No target to remove " + idAttr.toString());
			out.println("<!--PTT: EQUIVALENT MUTANT! No target to remove " + idAttr.toString() + "-->");
			return false;
		} else {
			out.println("<!--PTT: Removed target " + idAttr.toString() + "-->");
			target = null;
		}
		encode(output, indenter);
		// restore
		target = oldTarget;		
		return true;
	}	

	public boolean PSTF(OutputStream output, Indenter indenter) {
		if (!(this instanceof PolicySet)) {
			logger4j.error(this.getClass() + " Can not set policy set target false on this type " + getId().toString());
    		return false;
    	}
		PrintStream out = new PrintStream(output);
		Target oldTarget = target;
		out.println("<!--PSTF: Mutated target " + idAttr.toString() + "-->");
		
		// create a bogus Resource section
		List resource = new ArrayList();
		try {
			String resourceMatchId = "urn:oasis:names:tc:xacml:1.0:function:anyURI-equal";
			URI resourceDesignatorType;
			resourceDesignatorType = new URI("http://www.w3.org/2001/XMLSchema#anyURI");
			
			URI resourceDesignatorId =
				new URI("urn:oasis:names:tc:xacml:1.0:resource:resource-id");
			AttributeDesignator resourceDesignator =
				new AttributeDesignator(AttributeDesignator.RESOURCE_TARGET,
						resourceDesignatorType,
						resourceDesignatorId, false);
			
			AnyURIAttribute resourceValue =
				new AnyURIAttribute(new URI("http://127.0.0.1.abcdefh.mutated.hopefully.not.com"));
			
			resource.add(createTargetMatch(TargetMatch.RESOURCE, resourceMatchId,
					resourceDesignator, resourceValue));
		} catch (URISyntaxException e) {
			logger4j.error("Error mutating "  + getId().toString());
			return false;
		}
		
		// put the Resource sections into their lists
		List resources = new ArrayList();
		resources.add(resource);
		
		// create a bogus target
		target = new Target(null, resources, null);
		encode(output, indenter);
		
		// restore
		target = oldTarget;	
		return true;
	}

	public boolean PSTT(OutputStream output, Indenter indenter) {
		if (!(this instanceof PolicySet)) {
			logger4j.error(this.getClass() + " Can not set policy set target true on this type " + getId().toString());
    		return false;
    	}
		PrintStream out = new PrintStream(output);
		Target oldTarget = target;
		if (target == null) {
			// doesn't do any good
			logger4j.debug("PSTT: EQUIVALENT MUTANT! No target to remove " + idAttr.toString());
			out.println("<!--PSTT: EQUIVALENT MUTANT! No target to remove " + idAttr.toString() + "-->");
			return false;
		} else {
			out.println("<!--PSTT: Removed target " + idAttr.toString() + "-->");
			target = null;
		}
		encode(output, indenter);
		// restore
		target = oldTarget;		
		return true;
	}
	

	public boolean oneToEmpty(OutputStream output, Indenter indenter) {

		if (this instanceof PolicySet) {
			PrintStream out = new PrintStream(output);
	        String indent = indenter.makeString();

	        out.println(indent + "<PolicySet PolicySetId=\"" + getId().toString() +
	                    "\" PolicyCombiningAlgId=\"" +
	                    getCombiningAlg().getIdentifier().toString() +
	                    "\">");
	        
	        indenter.in();
	        String nextIndent = indenter.makeString();

	        String description = getDescription();
	        if (description != null)
	            out.println(nextIndent + "<Description>" + description +
	                        "</Description>");

	        String version = getDefaultVersion();
	        if (version != null)
	            out.println("<PolicySetDefaults><XPathVersion>" + version +
	                        "</XPathVersion></PolicySetDefaults>");

	        encodeCommonElements(output, indenter, "oneToEmpty");

	        indenter.out();
	        out.println(indent + "</PolicySet>");
	        return true;
		} else if (this instanceof Policy) {
			PrintStream out = new PrintStream(output);
	        String indent = indenter.makeString();

	        out.println(indent + "<Policy PolicyId=\"" + getId().toString() +
	                    "\" RuleCombiningAlgId=\"" +
	                    getCombiningAlg().getIdentifier().toString() +
	                    "\">");
	        
	        indenter.in();
	        String nextIndent = indenter.makeString();

	        String description = getDescription();
	        if (description != null)
	            out.println(nextIndent + "<Description>" + description +
	                        "</Description>");

	        String version = getDefaultVersion();
	        if (version != null)
	            out.println("<PolicyDefaults><XPathVersion>" + version +
	                        "</XPathVersion></PolicyDefaults>");

	        if (target != null)
	    		target.encode(output, indenter);
	    	
	    	Iterator it = children.iterator();
	    	while (it.hasNext()) {
	    		AbstractPolicyTreeElement pte = (AbstractPolicyTreeElement) it.next();
	    		pte.encodeMutant(out, indenter);
	    	}
	    	
	    	if (obligations.size() != 0) {	    		
	    		out.println(indent + "<Obligations>");
	    		indenter.in();
	    		
	    		it = obligations.iterator();
	    		while (it.hasNext()) {
	    			((Obligation)(it.next())).encode(output, indenter);
	    		}
	    		
	    		out.println(indent + "</Obligations>");
	    		indenter.out();
	    	}

	        indenter.out();
	        out.println(indent + "</Policy>");
	        return true;
		} else if (this instanceof PolicyReference) {
			logger4j.error("How did I get here? " + getId().toString());
			return false;
		} else {
			logger4j.error("How did I get here? " + getId().toString());
			return false;
		}
	} 

	public String toString() {
		return idAttr.toString();
	}
}
