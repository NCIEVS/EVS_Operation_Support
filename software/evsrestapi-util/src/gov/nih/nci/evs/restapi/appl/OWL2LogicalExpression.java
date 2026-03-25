package gov.nih.nci.evs.restapi.appl;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.common.*;
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
 * Copyright 2008-2017 NGIS. This software was developed in conjunction
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
 *      "This product includes software developed by NGIS and the National
 *      Cancer Institute."   If no such end-user documentation is to be
 *      included, this acknowledgment shall appear in the software itself,
 *      wherever such third-party acknowledgments normally appear.
 *   3. The names "The National Cancer Institute", "NCI" and "NGIS" must
 *      not be used to endorse or promote products derived from this software.
 *   4. This license does not authorize the incorporation of this software
 *      into any third party proprietary programs. This license does not
 *      authorize the recipient to use any trademarks owned by either NCI
 *      or NGIS
 *   5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED
 *      WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *      OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE
 *      DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
 *      NGIS, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT,
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
 *     Initial implementation kim.ong@ngc.com
 *
 */

public class OWL2LogicalExpression {

    OWLScanner owlScanner = null;

	static String NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile; //"ThesaurusInferred_forTS.owl";
	static String PARENT_CHILD_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile; // "parent_child.txt";
	static String RESTRICTION_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.rolefile; //"roles.txt";
	static String AXIOM_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.axiomfile;
	static String PROPERTY_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.propertyfile;

	static Vector ROLE_DATA = null;
	static Vector roleDomanAndRange_vec = null;
	static HashMap code2LabelMap = null;
	static HashMap roleCode2RangeNameMap = null;
	static HashMap roleCode2RoleNameMap = null;

	static String CONCEPT_FILE = "concepts_with_rolegroups.txt";
	static String RANGE_UNSPECIFIED = "[Range Unspecified]";

	static {
		long ms0 = System.currentTimeMillis();
        roleDomanAndRange_vec = OWLScanner.extractRoleDomainAndRange(NCIT_OWL);
        OWLScanner owlScanner = new OWLScanner(NCIT_OWL);
        code2LabelMap = owlScanner.getCode2LabelMap();
        roleCode2RangeNameMap = new HashMap();
        roleCode2RoleNameMap  = new HashMap();
        for (int i=0; i<roleDomanAndRange_vec.size(); i++) {
			String line = (String) roleDomanAndRange_vec.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String roleCode = (String) u.elementAt(0);
			String roleName = (String) u.elementAt(1);
			String rangeCode = (String) u.elementAt(3);
			String rangeName = (String) code2LabelMap.get(rangeCode);
			roleCode2RangeNameMap.put(roleCode, rangeName);
			roleCode2RoleNameMap.put(roleCode, roleName);
		}
		Vector v = owlScanner.scanSubproperties(owlScanner.get_owl_vec());
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String parentCode = (String) u.elementAt(0);
			String childCode = (String) u.elementAt(1);
			String rangeName = (String) roleCode2RangeNameMap.get(parentCode);
			roleCode2RangeNameMap.put(childCode, rangeName);
		}
		long ms = System.currentTimeMillis();
        System.out.println("Total initialization run time (ms): " + (ms - ms0));
	}

    public OWL2LogicalExpression() {
		owlScanner = new OWLScanner(NCIT_OWL);
    }

	public Vector getOWLClassDataByCode(String code) {
		return owlScanner.getOWLClassDataByCode(code);
	}

	public String findRoleCode(String line) {
		if (line.indexOf("<owl:onProperty ") == -1) return null;
		int n = line.lastIndexOf("#");
		String t = line.substring(n+1, line.length()-3);
		return t;
	}

	public String findRoleTargetCode(String line) {
		if (line.indexOf("<owl:someValuesFrom ") == -1) return null;
		int n = line.lastIndexOf("#");
		String t = line.substring(n+1, line.length()-3);
		return t;
	}

	public String findParentCode(String line) {
		if (line.indexOf("<rdf:Description ") == -1) return null;
		int n = line.lastIndexOf("#");
		String t = line.substring(n+1, line.length()-3);
		return t;
	}

	public Vector getCodes() {
		Vector w = new Vector();
		Vector v = Utils.readFile(CONCEPT_FILE);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(0);
			if (!w.contains(code)) {
				w.add(code);
			}
		}
		return w;
	}


	public String getLabel(String code) {
		return (String) code2LabelMap.get(code);
	}

	public Vector getLogicalExpressionData(Vector v) {
		Vector role_vec = new Vector();
		String path = "";
		boolean cont = true;
		String roleCode = null;
		String roleTargetCode = null;
		boolean role_group_started = false;
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (line.indexOf("</owl:equivalentClass>") != -1) break;
			if (line.indexOf("<owl:equivalentClass>") != -1) {
				path = path + "E|";
			} else if (line.indexOf("<owl:Class>") != -1) {
				path = path + "C|";
			} else if (line.indexOf("<owl:intersectionOf ") != -1) {
				path = path + "I|";
				//E|C|I|C|U|C|I
				if (path.endsWith("U|C|I|") && !role_group_started) {
					role_vec.add("Role Group starts");
					role_group_started = true;
				}
			} else if (line.indexOf("<owl:unionOf ") != -1) {
				path = path + "U|";
			} else if (line.indexOf("<owl:Restriction>") != -1) {
				path = path + "R|";
			} else if (line.indexOf("<owl:onProperty ") != -1) {
				roleCode = findRoleCode(line);
			} else if (line.indexOf("<owl:someValuesFrom ") != -1) {
				roleTargetCode = findRoleTargetCode(line);
				role_vec.add(path + roleCode + "$" + roleTargetCode);
				roleCode = null;
				roleTargetCode = null;
			} else if (line.indexOf("</owl:Restriction>") != -1) {
				int n = path.lastIndexOf("R|");
				path = path.substring(0, n);
			} else if (line.indexOf("</owl:Class>") != -1) {
				int n = path.lastIndexOf("C|");
				path = path.substring(0, n);
			} else if (line.indexOf("</owl:intersectionOf>") != -1) {
				if (path.indexOf("|U|C|I|") != -1) {
					role_vec.add("or");
				}
				int n = path.lastIndexOf("I");
				path = path.substring(0, n);
			} else if (line.indexOf("</owl:unionOf>") != -1) {
				int n = path.lastIndexOf("U|");
				path = path.substring(0, n);
			} else if (line.indexOf("<rdf:Description ") != -1) {
				String parentCode = findParentCode(line);
				role_vec.add("P|" + parentCode);
			}
		}
		return role_vec;
	}

/*
Raw Logical Expression Data:
        (1) P|C3720
        (2) Role Group starts
        (3) E|C|I|C|U|C|I|R|R114$C27711
        (4) E|C|I|C|U|C|I|R|R89$C36706
        (5) Role Group ends
        (6) Role Group starts
        (7) E|C|I|C|U|C|I|R|R114$C36435
        (8) E|C|I|C|U|C|I|R|R89$C36707
        (9) Role Group ends
        (10) Role Group starts
        (11) E|C|I|C|U|C|I|R|R114$C36436
        (12) E|C|I|C|U|C|I|R|R89$C36708
        (13) Role Group ends
        (14) Role Group starts
        (15) E|C|I|C|U|C|I|R|R114$C36437
        (16) E|C|I|C|U|C|I|R|R89$C36711
        (17) Role Group ends
        (18) Role Group starts
        (19) E|C|I|C|U|C|I|R|R114$C36590
        (20) E|C|I|C|U|C|I|R|R89$C37216
        (21) Role Group ends
        (22) Role Group starts
        (23) E|C|I|C|U|C|I|R|R114$C36591
        (24) E|C|I|C|U|C|I|R|R89$C38348
        (25) Role Group ends
        (26) Role Group starts
        (27) E|C|I|C|U|C|I|R|R114$C45439
        (28) E|C|I|C|U|C|I|R|R89$C45442
        (29) Role Group ends
        (30) Role Group starts
        (31) E|C|I|C|U|C|I|R|R114$C45440
        (32) E|C|I|C|U|C|I|R|R89$C45443
        (33) Role Group ends
        (34) Role Group starts
        (35) E|C|I|C|U|C|I|R|R114$C45441
        (36) E|C|I|C|U|C|I|R|R89$C45444
        (37) Role Group ends
        (38) E|C|I|R|R104$C39687
        (39) E|C|I|R|R105$C39679
        (40) E|C|I|R|R106$C37208
        (41) E|C|I|R|R106$C81946
        (42) E|C|I|R|R113$C37024
        (43) E|C|I|R|R113$C37026
        (44) E|C|I|R|R113$C39680
        (45) E|C|I|R|R115$C36156
        (46) E|C|I|R|R115$C39695
        (47) E|C|I|R|R115$C50764
        (48) E|C|I|R|R176$C101046
        (49) E|C|I|R|R176$C101059
        (50) E|C|I|R|R176$C101075
        (51) E|C|I|R|R176$C101083
        (52) E|C|I|R|R176$C101085
        (53) E|C|I|R|R176$C38184
        (54) E|C|I|R|R176$C99361
        (55) E|C|I|R|R176$C99869
        (56) E|C|I|R|R176$C99873
*/

    public Vector appendRoleRange(String codefile) {
		long ms = System.currentTimeMillis();
		//OWL2LogicalExpression test = new OWL2LogicalExpression();
		long ms0 = System.currentTimeMillis();
		Vector w0 = new Vector();
		Vector codes = null;
		codes = getCodes();
		if (codefile != null) {
			codes = Utils.readFile(codefile);
		}

		Vector display_name_vec = new Vector();

		for (int i=0; i<codes.size(); i++) {
			String code = (String) codes.elementAt(i);
			String label = getLabel(code);
			System.out.println(label + " (" + code + ")");
			String displayName = label + " (" + code + ")";
			System.out.println(displayName);
			display_name_vec.add(displayName);
			Vector w = appendRoleRange(code);
			w0.add("\n");
			w0.addAll(w);
			ms = System.currentTimeMillis();
			System.out.println("Time elapsed (ms): " + (ms - ms0));
		}
		Utils.saveToFile("range_" + CONCEPT_FILE, w0);
		ms = System.currentTimeMillis();
        System.out.println("Total run time (ms): " + (ms - ms0));
        Utils.dumpVector("Concepts processed", display_name_vec);
        return w0;
    }

/*
Modified Logical Expression Data:
	(1) P|C3720
	(2) Role Group starts
	(3) E|C|I|C|U|C|I|R|R114$C27711
	(4) E|C|I|C|U|C|I|R|R89$C36706
	(5) or
	(6) E|C|I|C|U|C|I|R|R114$C36435
	(7) E|C|I|C|U|C|I|R|R89$C36707
	(8) or
	(9) E|C|I|C|U|C|I|R|R114$C36436
	(10) E|C|I|C|U|C|I|R|R89$C36708
	(11) or
	(12) E|C|I|C|U|C|I|R|R114$C36437
	(13) E|C|I|C|U|C|I|R|R89$C36711
	(14) or
	(15) E|C|I|C|U|C|I|R|R114$C36590
	(16) E|C|I|C|U|C|I|R|R89$C37216
	(17) or
	(18) E|C|I|C|U|C|I|R|R114$C36591
	(19) E|C|I|C|U|C|I|R|R89$C38348
	(20) or
	(21) E|C|I|C|U|C|I|R|R114$C45439
	(22) E|C|I|C|U|C|I|R|R89$C45442
	(23) or
	(24) E|C|I|C|U|C|I|R|R114$C45440
	(25) E|C|I|C|U|C|I|R|R89$C45443
	(26) or
	(27) E|C|I|C|U|C|I|R|R114$C45441
	(28) E|C|I|C|U|C|I|R|R89$C45444
	(29) Role Group ends
	(30) E|C|I|R|R104$C39687
	(31) E|C|I|R|R105$C39679
	(32) E|C|I|R|R106$C37208
	(33) E|C|I|R|R106$C81946
	(34) E|C|I|R|R113$C37024
	(35) E|C|I|R|R113$C37026
	(36) E|C|I|R|R113$C39680
	(37) E|C|I|R|R115$C36156
	(38) E|C|I|R|R115$C39695
	(39) E|C|I|R|R115$C50764
	(40) E|C|I|R|R176$C101046
	(41) E|C|I|R|R176$C101059
	(42) E|C|I|R|R176$C101075
	(43) E|C|I|R|R176$C101083
	(44) E|C|I|R|R176$C101085
	(45) E|C|I|R|R176$C38184
	(46) E|C|I|R|R176$C99361
	(47) E|C|I|R|R176$C99869
	(48) E|C|I|R|R176$C99873
	*/

    public static HashMap sortLogicalExpressionData(Vector v) {
        HashMap hmap = new HashMap();
        hmap.put("Parent", new Vector());
        hmap.put("Role Group", new Vector());
        hmap.put("Role", new Vector());
        hmap.put("Role Union", new Vector());

        boolean start = false;
        //search for role groups
        Vector w = new Vector();
        StringBuffer rg_buf = new StringBuffer();

        for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (line.indexOf("Role Group starts") != -1) {
				start = true;
				rg_buf = new StringBuffer();
			} else if (line.indexOf("Role Group ends") != -1) {
				String s = rg_buf.toString();
				if (s.length() > 0) {
					if (s.endsWith("|")) {
						s = s.substring(0, s.length()-1);
					}
					w = (Vector) hmap.get("Role Group");
					w.add(s);
					hmap.put("Role Group", w);
					rg_buf = new StringBuffer();
				}
				start = false;
				rg_buf = new StringBuffer();
			}
			if (start) {
				if (line.compareTo("or") == 0) {
					//do nothing
				} else if (line.compareTo("Role Group starts") == 0) {
					//do nothing
				} else if (line.indexOf("|U|C|I|R|") != -1) {
					int n = line.lastIndexOf("R");
					String role = line.substring(n, line.length());
					rg_buf.append(role).append("|");
				}
			} else {
				if (line.startsWith("P|")) {
					Vector u = StringUtils.parseData(line, '|');
					String role = (String) u.elementAt(1);
					w = (Vector) hmap.get("Parent");
					w.add(role);
					hmap.put("Parent", w);
				} else if (!line.startsWith("P|") && line.indexOf("|U|C|I|R|") == -1) {
					if (line.indexOf("Role Group ends") == -1) {
						//|U|R| Role Union
						if (line.indexOf("|U|R|") == -1) {
							Vector u = StringUtils.parseData(line, '|');
							String role = (String) u.elementAt(u.size()-1);
							w = (Vector) hmap.get("Role");
							w.add(role);
							hmap.put("Role", w);
						}
					}
				}
			}
		}
		StringBuffer ru_buf = new StringBuffer();
        for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (line.indexOf("|U|R") != -1) {
				Vector u = StringUtils.parseData(line, '|');
				String role = (String) u.elementAt(u.size()-1);
				int n = line.lastIndexOf("R");
				role = line.substring(n, line.length());
				ru_buf.append(role).append("|");
			}
		}
		String s = ru_buf.toString();
		if (s.length() > 0) {
			if (s.endsWith("|")) {
				s = s.substring(0, s.length()-1);
			}
			w = (Vector) hmap.get("Role Union");
			w.add(s);
			hmap.put("Role Union", w);
		}
        return hmap;
	}


    public static boolean validateRoleGroups(HashMap hmap) {
		Vector role_group_vec = (Vector) hmap.get("Role Group");
		for (int i=0; i<role_group_vec.size(); i++) {
			String role_group = (String) role_group_vec.elementAt(i);
			//R114$C36317|R89$C37238
            Vector u = StringUtils.parseData(role_group, '|');
            String firstRole = (String) u.elementAt(0);
            Vector u2 = StringUtils.parseData(firstRole, '$');
            String firstRoleCode = (String) u2.elementAt(0);
            String range = (String) roleCode2RangeNameMap.get(firstRoleCode);
            for (int j=1; j<u.size(); j++) {
				String role = (String) u.elementAt(j);
				Vector u3 = StringUtils.parseData(role, '$');
				String roleCode = (String) u3.elementAt(0);
				String roleRange = (String) roleCode2RangeNameMap.get(roleCode);
				//String roleTargetCode = (String) u2.elementAt(1);
				if (roleRange.compareTo(range) != 0) {
					System.out.println("Multiple role ranges detected in " + role_group);
					System.out.println(firstRoleCode + " range:  " + range);
					System.out.println(roleCode + " range:  " + roleRange);
					return false;
				}
			}
		}
		return true;
	}

    public static HashMap formatLogicalExpression(HashMap hmap) {
		HashMap map = new HashMap();
		Vector w = new Vector();
		Vector v = (Vector) hmap.get("Parent");
		for (int i=0; i<v.size(); i++) {
			String code = (String) v.elementAt(i);
            String label = (String) code2LabelMap.get(code);
            w.add(label + " (" + code + ")");
		}
		w = new SortUtils().quickSort(w);
		map.put("Parent", w);

		w = new Vector();
		v = (Vector) hmap.get("Role");
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '$');
			String roleCode = (String) u.elementAt(0);
			String roleTargetCode = (String) u.elementAt(1);
			String roleName = (String) roleCode2RoleNameMap.get(roleCode);
			String roleTargetName = (String) code2LabelMap.get(roleTargetCode);
			String range = (String) roleCode2RangeNameMap.get(roleCode);
			w.add(roleName + "\t" + roleTargetName + " (" + roleTargetCode + ")" + "|" + range);
		}
		w = new SortUtils().quickSort(w);
		map.put("Role", w);

		w = new Vector();
		v = (Vector) hmap.get("Role Union");
		for (int i=0; i<v.size(); i++) {
			//R176$C99200|R176$C99279
			String line = (String) v.elementAt(i);
			StringBuffer buf = new StringBuffer();
			Vector u = StringUtils.parseData(line, '|');
			String range = null;
			for (int j=0; j<u.size(); j++) {
				String role = (String) u.elementAt(j);
				Vector u2 = StringUtils.parseData(role, '$');
				String roleCode = (String) u2.elementAt(0);
				String roleTargetCode = (String) u2.elementAt(1);
				String roleName = (String) roleCode2RoleNameMap.get(roleCode);
				String roleTargetName = (String) code2LabelMap.get(roleTargetCode);
				range = (String) roleCode2RangeNameMap.get(roleCode);
				buf.append(roleName + "\t" + roleTargetName + " (" + roleTargetCode + ")").append("|");
			}
			String t = buf.toString();
			t = t.substring(0, t.length()-1);
			w.add(t + "|" + range);
		}
		map.put("Role Union", w);

		w = new Vector();
		v = (Vector) hmap.get("Role Union");
		for (int i=0; i<v.size(); i++) {
			//R114$C36317|R89$C37238
			String line = (String) v.elementAt(i);
			StringBuffer buf = new StringBuffer();
			Vector u = StringUtils.parseData(line, '|');
			//R114$C36317
			String range = null;
			for (int j=0; j<u.size(); j++) {
				String role = (String) u.elementAt(j);
				Vector u2 = StringUtils.parseData(role, '$');
				String roleCode = (String) u2.elementAt(0);
				String roleTargetCode = (String) u2.elementAt(1);
				String roleName = (String) roleCode2RoleNameMap.get(roleCode);
				String roleTargetName = (String) code2LabelMap.get(roleTargetCode);
				range = (String) roleCode2RangeNameMap.get(roleCode);
				buf.append(roleName + "\t" + roleTargetName + " (" + roleTargetCode + ")").append("|");
			}
			String t = buf.toString();
			t = t.substring(0, t.length()-1);
			w.add(t + "|" + range);
		}
		map.put("Role Union", w);

		w = new Vector();

		boolean singleRange = validateRoleGroups(hmap);
		String range = null;
		if (!singleRange) {
			range = RANGE_UNSPECIFIED;
		}
		v = (Vector) hmap.get("Role Group");
		for (int i=0; i<v.size(); i++) {
			//R114$C36317|R89$C37238
			String line = (String) v.elementAt(i);
			StringBuffer buf = new StringBuffer();
			Vector u = StringUtils.parseData(line, '|');
			//R114$C36317
			for (int j=0; j<u.size(); j++) {
				String role = (String) u.elementAt(j);
				Vector u2 = StringUtils.parseData(role, '$');
				String roleCode = (String) u2.elementAt(0);
				String roleTargetCode = (String) u2.elementAt(1);
				String roleName = (String) roleCode2RoleNameMap.get(roleCode);
				String roleTargetName = (String) code2LabelMap.get(roleTargetCode);
				if (range == null) {
					range = (String) roleCode2RangeNameMap.get(roleCode);
				}
				buf.append(roleName + "\t" + roleTargetName + " (" + roleTargetCode + ")").append("|");
			}
			String t = buf.toString();
			t = t.substring(0, t.length()-1);
			w.add(t + "|" + range);
		}
		map.put("Role Group", w);
		return map;
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
				//Roles
				int knt = 0;
				for (int j=0; j<values.size(); j++) {
					String value = (String) values.elementAt(j);
					Vector u = StringUtils.parseData(value, '|');
					value = (String) u.elementAt(0);
					/*
					if (value.indexOf("Role Group") == -1) {
						knt++;
						buf.append("\t" + value).append("\n");
					}
					*/
					buf.append("\t\t" + value).append("\n");
				}
				//Role groups
				for (int j=0; j<values.size(); j++) {
					String value = (String) values.elementAt(j);
					Vector u = StringUtils.parseData(value, '|');
					value = (String) u.elementAt(0);
					if (value.indexOf("Role Group") != -1) {
						buf.append("\t" + value).append("\n");
					}
				}
			}
		}
		return buf.toString();
	}

	public HashMap generateRange2RoleUnionExpressionMap(Vector role_union_vec) {
		if (role_union_vec == null || role_union_vec.size() == 0) {
			System.out.println("INFO: role_union_vec == null || role_union_vec.size() == 0 return null");
			return null;
		}
		HashMap expressionMap = new HashMap();
		Vector w = new Vector();
		for (int i=0; i<role_union_vec.size(); i++) {
			String role_union_line = (String) role_union_vec.elementAt(i);
			String expression = generateRoleUnionExpression(role_union_line);
			Vector u = StringUtils.parseData(role_union_line, '|');
			String roleRange = (String) u.elementAt(u.size()-1);
			w = new Vector();
		    if (expressionMap.containsKey(roleRange)) {
				w = (Vector) expressionMap.get(roleRange);
			}
			int n = role_union_line.lastIndexOf("|");
			String line = role_union_line.substring(0, n);
			w.add(expression + "|RU");
			expressionMap.put(roleRange, w);
		}
		return expressionMap;
	}

	public HashMap generateRange2RoleGroupExpressionMap(Vector role_group_vec) {
		if (role_group_vec == null || role_group_vec.size() == 0) {
			System.out.println("INFO: role_group_vec == null || role_group_vec.size() == 0 return null");
			return null;
		}
		HashMap expressionMap = new HashMap();
		Vector w = new Vector();
		for (int i=0; i<role_group_vec.size(); i++) {
			String role_group_line = (String) role_group_vec.elementAt(i);
			String expression = generateRoleGroupExpression(role_group_line);
			Vector u = StringUtils.parseData(role_group_line, '|');
			String roleRange = (String) u.elementAt(u.size()-1);
			w = new Vector();
		    if (expressionMap.containsKey(roleRange)) {
				w = (Vector) expressionMap.get(roleRange);
			}
			int n = role_group_line.lastIndexOf("|");
			String line = role_group_line.substring(0, n);
			w.add(expression + "|RG");
			expressionMap.put(roleRange, w);
		}
		return expressionMap;
	}

/*

Role Union --> Disease_Mapped_To_Gene	EWSR1/DDIT3 Fusion Gene (C99200)|Disease_Mapped_To_Gene	FUS/DDIT3 Fusion Gene (C99279)|Gene


			Disease_Mapped_To_Gene	EWSR1/DDIT3 Fusion Gene (C99200)
	or
		Disease_Mapped_To_Gene	FUS/DDIT3 Fusion Gene (C99279)
	or
		Gene
*/

    public String generateRoleUnionExpression(String role_union_line) {
		StringBuffer buf2 = new StringBuffer();
		buf2.append("\n");
		Vector role_union_vec = StringUtils.parseData(role_union_line, '|');
		for (int k=0; k<role_union_vec.size()-1; k++) {
			buf2.append("\t").append((String) role_union_vec.elementAt(k)).append("\n");
			if (k<role_union_vec.size() - 2) {
				buf2.append("\t").append("or").append("\n");
			}
		}
		return buf2.toString();
	}

    public String generateRoleGroupExpression(String role_group_line) {
		StringBuffer buf2 = new StringBuffer();
		buf2.append("\n\t").append("Role Group(s)").append("\n");
		Vector role_group_vec = StringUtils.parseData(role_group_line, '|');
		for (int k=0; k<role_group_vec.size()/2; k++) {
			buf2.append("\t\t").append((String) role_group_vec.elementAt(k)).append("\n");
			buf2.append("\t\t").append((String) role_group_vec.elementAt(k+1)).append("\n");
			if (k<role_group_vec.size()/2 - 1) {
				buf2.append("\t").append("or").append("\n");
			}
		}
		return buf2.toString();
	}

    public String getLogicalExpression(HashMap hmap) {
// Parents:
        StringBuffer buf = new StringBuffer();
        Vector parents = (Vector) hmap.get("Parent");
        Vector parent_vec = new Vector();
        Vector w = new Vector();
        if (parents != null && parents.size() > 0) {
			buf.append("Parent(s)").append("\n");
			for (int i=0; i<parents.size(); i++) {
				String line = (String) parents.elementAt(i);
				parent_vec.add("\t" + line);
			}
		}
		parent_vec = new SortUtils().quickSort(parent_vec);
		for (int i=0; i<parent_vec.size(); i++) {
			String line = (String) parent_vec.elementAt(i);
			buf.append(line).append("\n");
		}
// Role:
        HashMap range2RolesHashMap = new HashMap();
        Vector v = (Vector) hmap.get("Role");
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String roleDisplayLabel = (String) u.elementAt(0);
			String range = (String) u.elementAt(1);
			w = new Vector();
			if (range2RolesHashMap.containsKey(range)) {
				w = (Vector) range2RolesHashMap.get(range);
			}
			w.add(line);
			range2RolesHashMap.put(range, w);
		}

// multiple_role_groups:
        Vector multiple_role_groups = (Vector) hmap.get("Role Group");
        HashMap range2RoleGroupExpressionMap = null;
        range2RoleGroupExpressionMap = generateRange2RoleGroupExpressionMap(multiple_role_groups);
        if (range2RoleGroupExpressionMap != null) {
            Iterator it = range2RoleGroupExpressionMap.keySet().iterator();
            while (it.hasNext()) {
				String range = (String) it.next();
				Vector roleExpression_vec = (Vector) range2RoleGroupExpressionMap.get(range);
				w = new Vector();
				if (range2RolesHashMap.containsKey(range)) {
					w = (Vector) range2RolesHashMap.get(range);
				}
				w.addAll(roleExpression_vec);
			    range2RolesHashMap.put(range, w);
			}
		}
		Vector role_unions = (Vector) hmap.get("Role Union");
        HashMap range2RoleUnionExpressionMap = null;
        range2RoleUnionExpressionMap = generateRange2RoleUnionExpressionMap(role_unions);
        if (range2RoleUnionExpressionMap != null) {
            Iterator it = range2RoleUnionExpressionMap.keySet().iterator();
            while (it.hasNext()) {
				String range = (String) it.next();
				Vector roleExpression_vec = (Vector) range2RoleUnionExpressionMap.get(range);
				w = new Vector();
				if (range2RolesHashMap.containsKey(range)) {
					w = (Vector) range2RolesHashMap.get(range);
				}
				w.addAll(roleExpression_vec);
			    range2RolesHashMap.put(range, w);
			}
		}
		String parentStr = buf.toString();
		String t = range2RolesHashMap2Expression(range2RolesHashMap);
		return parentStr + "\n" + t;
	}

    public static String run(Vector class_data_vec) {
		return run(class_data_vec, false);
	}

	public static Vector modifyLogicalExpressionData(Vector v) {
		boolean role_group_starts = false;
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (line.indexOf("Role Group starts") != -1) {
				role_group_starts = true;
			}
			if (role_group_starts) {
				if (i > 0 && i < v.size()-1) {
					String prevLine = (String) v.elementAt(i-1);
					if (line.compareTo("or") == 0) {
						String nextLine = (String) v.elementAt(i+1);
						if (prevLine.startsWith("E|C|I|C|U|C|I") && !nextLine.startsWith("E|C|I|C|U|C|I")) {
							v.set(i, "Role Group ends");
							role_group_starts = false;
						}
					}
				}
			}
		}
		return v;
	}

    public static String run(Vector class_data_vec, boolean debug) {
		String expression = null;
		OWL2LogicalExpression test = new OWL2LogicalExpression();
        Vector logicalExpressionData = test.getLogicalExpressionData(class_data_vec);
        if (debug) {
			Utils.dumpVector("Raw Logical Expression Data", logicalExpressionData);
		}
		logicalExpressionData = test.modifyLogicalExpressionData(logicalExpressionData);
        if (debug) {
			Utils.dumpVector("Modified Logical Expression Data", logicalExpressionData);
		}
        HashMap hmap = test.sortLogicalExpressionData(logicalExpressionData);
        if (debug) {
			Utils.dumpMultiValuedHashMap("Sorted Logical Expression Data", hmap);
		}
        boolean bool = test.validateRoleGroups(hmap);
        if (!bool){
        	System.out.println("WARNING: validateRoleGroups returns: " + bool);
		}
		hmap = test.formatLogicalExpression(hmap);
		if (debug) {
			Utils.dumpMultiValuedHashMap("formatted LogicalExpressionData", hmap);
		}
		expression = test.getLogicalExpression(hmap);
 		return expression;
    }

    public String run(String owlfile) {
		Vector class_data_vec = Utils.readFile(owlfile);
		return run(class_data_vec);
    }

    public void dumpRoleCode2RangeNameMap() {
		Utils.dumpHashMap("RoleCode2RangeNameMap", roleCode2RangeNameMap);
	}

    public static void main(String[] args) {
		long ms = System.currentTimeMillis();
		OWL2LogicalExpression test = new OWL2LogicalExpression();
		String owlfile = args[0];
		Vector class_data_vec = Utils.readFile(owlfile);

        boolean debug = true;
        if (debug) {
			Utils.dumpVector(owlfile, class_data_vec);
			test.dumpRoleCode2RangeNameMap();
		}
		String expression = run(class_data_vec, debug);
		int n = owlfile.indexOf(".");
		String code = owlfile.substring(0, n);
		String label = test.getLabel(code);
		System.out.println(label + " (" + code + "):");
		System.out.println(expression);

    }
}

/*
//replace or by Role Group ends
	(8) E|C|I|C|U|C|I|R|R116$C4074
	(9) or
	(10) E|C|I|R|R108$C198590


Raw Logical Expression Data:
	(1) P|C178541
	(2) P|C45219
	(3) Role Group starts
	(4) E|C|I|C|U|C|I|R|R113$C37010
	(5) E|C|I|C|U|C|I|R|R116$C3461
	(6) or
	(7) E|C|I|C|U|C|I|R|R113$C37014
	(8) E|C|I|C|U|C|I|R|R116$C4074
	(9) or
	(10) E|C|I|R|R108$C198590
	(11) E|C|I|R|R115$C50764
	(12) E|C|I|R|R140$C38172
	(13) E|C|I|R|R140$C38174
	(14) E|C|I|R|R140$C38176
	(15) E|C|I|R|R140$C40571


		Vector v = test.scanSubproperties();
		//System.out.println(expression);
		Utils.dumpVector("scanSubproperties", v);

*/
