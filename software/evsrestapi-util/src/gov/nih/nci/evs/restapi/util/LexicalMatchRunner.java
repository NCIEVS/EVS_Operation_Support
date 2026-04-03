package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;

import java.io.*;
import java.text.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;
import java.nio.charset.Charset;

import java.util.regex.*;
import opennlp.tools.stemmer.PorterStemmer;

/**
 * <!-- LICENSE_TEXT_START -->
 * Copyright 2008-2017 NGIS. This software was developed in conjunction
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
 *      "This product includes software developed by NGIS and the National
 *      Cancer Institute."   If no such end-user documentation is to be
 *      included, this acknowledgment shall appear in the software itself,
 *      wherever such third-party acknowledgments normally appear.
 *   3. The names "The National Cancer Institute", "NCI" and "NGIS" must
 *      not be used to endorse or promote products derived from this software.
 *   4. This license does not authorize the incorporation of this software
 *      into any third party proprietary programs. This license does not
 *      authorize the recipient to use any trademarks owned by either NCI
 *      or NGIS
 *   5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED
 *      WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *      OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE
 *      DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
 *      NGIS, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT,
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
 *     Initial implementation kim.ong@ngc.com
 *
 */


public class LexicalMatchRunner {
	static PorterStemmer stemmer = null;
    public HashMap signatureMap = null;
    public HashMap term2CodeMap = new HashMap();
    public HashMap code2PTMap = new HashMap();
    public HashMap code2FULLSYNMap = new HashMap();
    public HashMap nciDefMap = new HashMap();
    public Vector term_vec = new Vector();

    static HashSet STOP_WORDS = null;
    static String STOPWORD_FILE = "stop_words.txt";
    static boolean APPLY_STEMMING = true;
    static String AXIOM_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.axiomfile;
    static HashSet retiredConcepts = new HashSet();
    static HashSet treedata = new HashSet();

    //C100067|P310|Retired_Concept
    static HashSet createRetiredConceptHashSet() {
		long ms = System.currentTimeMillis();
		HashSet retiredConcepts = new HashSet();
		String owlfile = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile;
		OWLScanner scanner = new OWLScanner(owlfile);
		String prop_code = "P310";
		File file = new File(prop_code + ".txt");
		Vector v = null;
		if (!file.exists()) {
			v = scanner.extractProperties(scanner.get_owl_vec(), prop_code);
			Utils.saveToFile(prop_code + ".txt", v);
		} else {
			v = Utils.readFile(prop_code + ".txt");
		}
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String status = (String) u.elementAt(2);
			if (status.compareTo("Retired_Concept") == 0) {
				retiredConcepts.add((String) u.elementAt(0));
			}
		}
        System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
        return retiredConcepts;
	}

    static {
   	   stemmer = new PorterStemmer();
   	   STOP_WORDS = createStopWordSet(STOPWORD_FILE);
   	   retiredConcepts = createRetiredConceptHashSet();
    }

    public static boolean isRetired(String code) {
		return retiredConcepts.contains(code);
	}

    public static void set_APPLY_STEMMING(boolean bool) {
		APPLY_STEMMING = bool;
	}

    public String getNCIDef(String code) {
		if (!nciDefMap.containsKey(code)) return "";
		return (String) nciDefMap.get(code);
	}

	public Vector getMatchedCodes(String signature) {
		if (!signatureMap.containsKey(signature)) return new Vector();
		return (Vector) signatureMap.get(signature);
	}

	public static HashMap createCode2FULLSYNMap(Vector v) {
		HashMap hmap = new HashMap();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String label = (String) u.elementAt(0);
			String code = (String) u.elementAt(1);
			String term = (String) u.elementAt(2);
			Vector w = new Vector();
			if (hmap.containsKey(code)) {
				w = (Vector) hmap.get(code);
			}
			if (!w.contains(term)) {
				w.add(term);
			}
			w = new SortUtils().quickSort(w);
			hmap.put(code, w);
		}
		return hmap;
	}

	public String getFullSyns(String code) {
		if (!code2FULLSYNMap.containsKey(code)) return "";
		StringBuffer buf = new StringBuffer();
		Vector terms = (Vector) code2FULLSYNMap.get(code);
		for (int i=0; i<terms.size(); i++) {
			String term = (String) terms.elementAt(i);
			buf.append(term).append("$");
		}
		String t = buf.toString();
		return t.substring(0, t.length()-1);
	}

	public static HashSet createStopWordSet(String filename) {
		File f = new File(filename);
		HashSet hset = new HashSet();
		if(f.exists() && !f.isDirectory()) {
			Vector v = Utils.readFile(filename);
			for (int i=0; i<v.size(); i++) {
				String word = (String) v.elementAt(i);
				if (!hset.contains(word)) {
					hset.add(word);
				}
			}
		} else {
			System.out.println("WARNING: file " + filename + " does not exist.");
		}
		return hset;
	}

	public LexicalMatchRunner(String axiomfile, HashSet branch) {
		long ms = System.currentTimeMillis();
		File f = new File(axiomfile);
		if(f.exists() && !f.isDirectory()) {
			createSupportingHashMaps(axiomfile, branch);
			System.out.println("Total initialization run time (ms): " + (System.currentTimeMillis() - ms));
		} else {
			System.out.println(axiomfile + " does not exists -- program aborts.");
			System.exit(1);
		}
	}

	public String getPT(String code) {
		if (!code2PTMap.containsKey(code)) return "";
		return (String) code2PTMap.get(code);
	}

    public String getPTs(Vector codes, char delim) {
		StringBuffer buf = new StringBuffer();
        for (int i=0; i<codes.size(); i++) {
			String code = (String) codes.elementAt(i);
			String pt = getPT(code);
			buf.append(pt).append("" + delim);
		}
		String s = buf.toString();
		if (codes.size() > 0) {
			s = s.substring(0, s.length()-1);
		}
		return s;
	}


    public void createSupportingHashMaps(String axiomfile, HashSet branch) {
		Vector v = Utils.readFile(axiomfile);
		term2CodeMap = new HashMap();
		code2PTMap = new HashMap();
		term_vec = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			//Recombinant Amphiregulin|C1000|P90|Recombinant Amphiregulin|P383$PT|P384$NCI
            String label = (String) u.elementAt(0);
			String code = (String) u.elementAt(1);
			String prop_code = (String) u.elementAt(2);
			String term = (String) u.elementAt(3);

			if (prop_code.compareTo("P90") == 0) {
				String term_lc = term.toLowerCase();
				term2CodeMap.put(term_lc, (String) u.elementAt(1));
				term_vec.add(label + "|" + code + "|" + term);
				if (u.contains("P383$PT") && u.contains("P384$NCI")) {
					code2PTMap.put((String) u.elementAt(1), (String) u.elementAt(0));
				}
			}
		}
		nciDefMap = createNCIDefMap(axiomfile);
		code2FULLSYNMap = createCode2FULLSYNMap(term_vec);
		signatureMap = createSignatureMap(term_vec);
	}

    public HashMap createNCIDefMap(String axiomfile) {
		HashMap nciDefMap = new HashMap();
		Vector v = Utils.readFile(axiomfile);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String prop_code = (String) u.elementAt(2);
			if (prop_code.compareTo("P97") == 0) {
				if (u.contains("P378$NCI")) {
					String ncit_def = (String) u.elementAt(3);
					nciDefMap.put((String) u.elementAt(1), ncit_def);
				}
			}
		}
		return nciDefMap;
	}

	public HashMap createSignatureMap(Vector v) {
		HashMap hmap = new HashMap();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String term = (String) u.elementAt(2);
			String code = (String) u.elementAt(1);

			if (!isRetired(code)) {
				String label = (String) u.elementAt(0);
				String signature = getSignature(term);
				Vector w = new Vector();
				if (hmap.containsKey(signature)) {
					w = (Vector) hmap.get(signature);
				}
				if (!w.contains(code)) {
					w.add(code);
				}
				hmap.put(signature, w);

				w = new Vector();
				signature = getSignature(label);
				if (hmap.containsKey(signature)) {
					w = (Vector) hmap.get(signature);
				}
				if (!w.contains(code)) {
					w.add(code);
				}
				hmap.put(signature, w);
			}
		}
		v.clear();
		return hmap;
	}


	public static String stemTerm(String term) {
	    if (APPLY_STEMMING) {
			if (term.length() <= 3) return term;
			return stemmer.stem(term);
		}
	    return term;
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

	public static boolean isFiller(String word) {
		return STOP_WORDS.contains(word);
	}

    public String getSignature(String term) {
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

    public static String removeSpecialCharacters(String t) {
		t = t.replace("(", " ");
		t = t.replace(")", " ");
		t = t.replace(",", " ");
		t = t.replace(";", " ");
		t = t.replace("'s", " ");
		t = t.replace("-", " ");
		t = t.replace("/", " ");
		t = t.replace("+", " ");
		t = t.replace("—", " ");
		t = t.replace(".", " ");

		//add &
		t = t.replace("&", " ");
		return t;
	}

    public static Vector parseData(String line, char delimiter) {
		if(line == null) return null;
		Vector w = new Vector();
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<line.length(); i++) {
			char c = line.charAt(i);
			if (c == delimiter) {
				w.add(buf.toString());
				buf = new StringBuffer();
			} else {
				buf.append(c);
			}
		}
		w.add(buf.toString());
		return w;
	}

    public Vector get_matched_codes(String term) {
		String signature = getSignature(term);
		//System.out.println("signature: " + signature);
		return getMatchedCodes(signature);
	}

    public Vector extractColumnData(Vector v, int col, char delim) {
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, delim);
			String t = (String) u.elementAt(col);
			w.add(t);
		}
		return w;
	}

    public static void reviewResultFile(String filename) {
        Vector v = Utils.readFile(filename);
        int knt = 0;
		for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '\t');
			String code = (String) u.elementAt(2);
			if (isRetired(code)) {
				knt++;
			}
		}
        System.out.println("Number of retired concepts: " + knt);
	}

	public static String run(String datafile, String outputfile) {
		return run(datafile, outputfile, 0);
	}


	public static String run(String datafile, String outputfile, int col) {
		String axiomfile = AXIOM_FILE;
		String branchfile = ConfigurationController.branchfile;
        HashSet branch = new HashSet();
        Vector codes = null;
        if (branchfile != null && branchfile.compareTo("null") != 0) {
			File file = new File(branchfile);
			if (!file.exists()) {
				System.out.println(branchfile + " does not exists.");
				String HIER_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile;
				Vector v = Utils.readFile(HIER_FILE);
				HierarchyHelper hh = new HierarchyHelper(v);
				int n = branchfile.indexOf(".");
				String root = branchfile.substring(0, n);//"C62634";
				codes = hh.get_transitive_closure_v3(root);
				Utils.saveToFile(branchfile, v);
			} else {
				System.out.println(branchfile + " exists.");
				codes = Utils.readFile(branchfile);
			}
			branch = Utils.vector2HashSet(codes);
		}

		LexicalMatchRunner lmr = new LexicalMatchRunner(axiomfile, branch);
		return lmr.submit(datafile, outputfile, branch, col);
	}

	public String submit(String datafile, String outputfile, HashSet branch) {
		return submit(datafile, outputfile, branch, 0);
	}

	// *************************************************************************
	public String preprocessing(String vbt) {
		vbt = vbt.toLowerCase();
		//vbt = vbt.replace("disease not specified", "");
		vbt = vbt.trim();
		return vbt;
	}
	// *************************************************************************

	public String submit(String datafile, String outputfile, HashSet branch, int col) {
		if (branch == null) {
			System.out.println("branch == null");
		} else {
			System.out.println("branch: " + branch.size());
		}
		long ms = System.currentTimeMillis();
		Vector codes = new Vector();
		//int col = 0;
		Vector no_matches = new Vector();
		Vector matches = new Vector();
        Vector v = Utils.readFile(datafile);
        Vector w = new Vector();
        String header = (String) v.elementAt(0);
        w.add(header + "\tMatched NCIt Code(s)\tNCI PT(s)\tNCI SY(s)");
        no_matches.add(header);
        matches.add(header);
        String term = null;
		for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			line = line.trim();
			if (line.length() > 0) {
				if (line.indexOf("No match") == -1) {
					w.add(line);
					matches.add(line);
				} else {
					Vector u = StringUtils.parseData(line, '\t');
					term = (String) u.elementAt(col);
					String vbt = term;

					vbt = preprocessing(vbt);

					boolean matched = false;
					Vector codes_0 = get_matched_codes(vbt);

					codes = new Vector();
					for (int j=0; j<codes_0.size(); j++) {
						String code = (String) codes_0.elementAt(j);
						if (branch != null && branch.size() > 0) {
							if (branch.contains(code) && !ExactMatchByTerm.is_retired(code)) {
								codes.add(code);
							}
						} else {
							if (!ExactMatchByTerm.is_retired(code)) {
								codes.add(code);
							}
						}
					}
					if (codes.size() > 0) {
						String matchData = ExactMatchByTerm.getMatchedData(codes);
						line = line.replace("No match", matchData);
						w.add(line);
						matches.add(line);
						matched = true;
					} else {
						no_matches.add(term);
						w.add(line);
					}

				}
			}
		}
		Utils.saveToFile(outputfile, w);
		Utils.saveToFile("no_matches_" + outputfile, no_matches);
		Utils.saveToFile("matches_" + outputfile, matches);
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
		System.out.println(outputfile + " generated.");

		int n1 = v.size()-1;
		int n2 = matches.size()-1;
		int n3 = no_matches.size()-1;

		System.out.println("Number of terms: " + n1);
		System.out.println("Number of matches: " + n2);
		System.out.println("Number of no matches: " + n3);

		return outputfile;
	}
}
