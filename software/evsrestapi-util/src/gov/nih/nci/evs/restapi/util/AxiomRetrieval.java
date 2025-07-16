package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;
import gov.nih.nci.evs.restapi.bean.*;
import java.io.*;
import java.util.*;

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
public class AxiomRetrieval {

    static String NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile; //"ThesaurusInferred_forTS.owl";
	static String PARENT_CHILD_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile; // "parent_child.txt";
	static String RESTRICTION_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.rolefile; //"roles.txt";
	static String AXIOM_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.axiomfile;

    static HashMap propertyMap = null;
    static HashMap axiomMap = null;
    static HashSet retiredConcepts = new HashSet();

    static String REQUIRED_DATA_FILE = ConfigurationController.requiredDataFile;
    static Vector req_data_vec = null;

//String req_data = "P90|P384$NCI|P383$PT";
//Recombinant Amphiregulin|C1000|P90|KAF|P383$AB|P384$NCI

    static {
		long ms = System.currentTimeMillis();
		OWLScanner owlScanner = new OWLScanner(NCIT_OWL);
		retiredConcepts = owlScanner.createRetiredConceptSet();
		System.out.println("retiredConcepts: " + retiredConcepts.size());
		propertyMap = owlScanner.getPropertyMap(owlScanner.get_owl_vec());
		//A8Map = (HashMap) propertyMap.get("A8");
		System.out.println("propertyMap: " + propertyMap.keySet().size());
        File f = new File(REQUIRED_DATA_FILE);
        if (f.exists()) {
			req_data_vec = Utils.readFile(REQUIRED_DATA_FILE);
			axiomMap = getAxiomMap(req_data_vec);
		} else {
			System.out.println(REQUIRED_DATA_FILE + " does not exist.");
			axiomMap = getAxiomMap();
		}
		System.out.println("axiomMap: " + axiomMap.keySet().size());
		System.out.println("Total initialization time (ms): " + (System.currentTimeMillis() - ms));
	}

	public static HashSet getRetiredConcepts() {
		if (retiredConcepts != null) return retiredConcepts;
		OWLScanner owlScanner = new OWLScanner(NCIT_OWL);
		HashSet retiredConcepts = owlScanner.createRetiredConceptSet();
		System.out.println("retiredConcepts: " + retiredConcepts.size());
		return retiredConcepts;
	}

	public static HashMap getPropertyMap() {
		if (propertyMap != null) return propertyMap;
		OWLScanner owlScanner = new OWLScanner(NCIT_OWL);
		HashMap propertyMap = owlScanner.getPropertyMap(owlScanner.get_owl_vec());
        System.out.println("propertyMap: " + propertyMap.keySet().size());
		return propertyMap;
	}

	public static HashMap getAxiomMap() {
		HashMap axiomMap = new HashMap();
		Vector w = new Vector();
		Vector v = Utils.readFile(AXIOM_FILE);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(1);
			String propCode = (String) u.elementAt(2);

			if (propCode.compareTo("P90") == 0) {
				line = HTMLDecoder.decode(line);
				HashMap hmap = new HashMap();
				if (axiomMap.containsKey("P90")) {
					hmap = (HashMap) axiomMap.get("P90");
				}
				Synonym syn = ParserUtils.axiom2Synonym(line);
				Vector axioms = new Vector();
				if (hmap.containsKey(code)) {
					axioms = (Vector) hmap.get(code);
				}
				axioms.add(syn);
				hmap.put(code, axioms);
				axiomMap.put("P90", hmap);

			} else if (propCode.compareTo("P97") == 0) {
				HashMap hmap = new HashMap();
				if (axiomMap.containsKey("P97")) {
					hmap = (HashMap) axiomMap.get("P97");
				}
				Definition def = ParserUtils.axiom2Definition(line);
				Vector axioms = new Vector();
				if (hmap.containsKey(code)) {
					axioms = (Vector) hmap.get(code);
				}
				axioms.add(def);
				hmap.put(code, axioms);
				axiomMap.put("P97", hmap);

			} else if (propCode.compareTo("P325") == 0) {
				HashMap hmap = new HashMap();
				if (axiomMap.containsKey("P325")) {
					hmap = (HashMap) axiomMap.get("P325");
				}
				AltDefinition altdef = ParserUtils.axiom2AltDefinition(line);
				Vector axioms = new Vector();
				if (hmap.containsKey(code)) {
					axioms = (Vector) hmap.get(code);
				}
				axioms.add(altdef);
				hmap.put(code, axioms);
				axiomMap.put("P325", hmap);

			} else if (propCode.compareTo("P211") == 0) {
				HashMap hmap = new HashMap();
				if (axiomMap.containsKey("P211")) {
					hmap = (HashMap) axiomMap.get("P211");
				}
				GoAnnotation go = ParserUtils.axiom2GoAnnotation(line);
				Vector axioms = new Vector();
				if (hmap.containsKey(code)) {
					axioms = (Vector) hmap.get(code);
				}
				axioms.add(go);
				hmap.put(code, axioms);
				axiomMap.put("P211", hmap);

			} else if (propCode.compareTo("P375") == 0) {
				HashMap hmap = new HashMap();
				if (axiomMap.containsKey("P375")) {
					hmap = (HashMap) axiomMap.get("P375");
				}
				MapToEntry entry = ParserUtils.axiom2MapToEntry(line);
				Vector axioms = new Vector();
				if (hmap.containsKey(code)) {
					axioms = (Vector) hmap.get(code);
				}
				axioms.add(entry);
				hmap.put(code, axioms);
				axiomMap.put("P375", hmap);
			}
		}
		return axiomMap;
	}

	public static HashMap getAxiomMap(Vector req_data_vec) {
		HashMap axiomMap = new HashMap();
		Vector v = Utils.readFile(AXIOM_FILE);
		for (int k=0; k<req_data_vec.size(); k++) {
			String req_data = (String) req_data_vec.elementAt(k);
			Vector w = new Vector();
			Vector u0 = StringUtils.parseData(req_data, '|');
			String propCode = (String) u0.elementAt(0);
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
						line = HTMLDecoder.decode(line);
						if (propCode.compareTo("P90") == 0) {
							HashMap hmap = new HashMap();
							if (axiomMap.containsKey("P90")) {
								hmap = (HashMap) axiomMap.get("P90");
							}
							Synonym syn = ParserUtils.axiom2Synonym(line);
							Vector axioms = new Vector();
							if (hmap.containsKey(code)) {
								axioms = (Vector) hmap.get(code);
							}
							axioms.add(syn);
							hmap.put(code, axioms);
							axiomMap.put("P90", hmap);

						} else if (propCode.compareTo("P97") == 0) {
							HashMap hmap = new HashMap();
							if (axiomMap.containsKey("P97")) {
								hmap = (HashMap) axiomMap.get("P97");
							}
							Definition def = ParserUtils.axiom2Definition(line);
							Vector axioms = new Vector();
							if (hmap.containsKey(code)) {
								axioms = (Vector) hmap.get(code);
							}
							axioms.add(def);
							hmap.put(code, axioms);
							axiomMap.put("P97", hmap);

						} else if (propCode.compareTo("P325") == 0) {
							HashMap hmap = new HashMap();
							if (axiomMap.containsKey("P325")) {
								hmap = (HashMap) axiomMap.get("P325");
							}
							AltDefinition altdef = ParserUtils.axiom2AltDefinition(line);
							Vector axioms = new Vector();
							if (hmap.containsKey(code)) {
								axioms = (Vector) hmap.get(code);
							}
							axioms.add(altdef);
							hmap.put(code, axioms);
							axiomMap.put("P325", hmap);

						} else if (propCode.compareTo("P211") == 0) {
							HashMap hmap = new HashMap();
							if (axiomMap.containsKey("P211")) {
								hmap = (HashMap) axiomMap.get("P211");
							}
							GoAnnotation go = ParserUtils.axiom2GoAnnotation(line);
							Vector axioms = new Vector();
							if (hmap.containsKey(code)) {
								axioms = (Vector) hmap.get(code);
							}
							axioms.add(go);
							hmap.put(code, axioms);
							axiomMap.put("P211", hmap);

						} else if (propCode.compareTo("P375") == 0) {
							HashMap hmap = new HashMap();
							if (axiomMap.containsKey("P375")) {
								hmap = (HashMap) axiomMap.get("P375");
							}
							MapToEntry entry = ParserUtils.axiom2MapToEntry(line);
							Vector axioms = new Vector();
							if (hmap.containsKey(code)) {
								axioms = (Vector) hmap.get(code);
							}
							axioms.add(entry);
							hmap.put(code, axioms);
							axiomMap.put("P375", hmap);
						}
					}
				}
			}
		}
		return axiomMap;
	}

    public static String getPropertyValues(Vector w, String code) {
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

	public static void test(String code) {
		Iterator it = propertyMap.keySet().iterator();
		while (it.hasNext()) {
			String propCode = (String) it.next();
			HashMap hmap = (HashMap) propertyMap.get(propCode);
			Vector values = (Vector) hmap.get(code);
			if (values != null) {
				Utils.dumpVector(code + " " + propCode, values);
			}
		}

		Vector w = (Vector) axiomMap.get(code);
		for (int j=0; j<w.size(); j++) {
			Object obj = w.elementAt(j);
			if (obj instanceof String) {
				//String value = (String) obj;
				//buf.append(value).append("|");
				System.out.println(obj.toString());
			} else if (obj instanceof Synonym) {
				Synonym syn = (Synonym) obj;
				//String value = syn.getTermName();
				//buf.append(value).append("|");
				System.out.println(syn.toJson());
			} else if (obj instanceof Definition) {
				Definition def = (Definition) obj;
				//String value = def.getDescription();
				//buf.append(value).append("|");
				System.out.println(def.toJson());
			} else if (obj instanceof AltDefinition) {
				AltDefinition def = (AltDefinition) obj;
				//String value = def.getDescription();
				//buf.append(value).append("|");
				System.out.println(def.toJson());
			} else if (obj instanceof GoAnnotation) {
				GoAnnotation go = (GoAnnotation) obj;
				//String value = go.getAnnotation();
				//buf.append(value).append("|");
				System.out.println(go.toJson());
			} else if (obj instanceof MapToEntry) {
				MapToEntry entry = (MapToEntry) obj;
				//String value = go.getAnnotation();
				//buf.append(value).append("|");
				System.out.println(entry.toJson());
			}
		}
	}


	public static void main(String[] args) {
		//long ms = System.currentTimeMillis();
        String code = "C123456";
        test(code);
		//System.out.println("Total initialization time (ms): " + (System.currentTimeMillis() - ms));

	}
}