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


public class MappingRunner {
	static String HEADING = "Term	Matched NCIt Code(s)	NCI PT(s)	NCI SY(s)";
    static String NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile;
	static String AXIOM_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.axiomfile;
	static String PARENT_CHILD_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile;

	static String requiredDataFile = ConfigurationController.requiredDataFile;
	public static HashMap ncitPTMap = null;
	public static HashMap ncitSYMap = null;

	static {
        Vector required_data = Utils.readFile(requiredDataFile);
 		DataRetrieval dataRetrieval = new DataRetrieval(NCIT_OWL, required_data);
        ncitPTMap = dataRetrieval.createCode2ValuesMap("P90|P384$NCI|P383$PT");
        ncitSYMap = dataRetrieval.createCode2ValuesMap("P90|P384$NCI|P383$SY");
	}

	public static HashMap createCode2LabelMap() {
		Vector v = Utils.readFile(PARENT_CHILD_FILE);
		HashMap hmap = new HashMap();
        for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			hmap.put((String) u.elementAt(1), HTMLDecoder.decode((String) u.elementAt(0)));
            hmap.put((String) u.elementAt(3), HTMLDecoder.decode((String) u.elementAt(2)));
		}
		return hmap;
	}

	public static String vector2Delimited(Vector v, char delim) {
		if (v == null || v.size() == 0) return "";
		v = new SortUtils().quickSort(v);
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<v.size(); i++) {
			String t = (String) v.elementAt(i);
			buf.append(t);
			if (i < v.size()-1) {
				buf.append(delim);
			}
		}
		return buf.toString();
	}

	public static String STARTSWITH = "StartWith";
	public static String CONTAINS = "Contains";

	public static Vector extractRowsFromAxiomFile(String pattern, String algorithm) {
		pattern = pattern.toLowerCase();
		Vector w = new Vector();
		Vector v = Utils.readFile(AXIOM_FILE);
		HashMap hmap = new HashMap();
        for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String t0 = (String) u.elementAt(0);
			t0 = t0.toLowerCase();
			if (algorithm.compareTo(STARTSWITH) == 0) {
				if (t0.startsWith(pattern)) {
					w.add(line);
				}
			} else if (algorithm.compareTo(CONTAINS) == 0) {
				if (t0.indexOf(pattern) != -1) {
					w.add(line);
				}
			}
		}
		return w;
	}

	public static Vector extractRowsFromAxiomFile(Vector req_vec) {
		return Utils.extractRowsFromFile(AXIOM_FILE, req_vec);
	}

	public static void generateMappingTarget(Vector req_vec) {
	    Vector w = extractRowsFromAxiomFile(req_vec);
	    Utils.saveToFile(ConfigurationController.axiomfile, w);
	}

	public static String runExactMatch(String datafile, int col) {
        return runExactMatch(datafile, null, col);
	}

	public static String runExactMatch(String datafile, Vector req_vec, int col) {
		if (req_vec != null) {
			generateMappingTarget(req_vec);
		}
		String outputfile = "results_" + datafile;
		Vector v = Utils.readFile(datafile);
		String heading = (String) v.elementAt(0);
		Vector u = StringUtils.parseData(heading, '\t');
		heading = heading + "\t" + "Matched NCIt Code(s)\tNCI PT(s)\tNCI SY(s)";
		Vector w = new Vector();
		w.add(heading);
		for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			u = StringUtils.parseData(line, '\t');
			String term = (String) u.elementAt(col);
			term = term.toLowerCase();
			Vector codes = ExactMatchByTerm.getMatchedCodes(term);
			if (codes == null || codes.size() == 0) {
				w.add(line + "\t" + "No match");
			} else {
				String data = getAppendedData(codes);
				w.add(line + "\t" + vector2Delimited(codes, '|') + "\t" + data);
			}
		}

		outputfile = "results_" + datafile;
		Utils.saveToFile(outputfile, w);
		System.out.println("ExactMatch outputfile: " + outputfile);
		tallyResults(outputfile);
		System.out.println(outputfile + " generated.");
		return outputfile;
	}

	public static String runLexicalMatch(String datafile, int col) {
		String outputfile = "results_" + datafile;
		Vector v = Utils.readFile(datafile);
		String heading = (String) v.elementAt(0);
		Vector u = StringUtils.parseData(heading, '\t');
		heading = heading + "\t" + "Matched NCIt Code(s)\tNCI PT(s)\tNCI SY(s)";
		Vector w = new Vector();
		w.add(heading);
		for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			u = StringUtils.parseData(line, '\t');
			String term = (String) u.elementAt(col);
			term = term.toLowerCase();
			boolean codeOnly = true;
			String codes_str = LexicalMatching.lexicalMatch(term, codeOnly);
			if (codes_str == null || codes_str.length() == 0) {
				w.add(line + "\t" + "No match");
			} else {
				Vector codes = StringUtils.parseData(codes_str, '|');
				String data = getAppendedData(codes);
				w.add(line + "\t" + codes_str + "\t" + data);
			}
		}

		outputfile = "results_" + datafile;
		Utils.saveToFile(outputfile, w);
		System.out.println("LexicalMatching outputfile: " + outputfile);
		tallyResults(outputfile);
		System.out.println(outputfile + " generated.");
		return outputfile;
	}

	public static String runContainsSearch(String datafile, int col) {
		//String outputfile = ContainsSearch.run(filename, col);
		long ms = System.currentTimeMillis();
		String outputfile = "results_" + datafile;
		Vector output_vec = new Vector();
		Vector v = Utils.readFile(datafile);
		String heading = (String) v.elementAt(0);
		Vector u = StringUtils.parseData(heading, '\t');
		heading = heading + "\t" + "Matched NCIt Code(s)\tNCI PT(s)\tNCI SY(s)";
		Vector w = new Vector();
		output_vec.add(heading);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			u = StringUtils.parseData(line, '\t');
			String term = (String) u.elementAt(col);
			term = term.toLowerCase();
			w = ContainsSearch.contains(term);
			if (w.size() == 0) {
				output_vec.add(line + "\tNo match");
			} else if (w.size() == 1) {
				String code = (String) w.elementAt(0);
				String data = getAppendedData(code);
				output_vec.add(line + "\t" + code + "\t" + data);
			} else {
				output_vec.add(line + "\tMatches: " + w.size());
				for (int j=0; j<w.size(); j++) {
					String code = (String) w.elementAt(j);
					output_vec.add((String) LexicalMatching.id2LabelMap.get(code) + " (" + code + ")");
				}
			}
		}
		Utils.saveToFile(outputfile, output_vec);
		System.out.println("ContainsSearch outputfile: " + outputfile);
		tallyResults(outputfile);
		System.out.println(outputfile + " generated.");
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
		return outputfile;
	}

    public static void runIndexer(String datafile, char delim, int col) {
		long ms = System.currentTimeMillis();
		IndexUtils indexer = new IndexUtils();
		Vector lines = Utils.readFile(datafile);
		String narrative = null;
        for (int i=0; i<lines.size(); i++) {
   			String line = (String) lines.elementAt(i);
   			line = line.trim();
			Vector u = StringUtils.parseData(line, delim);
			narrative = (String) u.elementAt(col);
			Vector w = indexer.matchBySignature(narrative);
			if (w != null) {
				Utils.dumpVector(narrative, indexer.appendLabels(w));
			} else {
				w = indexer.indexNarrative(narrative);
				Utils.dumpVector(narrative, w);
			}
		}
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
    }

    public static String getAppendedData(String codes_str) {
		if (codes_str == null || codes_str.length() == 0) return "";
		Vector codes = StringUtils.parseData(codes_str, '|');
		return getAppendedData(codes);
	}

    public static String getAppendedData(Vector codes) {
		StringBuffer buf = new StringBuffer();
		for (int j=0; j<codes.size(); j++) {
			String code = (String) codes.elementAt(j);
			Vector pts = (Vector) ncitPTMap.get(code);
			String values = vector2Delimited(pts, '$');
			buf.append(values);
			if (j < codes.size()-1) {
				buf.append("|");
			}
		}
		String ncipt = buf.toString();
		buf = new StringBuffer();
		for (int j=0; j<codes.size(); j++) {
			String code = (String) codes.elementAt(j);
			Vector sys = (Vector) ncitSYMap.get(code);
			String values = vector2Delimited(sys, '$');
			buf.append(values);
			if (j < codes.size()-1) {
				buf.append("|");
			}
		}
		String ncisy = buf.toString();
		return ncipt + "\t" + ncisy;
	}

	public static String generateNoMatchFile(String filename) {
		Vector v = Utils.readFile(filename);
		Vector w = new Vector();
		String heading = (String) v.elementAt(0);
		int n = heading.indexOf("Matched NCIt Code(s)");
		String t = null;
		if (n != -1) {
			t = heading.substring(0, n-1);
			w.add(t);
		}
		for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			n = line.indexOf("No match");
			if (n != -1) {
				t = line.substring(0, n-1);
				w.add(t);
			}
		}
		String outputfile = "nomatch_" + filename;
		Utils.saveToFile(outputfile, w);
		return outputfile;
	}

	public static void tallyResults(String filename) {
		int result_col = 1;
		Vector v = Utils.readFile(filename);
		int min_cols = -1;
        for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (line.indexOf("No match") != -1) {
				Vector u = StringUtils.parseData(line, '\t');
				min_cols = u.size();
				for (int j=0; j<u.size(); j++) {
					String t = (String) u.elementAt(j);
					if (t.compareTo("No match") == 0) {
						result_col = j;
						break;
					}
				}
			}
		}
		Vector w = new Vector();
		Vector w1 = new Vector();
		int total = 0;
		int matched = 0;
		int unmatched = 0;
		HashSet hset = new HashSet();

        for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '\t');
			if (u.size() >= min_cols) {
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
						w.add(line);
					}
				}
			}
		}
		System.out.println("matched: " + matched);
		System.out.println("unmatched: " + unmatched);
		System.out.println("total: " + total);
		Utils.saveToFile("matched_" + filename, w);
		Utils.saveToFile("notmatched_" + filename, w1);
	}


}

