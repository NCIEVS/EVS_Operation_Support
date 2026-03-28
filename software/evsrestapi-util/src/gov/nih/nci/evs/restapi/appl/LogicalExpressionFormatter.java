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
	static HashMap roleDataMap = null;

	String code = null;
	String label = null;

	static HashMap range2ExpressionMap = null;
	static String RANGE_RESULT = "range_results.txt";
	static String RANGE_UNSPECIFIED = "[Range Unspecified]";

	public LogicalExpressionFormatter(HashMap roleCode2RangeNameMap, HashMap roleName2RangeNameMap) {
		this.roleName2RangeNameMap = roleName2RangeNameMap;
		this.roleCode2RangeNameMap = roleCode2RangeNameMap;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void setLabel(String label) {
		this.label = label;
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

	public static HashMap parseLogicalExpressionData(HashMap hmap) {
		HashMap map = new HashMap();
		Vector parent_vec = (Vector) hmap.get("PARENT");
		List<Concept> parents = new ArrayList();
		if (parent_vec == null) {
			System.out.println("ERROR: parent_vec == null");
		} else {
			for (int i=0; i<parent_vec.size(); i++) {
				String line = (String) parent_vec.elementAt(i);
				Vector u = StringUtils.parseData(line, '|');
				Concept c = new Concept((String) u.elementAt(1), (String) u.elementAt(0));
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
		Vector roleunions = (Vector) hmap.get("ROLE UNION");
		if (roleunions != null && roleunions.size() > 0) {
			w = new Vector();
			Vector ids = new Vector();
			for (int i=0; i<roleunions.size(); i++) {
				String line = (String) roleunions.elementAt(i);
				Vector u = StringUtils.parseData(line, '|');
				String id = (String) u.elementAt(2);
				if (!ids.contains(id)) {
					ids.add(id);
				}
			}

//ROLE UNION --> C27781|Myxoid Liposarcoma|b0|b1|Disease_Mapped_To_Gene|R176|C99200|EWSR1/DDIT3 Fusion Gene
//ROLE UNION --> C27781|Myxoid Liposarcoma|b0|b2|Disease_Mapped_To_Gene|R176|C99279|FUS/DDIT3 Fusion Gene
			for (int i=0; i<ids.size(); i++) {
				String id = (String) ids.elementAt(i);
				List<Restriction> list = new ArrayList();
				for (int j=0; j<roleunions.size(); j++) {
					String line = (String) roleunions.elementAt(j);
					Vector u = StringUtils.parseData(line, '|');
					String line_id = (String) u.elementAt(2);
					if (line_id.compareTo(id) == 0) {
						Restriction r = line2Restriction(line);
						list.add(r);
					}
				}
				RoleUnion ru = new RoleUnion(list);

				System.out.println(ru.toJson());

				w.add(ru);
			}
			map.put("ROLE UNION", w);
		}

/////////////////////////////////////////////////////////////////////////////////////////
		Vector rolegroups = (Vector) hmap.get("ROLE GROUP");
		if (rolegroups != null && rolegroups.size() > 0) {
			Vector ids = new Vector();
			for (int i=0; i<rolegroups.size(); i++) {
				String line = (String) rolegroups.elementAt(i);
				Vector u = StringUtils.parseData(line, '|');
				if (u.size() > 2) {
					String id = (String) u.elementAt(2) + "|" + (String) u.elementAt(3);
					if (!ids.contains(id)) {
						ids.add(id);
					}
				}
			}
///////////////////////////////////////////////////////////////////////////////
			w = new Vector();
			List<RoleSet> list = new ArrayList();
			for (int i=0; i<ids.size(); i++) {
				String id = (String) ids.elementAt(i);
				System.out.println(id);

				List<Restriction> restrictions = new ArrayList();
				for (int j=0; j<rolegroups.size(); j++) {
					String line = (String) rolegroups.elementAt(j);
					Vector u = StringUtils.parseData(line, '|');

					String line_id = (String) u.elementAt(2) + "|" + (String) u.elementAt(3);

					if (line_id.compareTo(id) == 0) {
						System.out.println("lineID: " + line_id);
						Restriction r = line2Restriction(line);
						System.out.println(r.toJson());
						restrictions.add(r);
					}
				}
				RoleSet set = new RoleSet(restrictions);
				System.out.println(set.toJson());

				list.add(set);
			}
			RoleGroup rg = new RoleGroup(list);
			System.out.println(rg.toJson());

			w.add(rg);
///////////////////////////////////////////////////////////////////////////////

			map.put("ROLE GROUP", w);
		}
		return map;
	}

	public Vector findRangesInLEData(HashMap hmap) {

System.out.println("********************** 	findRangesInLEData 	**********************");

		Vector ranges = new Vector();

		Vector roles = (Vector) hmap.get("ROLE");
		Vector roleUnions = (Vector) hmap.get("ROLE UNION");
		Vector roleGroups = (Vector) hmap.get("ROLE GROUP");

		for (int i=0; i<roles.size(); i++) {
			Restriction r = (Restriction) roles.elementAt(i);
			System.out.println(r.toJson());
			if (r == null) {
				System.out.println("ERROR r = null???");
			}
			String label = (String) r.getRoleLabel();
			String range = (String) roleName2RangeNameMap.get(label);
			if (!ranges.contains(range)) {
				ranges.add(range);
			}
		}
		if (roleUnions != null && roleUnions.size() > 0) {
			for (int i=0; i<roleUnions.size(); i++) {
				RoleUnion ru = (RoleUnion) roleUnions.elementAt(i);
				System.out.println(ru.toJson());
				List<Restriction> restrictions = ru.getRoles();
				for (int j=0; j<restrictions.size(); j++) {
					Restriction r = (Restriction) restrictions.get(j);
					System.out.println(r.toJson());
					String label = (String) r.getRoleLabel();
					String range = (String) roleName2RangeNameMap.get(label);
					if (!ranges.contains(range)) {
						ranges.add(range);
					}
				}
			}
		}

/*
{
  "roleSets": [
    {
      "roles": [
        {
          "sourceCode": "C27781",
          "sourceLabel": "Myxoid Liposarcoma",
          "roleCode": "R114",
          "roleLabel": "Disease_May_Have_Cytogenetic_Abnormality",
          "targetCode": "C36317",
          "targetLabel": "t(12;16)(q13;p11)"
        },
        {
          "sourceCode": "C27781",
          "sourceLabel": "Myxoid Liposarcoma",
          "roleCode": "R89",
          "roleLabel": "Disease_May_Have_Molecular_Abnormality",
          "targetCode": "C37238",
          "targetLabel": "FUS-DDIT3 Fusion Protein Expression"
        }
      ]
    },
    {
      "roles": [
        {
          "sourceCode": "C27781",
          "sourceLabel": "Myxoid Liposarcoma",
          "roleCode": "R114",
          "roleLabel": "Disease_May_Have_Cytogenetic_Abnormality",
          "targetCode": "C36374",
          "targetLabel": "t(12;22)(q13;q12)"
        },
*/

		if (roleGroups != null && roleGroups.size() > 0) {

			System.out.println("roleGroups.size(): " + roleGroups.size());

			for (int i=0; i<roleGroups.size(); i++) {
				RoleGroup rg = (RoleGroup) roleGroups.elementAt(i);
				List<RoleSet> roleSets = rg.getRoleSets();
				for (int k=0; k<roleSets.size(); k++) {
					RoleSet set = (RoleSet) roleSets.get(k);
					List<Restriction> list = set.getRoles();
					for (int m=0; m<list.size(); m++) {
						Restriction r1 = (Restriction) list.get(m);
						String label = (String) r1.getRoleLabel();
						String range = (String) roleName2RangeNameMap.get(label);
						System.out.println(range);
						if (!ranges.contains(range)) {
							ranges.add(range);
						}
					}
				}
			}
		}
		return ranges;
	}

	public boolean roleUnionwithMultipleRanges(HashMap hmap) {
		Vector roleUnions = (Vector) hmap.get("ROLE UNION");
        Vector ranges = new Vector();
		if (roleUnions != null && roleUnions.size() > 0) {
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
					if (!ranges.contains(range_j)) {
						ranges.add(range_j);
					}
				}
			}
		}
		if (ranges.size() > 1) return false;
		return true;
	}


    public Vector findRangesInRoleUnion(RoleUnion ru) {
		Vector ranges = new Vector();
		List<Restriction> restrictions = ru.getRoles();
		for (int j=0; j<restrictions.size(); j++) {
			Restriction r1 = (Restriction) restrictions.get(j);
			String label = (String) r1.getRoleLabel();
			String range = (String) roleName2RangeNameMap.get(label);
			if (!ranges.contains(range)) {
				ranges.add(range);
			}
		}
		return ranges;
	}

    public Vector findRangesInRoleGroup(RoleGroup rg) {
		Vector ranges = new Vector();
		List<RoleSet> roleSets = rg.getRoleSets();
		for (int j=0; j<roleSets.size(); j++) {
			RoleSet set = (RoleSet) roleSets.get(j);
			List restrictions = set.getRoles();
			for (int k=0; k<restrictions.size(); k++) {
				Restriction r1 = (Restriction) restrictions.get(k);
				String label = (String) r1.getRoleLabel();
				String range = (String) roleName2RangeNameMap.get(label);
				if (!ranges.contains(range)) {
					ranges.add(range);
				}
			}
		}
		return ranges;
	}



    public Vector findRangesInRoleGroups(Vector roleGroups) {
		Vector ranges = new Vector();
		if (roleGroups != null && roleGroups.size() > 0) {
			for (int i=0; i<roleGroups.size(); i++) {
				RoleGroup rg = (RoleGroup) roleGroups.elementAt(i);
				List<RoleSet> roleSets = rg.getRoleSets();
				for (int j=0; j<roleSets.size(); j++) {
					RoleSet set = (RoleSet) roleSets.get(j);
					List restrictions = set.getRoles();
					for (int k=0; k<restrictions.size(); k++) {
				        Restriction r1 = (Restriction) restrictions.get(k);
						String label = (String) r1.getRoleLabel();
						String range = (String) roleName2RangeNameMap.get(label);
						if (!ranges.contains(range)) {
							ranges.add(range);
						}
					}
				}
			}
		}
		if (ranges.size() == 0) {
			ranges.add(RANGE_UNSPECIFIED);
		}
		return ranges;
	}

	public boolean roleGroupwithMultipleRanges(HashMap hmap) {
		Vector roleGroups = (Vector) hmap.get("ROLE GROUP");
		Vector ranges = findRangesInRoleGroups(roleGroups);
		if (ranges != null && ranges.size() > 1) return false;
		return true;
	}

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

		Vector roleUnions = (Vector) hmap.get("ROLE UNION");
		List<RoleUnion> roleUnion_list = new ArrayList();
		List roleList = new ArrayList();
		if (roleUnions != null && roleUnions.size() > 0) {
			for (int i=0; i<roleUnions.size(); i++) {
				RoleUnion ru = (RoleUnion) roleUnions.elementAt(i);
				Vector ranges = findRangesInRoleUnion(ru);

				if (ranges.size() > 1 && range.compareTo(RANGE_UNSPECIFIED) == 0) {
					roleUnion_list.add(ru);
			    } else {
					String range_1 = (String) ranges.elementAt(0);
					if (range_1.compareTo(range) == 0) {
						roleUnion_list.add(ru);
					}
				}
			}
		}

		Vector roleGroups = (Vector) hmap.get("ROLE GROUP");
		List<RoleGroup> roleGroup_list = new ArrayList();
		for (int i=0; i<roleGroups.size(); i++) {
			RoleGroup rg = (RoleGroup) roleGroups.elementAt(i);
			Vector range_vec = findRangesInRoleGroup(rg);
			if (range_vec != null && range_vec.size() == 1 && range.compareTo((String) range_vec.elementAt(0)) == 0) {
				roleGroup_list.add(rg);
			} else if (range_vec == null || range_vec.size() == 0) {
				if (range.compareTo(RANGE_UNSPECIFIED) == 0) {
					roleGroup_list.add(rg);
				}
			}
		}

		if (simpleRoleList.size() == 0 && roleUnion_list.size() == 0 && roleGroup_list.size() == 0) {
			System.out.println("INFO getLogicalExpressionElement " + range + " returns null.");
			return null;
		}

		LogicalExpressionElement e = new LogicalExpressionElement(
			simpleRoleList,
			roleUnion_list,
			roleGroup_list,
			range);


		System.out.println("******************** DEBUG **************\n" + e.toJson());
		if (simpleRoleList.size() == 0 && roleUnion_list.size() == 0 && roleGroup_list.size() == 0) {
			System.out.println("INFON getLogicalExpressionElement " + range + " returns null.");
			return null;
		}
		return e;
	}

	public String getLogicalExpression(HashMap hmap) {
		/*
		Vector w = (Vector) hmap.get("ROLE");
		if (w == null) w = (Vector) hmap.get("ROLE GROUP");
		if (w == null) w = (Vector) hmap.get("ROLE UNION");
		if (w == null) {
			System.out.println("Please add code and label to the HashMap.");
			return null;
		}
		*/

		//Vector u = StringUtils.parseData((String) w.elementAt(0), '|');
		//String code = (String) u.elementAt(0);
		//String label = (String) u.elementAt(1);
		hmap = parseLogicalExpressionData(hmap);
		Vector ranges = findRangesInLEData(hmap);
		ranges.add(RANGE_UNSPECIFIED);
		ranges = new SortUtils().quickSort(ranges);
		List<LogicalExpressionElement> elements = new ArrayList();
		for (int i=0; i<ranges.size(); i++) {
			String range = (String) ranges.elementAt(i);
			LogicalExpressionElement e = getLogicalExpressionElement(hmap, range);
			if (e != null) {
				elements.add(e);
			}
		}

		//String label = le.getLabelByCode(named_graph, code);
		List parents = (List) hmap.get("PARENT");

		gov.nih.nci.evs.restapi.bean.LogicalExpression logicalExpression
			= new gov.nih.nci.evs.restapi.bean.LogicalExpression(
				  code, label, parents, elements, null); // null: expression not populated yet
		System.out.println(logicalExpression.toJson());

		String expression = null;
		StringBuffer buf = new StringBuffer();
		buf.append("\nLogical Expression of " + label + " (" + code + ")").append("\n\n");
		buf.append("\nParent(s)").append("\n");
		for (int i=0; i<parents.size(); i++) {
			Concept c = (Concept) parents.get(i);
			buf.append(toString(c)).append("\n");
		}
		elements = logicalExpression.getElements();
		for (int i=0; i<ranges.size(); i++) {
			String range = (String) ranges.elementAt(i);
			int knt = 0;
			for (int j=0; j<elements.size(); j++) {
				LogicalExpressionElement element = elements.get(j);
				String range_e = element.getRange();
				if (range_e.compareTo(range) == 0) {
					knt++;
				}
			}
			if (knt > 0) {
				for (int j=0; j<elements.size(); j++) {
					LogicalExpressionElement element = elements.get(j);
					String range_e = element.getRange();
					if (range_e.compareTo(range) == 0) {
						String s = toString(element);
						buf.append(s).append("\n");
					}
				}
			}
		}
		expression = buf.toString();
		return expression;
	}


    public String getLogicalExpression(gov.nih.nci.evs.restapi.appl.LogicalExpression le, String named_graph, String code) {
		HashMap hmap = le.getLogicalExpressionData(named_graph, code);
		hmap = parseLogicalExpressionData(hmap);
		Vector ranges = findRangesInLEData(hmap);
		ranges.add(RANGE_UNSPECIFIED);
		ranges = new SortUtils().quickSort(ranges);
		List<LogicalExpressionElement> elements = new ArrayList();
		for (int i=0; i<ranges.size(); i++) {
			String range = (String) ranges.elementAt(i);
			LogicalExpressionElement e = getLogicalExpressionElement(hmap, range);
			if (e != null) {
				elements.add(e);
			}
		}

		String label = le.getLabelByCode(named_graph, code);
		List parents = (List) hmap.get("PARENT");
		gov.nih.nci.evs.restapi.bean.LogicalExpression logicalExpression
		    = new gov.nih.nci.evs.restapi.bean.LogicalExpression(
			      code, label, parents, elements, null); // null: expression not populated yet
		String expression = null;
		StringBuffer buf = new StringBuffer();
		buf.append("\nLogical Expression of " + logicalExpression.getLabel() + " (" + logicalExpression.getCode() + ")").append("\n\n");
		buf.append("\nParent(s)").append("\n");
		for (int i=0; i<parents.size(); i++) {
			Concept c = (Concept) parents.get(i);
			buf.append(toString(c)).append("\n");
		}
        elements = logicalExpression.getElements();
        for (int i=0; i<ranges.size(); i++) {
			String range = (String) ranges.elementAt(i);
			int knt = 0;
			for (int j=0; j<elements.size(); j++) {
				LogicalExpressionElement element = elements.get(j);
				String range_e = element.getRange();
				if (range_e.compareTo(range) == 0) {
					knt++;
				}
			}
			if (knt > 0) {
				for (int j=0; j<elements.size(); j++) {
					LogicalExpressionElement element = elements.get(j);
					String range_e = element.getRange();
					if (range_e.compareTo(range) == 0) {
						String s = toString(element);
						buf.append(s).append("\n");
					}
				}
			}
		}
		expression = buf.toString();
    	return expression;
	}

	public static String toString(Object obj) {
		if (obj instanceof Concept) {
			Concept c = (Concept) obj;
			return "\t\t" + c.getLabel() + " (" + c.getCode() + ")";
		} else if (obj instanceof Restriction) {
			Restriction r = (Restriction) obj;
			return "\t" + r.getRoleLabel() + "\t" + r.getTargetLabel() + " (" + r.getTargetCode() + ")";
		} else if (obj instanceof RoleUnion) {
			RoleUnion ru = (RoleUnion) obj;
			List<Restriction> roles = ru.getRoles();
			StringBuffer buf = new StringBuffer();
			for (int k=0; k<roles.size(); k++) {
				Restriction r = (Restriction) roles.get(k);
				String s = "\t\t" + r.getRoleLabel() + "\t" + r.getTargetLabel() + " (" + r.getTargetCode() + ")";
				buf.append("\n");
				buf.append(s).append("\n");
				if (k<roles.size()-1) {
					buf.append("\t\t").append("or");
				}
			}
			return buf.toString();

		} else if (obj instanceof RoleGroup) {
			RoleGroup rg = (RoleGroup) obj;
			List<RoleSet> sets = rg.getRoleSets();
			StringBuffer buf = new StringBuffer();
			buf.append("Role Group").append("\n");
			for (int k=0; k<sets.size(); k++) {
				RoleSet set = (RoleSet) sets.get(k);
				List<Restriction> list = set.getRoles();
				for (int m=0; m<list.size(); m++) {
					Restriction r1 = (Restriction) list.get(m);
					buf.append("\t\t" + r1.getRoleLabel()
							   + "\t" + r1.getTargetLabel()
							   + " (" + r1.getTargetCode()
							   + ")").append("\n");
				}
				if (k < sets.size()-1) {
					buf.append("\t").append("or").append("\n");
				}
			}
			return buf.toString();
		} else if (obj instanceof LogicalExpressionElement) {
			LogicalExpressionElement e = (LogicalExpressionElement) obj;
			List<Restriction> roles = e.getRoles();
			List<RoleUnion> unions = e.getRoleUnions();
			List<RoleGroup> groups = e.getRoleGroups();
			StringBuffer buf = new StringBuffer();
			if (roles.size() > 0 || unions.size() > 0 || groups.size() > 0) {
				buf.append("\n").append(e.getRange()).append("\n");

				for (int i=0; i<roles.size(); i++) {
					Restriction r = roles.get(i);
					String s = toString(r);
					buf.append("\t").append(s).append("\n");
				}

				for (int i=0; i<unions.size(); i++) {
					RoleUnion ru = unions.get(i);
					String s = toString(ru);
					buf.append("\t").append(s).append("\n");
				}

				for (int i=0; i<groups.size(); i++) {
					RoleGroup rg = groups.get(i);
					String s = toString(rg);
					buf.append("\t").append(s).append("\n");
				}
			}

			String s = buf.toString();
			return s;
		} else if (obj instanceof gov.nih.nci.evs.restapi.bean.LogicalExpression) {
			gov.nih.nci.evs.restapi.bean.LogicalExpressionElement le = (gov.nih.nci.evs.restapi.bean.LogicalExpressionElement) obj;
			return le.toString();
		}
		return null;
	}

    public static String run(gov.nih.nci.evs.restapi.appl.LogicalExpression le, String named_graph, String code) {
        LogicalExpressionFormatter formatter = new LogicalExpressionFormatter(le.getRoleCode2RangeNameMap(), le.getRoleName2RangeNameMap());
        String expression = formatter.getLogicalExpression(le, named_graph, code);
        return expression;
	}

	public static HashMap loadRawLogicalExpressionData(String filename) {
		HashMap hmap = new HashMap();
		Vector v = Utils.readFile(filename);
		String key = "PARENT";
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, ' ');
			if (u.size() > 1) {
				if (line.indexOf(" --> ") != -1) {
					line = line.replace(" --> ", "$");
				} else {
					line = line.replace(" ", "$");
				}
				u = StringUtils.parseData(line, '$');
				key = (String) u.elementAt(0);
				line = (String) u.elementAt(1);
				Vector w = new Vector();
				if (hmap.containsKey(key)) {
					w = (Vector) hmap.get(key);
				}
				w.add(line);
				hmap.put(key, w);
			} else {
				u = StringUtils.parseData(line, '|');
				if (key.compareTo("PARENT") == 0 && u.size() == 8) {
					key = "ROLE";
				} else if (key.compareTo("PARENT") == 0 && u.size() == 9) {
					key = "ROLE GROUP";
				} else if (key.compareTo("ROLE") == 0 && u.size() == 9) {
					key = "ROLE GROUP";
				} else if (key.compareTo("ROLE GROUP") == 0 && u.size() != 9) {
					key = "ROLE UNION";
				}

				Vector w = new Vector();
				if (hmap.containsKey(key)) {
					w = (Vector) hmap.get(key);
				}
				w.add(line);
				hmap.put(key, w);
			}
		}
		return hmap;
	}

	public static HashMap getRoleCode2RangeNameMap() {
		HashMap hmap = new HashMap();
		Vector v = Utils.readFile(RANGE_RESULT);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			line = line.trim();
			int n = line.indexOf(")");
			if (n != -1) {
				line = line.substring(n+1, line.length());
			}
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(0);
			String rangeName = (String) u.elementAt(3);
			hmap.put(code, rangeName);
		}
		return hmap;
	}

	public static HashMap getRoleName2RangeNameMap() {
		HashMap hmap = new HashMap();
		Vector v = Utils.readFile(RANGE_RESULT);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			line = line.trim();
			int n = line.indexOf(")");
			if (n != -1) {
				line = line.substring(n+1, line.length());
			}
			Vector u = StringUtils.parseData(line, '|');
			String name = (String) u.elementAt(1);
			String rangeName = (String) u.elementAt(3);
			hmap.put(name, rangeName);
		}
		return hmap;
	}

    public String run(String rawdatafile) {
		String serviceUrl = ConfigurationController.serviceUrl;
		String named_graph =  ConfigurationController.namedGraph;
		String username =  ConfigurationController.username;
		String password =  ConfigurationController.password;

		String expression = null;
		if (rawdatafile != null) {
        	//LogicalExpressionFormatter formatter = new LogicalExpressionFormatter(getRoleCode2RangeNameMap(), getRoleName2RangeNameMap());
			HashMap hmap = loadRawLogicalExpressionData(rawdatafile);
			Utils.dumpMultiValuedHashMap(rawdatafile, hmap);
			expression = getLogicalExpression(hmap);

		} else {
		    gov.nih.nci.evs.restapi.appl.LogicalExpression le = new gov.nih.nci.evs.restapi.appl.LogicalExpression(serviceUrl, named_graph, username, password);
		    expression = run(le, named_graph, code);
		}
		return expression;
	}

    public String run() {
		return run(null);
	}

/*
    public String run(String rawdatafile) {
		String serviceUrl = ConfigurationController.serviceUrl;
		String named_graph =  ConfigurationController.namedGraph;
		String username =  ConfigurationController.username;
		String password =  ConfigurationController.password;

		String expression = null;
		if (rawdatafile != null) {
        	//LogicalExpressionFormatter formatter = new LogicalExpressionFormatter(getRoleCode2RangeNameMap(), getRoleName2RangeNameMap());
			HashMap hmap = loadRawLogicalExpressionData(rawdatafile);
			Utils.dumpMultiValuedHashMap(rawdatafile, hmap);
			expression = formatter.getLogicalExpression(hmap);
		} else {
		    gov.nih.nci.evs.restapi.appl.LogicalExpression le = new gov.nih.nci.evs.restapi.appl.LogicalExpression(serviceUrl, named_graph, username, password);
		    expression = run(le, named_graph, code);
		}
		return expression;
	}
*/

    public static void main(String[] args) {
		String code = args[0];
		String label = args[1];
		LogicalExpressionFormatter formatter = new LogicalExpressionFormatter(getRoleCode2RangeNameMap(), getRoleName2RangeNameMap());
		formatter.setCode(code);
		formatter.setLabel(label);
		String expression = formatter.run();
		System.out.println(expression);
	}
}
