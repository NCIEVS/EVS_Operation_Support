package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;
import gov.nih.nci.evs.restapi.bean.*;

import java.io.*;
import java.util.*;

public class ReportGenerator {
    public static String REPORT_GENERATION_DIRECTORY = ConfigurationController.reportGenerationDirectory;
	public static String HIER_FILE = REPORT_GENERATION_DIRECTORY + File.separator + ConfigurationController.hierfile;
	public static String ROLE_FILE = REPORT_GENERATION_DIRECTORY + File.separator + ConfigurationController.rolefile;
	public static String PROPERTY_FILE = REPORT_GENERATION_DIRECTORY + File.separator + ConfigurationController.propertyfile;
	public static String NCIT_OWL_FILE = REPORT_GENERATION_DIRECTORY + File.separator + ConfigurationController.owlfile;
	public static String AXIOM_FILE = REPORT_GENERATION_DIRECTORY + File.separator + ConfigurationController.axiomfile;
	public static String SUBSET_FILE = REPORT_GENERATION_DIRECTORY + File.separator + ConfigurationController.subsetfile;
	public static String LABEL_FILE = REPORT_GENERATION_DIRECTORY + File.separator + "label.txt";

	public Vector dataReq = null;
	public HashMap dataHashMap = null;
	public static HierarchyHelper hh = null;

    static HashMap propertyMap = null;
    static Vector properties = null;
    static Vector annotationProperties = null;
    static Vector objectProperties = null;

    public static HashMap annotationPropertyCode2LabelMap = null;
    public static HashMap annotationPropertyLabel2CodeMap = null;

    public static HashMap objectPropertyCode2LabelMap = null;
    public static HashMap objectPropertyLabel2CodeMap = null;

	public static String DATA_MAP = "data_map.txt";
	public static HashMap dataReqSrc2TargetHashMap = null;
	public static HashMap dataReqTarget2SrcHashMap = null;

    public static HashMap subset_hmap = null;//create_subset_hmap();
    public static HashMap synonymMap = null;
    static HashSet published_valuesets = null;

    static {
		long ms = System.currentTimeMillis();
		OWLScanner owlscanner = null;
		hh = new HierarchyHelper(Utils.readFile(HIER_FILE));
		owlscanner = new OWLScanner(NCIT_OWL_FILE);
		annotationProperties = owlscanner.extractAnnotationProperties(owlscanner.get_owl_vec());
		annotationPropertyCode2LabelMap = new HashMap();
		annotationPropertyLabel2CodeMap = new HashMap();
		for (int i=0; i<annotationProperties.size(); i++) {
			String line = (String) annotationProperties.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			annotationPropertyCode2LabelMap.put((String) u.elementAt(0),(String) u.elementAt(1));
			annotationPropertyLabel2CodeMap.put((String) u.elementAt(1),(String) u.elementAt(0));
		}

		objectProperties = owlscanner.extractObjectProperties(owlscanner.get_owl_vec());
		objectPropertyCode2LabelMap = new HashMap();
		objectPropertyLabel2CodeMap = new HashMap();
		for (int i=0; i<objectProperties.size(); i++) {
			String line = (String) objectProperties.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			objectPropertyCode2LabelMap.put((String) u.elementAt(0),(String) u.elementAt(1));
			objectPropertyLabel2CodeMap.put((String) u.elementAt(1),(String) u.elementAt(0));
		}

		dataReqSrc2TargetHashMap = new HashMap();
		dataReqTarget2SrcHashMap = new HashMap();
		File f = new File(DATA_MAP);
		if (f.exists()) {
			Vector v = Utils.readFile(DATA_MAP);
			for (int i=0; i<v.size(); i++) {
				String line = (String) v.elementAt(i);
				Vector u = StringUtils.parseData(line, '=');
				dataReqSrc2TargetHashMap.put((String) u.elementAt(0), (String) u.elementAt(1));
				dataReqTarget2SrcHashMap.put((String) u.elementAt(1), (String) u.elementAt(0));
			}
		} else {
			System.out.println("Generating " + DATA_MAP + " ... ");
			generateDataMap();
		}

		//Publish_Value_Set 	P372
		String prop_code = "P372";
		Vector published_vaueset_vec = owlscanner.extractProperties(owlscanner.get_owl_vec(), prop_code);

		published_valuesets = new HashSet();
		for (int i=0; i<published_vaueset_vec.size(); i++) {
			String line = (String) published_vaueset_vec.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String yesOrNo = (String) u.elementAt(2);
			if (yesOrNo.compareTo("Yes") == 0) {
				published_valuesets.add((String) u.elementAt(0));
			}
		}

		subset_hmap = create_subset_hmap();
		synonymMap = AxiomParser.loadSynonyms(AXIOM_FILE);
		System.out.println("Total initialization run time (ms): " + (System.currentTimeMillis() - ms));
	}

	public static boolean is_published_valueset(String code) {
		return published_valuesets.contains(code);
	}

	public static Vector getSubclassCodes(String code) {
		return hh.getSubclassCodes(code);
	}

    public static HashMap getAxiomMap(String data_req) {
		HashMap hmap = new HashMap();
		OWLScanner scanner = new OWLScanner(NCIT_OWL_FILE);
		Vector v = scanner.getAxioms(AXIOM_FILE, data_req);//"P90|P384$CDISC|P383$SY");
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

///////////////////////////////////////////////////////////
	public static void generateDataMap() {
    	Vector w = getDistinctQualifiers(AXIOM_FILE);
    	Vector w2 = interpret(w);
    	Vector v = annotationProperties;
    	Vector w3 = properties2RequiredData(v);
    	w2.addAll(w3);
    	Utils.saveToFile(DATA_MAP, w2);
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

///////////////////////////////////////////////////////////
	public Vector getSubsetMemberCodes(String subsetCode) {
        return (Vector) subset_hmap.get(subsetCode);
	}

	public static HashMap create_subset_hmap() {
		Vector association_vec = Utils.readFile(SUBSET_FILE);
		HashMap hmap = new HashMap();
		for (int i=0; i<association_vec.size(); i++) {
			String line = (String) association_vec.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String member_code = (String) u.elementAt(0);
			String asso = (String) u.elementAt(1);
			if (asso.compareTo("A8") == 0) {
				String subset_code = (String) u.elementAt(2);
				Vector w = new Vector();
				if (hmap.containsKey(subset_code)) {
					w = (Vector) hmap.get(subset_code);
				}
				w.add(member_code);
				hmap.put(subset_code, w);
			}
		}
		return hmap;
	}

    public ReportGenerator(Vector dataReq) {
		this.dataReq = dataReq;
		long ms = System.currentTimeMillis();
		initialize();
		System.out.println("Total initialization run time (ms): " + (System.currentTimeMillis() - ms));
	}

	public String getAnnotationPropertyLabel(String propertyCode) {
		return (String) annotationPropertyCode2LabelMap.get(propertyCode);
	}

	public String getObjectPropertyLabel(String propertyCode) {
		return (String) objectPropertyCode2LabelMap.get(propertyCode);
	}

	public Vector retrieveData(Vector dataReq) {
		if (dataReq == null) return null;
		DataRetrieval test = new DataRetrieval(NCIT_OWL_FILE, dataReq);
        Vector w = new Vector();
        for (int i=0; i<dataReq.size(); i++) {
			String data_req = (String) dataReq.elementAt(i);
			w.addAll(test.getData(data_req));
		}
		return w;
	}

	public HashMap createCode2ValuesHashMap(Vector v, String req) {
		HashMap hmap = new HashMap();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '\t');
			String code = (String) u.elementAt(0);
			String label = (String) u.elementAt(1);
			if (label.compareTo(req) == 0) {
				String value = (String) u.elementAt(2);
				Vector w = new Vector();
				if (hmap.containsKey(code)) {
					w = (Vector) hmap.get(code);
				}
				w.add(value);
				hmap.put(code, w);
			}
	    }
		return hmap;
	}

	public void initialize() {
		Vector v = retrieveData(this.dataReq);
		dataHashMap = new HashMap();
		for (int i=0; i<this.dataReq.size(); i++) {
			String req = (String) this.dataReq.elementAt(i);
			HashMap hmap = createCode2ValuesHashMap(v, req);
			dataHashMap.put(req, hmap);
		}
	}

    public static String getLabel(String code) {
	    return hh.getLabel(code);
	}

	public Vector appendData(String filename, int codeCol, String dataHeading) {
		Vector v = Utils.readFile(filename);
		Vector w = new Vector();
		w.add((String) v.elementAt(0) + "\t" + dataHeading);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(codeCol);
			w.add(line + "\t" + getValues(code));
		}
		return w;
	}

    public static String getNCItVersion() {
		long ms = System.currentTimeMillis();
		Vector v = Utils.readFile(NCIT_OWL_FILE);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			line = line.trim();
			if (line.indexOf("<owl:versionInfo>") != -1) {
				int n = line.indexOf(">");
				String t = line.substring(n+1, line.length());
				n = t.indexOf("<");
				t = t.substring(0, n);
				System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
				return t;
			}
		}
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
		return null;
	}

	public static String vector2Delimited(Vector v, String delim) {
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<v.size(); i++) {
			String value = (String) v.elementAt(i);
			buf.append(value);
			if (i < v.size()-1) {
				buf.append(delim);
			}
		}
		return buf.toString();
	}

	public static void saveData(String outputDir, String outputfile, Vector v) {
		File dir = new File(outputDir);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		Utils.saveToFile(outputDir + File.separator + outputfile, v);
	}

////////////////////////////////////////////////////////////////////////////////////////////
	public String getValues(String req, String code, String delim) {
		HashMap hmap = (HashMap) dataHashMap.get(req);
		if (hmap == null) {
			System.out.println("WARNING: " + req + " data not available.");
			return "";
		}
		if (hmap.containsKey(code)) {
			Vector w = (Vector) hmap.get(code);
			String s = vector2Delimited(w, delim);
			return s;
		}
		return "";
	}

	public Vector getValues(Vector codes) {
		Vector w = new Vector();
		for (int i=0; i<codes.size(); i++) {
			String code = (String) codes.elementAt(i);
			w.add(getValues(code));
		}
		return w;
	}

	public String getValues(String code) {
		return getValues(this.dataReq, code, " || ");
	}

	public String getValues(String code, String delim) {
		return getValues(this.dataReq, code, delim);
	}

	public String getValues(Vector dataReq, String code, String delim) {
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<dataReq.size(); i++) {
			String req = (String) dataReq.elementAt(i);
			String s = getValues(req, code, delim);
			buf.append(s);
			if (i < dataReq.size()-1) {
				buf.append("\t");
			}
		}
		return buf.toString();
	}

	public String getPT(String code, String delim) {
		String source = "NCI";
		String termType = "SY";
		return getTermName(code, termType, source, delim);
	}

	public String getSY(String code, String delim) {
		String source = "NCI";
		String termType = "SY";
		return getTermName(code, termType, source, delim);
	}

	public String getNCIPT(String code, String delim) {
		String source = "NCI";
		String termType = "PT";
		return getTermName(code, termType, source, delim);
	}

	public String getNCISY(String code, String delim) {
		String source = "NCI";
		String termType = "SY";
		return getTermName(code, termType, source, delim);
	}

	public String getPT(String code, String source, String delim) {
		String termType = "PT";
		return getTermName(code, termType, source, delim);
	}

	public String getSY(String code, String source, String delim) {
		String termType = "SY";
		return getTermName(code, termType, source, delim);
	}

	public String getTermName(String code, String termType, String source, String delim) {
		String req = "P90|P383$" + termType + "|P384$" + source;
		return getValues(req, code, delim);
	}


	public Vector getRelatedConceptCodes(String code, String prop_code) {
		String delim = "|";

		System.out.println("code: " + code);
		System.out.println("prop_code: " + prop_code);

		String str = getValues(prop_code, code, delim);
		return StringUtils.parseData(str, delim);
	}

	public Vector getRelatedConceptPTs(String code, String prop_code) {
		String source = "NCI";
		return getRelatedConceptSourcePTs(code, prop_code, source);
	}

	public Vector getRelatedConceptSourcePTs(String code, String prop_code, String source) {
		Vector w = new Vector();
		Vector codes = getRelatedConceptCodes(code, prop_code);
		for (int i=0; i<codes.size(); i++) {
			code = (String) codes.elementAt(i);
			String pt = getPT(code, source, "|");
			w.add(pt);
		}
		return w;
	}

////////////////////////////////////////////////////////////////////////////////////////////
	public static Vector exploreTemplateColumnLabels(String templateFile) {
		Vector w = new Vector();
		TemplateLoader test = new TemplateLoader();
		Template template = test.load(templateFile);
        List<TemplateColumn> list = template.getColumns();
        for (int i=0; i<list.size(); i++) {
			TemplateColumn col = (TemplateColumn) list.get(i);
			w.add(col.getLabel());
			Vector u = StringUtils.parseData(col.getLabel(), ' ');
			//Utils.dumpVector(col.getLabel(), u);
			String key = (String) u.elementAt(0);
			if (objectPropertyLabel2CodeMap.containsKey(key)) {
				System.out.println((String) objectPropertyLabel2CodeMap.get(key));
			} else if (annotationPropertyLabel2CodeMap.containsKey(key)) {
				System.out.println((String) annotationPropertyLabel2CodeMap.get(key));
			}
		}
		return w;
	}

	public static String generateReportHeader(String templateFile) {
		StringBuffer buf = new StringBuffer();
		TemplateLoader test = new TemplateLoader();
		Template template = test.load(templateFile);
        List<TemplateColumn> list = template.getColumns();
        for (int i=0; i<list.size(); i++) {
			TemplateColumn col = (TemplateColumn) list.get(i);
			buf.append(col.getLabel()).append("\t");
		}
		String t = buf.toString();
		t = t.substring(0, t.length()-1);
		return t;
	}

/*
	public static Vector resolveValueSet(String rootConceptCode) {
		if (!subset_hmap.containsKey(rootConceptCode)) {
			return null;
		}
		return (Vector) subset_hmap.get(rootConceptCode);
	}
*/

	public static Vector resolveValueSet(String root) {
		return resolveValueSet(root, 0, true);
	}

	public static Vector resolveValueSet(String root, int subsetLevel) {
		return resolveValueSet(root, subsetLevel, false);
	}

	public static Vector resolveValueSet(String root, int subsetLevel, boolean includeRootMembers) {
		Stack stack = new Stack();
		Vector w = new Vector();
		stack.push("0|" + root);
		while (!stack.isEmpty()) {
			String s = (String) stack.pop();
			System.out.println(s);
			Vector u = StringUtils.parseData(s, '|');
			String levelStr = (String) u.elementAt(0);

			String code = (String) u.elementAt(1);
			if (includeRootMembers) {
				if (is_published_valueset(code)) {
					String label = getLabel(code);
					Vector members = (Vector) subset_hmap.get(code);
					for (int k=0; k<members.size(); k++) {
						String member_code = (String) members.elementAt(k);
						String member_label = getLabel(member_code);
						String t = label + "|" + code + "|" + member_label + "|" + member_code;
						if (!w.contains(t)) {
							w.add(t);
						}
					}
				}
			}
			Vector subs = getSubclassCodes(code);

            if (subs == null) {
				System.out.println("getSubclassCodes(" + code + ") returns null.");
				if (is_published_valueset(code)) {
					String label = getLabel(code);
					Vector members = (Vector) subset_hmap.get(code);
					for (int k=0; k<members.size(); k++) {
						String member_code = (String) members.elementAt(k);
						String member_label = getLabel(member_code);
						String t = label + "|" + code + "|" + member_label + "|" + member_code;
						if (!w.contains(t)) {
							w.add(t);
						}
					}
				}
			} else {
				System.out.println("subs.size(): " + subs.size());
				Utils.dumpVector(code, subs);
				int level = Integer.parseInt(levelStr);
				if (level == subsetLevel) {
					for (int j=0; j<subs.size(); j++) {
						String sub = (String) subs.elementAt(j);
						if (is_published_valueset(sub)) {
							String label = getLabel(code);
							Vector members = (Vector) subset_hmap.get(sub);
							for (int k=0; k<members.size(); k++) {
								String member_code = (String) members.elementAt(k);
								String member_label = getLabel(member_code);
								String t = label + "|" + code + "|" + member_label + "|" + member_code;
								if (!w.contains(t)) {
									w.add(t);
								}
							}
						}
					}
				} else {
					int nextLevel = level + 1;
					for (int j=0; j<subs.size(); j++) {
						String sub = (String) subs.elementAt(j);
						stack.push("" + nextLevel + "|" + sub);
					}
				}
			}
		}
        return w;
	}

////////////////////////////////////////////////////////////////////////////////////////////
	public static String getMethod4RetrievingRelatedConceptData(String s) {
		Vector u = StringUtils.parseData(s, ' ');
		if (u.size() == 2) {
			String rel = (String) u.elementAt(0);
			boolean bool = annotationPropertyCode2LabelMap.containsKey(rel);
			if (annotationPropertyCode2LabelMap.containsKey(rel)) {
				String t = (String) u.elementAt(1);
				if (t.compareTo("Code") == 0) {
					return "generator.vector2Delimited(generator.getRelatedConceptCodes(code, \"" + rel + "\"), \"|\")";
				} else if (t.compareTo("PT") == 0) {
					return "generator.vector2Delimited(generator.getRelatedConceptPTs(code, \"" + rel + "\"), \"|\")";
				}
			} else if (objectPropertyCode2LabelMap.containsKey(rel)) {
				String t = (String) u.elementAt(1);
				if (t.compareTo("Code") == 0) {
					return "generator.vector2Delimited(generator.getRelatedConceptCodes(code, \"" + rel + "\"), \"|\")";
				} else if (t.compareTo("PT") == 0) {
					return "generator.vector2Delimited(generator.getRelatedConceptPTs(code, \"" + rel + "\"), \"|\")";
				}
			}
		}
		return null;
	}

    public Vector exportData(String req) {
		if (!dataHashMap.containsKey(req)) {
			return null;
		}
		HashMap hmap = (HashMap) dataHashMap.get(req);
		Iterator it = hmap.keySet().iterator();
		Vector w = new Vector();
		while (it.hasNext()) {
			String key = (String) it.next();
			Vector values = (Vector) hmap.get(key);
			for (int i=0; i<values.size(); i++) {
				String value = (String) values.elementAt(i);
				w.add(key + "|" + req + "|" + value);
			}
		}
		return w;
	}

	public static Vector extractColumnData(String filename, String col_str, char delim) {
		DataExtractor.dumpHeading(filename);
		return DataExtractor.extractColumnData(filename, col_str, delim);
	}

	public static Vector insertColumnData(Vector v, Vector values, int col) {
		return DataExtractor.insertColumnData(v, values, col);
	}

	public static Vector rearrageColumnData(String filename, String col_str, char delim) {
		return DataExtractor.rearrageColumnData(filename, col_str, delim);
	}

    public String getCodeListName(String codelistcode) {
		Vector v1 = (Vector) synonymMap.get(codelistcode);
		List parentAxioms = SpecialProperties.vector2List(v1);
		return SpecialProperties.getCodeListName(codelistcode, parentAxioms);
	}

    public String getCDISCSubmissionValue(String subsetcode, String code) {
		Vector v1 = (Vector) synonymMap.get(code);
		List<Synonym> axioms = SpecialProperties.vector2List(v1);
		Vector v2 = (Vector) synonymMap.get(subsetcode);
		List<Synonym> parentAxioms = SpecialProperties.vector2List(v2);
		return SpecialProperties.getCDISCSubmissionValue(subsetcode, parentAxioms, code, axioms);
	}

	public static String[] header2DataReq(String templateFile) {
		Vector colData = new TemplateLoader().exploreTemplateColumnLabels(templateFile);
		Utils.dumpVector("columns", colData);
		return header2DataReq(colData);
	}

	public static String[] header2DataReq(Vector colHeaders) {
		Utils.dumpVector("colHeaders", colHeaders);
		Vector reqVec = new Vector();
		HashMap hmap = new HashMap();
		String[] colData = new String[colHeaders.size()];
		for (int i=0; i<colHeaders.size(); i++) {
			colData[i] = "";
			String t = (String) colHeaders.elementAt(i);
			t = t.replace("NCIt", "NCI");
			if (dataReqSrc2TargetHashMap.containsKey(t)) {
				colData[i] = (String) dataReqSrc2TargetHashMap.get(t);
			} else {
				Vector u = StringUtils.parseData(t, ' ');
				for (int j=0; j<u.size(); j++) {
					String key = (String) u.elementAt(0);
					if (objectPropertyLabel2CodeMap.containsKey(key)) {
						colData[i] = (String) dataReqSrc2TargetHashMap.get(key);
					} else if (annotationPropertyLabel2CodeMap.containsKey(key)) {
						colData[i] = (String) dataReqSrc2TargetHashMap.get(key);
					}
				}
			}
		}
		for (int i=0; i<colHeaders.size(); i++) {
			String t = (String) colHeaders.elementAt(i);
			if (colData[i] == "") {
				colData[i] = (String) colHeaders.elementAt(i);
			}
		}
		for (int i=0; i<colHeaders.size(); i++) {
			String t = (String) colHeaders.elementAt(i);
			Vector u = StringUtils.parseData(t, ' ');
			String val =(String) u.elementAt(0);
			String s = colData[i];
			if (objectPropertyLabel2CodeMap.containsKey(val)) {
				colData[i] = (String) objectPropertyLabel2CodeMap.get(val) + " " + (String) u.elementAt(1);

			} else if (annotationPropertyLabel2CodeMap.containsKey(val)) {
				colData[i] = (String) annotationPropertyLabel2CodeMap.get(val) + " " + (String) u.elementAt(1);
			}
		}
		return colData;
	}

    public static String dataReq2Header(Vector dataReq) {
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<dataReq.size(); i++) {
			String line = (String) dataReq.elementAt(i);
			if (dataReqTarget2SrcHashMap.containsKey(line)) {
				buf.append(line).append("\t");
			} else {
				Vector u = StringUtils.parseData(line, ' ');
				if (u.size() == 2) {
					String prop_code = (String) u.elementAt(0);
					if (annotationPropertyCode2LabelMap.containsKey(prop_code)) {
						String prop_label = (String) annotationPropertyCode2LabelMap.get(prop_code);
						buf.append(prop_label + " " + (String) u.elementAt(1) + "\t");
					} else if (objectPropertyCode2LabelMap.containsKey(prop_code)) {
						String prop_label = (String) objectPropertyCode2LabelMap.get(prop_code);
						buf.append(prop_label + " " + (String) u.elementAt(1) + "\t");
					}
				} else {
					buf.append(line).append("\t");
				}
			}
		}
		String t = buf.toString();
		t = t.substring(t.length()-1);
		return t;
	}

    public static void generateExcel(String dir, String excelfile, char delim) {
		Text2Excel.generateExcel(dir, excelfile, delim);
	}

}

