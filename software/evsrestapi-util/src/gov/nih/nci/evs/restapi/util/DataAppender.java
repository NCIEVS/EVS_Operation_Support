package gov.nih.nci.evs.restapi.util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.text.*;
import java.util.*;

import gov.nih.nci.evs.restapi.config.*;

public class DataAppender {
    static String NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile;

	public static HashMap createMultiValuedHashMap(Vector v) {
		return Utils.createMultiValuedHashMap(v, '\t', 0, 2);
	}

	public static Vector extractColumnData(String filename, String cols, char delim) {
		Vector<String> col_vec = StringUtils.parseData(cols, '|');
		Vector<Integer> propCode_vec = new Vector();
		for (int i=0; i<col_vec.size(); i++) {
			String s = (String) col_vec.elementAt(i);
			int k = Integer.parseInt(s);
			System.out.println(k);
			propCode_vec.add(Integer.valueOf(k));
		}
		return DataExtractor.extractColumnData(filename, propCode_vec, delim);
	}

	public static HashMap createCode2LabelMap(String parent_child_file) {
		Vector v = Utils.readFile(parent_child_file);
		HashMap hmap = new HashMap();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String s0 = (String) u.elementAt(0);
			String s1 = (String) u.elementAt(1);
			hmap.put(s1, s0);
			if (u.size() > 2) {
				String s2 = (String) u.elementAt(2);
				String s3 = (String) u.elementAt(3);
				hmap.put(s3, s2);
			}
		}
		return hmap;
	}

	public static Vector sortBy(Vector v, char delim, int col, boolean skipHeading) {
		Vector w = new Vector();
		int istart = 0;
		if (skipHeading) {
			istart = 1;
			w.add((String) v.elementAt(0));
		}
		HashMap hmap = new HashMap();
		Vector keys = new Vector();
		for (int i=istart; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, delim);
			String key = (String) u.elementAt(col);
			if (!keys.contains(key)) {
				keys.add(key);
			}
			Vector lines = new Vector();
			if (hmap.containsKey(key)) {
				lines = (Vector) hmap.get(key);
			}
			lines.add(line);
			hmap.put(key, lines);
		}
		keys = new SortUtils().quickSort(keys);
		for (int i=0; i<keys.size(); i++) {
			String key = (String) keys.elementAt(i);
			Vector lines = (Vector) hmap.get(key);
			for (int j=0; j<lines.size(); j++) {
				String line = (String) lines.elementAt(j);
				w.add(line);
			}
		}
		return w;
	}

    public static void append(HashMap code2LabelMap, Vector propCode_vec, String outputfile, String heading) {
        Vector w0 = new Vector();
        w0.add(heading);
        HashMap propCode2DataMap = new HashMap();
        Vector w = new Vector();
		for (int i=0; i<propCode_vec.size(); i++) {
			String propCode = (String) propCode_vec.elementAt(i);
			Vector dataVec = new Vector();
			dataVec.add(propCode);
			DataRetrieval test = new DataRetrieval(NCIT_OWL, dataVec);
			Vector v0 = new Vector();
			for (int j=0; j<dataVec.size(); j++) {
				String data_req = (String) dataVec.elementAt(j);
				v0.addAll(test.getData(data_req));
			}
			String propfile = propCode + ".txt";
			Utils.saveToFile(propfile, v0);
			HashMap hmap = createMultiValuedHashMap(v0);
			propCode2DataMap.put(propCode, hmap);
		}

		w = new Vector();
		Iterator it = code2LabelMap.keySet().iterator();
		while (it.hasNext()) {
			String code = (String) it.next();
			String label = (String) code2LabelMap.get(code);
			String line = code + "\t" + label;
			String appended_data = "";
			Iterator it2 = propCode2DataMap.keySet().iterator();
			while (it2.hasNext()) {
				String propCode = (String) it2.next();
				HashMap hmap = (HashMap) propCode2DataMap.get(propCode);
				Vector v1 = (Vector) hmap.get(code);
				String t = "";
				if (v1 != null) {
					t = Utils.vector2DelimitedString(v1);
					appended_data = appended_data + "\t" + t;
				} else {
					appended_data = appended_data + "\t";
				}
			}
			w.add(line + "\t" + appended_data);
		}
		w0.addAll(w);
		Utils.saveToFile(outputfile, w0);
	}

	public static void main(String[] args) {
		String datafile = args[0];
		HashMap code2LabelMap = createCode2LabelMap(datafile);
		char delim = '|';
		String propCodes = args[1];
		Vector w1 = StringUtils.parseData(propCodes, '|');
		Vector propCode_vec = new Vector();
		for (int i=0; i<w1.size(); i++) {
			String s = (String) w1.elementAt(i);
			propCode_vec.add(s);
		}

		String outputfile = "prop_values_" + datafile;
		System.out.println("outputfile: " + outputfile);
		String heading = "Code\tLabel\tSemantic Type(s)";
		append(code2LabelMap, propCode_vec, outputfile, heading);
        Vector w = Utils.readFile(outputfile);
        boolean skipHeading = true;
        w = sortBy(w, '\t', 1, skipHeading);
        Utils.saveToFile(outputfile, w);
	}
}