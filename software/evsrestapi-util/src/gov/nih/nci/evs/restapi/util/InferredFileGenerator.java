import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.bean.*;
import gov.nih.nci.evs.restapi.config.*;
import java.io.*;
import java.util.*;

public class InferredFileGenerator {
	static String NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile;

    public static String METADATA = "metadata.owl";
    public static String CLASSDATA = "classdata.owl";
	public static String ONTOLOGY_INFO_FILE = "ontology_info.owl";
	public static String ANNOTATION_PROPERTIES_FILE = "supported_properties.txt";
	public static String ANNOTATIONS_FILE = "annotations.owl";
    static String SCRUBBED_PROPERTIES_FILE = "scrubbedProperties.txt";
    static String SCRUBBED_CLASSDATA_FILE = "scrubbed_class_data.txt";
    static String SCRUBBED_ANNOTATIONS_FILE = "scrubbed_annotations.txt";
    static String CLASSID_FILE = "classIds.txt";
    static Vector EXCLUDED_PROPERTIES = null;
    static String HASDBXREF = "oboInOwl:hasDbXref";

	static String open_tag = "<owl:Axiom>";
	static String close_tag = "</owl:Axiom>";
	static String owlannotatedSource = "owl:annotatedSource";
	static String owlannotatedProperty = "<owl:annotatedProperty";
	static String owlannotatedTarget_open = "owl:annotatedTarget";
	static String owlannotatedTarget_close = "</owl:annotatedTarget>";
	static String pt_code = "P108";
	static String pt_tag_open = "<P108>";
	static String NAMESPACE = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#";
	static String NAMESPACE_TARGET = "<!-- " + NAMESPACE;
	static String OWL_CLS_TARGET = NAMESPACE_TARGET + "C";
	static String P325 = "P325";
	static String LITERAL = "LITERAL";
	public static String SUBCLASSOF = "subClassOf";

    static {
		EXCLUDED_PROPERTIES = new Vector();
		EXCLUDED_PROPERTIES.add("P374"); // Value_Set_Location
		EXCLUDED_PROPERTIES.add("P365");
		EXCLUDED_PROPERTIES.add("P205");
		EXCLUDED_PROPERTIES.add("P320");  // (Not at FTP)
	}

	//Remove P325 P325|LITERAL along with Axiom
    static String[] TEMPORARY_FILES = new String[] {
			ONTOLOGY_INFO_FILE,
			METADATA,
			CLASSDATA,
			CLASSID_FILE,
			ANNOTATIONS_FILE,
            SCRUBBED_CLASSDATA_FILE};

	String assertedOWL = null;
	SimpleReasoner reasoner = null;
	String inferredMetadataFile = null;
	Vector metadata_vec = null;
	HashSet retiredConcepts = null;
	Vector debug_vec= new Vector();
	HierarchyHelper hh = null;
	HashSet equiv_class_set = null;

	OWLClassLoader loader = null;//new OWLClassLoader(outputfile);
	HashMap classDataHashMap = null;//loader.getClassDataHashMap();
	Vector classIdVec = null;//loader.getClassIdVec();
	Vector conceptsWithInheritedAnonymousSuperClasses = null;
	HashMap conceptsWithInheritedAnonymousSuperClassesMap = null;
	Vector owl_vec = null;

	public InferredFileGenerator(String assertedOWL) {
		this.assertedOWL = assertedOWL;
		initialize();
	}

	public HashMap vector2HashMap(Vector v, int keyCol, int valueCol) {
		HashMap hmap = new HashMap();
	    for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			hmap.put((String) u.elementAt(keyCol), (String) u.elementAt(valueCol));
		}
		return hmap;
	}

	public void initialize() {
		long ms = System.currentTimeMillis();
		System.out.println("Instantiating InferredFileGenerator ... ");
		System.out.println("OWL File: " + assertedOWL);
		File f = new File(SCRUBBED_PROPERTIES_FILE);
		if (!f.exists()) {
			Vector w = NCItMetadataUtils.generateScrubbedProperties(assertedOWL);
			Utils.saveToFile(SCRUBBED_PROPERTIES_FILE, w);
		}
        Vector propVec = Utils.readFile(SCRUBBED_PROPERTIES_FILE);
        Utils.dumpVector("Scrubbed properties", propVec);
		System.out.println("Instantiating InheritanceAnalyzer ... ");
		InheritanceAnalyzer analyzer = new InheritanceAnalyzer(assertedOWL);
		System.out.println("InheritanceAnalyzer instantiated. ");
        System.out.println("Finding concepts with inherited anonymous superClasses...");
        conceptsWithInheritedAnonymousSuperClasses = analyzer.matchAncestorRelations(false);
	    conceptsWithInheritedAnonymousSuperClassesMap = Utils.vector2HashMap(conceptsWithInheritedAnonymousSuperClasses, 0, 1);
		Utils.dumpHashMap("Concepts with inherited anonymous superClasses", conceptsWithInheritedAnonymousSuperClassesMap);
		System.out.println("Searching for parent-child (hierarchical) relationships ...");
		Vector parent_child_vec = analyzer.get_parent_child_vec();
		parent_child_vec = HTMLDecoder.run(parent_child_vec);
		hh = new HierarchyHelper(parent_child_vec);
        System.out.println("parent-child (distance-1 hierarchical) relationships generated.");
        System.out.println("Loading " + assertedOWL + " ...");
        this.owl_vec = Utils.readFile(assertedOWL);
 		System.out.println("Extracting Ontology Info...");
		extractOntologyInfo(this.owl_vec);
		System.out.println("Ontology Info instantiated.");

		System.out.println("Extracting metadata (annotaion properties, datatype properties, and object properties) ...");
	    metadata_vec = NCItMetadataUtils.getFilteredMetadata(assertedOWL, propVec);
	    Utils.saveToFile(METADATA, metadata_vec);
	    System.out.println("Metadata extracted.");

        System.out.println("Extracting annotations...");
		extractAnnotations(this.owl_vec, ANNOTATIONS_FILE);
		System.out.println("Annotations extracted.");

        System.out.println("Extracting class data...");
		extractClassData(this.owl_vec, CLASSDATA);
		System.out.println("Class data extracted.");

		System.out.println("\nStep 1: Running OWLScrubber " + SCRUBBED_PROPERTIES_FILE);
        Vector v = Utils.readFile(CLASSDATA);
		v = OWLScrubber.run(v, propVec);

		Utils.saveToFile(SCRUBBED_CLASSDATA_FILE, v);
		System.out.println(SCRUBBED_CLASSDATA_FILE + " generated.");

		System.out.println("\nStep 2: OWLClassLoader " + SCRUBBED_CLASSDATA_FILE);
		loader = new OWLClassLoader(SCRUBBED_CLASSDATA_FILE);

		classDataHashMap = loader.getClassDataHashMap();
		classIdVec = loader.getClassIdVec();

        Vector equiv_classes = extractEquivalenceClasses(this.owl_vec);
		equiv_class_set = Utils.vector2HashSet(equiv_classes);
		reasoner = analyzer.getSimpleReasoner();

		System.out.println("Total InferredFileGenerator initializaion run time (ms): " + (System.currentTimeMillis() - ms));
	}

	public boolean isDefined(String code) {
		return equiv_class_set.contains(code);
	}

    public HashMap getRelationshipHashMap(String code) {
		Vector w = extractRelationships(code);
		HashMap hmap = createRelationshipHashMap(w);
		return hmap;
	}

	public Vector extractRelationships(String code) {
        Vector class_vec = (Vector) classDataHashMap.get(code);
        return ScannerUtils.extractRelationships(class_vec);
	}

    public Vector getAncestorCodes(String code) {
		boolean traverseDown = false;
		Vector v = hh.getTransitiveClosure(code, traverseDown);
		String removed = (String) v.remove(0);
		return v;
	}

	public static HashMap createRelationshipHashMap(Vector v) {
		HashMap hmap = new HashMap();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String rel = (String) u.elementAt(1);
			Vector w = new Vector();
			if (rel.indexOf(SUBCLASSOF) != -1) {
				if (hmap.containsKey(rel)) {
					w = (Vector) hmap.get(rel);
			    }
			    w.add((String) u.elementAt(2));
			    hmap.put(rel, w);
			} else {
				u = StringUtils.parseData(line, ' ');
				String pathId = (String) u.elementAt(1);
				w = new Vector();
				if (hmap.containsKey(pathId)) {
					w = (Vector) hmap.get(pathId);
			    }
			    w.add((String) u.elementAt(0));
			    hmap.put(pathId, w);
			}
		}
	    return hmap;
	}

    public HashSet getRetiredConcepts() {
		OWLScanner owlScanner = new OWLScanner(assertedOWL);
		return owlScanner.getRetiredConcepts();
	}

	public Vector get_metadata_vec() {
		return metadata_vec;
	}

	public Vector getAnnotationProperties(String owlfile) {
		Vector w = new OWLScanner(owlfile).getSupportedProperties();
		return w;
	}

    public void extractOntologyInfo(Vector v) {
	    int istart = 0;
	    String target = "</owl:Ontology>";
	    int iend = TextFileExtractor.findLineNumber(v, target) + 3;
	    String outputfile = ONTOLOGY_INFO_FILE;
	    Vector w = TextFileExtractor.extractLines(v, istart, iend);
	    Utils.saveToFile(ONTOLOGY_INFO_FILE, w);
	}

	public static void extractAnnotations(String filename, String outputfile) {
	    String target = "// Annotations";
	    Vector v = Utils.readFile(filename);
	    extractAnnotations(v, outputfile);
	}

	public static void extractAnnotations(Vector v, String outputfile) {
	    String target = "// Annotations";
	    int istart = TextFileExtractor.findLineNumber(v, target) - 3;
	    target = "</rdf:RDF>";
	    int iend = TextFileExtractor.reverseFindLineNumber(v, target);
	    System.out.println("iend: " + iend);
	    Vector w = TextFileExtractor.extractLines(v, istart, iend);
	    Utils.saveToFile(outputfile, w);
	}

	public void extractClassData(Vector v, String outputfile) {
	    String target = "// Classes";
	    int istart = TextFileExtractor.findLineNumber(v, target) - 3;
	    target = "// Annotations";
	    int iend = TextFileExtractor.findLineNumber(v, target) - 3;
	    Vector w = TextFileExtractor.extractLines(v, istart, iend);
	    Utils.saveToFile(outputfile, w);
	}

	public static void extractMetadata(Vector v, String outputfile) {
	    String target = "// Annotation properties";
	    int istart = TextFileExtractor.findLineNumber(v, target) - 3;
	    target = "// Classes";
	    int iend = TextFileExtractor.reverseFindLineNumber(v, target) - 3;
	    Vector w = TextFileExtractor.extractLines(v, istart, iend);
	    Utils.saveToFile(outputfile, w);
	}

	public Vector extractEquivalenceClasses(Vector owl_vec) {
		long ms = System.currentTimeMillis();
		OWLScanner owlscanner = new OWLScanner(owl_vec);
		Vector w = owlscanner.extractEquivalenceClasses();
		owlscanner.clear();
		System.out.println("Total initialization run time (ms): " + (System.currentTimeMillis() - ms));
		Utils.saveToFile("equivalentClasses.txt", w);
		w = DelimitedDataExtractor.extract(w, "0", '|');
		return w;
	}

	public boolean isRetired(String code) {
		return retiredConcepts.contains(code);
	}

    public Vector composeInferredOWLClass(String code, String ancestor, Vector class_vec) {
		Vector sup_class_vec = (Vector) classDataHashMap.get(ancestor);
		Vector v = ScannerUtils.extractRelationships(sup_class_vec);
		HashMap sup_hmap = createRelationshipHashMap(v);
		return composeInferredOWLClass(code, sup_hmap, class_vec);
	}

    public Vector generateInheritedSubClassOfStmts(HashMap hmap) {
		Vector sups = (Vector) hmap.get(SUBCLASSOF);
		Vector roles = (Vector) hmap.get("CECICI");
		Vector w = new Vector();
w.add("        <rdfs:subClassOf>");
w.add("            <owl:Class>");
w.add("                <owl:intersectionOf rdf:parseType=\"Collection\">");
for (int i=0; i<sups.size(); i++) {
	String sup = (String) sups.elementAt(i);
w.add("                    <rdf:Description rdf:about=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#" + sup + "\"/>");
}
w.add("                    <owl:Class>");
w.add("                        <owl:intersectionOf rdf:parseType=\"Collection\">");
        if (roles != null) {
			for (int j=0; j<roles.size(); j++) {
				String line = (String) roles.elementAt(j);
				Vector u = StringUtils.parseData(line, '|');
				String roleCode = (String) u.elementAt(1);
				String targetCode = (String) u.elementAt(2);
	w.add("                            <owl:Restriction>");
	w.add("                                <owl:onProperty rdf:resource=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#" + roleCode + "\"/>");
	w.add("                                <owl:someValuesFrom rdf:resource=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#" + targetCode + "\"/>");
	w.add("                            </owl:Restriction>");
			}
	    }
w.add("                        </owl:intersectionOf>");
w.add("                    </owl:Class>");
w.add("                </owl:intersectionOf>");
w.add("            </owl:Class>");
w.add("        </rdfs:subClassOf>");
         return w;
	}

    public Vector composeInferredOWLClass(String code, HashMap hmap, Vector class_vec) {
		//Vector class_vec = (Vector) classDataHashMap.get(code);
		Vector w = new Vector();
	    String target = "</owl:equivalentClass>";
	    int iend = TextFileExtractor.reverseFindLineNumber(class_vec, target);

	    for (int i=0; i<=iend; i++) {
			String line = (String) class_vec.elementAt(i);
			w.add(line);
		}
		Vector stmts = generateInheritedSubClassOfStmts(hmap);
	    for (int i=0; i<stmts.size(); i++) {
			String line = (String) stmts.elementAt(i);
			w.add(line);
		}
	    for (int i=iend+1; i<class_vec.size(); i++) {
			String line = (String) class_vec.elementAt(i);
			w.add(line);
		}
		return w;
	}


	public static Vector getClassesStartStmts() {
		return getStartStmts("Classes");
	}

	public static Vector getStartStmts(String label) {
		Vector w = new Vector();
		w.add("\n    <!-- ");
		w.add("    ///////////////////////////////////////////////////////////////////////////////////////");
		w.add("    //");
		w.add("    // " + label);
		w.add("    //");
		w.add("    ///////////////////////////////////////////////////////////////////////////////////////");
		w.add("    -->\n");
		return w;
	}

	public static Vector removeRestrictionSourceCode(Vector v) {
		if (v == null) return null;
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			w.add((String) u.elementAt(1) + "|" + (String) u.elementAt(2));
		}
		return w;
	}

	public Vector appendInheritedRestrictions(String code, Vector classData) {

		if (conceptsWithInheritedAnonymousSuperClassesMap.containsKey(code)) {
			String ancestor = (String) conceptsWithInheritedAnonymousSuperClassesMap.get(code);
    		classData = composeInferredOWLClass(code, ancestor, classData);
		}

		Vector w = new Vector();
		Vector ancestor_roles = null;
        Vector w1 = OWLScanner.extractSimpleOWLRestrictions(classData);
        //Utils.dumpVector("asserted_roles_" + code, w1);
        w1 = removeRestrictionSourceCode(w1);
		Vector w2 = reasoner.get_ancestor_roles(code);
		//Utils.dumpVector("(1) asserted_ancestor_roles_" + code, w2);
		ancestor_roles = new Vector();
		if (w1 == null) {
			ancestor_roles = w2;
		} else {
			for (int k=0; k<w2.size(); k++) {
				String line = (String) w2.elementAt(k);
				if (!w1.contains(line)) {
					ancestor_roles.add(line);
				}
			}
		}
		if (ancestor_roles.size() > 0) {
			Vector v3 = reasoner.generateOWLRestrictionStmts(ancestor_roles);
           if (v3 != null && v3.size() > 0) {
				String target = "rdfs:subClassOf";
				int iend = TextFileExtractor.findLastOccurrenceLineNumber(classData, target);
				if (iend == -1) {
					target = "<rdfs:label>";
					iend = TextFileExtractor.findLineNumber(classData, target);
				}
				else {
					iend = iend - 2;
				}
				Vector v1 = TextFileExtractor.extractLines(classData, 0, iend+1);
				Vector v2 = TextFileExtractor.extractLines(classData, iend+1, classData.size());
				w.addAll(v1);
				w.addAll(v3);
				w.addAll(v2);
				w.add("\n\n");
				return w;
			} else {
				classData.add("\n\n");
				return classData;
			}
		} else {
			classData.add("\n\n");
			return classData;
		}
	}


	public static Vector updateOntologyInfo(String ontolofyInfoFile) {
		System.out.println("ontolofyInfoFile: " + ontolofyInfoFile);
		File f = new File(ontolofyInfoFile);
		if (!f.exists()) {
			System.out.println("ERROR: " + ontolofyInfoFile + " not found.");
			return new Vector();
		}
		Vector v = Utils.readFile(ontolofyInfoFile);
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (line.indexOf("<dc:date>") != -1) {
				line = "        <dc:date>" + DateUtils.getCurrDate() + "</dc:date>";
			}
			w.add(line);
		}
		return w;
	}

	public static Vector findPropertyCodes(String owlfile) {
		Vector v1 = Utils.readFile(owlfile);
		Vector w = new Vector();
		for (int i=0; i<v1.size(); i++) {
			String line = (String) v1.elementAt(i);
			line = line.trim();
			if (line.length() > 0) {
				line = line.substring(1, line.length()-1);
				line = line.replace("</", ">");
				Vector u = StringUtils.parseData(line, '>');
				if (u.size() == 3) {
					String s0 = (String) u.elementAt(0);
					String s2 = (String) u.elementAt(2);
					if (s0.compareTo(s2) == 0) {
						String tag = (String) u.elementAt(0);
						if (tag.indexOf(":") == -1) {
							if (!w.contains(tag)) {
								w.add(tag);
							}
						}
					}
				}
			}
		}
		return w;
	}

	public static Vector findScrubbedPropertyCodes(String assertedOWL, String inferredOWL) {
		Vector v1 = findPropertyCodes(assertedOWL);
		Vector v2 = findPropertyCodes(inferredOWL);
		Vector w = new Vector();
        HashSet hset = Utils.vector2HashSet(v2);
        for (int i=0; i<v1.size(); i++) {
			String t = (String) v1.elementAt(i);
			if (!hset.contains(t)) {
				w.add(t);
			}
		}
		w.add("P379");
		w.add("P380");
		w.add("P374");
		return new SortUtils().quickSort(w);
	}

	public static Vector removeTemporaryFiles() {
		Vector filenames = new Vector();
		try {
			String currentPath = new java.io.File(".").getCanonicalPath();
			for (int i=0; i<TEMPORARY_FILES.length; i++) {
				String filename = currentPath + File.separator + TEMPORARY_FILES[i];
				FileUtils.deleteFile(filename);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return filenames;
	}

	public static boolean isDeprecated(Vector v) {
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (line.indexOf("owl:deprecated") != -1 || line.indexOf("<rdfs:label>Retired Concept</rdfs:label>") != -1) return true;
		}
		return false;
	}

	public void extractMetadata(String filename, String outputfile, Vector excludedProperties) {
		extractMetadata(Utils.readFile(filename), outputfile);
	}

	//oboInOwl:hasDbXref
	//<oboInOwl:hasDbXref>IMDRF:E0128</oboInOwl:hasDbXref>
	public static Vector addPrefix2PropertyValue(Vector v, String propCode) {
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			w.add(addPrefix2PropertyValue(line, propCode));
		}
		return w;
	}


	public static String addPrefix2PropertyValue(String line, String propCode) {
		if (line.indexOf("<" + propCode + ">") == -1) return line;
		String line0 = line;
		line = line.trim();
		String value = "";
		if (line.indexOf("<" + propCode + ">") != -1) {
			int n1 = line.indexOf(">");
			int n2 = line.lastIndexOf("<");
			value = line.substring(n1+1, n2);
			char c = value.charAt(0);
			if (Character.isDigit(c)) {
				line0 = line0.replace(value, "UBERON:" + value);
			} else {
				line0 = line0.replace(value, "IMDRF:" + value);
			}
		}
		return line0;

	}

    public static Vector line2AxiomStatements(String line) {
		return AxiomParser.line2AxiomStatements(line);
	}

    public static Vector extract_axioms(Vector data_vec) {
		Vector w = ScannerUtils.extractAxioms(data_vec);
		return w;
	}

	public static Vector extractOwlClassStatements(Vector class_vec) {
	    String target = "</owl:Class>";
	    int istart = 0;//TextFileExtractor.findLineNumber(class_vec, target) - 3;
	    //target = "</rdf:RDF>";
	    int iend = TextFileExtractor.reverseFindLineNumber(class_vec, target)+1;
	    return TextFileExtractor.extractLines(class_vec, istart, iend);
	}

	public static Vector remove_axioms(Vector class_vec, String propCode, String target) {  //P325, LITERAL
        Vector w0 = extractOwlClassStatements(class_vec);
        Vector w = new Vector();
        //<P325>LITERAL</P325>
        String value = "<" + propCode + ">" + target + "</" + propCode + ">";
        for (int i=0; i<w0.size(); i++) {
			String line = (String) w0.elementAt(i);
			if (line.indexOf(value) == -1) {
				w.add(line);
			}
		}

        Vector lines = extract_axioms(class_vec);
        for (int i=0; i<lines.size(); i++) {
			String line = (String) lines.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String prop_code = (String) u.elementAt(1);
			String annotationTarget = (String) u.elementAt(2);
			if (!(prop_code.compareTo(propCode) == 0 && target.compareTo(annotationTarget) == 0)) {
				Vector w1 = AxiomParser.line2AxiomStatements(line);
				w.addAll(w1);
			}
		}
		return w;
	}

	public void setOWLClassLoader(OWLClassLoader loader) {
		this.loader = loader;
	}

	public void run(String assertedOWL) {
		long ms = System.currentTimeMillis();
		System.out.println("Start processsing ...");
		Vector deprecated = new Vector();
		Vector removed_concepts = new Vector();

		Vector w = new Vector();
		System.out.println("Updating OntologyInfo ...");
		Vector ontologyInfo = updateOntologyInfo(ONTOLOGY_INFO_FILE);
		System.out.println("OntologyInfo updated.");
		w.addAll(ontologyInfo);
		w.addAll(get_metadata_vec());
		w.addAll(getClassesStartStmts());

		Vector classIdVec = loader.getClassIdVec();
		int lcv = 1;
		int increment = 10000;
		int total = classIdVec.size();

        System.out.println("\nStep 3: Computing inheritance (generating inherited relationships) ...");
		for (int i=0; i<classIdVec.size(); i++) {
			int j = i+1;
			if (lcv == increment) {
				System.out.println("" + j + " out of " + total + " completed.");
				lcv = 0;
			}
			lcv++;
			String code = (String) classIdVec.elementAt(i);
			if (StringUtils.isNCItCode(code)) {
				Vector classData = loader.getClassData(code);
				//=========================================================================
				classData = appendInheritedRestrictions(code, classData);
				//=========================================================================
				classData = remove_axioms(classData, P325, LITERAL);
				classData = addPrefix2PropertyValue(classData, HASDBXREF);
				w.add("\n\n");
				w.addAll(classData);
			} else {
				removed_concepts.add(code);
			}
		}
		System.out.println("" + total + " out of " + total + " completed.");
		w.add("\n\n");
		w.addAll(Utils.readFile(ANNOTATIONS_FILE));
		w.add("</rdf:RDF>");
		w.add("\n");
		w.add("<!-- Generated by the OWL API (version 5.1.6) https://github.com/owlcs/owlapi/ -->");
		w.add("<!-- Modified by InferredFileGenerator (version 1.0) on " + StringUtils.getToday("MM-dd-yyyy") + " -->");

		System.out.println("" + total + " out of " + total + " completed.");
		Utils.dumpVector("Removed Concepts", removed_concepts);

        System.out.println("\nStep 4: Composing inferred NCI Thesaurus OWL ... ");
		String inferredFileName = "ThesaurusInferred_forTS_" + StringUtils.getToday() + ".owl";
		System.out.println("Generating " + inferredFileName + ". (This may take a few minutes. Please wait...)");
		Utils.saveToFile(inferredFileName, w);

		System.out.println("\nStep 5: Remove temporary files ... ");
		removeTemporaryFiles();
		System.out.println("\tTotal processing run time (ms): " + (System.currentTimeMillis() - ms));
	}

	public void test(String code) {
		Vector classData = (Vector) classDataHashMap.get(code);
		Utils.dumpVector("Asserted_" + code, classData);
		classData = appendInheritedRestrictions(code, classData);
		Utils.dumpVector("Inferred_" + code, classData);
	}

	public static void main(String[] args) {
		long ms = System.currentTimeMillis();
		String owlfile = args[0];
		InferredFileGenerator generator = new InferredFileGenerator(owlfile);
		String code = "C9128";
		generator.run(owlfile);
		System.out.println("\tTotal run run time (ms): " + (System.currentTimeMillis() - ms));
	}
}
