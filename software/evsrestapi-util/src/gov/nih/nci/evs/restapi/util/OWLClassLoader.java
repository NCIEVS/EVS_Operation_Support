package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;
import java.io.*;
import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class OWLClassLoader {
	private String owlfile = null;
	public Vector classIdVec = null;
	static String TARGET = "<!-- http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#";
	public HashMap classDataHashMap = null;
	public static String CLASSID_FILE = "classIds.txt";
	public Vector owl_vec = null;

	public OWLClassLoader(String owlfile) {
		this.owlfile = owlfile;
		this.owl_vec = Utils.readFile(owlfile);
		initialize();
	}

	public OWLClassLoader(Vector owl_vec) {
		this.owl_vec = owl_vec;
		initialize();
	}

	public void initialize() {
		long ms = System.currentTimeMillis();
		loadOWLClasses();
		//Utils.saveToFile(CLASSID_FILE, classIdVec);
        System.out.println("Total initialization run time (ms): " + (System.currentTimeMillis() - ms));
	}

	public static String extractClassId(String line) {
		line = line.trim();
		int n1 = line.lastIndexOf("#");
		int n2 = line.lastIndexOf(" ");
		String id = line.substring(n1+1, n2);
		return id;
	}

	public void clear() {
		classDataHashMap.clear();
		classIdVec.clear();
	}

/*
	public void loadOWLClasses() {
		classDataHashMap = new HashMap();
		classIdVec = new Vector();
		Vector w = new Vector();
		BufferedReader reader;
		String classId = null;
		try {
			reader = new BufferedReader(new FileReader(owlfile));
			int k=0;
			String line = reader.readLine();
			while (line != null) {
				if (line.indexOf(TARGET) != -1) {
					if (classId != null) {
						classIdVec.add(classId);
						classDataHashMap.put(classId, w);
				    }
				    classId = extractClassId(line);
				    w = new Vector();
				}
				w.add(line);
    			line = reader.readLine();
			}
			if (classId != null && w.size() > 0) {
				classIdVec.add(classId);
				classDataHashMap.put(classId, w);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
*/

	public void loadOWLClasses() {
		classDataHashMap = new HashMap();
		classIdVec = new Vector();
		Vector w = new Vector();
		String classId = null;
		for (int i=0; i<owl_vec.size(); i++) {
			String line = (String) owl_vec.elementAt(i);
			if (line.indexOf(TARGET) != -1) {
				if (classId != null) {
					classIdVec.add(classId);
					classDataHashMap.put(classId, w);
				}
				classId = extractClassId(line);
				w = new Vector();
			}
			w.add(line);
		}
		if (classId != null && w.size() > 0) {
			classIdVec.add(classId);
			classDataHashMap.put(classId, w);
		}
	}

	public void loadOWLClasses2() {
		classDataHashMap = new HashMap();
		classIdVec = new Vector();
		Vector w = new Vector();
		String classId = null;
		for (int i=0; i<owl_vec.size(); i++) {
			String line = (String) owl_vec.elementAt(i);
			//int k = 0;
			if (line.indexOf(TARGET) != -1) {
				if (classId != null) {
					classIdVec.add(classId);
					classDataHashMap.put(classId, w);
				}
				classId = extractClassId(line);
				w = new Vector();
			}
			w.add(line);
			if (classId != null && w.size() > 0) {
				classIdVec.add(classId);
				classDataHashMap.put(classId, w);
			}
		}
	}

	public Vector getClassIdVec() {
		return classIdVec;
	}

	public HashMap getClassDataHashMap() {
		return classDataHashMap;
	}

	public Vector getClassData(String code) {
		return (Vector) classDataHashMap.get(code);
	}

	public static void main(String[] args) {
		String owlfile = args[0];
		OWLClassLoader loader = new OWLClassLoader(owlfile);
		HashMap classDataHashMap = loader.getClassDataHashMap();
		Vector classIdVec = loader.getClassIdVec();
		System.out.println("*****classIdVec.size()********" + classIdVec.size());
		System.out.println(classDataHashMap.keySet().size());

		String code = "C1000";
		Vector w = (Vector) classDataHashMap.get(code);
		Utils.dumpVector(code, w);

		code = "C99999";
		w = (Vector) classDataHashMap.get(code);
		Utils.dumpVector(code, w);
	}

}