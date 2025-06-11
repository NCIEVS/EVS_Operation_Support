package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.bean.*;
import gov.nih.nci.evs.restapi.common.*;
import gov.nih.nci.evs.restapi.config.*;
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
import opennlp.tools.stemmer.PorterStemmer;
import org.json.*;

/**
 * <!-- LICENSE_TEXT_START -->
 * Copyright 2022 Guidehouse. This software was developed in conjunction
 * with the National Cancer Institute, and so to the extent government
 * employees are co-authors, any rights in such works shall be subject
 * to Title 17 of the United States Code, section 105.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the disclaimer of Article 3,
 *      below. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *   2. The end-user documentation included with the redistribution,
 *      if any, must include the following acknowledgment:
 *      "This product includes software developed by Guidehouse and the National
 *      Cancer Institute."   If no such end-user documentation is to be
 *      included, this acknowledgment shall appear in the software itself,
 *      wherever such third-party acknowledgments normally appear.
 *   3. The names "The National Cancer Institute", "NCI" and "Guidehouse" must
 *      not be used to endorse or promote products derived from this software.
 *   4. This license does not authorize the incorporation of this software
 *      into any third party proprietary programs. This license does not
 *      authorize the recipient to use any trademarks owned by either NCI
 *      or GUIDEHOUSE
 *   5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED
 *      WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *      OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE
 *      DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
 *      GUIDEHOUSE, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT,
 *      INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 *      BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *      LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *      CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 *      LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 *      ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *      POSSIBILITY OF SUCH DAMAGE.
 * <!-- LICENSE_TEXT_END -->
 */

/**
 * @author EVS Team
 * @version 1.0
 *
 * Modification history:
 *     Initial implementation kim.ong@nih.gov
 *
 */
public class LexicalMatching {
    JSONUtils jsonUtils = null;
    HTTPUtils httpUtils = null;
    String named_graph = null;
    String prefixes = null;
    String serviceUrl = null;
    String restURL = null;
    String named_graph_id = ":NHC0";
    String BASE_URI = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl";

    ParserUtils parser = new ParserUtils();
    HashMap nameVersion2NamedGraphMap = null;
    HashMap ontologyUri2LabelMap = null;
    String version = null;
    String username = null;
    String password = null;
    OWLSPARQLUtils owlSPARQLUtils = null;

    static PorterStemmer stemmer = null;

    public static HashSet STOP_WORDS = null;
    public static HashSet KEYWORDS = null;
    public static HashMap signatureMap = null;
    public static HashMap id2LabelMap = null;

    static String STOPWORD_FILE = "stop_words.txt";

    static String AXIOM_FILE_NAME = "axiom_ThesaurusInferred_forTS.txt";
    static String FULL_SYN_FILE = ConfigurationController.reportGenerationDirectory + File.separator + AXIOM_FILE_NAME;

    static {
		long ms = System.currentTimeMillis();
		File file = new File(STOPWORD_FILE);

		STOP_WORDS = new HashSet();
		if (file.exists()) {
			STOP_WORDS = createStopWordSet(STOPWORD_FILE);
		}
		stemmer = new PorterStemmer();
		signatureMap = createSignatureMap();
		id2LabelMap = createId2LabelMap();
		KEYWORDS = createKeywordSet(FULL_SYN_FILE);
		System.out.println("Total initialization run time (ms): " + (System.currentTimeMillis() - ms));
	}

	public static Vector readFile(String datafile) {
		Vector v = new Vector();
        try {
			File file = new File(datafile);
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			BufferedReader br = null;
			try {
				br = new BufferedReader(new InputStreamReader(bis));
			} catch (Exception ex) {
				return null;
			}

            while (true) {
                String line = br.readLine();
				if (line == null) {
					break;
				}
				v.add(line);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return v;
	}

	public static String getLabel(String id) {
		return (String) id2LabelMap.get(id);
	}

	public static boolean isFiller(String word) {
		return STOP_WORDS.contains(word);
	}

	public static boolean isKeyword(String word) {
		return KEYWORDS.contains(word);
	}

    public LexicalMatching(String serviceUrl, String named_graph, String username, String password) {
		this.serviceUrl = serviceUrl;
    	this.named_graph = named_graph;
    	this.username = username;
    	this.password = password;
        this.owlSPARQLUtils = new OWLSPARQLUtils(serviceUrl, username, password);
        this.owlSPARQLUtils.set_named_graph(named_graph);
    }

    public OWLSPARQLUtils getOWLSPARQLUtils() {
		return this.owlSPARQLUtils;
	}

    public static String removeSpecialCharacters(String t) {
		t = t.replace("/", " ");
		t = t.replace("-", " ");
		t = t.replace("(", " ");
		t = t.replace(")", " ");
		t = t.replace(",", " ");
		t = t.replace(";", " ");
		t = t.replace("'s", " ");
		return t;
	}

	public static HashSet createKeywordSet(String filename) {
		HashSet hset = new HashSet();
		Vector v = readFile(filename);
        for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector values = StringUtils.parseData(line, '|');
			String term = (String) values.elementAt(3);
			String type = (String) values.elementAt(2);
			if (type.compareTo("P90") == 0) {
				Vector w = tokenize(term);
				for (int j=0; j<w.size(); j++) {
					String word = (String) w.elementAt(j);
					if (!hset.contains(word)) {
						hset.add(word);
					}
				}
			}
		}
		System.out.println("Number of keywords: " + hset.size());
		return hset;
	}

    public static String getSignature(String term) {
		Vector words = tokenize(term);
		Vector stemmed_words = new Vector();
		for (int i=0; i<words.size(); i++) {
			String word = (String) words.elementAt(i);
			if (!isFiller(word)) {
				String stemmed_word = stemTerm(word);
				stemmed_words.add(stemmed_word);
			}
		}
		stemmed_words = new SortUtils().quickSort(stemmed_words);
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<stemmed_words.size(); i++) {
			String stemmed_word = (String) stemmed_words.elementAt(i);
			buf.append(stemmed_word).append("$");
		}
		String t = buf.toString();
		if (t.length() == 0) return "";
		return t.substring(0, t.length()-1);
	}

	public static HashSet createStopWordSet(String filename) {
		HashSet hset = new HashSet();
		Vector v = readFile(filename);
        for (int i=0; i<v.size(); i++) {
			String word = (String) v.elementAt(i);
			if (!hset.contains(word)) {
				hset.add(word);
			}
		}
		System.out.println("Number of stop words: " + hset.size());
		return hset;
	}


	public static String stemTerm(String term) {
	    return stemmer.stem(term);
	}

    public static Vector tokenize(String term) {
		term = term.toLowerCase();
		term = removeSpecialCharacters(term);
		Vector words = new Vector();
		StringTokenizer st = new StringTokenizer(term);
		while (st.hasMoreTokens()) {
		     words.add(st.nextToken());
		}
		return words;
	}

    public static void sort(String filename) {
		HashMap hmap = new HashMap();
		Vector v = readFile(filename);
        for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			line = line.trim();
			Vector u = StringUtils.parseData(line, '\t');
			String term = (String) u.elementAt(0);
			String match = (String) u.elementAt(1);
			term = term.trim();
			if (term.length() > 0) {
				Vector w = new Vector();
				if (hmap.containsKey(term)) {
					w = (Vector) hmap.get(term);
				}
				w.add(match);
				hmap.put(term, w);
			}
		}
        System.out.println(hmap.keySet().size());
        Iterator it = hmap.keySet().iterator();
        int lcv = 0;
        Vector keys = new Vector();
        while (it.hasNext()) {
			String key = (String) it.next();
			keys.add(key);
		}
		keys = new SortUtils().quickSort(keys);

        for (int i=0; i<keys.size(); i++) {
			String key = (String) keys.elementAt(i);
			lcv++;
			Vector values = (Vector) hmap.get(key);
			Utils.dumpVector("(" + lcv + ") " + key, values);
		}
	}

	public static HashMap createId2LabelMap() {
		HashMap hmap = new HashMap();
		Vector v = readFile(FULL_SYN_FILE);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String label = (String) u.elementAt(0);
			String code = (String) u.elementAt(1);
			hmap.put(code, label);
		}
		return hmap;
	}

	public static HashMap createSignatureMap() {
		HashMap hmap = new HashMap();
		Vector v = readFile(FULL_SYN_FILE);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String term = (String) u.elementAt(3);
			String code = (String) u.elementAt(1);
			String type = (String) u.elementAt(2);
			if (type.compareTo("P90") == 0) {
				String signature = getSignature(term);
				Vector w = new Vector();
				if (hmap.containsKey(signature)) {
					w = (Vector) hmap.get(signature);
				}
				if (!w.contains(code)) {
					w.add(code);
				}
				hmap.put(signature, w);
			}
		}
		return hmap;
	}

	public static String lexicalMatch(String term) {
		if (term.length() == 0) return "No match";
		//Vector w = tokenize(term);
		String signature = getSignature(term);
		if (signatureMap.containsKey(signature)) {
			StringBuffer buf = new StringBuffer();
			Vector w = (Vector) signatureMap.get(signature);
			for (int k=0; k<w.size(); k++) {
				String code = (String) w.elementAt(k);
				String label = (String) getLabel(code);
				buf.append(label + "|" + code).append("$");
			}
			String t = buf.toString();
			return t.substring(0, t.length()-1);
		} else {
			return "No match";
		}
	}

	public static boolean checkCoocurrence(String wd_1, String wd_2) {
		String stemmed_wd_1 = stemTerm(wd_1);
		String stemmed_wd_2 = stemTerm(wd_2);
		stemmed_wd_1 = stemmed_wd_1.toLowerCase();
		stemmed_wd_2 = stemmed_wd_2.toLowerCase();

		Iterator it = signatureMap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			key = key + "$";
			if (key.indexOf(stemmed_wd_1) != -1 && key.indexOf(stemmed_wd_2) != -1) {
				return true;
			}
		}
		return false;
	}

    public static String run(String termfile) {
		return run(termfile, true, 0);
	}

    public static String run(String termfile, boolean header, int term_col) {
		int total = 0;
		int num_matches = 0;
		Vector v = readFile(termfile);
		Vector w = new Vector();
		int istart = 0;
		if (header) {
			String t = (String) v.elementAt(0);
			if (t.endsWith("\t")) {
				t = t.substring(0, t.length()-1);
			}
			w.add(t + "\tMatches");
			istart = 1;
		}

        for (int i=istart; i<v.size(); i++) {
			total++;
			String line = (String) v.elementAt(i);
			if (line.endsWith("\t")) {
				line = line.substring(0, line.length()-1);
			}
			Vector u = StringUtils.parseData(line, '\t');
			String term0 = (String) u.elementAt(term_col);
			String term = term0.trim();
			String result = lexicalMatch(term);
			if (result.compareTo("No match") != 0) {
				num_matches++;
			}
			w.add(line + "\t" + result);
		}
		String outputfile = "result_" + termfile;
		Utils.saveToFile(outputfile, w);
        System.out.println("" + num_matches + " out of " + total + " matches.");

        return outputfile;
	}

	public static void analyze(String filename, int col) {
		Vector v = readFile(filename);
		Vector w = new Vector();
		int istart = 0;
        for (int i=istart; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '\t');
			String term = (String) u.elementAt(col);
			int j = i+1;
			w.add("(" + j + ") " + term);
			Vector wds = tokenize(term);
			for (int k=0; k<wds.size(); k++) {
				String word = (String) wds.elementAt(k);
				if (isKeyword(word)) {
					if (isFiller(word)) {
						w.add("\t" + word + " (*)");
					} else {
						w.add("\t" + word);
					}
				} else {
					w.add("\t" + word + " (?)");
				}
			}
			w.add("\n");
		}

		Utils.saveToFile("analysis_" + filename, w);
	}

	public static void tallyResults(String filename, int result_col) {
		Vector v = Utils.readFile(filename);
		Vector w = new Vector();
		Vector w1 = new Vector();
		int total = 0;
		int matched = 0;
		int unmatched = 0;
		HashSet hset = new HashSet();

        for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '\t');
			String t = (String) u.elementAt(0);
			String s = (String) u.elementAt(result_col);
			if (!hset.contains(t)) {
				hset.add(t);
				total++;
				if (s.compareTo("No match") == 0) {
					unmatched++;
					w1.add(line);
				} else {
					matched++;
					System.out.println(line);
					w.add(line);
				}
			}
		}
		System.out.println("matched: " + matched);
		System.out.println("unmatched: " + unmatched);
		System.out.println("total: " + total);
		Utils.saveToFile("matched_" + filename, w);
		Utils.saveToFile("notmatched_" + filename, w1);
	}

    public static void main(String[] args) {
		String termfile = "normalized_data_NCCN_regimens_05-08-2025.txt";
		String outputfile = run(termfile, true, 1);
		tallyResults(outputfile, 2);
		analyze("notmatched_" + outputfile, 1);
	}
}
