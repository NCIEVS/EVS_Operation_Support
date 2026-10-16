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


public class AxiomFileTrimmer {
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

	public static Vector retrieveAxioms(Vector codes, String prop_code) {
		Vector dataSpec = new Vector();
		dataSpec.add(prop_code);
		return retrieveAxioms(codes, dataSpec);
	}


	public static Vector retrieveAxioms(Vector codes, Vector dataSpec) {
		HashSet hset = Utils.vector2HashSet(codes);
		Vector w = new Vector();
		Vector v = Utils.readFile(AXIOM_FILE);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, "|");
			boolean include = true;
			for (int j=0; j<u.size(); j++) {
				String s = (String) dataSpec.elementAt(j);
				if (!u.contains(s)) {
					include = false;
					break;
				}
			}
			String code = (String) u.elementAt(0);
			if (hset.contains(code) && include) {
				w.add(line);
			}
		}
		v.clear();
		w = new SortUtils().quickSort(w);
		return w;
    }

	public static Vector retrieveAxioms(Vector dataSpec) {
		Vector w = new Vector();
		Vector v = Utils.readFile(AXIOM_FILE);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, "|");
			boolean include = true;
			for (int j=0; j<u.size(); j++) {
				String s = (String) dataSpec.elementAt(j);
				if (!u.contains(s)) {
					include = false;
					break;
				}
			}
			if (include) {
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

    public static Vector generateAxiomFile(int mode, Vector roots) {
		Vector codes = getCodes(mode, roots);
        Vector w = retrieveAxioms(codes);
        return w;
	}

    public static void generateAxiomFile(int mode, Vector roots, String axiomfile) {
		Vector codes = getCodes(mode, roots);
		Vector dataSpec = new Vector();
		dataSpec.add("P90");
        Vector w = retrieveAxioms(codes, dataSpec);
        Utils.saveToFile(axiomfile, w);
	}

    public static void generateAxiomFile(int mode, Vector roots, Vector dataSpec, String axiomfile) {
		Vector codes = getCodes(mode, roots);
        Vector w = retrieveAxioms(codes, dataSpec);
        Utils.saveToFile(axiomfile, w);
	}

    public static void generateAxiomFile(Vector dataSpec, String axiomfile) {
        Vector w = retrieveAxioms(dataSpec);
        Utils.saveToFile(axiomfile, w);
	}

	public static void main(String[] args) {
		String modeStr = args[0];
        int mode = Integer.parseInt(modeStr);
        if (mode == ExactMatch.DEFAULT) {
			Vector dataSpec = StringUtils.parseData(args[1], '|');
			String axiomfile = "axiom_"+ StringUtils.getToday() + ".txt";
			if (args.length == 3) {
				axiomfile = args[2];
			}
			generateAxiomFile(dataSpec, axiomfile);
		} else {
			String rootStr = args[1];
			Vector roots = StringUtils.parseData(rootStr, '|');
			String dataSpecStr = args[2];
			Vector dataSpec = StringUtils.parseData(dataSpecStr, '|');
			String axiomfile = "axiom_"+ StringUtils.getToday() + ".txt";
			if (args.length == 4) {
				axiomfile = args[3];
			}
			generateAxiomFile(mode, roots, dataSpec, axiomfile);
		}
	}
}

