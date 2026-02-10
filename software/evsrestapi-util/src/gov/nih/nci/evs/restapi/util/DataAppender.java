package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.text.*;
import java.util.*;


public class DataAppender {
    static String NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile;

	public static HashMap createMultiValuedHashMap(Vector v) {
		return Utils.createMultiValuedHashMap(v, '|', 0, 2);
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

    public static void run(String datafile, char delim, int keycol, Vector propCode_vec, String outputfile, String heading) {
		Vector v = Utils.readFile(datafile);
        Vector w0 = new Vector();
        HashSet hset = new HashSet();
        w0.add(heading);
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
			w = new Vector();
			for (int j=0; j<v.size(); j++) {
				String line = (String) v.elementAt(j);
				Vector u2 = StringUtils.parseData(line, delim);
				String code = (String) u2.elementAt(keycol);
				String appended_data = "";
				if (!hset.contains(code)) {
					Vector v1 = (Vector) hmap.get(code);
					String t = "";
					if (v1 != null) {
						t = Utils.vector2DelimitedString(v1);
						appended_data = appended_data + "\t" + t;
					}
					line = line.replace("|", "\t");
					w.add(line + "\t" + appended_data);
					hset.add(code);
				}
			}
		}
		w = new SortUtils().quickSort(w);
		w0.addAll(w);
		Utils.saveToFile(outputfile, w0);
	}

	public static void main(String[] args) {
		String datafile = args[0];
		Vector w = extractColumnData(datafile, "0|1", '|');
		String modified_datafile = "modified_" + datafile;
		Utils.saveToFile(modified_datafile, w);

		char delim = '|';
		int keycol = 1;
		String propCodes = args[1];
		Vector w1 = StringUtils.parseData(propCodes, '|');
		Vector propCode_vec = new Vector();
		for (int i=0; i<w1.size(); i++) {
			String s = (String) w1.elementAt(i);
			propCode_vec.add(s);
		}
		String outputfile = "rsults_" + datafile;
		String heading = "Label\tCode\tSemantic Type(s)";
		run(modified_datafile, delim, keycol, propCode_vec, outputfile, heading);

	}

}