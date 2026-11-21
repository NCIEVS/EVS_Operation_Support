package gov.nih.nci.evs.restapi.ui;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.config.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.text.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.border.EmptyBorder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.XMLConstants;

import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import java.io.*;
import java.util.*;

public class DomPostProcessor {

	public static String NCIT_NS = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#";

	public static HashMap loadClasses(String filename) { //"dom_class.txt"
		return NCItDomParser.loadClasses(filename);
	}

	public static HashMap generateCode2LabeMap(HashMap map) {
		HashMap code2LabeMap = new HashMap();
		Iterator it = map.keySet().iterator();
		while (it.hasNext()) {
			String code = (String) it.next();
			if (!StringUtils.isNCItCode(code)) {
				System.out.println("WARNING code " + code + " is not an NCIt code.");
			} else {
				if (!map.containsKey(code)) {
					System.out.println("Error -- " + code + " not found.");
				} else {
					HashMap hmap = (HashMap) map.get(code);//"NHC0");
					Vector v = (Vector) hmap.get("rdfs:label");
					if (v == null) {
						System.out.println("Error rdfs:label not found for " + code);
					} else {
						String label = (String) v.elementAt(0);
						code2LabeMap.put(code, label);
					}
				}
			}
		}
		return code2LabeMap;
	}

	public static String decode(String line) {
		line = line.replace("&quot;" ,"\"");
		line = line.replace("&amp;" ,"&");
		return line;
	}

	public static Vector generateA8File(HashMap map) {
        HashMap code2LabeMap = generateCode2LabeMap(map);
		Vector w = new Vector();
		Vector error_vec = new Vector();
		Iterator it = map.keySet().iterator();
		int knt = 0;
		while (it.hasNext()) {
			String key = (String) it.next();
			if (!StringUtils.isNCItCode(key)) {
				knt++;
			} else {
				HashMap hmap = (HashMap) map.get(key);
				Vector codes = (Vector) hmap.get("NHC0");
				String code = (String) codes.elementAt(0);
				Vector a8_vec = (Vector) hmap.get("A8");
				if (a8_vec != null && a8_vec.size() > 0) {
					for (int i=0; i<a8_vec.size(); i++) {
						String targetCode = (String) a8_vec.elementAt(i);
						w.add(code + "|A8|" + targetCode);
					}
				}
			}
		}
		return new SortUtils().quickSort(w);
	}

/*
C114452|rdfs:label|RANBP2 Gene
C114453|NHC0|C114453
*/
	public static boolean isPropertyCode(String s) {
		if (s.compareTo("rdfs:label") == 0 || s.compareTo("NHC0") == 0 || s.compareTo("oboInOwl:hasDbXref") == 0) {
			return true;
		}
		if (!s.startsWith("P")) {
			return false;
		}
		if (s.length() < 2) return false;
		String t = s.substring(1, s.length());
		try {
			int n = Integer.parseInt(t);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	public static boolean isAssociationCode(String s) {
		if (!s.startsWith("A")) {
			return false;
		}
		if (s.length() < 2) return false;
		String t = s.substring(1, s.length());
		try {
			int n = Integer.parseInt(t);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	public static Vector generateAssociationFile(HashMap map) {
        HashMap code2LabeMap = generateCode2LabeMap(map);
		Vector w = new Vector();
		Vector error_vec = new Vector();
		Iterator it = map.keySet().iterator();
		int knt = 0;
		while (it.hasNext()) {
			String key = (String) it.next();
			if (!StringUtils.isNCItCode(key)) {
				knt++;
			} else {
				HashMap hmap = (HashMap) map.get(key);
				Vector codes = (Vector) hmap.get("NHC0");
				String code = (String) codes.elementAt(0);
				Iterator it2 = hmap.keySet().iterator();
				while (it2.hasNext()) {
					String key2 = (String) it2.next();
					if (isAssociationCode(key2)) {
						Vector v = (Vector) hmap.get(key2);
						if (v != null && v.size() > 0) {
							for (int i=0; i<v.size(); i++) {
								String targetCode = (String) v.elementAt(i);
								w.add(code + "|" + key2 + "|" + targetCode);
							}
						}
					}
				}
			}
		}
		return new SortUtils().quickSort(w);
	}

	public static Vector generatePropertyFile(HashMap map) {
        HashMap code2LabeMap = generateCode2LabeMap(map);
		Vector w = new Vector();
		Vector error_vec = new Vector();
		Iterator it = map.keySet().iterator();
		int knt = 0;
		while (it.hasNext()) {
			String key = (String) it.next();
			if (!StringUtils.isNCItCode(key)) {
				knt++;
			} else {
				HashMap hmap = (HashMap) map.get(key);
				Vector codes = (Vector) hmap.get("NHC0");
				String code = (String) codes.elementAt(0);
				Iterator it2 = hmap.keySet().iterator();
				while (it2.hasNext()) {
					String key2 = (String) it2.next();
					if (isPropertyCode(key2)) {
						Vector v = (Vector) hmap.get(key2);
						if (v != null && v.size() > 0) {
							for (int i=0; i<v.size(); i++) {
								String targetCode = (String) v.elementAt(i);
								w.add(code + "|" + key2 + "|" + targetCode);
							}
						}
					}
				}
			}
		}
		return new SortUtils().quickSort(w);
	}


	public static Vector generateHierarchyDataFile(HashMap map) {
        HashMap code2LabeMap = generateCode2LabeMap(map);
		Vector w = new Vector();
		Vector error_vec = new Vector();

		Iterator it = map.keySet().iterator();
		int knt = 0;
		while (it.hasNext()) {
			String key = (String) it.next();
			if (!StringUtils.isNCItCode(key)) {
				knt++;
			} else {
				HashMap hmap = (HashMap) map.get(key);
				Vector codes = (Vector) hmap.get("NHC0");
				String code = (String) codes.elementAt(0);
				if (!StringUtils.isNCItCode(code)) {
					knt++;
				}
			}
		}
 		it = map.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			if (!StringUtils.isNCItCode(key)) {
				//System.out.println("WARNING key " + key + " in map is not an NCIt code.");
			} else {
				String label = (String) code2LabeMap.get(key);
				HashMap hmap = (HashMap) map.get(key);
				Vector v = (Vector) hmap.get("rdfs:subClassOf");
				Vector code_vec = (Vector) hmap.get("NHC0");
				String code = (String) code_vec.elementAt(0);
				if (v != null && v.size() > 0) {
					for (int i=0; i<v.size(); i++) {
						String parentCode = (String) v.elementAt(i);
						String parentLabel = (String) code2LabeMap.get(parentCode);
						if (parentLabel == null) {
							System.out.println(parentCode + " of parentCode " + parentCode + " is null.");
						} else if (parentLabel.indexOf("null") != -1) {
							System.out.println(parentCode + " -- label --> " + parentLabel);
						}
						String line = parentLabel + "|" + parentCode + "|" + label + "|" + code;
						if (line.indexOf("owl:Class") != -1) {
							error_vec.add(line);
							error_vec.add("parentLabel of parentCode " + parentCode + " = " + parentLabel);
						} else {
							w.add(parentLabel + "|" + parentCode + "|" + label + "|" + code);
						}
					}
				}
				v = (Vector) hmap.get("rdf:Description");
				if (v != null && v.size() > 0) {
					for (int i=0; i<v.size(); i++) {
						String parentCode = (String) v.elementAt(i);
						String parentLabel = (String) code2LabeMap.get(parentCode);
						String line = parentLabel + "|" + parentCode + "|" + label + "|" + code;
						if (line.indexOf("owl:Class") != -1) {
							error_vec.add(line);
							error_vec.add("parentLabel of parentCode " + parentCode + " = " + parentLabel);
						} else {
							w.add(parentLabel + "|" + parentCode + "|" + label + "|" + code);
						}
					}
				}
			}
 		}
 		Utils.dumpVector("error", error_vec);
        return new SortUtils().quickSort(w);
	}

	public static void decodeFile(String filename) {
		Vector w = new Vector();
		Vector v = Utils.readFile(filename);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			w.add(decode(line));
		}
		Utils.saveToFile(filename, w);
	}

    public static void main(String[] args) {
		long ms = System.currentTimeMillis();

		String filename = "dom_class.txt";
		//filename = "dom_test.txt";
		System.out.println("loadClasses ...");
		HashMap map = loadClasses(filename);
		System.out.println("Done loadClasses ...");

        // Vector parent_child_vec = generateHierarchyDataFile(map);
        // Utils.saveToFile("parent_child.txt", parent_child_vec);

        // Vector a8_vec = generateA8File(map);
        // Utils.saveToFile("A8.txt", a8_vec);

        Vector prop_vec = generatePropertyFile(map);
        Utils.saveToFile("properties.txt", prop_vec);
        decodeFile("properties0.txt");

        String label = "dom_prop";
        Vector diff_vec = DiffUtils.run(label, "properties0.txt", "properties.txt");
        Utils.saveToFile("diff_properties.txt", diff_vec);

        System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}
}

