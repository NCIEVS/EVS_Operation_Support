package gov.nih.nci.evs.restapi.util;

import java.io.*;
import java.util.*;

public class MSHierarchy {

    public static String run(String root) {
        boolean useNCIPT = false;
        return run(root, useNCIPT);
	}

    public static String run(String root, boolean useNCIPT) {
		Vector w = TreeDrawer.generateHierarchyData(root, useNCIPT);
		Utils.saveToFile(root + ".txt", w);
		try {
			w = new Vector();
			w.add("Parent Label|Parent Code|Child Label|Child Code");
			Vector v = Utils.readFile(root + ".txt");
			w.addAll(v);
			Utils.saveToFile(root + ".txt", w);
			MSTable.run(root + ".txt");
		} catch (Exception ex) {
			ex.printStackTrace();
		}

        String title = root + " Hierarchy";
        HTMLHierarchy.run(w, title, root);
        HierarchyHelper hh = new HierarchyHelper(w);
        hh.printTree(root);
        String outputfile = root + "_tree.txt";
        ASCIITree2Word.run(outputfile);
        return outputfile;
	}

    public static void main(String [] args) {
		String root = args[0];
		boolean useNCIPT = false;
		if (args.length > 1) {
			String useNCIPTStr = args[1];
			if (useNCIPTStr.compareTo("false") == 0) {
				useNCIPT = true;
			}
	    }
	    String outputfile = run(root, useNCIPT);
	    System.out.println(outputfile + " generated.");
	}

}