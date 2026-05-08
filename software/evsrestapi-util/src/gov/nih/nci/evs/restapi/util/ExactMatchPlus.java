package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.XStream;
import gov.nih.nci.evs.restapi.bean.*;
import gov.nih.nci.evs.restapi.common.*;
import java.io.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.text.similarity.*;
import org.json.*;

public class ExactMatchPlus {
	static String NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile;
	static String AXIOM_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.axiomfile;

    static HashMap acronymMap = null;
    static String ACRONYM_FILE = "analysis_NCIt_acronyms.txt";

	static DataRetrieval retrieval = null;
    static HashMap NCIPTMap = null;
    static HashMap NCISYMap = null;

    static {
		long ms = System.currentTimeMillis();
		Vector dataVec = new Vector();
		dataVec.add("P90|P384$NCI|P383$SY");
		dataVec.add("P90|P384$NCI|P383$PT");
		dataVec.add("P310");
		retrieval = new DataRetrieval(NCIT_OWL, dataVec);
	    NCIPTMap = retrieval.createCode2ValuesMap("P90|P384$NCI|P383$PT");
	    NCISYMap = retrieval.createCode2ValuesMap("P90|P384$NCI|P383$SY");

		acronymMap = new HashMap();
		File f = new File(ACRONYM_FILE);
		if (!f.exists()) {
			System.out.println("WARNING: " + ACRONYM_FILE + " does not exist.");
		} else {
			Vector v = Utils.readFile(ACRONYM_FILE);
			for (int i=1; i<v.size(); i++) {
				String line = (String) v.elementAt(i);
				Vector u = StringUtils.parseData(line, '\t');
				String pair = (String) u.elementAt(0);
				Vector u2 = StringUtils.parseData(pair, '|');
				String acronym = (String) u2.elementAt(0);
				String fullStr = (String) u2.elementAt(1);
				Vector w = new Vector();
				if (acronymMap.containsKey(acronym)) {
					w = (Vector) acronymMap.get(acronym);
				}
				w.add(fullStr);
				acronymMap.put(acronym, w);
			}
		}
		long ms1 = System.currentTimeMillis();
		long timeElapsed = ms1 - ms;
		System.out.println("Total initialization run time: " + timeElapsed + " (milli-seconds)");
	}

	public static String getNCIPT(String code) {
		Vector w = (Vector) NCIPTMap.get(code);
		if (w == null) return "";
		return Utils.vector2Delimited(w, "$");
	}

	public static String getNCIPT(Vector codes) {
		Vector w = new Vector();
		for (int i=0; i<codes.size(); i++) {
			String code = (String) codes.elementAt(i);
			w.add(getNCIPT(code));
		}
		return Utils.vector2Delimited(w, "|");
	}

	public static String getNCISY(String code) {
		Vector w = (Vector) NCISYMap.get(code);
		if (w == null) return "";
		return Utils.vector2Delimited(w, "$");
	}

	public static String getNCISY(Vector codes) {
		Vector w = new Vector();
		for (int i=0; i<codes.size(); i++) {
			String code = (String) codes.elementAt(i);
			w.add(getNCISY(code));
		}
		return Utils.vector2Delimited(w, "|");
	}

    public static int findColumeNumber(String line, String fieldName, char delim) {
		Vector u = StringUtils.parseData(line, delim);
		for (int i=0; i<u.size(); i++) {
			String field = (String) u.elementAt(i);
			if (field.compareToIgnoreCase(fieldName) == 0) {
				return i;
			}
		}
		if (fieldName.compareTo("Term") == 0) {
			return 0; // default
		}
		return -1;
	}

    public static void run(String filename) {
		ExactMatch test = new ExactMatch(AXIOM_FILE);
		Vector w = new Vector();
		Vector w2 = new Vector();
		Vector v = Utils.readFile(filename);
		w.add((String) v.elementAt(0));
		w2.add((String) v.elementAt(0));
		int termCol = findColumeNumber((String) v.elementAt(0), "Term", '\t');
		int codesCol = findColumeNumber((String) v.elementAt(0), "Matched NCIt Code(s)", '\t');
		for (int i=1; i<v.size(); i++) {
			String line0 = (String) v.elementAt(i);
			String line = line0;
			Vector u = StringUtils.parseData(line, '\t');
			String status = (String) u.elementAt(codesCol);
			if (status.compareTo("No match") != 0) {
				w.add(line);
			} else {
				String term = (String) u.elementAt(termCol);
				term = term.toUpperCase();
				Vector u2 = StringUtils.parseData(term, ' ');
				for (int j=0; j<u2.size(); j++) {
					String token = (String) u2.elementAt(j);
					if (acronymMap.containsKey(token)) {
						Vector w1 = (Vector) acronymMap.get(token);
						for (int k=0; k<w1.size(); k++) {
							String fullStr = (String) w1.elementAt(k);
							String term1 = term.replace(token, fullStr);
							//////////////////////////////////////////////////////////////
							Vector codes = test.findMatchedConcepts(term1);
							if (codes != null && codes.size() > 0) {
								line = line.replace("No match", Utils.vector2Delimited(codes, "|"));
								line = line + "\t" + getNCIPT(codes) + "\t" + getNCISY(codes);

								System.out.println(line);
								Vector w3 = new Vector();
								for (int k1=0; k1<codes.size(); k1++) {
									String code = (String) codes.elementAt(k1);
									String displayLabel = test.getLabel(code) + " (" + code + ")";
									w3.add(displayLabel);
								}
								Utils.dumpVector("matches", w3);//System.out.println(Utils.vector2Delimited(w1, "|"));
							}
						}
					}
				}
				w.add(line);
				if (line0.compareTo(line) != 0) {
					w2.add(line);
				}
			}
		}
		Utils.saveToFile("mod_" + filename, w);
		Utils.saveToFile("change_" + filename, w2);
	}

    public static void main(String[] args) {
		long ms = System.currentTimeMillis();
		String filename = args[0];//results_hemonc_condition.txt
		run(filename);
		long ms1 = System.currentTimeMillis();
		long timeElapsed = ms1 - ms;
		System.out.println("Total run time: " + timeElapsed + " (milli-seconds)");

	}

}
