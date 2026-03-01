package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;

import java.io.*;
import java.util.*;

public class NCItDiff {
	public static String ID_FILE = "label.txt";
	public static String HIER_FILE = "parent_child.txt";
	public static String ROLE_FILE = "roles.txt";
	public static String PROPERTY_FILE = "properties.txt";
	public static String OBJECT_PROPERTY_FILE = "objectProperies.txt";
	public static String SEMANTIC_TYPE_FILE = "P106.txt";
	public static String NCIT_OWL_FILE = "ThesaurusInferred_forTS.owl";
	public static String AXIOM_FILE = "axiom_ThesaurusInferred_forTS.txt";
	public static String ASSOCIATION_FILE = "association.txt";
	public static String VS_FILE = "A8.txt";

	static String serviceUrl = ConfigurationController.serviceUrl;
	static String namedGraph = ConfigurationController.namedGraph;
	static String username = ConfigurationController.username;
    static String password = ConfigurationController.password;

	static String[] root_array = null;

	static HierarchyHelper hh = null;
	static Vector roots = null;
	static Vector displayLabels = null;

	static String PRIMITIVE_CONCEPT_ROOTS = null;

	int numCompleted = 0;
	String outputfile = null;
	Vector semanticTypes = new Vector();
	Vector objectProperies = new Vector();

	String[] object_properties = null;
	HashMap roleMap = null;
	HashMap roleCode2LabelMap = null;
	HashMap semanticTypeMap = null;

	boolean REPORT_GENERATION_MODE = true;
	static String NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile;
	static OWLScanner owlscanner = null;
	Vector output_vec = null;

	static String PREV_FOLDER = "old";
	static String CURR_FOLDER = "new";

	static {
		File f = new File(PREV_FOLDER);
		if (!f.exists()) {
			f.mkdir();
		}
		f = new File(CURR_FOLDER);
		if (!f.exists()) {
			f.mkdir();
		}

		Vector files1 = listFilesInDirectory(PREV_FOLDER);
		Utils.dumpVector(PREV_FOLDER, files1);
		Vector files2 = listFilesInDirectory(CURR_FOLDER);
		Utils.dumpVector(CURR_FOLDER, files2);
	}

	public static OWLScanner getOWLScanner() {
		return owlscanner;
	}

	public static void clearOWLScanner() {
		if (owlscanner != null) {
			owlscanner.clear();
		}
	}

	public static void setOWLScanner(OWLScanner owl_scanner) {
		owlscanner.clear();
		owlscanner = owl_scanner;
	}

	public static HashMap createHashCode2StringMap(Vector v) {
		HashMap hmap = new HashMap();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			int hashcode = line.hashCode();
		    Integer int_obj = Integer.valueOf(hashcode);
		    Vector w = new Vector();
		    if (hmap.containsKey(int_obj)) {
				w = (Vector) hmap.get(int_obj);
			}
			if (!w.contains(line)) {
				w.add(line);
				w = new SortUtils().quickSort(w);
			}
		    hmap.put(int_obj, w);
		}
		return hmap;
	}



    public static Vector diff(String label, String outputfile1, String outputfile2) {
		HashMap hmap1 = createHashCode2StringMap(Utils.readFile(outputfile1));
        HashMap hmap2 = createHashCode2StringMap(Utils.readFile(outputfile2));

        Vector w = new Vector();
        w.add("Edit Action\tType\tPrevious Value\tCurrent Value");

        Vector diff_vec = new Vector();
        Iterator it1 = hmap1.keySet().iterator();
        while (it1.hasNext()) {
			Integer int_obj = (Integer) it1.next();
			Vector w1 = (Vector) hmap1.get(int_obj);
			if (!hmap2.containsKey(int_obj)) {
				for (int j=0; j<w1.size(); j++) {
					diff_vec.add("Delete\t" + label + "\t" + (String) w1.elementAt(j));
				}
			} else {
				Vector w2 = (Vector) hmap2.get(int_obj);
  				String s1 = Utils.vector2DelimitedString(w1);
  				String s2 = Utils.vector2DelimitedString(w2);
  				if (s1.compareTo(s2) != 0) {
					diff_vec.add("Modify\t" + label + "\t" + s1 + "\t" + s2);
				}
			}
		}
        Iterator it2 = hmap2.keySet().iterator();
        while (it2.hasNext()) {
			Integer int_obj = (Integer) it2.next();
			Vector w2 = (Vector) hmap2.get(int_obj);
			if (!hmap1.containsKey(int_obj)) {
				for (int j=0; j<w2.size(); j++) {
					diff_vec.add("Add\t" + label + "\t" + (String) w2.elementAt(j));
				}
			}
		}
		w.addAll(diff_vec);
		return w;
	}

    static int TYPE_ID = 0;
    static int TYPE_AXIOM = 1;
    static int TYPE_HIER = 2;
    static int TYPE_ROLE = 3;
    static int TYPE_ASSOCIATION = 4;
    static int TYPE_VS = 5;
    static int TYPE_PROPERTY = 6;


    public static HashMap getDataFileInfo(int type) {
		HashMap hmap = new HashMap();

		if (type == TYPE_ID) {
			hmap.put("label", "Label");
			hmap.put("file1", PREV_FOLDER + File.separator + ID_FILE);
		    hmap.put("file2", CURR_FOLDER + File.separator + ID_FILE);
		} else if (type == TYPE_AXIOM) {
			hmap.put("label", "Axiom");
			hmap.put("file1", PREV_FOLDER + File.separator + AXIOM_FILE);
		    hmap.put("file2", CURR_FOLDER + File.separator + AXIOM_FILE);
		} else if (type == TYPE_HIER) {
			hmap.put("label", "Hierarhy");
			hmap.put("file1", PREV_FOLDER + File.separator + HIER_FILE);
		    hmap.put("file2", CURR_FOLDER + File.separator + HIER_FILE);
		} else if (type == TYPE_ROLE) {
			hmap.put("label", "Role");
			hmap.put("file1", PREV_FOLDER + File.separator + ROLE_FILE);
		    hmap.put("file2", CURR_FOLDER + File.separator + ROLE_FILE);
		} else if (type == TYPE_ASSOCIATION) {
			hmap.put("label", "Association");
			hmap.put("file1", PREV_FOLDER + File.separator + ASSOCIATION_FILE);
		    hmap.put("file2", CURR_FOLDER + File.separator + ASSOCIATION_FILE);
		} else if (type == TYPE_VS) {
			hmap.put("label", "Subset");
			hmap.put("file1", PREV_FOLDER + File.separator + VS_FILE);
		    hmap.put("file2", CURR_FOLDER + File.separator + VS_FILE);
		} else if (type == TYPE_PROPERTY) {
			hmap.put("label", "Property");
			hmap.put("file1", PREV_FOLDER + File.separator + PROPERTY_FILE);
		    hmap.put("file2", CURR_FOLDER + File.separator + PROPERTY_FILE);
		}
		return hmap;
	}

	public static void generateDataFile(int type) {
		OWLScanner owlscanner = null;
		HashMap file_info_map = getDataFileInfo(type);
		String label = (String) file_info_map.get("label");
		String outputfile1 = (String) file_info_map.get("file1");
		String outputfile2 = (String) file_info_map.get("file2");

		System.out.println("type: " + type);
		System.out.println("\tlabel: " + label);
		System.out.println("\tfile1: " + outputfile1);
		System.out.println("\tfile2: " + outputfile2);


		File f = new File(outputfile1);
		if (!f.exists()) {
			owlscanner = new OWLScanner(PREV_FOLDER + File.separator + ConfigurationController.owlfile);
			Vector w = null;
			if (type == TYPE_ID) {
				w = owlscanner.extractRDFSLabels(owlscanner.get_owl_vec());
			} else if (type == TYPE_AXIOM) {
				w = owlscanner.extractAxiomData(null);
		    } else if (type == TYPE_HIER) {
				w = owlscanner.extractHierarchicalRelationships(owlscanner.get_owl_vec());
			} else if (type == TYPE_ROLE) {
				w = owlscanner.extractOWLRestrictions(owlscanner.get_owl_vec());
		    } else if (type == TYPE_ASSOCIATION) {
                w = owlscanner.extractAssociations(owlscanner.get_owl_vec());
		    } else if (type == TYPE_VS) {
				w = owlscanner.extractAssociations(owlscanner.get_owl_vec(), "A8");
		    } else if (type == TYPE_PROPERTY) {
				w = owlscanner.extractProperties(owlscanner.get_owl_vec());
			}
			Utils.saveToFile(outputfile1, w);
			HTMLDecoder.run(outputfile1);
			owlscanner.clear();
		}
		f = new File(outputfile2);
		if (!f.exists()) {
			owlscanner = new OWLScanner(CURR_FOLDER + File.separator + ConfigurationController.owlfile);
			Vector w = null;
			if (type == TYPE_ID) {
				w = owlscanner.extractRDFSLabels(owlscanner.get_owl_vec());
			} else if (type == TYPE_AXIOM) {
				w = owlscanner.extractAxiomData(null);
		    } else if (type == TYPE_HIER) {
				w = owlscanner.extractHierarchicalRelationships(owlscanner.get_owl_vec());
			} else if (type == TYPE_ROLE) {
				w = owlscanner.extractOWLRestrictions(owlscanner.get_owl_vec());
		    } else if (type == TYPE_ASSOCIATION) {
                w = owlscanner.extractAssociations(owlscanner.get_owl_vec());
		    } else if (type == TYPE_VS) {
				w = owlscanner.extractAssociations(owlscanner.get_owl_vec(), "A8");
		    } else if (type == TYPE_PROPERTY) {
				w = owlscanner.extractProperties(owlscanner.get_owl_vec());
			}

			Utils.saveToFile(outputfile2, w);
			HTMLDecoder.run(outputfile2);
			owlscanner.clear();
		}
	}

	public static void compare(int type) {
        generateDataFile(type);
 		HashMap file_info_map = getDataFileInfo(type);
		String label = (String) file_info_map.get("label");
		String outputfile1 = (String) file_info_map.get("file1");
		String outputfile2 = (String) file_info_map.get("file2");
		Vector diff_vec = diff(label, outputfile1, outputfile2);
		Utils.saveToFile("diff_" + label + ".txt", diff_vec);
	}

	public static void download() {
		NCItDownload.download();
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

    public static String searchForVersions() {
		Vector v = listFilesInDirectory();
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String t = (String) v.elementAt(i);
			if (t.endsWith(".xls")) {
				int n = t.indexOf("Terminology");
				String s = t.substring(n+ "Terminology".length()+1, t.length());
				n = s.lastIndexOf(".");
				s = s.substring(0, n);
                if (!w.contains(s)) {
					w.add(s);
				}
			}
		}
		w = new SortUtils().quickSort(w);
		Utils.dumpVector("w", w);

		String oldVersion = (String) w.elementAt(0);
		String newVersion = (String) w.elementAt(1);
		return oldVersion + "|" + newVersion;
	}

	public static void run(int type) {
		long ms = System.currentTimeMillis();
		System.out.println("Data Type: " + type);
		compare(type);
		System.out.println("Run time (ms): " + (System.currentTimeMillis() - ms));
	}

	public static void run() {
		long ms0 = System.currentTimeMillis();
		for (int type=0; type<=6; type++) {
			run(type);
		}
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms0));
	}


//// HIERARCHY ////////////////////////////////////////////////////////////////////////////////////////////////
	public static HashSet getCodes(Vector parent_child_vec) {
		HashSet hset = new HashSet();
		for (int i=0; i<parent_child_vec.size(); i++) {
			String line = (String) parent_child_vec.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(1);
			if (!hset.contains(code)) {
				hset.add(code);
			}
			code = (String) u.elementAt(3);
			if (!hset.contains(code)) {
				hset.add(code);
			}
		}
		return hset;
	}

	public static HashSet getRelations(Vector parent_child_vec) {
		HashSet hset = new HashSet();
		for (int i=0; i<parent_child_vec.size(); i++) {
			String line = (String) parent_child_vec.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String code1 = (String) u.elementAt(1);
			String code2 = (String) u.elementAt(3);
			String rel = code1 + "|" + code2;
			if (!hset.contains(rel)) {
				hset.add(rel);
			}
		}
		return hset;
	}

	public static HashMap getCode2LabelMap(Vector parent_child_vec) {
		HashMap hmap = new HashMap();
		for (int i=0; i<parent_child_vec.size(); i++) {
			String line = (String) parent_child_vec.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(1);
			String label = (String) u.elementAt(0);
			hmap.put(code, label);
		}
		return hmap;
	}

    //hset1L old; hset2: new
	public static Vector hashSetDiff(HashSet hset1, HashSet hset2) {
		Vector w = new Vector();
		Iterator it1 = hset1.iterator();
		while (it1.hasNext()) {
			String key = (String) it1.next();
			if (!hset2.contains(key)) {
				w.add("Delete\t" + key);
			}
		}
		Iterator it2 = hset2.iterator();
		while (it2.hasNext()) {
			String key = (String) it2.next();
			if (!hset1.contains(key)) {
				w.add("Add\t" + key);
			}
		}
		return w;
	}

	public static Vector hashMapDiff(HashMap hmap1, HashMap hmap2) {
		Vector w = new Vector();
		Iterator it1 = hmap1.keySet().iterator();
		while (it1.hasNext()) {
			String key = (String) it1.next();
			if (!hmap2.containsKey(key)) {
				w.add("Delete\t" + key);
			}
		}
		Iterator it2 = hmap2.keySet().iterator();
		while (it2.hasNext()) {
			String key = (String) it2.next();
			if (!hmap1.containsKey(key)) {
				w.add("Add\t" + key);
			} else {
				String value1 = (String) hmap1.get(key);
				String value2 = (String) hmap2.get(key);
				if (value1.compareTo(value2) != 0) {
					w.add("Modify\t" + value1 + "\t" + value2);
				}
			}
		}
		return w;
	}

	public static void hierDiff(String hierfile1, String hierfile2, String outputDir) {
		File f = new File(outputDir);
		if (!f.exists()) {
			f.mkdir();
		}

		Vector parent_child_vec_1 = Utils.readFile(hierfile1);
		Vector parent_child_vec_2 = Utils.readFile(hierfile2);
		HashSet code_vec_1 = getCodes(parent_child_vec_1);
		HashSet code_vec_2 = getCodes(parent_child_vec_2);
		Vector w = hashSetDiff(code_vec_1, code_vec_2);
		Utils.saveToFile(outputDir + File.separator + "concepts.txt", w);

		HashMap code2LabelMap_1 = getCode2LabelMap(parent_child_vec_1);
		HashMap code2LabelMap_2 = getCode2LabelMap(parent_child_vec_2);
		w = hashMapDiff(code2LabelMap_1, code2LabelMap_2);
		Utils.saveToFile(outputDir + File.separator + "labels.txt", w);

		HashSet rel_vec_1 = getRelations(parent_child_vec_1);
		HashSet rel_vec_2 = getRelations(parent_child_vec_2);
		w = hashSetDiff(rel_vec_1, rel_vec_2);
		Utils.saveToFile(outputDir + File.separator + "relationships.txt", w);

		String excelfile = outputDir + ".xlsx";
		System.out.println("excelfile: " + excelfile);
		char delim = '\t';
		System.out.println("Calling generateExcel...");
		Text2Excel.generateExcel(outputDir, excelfile, delim);
	}
////////////////////////////////////////////////////////////////////////////////////////////////////


	public static void main(String[] args) {
		run();
	}
}

