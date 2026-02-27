package gov.nih.nci.evs.restapi.util;
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
public class ValueSetResolver {

	static String NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile; //"ThesaurusInferred_forTS.owl";
	static String PARENT_CHILD_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile; // "parent_child.txt";
	static String ASSOCIATION_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.subsetfile;

	private String owlfile = null;
	private Vector parent_child_vec = null;
	private Vector association_vec = null;
	private Vector fullsyn_vec = null; //    public Vector extractAxiomData(String prop_code)

	private HierarchyHelper hh = null;
	private HashMap valuesetMap = null;

    HashMap parent_child_hmap = null;
    HashMap subset_hmap = null;

	public static int FORMAT_PARENT_CHILD = 1;
	public static int FORMAT_CHILD_PARENT = 2;

	HashMap code2LabelMap = null;
	HashMap code2SourcePTMap = null;

	String sourcePTQualifiers = null; //"P384$PCDC|P386$ALL"

/*
ALL Vitals Table|C178124|P90|ALL Vitals Table|P383$PT|P384$NCI
ALL Vitals Table|C178124|P90|Acute Lymphocytic Leukemia Vitals Table|P383$SY|P384$NCI
ALL Vitals Table|C178124|P90|Vitals Table|P383$PT|P384$PCDC|P386$ALL
*/
	private HashMap createCode2SourcePTMap(Vector w) {
		HashMap label_hmap = new HashMap();
        for (int i=0; i<w.size(); i++) {
			String t = (String) w.elementAt(i);
			if (t.indexOf(this.sourcePTQualifiers) != -1) {
				Vector u = StringUtils.parseData(t, '|');
				String code = (String) u.elementAt(1);
				String pt = (String) u.elementAt(3);
				label_hmap.put(code, pt);
			}
		}
		return label_hmap;
	}

	public ValueSetResolver(String owlfile) {
		this.owlfile = owlfile;

	}

	public void setSourcePTQualifiers(String sourcePTQualifiers) {
		this.sourcePTQualifiers = sourcePTQualifiers;
	}

    public void initialize() {
		long ms = System.currentTimeMillis();
		Vector parent_child_vec = Utils.readFile(PARENT_CHILD_FILE);
		parent_child_hmap = create_parent_child_hmap();
		subset_hmap = create_subset_hmap();
		code2LabelMap = createCode2LabelMap(parent_child_vec);

		if (sourcePTQualifiers != null) {
			OWLScanner scanner = new OWLScanner(owlfile);
			System.out.println("extractAxiomData ...");
			fullsyn_vec = scanner.extractAxiomData("P90");
			System.out.println("fullsyn_vec: " + fullsyn_vec.size());
			code2SourcePTMap = createCode2SourcePTMap(fullsyn_vec);
			Utils.saveToFile("fullsyn_" + owlfile, fullsyn_vec);
	    }

		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}

	public Vector getSubclassCodes(String parentCode) {
		return (Vector) parent_child_hmap.get(parentCode);
	}
	public Vector getSubsetMemberCodes(String subsetCode) {
        return (Vector) subset_hmap.get(subsetCode);
	}


    private HashMap createCode2LabelMap(Vector w) {
		return createCode2LabelMap(w, FORMAT_PARENT_CHILD);
	}

	public static String decode(String text) {
		return HTMLDecoder.decode(text);
	}

	private HashMap createCode2LabelMap(Vector w, int format) {
		HashMap label_hmap = new HashMap();
        for (int i=0; i<w.size(); i++) {
			String t = (String) w.elementAt(i);
			Vector u = StringUtils.parseData(t);
			if (u.size() == 2) {
				String code = (String) u.elementAt(0);
				String label = (String) u.elementAt(1);
				label_hmap.put(code, label);

			} else {
				String parent_code = null;
				String child_code = null;
				String parent_label = null;
				String child_label = null;

				if (format == FORMAT_PARENT_CHILD) {
					parent_label = (String) u.elementAt(0);
					parent_code = (String) u.elementAt(1);
					child_label = (String) u.elementAt(2);
					child_code = (String) u.elementAt(3);
				} else {
					child_label = (String) u.elementAt(0);
					child_code = (String) u.elementAt(1);
					parent_label = (String) u.elementAt(2);
					parent_code = (String) u.elementAt(3);
				}
				label_hmap.put(parent_code, parent_label);
				label_hmap.put(child_code, child_label);
		    }
		}
		return label_hmap;
	}

	public HashMap create_subset_hmap() {
		Vector association_vec = Utils.readFile(ASSOCIATION_FILE);
		HashMap hmap = new HashMap();
		for (int i=0; i<association_vec.size(); i++) {
			String line = (String) association_vec.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String member_code = (String) u.elementAt(0);
			String asso = (String) u.elementAt(1);
			if (asso.compareTo("A8") == 0) {
				String subset_code = (String) u.elementAt(2);
				Vector w = new Vector();
				if (hmap.containsKey(subset_code)) {
					w = (Vector) hmap.get(subset_code);
				}
				w.add(member_code);
				hmap.put(subset_code, w);
			}
		}
		return hmap;
	}
	public HashMap create_parent_child_hmap() {
		Vector parent_child_vec = Utils.readFile(PARENT_CHILD_FILE);
		HashMap hmap = new HashMap();
		for (int i=0; i<parent_child_vec.size(); i++) {
			String line = (String) parent_child_vec.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String parent_code = (String) u.elementAt(1);
			String child_code = (String) u.elementAt(3);
			Vector w = new Vector();
			if (hmap.containsKey(parent_code)) {
				w = (Vector) hmap.get(parent_code);
			}
			w.add(child_code);
			hmap.put(parent_code, w);
		}
		return hmap;
	}

	public Vector resolve(String root) {
		initialize();
		Vector v = new Vector();
		Stack stack = new Stack();
        stack.push("@" + "|" + root);
        v.add("NCIt Code of Table	Table PT	NCIt Concept Code	NCIt PT");
		while (!stack.isEmpty()) {
			String t = (String) stack.pop();
			Vector u = StringUtils.parseData(t, '|');
			String code = (String) u.elementAt(0);
			if (code.compareTo("@@") != 0) {
				String label = (String) code2LabelMap.get(code);
				if (sourcePTQualifiers != null) {
				    label = (String) code2SourcePTMap.get(code);
				}
				if (label != null) {
					Vector memberCodes = (Vector) subset_hmap.get(code);
					if (memberCodes != null) {
						for (int i=0; i<memberCodes.size(); i++) {
							String memberCode = (String) memberCodes.elementAt(i);
							String memberLabel = (String) code2LabelMap.get(memberCode);
							String data = code + "\t" + label + "\t" + memberCode + "\t" + memberLabel;
							if (!v.contains(data)) {
								v.add(data);
							}
						}
					}
				} else {
					System.out.println("WARNING: Source PT not found for concept " + code);
				}

				String sub_code = (String) u.elementAt(1);
				Vector w = (Vector) parent_child_hmap.get(sub_code);
				if (w == null) {
					stack.push(sub_code + "|@@");
				}
				if (w != null && w.size() > 0) {
					for (int i=0; i<w.size(); i++) {
						String s = (String) w.elementAt(i);
						stack.push(sub_code + "|" + s);
					}
				}
			}
		}
		System.out.println("Number of concepts in value set " + root + ": " + v.size());
		return v;
	}

	public static void main(String[] args) {
		String owlfile = NCIT_OWL;
		String subsetCode = args[0];
		System.out.println("subsetCode: " + subsetCode);
		ValueSetResolver resolver = new ValueSetResolver(owlfile);
		//resolver.setSourcePTQualifiers("P384$PCDC|P386$ALL");
        String outputfile = subsetCode + "_members.txt";
		Vector w = resolver.resolve(subsetCode);
		Utils.saveToFile(outputfile, w);
	}
}

