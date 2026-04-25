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


public class ExactMatch {
    public static int DEFAULT = 0;
    public static int BRANCH = 1;
    public static int SUBSET = 2;

	HashMap term2CodesMap = null;
    boolean caseSensitive = false;
    String termfile = null;
    HashSet retiredConcepts = new HashSet();

    public ExactMatch(String termfile) {
		this.termfile = termfile;
		initialize();
	}

    public void initialize() {
		term2CodesMap = createTerm2CodesMap();
	}

	public boolean is_retired(String code) {
		return retiredConcepts.contains(code);
	}

	public void setRetiredConcepts(Vector retired_concepts) {
		retiredConcepts = Utils.vector2HashSet(retired_concepts);
	}

	public Vector getMatchedCodes(String term) {
		if (!caseSensitive) {
			term = term.toLowerCase();
		}
		if (!term2CodesMap.containsKey(term)) return null;
		return (Vector) term2CodesMap.get(term);
	}

    public HashMap createTerm2CodesMap() {
		HashMap hmap = new HashMap();
		Vector v = Utils.readFile(termfile);
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

	public String run(String datafile, String outputfile, int col) {
		System.out.println("ExactMatch datafile: " + datafile);
		System.out.println("ExactMatch outputfile: " + col);

		long ms = System.currentTimeMillis();

		Vector no_matches = new Vector();
		Vector matches = new Vector();
        Vector v = Utils.readFile(datafile);
        Vector w = new Vector();
        String header = (String) v.elementAt(0);
        w.add(header + "\tMatched NCIt Code(s)");

        no_matches.add(header);
        matches.add(header + "\tMatched NCIt Code(s)");
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
						if (!is_retired(code)) {
							codes.add(code);
						}
					}
					if (codes.size() > 0) {
						w.add(line + "\t" + Utils.vector2Delimited(codes, "|"));
						matches.add(line + "\t" + Utils.vector2Delimited(codes, "|"));
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
		System.out.println("w: " + w.size());
		Utils.saveToFile(outputfile, w);
		Utils.saveToFile("no_matches_" + outputfile, no_matches);
		Utils.saveToFile("matches_" + outputfile, matches);
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
		System.out.println(outputfile + " generated.");
		return outputfile;
	}

    public static String toExcel(String textfile) {
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
		return xlsfile;
	}

	public String run(String datafile, String outputfile) {
		return run(datafile, outputfile, 0);
	}

	public void run(String datafile, int colNum) {
		long ms = System.currentTimeMillis();
		String outputfile = "results_" + datafile;
		run(datafile, outputfile, colNum);
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}

	public void run(String datafile) {
		run(datafile, 0);
	}

}
