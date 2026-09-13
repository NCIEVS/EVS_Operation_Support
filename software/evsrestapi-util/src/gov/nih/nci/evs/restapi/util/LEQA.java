package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;
import gov.nih.nci.evs.restapi.bean.*;
import java.io.*;
import java.util.*;
import org.apache.commons.text.StringEscapeUtils;

class LEQA {

	static String NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile; //"ThesaurusInferred_forTS.owl";
	static String PARENT_CHILD_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile; // "parent_child.txt";
    String textfile = null;
	OWLClassLoader loader = null;
	HierarchyHelper hh = null;
	String code = null;
	String label = null;
	Vector raw_data_vec = null;
	Vector classData = null;
	HashMap roleCode2RoleNameMap = null;
	HashMap objectPropertyCode2LabelMap = null;
	HashMap roleName2RangeNameMap = null;

	public LEQA() {
		initialize();
	}

	public void initialize() {
		roleName2RangeNameMap = NCItProperties.getRoleName2RangeNameMap();
		roleCode2RoleNameMap = NCItProperties.getRoleCode2RoleNameMap();
		hh = new HierarchyHelper(Utils.readFile(PARENT_CHILD_FILE));
		OWLScanner owlscanner = new OWLScanner(NCIT_OWL);
		Vector objectProperties = owlscanner.extractObjectProperties(owlscanner.get_owl_vec());
		objectPropertyCode2LabelMap = new HashMap();
		for (int i=0; i<objectProperties.size(); i++) {
			String line = (String) objectProperties.elementAt(i);
			Vector u = gov.nih.nci.evs.restapi.util.StringUtils.parseData(line, '|');
			objectPropertyCode2LabelMap.put((String) u.elementAt(0),(String) u.elementAt(1));
		}

		loader = new OWLClassLoader(NCIT_OWL);
	}

	public Vector getClassData(String code) {
		return (Vector) loader.getClassData(code);
	}

	public static String htmlEncode(String input) {
		return StringEscapeUtils.escapeHtml4(input);
	}

	public void le2HTML(String textfile) {
		//HashMap roleName2RangeNameMap = NCItProperties.getRoleName2RangeNameMap();
		Vector raw_data_vec = Utils.readFile(textfile);
        Vector data_vec = new Vector();
		HTMLTemplate test = null;
		try {
			test = new HTMLTemplate();
			test.setTooltipWidth(200);
			test.setTooltipHashMap(roleName2RangeNameMap);
			for (int i=0; i<raw_data_vec.size(); i++) {
				String line = (String) raw_data_vec.elementAt(i);
				if (line.indexOf("Logical expression of:") != -1) {

					Vector u = StringUtils.parseData(line, ':');
					String displayName = (String) u.elementAt(1);
					displayName = displayName.trim();
					int n = displayName.lastIndexOf("(");
					code = displayName.substring(n+1, displayName.length()-1);
					label = displayName.substring(0, n-1);
					System.out.println("code: " + code);
					System.out.println("label: " + label);

					data_vec.add("<hr></hr><h2><center>" + label + " (" + code + ")" + "</center></h2>");
					data_vec.add("<pre>");

					Vector classData = loader.getClassData(code);
					for (int j=0; j<classData.size(); j++) {
						String s = (String) classData.elementAt(j);
						s = htmlEncode(s);
						String indent = HTMLTemplate.getIndentation(s);
					    data_vec.add(indent + s);
					}
					data_vec.add("</pre>");
					data_vec.add("<p></p>");
					data_vec.add("<hr></hr><h2><center>" + line + "</center></h2>");

				} else {
					Vector u = StringUtils.parseData(line, '\t');
					String indent = HTMLTemplate.getIndentation(line);
					for (int j=0; j<u.size(); j++) {
						String s = (String) u.elementAt(j);
						if (roleName2RangeNameMap.containsKey(s)) {
							String toolTipvalue = (String) roleName2RangeNameMap.get(s);
							line = line.replace(s, test.toTooltip(s));
						}
					}
					if (indent.length() == 0) {
						data_vec.add("<p></p>" + line);
					} else {
						data_vec.add(indent + line);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		int n = textfile.lastIndexOf(".");
		String outputfile = textfile.substring(0, n) + ".html";
		String heading = (String) data_vec.elementAt(0);
		heading = heading.trim();
		data_vec.remove(0);
		test.generate(outputfile, heading, data_vec);
	}

/*
	public Restriction(
		String roleLabel,
		String targetCode,
		String targetLabel) {
Disease_Mapped_To_Gene	C38184	ALK Gene
*/
	public Vector<Restriction> countRestrictions(Vector le_vec) {
		Vector<Restriction> roles = new Vector();
		for (int i=0; i<le_vec.size(); i++) {
			String line = (String) le_vec.elementAt(i);
			Vector u = StringUtils.parseData(line, '\t');
			if (u.size() == 3) {
				Restriction r = new Restriction((String) u.elementAt(0), (String) u.elementAt(1), (String) u.elementAt(2));
				roles.add(r);
			}
		}
		return roles;
	}

    public String extractCode(String line) {
		int n1 = line.lastIndexOf("#");
		int n2 = line.lastIndexOf("\"");
		return line.substring(n1+1, n2);
	}

	public Vector<Restriction> countRestrictionsInOWL(Vector classData) {
		Vector<Restriction> roles = new Vector();
		boolean istart = false;
		for (int i=0; i<classData.size(); i++) {
			String line = (String) classData.elementAt(i);
			if (line.indexOf("<owl:equivalentClass>") != -1) {
				istart = true;
			} else if (line.indexOf("</owl:equivalentClass>") != -1) {
				istart = false;
				break;
			}
			if (istart) {
				if (line.indexOf("<owl:someValuesFrom") != -1) {
					String targetCode = extractCode(line);
					String targetLabel = hh.getLabel(targetCode);
					String line1 = (String) classData.elementAt(i-1);
					String propCode = extractCode(line1);
					String propName = (String) objectPropertyCode2LabelMap.get(propCode);
					Restriction r = new Restriction(propName, targetCode, targetLabel);
					roles.add(r);
				}
			}
		}
		return roles;
	}

	public Vector getEquivVec(Vector classData) {
		Vector w = new Vector();
		boolean istart = false;
		for (int i=0; i<classData.size(); i++) {
			String line = (String) classData.elementAt(i);
			if (line.indexOf("<owl:equivalentClass>") != -1) {
				istart = true;
			} else if (line.indexOf("</owl:equivalentClass>") != -1) {
				istart = false;
				w.add(line);
				break;
			}
			if (istart) {
				w.add(line);
			}
		}
		return w;
	}

	public Vector restrictionPathFinder(Vector equivClassData) {
		Vector w = new Vector();
		int equivClassCount = 0;
		int classCount = 0;
		int unionOfCount = 0;
		int intersectionOfCount = 0;
		int restrictionCount = 0;
		int i = 0;
		Stack stack = new Stack();
		String path = "";
		while (i<equivClassData.size()) {
			String line = (String) equivClassData.elementAt(i);
			if (line.indexOf("<owl:equivalentClass>") != -1) {
				path = "E";
				stack.push(path);
			} else if (line.indexOf("<owl:Class>") != -1) {
				path = (String) stack.pop();
				classCount++;
				path = path +"|C" + classCount;
				stack.push(path);
			} else if (line.indexOf("<owl:intersectionOf") != -1) {
				intersectionOfCount++;
				path = (String) stack.pop();
				path = path +"|I"+ intersectionOfCount;
				stack.push(path);
			} else if (line.indexOf("<owl:unionOf") != -1) {
				unionOfCount++;
				path = (String) stack.pop();
				path = path +"|U"+ unionOfCount;
				stack.push(path);
			} else if (line.indexOf("<rdf:Description") != -1) {
				path = (String) stack.pop();
				String line_trim = line;
				line_trim = line_trim.trim();
				String code = extractCode(line_trim);
				w.add(path + "|D|" + code);
				stack.push(path);
			} else if (line.indexOf("<owl:Restriction>") != -1) {
				path = (String) stack.pop();
				restrictionCount++;
				path = path +"|R"+ restrictionCount;
				stack.push(path);
			} else if (line.indexOf("<owl:onProperty") != -1) {
				path = (String) stack.pop();
				String line_trim = line;
				line_trim = line_trim.trim();
				String propCode = extractCode(line_trim);
				//w.add(path + "|" + code);
				i++;
				line = (String) equivClassData.elementAt(i);
				line_trim = line;
				line_trim = line_trim.trim();
				String targetCode = extractCode(line_trim);
				w.add(path + "|" + propCode + "$" + targetCode);
				stack.push(path);
			} else if (line.indexOf("</owl:Restriction>") != -1 ||
			           line.indexOf("</owl:unionOf>") != -1 ||
			           line.indexOf("</owl:intersectionOf>") != -1 ||
			           line.indexOf("</owl:Class>") != -1
			           ) {
				path = (String) stack.pop();
				int n = path.lastIndexOf("|");
				path = path.substring(0, n);
				stack.push(path);
			}
			i++;
		}
		return w;
	}

/*
	(1) E|C1|I1|D|C3720
	(2) E|C1|I1|C2|U1|C3|I2|R1|R114$C27711
	(3) E|C1|I1|C2|U1|C3|I2|R2|R89$C36706
	(4) E|C1|I1|C2|U1|C4|I3|R3|R114$C36435
	(5) E|C1|I1|C2|U1|C4|I3|R4|R89$C36707
	(6) E|C1|I1|C2|U1|C5|I4|R5|R114$C36436
	(7) E|C1|I1|C2|U1|C5|I4|R6|R89$C36708
	(8) E|C1|I1|C2|U1|C6|I5|R7|R114$C36437
	(9) E|C1|I1|C2|U1|C6|I5|R8|R89$C36711
	(10) E|C1|I1|C2|U1|C7|I6|R9|R114$C36590
	(11) E|C1|I1|C2|U1|C7|I6|R10|R89$C37216
	(12) E|C1|I1|C2|U1|C8|I7|R11|R114$C36591
	(13) E|C1|I1|C2|U1|C8|I7|R12|R89$C38348
	(14) E|C1|I1|C2|U1|C9|I8|R13|R114$C45439
	(15) E|C1|I1|C2|U1|C9|I8|R14|R89$C45442
	(16) E|C1|I1|C2|U1|C10|I9|R15|R114$C45440
	(17) E|C1|I1|C2|U1|C10|I9|R16|R89$C45443
	(18) E|C1|I1|C2|U1|C11|I10|R17|R114$C45441
	(19) E|C1|I1|C2|U1|C11|I10|R18|R89$C45444

	A role group contains many role sets
	A role group is identifed by E|C1|I1|C2|U1| (first five segments of a path)
	A role group is a union of several role sets
	A role set is the interception of roles

public class RoleSet {

// Variable declaration
	private List<Restriction> roles;
	*/

	public static HashMap createRoleGroupMap(Vector w) {
		HashMap hmap = new HashMap();
		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			if (u.size() > 6) {
				String key = (String) u.elementAt(0) + "|"
				           + (String) u.elementAt(1) + "|"
				           + (String) u.elementAt(2) + "|"
				           + (String) u.elementAt(3) + "|"
				           + (String) u.elementAt(4);
				HashMap setId2RolesMap = new HashMap();
				hmap.put(key, setId2RolesMap);
			}
		}

		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			if (u.size() > 7) {
				String key = (String) u.elementAt(0) + "|"
				           + (String) u.elementAt(1) + "|"
				           + (String) u.elementAt(2) + "|"
				           + (String) u.elementAt(3) + "|"
				           + (String) u.elementAt(4);

				HashMap setId2RolesMap = new HashMap();
				if (hmap.containsKey(key)) {
					setId2RolesMap = (HashMap) hmap.get(key);
				}
				String setId = (String) u.elementAt(5) + "|" + (String) u.elementAt(6); //C6|I5
				List groupSet = new ArrayList();
				if (setId2RolesMap.containsKey(setId)) {
					groupSet = (List) setId2RolesMap.get(setId);
				}
				String r = (String) u.elementAt(8);
				groupSet.add(r);
				setId2RolesMap.put(setId, groupSet);
				hmap.put(key, setId2RolesMap);
			}
		}
        return hmap;
	}

	public static Vector dumpRoleUnionMap(String type, HashMap hmap) {
		return dumpRoleGroupMap(type, hmap);
	}

	public static Vector dumpRoleGroupMap(String type, HashMap hmap) {
		Vector w = new Vector();
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			HashMap setId2RolesMap = (HashMap) hmap.get(key);
			if (setId2RolesMap.keySet().size() > 0) {
				System.out.println(type + ": " + key);
				w.add(type + ": " + key);
				Iterator it2 = setId2RolesMap.keySet().iterator();
				while (it2.hasNext()) {
					String setId = (String) it2.next();
					System.out.println("\tRole set: " + setId);
					List list = (List) setId2RolesMap.get(setId);
					for (int j=0; j<list.size(); j++) {
						String r = (String) list.get(j);
						System.out.println("\t\tRole: " + r);
						w.add("\t\tRole: " + r);
					}
				}
			}
		}
		return w;
	}

	public static HashMap createRoleUnionMap(Vector w) {
		HashMap hmap = new HashMap();
		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			if (u.size() == 7) {
				String key = (String) u.elementAt(0) + "|"
				           + (String) u.elementAt(1) + "|"
				           + (String) u.elementAt(2) + "|"
				           + (String) u.elementAt(3);
				HashMap setId2RolesMap = new HashMap();
				hmap.put(key, setId2RolesMap);
			}
		}

		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			if (u.size() == 7) {
				String key = (String) u.elementAt(0) + "|"
				           + (String) u.elementAt(1) + "|"
				           + (String) u.elementAt(2) + "|"
				           + (String) u.elementAt(3);

				HashMap setId2RolesMap = new HashMap();
				if (hmap.containsKey(key)) {
					setId2RolesMap = (HashMap) hmap.get(key);
				}

				String setId = (String) u.elementAt(4);
				List groupSet = new ArrayList();
				if (setId2RolesMap.containsKey(setId)) {
					groupSet = (List) setId2RolesMap.get(setId);
				}
				String r = (String) u.elementAt(6);
				groupSet.add(r);
				setId2RolesMap.put(setId, groupSet);
				hmap.put(key, setId2RolesMap);
			}
		}
        return hmap;
	}

	public static Vector getSimpleRestrictions(Vector w) {
		Vector roles = new Vector();
		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			if (u.size() < 6) {
				if (!u.contains("D")) {
					String r = (String) u.elementAt(4);
					roles.add(r);
				}
			}
		}
		return roles;
	}

	public static Vector getParents(Vector w) {
		Vector parents = new Vector();
		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			if (u.size() < 6) {
				if (u.contains("D")) {
					String s = (String) u.elementAt(u.size()-1);
					parents.add(s);
				}
			}
		}
		return parents;
	}

/*
UNION
paths:
	(1) E|C1|I1|D|C3029
	(2) E|C1|I1|D|C3266
	(3) E|C1|I1|D|C97075
	(4) E|C1|I1|C2|U1|R1|R176$C18251
	(5) E|C1|I1|C2|U1|R2|R176$C18252

	(18) E|C1|I1|C2|U1|C11|I10|R17|R114$C45441
	(19) E|C1|I1|C2|U1|C11|I10|R18|R89$C45444
*/

/*
	(20) E|C1|I1|R19|R104$C39687
	(21) E|C1|I1|R20|R105$C39679
	(22) E|C1|I1|R21|R106$C37208
	(23) E|C1|I1|R22|R106$C81946
	(24) E|C1|I1|R23|R113$C37024
	(25) E|C1|I1|R24|R113$C37026
	(26) E|C1|I1|R25|R113$C39680
	(27) E|C1|I1|R26|R115$C36156
	(28) E|C1|I1|R27|R115$C39695
	(29) E|C1|I1|R28|R115$C50764
	(30) E|C1|I1|R29|R176$C101046
	(31) E|C1|I1|R30|R176$C101059
	(32) E|C1|I1|R31|R176$C101075
	(33) E|C1|I1|R32|R176$C101083
	(34) E|C1|I1|R33|R176$C101085
	(35) E|C1|I1|R34|R176$C38184
	(36) E|C1|I1|R35|R176$C99361
	(37) E|C1|I1|R36|R176$C99869
	(38) E|C1|I1|R37|R176$C99873
*/

    public HashMap getLEData(Vector codes) {
		HashMap map = new HashMap();
		for (int i=0; i<codes.size(); i++) {
			String code = (String) codes.elementAt(i);
			map.put(code, getLEData(code));
		}
		return map;
	}

    public HashMap getLEData(String code) {
		HashMap hmap = new HashMap();
		hmap.put("code", code);
		hmap.put("label", hh.getLabel(code));
		Vector classData = getClassData(code);
		hmap.put("classData", classData);
		Vector equiv_vec = getEquivVec(classData);
		Vector w = restrictionPathFinder(equiv_vec);
		hmap.put("paths", w);
		Vector parents = getParents(w);
		hmap.put("Parents", parents);
		HashMap roleGroupMap = createRoleGroupMap(w);
		hmap.put("Role group", roleGroupMap);
		HashMap roleUnionMap = createRoleUnionMap(w);
		hmap.put("Role union", roleUnionMap);
		Vector roles = getSimpleRestrictions(w);
		hmap.put("Roles", roles);
		HashMap role2RangeMap = findRoleRange(hmap);
		hmap.put("role2RangeMap", role2RangeMap);
		return hmap;
	}

	public HashMap findRoleRange(HashMap hmap) {
		HashMap role2RangeMap = new HashMap();
		Vector w = (Vector) hmap.get("paths");
		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			if (u.size() > 1) {
				String role = (String) u.elementAt(u.size()-1);
				Vector u2 = StringUtils.parseData(role, '$');
				if (u2.size() > 1) {
					String propCode = (String) u2.elementAt(0);
					String roleName = (String) roleCode2RoleNameMap.get(propCode);
					String range = (String) roleName2RangeNameMap.get(roleName);
					role2RangeMap.put(propCode, range);
				}
			}
		}
		return role2RangeMap;
	}

	public static void dumpLEData(HashMap hmap) {
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String code = (String) it.next();
			HashMap leMap = (HashMap) hmap.get(code);
			String displayName = (String) leMap.get("label") + " (" + code + ")";
			dumpLEData(displayName, leMap);
		}
	}

	public static void dumpLEData(String displayName, HashMap hmap) {
		System.out.println(displayName);
		Vector classData = (Vector) hmap.get("classData");
		Utils.dumpVector("classData", classData);
        Vector paths = (Vector) hmap.get("paths");
        Utils.dumpVector("Paths", paths);
		Vector parents = (Vector) hmap.get("Parents");
		Utils.dumpVector("Parents", parents);
        Iterator it = null;
		HashMap roleGroupMap = (HashMap) hmap.get("Role group");
		if (roleGroupMap != null && roleGroupMap.keySet().size() > 0) {
			System.out.println("Role group");
			it = roleGroupMap.keySet().iterator();
			while (it.hasNext()) {
				String key = (String) it.next();
				HashMap map = (HashMap) roleGroupMap.get(key);
				Iterator it2 = map.keySet().iterator();
				while (it2.hasNext()) {
					String key2 = (String) it2.next();
					List list = (List) map.get(key2);
				    Utils.dumpList("\t" + key2, list);
				}
			}
		}

		HashMap roleUnionMap = (HashMap) hmap.get("Role union");
		if (roleUnionMap != null && roleUnionMap.keySet().size() > 0) {
			System.out.println("Role union");
			it = roleUnionMap.keySet().iterator();
			while (it.hasNext()) {
				String key = (String) it.next();
				HashMap map = (HashMap) roleUnionMap.get(key);
				Iterator it2 = map.keySet().iterator();
				while (it2.hasNext()) {
					String key2 = (String) it2.next();
					List list = (List) map.get(key2);
				    Utils.dumpList("\t" + key2, list);
				}
			}
		}

		Vector roles = (Vector) hmap.get("Roles");
		if (roles != null) {
			Utils.dumpVector("Roles", roles);
		}

		HashMap role2RangeMap = (HashMap) hmap.get("role2RangeMap");
		Utils.dumpHashMap("role2RangeMap", role2RangeMap);
	}

	public static void leData2Expression(HashMap hmap) {


	}

	public static void main(String[] args) {
		LEQA test = new LEQA();
		String textfile = args[0];
		Vector v = Utils.readFile(textfile);
		boolean html = false;
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '\t');
			if (u.size() > 1) {
				html = true;
			}
		}
		if (html) {
			System.out.println("Calling le2HTML ...");
			test.le2HTML(textfile);
		} else {
			System.out.println("Calling getLEData ...");
			HashMap hmap = test.getLEData(v);
			dumpLEData(hmap);
			//"le_data_" + textfile,
		}
	}
}
