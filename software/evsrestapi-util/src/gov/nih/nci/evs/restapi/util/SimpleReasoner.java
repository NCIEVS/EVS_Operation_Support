package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;
import java.io.*;
import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SimpleReasoner {
	HierarchyHelper hh = null;
	HashMap roleMap = null;
	String assertedOWL = null;
	Vector parent_child_vec = null;
	Vector role_vec = null;

	public SimpleReasoner(String assertedOWL) {
		this.assertedOWL = assertedOWL;
		initialize();
	}

    public void initialize() {
		long ms = System.currentTimeMillis();
		OWLScanner owlscanner = new OWLScanner(assertedOWL);
		parent_child_vec = owlscanner.extractHierarchicalRelationships(owlscanner.get_owl_vec());
		hh = new HierarchyHelper(parent_child_vec);
		role_vec = owlscanner.extractOWLRestrictions(owlscanner.get_owl_vec());
		roleMap = createRoleMap(role_vec);
		System.out.println("\tTotal initialization run time (ms): " + (System.currentTimeMillis() - ms));
	}

    public HashMap createRoleMap(Vector v) {
		HashMap hmap = new HashMap();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String srcCode = (String) u.elementAt(0);
			Vector w = new Vector();
			if (hmap.containsKey(srcCode)) {
				w = (Vector) hmap.get(srcCode);
			}
			String t = (String) u.elementAt(1) + "|" + (String) u.elementAt(2);
			w.add(t);
			hmap.put(srcCode, w);
		}
		return hmap;
	}

    public Vector get_ancestor_codes(String code) {
		Vector w = new Vector();
		Stack stack = new Stack();
		stack.push(code);
		HashSet hset = new HashSet();
		while (!stack.isEmpty()) {
			String next_code = (String) stack.pop();
			if (!hset.contains(next_code)) {
				hset.add(next_code);
				w.add(next_code);
			}
			Vector v = hh.getSuperclassCodes(next_code);
			if (v != null) {
				for (int i=0; i<v.size(); i++) {
					String child_code = (String) v.elementAt(i);
					stack.push(child_code);
				}
			}
		}
		hset.clear();
		return w;
	}

    public Vector get_ancestor_roles(String code) {
		Vector parent_codes = get_ancestor_codes(code);
		if (parent_codes == null || parent_codes.size() == 0) return new Vector();
		Vector w = new Vector();
		Stack stack = new Stack();
		stack.push(code);
		HashSet hset = new HashSet();
		while (!stack.isEmpty()) {
			String next_code = (String) stack.pop();
			if (next_code != null) {
				if (!hset.contains(next_code)) {
					hset.add(next_code);
					if (next_code.compareTo(code) != 0) {
						Vector roles = (Vector) roleMap.get(next_code);
						if (roles != null) {
							w.addAll(roles);
						}
					}
				}
				Vector v = hh.getSuperclassCodes(next_code);
				if (v != null) {
					for (int i=0; i<v.size(); i++) {
						String parent_code = (String) v.elementAt(i);
						stack.push(parent_code);
					}
				}
			}
		}
		hset.clear();
		return w;
	}

    public static Vector generateOWLRestrictionStmts(String roleCode, String targetCode) {
		Vector w = new Vector();
		w.add("        <rdfs:subClassOf>");
		w.add("            <owl:Restriction>");
		w.add("                <owl:onProperty rdf:resource=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#" + roleCode + "\"/>");
		w.add("                <owl:someValuesFrom rdf:resource=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#" + targetCode + "\"/>");
		w.add("            </owl:Restriction>");
		w.add("        </rdfs:subClassOf>");
        return w;
	}

    public Vector generateOWLRestrictionStmts(Vector v) {
		Vector w0 = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String propCode = (String) u.elementAt(0);
			String targetCode = (String) u.elementAt(1);
			w0.addAll(generateOWLRestrictionStmts(propCode, targetCode));
		}
        return w0;
	}

	public static void main(String[] args) {
		String assertedOWL = args[0];
		SimpleReasoner reasoner = new SimpleReasoner(assertedOWL);
		long ms = System.currentTimeMillis();
		String code = "C3224";
		Vector w = reasoner.get_ancestor_roles(code);
		w = reasoner.generateOWLRestrictionStmts(w);
		Utils.dumpVector("get_ancestor_roles", w);
		System.out.println("\tTotal run time (ms): " + (System.currentTimeMillis() - ms));
	}

}