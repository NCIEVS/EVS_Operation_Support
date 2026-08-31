package gov.nih.nci.evs.restapi.util;

import java.io.*;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OWLData {
    private HashMap<String, String> conceptAndDef = new HashMap<String, String>();
    private HashMap<String, Vector<String>> conceptAndSemanticTypes = new HashMap<String, Vector<String>>();
    HashMap definitionMap = null;

	public static String DEFINITION_CODE = "P97";
	public static String PREFERRED_NAME_CODE = "P108";
    public static String SEMANTIC_TYPE_CODE = "P106";
    public static String CONCEPT_STATUS_CODE = "P310";
    public static String CONTRIBUTING_SOURCE_CODE = "P322";
    public static String TERM_SOURCE_CODE = "P384";

    private final Integer conceptCount;
    private HashMap<String, Integer> conceptCountsPerKind = new HashMap<String, Integer>();
    public HashMap<String, Vector<String>> conceptsPerKind = new HashMap<String, Vector<String>>();
    public HashMap<String, Vector<String>> parentCodeMap = new HashMap<String, Vector<String>>();

    Vector<String> conceptCodes;

    HierarchyHelper hh = null;
    HashMap roleMap = null;
    HashMap associationMap = null;
    HashMap propertyMap = null;
    HashMap preferredNameMap = null;
    HashMap conceptStatusMap = null;

    OWLScanner scanner = null;
    Vector property_vec = null;
    Vector association_vec = null;
    Vector role_vec = null;
    Vector roots = null;
    HashSet equivalentClassCodes = null;
    Vector axiom_vec = null;

    String owlfile = null;

	HashMap objectPropertyCode2NameMap = null;
	HashMap objectValuedAnnotationPropertyCode2NameMap = null;
	HashMap stringValuedAnnotationPropertyCode2NameMap = null;

	HashMap code2PropertyCountMap = null;
	HashMap code2AxiomMap = null;
	HashSet retired_concepts = null;

	public OWLData(String owlfile) {
		this.owlfile = owlfile;
		scanner = new OWLScanner(owlfile);
        System.out.println("Initialize OWLData " + owlfile);
        initialize();
        System.out.println("Done initializing OWLData " + owlfile);

        String ontologyNamespace = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl";
        loadConceptClasses();
        conceptCount = conceptCodes.size();
        System.out.println("Number of concepts: " + conceptCount.toString());
    }

    public void initialize() {
        Vector parent_child_vec = scanner.extractHierarchicalRelationships(scanner.get_owl_vec());
		hh = new HierarchyHelper(parent_child_vec);
		axiom_vec = ScannerUtils.extractAxioms(scanner.get_owl_vec());
		code2AxiomMap = createCode2AxiomMap(axiom_vec);

		roots = scanner.getRootCodes();
		property_vec = scanner.extractProperties(scanner.get_owl_vec());
		code2PropertyCountMap = createCode2PropertyCountMap(property_vec);

		propertyMap = createMultiValuedRelMap(property_vec);
		Vector w = new Vector();
		Iterator it = propertyMap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			w.add(key);
		}
		Utils.saveToFile("propertyMapKeys.txt", w);


		association_vec = scanner.extractAssociations(scanner.get_owl_vec());
		associationMap = createMultiValuedRelMap(association_vec);
		role_vec = scanner.extractOWLRestrictions(scanner.get_owl_vec());
		roleMap = createMultiValuedRelMap(role_vec);
		definitionMap = createdefinitionMap();
		preferredNameMap = createPreferredNameMap();
		equivalentClassCodes = setEquivalentClassCodes();

	    objectPropertyCode2NameMap = scanner.createObjectPropertyCode2NameMap();
	    objectValuedAnnotationPropertyCode2NameMap = scanner.createObjectValuedAnnotationPropertyCode2NameMap();
	    stringValuedAnnotationPropertyCode2NameMap = scanner.createStringValuedAnnotationPropertyCode2NameMap();

	    retired_concepts = createRetiredConceptHashSet();
	    Vector retiredConcepts = Utils.hashSet2Vector(retired_concepts);
	    Utils.saveToFile(CONCEPT_STATUS_CODE + ".txt", retiredConcepts);
	}

	public Vector get_role_vec() {
		return this.role_vec;
	}

	public boolean isRetired(String code) {
		return retired_concepts.contains(code);
	}

	public HashMap getCode2AxiomMap() {
		return code2AxiomMap;
	}

    public String getLabel(String code) {
		return hh.getLabel(code);
	}

	public HierarchyHelper getHierarchyHelper() {
		return hh;
	}

	public Vector get_axiom_vec() {
		return axiom_vec;
	}

	public OWLScanner getOWLScanner() {
		return this.scanner;
	}

	public HashMap getObjectPropertyCode2NameMap() {
		return objectPropertyCode2NameMap;
	}

	public HashMap getObjectValuedAnnotationPropertyCode2NameMap() {
		return objectValuedAnnotationPropertyCode2NameMap;
	}

	public HashMap getStringValuedAnnotationPropertyCode2NameMap() {
		return stringValuedAnnotationPropertyCode2NameMap;
	}

    public Vector getConceptCodes() {
		return this.conceptCodes;
	}

    public Vector getRoots() {
		return this.roots;
	}

    public HashMap getPropertyMap() {
		return this.propertyMap;
	}

    public HashMap getAssociationMap() {
		return this.associationMap;
	}

    public HashMap getRoleMap() {
		return this.roleMap;
	}

    public HashMap getDefinitionMap() {
		return this.definitionMap;
	}

    public HashMap getPreferredNameMap() {
		return this.preferredNameMap;
	}

	public HashMap createdefinitionMap() {
		HashMap defMap = new HashMap();
		HashMap hmap = (HashMap) propertyMap.get(DEFINITION_CODE);
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			Vector v = (Vector) hmap.get(key);
			if (v != null) {
				String value = (String) v.elementAt(0);
				defMap.put(key, value);
			}
		}
		return defMap;
	}

	public HashMap createPreferredNameMap() {
		HashMap defMap = new HashMap();
		HashMap hmap = (HashMap) propertyMap.get(PREFERRED_NAME_CODE);
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			Vector v = (Vector) hmap.get(key);
			if (v != null) {
				String value = (String) v.elementAt(0);
				defMap.put(key, value);
			}
		}
		return defMap;
	}

	public HashSet createRetiredConceptHashSet() {
		HashSet hset = new HashSet();
		HashMap hmap = (HashMap) propertyMap.get(CONCEPT_STATUS_CODE);
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			Vector v = (Vector) hmap.get(key);
			if (v != null) {
				if (v.contains("Retired_Concept")) {
					hset.add(key);
				}
			}
		}
		return hset;
	}

    private void loadConceptClasses() {
        loadConceptCodes();
        loadConceptAndDef();
        loadSemanticTypes();
    }

    public static HashMap createMultiValuedRelMap(Vector rel_vec) {
		HashMap relMap = null;
		relMap = new HashMap();
		for (int i=0; i<rel_vec.size(); i++) {
			String line = (String) rel_vec.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String src_code = (String) u.elementAt(0);
			String asso_code = (String) u.elementAt(1);
			String target_code = (String) u.elementAt(2);
			HashMap hmap = new HashMap();
			if (relMap.containsKey(asso_code)) {
				hmap = (HashMap) relMap.get(asso_code);
			}
			Vector w = new Vector();
			if (hmap.containsKey(src_code)) {
				w = (Vector) hmap.get(src_code);
			}
			if (!w.contains(target_code)) {
				w.add(target_code);
			}
			hmap.put(src_code, w);
			relMap.put(asso_code, hmap);
		}
		return relMap;
	}

    public HashMap getParentCodeMap() {
		return parentCodeMap;
	}

    public void loadConceptParents(String conceptCode) {
        try {
			Vector parentCodes = hh.getSuperclassCodes(conceptCode);
			if (parentCodes != null) {
				parentCodeMap.put(conceptCode, parentCodes);
			}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadHeaders() {
        System.out.println("Load header properties, roles and associations");
    }

    private void loadPreferredName() {
    }

    private void loadConceptCodes() {
        conceptCodes = scanner.extractClassIDs(scanner.get_owl_vec());
        Utils.saveToFile("conceptCodes.txt", conceptCodes);
        loadPreferredName();
    }

    private void loadConceptAndDef() {
		conceptAndDef = new HashMap<>(definitionMap);
    }

    private void loadSemanticTypes() {
		conceptAndSemanticTypes = (HashMap) propertyMap.get(SEMANTIC_TYPE_CODE);
    }

    public HashSet setEquivalentClassCodes() {
		HashSet equivalentClassCodes = new HashSet();
		Vector v = scanner.extractEquivalenceClasses();
		equivalentClassCodes = Utils.vector2HashSet(v);
		return equivalentClassCodes;
	}

	public Vector get_property_vec() {
		return this.property_vec;
	}

	public HashSet getEquivalentClassCodes() {
		return equivalentClassCodes;
	}

    public Vector getNonHierarchicalRelationships(String rootCode) {
		Vector descendants = hh.get_transitive_closure_v3(rootCode);
		Vector w = new Vector();
		Iterator it1 = roleMap.keySet().iterator();
		while (it1.hasNext()) {
			String propCode = (String) it1.next();
			HashMap hmap = (HashMap) roleMap.get(propCode);
			for (int i=0; i<descendants.size(); i++) {
				String descedant = (String) descendants.elementAt(i);
				Vector v1 = (Vector) hmap.get(descedant);
				if (v1 != null) {
					w.addAll(v1);
				}
			}
		}
		Iterator it2 = associationMap.keySet().iterator();
		while (it2.hasNext()) {
			String propCode = (String) it2.next();
			HashMap hmap = (HashMap) associationMap.get(propCode);
			for (int i=0; i<descendants.size(); i++) {
				String descedant = (String) descendants.elementAt(i);
				Vector v2 = (Vector) hmap.get(descedant);
				if (v2 != null) {
					w.addAll(v2);
				}
			}
		}
		return w;
	}

   public HashMap getCode2PropertyCountMap() {
	   return code2PropertyCountMap;
   }

   public HashMap createCode2PropertyCountMap(Vector property_vec) {
		HashMap map = new HashMap();
		for (int i=0; i<property_vec.size(); i++) {
			String line = (String) property_vec.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String s0 = (String) u.elementAt(0);
			String s1 = (String) u.elementAt(1);
			String s2 = (String) u.elementAt(2);
			HashMap hmap = new HashMap();
			if (map.containsKey(s0)) {
				hmap = (HashMap) map.get(s0);
			}
			int n = 0;
			if (hmap.containsKey(s1)) {
				Integer int_obj = (Integer) hmap.get(s1);
				n = int_obj.intValue();
				n++;
			}
			hmap.put(s1, Integer.valueOf(n));
			map.put(s0, hmap);
		}
		return map;
	}

	public HashMap createCode2AxiomMap(Vector v) {
		HashMap hmap = new HashMap();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(0);
			Vector w = new Vector();
			if (hmap.containsKey(code)) {
				w = (Vector) hmap.get(code);
			}
			w.add(line);
			hmap.put(code, w);
		}
		return hmap;
	}

	public static void main(String[] args) {
		String owlfile = args[0];
		OWLData owlData = new OWLData(owlfile);
	}
}

