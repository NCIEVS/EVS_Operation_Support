package gov.nih.nci.evs.restapi.appl;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.bean.*;
import gov.nih.nci.evs.restapi.config.*;


import java.io.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.*;
import org.json.*;

/**
 * <!-- LICENSE_TEXT_START -->
 * Copyright 2020 MSC. This software was developed in conjunction
 * with the National Cancer Institute, and so to the extent government
 * employees are co-authors, any rights in such works shall be subject
 * to Title 17 of the United States Code, section 105.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the disclaimer of Article 3,
 *      below. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *   2. The end-user documentation included with the redistribution,
 *      if any, must include the following acknowledgment:
 *      "This product includes software developed by MSC and the National
 *      Cancer Institute."   If no such end-user documentation is to be
 *      included, this acknowledgment shall appear in the software itself,
 *      wherever such third-party acknowledgments normally appear.
 *   3. The names "The National Cancer Institute", "NCI" and "MSC" must
 *      not be used to endorse or promote products derived from this software.
 *   4. This license does not authorize the incorporation of this software
 *      into any third party proprietary programs. This license does not
 *      authorize the recipient to use any trademarks owned by either NCI
 *      or MSC
 *   5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED
 *      WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *      OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE
 *      DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
 *      MSC, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT,
 *      INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 *      BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *      LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *      CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 *      LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 *      ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *      POSSIBILITY OF SUCH DAMAGE.
 * <!-- LICENSE_TEXT_END -->
 */

/**
 * @author EVS Team
 * @version 1.0
 *
 * Modification history:
 *     Initial implementation kim.ong@nih.gov
 *
 */


public class LogicalExpressionFormatter {
	HashMap roleName2RangeNameMap = null;
	HashMap roleCode2RangeNameMap = null;
	Vector PATHS = null;
	static HashMap roleDataMap = null;
	static HashMap range2ExpressionMap = null;
	static String RANGE_RESSULT = "range_results.txt";

	public LogicalExpressionFormatter() {

	}

	public void setRangeMaps(HashMap roleName2RangeNameMap, HashMap roleCode2RangeNameMap) {
		this.roleName2RangeNameMap = roleName2RangeNameMap;
		this.roleCode2RangeNameMap = roleCode2RangeNameMap;
	}

	public void setpaths(Vector paths) {
		this.PATHS = paths;
	}

	public void initialize() {
        Vector v = Utils.readFile(RANGE_RESSULT);
        v = removeBrackets(v);
        //(1) R100|Disease_Has_Associated_Anatomic_Site|C12219|Anatomic Structure, System, or Substance
        roleName2RangeNameMap = new HashMap();
        roleCode2RangeNameMap = new HashMap();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String roleCode = (String) u.elementAt(0);
			String roleName = (String) u.elementAt(1);
			String range = (String) u.elementAt(3);
			roleName2RangeNameMap.put(roleName, range);
			roleCode2RangeNameMap.put(roleCode, range);
		}

        PATHS = new Vector();
        PATHS.add("E|I|O|C");
        PATHS.add("E|I|O|R");
        PATHS.add("E|I|O|U|O|R");
        PATHS.add("E|I|O|U|O|I|O|R");
	}

	public static Vector removeBrackets(Vector v) {
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			int n = line.indexOf(")");
			if (n != -1) {
				line = line.substring(n+2, line.length());
			}
			w.add(line);
		}
		return w;
	}

	public HashMap loadRoleDataMap(String roledatafile) {
	    Vector v = Utils.readFile(roledatafile);
	    v = removeBrackets(v);
	    roleDataMap = new HashMap();
	    Vector w = new Vector();
	    String key = null;
	    for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (PATHS.contains(line)) {
				if (key != null) {
					roleDataMap.put(key, w);
					w = new Vector();
				}
				key = line;
			} else {
				w.add(line);
			}
		}
		roleDataMap.put(key, w);
		return roleDataMap;
	}

	public HashMap generateRange2RoleGroupExpressionMap(Vector role_group_vec) {
		if (role_group_vec == null || role_group_vec.size() == 0) return null;
		HashMap hmap = new HashMap();
		Vector w = new Vector();
		for (int i=0; i<role_group_vec.size(); i++) {
			String line = (String) role_group_vec.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String roleCode = (String) u.elementAt(u.size()-4);
			String roleRange = (String) roleName2RangeNameMap.get(roleCode);
			w = new Vector();
		    if (hmap.containsKey(roleRange)) {
				w = (Vector) hmap.get(roleRange);
			}
			w.add(line);
			hmap.put(roleRange, w);
		}
		HashMap expressionMap = new HashMap();
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String range = (String) it.next();
			w = (Vector) hmap.get(range);
			//Utils.dumpVector(range, w);
			String roleExpression = generateRoleGroupExpression(w);
			expressionMap.put(range, roleExpression);
		}
		return expressionMap;
	}

	public static void dumpMultiValuedHashMap(HashMap hmap) {
		Utils.dumpMultiValuedHashMap("LogicalExpression Data", hmap);
	}

    public String getLogicalExpression(HashMap hmap) {
// Parents:
        StringBuffer buf = new StringBuffer();
        Vector parents = (Vector) hmap.get("E|I|O|C");
        Vector parent_vec = new Vector();
        Vector w = new Vector();

        if (parents != null && parents.size() > 0) {
			buf.append("Parent").append("\n");
			for (int i=0; i<parents.size(); i++) {
				String line = (String) parents.elementAt(i);
				Vector u = StringUtils.parseData(line, '|');
				try {
					parent_vec.add("\t" + (String) u.elementAt(u.size()-1) + " (" + (String) u.elementAt(u.size()-2) + ")");
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		parent_vec = new SortUtils().quickSort(parent_vec);
		for (int i=0; i<parent_vec.size(); i++) {
			String line = (String) parent_vec.elementAt(i);
			buf.append(line).append("\n");
		}

// Simple Restrictions:
        HashMap range2RolesHashMap = new HashMap();
        String path = "E|I|O|R";
        Vector restrictions = (Vector) hmap.get("E|I|O|R");
	    Vector v = retrieveRestrictions(restrictions);
//	(1) C6481|Inflammatory Myofibroblastic Tumor|b0|b1|Disease_Has_Abnormal_Cell|R105|C36957|Neoplastic Spindle-Shaped Myofibroblast
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '\t');
			String roleName = (String) u.elementAt(0);
			String range = (String) roleName2RangeNameMap.get(roleName);
			w = new Vector();
			if (range2RolesHashMap.containsKey(range)) {
				w = (Vector) range2RolesHashMap.get(range);
			}
			w.add(line);
			range2RolesHashMap.put(range, w);
		}

// Simple Restriction group:
        Vector simple_role_groups = (Vector) hmap.get("E|I|O|U|O|R");
        //Utils.dumpVector("simple_role_groups (E|I|O|U|O|R)", simple_role_groups);
        HashMap range2RoleGroupExpressionMap = generateRange2RoleGroupExpressionMap(simple_role_groups);
        if (range2RoleGroupExpressionMap != null) {
            Iterator it = range2RoleGroupExpressionMap.keySet().iterator();
            while (it.hasNext()) {
				String range = (String) it.next();
				String roleExpression = (String) range2RoleGroupExpressionMap.get(range);
				w = new Vector();
				if (range2RolesHashMap.containsKey(range)) {
					w = (Vector) range2RolesHashMap.get(range);
				}
				w.add(roleExpression + "|RG");
			    range2RolesHashMap.put(range, w);
			}
		}

// multiple_role_groups:
        Vector multiple_role_groups = (Vector) hmap.get("E|I|O|U|O|I|O|R");
        //Utils.dumpVector("multiple_role_groups (E|I|O|U|O|I|O|R)", multiple_role_groups);
        range2RoleGroupExpressionMap = generateRange2RoleGroupExpressionMap(multiple_role_groups);
        if (range2RoleGroupExpressionMap != null) {
            Iterator it = range2RoleGroupExpressionMap.keySet().iterator();
            while (it.hasNext()) {
				String range = (String) it.next();
				String roleExpression = (String) range2RoleGroupExpressionMap.get(range);
				w = new Vector();
				if (range2RolesHashMap.containsKey(range)) {
					w = (Vector) range2RolesHashMap.get(range);
				}
				w.add(roleExpression + "|RG");
			    range2RolesHashMap.put(range, w);
			}
		}
		String parentStr = buf.toString();
		return parentStr + "\n" + range2RolesHashMap2Expression(range2RolesHashMap);
	}

    static String range2RolesHashMap2Expression(HashMap range2RolesHashMap) {
		StringBuffer buf = new StringBuffer();
		Vector key_vec = new Vector();
		Iterator it = range2RolesHashMap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			key_vec.add(key);
		}
		key_vec = new SortUtils().quickSort(key_vec);
		for (int i=0; i<key_vec.size(); i++) {
			String key = (String) key_vec.elementAt(i);
			if (key != null) {
				buf.append("\n" + key).append("\n");
				Vector values = (Vector) range2RolesHashMap.get(key);
				Vector sorted_values = new Vector();
				for (int j=0; j<values.size(); j++) {
					String value = (String) values.elementAt(j);
					if (!value.endsWith("|RG")) {
						sorted_values.add(value);
						//buf.append("\t" + value).append("\n");
					}
				}
				//sorted_values = new SortUtils().quickSort(sorted_values);
				for (int j=0; j<sorted_values.size(); j++) {
					String value = (String) sorted_values.elementAt(j);
					buf.append("\t" + value).append("\n");
				}
				for (int j=0; j<values.size(); j++) {
					String value = (String) values.elementAt(j);
					if (value.endsWith("|RG")) {
						Vector u = StringUtils.parseData(value, '|');
						value = (String) u.elementAt(0);
						buf.append("\t" + value).append("\n");
					}
				}
			}
		}
		return buf.toString();
	}

    public void dumpRoleGroupRangeData(Vector role_group_vec) {
		for (int i=0; i<role_group_vec.size(); i++) {
			String line = (String) role_group_vec.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String roleName = (String) u.elementAt(u.size()-4);
			String roleRange = (String) roleName2RangeNameMap.get(roleName);
			System.out.println(line + " (" + roleName + " range: " + roleRange + ")");
		}
	}

    public String generateRoleGroupExpression(Vector role_group_vec) {
		if (role_group_vec == null || role_group_vec.size() == 0) return "";
		role_group_vec = retrieveRestrictions(role_group_vec);
//Utils.dumpVector("generateRoleGroupExpression role_group_vec", role_group_vec);

		StringBuffer buf2 = new StringBuffer();
		buf2.append("\n\t").append("Role Group(s)").append("\n");
		for (int k=0; k<role_group_vec.size()/2; k++) {
			buf2.append("\t\t").append((String) role_group_vec.elementAt(k)).append("\n");
			buf2.append("\t\t").append((String) role_group_vec.elementAt(k+1)).append("\n");
			if (k<role_group_vec.size()/2 - 1) {
				buf2.append("\t").append("or").append("\n");
			}
		}
		return buf2.toString();
	}

	public static Vector retrieveRestrictions(Vector result_vec) {
		Vector w = new Vector();
		if (result_vec == null || result_vec.size() == 0) return w;
		for (int i=0; i<result_vec.size(); i++) {
			String line = (String) result_vec.elementAt(i);
			line = line.trim();
			if (line.length() > 0) {
				Vector u = StringUtils.parseData(line, '|');
				String roleName = (String) u.elementAt(u.size()-4);
				String roleTargetCode = (String) u.elementAt(u.size()-2);
				String roleTargetName = (String) u.elementAt(u.size()-1);
				w.add(roleName + "\t" + roleTargetName + " (" + roleTargetCode + ")");
			}
		}
		return w;
	}

	public static Restriction line2Restriction(String line) {
		Vector v = StringUtils.parseData(line, '|');
		if (v.size() == 8) {
        //(1) C27781|Myxoid Liposarcoma|b0|b1|Disease_Has_Abnormal_Cell|R105|C36975|Signet Ring Lipoblast
        	return new Restriction((String) v.elementAt(0),
                        (String) v.elementAt(1),
                        (String) v.elementAt(5),
                        (String) v.elementAt(4),
                        (String) v.elementAt(6),
                        (String) v.elementAt(7));
		} else if (v.size() == 9) {
//        (1) C27781|Myxoid Liposarcoma|b0|b1|b2|Disease_May_Have_Cytogenetic_Abnormality|R114|C36317|t(12;16)(q13;p11)
        	return new Restriction((String) v.elementAt(0),
                        (String) v.elementAt(1),
                        (String) v.elementAt(6),
                        (String) v.elementAt(5),
                        (String) v.elementAt(7),
                        (String) v.elementAt(8));
		} else {
			System.out.println("ERROR: Invalid input line in line2Restriction: " + line);
		}
		return null;
	}

	//        (1) C27781|Myxoid Liposarcoma|b0|b1|b2|Disease_May_Have_Cytogenetic_Abnormality|R114|C36317|t(12;16)(q13;p11)
/*
	public HashMap loadLogialExpressionData(String filename) {
		HashMap hmap = new HashMap();
//roles of C27781:
		String key = null;
		Vector v = Utils.readFile(filename);
		v = removeBrackets(v);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (line.indexOf("roles") != -1) {
				key = "ROLE";
			} else if (line.indexOf("role unions") != -1) {
				key = "ROLE UNION";
			} if (line.indexOf("role groups") != -1) {
				key = "ROLE GROUP";
			}
			Vector u = StringUtils.parseData(line, '|');
			if (line.length() > 0 && u.size() > 1) {
				System.out.println(line);
				Vector w = new Vector();
				if (hmap.containsKey(key)) {
					w = (Vector) hmap.get(key);
				}
				w.add(line);
				hmap.put(key, w);
			}
		}

		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			key = (String) it.next();
			Vector values = (Vector) hmap.get(key);
			//System.out.println(key + ": " + values.size());
			Utils.dumpVector(key, values);
		}
		return hmap;
	}
*/
	public static HashMap parseLogicalExpressionData(HashMap hmap) {
		HashMap map = new HashMap();
		Vector parent_vec = (Vector) hmap.get("PARENT");

		Utils.dumpVector("parent_vec", parent_vec);

		List<Concept> parents = new ArrayList();
		if (parent_vec == null) {
			System.out.println("ERROR: parent_vec == null");
		} else {
			for (int i=0; i<parent_vec.size(); i++) {
				String line = (String) parent_vec.elementAt(i);
				Vector u = StringUtils.parseData(line, '|');
				Concept c = new Concept((String) u.elementAt(0), (String) u.elementAt(1));
				parents.add(c);
			}
		}
		map.put("PARENT", parents);

		Vector roles = (Vector) hmap.get("ROLE");
		Vector w = new Vector();
		for (int i=0; i<roles.size(); i++) {
			String line = (String) roles.elementAt(i);
			Restriction r = line2Restriction(line);
			//System.out.println(r.toJson());
			w.add(r);
		}
		map.put("ROLE", w);

/*
        (1) C27781|Myxoid Liposarcoma|b0|b1|Disease_Mapped_To_Gene|R176|C99200|EWSR1/DDIT3 Fusion Gene
        (2) C27781|Myxoid Liposarcoma|b0|b2|Disease_Mapped_To_Gene|R176|C99279|FUS/DDIT3 Fusion Gene
*/

		Vector roleunions = (Vector) hmap.get("ROLE UNION");
		w = new Vector();
		Vector ids = new Vector();
		for (int i=0; i<roleunions.size(); i++) {
			String line = (String) roleunions.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			if (u.size() > 1) {
				String id = (String) u.elementAt(2);
				if (!ids.contains(id)) {
					ids.add(id);
				}
			}
		}
        for (int i=0; i<ids.size(); i++) {
			String id = (String) ids.elementAt(i);
			List<Restriction> list = new ArrayList();
			for (int j=0; j<roleunions.size(); j++) {
				String line = (String) roleunions.elementAt(j);
				Vector u = StringUtils.parseData(line, '|');
				if (u.size() > 2) {
					String line_id = (String) u.elementAt(2);
					if (line_id.compareTo(id) == 0) {
						Restriction r = line2Restriction(line);
						list.add(r);
					}
				}
			}
			RoleUnion ru = new RoleUnion(list);
			w.add(ru);
		}
		map.put("ROLE UNION", w);
		Vector rolegroups = (Vector) hmap.get("ROLE GROUP");
		ids = new Vector();
		for (int i=0; i<rolegroups.size(); i++) {
			String line = (String) rolegroups.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			if (u.size() > 2) {
				String id = (String) u.elementAt(2);
				if (!ids.contains(id)) {
					ids.add(id);
				}
			}
		}

        w = new Vector();
        List<Restriction> list = new ArrayList();
        for (int i=0; i<ids.size(); i++) {
			String id = (String) ids.elementAt(i);
			List<RolePair> rp_list = new ArrayList();
			for (int j=0; j<rolegroups.size(); j++) {
				String line = (String) rolegroups.elementAt(j);
				Vector u = StringUtils.parseData(line, '|');
				if (u.size() > 2) {
					String line_id = (String) u.elementAt(2);
					if (line_id.compareTo(id) == 0) {
						Restriction r = line2Restriction(line);
						list.add(r);
					}
				}
			}
			for (int k=0; k<list.size()/2; k++) {
				Restriction r1 = (Restriction) list.get(k);
				Restriction r2 = (Restriction) list.get(k+1);
				RolePair rp = new RolePair(r1, r2);
				rp_list.add(rp);
			}
			RoleGroup rg = new RoleGroup(rp_list);
			//System.out.println(rg.toJson());
			w.add(rg);
		}
		map.put("ROLE GROUP", w);
		return map;
	}

	public Vector findRangesInLEData(HashMap hmap) {
		Vector ranges = new Vector();
		Vector roles = (Vector) hmap.get("ROLE");
		Vector roleUnions = (Vector) hmap.get("ROLE UNION");
		Vector roleGroups = (Vector) hmap.get("ROLE GROUP");
		for (int i=0; i<roles.size(); i++) {
			Restriction r = (Restriction) roles.elementAt(i);
			if (r == null) {
				System.out.println("ERROR r = null???");
			}
			String label = (String) r.getRoleLabel();
			String range = (String) roleName2RangeNameMap.get(label);
			if (!ranges.contains(range)) {
				ranges.add(range);
			}
		}
		for (int i=0; i<roleUnions.size(); i++) {
			RoleUnion ru = (RoleUnion) roleUnions.elementAt(i);
			//System.out.println(ru.toJson());

			List<Restriction> restrictions = ru.getRoles();
			for (int j=0; j<restrictions.size(); j++) {
				Restriction r = (Restriction) restrictions.get(j);
				//System.out.println(r.toJson());
				String label = (String) r.getRoleLabel();
				String range = (String) roleName2RangeNameMap.get(label);
				if (!ranges.contains(range)) {
					ranges.add(range);
				}
			}
		}
		for (int i=0; i<roleGroups.size(); i++) {
			RoleGroup rg = (RoleGroup) roleGroups.elementAt(i);
			//System.out.println(rg.toJson());
			List<RolePair> pairs = rg.getRolePairs();
			for (int k=0; k<pairs.size(); k++) {
				RolePair rp = (RolePair) pairs.get(k);
				Restriction r1 = rp.getRole1();
				Restriction r2 = rp.getRole1();
				String label = (String) r1.getRoleLabel();
				String range = (String) roleName2RangeNameMap.get(label);
				if (!ranges.contains(range)) {
					ranges.add(range);
				}
				label = (String) r2.getRoleLabel();
				range = (String) roleName2RangeNameMap.get(label);
				if (!ranges.contains(range)) {
					ranges.add(range);
				}
			}
		}
		return ranges;
	}

	public boolean valiateRangesInLEData(HashMap hmap) {
		Vector roleUnions = (Vector) hmap.get("ROLE UNION");
		System.out.println("roleUnions.size(): " + roleUnions.size());

		Vector roleGroups = (Vector) hmap.get("ROLE GROUP");
		System.out.println("roleGroups.size(): " + roleGroups.size());

		for (int i=0; i<roleUnions.size(); i++) {
			RoleUnion ru = (RoleUnion) roleUnions.elementAt(i);
			List<Restriction> restrictions = ru.getRoles();
			Restriction r = (Restriction) restrictions.get(0);
			String label = (String) r.getRoleLabel();
			String range = (String) roleName2RangeNameMap.get(label);
			boolean bool = true;
			for (int j=1; j<restrictions.size(); j++) {
				r = (Restriction) restrictions.get(j);
				label = (String) r.getRoleLabel();
				String range_j = (String) roleName2RangeNameMap.get(label);
				if (range_j.compareTo(range) != 0) {
					bool = false;
					break;
				}
			}
			if (!bool) {
				System.out.println("Invalid RoleUnion: ");
				System.out.println(ru.toJson());
			}
		}
		for (int i=0; i<roleGroups.size(); i++) {
			RoleGroup rg = (RoleGroup) roleGroups.elementAt(i);
			List<RolePair> pairs = rg.getRolePairs();
			RolePair rp = (RolePair) pairs.get(0);
			Restriction r1 = rp.getRole1();
			String label = (String) r1.getRoleLabel();
			String range = (String) roleName2RangeNameMap.get(label);
			Restriction r2 = rp.getRole2();
			label = (String) r1.getRoleLabel();
			String range_2 = (String) roleName2RangeNameMap.get(label);
			if (range_2.compareTo(range) != 0) {
				System.out.println("Invalid RoleGroup: ");
				System.out.println(rg.toJson());
				return false;
			}
		    for (int k=1; k<pairs.size(); k++) {
				rp = (RolePair) pairs.get(k);
				r1 = rp.getRole1();
				r2 = rp.getRole1();
				label = (String) r1.getRoleLabel();
				range_2 = (String) roleName2RangeNameMap.get(label);
				if (range_2.compareTo(range) != 0) {
					System.out.println("Invalid RoleGroup: ");
					System.out.println(rg.toJson());
					return false;
				}
				label = (String) r2.getRoleLabel();
				range_2 = (String) roleName2RangeNameMap.get(label);
				if (range_2.compareTo(range) != 0) {
					System.out.println("Invalid RoleGroup: ");
					System.out.println(rg.toJson());
					return false;
				}
			}
		}
		return true;
	}


	public static String toString(Object obj) {
		if (obj instanceof Restriction) {
			Concept c = (Concept) obj;
			return "\t" + c.getLabel() + "\t" + c.getCode();
		} else if (obj instanceof Restriction) {
			Restriction r = (Restriction) obj;
			return "\t" + r.getRoleLabel() + "\t" + r.getTargetLabel() + " (" + r.getTargetCode() + ")";
		} else if (obj instanceof RoleUnion) {
			RoleUnion ru = (RoleUnion) obj;
			List<Restriction> roles = ru.getRoles();
			StringBuffer buf = new StringBuffer();
			for (int k=0; k<roles.size(); k++) {
				Restriction r = (Restriction) roles.get(k);
				String s = "\t" + r.getRoleLabel() + "\t" + r.getTargetLabel() + " (" + r.getTargetCode() + ")";
				buf.append("\n");
				buf.append(s);
				if (k<roles.size()) {
					buf.append("\t").append("or").append("\n");
				}
			}
			return buf.toString();


		} else if (obj instanceof RoleGroup) {
			RoleGroup rg = (RoleGroup) obj;
			List<RolePair> pairs = rg.getRolePairs();
			StringBuffer buf = new StringBuffer();
			buf.append("\n\t").append("Role Group(s)").append("\n");
			for (int k=0; k<pairs.size(); k++) {
				RolePair rp = (RolePair) pairs.get(k);
				Restriction r1 = rp.getRole1();
				Restriction r2 = rp.getRole1();
				buf.append("\t\t" + r1.getRoleLabel()
				           + "\t" + r1.getTargetLabel()
				           + " (" + r1.getTargetCode()
				           + ")").append("\n");
				buf.append("\t\t" + r2.getRoleLabel()
				           + "\t" + r2.getTargetLabel()
				           + " (" + r2.getTargetCode()
				           + ")").append("\n");
				if (k<pairs.size()) {
					buf.append("\t\t").append("or").append("\n");
				}
			}
			return buf.toString();
		} else if (obj instanceof LogicalExpressionElement) {
			LogicalExpressionElement e = (LogicalExpressionElement) obj;
			StringBuffer buf = new StringBuffer();
			buf.append("\n").append(e.getRange()).append("\n");
			buf.append("\n\t").append(e.getRoles()).append("\n");
			buf.append("\n\t").append(e.getRoleUnions()).append("\n");
			buf.append("\n\t").append(e.getRoleGroups()).append("\n");
		} else if (obj instanceof gov.nih.nci.evs.restapi.bean.LogicalExpression) {
			gov.nih.nci.evs.restapi.bean.LogicalExpressionElement le = (gov.nih.nci.evs.restapi.bean.LogicalExpressionElement) obj;
		}
		return null;
	}

/*
// Variable declaration
	private List<Restriction> roles;
	private List<RoleUnion> roleUnions;
	private List<RoleGroup> roleGroups;
	private String range;
	*/

    public LogicalExpressionElement getLogicalExpressionElement(HashMap hmap, String range) {
		Vector roles = (Vector) hmap.get("ROLE");
		List<Restriction> simpleRoleList = new ArrayList();
		for (int i=0; i<roles.size(); i++) {
			Restriction r = (Restriction) roles.elementAt(i);
			String label = (String) r.getRoleLabel();
			String range_1 = (String) roleName2RangeNameMap.get(label);
			if (range_1.compareTo(range) == 0) {
				simpleRoleList.add(r);
			}
		}
		//e.setRoles(simpleRoleList);

		Vector roleUnions = (Vector) hmap.get("ROLE UNION");
		List<RoleUnion> roleUnion_list = new ArrayList();
		List roleList = new ArrayList();
		for (int i=0; i<roleUnions.size(); i++) {
			RoleUnion ru = (RoleUnion) roleUnions.elementAt(i);
			List<Restriction> restrictions = ru.getRoles();
			for (int j=0; j<restrictions.size(); j++) {
				Restriction r = (Restriction) restrictions.get(0);
				String label = (String) r.getRoleLabel();
				String range_2 = (String) roleName2RangeNameMap.get(label);
				if (range_2.compareTo(range) == 0) {
					roleList.add(r);
				}
			}
			if (roleList.size() > 1) {
				RoleUnion ru_i = new RoleUnion(roleList);
				roleUnion_list.add(ru_i);
			} else if (roleList.size() == 1) {
				simpleRoleList.add((Restriction) roleList.get(0));
			}
		}

		Vector roleGroups = (Vector) hmap.get("ROLE GROUP");
		List<RoleGroup> roleGroup_list = new ArrayList();
		for (int i=0; i<roleGroups.size(); i++) {
			RoleGroup rg = (RoleGroup) roleGroups.elementAt(i);
			List<RolePair> pairs = rg.getRolePairs();
			RolePair rp = (RolePair) pairs.get(0);
			Restriction r1 = rp.getRole1();
			String label = (String) r1.getRoleLabel();
			String range_3 = (String) roleName2RangeNameMap.get(label);
			if (range_3.compareTo(range) == 0) {
				roleGroup_list.add(rg);
			}
		}

		LogicalExpressionElement e = new LogicalExpressionElement(
			simpleRoleList,
			roleUnion_list,
			roleGroup_list,
			range);
		return e;
	}

	/*
public class LogicalExpressionElement {

// Variable declaration
	private List<Restriction> roles;
	private List<RoleUnion> roleUnions;
	private List<RoleGroup> roleGroups;
	private String range;
	*/

    public static void main(String[] args) {
		//String roledatafile = args[0];
		LogicalExpressionFormatter test = new LogicalExpressionFormatter();
		test.initialize();
		/*
		HashMap hmap = test.loadRoleDataMap(roledatafile);
		String expression = test.getLogicalExpression(hmap);
		System.out.println(expression);
		*/
		//HashMap hmap = test.loadLogialExpressionData(roledatafile);

		String serviceUrl = ConfigurationController.serviceUrl;
		String named_graph =  ConfigurationController.namedGraph;
		String username =  ConfigurationController.username;
		String password =  ConfigurationController.password;
		String code = args[0];
		gov.nih.nci.evs.restapi.appl.LogicalExpression le = new gov.nih.nci.evs.restapi.appl.LogicalExpression(serviceUrl, named_graph, username, password);
		HashMap hmap = le.getLogicalExpressionData(named_graph, code);

		hmap = test.parseLogicalExpressionData(hmap);
		//Utils.dumpMultiValuedHashMap(roledatafile, hmap);

		boolean bool = test.valiateRangesInLEData(hmap);
		System.out.println("valiateRangesInLEData returns: " + bool);

		Vector ranges = test.findRangesInLEData(hmap);
		Utils.dumpVector("ranges found in " + code, ranges);

		ranges = new SortUtils().quickSort(ranges);
		List<LogicalExpressionElement> elements = new ArrayList();
		for (int i=0; i<ranges.size(); i++) {
			String range = (String) ranges.elementAt(i);
			LogicalExpressionElement e = test.getLogicalExpressionElement(hmap, range);
			elements.add(e);
		}

		String label = le.getLabelByCode(named_graph, code);
		List parents = (List) hmap.get("PARENT");

		gov.nih.nci.evs.restapi.bean.LogicalExpression logicalExpression
		    = new gov.nih.nci.evs.restapi.bean.LogicalExpression(
			      code, label, parents, elements, null);
		System.out.println(logicalExpression.toJson());
	}
}

/*
roles of C27781:
        (1) C27781|Myxoid Liposarcoma|b0|b1|Disease_Has_Abnormal_Cell|R105|C36975|Signet Ring Lipoblast
        (2) C27781|Myxoid Liposarcoma|b0|b2|Disease_Has_Abnormal_Cell|R105|C48916|Primitive Mesenchymal Round to Oval Cell
        (3) C27781|Myxoid Liposarcoma|b0|b3|Disease_Has_Finding|R108|C35899|Nodular Pattern
        (4) C27781|Myxoid Liposarcoma|b0|b4|Disease_Has_Finding|R108|C35998|Myxoid Stroma Formation
        (5) C27781|Myxoid Liposarcoma|b0|b5|Disease_Has_Finding|R108|C41457|Well-Circumscribed Lesion
        (6) C27781|Myxoid Liposarcoma|b0|b6|Disease_Has_Finding|R108|C48917|Delicate Arborizing Vascular Channel Formation
        (7) C27781|Myxoid Liposarcoma|b0|b7|Disease_Mapped_To_Gene|R176|C92539|DDIT3 Gene



role unions of C27781:
        (1) C27781|Myxoid Liposarcoma|b0|b1|Disease_Mapped_To_Gene|R176|C99200|EWSR1/DDIT3 Fusion Gene
        (2) C27781|Myxoid Liposarcoma|b0|b2|Disease_Mapped_To_Gene|R176|C99279|FUS/DDIT3 Fusion Gene



role groups of C27781:
        (1) C27781|Myxoid Liposarcoma|b0|b1|b2|Disease_May_Have_Cytogenetic_Abnormality|R114|C36317|t(12;16)(q13;p11)
        (2) C27781|Myxoid Liposarcoma|b0|b1|b3|Disease_May_Have_Molecular_Abnormality|R89|C37238|FUS-DDIT3 Fusion Protein Expression
        (3) C27781|Myxoid Liposarcoma|b0|b4|b5|Disease_May_Have_Cytogenetic_Abnormality|R114|C36374|t(12;22)(q13;q12)
        (4) C27781|Myxoid Liposarcoma|b0|b4|b6|Disease_May_Have_Molecular_Abnormality|R89|C45472|EWSR1-DDIT3 Fusion Protein Expression

*/