package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.bean.*;
import gov.nih.nci.evs.restapi.common.*;

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


public class SPARQLBuilder {

    public SPARQLBuilder() {

    }

    public void generateCode(String className, String queryfile) {
		CodeGenerator.run(className, queryfile);
	}

/*
Preferred_Name=P90|P384$CTCAE 6.0|P383$PT
DEFINITION=P97|P378$NCI
ALT_DEFINITION=P325|P378$CTCAE 6.0
rdfs:label=P90|P384$CTCAE 6.0|P383$PT
Maps_To=P375|P396$MedDRA|P397$28.0
FULL_SYN=P90|P384$CTCAE 6.0|P383$PT
FULL_SYN=P90|P384$NCI|P383$PT
*/

    public static String getPrefixes() {
		StringBuffer buf = new StringBuffer();
		buf.append("PREFIX :<http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#>").append("\n");
		buf.append("PREFIX base:<http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl>").append("\n");
		buf.append("PREFIX xml:<http://www.w3.org/XML/1998/namespace>").append("\n");
		buf.append("PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>").append("\n");
		buf.append("PREFIX owl:<http://www.w3.org/2002/07/owl#>").append("\n");
		buf.append("PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>").append("\n");
		buf.append("PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>").append("\n");
		return buf.toString();
	}

	public String construct_get_axiom_fullsyns(String named_graph, String subsetcode) {
		String prefixes = getPrefixes();
		StringBuffer buf = new StringBuffer();
		buf.append(prefixes);
		buf.append("select distinct ?x_label ?x_code ?a_target ?q1_label ?q1_value ?q2_label ?q2_value").append("\n");
		buf.append("from <" + named_graph + ">").append("\n");
		buf.append("where  { ").append("\n");
		buf.append("            ?x a owl:Class .").append("\n");
		buf.append("            ?x :NHC0 ?x_code .").append("\n");
		buf.append("            ?x rdfs:label ?x_label .").append("\n");
		buf.append("").append("\n");
		buf.append("            ?y a owl:Class .").append("\n");
		buf.append("            ?y :NHC0 ?y_code .").append("\n");
		buf.append("            ?y :NHC0 \"" + subsetcode + "\"^^xsd:string .").append("\n");
		buf.append("            ?y rdfs:label ?y_label .").append("\n");
		buf.append("").append("\n");
		buf.append("            ?x ?p ?y .").append("\n");
		buf.append("            ?p rdfs:label ?p_label .").append("\n");
		buf.append("            ?p rdfs:label \"Concept_In_Subset\"^^xsd:string .").append("\n");
		buf.append("            ").append("\n");
		buf.append("                ?p2 a owl:AnnotationProperty .").append("\n");
		buf.append(" ").append("\n");
		buf.append("                ?a a owl:Axiom .").append("\n");
		buf.append("                ?a owl:annotatedSource ?x .").append("\n");
		buf.append("                ?a owl:annotatedProperty ?p2 .").append("\n");
		buf.append("                ?p2 :NHC0 \"P90\"^^xsd:string .").append("\n");
		buf.append("                ?a owl:annotatedTarget ?a_target .").append("\n");
		buf.append("").append("\n");
		buf.append("                ?q1 :NHC0 \"P384\"^^xsd:string .").append("\n");
		buf.append("                ?q1 rdfs:label ?q1_label .").append("\n");
		buf.append("                ?a ?q1 ?q1_value .").append("\n");
		buf.append("").append("\n");
		buf.append("                ?q2 :NHC0 \"P383\"^^xsd:string .  ").append("\n");
		buf.append("                ?q2 rdfs:label ?q2_label . ").append("\n");
		buf.append("                ?a ?q2 ?q2_value .").append("\n");
		buf.append("").append("\n");
		buf.append("}").append("\n");
		buf.append("").append("\n");
		buf.append("").append("\n");
		return buf.toString();
	}

    public static Vector parseData(String line, char delimiter) {
		if(line == null) return null;
		Vector w = new Vector();
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<line.length(); i++) {
			char c = line.charAt(i);
			if (c == delimiter) {
				w.add(buf.toString());
				buf = new StringBuffer();
			} else {
				buf.append(c);
			}
		}
		w.add(buf.toString());
		return w;
	}

    //Maps_To=P375|P393$|P394$|P395$|P396$MedDRA|P397$28.0
	public static String construct_sparql_query(String named_graph, String dataReq, String subsetCode) {
		Vector u = parseData(dataReq, '=');
		String queryName = (String) u.elementAt(0);
		String queryDescription = (String) u.elementAt(1);
		String root = null;
		boolean subset = false;
		if (queryName.compareTo("subset") == 0) {
			subset = true;
			subsetCode = queryDescription;
		}

		boolean rdfsLabel = false;
		if (queryDescription.compareTo("rdfs:label") == 0) {
			rdfsLabel = true;
		}

		boolean subclassof = false;
		if (queryDescription.compareTo("subClassOf") == 0) {
			subclassof = true;
		}

		u = parseData(queryDescription, '|');
		HashMap qualifierMap = new HashMap();
		Vector u2 = new Vector();
		String propCode = (String) u.elementAt(0);
		if (u.size() > 1) {
			for (int i=1; i<u.size(); i++) {
				String t = (String) u.elementAt(i);
				u2 = parseData(t, '$');
				qualifierMap.put((String) u2.elementAt(0), (String) u2.elementAt(1));
			}
		}
		boolean propOnly = false;
		if (qualifierMap.keySet().size() == 0) {
			propOnly = true;
		}

		StringBuffer tmp = new StringBuffer();
		if (subset) {
			tmp.append("select distinct ?x_label ?x_code ");
		} else if (rdfsLabel) {
			tmp.append("select distinct ?x_label ?x_code ");
		} else if (subclassof) {
			tmp.append("SELECT distinct ?x_label ?x_code ?z_label ?z_code").append("\n");
		} else if (propOnly) {
			tmp.append("select distinct ?x_label ?x_code ?p2_value ");
		} else {
			tmp.append("select distinct ?x_label ?x_code ?a_target ");
			for (int i=1; i<u.size(); i++) {
				int j = i;
				tmp.append("?q" + j + "_label ?q" + j + "_value ");
			}
		}
		String selectStmt = tmp.toString();
		String prefixes = getPrefixes();
		StringBuffer buf = new StringBuffer();
		buf.append(prefixes);
		buf.append(selectStmt).append("\n");
		buf.append("from <" + named_graph + ">").append("\n");
		buf.append("where  { ").append("\n");
		buf.append("            ?x a owl:Class .").append("\n");
		buf.append("            ?x :NHC0 ?x_code .").append("\n");
		buf.append("            ?x rdfs:label ?x_label .").append("\n");
		buf.append("").append("\n");
		buf.append("            ?y a owl:Class .").append("\n");
		buf.append("            ?y :NHC0 ?y_code .").append("\n");
		buf.append("            ?y :NHC0 \"" + subsetCode + "\"^^xsd:string .").append("\n");
		buf.append("            ?y rdfs:label ?y_label .").append("\n");
		buf.append("").append("\n");
		buf.append("            ?x ?p ?y .").append("\n");
		buf.append("            ?p rdfs:label ?p_label .").append("\n");
		buf.append("            ?p rdfs:label \"Concept_In_Subset\"^^xsd:string .").append("\n");
		buf.append("").append("\n");
		if (rdfsLabel || subset) {
			buf.append("}").append("\n");
			return buf.toString();
		}

		if (subclassof) {
			buf.append("            ?z :NHC0 ?z_code .").append("\n");
			buf.append("            ?z rdfs:label ?z_label .  ").append("\n");
			buf.append("            ?z ?p ?y .").append("\n");
			buf.append("            ?x rdfs:subClassOf ?z .").append("\n");
			buf.append("}").append("\n");
			buf.append("").append("\n");
			return buf.toString();
		}

		buf.append("            ?p2 a owl:AnnotationProperty .").append("\n");
		buf.append(" ").append("\n");
		if (propOnly) {
			buf.append("			?p2 :NHC0 ?p2_code .").append("\n");
			buf.append("            ?p2 :NHC0 \"" + propCode + "\"^^xsd:string .").append("\n");
        	buf.append("            ?x ?p2 ?p2_value .").append("\n");
		} else {
			buf.append("            ?a a owl:Axiom .").append("\n");
			buf.append("            ?a owl:annotatedSource ?x .").append("\n");
			buf.append("            ?a owl:annotatedProperty ?p2 .").append("\n");
			buf.append("            ?p2 :NHC0 \"" + propCode + "\"^^xsd:string .").append("\n");
			buf.append("            ?a owl:annotatedTarget ?a_target .").append("\n");
			buf.append("").append("\n");
			for (int i=1; i<u.size(); i++) {
				int j = i;
				String t = (String) u.elementAt(i);
				Vector u3 = parseData(t, '$');
				String qualifierCode = (String) u3.elementAt(0);
				String qualifierValue = "";
				if (u3.size() > 1) {
					qualifierValue = (String) u3.elementAt(1);
				}
				buf.append("            ?q" + j + " :NHC0 \"" + qualifierCode + "\"^^xsd:string .").append("\n");
				buf.append("            ?q" + j + " rdfs:label ?q" + j + "_label .").append("\n");
				buf.append("            ?a ?q" + j + " ?q" + j + "_value .").append("\n");
				if (qualifierValue.length() > 0) {
					buf.append("            ?a ?q" + j + " \"" + qualifierValue + "\"^^xsd:string .").append("\n");
				}
			}
	    }
		buf.append("}").append("\n");
		buf.append("").append("\n");
		return buf.toString();
	}

	public static HashMap createQueryHashMap(String named_graph, String subsetCode, Vector v) {
		HashMap queryHashMap = new HashMap();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = parseData(line, '=');
			String queryName = (String) u.elementAt(0);
            String query = construct_sparql_query(named_graph, line, subsetCode);
            queryHashMap.put(queryName, query);
		}
		return queryHashMap;
	}

	public static void runTest(String serviceUrl,
	                           String namedGraph,
	                           String username,
	                           String password,
	                           String subsetCode,
	                           String filename
	                           ) {
        BasicQueryUtils basicQueryUtils = new BasicQueryUtils(serviceUrl, namedGraph, username, password);
		Vector v = Utils.readFile(filename);
        HashMap queryHashMap = SPARQLBuilder.createQueryHashMap(namedGraph, subsetCode, v);
		String outputfile = "results_sparql_" + filename;
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(outputfile);
			queryHashMap = SPARQLBuilder.createQueryHashMap(namedGraph, subsetCode, v);
			Iterator it = queryHashMap.keySet().iterator();
			int j = 0;
			while (it.hasNext()) {
				String queryName = (String) it.next();
				if (!queryName.startsWith("#")) {
					j++;
					System.out.println("\n(" + j + ") " + queryName);
					String query = (String) queryHashMap.get(queryName);
					pw.println(queryName);
					pw.println(query);
					Vector w = basicQueryUtils.executeQuery(query);
					if (w != null) {
						for (int k=0; k<w.size(); k++) {
							String t = (String) w.elementAt(k);
							pw.println(t);
						}
						pw.println("\n");
					}
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (pw != null) {
				try {
					pw.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	public static void run(
		String named_graph,
		String subsetCode,
		String filename) {
		Vector v = Utils.readFile(filename);

		String outputfile = "sparql_" + filename;
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(outputfile);

			HashMap queryHashMap = createQueryHashMap(named_graph, subsetCode, v);
			Iterator it = queryHashMap.keySet().iterator();
			int j = 0;
			while (it.hasNext()) {
				String queryName = (String) it.next();
				j++;
				System.out.println("\n(" + j + ") " + queryName);
				String query = (String) queryHashMap.get(queryName);
				pw.print(query);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (pw != null) {
				try {
					pw.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) {
		String named_graph = args[0];
		String subsetCode = args[1];
		String filename = args[2];
		run(named_graph, subsetCode, filename);
	}
}