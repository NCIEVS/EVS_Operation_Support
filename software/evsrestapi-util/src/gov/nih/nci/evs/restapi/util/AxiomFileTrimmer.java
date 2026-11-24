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
	public String A8_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.subsetfile;

	public String axiomfile = null;
	public String hierarchyfile = null;
	public String a8file = null;

	public HashMap a8Map = null;
	public HashMap inverse_a8Map = null;
	public HierarchyHelper hh = null;

    public AxiomFileTrimmer() {
		this.axiomfile = AXIOM_FILE;
		this.hierarchyfile = PARENT_CHILD_FILE;
		this.a8file = A8_FILE;
		initialize();
	}

    public AxiomFileTrimmer(String axiomfile, String hierarchyfile, String a8file) {
		this.axiomfile = axiomfile;
		this.hierarchyfile = hierarchyfile;
		this.a8file = a8file;
		initialize();
	}

	public void initialize() {
		Vector v = Utils.readFile(a8file);
		hh = new HierarchyHelper(Utils.readFile(hierarchyfile));
        a8Map = new HashMap();
        inverse_a8Map = new HashMap();
		v = Utils.readFile(a8file);
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

	public Vector retrieveAxioms(Vector codes, String prop_code) {
		Vector dataSpec = new Vector();
		dataSpec.add(prop_code);
		return retrieveAxioms(codes, dataSpec);
	}

	public Vector retrieveAxioms(Vector codes, Vector dataSpec) {
		HashSet hset = Utils.vector2HashSet(codes);
		Vector w = new Vector();
		Vector v = Utils.readFile(axiomfile);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, "|");
			boolean include = true;
			for (int j=0; j<dataSpec.size(); j++) {
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

	public Vector retrieveAxioms(Vector dataSpec) {
		Vector w = new Vector();
		Vector v = Utils.readFile(axiomfile);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, "|");
			boolean include = true;
			for (int j=0; j<dataSpec.size(); j++) {
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

    public Vector getCodes(int mode, Vector roots) {
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

    ////////////////////////////////////////////////////////////////////////////////////////////

    public Vector generateAxiomFile(int mode, Vector roots) {
		Vector codes = getCodes(mode, roots);
        Vector w = retrieveAxioms(codes);
        return w;
	}

    public void generateAxiomFile(int mode, Vector roots, String outputfile) {
		Vector codes = getCodes(mode, roots);
		Vector dataSpec = new Vector();
		dataSpec.add("P90");
        Vector w = retrieveAxioms(codes, dataSpec);
        Utils.saveToFile(outputfile, w);
	}

    public void run(int mode, Vector roots, Vector dataSpec, String outputfile) {
		Vector codes = getCodes(mode, roots);
        Vector w = retrieveAxioms(codes, dataSpec);
        Utils.saveToFile(outputfile, w);
	}

	public static void run(String axiomfile, String hierarchyFile, String a8file, int mode, Vector roots, Vector dataSpec, String outputfile) {
        new AxiomFileTrimmer(axiomfile, hierarchyFile, a8file).run(mode, roots, dataSpec, outputfile);
	}

	public static void main(String[] args) {
		String axiomfile = args[0];
		String hierarchyFile = args[1];
		String a8file = args[2];
		String modeStr = args[3];
        int mode = Integer.parseInt(modeStr);
        if (mode == ExactMatch.DEFAULT) {
			Vector roots = null;
			Vector dataSpec = StringUtils.parseData(args[4], '|');
			int n = axiomfile.lastIndexOf(".");
			String outputfile = axiomfile.substring(0, n) + "_" + StringUtils.getToday() + ".txt";
            run(axiomfile, hierarchyFile, a8file, mode, roots, dataSpec, outputfile);
		} else {
			String rootStr = args[4];
			Vector roots = StringUtils.parseData(rootStr, '|');
			String dataSpecStr = args[5];
			Vector dataSpec = StringUtils.parseData(dataSpecStr, '|');
			int n = axiomfile.lastIndexOf(".");
			String outputfile = axiomfile.substring(0, n) + "_" + StringUtils.getToday() + ".txt";
			run(axiomfile, hierarchyFile, a8file, mode, roots, dataSpec, outputfile);
		}
	}
}

