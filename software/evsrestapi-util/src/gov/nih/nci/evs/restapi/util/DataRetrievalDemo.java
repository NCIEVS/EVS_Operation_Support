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

public class DataRetrievalDemo {
    static String NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile;
	static String PARENT_CHILD_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile;
	static String A8_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.subsetfile;
	static HashMap inverse_a8Map = null;

	public static HashMap create_subset_hmap() {
		Vector association_vec = Utils.readFile(A8_FILE);
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

    public static Vector heading2DataReq(String delimitedHeading, char delim) {
		Vector w = StringUtils.parseData(delimitedHeading, delim);
		Vector v = new Vector();
		//v.add("Code\tNCI PT\tParent Code\tParent NCI PT\tNCI SY\tContributing_Source");
		for (int i=0; i<w.size(); i++) {
			String s = (String) w.elementAt(i);
			if (s.compareToIgnoreCase("Code") == 0) {
				v.add("code");
			} else if (s.compareToIgnoreCase("NCI PT") == 0) {
				v.add("P90|P384$NCI|P383$PT");
			} else if (s.compareToIgnoreCase("NCI SY") == 0) {
				v.add("P90|P384$NCI|P383$SY");
			} else if (s.compareToIgnoreCase("Contributing_Source") == 0) {
				v.add("P322");
			} else {

			}
		}
		return v;
	}

	public static void main(String[] args) {
        HierarchyHelper hh = null;
        HashMap inverse_a8Map = null;
 		String mode_str = args[0];
 		int mode = Integer.parseInt(mode_str);
 		if (mode == ExactMatch.BRANCH) {
			hh = DataRetrieval.getHierarchyHelper();
		} else if (mode == ExactMatch.SUBSET) {
            inverse_a8Map = create_subset_hmap();
		}
		System.out.println("mode " + mode);

		String delimitedHeading = args[1];
		System.out.println("delimitedHeading " + delimitedHeading);
		//"Code\tNCI PT\tParent Code\tParent NCI PT\tNCI SY\tContributing_Source"
        Vector dataVec = heading2DataReq(delimitedHeading, '|');
        Utils.dumpVector(delimitedHeading, dataVec);
        DataRetrieval test = new DataRetrieval(NCIT_OWL, dataVec);

		String root_str = args[2];
		System.out.println("root_str " + root_str);
		Vector roots = StringUtils.parseData(root_str, '|');
		Vector v = new Vector();
		Vector w = null;
		Vector col_labels = StringUtils.parseData(delimitedHeading, '|');
		for (int i=0; i<roots.size(); i++) {
			String root = (String) roots.elementAt(i);
			System.out.println("root: " + root);
			v = new Vector();
			v.add(delimitedHeading);
			if (mode == ExactMatch.BRANCH) {
			    w = hh.get_transitive_closure_v3(root);
			} else if (mode == ExactMatch.SUBSET) {
			    w = (Vector) inverse_a8Map.get(root);
			}
			System.out.println("w.size(): " + w.size());

			for (int lcv=0; lcv<w.size(); lcv++) {
				String code = (String) w.elementAt(lcv);
				StringBuffer buf = new StringBuffer();
				for (int j=0; j<col_labels.size(); j++) {
					String col_label = (String) col_labels.elementAt(j);
					if (col_label.compareToIgnoreCase("code") == 0) {
						buf.append(code).append("\t");
					} else if (col_label.compareTo("NCI PT") == 0) {
						String nci_pt = test.getAxiomValues("P90|P384$NCI|P383$PT", code);
						buf.append(nci_pt).append("\t");
					} else if (col_label.compareTo("NCI SY") == 0) {
						String nci_sy = test.getAxiomValues("P90|P384$NCI|P383$SY", code);
						if (nci_sy == null) {
							nci_sy = "";
						}
						buf.append(nci_sy).append("\t");
					} else if (col_label.compareToIgnoreCase("Parent Code") == 0) {
						Vector parent_codes = hh.getSuperclassCodes(code);
						String parent_code_str = Utils.vector2Delimited(parent_codes, "|");
						buf.append(parent_code_str).append("\t");
					} else if (col_label.compareToIgnoreCase("Parent NCI PT") == 0) {
						Vector parent_codes = hh.getSuperclassCodes(code);
						Vector parent_pt_vec = new Vector();
						if (parent_codes != null && parent_codes.size()> 0) {
							for (int k=0; k<parent_codes.size(); k++) {
								String parent_code = (String) parent_codes.elementAt(k);
								String parent_pt = test.getAxiomValues("P90|P384$NCI|P383$PT", parent_code);
								parent_pt_vec.add(parent_pt);
							}
						}
						String parent_pts = Utils.vector2Delimited(parent_pt_vec, "|");
						buf.append(parent_pts).append("\t");
					} else if (col_label.compareToIgnoreCase("Contributing_Source") == 0) {
						String cs = test.getPropertyValues("P322", code);
						if (cs == null || cs.compareTo("null") == 0) {
							cs = "";
						}
						buf.append(cs).append("\t");
					}
				}
				String t = buf.toString();
				if (t.endsWith("\t")) {
					t = t.substring(0, t.length()-1);
				}
				v.add(t);
			}
			Utils.saveToFile(root + ".txt", v);
		}
	}
}
