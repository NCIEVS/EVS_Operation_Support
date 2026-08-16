package gov.nih.nci.evs.restapi.util;

import java.io.*;
import java.util.*;

public class NCItDiff {
	public static String DELETE = "Delete";
	public static String ADD = "Add";

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

    public static HashSet setDifference(HashSet set1, HashSet set2) {
		set1.removeAll(set2);
		return set1;
	}

    public static HashSet setDifference(Vector v1, Vector v2) {
		HashSet set1 = Utils.vector2HashSet(v1);
		HashSet set2 = Utils.vector2HashSet(v2);
        return setDifference(set1, set2);
	}

	public static Vector dumpHashSet(String label, String action, HashSet set) {
		Vector w = new Vector();
		Iterator it = set.iterator();
		while (it.hasNext()) {
			String t = (String) it.next();
			String s = label + "\t" + action + "\t" + t;
			w.add(s);
		}
		return w;
	}

	public static void setup_diff(String owlfile1, String outputDir1, String owlfile2, String outputDir2) {
		File f = new File(outputDir1);
		if (!f.exists()) {
			f.mkdir();
		}
		f = new File(outputDir2);
		if (!f.exists()) {
			f.mkdir();
		}

		NCItUtils.generateReports(owlfile1, outputDir1);
		NCItUtils.generateReports(owlfile2, outputDir2);

	}

	public static void run_diff(String outputDir1, String outputDir2) {
		String cwd = System.getProperty("user.dir");
		Vector w = new Vector();
		Vector files1 = listFilesInDirectory(outputDir1);
		Utils.dumpVector(outputDir1, files1);

		Vector files2 = listFilesInDirectory(outputDir2);
		Utils.dumpVector(outputDir2, files2);

		for (int i=0; i<files1.size(); i++) {
			String filename = (String) files1.elementAt(i);
			String pathname = cwd + File.separator + outputDir1 + File.separator + (String) files1.elementAt(i);
			Vector v = Utils.readFile(pathname);
			System.out.println(pathname + ": " + v.size());
		}

		System.out.println("\n");
		for (int i=0; i<files2.size(); i++) {
			String filename = (String) files2.elementAt(i);
			String pathname = cwd + File.separator + outputDir2 + File.separator + (String) files2.elementAt(i);
			Vector v = Utils.readFile(pathname);
			System.out.println(pathname + ": " + v.size());
		}

		for (int i=0; i<files1.size(); i++) {
			String filename = (String) files1.elementAt(i);
			String pathname1 = cwd + File.separator + outputDir1 + File.separator + (String) files1.elementAt(i);
			String pathname2 = cwd + File.separator + outputDir2 + File.separator + (String) files1.elementAt(i);
			int n = filename.lastIndexOf(".");
			String label = filename.substring(0, n);
			w = new Vector();
			Vector v1 = Utils.readFile(pathname1);
			Vector v2 = Utils.readFile(pathname2);
			HashSet set1 = Utils.vector2HashSet(v1);
			HashSet set2 = Utils.vector2HashSet(v2);
			HashSet set_diff = setDifference(set1, set2);
			Vector v = dumpHashSet(label, DELETE, set_diff);
			w.addAll(v);
			set_diff = setDifference(set2, set1);
			v = dumpHashSet(label, ADD, set_diff);
			w.addAll(v);
			Utils.saveToFile(label + "_diff.txt", w);
		}
	}

	public static void run(String owlfile1, String outputDir1, String owlfile2, String outputDir2) {
		setup_diff(owlfile1, outputDir1, owlfile2, outputDir2);
        run_diff(outputDir1, outputDir2);
	}

	public static void main(String[] args) {
		long ms = System.currentTimeMillis();
        String owlfile1 = args[0];
        String owlfile2 = args[1];

        String outputDir1 = "OLD";
        String outputDir2 = "NEW";
        run(owlfile1, outputDir1, owlfile2, outputDir2);

        System.out.println("\tTotal run time (ms): " + (System.currentTimeMillis() - ms));
	}
}

