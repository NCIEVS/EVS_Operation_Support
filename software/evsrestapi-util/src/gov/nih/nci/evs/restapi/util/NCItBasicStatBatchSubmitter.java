package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.appl.*;
import gov.nih.nci.evs.restapi.ui.*;
import gov.nih.nci.evs.restapi.config.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.text.*;

public class NCItBasicStatBatchSubmitter {
    static HashMap filenameMap = null;
    static {
		filenameMap = new HashMap();
		filenameMap.put("Concept", "labels.txt");
		filenameMap.put("Axiom", "axiom_ThesaurusInferred_forTS.txt");
		filenameMap.put("Hierarchy", "parent_child.txt");
		filenameMap.put("Restriction", "roles.txt");
		filenameMap.put("Association", "associations.txt");
		filenameMap.put("Property", "properties.txt");
	}

	public NCItBasicStatBatchSubmitter() {
		initialize();
	}

	public void initialize() {
		File f = new File("labels.txt");
		if (!f.exists()) {
			NCItBasicStatUI.createLabelFile();
		}
	}

	public static void run(String datafile) {
		File f = new File(datafile);
		if (!f.exists()) {
			System.out.println(datafile + " does not exist.");
			return;
		}
		Vector v = Utils.readFile(datafile);
		Vector w = new Vector();
		String firstLn = (String) v.elementAt(0);
		w.add(firstLn.replace("|", "\t") + "\tCount");
		for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String label = (String) u.elementAt(0);
			String type = (String) u.elementAt(1);
			String pattern = (String) u.elementAt(2);
			String excludeStr = (String) u.elementAt(3);
			boolean excludeRetired = true;
			if (excludeStr.compareTo("true") != 0) {
				excludeRetired = false;
			}
			String filename = (String) filenameMap.get(type);
			Vector data_vec = Utils.readFile((String) filenameMap.get(type));
			String countStr = "";
			try {
				Vector w1 = NCItBasicStatUI.match(data_vec, pattern, excludeRetired);
				countStr = "" + w1.size();
		    } catch (Exception ex) {
				ex.printStackTrace();
			}
			line = line.replace("|", "\t");
			w.add(line + "\t" + countStr);
		}
		int n = datafile.lastIndexOf(".");
		String outputfile = datafile.substring(0, n) + "_results.txt";
		Utils.saveToFile(outputfile, w);
		System.out.println(outputfile + " generated.");
	}

	public static void main(String[] args) {
		String datafile = args[0];
		run(datafile);
	}
}

/*
Sample datafile:
Label|Type|Match Pattern|Exclude Retired?
Concept Count|Concept||true
Hierarchical Relationship Count|Hierarchy||true
DEFINITION Count|Property|P97|true
ALT-DEFINITION Count|Property|P325|true
Restriction Count|Restriction||true
Association Count|Association||true
*/