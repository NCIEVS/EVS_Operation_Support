package gov.nih.nci.evs.restapi.util;
import java.io.*;
import java.util.*;


public class InheritanceUtils {

	public static void dumpClassData(PrintWriter pw, Vector classData) {
		for (int k=0; k<classData.size(); k++) {
			String t = (String) classData.elementAt(k);
			pw.println(t);
		}
	}

	public static void dumpVector(PrintWriter pw, String label, Vector v) {
		pw.println(label);
		for (int k=0; k<v.size(); k++) {
			String t = (String) v.elementAt(k);
			pw.println("\t" + t);
		}
	}

	public static void run(String assertedOWL) {
		InheritanceAnalyzer analyzer = new InheritanceAnalyzer(assertedOWL);
		Vector conceptsWithInheritedAnonymousSuperClasses = analyzer.matchAncestorRelationships();
		Utils.dumpVector("conceptsWithInheritedAnonymousSuperClasses", conceptsWithInheritedAnonymousSuperClasses);

		OWLClassLoader loader = null;
		HashMap classDataHashMap = null;//loader.getClassDataHashMap();
		Vector classIdVec = null;//loader.getClassIdVec();
		loader = new OWLClassLoader(assertedOWL);

		Vector parent_child_vec = analyzer.get_parent_child_vec();
		parent_child_vec = HTMLDecoder.run(parent_child_vec);
		HierarchyHelper hh = new HierarchyHelper(parent_child_vec);
        System.out.println("parent-child (distance-1 hierarchical) relationships generated.");

		String outputfile = "inheritance_anaylsis.txt";
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(outputfile, "UTF-8");
			for (int i=0; i<conceptsWithInheritedAnonymousSuperClasses.size(); i++) {
				String line = (String) conceptsWithInheritedAnonymousSuperClasses.elementAt(i);
				int j = i+1;
				pw.println("********************************");
				pw.println("**" + line);
				pw.println("********************************");
				Vector u = StringUtils.parseData(line, '|');
				for (int m=0; m<u.size(); m++) {
					String id = (String) u.elementAt(m);
					Vector classData = loader.getClassData(id);
					dumpClassData(pw, classData);
				}
				String code = (String) u.elementAt(0);
				hh.path2Roots(pw, code);
                boolean traverseDown = false;
				Vector ancestors = hh.getTransitiveClosure(code, traverseDown);
				ancestors.remove(code);
				dumpVector(pw, "ancestors", ancestors);
				for (int m=0; m<ancestors.size(); m++) {
					int m1 = m+1;
					String ancestor = (String) ancestors.elementAt(m);
					pw.println("\n(" + m1 + ") relationship " + ancestor);
					HashMap relMap = analyzer.createRelationshipHashMap(ancestor);
					Iterator it = relMap.keySet().iterator();
					while (it.hasNext()) {
						String key = (String) it.next();
						Vector values = (Vector) relMap.get(key);
						dumpVector(pw, key, values);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				pw.close();
				System.out.println("Output file " + outputfile + " generated.");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		String assertedOWL = args[0];
		run(assertedOWL);
	}
}



