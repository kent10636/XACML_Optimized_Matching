package reqGen.ncsu.xacml.mutator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;


import org.apache.log4j.Logger;

import reqGen.com.sun.xacml.PDP;
import reqGen.com.sun.xacml.PDPConfig;
import reqGen.com.sun.xacml.cond.FunctionFactory;
import reqGen.com.sun.xacml.cond.FunctionFactoryProxy;
import reqGen.com.sun.xacml.cond.StandardFunctionFactory;
import reqGen.com.sun.xacml.finder.AttributeFinder;
import reqGen.com.sun.xacml.finder.PolicyFinder;
import reqGen.com.sun.xacml.finder.impl.CurrentEnvModule;
import reqGen.com.sun.xacml.finder.impl.SelectorModule;
import reqGen.com.sun.xacml.tests.TimeInRangeFunction;
import reqGen.ncsu.margrave.MrgrvCIScriptGenerator;
import reqGen.ncsu.margrave.MrgrvExec;
import reqGen.ncsu.util.Util;
import reqGen.ncsu.xacml.RequestFactoryIntf;


public class FindEquivalent {
	
	protected static Logger logger = Logger.getLogger(FindEquivalent.class);
	
	private File subjectDir;
	protected MutatorPolicyFinderModule policyModule;
	private String policySet;	
	private PrintStream outStream;
	private double numMut, oldEquiv, newEquiv;
	private long startT, stopT;
	
	public FindEquivalent(File subjectDir) throws Exception {
		this.subjectDir = subjectDir;		
		
		// find policy 
		File[] files = subjectDir.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				if (!files[i].getName().equals(Util.MUTANT_DIR)) {
					logger.warn("Unexpected directory found " + files[i]);
				}
			} else {
				if (policySet == null) {
					policySet = files[i].getPath();
				} else {
					logger.warn("Policy set already defined as " + policySet);
					logger.warn("Ignoring unexpected file " + files[i]);
				}
			}
		}
		
		policyModule = new MutatorPolicyFinderModule(policySet);

        // next, setup the PolicyFinder that this PDP will use
        PolicyFinder policyFinder = new PolicyFinder();
        Set policyModules = new HashSet();
        policyModules.add(policyModule);
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
        FunctionFactoryProxy proxy = StandardFunctionFactory.getNewFactoryProxy();
        FunctionFactory factory = proxy.getConditionFactory();
        factory.addFunction(new TimeInRangeFunction());
        FunctionFactory.setDefaultFactory(proxy);

        // finally, initialize our pdp
        PDP pdp = new PDP(new PDPConfig(attributeFinder, policyFinder, null));
	}
	
	public void markEquivalent(String outputDir) {
		clearMetrics();
		File mutantDir = new File(subjectDir + "/" + Util.MUTANT_DIR);
		File[] files = mutantDir.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				logger.info("Processing " + files[i].getName());
				startT = System.currentTimeMillis();
				policyModule.markEquivalent(files[i]);
				stopT = System.currentTimeMillis();
				appendSummary(outputDir, files[i].getName());
			} else {
				logger.warn("Ignoring unexpected file " + files[i]);
			}
		}
		outStream.close();
	}
	
	public void markEquivalent(File opDir) {
		logger.info("Finding equivalent mutants...");
		File original = policyModule.getPolicyFile();
		File[] mutants = opDir.listFiles();
		for (int i = 0; i < mutants.length; i++) {
			numMut++;
			if (mutants[i].getName().contains("equiv")) {
				// skip preidentified equivalent mutants
				oldEquiv++;
				continue;
			}
			MrgrvCIScriptGenerator.generateScript(original.getPath(), mutants[i].getPath());
			RequestFactoryIntf factory = MrgrvExec.execScript(MrgrvCIScriptGenerator.getScriptName());
			logger.debug("for " + mutants[i]);
			try {
				if (factory.nextRequest() == null || !factory.hasNext()) {
					System.out.print("+");
					// equivalent mutant detected
					newEquiv++;
					String path = mutants[i].getParent();
					String name = "/equiv-" + mutants[i].getName();
					if (!mutants[i].renameTo(new File(path + name))) {
						logger.error("Failed to rename " + mutants[i] + " to " + path + name);	
					}
				} else {
					System.out.print(".");
				}
			} catch (Exception e) {
				logger.error("Exception thrown while detecting equivalent mutants for " + mutants[i], e);
			}
		}	
		System.out.println();
	}
		
	private void appendSummary(String outputDir, String op) {		
		if (outStream == null) {
			File summary = new File(Util.tablesDir(outputDir) + "/" + subjectDir.getName() + "/" + Util.MRGRV_EQUIV_MUTANTS_TAB);
			Util.createFile(summary, logger);
			try {
				outStream = new PrintStream(summary);
			} catch (FileNotFoundException e) {
				logger.error("File not found " + summary, e);
			}
			outStream.println("Operator\tMutants\tOldEquiv\tNewEquiv\tEquiv\tPercent\tTime");
		} else {
			String line = op + "\t" + 
				Util.getNumberFormat().format(numMut) + "\t" + 
				Util.getNumberFormat().format(oldEquiv) + "\t" + 
				Util.getNumberFormat().format(newEquiv) + "\t" + 
				Util.getNumberFormat().format(oldEquiv + newEquiv) + "\t" + 
				Util.getNumberFormat().format((oldEquiv + newEquiv)/numMut) + "\t" + 
				Util.getNumberFormat().format((double) (stopT - startT) / (double) 1000);
			outStream.println(line);
			logger.info(line);
		}
	}
	
	private void clearMetrics() {
		numMut = 0;
		oldEquiv = 0;
		newEquiv = 0;
		startT = 0;
		stopT = 0;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Util.setupLogger();
		logger.info("Starting");
		File[] subjects = new File[1];
		if (args.length == 1) {
			subjects[0] = new File(args[0]);
		} else {
			subjects = new File("../saved-output/policies/").listFiles();
		}
		for (int i = 0; i < subjects.length; i++) {
			FindEquivalent finder;
			try {
				logger.info("Detecting equivalents for " + subjects[i]);
				finder = new FindEquivalent(subjects[i]);
				finder.markEquivalent("../output");
			} catch (Exception e) {
				logger.error("Error. ", e);
			}

		}
		logger.info("Done");
	}

}
