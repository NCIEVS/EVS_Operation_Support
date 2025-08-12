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


public class MatchResultFilter {
	static String AXIOM_FILE_NAME = "axiom_ThesaurusInferred_forTS.txt";
	static String HIER_FILE_NAME = "parent_child.txt";
	static String AXIOM_FILE = null;
	static String NCIT_OWL = null;
	static HashMap NCIPTMap = null;
	static HashMap NCISYMap = null;
    static HashSet branch = null;
    static HashMap code2FULLSYNMap = null;
    static HashSet retiredConcepts = new HashSet();
    static String CONCEPT_STATUS_FILE = "P310.txt";
    static String branchfile = null;

	static {
		AXIOM_FILE = ConfigurationController.reportGenerationDirectory + File.separator + AXIOM_FILE_NAME;
        NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile;

		System.out.println(AXIOM_FILE);
		File file = new File(AXIOM_FILE);
		if (!file.exists()) {
			System.out.println("WARNING: " + AXIOM_FILE + " does not exist.");
			System.exit(0);
		} else {
			System.out.println("INFO: " + AXIOM_FILE + " exists.");
		}

		NCIPTMap = createNCIPTMap();
		System.out.println("NCIPTMap: " + NCIPTMap.keySet().size());

		NCISYMap = createNCISYMap();
		System.out.println("NCISYMap: " + NCISYMap.keySet().size());

		code2FULLSYNMap = createCode2FULLSYNMap();
		System.out.println("code2FULLSYNMap: " + code2FULLSYNMap.keySet().size());

		retiredConcepts = createRetiredConceptSet();
		System.out.println("retiredConcepts: " + retiredConcepts.size());

        branch = new HashSet();
        Vector codes = null;

		branchfile = ConfigurationController.branchfile;
		System.out.println("branchfile: " + branchfile);
		if (branchfile != null && branchfile.compareTo("null") != 0) {
			file = new File(branchfile);
			if (!file.exists()) {
				System.out.println(branchfile + " does not exists.");
				String HIER_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile;
				Vector v = Utils.readFile(HIER_FILE);
				HierarchyHelper hh = new HierarchyHelper(v);
				int n = branchfile.indexOf(".");
				String root = branchfile.substring(0, n);//"C62634";
				codes = extractBranchCodes(root);
				Utils.saveToFile(branchfile, codes);
			} else {
				System.out.println(branchfile + " exists.");
				codes = Utils.readFile(branchfile);
			}
			branch = Utils.vector2HashSet(codes);
		}
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

    public static HashMap createNCIPTMap() {
		return createSourcePTMap("NCI");
	}

    public static HashMap createNCISYMap() {
		return createSourceSYMap("NCI");
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

	public static String run(String resultfile) {
		String outputfile = "filtered_" + resultfile;
		boolean generateXLS = true;
        return run(resultfile, outputfile, generateXLS);
	}

	public static String run(String datafile, String outputfile, boolean generateXLS) {
		long ms = System.currentTimeMillis();
		Vector no_matches = new Vector();
		Vector matches = new Vector();
        Vector v = Utils.readFile(datafile);
        Vector w = new Vector();
        String header = (String) v.elementAt(0);
        w.add(header);
        matches.add(header);
        Vector u0 = StringUtils.parseData(header, '\t');
        int m = -1;
        StringBuffer b = new StringBuffer();
		for (int i=0; i<u0.size(); i++) {
			String s = (String) u0.elementAt(i);
			b.append(s).append("\t");
			if (s.compareTo("Matched NCIt Code(s)") == 0) {
				m = i;
				break;
			}
		}
		String s1 = b.toString();
		s1 = s1.substring(0, s1.length()-1);
		no_matches.add(s1);
		for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			line = line.trim();
			String line0 = line;
			if (line.length() > 0) {
				Vector u = StringUtils.parseData(line, '\t');
				StringBuffer buf = new StringBuffer();
				for (int k=0; k<m; k++) {
					buf.append((String) u.elementAt(k));
					if (k < m-1) {
						buf.append("\t");
					}
				}
				line = buf.toString();
				String codeStr = (String) u.elementAt(m);
				Vector codes_0 = StringUtils.parseData(codeStr, '|');
				Vector codes = new Vector();
				if (codeStr.indexOf("No match") == -1) {
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
				} else {
					codes_0 = new Vector();
				}

				boolean matched = false;
				if (codes.size() > 0) {
					String matchData = getMatchedData(codes);
					w.add(line + "\t" + matchData);
					matches.add(line + "\t" + matchData);
					matched = true;
					System.out.println(line0);
					System.out.println(line + "\t" + matchData);
				} else {
					no_matches.add(line);
					w.add(line + "\tNo match");
					System.out.println(line0);
					System.out.println(line + "\tNo match");
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

    public static void main(String[] args) {
		String resultfile = args[0];
	    String outputfile = run(resultfile);
	}
}
