import gov.nih.nci.evs.restapi.appl.*;
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

public class RoleGroupQuery {
    JSONUtils jsonUtils = null;
    HTTPUtils httpUtils = null;
    String named_graph = null;
    String prefixes = null;
    String serviceUrl = null;
    String restURL = null;
    String named_graph_id = ":NHC0";
    String BASE_URI = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl";
    String CONCEPTS_WITH_ROLE_GROUPS_FILE = "concepts_with_rolegroups.txt";
    String CODES_WITH_ROLE_GROUPS_FILE = "codes_with_rolegroups.txt";

    ParserUtils parser = new ParserUtils();
    HashMap nameVersion2NamedGraphMap = null;
    HashMap ontologyUri2LabelMap = null;
    String version = null;
    String username = null;
    String password = null;
    OWLSPARQLUtils owlSPARQLUtils = null;

	static String NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile; //"ThesaurusInferred_forTS.owl";
	static String PARENT_CHILD_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile; // "parent_child.txt";
	static String RESTRICTION_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.rolefile; //"roles.txt";
	static String AXIOM_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.axiomfile;
	static Vector ROLE_DATA = null;
	static {
		ROLE_DATA = Utils.readFile(RESTRICTION_FILE);
	}

    public RoleGroupQuery(String serviceUrl, String named_graph, String username, String password) {
		this.serviceUrl = serviceUrl;
    	this.named_graph = named_graph;
    	this.username = username;
    	this.password = password;
        this.owlSPARQLUtils = new OWLSPARQLUtils(serviceUrl, username, password);
        this.owlSPARQLUtils.set_named_graph(named_graph);
    }

    public OWLSPARQLUtils getOWLSPARQLUtils() {
		return this.owlSPARQLUtils;
	}


	public String construct_get_rolegroup(String named_graph, String code) {
		String prefixes = owlSPARQLUtils.getPrefixes();
		StringBuffer buf = new StringBuffer();
		buf.append(prefixes);
		buf.append("select distinct ?x_code ?x_label ?e1 ?i1 ?u1 ?i2 ?p_label ?p_code ?y_code ?y_label").append("\n");
		buf.append("from <" + named_graph + ">").append("\n");
		buf.append("where {").append("\n");
		buf.append("            ?x :NHC0 ?x_code .").append("\n");
		buf.append("            ?x rdfs:label ?x_label .").append("\n");
		buf.append("            ?x :NHC0 \"" + code + "\"^^xsd:string .").append("\n");
		buf.append("            ?x owl:equivalentClass ?e1 .").append("\n");
		buf.append("            ?e1 owl:intersectionOf ?i1 .").append("\n");
		buf.append("            ?i1 rdf:rest*/rdf:first ?u1 .").append("\n");
		buf.append("            ?u1 owl:unionOf ?u2 .").append("\n");
		buf.append("            ?u2 rdf:rest*/rdf:first ?i2 .").append("\n");
		buf.append("            ?i2 owl:intersectionOf ?i3 .").append("\n");
		buf.append("            ?i3 rdf:rest*/rdf:first ?rs .").append("\n");
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

	public Vector getRolegroup(String named_graph, String code) {
		String query = construct_get_rolegroup(named_graph, code);
		Vector v = owlSPARQLUtils.executeQuery(query);
		return v;
	}

	//C100051|Renal Cell Carcinoma Associated with Neuroblastoma|bnode_b241ba03_70d8_44a3_a30d_b4f48a5e6700_1335
	public Vector getConceptsWithRolegroup(String named_graph) {
		String filename = "equivalentClass_" + StringUtils.getToday() + ".txt";
		File f = new File(filename);
		Vector v = new Vector();
		if (!f.exists()) {
		    v = findConceptsWithEquivalentclass();
		    System.out.println("findConceptsWithEquivalentclass returns: " + v.size());
		} else {
			v = Utils.readFile(filename);
			System.out.println("Equivalentclass: " + v.size());
		}
		Vector w = new Vector();
		int knt = 0;
		long ms0 = System.currentTimeMillis();
		LogicalExpressionGenerator generator = new LogicalExpressionGenerator();

		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(1);

			HashMap dataMap = generator.getLogicalExpressionData(named_graph, code);
			String key1 = "E|I|O|U|O|R";
			String key2 = "E|I|O|U|O|I|O|R";
			Vector w1 = new Vector();
			Vector values_1 = (Vector) dataMap.get(key1);
			if (values_1 != null && values_1.size() > 0) {
				w1.addAll(values_1);
			}
			Vector values_2 = (Vector) dataMap.get(key2);
			if (values_2 != null && values_2.size() > 0) {
				w1.addAll(values_2);
			}
			if (w1 != null && w1.size() > 0) {
				w.addAll(w1);
				knt++;
				System.out.println("" + i + " out of " + v.size() + " completed. Found: " + knt);
				long ms = System.currentTimeMillis();
				System.out.println("Time elapsed (ms): " + (ms - ms0));
			}
		}
		long ms = System.currentTimeMillis();
		System.out.println("Total run time (ms): " + (ms - ms0));
		return w;
	}

    public Vector findConceptsWithEquivalentclass() {
		Vector v = owlSPARQLUtils.getEquivalentClass(named_graph);
		Utils.saveToFile("equivalentClass_" + StringUtils.getToday() + ".txt", v);
		return v;
    }

	public static Vector getCodes(String filename) {
		Vector v = Utils.readFile(filename);
		Vector codes = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(0);
			if (!codes.contains(code)) {
				codes.add(code);
			}
		}
		return codes;
	}

    public void debug(String filename) {
		String named_graph =  ConfigurationController.namedGraph;
		LogicalExpressionGenerator generator = new LogicalExpressionGenerator();

		Vector codes = getCodes(filename);
		HashMap dataMap = getLogicalExpressionData(codes);
		Iterator it = dataMap.keySet().iterator();
		int j = 0;
		while (it.hasNext()) {
			String code = (String) it.next();
			HashMap hmap = (HashMap) dataMap.get(code);
			Utils.dumpMultiValuedHashMap(code, hmap);

			System.out.println("Range of roles in role group:");
			String path = "E|I|O|U|O|I|O|R";
			Vector v = (Vector) hmap.get(path);
			String label = null;
			if (v != null && v.size() > 0) {
				for (int i=0; i<v.size(); i++) {
					String line = (String) v.elementAt(i);
					Vector u = StringUtils.parseData(line, '|');
					label = (String) u.elementAt(1);
					String roleName = (String) u.elementAt(u.size()-4);
					String roleRange = (String) generator.getRangeNameByRoleName(roleName);
					System.out.println(line + " (" + roleName + " range: " + roleRange + ")");
				}
			}

			String expression = generator.getLogicalExpression(named_graph, code);
			j++;
			System.out.println("\n(" + j + ") Logical expression of " + label + " (" + code + ")");
			System.out.println(expression);

			HashMap queryMap = generator.getQueries(named_graph, code);
			String query = (String) queryMap.get(path);
			System.out.println(query);
		}
	}

    public HashMap getLogicalExpressionData(Vector codes) {
		LogicalExpressionGenerator generator = new LogicalExpressionGenerator();
		HashMap dataMap = new HashMap();
		String named_graph =  ConfigurationController.namedGraph;
		for (int i=0; i<codes.size(); i++) {
			String code = (String) codes.elementAt(i);
        	HashMap hmap = generator.getLogicalExpressionData(named_graph, code);
        	dataMap.put(code, hmap);
		}
        return dataMap;
	}

	public static Vector run(Vector codes) {
		String named_graph =  ConfigurationController.namedGraph;
		LogicalExpressionGenerator generator = new LogicalExpressionGenerator();
		HashMap code2LabelMap = new LogicalExpressionGenerator().getCode2LabelMap();
		Vector w = new Vector();
		for (int i=0; i<codes.size(); i++) {
			int j = i+1;
			String code = (String) codes.elementAt(i);
			String label = (String) code2LabelMap.get(code);
			System.out.println("(" + j + ") Logical expression of " + label + " (" + code + ")");
			w.add("\n(" + j + ") Logical expression of " + label + " (" + code + ")");
			String expression = generator.getLogicalExpression(named_graph, code);
			System.out.println("\n(" + j + ") Logical expression of " + label + " (" + code + ")");
			w.add(expression);
			System.out.println(expression);
		}
		return w;
	}



    public static void getLogicalExpression(String filename) {
		Vector codes = getCodes(filename);
		Vector w = run(codes);
		Utils.saveToFile("expression_" + filename, w);
	}


	public String construct_get_roles(String named_graph, String code) {
        String prefixes = owlSPARQLUtils.getPrefixes();
        StringBuffer buf = new StringBuffer();
        buf.append(prefixes);
        buf.append("select distinct ?x_code ?x_label ?p_code ?p_label ?y_code ?y_label").append("\n");
        buf.append("{").append("\n");
		if (named_graph != null) {
			buf.append("graph <" + named_graph + ">").append("\n");
		}
        buf.append("	{").append("\n");
        buf.append("            ?x a owl:Class .").append("\n");
        buf.append("            ?x :NHC0 ?x_code .").append("\n");
        if (code != null) {
            buf.append("            ?x :NHC0 \"" + code + "\"^^xsd:string . ").append("\n");
	    }
        buf.append("            ?x rdfs:label ?x_label .").append("\n");
        buf.append("            ?x (rdfs:subClassOf|owl:equivalentClass|owl:unionOf/rdf:rest*/rdf:first|owl:intersectionOf/rdf:rest*/rdf:first)* ?rs .").append("\n");
        buf.append("            ?rs a owl:Restriction .").append("\n");
        buf.append("            ?rs owl:onProperty ?p .").append("\n");
        buf.append("            ?p :NHC0 ?p_code .").append("\n");
        buf.append("            ?p rdfs:label ?p_label .").append("\n");
        buf.append("            ?rs owl:someValuesFrom ?y .").append("\n");
        buf.append("            ?y :NHC0 ?y_code .").append("\n");
        buf.append("            ?y rdfs:label ?y_label .").append("\n");
        buf.append("	}").append("\n");
        buf.append("}").append("\n");
        return buf.toString();
	}

    public static Vector getRolesByOWL(String code) {
		Vector v = new Vector();
		for (int i=0; i<ROLE_DATA.size(); i++) {
			String line = (String) ROLE_DATA.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String src_code = (String) u.elementAt(1);
			if (src_code.compareTo(code) == 0) {
				v.add(line);
			}
		}
		return v;
	}

    public static void main(String[] args) {
		long ms = System.currentTimeMillis();
		String serviceUrl = ConfigurationController.serviceUrl;
		String named_graph =  ConfigurationController.namedGraph;
		String username =  ConfigurationController.username;
		String password =  ConfigurationController.password;
		//String code = args[0];
        RoleGroupQuery test = new RoleGroupQuery(serviceUrl, named_graph, username, password);
        /*
		Vector w = test.getRolegroup(named_graph, code);
		Utils.saveToFile("results_rolegroup_query.txt", w);
		*/

		/*
		Vector w = test.getConceptsWithRolegroup(named_graph);
		String filename = "concepts_with_rolegroups.txt";
		Utils.saveToFile(filename, w);
		*/

        /*
		String filename = args[0];//"concepts_with_rolegroups.txt";
		if (args.length > 0) {
			filename = args[0];
		}
        test.getLogicalExpression(filename);
        */

        /*
        String filename = args[0];
        test.debug(filename);

        String query = test.construct_get_roles(named_graph, "C9110");
        System.out.println(query);
        */
        String code = "C9110";
        Vector v = getRolesByOWL(code);
        Utils.dumpVector(code, v);
    }

}
