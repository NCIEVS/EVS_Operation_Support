package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;
import gov.nih.nci.evs.restapi.bean.*;
import java.io.*;
import java.util.*;
import org.apache.commons.text.StringEscapeUtils;

import java.util.LinkedList;
import java.util.Queue;

public class Path2SPARQL {
    static String NS = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl";
	static String NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile; //"ThesaurusInferred_forTS.owl";
	static String PARENT_CHILD_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile; // "parent_child.txt";
    String serviceUrl = ConfigurationController.serviceUrl;
    String named_graph = ConfigurationController.namedGraph;
    String username = ConfigurationController.username;
    String password = ConfigurationController.password;
    OWLSPARQLUtils owlSPARQLUtils = null;
    HashMap enumerationMap = null;

 	public Path2SPARQL() {
		initialize();
	}

	public void initialize() {
		owlSPARQLUtils = new OWLSPARQLUtils(serviceUrl, username, password);
		owlSPARQLUtils.set_named_graph(named_graph);
		enumerationMap = getEnumerationMap(named_graph);
	}

	public String construct_get_dt(String named_graph) {
		String prefixes = owlSPARQLUtils.getPrefixes();
		StringBuffer buf = new StringBuffer();
		buf.append(prefixes);
		buf.append("").append("\n");
		buf.append("select distinct ?dt ?element").append("\n");
		buf.append("from <" + named_graph + ">").append("\n");
		buf.append("where  { ").append("\n");
		buf.append("     ?dt a rdfs:Datatype .").append("\n");
		buf.append("     ?dt owl:equivalentClass ?e .").append("\n");
		buf.append("     ?e owl:oneOf ?y .").append("\n");
		buf.append("     ?y rdf:first ?element .").append("\n");
		buf.append("}").append("\n");
		return buf.toString();
	}

	public HashMap getEnumerationMap(String named_graph) {
		String query = construct_get_dt(named_graph);
		Vector v = owlSPARQLUtils.executeQuery(query);
		if (v == null) return null;
		if (v.size() == 0) return null;
		Vector w = new Vector();
		HashMap hmap = new HashMap();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			int n = line.lastIndexOf("#");
			line = line.substring(n+1, line.length());
			Vector u = StringUtils.parseData(line, '|');
		    hmap.put((String) u.elementAt(0), (String) u.elementAt(1));
		}
		return hmap;
	}

	public String construct_enumeration_elements_query(String named_graph, String enumeration) {
		if (!enumeration.endsWith("-enum")) {
			enumeration = enumeration + "-enum";
		}
		String firstElem = (String) enumerationMap.get(enumeration);
		//Semantic_Type-enum --> Acquired Abnormality
		String prefixes = owlSPARQLUtils.getPrefixes();
		StringBuffer buf = new StringBuffer();
		buf.append(prefixes);
		buf.append("select ?element ").append("\n");
		buf.append("from <" + named_graph + ">").append("\n");
		buf.append("where ").append("\n");
		buf.append("{ ").append("\n");
		buf.append("   ?dt a rdfs:Datatype .").append("\n");
		buf.append("   ?dt ?x ?x_value .").append("\n");
		buf.append("   ?x_value ?p ?e .").append("\n");
		buf.append("   ?x_value ?p \"" + firstElem + "\" .").append("\n");
        buf.append("   ?dt owl:oneOf/rdf:rest*/rdf:first ?element .").append("\n");
		buf.append("}").append("\n");
		return buf.toString();
	}

	public Vector getEnumerationElements(String named_graph, String enumeration) {
		return submitQuery(construct_enumeration_elements_query(named_graph, enumeration));
	}

	public String construct_get_datatypes(String named_graph) {
		String prefixes = owlSPARQLUtils.getPrefixes();
		StringBuffer buf = new StringBuffer();
		buf.append(prefixes);
		buf.append("select ?dt ?element ?elementType ").append("\n");
		buf.append("from <" + named_graph + ">").append("\n");
		buf.append("where ").append("\n");
		buf.append("{ ").append("\n");
		buf.append("   ?dt a rdfs:Datatype ;").append("\n");
		buf.append("   owl:oneOf/rdf:rest*/rdf:first ?element .").append("\n");
		buf.append("   bind(datatype(?element) as ?elementType)").append("\n");
		buf.append("}").append("\n");
		buf.append("").append("\n");
		buf.append("").append("\n");
		return buf.toString();
	}

    public String constructQuery(String named_graph, String code, String path) {
        Vector u = StringUtils.parseData(path, '|');
        Queue<String> queue = new LinkedList<>();
        for (int i=0; i<u.size(); i++) {
			String t = (String) u.elementAt(i);
			queue.offer(t);
		}
		String prefixes = owlSPARQLUtils.getPrefixes();
		StringBuffer buf = new StringBuffer();
		buf.append(prefixes);

		if (path.contains("Q") && u.size() == 1) {
			buf.append("SELECT distinct ?q_label ?q_code ").append("\n");
		} else if (path.contains("Q") && u.size() == 2) {
			buf.append("SELECT distinct ?p_label ?p_code ?q_label ?q_code ").append("\n");
		} else if (path.contains("Q") && u.size() == 3) {
			buf.append("SELECT distinct ??q_value ").append("\n");

		} else if (path.contains("M")) {
			buf.append("SELECT distinct ?p_label ?p_code ").append("\n");
		} else if (u.size() == 1 && path.contains("L")) {
			buf.append("select distinct ?x_label ?x_code").append("\n");
		} else if (u.size() == 1 && path.contains("H")) {
			buf.append("select distinct ?y_label ?y_code ?x_label ?x_code").append("\n");
		} else if (u.size() == 1 && (path.startsWith("P") || path.startsWith("A"))) {
		    buf.append("select distinct ?x_code ?p_code ?p_value").append("\n");
		} else if (u.contains("D")) {
			buf.append("select distinct ?x_code ?x_label ?y_code ?y_label").append("\n");

	    } else if (u.contains("Z")) {
			StringBuffer selectBuf = new StringBuffer();
			selectBuf.append("select distinct ?x_code ?p_code ?z_target ");
			for (int i=2; i<u.size(); i++) {
				int j = i-1;
				selectBuf.append("?q" + j + "_code ?q" + j + "_value ");
			}
			String selectBStmt = selectBuf.toString();
			buf.append(selectBStmt).append("\n");

		} else  {
			buf.append("select distinct ?x_code ?x_label ?rs ?p_label ?p_code ?y_code ?y_label").append("\n");
		}
		buf.append("from <" + named_graph + ">").append("\n");
		buf.append("where {").append("\n");
		if (!u.contains("M") && !u.contains("Q")) {
			buf.append("            ?x a owl:Class .").append("\n");
			buf.append("            ?x :NHC0 ?x_code .").append("\n");
			buf.append("            ?x rdfs:label ?x_label .").append("\n");
			if (code != null) {
				buf.append("            ?x :NHC0 \"" + code + "\"^^xsd:string .").append("\n");
			}
		}

		int c_count = 0;
		String c_id = null;
		int z_count = -1;
		int p_count = 0;
		int q_count = 0;
		String p_id = null;
		String q_id = null;
		String q_value = null;
		int s_count = 0; //step count

		while (queue.size() > 0) {
			String t = (String) queue.poll();
			String s = (String) queue.peek();

			if (path.contains("Q")) {
				buf.append("?x a owl:Class .").append("\n");
				buf.append("?z_axiom a owl:Axiom .").append("\n");
				buf.append("?z_axiom owl:annotatedSource ?x .").append("\n");
				buf.append("?z_axiom owl:annotatedProperty ?p .").append("\n");
				if (s != null) {
					buf.append("?p :NHC0 \"" + s + "\"^^xsd:string .").append("\n");
				} else {
					buf.append("?p rdfs:label ?p_label .").append("\n");
					buf.append("?p :NHC0 ?p_code .").append("\n");
				}

				buf.append("?q rdfs:label ?q_label .").append("\n");
				buf.append("?q :NHC0 ?q_code .").append("\n");

				if (u.size() == 3) {
					String q_code = (String) u.elementAt(2);
					buf.append("?q :NHC0 \"" + q_code + "\"^^xsd:string .").append("\n");
				}
				buf.append("?z_axiom ?q ?q_value .").append("\n");
				break;

			} else if (t.equals("M") && s.equals("A")) {
				buf.append("            ?p a owl:AnnotationProperty .").append("\n");
				buf.append("            ?p :NHC0 ?p_code .").append("\n");
				buf.append("            ?p rdfs:label ?p_label ").append("\n");
				break;

			} else if (t.equals("M") && s.equals("O")) {
				buf.append("            ?p a owl:ObjectProperty .").append("\n");
				buf.append("            ?p :NHC0 ?p_code .").append("\n");
				buf.append("            ?p rdfs:label ?p_label ").append("\n");
				break;

			} else if (t.startsWith("H")) { // Hierarchical relationship
				buf.append("            ?y a owl:Class .").append("\n");
				buf.append("            ?y :NHC0 ?y_code .").append("\n");
				buf.append("            ?y rdfs:label ?y_label .").append("\n");
				//buf.append("            ?x (rdfs:subClassOf|(owl:equivalentClass/owl:intersectionOf/rdf:rest*/rdf:first)) ?y . ").append("\n");
				buf.append("            ?x rdfs:subClassOf ?y . ").append("\n");

			} else if (t.startsWith("Z")) { // axiom
				buf.append("            ?z_axiom a owl:Axiom .").append("\n");
				buf.append("            ?z_axiom owl:annotatedSource ?x .").append("\n");

			} else if (t.startsWith("P") && s_count == 0) {
				p_count++;
				p_id = "?p";
				buf.append("            ?z_axiom owl:annotatedProperty " + p_id + " .").append("\n");
				buf.append("            ?z_axiom owl:annotatedTarget ?z_target .").append("\n");
				buf.append("            " + p_id + " :NHC0 " + p_id + "_code .").append("\n");
				buf.append("            " + p_id + " :NHC0 \"" + t + "\"^^xsd:string .").append("\n");
				buf.append("            " + p_id + " rdfs:label " + p_id + "_label .").append("\n");
				s_count++;

            } else if (t.startsWith("P") && s_count >= 1) {
				q_count++;
				q_id = "?q" + q_count;
				q_value = "?q" + q_count + "_value";
				buf.append("            " + q_id + " :NHC0 " + q_id + "_code .").append("\n");
				buf.append("            " + q_id + " :NHC0 \"" + t + "\"^^xsd:string .").append("\n");
				buf.append("            ?z_axiom " + q_id + " " + q_value + " .").append("\n");
		    }

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
		System.out.println(query);
		Vector v = owlSPARQLUtils.executeQuery(query);
		if (v == null) return null;
		if (v.size() == 0) return v;
		return new SortUtils().quickSort(v);
	}


	public static Vector run(Vector paths, String code) {
		Path2SPARQL test = new Path2SPARQL();
		HashSet hset = new HashSet();
		Vector w = new Vector();
		String named_graph = ConfigurationController.namedGraph;
		for (int i=0; i<paths.size(); i++) {
			String path = (String) paths.elementAt(i);
			System.out.println(path);
			String query = test.constructQuery(named_graph, code, path);
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

	public static Vector run(Vector paths) {
		String code = null;
		return run(paths, code);
	}

	public static void main(String[] args) {
		/*
		String filename = args[0];
		//String code = args[1];
		Vector paths = Utils.readFile(filename);
		//Vector w = run(paths, code);
		Vector w = run(paths);
		Utils.saveToFile("sparql_" + filename, w);
		*/
		Path2SPARQL path2SPARQL = new Path2SPARQL();
		String named_graph = ConfigurationController.namedGraph;
		//Vector v = path2SPARQL.submitQuery(path2SPARQL.construct_get_datatypes(named_graph));
		//Vector v = path2SPARQL.submitQuery(path2SPARQL.construct_get_semantictypes(named_graph));
		//HashMap hmap = path2SPARQL.getEnumerationMap(named_graph);
		//Utils.dumpHashMap("getEnumerations", hmap);
		Vector v = path2SPARQL.getEnumerationElements(named_graph, "Semantic_Type");
		Utils.dumpVector("Semantic_Type", v);
	}
}

