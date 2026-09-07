package gov.nih.nci.evs.restapi.util;

import java.io.*;
import java.net.URI;
import java.text.*;
import java.util.*;


public class BranchTrimmer {
	String owlfile = null;
	static String RETIRED_CONCEPT_ROOT = "C28428";
	OWLScanner owlscanner = null;
    HierarchyHelper hh = null;
    HashSet retired_concepts = null;

    public static int BY_CONCEPT_STATUS_PROPERY = 1;
    public static int BY_BRANCH = 2;

    public BranchTrimmer(String owlfile) {
		this.owlfile = owlfile;
	}

	public void initialize() {
		Vector parent_child_vec = owlscanner.extractHierarchicalRelationships(owlscanner.get_owl_vec());
		hh = new HierarchyHelper(parent_child_vec);
	}

	public HashSet get_retired_concepts() {
		int option = BY_CONCEPT_STATUS_PROPERY;
		return get_retired_concepts(option);
	}


	public HashSet get_retired_concepts(int option) {
		if (retired_concepts != null) {
			return retired_concepts;
		}
		owlscanner = new OWLScanner(owlfile);
		if (option == BY_BRANCH) {
			Vector retired_branch_codes = getRetiredBranchCodes();
			retired_concepts = Utils.vector2HashSet(retired_branch_codes);
		} else {
			retired_concepts = owlscanner.createRetiredConceptSet();
		}
		return retired_concepts;
	}

	public Vector getRetiredBranchCodes() {
		Vector v = get_transitive_closure(RETIRED_CONCEPT_ROOT);
		v.removeElement(0);
		return v;
	}

	public Vector get_transitive_closure(String root) {
		return hh.get_transitive_closure_v3(root);
	}

	public static String extractID(String line) {
		int n = line.lastIndexOf("#");
		String s = line.substring(n+1, line.length());
		n = s.lastIndexOf(" ");
		return s.substring(0, n);
	}

	public boolean isRetired(String code) {
		return retired_concepts.contains(code);
	}

/*
    <!--
    ///////////////////////////////////////////////////////////////////////////////////////
    //
    // Annotations
    //
    ///////////////////////////////////////////////////////////////////////////////////////
     -->
*/
	public Vector trim(HashSet nodes) {
		int option = BY_CONCEPT_STATUS_PROPERY;
		return trim(nodes, option);
	}

	public Vector trim(HashSet nodes, int option) {
		if (retired_concepts == null) {
			retired_concepts = get_retired_concepts(option);
		}

		Vector v = owlscanner.get_owl_vec();
		Vector w = new Vector();
		String id = null;
		Vector classData = null;

		int lcv = 1;
		int increment = 100000;
		int retired_knt = 0;

		for (int i=0; i<v.size(); i++) {
			int j = i+1;
			if (lcv == increment) {
				System.out.println("" + j + " out of " + v.size() + " completed.");
				lcv = 0;
			}
			lcv++;

			String line = (String) v.elementAt(i);
			if (line.indexOf("// Annotations") != -1) {
				w.addAll(classData);
				id = null;
			}

			if (id == null) {
				w.add(line);
			} else {
				if (line.indexOf("<!-- http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C") == -1) {
					classData.add(line);
				}
			}

			if (line.indexOf("<!-- http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C") != -1 && line.endsWith("-->")) {
				if (id != null && classData != null) {
					boolean retired = isRetired(id);
					if (!retired) {
						w.addAll(classData);
					}
				}
				id = extractID(line);
				classData = new Vector();
				classData.add(line);
			}
		}

		System.out.println("" + v.size() + " out of " + v.size() + " completed.");
		return w;
	}

	public static void main(String args[]) {
		String owlfile = args[0];
		BranchTrimmer test = new BranchTrimmer(owlfile);
		HashSet nodes = test.get_retired_concepts();
		Vector w = test.trim(nodes);
		Utils.saveToFile("trimmed_" + owlfile, w);
	}
}