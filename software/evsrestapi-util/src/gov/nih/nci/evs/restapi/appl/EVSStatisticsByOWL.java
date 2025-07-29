package gov.nih.nci.evs.restapi.appl;
import gov.nih.nci.evs.restapi.config.*;
import gov.nih.nci.evs.restapi.util.*;
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
import org.apache.commons.codec.binary.Base64;
import java.nio.charset.Charset;

import java.time.Duration;
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


public class EVSStatisticsByOWL {
	String named_graph = null;
	int table_number = 0;
	Vector all_properties = null;
	HashSet publishedValueSets = null;

	String prefixes = null;
	static String PUBLISH_VALUE_SET = "Publish_Value_Set";

	static String[] ANNOTATED_TARGETS = null;
	static String[] ANNOTATED_TARGET_CODES = null;
	static List ANNOTATED_TARGET_LIST = null;
	static HashMap ANNOTATED_TARGET_HASHMAP = null;
	static HashMap INV_ANNOTATED_TARGET_HASHMAP = null;

	Vector properties = null;
	HashMap propertyCode2NameHashMap = new HashMap();
	HashMap propertyName2CodeHashMap = new HashMap();

	HashMap roleCode2NameHashMap = new HashMap();
	HashMap roleName2CodeHashMap = new HashMap();

	Vector table_data = null;
	String version = null;

	String named_graph_id = ":NHC0";
	Vector roots = null;

	HierarchyHelper hh = null;
    static String NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile; //"ThesaurusInferred_forTS.owl";
	static String PARENT_CHILD_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile; // "parent_child.txt";
	static String RESTRICTION_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.rolefile; //"roles.txt";
	static String AXIOM_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.axiomfile;

	HashSet retired_concepts = new HashSet();
	HashMap valueset2ContributingSourceMap = new HashMap();
	HashMap valuesetCode2NameMap = new HashMap();
	HashMap valuesetName2CodeMap = new HashMap();
	HashMap code2LabelMap = new HashMap();
	HashMap contributingSourceMap = new HashMap();

	static HashMap propertyCode2CountMap = null;
	static OWLScanner scanner = null;
	HashMap propertyMap = null;

    public HashMap getPropertyCode2CountMap() {
		HashMap countMap = new HashMap();
		Iterator it = propertyMap.keySet().iterator();
		while (it.hasNext()) {
			String prop_code = (String) it.next();
			//if (prop_code.startsWith("P")) {
				HashMap hmap = (HashMap) propertyMap.get(prop_code);
				Iterator it2 = hmap.keySet().iterator();
				while (it2.hasNext()) {
					String code = (String) it2.next();
					Vector v = (Vector) hmap.get(code);
					Integer int_obj = Integer.valueOf(0);
					if (countMap.containsKey(prop_code)) {
						int_obj = (Integer) countMap.get(prop_code);
					}
					int count = int_obj.intValue();
					int_obj = Integer.valueOf(count+v.size());
					countMap.put(prop_code, int_obj);
				}
			//}
		}
		return countMap;
	}

    static {
		ANNOTATED_TARGETS = new String[] {"term-name", "go-term", "Target_Term"};
		ANNOTATED_TARGET_CODES = new String[] {"P382", "P388", "P392"};
		ANNOTATED_TARGET_LIST = Arrays.asList(ANNOTATED_TARGET_CODES);
		ANNOTATED_TARGET_HASHMAP = new HashMap();
		ANNOTATED_TARGET_HASHMAP.put("P382", "P90");
		ANNOTATED_TARGET_HASHMAP.put("P388", "P211");
		ANNOTATED_TARGET_HASHMAP.put("P392", "P375");

		INV_ANNOTATED_TARGET_HASHMAP = new HashMap();
		INV_ANNOTATED_TARGET_HASHMAP.put("P90", "P382");
		INV_ANNOTATED_TARGET_HASHMAP.put("P211", "P388");
		INV_ANNOTATED_TARGET_HASHMAP.put("P375", "P392");

        if (ReportSubmitter.getOWLScanner() != null) {
			scanner = ReportSubmitter.getOWLScanner();
		} else {
			scanner = new OWLScanner(NCIT_OWL);
		}

		File file = new File(RESTRICTION_FILE);
		Vector roles = null;
		if (!file.exists()) {
		    roles = scanner.extractOWLRestrictions(scanner.get_owl_vec());
		    Utils.saveToFile(RESTRICTION_FILE, roles);
		}
	}

	public String get_ncit_version() {
		String ncit_version = scanner.extractVersion();
		return ncit_version;
	}


	public HashMap getPropertyMap(Vector v) {
        HashMap hmap = new HashMap();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(0);
			String prop_label = (String) u.elementAt(1);
			String value = (String) u.elementAt(2);
			HashMap map = new HashMap();
			if (hmap.containsKey(prop_label)) {
				map = (HashMap) hmap.get(prop_label);
			}
			Vector w = new Vector();
			if (prop_label.startsWith("P")) {
				if (map.containsKey(code)) {
					w = (Vector) map.get(code);
				}
				if (!w.contains(value)) {
					w.add(value);
				}
				map.put(code, w);
			} else if (prop_label.startsWith("A")) {
				if (map.containsKey(value)) {
					w = (Vector) map.get(value);
				}
				if (!w.contains(code)) {
					w.add(code);
				}
				map.put(value, w);
			}
		}
		return hmap;
	}

	public EVSStatisticsByOWL(String owlfile) {
		this.table_number = 0;
		all_properties = scanner.extractAllProperties(scanner.get_owl_vec());
		propertyMap = scanner.getPropertyMap(scanner.get_owl_vec());
		propertyCode2CountMap = getPropertyCode2CountMap();
		//all_properties.clear();

		this.version  = get_ncit_version();//metadataUtils.getVocabularyVersion(named_graph);
        System.out.println("NCI Thesaurus version: " + version);
		System.out.println("getSupportedProperties ...");
		properties = scanner.extractAnnotationProperties(scanner.get_owl_vec());
		System.out.println("properties: " + properties.size());
		Utils.saveToFile("properties.txt", properties);

		propertyCode2NameHashMap = new HashMap();
		propertyName2CodeHashMap = new HashMap();
        for (int i=0; i<properties.size(); i++) {
			String line = (String) properties.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String property_name = (String) u.elementAt(1);
			String property_code = (String) u.elementAt(0);
			propertyCode2NameHashMap.put(property_code, property_name);
			propertyName2CodeHashMap.put(property_name, property_code);
		}
		this.table_data = new Vector();
		this.roots = scanner.getRootCodes();//getRoots(named_graph, true);

        System.out.println("getObjectProperties ...");
		Vector supported_roles = scanner.extractObjectProperties(scanner.get_owl_vec());//getObjectProperties(named_graph);
		roleCode2NameHashMap = new HashMap();
		roleName2CodeHashMap = new HashMap();
        for (int i=0; i<supported_roles.size(); i++) {
			String line = (String) supported_roles.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String role_name = (String) u.elementAt(1);
			String role_code = (String) u.elementAt(0);
			roleCode2NameHashMap.put(role_code, role_name);
			roleName2CodeHashMap.put(role_name, role_code);
		}

		//retired_concepts
		retired_concepts = new HashSet();
	    String property_name = "Concept_Status";
	    String property_value = "Retired_Concept";
	    System.out.println("findConceptsWithProperty property_value: " + property_value);

        Vector w = scanner.extractPropertyData("P310");
		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String status = (String) u.elementAt(u.size()-1);
			if (status.compareTo(property_value) == 0) {
				String code = (String) u.elementAt(0);
				retired_concepts.add(code);
			}
		}

		System.out.println("Number of retired concepts: " + retired_concepts.size());
		valuesetCode2NameMap = new HashMap();
		valuesetName2CodeMap = new HashMap();
		valueset2ContributingSourceMap = new HashMap();

		String prop_code = "rdfs:label";
		System.out.println(prop_code);
		//10-Point Importance Scale|C153596|Importance Score 8|C153606

		w = Utils.readFile(PARENT_CHILD_FILE);
		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(1);
			String label = (String) u.elementAt(0);
			label = HTMLDecoder.decode(label);
			code2LabelMap.put(code, label);

			code = (String) u.elementAt(3);
			label = (String) u.elementAt(2);
			label = HTMLDecoder.decode(label);
			code2LabelMap.put(code, label);
		}
		/*
		w = scanner.extractPropertyData(prop_code);
		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(0);
			String label = (String) u.elementAt(1);
			label = HTMLDecoder.decode(label);
			code2LabelMap.put(code, label);
		}
		*/


		prop_code = "P322"; //Contributing_Source
		System.out.println(prop_code);
		w = scanner.extractPropertyData(prop_code);
		Utils.saveToFile(prop_code + ".txt", w);
		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(0);
			Vector w1 = new Vector();
			if (contributingSourceMap.containsKey(code)) {
				w1 = (Vector) contributingSourceMap.get(code);
			}
			for (int j=1; j<u.size(); j++) {
				String cs = (String) u.elementAt(j);
				if (!w1.contains(cs)) {
					w1.add(cs);
				}
			}
			contributingSourceMap.put(code, w1);
		}

        prop_code = "P372"; //Publish_Value_Set
        System.out.println(prop_code);
        publishedValueSets = new HashSet();
		w = scanner.extractPropertyData(prop_code);

		Utils.saveToFile(prop_code + ".txt", w);

		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(0);
			String yesOrNo = (String) u.elementAt(1);

			String label = (String) code2LabelMap.get(code);
			valuesetCode2NameMap.put(code, label);
			valuesetName2CodeMap.put(label, code);

			Vector cs_vec = (Vector) contributingSourceMap.get(code);
			if (cs_vec == null) {
				System.out.println("cs_vec == null - " + code);
			} else {
				String cs = (String) cs_vec.elementAt(0);
				valueset2ContributingSourceMap.put(code, cs);
			}

			if (yesOrNo.compareTo("Yes") == 0) {
				publishedValueSets.add(code);
			}
		}
		System.out.println("EVSStatistics instantiated.");
	}

	public int getPropertyCount(String propertyCode) {
		if (!propertyCode2CountMap.containsKey(propertyCode)) {
			return 0;
		}
		Integer int_obj = (Integer) propertyCode2CountMap.get(propertyCode);
		return int_obj.intValue();
	}

	public void addTitle(String title) {
		table_data.add("<title>" + title);
	}

	public boolean is_retired(String code) {
		return retired_concepts.contains(code);
	}

	public void addTable(String tableName, Vector th_vec, Vector data) {
		table_data.add("<table>" + tableName);
		for (int i=0; i<th_vec.size(); i++) {
			String th = (String) th_vec.elementAt(i);
			table_data.add("<th>" + th);
		}
		table_data.add("<data>");
		table_data.addAll(data);
		table_data.add("</data>");
        table_data.add("</table>");
	}

    public void addFooter() {
		table_data.add("<footer>(Source; NCI Thesaurus, version " + this.version + ")");
	}

    public boolean is_ANNOTATED_TARGET_CODES(String propertyCode) {
		return ANNOTATED_TARGET_LIST.contains(propertyCode);
	}

	public static void dumpVector(String label, Vector v) {
		System.out.println(label);
		for (int i=0; i<v.size(); i++) {
			System.out.println((String) v.elementAt(i));
		}
	}

	public String construct_get_associations(String named_graph) {
		return null;//_get_associations(named_graph, null);
	}

	public Vector getAssociations() {
		Vector w = new Vector();
		Iterator it = propertyCode2NameHashMap.keySet().iterator();
		while (it.hasNext()) {
			String prop_code = (String) it.next();
			if (prop_code.startsWith("A")) {
				Vector v = getAssociations(prop_code);
				w.addAll(v);
			}
		}
		return w;
	}


	public Vector getAssociations(String code) {
		Vector v = new Vector();
		String label = (String) propertyCode2NameHashMap.get(code);
		HashMap hmap = (HashMap) propertyMap.get(code);
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String targetCode = (String) it.next();
			String targetLabel = (String) code2LabelMap.get(targetCode);
			Vector w = (Vector) hmap.get(targetCode);
			for (int i=0; i<w.size(); i++) {
				String srcCode = (String) w.elementAt(i);
				String srcLabel = (String) code2LabelMap.get(srcCode);
				v.add(srcLabel + "|" + srcCode + "|" + label + "|" + code + "|" + targetLabel + "|" + targetCode);
			}
		}
		return v;
	}

	public Vector getRoleTargets(String named_graph, String code, String roleName) {
        String query = null;//_get_role_targets(named_graph, code, roleName);
        return null;//executeQuery(query);
	}

	public Vector getHierarchicalRelationships(String named_graph) {
		//return executeQuery(construct_get_hierarchical_relationships(named_graph));
		return Utils.readFile(PARENT_CHILD_FILE);
	}

	public Vector getConceptsWithAnnotationProperty(String propertyName) {
		String prop_code = (String) propertyName2CodeHashMap.get(propertyName);
		Vector v = new Vector();
		HashMap hmap = (HashMap) propertyMap.get(prop_code);
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String code = (String) it.next();
			String label = (String) code2LabelMap.get(code);
			Vector values = (Vector) hmap.get(code);
			for (int i=0; i<values.size(); i++) {
				String value = (String) values.elementAt(i);
				v.add(label + "|" + code + "|" + propertyName + "|" + value);
			}
		}
		return v;
	}


	public Vector getConceptsWithAnnotationPropertyMatching(String propertyName, String propertyValue) {
		String prop_code = (String) propertyName2CodeHashMap.get(propertyName);
		Vector v = new Vector();
		HashMap hmap = (HashMap) propertyMap.get(prop_code);
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String code = (String) it.next();
			String label = (String) code2LabelMap.get(code);
			Vector values = (Vector) hmap.get(code);
			for (int i=0; i<values.size(); i++) {
				String value = (String) values.elementAt(i);
				if (value.compareTo(propertyValue) == 0) {
					v.add(label + "|" + code + "|" + propertyName + "|" + value);
				}
			}
		}
		return v;
	}

	public Vector getSupportedProperties(String named_graph) {
		return scanner.extractAnnotationProperties(scanner.get_owl_vec());
	}

    public Vector getConceptsWithProperty(String property_code) {
        String property_label = (String) propertyCode2NameHashMap.get(property_code);
        HashMap hmap = (HashMap) propertyMap.get(property_code);
        Vector v = new Vector();
        Iterator it = hmap.keySet().iterator();
        while (it.hasNext()) {
			String code = (String) it.next();
			String label = (String) code2LabelMap.get(code);
			Vector values = (Vector) hmap.get(code);
			for (int i=0; i<values.size(); i++) {
				String value = (String) values.elementAt(i);
				v.add(label + "|" + code + "|" + property_label + "|" + value);
			}
		}
        return v;
	}

    public HashMap createQualifierCode2PropertyCodeHashMap(Vector v) {
		HashMap hmap = new HashMap();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String prop_code = (String) u.elementAt(2);
			if (u.size() > 3) {
				for (int j=4; j<u.size(); j++) {
					String t = (String) u.elementAt(j);
					Vector u2 = StringUtils.parseData(t, '$');
					String qual_code = (String) u2.elementAt(0);
					Vector w = new Vector();
					if (hmap.containsKey(qual_code)) {
						w = (Vector) hmap.get(qual_code);
					}
					if (!w.contains(prop_code)) {
						w.add(prop_code);
					}
					hmap.put(qual_code, w);
				}
		    }
		}
		return hmap;
	}


	public Vector getSupportedQualifiers(String named_graph) {
		String query = null;//_get_supported_qualifiers(named_graph);
		Vector v = null;//(query);
		v = new SortUtils().quickSort(v);
		return v;
	}

	public int getAnnotatedTargetCount(String named_graph, String propertyCode, String qualifierCode) {
		String query = null;//_get_annotated_target_count(named_graph, propertyCode, qualifierCode);
		Vector v = null;//(query);
		if (v == null || v.size() == 0) return 0;
		return v.size();
	}

	public int getAnnotatedTargetCount(String named_graph, String propertyCode) {
		String query = null;//_get_annotated_target_count(named_graph, propertyCode);
		Vector v = null;//(query);
		if (v == null || v.size() == 0) return 0;
		return v.size();
	}

	public Vector getDistinctPropertyQualifierValues(String named_graph, String code, String propertyCode, String qualifierCode) {
		String query = null;//_get_annotated_target_count(named_graph, propertyCode);
		Vector v = null;//(query);
		v = new SortUtils().quickSort(v);
		return v;
	}

	public Vector getDistinctPropertyValues(String named_graph, String propertyName) {
        String query = null;//_get_distinct_property_values(named_graph, propertyName);
        Vector v = null;//(query);
        v = new SortUtils().quickSort(v);
        return v;
	}

	public Vector getConceptsWithPropertyValue(String named_graph, String propertyName, String propertyValue) {
		String query = null;//_get_concepts_with_property_value(named_graph, propertyName, propertyValue);
		Vector v = null;//(query);
		v = removeDuplicates(v);
		return v;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static HashMap getPropertyCountHashMap(String owlfile) {
		OWLScanner owlscanner = new OWLScanner(owlfile);
		Vector v = owlscanner.extractProperties(owlscanner.get_owl_vec());
		HashMap propertyCountHashMap = new HashMap();
		for (int i=0; i<v.size(); i++) {
			String line = (String)v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String propertyCode = (String) u.elementAt(1);
			Integer int_obj = Integer.valueOf(0);
			if (propertyCountHashMap.containsKey(propertyCode)) {
				int_obj = (Integer) propertyCountHashMap.get(propertyCode);
			}
			int count = int_obj.intValue();
            int_obj = Integer.valueOf(count+1);
            propertyCountHashMap.put(propertyCode, int_obj);
		}
		return propertyCountHashMap;
	}

	public Vector getPropertyCounts(Vector w) {
		HashMap hmap = createQualifierCode2PropertyCodeHashMap(w);
		int count = 0;
        Vector v = new Vector();
        for (int i=0; i<properties.size(); i++) {
			String line = (String) properties.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String property_name = (String) u.elementAt(0);
			String property_code = (String) u.elementAt(1);

            if (is_ANNOTATED_TARGET_CODES(property_code)) {
				String prop_code = (String) ANNOTATED_TARGET_HASHMAP.get(property_code);
				int knt = getAnnotatedTargetCount(named_graph, prop_code);
				count = count + knt;
				v.add(property_name + " (" + property_code + ")" + "|" + knt);
				System.out.println("(*)" + property_name + " (" + property_code + ")" + "|" + knt);

			} else if (property_code.startsWith("P") && !hmap.containsKey(property_code)) {
				w = null;
				try {
					Integer prop_count_obj = (Integer) propertyCode2CountMap.get(property_code);
					int prop_count = prop_count_obj.intValue();
					count = count + prop_count;
					v.add(property_name + " (" + property_code + ")" + "|" +  prop_count);

					System.out.println(property_name + " (" + property_code + ")" + "|" + prop_count);
				} catch (Exception ex) {
					int knt = getPropertyCount(property_code);
					count = count + knt;
					v.add(property_name + " (" + property_code + ")" + "|" +  knt);
					System.out.println("(**)" + property_name + " (" + property_code + ")" + "|" + knt);
				}

			} else if (property_code.startsWith("P") && hmap.containsKey(property_code)) {
				Vector v2 = (Vector) hmap.get(property_code);
				for (int k=0; k<v2.size(); k++) {
					String prop_code = (String) v2.elementAt(k);
					int knt = getAnnotatedTargetCount(named_graph, prop_code, property_code);
					count = count + knt;
					String label1 = property_name + " (" + property_code + ")";
					String prop_name = (String) propertyCode2NameHashMap.get(prop_code);
					String label2 = prop_name + " (" + prop_code + ")";
					v.add(label1 + " of " + label2 + "|" + knt);
					System.out.println(label1 + " of " + label2 + "|" + knt);
				}
			}
		}
		v = new SortUtils().quickSort(v);
		v.add("Total" + "|" + count);
		return v;
	}

	public static Vector findDuplicates(Vector v) {
		Vector w = new Vector();
		HashSet hset = new HashSet();
		for (int i=0; i<v.size(); i++) {
			String t = (String) v.elementAt(i);
			if (!hset.contains(t)) {
				hset.add(t);
			} else {
				w.add(t);
			}
		}
		return w;
	}

	public Vector getPropertyValueCounts(String propertyName) {
		String propertyCode = (String) propertyName2CodeHashMap.get(propertyName);
		return getPropertyValueTableData(propertyCode);
	}

	public static Vector dumpTallies(HashMap hmap) {
		Vector v = new Vector();
		int count = 0;
		Vector keys = new Vector();
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			keys.add(key);
		}
		keys = new SortUtils().quickSort(keys);
		it = hmap.keySet().iterator();
		for (int i=0; i<keys.size(); i++) {
			String key = (String) keys.elementAt(i);
			Integer int_obj = (Integer) hmap.get(key);
			v.add(key + "|" + Integer.valueOf(int_obj));
			count = count + Integer.valueOf(int_obj);
		}
		v.add("Total|" + count);
		return v;
	}

	public String construct_get_value_sets(String named_graph) {
		boolean publishedOnly = true;
		return null;//_get_value_sets(named_graph, publishedOnly);
	}

	public Vector getValueSets(String named_graph) {
		boolean publishedOnly = true;
		String query = null;//_get_value_sets(named_graph, publishedOnly);
		Vector v = null;//(query);
		return v;
	}

	public Vector getValueSets(String named_graph, boolean publishedOnly) {
		String query = null;//_get_value_sets(named_graph, publishedOnly);
		Vector v = null;//(query);
		return v;
	}

	public String construct_get_value_sets_with_contributing_source(String named_graph) {
		boolean publishedOnly = true;
		return null;//_get_value_sets_with_contributing_source(named_graph, publishedOnly);
	}

	public Vector getValueSetsWithContributingSource(String named_graph) {
		boolean publishedOnly = true;
		String query = null;//_get_value_sets_with_contributing_source(named_graph, publishedOnly);
		Vector v = null;//(query);
		return v;
	}

	public Vector getValueSetsWithContributingSource(String named_graph, boolean publishedOnly) {
		String query = null;//_get_value_sets_with_contributing_source(named_graph, publishedOnly);
		Vector v = null;//(query);
		return v;
	}

	public Vector getConceptsInSubset(String named_graph, String code, boolean codeOnly) {
		return null;//executeQuery(construct_get_concepts_in_subset(named_graph, code, codeOnly));
	}

	public String construct_get_value_set_data(String named_graph) {
		boolean publishedOnly = true;
		return null;//_get_value_set_data(named_graph, publishedOnly);
	}

	public Vector getValueSetData() {
		boolean publishedOnly = true;
		return create_value_set_data(publishedOnly);
	}

    public Vector create_value_set_data(boolean publishedOnly) {
		Vector v = new Vector();
		HashMap hmap = (HashMap) propertyMap.get("A8");
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String valueSetCode = (String) it.next();
			String published = "Yes";
			if (!publishedValueSets.contains(valueSetCode)) {
				published = "No";
			}
			String cs = (String) valueset2ContributingSourceMap.get(valueSetCode);
			boolean include = true;
			if (publishedOnly) {
				if (!publishedValueSets.contains(valueSetCode)) {
					include = false;
				}
			}
			if (include) {
				Vector codes = (Vector) hmap.get(valueSetCode);
				for (int i=0; i<codes.size(); i++) {
					String code = (String) codes.elementAt(i);
					v.add((String) code2LabelMap.get(code) + "|"
					    + code  + "|"
					    + "Concept_In_Subset" +  "|"
					    + (String) code2LabelMap.get(valueSetCode) + "|"
					    + valueSetCode  + "|"
					    + "Contributing_Source" +  "|"
					    + cs  + "|"
					    + "Publish_Value_Set"  + "|"
					    + published);
				}
			}
		}
		return v;
	}

	public Vector getValueSetData(boolean publishedOnly) {
		Vector v = null;//(construct_get_value_set_data(named_graph, publishedOnly));
		v = create_value_set_data(publishedOnly);
		Utils.saveToFile("value_set_data.txt", v);
		System.out.println("Fle saved as  " + "value_set_data.txt");

		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(1);
			boolean include = true;
			boolean is_retired = is_retired(code);

			String subset_code = (String) u.elementAt(4);
			boolean is_subset_code_retired = is_retired(subset_code);

			if (publishedOnly) {
				if (!publishedValueSets.contains(subset_code)) {
					include = false;
				}
			}
			if (include && !is_retired && !is_subset_code_retired) {
				String name = (String) u.elementAt(0);
				String Concept_In_Subset = (String) u.elementAt(2);
				String subset_name = (String) u.elementAt(3);
				String Contributing_Source = (String) u.elementAt(5);
				String Contributing_Source_value = (String) u.elementAt(6);

				if (Contributing_Source_value.compareTo("null") == 0) {
					Contributing_Source_value = "No External Source";
				}

				String Publish_Value_Set = (String) u.elementAt(7);
				String Publish_Value_Set_value = (String) u.elementAt(8);
				String t = name + "|" + code + "|" + Concept_In_Subset + "|" + subset_name + "|" + subset_code
				    + "|Contributing_Source|" + Contributing_Source_value + "|"
				    + Publish_Value_Set + "|" + Publish_Value_Set_value;

				w.add(t);

			}
		}
		return w;
	}

	public static HashMap getTallyHashMap(String datafile) {
		HashMap hmap = new HashMap();
		Vector w = Utils.readFile(datafile);
		for (int i=0; i<w.size(); i++) {
			String t = (String) w.elementAt(i);
			Vector u = StringUtils.parseData(t, '|');
			String roleName = (String) u.elementAt(2);
			String roleCode = (String) u.elementAt(3);
			String key = roleName + " (" + roleCode + ")";
			Integer int_obj = Integer.valueOf(0);
			if (hmap.containsKey(key)) {
				int_obj = (Integer) hmap.get(key);
			}
			int count = Integer.valueOf(int_obj);
			hmap.put(key, Integer.valueOf(count+1));
		}
		return hmap;
	}


    public Vector removeDuplicates(Vector v) {
		if (v == null) return v;
		HashSet hset = new HashSet();
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String t = (String) v.elementAt(i);
			if (!hset.contains(t)) {
				hset.add(t);
			}
		}
		Iterator it = hset.iterator();
		while (it.hasNext()) {
			String t = (String) it.next();
			w.add(t);
		}
		return w;
	}

	public Vector getPropertyValueTableData(String propertyCode) {
		Vector v = getConceptsWithProperty(propertyCode);
		return getPropertyValueTableData(v);
	}

	public Vector getPropertyValueTableData(Vector v) {
		return generatePropertyValuetableData(getPropertyValueHashMap(v));
	}

	public void dumpHashMap(HashMap hmap) {
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			Integer value = (Integer) hmap.get(key);
			System.out.println(key + " --> " + Integer.valueOf(value));
        }
	}


	public HashMap getPropertyValueHashMap(Vector v) {
		HashMap hmap = new HashMap();
		if (v == null) return hmap;
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String key = (String) u.elementAt(u.size()-1);
			Integer int_obj = Integer.valueOf(0);
			if (hmap.containsKey(key)) {
			    int_obj = (Integer) hmap.get(key);
			}
			int knt = Integer.valueOf(int_obj);
			knt++;

			hmap.put(key, Integer.valueOf(knt));
		}
		return hmap;
	}

	public Vector generatePropertyValuetableData(HashMap hmap) {
		return generatePropertyValuetableData(null, hmap);
	}

	public Vector generatePropertyValuetableData(Vector values, HashMap hmap) {
		Vector w = new Vector();
		if (hmap == null) return null;
		if (values != null) {
			for (int k=0; k<values.size(); k++) {
				String value = (String) values.elementAt(k);
				if (!hmap.containsKey(value)) {
					hmap.put(value, Integer.valueOf(0));
				}
			}
		}
		Vector keys = new Vector();
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			keys.add(key);
		}
		keys = new SortUtils().quickSort(keys);
		int total = 0;
		for (int i=0; i<keys.size(); i++) {
			String key = (String) keys.elementAt(i);
			Integer int_obj = (Integer) hmap.get(key);
			int count = Integer.valueOf(int_obj);
			total = total + count;
			w.add(key + "|" + count);
		}
		w.add("Total|" + total);
		return w;
	}

    public static boolean checkIfFileExists(String filename) {
		String currentDir = System.getProperty("user.dir");
		File f = new File(currentDir + "\\" + filename);
		if(f.exists() && !f.isDirectory()) {
			return true;
		} else {
			return false;
		}
	}

    public Vector generateBranchSizeTableData() {
		return generateBranchSizeTableData(false);
	}

    public Vector generateBranchSizeTableData(boolean savedata) {
		Vector v = new Vector();
		if (checkIfFileExists(PARENT_CHILD_FILE)) {
			v = Utils.readFile(PARENT_CHILD_FILE);
			savedata = false;
		} else {
			v = scanner.extractHierarchicalRelationships();//getHierarchicalRelationships(named_graph);
		}
		if (savedata) {
		    Utils.saveToFile(PARENT_CHILD_FILE, v);
		}
		hh = new HierarchyHelper(v, 1);
		return generate_branch_size_table_data();
	}

    public Vector convert(Vector v) {
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String src_code = (String) u.elementAt(0);
			String role_code = (String) u.elementAt(1);
			String target_code = (String) u.elementAt(2);

			String role_label = (String) roleCode2NameHashMap.get(role_code);
			String src_label = hh.getLabel(src_code);
			String target_label = hh.getLabel(target_code);
			w.add(src_label + "|" + src_code + "|" + role_label + "|" + role_code + "|" + target_label + "|" + target_code);
		}
		return w;
	}

    public Vector generate_branch_size_table_data() {
		Vector roots = hh.getRoots();
		Vector w = new Vector();
		for (int i=0; i<roots.size(); i++) {
		    String root = (String) roots.elementAt(i);
		    String label = hh.getLabel(root);
		    w.add(label + " (" + root + ")");
		}
		Vector v0 = new Vector();
		w = new SortUtils().quickSort(w);
		StringUtils.dumpVector("roots", w);
        int total = 0; // nodes
        int total2 = 0; // concepts excl retired
        int total3 = 0;
        int total4 = 0;
        Vector active_retired_concepts = new Vector();
        HashSet active_retired_concepts_hset = new HashSet();
		for (int i=0; i<roots.size(); i++) {
			String root = (String) roots.elementAt(i);
			String root_label = hh.getLabel(root);
			String label = hh.getLabel(root);
			//System.out.println("Processing " + label + " (" + root + ")");
			int count = hh.get_transitive_closure(root); //number of nodes
			Vector v3 = hh.get_transitive_closure_v3(root);
			HashSet hset = new HashSet();
			int retired = 0;
			for (int j=0; j<v3.size(); j++) {
				String t = (String) v3.elementAt(j);
				if (is_retired(t)) {
					if (root.compareTo("C28428") != 0) {
						String s = hh.getLabel(t);
						String str = root + "|" + root_label + "|" + t + "|" + s;
						if (!active_retired_concepts_hset.contains(str)) {
							active_retired_concepts.add(str);
							active_retired_concepts_hset.add(str);
						}
					}
					retired++;
				}
				hset.add(t);
			}
			int count2 = hset.size() - retired;
			int count3 = retired;
			int count4 = hset.size();

			total2 = total2 + count2;
			total3 = total3 + count3;
			total4 = total4 + count4;

			total = total + count;
			v0.add(hh.getLabel(root) + " (" + root + ")|" + count + "|" + count2 + "|" + count3 + "|" + count4);
		}
		v0 = new SortUtils().quickSort(v0);
		v0.add("Total|" + total + "|" + total2 + "|" + total3 + "|" + total4);
		active_retired_concepts = new SortUtils().quickSort(active_retired_concepts);
		Utils.dumpVector("active_retired_concepts.txt", active_retired_concepts);
		return v0;
	}

	public Vector getValueSets(boolean publishedOnly) {
		Vector v = new Vector();
		Iterator it = valuesetCode2NameMap.keySet().iterator();
		while (it.hasNext()) {
			String code = (String) it.next();
			if (publishedOnly) {
				if (publishedValueSets.contains(code)) {
					v.add((String) valuesetCode2NameMap.get(code) + "|" + code);
				}
			} else {
				v.add((String) valuesetCode2NameMap.get(code) + "|" + code);
			}
		}
		return v;
	}

	public Vector getConceptsInSubset(String code, boolean codeOnly) {
		HashMap hmap = (HashMap) propertyMap.get("A8");
		Vector w = (Vector) hmap.get(code);
		if (codeOnly) return w;
		Vector v = new Vector();
		for (int i=0; i<w.size(); i++) {
			String s = (String) w.elementAt(i);
			v.add((String) code2LabelMap.get(s) + "|" + s);
		}
		return new SortUtils().quickSort(v);
	}

    public Vector generateValueSetTableData() {
		Vector ret_vec = new Vector();
		boolean publishedOnly = false;
		Vector v = getValueSets(publishedOnly);
		int total = 0;
		for (int i=0 ; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String label = (String) u.elementAt(0);
			String code = (String) u.elementAt(1);
			if (!is_retired(code)) {
				String contributing_source = (String) valueset2ContributingSourceMap.get(code);
				Vector w = getConceptsInSubset(code, true);
				int count = 0;
				if (w != null) {
					count = w.size();
				}
				total = total + count;
				String key = label + " (" + code + ")";
				ret_vec.add(key + "|" + contributing_source + "|" + count);
			}
		}
		ret_vec = new SortUtils().quickSort(ret_vec);
		ret_vec.add("Total||" + total);
        return ret_vec;
	}

    public Vector generateSubBranchData(Vector roots) {
		Vector v0 = new Vector();
		for (int i=0; i<roots.size(); i++) {
		    String root = (String) roots.elementAt(i);
		    String label = hh.getLabel(root);
			int count = hh.get_transitive_closure(root); //number of nodes
			Vector v3 = hh.get_transitive_closure_v3(root);
			HashSet hset = new HashSet();
			int retired = 0;
			for (int j=0; j<v3.size(); j++) {
				String t = (String) v3.elementAt(j);
				if (is_retired(t)) {
					retired++;
				}
                hset.add(t);
			}
			int count2 = hset.size() - retired;
			int count3 = retired;
			int count4 = hset.size();
			v0.add(hh.getLabel(root) + " (" + root + ")|" + count + "|" + count2 + "|" + count3 + "|" + count4);
		}
		v0 = new SortUtils().quickSort(v0);
		return v0;
	}

    public static Vector createValueSetTableData(HashMap valuesetCountHashMap, HashMap hmap) {
        Vector w0 = new Vector();
        Vector w = new Vector();
        Vector keys = new Vector();
        Iterator it = hmap.keySet().iterator();
        while (it.hasNext()) {
			String t = (String) it.next();
			keys.add(t);
		}
		int total = 0;
		int sub_total = 0;
		keys = new SortUtils().quickSort(keys);
		for (int i=0; i<keys.size(); i++) {
			String key = (String) keys.elementAt(i);
			w = new Vector();
			Vector values = (Vector) hmap.get(key);
			values = new SortUtils().quickSort(values);
			sub_total = 0;
			for (int k=0; k<values.size(); k++) {
				String value = (String) values.elementAt(k);
				Integer int_obj = (Integer) valuesetCountHashMap.get(value);
				int count = Integer.valueOf(int_obj);
				sub_total = sub_total + count;
				int k1 = k+1;
				total = total + count;
				w.add(key + "|" + value + "|" + count);
			}
			w = new SortUtils().quickSort(w);
			w.add("Subtotal (" + key + ")|" + "" + "|" + sub_total);
			w0.addAll(w);
		}
		w0.add("Total||" + total);
		return w0;
    }

    public static Vector createValueSetSourceTableData(
            HashMap source2ValueSetCountHashMap,
            HashMap source2ConceptCountHashMap,
            HashMap source2UniqueConceptCountHashMap) {
        Vector w = new Vector();
        Vector keys = new Vector();
        Iterator it = source2ValueSetCountHashMap.keySet().iterator();
        while (it.hasNext()) {
			String t = (String) it.next();
			keys.add(t);
		}
		int total_valueset_count = 0;
		int total_concept_count = 0;
		int total_unique_concept_count = 0;

		HashMap countMap = new HashMap();
		keys = new SortUtils().quickSort(keys);
		for (int i=0; i<keys.size(); i++) {
			String key = (String) keys.elementAt(i);
			int valueset_count = ((Integer) source2ValueSetCountHashMap.get(key)).intValue();
			int concept_count = ((Integer) source2ConceptCountHashMap.get(key)).intValue();
			int unique_concept_count = ((Integer) source2UniqueConceptCountHashMap.get(key)).intValue();
            total_valueset_count = total_valueset_count +  valueset_count;
            total_concept_count = total_concept_count +  concept_count;
            total_unique_concept_count = total_unique_concept_count +  unique_concept_count;
			w.add(key + "|" + valueset_count + "|" + concept_count + "|" + unique_concept_count);
		}
		w = new SortUtils().quickSort(w);
		w.add("Total|" + total_valueset_count + "|" + total_concept_count + "|" + total_unique_concept_count);
		return w;
    }

    public HashMap getSource2valueSetCountHashMap(Vector v) {
        HashMap hmap = new HashMap();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String source = (String) u.elementAt(6);
			HashSet hset = new HashSet();
			if (hmap.containsKey(source)) {
				hset = (HashSet) hmap.get(source);
			}
			String code = (String) u.elementAt(4);
			if (!hset.contains(code)) {
				hset.add(code);
			}
			hmap.put(source, hset);
		}
		HashMap source2valueSetCountHashMap = new HashMap();
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String source = (String) it.next();
			HashSet hset = (HashSet) hmap.get(source);
			source2valueSetCountHashMap.put(source, Integer.valueOf(hset.size()));
		}
		hmap.clear();
        return source2valueSetCountHashMap;
	}

    public HashMap getSource2UniqueConceptCountHashMap(Vector v) {
        HashMap hmap = new HashMap();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String source = (String) u.elementAt(6);
			HashSet hset = new HashSet();
			if (hmap.containsKey(source)) {
				hset = (HashSet) hmap.get(source);
			}
			String code = (String) u.elementAt(1);
			if (!hset.contains(code)) {
				hset.add(code);
			}
			hmap.put(source, hset);
		}
		HashMap source2UniqueConceptCountHashMap = new HashMap();
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String source = (String) it.next();
			HashSet hset = (HashSet) hmap.get(source);
			source2UniqueConceptCountHashMap.put(source, Integer.valueOf(hset.size()));
		}
		hmap.clear();
        return source2UniqueConceptCountHashMap;
	}

	public void run_valuse_set() {
		boolean publishedOnly = false;
		Vector v = getValueSetData(publishedOnly);
		int number_of_valueses = 0;
		int number_of_sources = 0;
		HashSet sourceHashSet = new HashSet();
		HashSet valuesets = new HashSet();
		HashMap valuesetCountHashMap = new HashMap();
		HashMap source2valueSetHashMap = new HashMap();
		HashMap source2ConceptCountHashMap = new HashMap();
		HashMap source2ValueSetCountHashMap = getSource2valueSetCountHashMap(v);
		HashMap source2UniqueConceptCountHashMap = getSource2UniqueConceptCountHashMap(v);

		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String source = (String) u.elementAt(6);

			String valueset = (String) u.elementAt(3) + " (" +
			                  (String) u.elementAt(4) + ")";

            String code = (String)u.elementAt(1);
            Integer int_obj = Integer.valueOf(0);
            if (source2ConceptCountHashMap.containsKey(source)) {
				int_obj = (Integer) source2ConceptCountHashMap.get(source);
			}
			int count = Integer.valueOf(int_obj);
			int_obj = Integer.valueOf(count+1);
			source2ConceptCountHashMap.put(source, int_obj);

			if (!valuesets.contains(valueset)) {
				valuesets.add(valueset);
			}

			if (!sourceHashSet.contains(source)) {
				sourceHashSet.add(source);
			}

			Vector w = new Vector();
			if (source2valueSetHashMap.containsKey(source)) {
				w = (Vector) source2valueSetHashMap.get(source);
			}
			if (!w.contains(valueset)) {
				w.add(valueset);
			}
			source2valueSetHashMap.put(source, w);

            int_obj = Integer.valueOf(0);
            if (valuesetCountHashMap.containsKey(valueset)) {
				int_obj = (Integer) valuesetCountHashMap.get(valueset);
			}
			count = Integer.valueOf(int_obj);
			int_obj = Integer.valueOf(count+1);
			valuesetCountHashMap.put(valueset, int_obj);
		}

        Vector v1 = createValueSetTableData(valuesetCountHashMap, source2valueSetHashMap);
	    String tableName = addTableNumber("Concepts In Value Set Grouped by Contributing Source");
	    Vector th_vec = new Vector();

	    th_vec.add("Contributing Source");
	    th_vec.add("Value Set");
	    th_vec.add("Count");

	    addTable(tableName, th_vec, v1);

        Vector v2 = createValueSetSourceTableData(
            source2ValueSetCountHashMap,
            source2ConceptCountHashMap,
            source2UniqueConceptCountHashMap);

	    tableName = addTableNumber("Concepts In Value Set Grouped by Contributing Source Summary");
	    th_vec = new Vector();
	    th_vec.add("Contributing Source");
		th_vec.add("Value Set Count");
		th_vec.add("Concept Count");
        th_vec.add("Unique Concept Count");

	    addTable(tableName, th_vec, v2);
	}

	public String addTableNumber(String tableName) {
		table_number++;
		String str = "Table " + table_number + ". " + tableName;
		System.out.println("Generating " + str);
		return str;
	}

    public void generateTableData() {
		System.out.println("generateTableData...");
		Vector v = new Vector();
		String tableName = null;
		Vector th_vec = null;
		addTitle("NCI Thesaurus Statistics");

		System.out.println("generateBranchSizeTableData ...");
        v = generateBranchSizeTableData();
	    tableName = addTableNumber("Branch Size");
	    th_vec = new Vector();
	    th_vec.add("Root");
	    th_vec.add("Node Count");
	    th_vec.add("Concepts Excl. Retired");
	    th_vec.add("Retired");
	    th_vec.add("Total");
	    addTable(tableName, th_vec, v);

        v = Utils.readFile(RESTRICTION_FILE);
		System.out.println("getRoleTargets... v.size(): " + v.size());
		String firstLine = (String) v.elementAt(0);
		Vector u = StringUtils.parseData(firstLine, '|');
		if (u.size() == 3) {
			v = convert(v);
			Utils.saveToFile(RESTRICTION_FILE, v);
		}

        Vector spec_roots = new Vector();
        spec_roots.add("C2991");
        spec_roots.add("C3262");
        spec_roots.add("C8278");
        spec_roots.add("C4873");
        spec_roots.add("C89328");
        spec_roots.add("C3367");
        spec_roots.add("C16203");
        spec_roots.add("C25218");
        spec_roots.add("C1909");

		spec_roots.add("C62633");
		spec_roots.add("C62634");
		spec_roots.add("C16342");
		spec_roots.add("C192880");
		spec_roots.add("C192883");

        System.out.println("generateSubBranchData ...");
        Vector subbranchdata = generateSubBranchData(spec_roots);
        subbranchdata = new SortUtils().quickSort(subbranchdata);
	    tableName = addTableNumber("Subtrees");
	    th_vec = new Vector();
	    th_vec.add("Root");
	    th_vec.add("Node Count");
	    th_vec.add("Concepts Excl. Retired");
	    th_vec.add("Retired");
	    th_vec.add("Total");
	    addTable(tableName, th_vec, subbranchdata);

        System.out.println("generateValueSetTableData ...");
		Vector ret_vec = generateValueSetTableData();
	    tableName = addTableNumber("Tallies of Concepts_In_Subset Associations");
	    th_vec = new Vector();
	    th_vec.add("Value Set");
	    th_vec.add("Contributing Source");
	    th_vec.add("Count");
	    addTable(tableName, th_vec, ret_vec);

	    run_valuse_set();

        System.out.println("getPropertyValueCounts ...");
	    v = getPropertyValueCounts("Semantic_Type");
	    tableName = addTableNumber("Semantic_Type");
	    th_vec = new Vector();
	    th_vec.add("Semantic Type");
	    th_vec.add("Count");
	    addTable(tableName, th_vec, v);
	    addFooter();

        System.out.println("getPropertyValueCounts (Contributing_Source) ...");
	    v = getPropertyValueCounts("Contributing_Source");
	    tableName = addTableNumber("Contributing_Source");
	    th_vec = new Vector();
	    th_vec.add("Contributing Source");
	    th_vec.add("Count");
	    addTable(tableName, th_vec, v);
	    addFooter();

        System.out.println("getPropertyValueCounts (Concept_Status) ...");
	    v = getPropertyValueCounts("Concept_Status");
	    tableName = addTableNumber("Concept_Status");
	    th_vec = new Vector();
	    th_vec.add("Concept Status");
	    th_vec.add("Count");
	    addTable(tableName, th_vec, v);
	    addFooter();

        System.out.println("getPropertyCounts ...");
        Vector property_knt_vec = getPropertyCountData();
        //Vector v0 = Utils.readFile(AXIOM_FILE);
        //v = getPropertyCounts(v0);
	    tableName = addTableNumber("Properties");
	    th_vec = new Vector();
	    th_vec.add("Property");
	    th_vec.add("Count");
	    addTable(tableName, th_vec, property_knt_vec);
	    addFooter();

        System.out.println("getAssociations ...");
        Vector association_knt_vec = getAssociationCountData();
	    tableName = addTableNumber("Associations");
	    th_vec = new Vector();
	    th_vec.add("Association");
	    th_vec.add("Count");
	    addTable(tableName, th_vec, association_knt_vec);

		v = Utils.readFile(RESTRICTION_FILE);
        System.out.println("getRelationsipCounts ...");

		Vector role_vec = getRelationsipCounts(v);
		tableName = addTableNumber("Roles");
		th_vec = new Vector();
		th_vec.add("Role");
		th_vec.add("Count");
		addTable(tableName, th_vec, role_vec);

	    addFooter();
	    System.out.println("Done.");
	}


    public Vector getRelationsipCounts(Vector asso_vec) {
		HashMap hmap = new HashMap();
		Vector keys = new Vector();
		for (int i=0; i<asso_vec.size(); i++) {
			String line = (String) asso_vec.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String asso_label = (String) u.elementAt(2);
			Integer int_obj = Integer.valueOf(0);
			if (hmap.containsKey(asso_label)) {
				int_obj = (Integer) hmap.get(asso_label);
			}
			int count = Integer.valueOf(int_obj);
			count++;
			hmap.put(asso_label, Integer.valueOf(count));
			if (!keys.contains(asso_label)) {
				keys.add(asso_label);
			}
		}
		keys = new SortUtils().quickSort(keys);
		Vector v = new Vector();
		int total = 0;
		for (int i=0; i<keys.size(); i++) {
			String key = (String) keys.elementAt(i);
			Integer int_obj = (Integer) hmap.get(key);
			int count = Integer.valueOf(int_obj);
			total = total + count;
			v.add(key + "|" + count);
		}
		v.add("Total|" + total);
		return v;
	}

    public int get_branch_size(String code) {
		Vector v = hh.get_transitive_closure_v3(code);
		return v.size();//count;
	}

	public void generate() {
		generateTableData();
		Utils.saveToFile("table_data_" + StringUtils.getToday() + ".txt", table_data);
		String outputfile = new HTMLTable().generate(table_data);
		System.out.println(outputfile + " generated.");
	}

	public Vector getPropertyCountData() {
		Vector w = new Vector();
		int total = 0;
		Iterator it = propertyCode2CountMap.keySet().iterator();
		while (it.hasNext()) {
			String prop_code = (String) it.next();
			if (prop_code.startsWith("P")) {
				Integer knt_obj = (Integer) propertyCode2CountMap.get(prop_code);
				if (knt_obj == null) {
					//System.out.println("WARNING: " + prop_code + " count not found.");
				} else {
					int knt = knt_obj.intValue();
					w.add((String) propertyCode2NameHashMap.get(prop_code) + " (" + prop_code + ")|" + knt);
					total = total + knt;
				}
			}
		}
		Vector w1 = getQualifierCountData();
		w.addAll(w1);
		for (int i=0; i<w1.size(); i++) {
			String line = (String) w1.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String knt_str = (String) u.elementAt(1);
			total = total + Integer.parseInt(knt_str);
		}
		w = new SortUtils().quickSort(w);
		w.add("Total| " + total);
		return w;
	}

	public Vector getAssociationCountData() {
		Vector w = new Vector();
		int total = 0;
		Iterator it = propertyCode2CountMap.keySet().iterator();
		while (it.hasNext()) {
			String prop_code = (String) it.next();
			if (prop_code.startsWith("A")) {
				Integer knt_obj = (Integer) propertyCode2CountMap.get(prop_code);
				if (knt_obj == null) {
					//System.out.println("WARNING: " + prop_code + " count not found.");
				} else {
					int knt = knt_obj.intValue();
					//w.add((String) u.elementAt(0) + " (" + (String) u.elementAt(1) + ")|" + knt);
					w.add((String) propertyCode2NameHashMap.get(prop_code) + " (" + prop_code + ")|" + knt);
					total = total + knt;
				}
			}
		}
		w = new SortUtils().quickSort(w);
		w.add("Total| " + total);
		return w;
	}

	public Vector getQualifierCountData() {
		Vector v = Utils.readFile(AXIOM_FILE);
		Vector w = new Vector();
		HashMap qualifierCountMap = new HashMap();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');

            String prop_code = (String) u.elementAt(2);
			if (INV_ANNOTATED_TARGET_HASHMAP.containsKey(prop_code)) {
				prop_code = (String) INV_ANNOTATED_TARGET_HASHMAP.get(prop_code);
				String qual_code = "null";
				String key = qual_code + "|" + prop_code;
				Integer knt_obj = Integer.valueOf(0);
				if (qualifierCountMap.containsKey(key)) {
					knt_obj = (Integer) qualifierCountMap.get(key);
				}
				int knt = knt_obj.intValue();
				knt_obj = Integer.valueOf(knt+1);
				qualifierCountMap.put(key, knt_obj);
			}
            prop_code = (String) u.elementAt(2);
			for (int j=4; j<u.size(); j++) {
				String s = (String) u.elementAt(j);
				Vector u2 = StringUtils.parseData(s, '$');
				String qual_code = (String) u2.elementAt(0);
				String key = qual_code + "|" + prop_code;
				Integer knt_obj = Integer.valueOf(0);
				if (qualifierCountMap.containsKey(key)) {
					knt_obj = (Integer) qualifierCountMap.get(key);
				}
				int knt = knt_obj.intValue();
				knt_obj = Integer.valueOf(knt+1);
				qualifierCountMap.put(key, knt_obj);
			}
		}
		int count = 0;

        dumpHashMap(qualifierCountMap);

		Iterator it = qualifierCountMap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			Vector u = StringUtils.parseData(key, '|');
			String qual_code = (String) u.elementAt(0);
			String prop_code = (String) u.elementAt(1);
			String prop_name = (String) propertyCode2NameHashMap.get(prop_code);
			if (qual_code.compareTo("null") != 0) {
				String qual_name = (String) propertyCode2NameHashMap.get(qual_code);
				String label1 = qual_name + " (" + qual_code + ")";
				String label2 = prop_name + " (" + prop_code + ")";
				Integer count_obj =  (Integer) qualifierCountMap.get(key);
				count = count_obj.intValue();
				w.add(label1 + " of " + label2 + "|" + count);
			} else {
				//go-term (P388) 	34022
				String label2 = prop_name + " (" + prop_code + ")";
				Integer count_obj =  (Integer) qualifierCountMap.get(key);
				count = count_obj.intValue();
				w.add(label2 + "|" + count);
			}
		}
		w = new SortUtils().quickSort(w);
		Utils.saveToFile("qualifiers.txt", w);
		return w;
	}

    public static void main(String[] args) {
		long ms = System.currentTimeMillis();
		String NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile; //"ThesaurusInferred_forTS.owl";
		EVSStatisticsByOWL evsStatistics = new EVSStatisticsByOWL(NCIT_OWL);
	    evsStatistics.generate();
        System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}
}

