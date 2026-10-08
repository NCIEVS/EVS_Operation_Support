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
    HashMap valueSetCountMap = new HashMap();

    public MemberConceptCount(String serviceUrl, String namedGraph, String username, String password) {
		this.serviceUrl = serviceUrl;
		this.namedGraph = namedGraph;
		this.username = username;
		this.password = password;
	    this.owlSPARQLUtils = new OWLSPARQLUtils(serviceUrl, username, password);
	    owlSPARQLUtils.set_named_graph(namedGraph);
	    version = owlSPARQLUtils.getVersion(namedGraph);
	    valueSetCountMap = new HashMap();
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
	public String formatCounts(String filename) {
		Vector v = Utils.readFile(filename);
		Vector w = new Vector();
		w.add("Terminology\tCount");
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			if (u.size() == 3) {
				w.add((String) u.elementAt(0) + " (" + (String) u.elementAt(1) + ")" + "\t" + (String) u.elementAt(2));
				Integer count = Integer.valueOf(Integer.parseInt((String) u.elementAt(2)));

				valueSetCountMap.put((String) u.elementAt(0) + " (" + (String) u.elementAt(1) + ")", count);
			}
		}
		String outputfile = "v2_" + filename;
		Utils.saveToFile(outputfile, w);
		return outputfile;
	}

    public void run(String source) {
		long ms = System.currentTimeMillis();
	   	Vector w = getValuesetMembers(namedGraph, source);
	   	Utils.saveToFile(source + ".txt", w);
	   	System.out.println(source + ".txt" + " generated.");
	   	getCountsByValueSet(source + ".txt");
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
		if (!displayName.endsWith(")")) {
			return null;
		}
		int n1 = displayName.lastIndexOf("(");
		String code = displayName.substring(n1+1, displayName.length()-1);
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

    public void generateCounts(String count_EDQM_HC, String count_FDA, String root) {
		Vector v2 = Utils.readFile(count_EDQM_HC);
		Vector v3 = Utils.readFile(count_FDA);
		String v2_FDA = formatCounts(count_FDA);
		String v2_EDQM_HC = formatCounts(count_EDQM_HC);

		HashMap countMap = getCountHashMap(v2_FDA, v2_EDQM_HC);

		String parent_child_filename = extractBranches(root);
		Vector w1 = Utils.readFile(parent_child_filename);
		HierarchyHelper hh = new HierarchyHelper(w1);

		Iterator it = countMap.keySet().iterator();
		Vector w4 = new Vector();
		while (it.hasNext()) {
			String t = (String) it.next();
			try {
				String headerCode = displayName2Code(t);
				int count = getCount(hh, countMap, headerCode);
				w4.add(t + "\t" + count);
			} catch (Exception ex) {

			}
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



	public String construct_get_concepts_with_contributing_source(String named_graph, String cs) {
        String prefixes = owlSPARQLUtils.getPrefixes();
        StringBuffer buf = new StringBuffer();
        buf.append(prefixes);
        buf.append("select distinct ?x1_code ").append("\n");
        buf.append("from <" + named_graph + ">").append("\n");
        buf.append("where  { ").append("\n");
        buf.append("            ?x1 a owl:Class .").append("\n");
        buf.append("            ?x1 :NHC0 ?x1_code .").append("\n");
        buf.append("            ?x1 ?p3 ?p3_value .").append("\n");
        buf.append("            ?p3 rdfs:label ?p3_label .").append("\n");
        buf.append("            ?p3 rdfs:label \"Contributing_Source\"^^xsd:string .").append("\n");
        buf.append("            ?x1 ?p3 \"" + cs + "\"^^xsd:string .").append("\n");
        buf.append("}").append("\n");
        return buf.toString();
	}

	public int getConceptsWithContributingSource(String named_graph, String cs) {
        String query = construct_get_concepts_with_contributing_source(named_graph, cs);
        Vector v = owlSPARQLUtils.executeQuery(query);
        if (v == null) return -1;
        if (v.size() == 0) return 0;
        return v.size();
	}

	public String construct_get_valueset_size(String named_graph, String subsetCode) {
        String prefixes = owlSPARQLUtils.getPrefixes();
        StringBuffer buf = new StringBuffer();
        buf.append(prefixes);
        buf.append("select distinct ?x1_code ").append("\n");
        buf.append("from <" + named_graph + ">").append("\n");
        buf.append("where  { ").append("\n");
        buf.append("            ?x1 a owl:Class .").append("\n");
        buf.append("            ?x1 :NHC0 ?x1_code .").append("\n");
        buf.append("            ?y a owl:Class .").append("\n");
        buf.append("            ?y :NHC0 ?y_code .").append("\n");
        buf.append("            ?y :NHC0 \"" + subsetCode + "\"^^xsd:string .").append("\n");
        buf.append("            ?x1 ?p ?y .").append("\n");
        buf.append("            ?p rdfs:label ?p_label .").append("\n");
        buf.append("            ?p rdfs:label \"Concept_In_Subset\"^^xsd:string .").append("\n");
        buf.append("}").append("\n");
        return buf.toString();
	}

	public int getValuesetSize(String named_graph, String subsetCode) {
        String query = construct_get_valueset_size(named_graph, subsetCode);
        Vector v = owlSPARQLUtils.executeQuery(query);
        if (v == null) return -1;
        if (v.size() == 0) return 0;
        return v.size();
	}


	public String construct_get_valuesets_with_contributing_Source(String named_graph, String cs) {
        String prefixes = owlSPARQLUtils.getPrefixes();
        StringBuffer buf = new StringBuffer();
        buf.append(prefixes);
        buf.append("select distinct ?x1_label ?x1_code ").append("\n");
        buf.append("from <" + named_graph + ">").append("\n");
        buf.append("where  { ").append("\n");
        buf.append("            ?x1 a owl:Class .").append("\n");
        buf.append("            ?x1 :NHC0 ?x1_code .").append("\n");
        buf.append("            ?x1 rdfs:label ?x1_label .").append("\n");

        buf.append("            ?y a owl:Class .").append("\n");
        //buf.append("            ?y :NHC0 ?y_code .").append("\n");

        buf.append("            ?p rdfs:label ?p_label .").append("\n");
        buf.append("            ?p rdfs:label \"Concept_In_Subset\"^^xsd:string .").append("\n");
        buf.append("            ?y ?p ?x1 .").append("\n");

        buf.append("            ?p3 rdfs:label ?p3_label .").append("\n");
        buf.append("            ?p3 rdfs:label \"Contributing_Source\"^^xsd:string .").append("\n");
        buf.append("            ?x1 ?p3 \"" + cs + "\"^^xsd:string .").append("\n");
        buf.append("}").append("\n");
        return buf.toString();
	}

	public int getValuesetCountWithContributingSource(String named_graph, String source) {
        String query = construct_get_valuesets_with_contributing_Source(named_graph, source);
        Vector v = owlSPARQLUtils.executeQuery(query);
        if (v == null) return -1;
        if (v.size() == 0) return 0;
        return v.size();
	}

	public Vector getValuesetsWithContributingSource(String named_graph, String source) {
        String query = construct_get_valuesets_with_contributing_Source(named_graph, source);
        Vector v = owlSPARQLUtils.executeQuery(query);
        if (v == null) return null;
        if (v.size() == 0) return new Vector();
        return v;
	}

	public String line2DisplayName(String line) {
		Vector u = StringUtils.parseData(line, '|');
		String label = (String) u.elementAt(0);
		String code = (String) u.elementAt(1);
		return label + " (" + code + ")";
	}

	public Vector GenerateReport(boolean qa_mode) {
		Vector w = new Vector();
		w.add("Terminology	NCI Thesaurus (" + version + ")");
		w.add("FDA TERMINOLOGY	" + StringUtils.getToday("MM/dd/YYYY"));

		int fda_count = getConceptsWithContributingSource(namedGraph, "FDA");
		//w.add("fda_count: " + fda_count);
		w.add("Total Number of FDA Data Items (Concepts) currently maintained in the NCI Thesaurus  (Contributing Source = FDA)\t" + fda_count);

		int fda_valueset_count = getValuesetCountWithContributingSource(namedGraph, "FDA");
		//w.add("fda_valueset_count: " + fda_valueset_count);
		w.add("Number of FDA Data Elements (Value Sets) Currently Being Maintained in the NCI Thesaurus	" + fda_valueset_count);
        w.add("All the concepts are reviewed every month because we have to post the changes, and in order to find out if there are changes we review the comparisons. We do not keep records on changes, which include amendments  or adjucations.");

		int EDQM_HC_count = getConceptsWithContributingSource(namedGraph, "EDQM-HC");
		//w.add("EDQM_HC_count: " + EDQM_HC_count);

		int EDQM_valueset_count = getValuesetCountWithContributingSource(namedGraph, "EDQM-HC");
		//w.add("EDQM_valueset_count: " + EDQM_valueset_count);

		w.add("Total Number of EDQM-HC Data Items (Concepts) currently maintained in the NCI Thesaurus for FDA 	" + EDQM_HC_count);
		w.add("Number of EDQM-HC Data Elements (Value Sets) Currently Being Maintained in the NCI Thesaurus for FDA 	" + EDQM_valueset_count);
		w.add("\n");

		Vector fda_valuesets = getValuesetsWithContributingSource(namedGraph, "FDA");
		for (int i=0; i<fda_valuesets.size(); i++) {
			String line = (String) fda_valuesets.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(1);
			int count = getValuesetSize(this.namedGraph, code);
			String displayName = line2DisplayName(line);

			if (qa_mode) {
				Integer int_obj = (Integer) valueSetCountMap.get(displayName);
				int number = int_obj.intValue();
                String str = Integer.toString(number);
				w.add(displayName + "\t" + count + "\t" + str);
			} else {
				w.add(displayName + "\t" + count);
			}
		}
		w.add("\n");

		Vector EDQM_HC_valuesets = getValuesetsWithContributingSource(namedGraph, "EDQM-HC");
		for (int i=0; i<EDQM_HC_valuesets.size(); i++) {
			String line = (String) EDQM_HC_valuesets.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(1);
			int count = getValuesetSize(this.namedGraph, code);
			String displayName = line2DisplayName(line);

			if (qa_mode) {
				Integer int_obj = (Integer) valueSetCountMap.get(displayName);
				int number = int_obj.intValue();
                String str = Integer.toString(number);
				w.add(displayName + "\t" + count + "\t" + str);
			} else {
				w.add(displayName + "\t" + count);
			}
		}
        return w;
	}

    public static void run(String serviceUrl, String named_graph, String username, String password) {
		MemberConceptCount test = new MemberConceptCount(serviceUrl, named_graph, username, password);
		test.run("EDQM-HC");
		test.run("FDA");
        test.generateCounts("count_EDQM-HC.txt", "count_FDA.txt", "C131123");
        test.generateCounts("count_EDQM-HC.txt", "count_FDA.txt", "C148636");
		Vector v = test.GenerateReport(false);
		Utils.saveToFile("v1_fda_statistics_" + StringUtils.getToday() + ".txt", v);
	}

	public static void main(String[] args) {
		String serviceUrl = args[0];
		String named_graph = args[1];
		String username = args[2];
		String password = args[3];
		run(serviceUrl, named_graph, username, password);
	}
}




















































































































