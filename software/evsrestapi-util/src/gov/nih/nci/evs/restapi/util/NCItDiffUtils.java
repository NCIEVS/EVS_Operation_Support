package gov.nih.nci.evs.restapi.util;

import java.io.*;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Comparator;
import java.util.stream.Stream;
import java.util.stream.*;

public class NCItDiffUtils {
	String owlfile1 = null;
	String owlfile2 = null;

	OWLClassLoader loader1 = null;
	HashMap classDataHashMap1 = null;
	Vector classIdVec1 = null;

	OWLClassLoader loader2 = null;
	HashMap classDataHashMap2 = null;
	Vector classIdVec2 = null;

	public NCItDiffUtils(String owlfile1, String owlfile2) {
		this.owlfile1 = owlfile1;
		this.owlfile2 = owlfile2;
		File f1 = new File(owlfile1);
		if (!f1.exists()) {
			System.out.println(owlfile1 + " does not exist.");
			System.exit(0);
		}
		File f2 = new File(owlfile2);
		if (!f2.exists()) {
			System.out.println(owlfile2 + " does not exist.");
			System.exit(0);
		}
		initialize();
	}

	public void initialize() {
		loader1 = new OWLClassLoader(owlfile1);
		classDataHashMap1 = loader1.getClassDataHashMap();
		classIdVec1 = loader1.getClassIdVec();
		int n = owlfile1.lastIndexOf(".");
		Utils.saveToFile("codes_" + owlfile1.substring(0, n) + ".txt", classIdVec1);

		loader2 = new OWLClassLoader(owlfile2);
		classDataHashMap2 = loader2.getClassDataHashMap();
		classIdVec2 = loader2.getClassIdVec();
		n = owlfile2.lastIndexOf(".");
		Utils.saveToFile("codes_" + owlfile2.substring(0, n) + ".txt", classIdVec2);
		Vector w = compareClasses();
		Utils.saveToFile("diff_codes.txt", w);
		//loader1.clear();
		//loader2.clear();
	}

	public static Vector compareHierarchies(String owlfile1, String owlfile2) {
		Vector v1 = Utils.readFile(owlfile1);
		OWLScanner scanner1 = new OWLScanner(v1);
		Vector parent_child_vec1 = scanner1.extractHierarchicalRelationships(v1);
		Vector v2 = Utils.readFile(owlfile2);
		OWLScanner scanner2 = new OWLScanner(v2);
		Vector parent_child_vec2 = scanner2.extractHierarchicalRelationships(v2);

		Vector w = new Vector();
		w.add(owlfile1 + "\t" + owlfile2);
		Vector w1 = v1_v2(parent_child_vec1, parent_child_vec2);
		for (int i=0; i<w1.size(); i++) {
			String line = (String) w1.elementAt(i);
			w.add(line + "\t");
		}
		Vector w2 = v1_v2(parent_child_vec2, parent_child_vec1);
		for (int i=0; i<w2.size(); i++) {
			String line = (String) w2.elementAt(i);
			w.add("|" + line);
		}

		parent_child_vec1.clear();
		parent_child_vec2.clear();
		scanner1.get_owl_vec().clear();
		scanner2.get_owl_vec().clear();
        return w;
	}

	public static Vector compareProperties(String owlfile1, String owlfile2) {
		System.out.println(owlfile1);
		System.out.println(owlfile2);
		long ms = System.currentTimeMillis();
		OWLClassLoader loader1 = new OWLClassLoader(owlfile1);
		HashMap classDataHashMap1 = loader1.getClassDataHashMap();
		Vector classIdVec1 = loader1.getClassIdVec();
		System.out.println("classIdVec1: " + classIdVec1.size());

		OWLClassLoader loader2 = new OWLClassLoader(owlfile2);
		HashMap classDataHashMap2 = loader1.getClassDataHashMap();
		Vector classIdVec2 = loader1.getClassIdVec();
		System.out.println("classIdVec2: " + classIdVec2.size());

		Vector diff_codes = new Vector();
		if (classIdVec1.size() != classIdVec2.size()) {
			System.out.println("WARNING: Number of classes - " + classIdVec1.size() + " versus " + classIdVec2.size());
		}
        Vector w = new Vector();
        Vector diff_vec = new Vector();
		int lcv = 0;
		int increment = 10000;
		int total = classIdVec1.size();
		SortUtils sort = new SortUtils();
		for (int i=0; i<classIdVec1.size(); i++) {
			int j = i;
			if (lcv == increment) {
				if (j > 0) {
					System.out.println("" + j + " out of " + total + " completed.");
				}
				lcv = 0;
			}
			lcv++;
			String id = (String) classIdVec1.elementAt(i);
			if (classIdVec2.contains(id)) {
				Vector v1 = (Vector) classDataHashMap1.get(id);
				Vector v2 = (Vector) classDataHashMap2.get(id);
				OWLScanner scanner1 = new OWLScanner(v1);
				Vector prop_vec1 = scanner1.extractProperties(v1);
				OWLScanner scanner2 = new OWLScanner(v2);
				Vector prop_vec2 = scanner2.extractProperties(v2);
 				if (prop_vec1.size() != prop_vec2.size()) {
					String s = id + "\t" + prop_vec1.size() + "\t" + prop_vec2.size();
					w.add(s);
					diff_vec.add(id);
				} else {
					Vector v1_v2 = v1_v2(prop_vec1, prop_vec2);
					if (v1_v2.size() != 0) {
						v1 = sort.quickSort(v1);
						v2 = sort.quickSort(v2);
						for (int k=0; k<v1.size(); k++) {
							String value1 = (String) v1.elementAt(k);
							String value2 = (String) v2.elementAt(k);
							if (value1.compareTo(value2) != 0) {
								w.add(id + "|" + value1 + "|" + value2);
							}
						}
					}
				}
			}
		}
		System.out.println("" + total + " out of " + total + " completed.");
		Utils.saveToFile("diff_properties.txt", w);
		loader1.clear();
		loader2.clear();
		w.clear();
		System.out.println("\tTotal run time (ms): " + (System.currentTimeMillis() - ms));
		return diff_vec;
	}

	public Vector compareProperties() {
		Vector diff_codes = new Vector();
		if (classIdVec1.size() != classIdVec2.size()) {
			System.out.println("WARNING: Number of classes - " + classIdVec1.size() + " versus " + classIdVec2.size());
		}
        Vector w = new Vector();
        Vector diff_vec = new Vector();
		int lcv = 0;
		int increment = 10000;
		int total = classIdVec1.size();
		for (int i=0; i<classIdVec1.size(); i++) {
			int j = i;
			if (lcv == increment) {
				if (j > 0) {
					System.out.println("" + j + " out of " + total + " completed.");
				}
				lcv = 0;
			}
			lcv++;
			String id = (String) classIdVec1.elementAt(i);
			if (classIdVec2.contains(id)) {
				Vector v1 = (Vector) classDataHashMap1.get(id);
				Vector v2 = (Vector) classDataHashMap2.get(id);
				OWLScanner scanner1 = new OWLScanner(v1);
				Vector prop_vec1 = scanner1.extractProperties(v1);
				OWLScanner scanner2 = new OWLScanner(v2);
				Vector prop_vec2 = scanner2.extractProperties(v2);
 				if (prop_vec1.size() != prop_vec2.size()) {
					w.add(id + "\t" + prop_vec1.size() + "\t" + prop_vec2.size());
					diff_vec.add(id);
				}
			}
		}
		System.out.println("" + total + " out of " + total + " completed.");
		Utils.saveToFile("diff_properties.txt", w);
		return diff_vec;
	}

	public Vector compareRestrictions() {
		int num_diff = 0;
		Vector diff_codes = new Vector();
		if (classIdVec1.size() != classIdVec2.size()) {
			System.out.println("WARNING: Number of classes - " + classIdVec1.size() + " versus " + classIdVec2.size());
		}
        Vector w = new Vector();
		int lcv = 0;
		int increment = 10000;
		int total = classIdVec1.size();
		System.out.println("classIdVec1.size(): " + total);

		SortUtils sort = new SortUtils();

		for (int i=0; i<classIdVec1.size(); i++) {
			int j = i;
			if (lcv == increment) {
				if (j > 0) {
					System.out.println("" + j + " out of " + total + " completed.");
				}
				lcv = 0;
			}
			lcv++;
			String id = (String) classIdVec1.elementAt(i);
			if (classIdVec2.contains(id)) {
				Vector v1 = (Vector) classDataHashMap1.get(id);
				Vector v2 = (Vector) classDataHashMap2.get(id);

				OWLScanner scanner1 = new OWLScanner(v1);
				Vector role_vec1 = scanner1.extractOWLRestrictions(v1);
				OWLScanner scanner2 = new OWLScanner(v2);
				Vector role_vec2 = scanner2.extractOWLRestrictions(v2);

				scanner1.get_owl_vec().clear();
				scanner2.get_owl_vec().clear();

 				if (role_vec1.size() != role_vec2.size()) {
					w.add("\n" + id + "\t" + role_vec1.size() + "\t" + role_vec2.size());
					w.add(owlfile1);
					role_vec1 = new SortUtils().quickSort(role_vec1);
					w.addAll(role_vec1);
					w.add(owlfile2);
					role_vec2 = new SortUtils().quickSort(role_vec2);
					w.addAll(role_vec2);
					diff_codes.add(id);
				} else {
					boolean bool = true;
					role_vec1 = sort.quickSort(role_vec1);
					role_vec2 = sort.quickSort(role_vec2);
					for (int k=0; k<role_vec1.size(); k++) {
						String r1 = (String) role_vec1.elementAt(k);
						String r2 = (String) role_vec2.elementAt(k);
						if (r1.compareTo(r2) != 0) {
							w.add(owlfile1);
							w.addAll(role_vec1);
							w.add(owlfile2);
							w.addAll(role_vec2);
							diff_codes.add(id);
							break;
						}
					}
				}
			}
		}
		System.out.println("" + total + " out of " + total + " completed.");
		Utils.saveToFile("diff_roles.txt", w);
		w.add("Total number of different cases: " + diff_codes.size());
        return diff_codes;
	}

	public Vector compareClasses() {
		//owlfile1: generated
		//owlfile2: ftp
		int num_diff = 0;
		Vector diff_codes = new Vector();
		int n = owlfile1.lastIndexOf(".");
		Vector classIdVec1 = Utils.readFile("codes_" + owlfile1.substring(0, n) + ".txt");
		n = owlfile2.lastIndexOf(".");
		Vector classIdVec2 = Utils.readFile("codes_" + owlfile2.substring(0, n) + ".txt");

		if (classIdVec1.size() != classIdVec2.size()) {
			System.out.println("WARNING: Number of classes - " + classIdVec1.size() + " versus " + classIdVec2.size());
		}
		Vector w = new Vector();
		w.add(owlfile1 + "|" + owlfile2);
		Vector w1 = v1_v2(classIdVec1, classIdVec2);
		for (int i=0; i<w1.size(); i++) {
			String code = (String) w1.elementAt(i);
			w.add(code + "|");
		}
		Vector w2 = v1_v2(classIdVec2, classIdVec1);
		for (int i=0; i<w2.size(); i++) {
			String code = (String) w2.elementAt(i);
			w.add("|" + code);
		}
		Utils.saveToFile("diff_codes.txt", w);
        return w;
	}


	private static long getFileSize(String filename) {
		File file = new File(filename);
	    long length = file.length();
	    return length;
    }

	private static int getFileLength(String filename) {
		Vector v = Utils.readFile(filename);
		return v.size();
    }

    public static List listFilesInDirectory(String dir) {
		List<String> result = null;
		try {
			String currentPath = new java.io.File(".").getCanonicalPath();
			Path startPath = Paths.get(currentPath + File.separator + dir);
			try (Stream<Path> stream = Files.walk(startPath)) {
				result = stream
					.map(Path::toString)
					.collect(Collectors.toList());
			} catch (IOException e) {
				e.printStackTrace();
			}
    	} catch (Exception ex) {

		}
		return result;
    }

    public static void run(String observedDir, String expectedDir) {
		Vector w = new Vector();
		String path = FileUtils.getCurrentWorkingDirectory() + File.separator + observedDir;
		Vector filenames = FileUtils.listFileNames(path);
		Vector files = FileUtils.listFiles(path);
		for (int i=0; i<files.size(); i++) {
			File f1 = (File) files.elementAt(i);
			String path1 = FileUtils.getCurrentWorkingDirectory() + File.separator + observedDir + File.separator + f1.getName();
			String path2 = FileUtils.getCurrentWorkingDirectory() + File.separator + expectedDir + File.separator + f1.getName();
			File f2 = new File(path2);
			if (f2.exists()) {
				w.add("\n" + observedDir + " " + f1.getName() + " (length: " + getFileLength(path1) + " size: " + getFileSize(path1) + ")");
				w.add(expectedDir + " " + f2.getName() + " (length: " + getFileLength(path2) + " size: " + getFileSize(path2) + ")");
			}
		}
		Utils.saveToFile(observedDir + "_vs_" + expectedDir + ".txt", w);
	}

    // In v2 but not in v1
	public static Vector v1_v2(Vector v1, Vector v2) {
		Vector w = new Vector();
        HashSet hset = Utils.vector2HashSet(v2);
        for (int i=0; i<v1.size(); i++) {
			String t = (String) v1.elementAt(i);
			if (!hset.contains(t)) {
				w.add(t);
			}
		}
		return new SortUtils().quickSort(w);
	}

	public static void getPropertiesOfCodes(String owlfile, String propCode) { //P310
	    OWLScanner owlscanner = new OWLScanner(Utils.readFile(owlfile));
        Vector v = owlscanner.extractProperties(owlscanner.get_owl_vec(), propCode);
        Utils.saveToFile(propCode + ".txt", v);
        owlscanner.get_owl_vec().clear();
	}

	public static Vector checkCodesStatus(String owlfile, String codefile) {
		String statusfile = "P310.txt";
		File f = new File(statusfile);
		if (!f.exists()) {
			String propCode = "P310";
			getPropertiesOfCodes(owlfile, propCode);
		}
		Vector w = new Vector();
 		HashMap P310Map = DelimitedDataExtractor.createHashMap(Utils.readFile(statusfile), 0, 2, '|');
		Vector v = Utils.readFile(codefile);
		for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			System.out.println(line);
			Vector u = StringUtils.parseData(line, '|');
			String s1 = (String) u.elementAt(0);
			String s2 = (String) u.elementAt(1);
			String status = (String) P310Map.get(s1);
			if (StringUtils.isNCItCode(s1) && status != null) {
				w.add(line + "|" + status);
			}
		}

		return w;
	}

	public static Vector sampling(String owlfile1, String owlfile2, int m) {
		System.out.println(owlfile1);
		System.out.println(owlfile2);
		long ms = System.currentTimeMillis();
		OWLClassLoader loader1 = new OWLClassLoader(owlfile1);
		HashMap classDataHashMap1 = loader1.getClassDataHashMap();
		Vector classIdVec1 = loader1.getClassIdVec();
		System.out.println("classIdVec1: " + classIdVec1.size());

		OWLClassLoader loader2 = new OWLClassLoader(owlfile2);
		HashMap classDataHashMap2 = loader1.getClassDataHashMap();
		Vector classIdVec2 = loader1.getClassIdVec();
		System.out.println("classIdVec2: " + classIdVec2.size());

		Vector diff_codes = new Vector();
		if (classIdVec1.size() != classIdVec2.size()) {
			System.out.println("WARNING: Number of classes - " + classIdVec1.size() + " versus " + classIdVec2.size());
		}
		List list = new RandomVariateGenerator().selectWithNoReplacement(m, classIdVec1.size());

        Vector w = new Vector();
        Vector diff_vec = new Vector();
		int lcv = 0;
		int increment = 10000;
		int total = classIdVec1.size();
		SortUtils sort = new SortUtils();
		w = new Vector();
		for (int i=0; i<classIdVec1.size(); i++) {
			int j = i;
			if (lcv == increment) {
				if (j > 0) {
					System.out.println("" + j + " out of " + total + " completed.");
				}
				lcv = 0;
			}
			lcv++;
			Integer int_obj = Integer.valueOf(j);
			String id = (String) classIdVec1.elementAt(i);
			if (classIdVec2.contains(id)) {
				Vector v1 = (Vector) classDataHashMap1.get(id);
				Vector v2 = (Vector) classDataHashMap2.get(id);
				OWLScanner scanner1 = new OWLScanner(v1);
				Vector prop_vec1 = scanner1.extractProperties(v1);
				OWLScanner scanner2 = new OWLScanner(v2);
				Vector prop_vec2 = scanner2.extractProperties(v2);
				if (list.contains(int_obj)) {
					prop_vec1 = sort.quickSort(prop_vec1);
					prop_vec2 = sort.quickSort(prop_vec2);
					w.add("\n\n" + owlfile1 + " " + id);
					w.addAll(v1);
					w.addAll(prop_vec1);
					w.add("\n\n" + owlfile2 + " " + id);
					w.addAll(v2);
					w.addAll(prop_vec2);
				}
			}
		}
		System.out.println("" + total + " out of " + total + " completed.");

		loader1.clear();
		loader2.clear();
		System.out.println("\tTotal run time (ms): " + (System.currentTimeMillis() - ms));
		return w;
	}

	public static void main(String[] args) {
		long ms = System.currentTimeMillis();
		String owlfile1 = args[0];
		String owlfile2 = args[1];

		NCItDiffUtils test = new NCItDiffUtils(owlfile1, owlfile2);
		//test.compareProperties();

		System.out.println("Calling compareRestrictions ...");

		Vector w = test.compareRestrictions();
		Utils.saveToFile("diff_roles_codes.txt", w);
		/*
		Vector w = test.compareClasses();
		String codefile = "diff_codes.txt";
		Utils.saveToFile(codefile, w);
		w = checkCodesStatus(owlfile1, codefile);
		Utils.saveToFile("status_" + codefile, w);

        //Vector w = compareHierarchies(owlfile1, owlfile2);
        */
        //Vector w = compareProperties(owlfile1, owlfile2);
        //Utils.saveToFile("code_diff_properties.txt", w);

        //Vector w = sampling(owlfile1, owlfile2, 100);
        //Utils.saveToFile("sample_classes.txt", w);

        System.out.println("\tTotal run time (ms): " + (System.currentTimeMillis() - ms));
	}
}

