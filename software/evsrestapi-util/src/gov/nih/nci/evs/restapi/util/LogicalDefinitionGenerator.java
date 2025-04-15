package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;
import gov.nih.nci.evs.restapi.bean.*;

import java.io.*;
import java.text.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;
import java.nio.charset.Charset;


public class LogicalDefinitionGenerator {
    static String NAMESPACE = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#";
    static String NAMESPACE_TARGET = "<!-- " + NAMESPACE;
    static String NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile; //"ThesaurusInferred_forTS.owl";
	static String PARENT_CHILD_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile; // "parent_child.txt";
	static String RESTRICTION_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.rolefile; //"roles.txt";
	static String AXIOM_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.axiomfile;

    static HashSet retiredConcepts = new HashSet();
    static OWLScanner scanner = null;
    static HashMap propertyMap = null;

    static HashMap role_hashmap = null;

    static OWLSPARQLUtils owlSPARQLUtils = null;

    public static String CLASS_IDS = "ClassIDs";

	public static HashMap RANGE_HASHMAP = null;
	public static HashMap code2LabelMap = null;
	public static HashMap roleCode2LabelMap = null;

	static {
        RANGE_HASHMAP = getRangeHashMap();
        //role_hashmap = createRoleMap();
        //Utils.dumpHashMap("RANGE_HASHMAP", RANGE_HASHMAP);

        code2LabelMap = createCode2LabelMap();
        roleCode2LabelMap = createRoleCode2LabelMap();
	}

	public static HashMap createCode2LabelMap() {
		HashMap code2LabelMap = new HashMap();
		Vector v = Utils.readFile(PARENT_CHILD_FILE);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			code2LabelMap.put((String) u.elementAt(1), (String) u.elementAt(0));
			code2LabelMap.put((String) u.elementAt(3), (String) u.elementAt(2));
		}
		return code2LabelMap;
	}

	public static HashMap createRoleCode2LabelMap() {
		HashMap map = new HashMap();
		OWLScanner scanner = new OWLScanner(NCIT_OWL);
	    Vector v = scanner.extractObjectProperties(scanner.get_owl_vec());
	    for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			map.put((String) u.elementAt(0), (String) u.elementAt(1));
		}
		return map;
	}


    public LogicalDefinitionGenerator() {

	}

	public static HashMap createRoleMap() {
		Vector v = extractOWLRestrictions();
		HashMap hmap = new HashMap();
        for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String src_code = (String) u.elementAt(0);
			String role_code = (String) u.elementAt(1);
			String target_code = (String) u.elementAt(2);
			HashMap map = new HashMap();
			if (hmap.containsKey(src_code)) {
				map = (HashMap) hmap.get(src_code);
			}
			Vector w = new Vector();
			if (map.containsKey(role_code)) {
				w = (Vector) map.get(role_code);
			}
			w.add(target_code);
			map.put(role_code, w);
			hmap.put(src_code, map);
		}
		return hmap;
	}


	public static boolean is_retired(String code) {
		return retiredConcepts.contains(code);
	}


	public String construct_get_role_group(String named_graph, String code) {
        String prefixes = owlSPARQLUtils.getPrefixes();
        StringBuffer buf = new StringBuffer();
        buf.append(prefixes);
        buf.append("select distinct ?x_code ?x_label ?p_code ?p_label ?y_code ?y_label").append("\n");
        buf.append("{").append("\n");
        buf.append("graph <" + named_graph + ">").append("\n");
        buf.append("{").append("\n");
        buf.append("?x a owl:Class .").append("\n");
        buf.append("?x :NHC0 ?x_code .").append("\n");
        buf.append("?x :NHC0 \"" + code + "\"^^xsd:string .").append("\n");
        buf.append("?x rdfs:label ?x_label .").append("\n");
        buf.append("#           ?x (owl:equivalentClass|owl:unionOf/owl:intersectionOf/rdf:rest*/rdf:first)* ?rs .").append("\n");
        buf.append("#?x (owl:equivalentClass|owl:unionOf/rdf:rest*/rdf:first|owl:intersectionOf/rdf:rest*/rdf:first)* ?rs .").append("\n");
        buf.append("?x (owl:equivalentClass|owl:intersectionOf/rdf:rest*/rdf:first|owl:unionOf/rdf:rest*/rdf:first|owl:intersectionOf/rdf:rest*/rdf:first)* ?rs .").append("\n");
        buf.append("?rs a owl:Restriction .").append("\n");
        buf.append("?rs owl:onProperty ?p .").append("\n");
        buf.append("?p :NHC0 ?p_code .").append("\n");
        buf.append("?p rdfs:label ?p_label .").append("\n");
        buf.append("?rs owl:someValuesFrom ?y .").append("\n");
        buf.append("?y :NHC0 ?y_code .").append("\n");
        buf.append("?y rdfs:label ?y_label .").append("\n");
        buf.append("}").append("\n");
        buf.append("}").append("\n");
        return buf.toString();
	}


	public Vector getRoleGroup(String named_graph, String code) {
        String query = construct_get_role_group(named_graph, code);
        Vector v = owlSPARQLUtils.executeQuery(query);
        if (v == null) return null;
        if (v.size() == 0) return v;
        return new SortUtils().quickSort(v);
	}

	public HashMap getRoleMap(String src_code) {
		if (!role_hashmap.containsKey(src_code)) return null;
		return (HashMap) role_hashmap.get(src_code);
	}

	public int getRoleTargetCount(String src_code) {
		HashMap map = getRoleMap(src_code);
		if (map == null) return 0;
		Iterator it = map.keySet().iterator();
		int knt = 0;
		while (it.hasNext()) {
			String key = (String) it.next();
			Vector w = (Vector) map.get(key);
			knt = knt + w.size();
		}
		return knt;
	}

	public Vector getOutboundRoles(String src_code) {
		String filename = src_code + ".owl";
		Vector v = Utils.readFile(filename);
		Vector w = new Vector();
		boolean restriction_start = false;
        OWLRestriction r = null;
        String onProperty = null;
        String someValueFrom = null;
        String classId = src_code;
        HashSet hset = new HashSet();

		int knt = 0;
        for (int i=0; i<v.size(); i++) {
			String t = (String) v.elementAt(i);

			if (t.indexOf("<owl:Restriction>") != -1) {
				restriction_start = true;
				r = new OWLRestriction();
				r.setClassId(classId);
			} else if (r != null) {
				t = t.trim();
				if (t.startsWith("<owl:onProperty")) {
					//<owl:onProperty rdf:resource="http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#R101"/>
					int n = t.lastIndexOf("#");
					t = t.substring(n, t.length());
					n = t.lastIndexOf("\"");
					onProperty = t.substring(1, n);
					r.setOnProperty(onProperty);
				}
				if (t.startsWith("<owl:someValuesFrom")) {
					// <owl:someValuesFrom rdf:resource="http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C12382"/>
					int n = t.lastIndexOf("#");
					t = t.substring(n, t.length());
					n = t.lastIndexOf("\"");
					someValueFrom = t.substring(1, n);
					r.setSomeValuesFrom(someValueFrom);
					if (!hset.contains(r.toString())) {
						hset.add(r.toString());
						w.add(r.toString());
					}
					r = null;
				}
			}
		}
		return w;
	}




	public HashMap getOutboundRolesV2(String classId, Vector v) {
		HashMap roleMap = new HashMap();
	    HashMap tagMap = createTagMap(extractTags(v));
		Vector w = new Vector();
		boolean restriction_start = false;
        OWLRestriction r = null;
        String onProperty = null;
        String someValueFrom = null;
        HashSet hset = new HashSet();

        String path = "";
/*
tag map:
owl:Axiom --> A
owl:Class --> C
owl:equivalentClass --> E
owl:intersectionOf --> I
owl:onProperty --> O
owl:Restriction --> R
owl:someValuesFrom --> S
owl:unionOf --> U
*/


		int knt = 0;
        for (int i=0; i<v.size(); i++) {
			String t = (String) v.elementAt(i);
			String s = t.trim();

			String p = null;
			boolean openTag = true;
			Iterator it = tagMap.keySet().iterator();
			while (it.hasNext()) {
				String tag = (String) it.next();
				if (s.startsWith("<" + tag)) {
					p = (String) tagMap.get(tag);
					openTag = true;
					break;
				} else if (s.startsWith("</" + tag)) {
					p = (String) tagMap.get(tag);
					openTag = false;
					break;
				}
			}

            if (p != null) {
				p = p.trim();

			    if (p.compareTo("E") == 0 || p.compareTo("I") == 0 || p.compareTo("U") == 0) {
					if (p != null && openTag) {
						path = path + "|" + p;
					} else if (p != null && !openTag) {
						path = path.substring(0, path.length()-2);
					}
				}
			}

			if (t.indexOf("<owl:Restriction>") != -1) {
				restriction_start = true;
				r = new OWLRestriction();
				r.setClassId(classId);
			} else if (r != null) {
				t = t.trim();
				if (t.startsWith("<owl:onProperty")) {
					//<owl:onProperty rdf:resource="http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#R101"/>
					int n = t.lastIndexOf("#");
					t = t.substring(n, t.length());
					n = t.lastIndexOf("\"");
					onProperty = t.substring(1, n);
					r.setOnProperty(onProperty);
				}
				if (t.startsWith("<owl:someValuesFrom")) {
					// <owl:someValuesFrom rdf:resource="http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C12382"/>
					int n = t.lastIndexOf("#");
					t = t.substring(n, t.length());
					n = t.lastIndexOf("\"");
					someValueFrom = t.substring(1, n);
					r.setSomeValuesFrom(someValueFrom);
					if (!hset.contains(r.toString())) {
						hset.add(r.toString());
						Vector roles = new Vector();
						if (roleMap.containsKey(path)) {
							roles = (Vector) roleMap.get(path);
						}
						roles.add(r.toString());
						roleMap.put(path, roles);
					}
					r = null;
				}
			}
		}
		return roleMap;
	}



	public static Vector extractOWLRestrictions() {
		String owlfile = NCIT_OWL;
		int n = owlfile.lastIndexOf(".");
		String outputfile = "roles.txt";
		OWLScanner owlscanner = new OWLScanner(owlfile);
		return owlscanner.extractOWLRestrictions(owlscanner.get_owl_vec());
	}

	public HashMap createTagMap(Vector tags) {
		HashMap hmap = new HashMap();
		for (int i=0; i<tags.size(); i++) {
			String tag = (String) tags.elementAt(i);
			//owl:Axiom
			char c = tag.charAt(4);
			String s = "" + c;
			s = s.toUpperCase();
			hmap.put(tag, s);
		}
		return hmap;
	}


	public Vector extractTags(Vector v) {
		//Vector v = Utils.readFile(filename);
		Vector w = new Vector();
		HashSet hset = new HashSet();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			line = line.trim();
			if (line.startsWith("<owl:")) {
				if (line.indexOf("owl:annotated") == -1) {
					int n = line.indexOf(" ");
					//<owl:unionOf rdf:parseType="Collection">
					if (n != -1) {
						String t = line.substring(1, n);
						if (!hset.contains(t)) {
							hset.add(t);
							w.add(t);
						}
					} else {
						String t = line.substring(1, line.length()-1);
						if (!hset.contains(t)) {
							hset.add(t);
							w.add(t);
						}
					}
				}
			}
		}
		w = new SortUtils().quickSort(w);
		return w;
	}


	public static HashMap getRangeHashMap() {
		String restURL = ConfigurationController.serviceUrl;
		String namedGraph =  ConfigurationController.namedGraph;
		String username =  ConfigurationController.username;
		String password =  ConfigurationController.password;
		LogicalExpression le = new LogicalExpression(restURL, namedGraph, username, password);
		return le.getRangeHashMap(namedGraph);
	}



    public HashMap extractOutboundRolesInEquivalenceClasses(String owlfile) {
		int num_classes = 0;
		HashMap roleMap = new HashMap();
		String rdfs_label = "rdfs:label";
        Vector w = new Vector();
        boolean istart = false;
        String label = null;
        boolean isDefined = false;
        String classId = null;

        w = new Vector();
        StringBuffer buf = null;

        Vector class_vec = new Vector();

        Vector owl_vec = Utils.readFile(owlfile);

        //<?xml version="1.0"?>
        String firstLn = (String) owl_vec.elementAt(0);
        if (firstLn.indexOf("xml version=") == -1) {
			istart = true;
		}

        for (int i=0; i<owl_vec.size(); i++) {
			String t = (String) owl_vec.elementAt(i);
			t = t.trim();

			if (t.indexOf("// Classes") != -1) {
				istart = true;
				isDefined = false;
			}

			if (istart) {
				if (t.indexOf(NAMESPACE_TARGET) != -1 && t.endsWith("-->")) {
					if (classId != null && isDefined) {
						num_classes++;
						w.add(classId + "|" + label);
						HashMap map = getOutboundRolesV2(classId, class_vec);
						roleMap.put(classId, map);
						if (num_classes == 10) break;
					}
					classId = OWLScanner.extractClassId(t);
					label = null;
					isDefined = false;
					//process class_vec
					class_vec = new Vector();
				}

			    if (t.indexOf("<" + rdfs_label + ">") != -1) {
					String retstr = OWLScanner.parseProperty(t);
					Vector u = OWLScanner.split(retstr);
					label = (String) u.elementAt(1);
				}

				if (t.startsWith("<owl:equivalentClass")) {
                    isDefined = true;
				}
				class_vec.add(t);
			}
        }
		if (classId != null && isDefined) {
			w.add(classId + "|" + label);
			HashMap map = getOutboundRolesV2(classId, class_vec);
			roleMap.put(classId, map);
		}
		roleMap.put(CLASS_IDS, w);
		return roleMap;
    }

    public static void expressLogicalDefinition(PrintWriter pw, String classId, Vector parentCodes, HashMap hmap) {
		Vector u = StringUtils.parseData(classId, '|');
		String code = (String) u.elementAt(0);
		String label = (String) u.elementAt(1);
		pw.println("\n******************************************************************************************");
		pw.println(label + " (" + code + ")");

		pw.println("Parent");
		for (int i=0; i<parentCodes.size(); i++) {
			String parentCode = (String) parentCodes.elementAt(i);
			String parentLabel = (String) code2LabelMap.get(parentCode);
			pw.println("\t" + parentLabel + " (" + parentCode + ")");
		}

		Vector ranges = sortRanges(hmap);

		for (int i=0; i<ranges.size(); i++) {
			String range = (String) ranges.elementAt(i);
		    displayRolesForRange(pw, hmap, range);
		}
	}

	public static Vector sortRanges(HashMap hmap) {
		Vector w = new Vector();
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String path = (String) it.next();
			Vector v = (Vector) hmap.get(path);
			for(int i=0; i<v.size(); i++) {
				String value = (String) v.elementAt(i);
				Vector u = StringUtils.parseData(value, '|');
				String role = (String) u.elementAt(1);
                String range = (String) RANGE_HASHMAP.get(role);
                if (!w.contains(range)) {
					w.add(range);
				}
			}
		}
		return new SortUtils().quickSort(w);
	}


	public static void displayRolesForRange(PrintWriter pw, HashMap hmap, String range0) {
        //pw.println("\n" + range0);
		int count = 0;
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String path = (String) it.next();
			if (path.length() > 0) {
				Vector v = (Vector) hmap.get(path);
				if (path.endsWith("I|U|I")) {
                    int knt = 0;
					for(int i=0; i<v.size(); i++) {
						String value = (String) v.elementAt(i);
						Vector u = StringUtils.parseData(value, '|');
						String role = (String) u.elementAt(1);
						String targetCode = (String) u.elementAt(2);
						String range = (String) RANGE_HASHMAP.get(role);
						if (range.compareTo(range0) == 0) {
							count++;
						}
					}

				} else {
					for(int i=0; i<v.size(); i++) {
						String value = (String) v.elementAt(i);
						Vector u = StringUtils.parseData(value, '|');
						String role = (String) u.elementAt(1);
						String targetCode = (String) u.elementAt(2);
						String range = (String) RANGE_HASHMAP.get(role);
						if (range.compareTo(range0) == 0) {
							count++;
						}
					}
				}
			}
		}
		if (count == 0) return;
		pw.println("\n" + range0);
		it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String path = (String) it.next();
			if (path.length() > 0) {
				Vector v = (Vector) hmap.get(path);
				if (path.endsWith("I|U|I")) {
                    int knt = 0;
					for(int i=0; i<v.size(); i++) {
						String value = (String) v.elementAt(i);
						Vector u = StringUtils.parseData(value, '|');
						String role = (String) u.elementAt(1);
						String targetCode = (String) u.elementAt(2);
						String range = (String) RANGE_HASHMAP.get(role);
						if (range.compareTo(range0) == 0) {
							if (knt == 0) {
								pw.println("\tRole group");
							}
							String targetLabel = (String) code2LabelMap.get(targetCode);
							pw.println("\t" + (String) roleCode2LabelMap.get(role) + "\t" + targetLabel + " (" + targetCode + ")");
							knt++;
							if (knt == knt/2*2) {
								pw.println("\tor");
							}
						}
					}

				} else {
					for(int i=0; i<v.size(); i++) {
						String value = (String) v.elementAt(i);
						Vector u = StringUtils.parseData(value, '|');
						String role = (String) u.elementAt(1);
						String targetCode = (String) u.elementAt(2);
						String range = (String) RANGE_HASHMAP.get(role);
						if (range.compareTo(range0) == 0) {
							String targetLabel = (String) code2LabelMap.get(targetCode);
							pw.println("\t" + (String) roleCode2LabelMap.get(role) + "\t" + targetLabel + " (" + targetCode + ")");
						}
					}
				}
			}
		}
	}


	public static void main2(String[] args) {
        HashMap hmap = getRangeHashMap();
        Utils.dumpHashMap("getRangeHashMap", hmap);
	}

//
//<rdf:Description rdf:about="http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C3720"/>
    public static Vector getParentCodes(Vector class_vec) {
		Vector v = new Vector();
        for (int i=0; i<class_vec.size(); i++) {
			String t = (String) class_vec.elementAt(i);
			t = t.trim();

			if (t.indexOf("rdf:Description rdf:about") != -1) {
				int n = t.lastIndexOf("#");
				int m = t.lastIndexOf("\"");
				t = t.substring(n+1, m);
				v.add(t);
			}
		}
		return v;
	}

    public HashMap extractParentCodesInEquivalenceClasses(String owlfile) {
		int num_classes = 0;
		HashMap parentMap = new HashMap();
		String rdfs_label = "rdfs:label";
        Vector w = new Vector();
        boolean istart = false;
        String label = null;
        boolean isDefined = false;
        String classId = null;

        w = new Vector();
        StringBuffer buf = null;

        Vector class_vec = new Vector();

        Vector owl_vec = Utils.readFile(owlfile);

        //<?xml version="1.0"?>
        String firstLn = (String) owl_vec.elementAt(0);
        if (firstLn.indexOf("xml version=") == -1) {
			istart = true;
		}

        for (int i=0; i<owl_vec.size(); i++) {
			String t = (String) owl_vec.elementAt(i);
			t = t.trim();

			if (t.indexOf("// Classes") != -1) {
				istart = true;
				isDefined = false;
			}

			if (istart) {
				if (t.indexOf(NAMESPACE_TARGET) != -1 && t.endsWith("-->")) {
					if (classId != null && isDefined) {
						num_classes++;
						w.add(classId + "|" + label);
						Vector parents = getParentCodes(class_vec);
						parentMap.put(classId, parents);
						if (num_classes == 10) break;
					}
					classId = OWLScanner.extractClassId(t);
					label = null;
					isDefined = false;
					//process class_vec
					class_vec = new Vector();
				}

			    if (t.indexOf("<" + rdfs_label + ">") != -1) {
					String retstr = OWLScanner.parseProperty(t);
					Vector u = OWLScanner.split(retstr);
					label = (String) u.elementAt(1);
				}

				if (t.startsWith("<owl:equivalentClass")) {
                    isDefined = true;
				}
				class_vec.add(t);
			}
        }
		if (classId != null && isDefined) {
			w.add(classId + "|" + label);
			Vector parents = getParentCodes(class_vec);
			parentMap.put(classId, parents);
		}
		parentMap.put(CLASS_IDS, w);
		return parentMap;
    }

	public void generateLogicalDefinitionReport(String owlfile) {
		int n = owlfile.lastIndexOf(".");
		String outputfile = "LD_" + owlfile.substring(0, n) + ".txt";
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(outputfile, "UTF-8");
			HashMap parentMap = extractParentCodesInEquivalenceClasses(owlfile);
			HashMap roleMap = extractOutboundRolesInEquivalenceClasses(owlfile);
			Vector codes = (Vector) roleMap.get(CLASS_IDS);
			for (int i=0; i<codes.size(); i++) {
				String classId = (String) codes.elementAt(i);
				Vector u = StringUtils.parseData(classId, '|');
				String code = (String) u.elementAt(0);
				Vector parentCodes = (Vector) parentMap.get(code);
				HashMap hmap = (HashMap) roleMap.get(code);
				expressLogicalDefinition(pw, classId, parentCodes, hmap);
			}
		} catch (Exception ex) {

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
		long ms = System.currentTimeMillis();
		LogicalDefinitionGenerator test = new LogicalDefinitionGenerator();
        String owlfile = NCIT_OWL;
        if (args.length > 0) {
			owlfile = args[0];
		}
		test.generateLogicalDefinitionReport(owlfile);
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}
}

