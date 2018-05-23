package ncsu.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

import org.apache.log4j.Logger;

public class LaTeXiFier {

	static Logger logger = Logger.getLogger(LaTeXiFier.class);
	
	private LaTeXiFier() {}
	
	public static void writeTableFromFile(File src) {
		writeTableFromFile(src, "caption", "label");
	}
	
	public static void writeTableFromFile(File src, String caption, String label) {
		writeTableFromFile(src, caption, label, "\t");
	}
	
	public static void writeTableFromFile(File src, String caption, String label, String delimiter) {
		File dest = new File(src.getPath().replace(".txt", ".tex"));
		writeTableFromFile(src, dest, caption, label, delimiter);
	}
	public static void writeTableFromFile(File src, File dest, String caption, String label, String delimiter) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(src));
			// read header
			String[] header = null;
			String line;
			if (reader.ready()) {
				line = reader.readLine();
				header = line.split(delimiter);
			}
			// read data
			Vector dataV = new Vector();
			int rowCount = 0;
			int colCount = 0;
			while (reader.ready()) {
				line = reader.readLine().trim();
				String[] row = line.split(delimiter);
				dataV.add(row);
				rowCount++;
				if (colCount != 0 && colCount != row.length) {
					logger.error("Inconsistent row length." + row.length + " Pray..." + src);
					logger.error(line);
				}
				colCount = row.length;
			}
			// convert data
			String[][] data = new String[rowCount][colCount];
			for (int i = 0; i < dataV.size(); i++) {
				data[i] = (String[]) dataV.get(i);
			}
			// write data
			writeTable(dest, caption, label, header, data);
		} catch (FileNotFoundException e) {
			logger.error("Could not find file " + src, e);
		} catch (IOException e) {
			logger.error("Error reading from file " + src, e);
		}
	}
	
	public static void writeTable(File f, String caption, String label, 
			String[] header, String[][] data) {
		if (f.isDirectory()) {
			logger.error("Can not write table to directory : " + f);
			return;
		}
		if (header == null) {
			logger.error("Header is null. " + f);
			return;
		}
		if (data == null) {
			logger.error("Data is null." + f);
			return;
		}
		if (header.length == 0) {
			logger.error("Header length is zero." + f);
			return;
		}
		if (data.length == 0) {
			logger.error("Number of rows is zero." + f);
			return;
		}
		if (data[0].length == 0) {
			logger.error("Number of columns is zero." + f);
			return;
		}
		if (header.length != data[0].length) {
			logger.error("Mismatch in header count and row count. Pray..." + f);
		}
		try {
			PrintWriter out = new PrintWriter(new BufferedOutputStream(new FileOutputStream(f)));
			out.write("\\begin{table}[t]\n");
			out.write("\\begin{small}\n");
			out.write("\\begin{center}\n");
			out.write("\\caption{\\label{" + label + "}" + caption + "}\n");
			out.write("\\begin{tabular}{|");
			for (int i = 0; i < header.length; i++) {
				out.write("l|");
			}
			out.write("}\n");
			out.write("\\hline " + header[0]); 
			for (int i = 1; i < header.length; i++) {
				out.write(" & " + header[i]);
			}
			out.write("\\\\\n");
			for (int row = 0; row < data.length; row++) {
				out.write("\\hline " + data[row][0]);
				for (int col = 1; col < data[0].length; col++) {
					out.write(" & " + data[row][col]);
				}
				out.write("\\\\\n");
			}
			out.write("\\hline\n");
			out.write("\\end{tabular}\n");
			out.write("\\end{center}\n");
			out.write("\\end{small}\n");
			out.write("\\end{table}\n");	
			out.close();
		} catch (FileNotFoundException e) {
			logger.error("Could not find file " + f, e);
		}
	}
	
	public static void main(String[] args) {
		Util.setupLogger();
		File[] file = new File("../saved-output/tables/").listFiles();
		Vector outputs = new Vector();
		for (int i = 0; i < file.length; i++) {
			if (file[i].isDirectory()) {
				File src = new File(file[i].getPath() + "/equiv-mutants.txt");
				File dest = new File("../saved-output/tex/" + file[i].getName() + ".tex");
				Util.createFile(dest, logger);
				outputs.add(dest.getName());
				String caption = "Equivalent mutants for " + file[i].getName();
				String label = "table:equivmutants" + file[i].getName();
				String delimiter = ",";
				writeTableFromFile(src, dest, caption, label, delimiter);
			} else {
				File src = file[i];
				File dest = new File("../saved-output/tex/" + file[i].getName() + ".tex");
				Util.createFile(dest, logger);
				outputs.add(dest.getName());
				String caption = "Equivalent mutants for " + file[i].getName();
				String label = "table:equivmutants" + file[i].getName();
				String delimiter = ",";
				writeTableFromFile(src, dest, caption, label, delimiter);
			}
		}
		File f= new File("../saved-output/tex/all.tex");
		Util.createFile(f, logger);
		try {
			PrintWriter out = new PrintWriter(f);
			out.println("\\documentclass[times, 10pt,twocolumn]{article}");
			out.println("\\usepackage{latex8}");
			out.println("\\usepackage{times}");
			out.println("\\pagestyle{empty}");
			out.println("\\begin{document}");
			out.println("\\thispagestyle{empty}");			
			for (int i = 0; i < outputs.size(); i++) {
				String name = outputs.elementAt(i).toString().replace(".tex", "");
				out.println("\\input{" + name + "}");
			}
			out.println("\\end{document}");
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
