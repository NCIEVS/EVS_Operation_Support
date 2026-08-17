package gov.nih.nci.evs.restapi.appl;
import gov.nih.nci.evs.restapi.bean.*;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.config.*;
import java.io.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.*;

/**
 * <!-- LICENSE_TEXT_START -->
 * Copyright 2022 Guidehouse. This software was developed in conjunction
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
 *      "This product includes software developed by Guidehouse and the National
 *      Cancer Institute."   If no such end-user documentation is to be
 *      included, this acknowledgment shall appear in the software itself,
 *      wherever such third-party acknowledgments normally appear.
 *   3. The names "The National Cancer Institute", "NCI" and "Guidehouse" must
 *      not be used to endorse or promote products derived from this software.
 *   4. This license does not authorize the incorporation of this software
 *      into any third party proprietary programs. This license does not
 *      authorize the recipient to use any trademarks owned by either NCI
 *      or GUIDEHOUSE
 *   5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED
 *      WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *      OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE
 *      DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
 *      GUIDEHOUSE, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT,
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
public class NCItQA {
	static boolean debugOn = false;
    static String NAMESPACE = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#";
    static String NAMESPACE_TARGET = "<!-- " + NAMESPACE;

    static String[] TEMPORARY_FILES = new String[] {
			"deprecated.txt",
			"annotationProperties.txt",
			"FULLSYN.txt",
			"semantic_types.txt"};

    public static String SEMANTIC_TYPE_PROP_CODE = "P106";
    public static String PREFERRED_NAME_PROP_CODE = "P108";
    public static String CONCEPT_STATUS_PROP_CODE = "P310";

	String DATA_DIRECTORY = null;
	String cwd = System.getProperty("user.dir");
	String NCIT_OWL = null;
	String PARENT_CHILD_FILE = null;
	String RESTRICTION_FILE = null;
	String AXIOM_FILE = null;
	String PROPERTY_FILE = null;
	String ROLE_FILE = null;
	String OBJECT_PROPERTY_FILE = null;
	String FULLSYN_FILE = null;
	String DEPRECATED_FILE = null;
	String ANNOTATION_PROPERTY_FILE = null;

    public Vector semantic_types = null;
    Vector objectProperties = null;
    Vector association_vec = null;

    Vector deprecated_vec = null;
    Vector property_vec = null;
    Vector role_vec = null;

    private PrintWriter pw = null;

    HashMap code2LabelMap = null;//getCode2LabelMap
    HashMap roleCode2LabelMap = null;

    HashMap concept_status_map = null;
    String owlfile = null;
    OWLScanner owlScanner = null;
    List<gov.nih.nci.evs.restapi.bean.Synonym> full_syn_list = null;
    List<gov.nih.nci.evs.restapi.bean.Synonym> active_full_syn_list = null;

    Vector annotationProperties = null;
    boolean saveOption = false;

	public NCItQA(String owlfile) {
		this.owlfile = owlfile;
		initialize();
	}

	public void initialize() {
        long ms = System.currentTimeMillis();
		DATA_DIRECTORY = "DataDir";
		File f = new File(DATA_DIRECTORY);
		if (!f.exists()) {
			f.mkdir();
			NCItUtils.generateReports(owlfile, DATA_DIRECTORY);
		}
		listFilesInDirectory(DATA_DIRECTORY);

		cwd = System.getProperty("user.dir");
		NCIT_OWL = owlfile;
		PARENT_CHILD_FILE = cwd + File.separator + DATA_DIRECTORY + File.separator + NCItUtils.HIER_FILE;
		RESTRICTION_FILE = cwd + File.separator + DATA_DIRECTORY + File.separator + NCItUtils.ROLE_FILE;
		AXIOM_FILE = cwd + File.separator + DATA_DIRECTORY + File.separator + NCItUtils.AXIOM_FILE;
		PROPERTY_FILE = cwd + File.separator + DATA_DIRECTORY + File.separator + NCItUtils.PROPERTY_FILE;
		ROLE_FILE = cwd + File.separator + DATA_DIRECTORY + File.separator + NCItUtils.ROLE_FILE;
		OBJECT_PROPERTY_FILE = cwd + File.separator + DATA_DIRECTORY + File.separator + NCItUtils.OBJECT_PROPERTY_FILE;

		FULLSYN_FILE = "FULLSYN.txt"; //generateFULLSYN_FILE();
		DEPRECATED_FILE = "deprecated.txt";
		ANNOTATION_PROPERTY_FILE = "annotationProperties.txt";

		owlScanner = new OWLScanner(owlfile);
		f = new File(FULLSYN_FILE);
		//if (FileUtils.fileExists(FULLSYN_FILE)) {
	    if (f.exists()) {
			System.out.println(FULLSYN_FILE + " exists.");
			Vector w = Utils.readFile(FULLSYN_FILE);
            full_syn_list = getSynonyms(w);
		} else {
			System.out.println(FULLSYN_FILE + " does not exists.");
		    full_syn_list = extractFULLSyns();
		}

		//semantic_types = owlScanner.extractSemanticTypes(owlScanner.get_owl_vec());
		semantic_types = extractEnum(owlScanner.get_owl_vec(), "Semantic_Type");
		Utils.saveToFile("semantic_types.txt", semantic_types);

		property_vec = null;
		if (!FileUtils.fileExists(PROPERTY_FILE)) {
			property_vec = owlScanner.extractProperties(owlScanner.get_owl_vec());
			if (saveOption) Utils.saveToFile(PROPERTY_FILE, property_vec);
		} else {
			property_vec = Utils.readFile(PROPERTY_FILE);
		}

		Vector w = null;
		if (!FileUtils.fileExists(ROLE_FILE)) {
			role_vec = owlScanner.extractOWLRestrictions(owlScanner.get_owl_vec());
			if (saveOption)  Utils.saveToFile(ROLE_FILE, role_vec);
		} else {
			role_vec = Utils.readFile(ROLE_FILE);
		}

		if (!FileUtils.fileExists(OBJECT_PROPERTY_FILE)) {
			objectProperties = owlScanner.extractObjectProperties(owlScanner.get_owl_vec());
			if (saveOption)  Utils.saveToFile(OBJECT_PROPERTY_FILE, objectProperties);
		} else {
			objectProperties = Utils.readFile(OBJECT_PROPERTY_FILE);
		}

		roleCode2LabelMap = new HashMap();
		for (int i=0; i<objectProperties.size(); i++) {
			String line = (String) objectProperties.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			roleCode2LabelMap.put((String) u.elementAt(0), (String) u.elementAt(1));
		}

		if (!FileUtils.fileExists(DEPRECATED_FILE)) {
			deprecated_vec = owlScanner.extractDeprecatedObjects(owlScanner.get_owl_vec());
			if (saveOption)  Utils.saveToFile(DEPRECATED_FILE, deprecated_vec);
		} else {
			deprecated_vec = Utils.readFile(DEPRECATED_FILE);
		}

		if (!FileUtils.fileExists(OBJECT_PROPERTY_FILE)) {
			objectProperties = owlScanner.extractObjectProperties(owlScanner.get_owl_vec());
			if (saveOption)  Utils.saveToFile(OBJECT_PROPERTY_FILE, objectProperties);
		} else {
			objectProperties = Utils.readFile(OBJECT_PROPERTY_FILE);
		}

		if (!FileUtils.fileExists(ANNOTATION_PROPERTY_FILE)) {
			annotationProperties = owlScanner.extractAnnotationProperties(owlScanner.get_owl_vec());
			if (saveOption)  Utils.saveToFile(ANNOTATION_PROPERTY_FILE, annotationProperties);
		} else {
			annotationProperties = Utils.readFile(ANNOTATION_PROPERTY_FILE);
		}

		concept_status_map = new HashMap();
		for (int i=0; i<property_vec.size(); i++) {
			String t = (String) property_vec.elementAt(i);
			Vector u = StringUtils.parseData(t, '|');
			String code = (String) u.elementAt(0);
			String prop_code = (String) u.elementAt(1);
			if (prop_code.compareTo(CONCEPT_STATUS_PROP_CODE) == 0) {
				String value = (String) u.elementAt(2);
				Vector v = new Vector();
				if (concept_status_map.containsKey(value)) {
					v = (Vector) concept_status_map.get(value);
				}
				v.add(code);
				concept_status_map.put(value, v);
			}
		}

		code2LabelMap = owlScanner.getCode2LabelMap();
		active_full_syn_list = removeDeprecated(full_syn_list);
		System.out.println("Total initialization run time (ms): " + (System.currentTimeMillis() - ms));
	}

    public static Vector listFilesInDirectory(String dirName) {
		Vector v = new Vector();
		if (dirName == null) {
			dirName = System.getProperty("user.dir");;
		}
        File f = new File(dirName);
        String[] pathnames = f.list();
        for (String pathname : pathnames) {
            v.add(pathname);
        }
        return v;
	}

/*
Recombinant Amphiregulin|C1000|P90|CRDGF|P383$AB|P384$NCI
Recombinant Amphiregulin|C1000|P90|KAF|P383$AB|P384$NCI
*/
	public String generateFULLSYN_FILE() {
		Vector v = Utils.readFile(AXIOM_FILE);
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String propCode = (String) u.elementAt(2);
			if (propCode.compareTo("P90") == 0) {
				w.add(line);
			}
		}
		String fullsynfile = "FULLSYN.txt";
		Utils.saveToFile(fullsynfile, w);
		return fullsynfile;
	}

	public void setSaveOption(boolean saveOption) {
		this.saveOption = saveOption;
	}

/*
Recombinant Amphiregulin|C1000|P90|AMPHIREGULIN|P383$PT|P384$FDA|P385$7MGE0HPM2H|P386$UNII
Recombinant Amphiregulin|C1000|P90|AR|P383$AB|P384$NCI
Recombinant Amphiregulin|C1000|P90|CRDGF|P383$AB|P384$NCI
Recombinant Amphiregulin|C1000|P90|KAF|P383$AB|P384$NCI
Recombinant Amphiregulin|C1000|P90|Recombinant Amphiregulin|P383$PT|P384$NCI
*/

    public List getSynonyms(Vector axiom_data) {
		if (axiom_data == null) return null;
		List syn_list = new ArrayList();
		HashMap hmap = new HashMap();
		for (int i=0; i<axiom_data.size(); i++) {
			String t = (String) axiom_data.elementAt(i);
			gov.nih.nci.evs.restapi.bean.Synonym syn = AxiomParser.line2Synonym(t);// {
			syn_list.add(syn);
		}
		return syn_list;
	}

    public String getOutputFileName() {
		String version = owlScanner.extractVersion();
		System.out.println("version: " + version);
		String t = this.owlfile;
		int n = t.lastIndexOf(".");
		String outputfile = t.substring(0, n) + "_" + version + "_" + StringUtils.getToday() + "_QA.txt";
		//String outputfile =.owlfile + "_" + version + "_" + StringUtils.getToday() + "_QA.txt";
		return outputfile;
	}

	public void runQA() {
        long ms = System.currentTimeMillis();
		PrintWriter pw = null;
		String outputfile = getOutputFileName();
		try {
			pw = new PrintWriter(outputfile, "UTF-8");
//1.	Preferred name not unique to single concept
			QA_PreferredNames(pw);
//2.	Duplicate roles within a concept (i.e., RELAs)
            QA_duplicate_roles(pw);
//3.	Duplicate properties within a concept (includes definitions, STYs, etc.)
            QA_duplicated_properties(pw);
//4.	Invalid STYs
            QA_SemanticTypes(pw);
//5.	Verify exactly one NCI PT per concept
            QA_NCIPT(pw);
//6.	New and deprecated roles and associations (i.e., RELAs)
			QA_obsolete_roles(pw);
			QA_obsolete_associations(pw);
//7.	New and deprecated properties (i.e., ATNs)
            QA_obsolete_properties(pw);
//8.	New and deprecated subsources
            QA_obsolete_subsources(pw);
//9.	Pipe characters within the data
            QA_pipe_characters(pw);
//10.	@ characters within the definition or synonym field
            QA_special_characters(pw, '@');
//11.	Check for duplicate atoms -- same name, code, term type, and SAB appearing in DIFFERENT concepts
            QA_DuplicatedAtomsInDifferentConcepts(pw);
//12.	Property names that appear in the data but are not being processed (e.g., legacy concept name, umls_cui, etc)
            QA_non_processed_properties(pw);
//13.	Check for duplicate atoms -- same name, term type, and sab but with different codes appearing in the SAME concept
            QA_DuplicatedAtoms(pw);
//14.	Self-referential relationships
            QA_self_referential_relationships(pw);
//15.	Conflicting RELAs between same concept pairs
            QA_conflicting_RELAs(pw);
//16.	Blank property values
            QA_blankPropertyValues(pw);

		} catch (Exception ex) {

		} finally {
			try {
				pw.close();
				System.out.println("Output file " + outputfile + " generated.");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}

	public List removeDeprecated(List full_syn_list) {
		List list = new ArrayList();
		for (int i=0; i<full_syn_list.size(); i++) {
		    gov.nih.nci.evs.restapi.bean.Synonym syn = (gov.nih.nci.evs.restapi.bean.Synonym) full_syn_list.get(i);
		    if (!isDeprecated(syn.getCode())) {
				list.add(syn);
			}
		}
		return list;
	}


	public boolean isDeprecated(String code) {
		return deprecated_vec.contains(code);
	}

	public OWLScanner getOWLScanner() {
		return this.owlScanner;
	}

    public Vector getSemanticTypes() {
		return this.semantic_types;
	}

    public Vector getObjectProperties() {
		return this.objectProperties;
	}

    public Vector getassociation_vec() {
		return association_vec;
	}

    public String getConceptStatus(String code) {
		Vector v = (Vector) concept_status_map.get(code);
		if (v == null || v.size() == 0) return null;
		return (String) v.elementAt(0);
	}

    public HashMap createSynonymKeys2CodeHashMap(Vector keys, Vector axiom_data) {
		HashMap hmap = new HashMap();
		List list = new AxiomUtils().getSynonyms(axiom_data);
		for (int i=0; i<list.size(); i++) {
			gov.nih.nci.evs.restapi.bean.Synonym syn = (gov.nih.nci.evs.restapi.bean.Synonym) list.get(i);
			StringBuffer buf = new StringBuffer();
			if (keys.contains("termName")) {
				if (syn.getTermName() != null) {
					buf.append(syn.getTermName());
				}
				buf.append("|");
			}
			if (keys.contains("termGroup")) {
				if (syn.getTermGroup() != null) {
					buf.append(syn.getTermGroup());
				}
				buf.append("|");
			}
			if (keys.contains("termSource")) {
				if (syn.getTermSource() != null) {
					buf.append(syn.getTermSource());
				}
				buf.append("|");
			}
			if (keys.contains("subSourceName")) {
				if (syn.getSubSourceName() != null) {
					buf.append(syn.getSubSourceName());
				}
				buf.append("|");
			}
			if (keys.contains("code")) {
				if (syn.getCode() != null) {
					buf.append(syn.getCode());
				}
				buf.append("|");
			}
			Vector w = new Vector();
			String key = buf.toString();
			if (hmap.containsKey(key)) {
				w = (Vector) hmap.get(key);
			}
			w.add(syn.getCode());
			hmap.put(key, w);
		}
		return hmap;
	}

    public Vector filterAxiomData(Vector axiom_data, String prop_code) {
		Vector w = new Vector();
		for (int i=0; i<axiom_data.size(); i++) {
			String t = (String) axiom_data.elementAt(i);
			Vector u = StringUtils.parseData(t, '|');
		    String propertyCode = (String) u.elementAt(1); // P90
		    if (propertyCode.compareTo(prop_code) == 0) {
				w.add(t);
			}
		}
		return w;
	}

    public List extractFULLSyns() {
		Vector v = Utils.readFile(AXIOM_FILE);
		System.out.println("v: " + v.size());
		Vector w = filterAxiomData(v, "P90");
		System.out.println("w: " + w.size());
		//if (saveOption) Utils.saveToFile("owlAxioms.txt", w);
		//w = filterAxiomData(w, "P90");
        if (saveOption) Utils.saveToFile("FULLSYN.txt", w);
		//List list = new AxiomUtils().getSynonyms(w);
		List list = getSynonyms(w);
		return list;
	}

    public void QA_PreferredNames(PrintWriter pw) {
		pw.println("\n1.	Preferred name not unique to single concept.");
		int error_count = 0;
		HashMap pt_map = new HashMap();
		for (int i=0; i<property_vec.size(); i++) {
			String t = (String) property_vec.elementAt(i);
			Vector u = StringUtils.parseData(t, '|');
			String code = (String) u.elementAt(0);
			String pt_code = (String) u.elementAt(1);

			if (debugOn && i < 10) {
				System.out.println("QA_PreferredNames " + t);
			}

			if (pt_code.compareTo(PREFERRED_NAME_PROP_CODE) == 0) {
				String name = (String) u.elementAt(2);
				Vector v = new Vector();
				if (pt_map.containsKey(code)) {
					v = (Vector) pt_map.get(code);
				}
				if (v.size() > 0) {
					pw.println("WARNING: multiple Preferred names detected for " + code);
					error_count++;
				}
				v.add(name);
				pt_map.put(code, v);
			}
		}
        String methodName = "QA_PreferredNames";
	    dump_QA_error_counts(pw, methodName, error_count);
	}

	public void QA_NCIPT(PrintWriter pw) {
		pw.println("\n5.	Verify exactly one NCI PT per concept.");
		int num_errors = 0;
		//System.out.println("active_full_syn_list: " + active_full_syn_list.size());
		HashMap hmap = new HashMap();
		for (int i=0; i<active_full_syn_list.size(); i++) {
			gov.nih.nci.evs.restapi.bean.Synonym syn = (gov.nih.nci.evs.restapi.bean.Synonym) active_full_syn_list.get(i);

			if (debugOn && i < 10) {
				System.out.println(" QA_NCIPT " + syn.toJson());
			}

			String key = null;
			if (syn.getTermSource() != null && syn.getTermSource() .compareTo("NCI") == 0 &&
			    syn.getTermGroup() != null && syn.getTermGroup() .compareTo("PT") == 0
			) {
				key = syn.getTermSource() + "|" + syn.getTermGroup() + "|" + syn.getCode();
				Vector v = new Vector();
				if (hmap.containsKey(key)) {
					System.out.println("WARNING: Duplicated NCI PT identified.");
					v = (Vector) hmap.get(key);
					num_errors++;

					for (int k=0; k<v.size(); k++) {
						gov.nih.nci.evs.restapi.bean.Synonym sy = (gov.nih.nci.evs.restapi.bean.Synonym) v.get(k);
						pw.println(sy.toJson());
					}
					pw.println(syn.toJson());
				}
				v.add(syn);
				hmap.put(key, v);
			}
		}
        String methodName = "QA_NCIPT";
	    dump_QA_error_counts(pw, methodName, num_errors);
	}

/*
	private String code;
	private String label;
	private String termName;
	private String termGroup;
	private String termSource;
	private String sourceCode;
	private String subSourceName; (*)
	private String subSourceCode;

{
  "code": "C153245",
  "label": "Hematopoietic Immune Cell",
  "termName": "Hematopoietic Immune Cell",
  "termGroup": "DN",
  "termSource": "CTRP"
}
*/

//13.	Check for duplicate atoms -- same name, term type, and sab but with different codes appearing in the SAME concept
	public void QA_DuplicatedAtoms(PrintWriter pw) {
		pw.println("\n13.	Check for duplicate atoms -- same name, term type, and sab but with different codes appearing in the SAME concept.");
		int num_errors = 0;
		HashMap hmap = new HashMap();
		for (int i=0; i<active_full_syn_list.size(); i++) {
			gov.nih.nci.evs.restapi.bean.Synonym syn = (gov.nih.nci.evs.restapi.bean.Synonym) active_full_syn_list.get(i);

			if (debugOn && i < 10) {
				System.out.println("QA_DuplicatedAtoms " + syn.toJson());
			}

			String key = null;
			if (syn.getTermName() != null && syn.getTermGroup() != null && syn.getTermSource() != null
			    && syn.getSourceCode() != null && syn.getSourceCode().length() > 0) {
				key = syn.getTermName() + "|" + syn.getTermGroup() + "|" + syn.getTermSource() + "|" + syn.getCode();
				String sourceCode = syn.getSourceCode();
				if (sourceCode == null) {
					sourceCode = "null";
				}
				key = key + "|" + sourceCode;
				String sourceSourceName = syn.getSubSourceName();
				if (sourceSourceName == null) {
					sourceSourceName = "null";
				}
				key = key + "|" + sourceSourceName;

				Vector v = new Vector();
				if (hmap.containsKey(key)) {
					pw.println("JSON: " + syn.toJson());
					System.out.println("KEY: " + key);
					pw.println("KEY: " + key);

					v = (Vector) hmap.get(key);
					num_errors++;
					for (int k=0; k<v.size(); k++) {
						gov.nih.nci.evs.restapi.bean.Synonym sy = (gov.nih.nci.evs.restapi.bean.Synonym) v.get(k);
						pw.println(sy.toJson());
					}
					pw.println(syn.toJson());
				}
				v.add(syn);
				hmap.put(key, v);
			}
		}
        String methodName = "QA_DuplicatedAtoms (same concept)";
	    dump_QA_error_counts(pw, methodName, num_errors);
	}

//11.	Check for duplicate atoms -- same name, code, term type, and SAB appearing in DIFFERENT concepts
	public void QA_DuplicatedAtomsInDifferentConcepts(PrintWriter pw) {
		pw.println("\n11.	Check for duplicate atoms -- same name, code, term type, and SAB appearing in DIFFERENT concepts");
		int num_errors = 0;
		//System.out.println("full_syn_list: " + full_syn_list.size());
		HashMap hmap = new HashMap();
		for (int i=0; i<active_full_syn_list.size(); i++) {
			gov.nih.nci.evs.restapi.bean.Synonym syn = (gov.nih.nci.evs.restapi.bean.Synonym) active_full_syn_list.get(i);

			if (debugOn && i < 10) {
				System.out.println("QA_DuplicatedAtomsInDifferentConcepts " + syn.toJson());
			}

			String key = null;
			if (syn.getTermName() != null && syn.getTermGroup() != null && syn.getTermSource() != null
				&& syn.getSourceCode() != null && syn.getSourceCode().length() > 0) {
				key = syn.getTermName() + "|" + syn.getTermGroup() + "|" + syn.getTermSource() + "|" + syn.getSourceCode();
				String subSourceName = syn.getSubSourceName();
				if (subSourceName == null) {
					subSourceName = "null";
				}
				key = key + "|" + subSourceName;

				Vector v = new Vector();
				if (hmap.containsKey(key)) {
					//System.out.println("\nWARNING: Duplicated Atoms in multipe concepts identified.");
					v = (Vector) hmap.get(key);
					for (int k=0; k<v.size(); k++) {
						gov.nih.nci.evs.restapi.bean.Synonym sy = (gov.nih.nci.evs.restapi.bean.Synonym) v.get(k);
						if (sy.getCode().compareTo(syn.getCode()) != 0) {
							num_errors++;
							for (int k2=0; k2<v.size(); k2++) {
								gov.nih.nci.evs.restapi.bean.Synonym sy2 = (gov.nih.nci.evs.restapi.bean.Synonym) v.get(k2);
								pw.println(sy2.toJson());
							}
							pw.println(syn.toJson());
						}
					}
				} else {
					v.add(syn);
				}
				hmap.put(key, v);
			 }
		 }
        String methodName = "QA_DuplicatedAtomsInDifferentConcepts";
	    dump_QA_error_counts(pw, methodName, num_errors);
	}


	//Blank property values
	public void QA_blankPropertyValues(PrintWriter pw) {
		pw.println("\n16.	Blank property values.");
		int num_errors = 0;
		for (int i=0; i<property_vec.size(); i++) {
			String line = (String) property_vec.elementAt(i);

			if (debugOn && i < 10) {
				System.out.println("QA_blankPropertyValues " + line);
			}

			Vector u = StringUtils.parseData(line, '|');
			String value = (String) u.elementAt(2);
			if (value.length() == 0) {
				pw.println(line);
				num_errors++;
			}
		}
        String methodName = "QA_blankPropertyValue";
	    dump_QA_error_counts(pw, methodName, num_errors);
	}


    public void QA_duplicated_properties(PrintWriter pw) {
		//3.	Duplicate properties within a concept (includes definitions, STYs, etc.)
		pw.println("\n3.	Duplicate properties within a concept (includes definitions, STYs, etc.)");
		int num_errors = 0;
		HashSet hset = new HashSet();
		for (int i=0; i<property_vec.size(); i++) {
			String line = (String) property_vec.elementAt(i);

			if (debugOn && i < 10) {
				System.out.println("QA_duplicated_properties " + line);
			}

			if (hset.contains(line)) {
				pw.println(line);
				num_errors++;

				if (num_errors == 5) {
					break;
				}
			} else {
				hset.add(line);
			}
		}
        String methodName = "QA_duplicated_properties";
	    dump_QA_error_counts(pw, methodName, num_errors);
	}


    public Vector extractProperties(Vector class_vec) {
        Vector w = new Vector();
        boolean istart = false;
        boolean istart0 = false;
        String classId = null;
        boolean switch_off = false;

        for (int i=0; i<class_vec.size(); i++) {
			String t = (String) class_vec.elementAt(i);
			System.out.println(t);
			if (t.indexOf("// Classes") != -1) {
				istart0 = true;
			}
		    if (t.indexOf("</rdf:RDF>") != -1) {
				break;
			}

			if (t.indexOf("<owl:Axiom>") != -1) {
				switch_off = true;
			}
			if (t.indexOf("</owl:Axiom>") != -1) {
				switch_off = false;
			}

			if (t.indexOf("<!-- http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#") != -1 && t.endsWith("-->")) {
				int n = t.lastIndexOf("#");
				t = t.substring(n, t.length());
				n = t.lastIndexOf(" ");
				classId = t.substring(1, n);
				if (istart0) {
					istart = true;
				}
			}
			if (istart) {
				t = t.trim();
				if (t.startsWith("<") && t.indexOf("rdf:resource=") != -1 && t.indexOf("owl:") == -1 && t.indexOf("rdfs:subClassOf") == -1) {
					int n = t.indexOf(">");
                    if (n != -1) {
						String s = t.substring(1, n-1);
						if (!switch_off) {
							w.add(classId + "|" + new OWLScanner().parseProperty(t));
					    }
					}
				} else if (t.startsWith("<") && t.indexOf("rdf:resource=") == -1 && t.indexOf("owl:") == -1 && t.indexOf("rdfs:subClassOf") == -1
				    && t.indexOf("rdf:Description") == -1 && t.indexOf("rdfs:subClassOf") == -1) {
					int n = t.indexOf(">");
                    if (n != -1) {
						String s = t.substring(1, n-1);
						if (!switch_off) {
						    w.add(classId + "|" + new OWLScanner().parseProperty(t));
						}
					}
				}
		    }
		}
		return w;
	}

    public void QA_SemanticTypes(PrintWriter pw) {
		pw.println("\n4.	Invalid STYs.");
        int number_of_errors = 0;
        for (int i=0; i<property_vec.size(); i++) {
			String t = (String) property_vec.elementAt(i);

			if (debugOn && i < 10) {
				System.out.println("QA_SemanticTypes " + t);
			}


			Vector u = StringUtils.parseData(t, '|');
			String concept_code = (String) u.elementAt(0);
			if (!isDeprecated(concept_code)) {
				String prop_code = (String) u.elementAt(1);
				if (prop_code.compareTo(SEMANTIC_TYPE_PROP_CODE) == 0) {
					String semantic_type = (String) u.elementAt(2);
					if (!semantic_types.contains(semantic_type)) {
						pw.println("WARNING: Invalid Semantic_Type: " + semantic_type + " -- found in " + owlScanner.getLabel(concept_code) + " (" + concept_code + ")");
						number_of_errors++;
						String concept_status = getConceptStatus(concept_code);
						pw.println("\tConcept_Status: " + concept_status);
					}
				}
		    }
		}
        String methodName = "QA_SemanticTypes";
	    dump_QA_error_counts(pw, methodName, number_of_errors);
	}


	////		//9.	Pipe characters within the data
	public void QA_pipe_characters(PrintWriter pw) {
		pw.println("\n9.	Pipe characters within the data.");
		//property_vec
		int num_errors = 0;
		for (int i=0; i<property_vec.size(); i++) {
			String line = (String) property_vec.elementAt(i);

			if (debugOn && i < 10) {
				System.out.println("QA_pipe_characters " + line);
			}

			Vector u = StringUtils.parseData(line, '|');
			if (u.size() != 3) {
				String concept_code = (String) u.elementAt(0);
				if (!isDeprecated(concept_code)) {
					pw.println(line);
					num_errors++;
				}
			}
		}
        String methodName = "QA_pipe_characters";
	    dump_QA_error_counts(pw, methodName, num_errors);
	}

//		//10.	@ characters within the definition or synonym field
	public void QA_special_characters(PrintWriter pw, char ch) {
		pw.println("\n10.	@ characters within the definition or synonym field.");
		String c = "" + ch;
		int num_errors = 0;
		for (int i=0; i<property_vec.size(); i++) {
			String line = (String) property_vec.elementAt(i);

			if (debugOn && i < 10) {
				System.out.println("QA_special_characters " + line);
			}

			Vector u = StringUtils.parseData(line, '|');
			String concept_code = (String) u.elementAt(0);
			if (line.indexOf(c) != -1) {
				if (!isDeprecated(concept_code)) {
					pw.println(line);
					num_errors++;
				}
			}
		}

		for (int i=0; i<active_full_syn_list.size(); i++) {
			gov.nih.nci.evs.restapi.bean.Synonym syn = (gov.nih.nci.evs.restapi.bean.Synonym) active_full_syn_list.get(i);
			String json = syn.toJson();
		    if (json.indexOf(c) != -1) {
				pw.println(json);
				num_errors++;
			}
		}
        String methodName = "QA_special_characters";
	    dump_QA_error_counts(pw, methodName, num_errors);
	}

//		//10.	@ characters within the definition or synonym field
	public void QA_obsolete_objects(PrintWriter pw, String c) {
		//6.	New and deprecated roles and associations (i.e., RELAs)
		//7.	New and deprecated properties (i.e., ATNs)
		//8.	New and deprecated subsources
		if (c.compareTo("R") == 0) {
			pw.println("\n6.	Deprecated roles and associations.");
		} else if (c.compareTo("P") == 0) {
			pw.println("\n7.	Deprecated properties.");
		} else if (c.compareTo("P92") == 0) {
			pw.println("\n8.	Deprecated subsources.");
		}

		int num_errors = 0;
		for (int i=0; i<deprecated_vec.size(); i++) {
			String line = (String) deprecated_vec.elementAt(i);

			if (debugOn && i < 10) {
				System.out.println("QA_obsolete_objects " + line);
			}

			if (line.startsWith(c)) {
				pw.println(line);
				num_errors++;
			}
		}
		String type = null;
		if (c.compareTo("R") == 0) {
			type = "roles";
		} else if (c.compareTo("A") == 0) {
			type = "associations";
		} else if (c.compareTo("P92") == 0) {
			type = "subsource";
		} else if (c.compareTo("P") == 0) {
			type = "properties";
		}
		String methodName = "QA_obsolete_" + type;
	    dump_QA_error_counts(pw, methodName, num_errors);
	}

	public void QA_obsolete_roles(PrintWriter pw) {
		QA_obsolete_objects(pw, "R");
	}

	public void QA_obsolete_associations(PrintWriter pw) {
		QA_obsolete_objects(pw, "A");
	}

	public void QA_obsolete_subsources(PrintWriter pw) {
		QA_obsolete_objects(pw, "P92");
	}

	public void QA_obsolete_properties(PrintWriter pw) {
		QA_obsolete_objects(pw, "P");
	}

	//Self-referential relationships
	public void QA_self_referential_relationships(PrintWriter pw) {
		pw.println("\n14.	Self-referential relationships.");

		int num_errors = 0;
		for (int i=0; i<role_vec.size(); i++) {
			String line = (String) role_vec.elementAt(i);

			if (debugOn && i < 10) {
				System.out.println("QA_self_referential_relationships " + line);
			}


            Vector u = StringUtils.parseData(line, '|');
            String source_code = (String) u.elementAt(0);
            String target_code = (String) u.elementAt(2);
            if (source_code.compareTo(target_code) == 0) {
				pw.println(line);
				num_errors++;
			}
		}
	    dump_QA_error_counts(pw, "QA_self_referential_relationships", num_errors);
	}

    public void QA_duplicate_roles(PrintWriter pw) {
		pw.println("\n2.	Duplicate roles within a concept (i.e., RELAs).");
		StringBuffer buf = new StringBuffer();
		buf.append("\tA role can appear in owl:equivalentClass as an operand; the same role also appear as an individual owl:Restriction in the same class.");
		buf.append("\tDuplicate roles would be detected by a Protege reasoner before exporting.");
	    pw.println(buf.toString());
	}

    public void QA_non_processed_properties(PrintWriter pw) {
		pw.println("\n12.	Property names that appear in the data but are not being processed (e.g., legacy concept name, umls_cui, etc)");
		StringBuffer buf = new StringBuffer();
		//dumpAnnotationProperties(pw, 'P');
		buf.append("\t(Note: These data are not contained in " + owlfile + ". ").append("MEME users should be able to identify which property is not being processed.)");
	    pw.println(buf.toString());
	}

    public String fomatRoleRelationship(String line) {
		Vector u = StringUtils.parseData(line, '|');
		String sourceCode = (String) u.elementAt(0);
		String roleCode = (String) u.elementAt(1);
		String targetCode = (String) u.elementAt(2);

		String sourceLabel = (String) code2LabelMap.get(sourceCode);
		String roleLabel = (String) roleCode2LabelMap.get(roleCode);
		String targetLabel = (String) code2LabelMap.get(targetCode);

		return sourceLabel + " (" + sourceCode + ") --[" +
		       roleLabel + " (" + roleCode + ")]--> " +
		       targetLabel + " (" + targetCode + ")";
	}

    public void QA_conflicting_RELAs(PrintWriter pw) {
		pw.println("\n15.	Conflicting RELAs between same concept pairs.");

		int num_errors = 0;
		HashSet hset = new HashSet();
		for (int i=0; i<role_vec.size(); i++) {
			String line = (String) role_vec.elementAt(i);

			if (debugOn && i < 10) {
				System.out.println("QA_conflicting_RELAs " + line);
			}

			Vector u = StringUtils.parseData(line, '|');
			String roleCode = (String) u.elementAt(1);
			if (roleCode.compareTo("R116") != 0 && roleCode.compareTo("R126") != 0) {
				hset.add((String) u.elementAt(2) + "|" + (String) u.elementAt(1) + "|" + (String) u.elementAt(0));
			}
		}

		//exclusion: Disease_May_Have_Associated_Disease (R116)
        Vector w = new Vector();
		for (int i=0; i<role_vec.size(); i++) {
			String line = (String) role_vec.elementAt(i);
			if (hset.contains(line)) {
				String s = fomatRoleRelationship(line);
				if (!w.contains(s)) {
					w.add(s);
				}
				Vector u = StringUtils.parseData(line, '|');
				String t = (String) u.elementAt(2) + "|" + (String) u.elementAt(1) + "|" + (String) u.elementAt(0);
				t = fomatRoleRelationship(t);
				if (!w.contains(t)) {
					w.add(t);
				}
				num_errors++;
			}
		}
		for (int k=0; k<w.size(); k++) {
			String line = (String) w.elementAt(k);
			pw.println("\t\t" + line);
		}
	    dump_QA_error_counts(pw, "QA_conflicting_RELAs", num_errors);
	    pw.println("\t(Note: Subject expert review is required.)");
	}

	public void dumpAnnotationProperties(PrintWriter pw, char c) {
		String ch = "" + c;
		annotationProperties = new SortUtils().quickSort(annotationProperties);
		for (int i=0; i<annotationProperties.size(); i++) {
			String t = (String) annotationProperties.elementAt(i);
			if (t.startsWith(ch)) {
				Vector u = StringUtils.parseData(t, '|');
				pw.println((String) u.elementAt(1) + " (" + (String) u.elementAt(0) + ")");
			}
		}
	}

	public void dumpObjectProperties(PrintWriter pw) {
		objectProperties = new SortUtils().quickSort(objectProperties);
		for (int i=0; i<objectProperties.size(); i++) {
			String t = (String) objectProperties.elementAt(i);
			Vector u = StringUtils.parseData(t, '|');
			pw.println((String) u.elementAt(1) + " (" + (String) u.elementAt(0) + ")");
		}
	}

	public void dump_QA_error_counts(PrintWriter pw, String methodName, int count) {
		pw.println("\t" + methodName + " -- number of potential violations detected: " + count);
		System.out.println("\t" + methodName + " -- number of potential violations detected: " + count);
	}




    public Vector extractEnum(Vector class_vec, String type) {
        Vector w = new Vector();
        boolean istart = false;
        String classId = null;

        for (int i=0; i<class_vec.size(); i++) {
			String t = (String) class_vec.elementAt(i);

			if (t.indexOf("General axioms") != -1) break;
		    if (t.indexOf("// Annotations") != -1) {
				break;
			}

			if (t.indexOf(NAMESPACE_TARGET + type + "-enum") != -1) {
				istart = true;
			}
			if (istart && t.indexOf("</rdfs:Datatype>") != -1) {
				istart = false;
				break;
			}
			if (istart && t.indexOf("<rdf:first>") != -1) {
				t = t.trim();
				int n = t.lastIndexOf("</rdf:first>");
				t = t.substring("<rdf:first>".length(), n);
				w.add(t);
			}
		}
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

	public static void main(String[] args) {
		long ms = System.currentTimeMillis();
		String owlfile = null;
		boolean saveOption = false;
		if (args.length > 0) {
			owlfile = (String) args[0];
		}
		if (args.length > 1) {
			String saveOptionStr = (String) args[1];
			if (saveOptionStr.compareTo("true") == 0) {
				saveOption = true;
			}
		}
		NCItQA ncitQA = new NCItQA(owlfile);
		ncitQA.setSaveOption(saveOption);
		ncitQA.initialize();
        ncitQA.runQA();
        //removeTemporaryFiles();
        System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}
}

