package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;
import gov.nih.nci.evs.restapi.bean.*;
import java.io.*;
import java.net.*;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.*;
import java.util.*;


public class TermFileGenerator {
	static String AXIOM_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.axiomfile;
	static String PARENT_CHILD_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile; // "parent_child.txt";

	static String A8_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.subsetfile;
	static HashMap a8Map = null;
	static HashMap inverse_a8Map = null;
	static HierarchyHelper hh = null;

	static {
		Vector v = Utils.readFile(A8_FILE);
		hh = new HierarchyHelper(Utils.readFile(PARENT_CHILD_FILE));
        a8Map = new HashMap();
        inverse_a8Map = new HashMap();
		v = Utils.readFile(A8_FILE);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String src = (String) u.elementAt(0);
			String target = (String) u.elementAt(2);
			Vector w = new Vector();
			if (a8Map.containsKey(src)) {
				w = (Vector) a8Map.get(src);
			}
			w.add(target);
			a8Map.put(src, w);

			w = new Vector();
			if (inverse_a8Map.containsKey(target)) {
				w = (Vector) inverse_a8Map.get(target);
			}
			w.add(src);
			inverse_a8Map.put(target, w);
		}
	}

	public static Vector retrieveAxioms(Vector codes) {
		HashSet hset = Utils.vector2HashSet(codes);
		Vector w = new Vector();
		Vector v = Utils.readFile(AXIOM_FILE);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, "|");
			String code = (String) u.elementAt(1);
			String prop_code = (String) u.elementAt(2);
			if (hset.contains(code) && prop_code.compareTo("P90") == 0) {
				w.add(line);
			}
		}
		v.clear();
		w = new SortUtils().quickSort(w);
		return w;
    }

    public static Vector getCodes(int mode, Vector roots) {
		Vector codes = new Vector();
		if (mode == ExactMatch.BRANCH) { // branch
			for (int i=0; i<roots.size(); i++) {
				String root = (String) roots.elementAt(i);
			    Vector w = hh.get_transitive_closure_v3(root);
			    codes.addAll(w);
			}
		} else if (mode == ExactMatch.SUBSET) {
			for (int i=0; i<roots.size(); i++) {
				String root = (String) roots.elementAt(i);
				codes.add(root);
				Vector w = (Vector) inverse_a8Map.get(root);
				codes.addAll(w);
			}
		}
		return codes;
	}

    public static Vector generateTermFile(int mode, Vector roots) {
		Vector codes = getCodes(mode, roots);
        Vector w = retrieveAxioms(codes);
        return w;
	}

    public static void generateTermFile(int mode, Vector roots, String termfile) {
		Vector codes = getCodes(mode, roots);
        Vector w = retrieveAxioms(codes);
        Utils.saveToFile(termfile, w);
	}

	public static void main(String[] args) {
		Vector roots = new Vector();
		int mode = ExactMatch.BRANCH;
		String root = "C2991";
		roots.add("C2991");
		String termfile = "axioms.txt";
		generateTermFile(mode, roots, termfile);
	}
}

