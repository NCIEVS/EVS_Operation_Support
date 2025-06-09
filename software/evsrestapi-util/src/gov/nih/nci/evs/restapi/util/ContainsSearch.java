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
public class ContainsSearch {
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

    static HashSet STOP_WORDS = null;
    static HashSet KEYWORDS = null;
    static HashMap signatureMap = null;
    static HashMap id2LabelMap = null;
    static HashSet keywords = null;


    static String STOPWORD_FILE = "stop_words.txt";

    static String AXIOM_FILE_NAME = "axiom_ThesaurusInferred_forTS.txt";
    static String AXIOM_FILE = ConfigurationController.reportGenerationDirectory + File.separator + AXIOM_FILE_NAME;

    static HashMap term2CodesMap = null;
    static HashMap term2TokensMap = null;

    static {
		long ms = System.currentTimeMillis();
		File file = new File(STOPWORD_FILE);
/*
		STOP_WORDS = new HashSet();
		if (file.exists()) {
			STOP_WORDS = LexicalMatching.createStopWordSet(STOPWORD_FILE);
		}
*/
        STOP_WORDS = LexicalMatching.STOP_WORDS;

		stemmer = new PorterStemmer();
		//signatureMap = createSignatureMap();
		//id2LabelMap = createId2LabelMap();
		keywords = LexicalMatching.KEYWORDS;

		loadToken2CodeMap();

		System.out.println("Total initialization run time (ms): " + (System.currentTimeMillis() - ms));
	}

	public static boolean isFiller(String wd) {
		wd = wd.toLowerCase();
		return STOP_WORDS.contains(wd);
	}

	public static Vector removeFillers(Vector wds) {
		Vector w = new Vector();
		for (int i=0; i<wds.size(); i++) {
			String wd = (String) wds.elementAt(i);
			if (!isFiller(wd)) {
				w.add(wd);
			}
		}
		return w;
	}

/*
Recombinant Amphiregulin|C1000|P90|AMPHIREGULIN|P383$PT|P384$FDA|P385$7MGE0HPM2H|P386$UNII
Recombinant Amphiregulin|C1000|P90|AR|P383$AB|P384$NCI
Recombinant Amphiregulin|C1000|P90|CRDGF|P383$AB|P384$NCI
*/
	public static void loadToken2CodeMap() {
		term2CodesMap = new HashMap();
		term2TokensMap = new HashMap();
		id2LabelMap = new HashMap();

        Vector w = new Vector();
		Vector v = Utils.readFile(AXIOM_FILE);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String type = (String) u.elementAt(2);
			if (type.compareTo("P90") == 0) {
				String label = (String) u.elementAt(0);
				Vector tokens = LexicalMatching.tokenize(label);
				String code = (String) u.elementAt(1);
				id2LabelMap.put(code, label);
				w = new Vector();
				if (term2CodesMap.containsKey(label)) {
					w = (Vector) term2CodesMap.get(label);
				}
				if (!w.contains(code)) {
					w.add(code);
				}
				term2CodesMap.put(label, w);
				tokens = LexicalMatching.tokenize(label);
				//addKeywords(tokens);
				term2TokensMap.put(label, removeFillers(tokens));

				label = (String) u.elementAt(3);
				w = new Vector();
				if (term2CodesMap.containsKey(label)) {
					w = (Vector) term2CodesMap.get(label);
				}
				if (!w.contains(code)) {
					w.add(code);
				}
				term2CodesMap.put(label, w);
				tokens = LexicalMatching.tokenize(label);
				//addKeywords(tokens);
				term2TokensMap.put(label, removeFillers(tokens));
			}
		}
	}

	public static Vector contains(String t) {
		Vector wds = LexicalMatching.tokenize(t);
		wds = removeFillers(wds);
		Iterator it = term2CodesMap.keySet().iterator();
		Vector codes = new Vector();
		while (it.hasNext()) {
			String term = (String) it.next();
			Vector w = (Vector) term2TokensMap.get(term);
			boolean found = true;
			for (int i=0; i<wds.size(); i++) {
				String s = (String) wds.elementAt(i);
				if (!w.contains(s)) {
					found = false;
					break;
				}
			}
			if (found) {
				Vector w1 = (Vector) term2CodesMap.get(term);
				for (int k=0; k<w1.size(); k++) {
					String s = (String) w1.elementAt(k);
					if (!codes.contains(s)) {
						codes.add(s);
					}
				}
			}
		}
		return codes;
	}

	public static String run(String filename, int col) {
		Vector v = Utils.readFile(filename);
		Vector output_vec = new Vector();
		for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '\t');
			String t = (String) u.elementAt(col);
			Vector w = contains(t);
			output_vec.add("\n(" + i + ") " + line);
			if (w.size() == 0) {
				output_vec.add("\nNo match.");
			} else {
				for (int j=0; j<w.size(); j++) {
					String code = (String) w.elementAt(j);
					output_vec.add((String) id2LabelMap.get(code) + " (" + code + ")");
				}
			}
		}
		Utils.saveToFile("contains_" + filename, output_vec);
		return "contains_" + filename;
	}

	public static void main(String[] args) {
		String filename = args[0];
		String outputfile = run(filename, 1);
		System.out.println(outputfile + " generated.");
	}

}