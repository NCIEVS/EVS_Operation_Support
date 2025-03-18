package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;

import java.io.*;
import java.text.*;
import java.util.*;

public class RelationshipTableGenerator {

	OWLSPARQLUtils owlSPARQLUtils = null;

    String version = null;
    String serviceUrl = null;
    String named_graph = null;
    String username = null;
    String password = null;

    Vector supportedPropertyQualifiers = null;

    public RelationshipTableGenerator(String serviceUrl, String named_graph, String username, String password) {
		this.serviceUrl = serviceUrl;
		this.named_graph = named_graph;
		this.username = username;
		this.password = password;

	    this.owlSPARQLUtils = new OWLSPARQLUtils(serviceUrl, username, password);
        this.owlSPARQLUtils.set_named_graph(named_graph);

	    this.version = owlSPARQLUtils.get_version();
	    System.out.println(this.version);
	}

	public void generateHTML(String filename) {
		Vector v = Utils.readFile(filename);
		HTMLTable.generate(v);
	}

	public void generate(String outputfile) {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(outputfile, "UTF-8");
			pw.println("<title>NCIt Relationships");
			pw.println("<table>Supported Roles");
			pw.println("<th>Name");
			pw.println("<th>Code");
			pw.println("<th>Domain");
			pw.println("<th>Range");
			pw.println("<th>Definition");
			pw.println("<data>");
			Vector v = getRoleDef(named_graph);

			if (v == null) {
				System.out.println("getRoleDef returns NULL???");
				return;
			}

			for (int i=0; i<v.size(); i++) {
				String t = (String) v.elementAt(i);
				pw.println(t);
			}
			pw.println("</data>");
			pw.println("</table>");
			pw.println("<br></br>");
            pw.println("<br></br>");
			pw.println("<table>Supported Associations");
			pw.println("<th>Name");
			pw.println("<th>Code");
			pw.println("<th>Definition");
			pw.println("<data>");
			v = getAssociationDef(named_graph);
			for (int i=0; i<v.size(); i++) {
				String t = (String) v.elementAt(i);
				pw.println(t);
			}
			pw.println("</data>");
			pw.println("</table>");

			pw.println("<footer>(Source: NCI Thesaurus, version " + this.version + ")");
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				pw.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public String construct_get_roleDef(String named_graph) {
		String prefixes = owlSPARQLUtils.getPrefixes();
		StringBuffer buf = new StringBuffer();
		buf.append(prefixes);
		buf.append("SELECT ?x_label ?x_code ?x_domain_label ?x_range_label ?p_value").append("\n");
		buf.append("{").append("\n");
		buf.append("graph <" + named_graph + ">").append("\n");
		buf.append("{").append("\n");
		buf.append("?x a owl:ObjectProperty .").append("\n");
		buf.append("?x rdfs:label ?x_label .").append("\n");
		buf.append("?x :NHC0 ?x_code .").append("\n");
		buf.append("?x rdfs:domain ?x_domain .").append("\n");
		buf.append("?x rdfs:range  ?x_range .").append("\n");
		buf.append("?x_domain rdfs:label ?x_domain_label .").append("\n");
		buf.append("?x_range rdfs:label ?x_range_label .").append("\n");
		buf.append("?p a owl:AnnotationProperty .").append("\n");
		buf.append("?p :NHC0 \"P97\"^^xsd:string .").append("\n");
		buf.append("?x ?p ?p_value .").append("\n");
		buf.append("}").append("\n");
		buf.append("}").append("\n");
		return buf.toString();
	}


	public Vector getRoleDef(String named_graph) {
		String query = construct_get_roleDef(named_graph);
		Vector v = owlSPARQLUtils.executeQuery(query);
		if (v == null) return null;
		if (v.size() == 0) return v;
		return new SortUtils().quickSort(v);
	}


	public String construct_get_associationDef(String named_graph) {
		String prefixes = owlSPARQLUtils.getPrefixes();
		StringBuffer buf = new StringBuffer();
		buf.append(prefixes);
		buf.append("SELECT ?x_label ?x_code ?p_value").append("\n");
		buf.append("{").append("\n");
		buf.append("graph <" + named_graph + ">").append("\n");
		buf.append("{").append("\n");
		buf.append("?x a owl:AnnotationProperty .").append("\n");
		buf.append("?x rdfs:label ?x_label .").append("\n");
		buf.append("?x :NHC0 ?x_code .").append("\n");
		buf.append("?x rdfs:range xsd:anyURI .").append("\n");
		buf.append("?p a owl:AnnotationProperty .").append("\n");
		buf.append("?p :NHC0 \"P97\"^^xsd:string .").append("\n");
		buf.append("?x ?p ?p_value .").append("\n");
		buf.append("}").append("\n");
		buf.append("}").append("\n");
		buf.append("").append("\n");
		return buf.toString();
	}


	public Vector getAssociationDef(String named_graph) {
		String query = construct_get_associationDef(named_graph);
		Vector v = owlSPARQLUtils.executeQuery(query);
		if (v == null) return null;
		if (v.size() == 0) return v;
		return new SortUtils().quickSort(v);
	}


	public static void main(String[] args) {
		String serviceUrl = ConfigurationController.serviceUrl;
		String named_graph = ConfigurationController.namedGraph;
		String username = ConfigurationController.username;
		String password = ConfigurationController.password;
        RelationshipTableGenerator RelationshipTableGenerator = new RelationshipTableGenerator(serviceUrl, named_graph, username, password);

        String filename = "relationshipdata.txt";
        RelationshipTableGenerator.generate(filename);
        RelationshipTableGenerator.generateHTML(filename);

	}
}