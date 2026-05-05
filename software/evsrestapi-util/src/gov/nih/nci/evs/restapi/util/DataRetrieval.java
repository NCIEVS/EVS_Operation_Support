package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;
import gov.nih.nci.evs.restapi.bean.*;

import java.io.*;
import java.text.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;
import java.nio.charset.Charset;
import org.apache.commons.text.*;

public class DataRetrieval {
    static String NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile;
	static String AXIOM_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.axiomfile;
	static String PARENT_CHILD_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile;

    static HierarchyHelper hh =  null;

    public HashMap propertyMap = null;
    public HashMap axiomMap = null;
    Vector properties = null;
    Vector annotationProperties = null;
    HashMap annotationPropertyCode2LabelMap = null;

    Vector objectProperties = null;
    HashMap objectPropertyCode2LabelMap = null;

    Vector dataVec = null;
    String owlfile = null;

    static {
		hh = new HierarchyHelper(Utils.readFile(PARENT_CHILD_FILE));
	}

	public static HierarchyHelper getHierarchyHelper() {
		return hh;
	}

	public static String getLabel(String code) {
		return hh.getLabel(code);
	}

	public static Vector getLabels(Vector codes) {
		Vector w = new Vector();
		for (int i=0; i<codes.size(); i++) {
			String code = (String) codes.elementAt(i);
			String label = hh.getLabel(code);
			w.add(label + "|" + code);
		}
		return w;
	}

    public DataRetrieval(String owlfile, Vector dataVec) {
		this.owlfile = owlfile;
		this.dataVec = dataVec;
		initialize();
	}

	public HashMap createCode2ValuesMap(String req) {
		Vector v = getAxiomData(req);//"P90|P384$NCI|P383$SY");
		HashMap hmap = new HashMap();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '\t');
			String code = (String) u.elementAt(0);
			String value = (String) u.elementAt(2);
			hmap.put(code, StringUtils.parseData(value, '|'));
	    }
	    return hmap;
	}

	public static HashMap createCode2ValuesHashMap(Vector v, String req) {
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

	public static HashMap getAxiomMap(Vector req_data_vec) {
		HashMap axiomMap = new HashMap();
		for (int k=0; k<req_data_vec.size(); k++) {
			String req_data = (String) req_data_vec.elementAt(k);
			Vector w = new Vector();
			Vector u0 = StringUtils.parseData(req_data, '|');
			String propCode = (String) u0.elementAt(0);

			Vector v = Utils.readFile(AXIOM_FILE);
			for (int i=0; i<v.size(); i++) {
				String line = (String) v.elementAt(i);
				Vector u = StringUtils.parseData(line, '|');
				String code = (String) u.elementAt(1);
				String prop_code = (String) u.elementAt(2);
				if (prop_code.compareTo(propCode) == 0) {
					boolean matched = true;
					if (u0.size() > 1) {
						for (int j=1; j<u0.size(); j++) {
							String t = (String) u0.elementAt(j);
							if (!u.contains(t)) {
								matched = false;
								break;
							}
						}
					}
					if (matched) {
						if (propCode.compareTo("P90") == 0) {
							HashMap hmap = new HashMap();
							if (axiomMap.containsKey(req_data)) {
								hmap = (HashMap) axiomMap.get(req_data);
							}
							Synonym syn = ParserUtils.axiom2Synonym(line);
							Vector axioms = new Vector();
							if (hmap.containsKey(code)) {
								axioms = (Vector) hmap.get(code);
							}
							axioms.add(syn);
							hmap.put(code, axioms);
							//axiomMap.put("P90", hmap);
							axiomMap.put(req_data, hmap);

						} else if (propCode.compareTo("P97") == 0) {
							HashMap hmap = new HashMap();
							if (axiomMap.containsKey(req_data)) {
								hmap = (HashMap) axiomMap.get(req_data);
							}
							Definition def = ParserUtils.axiom2Definition(line);
							Vector axioms = new Vector();
							if (hmap.containsKey(code)) {
								axioms = (Vector) hmap.get(code);
							}
							axioms.add(def);
							hmap.put(code, axioms);
							//axiomMap.put("P97", hmap);
							axiomMap.put(req_data, hmap);

						} else if (propCode.compareTo("P325") == 0) {
							HashMap hmap = new HashMap();
							if (axiomMap.containsKey(req_data)) {
								hmap = (HashMap) axiomMap.get(req_data);
							}
							AltDefinition altdef = ParserUtils.axiom2AltDefinition(line);
							Vector axioms = new Vector();
							if (hmap.containsKey(code)) {
								axioms = (Vector) hmap.get(code);
							}
							axioms.add(altdef);
							hmap.put(code, axioms);
							//axiomMap.put("P325", hmap);
							axiomMap.put(req_data, hmap);

						} else if (propCode.compareTo("P211") == 0) {
							HashMap hmap = new HashMap();
							if (axiomMap.containsKey(req_data)) {
								hmap = (HashMap) axiomMap.get(req_data);
							}
							GoAnnotation go = ParserUtils.axiom2GoAnnotation(line);
							Vector axioms = new Vector();
							if (hmap.containsKey(code)) {
								axioms = (Vector) hmap.get(code);
							}
							axioms.add(go);
							hmap.put(code, axioms);
							//axiomMap.put("P211", hmap);
							axiomMap.put(req_data, hmap);

						} else if (propCode.compareTo("P375") == 0) {
							HashMap hmap = new HashMap();
							if (axiomMap.containsKey(req_data)) {
								hmap = (HashMap) axiomMap.get(req_data);
							}
							MapToEntry entry = ParserUtils.axiom2MapToEntry(line);
							Vector axioms = new Vector();
							if (hmap.containsKey(code)) {
								axioms = (Vector) hmap.get(code);
							}
							axioms.add(entry);
							hmap.put(code, axioms);
							//axiomMap.put("P375", hmap);
							axiomMap.put(req_data, hmap);
						}
					}
				}
			}
		}
		return axiomMap;
	}

	public static HashMap getAxiomMap(String propCode) {
		String reqs = propCode;
		Vector w = DataRetrieval.extractRowsFromAxiomFile(reqs);
		HashMap hmap = new HashMap();
		for (int i=0; i<w. size(); i++) {
			String line = (String) w.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(1);
			if (propCode.compareTo("P90") == 0) {
				Synonym syn = ParserUtils.axiom2Synonym(line);
				Vector v = new Vector();
				if (hmap.containsKey(code)) {
					v = (Vector) hmap.get(code);
				}
				v.add(syn);
				hmap.put(code, v);
			} else if (propCode.compareTo("P97") == 0) {
				Definition def = ParserUtils.axiom2Definition(line);
				Vector v = new Vector();
				if (hmap.containsKey(code)) {
					v = (Vector) hmap.get(code);
				}
				v.add(def);
				hmap.put(code, v);
			} else if (propCode.compareTo("P325") == 0) {
				AltDefinition altdef = ParserUtils.axiom2AltDefinition(line);
				Vector v = new Vector();
				if (hmap.containsKey(code)) {
					v = (Vector) hmap.get(code);
				}
				v.add(altdef);
				hmap.put(code, v);
			} else if (propCode.compareTo("P211") == 0) {
				GoAnnotation go = ParserUtils.axiom2GoAnnotation(line);
				Vector v = new Vector();
				if (hmap.containsKey(code)) {
					v = (Vector) hmap.get(code);
				}
				v.add(go);
				hmap.put(code, v);
			} else if (propCode.compareTo("P375") == 0) {
				MapToEntry entry = ParserUtils.axiom2MapToEntry(line);
				Vector v = new Vector();
				if (hmap.containsKey(code)) {
					v = (Vector) hmap.get(code);
				}
				v.add(entry);
				hmap.put(code, v);
			}
		}
		return hmap;
	}

	public void initialize() {
		Vector properties = new Vector();
		Vector axiomVec = new Vector();
		for (int i=0; i<dataVec.size(); i++) {
			String line = (String) dataVec.elementAt(i);
			if (line.indexOf("|") == -1) {
				properties.add(line);
			} else {
				axiomVec.add(line);
			}
		}
		PropertyRetrieval propertyRetrieval = new PropertyRetrieval(owlfile, properties);
		propertyMap = propertyRetrieval.getPropertyMap();

		OWLScanner owlscanner = new OWLScanner(owlfile);
		annotationProperties = owlscanner.extractAnnotationProperties(owlscanner.get_owl_vec());
		annotationPropertyCode2LabelMap = new HashMap();
		for (int i=0; i<annotationProperties.size(); i++) {
			String line = (String) annotationProperties.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			annotationPropertyCode2LabelMap.put((String) u.elementAt(0),(String) u.elementAt(1));
		}

		objectProperties = owlscanner.extractObjectProperties(owlscanner.get_owl_vec());
		objectPropertyCode2LabelMap = new HashMap();
		for (int i=0; i<objectProperties.size(); i++) {
			String line = (String) objectProperties.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			objectPropertyCode2LabelMap.put((String) u.elementAt(0),(String) u.elementAt(1));
		}
		axiomMap = getAxiomMap(axiomVec);
	}

    public String getPropertyValues(String propCode, String code) {
		if (!propertyMap.containsKey(propCode)) return null;
		HashMap hmap = (HashMap) propertyMap.get(propCode);
		if (!hmap.containsKey(code)) return null;
		Vector values = (Vector) hmap.get(code);
		return getValues(values, code);
	}

    public String getAxiomValues(String propCode, String code) {
		if (!axiomMap.containsKey(propCode)) return null;
		HashMap hmap = (HashMap) axiomMap.get(propCode);
		if (!hmap.containsKey(code)) return null;
		Vector values = (Vector) hmap.get(code);
		return getValues(values, code);
	}

    public String getValues(Vector w, String code) {
		String s = null;
		StringBuffer buf = new StringBuffer();
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

	public String getAnnotationPropertyLabel(String propertyCode) {
		return (String) annotationPropertyCode2LabelMap.get(propertyCode);
	}

	public String getObjectPropertyLabel(String propertyCode) {
		return (String) objectPropertyCode2LabelMap.get(propertyCode);
	}

	public void exportPropertyData(String propertyCode) {
		if (!propertyMap.containsKey(propertyCode)) return;
		Utils.saveToFile("property_" + propertyCode + ".txt", getPropertyData(propertyCode));
	}

	public void exportAxiomData(String data_req) {
		if (!axiomMap.containsKey(data_req)) return;
		Utils.saveToFile(generateAxiomFileName(data_req), getAxiomData(data_req));
	}

	public Vector getPropertyData(String propertyCode) {
		Vector w0 = new Vector();
		if (!propertyMap.containsKey(propertyCode)) return w0;
		HashMap hmap = (HashMap) propertyMap.get(propertyCode);
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String code = (String) it.next();
			Vector w = (Vector) hmap.get(code);
			for (int i=0; i<w.size(); i++) {
				String value = (String) w.elementAt(i);
				w0.add(code + "\t" + propertyCode + "\t" + value);
			}
		}
		return w0;
	}

	public Vector getAxiomData(String data_req) {
		Vector w0 = new Vector();
		if (!axiomMap.containsKey(data_req)) return w0;
		HashMap hmap = (HashMap) axiomMap.get(data_req);
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String code = (String) it.next();
			Vector w = (Vector) hmap.get(code);
			String value = getValues(w, code);
			w0.add(code + "\t" + data_req + "\t" + value);
		}
		return w0;
	}

	public Vector getData(String data_req) {
		Vector w0 = new Vector();
		if (propertyMap.containsKey(data_req)) {
			return getPropertyData(data_req);
		} else if (axiomMap.containsKey(data_req)) {
			return getAxiomData(data_req);
		}
		return w0;
	}

	public static String generateAxiomFileName(String req_data) {
		Vector w = new Vector();
		Vector u0 = StringUtils.parseData(req_data, '|');
		String propCode = (String) u0.elementAt(0);
		StringBuffer buf = new StringBuffer();
		buf.append("axiom_" + propCode);
		for (int i=1; i<u0.size(); i++) {
			String t = (String) u0.elementAt(i);
			Vector u = StringUtils.parseData(t, '$');
			String s = (String) u.elementAt(1);
			buf.append("_").append(s);
		}
		String filename = buf.toString() + ".txt";
		return filename;
	}

	public static Vector retrieveData(Vector dataVec) {
		DataRetrieval test = new DataRetrieval(NCIT_OWL, dataVec);
        Vector w = new Vector();
        for (int i=0; i<dataVec.size(); i++) {
			String data_req = (String) dataVec.elementAt(i);
			w.addAll(test.getData(data_req));
		}
		return w;
	}

	public static HashMap generateMultivaluedPropMap(Vector dataReq) {
		char delim = '|';
		return generateMultivaluedPropMap(dataReq, delim);
	}

	public static HashMap generateMultivaluedPropMap(Vector dataReq, char delim) {
		Vector v = retrieveData(dataReq);
		HashMap hmap = new HashMap();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, delim);
			String src = (String) u.elementAt(0);
			String prop = (String) u.elementAt(1);
			String target = (String) u.elementAt(2);
			HashMap map = new HashMap();
			if (hmap.containsKey(prop)) {
				map = (HashMap) hmap.get(prop);
			}
			Vector w = new Vector();
			if (map.containsKey(src)) {
				w  = (Vector) map.get(src);
			}
			if (!w.contains(target)) {
				w.add(target);
			}
			map.put(src, w);
			hmap.put(prop, map);
		}
		return hmap;
	}

	public static HashMap vector2MultiValuedHashMap(Vector v) {
		int keyCol = 0;
		int valueCol = 1;
		return vector2MultiValuedHashMap(v, keyCol, valueCol);
	}

	public static HashMap vector2MultiValuedPropMap(Vector v) {
		return vector2MultiValuedHashMap(v, 0, 2);
	}

	public static HashMap vector2MultiValuedHashMap(Vector v, int keyCol, int valueCol) {
		char delim = '|';
		return vector2MultiValuedHashMap(v, keyCol, valueCol, delim);
	}

	public static HashMap vector2MultiValuedHashMap(Vector v, int keyCol, int valueCol, char delim) {
		HashMap hmap = new HashMap();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, delim);
			String key = (String) u.elementAt(keyCol);
			String value = (String) u.elementAt(valueCol);
			Vector w = new Vector();
			if (hmap.containsKey(key)) {
				w  = (Vector) hmap.get(key);
			}
			if (!w.contains(value)) {
				w.add(value);
			}
			hmap.put(key, w);
		}
		return hmap;
	}


	public static HashMap generateInverseHashMap(HashMap hmap) {
		HashMap invMap = new HashMap();
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			Vector values = (Vector) hmap.get(key);
			for (int i=0; i<values.size(); i++) {
				String value = (String) values.elementAt(i);
				Vector w = new Vector();
				if (invMap.containsKey(value)) {
					w = (Vector) invMap.get(value);
				}
				if (!w.contains(key)) {
					w.add(key);
				}
				invMap.put(value, w);
			}
		}
		return invMap;
	}

    public static Vector extractRowsFromAxiomFile(String reqs) {
		Vector w = new Vector();
		Vector req_vec = StringUtils.parseData(reqs, '|');
		Vector v = Utils.readFile(AXIOM_FILE);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			boolean bool = true;
			for (int j=0; j<req_vec.size(); j++) {
				String req = (String) req_vec.elementAt(j);
				if (!u.contains(req)) {
					bool = false;
					break;
				}
			}
			if (bool) {
				w.add(line);
			}
		}
		return new SortUtils().quickSort(w);
	}

    public static HashMap createTerm2CodesMap(String filename) {
		if (filename == null) {
			filename = AXIOM_FILE;
		}
		Vector v = Utils.readFile(filename);
		return createTerm2CodesMap(v);
	}


    public static HashMap createTerm2CodesMap(Vector v) {
		boolean caseSensitive = false;
		HashMap hmap = new HashMap();
        for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String prop_code = (String) u.elementAt(2);
			if (prop_code.compareTo("P90") == 0) {
				String term = (String) u.elementAt(3);
				term = HTMLDecoder.decode(term);
				String term_lc = term.toLowerCase();
				if (!caseSensitive) {
					term = term_lc;
				}

				Vector w = new Vector();
				if (hmap.containsKey(term)) {
					w = (Vector) hmap.get(term);
				}
				String code = (String) u.elementAt(1);
				if (!w.contains(code)) {
					w.add(code);
				}
				hmap.put(term, w);
			}
		}
		return hmap;
	}

    public static HashMap createPropValue2CodesMap(String filename, String propcode) {
		if (filename == null) {
			filename = AXIOM_FILE;
		}
		Vector v = Utils.readFile(filename);
		return createPropValue2CodesMap(v, propcode);
	}

    public static HashMap createPropValue2CodesMap(Vector v, String propcode) {
		boolean caseSensitive = false;
		HashMap hmap = new HashMap();
        for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(1);
			String prop_code = (String) u.elementAt(2);
			if (prop_code.compareTo(propcode) == 0) {
				String term = (String) u.elementAt(3);
				term = HTMLDecoder.decode(term);
				String term_lc = term.toLowerCase();
				if (!caseSensitive) {
					term = term_lc;
				}
				Vector w = new Vector();
				if (hmap.containsKey(term)) {
					w = (Vector) hmap.get(term);
				}
				if (!w.contains(code)) {
					w.add(code);
				}
				hmap.put(term, w);
			}
		}
		return hmap;
	}

	public static void axiomReport(String filename) { // contains a list of codes
		Vector warnings = new Vector();
		Vector codes = Utils.readFile(filename);
		HashMap fullsynMap = getAxiomMap("P90");
		HashMap defMap = getAxiomMap("P97");
		HashMap mapsToMap = getAxiomMap("P375");
		Vector w1 = new Vector();
		w1.add("Label\tCode\tTerm Name\tTerm Group\tTerm Source");
        Vector w2 = new Vector();
		for (int i=0; i<codes. size(); i++) {
			String code = (String) codes.elementAt(i);
			Vector syns = (Vector) fullsynMap.get(code);
			for (int j=0; j<syns.size(); j++) {
				Synonym syn = (Synonym) syns.elementAt(j);
				w2.add(syn.getLabel() + "\t" + syn.getCode() + "\t" + syn.getTermName() + "\t" + syn.getTermGroup() + "\t" + syn.getTermSource());
			}
		}
		w2 = new SortUtils().quickSort(w2);
		w1.addAll(w2);
		Utils.saveToFile("FULL_SYN_" + filename, w1);

		/////////////////////////////////////////////////////////////////////
		w1 = new Vector();
		w1.add("Label\tCode\tDefinition\tSource");

        w2 = new Vector();
		for (int i=0; i<codes. size(); i++) {
			String code = (String) codes.elementAt(i);
			Vector defs = (Vector) defMap.get(code);
			if (defs != null) {
				for (int j=0; j<defs.size(); j++) {
					Definition def = (Definition) defs.elementAt(j);
					w2.add(def.getLabel() + "\t" + def.getCode() + "\t" + def.getDescription() + "\t" + def.getSource());
				}
			} else {
				warnings.add("WARNING: No DEFINITION found for " + code);
			}
		}
		w2 = new SortUtils().quickSort(w2);
		w1.addAll(w2);
		Utils.saveToFile("DEFINITION_" + filename, w1);

		/////////////////////////////////////////////////////////////////////

		w1 = new Vector();
		w1.add("Label\tCode\tRelationship To Target\tTarget Code\tTarget Term\tTarget Term Type\tTartget Terminoogy\tTarget Terminology Version");

        w2 = new Vector();
		for (int i=0; i<codes. size(); i++) {
			String code = (String) codes.elementAt(i);
			Vector entries = (Vector) mapsToMap.get(code);
			if (entries != null) {
				for (int j=0; j<entries.size(); j++) {
					MapToEntry entry = (MapToEntry) entries.elementAt(j);
					w2.add(entry.getPreferredName() + "\t" + entry.getCode()
						+ "\t" + entry.getRelationshipToTarget()
						+ "\t" + entry.getTargetCode()
						+ "\t" + entry.getTargetTerm()
						+ "\t" + entry.getTargetTermType()
						+ "\t" + entry.getTargetTerminology()
						+ "\t" + entry.getTargetTerminologyVersion());
				}
			} else {
				warnings.add("INFO: No MapsTo found for " + code);
			}
		}
		w2 = new SortUtils().quickSort(w2);
		w1.addAll(w2);
		Utils.saveToFile("MapsTo_" + filename, w1);
		Utils.saveToFile("Warnings_" + filename, warnings);
	}

	public static void main(String[] args) {
        Vector dataVec = new Vector();
		dataVec.add("P108");
		dataVec.add("A5");
		dataVec.add("R108");
		dataVec.add("P90|P384$NCI|P383$PT");
		DataRetrieval test = new DataRetrieval(NCIT_OWL, dataVec);
        Vector w = new Vector();
        for (int i=0; i<dataVec.size(); i++) {
			String data_req = (String) dataVec.elementAt(i);
			w.addAll(test.getData(data_req));
		}
		Utils.saveToFile("test.txt", w);
	}
}
