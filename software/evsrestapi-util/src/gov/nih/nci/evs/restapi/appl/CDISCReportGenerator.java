package gov.nih.nci.evs.restapi.appl;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.bean.*;
import gov.nih.nci.evs.restapi.common.*;
import gov.nih.nci.evs.restapi.config.*;
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
public class CDISCReportGenerator {
    String named_graph = null;
    String prefixes = null;
    String serviceUrl = null;
    String username = null;
    String password = null;
    OWLSPARQLUtils owlSPARQLUtils = null;
    public static String HEADING = "Code	Codelist Code	Codelist Extensible (Yes/No)	Codelist Name	CDISC Submission Value	CDISC Synonym(s)	CDISC Definition	NCI Preferred Term";

    public CDISCReportGenerator(String serviceUrl, String named_graph, String username, String password) {
		this.serviceUrl = serviceUrl;
    	this.named_graph = named_graph;
    	this.username = username;
    	this.password = password;
    	initialize();
    }

    public void initialize() {
		owlSPARQLUtils = new OWLSPARQLUtils(serviceUrl, username, password);
		owlSPARQLUtils.set_named_graph(named_graph);
	}

	public String getPrefixes() {
		return owlSPARQLUtils.getPrefixes();
	}

	public Vector executeQuery(String query) {
		return owlSPARQLUtils.executeQuery(query);
	}

	public String construct_get_hierarchical_relationships(String named_graph) {
        String prefixes = owlSPARQLUtils.getPrefixes();
        StringBuffer buf = new StringBuffer();
        buf.append(prefixes);
        buf.append("select distinct ?y_label ?y_code ?x_label ?x_code").append("\n");
        buf.append("{").append("\n");
        buf.append("    graph <" + named_graph + ">").append("\n");
        buf.append("    {").append("\n");
        buf.append("                ?x a owl:Class .").append("\n");
        buf.append("                ?x :NHC0 ?x_code .").append("\n");
        buf.append("                ?x rdfs:label ?x_label .").append("\n");
        buf.append("").append("\n");
        buf.append("                ?y a owl:Class .").append("\n");
        buf.append("                ?y :NHC0 ?y_code .").append("\n");
        buf.append("                ?y rdfs:label ?y_label .").append("\n");
        buf.append("").append("\n");
        buf.append("                ?x (rdfs:subClassOf|(owl:equivalentClass/owl:intersectionOf/rdf:rest*/rdf:first)) ?y .").append("\n");
        buf.append("    } ").append("\n");
        buf.append("}").append("\n");
        return buf.toString();
	}

	public Vector getHierarchicalRelationships(String named_graph) {
        String query = construct_get_hierarchical_relationships(named_graph);
        Vector v = owlSPARQLUtils.executeQuery(query);
        if (v == null) return null;
        if (v.size() == 0) return v;
        return new SortUtils().quickSort(v);
	}

	public String construct_get_subsets(String named_graph, String code) {
        String prefixes = owlSPARQLUtils.getPrefixes();
        StringBuffer buf = new StringBuffer();
        buf.append(prefixes);
        buf.append("SELECT ?x_label ?x_code ?p_label ?y_label ?y_code ").append("\n");
        buf.append("{").append("\n");
        buf.append("    graph <" + named_graph + ">").append("\n");
        buf.append("{").append("\n");
        buf.append("        ?x a owl:Class .  ").append("\n");
        buf.append("    	?x :NHC0 ?x_code .").append("\n");
        buf.append("    	?x rdfs:label ?x_label . ").append("\n");
        buf.append("        ?p a owl:AnnotationProperty .       ").append("\n");
        buf.append("        ?p :NHC0 \"A8\"^^xsd:string .").append("\n");
        buf.append("        ?p rdfs:label ?p_label . ").append("\n");
        buf.append("        ?y a owl:Class .  ").append("\n");
        buf.append("   	    ?y :NHC0 ?y_code .").append("\n");
        if (code != null) {
        	buf.append("        ?y :NHC0 \"" + code + "\"^^xsd:string .").append("\n");
		}
        buf.append("    	?y rdfs:label ?y_label . ").append("\n");
        buf.append("        ?x ?p ?y").append("\n");
        buf.append("}").append("\n");
        buf.append("}").append("\n");
        return buf.toString();
	}

	public Vector getSubsets(String named_graph, String code) {
        String query = construct_get_subsets(named_graph, code);
        Vector v = owlSPARQLUtils.executeQuery(query);
        if (v == null) return null;
        if (v.size() == 0) return v;
        return new SortUtils().quickSort(v);
	}

	public Vector getSubsets(String named_graph) {
		return getSubsets(named_graph, null);
	}

	public String construct_get_published_subset(String named_graph) {
        String prefixes = owlSPARQLUtils.getPrefixes();
        StringBuffer buf = new StringBuffer();
        buf.append(prefixes);
        buf.append("SELECT ?x_label ?x_code ?p_label ?p_value").append("\n");
        buf.append("{").append("\n");
        buf.append("    graph <" + named_graph + ">").append("\n");
        buf.append("   {").append("\n");
        buf.append("        ?x a owl:Class .  ").append("\n");
        buf.append("        ?x rdfs:label ?x_label .").append("\n");
        buf.append("    	?x :NHC0 ?x_code .").append("\n");
        buf.append("        ?p a owl:AnnotationProperty .      ").append("\n");
        buf.append("        ?p rdfs:label ?p_label . ").append("\n");
        buf.append("        ?p rdfs:label \"Publish_Value_Set\"^^xsd:string . ").append("\n");
        buf.append("        ?x ?p ?p_value").append("\n");
        buf.append("   }").append("\n");
        buf.append("}").append("\n");
        return buf.toString();
	}


	public Vector getPublishedSubset(String named_graph) {
        String query = construct_get_published_subset(named_graph);
        Vector v = owlSPARQLUtils.executeQuery(query);
        if (v == null) return null;
        if (v.size() == 0) return v;
        return new SortUtils().quickSort(v);
	}


	public String construct_get_labels(String named_graph) {
        String prefixes = owlSPARQLUtils.getPrefixes();
        StringBuffer buf = new StringBuffer();
        buf.append(prefixes);
        buf.append("select distinct ?x_label ?x_code").append("\n");
        buf.append("{ ").append("\n");
        buf.append("graph <" + named_graph + ">").append("\n");
        buf.append("{ ").append("\n");
        buf.append("                ?x a owl:Class .").append("\n");
        buf.append("                ?x :NHC0 ?x_code .").append("\n");
        buf.append("                ?x rdfs:label ?x_label .").append("\n");
        buf.append("}").append("\n");
        buf.append("}").append("\n");
        buf.append("order by ?x_label").append("\n");
        buf.append(" ").append("\n");
        buf.append("").append("\n");
        return buf.toString();
	}


	public Vector getLabels(String named_graph) {
        String query = construct_get_labels(named_graph);
        Vector v = owlSPARQLUtils.executeQuery(query);
        if (v == null) return null;
        if (v.size() == 0) return v;
        return new SortUtils().quickSort(v);
	}

	public String construct_get_label(String named_graph, String code) {
        String prefixes = owlSPARQLUtils.getPrefixes();
        StringBuffer buf = new StringBuffer();
        buf.append(prefixes);
        buf.append("select distinct ?x_label").append("\n");
        buf.append("{ ").append("\n");
        buf.append("graph <" + named_graph + ">").append("\n");
        buf.append("{ ").append("\n");
        buf.append("                ?x a owl:Class .").append("\n");
        buf.append("                ?x :NHC0 ?x_code .").append("\n");
        buf.append("                ?x :NHC0 \"" + code + "\"^^xsd:string .").append("\n");
        buf.append("                ?x rdfs:label ?x_label .").append("\n");
        buf.append("}").append("\n");
        buf.append("}").append("\n");
        buf.append("order by ?x_label").append("\n");
        buf.append(" ").append("\n");
        buf.append("").append("\n");
        return buf.toString();
	}


	public String getLabel(String named_graph, String code) {
        String query = construct_get_label(named_graph, code);
        Vector v = owlSPARQLUtils.executeQuery(query);
        if (v == null) return null;
        if (v.size() == 0) return null;
        Utils.dumpVector(code, v);
        return (String) v.elementAt(0);
	}


	public String construct_get_object_properties(String named_graph) {
        String prefixes = owlSPARQLUtils.getPrefixes();
        StringBuffer buf = new StringBuffer();
        buf.append(prefixes);
        buf.append("SELECT ?p_label ?p_code").append("\n");
        buf.append("{").append("\n");
        buf.append("graph <" + named_graph + ">").append("\n");
        buf.append("{").append("\n");
        buf.append(" ").append("\n");
        buf.append("        ?p a owl:ObjectProperty .       ").append("\n");
        buf.append("    ?p :NHC0 ?p_code .").append("\n");
        buf.append("        ?p rdfs:label ?p_label . ").append("\n");
        buf.append("}").append("\n");
        buf.append("}").append("\n");
        return buf.toString();
	}

	public Vector getObjectProperties(String named_graph) {
        String query = construct_get_object_properties(named_graph);
        Vector v = owlSPARQLUtils.executeQuery(query);
        if (v == null) return null;
        if (v.size() == 0) return v;
        return new SortUtils().quickSort(v);
	}

	public String construct_get_object_valued_annotation_properties(String named_graph) {
        String prefixes = owlSPARQLUtils.getPrefixes();
        StringBuffer buf = new StringBuffer();
        buf.append(prefixes);
        buf.append("SELECT ?p_label ?p_code").append("\n");
        buf.append("{").append("\n");
        buf.append("graph <" + named_graph + ">").append("\n");
        buf.append("{").append("\n");
        buf.append(" ").append("\n");
        buf.append("        ?p a owl:AnnotationProperty .       ").append("\n");
        buf.append("    ?p :NHC0 ?p_code .").append("\n");
        buf.append("        ?p rdfs:label ?p_label . ").append("\n");
        buf.append("        ?p rdfs:range ?p_range .").append("\n");
        buf.append("        FILTER(str(?p_range) = \"http://www.w3.org/2001/XMLSchema#anyURI\"^^xsd:string)").append("\n");
        buf.append("}").append("\n");
        buf.append("}").append("\n");
        return buf.toString();
	}


	public Vector getObjectValuedAnnotationProperties(String named_graph) {
        String query = construct_get_object_valued_annotation_properties(named_graph);
        Vector v = owlSPARQLUtils.executeQuery(query);
        if (v == null) return null;
        if (v.size() == 0) return v;
        return new SortUtils().quickSort(v);
	}

	public String construct_get_string_valued_annotation_properties(String named_graph) {
        String prefixes = owlSPARQLUtils.getPrefixes();
        StringBuffer buf = new StringBuffer();
        buf.append(prefixes);
        buf.append("SELECT ?p_label ?p_code").append("\n");
        buf.append("{").append("\n");
        buf.append("graph <" + named_graph + ">").append("\n");
        buf.append("{").append("\n");
        buf.append(" ").append("\n");
        buf.append("        ?p a owl:AnnotationProperty .       ").append("\n");
        buf.append("    ?p :NHC0 ?p_code .").append("\n");
        buf.append("        ?p rdfs:label ?p_label . ").append("\n");
        buf.append("        ?p rdfs:range ?p_range .").append("\n");
        buf.append("        FILTER(str(?p_range) = \"http://www.w3.org/2001/XMLSchema#string\"^^xsd:string)").append("\n");
        buf.append("}").append("\n");
        buf.append("}").append("\n");

        return buf.toString();
	}


	public Vector getStringValuedAnnotationProperties(String named_graph) {
        String query = construct_get_string_valued_annotation_properties(named_graph);
        Vector v = owlSPARQLUtils.executeQuery(query);
        if (v == null) return null;
        if (v.size() == 0) return v;
        return new SortUtils().quickSort(v);
	}

/////////////////////////////////////////////////////////////////////////////////////////////////////
	public String construct_get_cdisc_codelists(String named_graph, String code, boolean withExtensibleList) {
        String prefixes = owlSPARQLUtils.getPrefixes();
        StringBuffer buf = new StringBuffer();
        buf.append(prefixes);
        if (withExtensibleList) {
        	buf.append("SELECT distinct ?x_label ?x_code ?p1_label ?p1_value ?p_label ?y_label ?y_code ").append("\n");
		} else {
			buf.append("SELECT distinct ?x_label ?x_code ?p_label ?y_label ?y_code ").append("\n");
		}
        buf.append("{").append("\n");
        buf.append("graph <" + named_graph + ">").append("\n");
        buf.append("{").append("\n");
        buf.append("    ?x a owl:Class .  ").append("\n");
        buf.append("    ?x :NHC0 ?x_code .").append("\n");
        buf.append("    ?x rdfs:label ?x_label . ").append("\n");
        buf.append("    ?p a owl:AnnotationProperty .       ").append("\n");
        buf.append("    ?p :NHC0 \"A8\"^^xsd:string .").append("\n");
        buf.append("    ?p rdfs:label ?p_label . ").append("\n");
        buf.append("    ?y a owl:Class .  ").append("\n");
        buf.append("    ?y :NHC0 ?y_code .").append("\n");
        buf.append("    ?y :NHC0 \"" + code + "\"^^xsd:string .").append("\n");
        buf.append("    ?y rdfs:label ?y_label . ").append("\n");
        if (withExtensibleList) {
			buf.append("    ?p1 a owl:AnnotationProperty .       ").append("\n");
			buf.append("    ?p1 :NHC0 \"P361\"^^xsd:string .").append("\n");
			buf.append("    ?p1 rdfs:label ?p1_label .").append("\n");
			buf.append("    ?x ?p1 ?p1_value . ").append("\n");
		}
        buf.append("    ?x ?p ?y").append("\n");
        buf.append("}").append("\n");
        buf.append("}").append("\n");
        buf.append("ORDER BY ?x_label").append("\n");
        return buf.toString();
	}


	public Vector getCdiscCodelists(String named_graph, String code, boolean withExtensibleList) {
        String query = construct_get_cdisc_codelists(named_graph, code, withExtensibleList);
        Vector v = owlSPARQLUtils.executeQuery(query);
        if (v == null) return null;
        if (v.size() == 0) return v;
        return new SortUtils().quickSort(v);
	}


    public static List delimStr2IntList(String t) {
	    List a = new ArrayList();
	    Vector u = StringUtils.parseData(t, '|');
	    for (int i=0; i<u.size(); i++) {
			String s = (String) u.elementAt(i);
			int k = Integer.parseInt(s);
			Integer int_obj = Integer.valueOf(k);
			a.add(int_obj);
		}
		return a;
	}




//Code	Codelist Code	Codelist Extensible (Yes/No)	Codelist Name	CDISC Submission Value	CDISC Synonym(s)	CDISC Definition	NCI Preferred Term

    public static String extractFieldValues(String t, char delim, List a) {
		Vector u = StringUtils.parseData(t, delim);
		//Utils.dumpVector(t, u);
		StringBuffer buf = new StringBuffer();
        for (int i=0; i<a.size(); i++) {
			Integer int_obj = (Integer) a.get(i);
			int k = Integer.valueOf(int_obj);
			if (k == -1) {
				buf.append("").append("" + delim);
			} else {
				buf.append((String) u.elementAt(k)).append("" + delim);
			}
		}
		String s = buf.toString();
		if (s.length() > 0) {
			s = s.substring(0, s.length()-1);
		}
		return s;
	}


	public String construct_get_axiom_qualifier(String named_graph, String prop_code, String code) {
        String prefixes = owlSPARQLUtils.getPrefixes();
        StringBuffer buf = new StringBuffer();
        buf.append(prefixes);
        buf.append("select distinct ?a1 ?x1_code ?x1_label ?a1_target ?q1_code ?q1_value").append("\n");
        buf.append("from <" + named_graph + ">").append("\n");
        buf.append("where {").append("\n");
        buf.append("                ?x1 a owl:Class .").append("\n");
        buf.append("                ?x1 :NHC0 ?x1_code .").append("\n");
        buf.append("                ?x1 rdfs:label ?x1_label .").append("\n");
        buf.append("                ?x1 :NHC0 \"" + code + "\"^^xsd:string .").append("\n");
        buf.append("                ?a1 a owl:Axiom .").append("\n");
        buf.append("                ?a1 owl:annotatedSource ?x1 .").append("\n");
        buf.append("                ?a1 owl:annotatedProperty ?p .").append("\n");
        buf.append("                ?a1 owl:annotatedTarget ?a1_target .").append("\n");
        buf.append("                ?p :NHC0 \"" + prop_code + "\"^^xsd:string .").append("\n");
        buf.append("                ?q1 :NHC0 ?q1_code .").append("\n");
        buf.append("                ?a1 ?q1 ?q1_value .").append("\n");
        buf.append("}").append("\n");
        return buf.toString();
	}


	public Vector getAxiomQualifier(String named_graph, String prop_code, String code) {
        String query = construct_get_axiom_qualifier(named_graph, prop_code, code);
        Vector v = owlSPARQLUtils.executeQuery(query);
        if (v == null) return null;
        if (v.size() == 0) return v;
        return new SortUtils().quickSort(v);
	}

	public HashMap parseFULLSYNs(Vector v) {
		HashMap hmap = new HashMap();
		String curr_axiomId = "";
		Synonym syn = null;
		List w = new ArrayList();
		String code = null;
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String axiomId = (String) u.elementAt(0);
			code = (String) u.elementAt(1);
			String label = (String) u.elementAt(2);
			String target = (String) u.elementAt(3);
			String q_code = (String) u.elementAt(4);
			String q_value = (String) u.elementAt(5);
		    w = new ArrayList();
			if (hmap.containsKey(code)) {
				w = (List) hmap.get(code);
			}
			if (axiomId.compareTo(curr_axiomId) == 0) {
				if (q_code.compareTo("P383") == 0) {
					syn.setTermGroup(q_value);
				} else if (q_code.compareTo("P384") == 0) {
					syn.setTermSource(q_value);
				} else if (q_code.compareTo("P385") == 0) {
					syn.setSourceCode(q_value);
				} else if (q_code.compareTo("P386") == 0) {
					syn.setSubSourceName(q_value);
				}
			} else {
				if (syn != null) {
					w.add(syn);
					hmap.put(code, w);
				}
				curr_axiomId = axiomId;
				syn = new Synonym();
				syn.setCode(code);
				syn.setLabel(label);
				syn.setTermName(target);
				if (q_code.compareTo("P383") == 0) {
					syn.setTermGroup(q_value);
				} else if (q_code.compareTo("P384") == 0) {
					syn.setTermSource(q_value);
				} else if (q_code.compareTo("P385") == 0) {
					syn.setSourceCode(q_value);
				} else if (q_code.compareTo("P386") == 0) {
					syn.setSubSourceName(q_value);
				}
			}
		}
		if (syn != null) {
			w.add(syn);
			hmap.put(code, w);
		}
		return hmap;
	}

	public HashMap parseAltDEFINITIONs(Vector v) {
		HashMap hmap = new HashMap();
		String curr_axiomId = "";
		AltDefinition def = null;
		List w = new ArrayList();
		String code = null;
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String axiomId = (String) u.elementAt(0);
			code = (String) u.elementAt(1);
			String label = (String) u.elementAt(2);
			String target = (String) u.elementAt(3);
			String q_code = (String) u.elementAt(4);
			String q_value = (String) u.elementAt(5);
		    w = new ArrayList();
			if (hmap.containsKey(code)) {
				w = (List) hmap.get(code);
			}
			if (axiomId.compareTo(curr_axiomId) == 0) {
				if (q_code.compareTo("P381") == 0) {
					def.setAttribution(q_value);
				} else if (q_code.compareTo("P378") == 0) {
					def.setSource(q_value);
				}
			} else {
				if (def != null) {
					w.add(def);
					hmap.put(code, w);
				}
				curr_axiomId = axiomId;
				def = new AltDefinition();
				def.setCode(code);
				def.setLabel(label);
				def.setDescription(target);
				if (q_code.compareTo("P381") == 0) {
					def.setAttribution(q_value);
				} else if (q_code.compareTo("P378") == 0) {
					def.setSource(q_value);
				}
			}
		}
		if (def != null) {
			w.add(def);
			hmap.put(code, w);
		}
		return hmap;
	}

	public static String getCodeListName(String code, List<Synonym> parentAxioms) {
		String termSource = "CDISC";
		String termType = "SY";
		String sourceCode = null;
		String subsourceName = null;
	    List<String> cdisc_sys = getSynonymsWithQualifiers(parentAxioms,
	       termSource, termType, sourceCode, subsourceName);

	    if (cdisc_sys == null || cdisc_sys.size() == 0) {
			System.out.println("\nINFO: No CDISC SY is found in " + code);
			return null;
		} else if (cdisc_sys.size() == 1) {
			return cdisc_sys.get(0);
		} else {
			System.out.println("\nINFO: Multiple CDISC SYs are found in " + code);
			return cdisc_sys.get(0);
		}
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

	public void run(String subsetcode) {
		String serviceUrl = ConfigurationController.serviceUrl;
		String namedGraph = ConfigurationController.namedGraph;
		String username = ConfigurationController.username;
		String password = ConfigurationController.password;
        CDISCReportGenerator utils = new CDISCReportGenerator(serviceUrl, namedGraph, username, password);

        Vector w = new Vector();
		String t = "0|1|2";
		List a = delimStr2IntList(t);
		String value = extractFieldValues(HEADING, '\t', a);
		//w.add(value);
		w.add(HEADING);

		//CDISC ADaM Analysis Stratum Terminology|C204411|Extensible_List|Yes|Concept_In_Subset|CDISC ADaM Terminology|C81222
        //String code = "C81222";
        boolean withExtensibleList = true;
        Vector v = utils.getCdiscCodelists(namedGraph, subsetcode, withExtensibleList);

        //Utils.dumpVector(subsetcode, v);

        withExtensibleList = false;
        for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			t = "1|-1|3";
			a = delimStr2IntList(t);
			value = extractFieldValues(line, '|', a);
			w.add(value.replace("|", "\t"));

			Vector u = StringUtils.parseData(line, '|');
			String codelistcode = (String) u.elementAt(1);
			Vector w1 = utils.getCdiscCodelists(namedGraph, codelistcode, withExtensibleList);

			t = "1|4|-1";
			a = delimStr2IntList(t);
			for (int j=0; j<w1.size(); j++) {
				line = (String) w1.elementAt(j);
				value = extractFieldValues(line, '|', a);
				w.add(value.replace("|", "\t"));
			}
		}
		//Utils.saveToFile("CDISC_" + code + ".txt", w);
		Vector w1 = new Vector();
		w1.add(HEADING);
		for (int i=1; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			line = appendRowData(line);
			w1.add(line);
		}

		String label = utils.getLabel(namedGraph, subsetcode);
		String outputfile = label + "_" + StringUtils.getToday() + ".txt";
		outputfile = outputfile.replace(" ", "_");
		Utils.saveToFile(outputfile, w1);
		System.out.println(outputfile + " generated.");
	}

    public List getFULLSYNList(String code) {
        String prop_code = "P90";
	    Vector v = getAxiomQualifier(named_graph, prop_code, code);
	    HashMap hmap = parseFULLSYNs(v);
	    List list = (List) hmap.get(code);
	    return list;
	}

    public List getAltDefinitionList(String code) {
        String prop_code = "P325";
	    Vector v = getAxiomQualifier(named_graph, prop_code, code);
	    HashMap hmap = parseAltDEFINITIONs(v);
	    List list = (List) hmap.get(code);
	    return list;
	}

	public static String getSynonyms(List axioms, String source, String type) {
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<axioms.size(); i++) {
			Synonym syn = (Synonym) axioms.get(i);
			if (source != null) {
				if (syn.getTermSource() != null && syn.getTermSource().compareTo(source) == 0) {
					if (syn.getTermGroup().compareTo(type) == 0) {
						buf.append(syn.getTermName()).append("|");
					}
				}
		    } else {
				if (syn.getTermGroup().compareTo(type) == 0) {
					buf.append(syn.getTermName()).append("|");
				}
			}
		}
		String t = buf.toString();
		if (t.length() > 0) {
			t = t.substring(0, t.length()-1);
		}
		return t;
	}

	public static String getAltDefinitions(List axioms, String source) {
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<axioms.size(); i++) {
			AltDefinition def = (AltDefinition) axioms.get(i);
			if (source != null) {
				if (def.getSource() != null && def.getSource().compareTo(source) == 0) {
					buf.append(def.getDescription()).append("|");
				}
		    } else {
				buf.append(def.getDescription()).append("|");
			}
		}
		String t = buf.toString();
		if (t.length() > 0) {
			t = t.substring(0, t.length()-1);
		}
		return t;
	}

//    public static String HEADING = "Code	Codelist Code	Codelist Extensible (Yes/No)	Codelist Name	CDISC Submission Value	CDISC Synonym(s)	CDISC Definition	NCI Preferred Term";
	public String appendRowData(String line) {
		Vector u = StringUtils.parseData(line, '\t');
		String code = (String) u.elementAt(0);
		String codelistCode = (String) u.elementAt(1);

		if (codelistCode == null || codelistCode.length() == 0) {
			codelistCode = code;
		}

		String yesOrNo = (String) u.elementAt(2);
		List axioms = (List) getFULLSYNList(code);
		List parentAxioms = (List) getFULLSYNList(codelistCode);
		//Codelist Name
		String codelistName = getCodeListName(code, parentAxioms);
		//CDISC Submission Value
        String cdiscSubmissionValue = getCDISCSubmissionValue(codelistCode, parentAxioms, code, axioms);
        //CDISC Synonym(s)
		String synonyms = getSynonyms(axioms, null, "SY");
		//CDISC Definition
		List altDefinitionList = getAltDefinitionList(code);
		String cdisdcDef = getAltDefinitions(altDefinitionList, "CDISC");
		//NCI Preferred Term
		String pts = getSynonyms(axioms, "NCI", "PT");
        return line + "\t" + codelistName
                    + "\t" + cdiscSubmissionValue
                    + "\t" + synonyms
                    + "\t" + cdisdcDef
                    + "\t" + pts;
	}

	public static void main(String[] args) {
		String serviceUrl = ConfigurationController.serviceUrl;
		String namedGraph = ConfigurationController.namedGraph;
		String username = ConfigurationController.username;
		String password = ConfigurationController.password;
        CDISCReportGenerator utils = new CDISCReportGenerator(serviceUrl, namedGraph, username, password);
        String subsetcode = args[0];//"C81222";
        long ms = System.currentTimeMillis();
        utils.run(subsetcode);
        System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}

}
