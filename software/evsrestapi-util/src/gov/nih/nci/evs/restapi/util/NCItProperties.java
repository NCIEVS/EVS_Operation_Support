package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;
import java.io.*;
import java.util.*;

public class NCItProperties {
	static String NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile; //"ThesaurusInferred_forTS.owl";

	static Vector roleDomanAndRange_vec = null;
	static HashMap code2LabelMap = null;
	static HashMap roleCode2RangeNameMap = null;
	static HashMap roleCode2RoleNameMap = null;
	static HashMap roleName2RoleCodeMap = null;
	static HashMap roleName2RangeNameMap = null;

	static {
		long ms0 = System.currentTimeMillis();
        roleDomanAndRange_vec = OWLScanner.extractRoleDomainAndRange(NCIT_OWL);
        OWLScanner owlScanner = new OWLScanner(NCIT_OWL);
        code2LabelMap = owlScanner.getCode2LabelMap();
        roleCode2RangeNameMap = new HashMap();
        roleCode2RoleNameMap  = new HashMap();
        roleName2RoleCodeMap = new HashMap();
        roleName2RangeNameMap = new HashMap();
        for (int i=0; i<roleDomanAndRange_vec.size(); i++) {
			String line = (String) roleDomanAndRange_vec.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String roleCode = (String) u.elementAt(0);
			String roleName = (String) u.elementAt(1);
			String rangeCode = (String) u.elementAt(3);
			String rangeName = (String) code2LabelMap.get(rangeCode);
			roleCode2RangeNameMap.put(roleCode, rangeName);
			roleCode2RoleNameMap.put(roleCode, roleName);
			roleName2RoleCodeMap.put(roleName, roleCode);
			roleName2RangeNameMap.put(roleName, rangeName);
		}
		Vector v = owlScanner.scanSubproperties(owlScanner.get_owl_vec());
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String parentCode = (String) u.elementAt(0);
			String childCode = (String) u.elementAt(1);
			String rangeName = (String) roleCode2RangeNameMap.get(parentCode);
			roleCode2RangeNameMap.put(childCode, rangeName);
			String childName = (String) roleCode2RoleNameMap.get(childCode);
			roleName2RangeNameMap.put(childName, rangeName);
		}
		long ms = System.currentTimeMillis();
        System.out.println("Total initialization run time (ms): " + (ms - ms0));
	}

	public NCItProperties() {

	}

	static HashMap getCode2LabelMap() {
		return code2LabelMap;
	}

	public static HashMap getRoleCode2RangeNameMap() {
		return roleCode2RangeNameMap;
	}

	public static HashMap getRoleName2RangeNameMap() {
		return roleName2RangeNameMap;
	}

	public static HashMap getRoleName2RoleCodeMap() {
		return roleName2RoleCodeMap;
	}


	/*
	public static void main(String[] args) {
		String textfile = args[0];
		Vector v = Utils.readFile(textfile);
		Vector data_vec = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			line = escape(line);
			data_vec.add(line);
		}
		NCItProperties test = new NCItProperties();
		int n = textfile.lastIndexOf(".");

		String outputfile = textfile.substring(0, n) + ".html";
		String heading = (String) data_vec.elementAt(0);
		heading = heading.trim();
		data_vec.remove(0);
		test.generate(outputfile, heading, data_vec);
	}
	*/

}