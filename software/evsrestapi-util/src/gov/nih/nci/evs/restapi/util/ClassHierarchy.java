package gov.nih.nci.evs.restapi.util;

import java.io.*;
import java.net.URI;
import java.text.*;
import java.util.*;


public class ClassHierarchy {

    public static Vector generateHierarchyData(String foldername) {
		if (foldername == null) {
			Vector srcfiles = new Vector();
			String currentDir = System.getProperty("user.dir");
			foldername = currentDir + File.separator + "src";
		}
		Vector w = new Vector();
		List list = FileUtils.listFilesInDirectory(foldername);
        int classknt = 0;
        HashMap label2IdMap = new HashMap();

		for (int i=0; i<list.size(); i++) {
			String filepath = (String) list.get(i);
			if (filepath.endsWith("java")) {
				String parent = ClassNameFinder.getJavaClassName(filepath);
				String parentId = null;
				if (label2IdMap.containsKey(parent)) {
					parentId = (String) label2IdMap.get(parent);
				} else {
					classknt++;
					parentId = "c" + classknt;
					label2IdMap.put(parent, parentId);
     			}
				Vector v = ClassNameFinder.searchClassNames(filepath);
				v.removeElement(parent);
				if (v.size() > 0) {
					for (int j=0; j<v.size(); j++) {
						String child = (String) v.get(j);
						String childId = null;
						if (label2IdMap.containsKey(child)) {
							childId = (String) label2IdMap.get(child);
						} else {
							classknt++;
							childId = "c" + classknt;
							label2IdMap.put(child, childId);
						}
						w.add(parent + "|" + parentId + "|" + child + "|" + childId);
					}
				}
			}
		}

        return w;
	}

	public static void run(String foldername) {
		Vector w = generateHierarchyData(foldername);
		Utils.saveToFile("Class_Hierarhy.txt", w);

		HierarchyHelper hh = new HierarchyHelper(w);
		hh.printTree();
	}

	public static void main(String args[]) {
		String foldername = null;
		Vector w = new Vector();
		if (args.length == 0) {
			foldername = System.getProperty("user.dir");
		} else {
			foldername = args[0];
		}
		run(foldername);
	}
}

