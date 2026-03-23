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
		buf.append("	    ?e (owl:intersectionOf/rdf:rest*/rdf:first) ?y .").append("\n");
		buf.append("#            ?y an owl:Class .").append("\n");
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

System.out.println("++++++++++++++++++++++++++++++++++++++++");

		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			System.out.println(key);
		}
        return hmap;
	}

    public static HashMap sortLogicalExpressionData(Vector v) {
        HashMap hmap = new HashMap();
        hmap.put("Parent", new Vector());
        hmap.put("Role Group", new Vector());
        hmap.put("Role", new Vector());
        hmap.put("Role Union", new Vector());

        boolean start = false;
        //search for role groups
        Vector w = new Vector();
        StringBuffer rg_buf = new StringBuffer();

        for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (line.indexOf("Role Group starts") != -1) {
				start = true;
				rg_buf = new StringBuffer();
			} else if (line.indexOf("Role Group ends") != -1) {
				String s = rg_buf.toString();
				if (s.length() > 0) {
					if (s.endsWith("|")) {
						s = s.substring(0, s.length()-1);
					}
					w = (Vector) hmap.get("Role Group");
					w.add(s);
					hmap.put("Role Group", w);
					rg_buf = new StringBuffer();
				}
				start = false;
				rg_buf = new StringBuffer();
			}
			if (start) {
				if (line.compareTo("or") == 0) {
					//do nothing
				} else if (line.compareTo("Role Group starts") == 0) {
					//do nothing
				} else if (line.indexOf("|U|C|I|R|") != -1) {
					int n = line.lastIndexOf("R");
					String role = line.substring(n, line.length());
					rg_buf.append(role).append("|");
				}
			} else {
				if (line.startsWith("P|")) {
					Vector u = StringUtils.parseData(line, '|');
					String role = (String) u.elementAt(1);
					w = (Vector) hmap.get("Parent");
					w.add(role);
					hmap.put("Parent", w);
				} else if (!line.startsWith("P|") && line.indexOf("|U|C|I|R|") == -1) {
					if (line.indexOf("Role Group ends") == -1) {
						//|U|R| Role Union
						if (line.indexOf("|U|R|") == -1) {
							Vector u = StringUtils.parseData(line, '|');
							String role = (String) u.elementAt(u.size()-1);
							w = (Vector) hmap.get("Role");
							w.add(role);
							hmap.put("Role", w);
						}
					}
				}
			}
		}
		StringBuffer ru_buf = new StringBuffer();
        for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (line.indexOf("|U|R") != -1) {
				Vector u = StringUtils.parseData(line, '|');
				String role = (String) u.elementAt(u.size()-1);
				int n = line.lastIndexOf("R");
				role = line.substring(n, line.length());
				ru_buf.append(role).append("|");
			}
		}
		String s = ru_buf.toString();
		if (s.length() > 0) {
			if (s.endsWith("|")) {
				s = s.substring(0, s.length()-1);
			}
			w = (Vector) hmap.get("Role Union");
			w.add(s);
			hmap.put("Role Union", w);
		}
        return hmap;
	}


    public String generateRoleGroupExpression(String role_group_line) {
		StringBuffer buf2 = new StringBuffer();
		buf2.append("\n\t").append("Role Group(s)").append("\n");
		Vector role_group_vec = StringUtils.parseData(role_group_line, '|');
		for (int k=0; k<role_group_vec.size()/2; k++) {
			buf2.append("\t\t").append((String) role_group_vec.elementAt(k)).append("\n");
			buf2.append("\t\t").append((String) role_group_vec.elementAt(k+1)).append("\n");
			if (k<role_group_vec.size()/2 - 1) {
				buf2.append("\t").append("or").append("\n");
			}
		}
		return buf2.toString();
	}

	public HashMap generateRange2RoleGroupExpressionMap(Vector role_group_vec) {
		if (role_group_vec == null || role_group_vec.size() == 0) {
			System.out.println("INFO: role_group_vec == null || role_group_vec.size() == 0 return null");
			return null;
		}
		HashMap expressionMap = new HashMap();
		Vector w = new Vector();
		for (int i=0; i<role_group_vec.size(); i++) {
			String role_group_line = (String) role_group_vec.elementAt(i);
			String expression = generateRoleGroupExpression(role_group_line);
			Vector u = StringUtils.parseData(role_group_line, '|');
			String roleRange = (String) u.elementAt(u.size()-1);
			w = new Vector();
		    if (expressionMap.containsKey(roleRange)) {
				w = (Vector) expressionMap.get(roleRange);
			}
			int n = role_group_line.lastIndexOf("|");
			String line = role_group_line.substring(0, n);
			w.add(expression + "|RG");
			expressionMap.put(roleRange, w);
		}
		return expressionMap;
	}

    public String generateRoleUnionExpression(String role_union_line) {
		StringBuffer buf2 = new StringBuffer();
		buf2.append("\n");
		Vector role_union_vec = StringUtils.parseData(role_union_line, '|');
		for (int k=0; k<role_union_vec.size()-1; k++) {
			buf2.append("\t").append((String) role_union_vec.elementAt(k)).append("\n");
			if (k<role_union_vec.size() - 2) {
				buf2.append("\t").append("or").append("\n");
			}
		}
		return buf2.toString();
	}

	public HashMap generateRange2RoleUnionExpressionMap(Vector role_union_vec) {
		if (role_union_vec == null || role_union_vec.size() == 0) {
			System.out.println("INFO: role_union_vec == null || role_union_vec.size() == 0 return null");
			return null;
		}
		HashMap expressionMap = new HashMap();
		Vector w = new Vector();
		for (int i=0; i<role_union_vec.size(); i++) {
			String role_union_line = (String) role_union_vec.elementAt(i);
			String expression = generateRoleUnionExpression(role_union_line);
			Vector u = StringUtils.parseData(role_union_line, '|');
			String roleRange = (String) u.elementAt(u.size()-1);
			w = new Vector();
		    if (expressionMap.containsKey(roleRange)) {
				w = (Vector) expressionMap.get(roleRange);
			}
			int n = role_union_line.lastIndexOf("|");
			String line = role_union_line.substring(0, n);
			w.add(expression + "|RU");
			expressionMap.put(roleRange, w);
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
						buf.append("\t" + value).append("\n");
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

    public String getLogicalExpression(HashMap hmap) {
// Parents:
        StringBuffer buf = new StringBuffer();
        Vector parents = (Vector) hmap.get("Parent");
        Vector parent_vec = new Vector();
        Vector w = new Vector();

        if (parents != null && parents.size() > 0) {
			buf.append("Parent").append("\n");
			for (int i=0; i<parents.size(); i++) {
				String line = (String) parents.elementAt(i);
				parent_vec.add("\t" + line);
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

// multiple_role_groups:
        Vector multiple_role_groups = (Vector) hmap.get("ROLE GROUP");
        HashMap range2RoleGroupExpressionMap = null;
        range2RoleGroupExpressionMap = generateRange2RoleGroupExpressionMap(multiple_role_groups);
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

		Vector role_unions = (Vector) hmap.get("ROLE UNION");
        HashMap range2RoleUnionExpressionMap = null;
        range2RoleUnionExpressionMap = generateRange2RoleUnionExpressionMap(role_unions);
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

		String parentStr = buf.toString();
		return parentStr + "\n" + range2RolesHashMap2Expression(range2RolesHashMap);
	}

    public String run(String named_graph, String code) {
		boolean debug = true;
		return run(named_graph, code, debug);
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
			w.add(roleName + "\t" + roleTargetName + " (" + roleTargetCode + ")" + "|" + range);
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
				w2.add(roleName + "\t" + roleTargetName + " (" + roleTargetCode + ")");
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
			String range = null;
			HashMap id2RangeMap = new HashMap();
			for (int i=0; i<v.size(); i++) {
				String line = (String) v.elementAt(i);
				Vector u = StringUtils.parseData(line, '|');
				String id = (String) u.elementAt(2);
				Vector w2 = new Vector();
				if (roleGroupId2RolesHashMap.containsKey(id)) {
					w2 = (Vector) roleGroupId2RolesHashMap.get(id);
				}
				String roleName = (String) u.elementAt(5);
				String roleCode = (String) u.elementAt(6);
				String roleTargetCode = (String) u.elementAt(7);
				String roleTargetName = (String) u.elementAt(8);
				range = (String) roleCode2RangeNameMap.get(roleCode);
				id2RangeMap.put(id, range);
				w2.add(roleName + "\t" + roleTargetName + " (" + roleTargetCode + ")");
				roleGroupId2RolesHashMap.put(id, w2);
			}

			Iterator it = roleGroupId2RolesHashMap.keySet().iterator();
			while (it.hasNext()) {
				String id = (String) it.next();
				StringBuffer buf = new StringBuffer();
				Vector w2 = (Vector) roleGroupId2RolesHashMap.get(id);
				for (int i=0; i<w2.size(); i++) {
					String displayLabel = (String) w2.elementAt(i);
					buf.append(displayLabel).append("|");
				}
				buf.append((String) id2RangeMap.get(id));
				String s = buf.toString();
				Vector values = (Vector) map.get("ROLE GROUP");
				values.add(s);
				map.put("ROLE GROUP", values);
			}
		}
		return map;
	}

    public String run(String named_graph, String code, boolean debug) {
		String expression = null;
        HashMap hmap = getLogicalExpressionData(named_graph, code);

        if (debug) {
			Utils.dumpMultiValuedHashMap("Raw Logical Expression Data", hmap);
		}
		hmap = formatLogicalExpression(hmap);
		if (debug) {
			Utils.dumpMultiValuedHashMap("formatted LogicalExpressionData", hmap);
		}
		expression = getLogicalExpression(hmap);
		return expression;
    }

    public Vector findRolesWithoutRange() {
		Vector w = new Vector();
		int knt = 0;
		Iterator it = roleCode2RangeNameMap.keySet().iterator();
		while (it.hasNext()) {
			String roleCode = (String) it.next();
			String roleName = (String) roleCode2RoleNameMap.get(roleCode);
			String range = (String) roleCode2RangeNameMap.get(roleCode);
			if (range == null) {
				w.add(roleName + " (" + roleCode + ")");
			} else {
				knt++;
			}
		}
		System.out.println("Total number of roles: " + roleCode2RangeNameMap.keySet().size());
		System.out.println("Number of roles with a range: " + knt);
		return w;
	}

	public HashMap getQueries(String named_graph, String code) {
	    HashMap hmap = new HashMap();
	    hmap.put("PARENT", construct_get_parents(named_graph, code));
	    hmap.put("ROLE", construct_get_simple_roles(named_graph, code));
	    hmap.put("ROLE UNION", construct_get_role_unions(named_graph, code));
	    hmap.put("ROLE GROUP", construct_get_role_groups(named_graph, code));
	    hmap.put("RANGE", construct_get_range(named_graph));
	    return hmap;
	}

	public static void main(String[] args) {
		long ms = System.currentTimeMillis();
		String serviceUrl = ConfigurationController.serviceUrl;
		String named_graph =  ConfigurationController.namedGraph;
		String username =  ConfigurationController.username;
		String password =  ConfigurationController.password;
		String code = args[0];
        LogicalExpression test = new LogicalExpression(serviceUrl, named_graph, username, password);
        String expression = test.run(named_graph, code);

        String label = test.getLabelByCode(named_graph, code);
        System.out.println("Logical expression of the concept: " + label + " (" + code + ")");
        System.out.println(expression);
	}
}
