package gov.nih.nci.evs.restapi.util;

import java.io.*;
import java.util.*;

public class NCItMetadataUtils {

    static String SCRUBBED_PROPERTIES_FILE = "scrubbedProperties.txt";
    static String NCIT_OWL = "ThesaurusInferred_forTS.owl";

    public NCItMetadataUtils() {

    }

    public static HashMap generatePropertyHashMap() {
		return generatePropertyHashMap(NCIT_OWL);
	}

    public static HashMap generatePropertyHashMap(String owlfile) {
		File f = new File(owlfile);
		if (!f.exists()) {
			NCItDownload.download();
		}
		HashMap propCode2propLabelMap = new HashMap();
		OWLScanner scanner = new OWLScanner(owlfile);
		Vector supportedProperties = scanner.getSupportedProperties();
        for (int i=0; i<supportedProperties.size(); i++) {
			String line = (String) supportedProperties.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			propCode2propLabelMap.put((String) u.elementAt(1), (String) u.elementAt(0));
		}
        Vector objectProperties = scanner.extractObjectProperties(scanner.get_owl_vec());
        for (int i=0; i<objectProperties.size(); i++) {
			String line = (String) objectProperties.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			propCode2propLabelMap.put((String) u.elementAt(0), (String) u.elementAt(1));
		}
		Iterator it = propCode2propLabelMap.keySet().iterator();
		Vector keys = new Vector();
		while (it.hasNext()) {
			String key = (String) it.next();
			keys.add(key);
		}
		keys = new SortUtils().quickSort(keys);
		Vector w = new Vector();
		for (int i=0; i<keys.size(); i++) {
			String key = (String) keys.elementAt(i);
			w.add(key + "|" + (String) propCode2propLabelMap.get(key));
		}
		int n = owlfile.lastIndexOf(".");
		String propfile = "properties_" + owlfile.substring(0, n) + ".txt";
		Utils.saveToFile(propfile, w);
		return propCode2propLabelMap;
	}

	public static Vector generateScrubbedProperties(String assertedOWL) {
		return generateScrubbedProperties(assertedOWL, null);
	}

	public static Vector generateScrubbedProperties(String assertedOWL, String inferredOWL) {
		if (inferredOWL == null) {
			inferredOWL = NCIT_OWL;
		}
		HashMap oldMap = generatePropertyHashMap(assertedOWL);
		HashMap newMap = generatePropertyHashMap(inferredOWL);
		Vector w = diff(oldMap, newMap);
		w.add("default_on_create_class");
		w.add("default_on_edit_class");
		w.add("restricted_by");
		w.add("P379");
		w.add("P380");
		return new SortUtils().quickSort(w);
	}

	public static Vector diff(HashMap oldMap, HashMap newMap) {
		Vector w = new Vector();
		Iterator it = oldMap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			if (!newMap.containsKey(key)) {
				w.add(key);
			}
		}
		return w;
	}

	public static void main(String[] args) {
		String assertedOWL = args[0];
		String inferredOWL = null;
		if (args.length == 2) {
			inferredOWL = NCIT_OWL;
		}
		Vector w = generateScrubbedProperties(assertedOWL, inferredOWL);
		Utils.dumpVector("test_" + SCRUBBED_PROPERTIES_FILE, w);
	}
}