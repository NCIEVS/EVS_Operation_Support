package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.appl.*;
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
import java.nio.file.*;
import java.util.*;
import java.util.*;
import java.util.regex.*;
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
public class PolyHierarchy {
    static String REQUIRED_DATA_FILE = ConfigurationController.requiredDataFile;
	static Vector req_data_vec = null;
    static String DATA_INFO_FILE = "data_map.txt";
    static HashMap data_info_hashmap = null;
    static HashMap dataMap = null;
    static HashMap propertyMap = null;
    static HashMap synonymMap = null;
    static OWLScanner scanner = null;

    static HierarchyHelper hh = null;
    static Vector association_vec = null;
    static HashMap associationMap = null;
    static HashSet published_valuesets = null;
    static HashMap a8Map = null;

    static String NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile; //"ThesaurusInferred_forTS.owl";
	static String PARENT_CHILD_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile; // "parent_child.txt";
	static String RESTRICTION_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.rolefile; //"roles.txt";
	static String AXIOM_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.axiomfile;

    static Vector extensible_list_vec = null;
    static HashMap extensibleMap = null;

    static HashMap supportedPropertyCode2LabelMap = new HashMap();
    static HashMap supportedPropertyLabel2CodeMap = new HashMap();

    static HashMap subset_hmap = null;
    static Vector published_vaueset_vec = null;

    static Vector parent_child_vec = null;

	public static int BOTH = 0;
	public static int ISA_ONLY = 1;
	public static int SUBSET_ONLY = 2;

    static {
		parent_child_vec = Utils.readFile(PARENT_CHILD_FILE);
		hh = new HierarchyHelper(parent_child_vec);
		scanner = new OWLScanner(NCIT_OWL);
		//HashMap associationMap = scanner.getAssociationMap();
		//subset_hmap = create_subset_hmap();

		//HashMap propertyMap = scanner.getPropertyMap(scanner.get_owl_vec());
		//a8Map = (HashMap) propertyMap.get("A8");

		Vector v = scanner.extractAssociations(scanner.get_owl_vec(), "A8");
		a8Map = DataRetrieval.vector2MultiValuedPropMap(v);
		a8Map = DataRetrieval.generateInverseHashMap(a8Map);

        String prop_code = "P372";
		published_vaueset_vec = scanner.extractProperties(scanner.get_owl_vec(), prop_code);

		published_valuesets = new HashSet();
		for (int i=0; i<published_vaueset_vec.size(); i++) {
			String line = (String) published_vaueset_vec.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String yesOrNo = (String) u.elementAt(2);
			if (yesOrNo.compareTo("Yes") == 0) {
				published_valuesets.add((String) u.elementAt(0));
			}
		}
	}

	public static String getLabel(String code) {
		if (code.compareTo("<Root>") == 0) return "Root node";
		return hh.getLabel(code);
	}


	public static Vector getSubclassCodes(String code) {
		return hh.getSubclassCodes(code);
	}

	public PolyHierarchy(String owlfile) {

	}

	public static boolean is_published_valueset(String code) {
		return published_valuesets.contains(code);
	}

	public static String getIndentation(int n) {
	    StringBuffer buf = new StringBuffer();
	    for (int i=0; i<n; i++) {
			buf.append("\t");
		}
		return buf.toString();
	}

	public static String INVERSE_ISA = "[Inverse_Is_A]";
	public static String CONCEPT_IN_SUBSET = "[Subset_Concept]";

	public static Vector traverse(String root, int maxLevel) {
		Stack stack = new Stack();
		Vector w = new Vector();
		stack.push(INVERSE_ISA + "|0|" + root);
		stack.push(CONCEPT_IN_SUBSET + "|0|" + root);

		while (!stack.isEmpty()) {
			String s = (String) stack.pop();
			Vector u = StringUtils.parseData(s, '|');
			String rel = (String) u.elementAt(0);
			String levelStr = (String) u.elementAt(1);
			int level = Integer.parseInt(levelStr);
			String code = (String) u.elementAt(2);
			if (level == 0) {
				w.add(getLabel(code) + " (" + code + ")");
			} else {
				w.add(getIndentation(level) + rel + " " + getLabel(code) + " (" + code + ")");
			}
			level++;
			if (level <= maxLevel) {
				if (is_published_valueset(code)) {
					String label = getLabel(code);
					Vector members = (Vector) a8Map.get(code);
					members = sortCodesByLabels(members);

					for (int k=0; k<members.size(); k++) {
						String member_code = (String) members.elementAt(k);
						String member_label = getLabel(member_code);
						String t = CONCEPT_IN_SUBSET + "|" + level + "|" + member_code;
						stack.push(t);
					}
				}
				Vector subs = getSubclassCodes(code);
				if (subs != null) {
					subs = sortCodesByLabels(subs);
					for (int k=0; k<subs.size(); k++) {
						String sub = (String) subs.elementAt(k);
						String t = INVERSE_ISA + "|" + level + "|" + sub;
						stack.push(t);
					}
				}
			}
		}
        return w;
	}


	public static Vector traverse(String root, int maxLevel, int type) {
		Stack stack = new Stack();
		Vector w = new Vector();
		if (type == BOTH || type == ISA_ONLY) {
			stack.push(INVERSE_ISA + "|0|" + root);
		}
		if (type == BOTH || type == SUBSET_ONLY) {
			stack.push(CONCEPT_IN_SUBSET + "|0|" + root);
		}

		while (!stack.isEmpty()) {
			String s = (String) stack.pop();
			Vector u = StringUtils.parseData(s, '|');
			String rel = (String) u.elementAt(0);
			String levelStr = (String) u.elementAt(1);
			int level = Integer.parseInt(levelStr);
			String code = (String) u.elementAt(2);
			if (level == 0) {
				w.add(getLabel(code) + " (" + code + ")");
			} else {
				w.add(getIndentation(level) + rel + " " + getLabel(code) + " (" + code + ")");
			}
			level++;
			if (level <= maxLevel) {
				if (type == BOTH || type == SUBSET_ONLY) {
					if (is_published_valueset(code)) {
						String label = getLabel(code);
						Vector members = (Vector) a8Map.get(code);
						members = sortCodesByLabels(members);
						for (int k=0; k<members.size(); k++) {
							String member_code = (String) members.elementAt(k);
							String member_label = getLabel(member_code);
							String t = CONCEPT_IN_SUBSET + "|" + level + "|" + member_code;
							stack.push(t);
						}
					}
			    }
			    if (type == BOTH || type == ISA_ONLY) {
					Vector subs = getSubclassCodes(code);
					if (subs != null) {
						subs = sortCodesByLabels(subs);
						for (int k=0; k<subs.size(); k++) {
							String sub = (String) subs.elementAt(k);
							String t = INVERSE_ISA + "|" + level + "|" + sub;
							stack.push(t);
						}
					}
				}
			}
		}
        return w;
	}

    public static void hierarchyData2ASCIITree(String filename) {
		Vector w = new Vector();
		Vector v = Utils.readFile(filename);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String sup = (String) u.elementAt(0);
			String indent = getIndentation(Integer.parseInt((String) u.elementAt(5)));
			w.add(indent + (String) u.elementAt(2) + " (" + (String) u.elementAt(3) + ")");
		}
		Utils.saveToFile("tree_" + filename, w);
	}

    public static Vector extractCodesFromHierarchyData(String hierdatafile) {
		Vector v = Utils.readFile(hierdatafile);
		Vector codes = new Vector();
		for (int i=0; i<v. size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String sup_code = (String) u.elementAt(1);
			String sub_code = (String) u.elementAt(3);
			if (sup_code.compareTo("<Root>") != 0) {
				if (!codes.contains(sup_code)) {
					codes.add(sup_code);
				}
			}
			if (!codes.contains(sub_code)) {
				codes.add(sub_code);
			}
		}
		return codes;
	}

	public static Vector sortCodesByLabels(Vector codes) {
		HashMap code2LabelMap = new HashMap();
		Vector labels = new Vector();
		for (int i=0; i<codes.size(); i++) {
			String code = (String) codes.elementAt(i);
			String label = getLabel(code);
			labels.add(label);
			code2LabelMap.put(label, code);
		}
		labels = new SortUtils().quickSort(labels);
		Vector w = new Vector();
		for (int i=0; i<labels.size(); i++) {
			int j = labels.size()-i-1;
			String label = (String) labels.elementAt(j);
			String code = (String) code2LabelMap.get(label);
			w.add(code);
		}
		if (code2LabelMap.keySet().size() != codes.size()) {
			System.out.println("WARNING: " + " Multi-valued key encountered.");
		}
		return w;
	}

	public static Vector generateHierarchyData(String root, int maxLevel, int type) {
		Stack stack = new Stack();
		Vector w = new Vector();
		String sup = "<Root>";
		String sub = root;
		int level = 0;

		if (type == BOTH || type == ISA_ONLY) {
			stack.push(getLabel(sup) + "|" + sup + "|" + getLabel(sub) + "|" + sub + "|" + INVERSE_ISA + "|" + level);
		}
		if (type == BOTH || type == SUBSET_ONLY) {
			stack.push(getLabel(sup) + "|" + sup + "|" + getLabel(sub) + "|" + sub + "|" + CONCEPT_IN_SUBSET + "|" + level);
		}

		while (!stack.isEmpty()) {
			String s = (String) stack.pop();
			Vector u = StringUtils.parseData(s, '|');
			sup = (String) u.elementAt(1);
			sub = (String) u.elementAt(3);
			String code = sub;
			String rel = (String) u.elementAt(4);
			String levelStr = (String) u.elementAt(5);
			level = Integer.parseInt(levelStr);
			w.add(getLabel(sup) + "|" + sup + "|" + getLabel(sub) + "|" + sub + "|" + rel + "|" + level);
			level++;
			if (level <= maxLevel) {
				if (type == BOTH || type == SUBSET_ONLY) {
					if (is_published_valueset(code)) {
						String label = getLabel(code);
						Vector members = (Vector) a8Map.get(code);
						members = sortCodesByLabels(members);
						for (int k=0; k<members.size(); k++) {
							String member_code = (String) members.elementAt(k);
							String member_label = getLabel(member_code);
							stack.push(label + "|" + code + "|" + member_label + "|" + member_code + "|" + CONCEPT_IN_SUBSET + "|" + level);
						}
					}
			    }
			    if (type == BOTH || type == ISA_ONLY) {
					Vector subs = getSubclassCodes(code);
					if (subs != null) {
						subs = sortCodesByLabels(subs);
						for (int k=0; k<subs.size(); k++) {
							sub = (String) subs.elementAt(k);
							String label = getLabel(code);
							stack.push(label + "|" + code + "|" + getLabel(sub) + "|" + sub + "|" + INVERSE_ISA + "|" + level);
						}
					}
				}
			}
		}
        return w;
	}

	public static void generateHTMLTree(String asciitreedatafile, String title, String root) {
		HTMLHierarchy.run(asciitreedatafile, title, root);
	}

	public static void generateHTMLTree(String title, String root, int maxLevel, int type) {
		Vector parent_child_vec = generateHierarchyData(root, maxLevel, type);
		HTMLHierarchy.run(parent_child_vec, title, root);
	}

	public static int findMaxLevel(String root) {
		Stack stack = new Stack();
		Vector w = new Vector();
		String sub = root;
		int level = 0;
		stack.push(sub + "|" + level);
		int maxLevel = -1;
		int numNodes = 0;
		while (!stack.isEmpty()) {
			String s = (String) stack.pop();
			numNodes++;
			Vector u = StringUtils.parseData(s, '|');
			sub = (String) u.elementAt(0);
			String code = sub;
			String levelStr = (String) u.elementAt(1);
			level = Integer.parseInt(levelStr);
			if (level > maxLevel) {
				maxLevel = level;
			}
			level++;
			Vector subs = getSubclassCodes(code);
			if (subs != null) {
				for (int k=0; k<subs.size(); k++) {
					sub = (String) subs.elementAt(k);
					String label = getLabel(sub);
					stack.push(sub + "|" + level);
				}
			}
		}
		System.out.println(getLabel(root) + " (" + root + ")");
		System.out.println("Branch size: " + numNodes);
		System.out.println("Maximum level: " + maxLevel);
        return maxLevel;
	}

    public static void generateDynamicHTMLTree(String root, int maxLevel, Vector replace_vec, Vector by_vec) {
		Vector parent_child_vector = generateHierarchyData(root, maxLevel, ISA_ONLY);
		parent_child_vector.remove(0);
		String parent_child_file = root + "_tree" + ".txt";
		Utils.saveToFile(parent_child_file, parent_child_vector);
		int n = parent_child_file.lastIndexOf(".");
		String htmlfile = parent_child_file.substring(0, n) + ".html";
		ASCII2HTMLTreeConverter.generateDynamicHTMLTree(parent_child_file, htmlfile);
		/*
		Vector replace_vec = new Vector();
		Vector by_vec = new Vector();
		replace_vec.add("/ncitbrowser/js/");
		by_vec.add("");
		replace_vec.add("/ncitbrowser/images/");
		by_vec.add("");
		*/
		if (replace_vec != null && replace_vec.size() > 0) {
			substitute(htmlfile, replace_vec, by_vec);
		}
	}

	public static void substitute(String filename, Vector replace_vec, Vector by_vec) {
		Vector v = Utils.readFile(filename);
		int n = filename.lastIndexOf(".");
		String backupfile = "bak_" + filename;
		Utils.saveToFile(backupfile, v);
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			for (int j=0; j<replace_vec.size(); j++) {
				String replace = (String) replace_vec.elementAt(j);
				String by = (String) by_vec.elementAt(j);
				line = line.replace(replace, by);
			}
			w.add(line);
		}
		Utils.saveToFile(filename, w);
	}


	public static void main(String[] args) {
		String root = args[0];
		String maxLevelStr = args[1];
		int maxLevel = Integer.parseInt(maxLevelStr);
		Vector w = traverse(root, maxLevel);
		Utils.saveToFile(root + ".txt", w);
	}
}