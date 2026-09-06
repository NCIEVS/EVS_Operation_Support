package gov.nih.nci.evs.restapi.util;

import java.io.*;
import java.net.URI;
import java.text.*;
import java.util.*;


public class RetiredConcept {
	String owlfile = null;
    static String RETIRED_CONCEPT_ROOT = "C28428";
    OWLScanner owlscanner = null;
    HierarchyHelper hh = null;
    Vector retired_concept_codes = null;
    HashSet retired_concepts = null;
    HashSet retired_concept_set = null;
    static String CONCEPT_STATUS_CODE = "P310";

    public RetiredConcept(String owlfile) {
		this.owlfile = owlfile;
		initialize();
	}

	public void initialize() {
		long ms = System.currentTimeMillis();
		System.out.println(owlfile);
		owlscanner = new OWLScanner(owlfile);

		Vector parent_child_vec = owlscanner.extractHierarchicalRelationships(owlscanner.get_owl_vec());
		Utils.saveToFile("hier.txt", parent_child_vec);
		hh = new HierarchyHelper(parent_child_vec);

		retired_concept_set = createRetiredConceptSet();
		Utils.dumpHashSet("retired_concept_set (owlscanner)", retired_concept_set);

		retired_concept_codes = hh.get_transitive_closure_v3(RETIRED_CONCEPT_ROOT);
		retired_concepts = Utils.vector2HashSet(retired_concept_codes);
		//Utils.dumpHashSet("retired_concepts (HierarchyHelper)", retired_concepts);
		System.out.println("retired_concepts (HierarchyHelper): " + retired_concepts.size());
		System.out.println("retired_concept_set (owlscanner): " + retired_concept_set.size());
		checkRetiredCoceptStatus();

		Vector w = extractDeprecatedConcepts();
		Utils.dumpVector("Deprecated", w);
		System.out.println("retired_concepts (HierarchyHelper): " + retired_concepts.size());
		System.out.println("retired_concept_set (owlscanner): " + retired_concept_set.size());

		w = extractPropertyValues(CONCEPT_STATUS_CODE);
		Utils.dumpVector(CONCEPT_STATUS_CODE, w);

		System.out.println("Total initializaion run time (ms): " + (System.currentTimeMillis() - ms));
	}

	public void checkRetiredCoceptStatus() {
		System.out.println("retired_concepts: " + retired_concepts.size());
		Vector w = new Vector();
		Iterator it = retired_concepts.iterator();
		int knt = 0;
		while (it.hasNext()) {
			String code = (String) it.next();
			if (!retired_concept_set.contains(code)) {
				String label = hh.getLabel(code);
				w.add(code + "|" + label);
				knt++;
			}
		}
		System.out.println("retired_concept_set: " + retired_concept_set.size());
		System.out.println("diff: " + knt);
	}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	boolean isRetired(String code) {
		return retired_concepts.contains(code);
	}

	public static String extractID(String line) {
		int n = line.lastIndexOf("#");
		String s = line.substring(n+1, line.length());
		n = s.lastIndexOf(" ");
		return s.substring(0, n);
	}

	static String DEPRECATED = "<owl:deprecated rdf:datatype=\"http://www.w3.org/2001/XMLSchema#boolean\">true</owl:deprecated>";
	public Vector extractDeprecatedConcepts() {
		Vector v = owlscanner.get_owl_vec();
		Vector w = new Vector();
		String id = null;
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (line.indexOf("<!-- http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#") != -1 && line.endsWith("-->")) {
				id = extractID(line);
			}
			if (line.indexOf(DEPRECATED) != -1) {
				if (id != null) {
					w.add(id + "|" + hh.getLabel(id));
					id = null;
				}
			}
		}
		return w;
	}

	public Vector extractPropertyValues(String propCode) {
		Vector v = owlscanner.get_owl_vec();
		Vector w = new Vector();
		String id = null;
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (line.indexOf("<!-- http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#") != -1 && line.endsWith("-->")) {
				id = extractID(line);
			}
			if (line.indexOf("<" + propCode + ">") != -1 && line.indexOf("</" + propCode + ">") != -1) {
				if (id != null) {
					int n1 = line.indexOf(">");
					int n2 = line.lastIndexOf("<");
					String propValue = line.substring(n1+1, n2);
					w.add(id + "|" + hh.getLabel(id) + "|" + propValue);
				}
			}
		}
		return w;
	}

	public HashSet createRetiredConceptSet() {
		String propCode = CONCEPT_STATUS_CODE;
		Vector w = extractPropertyValues(propCode);
		HashSet hset = new HashSet();
		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(0);
			String status = (String) u.elementAt(2);
			if (status.compareTo("Retired_Concept") == 0) {
				hset.add(code);
			}
		}
		return hset;
	}

////////////////////////////////////////////////////////////////////////////////////

	public static void main(String args[]) {
		String owlfile = args[0];
		RetiredConcept test = new RetiredConcept(owlfile);
	}
}

/*

P310

    <!-- http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C28428 -->


    <owl:Class rdf:about="http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C28428">
        <owl:disjointWith rdf:resource="http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C3910"/>
        <owl:disjointWith rdf:resource="http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C43431"/>
        <owl:disjointWith rdf:resource="http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C7057"/>
        <owl:disjointWith rdf:resource="http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C97325"/>
        <NHC0>C28428</NHC0>
        <P106>Conceptual Entity</P106>
        <P108>Retired Concept</P108>
        <P366>Retired_Concepts</P366>
        <P90>All Retired Concepts</P90>
        <P90>Retired Concept</P90>
        <P90>Retired Concepts</P90>
        <P90>isRetired</P90>
        <P95>Do Not move anything here - strictly for the TDE Manager</P95>
        <P97>Concept has been retired, and should not be used except to deal with old data.</P97>
        <rdfs:label>Retired Concept</rdfs:label>
    </owl:Class>
    <owl:Axiom>
        <owl:annotatedSource rdf:resource="http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C28428"/>
        <owl:annotatedProperty rdf:resource="http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#P90"/>
        <owl:annotatedTarget>All Retired Concepts</owl:annotatedTarget>
        <P383>SY</P383>
        <P384>NCI</P384>
    </owl:Axiom>
    <owl:Axiom>
        <owl:annotatedSource rdf:resource="http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C28428"/>
        <owl:annotatedProperty rdf:resource="http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#P90"/>
        <owl:annotatedTarget>Retired Concept</owl:annotatedTarget>
        <P383>PT</P383>
        <P384>NCI</P384>
    </owl:Axiom>
    <owl:Axiom>
        <owl:annotatedSource rdf:resource="http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C28428"/>
        <owl:annotatedProperty rdf:resource="http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#P90"/>
        <owl:annotatedTarget>Retired Concepts</owl:annotatedTarget>
        <P383>SY</P383>
        <P384>NCI</P384>
    </owl:Axiom>
    <owl:Axiom>
        <owl:annotatedSource rdf:resource="http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C28428"/>
        <owl:annotatedProperty rdf:resource="http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#P90"/>
        <owl:annotatedTarget>isRetired</owl:annotatedTarget>
        <P383>SY</P383>
        <P384>NCI</P384>
    </owl:Axiom>
    <owl:Axiom>
        <owl:annotatedSource rdf:resource="http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C28428"/>
        <owl:annotatedProperty rdf:resource="http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#P97"/>
        <owl:annotatedTarget>Concept has been retired, and should not be used except to deal with old data.</owl:annotatedTarget>
        <P378>NCI</P378>
        <P379>DEFAULT_Review</P379>
        <P380>060127</P380>
    </owl:Axiom>


    <!-- http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C113462 -->


    <owl:Class rdf:about="http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C113462">
        <rdfs:subClassOf rdf:resource="http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C28428"/>
        <NHC0>C113462</NHC0>
        <P106>Conceptual Entity</P106>
        <P108>Retired Concept 2013</P108>
        <P90>Retired Concept 2013</P90>
        <P95>Bin for concepts retired in 2013.</P95>
        <P98>Bin for concepts retired in 2013.</P98>
        <rdfs:label>Retired Concept 2013</rdfs:label>
        <owl:deprecated rdf:datatype="http://www.w3.org/2001/XMLSchema#boolean">true</owl:deprecated>
    </owl:Class>
    <owl:Axiom>
        <owl:annotatedSource rdf:resource="http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C113462"/>
        <owl:annotatedProperty rdf:resource="http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#P90"/>
        <owl:annotatedTarget>Retired Concept 2013</owl:annotatedTarget>
        <P383>PT</P383>
        <P384>NCI</P384>
    </owl:Axiom>
*/