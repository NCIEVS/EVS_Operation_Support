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

	public static void setup_diff(String owlfile1, String dataDir1, String owlfile2, String dataDir2) {
		File f = new File(dataDir1);
		if (!f.exists()) {
			f.mkdir();
		}
		f = new File(dataDir2);
		if (!f.exists()) {
			f.mkdir();
		}
		NCItUtils.generateReports(owlfile1, dataDir1);
		NCItUtils.generateReports(owlfile2, dataDir2);
	}

	public static void run_diff(String dataDir1, String dataDir2, String outputDir) {
		String cwd = System.getProperty("user.dir");
		Vector files1 = listFilesInDirectory(dataDir1);
		Vector files2 = listFilesInDirectory(dataDir2);
		for (int i=0; i<files1.size(); i++) {
			String filename = (String) files1.elementAt(i);
			String pathname1 = cwd + File.separator + dataDir1 + File.separator + (String) files1.elementAt(i);
			String pathname2 = cwd + File.separator + dataDir2 + File.separator + (String) files1.elementAt(i);
			int n = filename.lastIndexOf(".");
			String label = filename.substring(0, n);
			Vector v = run(label, pathname1, pathname2);
			Utils.saveToFile(cwd + File.separator + outputDir + File.separator + label + "_diff.txt", v);
		}
	}

	public static void run(String owlfile1, String dataDir1, String owlfile2, String dataDir2, String outputDir) {
		File f = new File(outputDir);
		if (!f.exists()) {
			f.mkdir();
		}
		setup_diff(owlfile1, dataDir1, owlfile2, dataDir2);
        run_diff(dataDir1, dataDir2, outputDir);
	}

    public static Vector run(String label, String pathname1, String pathname2) {
		System.out.println("\nComputing differences between:");
		System.out.println("\t" + pathname1);
		System.out.println("\t" + pathname2);

		Vector w = new Vector();
		Vector v1 = Utils.readFile(pathname1);
		Vector v2 = Utils.readFile(pathname2);
		HashSet set1 = Utils.vector2HashSet(v1);
		HashSet set2 = Utils.vector2HashSet(v2);

		HashSet clonedSet1 = new HashSet<>(set1);
		HashSet clonedSet2 = new HashSet<>(set2);
		HashSet set_diff = setDifference(clonedSet1, clonedSet2);
		Vector v = dumpHashSet(label, DELETE, set_diff);
		w.addAll(v);

		clonedSet1 = new HashSet<>(set1);
		clonedSet2 = new HashSet<>(set2);
		set_diff = setDifference(clonedSet2, clonedSet1);
		v = dumpHashSet(label, ADD, set_diff);
		w.addAll(v);
		set1.clear();
		set2.clear();
		System.out.println("\t" + label + "_diff: " + w.size());
		return w;
	}

	public static void main(String[] args) {
		long ms = System.currentTimeMillis();
        String owlfile1 = args[0];
        String owlfile2 = args[1];
        String dataDir1 = "OLD";
        String dataDir2 = "NEW";
        String outputDir = "output";
        run(owlfile1, dataDir1, owlfile2, dataDir2, outputDir);
        System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}
}

