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

public class AcronymAnalyzer {
	HashMap term2CodesMap = null;
	HashMap camelCaseTerm2CodesMap = null;
	HashMap code2TermsMap = null;
	static String AXIOM_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.axiomfile;

    static int MAX_MATCHES = 20;
    static int INCREMENT = 100;

	public AcronymAnalyzer() {
		initialize();
	}

	public void initialize() {
	    long ms = System.currentTimeMillis();
		term2CodesMap = createTerm2CodesMap();
		code2TermsMap = createCode2TermsMap();
		camelCaseTerm2CodesMap = createCamelCaseTerm2CodesMap();

		long ms1 = System.currentTimeMillis();
		long timeElapsed = ms1 - ms;
		System.out.println("Total instantiation run time: " + timeElapsed + " (milli-seconds).");
	}

    public static String removeSpecialCharacters(String term) {
		//return AcronymDetector.removeSpecialCharacters(term);
		term = term.replace("(", "");
		term = term.replace(")", "");
		term = term.replace(",", "");
		term = term.replace("-", " ");
		return term;
	}

	public HashMap createCamelCaseTerm2CodesMap() {
		HashMap hmap = new HashMap();
		Vector v = Utils.readFile(AXIOM_FILE);
        for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String label = (String) u.elementAt(0);

			label = removeSpecialCharacters(label);
			label = HTMLDecoder.decode(label);
			label = toCamelCase(label);

			String prop_code = (String) u.elementAt(2);

			if (prop_code.compareTo("P90") == 0) {
				String term = (String) u.elementAt(3);
				term = removeSpecialCharacters(term);
				term = HTMLDecoder.decode(term);
				term = toCamelCase(term);

				Vector w = new Vector();
				if (hmap.containsKey(label)) {
					w = (Vector) hmap.get(label);
				}
				String code = (String) u.elementAt(1);
				if (!w.contains(code)) {
					w.add(code);
				}
				hmap.put(label, w);

				w = new Vector();
				if (hmap.containsKey(term)) {
					w = (Vector) hmap.get(term);
				}
				code = (String) u.elementAt(1);
				if (!w.contains(code)) {
					w.add(code);
				}
				hmap.put(term, w);
			}
		}
		return hmap;
	}


    public HashMap createCode2TermsMap() {
		HashMap hmap = new HashMap();
		Vector v = Utils.readFile(AXIOM_FILE);
        for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String label = (String) u.elementAt(0);
			String code = (String) u.elementAt(1);
			String prop_code = (String) u.elementAt(2);
			if (prop_code.compareTo("P90") == 0) {
				String term = (String) u.elementAt(3);
				term = HTMLDecoder.decode(term);
				Vector w = new Vector();
				if (hmap.containsKey(code)) {
					w = (Vector) hmap.get(code);
				}
				if (!w.contains(term)) {
					w.add(term);
				}
				hmap.put(code, w);

				label = HTMLDecoder.decode(label);
				w = new Vector();
				if (hmap.containsKey(code)) {
					w = (Vector) hmap.get(code);
				}
				if (!w.contains(label)) {
					w.add(label);
				}
				hmap.put(code, w);
			}
		}
		return hmap;
	}

    public HashMap createTerm2CodesMap() {
		HashMap hmap = new HashMap();
		Vector v = Utils.readFile(AXIOM_FILE);
        for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String label = (String) u.elementAt(0);
			label = removeSpecialCharacters(label);
			label = HTMLDecoder.decode(label);

			String prop_code = (String) u.elementAt(2);

			if (prop_code.compareTo("P90") == 0) {
				String term = (String) u.elementAt(3);
				term = removeSpecialCharacters(term);
				term = HTMLDecoder.decode(term);

				String term_uc = term.toUpperCase();

				String label_uc = label.toUpperCase();
				Vector w = new Vector();
				if (hmap.containsKey(label_uc)) {
					w = (Vector) hmap.get(label_uc);
				}
				String code = (String) u.elementAt(1);
				if (!w.contains(code)) {
					w.add(code);
				}

				hmap.put(label_uc, w);

				term = term_uc;

				w = new Vector();
				if (hmap.containsKey(term)) {
					w = (Vector) hmap.get(term);
				}
				code = (String) u.elementAt(1);
				if (!w.contains(code)) {
					w.add(code);
				}
				hmap.put(term, w);
			}
		}
		return hmap;
	}

	public Vector substringSearch(String s) {
		s = removeSpecialCharacters(s);
        String s_cc = toCamelCase(s);
		Vector w = new Vector();

		Iterator it = camelCaseTerm2CodesMap.keySet().iterator();
		boolean contains = true;
		while (it.hasNext()) {
			String key = (String) it.next();
			if (key.indexOf(s_cc) != -1) {
				w.addAll((Vector) camelCaseTerm2CodesMap.get(key));
			}
		}
		return w;
	}

	public static Vector extractRows(Vector v, int n) {
		Vector w = new Vector();
		int lcv = 0;
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			w.add(line);
			lcv++;
			if (lcv == n) {
				break;
			}
		}
		return w;
	}



	public Vector diff(Vector w1, Vector w2) {
		HashSet hset1 = Utils.vector2HashSet(w1);
		HashSet hset2 = Utils.vector2HashSet(w2);
		Vector w = new Vector();
		Iterator it = hset1.iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			if (!hset2.contains(key)) {
				w.add(key);
			}
		}
		return w;
	}

    public boolean containsAcronym(String term, String acronym) {
		term = removeSpecialCharacters(term);
		Vector u = StringUtils.parseData(term, ' ');
		return u.contains(acronym);
	}

    public boolean containsAcronym(Vector terms, String acronym) {
		for (int i=0; i<terms.size(); i++) {
			String term = (String) terms.elementAt(i);
			if (containsAcronym(term, acronym)) return true;
		}
		return false;
	}

	public Vector substringSearch(Vector v, char delim) {
		Vector w0 = new Vector();
		String firstLine = (String) v.elementAt(0);
		Vector u = StringUtils.parseData(firstLine, '|');
		firstLine= Utils.vector2Delimited(u, "\t");
		firstLine = firstLine.replace("|", "\t");
		w0.add(firstLine + "\tConcepts where Acronym is not Presented");
		Vector w = new Vector();
		int lcv = 0;
		int i = 0;
		for (i=1; i<v.size(); i++) {
			lcv++;
			if (lcv == INCREMENT) {
				System.out.println("" + i + " out of " + v.size() + " completed.");
				lcv = 0;
			}
			String line = (String) v.elementAt(i);
			u = StringUtils.parseData(line, delim);
			String acronym = (String) u.elementAt(0);
		    String term = (String) u.elementAt(1);
		    String term_uc = term.toUpperCase();
		    Vector w1 = substringSearch(acronym);
		    Vector w2 = substringSearch(term_uc);
		    Vector w2_w1 = diff(w2, w1);
		    String delimedStr = "";
		    Vector w3 = new Vector();
		    if (w2_w1.size() > 0) {
				for (int k=0; k<w2_w1.size(); k++) {
					String code = (String) w2_w1.elementAt(k);
					Vector terms = (Vector) code2TermsMap.get(code);
					if (!containsAcronym(terms, acronym)) {
						w3.add(code);
					}
				}
				delimedStr = Utils.vector2Delimited(w3, "|");
			}
			w0.add(line + "\t" + delimedStr);
		}
		System.out.println("" + i + " out of " + v.size() + " completed.");
		return w0;
	}

    public Vector dumpTerms(String filename) {
		Vector v = Utils.readFile(filename);
		Vector w = new Vector();
		Vector w0 = new Vector();
		w0.add("Acronym\tFull String\tCode\tTerm");
		for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '\t');
			String pair = (String) u.elementAt(0);
			pair = pair.replace("|", "\t");
			String codes = "";
			Vector code_vec = new Vector();
			if (u.size() > 1) {
				codes = (String) u.elementAt(1);
				code_vec = StringUtils.parseData(codes, '|');
			}
			if (u.size() == 1) {
				line = line.replace("|", "\t");
				w0.add(line);
			} else {
				for (int j=0; j<code_vec.size(); j++) {
					String code = (String) code_vec.elementAt(j);
					Vector w2 = (Vector) code2TermsMap.get(code);
					for (int k=0; k<w2.size(); k++) {
						String term = (String) w2.elementAt(k);
						w0.add(pair + "\t" + code + "\t" + term);
					}
				}
			}
		}
		return w0;
	}

	public static String toCamelCase(String t) {
		t= t.replace("-", " ");
		Vector u = StringUtils.parseData(t, ' ');
		Vector w = new Vector();
		for (int i=0; i<u.size(); i++) {
			String token = (String) u.elementAt(i);
			if (token.length() > 0) {
				token = token.toLowerCase();
				String firstChar = "" + token.charAt(0);
				firstChar = firstChar.toUpperCase();
				String s = firstChar + token.substring(1, token.length());
				w.add(s);
			}
		}
		return Utils.vector2Delimited(w, " ");
	}

    public static void main(String[] args) {
		long ms = System.currentTimeMillis();

		String acronymfile = args[0];
		String colNum_str = args[1];
		int colNum = Integer.parseInt(colNum_str);
		char delim = '|';

	    AcronymAnalyzer test = new AcronymAnalyzer();
	    Vector v = Utils.readFile(acronymfile);
	    Vector w = test.substringSearch(v, delim);
	    Utils.saveToFile("analysis_" + acronymfile, w);
	    long ms1 = System.currentTimeMillis();
	    long timeElapsed = ms1 - ms;
	    System.out.println("Total run time: " + timeElapsed + " (milli-seconds).");

	}
}
