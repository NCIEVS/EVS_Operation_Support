package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;
import gov.nih.nci.evs.restapi.bean.*;
import java.io.*;
import java.util.*;
import org.apache.commons.text.StringEscapeUtils;

class LEAnalyzer {

	static String NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile; //"ThesaurusInferred_forTS.owl";
	static String PARENT_CHILD_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile; // "parent_child.txt";

    String textfile = null;
	OWLClassLoader loader = null;
	HierarchyHelper hh = null;
	String code = null;
	String label = null;
	Vector raw_data_vec = null;
	Vector classData = null;
	HashMap roleCode2RoleNameMap = null;
	HashMap objectPropertyCode2LabelMap = null;
	HashMap roleName2RangeNameMap = null;
	Vector rangeNames = null;
	Vector equivalentClasses = null;

	static String PATH_FILE = "paths.txt";

	LEQA leqa = null;

	public LEAnalyzer() {
		initialize();
	}

	public void initialize() {
		leqa = new LEQA();
		roleName2RangeNameMap = NCItProperties.getRoleName2RangeNameMap();
		rangeNames = new Vector();
		Iterator it = roleName2RangeNameMap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			String range = (String) roleName2RangeNameMap.get(key);
			rangeNames.add(range);
		}
	}

    public Vector getPaths(String code) {
		Vector classData = leqa.getClassData(code);
		Vector equiv_vec = leqa.getEquivVec(classData);
		Vector paths = leqa.restrictionPathFinder(equiv_vec);
		return paths;
	}

	public Vector getEquivalentClasses() {
		return leqa.getEquivalentClasses();
	}

	public String normalizePath(String line) {
		if (line.indexOf("$") != -1) {
			int n = line.lastIndexOf("|R");
			String path = line.substring(0, n-3);
			if (path.endsWith("R")) {
				path = path.substring(0, path.length()-1);
			}
			if (path.endsWith("|")) {
				path = path.substring(0, path.length()-1);
			}
			if (!path.equals("E|C1|I1")) {
				return path;
			}
		}
		return line;
	}

	public HashSet analyze() {
		HashSet hset = new HashSet();
		File f = new File(PATH_FILE);
		if (!f.exists()) {
			Vector w = parse();
			Utils.saveToFile(PATH_FILE, w);
		}
		Vector w = Utils.readFile(PATH_FILE);
		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			String normalize_path = normalizePath(line);
			if (!normalize_path.equals(line)) {
				hset.add(normalize_path);
			}
		}
		return hset;
	}

    public Vector parse() {
		return leqa.parse();
	}

    public HashMap createPathMap() {
		HashMap hmap = new HashMap();
		HashSet path_set = analyze();
        Vector v = Utils.readFile(PATH_FILE);
        String displayName = null;
        Vector w = new Vector();
        for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (line.startsWith("(")) {
				if (displayName != null) {
					if (w.size() > 0) {
						hmap.put(displayName, w);
					}
				}
				displayName = line;
				w = new Vector();
			} else {
				String normalized_path = normalizePath(line);
				if (normalized_path.compareTo(line) != 0) {
					if (!w.contains(normalized_path)) {
						w.add(normalized_path);
					}
				}
			}
		}
		// selected_paths
		w = new Vector();
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String line = (String) it.next();
			System.out.println(line);

			Vector u0 = StringUtils.parseData(line, ' ');
			displayName = (String) u0.elementAt(1);
			w.add(displayName);
			Vector u = StringUtils.parseData(displayName, '|');
			String code = (String) u.elementAt(0);
			Vector classData = leqa.getClassData(code);
			Vector equiv_vec = leqa.getEquivVec(classData);
			Vector paths = leqa.restrictionPathFinder(equiv_vec);
			w.addAll(paths);
		}
		Utils.saveToFile("selected_paths.txt", w);
        return hmap;
	}

	public String getLE(String code) {
		HashMap map = leqa.getLEData(code);
		gov.nih.nci.evs.restapi.bean.LogicalExpression le = leqa.leData2Expression(map);
		try {
            return le.toString();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public void runQA(String filename) {
		Vector w = new Vector();
		Vector v = Utils.readFile(filename);
		String code = null;
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (line.indexOf("Logical expression of:") != -1) {
				int n = line.lastIndexOf("(");
				code = line.substring(n+1, line.length()-1);
				Vector classData = leqa.getClassData(code);
				w.addAll(classData);
			}
		}
		int n = filename.lastIndexOf(".");
		String htmlfile = filename.substring(0, n) + ".html";
		w.addAll(format(v));

		Vector paths = getPaths(code);
		w.add("\n=======================================================================================================");
		w.add("\t");
		w.add("\t");

		w.addAll(paths);

		String expression = getLE(code);
		Vector u = StringUtils.parseData(expression, '\n');
		w.addAll(u);
        leqa.le2HTML(w, htmlfile);
	}

    public Vector format(Vector v) {
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '\t');
			if(u.size() > 1) {
				line = "\t\t" + line;
			} else {
				String t = line;
				t = t.trim();
				if (rangeNames.contains(t)) {
					System.out.println(t);
					w.add("\t");
				}
			}
			w.add(line);
		}
		return w;
	}

	public static void main(String[] args) {
		LEAnalyzer test = new LEAnalyzer();
        String textfile = args[0];
        test.runQA(textfile);
	}
}
