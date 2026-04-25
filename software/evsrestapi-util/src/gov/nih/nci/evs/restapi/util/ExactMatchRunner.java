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

public class ExactMatchRunner {
	DataRetrieval retrieval = null;
	static String NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile;
	static String AXIOM_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.axiomfile;

	static String TERMFILE = "termfile.txt";

    Vector dataVec = null;
    HashSet retiredConcepts = new HashSet();
    HashMap NCIPTMap = null;
    HashMap NCISYMap = null;

	public ExactMatchRunner(Vector dataVec) {
		this.dataVec = dataVec;
		initialize();
	}

	public void initialize() {
		retrieval = new DataRetrieval(NCIT_OWL, dataVec);
		HashMap p310Map = (HashMap) retrieval.propertyMap.get("P310");
		retiredConcepts = new HashSet();
		Iterator it = p310Map.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			Vector values = (Vector) p310Map.get(key);
			if (values.contains("Retired_Concept")) {
				retiredConcepts.add(key);
			}
		}
	    NCIPTMap = retrieval.createCode2ValuesMap("P90|P384$NCI|P383$PT");
	    NCISYMap = retrieval.createCode2ValuesMap("P90|P384$NCI|P383$SY");
	}

	public boolean is_retired(String code) {
		return retiredConcepts.contains(code);
	}

    public String getPropertyValues(String propCode, String code) {
		return retrieval.getPropertyValues(propCode, code);
	}

    public String getAxiomValues(String propCode, String code) {
		return retrieval.getAxiomValues(propCode, code);
	}

	public String getMatchedData(Vector codes_0) {
		if (codes_0 == null || codes_0.size() == 0) return "";
		Vector codes = new Vector();
		for (int i=0; i<codes_0.size(); i++) {
			String code = (String) codes_0.elementAt(i);
			if (!is_retired(code)) {
				codes.add(code);
			}
		}
		StringBuffer buf = new StringBuffer();
		String s = null;
		//CODES
		if (codes != null && codes.size() > 0) {
			for (int i=0; i<codes.size(); i++) {
				String code = (String) codes.elementAt(i);
				buf.append(code).append("|");
			}
			s = buf.toString();
			s = s.substring(0, s.length()-1);
		}
		String code_str = s;

        // NCI PTs
        StringBuffer pt_buf = new StringBuffer();
        Vector pt_vec = null;
        String pts = "";
        String pt = "";
		if (codes != null && codes.size() > 0) {
			for (int i=0; i<codes.size(); i++) {
				String code = (String) codes.elementAt(i);
				pt = "";
				if (NCIPTMap.containsKey(code)) {
					//String pt = (String) NCIPTMap.get(code);
					pt_vec = (Vector) NCIPTMap.get(code);
					if (pt_vec != null) {
						pt = Utils.vector2Delimited(pt_vec, "|");
					}
					pt_buf.append(pt).append("|");
				}
			}
			pts = pt_buf.toString();
			if (pts.length() > 0) {
				pts = pts.substring(0, pts.length()-1);
			}
		}
		String pt_str = pts;

        // NCI SYs
        StringBuffer sy_buf = new StringBuffer();
        String syns_str = "";
		if (codes != null && codes.size() > 0) {
			for (int i=0; i<codes.size(); i++) {
				String code = (String) codes.elementAt(i);
				StringBuffer sb = new StringBuffer();
				if (NCISYMap.containsKey(code)) {
					Vector syns = (Vector) NCISYMap.get(code);
					for (int k=0; k<syns.size(); k++) {
						String sy = (String) syns.elementAt(k);
						sb.append(sy).append("$");
					}
				}
				s = sb.toString();
				if (s.length() > 0) {
					s = s.substring(0, s.length()-1);
				}
				sy_buf.append(s).append("|");
			}
			syns_str = sy_buf.toString();
			if (syns_str.length() > 0) {
				syns_str = syns_str.substring(0, syns_str.length()-1);
			}
		}
		return code_str + "\t" + pt_str + "\t" + syns_str;
	}

/*
Term	Id	Synonyms	Matched NCIt Code(s)	NCI PT(s)	NCI SY(s)
Eluvixtamab	1	AMG 330|AMG-330|AMG330|Eluvixtamab	C123332	Eluvixtamab	Anti-CD33/CD3 BiTE Antibody AMG 330$BiTE Antibody AMG 330$Bispecific T-cell Engager Antibody AMG 330$CD33/CD3-directed Bispecific T-cell Engager Antibody AMG 330
No match
*/

	public void appendData(String filename) {
		Vector w0 = new Vector();
		Vector w = new Vector();
		Vector v = Utils.readFile(filename);
		w0.add((String) v.elementAt(0) + "\tNCI PT(s)\tNCI SY(s)");
		for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '\t');
			if (u.contains("No match")) {
				w.add(line + "\t\t");
			} else {
				String code_str = (String) u.elementAt(u.size()-1);
				Vector codes_0 = StringUtils.parseData(code_str, '|');
				String data = getMatchedData(codes_0);
				int n = line.lastIndexOf("\t");
				line = line.substring(0, n) + "\t" + data;
				w.add(line);
			}
		}
		w0.addAll(w);
		Utils.saveToFile(filename, w0);
	}

    public static void run(int mode, Vector roots, String datafile, int colNum, Vector dataVec) {
		long ms = System.currentTimeMillis();
		ExactMatchRunner runner = new ExactMatchRunner(dataVec);
		String outputfile = "results_" + datafile;
		System.out.println("outputfile: " + outputfile);
		if (mode == ExactMatch.DEFAULT) {
			new ExactMatch(AXIOM_FILE).run(datafile, outputfile, colNum);
		} else if (mode == ExactMatch.BRANCH) {
			Vector w = TermFileGenerator.generateTermFile(mode, roots);
			Utils.saveToFile(TERMFILE,w);
			new ExactMatch(TERMFILE).run(datafile, outputfile, colNum);
		} else if (mode == ExactMatch.SUBSET) {
			Vector w = TermFileGenerator.generateTermFile(mode, roots);
			Utils.saveToFile(TERMFILE,w);
			new ExactMatch(TERMFILE).run(datafile, outputfile, colNum);
		}
		System.out.println("Appending data ...");
		runner.appendData(outputfile);
		ExactMatch.toExcel(outputfile);
		long ms1 = System.currentTimeMillis();
		long timeElapsed = ms1 - ms;
		System.out.println("Total run time: " + timeElapsed + " (milli-seconds)");
	}

    //command line parameters: datafile mode_str root_str col_str
    public static void main(String[] args) {
		Vector dataVec = new Vector();
		dataVec.add("P90|P384$NCI|P383$SY");
		dataVec.add("P90|P384$NCI|P383$PT");
		dataVec.add("P310");
        //int mode, Vector roots, String datafile, int colNum, Vector dataVec
		String datafile = args[0];
		String mode_str = args[1];
		int mode = Integer.parseInt(mode_str);
		Vector roots = null;
		String delimited_roots = args[2];
		if (delimited_roots.compareTo("null") != 0) {
			roots = StringUtils.parseData(delimited_roots, '|');
		}
		String col_str = args[3];
		int colNum = Integer.parseInt(col_str);
		System.out.println("mode: " + mode);
		System.out.println("roots: " + roots);
		System.out.println("datafile: " + datafile);
		System.out.println("colNum: " + colNum);
		Utils.dumpVector("dataVec", dataVec);

		run(mode, roots, datafile, colNum, dataVec);
	}
}

