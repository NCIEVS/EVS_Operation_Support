package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.bean.*;

import java.io.*;
import java.text.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;
import java.nio.charset.Charset;

public class SpecialProperties {
	SpecialProperties() {

	}

	public static List vector2List(Vector v) {
		List list = new ArrayList();
		for (int i=0; i<v.size(); i++) {
			Object obj = v.elementAt(i);
			list.add(obj);
		}
		return list;
	}

	public static List<String> getSynonymsWithQualifiers(List<Synonym> axioms,
	    String termSource, String termType, String sourceCode, String subsourceName) {
        if (axioms == null) {
			System.out.println("ERROR calling getSynonymsWithQualifiers: List<Synonym> axioms is NULL.");
			return null;
		}
		List<String> list = new ArrayList<String>();
		for (int i=0; i<axioms.size(); i++) {
			Synonym synonym = axioms.get(i);
            boolean match = true;
			if (termSource != null) {
				if (synonym.getTermSource() == null || synonym.getTermSource().compareTo("null") == 0) {
					match = false;
				} else if (synonym.getTermSource().compareTo(termSource) != 0) {
					match = false;
				}
			}
			if (termType != null) {
				if (synonym.getTermGroup() == null || synonym.getTermGroup().compareTo("null") == 0) {
					match = false;
				} else if (synonym.getTermGroup().compareTo(termType) != 0) {
					match = false;
				}
			}
			if (sourceCode != null) {
				if (synonym.getSourceCode() == null || synonym.getSourceCode().compareTo("null") == 0) {
					match = false;
				} else if (synonym.getSourceCode().compareTo(sourceCode) != 0) {
					match = false;
				}
			}

			if (subsourceName != null) {
				if (synonym.getSubSourceName() == null || synonym.getSubSourceName().compareTo("null") == 0) {
					match = false;
				} else if (synonym.getSubSourceName().compareTo(subsourceName) != 0) {
					match = false;
				}
			}

			if (match) {
				list.add(synonym.getTermName());
			}
		}
		return list;
	}

	public static String getCDISCSubmissionValue(String codelistCode, List<Synonym> parentAxioms, String code, List<Synonym> axioms) {
		String termSource = "CDISC";
		return getCDISCSubmissionValue(codelistCode, parentAxioms, code, axioms, termSource);
	}

	public static String getCDISCSubmissionValue(String codelistCode, List<Synonym> parentAxioms, String code, List<Synonym> axioms, String termSource) {
		String subsourceName = null;
	    return getCDISCSubmissionValue(codelistCode, parentAxioms, code, axioms, termSource, subsourceName);
	}

	public static String getCDISCSubmissionValue(String codelistCode, List<Synonym> parentAxioms, String code, List<Synonym> axioms, String termSource, String subsourceName) {
		if (code == null) {
			String termType = "PT";
			String sourceCode = null;
			List<String> cdisc_pts = null;
			try {
				cdisc_pts = getSynonymsWithQualifiers(parentAxioms,
				   termSource, termType, sourceCode, null);
				if (cdisc_pts == null) {
					System.out.println("WARNING: getSynonymsWithQualifiers returns null.");
					return null;
				}
		    } catch (Exception e) {
				System.out.println("\nINFO (0): getCDISCSubmissionValue No " + termSource + " PT is found for (codelistCode " + codelistCode + ", code " + code + ")");
				return null;
			}

			if (cdisc_pts == null || cdisc_pts.size() == 0) {
				System.out.println("\nINFO (1): getCDISCSubmissionValue No " + termSource + " PT is found for (codelistCode " + codelistCode + ", code " + code + ")");
				return null;
			} else if (cdisc_pts.size() == 1) {
				return cdisc_pts.get(0);
			}
		}
		String termType = "PT";
		String sourceCode = null;
	    List<String> cdisc_pts = getSynonymsWithQualifiers(axioms,
	       termSource, termType, sourceCode, null);

	    if (cdisc_pts == null || cdisc_pts.size() == 0) {
			System.out.println("\nINFO (2): getCDISCSubmissionValue No " + termSource + " PT is found for (codelistCode " + codelistCode + ", code " + code + ")");
			return null;
		} else if (cdisc_pts.size() == 1) {
			return cdisc_pts.get(0);
		}

		termSource = "NCI";
		termType = "AB";

		sourceCode = null;
	    List<String> nci_abs = getSynonymsWithQualifiers(parentAxioms,
	       termSource, termType, sourceCode, null);
	    if (nci_abs == null || nci_abs.size() == 0) {
			System.out.println("\nINFO (3): getCDISCSubmissionValue No NCI AB is found in (codelistCode " + codelistCode + ", code " + code + ")");
			return null;
		}

	    String nci_ab = nci_abs.get(0);

		termType = "PT";
		sourceCode = nci_ab;

		if (subsourceName.compareTo("M11") == 0) {
			termSource = "ICH";
		}

		subsourceName = null;
	    List<String> results = getSynonymsWithQualifiers(axioms,
	       termSource, termType, sourceCode, subsourceName);
	    if (results == null) {
			System.out.println("\nINFO (4): getCDISCSubmissionValue No NCI AB is found in (codelistCode " + codelistCode + ", code " + code + ")");
			return null;
		}
	    return results.get(0);
	}

	public static String getCodeListName(String parentCode, List<Synonym> parentAxioms) {
		String termSource = "CDISC";
		String subsourceName = null;
		return getCodeListName(parentCode, parentAxioms, termSource, subsourceName);
	}

	public static String getCodeListName(String parentCode, List<Synonym> parentAxioms, String termSource) {
		String subsourceName = null;
		return getCodeListName(parentCode, parentAxioms, termSource, subsourceName);
	}

	public static String getCodeListName(String parentCode, List<Synonym> parentAxioms, String termSource, String subsourceName) {
		String termType = "SY";
		String sourceCode = null;
	    List<String> cdisc_sys = getSynonymsWithQualifiers(parentAxioms,
	       termSource, termType, sourceCode, subsourceName);

	    if (cdisc_sys == null || cdisc_sys.size() == 0) {
			System.out.println("\nINFO: getCodeListName No " + termSource + " SY is found in " + parentCode);
			return null;
		} else if (cdisc_sys.size() == 1) {
			return cdisc_sys.get(0);
		} else {
			System.out.println("\nINFO: getCodeListName Multiple " + termSource + " SYs are found in " + parentCode);
			return cdisc_sys.get(0);
		}
	}

}

