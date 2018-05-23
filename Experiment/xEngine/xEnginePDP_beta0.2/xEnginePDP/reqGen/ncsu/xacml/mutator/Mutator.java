package reqGen.ncsu.xacml.mutator;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;


import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import reqGen.com.sun.xacml.AbstractPolicy;
import reqGen.com.sun.xacml.PDP;
import reqGen.com.sun.xacml.PDPConfig;
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
import reqGen.ncsu.margrave.MrgrvPoco;
import reqGen.ncsu.util.MethodLocator;
import reqGen.ncsu.util.Util;
import reqGen.ncsu.xacml.poco.RuntimeCoverage;



public class Mutator {
	
	static Logger logger = Logger.getLogger(Mutator.class);
		
	/**
	 * The identifiers
	 */
	public final static String[] ID = {
		"PSTT",
		"PSTF",
		"PTT",
		"PTF",
		"RTT",
		"RTF",
		"RCT",
		"RCF",
		"CPC",
		"CRC",
		"CPO",
		"CRO",
		"CRE",
		"RMPS",
		"RMP",
		"RMR",
		"oneToEmpty"
	};
	
	/**
	 * The descriptions
	 */
	public final static String[] DESCRIPTION = {
		"Policy Set Target True",
		"Policy Set Target False",
		"Policy Target True",
		"Policy Target False",
		"Rule Target True",
		"Rule Target False",
		"Rule Condition True",
		"Rule Condition False",
		"Change Policy Combining Algorithm",
		"Change Rule Combining Algorithm",
		"Change Policy Order",
		"Change Rule Order",
		"Change Rule Effect",
		"Remove Policy Set",
		"Remove Policy",
		"Remove Rule", 
		"one-to-empty"
	};
	
	
	private static String MUT = "Mutants";
	private static String EQUIV = "Equiv";
	private static String GEN = "GenTime";

	/**
	 * A hashtable that maps ID --> Description
	 */
	public final static Hashtable DESCRIP_TABLE;
	private final static Hashtable totals;
	
	static {
		DESCRIP_TABLE = new Hashtable(); 
		for (int i = 0; i < ID.length; i++) {
			DESCRIP_TABLE.put(ID[i], DESCRIPTION[i]);
		}
		
		totals = new Hashtable();
		for (int i = 0; i < ID.length; i++) {
			totals.put(ID[i] + MUT, new Double(0));
			totals.put(ID[i] + EQUIV, new Double(0));
			totals.put(ID[i] + GEN, new Double(0));
		}
	}	
		
	private MethodLocator locator;
	
	private PDP pdp = null;

    protected MutatorPolicyFinderModule policyModule;
		
	private String subject;
	
	private long genStart, genStop;		
	
	private String outputRoot;
	
	private String outputDirectory;
	
	public Mutator(String policySet, String description, boolean mutating) throws Exception {
		this(policySet, description, mutating, "../output");
	}
	
	public Mutator(String policySet, String description, boolean mutating, String output) throws Exception{
		this.outputRoot = output;
		this.subject = description;	
		String newPolicySet = policySet;
		if (mutating) {
			newPolicySet = copy(new MrgrvPoco(policySet));
		}
		locator = new MethodLocator(MutatorPolicyFinderModule.class);	
		policyModule = new MutatorPolicyFinderModule(newPolicySet);

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
        pdp = new PDP(new PDPConfig(attributeFinder, policyFinder, null));
        if (mutating) {
        		writeSubjectStats();
        }
	}
	
	private void writeSubjectStats() {

		RuntimeCoverage.reset(outputRoot + "/" + subject + "/empty-cov");
		HashSet set = new HashSet();
		set.add(policyModule.getRootPolicy());
		RuntimeCoverage.collectAllPolicies(set);
		
		File f = new File(Util.tablesDir(outputRoot) + "/" + Util.SUBJ_STATS_TAB);
		try {
			boolean firstRun = !f.exists();
			if (firstRun) {
				Util.createFile(f, logger);
			}
			PrintWriter out = new PrintWriter(new BufferedOutputStream(new FileOutputStream(f, true/*append*/)));
			if (firstRun) {
				out.write("Subject\tPolSet\tPol\tRule\tCond");
				out.write(Util.SEP);
			}
			int polSet = RuntimeCoverage.getPolicySetCount();
			int pol = RuntimeCoverage.getPolicyCount();
			int rule = RuntimeCoverage.getRuleCount();
			int cond = RuntimeCoverage.getCondCount();
				
			logger.info(
					subject + "," + 
					Util.getNumberFormat().format(polSet) + "," + 
					Util.getNumberFormat().format(pol) + "," +
					Util.getNumberFormat().format(rule) + "," + 
					Util.getNumberFormat().format(cond));
			out.write(
					subject + "\t" + 
					Util.getNumberFormat().format(polSet) + "\t" + 
					Util.getNumberFormat().format(pol) + "\t" +
					Util.getNumberFormat().format(rule) + "\t" + 
					Util.getNumberFormat().format(cond));
			out.write(Util.SEP);
			out.close();
		} catch (FileNotFoundException e) {
			logger.error("Could not create writer for " + f, e);
		}
	}
	
	private String copy(MrgrvPoco poco) throws Exception {
		if (subject == null) {
			// use a counter instead
			int filecount = 0;
			String[] polDirList = new File(Util.policiesDir(outputRoot)).list();
			if (polDirList == null) {
				filecount = 0;
			} else {
				filecount = polDirList.length;
			}
			subject = Integer.toString(filecount);
		}
		outputDirectory = Util.policiesDir(outputRoot) + "/" + subject + "/";
		File dir = new File(outputDirectory);
		if (dir.exists()) {
			logger.error("Attempting to write to existing directory = " + dir);
		} else if (!dir.mkdirs()) {
			logger.error("Failed to create directory = " + dir);
		}
		String policySet = poco.getFinderModule().copyPolicy(outputDirectory, subject);
		logger.info("Copied to " + dir + " using policy set " + policySet);
		return policySet;
	}
	
	public void copyRmRules(HashSet ruleIds, File output) {
		policyModule.copyRmRules(ruleIds, output);
	}
	
	public void copyRmAllConditions(File output) {
		policyModule.copyRmAllConditions(output);
	}
	
	public Hashtable createVersions(HashSet rules, File dir) {
		return policyModule.CRE(rules, dir);
	}
	
	public void createVersions(String op, File dir) {
		Object[] args = {dir};
		try {			
			Method m = locator.findMethod(op, args);
			// found method so lets create the output directory
			if (dir.exists()) {
				logger.error("Attempting to write to existing directory = " + dir);
			} else if (!dir.mkdirs()) {
				logger.error("Failed to create directory = " + dir);
			}
			// now create mutants
			logger.info("Mutating with " + op + ": " + DESCRIP_TABLE.get(op));
//			genStart = System.currentTimeMillis();
			m.invoke(policyModule, args);
//			if (detectEquiv) {
//				policyModule.markEquivalent(dir);
//			}
//			genStop = System.currentTimeMillis();
//			collectMetrics();
		} catch (IllegalArgumentException e) {
			logger.error("Illegal argument for " + op, e);
		} catch (IllegalAccessException e) {
			logger.error("Illegal access for " + op, e);
		} catch (InvocationTargetException e) {
			logger.error("Error invoking target for " + op, e);
		} catch (NoSuchMethodException e) {
			logger.error("No such method for " + op, e);
		}
	}
	
	/**
	 * Uses reflection to call specific operator
	 * @param op
	 */
	public File mutate(String op) {
		File dir = new File(outputDirectory + Util.MUTANT_DIR + "/" + op);
		return mutate(op, dir, false);
	}
	public File mutate(String op, File dir, boolean detectEquiv) {		
		Object[] args = {dir};
		try {			
			Method m = locator.findMethod(op, args);
			// found method so lets create the output directory
			if (dir.exists()) {
				logger.error("Attempting to write to existing directory = " + dir);
			} else if (!dir.mkdirs()) {
				logger.error("Failed to create directory = " + dir);
			}
			// now create mutants
			logger.info("Mutating with " + op + ": " + DESCRIP_TABLE.get(op));
			genStart = System.currentTimeMillis();
			m.invoke(policyModule, args);
			if (detectEquiv) {
				policyModule.markEquivalent(dir);
			}
			genStop = System.currentTimeMillis();
			collectMetrics();
		} catch (IllegalArgumentException e) {
			logger.error("Illegal argument for " + op, e);
		} catch (IllegalAccessException e) {
			logger.error("Illegal access for " + op, e);
		} catch (InvocationTargetException e) {
			logger.error("Error invoking target for " + op, e);
		} catch (NoSuchMethodException e) {
			logger.error("No such method for " + op, e);
		}
		return dir;
	}
		
	private void collectMetrics() {
		// equiv-mutants for each subject
		File equivMutants = new File(Util.tablesDir(outputRoot) + "/" + subject + Util.EQUIV_MUTANTS_TAB);
		try {
			boolean firstRun = !equivMutants.exists();
			if (firstRun) {
				Util.createFile(equivMutants, logger);
			}
			PrintWriter out = new PrintWriter(new BufferedOutputStream(new FileOutputStream(equivMutants, true/*append*/)));
			if (firstRun) {
				out.write("Operator\tMutants\tEquiv\tPercent\tGenTime");
				out.write(Util.SEP);
			}
			String op = policyModule.getLastOp();
			double numMutants = policyModule.getNumMutants();
			double numEquiv = policyModule.getNumEquivalent();
			double percent = (numMutants != 0) ? ((numEquiv / numMutants) * 100) : 0;
			double genTime = (double) (genStop - genStart) / (double) 1000;
			increment(op, numMutants, numEquiv, genTime);
			logger.info(
					"OP=" + op + "," + 
					"#MUT=" + Util.getNumberFormat().format(numMutants) + "," +
					"#EQM=" + Util.getNumberFormat().format(numEquiv) + "," + 
					"%=" + Util.getNumberFormat().format(percent) + "," +
					"sec=" + Util.getNumberFormat().format(genTime));
				
			out.write(
					op + "\t" + 
					Util.getNumberFormat().format(numMutants) + "\t" +
					Util.getNumberFormat().format(numEquiv) + "\t" + 
					Util.getNumberFormat().format(percent) + "\t" +
					Util.getNumberFormat().format(genTime));
			out.write(Util.SEP);
			out.close();
		} catch (FileNotFoundException e) {
			logger.error("Could not create writer for " + equivMutants, e);
		}	
	}
	
	private void increment(String op, double numMutants, double numEquiv, double genTime) {
		increment(op+MUT, numMutants);
		increment(op+EQUIV, numEquiv);
		increment(op+GEN, genTime);
	}
	
	private void increment(String key, double val) {
		double oldVal = ((Double) totals.get(key)).doubleValue();
		totals.put(key, new Double(oldVal+val));
	}
	
	public void outputTotals() {
		// equiv-mutants for totals
		File equivMutants = new File(Util.tablesDir(outputRoot) + "/" + Util.EQUIV_MUTANTS_TAB);
		try {
			Util.createFile(equivMutants, logger);
			PrintWriter out = new PrintWriter(new BufferedOutputStream(new FileOutputStream(equivMutants)));
			out.write("Operator\tMutants\tEquiv\tPercent\tGenTime");
			out.write(Util.SEP);
			for (int i = 0; i < ID.length; i++) {
				String op = ID[i];
				double numMutants = ((Double) totals.get(op+MUT)).doubleValue();
				double numEquiv = ((Double) totals.get(op+EQUIV)).doubleValue();
				double percent = (numMutants != 0) ? ((numEquiv / numMutants) * 100) : 0;
				double genTime = ((Double) totals.get(op+GEN)).doubleValue();
				logger.info(
						"OP=" + op + "," + 
						"#MUT=" + Util.getNumberFormat().format(numMutants) + "," +
						"#EQM=" + Util.getNumberFormat().format(numEquiv) + "," + 
						"%=" + Util.getNumberFormat().format(percent) + "," +
						"sec=" + Util.getNumberFormat().format(genTime));
				
				out.write(
						op + "\t" + 
						Util.getNumberFormat().format(numMutants) + "\t" +
						Util.getNumberFormat().format(numEquiv) + "\t" + 
						Util.getNumberFormat().format(percent) + "\t" +
						Util.getNumberFormat().format(genTime));
				out.write(Util.SEP);
			}
			out.close();
		} catch (FileNotFoundException e) {
			logger.error("Could not create writer for " + equivMutants, e);
		}	
	}
	
	public AbstractPolicy getRootPolicy() {
		return policyModule.getRootPolicy();
	}
	
	public ResponseCtx evaluate(RequestCtx request) {
		
//		System.out.println("111-2");
		
		return pdp.evaluate(request);
	}
	
	public File getPolicyFile() {
		return policyModule.getPolicyFile();
	}
	
	/**
	 * For printing a LaTeX table of identifiers and descriptors should additional mutators be implemented
	 * @return A LaTeX string for a table of identifiers and descriptors.
	 */
	public static String toLaTeXTableString() {	
		StringBuffer table = new StringBuffer();
		table.append("\\begin{table}[t]\n");
		table.append("\\begin{small}\n");
		table.append("\\begin{center}\n");
		table.append("\\caption{\\label{table:mutationops}Chosen mutation operators for XACML policies.}\n");
		table.append("\\begin{tabular}{|l|l|}\n");
		table.append("\\hline ID  & \\CenterCell{Description}\\\\\n");
		for (int i = 0; i < ID.length; i++) {
			table.append("\\hline " + ID[i] + " & " + DESCRIPTION[i] + "\\\\\n");
		}
		table.append("\\end{tabular}\n");
		table.append("\\end{center}\n");
		table.append("\\end{small}\n");
		table.append("\\end{table}\n");
		return table.toString();
	}
	
	public boolean isDecisionDeny() throws Exception {
		return policyModule.isDecisionDeny();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Util.setupLogger(Level.INFO);
//		Util.setupLogger(Level.DEBUG);
		if (args.length != 0 && args.length != 2) {
			logger.info("Usage: mutator <policy set> <subject name>");
			return;
		}
		logger.info("Cleaning output.");
		Util.cleanDir(new File("../output"), logger);
		try {
			if (args.length == 2) {
				String[] policies = {args[0]};
				String[] names = {args[1]};		
				run(policies, names);
			} else {
				String[] policies = {
						"../xacml-subjects/margrave/simple/simple-policy.xml", 
						"../xacml-subjects/margrave/codeA/RPSlist.xml",
//						"../xacml-subjects/margrave/codeB/RPSlist.xml",
//						"../xacml-subjects/margrave/codeC/RPSlist.xml",
//						"../xacml-subjects/margrave/codeD/RPSlist.xml",
//						"../xacml-subjects/margrave/continue-a/RPSlist.xml",
//						"../xacml-subjects/margrave/continue-b/RPSlist.xml",
//						"../xacml-subjects/conference/conference.xml",
//						"../xacml-subjects/fedora/default-2/RPSlist.xml",
//						"../xacml-subjects/fedora/modifedFedora/MyPolicySet.xml",
//						"../xacml-subjects/fedora/demo-5/demo-5.xml",
//						"../xacml-subjects/fedora/demo-11/demo-11.xml",
//						"../xacml-subjects/fedora/demo-26/demo-26.xml",
//						"../xacml-subjects/sun-samples/generated/generated.xml",
//						"../xacml-subjects/sun-samples/obligation/obligation.xml",
//						"../xacml-subjects/sun-samples/selector/selector.xml"
				};
				String[] names = {
						"simple-policy", 
						"codeA",
//						"codeB",
//						"codeC",
//						"codeD",
//						"continue-a",
//						"continue-b",
//						"conference",
//						"default-2",
//						"mod-fedora",
//						"demo-5",
//						"demo-11",
//						"demo-26",
//						"generated",
//						"obligation",
//						"selector"
				};
				run(policies, names);
			}
			
		} catch (Exception e) {
			logger.error("Error in Mutator.main()", e);
		}
//		Util.texifyTablesDir(logger);
		logger.info("DONE");
	}

	public static void run(String[] policies, String[] names) throws Exception {
		for (int j = 0; j < names.length; j++) {
			Mutator mutator = new Mutator(policies[j], names[j], true);
						
			// all rule ops
			mutator.mutate("RTT");
			mutator.mutate("RTF");
			mutator.mutate("RCT");
			mutator.mutate("RCF");
			mutator.mutate("CRE");
			mutator.mutate("RMR");
			
			// all policy ops
			mutator.mutate("PTT");
			mutator.mutate("PTF");
			mutator.mutate("CRC");
//			mutator.mutate("CRO");
			mutator.mutate("RMP");
			
			// policy set ops
			mutator.mutate("PSTT");
			mutator.mutate("PSTF");
			mutator.mutate("CPC");
//			mutator.mutate("CPO");
			mutator.mutate("RMPS");
			
			mutator.outputTotals();
			
//			for (int i = 0; i < ID.length; i++) {
//				mutator.mutate(ID[i]);
//			}
		}
	}
}
