package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;
import java.io.*;
import java.net.URI;
import java.text.*;
import java.util.*;

public class BatchMatchRunner {
	static String AXIOM_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.axiomfile;
    static String TERMFILE = "termfile.txt";
    String inputDir = null;
    String outputDir = null;
    String currentDir = null;
    String termfile = null;

    static {
		createTermFile();
	}

    public BatchMatchRunner(String inputDir, String outputDir) {
		this.inputDir = inputDir;
		this.outputDir = outputDir;
	}

	public static void createTermFile() {
		Vector v = Utils.readFile(AXIOM_FILE);
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (line.indexOf("P90") != -1) {
				w.add(line);
			}
		}
		Utils.saveToFile(TERMFILE, w);
	}

    public void run() {
		run(0);
	}

    public void run(int colNum) {
		currentDir = System.getProperty("user.dir");
		String input_dir = currentDir + File.separator + inputDir;
		File f = new File(input_dir);
		if (!f.exists()) {
			System.out.println("Directory " + input_dir + " does not exists - program abort.");
			System.exit(0);
		}
		String output_dir = currentDir + File.separator + outputDir;
		f = new File(output_dir);
		if (!f.exists()) {
			boolean bool = FileUtils.createDirectory(output_dir);
			System.out.println("Directory " + output_dir + " created.");
		}
		Vector inputfiles = FileUtils.listFileNames(input_dir);
		Utils.dumpVector("inputfiles", inputfiles);

		for (int i=0; i<inputfiles.size(); i++) {
			String datafile = (String) inputfiles.elementAt(i);
			f = new File(datafile);
			String outputfile = "results_" + f.getName();
			runExactMatch(datafile, colNum, outputfile);
			FileUtils.copyfile(outputfile, currentDir + File.separator + outputDir + File.separator + outputfile);
			FileUtils.deleteFile(outputfile);
			System.out.println("\n");
		}
		generateExcelReport();
	}

	public void addHeading(String filename) {
		Vector v = Utils.readFile(filename);
		String firstLine = (String) v.elementAt(0);
		System.out.println(firstLine);
		firstLine = firstLine.toLowerCase();
		if (firstLine.indexOf("term") == -1) {
			System.out.println("WARNING: Missing heading line.");
			Vector w = new Vector();
			w.add("Term");
			w.addAll(v);
			Utils.saveToFile(filename, w);
		}
	}



	public void generateExcelReport() {
		String excelfile = outputDir + ".xlsx";
		System.out.println("excelfile: " + excelfile);
		char delim = '\t';
		System.out.println("Calling generateExcelReport...");
		Text2Excel.generateExcel(outputDir, excelfile, delim);
	}

	public void runExactMatch(String datafile, int colNum, String outputfile) {
		File f = new File(datafile);
		addHeading(datafile);
		System.out.println("Running " + f.getName());
		System.out.println("outputfile: " + outputfile);
		new ExactMatch(TERMFILE).run(datafile, outputfile, colNum);
	}

	public static void main(String args[]) {
		long ms = System.currentTimeMillis();
		String inputDir = args[0];
		String outputDir = args[1];
		int colNum = 0;
		if (args.length > 2) {
			String col_str = args[2];
			colNum = Integer.parseInt(col_str);
		}
		new BatchMatchRunner(inputDir, outputDir).run(colNum);
		System.out.println("\tTotal run time (ms): " + (System.currentTimeMillis() - ms));
	}
}
