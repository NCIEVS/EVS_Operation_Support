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

    static Vector annotationProperties = null;
    static Vector objectProperties = null;
	public static HashMap annotationPropertyCode2LabelMap = null;
	public static HashMap annotationPropertyLabel2CodeMap = null;
	public static HashMap objectPropertyCode2LabelMap = null;
	public static HashMap objectPropertyLabel2CodeMap = null;

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

		annotationProperties = owlScanner.extractAnnotationProperties(owlScanner.get_owl_vec());
		annotationPropertyCode2LabelMap = new HashMap();
		annotationPropertyLabel2CodeMap = new HashMap();
		for (int i=0; i<annotationProperties.size(); i++) {
			String line = (String) annotationProperties.elementAt(i);
			Vector u = gov.nih.nci.evs.restapi.util.StringUtils.parseData(line, '|');
			annotationPropertyCode2LabelMap.put((String) u.elementAt(0),(String) u.elementAt(1));
			annotationPropertyLabel2CodeMap.put((String) u.elementAt(1),(String) u.elementAt(0));
		}

		objectProperties = owlScanner.extractObjectProperties(owlScanner.get_owl_vec());
		objectPropertyCode2LabelMap = new HashMap();
		objectPropertyLabel2CodeMap = new HashMap();
		for (int i=0; i<objectProperties.size(); i++) {
			String line = (String) objectProperties.elementAt(i);
			Vector u = gov.nih.nci.evs.restapi.util.StringUtils.parseData(line, '|');
			objectPropertyCode2LabelMap.put((String) u.elementAt(0),(String) u.elementAt(1));
			objectPropertyLabel2CodeMap.put((String) u.elementAt(1),(String) u.elementAt(0));
		}

		long ms = System.currentTimeMillis();
        System.out.println("Total initialization run time (ms): " + (ms - ms0));
	}

	public NCItProperties() {

	}

	public static Vector get_roleDomanAndRange_vec() {
		return roleDomanAndRange_vec;
	}

	public static HashMap getCode2LabelMap() {
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

	public static HashMap getAnnotationPropertyCode2LabelMap() {
		return annotationPropertyCode2LabelMap;
	}

	public static HashMap getAnnotationPropertyLabel2CodeMap() {
		return annotationPropertyLabel2CodeMap;
	}

	public static HashMap getObjectPropertyCode2LabelMap() {
		return objectPropertyCode2LabelMap;
	}

	public static HashMap getObjectPropertyLabel2CodeMap() {
		return objectPropertyLabel2CodeMap;
	}
}