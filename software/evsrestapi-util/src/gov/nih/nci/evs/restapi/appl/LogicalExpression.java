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
public class LogicalExpression {
	OWLSPARQLUtils owlSPARQLUtils = null;
	String named_graph = null;
	String prefixes = null;
	String serviceUrl = null;
	HashMap nameVersion2NamedGraphMap = null;
	HashMap ontologyUri2LabelMap = null;
	String version = null;
	String username = null;
	String password = null;
    HashMap roleCode2RangeNameMap = null;
    HashMap roleName2RangeNameMap = null;
    HashMap roleCode2RoleNameMap = null;
    static String RANGE_UNSPECIFIED = "[Range Unspecified]";

    public LogicalExpression(String serviceUrl, String named_graph, String username, String password) {
		this.serviceUrl = serviceUrl;
    	this.named_graph = named_graph;
    	this.username = username;
    	this.password = password;
        this.owlSPARQLUtils = new OWLSPARQLUtils(serviceUrl, username, password);
        this.owlSPARQLUtils.set_named_graph(named_graph);
        constructRangeHashMap(named_graph);
    }

    public HashMap getRoleCode2RangeNameMap() {
		return roleCode2RangeNameMap;
	}

    public HashMap getRoleName2RangeNameMap() {
		return roleName2RangeNameMap;
	}

    public HashMap getRoleCode2RoleNameMap() {
		return roleCode2RoleNameMap;
	}

    public OWLSPARQLUtils getOWLSPARQLUtils() {
		return this.owlSPARQLUtils;
	}

	public Vector executeQuery(String query) {
        Vector v = owlSPARQLUtils.executeQuery(query);
        if (v == null) return null;
        if (v.size() == 0) return v;
        return new SortUtils().quickSort(v);
	}

	public String construct_get_label(String named_graph) {
		String prefixes = owlSPARQLUtils.getPrefixes();
		StringBuffer buf = new StringBuffer();
		buf.append(prefixes);
		buf.append("select distinct ?x_code ?x_label").append("\n");
		buf.append("from <" + named_graph + ">").append("\n");
		buf.append("where  { ").append("\n");
		buf.append("                ?x a owl:Class .").append("\n");
		buf.append("                ?x :NHC0 ?x_code .").append("\n");
		buf.append("                ?x rdfs:label ?x_label .").append("\n");
		buf.append("}").append("\n");
		buf.append("").append("\n");
		return buf.toString();
	}


	public String construct_get_label_by_code(String named_graph, String code) {
		String prefixes = owlSPARQLUtils.getPrefixes();
		StringBuffer buf = new StringBuffer();
		buf.append(prefixes);
		buf.append("select distinct ?x_code ?x_label").append("\n");
		buf.append("from <" + named_graph + ">").append("\n");
		buf.append("where  { ").append("\n");
		buf.append("                ?x a owl:Class .").append("\n");
		buf.append("                ?x :NHC0 ?x_code .").append("\n");
		buf.append("                ?x :NHC0 \"" + code + "\"^^xsd:string .").append("\n");
		buf.append("                ?x rdfs:label ?x_label .").append("\n");
		buf.append("}").append("\n");
		buf.append("").append("\n");
		return buf.toString();
	}


	public String getLabelByCode(String named_graph, String code) {
		String query = construct_get_label_by_code(named_graph, code);
		Vector v = owlSPARQLUtils.executeQuery(query);
		if (v == null) return null;
		if (v.size() == 0) return null;
		String line = (String) v.elementAt(0);
		Vector u = StringUtils.parseData(line, '|');
		return (String) u.elementAt(1);
	}


	public void constructRangeHashMap(String named_graph) {
        roleCode2RangeNameMap = new HashMap();
        roleName2RangeNameMap = new HashMap();
        roleCode2RoleNameMap = new HashMap();
		Vector v = null;
		try {
			v = getRange(named_graph);
			if (v != null) {
				for (int i=0; i<v.size(); i++) {
					String line = (String) v.elementAt(i);
					Vector u = StringUtils.parseData(line, '|');
					roleCode2RangeNameMap.put((String) u.elementAt(0), (String) u.elementAt(3));
					roleName2RangeNameMap.put((String) u.elementAt(1), (String) u.elementAt(3));
					roleCode2RoleNameMap.put((String) u.elementAt(0), (String) u.elementAt(1));
				}
			}
			v = getSubproperty(named_graph);
			if (v != null) {
				for (int i=0; i<v.size(); i++) {
					String line = (String) v.elementAt(i);
					Vector u = StringUtils.parseData(line, '|');
					//buf.append("select distinct ?p1_label ?p1_code ?p2_label ?p2_code").append("\n");
					String parentLabel = (String) u.elementAt(0);
					String parentCode = (String) u.elementAt(1);
					String childLabel = (String) u.elementAt(2);
					String childCode = (String) u.elementAt(3);
					String rangeName = (String) roleCode2RangeNameMap.get(parentCode);
					roleCode2RangeNameMap.put(childCode, rangeName);
					roleName2RangeNameMap.put(childLabel, rangeName);
					roleCode2RoleNameMap.put(childCode, rangeName);
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public String construct_get_range(String named_graph) {
		String prefixes = owlSPARQLUtils.getPrefixes();
		StringBuffer buf = new StringBuffer();
		buf.append(prefixes);
		buf.append("select distinct ?r_code ?r_label ?r_range_code ?r_range_label").append("\n");
		buf.append("from <" + named_graph + ">").append("\n");
		buf.append("where  { ").append("\n");
		buf.append("            ?r a owl:ObjectProperty .").append("\n");
		buf.append("            ?r rdfs:label ?r_label .").append("\n");
		buf.append("            ?r :NHC0 ?r_code .").append("\n");
		buf.append("            ?r rdfs:range ?r_range .").append("\n");
		buf.append("            ?r_range :NHC0 ?r_range_code .").append("\n");
		buf.append("            ?r_range rdfs:label ?r_range_label .").append("\n");
		buf.append("}").append("\n");
		return buf.toString();
	}

	public Vector getRange(String named_graph) {
		String query = construct_get_range(named_graph);
		Vector v = executeQuery(query);
		if (v == null) return null;
		if (v.size() == 0) return v;
		return new SortUtils().quickSort(v);
	}

	public String construct_get_subproperty(String named_graph) {
		String prefixes = owlSPARQLUtils.getPrefixes();
		StringBuffer buf = new StringBuffer();
		buf.append(prefixes);
		buf.append("select distinct ?p1_label ?p1_code ?p2_label ?p2_code").append("\n");
		buf.append("from <" + named_graph + ">").append("\n");
		buf.append("where  { ").append("\n");
		buf.append("      ?p1 :NHC0 ?p1_code .   ").append("\n");
		buf.append("      ?p1 rdfs:label ?p1_label . ").append("\n");
		buf.append("      ?p2 :NHC0 ?p2_code .   ").append("\n");
		buf.append("      ?p2 rdfs:label ?p2_label . ").append("\n");
		buf.append("      ?p2 rdfs:subPropertyOf ?p1 .").append("\n");
		buf.append("}").append("\n");
		buf.append("").append("\n");
		return buf.toString();
	}

	public Vector getSubproperty(String named_graph) {
		String query = construct_get_subproperty(named_graph);
		Vector v = owlSPARQLUtils.executeQuery(query);
		if (v == null) return null;
		if (v.size() == 0) return v;
		return new SortUtils().quickSort(v);
	}

	public String construct_get_parents(String named_graph, String code) {
		String prefixes = owlSPARQLUtils.getPrefixes();
		StringBuffer buf = new StringBuffer();
		buf.append(prefixes);
		buf.append("select distinct ?y_code ?y_label").append("\n");
		buf.append("from <" + named_graph + ">").append("\n");
		buf.append("where {").append("\n");
		buf.append("            ?x :NHC0 ?x_code .").append("\n");
		buf.append("            ?x rdfs:label ?x_label .").append("\n");
		buf.append("            ?x :NHC0 \"" + code + "\"^^xsd:string .").append("\n");
		buf.append("            ?x owl:equivalentClass ?e .").append("\n");
		buf.append("	        ?e (owl:intersectionOf/rdf:rest*/rdf:first) ?y .").append("\n");
		buf.append("            ?y :NHC0 ?y_code .").append("\n");
		buf.append("            ?y rdfs:label ?y_label .").append("\n");
		buf.append("}").append("\n");
		return buf.toString();
	}


	public Vector getParents(String named_graph, String code) {
		String query = construct_get_parents(named_graph, code);
		Vector v = owlSPARQLUtils.executeQuery(query);
		if (v == null) return null;
		if (v.size() == 0) return v;
		return new SortUtils().quickSort(v);
	}

	public String construct_get_simple_roles(String named_graph, String code) {
		String prefixes = owlSPARQLUtils.getPrefixes();
		StringBuffer buf = new StringBuffer();
		buf.append(prefixes);
		buf.append("select distinct ?x_code ?x_label ?e1 ?u1 ?i1 ?rs ?p_label ?p_code ?y_code ?y_label").append("\n");
		buf.append("from <" + named_graph + ">").append("\n");
		buf.append("where {").append("\n");
		buf.append("            ?x :NHC0 ?x_code .").append("\n");
		buf.append("            ?x rdfs:label ?x_label .").append("\n");
		buf.append("            ?x :NHC0 \"" + code + "\"^^xsd:string .").append("\n");
		buf.append("            ?x owl:equivalentClass ?e1 .").append("\n");
		buf.append("            ?e1 (owl:intersectionOf/rdf:rest*/rdf:first) ?rs .").append("\n");
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


	public Vector getSimpleRoles(String named_graph, String code) {
		String query = construct_get_simple_roles(named_graph, code);
		Vector v = owlSPARQLUtils.executeQuery(query);
		if (v == null) return null;
		if (v.size() == 0) return v;
		return new SortUtils().quickSort(v);
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
		buf.append("            ?x :NHC0 \"" + code + "\"^^xsd:string .").append("\n");
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
		buf.append("            ?x :NHC0 \"" + code + "\"^^xsd:string .").append("\n");
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


	public HashMap getLogicalExpressionData(String named_graph, String code) {
		HashMap hmap = new HashMap();

        Vector v = getParents(named_graph, code);
        hmap.put("PARENT", v);

        v = getSimpleRoles(named_graph, code);
        if (v != null && v.size() > 0) {
        	hmap.put("ROLE", v);
		}

        v = getRoleUnions(named_graph, code);
        if (v != null && v.size() > 0) {
        	hmap.put("ROLE UNION", v);
		}

        v = getRoleGroups(named_graph, code);
        if (v != null && v.size() > 0) {
        	hmap.put("ROLE GROUP", v);
		}
        return hmap;
	}


    public String generateRoleGroupExpression(String role_group_line) {
		StringBuffer buf = new StringBuffer();
		buf.append("\n\tRole Group").append("\n");
		Vector role_collection_vec = StringUtils.parseData(role_group_line, '$');
		for (int i=0; i<role_collection_vec.size()-1; i++) { // last element is range
			String role_collection = (String) role_collection_vec.elementAt(i);
			StringBuffer buf2 = new StringBuffer();
			Vector roles = StringUtils.parseData(role_collection, '|');
			for (int k=0; k<roles.size(); k++) {
				buf2.append("\t\t").append((String) roles.elementAt(k)).append("\n");
			}
			if (i<role_collection_vec.size()-2) {
				buf2.append("\t").append("or").append("\n");
			}
			String s = buf2.toString();
			buf.append(s);
		}
		return buf.toString();
	}

	public Vector findRangesParticipatingInRoleUnionOrGroup(String role_group_line) {
		//ROLE GROUP --> Disease_May_Have_Abnormal_Cell	Neoplastic B-Immunoblast (C37010)|Disease_May_Have_Associated_Disease	Immunoblastic Lymphoma (C3461)|Disease_May_Have_Abnormal_Cell	Neoplastic Centroblast (C37014)|Disease_May_Have_Associated_Disease	Centroblastic Lymphoma (C4074)|Disease, Disorder or Finding
        Vector roles = StringUtils.parseData(role_group_line, '|');
		Vector w = new Vector();
		for (int i=0; i<roles.size(); i++) {
			String role = (String) roles.elementAt(i);
			Vector u = StringUtils.parseData(role, '\t');
			String roleName = (String) u.elementAt(0);
			String range = (String) roleName2RangeNameMap.get(roleName);
			if (!w.contains(range)) {
				w.add(range);
			}
		}
		return w;
	}

	public HashMap generateRange2RoleGroupExpressionMap(Vector role_group_vec) {
		if (role_group_vec == null || role_group_vec.size() == 0) {
			System.out.println("INFO: role_group_vec == null || role_group_vec.size() == 0 return null\n");
			return null;
		}
		HashMap expressionMap = new HashMap();
		Vector w = new Vector();
		for (int i=0; i<role_group_vec.size(); i++) {
			String role_group_line = (String) role_group_vec.elementAt(i);
			String expression = generateRoleGroupExpression(role_group_line);
			Vector ranges = findRangesParticipatingInRoleUnionOrGroup(role_group_line);
			String range = (String) ranges.elementAt(0);
			if (ranges.size() > 1) {
				range = RANGE_UNSPECIFIED;
			}
			w = new Vector();
			if (expressionMap.containsKey(range)) {
				w = (Vector) expressionMap.get(range);
			}
			w.add(expression);
			expressionMap.put(range, w);
		}
		return expressionMap;
	}


    public String generateRoleUnionExpression(String role_union_line) {
		StringBuffer buf2 = new StringBuffer();
		buf2.append("\n");
		Vector role_union_vec = StringUtils.parseData(role_union_line, '|');
		for (int k=0; k<role_union_vec.size()-1; k++) {
			buf2.append("\t\t").append((String) role_union_vec.elementAt(k)).append("\n");
			if (k<role_union_vec.size() - 2) {
				buf2.append("\t\t").append("or").append("\n");
			}
		}
		return buf2.toString();
	}

	public HashMap generateRange2RoleUnionExpressionMap(Vector role_union_vec) {
		if (role_union_vec == null || role_union_vec.size() == 0) {
			System.out.println("INFO: role_union_vec == null || role_union_vec.size() == 0 return null\n");
			return null;
		}
		HashMap expressionMap = new HashMap();
		Vector w = new Vector();
		for (int i=0; i<role_union_vec.size(); i++) {
			String role_union_line = (String) role_union_vec.elementAt(i);
			String expression = generateRoleUnionExpression(role_union_line);
			Vector ranges = findRangesParticipatingInRoleUnionOrGroup(role_union_line);
			String range = (String) ranges.elementAt(0);
			if (ranges.size() > 1) {
				range = RANGE_UNSPECIFIED;
			}
			w = new Vector();
			if (expressionMap.containsKey(range)) {
				w = (Vector) expressionMap.get(range);
			}
			w.add(expression);
			expressionMap.put(range, w);
		}
		return expressionMap;
	}

    static String range2RolesHashMap2Expression(HashMap range2RolesHashMap) {
		StringBuffer buf = new StringBuffer();
		Vector key_vec = new Vector();
		Iterator it = range2RolesHashMap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			key_vec.add(key);
		}
		key_vec = new SortUtils().quickSort(key_vec);
		for (int i=0; i<key_vec.size(); i++) {
			String key = (String) key_vec.elementAt(i);
			if (key != null) {
				buf.append("\n" + key).append("\n");
				Vector values = (Vector) range2RolesHashMap.get(key);
				//Roles
				int knt = 0;
				for (int j=0; j<values.size(); j++) {
					String value = (String) values.elementAt(j);
					Vector u = StringUtils.parseData(value, '|');
					value = (String) u.elementAt(0);
					if (value.indexOf("Role Group") == -1) {
						knt++;
						buf.append("\t\t" + value).append("\n");
					}
				}
				//Role groups
				for (int j=0; j<values.size(); j++) {
					String value = (String) values.elementAt(j);
					Vector u = StringUtils.parseData(value, '|');
					value = (String) u.elementAt(0);
					if (value.indexOf("Role Group") != -1) {
						buf.append("\t" + value).append("\n");
					}
				}
			}
		}
		return buf.toString();
	}



    public void run(String named_graph, String code) {
		boolean debug = true;
		run(named_graph, code, debug);
	}


    public HashMap formatLogicalExpression(HashMap hmap) {
		HashMap map = new HashMap();
		map.put("PARENT", new Vector());
		map.put("ROLE", new Vector());
		map.put("ROLE UNION", new Vector());
		map.put("ROLE GROUP", new Vector());

		Vector w = new Vector();
		Vector v = (Vector) hmap.get("PARENT");

        if (v == null) {
			System.out.println("ERROR: parent == null)");
			return null;
		}
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(0);
            String label = (String) u.elementAt(1);
            w.add(label + " (" + code + ")");
		}
		w = new SortUtils().quickSort(w);
		map.put("PARENT", w);

        //buf.append("select distinct ?x_code ?x_label ?e1 ?u1 ?i1 ?rs ?p_label ?p_code ?y_code ?y_label").append("\n");
		w = new Vector();
		v = (Vector) hmap.get("ROLE");

		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String roleCode = (String) u.elementAt(5);
			String roleTargetCode = (String) u.elementAt(6);
			String roleName = (String) u.elementAt(4);
			String roleTargetName = (String) u.elementAt(7);
			String range = (String) roleCode2RangeNameMap.get(roleCode);
			w.add(roleName + "\t\t" + roleTargetName + " (" + roleTargetCode + ")" + "|" + range);
		}
		w = new SortUtils().quickSort(w);
		map.put("ROLE", w);

        //buf.append("select distinct ?x_code ?x_label ?c1 ?rs ?p_label ?p_code ?y_code ?y_label").append("\n");
		w = new Vector();
		v = (Vector) hmap.get("ROLE UNION");

		if (v != null && v.size() > 0) {
			HashMap roleUnionId2RolesHashMap = new HashMap();
			String range = null;
			HashMap id2RangeMap = new HashMap();
			for (int i=0; i<v.size(); i++) {
				String line = (String) v.elementAt(i);
				Vector u = StringUtils.parseData(line, '|');
				String id = (String) u.elementAt(2);
				Vector w2 = new Vector();
				if (roleUnionId2RolesHashMap.containsKey(id)) {
					w2 = (Vector) roleUnionId2RolesHashMap.get(id);
				}
				String roleName = (String) u.elementAt(4);
				String roleCode = (String) u.elementAt(5);
				String roleTargetCode = (String) u.elementAt(6);
				String roleTargetName = (String) u.elementAt(7);
				range = (String) roleCode2RangeNameMap.get(roleCode);
				id2RangeMap.put(id, range);
				w2.add(roleName + "\t\t" + roleTargetName + " (" + roleTargetCode + ")");
				roleUnionId2RolesHashMap.put(id, w2);
			}

			Iterator it = roleUnionId2RolesHashMap.keySet().iterator();
			while (it.hasNext()) {
				String id = (String) it.next();
				StringBuffer buf = new StringBuffer();
				Vector w2 = (Vector) roleUnionId2RolesHashMap.get(id);
				for (int i=0; i<w2.size(); i++) {
					String displayLabel = (String) w2.elementAt(i);
					buf.append(displayLabel).append("|");
				}
				buf.append((String) id2RangeMap.get(id));
				String s = buf.toString();
				Vector values = (Vector) map.get("ROLE UNION");
				values.add(s);
				map.put("ROLE UNION", values);
			}
		}

		w = new Vector();
		v = (Vector) hmap.get("ROLE GROUP");
		if (v != null && v.size() > 0) {
			HashMap roleGroupId2RolesHashMap = new HashMap();
			for (int i=0; i<v.size(); i++) {
				String line = (String) v.elementAt(i);
				Vector u = StringUtils.parseData(line, '|');
				String rg_id = (String) u.elementAt(2);
				String collection_id = (String) u.elementAt(2) + "|" + (String) u.elementAt(3);
				HashMap collection_map = new HashMap();
				if (roleGroupId2RolesHashMap.containsKey(rg_id)) {
					collection_map = (HashMap) roleGroupId2RolesHashMap.get(rg_id);
				}
				Vector role_vec = new Vector();
				if (collection_map.containsKey(collection_id)) {
					role_vec = (Vector) collection_map.get(collection_id);
				}

				String roleName = (String) u.elementAt(5);
				String roleCode = (String) u.elementAt(6);
				String roleTargetCode = (String) u.elementAt(7);
				String roleTargetName = (String) u.elementAt(8);
				String s = roleName + "\t" + roleTargetName + " (" + roleTargetCode + ")";
				role_vec.add(s);
				collection_map.put(collection_id, role_vec);
				roleGroupId2RolesHashMap.put(rg_id, collection_map);
			}

			Iterator it = roleGroupId2RolesHashMap.keySet().iterator(); //rg_id
			while (it.hasNext()) {
				String rg_id = (String) it.next();
				HashMap collection_map = (HashMap) roleGroupId2RolesHashMap.get(rg_id);
				HashSet rangeSet = new HashSet();
                StringBuffer b = new StringBuffer();
				Iterator it2 = collection_map.keySet().iterator(); // collection_id
				while (it2.hasNext()) {
					String collection_id = (String) it2.next();
					StringBuffer buf = new StringBuffer();
					Vector w2 = (Vector) collection_map.get(collection_id);
					for (int i=0; i<w2.size(); i++) {
						String displayLabel = (String) w2.elementAt(i);
						Vector u = StringUtils.parseData(displayLabel, '\t');
						String roleName = (String) u.elementAt(0);
				        String range = (String) roleName2RangeNameMap.get(roleName);
				        rangeSet.add(range);
						buf.append(displayLabel).append("|");
					}
					String s = buf.toString();
					s = s.trim();
					if (s.endsWith("|")) {
						s = s.substring(0, s.length()-1);
					}
					b.append(s).append("$");
				}
				String s2 = b.toString();
				if (s2.endsWith("$")) {
					s2 = s2.substring(0, s2.length()-1);
				}
				if (rangeSet.size() == 1) {
					Iterator it3 = rangeSet.iterator();
					String rg_range = (String) it3.next();
					s2 = s2 + "$" + rg_range;
				} else {
					s2 = s2 + "$" + RANGE_UNSPECIFIED;
				}
				Vector values = (Vector) map.get("ROLE GROUP");
				values.add(s2);
				map.put("ROLE GROUP", values);
			}
		}
		return map;
	}

    public void run(String named_graph, String code, boolean debug) {
        HashMap hmap = getLogicalExpressionData(named_graph, code);
        if (debug) {
			Utils.dumpMultiValuedHashMap("Raw Logical Expression Data", hmap);
		}
		hmap = formatLogicalExpression(hmap);
		if (debug) {
		    Utils.dumpMultiValuedHashMap("formatted LogicalExpressionData", hmap);
		}
		gov.nih.nci.evs.restapi.bean.LogicalExpression le = constructLogicalExpression(code, hmap);
		System.out.println(le.toJson());
		String expression = le.toString();
		System.out.println("\n" + expression);
    }

    public gov.nih.nci.evs.restapi.bean.LogicalExpression getLogicalExpression(String named_graph, String code, boolean debug) {
        HashMap hmap = getLogicalExpressionData(named_graph, code);
		hmap = formatLogicalExpression(hmap);
		gov.nih.nci.evs.restapi.bean.LogicalExpression le = constructLogicalExpression(code, hmap);
		return le;
	}

	public String logicalExpressionElement2String(LogicalExpressionElement element) {
        StringBuffer buf = new StringBuffer();
        buf.append(element.getRange()).append("\n");
        List<Restriction> roles = element.getRoles();
        for (int i=0; i<roles.size(); i++) {
			Restriction r = (Restriction) roles.get(i);
			buf.append("\t\t" + r.toString()).append("\n");
		}

        List<RoleUnion> roleUnions = element.getRoleUnions();
        if (roleUnions != null && roleUnions.size() > 0) {
			buf.append("\n");
			for (int i=0; i<roleUnions.size(); i++) {
				RoleUnion ru = (RoleUnion) roleUnions.get(i);
				roles = ru.getRoles();
				for (int j=0; j<roles.size(); j++) {
					Restriction r = (Restriction) roles.get(j);
					buf.append("\t\t" + r.toString()).append("\n");
					if (j<roles.size()-1) {
						buf.append("\tor").append("\n");
					}
				}
				buf.append("\n");
			}
		}

        List<RoleGroup> roleGroups = element.getRoleGroups();
        if (roleGroups != null && roleGroups.size() > 0) {
			buf.append("\n");
			for (int i=0; i<roleGroups.size(); i++) {
				RoleGroup rg = (RoleGroup) roleGroups.get(i);
				List roleSets = rg.getRoleSets();
				buf.append("\tRole Group").append("\n");
				for (int j=0; j<roleSets.size(); j++) {
					RoleSet rs = (RoleSet) roleSets.get(j);
					roles = rs.getRoles();
					for (int k=0; k<roles.size()/2; k++) {
						Restriction r1 = (Restriction) roles.get(k*2);
						Restriction r2 = (Restriction) roles.get(k*2+1);
						buf.append("\t\t" + r1.toString()).append("\n");
						buf.append("\t\t" + r2.toString()).append("\n");
						if (k< roles.size()/2-1) {
							buf.append("\tor").append("\n");
						}
					}
					buf.append("\n");
				}
			}
		}
		return buf.toString();
	}

	public HashMap getQueries(String named_graph, String code) {
	    HashMap hmap = new HashMap();
	    hmap.put("PARENT", construct_get_parents(named_graph, code));
	    hmap.put("ROLE", construct_get_simple_roles(named_graph, code));
	    hmap.put("ROLE UNION", construct_get_role_unions(named_graph, code));
	    hmap.put("ROLE GROUP", construct_get_role_groups(named_graph, code));
	    hmap.put("RANGE", construct_get_range(named_graph));
	    hmap.put("SUBPROPERTY", construct_get_subproperty(named_graph));
	    return hmap;
	}

    public String getLogicalExpression(HashMap hmap) {
// Parents:
        StringBuffer buf = new StringBuffer();
        Vector parents = (Vector) hmap.get("PARENT");
        Vector parent_vec = new Vector();
        Vector w = new Vector();

        if (parents != null && parents.size() > 0) {
			buf.append("Parent(s)").append("\n");
			for (int i=0; i<parents.size(); i++) {
				String line = (String) parents.elementAt(i);
				parent_vec.add("\t\t" + line);
			}
		}
		parent_vec = new SortUtils().quickSort(parent_vec);
		for (int i=0; i<parent_vec.size(); i++) {
			String line = (String) parent_vec.elementAt(i);
			buf.append(line).append("\n");
		}

// Role:
        HashMap range2RolesHashMap = new HashMap();
        Vector v = (Vector) hmap.get("ROLE");
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String roleDisplayLabel = (String) u.elementAt(0);
			String range = (String) u.elementAt(1);
			w = new Vector();
			if (range2RolesHashMap.containsKey(range)) {
				w = (Vector) range2RolesHashMap.get(range);
			}
			w.add(line);
			range2RolesHashMap.put(range, w);
		}

// ROLE UNION:
		Vector role_unions = (Vector) hmap.get("ROLE UNION");
        HashMap range2RoleUnionExpressionMap = null;
        ////////////////////////////////////////////////////////////////////////////////////
        range2RoleUnionExpressionMap = generateRange2RoleUnionExpressionMap(role_unions);
        ////////////////////////////////////////////////////////////////////////////////////
        if (range2RoleUnionExpressionMap != null) {
            Iterator it = range2RoleUnionExpressionMap.keySet().iterator();
            while (it.hasNext()) {
				String range = (String) it.next();
				Vector roleExpression_vec = (Vector) range2RoleUnionExpressionMap.get(range);
				w = new Vector();
				if (range2RolesHashMap.containsKey(range)) {
					w = (Vector) range2RolesHashMap.get(range);
				}
				w.addAll(roleExpression_vec);
			    range2RolesHashMap.put(range, w);
			}
		}

// ROLE GROUP:
        Vector multiple_role_groups = (Vector) hmap.get("ROLE GROUP");
        HashMap range2RoleGroupExpressionMap = null;
        ////////////////////////////////////////////////////////////////////////////////////
        range2RoleGroupExpressionMap = generateRange2RoleGroupExpressionMap(multiple_role_groups);
        ////////////////////////////////////////////////////////////////////////////////////
        if (range2RoleGroupExpressionMap != null) {
            Iterator it = range2RoleGroupExpressionMap.keySet().iterator();
            while (it.hasNext()) {
				String range = (String) it.next();
				Vector roleExpression_vec = (Vector) range2RoleGroupExpressionMap.get(range);
				w = new Vector();
				if (range2RolesHashMap.containsKey(range)) {
					w = (Vector) range2RolesHashMap.get(range);
				}
				w.addAll(roleExpression_vec);
			    range2RolesHashMap.put(range, w);
			}
		}
		String parentStr = buf.toString();
		return parentStr + "\n" + range2RolesHashMap2Expression(range2RolesHashMap);
	}

///////////////////////////////////////////////////////////////////////
	public Vector findRangesInLEData(HashMap hmap) {
		Vector ranges = new Vector();
		Vector roles = (Vector) hmap.get("ROLE");
		Vector roleUnions = (Vector) hmap.get("ROLE UNION");
		Vector roleGroups = (Vector) hmap.get("ROLE GROUP");

		if (roles != null && roles.size() > 0) {
			for (int i=0; i<roles.size(); i++) {
				String line = (String) roles.elementAt(i);
				Vector u = StringUtils.parseData(line, '|');
				String range = (String) u.elementAt(u.size()-1);
				if (!ranges.contains(range)) {
					ranges.add(range);
				}
			}
		}

		if (roleUnions != null && roleUnions.size() > 0) {
			for (int i=0; i<roleUnions.size(); i++) {
				String line = (String) roleUnions.elementAt(i);
				Vector u = StringUtils.parseData(line, '|');
				String range = (String) u.elementAt(u.size()-1);
				if (!ranges.contains(range)) {
					ranges.add(range);
				}
			}
		}

		if (roleGroups != null && roleGroups.size() > 0) {
			for (int i=0; i<roleGroups.size(); i++) {
				String line = (String) roleGroups.elementAt(i);
				int n = line.lastIndexOf("$");
				Vector u = StringUtils.parseData(line, '$');
				String range = (String) u.elementAt(u.size()-1);
				if (!ranges.contains(range)) {
					ranges.add(range);
				}
			}
		}
		return ranges;
	}

	public static Restriction displayString2Restriction(String line) {
		//Disease_Has_Abnormal_Cell		Primitive Mesenchymal Round to Oval Cell (C48916)
		line = line.replace("\t\t", "\t");
		Vector u = StringUtils.parseData(line, "\t");
		String roleLabel = (String) u.elementAt(0);
		String t = (String) (String) u.elementAt(1);
		int n = t.lastIndexOf("(");
		String targetLabel = t.substring(0, n-1);
		String targetCode = t.substring(n+1, t.length()-1);
		return new Restriction(roleLabel, targetCode, targetLabel);
	}

    public gov.nih.nci.evs.restapi.bean.LogicalExpression constructLogicalExpression(String code, HashMap hmap) {
		String label = getLabelByCode(named_graph, code);
		List<Concept> parents = new ArrayList();

		Vector parent_vec = (Vector) hmap.get("PARENT");
		for (int i=0; i<parent_vec.size(); i++) {
			String displayName = (String) parent_vec.elementAt(i);
			int n = displayName.lastIndexOf("(");
			String parent_label = displayName.substring(0, n-1);
			String parent_code = displayName.substring(n+1, displayName.length()-1);
			parents.add(new Concept(parent_label, parent_code));
		}
		// Find all ranges
		Vector ranges = findRangesInLEData(hmap);
		ranges.add(RANGE_UNSPECIFIED);
		List<gov.nih.nci.evs.restapi.bean.LogicalExpressionElement> elements = new ArrayList();
		for (int i=0; i<ranges.size(); i++) {
			String range = (String) ranges.elementAt(i);
			gov.nih.nci.evs.restapi.bean.LogicalExpressionElement ele = constructLogicalExpressionElement(code, hmap, range);
			if (ele != null) {
				elements.add(ele);
			}
		}

        String expression = null;
		gov.nih.nci.evs.restapi.bean.LogicalExpression le = new gov.nih.nci.evs.restapi.bean.LogicalExpression(
			   code,
			   label,
               parents,
               elements,
               expression
		   );
		return le;
	}

    public gov.nih.nci.evs.restapi.bean.LogicalExpressionElement constructLogicalExpressionElement(String code, HashMap hmap, String ele_range) {
		String label = getLabelByCode(named_graph, code);
		List<Restriction> ele_roles = new ArrayList();
		List<RoleUnion> ele_roleUnions = new ArrayList();
		List<RoleGroup> ele_roleGroups = new ArrayList();
		Vector roles = (Vector) hmap.get("ROLE");
		Vector roleUnions = (Vector) hmap.get("ROLE UNION");
		Vector roleGroups = (Vector) hmap.get("ROLE GROUP");

 		if (roles != null && roles.size() > 0) {
			for (int i=0; i<roles.size(); i++) {
				String line = (String) roles.elementAt(i);
				Vector u = StringUtils.parseData(line, '|');
				String displayString = (String) u.elementAt(0);
				String range = (String) u.elementAt(u.size()-1);
				if (range.compareTo(ele_range) == 0) {
					Restriction r = displayString2Restriction(displayString);
					ele_roles.add(r);
				}
			}
		}

 		if (roleUnions != null && roleUnions.size() > 0) {
			for (int i=0; i<roleUnions.size(); i++) {
				String line = (String) roleUnions.elementAt(i);
				Vector u = StringUtils.parseData(line, '|');
				String range = (String) u.elementAt(u.size()-1);
				if (range.compareTo(ele_range) == 0) {
					List<Restriction> roleList = new ArrayList();
					for (int j=0; j<u.size()-1; j++) {
						String displayString = (String) u.elementAt(j);
						Restriction r = displayString2Restriction(displayString);
						roleList.add(r);
					}
					ele_roleUnions.add(new RoleUnion(roleList));
				}
			}
		}

 		if (roleGroups != null && roleGroups.size() > 0) {
			for (int i=0; i<roleGroups.size(); i++) {
				String line = (String) roleGroups.elementAt(i);
				Vector u = StringUtils.parseData(line, '$');
				String range = (String) u.elementAt(u.size()-1);
				if (range.compareTo(ele_range) == 0) {
					List<RoleSet> roleSets = new ArrayList();
					for (int j=0; j<u.size()-1; j++) {
						String rg_displayString = (String) u.elementAt(j);
						Vector u2 = StringUtils.parseData(rg_displayString, '|');
						List roleList = new ArrayList();
						for (int k=0; k<u2.size(); k++) {
							String role_displayString = (String) u2.elementAt(k);
							Restriction r = displayString2Restriction(role_displayString);
							roleList.add(r);
						}
						RoleSet roleSet = new RoleSet(roleList);
						roleSets.add(roleSet);
					}
					RoleGroup roleGroup = new RoleGroup(roleSets);
					ele_roleGroups.add(roleGroup);
				}
			}
		}

		if (ele_roles.size() > 0 || ele_roleUnions.size() > 0 || ele_roleGroups.size() > 0) {
			return new LogicalExpressionElement(ele_range, ele_roles, ele_roleUnions, ele_roleGroups);
		}
		return null;
	}

    public static void test_role(String displayString) {
		Restriction r = displayString2Restriction(displayString);
		System.out.println(displayString);
		System.out.println(r.toJson());
	}

	public static void run(String serviceUrl, String named_graph, String username, String password, String code, boolean debug) {
		long ms = System.currentTimeMillis();
        LogicalExpression test = new LogicalExpression(serviceUrl, named_graph, username, password);
        test.run(named_graph, code, debug);
        System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}

	public static void main(String[] args) {
		long ms = System.currentTimeMillis();
		String serviceUrl = ConfigurationController.serviceUrl;
		String named_graph =  ConfigurationController.namedGraph;
		String username =  ConfigurationController.username;
		String password =  ConfigurationController.password;
		String code = args[0];
        LogicalExpression test = new LogicalExpression(serviceUrl, named_graph, username, password);
        test.run(named_graph, code, true);
        System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}
}
