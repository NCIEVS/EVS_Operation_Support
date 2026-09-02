package gov.nih.nci.evs.restapi.util;

import java.io.*;
import java.net.URI;
import java.text.*;
import java.util.*;


public class ClassHierarchy {
	public static String CLASS_HIERARCHY_FILE = "Class_Hierarhy.txt";
    public static String CLASS_CODE_PREFIX = "c_";

    public static Vector generateHierarchyData(String foldername) {
		if (foldername == null) {
			Vector srcfiles = new Vector();
			String currentDir = System.getProperty("user.dir");
			foldername = currentDir + File.separator + "src";
		}
		System.out.println(foldername);

		Vector w = new Vector();
		List list = FileUtils.listFilesInDirectory(foldername);
		Utils.dumpList(foldername, list);

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
					parentId = CLASS_CODE_PREFIX + classknt;
					label2IdMap.put(parent, parentId);
     			}

				Vector v = ClassNameFinder.searchClassNames(filepath);
				v.removeElement(parent);
                v.removeElement("");
				if (v.size() > 0) {
					for (int j=0; j<v.size(); j++) {
						String child = (String) v.get(j);
						if (child.length() > 0) {
							String childId = null;
							if (label2IdMap.containsKey(child)) {
								childId = (String) label2IdMap.get(child);
							} else {
								classknt++;
								childId = CLASS_CODE_PREFIX + classknt;
								label2IdMap.put(child, childId);
							}
							w.add(parent + "|" + parentId + "|" + child + "|" + childId);
						}
					}
				}
			}
		}
        return w;
	}

	public static void run(String foldername) {
		System.out.println("ClassHierarchy generateHierarchyData ... ");
		Vector w = generateHierarchyData(foldername);
		Utils.saveToFile("Class_Hierarhy.txt", w);
		HierarchyHelper hh = new HierarchyHelper(w);
		hh.printPath();
	}

	public static Vector getExternalClasses(String dir) {
		String path = FileUtils.getCurrentWorkingDirectory() + File.separator + dir;
		Vector classNames = ClassNameFinder.getClassNames(path);
		Utils.dumpVector("classNames", classNames);
		HashSet hset = Utils.vector2HashSet(classNames);
		Vector v = Utils.readFile(CLASS_HIERARCHY_FILE);
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String s0 = (String) u.elementAt(0);
			String s2 = (String) u.elementAt(2);
			if (!hset.contains(s0)) {
				if (!w.contains(s0)) w.add(s0);
			}
			if (!hset.contains(s2)) {
				if (!w.contains(s2)) w.add(s2);
			}
		}
		return new SortUtils().quickSort(w);
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
		w = getExternalClasses(foldername);
		Utils.dumpVector("External classes", w);
	}
}

