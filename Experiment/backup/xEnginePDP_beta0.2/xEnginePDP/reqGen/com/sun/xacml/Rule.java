
/*
 * @(#)Rule.java
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
import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import reqGen.com.sun.xacml.attr.AnyURIAttribute;
import reqGen.com.sun.xacml.attr.AttributeDesignator;
import reqGen.com.sun.xacml.attr.AttributeValue;
import reqGen.com.sun.xacml.attr.BooleanAttribute;
import reqGen.com.sun.xacml.attr.StringAttribute;
import reqGen.com.sun.xacml.cond.Apply;
import reqGen.com.sun.xacml.cond.EvaluationResult;
import reqGen.com.sun.xacml.cond.Function;
import reqGen.com.sun.xacml.cond.FunctionFactory;
import reqGen.com.sun.xacml.ctx.Result;
import reqGen.com.sun.xacml.ctx.Status;



/**
 * Represents the RuleType XACML type. This has a target for matching, and
 * encapsulates the condition and all sub-operations that make up the heart
 * of most policies.
 *
 * @since 1.0
 * @author Seth Proctor
 */
public class Rule extends AbstractPolicyTreeElement
{

    // the attributes associated with this Rule
    private URI idAttr;
    private int effectAttr;

    // the elements in the rule, each of which is optional
    private String description = null;
    private Target target = null;
    private Apply condition = null;

    /**
     * Creates a new <code>Rule</code> object.
     *
     * @param id the rule's identifier
     * @param effect the effect to return if the rule applies (either
     *               Pemit or Deny) as specified in <code>Result</code>
     * @param description a textual description, or null
     * @param target the rule's target, or null if the target is to be
     *               inherited from the encompassing policy
     * @param condition the rule's condition, or null if there is none
     */
    public Rule(URI id, int effect, String description,  Target target,
                Apply condition) {
        idAttr = id;
        effectAttr = effect;
        this.description = description;
        this.target = target;
        this.condition = condition;
    }

    /**
     * Returns a new instance of the <code>Rule</code> class based on a
     * DOM node. The node must be the root of an XML RuleType.
     *
     * @param root the DOM root of a RuleType XML type
     * @param xpathVersion the XPath version to use in any selectors or XPath
     *                     functions, or null if this is unspecified (ie, not
     *                     supplied in the defaults section of the policy)
     *
     * @throws ParsingException if the RuleType is invalid
     */
    public static Rule getInstance(Node root, String xpathVersion)
        throws ParsingException
    {
        URI id = null;
        String name = null;
        int effect = 0;
        String description = null;
        Target target = null;
        Apply condition = null;

        // first, get the attributes
        NamedNodeMap attrs = root.getAttributes();

        try {
            // get the two required attrs...
            id = new URI(attrs.getNamedItem("RuleId").getNodeValue());
        } catch (URISyntaxException use) {
            throw new ParsingException("Error parsing required attribute " +
                                       "RuleId", use);
        }

        String str = attrs.getNamedItem("Effect").getNodeValue();
        if (str.equals("Permit")) {
            effect = Result.DECISION_PERMIT;
        } else if (str.equals("Deny")) {
            effect = Result.DECISION_DENY;
        } else {
            throw new ParsingException("Invalid Effect: " + effect);
        }

        // next, get the elements
        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String cname = child.getNodeName();

            if (cname.equals("Description")) {
                description = child.getFirstChild().getNodeValue();
            } else if (cname.equals("Target")) {
                target = Target.getInstance(child, xpathVersion);
            } else if (cname.equals("Condition")) {
                condition = Apply.getConditionInstance(child, xpathVersion);
            }
        }

        return new Rule(id, effect, description, target, condition);
    }

    /**
     * Returns the effect that this <code>Rule</code> will return from
     * the evaluate method (Permit or Deny) if the request applies.
     *
     * @return a decision effect, as defined in <code>Result</code>
     */
    public int getEffect() {
        return effectAttr;
    }

    /**
     * Returns the id of this <code>Rule</code>
     *
     * @return the rule id
     */
    public URI getId() {
        return idAttr;
    }

    /**
     * Returns the given description of this <code>Rule</code> or null if 
     * there is no description
     *
     * @return the description or null
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the target for this <code>Rule</code> or null if there
     * is no target
     *
     * @return the rule's target
     */
    public Target getTarget() {
        return target;
    }

    /**
     * Since a rule is always a leaf in a policy tree because it can have
     * no children, this always returns an empty <code>List</code>.
     *
     * @return a <code>List</code> with no elements
     */
    public List getChildren() {
        return Collections.EMPTY_LIST;
    }

    /**
     * Returns the condition for this <code>Rule</code> or null if there
     * is no condition
     *
     * @return the rule's condition
     */
    public Apply getCondition() {
        return condition;
    }

    /**
     * Given the input context sees whether or not the request matches this
     * <code>Rule</code>'s <code>Target</code>. Note that unlike the matching
     * done by the <code>evaluate</code> method, if the <code>Target</code>
     * is missing than this will return Indeterminate. This lets you write
     * your own custom matching routines for rules but lets evaluation
     * proceed normally.
     *
     * @param context the representation of the request
     *
     * @return the result of trying to match this rule and the request
     */
    public MatchResult match(EvaluationCtx context) {
        if (target == null) {
            ArrayList code = new ArrayList();
            code.add(Status.STATUS_PROCESSING_ERROR);
            Status status = new Status(code, "no target available for " +
                                       "matching a rule");

            return new MatchResult(MatchResult.INDETERMINATE, status);
        }

        return target.match(context);
    }

    /**
     * Evaluates the rule against the supplied context. This will check that
     * the target matches, and then try to evaluate the condition. If the
     * target and condition apply, then the rule's effect is returned in
     * the result.
     * <p>
     * Note that rules are not required to have targets. If no target is
     * specified, then the rule inherits its parent's target. In the event
     * that this <code>Rule</code> has no <code>Target</code> then the
     * match is assumed to be true, since evaluating a policy tree to this
     * level required the parent's target to match.
     *
     * @param context the representation of the request we're evaluating
     *
     * @return the result of the evaluation
     */
    public Result evaluate(EvaluationCtx context) {
        // If the Target is null then it's supposed to inherit from the
        // parent policy, so we skip the matching step assuming we wouldn't
        // be here unless the parent matched
        if (target != null) {
            MatchResult match = target.match(context);
            int result = match.getResult();

            // if the target didn't match, then this Rule doesn't apply
            if (result == MatchResult.NO_MATCH)
                return new Result(Result.DECISION_NOT_APPLICABLE,
                                  context.getResourceId().encode());

            // if the target was indeterminate, we can't go on
            if (result == MatchResult.INDETERMINATE)
                return new Result(Result.DECISION_INDETERMINATE,
                                  match.getStatus(),
                                  context.getResourceId().encode());
        }

//        ncsu.xacml.poco.RuntimeCoverage.coverRule(this);
        // if there's no condition, then we just return the effect...
        if (condition == null)
            return new Result(effectAttr, context.getResourceId().encode());

        // ...otherwise we evaluate the condition
        EvaluationResult result = condition.evaluate(context);
        
        if (result.indeterminate()) {
            // if it was INDETERMINATE, then that's what we return
            return new Result(Result.DECISION_INDETERMINATE,
                              result.getStatus(),
                              context.getResourceId().encode());
        } else {
            // otherwise we return the effect on tue, and NA on false
            BooleanAttribute bool =
                (BooleanAttribute)(result.getAttributeValue());

            if (bool.getValue()) {
            	reqGen.ncsu.xacml.poco.RuntimeCoverage.coverConditionofRule(this, condition, true);
                return new Result(effectAttr,
                                  context.getResourceId().encode());
            }
            else {
            	reqGen.ncsu.xacml.poco.RuntimeCoverage.coverConditionofRule(this, condition, false);
                return new Result(Result.DECISION_NOT_APPLICABLE,
                                  context.getResourceId().encode());
            }
        }
    }

//    /**
//     * Encodes this <code>Rule</code> into its XML representation and writes
//     * this encoding to the given <code>OutputStream</code> with no
//     * indentation.
//     *
//     * @param output a stream into which the XML-encoded data is written
//     */
//    public void encode(OutputStream output) {
//        encode(output, new Indenter(0));
//    }

    /**
     * Encodes this <code>Rule</code> into its XML representation and writes
     * this encoding to the given <code>OutputStream</code> with
     * indentation.
     *
     * @param output a stream into which the XML-encoded data is written
     * @param indenter an object that creates indentation strings
     */
    public void encode(OutputStream output, Indenter indenter) {
        PrintStream out = new PrintStream(output);
        String indent = indenter.makeString();

        out.print(indent + "<Rule RuleId=\"" + idAttr.toString() +
                  "\" Effect=\"" + Result.DECISIONS[effectAttr] + "\"");

        if ((description != null) || (target != null) || (condition != null)) {
            // there is some content in the Rule
            out.println(">");

            indenter.in();
            String nextIndent = indenter.makeString();
            
            if (description != null)
                out.println(nextIndent + "<Description>" + description +
                            "</Description>");
            
            if (target != null)
                target.encode(output, indenter);
            
            if (condition != null)
                condition.encode(output, indenter);

            indenter.out();
            out.println(indent + "</Rule>");
        } else {
            // the Rule is empty, so close the tag and we're done
            out.println("/>");
        }
    }

    public boolean encode(OutputStream output, Indenter indenter, PolicyTreeElement pte) {
    	if (pte == null) {
    		encode(output, indenter);
    		return false;
    	} else if (pte.getId().toString().equals(this.getId().toString())) {
    		// replace my encoding with pte's mutated encoding
    		return pte.encodeMutant(output, indenter);
    	} else {
    		encode(output, indenter);
    		return false;
    	}
    }

//	public boolean CPC(OutputStream output, Indenter indenter) {
//		logger4j.error("Can not change policy combining algorithm on <Rule> element. " + getId().toString());		
//		return false;
//	}

//	public boolean CPO(OutputStream output, Indenter indenter) {
//		logger4j.error("Can not change policy order on <Rule> element. " + getId().toString());	
//		return false;
//	}

//	public boolean CRC(OutputStream output, Indenter indenter) {
//		logger4j.error("Can not change rule condition on <Rule> element. " + getId().toString());			
//		return false;
//	}

	public boolean CRE(OutputStream output, Indenter indenter) {
		PrintStream out = new PrintStream(output);
		out.println("<!--CRE: Changed rule effect " + idAttr.toString() + "-->");
		int oldEffect = effectAttr;
		if (effectAttr == 0) {
			effectAttr = 1;
		} else if (effectAttr == 1) {
			effectAttr = 0;
		} else {
			out.println("<!--Error: Incorrect rule attribute for CRE: " + idAttr.toString() + "-->");
			logger4j.error("Error: Incorrect rule attribute for CRE: " + idAttr.toString());
			return false;
		}
		encode(output, indenter);
		// restore
		effectAttr = oldEffect;
		return true;
	}

//	public boolean CRO(OutputStream output, Indenter indenter) {
//		logger4j.error("Can not change rule order on <Rule> element. " + getId().toString());	
//		return false;
//	}

	public boolean PTF(OutputStream output, Indenter indenter) {
		logger4j.error("Can not set policy target false on <Rule> element. " + getId().toString());
		return false;
	}

	public boolean PTT(OutputStream output, Indenter indenter) {
		logger4j.error("Can not set policy target true on <Rule> element. " + getId().toString());
		return false;
	}

	public boolean RCF(OutputStream output, Indenter indenter) {
		PrintStream out = new PrintStream(output);
		Apply oldCondition = condition;
		out.println("<!--RCF: Mutated condition " + idAttr.toString() + "-->");
		
		// Create a bogus condition that "always" fails
		List conditionArgs = new ArrayList();
		
		// get the function that the condition uses
		FunctionFactory factory = FunctionFactory.getConditionInstance();
		Function conditionFunction = null;
		try {
			conditionFunction = factory.createFunction("urn:oasis:names:tc:xacml:1.0:function:string-equal");
		} catch (Exception e) {
			logger4j.error("Failed to create condition function in RCF for " + getId().toString(), e);
			return false;
		}
		
		// now create the apply section that gets the designator value
		List applyArgs = new ArrayList();
		
		factory = FunctionFactory.getGeneralInstance();
		Function applyFunction = null;
		try {
			applyFunction = factory.createFunction("urn:oasis:names:tc:xacml:1.0:function:string-one-and-only");
		} catch (Exception e) {
			logger4j.error("Failed to create apply function in RCF for " + getId().toString(), e);
			return false;
		}
		
		URI designatorType;
		try {
			designatorType = new URI("http://www.w3.org/2001/XMLSchema#string");
			URI designatorId =	new URI("mydesignatorid-xyz-abc");
			URI designatorIssuer =	new URI("mydesignatorissuer-xyz-abc");
			AttributeDesignator designator = new AttributeDesignator(
					AttributeDesignator.SUBJECT_TARGET,
					designatorType, 
					designatorId, 
					false,
					designatorIssuer);
			applyArgs.add(designator);
		} catch (URISyntaxException e) {
			logger4j.error("Failed to create designator in RCF for " + getId().toString(), e);
			return false;
		}
		
		Apply apply = new Apply(applyFunction, applyArgs, false);
		
		// add the new apply element to the list of inputs to the condition
		conditionArgs.add(apply);
		
		// create an AttributeValue and add it to the input list
		StringAttribute value = new StringAttribute("mutant-mutant-xyz-abc-pleasedontmatch");
		conditionArgs.add(value);	
		
		// set to bogus condtion
		condition = new Apply(conditionFunction, conditionArgs, true);
		encode(output, indenter);
		
		// restore
		condition = oldCondition;	
		return true;
	}

	public boolean RCT(OutputStream output, Indenter indenter) {
		PrintStream out = new PrintStream(output);
		Apply oldCondition = condition;
		if (condition == null) {
			// doesn't do any good
			logger4j.debug("RCT: EQUIVALENT MUTANT! No condition to remove " + idAttr.toString());
			out.println("<!--RCT: EQUIVALENT MUTANT! No condition to remove " + idAttr.toString() + "-->");
			encode(output, indenter);
			return false;
		} else {
			out.println("<!--RCT: Removed condition " + idAttr.toString() + "-->");
			condition = null;
		}
		encode(output, indenter);
		// restore
		condition = oldCondition;
		return true;
	}

	public boolean RMP(OutputStream output, Indenter indenter) {
		logger4j.error("Can not remove policy on <Rule> element. " + getId().toString());
		return false;
	}

	public boolean RMR(OutputStream output, Indenter indenter) {
		PrintStream out = new PrintStream(output);
		out.println("<!--RMR: Removed rule " + idAttr.toString() + "-->");
		return true;
	}

	public boolean RTF(OutputStream output, Indenter indenter) {
		PrintStream out = new PrintStream(output);
		Target oldTarget = target;
		out.println("<!--RTF: Mutated target " + idAttr.toString() + "-->");
		
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
			logger4j.error("Error mutating.");
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

	public boolean RTT(OutputStream output, Indenter indenter) {
		PrintStream out = new PrintStream(output);
		Target oldTarget = target;
		if (target == null) {
			// doesn't do any good
			logger4j.debug("RTT: EQUIVALENT MUTANT! No target to remove " + idAttr.toString());
			out.println("<!--RTT: EQUIVALENT MUTANT! No target to remove " + idAttr.toString() + "-->");
			return false;
		} else {
			out.println("<!--RTT: Removed target " + idAttr.toString() + "-->");
			target = null;
		}
		encode(output, indenter);
		// restore
		target = oldTarget;		
		return true;
	}
    
    public boolean PSTF(OutputStream output, Indenter indenter) {
    	logger4j.error("Can not set policy set target false on <Rule> element. " + getId().toString());		
		return false;
	}

	public boolean PSTT(OutputStream output, Indenter indenter) {
		logger4j.error("Can not set policy set target true on <Rule> element. " + getId().toString());		
		return false;
	}

	public boolean RMPS(OutputStream output, Indenter indenter) {
		logger4j.error("Can not remove set policy set on <Rule> element. " + getId().toString());		
		return false;
	}

	public boolean oneToEmpty(OutputStream output, Indenter indenter) {
		logger4j.error("Can not do oneToEmpty on <Rule> element. " + getId().toString());
		return false;
	}

	public void setId(String id) {
    	try {
    		idAttr = new URI(id);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
	public String toString() {
		return idAttr.toString();
	}
}
