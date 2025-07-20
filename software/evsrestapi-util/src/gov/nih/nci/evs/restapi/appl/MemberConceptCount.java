package gov.nih.nci.evs.restapi.appl;
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
 * @author EVS Team
 * @version 1.0
 *
 * Modification history:
 *     Initial implementation kim.ong@nih.gov
 *
 */


public class MemberConceptCount {
    String serviceUrl = null;
    String namedGraph = null;
    String named_graph_id = ":NHC0";
    HashMap nameVersion2NamedGraphMap = null;
    HashMap ontologyUri2LabelMap = null;
    String version = null;
    String username = null;
    String password = null;
    OWLSPARQLUtils owlSPARQLUtils = null;

    public MemberConceptCount(String serviceUrl, String namedGraph, String username, String password) {
		this.serviceUrl = serviceUrl;
		this.namedGraph = namedGraph;
		this.username = username;
		this.password = password;
	    this.owlSPARQLUtils = new OWLSPARQLUtils(serviceUrl, username, password);
	    owlSPARQLUtils.set_named_graph(namedGraph);
    }


	public String construct_get_valueset_members(String named_graph, String source) {
        String prefixes = owlSPARQLUtils.getPrefixes();
        StringBuffer buf = new StringBuffer();
        buf.append(prefixes);
        buf.append("select distinct ?x1_label ?x1_code ?y_label ?y_code ").append("\n");
        buf.append("from <" + named_graph + ">").append("\n");
        buf.append("where  { ").append("\n");
        buf.append("                    ?x1 a owl:Class .").append("\n");
        buf.append("                    ?x1 :NHC0 ?x1_code .").append("\n");
        buf.append("                    ?x1 rdfs:label ?x1_label .").append("\n");
        buf.append("").append("\n");
        /*
        buf.append("                ?x1 ?p2 ?p2_value .").append("\n");
        buf.append("                ?p2 rdfs:label ?p2_label .").append("\n");
        buf.append("                ?p2 rdfs:label \"Publish_Value_Set\"^^xsd:string .").append("\n");
        //buf.append("                ?x1 ?p2 \"Yes\"^^xsd:string .").append("\n");
        buf.append("").append("\n");
        */
        buf.append("                ?x1 ?p3 ?p3_value .").append("\n");
        buf.append("                ?p3 rdfs:label ?p3_label .").append("\n");
        buf.append("                ?p3 rdfs:label \"Contributing_Source\"^^xsd:string .").append("\n");
        buf.append("                ?x1 ?p3 \"" + source + "\"^^xsd:string .").append("\n");
        buf.append("").append("\n");
        buf.append("            ?y a owl:Class .").append("\n");
        buf.append("            ?y :NHC0 ?y_code .").append("\n");
        buf.append("            ?y rdfs:label ?y_label .").append("\n");
        buf.append("").append("\n");
        buf.append("            ?y ?p ?x1 .").append("\n");
        buf.append("            ?p rdfs:label ?p_label .").append("\n");
        buf.append("            ?p rdfs:label \"Concept_In_Subset\"^^xsd:string .").append("\n");
        buf.append("").append("\n");
        buf.append("}").append("\n");
        return buf.toString();
	}


	public Vector getValuesetMembers(String named_graph, String source) {
        String query = construct_get_valueset_members(named_graph, source);
        Vector v = owlSPARQLUtils.executeQuery(query);
        if (v == null) return null;
        if (v.size() == 0) return v;
        //Utils.dumpVector(source, v);
        //v = new ParserUtils().getResponseValues(v);
        return new SortUtils().quickSort(v);
	}


	public void getCountsByValueSet(String filename) {
		long ms = System.currentTimeMillis();

		Vector v = Utils.readFile(filename);

		HashMap hmap = new HashMap();
		for (int i=0; i<v.size(); i++) {
			String t = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(t, '|');
			String key = (String) u.elementAt(0) + "|" + (String) u.elementAt(1);
			Integer int_obj = Integer.valueOf(0);
			if (hmap.containsKey(key)) {
				int_obj = (Integer) hmap.get(key);
			}
			int count = int_obj.intValue();
			count++;
			int_obj = Integer.valueOf(count);
			hmap.put(key, int_obj);
		}
		Vector keys = new Vector();
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			keys.add(key);
		}
		Vector w = new Vector();
		int total = 0;
		keys = new SortUtils().quickSort(keys);
		for (int i=0; i<keys.size(); i++) {
			String key = (String) keys.elementAt(i);
			Integer int_obj = (Integer) hmap.get(key);
			w.add(key + "|" + int_obj.intValue());
			total = total + int_obj.intValue();
		}
		w.add("\nTotal: " + total);
		Utils.saveToFile("count_" + filename, w);
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}

	//Adverse Event Outcome ICSR Terminology|C54583|23
	public static String formatCounts(String filename) {
		Vector v = Utils.readFile(filename);
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			if (u.size() == 3) {
				w.add((String) u.elementAt(0) + " (" + (String) u.elementAt(1) + ")" + "\t" + (String) u.elementAt(2));
			}
		}
		String outputfile = "v2_" + filename;
		Utils.saveToFile(outputfile, w);
		return outputfile;
	}

    public static void run(String source) {
		long ms = System.currentTimeMillis();
		String serviceUrl = ConfigurationController.serviceUrl;
		String namedGraph = ConfigurationController.namedGraph;
		String username = ConfigurationController.username;
		String password = ConfigurationController.password;

	    MemberConceptCount test = new MemberConceptCount(serviceUrl, namedGraph, username, password);
	   	Vector w = test.getValuesetMembers(namedGraph, source);

	   	System.out.println("w: " + w.size());

	   	Utils.saveToFile(source + ".txt", w);
	   	System.out.println(source + ".txt" + " generated.");

	   	test.getCountsByValueSet(source + ".txt");

	   	String v2_file = formatCounts(source + ".txt");
	   	System.out.println(v2_file + " generated.");
	}


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// post processing

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static boolean isInteger(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException nfe) {
			return false;
		}
	}

    public static boolean containsCCode(String line) {
		Vector u = StringUtils.parseData(line, '\t');
		if (u.size() == 3) {
			String displayName = (String) u.elementAt(1);
			String count_str = (String) u.elementAt(2);
			if (displayName.endsWith(")")) {
				int n1 = displayName.lastIndexOf("(");
				int n2 = displayName.lastIndexOf(")");
				String code = displayName.substring(n1+1, n2);
				//System.out.println(code);
				String numStr = code.substring(1, code.length());
				if (isInteger(numStr)) {
					return true;
				}
			}
		}
		return false;
	}

	public static HashMap getCountHashMap(String v2_FDA, String v2_EDQM_HC) {
		HashMap hmap = new HashMap();
		Vector v = Utils.readFile(v2_FDA);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '\t');
			hmap.put((String) u.elementAt(0), (String) u.elementAt(1));
		}
		v = Utils.readFile(v2_EDQM_HC);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '\t');
			hmap.put((String) u.elementAt(0), (String) u.elementAt(1));
		}
		return hmap;
	}

	public static String displayName2Code(String displayName) {
		int n1 = displayName.lastIndexOf("(");
		int n2 = displayName.lastIndexOf(")");
		String code = displayName.substring(n1+1, n2);

		return code;
	}

	public static int getCount(HierarchyHelper hh, HashMap countMap, String code) {
		int count = 0;
		String label = hh.getLabel(code);
		String knt_str = (String) countMap.get(label + " (" + code + ")");
		if (knt_str != null) {
			count = count + Integer.parseInt(knt_str);
		}
		return count;
	}

/*
		axiomfile = ConfigurationController.reportGenerationDirectory + File.separator + AXIOM_FILE;
		//rolefile = ConfigurationController.reportGenerationDirectory + File.separator + ROLE_FILE;
		//hierfile = ConfigurationController.reportGenerationDirectory + File.separator + HIER_FILE;
		//subsetfile = ConfigurationController.reportGenerationDirectory + File.separator + SUBSET_FILE;
		//owlfile = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile;
*/

	public static String extractBranches(String root) {
		String hierfile = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile;
		Vector v = Utils.readFile(hierfile);
		HierarchyHelper hh = new HierarchyHelper(v, 1);
		Vector roots = new Vector();
		//FDA Terminology (Code C131123)
		//String code = "C131123";
		String code = root;
		roots.add(code);
		Vector w = new Vector();
		for (int i=0; i<roots.size(); i++) {
			root = (String) roots.elementAt(i);
			w = hh.getTransitiveClosure(root);
		}
		Utils.saveToFile(code + ".txt", w);
		w = hh.get_transitive_closure_v4(code);
		Utils.saveToFile("parent_child_" + code + ".txt", w);
		return "parent_child_" + code + ".txt";
	}

    public static void generateCounts(String count_EDQM_HC, String count_FDA, String root) {
		Vector v2 = Utils.readFile(count_EDQM_HC);
		Vector v3 = Utils.readFile(count_FDA);

		//dumpVector("count_EDQM-HC", v2);
		//dumpVector("count_FDA", v3);

		String v2_FDA = MemberConceptCount.formatCounts(count_FDA);
		String v2_EDQM_HC = MemberConceptCount.formatCounts(count_EDQM_HC);

		HashMap countMap = getCountHashMap(v2_FDA, v2_EDQM_HC);

		String parent_child_filename = extractBranches(root);
		Vector w1 = Utils.readFile(parent_child_filename);
		HierarchyHelper hh = new HierarchyHelper(w1);

		Iterator it = countMap.keySet().iterator();
		Vector w4 = new Vector();
		while (it.hasNext()) {
			String t = (String) it.next();
			System.out.println("displayName: " + t);
			String headerCode = displayName2Code(t);
			System.out.println("displayName2Code: " + headerCode);
			int count = getCount(hh, countMap, headerCode);
			System.out.println(t + "\t" + count);
			w4.add(t + "\t" + count);
		}
		Utils.saveToFile("final_count.txt", w4);
		Vector final_vec = new Vector();
		Vector subs_level_1 = hh.getSubclassCodes(root);

		Vector subclasses = new Vector();
		for (int i=0; i<subs_level_1.size(); i++) {
			String sub_code = (String) subs_level_1.elementAt(i);
			String sub_label = hh.getLabel(sub_code);
			String key = sub_label + " (" + sub_code + ")";
			int count_sub = getCount(hh, countMap, sub_code);
			final_vec.add("\n" + key + "\t" + count_sub);

			Vector subs_level_2 = hh.getSubclassCodes(sub_code);
			if (subs_level_2 != null && subs_level_2.size() > 0) {
				for (int i2=0; i2<subs_level_2.size(); i2++) {
					String sub_code_2 = (String) subs_level_2.elementAt(i2);
					String sub_label_2 = hh.getLabel(sub_code_2);
					String key2 = sub_label_2 + " (" + sub_code_2 + ")";
					int count_sub_2 = getCount(hh, countMap, sub_code_2);
					final_vec.add(key2 + "\t" + count_sub_2);
				}
			}
		}
        Utils.saveToFile(root + "_final_count.txt", final_vec);
	}


///////////////////////////////////////////////////////////////////////////////////////////////////////////


    public static void main(String[] args) {
		run("EDQM-HC");
		run("FDA");
        generateCounts("count_EDQM-HC.txt", "count_FDA.txt", "C131123");
        generateCounts("count_EDQM-HC.txt", "count_FDA.txt", "C148636");
	}

}







































































































































