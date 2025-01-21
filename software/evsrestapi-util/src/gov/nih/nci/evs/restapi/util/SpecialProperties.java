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

		if (code == null) {
			String termSource = "CDISC";
			String termType = "PT";
			String sourceCode = null;
			String subsourceName = null;
			List<String> cdisc_pts = getSynonymsWithQualifiers(parentAxioms,
			   termSource, termType, sourceCode, subsourceName);

			if (cdisc_pts == null || cdisc_pts.size() == 0) {
				System.out.println("\nINFO: No CDISC PT is found.");
				return null;
			} else if (cdisc_pts.size() == 1) {
				return cdisc_pts.get(0);
			}
		}

		String termSource = "CDISC";
		String termType = "PT";
		String sourceCode = null;
		String subsourceName = null;
	    List<String> cdisc_pts = getSynonymsWithQualifiers(axioms,
	       termSource, termType, sourceCode, subsourceName);

	    if (cdisc_pts == null || cdisc_pts.size() == 0) {
			System.out.println("\nINFO: No CDISC PT is found.");
			return null;
		} else if (cdisc_pts.size() == 1) {
			return cdisc_pts.get(0);
		}

		termSource = "NCI";
		termType = "AB";
		sourceCode = null;
		subsourceName = null;
	    List<String> nci_abs = getSynonymsWithQualifiers(parentAxioms,
	       termSource, termType, sourceCode, subsourceName);
	    if (nci_abs == null || nci_abs.size() == 0) {
			System.out.println("\nINFO: No NCI AB is found in " + codelistCode);
			return null;
		}

	    String nci_ab = nci_abs.get(0);
		termSource = "CDISC";
		termType = "PT";
		sourceCode = nci_ab;
		subsourceName = null;
	    List<String> results = getSynonymsWithQualifiers(axioms,
	       termSource, termType, sourceCode, subsourceName);
	    return results.get(0);
	}

	public static String getCodeListName(String parentCode, List<Synonym> parentAxioms) {
		String termSource = "CDISC";
		String termType = "SY";
		String sourceCode = null;
		String subsourceName = null;
	    List<String> cdisc_sys = getSynonymsWithQualifiers(parentAxioms,
	       termSource, termType, sourceCode, subsourceName);

	    if (cdisc_sys == null || cdisc_sys.size() == 0) {
			System.out.println("\nINFO: No CDISC SY is found in " + parentCode);
			return null;
		} else if (cdisc_sys.size() == 1) {
			return cdisc_sys.get(0);
		} else {
			System.out.println("\nINFO: Multiple CDISC SYs are found in " + parentCode);
			return cdisc_sys.get(0);
		}
	}


}