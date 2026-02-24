package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;

import java.io.*;
import java.util.*;

public class NCItMetadata {
	static HashMap propertyCode2LabelHashMap = null;
	static HashMap propertyLabel2CodeHashMap = null;

	static HashMap roleCode2LabelHashMap = null;
	static HashMap roleLabel2CodeHashMap = null;

	static HashMap associationCode2LabelHashMap = new HashMap();
	static HashMap associationLabel2CodeHashMap = new HashMap();

	static HashMap propertyQualifierCodeHashMap = null;
	static HashMap propertyQualifierLabelHashMap = null;

	public static HashMap getPropertyCode2LabelHashMap() {
		return propertyCode2LabelHashMap;
	}

	public static HashMap getPropertyLabel2CodeHashMap() {
		return propertyLabel2CodeHashMap;
	}

	public static HashMap getRoleCode2LabelHashMap() {
		return roleCode2LabelHashMap;
	}

	public static HashMap getRoleLabel2CodeHashMap() {
		return roleLabel2CodeHashMap;
	}

	public static HashMap getAssociationCode2LabelHashMap() {
		return associationCode2LabelHashMap;
	}

	public static HashMap getAssociationLabel2CodeHashMap() {
		return associationLabel2CodeHashMap;
	}

	public static HashMap getPropertyQualifierCodeHashMap() {
		return propertyQualifierCodeHashMap;
	}

	public static HashMap getPropertyQualifierLabelHashMap() {
		return propertyQualifierLabelHashMap;
	}

	static {
		String serviceUrl = ConfigurationController.serviceUrl;
		String named_graph = ConfigurationController.namedGraph;
		String username = ConfigurationController.username;
		String password = ConfigurationController.password;
		OWLSPARQLUtils owlSPARQLUtils = null;
	    owlSPARQLUtils = new OWLSPARQLUtils(serviceUrl, username, password);
        owlSPARQLUtils.set_named_graph(named_graph);

        try {
			Vector v = owlSPARQLUtils.getSupportedProperties(named_graph);
			propertyCode2LabelHashMap = new HashMap();
			propertyLabel2CodeHashMap = new HashMap();
			//ALT_DEFINITION|P325
			for (int i=0; i<v.size(); i++) {
				String line = (String) v.elementAt(i);
				Vector u = StringUtils.parseData(line, '|');
				String label = (String) u.elementAt(0);
				String code = (String) u.elementAt(1);
				propertyCode2LabelHashMap.put(code, label);
				propertyLabel2CodeHashMap.put(label, code);
			}

			v = owlSPARQLUtils.getSupportedRoles(named_graph);
			roleCode2LabelHashMap= new HashMap();
			roleLabel2CodeHashMap = new HashMap();
			//Allele_Absent_From_Wild-type_Chromosomal_Location|R156
			for (int i=0; i<v.size(); i++) {
				String line = (String) v.elementAt(i);
				Vector u = StringUtils.parseData(line, '|');
				String label = (String) u.elementAt(0);
				String code = (String) u.elementAt(1);
				roleCode2LabelHashMap.put(code, label);
				roleLabel2CodeHashMap.put(label, code);
			}

			v = owlSPARQLUtils.getSupportedAssociations(named_graph);
			associationCode2LabelHashMap = new HashMap();
			associationLabel2CodeHashMap = new HashMap();
			//Has_ACC-AHA_SARS2_Authorized_Value|A48
			for (int i=0; i<v.size(); i++) {
				String line = (String) v.elementAt(i);
				Vector u = StringUtils.parseData(line, '|');
				String label = (String) u.elementAt(0);
				String code = (String) u.elementAt(1);
				associationCode2LabelHashMap.put(code, label);
				associationLabel2CodeHashMap.put(label, code);
			}

			//ALT_DEFINITION|P325|attribution|P381
			v = owlSPARQLUtils.getSupportedPropertyQualifiers(named_graph);
			propertyQualifierCodeHashMap = new HashMap();
			propertyQualifierLabelHashMap = new HashMap();
			for (int i=0; i<v.size(); i++) {
				String line = (String) v.elementAt(i);
				Vector u = StringUtils.parseData(line, '|');
				String prop_label = (String) u.elementAt(0);
				String prop_code = (String) u.elementAt(1);
				String qual_label = (String) u.elementAt(2);
				String qual_code = (String) u.elementAt(3);
				Vector w = new Vector();
				if (propertyQualifierLabelHashMap.containsKey(prop_label)) {
					w = (Vector) propertyQualifierLabelHashMap.get(prop_label);
				}
				w.add(qual_label);
				if (qual_label.compareTo("Source Code") == 0) {
					w.add("subsource_code");
				}
				propertyQualifierLabelHashMap.put(prop_label, w);
				w = new Vector();
				if (propertyQualifierCodeHashMap.containsKey(prop_code)) {
					w = (Vector) propertyQualifierCodeHashMap.get(prop_code);
				}
				w.add(qual_code);
				if (qual_label.compareTo("Source Code") == 0) {
					w.add("P385");
				}
				propertyQualifierCodeHashMap.put(prop_code, w);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
