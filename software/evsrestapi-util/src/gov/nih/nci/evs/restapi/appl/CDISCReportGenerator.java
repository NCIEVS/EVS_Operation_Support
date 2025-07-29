package gov.nih.nci.evs.restapi.appl;
import gov.nih.nci.evs.restapi.config.*;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.bean.*;

import java.io.*;
import java.text.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;
import java.nio.charset.Charset;

public class CDISCReportGenerator {
	//static String sourceName = "ICH";
	//static String subSourceName = "M11";
    static String REQUIRED_DATA_FILE = ConfigurationController.requiredDataFile;
	static Vector req_data_vec = null;
    static String DATA_INFO_FILE = "data_map.txt";
    static HashMap data_info_hashmap = null;
    static HashMap dataMap = null;
    static HashMap propertyMap = null;
    static HashMap synonymMap = null;
    static OWLScanner scanner = null;

    static HierarchyHelper hh = null;
    static Vector association_vec = null;
    static HashMap associationMap = null;
    static HashSet published_valuesets = null;

    static String NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile; //"ThesaurusInferred_forTS.owl";
	static String PARENT_CHILD_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile; // "parent_child.txt";
	static String RESTRICTION_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.rolefile; //"roles.txt";
	static String AXIOM_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.axiomfile;

    static Vector extensible_list_vec = null;
    static HashMap extensibleMap = null;

    static HashMap supportedPropertyCode2LabelMap = new HashMap();
    static HashMap supportedPropertyLabel2CodeMap = new HashMap();

    static HashMap contributingSourceMap = null;

    String sourceName = null;
    String subSourceName = null;

    static {
		File f = new File(DATA_INFO_FILE);
		if (!f.exists()) {
			generateDataMap();
		}
		data_info_hashmap = load_data_info();
		System.out.println("data_info_hashmap: " + data_info_hashmap.keySet().size());

		hh = new HierarchyHelper(Utils.readFile(PARENT_CHILD_FILE));
		scanner = new OWLScanner(NCIT_OWL);

		synonymMap = AxiomParser.loadSynonyms(AXIOM_FILE);

    	associationMap = scanner.getAssociationMap();

		String prop_code = "P361";
		extensible_list_vec = scanner.extractProperties(scanner.get_owl_vec(), prop_code);
		Utils.saveToFile(prop_code + ".txt", extensible_list_vec);
		extensibleMap = new HashMap();
		for (int i=0; i<extensible_list_vec.size(); i++) {
			String line = (String) extensible_list_vec.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(0);
			String yesOrNo = (String) u.elementAt(2);
			extensibleMap.put(code, yesOrNo);
		}

		//Publish_Value_Set 	P372
		prop_code = "P372";
		Vector published_vaueset_vec = scanner.extractProperties(scanner.get_owl_vec(), prop_code);
		Utils.saveToFile(prop_code + ".txt", published_vaueset_vec);

		published_valuesets = new HashSet();
		for (int i=0; i<published_vaueset_vec.size(); i++) {
			String line = (String) published_vaueset_vec.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String yesOrNo = (String) u.elementAt(2);
			if (yesOrNo.compareTo("Yes") == 0) {
				published_valuesets.add((String) u.elementAt(0));
			}
		}

		synonymMap = AxiomParser.loadSynonyms(AXIOM_FILE);
		createSupportedPropertyMaps();
	}

	public CDISCReportGenerator() {

	}

	public CDISCReportGenerator(String sourceName, String subSourceName) {
		this.sourceName = sourceName;
		this.subSourceName = subSourceName;
	}

	public static String getLabel(String code) {
		return hh.getLabel(code);
	}

	public static void createSupportedPropertyMaps() {
        supportedPropertyCode2LabelMap = new HashMap();
        supportedPropertyLabel2CodeMap = new HashMap();
		Vector v = scanner.extractAnnotationProperties(scanner.get_owl_vec());
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			supportedPropertyCode2LabelMap.put((String) u.elementAt(0), (String) u.elementAt(1));
			supportedPropertyLabel2CodeMap.put((String) u.elementAt(1), (String) u.elementAt(0));
		}

		v = scanner.extractObjectProperties(scanner.get_owl_vec());
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			supportedPropertyCode2LabelMap.put((String) u.elementAt(0), (String) u.elementAt(1));
			supportedPropertyLabel2CodeMap.put((String) u.elementAt(1), (String) u.elementAt(0));
		}
	}

	public static List vector2List(Vector v) {
		List list = new ArrayList();
		for (int i=0; i<v.size(); i++) {
			Object obj = v.elementAt(i);
			list.add(obj);
		}
		return list;
	}

	public static List getSynonyms(String code) {
		Vector v = (Vector) synonymMap.get(code);
		if (v == null) return null;
		return vector2List(v);
	}

	public static HashMap load_data_info() {
		Vector v = Utils.readFile(DATA_INFO_FILE);
		HashMap hmap = new HashMap();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '=');
			hmap.put((String) u.elementAt(0), (String) u.elementAt(1));
		}
		return hmap;
	}

	public static Vector generateDataRequirements(String line) {
		System.out.println("generateDataRequirements");
		Vector w = new Vector();
		Vector u = StringUtils.parseData(line, '\t');
		for (int i=0; i<u.size(); i++) {
			String t = (String) u.elementAt(i);
			if (data_info_hashmap.containsKey(t)) {
				String value = (String) data_info_hashmap.get(t);
				w.add(value);
			} else {
				Vector u2 = StringUtils.parseData(t, ' ');
				if (u2.size() == 1) {
					String s = (String) u2.elementAt(0);
					if (data_info_hashmap.containsKey(s)) {
						String value = (String) data_info_hashmap.get(s);
						w.add(s);
					} else {
						w.add(t);
					}
				} else if (u2.size() == 2) {
					String src = (String) u2.elementAt(0);
					String s = (String) u2.elementAt(1);
					if (s.compareTo("PT") == 0) {
						String s1 = (String) data_info_hashmap.get("NCI PT");
						s1 = s1.replace("NCI", src);
						w.add(s1);
					} else if (s.compareTo("SY") == 0) {
						String s1 = (String) data_info_hashmap.get("NCI SY");
						s1 = s1.replace("NCI", src);
						w.add(s1);
					} else {
						w.add(t);
					}
				} else {
					w.add(t);
				}
			}
		}
        return w;
	}

	public static void exploreFileHeading(String filename) {
		Vector v = Utils.readFile(filename);
		String heading = (String) v.elementAt(0);
		Vector u = StringUtils.parseData(heading, '\t');
		Utils.dumpVector(heading, u);
	}

/*
	public static void main1(String[] args) {
		//CDISC ADaM Terminology (Code C81222)

		long ms = System.currentTimeMillis();
		String heading = "NCI Preferred Term\tCDISC Definition\tCDISC PT\tCDISC SY";
		heading = "Code	Codelist Code	Codelist Extensible (Yes/No)	Codelist Name	" + sourceName + " Submission Value	" + sourceName + " Synonym(s)	" + sourceName + " Definition	NCI Preferred Term";
		Vector u = StringUtils.parseData(heading, '\t');
		Utils.dumpVector(heading, u);

		Vector v = generateDataRequirements(heading);
		Utils.dumpVector(heading, v);

		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}
*/

	public static Vector getSubclassCodes(String code) {
		return hh.getSubclassCodes(code);
	}

	public static void getA8Map() {
		HashMap a8_map = (HashMap) associationMap.get("A8");
		Utils.dumpMultiValuedHashMap("A8", a8_map);
	}

    public static HashMap getAxiomMap(String axiomfile, String data_req) {
		HashMap hmap = new HashMap();
		Vector v = scanner.getAxioms(axiomfile, data_req);//"P90|P384$CDISC|P383$SY");
		String id = null;
		for (int i=0; i<v.size(); i++) {
			Object obj = v.elementAt(i);
			if (obj instanceof String) {
				id = (String) obj;
			} else if (obj instanceof Synonym) {
				Synonym syn = (Synonym) obj;
				id = syn.getCode();
			} else if (obj instanceof Definition) {
				Definition def = (Definition) obj;
				id = def.getCode();
			} else if (obj instanceof AltDefinition) {
				AltDefinition def = (AltDefinition) obj;
				id = def.getCode();
			} else if (obj instanceof GoAnnotation) {
				GoAnnotation go = (GoAnnotation) obj;
				id = go.getCode();
			}
			Vector w = new Vector();
			if (hmap.containsKey(id)) {
				w = (Vector) hmap.get(id);
			}
			w.add(obj);
			hmap.put(id, w);
		}
		return hmap;
	}

    public static String getPropertyValues(Vector w, String code) {
		String s = null;
		StringBuffer buf = new StringBuffer();
		if (w == null || w.size() == 0) return "";
		for (int j=0; j<w.size(); j++) {
			Object obj = w.elementAt(j);
			if (obj instanceof String) {
				String value = (String) obj;
				buf.append(value).append("|");
			} else if (obj instanceof Synonym) {
				Synonym syn = (Synonym) obj;
				String id = syn.getCode();
				if (id.compareTo(code) == 0) {
					String value = syn.getTermName();
					buf.append(value).append("|");
				}
			} else if (obj instanceof Definition) {
				Definition def = (Definition) obj;
				String id = def.getCode();
				if (id.compareTo(code) == 0) {
					String value = def.getDescription();
					buf.append(value).append("|");
				}
			} else if (obj instanceof AltDefinition) {
				AltDefinition def = (AltDefinition) obj;
				String id = def.getCode();
				if (id.compareTo(code) == 0) {
					String value = def.getDescription();
					buf.append(value).append("|");
				}
			} else if (obj instanceof GoAnnotation) {
				GoAnnotation go = (GoAnnotation) obj;
				String id = go.getCode();
				if (id.compareTo(code) == 0) {
					String value = go.getAnnotation();
					buf.append(value).append("|");
				}
			}
		}
		s = buf.toString();
		if (s.length() > 0) {
			s = s.substring(0, s.length()-1);
		}
		return s;
	}


    public static Vector extractObjectProperties() {
		return scanner.extractObjectProperties(scanner.get_owl_vec());
	}

	public static void generateDataMap() {
		String AXIOM_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.axiomfile;
    	Vector w = getDistinctQualifiers(AXIOM_FILE);
    	Vector w2 = interpret(w);
    	Vector v = scanner.extractAnnotationProperties(scanner.get_owl_vec());
    	Vector w3 = properties2RequiredData(v);
    	w2.addAll(w3);
    	Utils.saveToFile(DATA_INFO_FILE, w2);
	}

    public static Vector interpret(Vector v) {
		Vector w = new Vector();
		//Vector v = Utils.readFile(filename);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String prop_code = (String) u.elementAt(0);
			if (prop_code.compareTo("P97") == 0 && u.size() == 2) {
				String t = (String) u.elementAt(1);
				Vector u2 = StringUtils.parseData(t, '$');
				w.add((String) u2.elementAt(1) + " Definition=" + line);
			} else if (prop_code.compareTo("P325") == 0 && u.size() == 2) {
				String t = (String) u.elementAt(1);
				Vector u2 = StringUtils.parseData(t, '$');
				w.add((String) u2.elementAt(1) + " Definition=" + line);
			} else if (prop_code.compareTo("P90") == 0 && u.size() == 3) {
				String t = (String) u.elementAt(1);
				Vector u2 = StringUtils.parseData(t, '$');
				String term_type = (String) u2.elementAt(1);
				t = (String) u.elementAt(2);
				u2 = StringUtils.parseData(t, '$');
				String term_src = (String) u2.elementAt(1);
				w.add(term_src + " " + term_type + "=" + line);
			}
		}
		return w;
	}


    public static Vector getDistinctQualifiers(String axiomfile) {
		Vector w = new Vector();
		Vector v = Utils.readFile(axiomfile);
		SortUtils sortUtils = new SortUtils();
		HashSet hset = new HashSet();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String prop_code = (String) u.elementAt(2);
			if (prop_code.compareTo("P375") != 0 && prop_code.compareTo("P211") != 0) {
				Vector w0 = new Vector();
				w0.add(prop_code);
				Vector w1 = new Vector();
				for (int j=4; j<u.size(); j++) {
					String s = (String) u.elementAt(j);
					if (s.indexOf("P381") == -1 && s.indexOf("P385") == -1 && s.indexOf("P395") == -1 && s.indexOf("P391") == -1 && s.indexOf("P387") == -1) {
						w1.add(s);
					}
				}
				w1 = sortUtils.quickSort(w1);
				w0.addAll(w1);
				StringBuffer buf = new StringBuffer();
				for (int j=0; j<w0.size(); j++) {
					String s = (String) w0.elementAt(j);
					buf.append(s).append("|");
				}
				String t = buf.toString();
				t = t.substring(0, t.length()-1);
				if (!hset.contains(t)) {
					hset.add(t);
					w.add(t);
				}
			}
		}
		w = sortUtils.quickSort(w);
		return w;
	}

	public static Vector properties2RequiredData(Vector v) {
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String s0 = (String) u.elementAt(0);
			String s1 = (String) u.elementAt(1);
			w.add(s1 + "=" + s0);
		}
		return new SortUtils().quickSort(w);
	}

	public Vector resolveValueSet(String code) {
		Vector w = new Vector();
        HashMap cdisc_syn_map = getAxiomMap(AXIOM_FILE, "P90|P384$" + sourceName + "|P383$SY");
        HashMap cdisc_def_map = getAxiomMap(AXIOM_FILE, "P325|P378$" + sourceName);
        HashMap ncipt_map = getAxiomMap(AXIOM_FILE, "P90|P384$NCI|P383$PT");

		w.add("Code	Codelist Code	Codelist Extensible (Yes/No)	Codelist Name	" + sourceName + " Submission Value	" + sourceName + " Synonym(s)	" + sourceName + " Definition	NCI Preferred Term");

		Vector subs = getSubclassCodes(code);
		String cdisc_syns = null;
		String cdisc_def = null;
		String nci_pt = null;
		for (int i=0; i<subs.size(); i++) {
			String sub_code = (String) subs.elementAt(i);
			String extensible = (String) extensibleMap.get(sub_code);
			if (published_valuesets.contains(sub_code)) {
				List parentAxioms = getSynonyms(sub_code);
				List axioms = null;//getSynonyms(member_code);
				String member_code = null;
				String cdiscSubmissionValue = SpecialProperties.getCDISCSubmissionValue(sub_code, parentAxioms, null, null, sourceName, subSourceName);
				String codeListName = SpecialProperties.getCodeListName(sub_code, parentAxioms, sourceName);

				cdisc_syns = getPropertyValues((Vector) cdisc_syn_map.get(sub_code), sub_code);
				cdisc_def = getPropertyValues((Vector) cdisc_def_map.get(sub_code), sub_code);
				nci_pt = getPropertyValues((Vector) ncipt_map.get(sub_code), sub_code);

				w.add(sub_code + "\t\t" + extensible  + "\t" + codeListName + "\t" + cdiscSubmissionValue + "\t" + cdisc_syns + "\t" + cdisc_def + "\t" + nci_pt);

				HashMap a8Map = (HashMap) associationMap.get("A8");
				Vector members = (Vector) a8Map.get(sub_code);
				for (int j=0; j<members.size(); j++) {
					member_code = (String) members.elementAt(j);
	                parentAxioms = getSynonyms(sub_code);
	                axioms = getSynonyms(member_code);

	                cdiscSubmissionValue = "";
	                //try {
						cdiscSubmissionValue = SpecialProperties.getCDISCSubmissionValue(sub_code, parentAxioms, member_code, axioms, sourceName, subSourceName);
					//} catch (Exception ex) {
					if (cdiscSubmissionValue == null) {
						System.out.println("ERROR: SubmissionValue does not exist for (" + sub_code + ", " + member_code + ")");
					}

	                codeListName = "";
	                try {
						codeListName = SpecialProperties.getCodeListName(sub_code, parentAxioms, sourceName);
					} catch (Exception ex) {
						System.out.println("ERROR: CodeListName does not exist for (" + sub_code + ", " + member_code + ")");
					}

	                cdisc_syns = getPropertyValues((Vector) cdisc_syn_map.get(member_code), member_code);
	                cdisc_def = getPropertyValues((Vector) cdisc_def_map.get(member_code), member_code);
	                nci_pt = getPropertyValues((Vector) ncipt_map.get(member_code), member_code);

					w.add(member_code + "\t" + sub_code  + "\t\t" + codeListName + "\t" + cdiscSubmissionValue + "\t" + cdisc_syns + "\t" + cdisc_def + "\t" + nci_pt);
				}
			}
		}
		return w;
	}

	public static void substitute(String filename, Vector replace_vec, Vector by_vec) {
		Vector v = Utils.readFile(filename);
		int n = filename.lastIndexOf(".");
		String backupfile = "bak_" + filename;
		Utils.saveToFile(backupfile, v);
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			for (int j=0; j<replace_vec.size(); j++) {
				String replace = (String) replace_vec.elementAt(j);
				String by = (String) by_vec.elementAt(j);
				line = line.replace(replace, by);
			}
			w.add(line);
		}
		Utils.saveToFile(filename, w);
	}

	public void run(String root) {
		String code = root;
		CDISCReportGenerator generator = new CDISCReportGenerator(
		    sourceName, subSourceName);
		Vector w = generator.resolveValueSet(code);
		String label = getLabel(code);
		String textfile = label + ".txt";
		Vector replace_vec = new Vector();
		Vector by_vec = new Vector();
		replace_vec.add("\tnull\t");
		by_vec.add("\t\t");
        substitute(textfile, replace_vec, by_vec);
        int sheetIndex = 0;
		FormatUtils.delimitedText2Excel(textfile);
		char delim = '\t';
		String sheetName = label + " " + StringUtils.getToday("yyyy-MM-dd");
		String xlsfile = null;
		try {
			//ExcelReadWriteUtils.writeXLSFile(textfile, delim, sheetName);
			xlsfile = ExcelReadWriteUtils.writeXLSFile(textfile, delim, sheetName);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		CDISCExcelUtils.copyfile(xlsfile, "bak_" + xlsfile);
		boolean status = CDISCExcelUtils.reformat(xlsfile, sheetIndex, sheetName);
        System.out.println("Reformat of " + xlsfile + " successful? " + status);
    }

	public static void main(String[] args) {
		String sourceName = args[0];
		String subSourceName = args[1];
		String root = args[2]; //C217023

		CDISCReportGenerator generator = new CDISCReportGenerator(
		    sourceName, subSourceName);
		generator.run(root);
	}
}

/*
Code	Codelist Code	Codelist Extensible (Yes/No)	Codelist Name	CDISC Submission Value	CDISC Synonym(s)	CDISC Definition	NCI Preferred Term
C208382		No	Acute Physiology and Chronic Health Evaluation II Clinical Classification Parameter Code	APCH1PC	Acute Physiology and Chronic Health Evaluation II Clinical Classification Parameter Code	A parameter code codelist for the Acute Physiology and Chronic Health Evaluation II Clinical Classification (APACHE II) to support the calculation of total score in ADaM.	CDISC ADaM Acute Physiology and Chronic Health Evaluation II Clinical Classification Parameter Code Terminology
*/
