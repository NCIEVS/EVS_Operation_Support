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

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/*
    public boolean checkRoleGroupRangeConsistency(Vector role_group_vec) {
		if (role_group_vec == null || role_group_vec.size() == 0) return false;
		String line = (String) role_group_vec.elementAt(0);
		if (line.indexOf("\t") != -1) {
			line = line.trim();
			if (line.length() > 0) {
				Vector u = StringUtils.parseData(line, '\t');
				String roleName = (String) u.elementAt(0);
				String range = (String) roleName2RangeNameMap.get(roleName);
				for (int i=1; i<role_group_vec.size(); i++) {
					line = (String) role_group_vec.elementAt(i);
					if (line.length() > 0) {
						Vector u2 = StringUtils.parseData(line, '\t');
						roleName = (String) u2.elementAt(0);
						String roleRange = (String) roleName2RangeNameMap.get(roleName);
						if (roleRange.compareTo(range) != 0) return false;
					}
				}
			}
			return true;
		} else {
			line = line.trim();
			if (line.length() > 0) {
				Vector u = StringUtils.parseData(line, '|');
				String roleName = (String) u.elementAt(u.size()-4);
				String range = (String) roleName2RangeNameMap.get(roleName);
				for (int i=1; i<role_group_vec.size(); i++) {
					line = (String) role_group_vec.elementAt(i);
					if (line.length() > 0) {
						Vector u2 = StringUtils.parseData(line, '|');
						roleName = (String) u2.elementAt(u2.size()-4);
						String roleRange = (String) roleName2RangeNameMap.get(roleName);
						if (roleRange.compareTo(range) != 0) return false;
					}
				}
			}
			return true;
		}
	}
*/

    public void dumpRoleGroupRangeData(Vector role_group_vec) {
		for (int i=0; i<role_group_vec.size(); i++) {
			String line = (String) role_group_vec.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String roleName = (String) u.elementAt(u.size()-4);
			String roleRange = (String) roleName2RangeNameMap.get(roleName);
			System.out.println(line + "(range: + " + roleRange + ")");
		}
	}

    public String generateRoleGroupExpression(Vector role_group_vec) {
		if (role_group_vec == null || role_group_vec.size() == 0) return "";
		//boolean bool = checkRoleGroupRangeConsistency(role_group_vec);
		//System.out.println("checkRoleGroupRangeConsistency returns: " + bool);
/*

		if (!bool) {
			System.out.println("WARNING: Inconsistent role range found in role group data");
			System.out.println("dumpRoleGroupRangeData ...");
			dumpRoleGroupRangeData(role_group_vec);
			return null;
		}
*/
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

/*
    public String getRoleGroupRange(Vector role_group_vec) {
		if (role_group_vec == null || role_group_vec.size() == 0) return null;
		boolean bool = checkRoleGroupRangeConsistency(role_group_vec);
		System.out.println("getRoleGroupRange returns: " + bool);

		if (bool) {
			String line = (String) role_group_vec.elementAt(0);
			Vector u = StringUtils.parseData(line, '|');
			String roleCode = (String) u.elementAt(u.size()-4);
			String roleRange = (String) roleName2RangeNameMap.get(roleCode);
			return roleRange;
		}
		return null;
	}
*/

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

    public static void main(String[] args) {
		String roledatafile = args[0];
		LogicalExpressionFormatter test = new LogicalExpressionFormatter();
		test.initialize();
		HashMap hmap = test.loadRoleDataMap(roledatafile);
		String expression = test.getLogicalExpression(hmap);
		System.out.println(expression);
	}
}

