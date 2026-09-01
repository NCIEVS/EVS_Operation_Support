package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.bean.*;
import gov.nih.nci.evs.restapi.config.*;
import gov.nih.nci.evs.restapi.common.*;
import java.io.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.*;
import org.json.*;

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
public class AxiomReader {
	static String PARENT_CHILD_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile; // "parent_child.txt";
    static HierarchyHelper hh = null;

    static {
		hh = new HierarchyHelper(Utils.readFile(PARENT_CHILD_FILE));
	}

	public static String getLabel(String code) {
		return hh.getLabel(code);
	}

    public static Synonym line2Synonym(String line) {
		line = line.trim();
		if (line.length() == 0) return null;
		Vector u = StringUtils.parseData(line, '|');
//C107239|P90|FACT Complex-targeting Curaxin CBL0137|P383$DN|P384$CTRP
		String code = (String) u.elementAt(0);
		String label = getLabel(code);
		String termName = (String) u.elementAt(2);
		String termGroup = null;
		String termSource = null;
		String sourceCode = null;
		String subSourceName = null;
		String subSourceCode = null;
		for (int i=3; i<u.size(); i++) {
			String t = (String) u.elementAt(i);
			Vector u2 = StringUtils.parseData(t, '$');
			String s1 = (String) u2.elementAt(0);
			String s2 = (String) u2.elementAt(1);
			if (s1.compareTo("P383") == 0) {
				termGroup = s2;
			} else if (s1.compareTo("P384") == 0) {
				termSource = s2;
			} else if (s1.compareTo("P385") == 0) {
				sourceCode = s2;
			} else if (s1.compareTo("P386") == 0) {
				subSourceName = s2;
			}
		}
        return new Synonym(
			code,
			label,
			termName,
			termGroup,
			termSource,
			sourceCode,
			subSourceName,
			subSourceCode);
	}

    public static Definition line2Definition(String line) {
		line = line.trim();
		if (line.length() == 0) return null;
		Vector u = StringUtils.parseData(line, '|');
		String code = (String) u.elementAt(0);
		String label = getLabel(code);
		String description = (String) u.elementAt(2);

		//String code = (String) u.elementAt(1);
		//String label = (String) u.elementAt(0);
		//String description = (String) u.elementAt(3);

		String attribution = "";
		String source = "";

		for (int i=3; i<u.size(); i++) {
			String t = (String) u.elementAt(i);
			Vector u2 = StringUtils.parseData(t, '$');
			String s1 = (String) u2.elementAt(0);
			String s2 = (String) u2.elementAt(1);
			if (s1.compareTo("P381") == 0) {
				attribution = s2;
			} else if (s1.compareTo("P378") == 0) {
				source = s2;
			}
		}
		return new Definition(
			code,
			label,
			description,
			attribution,
			source);
	}

    public static AltDefinition line2AltDefinition(String line) {
		line = line.trim();
		if (line.length() == 0) return null;
		Vector u = StringUtils.parseData(line, '|');

		String code = (String) u.elementAt(0);
		String label = getLabel(code);

		//String code = (String) u.elementAt(1);
		//String label = (String) u.elementAt(0);
		//String description = (String) u.elementAt(3);

		String prop_code = (String) u.elementAt(1);
		String description = (String) u.elementAt(2);
		String attribution = "";
		String source = "";

		for (int i=3; i<u.size(); i++) {
			String t = (String) u.elementAt(i);
			Vector u2 = StringUtils.parseData(t, '$');
			String s1 = (String) u2.elementAt(0);
			String s2 = (String) u2.elementAt(1);
			if (s1.compareTo("P381") == 0) {
				attribution = s2;
			} else if (s1.compareTo("P378") == 0) {
				source = s2;
			}
		}
		return new AltDefinition(
			code,
			label,
			description,
			attribution,
			source);
	}

    public static GoAnnotation line2GoAnnotation(String line) {
		line = line.trim();
		if (line.length() == 0) return null;
		Vector u = StringUtils.parseData(line, '|');
		//String code = (String) u.elementAt(1);
		//String label = (String) u.elementAt(0);

		String code = (String) u.elementAt(0);
		String label = getLabel(code);

		String prop_code = (String) u.elementAt(1);
		String annotation = (String) u.elementAt(2);

		String goEvi = "";
		String goId = "";
		String goSource = "";
		String sourceDate = "";

		for (int i=3; i<u.size(); i++) {
			String t = (String) u.elementAt(i);
			Vector u2 = StringUtils.parseData(t, '$');
			String s1 = (String) u2.elementAt(0);
			String s2 = (String) u2.elementAt(1);
			if (s1.compareTo("P389") == 0) {
				goEvi = s2;
			} else if (s1.compareTo("P387") == 0) {
				goId = s2;
			} else if (s1.compareTo("P390") == 0) {
				goSource = s2;
			} else if (s1.compareTo("P391") == 0) {
				sourceDate = s2;
			}
		}

		return new GoAnnotation(
			code,
			label,
			annotation,
			goEvi,
			goId,
			goSource,
			sourceDate
		);
	}

    public static MapToEntry line2MapToEntry(String line) {
		line = line.trim();
		if (line.length() == 0) return null;
		Vector u = StringUtils.parseData(line, '|');
		//String code = (String) u.elementAt(1);
		//String label = (String) u.elementAt(0);

		String code = (String) u.elementAt(0);
		String label = getLabel(code);

		String prop_code = (String) u.elementAt(1);

		String targetTerm = (String) u.elementAt(2);
		String targetCode = "";
		String targetTermType = "";
		String targetTerminology = "";
		String targetTerminologyVersion = "";
		String relationshipToTarget = "";

		for (int i=3; i<u.size(); i++) {
			String t = (String) u.elementAt(i);
			Vector u2 = StringUtils.parseData(t, '$');
			String s1 = (String) u2.elementAt(0);
			String s2 = (String) u2.elementAt(1);
			if (s1.compareTo("P393") == 0) {
				relationshipToTarget = s2;
			} else if (s1.compareTo("P394") == 0) {
				targetTermType = s2;
			} else if (s1.compareTo("P395") == 0) {
				targetCode = s2;
			} else if (s1.compareTo("P396") == 0) {
				targetTerminology = s2;
			} else if (s1.compareTo("P397") == 0) {
				targetTerminologyVersion = s2;
			}
		}
		return new MapToEntry(
		 code,
		 label,
		 relationshipToTarget,
		 targetCode,
		 targetTerm,
		 targetTermType,
		 targetTerminology,
		 targetTerminologyVersion);
	}

    public static HashMap loadSynonyms(String filename) {
		HashMap hmap = new HashMap();
		Vector v = Utils.readFile(filename);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(0);

			if (StringUtils.isNCItCode(code)) {
				String type = (String) u.elementAt(1);
				if (type.compareTo("P90") == 0) {
					Synonym syn = line2Synonym(line);
					Vector w = new Vector();
					if (hmap.containsKey(code)) {
						w = (Vector) hmap.get(code);
					}
					w.add(syn);
					hmap.put(code, w);
				}
			}
		}

		return hmap;
	}

	public static String getCode(Object obj){
		if (obj instanceof Synonym) {
			Synonym syn = (Synonym) obj;
			return syn.getCode();
		} else if (obj instanceof Definition) {
			Definition def = (Definition) obj;
			return def.getCode();
		} else if (obj instanceof AltDefinition) {
			AltDefinition def = (AltDefinition) obj;
			return def.getCode();
		} else if (obj instanceof GoAnnotation) {
			GoAnnotation go = (GoAnnotation) obj;
			return go.getCode();
		} else if (obj instanceof GoAnnotation) {
			MapToEntry e = (MapToEntry) obj;
			return e.getCode();
		}
		return null;
	}

	public static Object line2Object(String line) {
		Vector u = StringUtils.parseData(line, '|');
		String type = (String) u.elementAt(2);
		if (type.compareTo("P90") == 0) {
			return line2Synonym(line);
		} else if (type.compareTo("P97") == 0) {
			return line2Definition(line);
		} else if (type.compareTo("P325") == 0) {
			return line2AltDefinition(line);
		} else if (type.compareTo("P211") == 0) {
			return line2GoAnnotation(line);
		} else if (type.compareTo("P375") == 0) {
			return line2MapToEntry(line);
		}
		return null;
	}

    public static void printAxiomObject(Object obj) {
		if (obj != null) {
			if (obj instanceof Synonym) {
				Synonym syn = (Synonym) obj;
				System.out.println(syn.toJson());
			} else if (obj instanceof Definition) {
				Definition def = (Definition) obj;
				System.out.println(def.toJson());
			} else if (obj instanceof AltDefinition) {
				AltDefinition def = (AltDefinition) obj;
				System.out.println(def.toJson());
			} else if (obj instanceof MapToEntry) {
				MapToEntry entry = (MapToEntry) obj;
				System.out.println(entry.toJson());
			} else if (obj instanceof GoAnnotation) {
				GoAnnotation go = (GoAnnotation) obj;
				System.out.println(go.toJson());
			}
		}
	}

	public static HashMap loadAxioms(Vector v) {
		HashMap hmap = new HashMap();
		v = new SortUtils().quickSort(v);
		Vector w = new Vector();
		String id = "";
		List list = new ArrayList();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			id = (String) u.elementAt(0);
			list = new ArrayList();
			if (hmap.containsKey(id)) {
				list = (List) hmap.get(id);
			}
			Object obj = line2Object(line);
			list.add(obj);
            hmap.put(id, list);
		}
		return hmap;
	}


	public HashMap createAxiomHashMap(Vector v) {
		HashMap axiomHashMap = new HashMap();
		int colNum = 2;
		char delim = '|';
		Vector propCodes = DelimitedDataExtractor.retrieveDistinctColumnValues(v, colNum, delim);
		Utils.dumpVector("retrieveDistinctColumnValues", propCodes);
		for (int i=0; i<propCodes.size(); i++) {
			String propCode = (String) propCodes.elementAt(i);
			Vector w = DelimitedDataExtractor.retrieveColumnData(v, colNum, propCode, delim);
			HashMap hmap = loadAxioms(w);
			Iterator it = hmap.keySet().iterator();
			while (it.hasNext()) {
				String key = (String) it.next();
				List list = (List) hmap.get(key);
				for (int j=0; j<list.size(); j++) {
					Object obj = list.get(j);
					printAxiomObject(obj);
				}
			}
			axiomHashMap.put(propCode, hmap);
		}
		return axiomHashMap;
	}

	public void dumpAxiomHashMap(HashMap axiomHashMap) {
		Iterator it = axiomHashMap.keySet().iterator();
		while (it.hasNext()) {
			String propCode = (String) it.next();
			HashMap hmap = (HashMap) axiomHashMap.get(propCode);
			Iterator it2 = hmap.keySet().iterator();
			while (it2.hasNext()) {
				String key = (String) it2.next();
				List list = (List) hmap.get(key);
				for (int j=0; j<list.size(); j++) {
					Object obj = list.get(j);
					printAxiomObject(obj);
				}
			}
		}
	}

    public Vector line2AxiomStatements(String line) {
		Vector w = new Vector();
		Vector u = StringUtils.parseData(line, '|');
		String code = (String) u.elementAt(0);
		String propCode = (String) u.elementAt(1);
		String target = (String) u.elementAt(2);
		w.add("    <owl:Axiom>");
		w.add("        <owl:annotatedSource rdf:resource=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#" + code + "\"/>");
		w.add("        <owl:annotatedProperty rdf:resource=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#" + propCode + "\"/>");
		w.add("        <owl:annotatedTarget>" + target + "</owl:annotatedTarget>");

		for (int i=3; i<u.size(); i++) {
			String qualifier = (String) u.elementAt(i);
			Vector u2 = StringUtils.parseData(qualifier, '$');
			String qualifierCode = (String) u2.elementAt(0);
			String qualifierValue = (String) u2.elementAt(1);
			w.add("        <" + qualifierCode + ">" + qualifierValue + "</" + qualifierCode + ">");
	    }
		w.add("    </owl:Axiom>");
		return w;
	}

    public void main(String[] args) {
		long ms = System.currentTimeMillis();
        String filename = args[0];
		HashMap hmap = loadAxioms(Utils.readFile(filename));
		System.out.println(hmap.keySet().size());
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String code = (String) it.next();
			List list = (List) hmap.get(code);
			for (int i=0; i<list.size(); i++) {
				Object obj = list.get(i);
				if (obj instanceof Definition) {
					Definition def = (Definition) obj;
					System.out.println(def.toJson());
				}
			}
		}
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
    }
}
