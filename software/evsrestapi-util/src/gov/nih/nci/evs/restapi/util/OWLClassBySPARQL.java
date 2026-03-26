package gov.nih.nci.evs.restapi.util;
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
public class OWLClassBySPARQL {
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

    public OWLClassBySPARQL(String serviceUrl, String named_graph, String username, String password) {
		this.serviceUrl = serviceUrl;
    	this.named_graph = named_graph;
    	this.username = username;
    	this.password = password;
        this.owlSPARQLUtils = new OWLSPARQLUtils(serviceUrl, username, password);
        this.owlSPARQLUtils.set_named_graph(named_graph);
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

	public String construct_get_restrictions(String named_graph, String code) {
		String prefixes = owlSPARQLUtils.getPrefixes();
		StringBuffer buf = new StringBuffer();
		buf.append(prefixes);
		buf.append("select distinct ?x_code ?x_label ?p_code ?p_label ?y_code ?y_label").append("\n");
		buf.append("from <" + named_graph + ">").append("\n");
		buf.append("where  { ").append("\n");
		buf.append("    ?x a owl:Class .").append("\n");
		buf.append("    ?x :NHC0 ?x_code .").append("\n");
		buf.append("    ?x :NHC0 \"" + code + "\"^^xsd:string . ").append("\n");
		buf.append("    ?x rdfs:label ?x_label .").append("\n");
		buf.append("    ?x rdfs:subClassOf ?rs .").append("\n");
		buf.append("    ?rs a owl:Restriction .").append("\n");
		buf.append("    ?rs owl:onProperty ?p .").append("\n");
		buf.append("    ?p :NHC0 ?p_code .").append("\n");
		buf.append("    ?p rdfs:label ?p_label .").append("\n");
		buf.append("    ?rs owl:someValuesFrom ?y .").append("\n");
		buf.append("    ?y :NHC0 ?y_code .").append("\n");
		buf.append("    ?y rdfs:label ?y_label .").append("\n");
		buf.append("}").append("\n");
		return buf.toString();
	}


	public Vector getRestrictions(String named_graph, String code) {
		String query = construct_get_restrictions(named_graph, code);
		Vector v = owlSPARQLUtils.executeQuery(query);
		if (v == null) return null;
		if (v.size() == 0) return v;
		return new SortUtils().quickSort(v);
	}

	public String construct_get_properties(String named_graph, String code) {
		String prefixes = owlSPARQLUtils.getPrefixes();
		StringBuffer buf = new StringBuffer();
		buf.append(prefixes);
		buf.append("select distinct ?x_label ?x_code ?p_label ?p_code ?p_value").append("\n");
		buf.append("from <" + named_graph + ">").append("\n");
		buf.append("where  { ").append("\n");
		buf.append("            	?x a owl:Class .").append("\n");
		buf.append("            	?x :NHC0 ?x_code .").append("\n");
		buf.append("            	?x :NHC0 \"" + code + "\"^^xsd:string .").append("\n");
		buf.append("            	?x rdfs:label ?x_label .").append("\n");
		buf.append("                ?p :NHC0 ?p_code .").append("\n");
		buf.append("           	    ?p rdfs:label ?p_label .").append("\n");
		buf.append("            	?x ?p ?p_value .").append("\n");
		buf.append("}").append("\n");
		return buf.toString();
	}

	public String construct_get_axioms(String named_graph, String code) {
		String prefixes = owlSPARQLUtils.getPrefixes();
		StringBuffer buf = new StringBuffer();
		buf.append(prefixes);
		buf.append("select distinct ?x_code ?x_label ?z_axiom ?p_label ?p_code ?z_target ?q_code ?q_label ?q_value").append("\n");
		buf.append("from <" + named_graph + ">").append("\n");
		buf.append("where {").append("\n");
		buf.append("            ?x a owl:Class .").append("\n");
		buf.append("            ?x :NHC0 ?x_code .").append("\n");
		buf.append("            	?x :NHC0 \"" + code + "\"^^xsd:string .").append("\n");
		buf.append("            ?x rdfs:label ?x_label .").append("\n");
		buf.append("            ?z_axiom a owl:Axiom  .").append("\n");
		buf.append("            ?z_axiom owl:annotatedSource ?x .").append("\n");
		buf.append("            ?z_axiom owl:annotatedProperty ?p .").append("\n");
		buf.append("            ?p rdfs:label ?p_label .").append("\n");
		buf.append("            ?p :NHC0 ?p_code .").append("\n");
		buf.append("            ?z_axiom owl:annotatedTarget ?z_target .").append("\n");
		buf.append("            ?z_axiom ?q ?q_value .").append("\n");
		buf.append("            ?q rdfs:label ?q_label .").append("\n");
		buf.append("            ?q :NHC0 ?q_code .").append("\n");
		buf.append("}").append("\n");
		return buf.toString();
	}


	public Vector getAxioms(String named_graph, String code) {
		String query = construct_get_axioms(named_graph, code);
		Vector v = owlSPARQLUtils.executeQuery(query);
		if (v == null) return null;
		if (v.size() == 0) return v;
		return new SortUtils().quickSort(v);
	}


/*
    <owl:Axiom>
        <owl:annotatedSource rdf:resource="http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C27781"/>
        <owl:annotatedProperty rdf:resource="http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#P375"/>
        <owl:annotatedTarget>Myxoid liposarcoma</owl:annotatedTarget>
        <P393>Related To</P393>
        <P394>PT</P394>
        <P395>8852/3</P395>
        <P396>ICDO3</P396>
        <P397>3.2</P397>
    </owl:Axiom>
*/

	public HashMap sortAxiomData(Vector axioms) {
		HashMap hmap = new HashMap();
		for(int i=0; i<axioms.size(); i++) {
			String line = (String) axioms.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String axiom_id = (String) u.elementAt(2);
			Vector w = new Vector();
			if (hmap.containsKey(axiom_id)) {
				w = (Vector) hmap.get(axiom_id);
			}
			w.add(line);
			hmap.put(axiom_id, w);
		}
        return hmap;
 	}

/*
        (10) C27781|Myxoid Liposarcoma|FULL_SYN|P90|Myxoid/Round Cell Liposarcoma|P383|Term Type|SY
        (11) C27781|Myxoid Liposarcoma|FULL_SYN|P90|Myxoid/Round Cell Liposarcoma|P384|Term Source|NCI
        (12) C27781|Myxoid Liposarcoma|FULL_SYN|P90|Myxoliposarcoma|P383|Term Type|SY
        (13) C27781|Myxoid Liposarcoma|FULL_SYN|P90|Myxoliposarcoma|P384|Term Source|GDC
        (14) C27781|Myxoid Liposarcoma|Maps_To|P375|8852/3|P393|Relationship_to_Target|Related To
        (15) C27781|Myxoid Liposarcoma|Maps_To|P375|8852/3|P394|Target_Term_Type|PT

*/


	public Vector getProperties(String named_graph, String code) {
		String query = construct_get_properties(named_graph, code);
		Vector v = owlSPARQLUtils.executeQuery(query);

		System.out.println("propertis: " + v.size());

		if (v == null) return null;
		if (v.size() == 0) return v;
		return new SortUtils().quickSort(v);
	}

    public void generate(PrintWriter out, String classId, Vector parents, Vector roles, Vector roleunions, Vector rolegroups, Vector restrictions,
        Vector properties, Vector axioms) {
		out.println("    <!-- http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#" + classId + " -->");
		out.println("");
		out.println("    <owl:Class rdf:about=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C45231\">");
		out.println("        <owl:equivalentClass>");
		out.println("            <owl:Class>");
		out.println("                <owl:intersectionOf rdf:parseType=\"Collection\">");
		for (int i=0; i<parents.size(); i++) {
			String line = (String) parents.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(0);
			out.println("                    <rdf:Description rdf:about=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#" + code + "\"/>");
	    }

		/*
		role group

rolegroups:
        (1) C45231|Diffuse Large B-Cell Lymphoma Unclassifiable|b0|b1|b2|Disease_May_Have_Abnormal_Cell|R113|C37010|Neoplastic B-Immunoblast
        (2) C45231|Diffuse Large B-Cell Lymphoma Unclassifiable|b0|b1|b3|Disease_May_Have_Associated_Disease|R116|C3461|Immunoblastic Lymphoma
        (3) C45231|Diffuse Large B-Cell Lymphoma Unclassifiable|b0|b4|b5|Disease_May_Have_Abnormal_Cell|R113|C37014|Neoplastic Centroblast
        (4) C45231|Diffuse Large B-Cell Lymphoma Unclassifiable|b0|b4|b6|Disease_May_Have_Associated_Disease|R116|C4074|Centroblastic Lymphoma
		*/

		if (rolegroups != null && rolegroups.size() > 0) {
			out.println("                    <owl:Class>");
			out.println("                        <owl:unionOf rdf:parseType=\"Collection\">");

			for (int i=0; i<rolegroups.size()/2; i++) {
				String line1 = (String) rolegroups.elementAt(2*i);
				String line2 = (String) rolegroups.elementAt(2*i+1);
				Vector u1 = StringUtils.parseData(line1, '|');
				String code1 = (String) u1.elementAt(6);
				String targetcode1 = (String) u1.elementAt(7);
				Vector u2 = StringUtils.parseData(line2, '|');
				String code2 = (String) u2.elementAt(6);
				String targetcode2 = (String) u2.elementAt(7);

				out.println("                            <owl:Class>");
				out.println("                                <owl:intersectionOf rdf:parseType=\"Collection\">");
				out.println("                                    <owl:Restriction>");
				out.println("                                        <owl:onProperty rdf:resource=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#" + code1 + "\"/>");
				out.println("                                        <owl:someValuesFrom rdf:resource=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#" + targetcode1 + "\"/>");
				out.println("                                    </owl:Restriction>");
				out.println("                                    <owl:Restriction>");
				out.println("                                        <owl:onProperty rdf:resource=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#" + code2 + "\"/>");
				out.println("                                        <owl:someValuesFrom rdf:resource=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#" + targetcode2 + "\"/>");
				out.println("                                    </owl:Restriction>");
				out.println("                                </owl:intersectionOf>");
				out.println("                            </owl:Class>");
			}
			out.println("                        </owl:unionOf>");
			out.println("                    </owl:Class>");
		}

/*
role unions:
        (1) C27781|Myxoid Liposarcoma|b0|b1|b2|Disease_May_Have_Cytogenetic_Abnormality|R114|C36317|t(12;16)(q13;p11)
        (2) C27781|Myxoid Liposarcoma|b0|b1|b3|Disease_May_Have_Molecular_Abnormality|R89|C37238|FUS-DDIT3 Fusion Protein Expression
        (3) C27781|Myxoid Liposarcoma|b0|b4|b5|Disease_May_Have_Cytogenetic_Abnormality|R114|C36374|t(12;22)(q13;q12)
        (4) C27781|Myxoid Liposarcoma|b0|b4|b6|Disease_May_Have_Molecular_Abnormality|R89|C45472|EWSR1-DDIT3 Fusion Protein Expression
*/

		if (roleunions != null && roleunions.size() > 0) {
			out.println("                    <owl:Class>");
			out.println("                        <owl:unionOf rdf:parseType=\"Collection\">");

			for (int i=0; i<roleunions.size()/2; i++) {
				String line1 = (String) roleunions.elementAt(2*i);
				String line2 = (String) roleunions.elementAt(2*i+1);
				Vector u1 = StringUtils.parseData(line1, '|');
				String code1 = (String) u1.elementAt(6);
				String targetcode1 = (String) u1.elementAt(7);
				Vector u2 = StringUtils.parseData(line2, '|');
				String code2 = (String) u2.elementAt(6);
				String targetcode2 = (String) u2.elementAt(7);
				out.println("                            <owl:Restriction>");
				out.println("                                <owl:onProperty rdf:resource=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#" + code1 + "\"/>");
				out.println("                                <owl:someValuesFrom rdf:resource=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#" + targetcode1 + "\"/>");
				out.println("                            </owl:Restriction>");
				out.println("                            <owl:Restriction>");
				out.println("                                <owl:onProperty rdf:resource=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#" + code2 + "\"/>");
				out.println("                                <owl:someValuesFrom rdf:resource=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#" + targetcode2 + "\"/>");
				out.println("                            </owl:Restriction>");
			}
			out.println("                        </owl:unionOf>");
			out.println("                    </owl:Class>	");
		}

		/*
		roles
		*/
		if (roles != null && roles.size() > 0) {
			for (int i=0; i<roles.size(); i++) {
				String line = (String) roles.elementAt(i);
				Vector u = StringUtils.parseData(line, '|');
				String code = (String) u.elementAt(5);
				String targetcode = (String) u.elementAt(6);
				out.println("                    <owl:Restriction>");
				out.println("                        <owl:onProperty rdf:resource=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#" + code + "\"/>");
				out.println("                        <owl:someValuesFrom rdf:resource=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#" + targetcode + "\"/>");
				out.println("                    </owl:Restriction>");
			}
	    }

		out.println("                </owl:intersectionOf>");
		out.println("            </owl:Class>");
		out.println("        </owl:equivalentClass>");


/*
restrictions:
        (1) C27781|Myxoid Liposarcoma|R100|Disease_Has_Associated_Anatomic_Site|C12964|Connective and Soft Tissue
        (2) C27781|Myxoid Liposarcoma|R101|Disease_Has_Primary_Anatomic_Site|C12964|Connective and Soft Tissue
        (3) C27781|Myxoid Liposarcoma|R103|Disease_Has_Normal_Tissue_Origin|C12472|Adipose Tissue
        (4) C27781|Myxoid Liposarcoma|R104|Disease_Has_Normal_Cell_Origin|C12555|Connective and Soft Tissue Cell
        (5) C27781|Myxoid Liposarcoma|R104|Disease_Has_Normal_Cell_Origin|C12556|Adipocyte
        (6) C27781|Myxoid Liposarcoma|R105|Disease_Has_Abnormal_Cell|C12917|Malignant Cell
        (7) C27781|Myxoid Liposarcoma|R105|Disease_Has_Abnormal_Cell|C12922|Neoplastic Cell
        (8) C27781|Myxoid Liposarcoma|R105|Disease_Has_Abnormal_Cell|C36887|Neoplastic Connective and Soft Tissue Cell
        (9) C27781|Myxoid Liposarcoma|R105|Disease_Has_Abnormal_Cell|C48683|Neoplastic Lipocyte
        (10) C27781|Myxoid Liposarcoma|R105|Disease_Has_Abnormal_Cell|C48877|Malignant Lipocyte
        (11) C27781|Myxoid Liposarcoma|R108|Disease_Has_Finding|C36115|Malignant Cellular Infiltrate
        (12) C27781|Myxoid Liposarcoma|R137|Disease_Excludes_Normal_Tissue_Origin|C45714|Epithelial Tissue
        (13) C27781|Myxoid Liposarcoma|R138|Disease_Excludes_Normal_Cell_Origin|C12578|Epithelial Cell
        (14) C27781|Myxoid Liposarcoma|R138|Disease_Excludes_Normal_Cell_Origin|C41405|Neuron, Neuroepithelial Cell, and Supporting Cell of the Nervous System
        (15) C27781|Myxoid Liposarcoma|R139|Disease_Excludes_Abnormal_Cell|C36779|Malignant Epithelial Cell
        (16) C27781|Myxoid Liposarcoma|R142|Disease_Excludes_Finding|C36105|Indolent Clinical Course
        (17) C27781|Myxoid Liposarcoma|R142|Disease_Excludes_Finding|C36122|Benign Cellular Infiltrate
        (18) C27781|Myxoid Liposarcoma|R142|Disease_Excludes_Finding|C53665|Intermediate Filaments Present
        (19) C27781|Myxoid Liposarcoma|R142|Disease_Excludes_Finding|C53667|Intracytoplasmic Eosinophilic Inclusion
*/
		if (restrictions != null && restrictions.size() > 0) {
			for (int i=0; i<restrictions.size(); i++) {
				String line = (String) restrictions.elementAt(i);
				Vector u = StringUtils.parseData(line, '|');
				String code = (String) u.elementAt(2);
				String targetcode = (String) u.elementAt(4);
				out.println("        <rdfs:subClassOf>");
				out.println("            <owl:Restriction>");
				out.println("                <owl:onProperty rdf:resource=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#" + code + "\"/>");
				out.println("                <owl:someValuesFrom rdf:resource=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#" + targetcode + "\"/>");
				out.println("            </owl:Restriction>");
				out.println("        </rdfs:subClassOf>");
			}
	    }

        Vector w = new Vector();
		if (properties != null && properties.size() > 0) {
			for (int i=0; i<properties.size(); i++) {
				String line = (String) properties.elementAt(i);
				Vector u = StringUtils.parseData(line, '|');
				w.add((String) u.elementAt(3) + "|" + (String) u.elementAt(4));
			}
			w = new SortUtils().quickSort(w);
			for (int i=0; i<w.size(); i++) {
				String line = (String) w.elementAt(i);
				Vector u = StringUtils.parseData(line, '|');
				String propCode = (String) u.elementAt(0);
				String propValue = (String) u.elementAt(1);
				if (propCode.startsWith("A")) {
					out.println("        <" + propCode + " rdf:resource=\"" + propValue + "\"/>");
				} else {
					out.println("        <" + propCode + ">" + propValue + "</" + propCode + ">");
				}
			}
		}
		out.println("    </owl:Class>");

/*
/*
 (1) C27781|Myxoid Liposarcoma|b0|Maps_To|P375|8852/3|P393|Relationship_to_Target|Related To
        (2) C27781|Myxoid Liposarcoma|b0|Maps_To|P375|8852/3|P394|Target_Term_Type|PT
        (3) C27781|Myxoid Liposarcoma|b0|Maps_To|P375|8852/3|P395|Target_Code|morphology
        (4) C27781|Myxoid Liposarcoma|b0|Maps_To|P375|8852/3|P396|Target_Terminology|GDC
        (5) C27781|Myxoid Liposarcoma|b1|Maps_To|P375|Myxoid liposarcoma|P393|Relationship_to_Target|Has Synonym
        (6) C27781|Myxoid Liposarcoma|b1|Maps_To|P375|Myxoid liposarcoma|P394|Target_Term_Type|PT
        (7) C27781|Myxoid Liposarcoma|b1|Maps_To|P375|Myxoid liposarcoma|P395|Target_Code|primary_diagnosis
        (8) C27781|Myxoid Liposarcoma|b1|Maps_To|P375|Myxoid liposarcoma|P396|Target_Terminology|GDC
        (9) C27781|Myxoid Liposarcoma|b2|Maps_To|P375|Myxoid liposarcoma|P393|Relationship_to_Target|Related To
        (10) C27781|Myxoid Liposarcoma|b2|Maps_To|P375|Myxoid liposarcoma|P394|Target_Term_Type|PT
        (11) C27781|Myxoid Liposarcoma|b2|Maps_To|P375|Myxoid liposarcoma|P395|Target_Code|8852/3
        (12) C27781|Myxoid Liposarcoma|b2|Maps_To|P375|Myxoid liposarcoma|P396|Target_Terminology|ICDO3
        (13) C27781|Myxoid Liposarcoma|b2|Maps_To|P375|Myxoid liposarcoma|P397|Target_Terminology_Version|3.1
        (14) C27781|Myxoid Liposarcoma|b3|Maps_To|P375|Myxoid liposarcoma|P393|Relationship_to_Target|Related To
        (15) C27781|Myxoid Liposarcoma|b3|Maps_To|P375|Myxoid liposarcoma|P394|Target_Term_Type|PT
        (16) C27781|Myxoid Liposarcoma|b3|Maps_To|P375|Myxoid liposarcoma|P395|Target_Code|8852/3
        (17) C27781|Myxoid Liposarcoma|b3|Maps_To|P375|Myxoid liposarcoma|P396|Target_Terminology|ICDO3
        (18) C27781|Myxoid Liposarcoma|b3|Maps_To|P375|Myxoid liposarcoma|P397|Target_Terminology_Version|3.2
        (19) C27781|Myxoid Liposarcoma|b4|Maps_To|P375|Myxoliposarcoma|P393|Relationship_to_Target|Has Synonym
        (20) C27781|Myxoid Liposarcoma|b4|Maps_To|P375|Myxoliposarcoma|P394|Target_Term_Type|PT
        (21) C27781|Myxoid Liposarcoma|b4|Maps_To|P375|Myxoliposarcoma|P395|Target_Code|primary_diagnosis
        (22) C27781|Myxoid Liposarcoma|b4|Maps_To|P375|Myxoliposarcoma|P396|Target_Terminology|GDC
        (23) C27781|Myxoid Liposarcoma|b5|FULL_SYN|P90|MLPS|P383|Term Type|AB
        (24) C27781|Myxoid Liposarcoma|b5|FULL_SYN|P90|MLPS|P384|Term Source|NCI
        (25) C27781|Myxoid Liposarcoma|b6|FULL_SYN|P90|Myxoid Liposarcoma|P383|Term Type|PT
        (26) C27781|Myxoid Liposarcoma|b6|FULL_SYN|P90|Myxoid Liposarcoma|P384|Term Source|Cellosaurus
        (27) C27781|Myxoid Liposarcoma|b7|FULL_SYN|P90|Myxoid Liposarcoma|P383|Term Type|PT
        (28) C27781|Myxoid Liposarcoma|b7|FULL_SYN|P90|Myxoid Liposarcoma|P384|Term Source|NCI
        (29) C27781|Myxoid Liposarcoma|b8|FULL_SYN|P90|Myxoid Liposarcoma|P383|Term Type|SY
        (30) C27781|Myxoid Liposarcoma|b8|FULL_SYN|P90|Myxoid Liposarcoma|P384|Term Source|NCI
        (31) C27781|Myxoid Liposarcoma|b9|FULL_SYN|P90|Myxoid liposarcoma|P383|Term Type|PT
        (32) C27781|Myxoid Liposarcoma|b9|FULL_SYN|P90|Myxoid liposarcoma|P384|Term Source|GDC
        (33) C27781|Myxoid Liposarcoma|b10|FULL_SYN|P90|Myxoid/Round Cell Liposarcoma|P383|Term Type|SY
        (34) C27781|Myxoid Liposarcoma|b10|FULL_SYN|P90|Myxoid/Round Cell Liposarcoma|P384|Term Source|NCI
        (35) C27781|Myxoid Liposarcoma|b11|FULL_SYN|P90|Myxoliposarcoma|P383|Term Type|SY
        (36) C27781|Myxoid Liposarcoma|b11|FULL_SYN|P90|Myxoliposarcoma|P384|Term Source|GDC
        (37) C27781|Myxoid Liposarcoma|b12|DEFINITION|P97|A liposarcoma characterized by the presence of round non-lipogenic primitive mesenchymal cells and small signet ring lipoblasts within a myxoid stoma with a branching vascular pattern.  This category includes hypercellular lesions with round cell morphology, formerly known as round cell liposarcoma.|P378|Definition Source|NCI

*/


		HashMap hmap = sortAxiomData(axioms);
		Vector keys = new Vector();
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			keys.add(key);
		}
		keys = new SortUtils().quickSort(keys);
		for (int i=0; i<keys.size(); i++) {
			out.println("    <owl:Axiom>");
			String key = (String) keys.elementAt(i); //axiom_id
			Vector values = (Vector) hmap.get(key);
			String line1 = (String) values.elementAt(0);
			Vector u = StringUtils.parseData(line1, '|');
			String annotatedProperty = (String) u.elementAt(4);
			String annotatedTarget = (String) u.elementAt(5);
			out.println("        <owl:annotatedSource rdf:resource=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#" + classId + "\"/>");
			out.println("        <owl:annotatedProperty rdf:resource=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#" + annotatedProperty + "\"/>");
			out.println("        <owl:annotatedTarget>" + annotatedTarget + "</owl:annotatedTarget>");
			for (int j=0; j<values.size(); j++) {
				String linej = (String) values.elementAt(j);
				Vector u2 = StringUtils.parseData(linej, '|');
				String q_code = (String) u2.elementAt(6);
				String q_value = (String) u2.elementAt(8);
				out.println("        <" + q_code + ">" + q_value + "</" + q_code + ">");
			}
			out.println("    </owl:Axiom>");
		}

        /*
		out.println("    <owl:Axiom>");
		out.println("        <owl:annotatedSource rdf:resource=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C45231\"/>");
		out.println("        <owl:annotatedProperty rdf:resource=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#P90\"/>");
		out.println("        <owl:annotatedTarget>DLBCL Unclassifiable</owl:annotatedTarget>");
		out.println("        <P383>SY</P383>");
		out.println("        <P384>NCI</P384>");
		out.println("    </owl:Axiom>");
		out.println("    <owl:Axiom>");
		out.println("        <owl:annotatedSource rdf:resource=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C45231\"/>");
		out.println("        <owl:annotatedProperty rdf:resource=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#P90\"/>");
		out.println("        <owl:annotatedTarget>Diffuse Large B-Cell Lymphoma Indeterminate</owl:annotatedTarget>");
		out.println("        <P383>SY</P383>");
		out.println("        <P384>NCI</P384>");
		out.println("    </owl:Axiom>");
		out.println("    <owl:Axiom>");
		out.println("        <owl:annotatedSource rdf:resource=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C45231\"/>");
		out.println("        <owl:annotatedProperty rdf:resource=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#P90\"/>");
		out.println("        <owl:annotatedTarget>Diffuse Large B-Cell Lymphoma Unclassifiable</owl:annotatedTarget>");
		out.println("        <P383>DN</P383>");
		out.println("        <P384>CTRP</P384>");
		out.println("    </owl:Axiom>");
		out.println("    <owl:Axiom>");
		out.println("        <owl:annotatedSource rdf:resource=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C45231\"/>");
		out.println("        <owl:annotatedProperty rdf:resource=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#P90\"/>");
		out.println("        <owl:annotatedTarget>Diffuse Large B-Cell Lymphoma Unclassifiable</owl:annotatedTarget>");
		out.println("        <P383>PT</P383>");
		out.println("        <P384>NCI</P384>");
		out.println("    </owl:Axiom>");
		out.println("    <owl:Axiom>");
		out.println("        <owl:annotatedSource rdf:resource=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C45231\"/>");
		out.println("        <owl:annotatedProperty rdf:resource=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#P90\"/>");
		out.println("        <owl:annotatedTarget>Type 3 DLBCL</owl:annotatedTarget>");
		out.println("        <P383>AB</P383>");
		out.println("        <P384>NCI</P384>");
		out.println("    </owl:Axiom>");
		out.println("    <owl:Axiom>");
		out.println("        <owl:annotatedSource rdf:resource=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C45231\"/>");
		out.println("        <owl:annotatedProperty rdf:resource=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#P90\"/>");
		out.println("        <owl:annotatedTarget>Type 3 Diffuse Large B-Cell Lymphoma</owl:annotatedTarget>");
		out.println("        <P383>SY</P383>");
		out.println("        <P384>NCI</P384>");
		out.println("    </owl:Axiom>");
		out.println("    <owl:Axiom>");
		out.println("        <owl:annotatedSource rdf:resource=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C45231\"/>");
		out.println("        <owl:annotatedProperty rdf:resource=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#P97\"/>");
		out.println("        <owl:annotatedTarget>A biologic subset of diffuse large B-cell lymphomas (DLBCL) that do not overexpress the genes whose overexpression characterizes the germinal center B-cell-like and activated B-cell-like DLBCL.  Morphologically, these lymphomas are either immunoblastic or centroblastic.  Patients with this type of diffuse large B-cell lymphoma are reported to have a less favorable outcome, similar to those with an activated B-cell-like gene expression profile.</owl:annotatedTarget>");
		out.println("        <P378>NCI</P378>");
		out.println("    </owl:Axiom>");
		*/
    }


	public void generate(String outputfile, String classId, Vector parents, Vector roles, Vector roleunions, Vector rolegroups, Vector restrictions,
	    Vector properties, Vector axioms) {
		long ms = System.currentTimeMillis();
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(outputfile, "UTF-8");
			generate(pw, classId, parents, roles, roleunions, rolegroups, restrictions, properties, axioms);

		} catch (Exception ex) {

		} finally {
			try {
				pw.close();
				System.out.println("Output file " + outputfile + " generated.");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}


	public static void main(String[] args) {
		long ms = System.currentTimeMillis();
		String serviceUrl = ConfigurationController.serviceUrl;
		String named_graph =  ConfigurationController.namedGraph;
		String username =  ConfigurationController.username;
		String password =  ConfigurationController.password;
		String code = args[0];
        OWLClassBySPARQL test = new OWLClassBySPARQL(serviceUrl, named_graph, username, password);
        Vector parents = test.getParents(named_graph, code);
        Utils.dumpVector("parents", parents);
        Vector roles = test.getSimpleRoles(named_graph, code);
        Utils.dumpVector("roles", roles);

        Vector rolegroups = test.getRoleGroups(named_graph, code);
        Utils.dumpVector("role groups", rolegroups);

        Vector roleunions = test.getRoleUnions(named_graph, code);
        Utils.dumpVector("role unions", rolegroups);

        Vector restrictions = test.getRestrictions(named_graph, code);

        Vector properties = test.getProperties(named_graph, code);
        Utils.dumpVector("properties", properties);

        Vector axioms = test.getAxioms(named_graph, code);
        Utils.dumpVector("axioms", axioms);

        String outputfile = "generated_" + code + ".owl";
        test.generate(outputfile, code, parents, roles, roleunions, rolegroups, restrictions, properties, axioms);
	}
}


