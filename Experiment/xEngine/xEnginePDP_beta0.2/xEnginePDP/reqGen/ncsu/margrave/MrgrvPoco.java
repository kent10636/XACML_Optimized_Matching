package reqGen.ncsu.margrave;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import reqGen.com.sun.xacml.Indenter;
import reqGen.com.sun.xacml.PDP;
import reqGen.com.sun.xacml.PDPConfig;
import reqGen.com.sun.xacml.ParsingException;
import reqGen.com.sun.xacml.cond.FunctionFactory;
import reqGen.com.sun.xacml.cond.FunctionFactoryProxy;
import reqGen.com.sun.xacml.cond.StandardFunctionFactory;
import reqGen.com.sun.xacml.ctx.RequestCtx;
import reqGen.com.sun.xacml.ctx.ResponseCtx;
import reqGen.com.sun.xacml.finder.AttributeFinder;
import reqGen.com.sun.xacml.finder.PolicyFinder;
import reqGen.com.sun.xacml.finder.impl.CurrentEnvModule;
import reqGen.com.sun.xacml.finder.impl.SelectorModule;
import reqGen.com.sun.xacml.tests.TimeInRangeFunction;
import reqGen.ncsu.xacml.poco.RuntimeCoverage;



public class MrgrvPoco {

	 // this is the actual PDP object we'll use for evaluation
    private PDP pdp = null;

    protected MrgrvPolicyFinderModule mrgrvPolicyModule;
    /**
     * Constructor that takes an array of filenames, each of which
     * contains an XACML policy, and sets up a <code>PDP</code> with access
     * to these policies only. The <code>PDP</code> is configured
     * programatically to have only a few specific modules.
     *
     * @param policyFiles an arry of filenames that specify policies
     */
    public MrgrvPoco(String listFile) throws Exception {
        // Create a PolicyFinderModule and initialize it...in this case,
        // we're using the sample FilePolicyModule that is pre-configured
        // with a set of policies from the filesystem
//        FilePolicyModule filePolicyModule = new FilePolicyModule();
//        for (int i = 0; i < policyFiles.length; i++)
//            filePolicyModule.addPolicy(policyFiles[i]);
        
        mrgrvPolicyModule = new MrgrvPolicyFinderModule(listFile);

        // next, setup the PolicyFinder that this PDP will use
        PolicyFinder policyFinder = new PolicyFinder();
        Set policyModules = new HashSet();
//        policyModules.add(filePolicyModule);
        policyModules.add(mrgrvPolicyModule);
        policyFinder.setModules(policyModules);

        // now setup attribute finder modules for the current date/time and
        // AttributeSelectors (selectors are optional, but this project does
        // support a basic implementation)
        CurrentEnvModule envAttributeModule = new CurrentEnvModule();
        SelectorModule selectorAttributeModule = new SelectorModule();

        // Setup the AttributeFinder just like we setup the PolicyFinder. Note
        // that unlike with the policy finder, the order matters here. See the
        // the javadocs for more details.
        AttributeFinder attributeFinder = new AttributeFinder();
        List attributeModules = new ArrayList();
        attributeModules.add(envAttributeModule);
        attributeModules.add(selectorAttributeModule);
        attributeFinder.setModules(attributeModules);

        // Try to load the time-in-range function, which is used by several
        // of the examples...see the documentation for this function to
        // understand why it's provided here instead of in the standard
        // code base.
        FunctionFactoryProxy proxy =
            StandardFunctionFactory.getNewFactoryProxy();
        FunctionFactory factory = proxy.getConditionFactory();
        factory.addFunction(new TimeInRangeFunction());
        FunctionFactory.setDefaultFactory(proxy);

        // finally, initialize our pdp
        pdp = new PDP(new PDPConfig(attributeFinder, policyFinder, null));
    }

    /**
     * Evaluates the given request and returns the Response that the PDP
     * will hand back to the PEP.
     *
     * @param requestFile the name of a file that contains a Request
     *
     * @return the result of the evaluation
     *
     * @throws IOException if there is a problem accessing the file
     * @throws ParsingException if the Request is invalid
     */
    public ResponseCtx evaluate(String requestFile)
        throws IOException, ParsingException
    {
        // setup the request based on the file
        RequestCtx request =
            RequestCtx.getInstance(new FileInputStream(requestFile));

        return this.evaluate(request);
    }
    
    public ResponseCtx evaluate(RequestCtx request) {
    		return pdp.evaluate(request);
    }

    public Set getPolicySet() {
    		return mrgrvPolicyModule.getPolicySet();
    }
    
    public MrgrvPolicyFinderModule getFinderModule() {
    	return mrgrvPolicyModule;
    }
    
    /**
     * Main-line driver for this sample code. This method lets you invoke
     * the PDP directly from the command-line.
     *
     * @param args the input arguments to the class. They are either the
     *             flag "-config" followed by a request file, or a request
     *             file followed by one or more policy files. In the case
     *             that the configuration flag is used, the configuration
     *             file must be specified in the standard java property,
     *             com.sun.xacml.PDPConfigFile.
     */
    public static void main(String [] args) throws Exception {
        if (args.length < 2) {
            System.out.println("       <request> <policy list>");
            System.exit(1);
        }
        
        MrgrvPoco simplePDP = null;
        String requestFile = null;
        requestFile = args[0];
        simplePDP = new MrgrvPoco(args[1]);
        

        RuntimeCoverage.setRequestFile(requestFile);
        // evaluate the request
        ResponseCtx response = simplePDP.evaluate(requestFile);

        // for this sample program, we'll just print out the response and output to a file
        String responseFileName; 
        if (requestFile.endsWith(".xml")) {
        	responseFileName = requestFile.substring(0, requestFile.length()-4) + "Response" + ".xml";
        } else {
        	responseFileName = requestFile + "Response" + ".xml";
        }
        PrintStream p = new PrintStream (new FileOutputStream (responseFileName));
        response.encode(p, new Indenter());        
        
        response.encode(System.out, new Indenter());        
        RuntimeCoverage.outputCoverageStatistics(System.out);
    }

}
