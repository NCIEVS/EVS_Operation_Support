package gov.nih.nci.evs.restapi.appl;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.bean.*;
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


public class LogicalExpressionGenerator {
	HashMap rangeHashMap = null;
	gov.nih.nci.evs.restapi.appl.LogicalExpression le = null;
	HashMap code2LabelMap = null;
	HashMap roleName2RangeNameMap = null;
	HashMap roleCode2RangeNameMap = null;
	Vector PATHS = null;
	HashMap range2ExpressionMap = null;
	LogicalExpressionFormatter formatter = null;

	private void initialize() {
		String serviceUrl = ConfigurationController.serviceUrl;
		String named_graph = ConfigurationController.namedGraph;
		String username = ConfigurationController.username;
		String password = ConfigurationController.password;

		try {
			code2LabelMap = generateLabelHashMap(named_graph);//loadOrGenerateLabelHashMap(named_graph);
			le = new gov.nih.nci.evs.restapi.appl.LogicalExpression(serviceUrl, named_graph, username, password);
			roleCode2RangeNameMap = le.getRoleCode2RangeNameMap();
			roleName2RangeNameMap = le.getRoleName2RangeNameMap();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
        PATHS = new Vector();
        PATHS.add("E|I|O|C");
        PATHS.add("E|I|O|R");
        PATHS.add("E|I|O|U|O|R");
        PATHS.add("E|I|O|U|O|I|O|R");

        formatter = new LogicalExpressionFormatter(roleCode2RangeNameMap, roleName2RangeNameMap);
        //formatter.setRangeMaps(roleName2RangeNameMap, roleCode2RangeNameMap);
        //formatter.setpaths(PATHS);
	}

    public LogicalExpressionGenerator() {
		initialize();
		range2ExpressionMap = new HashMap();
	}

	public String getLabel(String code) {
		return (String) code2LabelMap.get(code);
	}

    public HashMap getQueries(String named_graph, String code) {
		return le.getQueries(named_graph, code);
	}

    public void dumpLogicalExpressionDataMap(HashMap hmap) {
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			System.out.println(key);
		}
	}

	public HashMap getCode2LabelMap() {
		return code2LabelMap;
	}


    public String getRangeNameByRoleCode(String roleCode) {
		return (String) roleCode2RangeNameMap.get(roleCode);
	}

    public String getRangeNameByRoleName(String roleName) {
		return (String) roleName2RangeNameMap.get(roleName);
	}


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public HashMap getLogicalExpressionData(String named_graph, String code) {
        HashMap hmap = le.getLogicalExpressionData(named_graph, code);
        return hmap;
	}

    public String getLogicalExpression(String named_graph, String code) {
        //HashMap hmap = getLogicalExpressionData(named_graph, code);
        String expression = formatter.getLogialExpression(le, named_graph, code);
        return expression;
	}

	public static String construct_get_label(String prefixes, String named_graph) {
        StringBuffer buf = new StringBuffer();
        buf.append(prefixes);
        buf.append("select distinct ?x_label ?x_code").append("\n");
        buf.append("from <" + named_graph + ">").append("\n");
        buf.append("where  { ").append("\n");
        buf.append("                ?x a owl:Class .").append("\n");
        buf.append("                ?x :NHC0 ?x_code .").append("\n");
        buf.append("                ?x rdfs:label ?x_label .").append("\n");
        buf.append("").append("\n");
        buf.append("}").append("\n");
        buf.append("order by ?x_label").append("\n");
        buf.append("").append("\n");
        return buf.toString();
	}

    public static HashMap generateLabelHashMap(String named_graph) {
		String filename = "labels.txt";
		HashMap hmap = null;
		Vector v = null;

		String serviceUrl = ConfigurationController.serviceUrl;
		String namedGraph = ConfigurationController.namedGraph;
		String username = ConfigurationController.username;
		String password = ConfigurationController.password;
		OWLSPARQLUtils owlSPARQLUtils = new OWLSPARQLUtils(serviceUrl, username, password);
		owlSPARQLUtils.set_named_graph(namedGraph);
		String query = construct_get_label(owlSPARQLUtils.getPrefixes(), namedGraph);
		v = owlSPARQLUtils.executeQuery(query);
		if (v == null) return null;
		Utils.saveToFile(filename, v);
		return createHashmap(v,1, 0, '|');
	}

    public HashMap loadOrGenerateLabelHashMap(String named_graph) {
		String filename = "labels.txt";
		File file = new File(filename);
		HashMap hmap = null;
		Vector v = null;
		if (!file.exists()) {
			String serviceUrl = ConfigurationController.serviceUrl;
			String namedGraph = ConfigurationController.namedGraph;
			String username = ConfigurationController.username;
			String password = ConfigurationController.password;
			OWLSPARQLUtils owlSPARQLUtils = new OWLSPARQLUtils(serviceUrl, username, password);
			owlSPARQLUtils.set_named_graph(namedGraph);
			String query = construct_get_label(owlSPARQLUtils.getPrefixes(), namedGraph);
			v = owlSPARQLUtils.executeQuery(query);
			if (v == null) return null;
			Utils.saveToFile(filename, v);

		} else {
			v = Utils.readFile(filename);
		}
		return createHashmap(v,1, 0, '|');
	}

	public static HashMap createHashmap(Vector v, int keyCol, int valueCol, char delim) {
		HashMap hmap = new HashMap();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, delim);
			String key = (String) u.elementAt(keyCol);
			String value = (String) u.elementAt(valueCol);
			Vector w = new Vector();
			hmap.put(key, value);
	 	}
		return hmap;
	}

	public static Vector extract_column_data(String filename, int col, char delim) {
		Vector v = Utils.readFile(filename);
		Vector w = new Vector();
		HashSet hset = new HashSet();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, delim);
			String value = (String) u.elementAt(col);
			if (!hset.contains(value)) {
				hset.add(value);
				w.add(value);
			}
		}
		return w;
	}


    public static Vector removeDuplicats(Vector v) {
		return removeDuplicats(v, false);
	}

	public static Vector removeDuplicats(Vector v, boolean containsHeader) {
		Vector w = new Vector();
		int istart = 0;
		if (containsHeader) {
			istart = 1;
		}
		for (int i=istart; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (!w.contains(line)) {
				w.add(line);
			}
		}
		return w;
	}

	public Vector run(Vector codes) {
		String named_graph = ConfigurationController.namedGraph;
		Vector w = new Vector();
		for (int i=0; i<codes.size(); i++) {
			int j = i+1;
			String code = (String) codes.elementAt(i);
			String label = (String) code2LabelMap.get(code);
			System.out.println("(" + j + ") Logical expression of " + label + " (" + code + ")");
			w.add("\n(" + j + ") Logical expression of " + label + " (" + code + ")");
			String expression = getLogicalExpression(named_graph, code);
			System.out.println("\n(" + j + ") Logical expression of " + label + " (" + code + ")");
			w.add(expression);
			System.out.println(expression);
		}
		return w;
	}


	public void run(String code) {
		String named_graph = ConfigurationController.namedGraph;
		Vector w = new Vector();
		String label = (String) code2LabelMap.get(code);
		Vector output_vec = new Vector();
		output_vec.add("Logical expression of " + label + " (" + code + ")");
		String expression = getLogicalExpression(named_graph, code);
		output_vec.add(expression);
		//add version (Source; NCI Thesaurus, version )
		w.addAll(output_vec);
		String outputfile = code + ".txt";
		Utils.saveToFile(outputfile, w);
	}


	public static void main(String[] args) {
		long ms = System.currentTimeMillis();
		String filename = args[0];
        Vector codes = Utils.readFile(filename);
        System.out.println("codes: " + codes.size());
		String firstLn = (String) codes.elementAt(0);
		if (firstLn.indexOf("\t") != -1) {
			codes = extract_column_data(filename, 5, '\t');
		} else if (firstLn.indexOf("|") != -1) {
			codes = extract_column_data(filename, 0, '|');
		}
		codes = removeDuplicats(codes, false);
        LogicalExpressionGenerator test = new LogicalExpressionGenerator();
        Vector w = test.run(codes);
        int n = filename.lastIndexOf(".");
        String name = filename.substring(0, n);
        Utils.saveToFile("logical_expression_" + name + "_" + StringUtils.getToday() + ".txt", w);
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}
}

