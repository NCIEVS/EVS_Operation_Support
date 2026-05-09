package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;
import gov.nih.nci.evs.restapi.bean.*;
import java.io.*;
import java.util.*;
import java.net.*;

import java.util.Arrays;

public class TreeDrawer {
	static String PARENT_CHILD_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile;
	static String AXIOM_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.axiomfile;

	static HierarchyHelper hh = null;
	static {
		Vector parent_child_vec = Utils.readFile(PARENT_CHILD_FILE);
		hh = new HierarchyHelper(parent_child_vec);
	}

    public static HashMap createSourcePTMap(String source) {
		HashMap hmap = new HashMap();
		Vector v = Utils.readFile(AXIOM_FILE);
		int lcv = 0;
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			if (u.contains("P383$PT") && u.contains("P384$" + source) ) {
				lcv++;
				hmap.put((String) u.elementAt(1), (String) u.elementAt(3));
			}
		}
		return hmap;
	}

    public static int findLevel(String line) {
		int level = 0;
		for (int i=0; i<line.length(); i++) {
			char c = line.charAt(i);
			if (c != '\t') {
				level = i;
				break;
			}
		}
		return level;
	}

	public static Vector generateHierarchyData(String root, boolean useNCIPT) {
		Vector lines = hh.get_transitive_closure_v4(root);
		Utils.saveToFile(root + "_codes.txt", lines);
		Vector w = new Vector();
		if (useNCIPT) {
			HashMap NCIPTMap = createSourcePTMap("NCI");
			for (int i=0; i<lines.size(); i++) {
				String line = (String) lines.elementAt(i);
				Vector u = StringUtils.parseData(line, '|');
				String s0 = (String) u.elementAt(0);
				String s1 = (String) u.elementAt(1);
				String s2 = (String) u.elementAt(2);
				String s3 = (String) u.elementAt(3);
				String t0 = (String) NCIPTMap.get(s1);
				String t2 = (String) NCIPTMap.get(s3);
				w.add(t0 + "|" + s1 + "|" + t2 + "|" + s3);
			}
		} else {
			w = (Vector) lines.clone();
		}
		w = new SortUtils().quickSort(w);
        Utils.saveToFile(root + ".txt", w);
        return w;
	}

	public static void run(String root, boolean useNCIPT) {
		Vector w = generateHierarchyData(root, useNCIPT);
        String title = root + " Hierarchy";
        HTMLHierarchy.run(w, title, root);
        HierarchyHelper hh = new HierarchyHelper(w);
        hh.printTree(root);

        Vector v = Utils.readFile(root + "_tree.txt");
        int maxLevel = 0;
        for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			int level = findLevel(line);
			if (level > maxLevel) {
				maxLevel = level;
			}
		}

        String htmltreefile = DynamicTreePlus.generateDynamicHTMLTree(root, maxLevel);
        System.out.println(htmltreefile + " generated.");

        title = "hierarchy" + "_" + root;
        Utils.saveToFile(title + ".txt", w);
        PolyHierarchy.generateDynamicHTMLTree(title + ".txt");

	}

}
