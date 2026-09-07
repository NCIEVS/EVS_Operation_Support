package gov.nih.nci.evs.restapi.util;

import java.io.*;
import java.util.*;

public class PostDiffAnalyzer {
	public static String DELETE = "Delete";
	public static String ADD = "Add";
	private OWLClassLoader ldr1 = null;
	private OWLClassLoader ldr2 = null;

	String owlfile1 = null;
	String dataDir1 = null;
	String owlfile2 = null;
	String owlfile3 = null;
	String dataDir2 = null;
	String outputDir = null;

	public PostDiffAnalyzer(String owlfile1, String dataDir1, String owlfile2, String dataDir2, String outputDir, String owlfile3) {
        this.owlfile1 = owlfile1;
        this.dataDir1 = dataDir1;
        this.owlfile2 = owlfile2;
        this.dataDir2 = dataDir2;
        this.outputDir = outputDir;
        this.owlfile3 = owlfile3;
        initialize();
	}

	public void initialize() {
		long ms = System.currentTimeMillis();
		System.out.println("Setting up baseline (asserted OWL).");
		setUp();

        //System.out.println("removeRetiredConceptBranch " + owlfile2);
		//removeRetiredConceptBranch(owlfile2);
		//owlfile2 = "trimmed_" + owlfile2;

        System.out.println("Setting up NEW and OLD folders ... " + owlfile2);
		NCItDiff.run(owlfile1, dataDir1, owlfile2, dataDir2, outputDir);

        System.out.println("Setting up OWLClassLoader " + owlfile1);
		ldr1 = new OWLClassLoader(owlfile1);

		System.out.println("Setting up OWLClassLoader " + owlfile2);
		ldr2 = new OWLClassLoader(owlfile2);

		System.out.println("Total initialziation run time (ms): " + (System.currentTimeMillis() - ms));
	}

/*
	public static void removeRetiredConceptBranch(String inferredOWL) {
		System.out.println("removeRetiredConceptBranch: " + inferredOWL);
		BranchTrimmer test = new BranchTrimmer(inferredOWL);
		//Vector w = test.trim(test.get_retired_concepts());
		Vector w = test.trim(new HashSet());
		Utils.saveToFile("trimmed_" + inferredOWL, w);
	}
*/

    public void setUp() {
		File f = new File("BASELINE");
		if (!f.exists()) {
			f.mkdir();
		}
		NCItUtils.generateReports(owlfile3, "BASELINE");
	}

    public static Vector listFilesInDirectory() {
		String dirName = null;
		return listFilesInDirectory(dirName);
	}

    public static Vector listFilesInDirectory(String dirName) {
		Vector v = new Vector();
		if (dirName == null) {
			dirName = System.getProperty("user.dir");;
		}
        File f = new File(dirName);
        String[] pathnames = f.list();
        for (String pathname : pathnames) {
            v.add(pathname);
        }
        return v;
	}

	public void run() {
		long ms = System.currentTimeMillis();
		String cwd = System.getProperty("user.dir");
		String dir = cwd + File.separator + outputDir;
        Vector files = listFilesInDirectory(dir);
        Utils.dumpVector("files", files);
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}

/*
Data Type	Edit Action	Data Value
A8	Delete	C219693|A8|C177537
A8	Delete	C3844|A8|C177537
A8	Delete	C185876|A8|C176424
A8	Delete	C171322|A8|C116977
*/

	public void run(String filename) {
		String cwd = System.getProperty("user.dir");
		String dir = cwd + File.separator + outputDir;
		String filepath = dir + File.separator + filename;
		Vector v = Utils.readFile(filepath);
		String line = (String) v.elementAt(1);
		Vector u = StringUtils.parseData(line, '\t');
		String data = (String) u.elementAt(2);
		Vector u2 = StringUtils.parseData(data, '|');
		String code = (String) u2.elementAt(0);
		Vector class_vec_1 = ldr1.getClassData(code);
		Utils.dumpVector("class_vec_1", class_vec_1);
		Vector class_vec_2 = ldr2.getClassData(code);
		Utils.dumpVector("class_vec_2", class_vec_2);
	}

//java -Xms1024m -Xmx8g -classpath %CLASSPATH% PostDiffAnalyzer ThesaurusInferred-26.05d-ftp.owl ThesaurusInferred_forTS_09-05-2026.owl Thesaurus-260526-26.05d.owl
	public static void main(String[] args) {
		long ms = System.currentTimeMillis();
        String owlfile1 = args[0];
        String owlfile2 = args[1];
        String owlfile3 = args[2];
        String dataDir1 = "OLD";
        String dataDir2 = "NEW";
        String outputDir = "output";
        PostDiffAnalyzer test = new PostDiffAnalyzer(owlfile1, dataDir1, owlfile2, dataDir2, outputDir, owlfile3);
        //test.run("A8_diff.txt");
        System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}
}

