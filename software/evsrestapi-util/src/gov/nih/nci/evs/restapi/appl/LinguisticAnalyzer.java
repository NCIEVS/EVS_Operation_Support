package gov.nih.nci.evs.restapi.appl;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.bean.*;
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
 *     Initial implementation kim.ong@nih.gov
 *
 */


public class LinguisticAnalyzer {
    static HashSet retiredConcepts = new HashSet();
    static String CONCEPT_STATUS_FILE = "P310.txt";

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

    public static HashMap createterm2CodesMap(Vector v) {
		HashMap hmap = new HashMap();
        for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String prop_code = (String) u.elementAt(2);
			if (prop_code.compareTo("P90") == 0) {
				String term = (String) u.elementAt(0);
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

    public static HashMap createCode2TermMap(Vector v) {
		HashMap hmap = new HashMap();
        for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String prop_code = (String) u.elementAt(2);
			if (prop_code.compareTo("P90") == 0) {
				String term = (String) u.elementAt(0);
				String code = (String) u.elementAt(1);
				hmap.put(code, term);
			}
		}
		return hmap;
	}


    public static boolean containsAll(Vector v1, Vector v2) {
		for (int i=0; i<v2.size(); i++) {
			String t = (String) v2.elementAt(i);
			if (!v1.contains(t)) {
				return false;
			}
		}
		return true;
	}

	public static boolean is_retired(String code) {
		return retiredConcepts.contains(code);
	}

    public static HashSet createRetiredConceptSet() {
		HashSet hset = new HashSet();
		File file = new File(CONCEPT_STATUS_FILE);
		if (!file.exists()) {
			System.out.println("WARNING: " + CONCEPT_STATUS_FILE + " does not exist.");
			extractProperties("P310");
		}
		Vector v = Utils.readFile(CONCEPT_STATUS_FILE);
		//Coronary Reperfusion Procedure|C100067|Concept_Status|Retired_Concept
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

	public static Vector searchForCancerTypes() {
		String downloadDirectory = ConfigurationController.downloadDirectory;
		String hierfile = ConfigurationController.hierfile;
		String axiomfile = ConfigurationController.axiomfile;
		String rolefile = ConfigurationController.rolefile;

		System.out.println(downloadDirectory + File.separator + hierfile);
        File f = new File(downloadDirectory + File.separator + hierfile);
		System.out.println(hierfile + " exists? " + f.exists());

        System.out.println(downloadDirectory + File.separator + axiomfile);
        f = new File(downloadDirectory + File.separator + axiomfile);
		System.out.println(axiomfile + " exists? " + f.exists());

        System.out.println(downloadDirectory + File.separator + rolefile);
        f = new File(downloadDirectory + File.separator + rolefile);
		System.out.println(rolefile + " exists? " + f.exists());

		HierarchyHelper hh = null;
		Vector v = Utils.readFile(downloadDirectory + File.separator + hierfile);
		System.out.println(v.size());
		hh = new HierarchyHelper(v);
        System.out.println("HierarchyHelper instantiated.");

		v = new Vector();
        String code = "C3262";
        Vector neoplasm_branch_v3 = null;
        String filename = "v3_" + code + ".txt";
        File file = new File(filename);
        if (!file.exists()) {
			neoplasm_branch_v3 = hh.get_transitive_closure_v3(code);
			Utils.saveToFile(filename, neoplasm_branch_v3);
		} else {
             neoplasm_branch_v3 = Utils.readFile(filename);
		}
		Vector w = Utils.readFile(downloadDirectory + File.separator + axiomfile);
		HashMap code2TermMap = createCode2TermMap(w);
		for (int i=0; i<neoplasm_branch_v3.size(); i++) {
			String c = (String) neoplasm_branch_v3.elementAt(i);
			String s = (String) code2TermMap.get(c);
			Vector u2 = StringUtils.parseData(s, ' ');
			if (u2.size() == 2) {
				v.add(s + "|" + c);
			}
		}
		return new SortUtils().quickSort(v);
	}


	public static void run(Vector terms) {
		String downloadDirectory = ConfigurationController.reportGenerationDirectory;
		String hierfile = ConfigurationController.hierfile;
		String axiomfile = ConfigurationController.axiomfile;
		String rolefile = ConfigurationController.rolefile;

		System.out.println(downloadDirectory + File.separator + hierfile);
        File f = new File(downloadDirectory + File.separator + hierfile);
		System.out.println(hierfile + " exists? " + f.exists());

        System.out.println(downloadDirectory + File.separator + axiomfile);
        f = new File(downloadDirectory + File.separator + axiomfile);
		System.out.println(axiomfile + " exists? " + f.exists());

        System.out.println(downloadDirectory + File.separator + rolefile);
        f = new File(downloadDirectory + File.separator + rolefile);
		System.out.println(rolefile + " exists? " + f.exists());

        retiredConcepts = createRetiredConceptSet();

		HierarchyHelper hh = null;
		Vector v = Utils.readFile(downloadDirectory + File.separator + hierfile);
		System.out.println(v.size());
		hh = new HierarchyHelper(v);
        System.out.println("HierarchyHelper instantiated.");

        String code = "C3262";
        Vector neoplasm_branch_v3 = null;
        String filename = "v3_" + code + ".txt";
        File file = new File(filename);
        if (!file.exists()) {
			neoplasm_branch_v3 = hh.get_transitive_closure_v3(code);
			Utils.saveToFile(filename, neoplasm_branch_v3);
		} else {
            neoplasm_branch_v3 = Utils.readFile(filename);
		}

        Vector w = Utils.readFile(downloadDirectory + File.separator + axiomfile);
        System.out.println("axiom: " + v.size());

        HashMap term2CodeMap = createterm2CodesMap(w);
        HashMap code2TermMap = createCode2TermMap(w);

        Vector primitive_vec = new Vector();

        Vector warning_vec = new Vector();
        for (int lcv=0; lcv<terms.size(); lcv++) {
			int lcv1 = lcv + 1;
			String line = (String) terms.elementAt(lcv);
			Vector u0 = StringUtils.parseData(line, '|');
			String label = (String) u0.elementAt(0);
			code = (String) u0.elementAt(1);
			if (is_retired(code)) {
				System.out.println("(*) " + code + " is retired.");
			} else {
				System.out.println("(" + lcv1 + ") " + label + " (" + code + ")");
				Vector u1 = StringUtils.parseData(label, ' ');
				Vector subs = hh.get_transitive_closure_v3(code);
				System.out.println("subs: " + subs.size());
				for (int k=0; k<neoplasm_branch_v3.size(); k++) {
					String c = (String) neoplasm_branch_v3.elementAt(k);
					if (c.compareTo(code) != 0) {
						String s = (String) code2TermMap.get(c);
						Vector u2 = StringUtils.parseData(s, ' ');
						if (u2.size() == 3) {
							if (containsAll(u2, u1)) {
								if (!subs.contains(c) && !is_retired(c)) {
									//check if the concept is primitive
									boolean isDefined = PrimitiveConceptUtils.isDefined(c);
									if (isDefined) {
										System.out.println("\tWARNING: " + s + " (" + c + ")" + " is NOT a subconcept of " + label + " (" + code + ")" );
										warning_vec.add(s + " (" + c + ")" + " is NOT a subconcept of " + label + " (" + code + ")");
									} else {
										primitive_vec.add(label + " (" + c + ")");
									}
								}
							}
						}
					}
				}
			}
		}
		warning_vec = new SortUtils().quickSort(warning_vec);
		Utils.saveToFile("warnings_" + StringUtils.getToday() + ".txt", warning_vec);
		Utils.saveToFile("primitive_" + StringUtils.getToday() + ".txt", primitive_vec);
	}

	public static void run() {
		long ms = System.currentTimeMillis();
		String filename = "cancer_types.txt";
        File file = new File(filename);
        Vector v = null;
        if (!file.exists()) {
			v = searchForCancerTypes();
			Utils.saveToFile("cancer_types.txt", v);
		} else {
            v = Utils.readFile(filename);
		}
		run(v);
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}

	public static void main(String[] args) {
		run();
	}
}

