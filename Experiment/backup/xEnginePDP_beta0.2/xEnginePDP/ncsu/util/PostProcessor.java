package ncsu.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class PostProcessor {

	private PostProcessor() {
	}

	public static void computeTotals(File dir, File outFile, String fileName) {
		System.out.println("Totaling " + fileName);

		Vector theFiles = new Vector();
		// collect files in the vector
		File[] dirs = dir.listFiles();
		for (int i = 0; i < dirs.length; i++) {
			if (dirs[i].isDirectory()) {
				File[] files = dirs[i].listFiles();
				for (int j = 0; j < files.length; j++) {
					if (files[j].getName().equals(fileName)) {
						theFiles.add(files[j]);
						break;
					}
				}
			}
		}
		// process files
		Hashtable rows = new Hashtable();
		for (Enumeration e = theFiles.elements(); e.hasMoreElements();) {
			try {
				File f = (File) e.nextElement();
				BufferedReader in = new BufferedReader(new FileReader(f));
				String line = in.readLine(); // skip first line
				while ((line = in.readLine()) != null) {
					String[] split = line.split("\t");
					String key = split[0];
					Vector val = new Vector();
					if (!rows.containsKey(key)) {
						for (int i = 1; i < split.length; i++) {
							double newVal = parse(split[i]);
							val.add(i - 1, new Double(newVal));
						}
					} else {
						Vector oldValV = (Vector) rows.get(key);
						for (int i = 1; i < split.length; i++) {
							double oldVal = ((Double) oldValV.get(i - 1))
									.doubleValue();
							double newVal = parse(split[i]);
							val.add(i - 1, new Double(oldVal + (newVal)));
						}
					}
					rows.put(key, val);
				}
				in.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		// write output
		try {
			PrintWriter out = new PrintWriter(outFile);
			for (Enumeration keys = rows.keys(); keys.hasMoreElements();) {
				Object key = keys.nextElement();
				StringBuffer line = new StringBuffer();
				line.append(key);
				for (Enumeration vals = ((Vector) rows.get(key)).elements(); vals
						.hasMoreElements();) {
					line.append("\t");
					line.append(vals.nextElement());
				}
				out.println(line.toString());
			}
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Output to " + outFile);
	}

	public static void computeAverage(File dir, File outFile, String fileName) {
		System.out.println("Averaging " + fileName);

		Vector theFiles = new Vector();
		// collect files in the vector
		File[] dirs = dir.listFiles();
		for (int i = 0; i < dirs.length; i++) {
			if (dirs[i].isDirectory()) {
				File[] files = dirs[i].listFiles();
				for (int j = 0; j < files.length; j++) {
					if (files[j].getName().equals(fileName)) {
						theFiles.add(files[j]);
						break;
					}
				}
			}
		}
		// process files
		Hashtable rows = new Hashtable();
		double n = (double) theFiles.size();
		for (Enumeration e = theFiles.elements(); e.hasMoreElements();) {
			try {
				File f = (File) e.nextElement();
				BufferedReader in = new BufferedReader(new FileReader(f));
				String line = in.readLine(); // skip first line
				while ((line = in.readLine()) != null) {
					String[] split = line.split("\t");
					String key = split[0];
					Vector val = new Vector();
					if (!rows.containsKey(key)) {
						for (int i = 1; i < split.length; i++) {
							double newVal = parse(split[i]);
							val.add(i - 1, new Double(newVal / n));
						}
					} else {
						Vector oldValV = (Vector) rows.get(key);
						for (int i = 1; i < split.length; i++) {
							double oldVal = ((Double) oldValV.get(i - 1))
									.doubleValue();
							double newVal = parse(split[i]);
							val.add(i - 1, new Double(oldVal + (newVal / n)));
						}
					}
					rows.put(key, val);
				}
				in.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		// write output
		try {
			PrintWriter out = new PrintWriter(outFile);
			for (Enumeration keys = rows.keys(); keys.hasMoreElements();) {
				Object key = keys.nextElement();
				StringBuffer line = new StringBuffer();
				line.append(key);
				for (Enumeration vals = ((Vector) rows.get(key)).elements(); vals
						.hasMoreElements();) {
					line.append("\t");
					line.append(vals.nextElement());
				}
				out.println(line.toString());
			}
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Output to " + outFile);
	}

	public static double parse(String str) {
		Number num;
		try {
			num = Util.getNumberFormat().parse(str);
			return num.doubleValue();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File dir = new File(args[0]);
		File out = new File(args[1]);
		computeAverage(dir, out, args[2]);
		computeTotals(dir, out, args[2]);
	}

}
