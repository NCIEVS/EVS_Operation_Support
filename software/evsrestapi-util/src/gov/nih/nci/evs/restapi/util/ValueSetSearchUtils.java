package gov.nih.nci.evs.restapi.util;
import java.io.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.*;

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
public class ValueSetSearchUtils {
	private OWLSPARQLUtils owlSPARQLUtils = null;
	private String serviceUrl = null;
	private String named_graph = null;
	private String username = null;
	private String password = null;

	public static String EXACT_MATCH = "exactMatch";
	public static String STARTSWITH = "startsWith";
	public static String CONTAINS = "contains";

	public static String[] NAMES = new String[] {"label", "Display_Name", "Preferred_Name",
	    "FULL_SYN", "Legacy Concept Name"};

	public static String[] ALGORITHMS = new String[] {EXACT_MATCH, STARTSWITH, CONTAINS};

	public ValueSetSearchUtils(String serviceUrl, String named_graph, String username, String password) {
		this.serviceUrl = serviceUrl;
		this.named_graph = named_graph;
		this.username = username;
		this.password = password;
		this.owlSPARQLUtils = new OWLSPARQLUtils(serviceUrl, username, password);
		this.owlSPARQLUtils.set_named_graph(named_graph);
	}

	public String construct_get_valueset_code_search(String named_graph, String code, String subset_code) {
		String prefixes = owlSPARQLUtils.getPrefixes();
		StringBuffer buf = new StringBuffer();
		buf.append(prefixes);
		buf.append("select distinct ?x_label ?x_code ?p1_label ?y_label ?y_code").append("\n");
		buf.append("from <" + named_graph + ">").append("\n");
		buf.append("where  { ").append("\n");
		buf.append("            	?x a owl:Class .").append("\n");
		buf.append("            	?x :NHC0 ?x_code .").append("\n");
		buf.append("                ?x :NHC0 \"" + code + "\"^^xsd:string .").append("\n");
		buf.append("            	?x rdfs:label ?x_label .").append("\n");
		buf.append("            	").append("\n");
		buf.append("            	?y a owl:Class .").append("\n");
		buf.append("            	?y :NHC0 ?y_code .").append("\n");
		if (subset_code != null) {
			buf.append("                ?y :NHC0 \"" + subset_code + "\"^^xsd:string .").append("\n");
		}
		buf.append("            	?y rdfs:label ?y_label .").append("\n");
		buf.append("            	").append("\n");
		buf.append("            	?x ?p1 ?y .").append("\n");
		buf.append("                ?p1 rdfs:label ?p1_label .").append("\n");
		buf.append("                ?p1 rdfs:label \"Concept_In_Subset\"^^xsd:string .").append("\n");
		buf.append("").append("\n");
		buf.append("                ?y ?p2 \"Yes\"^^xsd:string .").append("\n");
		buf.append("                ?p2 rdfs:label ?p2_label .").append("\n");
		buf.append("                ?p2 rdfs:label \"Publish_Value_Set\"^^xsd:string .").append("\n");
		buf.append("                ").append("\n");
		buf.append("}").append("\n");
		return buf.toString();
	}

	public Vector searchByCode(String named_graph, String code) {
		return searchByCode(named_graph, code, null);
	}


	public Vector searchByCode(String named_graph, String code, String subset_code) {
		String query = construct_get_valueset_code_search(named_graph, code, subset_code);
		Vector v = owlSPARQLUtils.executeQuery(query);
		if (v == null) return null;
		if (v.size() == 0) return v;
		v = new ParserUtils().getResponseValues(v);
		v = new SortUtils().quickSort(v);
		HashSet hset = new HashSet();
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (!hset.contains(line)) {
				hset.add(line);
				w.add(line);
			}
		}
		return w;
	}

	public String construct_get_valueset_search(String named_graph, String term, String algorithm, String subset_code) {
		term = term.toLowerCase();
		String prefixes = owlSPARQLUtils.getPrefixes();
		StringBuffer buf = new StringBuffer();
		buf.append(prefixes);
		buf.append("select distinct ?x_label ?x_code ?p1_label ?y_label ?y_code ?p_label ?p_value").append("\n");
		buf.append("from <" + named_graph + ">").append("\n");
		buf.append("where  { ").append("\n");
		buf.append("            	?x a owl:Class .").append("\n");
		buf.append("            	?x :NHC0 ?x_code .").append("\n");
		buf.append("            	?x rdfs:label ?x_label .").append("\n");
		buf.append("            	").append("\n");
		buf.append("            	?y a owl:Class .").append("\n");
		buf.append("            	?y :NHC0 ?y_code .").append("\n");
		if (subset_code != null) {
			buf.append("                ?y :NHC0 \"" + subset_code + "\"^^xsd:string .").append("\n");
		}
		buf.append("            	?y rdfs:label ?y_label .").append("\n");
		buf.append("            	").append("\n");
		buf.append("            	?x ?p1 ?y .").append("\n");
		buf.append("                ?p1 rdfs:label ?p1_label .").append("\n");
		buf.append("                ?p1 rdfs:label \"Concept_In_Subset\"^^xsd:string .").append("\n");
		buf.append("").append("\n");
		buf.append("                ?y ?p2 \"Yes\"^^xsd:string .").append("\n");
		buf.append("                ?p2 rdfs:label ?p2_label .").append("\n");
		buf.append("                ?p2 rdfs:label \"Publish_Value_Set\"^^xsd:string .").append("\n");
		buf.append("                ").append("\n");

		buf.append("                ?x ?p ?p_value .").append("\n");
		buf.append("                ?p rdfs:label ?p_label .").append("\n");

		if (algorithm.compareTo(EXACT_MATCH) == 0) {
			buf.append("                FILTER(lcase(str(?p_value)) = \"" + term + "\"^^xsd:string)").append("\n");
		} else if (algorithm.compareTo(STARTSWITH) == 0) {
			buf.append("                FILTER(strStarts(lcase(str(?p_value)), \"" + term + "\"^^xsd:string))").append("\n");
		} else if (algorithm.compareTo(CONTAINS) == 0) {
			buf.append("                FILTER(contains(lcase(str(?p_value)), \"" + term + "\"^^xsd:string))").append("\n");
		}
		buf.append("                ").append("\n");
		buf.append("}").append("\n");
		return buf.toString();
	}

	public Vector search(String named_graph, String term, String algorithm) {
		return search(named_graph, term, algorithm, null);
	}

	public Vector search(String named_graph, String term, String algorithm, String subset_code) {
		String query = construct_get_valueset_search(named_graph, term, algorithm, subset_code);
		Vector v = owlSPARQLUtils.executeQuery(query);
		if (v == null) return null;
		if (v.size() == 0) return v;
		v = new ParserUtils().getResponseValues(v);
		v = new SortUtils().quickSort(v);
		HashSet hset = new HashSet();
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (!hset.contains(line)) {
				hset.add(line);
				w.add(line);
			}
		}
		return w;
	}

	public String construct_get_value_set_metadata(String named_graph, String subset_code) {
		StringBuffer buf = new StringBuffer();
		buf.append("PREFIX :<http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#>").append("\n");
		buf.append("PREFIX owl:<http://www.w3.org/2002/07/owl#>").append("\n");
		buf.append("PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>").append("\n");
		buf.append("SELECT distinct ?x_label ?x_code ?y1_label ?z1 ?y2_label ?z2 ").append("\n");
		buf.append("{").append("\n");
		buf.append("graph <" + named_graph + ">").append("\n");
		buf.append("{").append("\n");
		buf.append("?x a owl:Class .").append("\n");
		buf.append("?x rdfs:label ?x_label .").append("\n");
		buf.append("?x :NHC0 ?x_code .").append("\n");
		buf.append("?x ?y1 ?z1 .").append("\n");
		buf.append("?y1 rdfs:label ?y1_label .").append("\n");
		buf.append("?y1 rdfs:label \"DEFINITION\"^^xsd:string .").append("\n");
		buf.append("?x ?y2 ?z2 .").append("\n");
		buf.append("?y2 rdfs:label \"Contributing_Source\"^^xsd:string .").append("\n");
		buf.append("?y2 rdfs:label ?y2_label .").append("\n");
		buf.append("FILTER (str(?x_code) = \"" + subset_code + "\"^^xsd:string)").append("\n");
		buf.append("}").append("\n");
		buf.append("}").append("\n");
		return buf.toString();
	}

	public Vector getValueSetMetadata(String named_graph, String subset_code) {
	    String query = construct_get_value_set_metadata(named_graph, subset_code);
	    Vector v = owlSPARQLUtils.executeQuery(query);
	    if (v == null) return null;
	    if (v.size() == 0) return v;
	    return new ParserUtils().getResponseValues(v);
	}

	public Vector parseVSData(Vector v, boolean byName) {
		return parseVSData(v, byName, true);
	}

	public Vector parseVSData(Vector v, boolean byName, boolean forVS) {
		//(1) Blue|C48333|Concept_In_Subset|CPTAC Baseline Medical Forms Terminology|C156953|FULL_SYN|Blue
		List<String> nameList = new ArrayList<>(Arrays.asList(NAMES));
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String propertyName = (String) u.elementAt(5);

			if (forVS) {
				if (byName) {
					if (nameList.contains(propertyName)) {
						String t = (String) u.elementAt(3) + "|" + (String) u.elementAt(4);
						if (!w.contains(t)) {
							w.add(t);
						}
					}
				} else {
					if (!nameList.contains(propertyName)) {
						String t = (String) u.elementAt(3) + "|" + (String) u.elementAt(4);
						if (!w.contains(t)) {
							w.add(t);
						}
					}
				}
			} else {
				if (byName) {
					if (nameList.contains(propertyName)) {
						String t = (String) u.elementAt(0) + "|" + (String) u.elementAt(1);
						if (!w.contains(t)) {
							w.add(t);
						}
					}
				} else {
					if (!nameList.contains(propertyName)) {
						String t = (String) u.elementAt(0) + "|" + (String) u.elementAt(1);
						if (!w.contains(t)) {
							w.add(t);
						}
					}
				}
			}
		}
		return new SortUtils().quickSort(w);
	}

	public static void main(String[] args) {
		long ms = System.currentTimeMillis();
		String serviceUrl = args[0];
		String namedGraph = args[1];
		String username = args[2];
		String password = args[3];

		String term = "blue";
		String code = "C48333";
		String subset_code = "C54452";
		ValueSetSearchUtils utils = new ValueSetSearchUtils(serviceUrl, namedGraph, username, password);

		for (int i=0; i<ALGORITHMS.length; i++) {
			String algorithm = ALGORITHMS[i];
			Vector w = utils.search(namedGraph, term, algorithm, subset_code);
			Utils.dumpVector(algorithm + ", name - " + term, w);

			boolean byName = true;
			Vector w1 = utils.parseVSData(w, true, false);
			Utils.dumpVector("value set, " + algorithm + ", name - " + term, w1);
			Vector w2 = utils.parseVSData(w, false, false);
			Utils.dumpVector("value set, " + algorithm + ", property - " + term, w2);
		}

		Vector w = utils.searchByCode(namedGraph, code);
        Utils.dumpVector("exactMatch code - " + code, w);
        System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}

}

