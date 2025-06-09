package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;

import java.io.*;
import java.text.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;
import java.nio.charset.Charset;

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


public class ContainsSearchByTerm {
	static String AXIOM_FILE_NAME = "axiom_ThesaurusInferred_forTS.txt";
	static String AXIOM_FILE = null;
	static HashMap term2CodesMap = null;
	static HashMap NCIPTMap = null;
	static HashMap NCISYMap = null;
	static HashMap sourcePTMap = null;
	static HashMap sourceCode2NCItCodesMap = null;
	static HashMap code2FULLSYNMap = null;

    static boolean caseSensitive = false;
    static HashSet retiredConcepts = new HashSet();
    static String CONCEPT_STATUS_FILE = "P310.txt";
    static String sparqlRunnerDirectory = null;
    static HashSet branch = null;

    static String SYNONYM_FILE = "synonym.txt";
    static Vector SYNONYM_VEC = new Vector();
    static HashMap SYNONYMOUS_PHRASE_MAP = null;
	static HashMap SYNONYM_MAP = null;
	static HashMap SYNONYM_MAP2 = null;


	static {
		AXIOM_FILE = ConfigurationController.reportGenerationDirectory + File.separator + AXIOM_FILE_NAME;
		System.out.println(AXIOM_FILE);
		File file = new File(AXIOM_FILE);
		if (!file.exists()) {
			System.out.println("WARNING: " + AXIOM_FILE + " does not exist.");
			System.exit(0);
		} else {
			System.out.println("INFO: " + AXIOM_FILE + " exists.");
		}
		retiredConcepts = createRetiredConceptSet();
		System.out.println("retired concepts: " + retiredConcepts.size());
		term2CodesMap = createterm2CodesMap();
		NCIPTMap = createNCIPTMap();
		NCISYMap = createNCISYMap();
		code2FULLSYNMap = createCode2FULLSYNMap();

		String branchfile = ConfigurationController.branchfile;
        branch = new HashSet();
        Vector codes = null;
        if (branchfile != null && branchfile.compareTo("null") != 0) {
			file = new File(branchfile);
			if (!file.exists()) {
				System.out.println(branchfile + " does not exists.");
				String branchRoots = ConfigurationController.branchroots;
				System.out.println("branchRoots: "+ branchRoots);
				constructBranchFile(branchRoots, branchfile);
			}
			codes = Utils.readFile(branchfile);
			branch = Utils.vector2HashSet(codes);
			System.out.println("branch size: " + branch.size());
		} else {
			System.out.println("No restriction on branches - search will be performed against the entire NCI Thesaurus.");
		}

        String wd1 = null;
        String wd2 = null;
		SYNONYM_MAP = new HashMap();
		SYNONYM_MAP2 = new HashMap();
		SYNONYMOUS_PHRASE_MAP = new HashMap();
        Vector w = new Vector();
		for (int i=0; i<SYNONYM_VEC.size(); i++) {
			String wd_pair = (String) SYNONYM_VEC.elementAt(i);
			wd_pair = wd_pair.toLowerCase();
			Vector u = StringUtils.parseData(wd_pair, '|');
			wd1 = (String) u.elementAt(0);
			wd2 = (String) u.elementAt(1);

			if (wd1.indexOf(" ") != -1 || wd2.indexOf(" ") != -1) {
				w = new Vector();
				if (SYNONYMOUS_PHRASE_MAP.containsKey(wd1)) {
					w = (Vector) SYNONYMOUS_PHRASE_MAP.get(wd1);
				}
				if (!w.contains(wd2)) {
					w.add(wd2);
				}
				SYNONYMOUS_PHRASE_MAP.put(wd1, w);

				w = new Vector();
				if (SYNONYMOUS_PHRASE_MAP.containsKey(wd2)) {
					w = (Vector) SYNONYMOUS_PHRASE_MAP.get(wd2);
				}
				if (!w.contains(wd1)) {
					w.add(wd1);
				}
				SYNONYMOUS_PHRASE_MAP.put(wd2, w);

			} else {

				wd1 = gov.nih.nci.evs.restapi.util.Normalizer.stemTerm(wd1);
				wd2 = gov.nih.nci.evs.restapi.util.Normalizer.stemTerm(wd2);
				w = new Vector();
				if (SYNONYM_MAP.containsKey(wd1)) {
					w = (Vector) SYNONYM_MAP.get(wd1);
				}
				if (!w.contains(wd2)) {
					w.add(wd2);
				}
				SYNONYM_MAP.put(wd1, w);

				w = new Vector();
				if (SYNONYM_MAP.containsKey(wd2)) {
					w = (Vector) SYNONYM_MAP.get(wd2);
				}
				if (!w.contains(wd1)) {
					w.add(wd1);
				}
				SYNONYM_MAP.put(wd2, w);

				///////////////////////////////////////////////////////////
				wd1 = (String) u.elementAt(0);
				wd2 = (String) u.elementAt(1);
				w = new Vector();
				if (SYNONYM_MAP2.containsKey(wd1)) {
					w = (Vector) SYNONYM_MAP2.get(wd1);
				}
				if (!w.contains(wd2)) {
					w.add(wd2);
				}
				SYNONYM_MAP2.put(wd1, w);

				w = new Vector();
				if (SYNONYM_MAP2.containsKey(wd2)) {
					w = (Vector) SYNONYM_MAP2.get(wd2);
				}
				if (!w.contains(wd1)) {
					w.add(wd1);
				}
				SYNONYM_MAP2.put(wd2, w);
			}
		}

		Utils.dumpMultiValuedHashMap("SYNONYM_MAP", SYNONYM_MAP);
		System.out.println("Initialization of ContainsSearch completed.");

	}

	public static boolean is_retired(String code) {
		return retiredConcepts.contains(code);
	}

	public static void extractProperties(String prop_code) {
		long ms = System.currentTimeMillis();
		String reportGenerationDirectory = ConfigurationController.reportGenerationDirectory;
		String owlfile = reportGenerationDirectory + File.separator + ConfigurationController.owlfile;
		System.out.println(owlfile);
		OWLScanner scanner = new OWLScanner(owlfile);
		Vector w = scanner.extractProperties(scanner.get_owl_vec(), prop_code);
		String outputfile = prop_code + ".txt";
		Utils.saveToFile(outputfile, w);
		System.out.println(outputfile + " generated.");
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}

    public static HashSet createRetiredConceptSet() {
		HashSet hset = new HashSet();
		File file = new File(CONCEPT_STATUS_FILE);
		if (!file.exists()) {
			System.out.println("WARNING: " + CONCEPT_STATUS_FILE + " does not exist.");
			extractProperties("P310");
		}
		Vector v = Utils.readFile(CONCEPT_STATUS_FILE);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String status = (String) u.elementAt(2);
			if (status.compareTo("Retired_Concept") == 0) {
				String code = (String) u.elementAt(0);
				hset.add(code);
			}
		}
		return hset;
	}


    public static HashMap createNCIPTMap() {
		return createSourcePTMap("NCI");
	}

    public static HashMap createNCISYMap() {
		return createSourceSYMap("NCI");
	}

    public static HashMap createHemOncPTMap() {
		return createSourcePTMap("HemOnc");
	}

    public static HashMap createSourcePTMap(String source) {
		HashMap hmap = new HashMap();
		Vector v = Utils.readFile(AXIOM_FILE);
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			if (u.contains("P383$PT") && u.contains("P384$" + source) ) {
				hmap.put((String) u.elementAt(1), (String) u.elementAt(3));
			}
		}
		return hmap;
	}

    public static HashMap createSourceSYMap(String source) {
		HashMap hmap = new HashMap();
		Vector v = Utils.readFile(AXIOM_FILE);
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String type = (String) u.elementAt(2);
			if (type.compareTo("P90") == 0) {
				if (u.contains("P383$SY") && u.contains("P384$" + source) ) {
					w = new Vector();
					String key = (String) u.elementAt(1);
					if (hmap.containsKey(key)) {
						w = (Vector) hmap.get(key);
					}
					String sy = (String) u.elementAt(3);
					if (!w.contains(sy)) {
						w.add(sy);
					}
					hmap.put(key, w);
				}
			}
		}
		return hmap;
	}

    public static HashMap createSourceCode2NCItCodesMap(String source) {
		HashMap sourceCode2NCItCodesMap = new HashMap();
		Vector v = Utils.readFile(AXIOM_FILE);
		String target = "P384$" + source;
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			if (u.contains(target)) {
				if (line.indexOf("P385") != -1) {
					for (int j=0; j<u.size(); j++) {
						String token = (String) u.elementAt(j);
						if (token.startsWith("P385")) {
							Vector u2 = StringUtils.parseData(token, '$');
							String sourceCode = (String) u2.elementAt(1);
							Vector w1 = new Vector();
							if (sourceCode2NCItCodesMap.containsKey(sourceCode)) {
								System.out.println("INFO: multiple sourceCode to NCItCode mapping encountered. " + sourceCode);
								w1 = (Vector) sourceCode2NCItCodesMap.get(sourceCode);
							}
							String ncitCode = (String) u.elementAt(1);
							if (!w1.contains(ncitCode)) {
								w1.add(ncitCode);
							}
							sourceCode2NCItCodesMap.put(sourceCode, w1);
							if (w1.size() > 1) {
								Utils.dumpVector(sourceCode, w1);
							}
						}
					}
				}
			}
		}
		return sourceCode2NCItCodesMap;
	}

    public static HashMap createterm2CodesMap() {
		String filename = AXIOM_FILE;
		HashMap hmap = new HashMap();
		Vector v = Utils.readFile(filename);
        for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(1);
			boolean flag = true;
			if (branch != null && branch.size() > 0) {
				if (!branch.contains(code)) {
					flag = false;
				}
			}
			if (flag) {
				String prop_code = (String) u.elementAt(2);
				if (prop_code.compareTo("P90") == 0) {
					String term = (String) u.elementAt(3);
					String term_lc = term.toLowerCase();
					if (!caseSensitive) {
						term = term_lc;
					}
					Vector w = new Vector();
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
		}
		return hmap;
	}

    public static HashMap createCode2FULLSYNMap() {
		String filename = AXIOM_FILE;
		HashMap hmap = new HashMap();
		Vector v = Utils.readFile(filename);
        for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(1);
			String prop_code = (String) u.elementAt(2);
			if (prop_code.compareTo("P90") == 0) {
				Vector w = new Vector();
				if (hmap.containsKey(code)) {
					w = (Vector) hmap.get(code);
				}
				String term = (String) u.elementAt(3);
				if (!w.contains(term)) {
					w.add(term);
				}
				hmap.put(code, w);
			}
		}
		return hmap;
	}

	public static String getMatchedData(Vector codes, boolean ptOnly) {
		StringBuffer buf = new StringBuffer();
		StringBuffer buf_pt = new StringBuffer();
		StringBuffer all_syn_buf = new StringBuffer();
		String all_syn_str = "";
		for (int j=0; j<codes.size(); j++) {
			String code = (String) codes.elementAt(j);
			buf.append(code).append("|");
			buf_pt.append((String) NCIPTMap.get(code)).append("|");

			if (!ptOnly) {
				StringBuffer buf_syns = new StringBuffer();
				Vector fullSyns = (Vector) code2FULLSYNMap.get(code);
				for (int k=0; k<fullSyns.size(); k++) {
					String syn = (String) fullSyns.elementAt(k);
					buf_syns.append(syn).append("$");
				}
				String syn_str = buf_syns.toString();
				if (syn_str.length() > 0) {
					syn_str = syn_str.substring(0, syn_str.length()-1);
				}
				all_syn_buf.append(syn_str).append("|");
			}
		}
		String codes_str = buf.toString();
		String pts_str = buf_pt.toString();
		if (ptOnly) {
			codes_str = codes_str.substring(0, codes_str.length()-1);
			pts_str = pts_str.substring(0, pts_str.length()-1);
			return codes_str + "\t" + pts_str;
		} else {
			if (codes_str.length() > 0) {
				codes_str = codes_str.substring(0, codes_str.length()-1);
				pts_str = pts_str.substring(0, pts_str.length()-1);
			}
			all_syn_str = all_syn_buf.toString();
			if (all_syn_str.length() > 0) {
				all_syn_str = all_syn_str.substring(0, all_syn_str.length()-1);
			}
		}
		return codes_str + "\t" + pts_str + "\t" + all_syn_str;
	}

	public static String getMatchedData(Vector codes_0) {
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
        String pts = null;
		if (codes != null && codes.size() > 0) {
			for (int i=0; i<codes.size(); i++) {
				String code = (String) codes.elementAt(i);
				if (NCIPTMap.containsKey(code)) {
					String pt = (String) NCIPTMap.get(code);
					pt_buf.append(pt).append("|");
				}
			}
			pts = pt_buf.toString();
			pts = pts.substring(0, pts.length()-1);
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

	public static String run(String datafile, String outputfile) {
		return run(datafile, outputfile, 0);
	}

	public static Vector containsSearch(Vector tokens, boolean applyStem) {
		Vector w = new Vector();
		boolean matched = false;
		Iterator it = term2CodesMap.keySet().iterator();
		while (it.hasNext()) {
			String term = (String) it.next();
			Vector words = gov.nih.nci.evs.restapi.util.Normalizer.tokenize(term, applyStem);
			boolean contains = true;
			for (int i=0; i<tokens.size(); i++) {
				String token = (String) tokens.elementAt(i);
				token = token.toLowerCase();
				if (!words.contains(token)) {
					contains = false;
					break;
				}
			}
			if (contains) {
				Vector v = (Vector) term2CodesMap.get(term);
				w.addAll(v);
			}
		}
        return removeDuplicates(w);
	}

	public static Vector containsSearch(String vbt, boolean applyStem) {
		Vector tokens = gov.nih.nci.evs.restapi.util.Normalizer.tokenize(vbt, applyStem);
		return containsSearch(tokens, applyStem);
	}

	public static Vector containsSearch(String vbt) {
		vbt = vbt.toLowerCase();
		Vector w = new Vector();
		boolean matched = false;
		boolean applyStem = false;
		Iterator it = term2CodesMap.keySet().iterator();
		while (it.hasNext()) {
			String term = (String) it.next();
			Vector words = gov.nih.nci.evs.restapi.util.Normalizer.tokenize(term, applyStem); //tokenize(term);
			if (words.contains(vbt)) {
			//if (term.indexOf(vbt) != -1) {
				Vector v = (Vector) term2CodesMap.get(term);
				w.addAll(v);
			}
		}
        return removeDuplicates(w);
	}

	public static Vector removeDuplicates(Vector v) {
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String t = (String) v.elementAt(i);
			if (!w.contains(t)) {
				w.add(t);
			}
		}
		return w;

	}



	public static void constructBranchFile(String branchRoots, String outputfile) {
		System.out.println("branchRoots: " + branchRoots);
		String HIER_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile;
		Vector v = Utils.readFile(HIER_FILE);
		HierarchyHelper hh = new HierarchyHelper(v);
		Vector roots = StringUtils.parseData(branchRoots, '|');
		Utils.dumpVector("branchRoots", roots);
		Vector w = new Vector();
		for (int i=0; i<roots.size(); i++) {
			String root = (String) roots.elementAt(i);
			System.out.println(root);
			Vector codes = hh.get_transitive_closure_v3(root);
			w.addAll(codes);
		}
		Utils.saveToFile(outputfile, w);
		System.out.println(outputfile + " generated.");
	}

	public static String run(String datafile, String outputfile, int colIndex) {
		int col = colIndex;
		long ms = System.currentTimeMillis();
		Vector no_matches = new Vector();
		Vector matches = new Vector();
        Vector v = Utils.readFile(datafile);
        Vector w = new Vector();
        String header = (String) v.elementAt(0);
        header = header.replace("|", "\t");

        w.add(header + "\tMatched NCIt Code(s)\tNCI PT(s)\tNCI SY(s)");
        no_matches.add(header);
        matches.add(header + "\tMatched NCIt Code(s)\tNCI PT(s)\tNCI SY(s)");
		for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			//System.out.println("(" + i + ") " + line);
			line = line.trim();
			line = line.replace("|", "\t");
			if (line.length() > 0) {
				Vector u = StringUtils.parseData(line, '\t');
				String term = (String) u.elementAt(col);
				String term_lc = term.toLowerCase();
				if (!caseSensitive) {
					term = term_lc;
				}
				boolean matched = false;
				Vector tokens = gov.nih.nci.evs.restapi.util.Normalizer.tokenize(term, false);
				Vector codes_0 = containsSearch(tokens, false);
				if (codes_0 != null && codes_0.size() > 0) {
					Vector codes = new Vector();
					for (int j=0; j<codes_0.size(); j++) {
						String code = (String) codes_0.elementAt(j);
						if (branch.size() > 0) {
							if (branch.contains(code) && !is_retired(code)) {
								codes.add(code);
							}
						} else {
							if (!is_retired(code)) {
								codes.add(code);
							}
						}
					}
					if (codes.size() > 0) {
						String matchData = getMatchedData(codes);
						w.add(line + "\t" + matchData);
						matches.add(line + "\t" + matchData);
						matched = true;
					} else {
						no_matches.add(line);
						w.add(line + "\tNo match");
					}

				} else {
					no_matches.add(line);
					w.add(line + "\tNo match");
				}
			}
		}
		Utils.saveToFile(outputfile, w);
		Utils.saveToFile("no_matches_" + outputfile, no_matches);
		Utils.saveToFile("matches_" + outputfile, matches);
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
		System.out.println(outputfile + " generated.");
		return outputfile;
	}

	public static void loadSynonyms() {
		boolean exists = Utils.checkIfFileExists(SYNONYM_FILE);
		if (!exists) {
			System.out.println("WARNING: file " + SYNONYM_FILE + " does not exists.");
		}
        SYNONYM_VEC = Utils.readFile(SYNONYM_FILE);
	}

    public static String addSynonymousPhrases(String def) {
		def = def.toLowerCase();
		Iterator it = SYNONYMOUS_PHRASE_MAP.keySet().iterator();
		while (it.hasNext()) {
			String phrase = (String) it.next();
			if (def.indexOf(phrase) != -1) {
				Vector v = (Vector) SYNONYMOUS_PHRASE_MAP.get(phrase);
				for (int j=0; j<v.size(); j++) {
					String wd2 = (String) v.elementAt(j);
				    def = def + " " + wd2;
				}
			}
		}
		return def;
	}

	public static String encode(String t) {
		return HTMLDecoder.decode(t);
	}

	public static Vector encode(Vector v) {
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String t = (String) v.elementAt(i);
			String t1 = encode(t);
			if (t1.compareTo(t) != 0) {
				System.out.println("\nReplace " + t);
				System.out.println("by      "  + t1);
			}
			w.add(t1);
		}
		return w;
	}

	public static void encodeFile(String filename) {
		Vector v = Utils.readFile(filename);
		v = encode(v);
		Utils.saveToFile(filename, v);
	}

	public static void main(String[] args) {
		long ms = System.currentTimeMillis();
		String datafile = args[0];
		int n = datafile.lastIndexOf(".");
		String outputfile = "contains_iter1_" + datafile.substring(0, n) + "_" + StringUtils.getToday() + ".txt";
		String resultfile = ContainsSearchByTerm.run(datafile, outputfile, 0);
		encodeFile(resultfile);

		//outputfile = "iter2_" + datafile;
		//resultfile = ContainsSearchByTerm.run2(resultfile, outputfile);
		//outputfile = "iter3_" + datafile;
		//String outputfile = LexicalMatchRunner.run(resultfile, outputfile);
		//System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}

}

