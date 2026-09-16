package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;
import gov.nih.nci.evs.restapi.bean.*;
import java.io.*;
import java.util.*;
import org.apache.commons.text.StringEscapeUtils;

class LEQA {

	static String NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile; //"ThesaurusInferred_forTS.owl";
	static String PARENT_CHILD_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile; // "parent_child.txt";

	OWLClassLoader loader = null;
	HierarchyHelper hh = null;
	String code = null;
	String label = null;
	Vector raw_data_vec = null;
	Vector classData = null;
	HashMap roleCode2RoleNameMap = null;
	HashMap objectPropertyCode2LabelMap = null;
	HashMap roleName2RangeNameMap = null;
	Vector equivalentClasses = null;
	static String PATH_FILE = "paths.txt";
	Vector rangeNames = null;

	public LEQA() {
		initialize();
	}

	public void initialize() {
		roleName2RangeNameMap = NCItProperties.getRoleName2RangeNameMap();
		rangeNames = new Vector();
		Iterator it = roleName2RangeNameMap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			String range = (String) roleName2RangeNameMap.get(key);
			rangeNames.add(range);
		}
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
		equivalentClasses = owlscanner.extractEquivalenceClasses();
		owlscanner.get_owl_vec().clear();
	}

	public boolean isRangeName(String t) {
		return rangeNames.contains(t);
	}

	public Vector getEquivalentClasses() {
		return equivalentClasses;
	}

	public Vector getClassData(String code) {
		return (Vector) loader.getClassData(code);
	}

	public static String htmlEncode(String input) {
		return StringEscapeUtils.escapeHtml4(input);
	}

    public Vector format(Vector v) {
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '\t');
			if(u.size() > 1) {
				line = "\t\t" + line;
			} else {
				String t = line;
				t = t.trim();
				if (rangeNames.contains(t)) {
					System.out.println(t);
					w.add("\t");
				}
			}
			w.add(line);
		}
		return w;
	}

	public void le2HTML(String textfile) {
		int n = textfile.lastIndexOf(".");
		String htmlfile = textfile.substring(0, n) + ".html";
		le2HTML(textfile, htmlfile);
	}

	public void le2HTML(String textfile, String htmlfile) {
		Vector raw_data_vec = Utils.readFile(textfile);
		le2HTML(raw_data_vec, htmlfile);
	}

	public void le2HTML(Vector raw_data_vec, String htmlfile) {
        Vector data_vec = new Vector();
		HTMLTemplate test = null;
		try {
			test = new HTMLTemplate();
			test.setTooltipWidth(200);
			test.setTooltipHashMap(roleName2RangeNameMap);

			for (int i=0; i<raw_data_vec.size(); i++) {
				String line = (String) raw_data_vec.elementAt(i);
				String line0 = line;
				line0 = line0.trim();
				if (line.indexOf("Logical expression of:") != -1) {
					Vector u = StringUtils.parseData(line, ':');
					String displayName = (String) u.elementAt(1);
					displayName = displayName.trim();
					int n = displayName.lastIndexOf("(");
					code = displayName.substring(n+1, displayName.length()-1);
					label = displayName.substring(0, n-1);
					data_vec.add("<hr></hr><h2><center>" + label + " (" + code + ")" + "</center></h2>");
				} else if (line.indexOf("<!-- http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#") != -1) {
					data_vec.add("<p></p><hr></hr>");
				} else {
					Vector u = StringUtils.parseData(line, '\t');
					String indent = HTMLTemplate.getIndentation(line);
					String line1 = line;
					for (int j=0; j<u.size(); j++) {
						String s = (String) u.elementAt(j);
						if (roleName2RangeNameMap.containsKey(s)) {
							String toolTipvalue = (String) roleName2RangeNameMap.get(s);
							line1 = line.replace(s, test.toTooltip(s));
						}
					}

					if (line1.compareTo(line) != 0) {
						data_vec.add(indent + line1);
					} else {
						line = htmlEncode(line);
						data_vec.add(indent + line);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		String heading = (String) data_vec.elementAt(0);
		heading = heading.trim();
		data_vec.remove(0);
		test.generate(htmlfile, heading, data_vec);
	}

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

	public String getLabel(String code) {
		return hh.getLabel(code);
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
		HashMap roleUnionMap = createRoleUnionMap(w);
		Vector roles = getSimpleRestrictions(w);
		hmap.put("Roles", roles);

		HashMap role2RangeMap = findRoleRange(hmap);
		hmap.put("role2RangeMap", role2RangeMap);

		Vector roleRanges = findRoleRanges(hmap);
		hmap.put("roleRanges", roleRanges);

		List roleGroupList = constructRoleGroupList(roleGroupMap);
		hmap.put("roleGroupList", roleGroupList);

		List roleUnionList = constructRoleUnionList(roleUnionMap);
		hmap.put("roleUnionList", roleUnionList);

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
					role2RangeMap.put(roleName, range);
				}
			}
		}
		return role2RangeMap;
	}

	public Vector findRoleRanges(HashMap hmap) {
		Vector roleRanges = new Vector();
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
					roleRanges.add(range);
				}
			}
		}
		return roleRanges;
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
		boolean debug = false;
		System.out.println(displayName);
		Vector classData = (Vector) hmap.get("classData");
		if (debug) Utils.dumpVector("classData", classData);
        Vector paths = (Vector) hmap.get("paths");
        if (debug) Utils.dumpVector("Paths", paths);
		Vector parents = (Vector) hmap.get("Parents");
		if (debug) Utils.dumpVector("Parents", parents);
        Iterator it = null;
		HashMap roleGroupMap = (HashMap) hmap.get("Role group");
		if (roleGroupMap != null && roleGroupMap.keySet().size() > 0) {
			if (debug) System.out.println("Role group");
			it = roleGroupMap.keySet().iterator();
			while (it.hasNext()) {
				String key = (String) it.next();
				HashMap map = (HashMap) roleGroupMap.get(key);
				Iterator it2 = map.keySet().iterator();
				while (it2.hasNext()) {
					String key2 = (String) it2.next();
					List list = (List) map.get(key2);
				    if (debug) Utils.dumpList("\t" + key2, list);
				}
			}
		}

		HashMap roleUnionMap = (HashMap) hmap.get("Role union");
		if (roleUnionMap != null && roleUnionMap.keySet().size() > 0) {
			if (debug) System.out.println("Role union");
			it = roleUnionMap.keySet().iterator();
			while (it.hasNext()) {
				String key = (String) it.next();
				HashMap map = (HashMap) roleUnionMap.get(key);
				Iterator it2 = map.keySet().iterator();
				while (it2.hasNext()) {
					String key2 = (String) it2.next();
					List list = (List) map.get(key2);
				    if (debug) Utils.dumpList("\t" + key2, list);
				}
			}
		}

		Vector roles = (Vector) hmap.get("Roles");
		if (roles != null) {
			if (debug) Utils.dumpVector("Roles", roles);
		}

		HashMap role2RangeMap = (HashMap) hmap.get("role2RangeMap");
		if (debug) Utils.dumpHashMap("role2RangeMap", role2RangeMap);
	}

	static String NONE = "none";
	static String MULTIPLE = "multiple";

	public static String findRange(HashMap role2RangeMap, HashMap roleGroupMap) {
		if (roleGroupMap != null && roleGroupMap.keySet().size() > 0) {
			return NONE;
	    }
	    String range = null;
        boolean rg_consistent = true;
		Iterator it = null;
		if (roleGroupMap != null && roleGroupMap.keySet().size() > 0) {
			System.out.println("Role group");
			it = roleGroupMap.keySet().iterator();
			while (it.hasNext()) {
				if (!rg_consistent) break;
				String key = (String) it.next();
				HashMap map = (HashMap) roleGroupMap.get(key);
				Iterator it2 = map.keySet().iterator();
				while (it2.hasNext()) {
					String key2 = (String) it2.next();
					List list = (List) map.get(key2);
					for (int i=0; i<list.size(); i++) {
						String line = (String) list.get(i);
						Vector u = StringUtils.parseData(line, '|');
						String t = (String) u.elementAt(u.size()-1);
						Vector u2 = StringUtils.parseData(t, '$');
						String propCode = (String) u2.elementAt(0);
						String rangeName = (String) role2RangeMap.get(propCode);
						if (range == null) {
							range = rangeName;
						} else {
							if (range.compareTo(rangeName) != 0) {
								rg_consistent = false;
								break;
							}
						}
					}
				}
			}
		}
		if (!rg_consistent) return MULTIPLE;
		return range;
	}

	public String findRange(RoleGroup rg) {
	    String range = null;
        boolean rg_consistent = true;
        List<RoleSet> roleSets = rg.getRoleSets();
        for (int i=0; i<roleSets.size(); i++) {
			RoleSet rs = roleSets.get(i);
			List rs_list = rs.getRoles();
			for (int j=0; j<rs_list.size(); j++) {
				Restriction r = (Restriction) rs_list.get(j);
				String roleName = r.getRoleLabel();
				String rangeName = (String) roleName2RangeNameMap.get(roleName);
				if (range == null) {
					range = rangeName;
				} else {
					if (range.compareTo(rangeName) != 0) {
						rg_consistent = false;
						break;
					}
				}
			}
			if (!rg_consistent) {
				break;
			}
		}
		if (!rg_consistent) return MULTIPLE;
		return range;
	}

	public String findRange(RoleUnion ru) {
	    String range = null;
        boolean ru_consistent = true;
        List<Restriction> roles = ru.getRoles();
		for (int j=0; j<roles.size(); j++) {
			Restriction r = (Restriction) roles.get(j);
			String roleName = r.getRoleLabel();
			String rangeName = (String) roleName2RangeNameMap.get(roleName);
			if (range == null) {
				range = rangeName;
			} else {
				if (range.compareTo(rangeName) != 0) {
					ru_consistent = false;
					break;
				}
			}
		}
		if (!ru_consistent) return MULTIPLE;
		return range;
	}

	public static List vector2List(Vector v) {
		List list = new ArrayList();
		for (int i=0; i<v.size(); i++) {
			Object obj = v.elementAt(i);
			list.add(obj);
		}
		return list;
	}

    public Restriction toRestriction(String s) {
		Vector u = StringUtils.parseData(s, '$');
		return new Restriction((String) objectPropertyCode2LabelMap.get((String) u.elementAt(0)),
		                       (String) u.elementAt(1),
		                       hh.getLabel((String) u.elementAt(1)));
	}

    public List constructRoleUnionList(HashMap roleUnionMap) {
		List list = new ArrayList();
		Iterator it = roleUnionMap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			HashMap groupSetMap = (HashMap) roleUnionMap.get(key);
			Iterator it2 = groupSetMap.keySet().iterator();
			while (it2.hasNext()) {
				String key2 = (String) it2.next();
				List groupSet = (List) groupSetMap.get(key2);
				if (groupSet != null && groupSet.size() > 0) {
					RoleUnion union = new RoleUnion();
					List roles = new ArrayList();
					for (int i=0; i<groupSet.size(); i++) {
						String s = (String) groupSet.get(i);
						roles.add(toRestriction(s));
					}
					union.setRoles(roles);
					list.add(union);
				}
			}
		}
		return list;
	}

    public List constructRoleGroupList(HashMap roleGroupMap) {
		List list = new ArrayList();
		Iterator it = roleGroupMap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			RoleGroup group = new RoleGroup();
			List roleSets = new ArrayList();
			HashMap setId2RolesMap = (HashMap) roleGroupMap.get(key);
			Iterator it2 = setId2RolesMap.keySet().iterator();
			while (it2.hasNext()) {
				String setId = (String) it2.next();
			    List groupSet = (List) setId2RolesMap.get(setId);
			    List roles = new ArrayList();
				for (int i=0; i<groupSet.size(); i++) {
					String s = (String) groupSet.get(i);
					Restriction r = toRestriction(s);
					roles.add(r);
				}
				RoleSet roleSet = new RoleSet(roles);
				roleSets.add(roleSet);
			}
			group.setRoleSets(roleSets);
			list.add(group);
		}
		return list;
	}

	public gov.nih.nci.evs.restapi.bean.LogicalExpression leData2Expression(HashMap hmap) {
		// check for the consistency of ranges of role groups and rolel unions
		HashMap role2RangeMap = (HashMap) hmap.get("role2RangeMap");
        List<LogicalExpressionElement> elements = new ArrayList();
        HashMap range2ElementMap = new HashMap();
        Iterator it3 = role2RangeMap.keySet().iterator();
        while (it3.hasNext()) {
			String key = (String) it3.next();
			String value = (String) role2RangeMap.get(key);
			LogicalExpressionElement leElem = new LogicalExpressionElement(value, new ArrayList(), new ArrayList(), new ArrayList());
			range2ElementMap.put(value, leElem);
		}

		Vector parents = (Vector) hmap.get("Parents");
		List parentList = new ArrayList();
		for (int k=0; k<parents.size(); k++) {
			String code = (String) parents.elementAt(k);
			Concept c = new Concept(k, hh.getLabel(code), code);
			parentList.add(c);
		}

		Vector roleRanges = (Vector) hmap.get("roleRanges");
		List roleGroupList = (List) hmap.get("roleGroupList");
		if (roleGroupList != null && roleGroupList.size() > 0) {
			for (int i=0; i<roleGroupList.size(); i++) {
				RoleGroup rg = (RoleGroup) roleGroupList.get(i);
				String rg_range = findRange(rg);
				if (!roleRanges.contains(rg_range)) {
					LogicalExpressionElement leElem = new LogicalExpressionElement("", new ArrayList(), new ArrayList(), new ArrayList());
					range2ElementMap.put("", leElem);
					break;
				}
			}
		}
		List roleUnionList = (List) hmap.get("roleUnionList");
		if (!range2ElementMap.containsKey("")) {
			if (roleUnionList != null && roleUnionList.size() > 0) {
				for (int i=0; i<roleUnionList.size(); i++) {
					RoleUnion ru = (RoleUnion) roleUnionList.get(i);
     				String ru_range = findRange(ru);
					if (!roleRanges.contains(ru_range)) {
						LogicalExpressionElement leElem = new LogicalExpressionElement("", new ArrayList(), new ArrayList(), new ArrayList());
						range2ElementMap.put("", leElem);
						break;
					}
				}
			}
		}


		if (roleGroupList != null && roleGroupList.size() > 0) {
			for (int i=0; i<roleGroupList.size(); i++) {
				RoleGroup rg = (RoleGroup) roleGroupList.get(i);
				String rg_range = findRange(rg);
				String key = rg_range;
				if (!roleRanges.contains(rg_range)) {
					key = "";
				}
				LogicalExpressionElement leElem = (LogicalExpressionElement) range2ElementMap.get(key);
				List list = leElem.getRoleGroups();
				list.add(rg);
				leElem.setRoleGroups(list);
				range2ElementMap.put(key, leElem);
			}
		} else {
			//System.out.println("==================== No group data found. ====================");
		}

		if (roleUnionList != null && roleUnionList.size() > 0) {
			for (int i=0; i<roleUnionList.size(); i++) {
				RoleUnion ru = (RoleUnion) roleUnionList.get(i);
				String ru_range = findRange(ru);
				String key = ru_range;
				if (!roleRanges.contains(ru_range)) {
					key = "";
				}
    			LogicalExpressionElement leElem = (LogicalExpressionElement) range2ElementMap.get(key);
				List list = leElem.getRoleUnions();
				list.add(ru);
				leElem.setRoleUnions(list);
				range2ElementMap.put(key, leElem);
			}
		}
		Vector roles = (Vector) hmap.get("Roles");
		if (roles != null && roles.size() > 0) {
			for (int i=0; i<roles.size(); i++) {
				String r = (String) roles.elementAt(i);
				Vector u = StringUtils.parseData(r, '$');
				String roleCode = (String) u.elementAt(0);
				String roleName = (String) roleCode2RoleNameMap.get(roleCode);
				String range = (String) role2RangeMap.get(roleName);
				LogicalExpressionElement leElem = (LogicalExpressionElement) range2ElementMap.get(range);
				List list = leElem.getRoles();
				if (list == null) {
					list = new ArrayList();
				}
				list.add(toRestriction(r));
				leElem.setRoles(list);
				range2ElementMap.put(range, leElem);
			}
		}

		String label = (String) hmap.get("label");
		String code = (String) hmap.get("code");
		elements = new ArrayList();
		Iterator it0 = range2ElementMap.keySet().iterator();
		while (it0.hasNext()) {
			String key0 = (String) it0.next();
			LogicalExpressionElement leElem = (LogicalExpressionElement) range2ElementMap.get(key0);
			elements.add(leElem);
		}
        gov.nih.nci.evs.restapi.bean.LogicalExpression le = new gov.nih.nci.evs.restapi.bean.LogicalExpression(
			code,
			hh.getLabel(code),
			parentList,
			elements,
			null
			);
		return le;
	}


	public void analyze() {
		File f = new File(PATH_FILE);
		if (!f.exists()) {
			Vector w = parse();
			Utils.saveToFile(PATH_FILE, w);
		} else {
			HashSet hset = new HashSet();
			Vector w = Utils.readFile(PATH_FILE);
			for (int i=0; i<w.size(); i++) {
				String line = (String) w.elementAt(i);
				if (line.indexOf("$") != -1) {
					//Vector u = StringUtils.parseData(line, '|');
					//E|C1|I1|R1|R108$C35983
					int n = line.lastIndexOf("|R");
					String path = line.substring(0, n);
					if (!hset.contains(path)) {
						hset.add(path);
					}
				}
			}
			Vector v = Utils.hashSet2Vector(hset);
			v = new SortUtils().quickSort(v);
			Utils.dumpVector("paths", v);
		}
	}

    public Vector parse() {
		long ms = System.currentTimeMillis();
		if (equivalentClasses == null) {
			equivalentClasses = getEquivalentClasses();
		}
		Vector w = new Vector();
		int lcv = 1;
		int increment = 1000;
		int total = equivalentClasses.size();
		for (int i=0; i<equivalentClasses.size(); i++) {
			int j = i+1;
			if (lcv == increment) {
				System.out.println("" + j + " out of " + total + " completed.");
				lcv = 0;
			}
			lcv++;
			String line = (String) equivalentClasses.elementAt(i);
			w.add("(" + j + ") " + line);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(0);
			Vector classData = getClassData(code);
			Vector equiv_vec = getEquivVec(classData);
			Vector paths = restrictionPathFinder(equiv_vec);
			w.addAll(paths);
		}
		System.out.println("" + total + " out of " + total + " completed.");
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
		return w;
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
			test.le2HTML(textfile);
		} else {
			HashMap hmap = test.getLEData(v);
			Iterator it = hmap.keySet().iterator();
			while (it.hasNext()) {
				String key = (String) it.next();
				HashMap map = (HashMap) hmap.get(key);
    			gov.nih.nci.evs.restapi.bean.LogicalExpression le = test.leData2Expression(map);
    			try {
					System.out.println("==================================================================================================================");
    				System.out.println(le.toString());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}
}
