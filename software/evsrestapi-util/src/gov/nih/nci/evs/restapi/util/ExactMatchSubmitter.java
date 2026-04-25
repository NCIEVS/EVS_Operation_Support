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

public class ExactMatchSubmitter {
	static String PARENT_CHILD_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile;
	static String AXIOM_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.axiomfile;
	static String A8_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.subsetfile;
	static HashMap a8Map = null;
	static HashMap inverse_a8Map = null;
	static HierarchyHelper hh = null;
	static Vector AXIOMS = null;
	static String FULLSYN_FILE = "fullsyn.txt";
	static {
		AXIOMS = Utils.readFile(AXIOM_FILE);
		Vector w = Utils.readFile(PARENT_CHILD_FILE);
		hh = new HierarchyHelper(w);

		Vector v = Utils.readFile(A8_FILE);
        a8Map = new HashMap();
        inverse_a8Map = new HashMap();
		v = Utils.readFile(A8_FILE);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String src = (String) u.elementAt(0);
			String target = (String) u.elementAt(2);
			w = new Vector();
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

	public static String getLabel(String code) {
		return hh.getLabel(code);
    }

	public static Vector retrieveAxioms(Vector v, Vector codes) {
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, "|");
			String code = (String) u.elementAt(1);
			if (codes.contains(code)) {
				w.add(line);
			}
		}
		return w;
    }

    public static Vector getSubsets(Vector subsetCodes) {
		Vector codes = new Vector();
		for (int i=0; i<subsetCodes.size(); i++) {
			String root = (String) subsetCodes.elementAt(i);
			codes.add(root);
			Vector w = (Vector) inverse_a8Map.get(root);
			codes.addAll(w);
		}
		return codes;
	}

    public static Vector getBranches(Vector roots) {
		Vector codes = new Vector();
		for (int i=0; i<roots.size(); i++) {
			String root = (String) roots.elementAt(i);
			Vector w = hh.get_transitive_closure_v3(root);
			codes.addAll(w);
		}
		return codes;
	}


    public static Vector retrieveAxioms(Vector v, int mode, Vector roots) {
		Vector codes = new Vector();
		if (mode == ExactMatch.SUBSET) {
			codes = getSubsets(roots);
			return retrieveAxioms(v, codes);
		} else if (mode == ExactMatch.BRANCH) {
			codes = getBranches(roots);
			return retrieveAxioms(v, codes);
		} else {
			return v;
		}
	}

	public static Vector getFullSyns() {
		int colNum = 3;
		String matchingColValue = "P90";
		char delim = '|';
		return DelimitedDataExtractor.retrieveColumnData(AXIOMS, colNum, matchingColValue, delim);
	}

	public static void generateTermFile(Vector v, int mode, Vector roots, String outputfile) {
		Vector w = retrieveAxioms(v, mode, roots);
		Utils.saveToFile(outputfile, w);
	}

	public static void generateTermFile(Vector v, int mode, Vector roots) {
		String outputfile = "axiom_" + StringUtils.getToday() + ".txt";
        generateTermFile(v, mode, roots, outputfile);
	}

	public static String generateTermFile(int mode, Vector roots) {
		String outputfile = "axiom_" + StringUtils.getToday() + ".txt";
        generateTermFile(getFullSyns(), mode, roots, outputfile);
        return outputfile;
	}

	public static void runExactMatch(String datafile, int mode, int colNum, String termfile) {
		String outputfile = "results_" + datafile;
		new ExactMatch(termfile).run(datafile, outputfile, colNum);
		/*
        String[] parameters = new String[4];
        parameters[0] = datafile;
        parameters[1] = "" + mode;
        parameters[2] = "" + colNum;
        parameters[3] = termfile;
        runExactMatch(parameters);
        */
	}
/*
	public static void runExactMatch(String[] parameters) {
        ExactMatchRunner.run(parameters);
	}
*/

    public static void run(String[] args) {
		String datafile = args[0];
		String mode_str = args[1];
		int mode = Integer.parseInt(mode_str);
		String colNum_str = args[2];
		int colNum = Integer.parseInt(colNum_str);
		String termfile = null;
		if (args.length == 3) {
			Vector fullsyns = getFullSyns();
			Utils.saveToFile(FULLSYN_FILE, fullsyns);
			runExactMatch(datafile, mode, colNum, termfile);
		} else {
			String delimited_roots = args[3];
			Vector roots = StringUtils.parseData(delimited_roots, '|');
			termfile = generateTermFile(mode, roots);
			runExactMatch(datafile, mode, colNum, termfile);
		}
	}
}

