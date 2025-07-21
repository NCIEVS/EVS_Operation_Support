package gov.nih.nci.evs.restapi.appl;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.config.*;

import java.io.*;
import java.text.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;
import java.nio.charset.Charset;
import java.io.UnsupportedEncodingException;

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

public class IsAHierarchyQA {
	String serviceUrl = null;
	String named_graph = null;
	String username = null;
	String password = null;
	String ncit_version = null;
	OWLSPARQLUtils owlSPARQLUtils = null;
	Vector parent_child_vec = null;
    HashSet retired_concepts = new HashSet();
    HashMap code2LabelMap = null;
    HierarchyHelper hh = null;
    HashMap branchMap = null;
    Vector roots = null;

    public void initialize() {
        long ms = System.currentTimeMillis();
		this.owlSPARQLUtils = new OWLSPARQLUtils(serviceUrl, username, password);
		this.owlSPARQLUtils.set_named_graph(named_graph);
		System.out.println("OWLSPARQLUtils instantiated.");
		this.ncit_version  = get_ncit_version();
		System.out.println("NCI Thesaurus version: " + ncit_version);
		parent_child_vec = owlSPARQLUtils.getHierarchicalRelationships(named_graph);
		Utils.saveToFile("parent_child.txt", parent_child_vec);
		hh = new HierarchyHelper(parent_child_vec);
		System.out.println("HierarchyHelper instantiated.");
		code2LabelMap = new HashMap();
		for (int i=0; i<parent_child_vec.size(); i++) {
			String line = (String) parent_child_vec.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(1);
			String label = (String) u.elementAt(0);
			label = HTMLDecoder.decode(label);
			code2LabelMap.put(code, label);
			code = (String) u.elementAt(3);
			label = (String) u.elementAt(2);
			label = HTMLDecoder.decode(label);
			code2LabelMap.put(code, label);
		}
		System.out.println("code2LabelMap instantiated.");
		roots = hh.getRoots();
		Utils.dumpVector("roots", roots);
		branchMap = generateBranchHashMap();

		//retired_concepts
		retired_concepts = new HashSet();
	    String property_name = "Concept_Status";
	    String property_value = "Retired_Concept";
		Vector w = owlSPARQLUtils.findConceptsWithProperty(named_graph, property_name);
		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String status = (String) u.elementAt(u.size()-1);
			if (status.compareTo(property_value) == 0) {
				String code = (String) u.elementAt(1);
				retired_concepts.add(code);
			}
		}
		System.out.println("Number of retired concepts: " + retired_concepts.size());
		System.out.println("Total initialization run time (ms): " + (System.currentTimeMillis() - ms));
	}

	public IsAHierarchyQA(String serviceUrl, String namedGraph, String username, String password) {
		this.serviceUrl = serviceUrl;
		this.named_graph = namedGraph;
		this.username = username;
		this.password = password;
		initialize();
    }

	public HashMap generateBranchHashMap() {
		System.out.println("generateBranchHashMap ...");
		HashMap hmap = new HashMap();
		for (int i=0; i<roots.size(); i++) {
			String root = (String) roots.elementAt(i);
			Vector v = hh.get_transitive_closure_v3(root);
			hmap.put(root, Utils.vector2HashSet(v));
		}
		return hmap;
	}

	public boolean isRetired(String code) {
		return retired_concepts.contains(code);
	}

	public void loadSampleOutputData(String filename) {
		Vector v = Utils.readFile(filename);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector w = StringUtils.parseData(line, '\t');
			Utils.dumpVector(line, w);
		}
	}

	public String get_ncit_version() {
		Vector v = owlSPARQLUtils.get_ontology_info(named_graph);
		String line = (String) v.elementAt(0);
		Vector u = StringUtils.parseData(line, '|');
		String ncit_version = (String) u.elementAt(0);
		System.out.println(ncit_version);
		return ncit_version;
	}

	public Vector getRedundantParents(String code) {
		Vector w0 = new Vector();
		Vector v = hh.getSuperclassCodes(code);
		if (v != null && v.size() > 1) {
			HashSet hset = Utils.vector2HashSet(v);
			for (int i=0; i<v.size(); i++) {
				String parentCode = (String) v.elementAt(i);
				Vector w = hh.getSuperclassCodes(parentCode);
				if (w != null && w.size() > 0) {
					for (int j=0; j<w.size(); j++) {
						String ancestorCode = (String) w.elementAt(j);
						if (hset.contains(ancestorCode)) {
							String label = (String) code2LabelMap.get(code);
							StringBuffer buf = new StringBuffer();
							buf.append(code + "\t" + label);
							String s1 = buf.toString();
							buf = new StringBuffer();
							Iterator it = branchMap.keySet().iterator();
							while (it.hasNext()) {
								String root = (String) it.next();
								hset = (HashSet) branchMap.get(root);
								if (hset.contains(code)) {
									String rootLabel = (String) code2LabelMap.get(root);
									buf.append(rootLabel + " (" + root + ")");
									buf.append("; ");
								}
							}
							String s2 = buf.toString();
							s2 = s2.substring(0, s2.length()-2);
							String parentLabel = (String) code2LabelMap.get(parentCode);
							String ancestorLabel = (String) code2LabelMap.get(ancestorCode);
							String output = s1 + "\t" + s2 + "\t" + parentCode + "\t" + parentLabel + "\t" + ancestorCode + "\t" + ancestorLabel;
							w0.add(output);
							System.out.println(output);
						}
					}
				}
			}
		}
		return w0;
	}

	public void run() {
		long ms = System.currentTimeMillis();
		int n = 0;
		int total = code2LabelMap.keySet().size();
		System.out.println("total " + total);
		Vector w = new Vector();
		w.add("Code\tLabel\tBranch\tParent Code\tParent Label\tAncestor Code\tAncestor Label");
		Iterator it = code2LabelMap.keySet().iterator();
		while (it.hasNext()) {
			n++;
			if (n / 1000 * 1000 == n) {
				System.out.println(n + " out of " + total + " completed.");
			}
			String code = (String) it.next();
			Vector w0 = getRedundantParents(code);
			if (w0.size() > 0) {
				w.addAll(w0);
			}
		}
		System.out.println(total + " out of " + total + " completed.");
		try {
			System.out.println("Number of redundant parent cases found: " + w.size());
			String version = ncit_version;
			String outputfile = version + "_" + "redundant_parents.txt";
			System.out.println("Outputfile: " + outputfile);
			Utils.saveToFile(outputfile, w);
			System.out.println("Outputfile " + outputfile + " generated.");
			System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) {
		String serviceUrl = ConfigurationController.serviceUrl;
		String namedGraph = ConfigurationController.namedGraph;
		String username = ConfigurationController.username;
		String password = ConfigurationController.password;
		IsAHierarchyQA generator = new IsAHierarchyQA(serviceUrl, namedGraph, username, password);
		generator.run();
	}
}

