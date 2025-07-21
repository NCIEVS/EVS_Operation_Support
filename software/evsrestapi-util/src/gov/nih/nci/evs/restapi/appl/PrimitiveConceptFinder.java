package gov.nih.nci.evs.restapi.appl;
import gov.nih.nci.evs.restapi.util.*;
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
import java.util.*;
import java.util.regex.*;
import org.json.*;

/**
 * <!-- LICENSE_TEXT_START -->
 * Copyright 2020 MSC. This software was developed in conjunction
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
 *      "This product includes software developed by MSC and the National
 *      Cancer Institute."   If no such end-user documentation is to be
 *      included, this acknowledgment shall appear in the software itself,
 *      wherever such third-party acknowledgments normally appear.
 *   3. The names "The National Cancer Institute", "NCI" and "MSC" must
 *      not be used to endorse or promote products derived from this software.
 *   4. This license does not authorize the incorporation of this software
 *      into any third party proprietary programs. This license does not
 *      authorize the recipient to use any trademarks owned by either NCI
 *      or MSC
 *   5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED
 *      WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *      OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE
 *      DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
 *      MSC, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT,
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


public class PrimitiveConceptFinder {
    static String namedGraph = null;
    static String prefixes = null;
    static String serviceUrl = null;
    static String username = null;
    static String password = null;
    static OWLSPARQLUtils owlSPARQLUtils = null;
    static String HIER_FILE = "parent_child.txt";
    static Vector hier_vec = null;
    static HierarchyHelper hh = null;
    static HashMap code2LabelMap = null;
    static HashSet definedSet = null;
    //static String namedGraph = ConfigurationController.namedGraph;
    static String OUTPUTFILE = "primitive_subclasses.txt";

////////////////////////////////////////////////////////////////
    static Vector subset_vec = null;

    static {
		long ms = System.currentTimeMillis();
		serviceUrl = ConfigurationController.serviceUrl;
		namedGraph = ConfigurationController.namedGraph;
		username = ConfigurationController.username;
		password = ConfigurationController.password;
		File file = new File(HIER_FILE);
		if (!file.exists()) {
			System.out.println("Retrieving hierarchy data ...");
			BasicQueryUtils basicQueryUtils = new BasicQueryUtils(serviceUrl, namedGraph, username, password);
			hier_vec = basicQueryUtils.getHierarchicalRelationships(namedGraph);
			saveToFile(HIER_FILE, hier_vec);
		} else {
			hier_vec = Utils.readFile(HIER_FILE);
		}
		hh = new HierarchyHelper(hier_vec);

		owlSPARQLUtils = new OWLSPARQLUtils(serviceUrl, username, password);
		owlSPARQLUtils.set_named_graph(namedGraph);

        Vector labels = loadLabels(ConfigurationController.namedGraph);
        code2LabelMap = new HashMap();
        for (int i=0; i<labels.size(); i++) {
			String line = (String) labels.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			code2LabelMap.put((String) u.elementAt(1), (String) u.elementAt(0));
		}
		Vector definedConcepts = getDefinedConcepts(namedGraph);
		Utils.saveToFile("defined_concepts.txt", definedConcepts);
		definedSet = new HashSet();
        for (int i=0; i<definedConcepts.size(); i++) {
			String line = (String) definedConcepts.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			definedSet.add((String) u.elementAt(1));
		}
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}


////////////////////////////////////////////////////////////////
    public static void saveToFile(String outputfile, Vector v) {
        try {
            FileOutputStream output = new FileOutputStream(outputfile);
            for (int i=0; i<v.size(); i++) {
				String data = (String) v.elementAt(i);
				if (i < v.size()) {
					data = data + "\n";
				}
				byte[] array = data.getBytes();
				output.write(array);
			}
            output.close();
        } catch(Exception e) {
            e.getStackTrace();
        }
    }


    public PrimitiveConceptFinder(String serviceUrl, String named_graph, String username, String password) {
		this.serviceUrl = serviceUrl;
    	this.namedGraph = named_graph;
    	this.username = username;
    	this.password = password;
    	initialize();
    }

    public void initialize() {
		System.out.println("initialize ...");
		owlSPARQLUtils = new OWLSPARQLUtils(serviceUrl, username, password);
		owlSPARQLUtils.set_named_graph(namedGraph);

		System.out.println("Done with initialization.");
	}

	public String getPrefixes() {
		return owlSPARQLUtils.getPrefixes();
	}

	public Vector executeQuery(String query) {
		return owlSPARQLUtils.executeQuery(query);
	}

	public static String construct_get_labels(String named_graph) {
		String prefixes = owlSPARQLUtils.getPrefixes();
		StringBuffer buf = new StringBuffer();
		buf.append(prefixes);
		buf.append("select distinct ?x_label ?x_code").append("\n");
		buf.append("{").append("\n");
		buf.append("graph <" + named_graph + "> ").append("\n");
		buf.append("{").append("\n");
		buf.append("                ?x a owl:Class .").append("\n");
		buf.append("                ?x :NHC0 ?x_code .").append("\n");
		buf.append("                ?x rdfs:label ?x_label .").append("\n");
		buf.append("}").append("\n");
		buf.append("}").append("\n");
		buf.append("order by ?x_label").append("\n");
		return buf.toString();
	}


	public static Vector loadLabels(String named_graph) {
		String query = construct_get_labels(named_graph);
		Vector v = owlSPARQLUtils.executeQuery(query);
		if (v == null) return null;
		if (v.size() == 0) return v;
		return new SortUtils().quickSort(v);
	}


	public static String construct_get_defined_concepts(String named_graph) {
		String prefixes = owlSPARQLUtils.getPrefixes();
		StringBuffer buf = new StringBuffer();
		buf.append(prefixes);
		buf.append("SELECT distinct ?x_label ?x_code ?y").append("\n");
		buf.append("{").append("\n");
		buf.append("   graph <" + named_graph + "> ").append("\n");
		buf.append("   {").append("\n");
		buf.append("        ?x a owl:Class .  ").append("\n");
		buf.append("		?x :NHC0 ?x_code .").append("\n");
		buf.append("		?x rdfs:label ?x_label . ").append("\n");
		buf.append("        ?x owl:equivalentClass ?y").append("\n");
		buf.append("   }").append("\n");
		buf.append("}").append("\n");
		return buf.toString();
	}


	public static Vector getDefinedConcepts(String named_graph) {
		String query = construct_get_defined_concepts(named_graph);
		Vector v = owlSPARQLUtils.executeQuery(query);
		if (v == null) return null;
		if (v.size() == 0) return v;
		return new SortUtils().quickSort(v);
	}

	public Vector getBranchCodes(String root) {
		return hh.get_transitive_closure_v3(root);
	}

	public Vector findDefinedSubclasses(Vector subs) {
		Vector w = new Vector();
		for (int i=0; i<subs.size(); i++) {
			String sub = (String) subs.elementAt(i);
			if (definedSet.contains(sub)) {
				w.add(sub);
			}
		}
		return w;
	}

	public static boolean isDefined(String code) {
		return definedSet.contains(code);
	}

	public String getLabel(String code) {
		return (String) code2LabelMap.get(code);
	}

	public static void QA(String OUTPUTFILE) {
		int num_errors = 0;
		Vector v = Utils.readFile(OUTPUTFILE);
		for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '\t');
			String code_1 = (String) u.elementAt(3);
			String code_2 = (String) u.elementAt(5);
			if (!isDefined(code_1)) {
				System.out.println("ERROR: " + code_1 + " is not defined.");
				num_errors++;
			}
			if (isDefined(code_2)) {
				System.out.println("ERROR: " + code_2 + " is defined.");
				num_errors++;
			}
		}
		System.out.println("Number of errors: " + num_errors);
	}

	public static Vector markPlimitiveConcepts(Vector w) {
		Vector w1 = new Vector();
		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			int n = line.lastIndexOf("(");
			String s = line.substring(n+1, line.length()-1);
			s = s.trim();
			if (!isDefined(s)) {
				w1.add(line + " (*)");
			} else {
				w1.add(line);
			}
		}
		return w1;
	}

	public static void generateTree() {
		long ms = System.currentTimeMillis();
		//HierarchyHelper hh = new HierarchyHelper(Utils.readFile(HIER_FILE));
		String primitiveConceptRoots = ConfigurationController.primitiveConceptRoots;
		Vector u = StringUtils.parseData(primitiveConceptRoots, '|');
		for (int k=0; k<u.size(); k++) {
			String root = (String) u.elementAt(k);
			Vector v = hh.get_transitive_closure_v4(root);
			HierarchyHelper hh_1 = new HierarchyHelper(v);
			Vector w = hh_1.exportTree();
			saveToFile(root + ".txt", w);
			Vector w1 = markPlimitiveConcepts(w);
			saveToFile(root + "_tree.txt", w1);
		}
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}

//Non-Neoplastic Disorder C53529, Genetic Disorder C3101 and Neoplastic Syndrome C54705
	public static void run(String primitiveConceptRoots) {
		String serviceUrl = ConfigurationController.serviceUrl;
		String namedGraph = ConfigurationController.namedGraph;
		String username = ConfigurationController.username;
		String password = ConfigurationController.password;
		//String primitiveConceptRoots = ConfigurationController.primitiveConceptRoots;
        PrimitiveConceptFinder test = new PrimitiveConceptFinder(serviceUrl, namedGraph, username, password);

        Vector w = new Vector();
        w.add("Root Concept Label\tRoot Concept Code\tDefined Subconcept Label\tDefined Subconcept Code\tPrimitive Subconcept Label\tPrimitive Subconcept Code");

        Vector w0 = new Vector();
        Vector w1 = new Vector();
        Vector w2 = new Vector();

        System.out.println("primitiveConceptRoots: " + primitiveConceptRoots);

        Vector codes = StringUtils.parseData(primitiveConceptRoots, '|');
        for (int i=0; i<codes.size(); i++) {
			String code = (String) codes.elementAt(i);
			String label = (String) code2LabelMap.get(code);
			Vector subs = test.getBranchCodes(code);
			Utils.dumpVector(label + " (" + code + ")", subs);

/*
For example under Neoplastic Syndrome you identified the defined subclasses:
Multiple Endocrine Neoplasia|C6432
Hereditary Neoplastic Syndrome|C3266
Myelodysplastic Syndrome|C3247
Richter Syndrome|C35424
Autoimmune Lymphoproliferative Syndrome|C37864
*/
            Vector defined_subs = test.findDefinedSubclasses(subs);
            for (int j=0; j<defined_subs.size(); j++) {
				String defined_sub_code = (String) defined_subs.elementAt(j);
				String defined_sub_label = test.getLabel(defined_sub_code);

				System.out.println("defined_sub_code: " + defined_sub_code);

                Vector grand_children = test.getBranchCodes(defined_sub_code);

                Utils.dumpVector(defined_sub_label, grand_children);

                w2.addAll(grand_children);
                w1 = new Vector();
				for (int k=0; k<grand_children.size(); k++) {
					String grand_child_code = (String) grand_children.elementAt(k);
					if (!isDefined(grand_child_code)) {
						w1.add(label + "\t" + code + "\t" +
							defined_sub_label + "\t" + defined_sub_code + "\t" +
						(String) code2LabelMap.get(grand_child_code) + "\t" +
						grand_child_code);
					}
				}
				w1 = new SortUtils().quickSort(w1);
				w0.addAll(w1);
			}
		}
		w.addAll(w0);
		int n = OUTPUTFILE.lastIndexOf(".");
		String filename = OUTPUTFILE.substring(0, n) + "_" + StringUtils.getToday() + ".txt";
		Utils.saveToFile(filename, w);
		System.out.println(filename + " generated.");
		Utils.saveToFile("grand_children.txt", w2);
		test.QA(filename);
	}

	public static void main(String[] args) {
		String primitiveConceptRoots = ConfigurationController.primitiveConceptRoots;
		System.out.println("primitiveConceptRoots: " + primitiveConceptRoots);
		run(primitiveConceptRoots);
		generateTree();
	}
}


