package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;
import java.io.*;
import java.text.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;
import java.nio.charset.Charset;
import org.apache.poi.ss.usermodel.IndexedColors;

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


public class ExactMatchByTerm {
	static String AXIOM_FILE_NAME = "axiom_ThesaurusInferred_forTS.txt";
	static String HIER_FILE_NAME = "parent_child.txt";
	static String AXIOM_FILE = null;
	static String NCIT_OWL = null;
	static String PROPERTY_FILE = null;

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
    static HashMap sourceCode2LineMap = null;
    static String SYNONYM_EXT = null;
    static String termfile = null;

	static {
		AXIOM_FILE = ConfigurationController.reportGenerationDirectory + File.separator + AXIOM_FILE_NAME;
		PROPERTY_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.propertyfile;
        NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile;

		System.out.println(AXIOM_FILE);
		File file = new File(AXIOM_FILE);
		if (!file.exists()) {
			System.out.println("WARNING: " + AXIOM_FILE + " does not exist.");
			System.exit(0);
		} else {
			System.out.println("INFO: " + AXIOM_FILE + " exists.");
		}

		sourceCode2LineMap = createSourceCode2LineMap();

		retiredConcepts = createRetiredConceptSet();
		System.out.println("retired concepts: " + retiredConcepts.size());

		termfile = ConfigurationController.termfile;
		file = new File(termfile);
		if (!file.exists()) {
			System.out.println("INFO: term file does not exists, use AXIOM_FILE as default");
			termfile = AXIOM_FILE;
		} else {
			System.out.println("INFO: " + termfile + " exists.");
			System.out.println(termfile);
		}
		term2CodesMap = createTerm2CodesMap(termfile);

		SYNONYM_EXT = ConfigurationController.synonymExt;
		if (SYNONYM_EXT != null && SYNONYM_EXT.length() > 0) {
			file = new File(SYNONYM_EXT);
			if (file.exists()) {
				expandTerm2CodesMap(SYNONYM_EXT);
			}
		}

		NCIPTMap = createNCIPTMap();
		System.out.println("NCIPTMap: " + NCIPTMap.keySet().size());

		NCISYMap = createNCISYMap();
		System.out.println("NCISYMap: " + NCISYMap.keySet().size());

		code2FULLSYNMap = createCode2FULLSYNMap();
		System.out.println("code2FULLSYNMap: " + code2FULLSYNMap.keySet().size());

        branch = new HashSet();
        Vector codes = null;
	}

	public static Vector extractBranchCodes(String root) {
		String HIER_FILE = ConfigurationController.reportGenerationDirectory + File.separator + HIER_FILE_NAME;
		System.out.println(HIER_FILE);
		File file = new File(HIER_FILE);
		if (!file.exists()) {
			System.out.println("WARNING: " + HIER_FILE + " does not exist.");
			System.exit(0);
		} else {
			System.out.println("INFO: " + HIER_FILE + " exists.");
		}
		Vector v = Utils.readFile(HIER_FILE);
		HierarchyHelper hh = new HierarchyHelper(v);
		Vector w = hh.get_transitive_closure_v3(root);
		return w;
	}

	public static HashMap createSourceCode2LineMap() {
		String AXIOM_FILE = ConfigurationController.reportGenerationDirectory + File.separator + AXIOM_FILE_NAME;
		System.out.println(AXIOM_FILE);
		File file = new File(AXIOM_FILE);
		if (!file.exists()) {
			System.out.println("WARNING: " + AXIOM_FILE + " does not exist.");
			System.exit(0);
		} else {
			System.out.println("INFO: " + AXIOM_FILE + " exists.");
		}
		Vector v = Utils.readFile(AXIOM_FILE);
		HashMap hmap = new HashMap();
		//Recombinant Amphiregulin|C1000|P90|AMPHIREGULIN|P383$PT|P384$FDA|P385$7MGE0HPM2H|P386$UNII
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			for (int j=0; j<u.size(); j++) {
				String t = (String) u.elementAt(j);
				if (t.startsWith("P385$")) {
					line = HTMLDecoder.decode(line);
					hmap.put(t, line);
				}
			}
		}
        return hmap;
	}


	public static boolean is_retired(String code) {
		return retiredConcepts.contains(code);
	}

	public static Vector getMatchedCodes(String term) {
		if (!caseSensitive) {
			term = term.toLowerCase();
		}
		if (!term2CodesMap.containsKey(term)) return null;
		return (Vector) term2CodesMap.get(term);
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
		extractProperties("P310");
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

    //Recombinant Amphiregulin|C1000|P90|AR|P383$AB|P384$NCI
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
					sy = HTMLDecoder.decode(sy);
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

//Case sensitive match
    public static HashMap createTerm2CodesMap() {
		return createTerm2CodesMap(null);
	}

    public static HashMap createTerm2CodesMap(String filename) {
		HashMap hmap = new HashMap();
		if (filename == null) {
			filename = AXIOM_FILE;
		}
		Vector v = Utils.readFile(filename);
        for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String prop_code = (String) u.elementAt(2);
			if (prop_code.compareTo("P90") == 0) {
				String term = (String) u.elementAt(3);
				term = HTMLDecoder.decode(term);
				String term_lc = term.toLowerCase();
				if (!caseSensitive) {
					term = term_lc;
				}

				Vector w = new Vector();
				if (hmap.containsKey(term)) {
					w = (Vector) hmap.get(term);
				}
				String code = (String) u.elementAt(1);
				if (!w.contains(code)) {
					w.add(code);
				}
				hmap.put(term, w);
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
				term = HTMLDecoder.decode(term);
				if (!w.contains(term)) {
					w.add(term);
				}
				hmap.put(code, w);
			}
		}
		return hmap;
	}

	public static String getMatchedData(String code) {
		Vector codes = new Vector();
		codes.add(code);
		return getMatchedData(codes);
	}

	public static String getMatchedData(String code, boolean ptOnly) {
		Vector codes = new Vector();
		codes.add(code);
		return getMatchedData(codes, ptOnly);
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

	public static String run(String datafile, String outputfile) {
		return run(datafile, outputfile, 0);
	}

	public static String run(String datafile, String outputfile, int colIndex) {
		boolean generateXLS = false;
		return run(datafile, outputfile, colIndex, generateXLS);
	}

	public static String run(String datafile, String outputfile, int colIndex, boolean generateXLS) {
		return run(new HashSet(), datafile, outputfile, colIndex, generateXLS);
	}

	public static String run(HashSet branch, String datafile, String outputfile, int colIndex, boolean generateXLS) {
		int col = colIndex;
		long ms = System.currentTimeMillis();
		Vector no_matches = new Vector();
		Vector matches = new Vector();
        Vector v = Utils.readFile(datafile);
        Vector w = new Vector();
        String header = (String) v.elementAt(0);
        w.add(header + "\tMatched NCIt Code(s)\tNCI PT(s)\tNCI SY(s)");

        no_matches.add(header);
        matches.add(header + "\tMatched NCIt Code(s)\tNCI PT(s)\tNCI SY(s)");
		for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			line = line.trim();
			if (line.length() > 0) {
				Vector u = StringUtils.parseData(line, '\t');
				String term = (String) u.elementAt(col);
				String term_lc = term.toLowerCase();
				if (!caseSensitive) {
					term = term_lc;
				}
				boolean matched = false;
				if (term2CodesMap.containsKey(term)) {
					Vector codes_0 = (Vector) term2CodesMap.get(term);
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

        if (generateXLS) {
			String textfile = outputfile;
			int n = textfile.lastIndexOf(".");
			String sheetName = textfile.substring(0, n);
			String xlsfile = null;
			char delim = '\t';
			try {
				xlsfile = ExcelReadWriteUtils.writeXLSFile(textfile, delim, sheetName);
				short firstRowColor = IndexedColors.LIGHT_GREEN.getIndex();
				ExcelFormatter.reformat(xlsfile, firstRowColor);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		return outputfile;
	}


//////////////////////////////////////////////////////////////////////////////////////////////
	public static String matchByCode(String datafile, String outputfile) {
		return run(datafile, outputfile, 0);
	}

	public static void expandTerm2CodesMap(String filename) {
		Vector v = Utils.readFile(filename);
        for (int i=0; i<v.size(); i++) {
			String t = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(t, '|');
			String code = (String) u.elementAt(1);
			String term = (String) u.elementAt(0);
			term = term.toLowerCase();
			Vector w = new Vector();
			if (term2CodesMap.containsKey(term)) {
				w =	(Vector) term2CodesMap.get(term);
			}
			if (!w.contains(code)) {
				w.add(code);
			}
			term2CodesMap.put(term, w);
		}
	}

	public static String matchByCode(String datafile, String outputfile, int colIndex) {
		int col = colIndex;
		long ms = System.currentTimeMillis();
		Vector no_matches = new Vector();
		Vector matches = new Vector();
        Vector v = Utils.readFile(datafile);
        Vector w = new Vector();
        String header = (String) v.elementAt(0);
        Vector header_u = StringUtils.parseData(header, '\t');
        String hdr = (String) header_u.elementAt(0) + "\t" + (String) header_u.elementAt(col);
        w.add(hdr + "\tMatched NCIt Code(s)\tNCI PT(s)\tNCI SY(s)");
        no_matches.add(hdr);
        matches.add(hdr + "\tMatched NCIt Code(s)\tNCI PT(s)\tNCI SY(s)");
        int knt = 0;
		for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			line = line.trim();
			if (line.length() > 0) {
				Vector u = StringUtils.parseData(line, '\t');
				boolean matched = false;
				String coding_scheme = (String) u.elementAt(0);
				String source_code = (String) u.elementAt(col);
				String key = "P385$" + source_code;
				Vector codes = new Vector();
				if (sourceCode2LineMap.containsKey(key)) {
					knt++;
					String value = (String) sourceCode2LineMap.get(key);
					Vector value_u = StringUtils.parseData(value, '|');
					String code = (String) value_u.elementAt(1);
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
					w.add(coding_scheme + "\t" + source_code + "\t" + matchData);
					matches.add(coding_scheme + "\t" + source_code + "\t" + matchData);
					matched = true;
				} else {
					no_matches.add(coding_scheme + "\t" + source_code);
					w.add(coding_scheme + "\t" + source_code + "\tNo match");
				}
			}
		}
		Utils.saveToFile(outputfile, w);
		Utils.saveToFile("no_matches_" + outputfile, no_matches);
		Utils.saveToFile("matches_" + outputfile, matches);
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
		System.out.println(outputfile + " generated.");

		System.out.println("matched codes: " + knt);
		return outputfile;
	}

	public static String matchByProperty(String datafile, int col, String propertyCode) { // P319 FDA_UNII_Code
		//C1000|P319|7MGE0HPM2H
		long ms = System.currentTimeMillis();
		int knt = 0;
		boolean matched = false;
		Vector no_matches = new Vector();
		Vector matches = new Vector();
		Vector w0 = new Vector();
		Vector v = Utils.readFile(PROPERTY_FILE);
		HashMap hmap = new HashMap();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(0);
			String propCode = (String) u.elementAt(1);
			String value = (String) u.elementAt(2);
			if (propCode.compareTo(propertyCode) == 0) {
				Vector w = new Vector();
				if (hmap.containsKey(value)) {
					w = (Vector) hmap.get(value);
				}
				if (!w.contains(code)) {
					w.add(code);
				}
				hmap.put(value, w);
			}
		}

		v = Utils.readFile(datafile);
        String header = (String) v.elementAt(0);
        Vector header_u = StringUtils.parseData(header, '\t');
        String hdr = (String) header_u.elementAt(0);
        w0.add(hdr + "\tMatched NCIt Code(s)\tNCI PT(s)\tNCI SY(s)");
		for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String value = (String) u.elementAt(col);
			if (hmap.containsKey(value)) {
				Vector w = (Vector) hmap.get(value);
				String matchData = getMatchedData(w);
				w0.add(line + "\t" + Utils.vector2Delimited(w, "|") + "\t" + matchData);
				matches.add(line + "\t" + Utils.vector2Delimited(w, "|") + "\t" + matchData);
				matched = true;
				knt++;
			} else {
				no_matches.add(line);
				w0.add(line + "\tNo match");
			}
		}
		String outputfile = "results_" + datafile;
		Utils.saveToFile(outputfile, w0);
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
		System.out.println(outputfile + " generated.");
		System.out.println("matched property values: " + knt);
		return outputfile;
	}


	public static Vector extractRowsFromAxiomFile(Vector req_vec) {
//Recombinant Amphiregulin|C1000|P90|Recombinant Amphiregulin|P383$PT|P384$NCI
		Vector w = new Vector();
		Vector v = Utils.readFile(AXIOM_FILE);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			boolean match = true;
			for (int j=0; j<u.size(); j++) {
				for (int k=0; k<req_vec.size(); k++) {
					String req = (String) req_vec.elementAt(k);
					if (!u.contains(req)) {
						match = false;
						break;
					}
				}
			}
			if (match) {
				w.add(line);
			}
		}
		return new SortUtils().quickSort(w);
	}

	public static void setupAndRunExactMatch(String mappingTarget, String datafile, int col) {
		Vector dataVec = new Vector();
		dataVec.add("P90|P384$NCI|P383$PT");
		dataVec.add("P90|P384$NCI|P383$SY");
		DataRetrieval test = new DataRetrieval(NCIT_OWL, dataVec);
		HashMap ptMap = test.createCode2ValuesMap("P90|P384$NCI|P383$PT");
		HashMap syMap = test.createCode2ValuesMap("P90|P384$NCI|P383$SY");
		HashMap term2CodesMap = null;
		if (mappingTarget == null) {
			term2CodesMap = DataRetrieval.createTerm2CodesMap(AXIOM_FILE);
		} else {
			String reqs = mappingTarget;//"P90|P383$PT|P384$NCI";
			Vector w = DataRetrieval.extractRowsFromAxiomFile(reqs);
			term2CodesMap = DataRetrieval.createTerm2CodesMap(w);
		}
		Vector w = new Vector();
		Vector v = Utils.readFile(datafile);
		String line = (String) v.elementAt(0);
		Vector u = StringUtils.parseData(line, '\t');
		String heading = (String) u.elementAt(col);
		w.add(heading + "\t" + "Matched NCIt Code(s)	NCI PT(s)	NCI SY(s)");
		for (int i=1; i<v.size(); i++) {
			line = (String) v.elementAt(i);
			u = StringUtils.parseData(line, '|');
			String term = (String) u.elementAt(col);
			term = term.toLowerCase();
			if (term2CodesMap.containsKey(term)) {
				Vector codes = (Vector) term2CodesMap.get(term);
				String codeStr = Utils.vector2Delimited(codes, "|");
				StringBuffer ptBuf = new StringBuffer();
				StringBuffer syBuf = new StringBuffer();
				for (int j=0; j<codes.size(); j++) {
					String code = (String) codes.elementAt(j);
					if (ptMap.containsKey(code)) {
						ptBuf.append(Utils.vector2Delimited((Vector) ptMap.get(code), "|")).append("$");
					}
					if (syMap.containsKey(code)) {
						syBuf.append(Utils.vector2Delimited((Vector) syMap.get(code), "|")).append("$");
					}
				}
				String s1 = ptBuf.toString();
				if (s1.length() > 0) {
					s1 = s1.substring(0, s1.length()-1);
				}
				String s2 = syBuf.toString();
				if (s2.length() > 0) {
					s2 = s2.substring(0, s2.length()-1);
				}
				w.add(term + "\t" + codeStr + "\t" + s1 + "\t" + s2);
			} else {
				w.add(term + "\tNo match");
			}
		}
        Utils.saveToFile("results_" + datafile, w);
	}
}
