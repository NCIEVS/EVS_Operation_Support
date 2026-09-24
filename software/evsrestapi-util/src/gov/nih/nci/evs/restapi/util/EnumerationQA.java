package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;
import gov.nih.nci.evs.restapi.bean.*;
import java.io.*;
import java.util.*;
import org.apache.commons.text.StringEscapeUtils;

import java.util.LinkedList;
import java.util.Queue;

public class EnumerationQA {
    Path2SPARQL path2SPARQL = new Path2SPARQL();
    HashMap enumerationMap = null;
    HashMap propCode2EnumerationMap = null;
    String named_graph = ConfigurationController.namedGraph;

 	public EnumerationQA() {
		initialize();
	}

	public void initialize() {
		enumerationMap = path2SPARQL.getEnumerationMap(named_graph);
		Utils.dumpHashMap("enumerationMap", enumerationMap);

		propCode2EnumerationMap = path2SPARQL.creatPropCode2EnumerationMap();
		Utils.dumpHashMap("propCode2EnumerationMap", propCode2EnumerationMap);
	}

	public Vector submitQuery(String query) {
		return path2SPARQL.submitQuery(query);
	}

	public Vector getEnumerationElements(String named_graph, String propCode) {
		String enumeration = (String) propCode2EnumerationMap.get(propCode);
		if (enumeration == null) return null;
		Vector v = path2SPARQL.getEnumerationElements(named_graph, enumeration);
        return v;
	}

	public String construct_get_anno_prop_value_query(String named_graph, String propCode) {
		String prefixes = path2SPARQL.getPrefixes();
		StringBuffer buf = new StringBuffer();
		buf.append(prefixes);
		buf.append("SELECT distinct ?x_label ?x_code ?p2_value ?p_value").append("\n");
		buf.append("from <" + named_graph + ">").append("\n");
		buf.append("where {").append("\n");
		buf.append("   ?x a owl:Class .").append("\n");
		buf.append("   ?x :NHC0 ?x_code .").append("\n");
		buf.append("   ?x rdfs:label ?x_label .").append("\n");

		buf.append("   ?p a owl:AnnotationProperty .").append("\n");
		buf.append("   ?p :NHC0 \"" + propCode + "\"^^xsd:string .").append("\n");
        buf.append("   ?x ?p ?p_value .").append("\n");

        buf.append("   ?p2 a owl:AnnotationProperty .").append("\n");
        buf.append("   ?p2 :NHC0 \"" + "P310" + "\"^^xsd:string .").append("\n");
        buf.append("   ?x ?p2 ?p2_value .").append("\n");

		buf.append("}").append("\n");
		return buf.toString();
	}

	public String construct_get_anno_prop_qual_value_query(String named_graph, String qualiferCode) {
		String prefixes = path2SPARQL.getPrefixes();
		StringBuffer buf = new StringBuffer();
		buf.append(prefixes);
		buf.append("SELECT distinct ?x_label ?x_code ?p2_value ?q_value").append("\n");
		buf.append("from <" + named_graph + ">").append("\n");
		buf.append("where {").append("\n");
		buf.append("   ?x a owl:Class .").append("\n");
		buf.append("   ?x :NHC0 ?x_code .").append("\n");
		buf.append("   ?x rdfs:label ?x_label .").append("\n");

		buf.append("   ?z_axiom a owl:Axiom .").append("\n");
        buf.append("   ?z_axiom owl:annotatedSource ?x .").append("\n");
        buf.append("   ?q :NHC0 \"" + qualiferCode + "\"^^xsd:string .").append("\n");
        buf.append("   ?z_axiom ?q ?q_value .").append("\n");

        buf.append("   ?p2 a owl:AnnotationProperty .").append("\n");
        buf.append("   ?p2 :NHC0 \"" + "P310" + "\"^^xsd:string .").append("\n");
        buf.append("   ?x ?p2 ?p2_value .").append("\n");

		buf.append("}").append("\n");
		return buf.toString();
	}

	public Vector QA(String propCode) {
		Vector w = new Vector();
		String enumeration = (String) propCode2EnumerationMap.get(propCode);
		String propLabel = enumeration.replace("-enum", "");
		Vector w1 = getEnumerationElements(named_graph, propCode);
		Utils.dumpVector("(1) " + propLabel, w1);

		Vector w2 = submitQuery(construct_get_anno_prop_value_query(named_graph, propCode));
		if (w2 == null) {
			w2 = submitQuery(construct_get_anno_prop_qual_value_query(named_graph, propCode));
		}

		Utils.dumpVector("(2) " + propLabel, w2);

		if (w2 != null && w2.size() > 0) {
			HashSet hset = Utils.vector2HashSet(w1);
			for (int i=0; i<w2.size(); i++) {
				String line = (String) w2.elementAt(i);
				Vector u2 = StringUtils.parseData(line, '|');
				if (!u2.contains("Retired_Concept")) {
					String t = (String) u2.elementAt(3);
					if (!hset.contains(t)) {
						w.add(line);
						w.add("ERROR: " + t + " is not in the enumeration list for " + propLabel + " (" + propCode + ")");
					}
				}
			}
		}
		return w;
	}


	public Vector QA() {
		Vector w = new Vector();
		Iterator it = propCode2EnumerationMap.keySet().iterator();
		while (it.hasNext()) {
			String propCode = (String) it.next();
			Vector w1 = QA(propCode);
			if (w1.size() > 0) {
				w.addAll(w1);
			}
		}
		return w;
	}

	public static void review(String filename) { //QA_report.txt
	    Vector v = Utils.readFile(filename);
	    for (int i=0; i<v.size()-1; i++) {
			String line1 = (String) v.elementAt(i);
			i++;
			String line2 = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line1, '|');
			if (!u.contains("Retired_Concept")) {
				System.out.println(line1);
				System.out.println(line2);
			}
		}
	}

	public static void main(String[] args) {
		long ms = System.currentTimeMillis();
		EnumerationQA test = new EnumerationQA();
		String named_graph = ConfigurationController.namedGraph;
		Vector w = test.QA();
		Utils.dumpVector("QA", w);
        test.review("QA_report.txt");
        System.out.println("Total QA run time (ms): " + (System.currentTimeMillis() - ms));
	}
}

