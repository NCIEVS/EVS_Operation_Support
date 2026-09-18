package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;
import gov.nih.nci.evs.restapi.bean.*;
import java.io.*;
import java.util.*;
import org.apache.commons.text.StringEscapeUtils;

import java.util.LinkedList;
import java.util.Queue;

class Path2SPARQL {

	static String NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile; //"ThesaurusInferred_forTS.owl";
	static String PARENT_CHILD_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile; // "parent_child.txt";
    String serviceUrl = ConfigurationController.serviceUrl;
    String named_graph = ConfigurationController.namedGraph;
    String username = ConfigurationController.username;
    String password = ConfigurationController.password;
    OWLSPARQLUtils owlSPARQLUtils = null;

 	public Path2SPARQL() {
		initialize();
	}

	public void initialize() {
		owlSPARQLUtils = new OWLSPARQLUtils(serviceUrl, username, password);
		owlSPARQLUtils.set_named_graph(named_graph);
	}

    public String constructQuery(String named_graph, String code, String path) {
        Vector u = StringUtils.parseData(path, '|');
        //Utils.dumpVector(path, u);
        Queue<String> queue = new LinkedList<>();
        for (int i=0; i<u.size(); i++) {
			String t = (String) u.elementAt(i);
			queue.offer(t);
		}
		String prefixes = owlSPARQLUtils.getPrefixes();
		StringBuffer buf = new StringBuffer();
		buf.append(prefixes);
		if (u.size() == 1 && (path.startsWith("P") || path.startsWith("A"))) {
		    buf.append("select distinct ?x_code ?p_code ?p_value").append("\n");
		} else if (u.contains("D")) {
			buf.append("select distinct ?x_code ?x_label ?y_code ?y_label").append("\n");
		} else  {
			buf.append("select distinct ?x_code ?x_label ?rs ?p_label ?p_code ?y_code ?y_label").append("\n");
		}
		buf.append("from <" + named_graph + ">").append("\n");
		buf.append("where {").append("\n");
		buf.append("            ?x a owl:Class .").append("\n");
		buf.append("            ?x :NHC0 ?x_code .").append("\n");
		buf.append("            ?x rdfs:label ?x_label .").append("\n");
		if (code != null) {
			buf.append("            ?x :NHC0 \"" + code + "\"^^xsd:string .").append("\n");
		}

		int c_count = 0;
		String c_id = null;

		while (queue.size() > 0) {
			String t = (String) queue.poll();
			String s = (String) queue.peek();
			if (t.startsWith("P") || t.startsWith("A")) {
				buf.append("	    ?p a owl:AnnotationProperty .").append("\n");
				buf.append("	    ?p :NHC0 ?p_code .").append("\n");
				buf.append("	    ?p :NHC0 \"" + t + "\"^^xsd:string .").append("\n");
				buf.append("	    ?x ?p ?p_value .").append("\n");

			} else if (t.startsWith("E")) {
				buf.append("            ?x owl:equivalentClass ?e .").append("\n");

			} else if (t.startsWith("I")) {
				String cid = c_id;
				if (c_count == 0) {
					cid = "?e";
				}
				c_count++;
				c_id = "?c_" + c_count;
				if (s != null && s.startsWith("R")) {
					buf.append("            " + cid + " (owl:intersectionOf/rdf:rest*/rdf:first) ?rs ." ).append("\n");

				} else if (s != null && s.startsWith("D")) {
					buf.append("            " + cid + " (owl:intersectionOf/rdf:rest*/rdf:first) ?y ." ).append("\n");

				} else {
					if (cid.compareTo(c_id) != 0) {
						buf.append("            " + cid + " (owl:intersectionOf/rdf:rest*/rdf:first) " + c_id + " .").append("\n");
					}
				}

			} else if (t.startsWith("U")) {
				String cid = c_id;
				if (s != null && s.startsWith("R")) {
					buf.append("            " + cid + " (owl:unionOf/rdf:rest*/rdf:first) ?rs ." ).append("\n");
				} else {
					c_count++;
					c_id = "?c_" + c_count;
					if (cid.compareTo(c_id) != 0) {
						buf.append("            " + cid + " (owl:unionOf/rdf:rest*/rdf:first) " + c_id + " .").append("\n");
					}
				}

            } else if (t.startsWith("D")) {
				buf.append("            ?y :NHC0 ?y_code .").append("\n");
				buf.append("            ?y rdfs:label ?y_label .").append("\n");

            } else if (t.startsWith("R")) {
				buf.append("            ?rs a owl:Restriction .").append("\n");
				buf.append("            ?rs owl:onProperty ?p .").append("\n");
				buf.append("            ?p :NHC0 ?p_code .").append("\n");
				buf.append("            ?p rdfs:label ?p_label .").append("\n");
				buf.append("            ?rs owl:someValuesFrom ?y .").append("\n");
				buf.append("            ?y :NHC0 ?y_code .").append("\n");
				buf.append("            ?y rdfs:label ?y_label .").append("\n");
			}
		}
		buf.append("}").append("\n");
		return buf.toString();
	}

	public Vector submitQuery(String named_graph, String code, String path) {
		String query = constructQuery(named_graph, code, path);
		System.out.println(query);
		Vector v = owlSPARQLUtils.executeQuery(query);
		if (v == null) return null;
		if (v.size() == 0) return v;
		return new SortUtils().quickSort(v);
	}

	public Vector submitQuery(String query) {
		Vector v = owlSPARQLUtils.executeQuery(query);
		if (v == null) return null;
		if (v.size() == 0) return v;
		return new SortUtils().quickSort(v);
	}

	public static Vector run(Vector paths) {
		Path2SPARQL test = new Path2SPARQL();
		HashSet hset = new HashSet();
		Vector w = new Vector();
		String named_graph = ConfigurationController.namedGraph;
		for (int i=0; i<paths.size(); i++) {
			String path = (String) paths.elementAt(i);
			System.out.println(path);
			String query = test.constructQuery(named_graph, null, path);
			System.out.println(query);
			if (!hset.contains(query)) {
				hset.add(query);
				w.add("\n#" + path);
				w.add(query);
				Vector v = test.submitQuery(query);
				w.addAll(v);
			}
		}
        return w;
	}

	public static void main(String[] args) {
		String filename = args[0];//
		Vector paths = Utils.readFile(filename);
		Vector w = run(paths);
		Utils.saveToFile("sparql_" + filename, w);
	}

}
