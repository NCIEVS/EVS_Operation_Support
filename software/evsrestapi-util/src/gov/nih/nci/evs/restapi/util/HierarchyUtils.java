package gov.nih.nci.evs.restapi.util;
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


public class HierarchyUtils {
    static HierarchyHelper hh = null;
    static HashMap term2CodeMap = null;
    static HashMap code2LabelMap = null;
    static HashMap definitionMap = null;

    static {
		definitionMap = new HashMap();
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

		hh = null;
		Vector v = Utils.readFile(downloadDirectory + File.separator + hierfile);
		System.out.println(v.size());
		hh = new HierarchyHelper(v);
        System.out.println("HierarchyHelper instantiated - " + v.size());

        Vector w = Utils.readFile(downloadDirectory + File.separator + axiomfile);
        System.out.println("axiom: " + w.size());

        term2CodeMap = createterm2CodesMap(w);
        code2LabelMap = createCode2LabelMap(w);
        definitionMap = createDefinitionMap(w);
	}

    public static HashMap createDefinitionMap(Vector v) {
		HashMap definitionMap = new HashMap();
		for (int i=0; i<v.size(); i++) {
			String t = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(t, '|');
			String code = (String) u.elementAt(1);
			String prop_code = (String) u.elementAt(2);
			if (prop_code.compareTo("P97") == 0) {
				String def = (String) u.elementAt(3);
				definitionMap.put(code, def);
			}
		}
		return definitionMap;
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

    public static HashMap createCode2LabelMap(Vector v) {
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

	public static Vector getSubclassCodes(String code) {
		return hh.getSubclassCodes(code);
	}

	public static Vector getSuperclassCodes(String code) {
		return hh.getSuperclassCodes(code);
	}

	public static int TRAVERSE_UP = 1;
	public static int TRAVERSE_DOWN = 0;

	public static void traverse(PrintWriter pw, String root, Vector codes) {
		traverse(pw, root, TRAVERSE_DOWN, codes);
	}

	public static void traverse(PrintWriter pw, String root, int direction, Vector codes) {
		String code_1 = (String) codes.elementAt(0);
		String code_2 = (String) codes.elementAt(1);
		boolean traverseDown = false;
		Vector w0 = new Vector();
		for (int i=0; i<codes.size(); i++) {
			String c = (String) codes.elementAt(i);
			Vector w1 = hh.getTransitiveClosure(c, traverseDown);
			w0.addAll(w1);
		}
		Stack stack = new Stack();
		stack.push("0|"+root);
        while (!stack.isEmpty()) {
			String s = (String) stack.pop();
			Vector u = new Utils().parseData(s, '|');
			String level_str = (String) u.elementAt(0);
			int level = Integer.parseInt(level_str);
			String curr_code = (String) u.elementAt(1);

			String label = hh.getLabel(curr_code);
			String indent = "";
			for (int i=0; i<level; i++) {
				indent = indent + "\t";
			}
			if (codes.contains(curr_code)) {
				if (curr_code.compareTo(code_1) == 0) {
					pw.println(indent + label + " (" + curr_code + ") (**)");
				} else {
					pw.println(indent + label + " (" + curr_code + ") (*)");
				}
			} else {
				pw.println(indent + label + " (" + curr_code + ")");
		    }
			if (!codes.contains(curr_code) && w0.contains(curr_code)) {
				Vector w = null;
				if (direction == TRAVERSE_UP) {
					w = hh.getSuperclassCodes(curr_code);
				} else {
					w = hh.getSubclassCodes(curr_code);
				}

				if (w != null) {
					level = level + 1;
					for (int j=0; j<w.size(); j++) {
						String next_code = (String) w.elementAt(j);
						String t = "" + level + "|" + next_code;
						stack.push(t);
					}
				}
			}
		}
	}


	public static void run_parents(String filename) {
        Vector warnings = Utils.readFile(filename);//"warnings.txt");
		PrintWriter pw = null;
		String outputfile = "parents.txt";
		try {
			pw = new PrintWriter(outputfile, "UTF-8");
			for (int i=0; i<warnings.size(); i++) {
				int lcv = i+1;
				String line = (String) warnings.elementAt(i);
				//System.out.println("\n(" + lcv + ") " + line);
				pw.println("\n(" + lcv + ") " + line);
				line = line.replace(" (", "|");
				line = line.replace(" is NOT a subconcept of ", "|");
				line = line.replace(")", "");
				Vector u = StringUtils.parseData(line, '|');
				String code1 = (String) u.elementAt(1);
				String label1 = (String) code2LabelMap.get(code1);
				String def_1 = (String) definitionMap.get(code1);
				pw.println("\t" + label1 + " (" + code1 + "): " + def_1);
				Vector parent_codes = getSuperclassCodes(code1);
				for (int j=0; j<parent_codes.size(); j++) {
					String parent_code = (String) parent_codes.elementAt(j);
					String parent_label = (String) code2LabelMap.get(parent_code);
					String def_2 = (String) definitionMap.get(parent_code);
				    pw.println("\tParent: " + parent_label + " (" + parent_code + "): " + def_2);
		    	}
			}

		} catch (Exception ex) {

		} finally {
			try {
				pw.close();
				System.out.println("Output file " + outputfile + " generated.");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}


	public static void run_definition(String filename) {
        Vector warnings = Utils.readFile(filename);//"warnings.txt");
		PrintWriter pw = null;
		String outputfile = "definition.txt";
		try {
			pw = new PrintWriter(outputfile, "UTF-8");
			for (int i=0; i<warnings.size(); i++) {
				int lcv = i+1;
				String line = (String) warnings.elementAt(i);
				//System.out.println("\n(" + lcv + ") " + line);
				pw.println("\n(" + lcv + ") " + line);
				line = line.replace(" (", "|");
				line = line.replace(" is NOT a subconcept of ", "|");
				line = line.replace(")", "");
				Vector u = StringUtils.parseData(line, '|');
				String code1 = (String) u.elementAt(1);
				String code2 = (String) u.elementAt(3);
				String def_1 = (String) definitionMap.get(code1);
				String def_2 = (String) definitionMap.get(code2);
				String label1 = (String) code2LabelMap.get(code1);
				String label2 = (String) code2LabelMap.get(code2);
				pw.println("\t" + label1 + " (" + code1 + "): " + def_1);
                pw.println("\t" + label2 + " (" + code2 + "): " + def_2);
			}

		} catch (Exception ex) {

		} finally {
			try {
				pw.close();
				System.out.println("Output file " + outputfile + " generated.");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}

	public static void run(String filename) {
		System.out.println("filename: " + filename);
        Vector warnings = Utils.readFile(filename);//"warnings.txt");
        System.out.println("warnings: " + warnings.size());
		PrintWriter pw = null;
		String outputfile = "tree.txt";
        String root = "C3262"; //Neoplasm (Code C3262)
		try {
			pw = new PrintWriter(outputfile, "UTF-8");
			for (int i=0; i<warnings.size(); i++) {
				int lcv = i+1;
				String line = (String) warnings.elementAt(i);
				//System.out.println("\n(" + lcv + ") " + line);
				pw.println("\n(" + lcv + ") " + line);
				line = line.replace(" (", "|");
				line = line.replace(" is NOT a subconcept of ", "|");
				line = line.replace(")", "");
				Vector u = StringUtils.parseData(line, '|');
				Vector codes = new Vector();
				codes.add((String) u.elementAt(1));
				codes.add((String) u.elementAt(3));
				traverse(pw, root, codes);
			}

		} catch (Exception ex) {

		} finally {
			try {
				pw.close();
				System.out.println("Output file " + outputfile + " generated.");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}

	public static void main(String[] args) {
		long ms = System.currentTimeMillis();
		String filename = args[0];
        //run(filename);
        run_definition(filename);
        run_parents(filename);
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}

}