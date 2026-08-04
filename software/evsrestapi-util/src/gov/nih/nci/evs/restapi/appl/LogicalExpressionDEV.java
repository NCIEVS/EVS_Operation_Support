package gov.nih.nci.evs.restapi.appl;
import gov.nih.nci.evs.restapi.util.*;
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

public class LogicalExpressionDEV {
    HashMap roleName2RangeNameMap = null;
	LogicalExpression le = null;
	String serviceUrl = null;
	String named_graph = null;
	String username = null;
	String password = null;
	OWLSPARQLUtils owlSPARQLUtils = null;
	OWLClassLoader loader = null;

	public LogicalExpressionDEV(String serviceUrl, String named_graph, String username, String password) {
		this.serviceUrl = serviceUrl;
		this.named_graph = named_graph;
		this.username = username;
		this.password = password;
		initialize();
	}

	private void initialize() {
		long ms = System.currentTimeMillis();
		owlSPARQLUtils = new OWLSPARQLUtils(serviceUrl, username, password);
		owlSPARQLUtils.set_named_graph(named_graph);
		le = new LogicalExpression(serviceUrl, named_graph, username, password);
		roleName2RangeNameMap = le.getRoleName2RangeNameMap();

		String ncit_owl = ConfigurationController.owlfile;
		loader = new OWLClassLoader(ncit_owl);
		Vector classIdVec = loader.getClassIdVec();
		System.out.println("Total LogicalExpressionDEV initializaion run time (ms): " + (System.currentTimeMillis() - ms));
	}

	public String construct_get_role_unions(String named_graph, String code) {
		String prefixes = owlSPARQLUtils.getPrefixes();
		StringBuffer buf = new StringBuffer();
		buf.append(prefixes);
		buf.append("select distinct ?x_code ?x_label ?c1 ?rs ?p_label ?p_code ?y_code ?y_label").append("\n");
		buf.append("from <" + named_graph + ">").append("\n");
		buf.append("where {").append("\n");
		buf.append("            ?x :NHC0 ?x_code .").append("\n");
		buf.append("            ?x rdfs:label ?x_label .").append("\n");
		if (code != null) {
			buf.append("            ?x :NHC0 \"" + code + "\"^^xsd:string .").append("\n");
		} else {
			buf.append("            ?x :NHC0 ?x_code .").append("\n");
		}
		buf.append("            ?x owl:equivalentClass ?e1 .").append("\n");
		buf.append("            ?e1 (owl:intersectionOf/rdf:rest*/rdf:first) ?c1 .").append("\n");
		buf.append("            ?c1 (owl:unionOf/rdf:rest*/rdf:first) ?rs .").append("\n");
		buf.append("            ?rs a owl:Restriction .").append("\n");
		buf.append("            ?rs owl:onProperty ?p .").append("\n");
		buf.append("            ?p :NHC0 ?p_code .").append("\n");
		buf.append("            ?p rdfs:label ?p_label .").append("\n");
		buf.append("            ?rs owl:someValuesFrom ?y .").append("\n");
		buf.append("            ?y :NHC0 ?y_code .").append("\n");
		buf.append("            ?y rdfs:label ?y_label .").append("\n");
		buf.append("}").append("\n");
		return buf.toString();
	}


	public Vector getRoleUnions(String named_graph, String code) {
		String query = construct_get_role_unions(named_graph, code);
		Vector v = owlSPARQLUtils.executeQuery(query);
		if (v == null) return null;
		if (v.size() == 0) return v;
		return new SortUtils().quickSort(v);
	}

	public String construct_get_role_groups(String named_graph, String code) {
		String prefixes = owlSPARQLUtils.getPrefixes();
		StringBuffer buf = new StringBuffer();
		buf.append(prefixes);
		buf.append("select distinct ?x_code ?x_label ?u1 ?i1 ?rs ?p_label ?p_code ?y_code ?y_label").append("\n");
		buf.append("from <" + named_graph + ">").append("\n");
		buf.append("where {").append("\n");
		buf.append("            ?x :NHC0 ?x_code .").append("\n");
		buf.append("            ?x rdfs:label ?x_label .").append("\n");
		if (code != null) {
			buf.append("            ?x :NHC0 \"" + code + "\"^^xsd:string .").append("\n");
		} else {
			buf.append("            ?x :NHC0 ?x_code .").append("\n");
		}
		buf.append("            ?x owl:equivalentClass ?e1 .").append("\n");
		buf.append("            ?e1 (owl:intersectionOf/rdf:rest*/rdf:first) ?u1 .").append("\n");
		buf.append("            ?u1 (owl:unionOf/rdf:rest*/rdf:first) ?i1 .").append("\n");
		buf.append("            ?i1 (owl:intersectionOf/rdf:rest*/rdf:first) ?rs .").append("\n");
		buf.append("            ?rs a owl:Restriction .").append("\n");
		buf.append("            ?rs owl:onProperty ?p .").append("\n");
		buf.append("            ?p :NHC0 ?p_code .").append("\n");
		buf.append("            ?p rdfs:label ?p_label .").append("\n");
		buf.append("            ?rs owl:someValuesFrom ?y .").append("\n");
		buf.append("            ?y :NHC0 ?y_code .").append("\n");
		buf.append("            ?y rdfs:label ?y_label .").append("\n");
		buf.append("}").append("\n");
		return buf.toString();
	}


	public Vector getRoleGroups(String named_graph, String code) {
		String query = construct_get_role_groups(named_graph, code);
		Vector v = owlSPARQLUtils.executeQuery(query);
		if (v == null) return null;
		if (v.size() == 0) return v;
		return new SortUtils().quickSort(v);
	}

    public gov.nih.nci.evs.restapi.bean.LogicalExpression getLogicalExpression(String code) {
		return le.getLogicalExpression(named_graph, code, true);
	}

	public Vector run(Vector codes) {
		Vector w = new Vector();
		try {
			for (int i=0; i<codes.size(); i++) {
				int j = i+1;
				String code = (String) codes.elementAt(i);
				Vector class_vec = loader.getClassData(code);
				w.addAll(class_vec);

				gov.nih.nci.evs.restapi.bean.LogicalExpression e = getLogicalExpression(code);
				w.add("(" + j + ") " + code);
				w.add(e.toJson());
				w.add("\n");
				w.add(e.toString());
				w.add("\n");
				System.out.println("" + j + " out of " + codes.size() + " completed.");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return w;
	}

	public Vector removeDuplicates(Vector v) {
		HashSet hset = new HashSet();
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String t = (String) v.elementAt(i);
			if (!hset.contains(t)) {
				hset.add(t);
				w.add(t);
			}
		}
		return w;
	}

	public Vector getTestCases() {
		Vector codes = new Vector();
		String filename = "roleUnion.txt";
		Vector v1 = TextFileExtractor.extractDistinctColumnValues(filename, 0, '|');
		codes.addAll(v1);
		filename = "roleGroup.txt";
		v1 = TextFileExtractor.extractDistinctColumnValues(filename, 0, '|');
		codes.addAll(v1);
		return removeDuplicates(codes);
	}

	public void appendRange(String filename) {
		Vector w = new Vector();
		Vector v = Utils.readFile(filename);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			String t = line;
			t = t.replace("\t\t", "\t");
			t = t.trim();
			t = t.replace("\t", "|");
			System.out.println(t);

			Vector u = Utils.parseData(t, '|');
			if (u.size() > 1) {
				String s = (String) u.elementAt(0);
				String roleName = (String) roleName2RangeNameMap.get(s);
				line = line + "\t\t[" + roleName + "]";
			}
			w.add(line);
		}
		Utils.saveToFile("range_" + filename, w);
	}

	public static void main(String[] args) {
		String serviceUrl = ConfigurationController.serviceUrl;
		String named_graph = ConfigurationController.namedGraph;
		String username = ConfigurationController.username;
		String password = ConfigurationController.password;

		LogicalExpressionDEV dev = new LogicalExpressionDEV(serviceUrl, named_graph, username, password);

		Vector w = new Vector();
		Vector v = dev.getRoleUnions(named_graph, null);
		w.addAll(v);
		Utils.saveToFile("roleUnion.txt", w);

		w = new Vector();
		v = dev.getRoleGroups(named_graph, null);
		w.addAll(v);
		Utils.saveToFile("roleGroup.txt", w);

		Vector codes = dev.getTestCases();
		w = dev.run(codes);
		Utils.saveToFile("cd_dev.txt", w);

	}
}
