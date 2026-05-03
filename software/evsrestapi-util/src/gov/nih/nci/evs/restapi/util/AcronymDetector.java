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
public class AcronymDetector {

	static String NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile; //"ThesaurusInferred_forTS.owl";
	static String PARENT_CHILD_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile; // "parent_child.txt";
	static String RESTRICTION_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.rolefile; //"roles.txt";
	static String AXIOM_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.axiomfile;
	static String A8_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.subsetfile;
    static HashMap code2TermsHashMap = null;
    static String ACRONYM_DATA = "acronyms.txt";
    static int MAX_ACRONYM_LENGTH = 5;
    static int DEFAULT_MAX_ACRONYM_LENGTH = 5;


    static {
		code2TermsHashMap = createCode2TermsHashMap();
	}

    public static HashMap createCode2TermsHashMap() {
		HashMap hmap = new HashMap();
		Vector v = Utils.readFile(AXIOM_FILE);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String label = (String) u.elementAt(0);
			String code = (String) u.elementAt(1);
			String prop_code = (String) u.elementAt(2);
			if (prop_code.compareTo("P90") == 0) {
				Vector w = new Vector();
				if (hmap.containsKey(code)) {
					w = (Vector) hmap.get(code);
				}
				if (!w.contains(label)) {
					w.add(label);
				}
				hmap.put(code, w);
				String term = (String) u.elementAt(3);
				if (!w.contains(term)) {
					w.add(term);
				}
				hmap.put(code, w);
			}
		}
		return hmap;
	}



    public static String removeSpecialCharacters(String term) {
		term = term.replace("(", "");
		term = term.replace(")", "");
		term = term.replace(",", "");
		return term;
	}

	public static void set_MAX_ACRONYM_LENGTH(int max_acronym_length) {
		MAX_ACRONYM_LENGTH = max_acronym_length;
	}

    public static HashMap findAcronyms(String code) {
		HashMap hmap = new HashMap();
		Vector w = (Vector) code2TermsHashMap.get(code);
		for (int i=0; i<w.size(); i++) {
			String term = (String) w.elementAt(i);
			String term_uc = term.toUpperCase();
			if (term.compareTo(term_uc) != 0) {
				term = removeSpecialCharacters(term);
				Vector u = StringUtils.parseData(term, ' ');
				for (int j=0; j<u.size(); j++) {
					String token = (String) u.elementAt(j);
					String token_uc = token.toUpperCase();
					if (token.compareTo(token_uc) == 0) {
						if (token.length() <= MAX_ACRONYM_LENGTH) {
							for (int k=0; k<w.size(); k++) {
								String t = (String) w.elementAt(k);
								String t0 = t;
								t = t.toUpperCase();
								Vector u1 = StringUtils.parseData(t0, ' ');
								Vector u2 = StringUtils.parseData(t, ' ');
								StringBuffer buf0 = new StringBuffer();
								StringBuffer buf = new StringBuffer();
								for (int k1=0; k1<u2.size(); k1++) {
									String token_1 = (String) u2.elementAt(k1);
									if (token_1.length() > 0) {
										buf.append(token_1.charAt(0));
									}
								}
								String s = buf.toString();
								if (s.indexOf(token_uc) != -1) {
									int n = s.indexOf(token_uc);
									int m = n + token_uc.length();
									Vector words = new Vector();
									for (int k2=n; k2<m; k2++) {
										String word = (String) u1.elementAt(k2);
										words.add(word);
									}
									String s2 = Utils.vector2Delimited(words, " ");
									if (s2.endsWith(",")) {
										s2 = s2.substring(0, s2.length()-1);
									}
									Vector w3 = new Vector();
									if (hmap.containsKey(token)) {
									    w3 = (Vector) hmap.get(token);
									}
									if (!w3.contains(s2)) {
										w3.add(s2);
									}
									if (token.length() > 1) {
										hmap.put(token, w3);
									}
								}
							}
						}
					}
				}
			}
		}
        return hmap;
	}

    public static boolean isAlphanumeric(String str) {
		if (str == null || str.isEmpty()) return false;

		char c = str.charAt(0);
		if (Character.isDigit(c)) {
			return false;
		}

		if (str.length() == 1) return false;


		try {
			int n = Integer.parseInt(str);
			return false;
		} catch (Exception ex) {

		}


		for (char ch : str.toCharArray()) {
			if (!Character.isLetterOrDigit(ch)) {
				return false;
			}
		}
		return true;
	}

	public static void generateAcronymData() {
		Vector v = Utils.readFile(AXIOM_FILE);
		HashSet acronyms = new HashSet();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String label = (String) u.elementAt(0);
			String code = (String) u.elementAt(1);
			String prop_code = (String) u.elementAt(2);
			if (prop_code.compareTo("P90") == 0) {
				String term = (String) u.elementAt(3);
				String term_uc = term.toUpperCase();
				if (term.compareTo(term_uc) != 0) {
					Vector u2 = StringUtils.parseData(term, ' ');
					for (int j=0; j<u2.size(); j++) {
						String token = (String) u2.elementAt(j);
						String token_uc = token.toUpperCase();
						if (token.compareTo(token_uc) == 0) {
							if (token.length() <= MAX_ACRONYM_LENGTH) {
								if (isAlphanumeric(token)) {
									acronyms.add(token);
								}
							}
						}
					}
				} else {
					if (term.indexOf(" ") == -1) {
						if (term.length() <= MAX_ACRONYM_LENGTH) {
							if (isAlphanumeric(term)) {
								acronyms.add(term);
							}
						}
					}
				}
			}
		}
		Vector acronym_vec = Utils.hashSet2Vector(acronyms);
		acronym_vec = new SortUtils().quickSort(acronym_vec);
		Utils.saveToFile(ACRONYM_DATA, acronym_vec);
	}

    public static void dumpData(String label, HashMap hmap) {
		System.out.println(label);
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			Object obj = hmap.get(key);
			if (obj instanceof Vector) {
				Utils.dumpVector(key, (Vector) obj);
			} else if (obj instanceof String) {
				System.out.println(key + " --> " + (String) obj);
			} else if (obj instanceof HashMap) {
				Utils.dumpHashMap(key, (HashMap) obj);
			}
		}
	}

    public static HashMap findAcronyms(Vector v) {
		HashMap hmap = new HashMap();
		HashSet hset = new HashSet();
		int lcv = 0;
		int total = v.size();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			lcv++;
			if (lcv == 100) {
				System.out.println("" + i + " out of " + total + " completed.");
				lcv = 0;
			}
			if (line.indexOf("No match") == -1) {
				Vector u = StringUtils.parseData(line, '\t');
				String s = (String) u.elementAt(1);
				Vector u2 = StringUtils.parseData(s, '|');
				String code = (String) u2.elementAt(1);
				if (!hset.contains(code)) {
					hset.add(code);
					HashMap map = findAcronyms(code);
					if (map.keySet().size() > 0) {
						hmap.put(code, map);
					}
				}
			}
		}
		return hmap;
	}

	public static boolean isInteger(String t) {
		try {
			int int_key = Integer.parseInt(t);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}


	public static Vector sortAcronyms(HashMap hmap) {
		Vector w = new Vector();
		HashSet hset = new HashSet();
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String code = (String) it.next();
			HashMap map = (HashMap) hmap.get(code);
			Iterator it2 = map.keySet().iterator();
			while (it2.hasNext()) {
				String key = (String) it2.next();
				if (!isInteger(key)) {
					Vector values = (Vector) map.get(key);
					for (int j=0; j<values.size(); j++) {
						String value = (String) values.elementAt(j);
						String value_lc = value.toLowerCase();
						Vector u = StringUtils.parseData(value_lc, ' ');
						Vector u2 = new Vector();
						for (int k=0; k<u.size(); k++) {
							String token = (String) u.elementAt(k);
							if (token.length() > 0) {
								String firstCh = "" + token.charAt(0);
								firstCh = firstCh.toUpperCase();
								String s = firstCh + token.substring(1, token.length());
								u2.add(s);
							}
						}
						String t = Utils.vector2Delimited(u2, " ");
						String pair = key + "|" + t;
						if (!hset.contains(pair)) {
							hset.add(pair);
						}
					}
				}
			}
		}
		Vector w1 = Utils.hashSet2Vector(hset);
		w1 = new SortUtils().quickSort(w1);
		return w1;
	}

	public static String dumpAcronyms(HashMap hmap) {
		Vector w = sortAcronyms(hmap);
		Utils.saveToFile("NCIt_acronyms.txt", w);
		return "NCIt_acronyms.txt";
	}

	public static String run() {
		return run(DEFAULT_MAX_ACRONYM_LENGTH);
	}

	public static String run(int max_acronym_length) {
		if (max_acronym_length < 1) {
			System.out.println("max_acronym_length cannot be less than 2.");
			System.exit(0);
		}

		set_MAX_ACRONYM_LENGTH(max_acronym_length);
		long ms = System.currentTimeMillis();
		String serviceUrl = ConfigurationController.serviceUrl;
		String named_graph = ConfigurationController.namedGraph;
		String username = ConfigurationController.username;
		String password = ConfigurationController.password;
		System.out.println("serviceUrl: " + serviceUrl);
		System.out.println("named_graph: " + named_graph);
		String datafile = ACRONYM_DATA;
		File f = new File(datafile);
		if (!f.exists()) {
			System.out.println(datafile + " does not exist.");
			generateAcronymData();
			System.out.println(datafile + " generated.");
		}

		int colNum = 0;
		char delim = '\t';
		SPARQLSearchUtils.run(serviceUrl, named_graph, username, password, datafile, colNum, delim);
		HashMap hmap = findAcronyms(Utils.readFile("results_" + ACRONYM_DATA));
		String outputfile = dumpAcronyms(hmap);
		return outputfile;
	}

	public static void main(String[] args) {
		long ms = System.currentTimeMillis();
		String outputfile = run();
		System.out.println(outputfile + " generated.");
		long ms1 = System.currentTimeMillis();
		long timeElapsed = ms1 - ms;
		System.out.println("Total run time " + ms1 + " (milli-seconds).");
	}
}
