package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.bean.*;
import java.io.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.*;
import java.util.regex.*;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

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
public class ConceptDetailsUtils {

    static Vector serviceUrl_vec = null;
	static Duration readTimeout;
	static Duration connectTimeout;
	static String SERVICE_URL_DATA_FILE = "service_url_data.txt";

	String username = null;
	String password = null;
	static {
		if (new File(SERVICE_URL_DATA_FILE).exists()) {
			serviceUrl_vec = Utils.readFile(SERVICE_URL_DATA_FILE);
		}
	}

	public ConceptDetailsUtils() {

	}

	private static final Logger log = LoggerFactory.getLogger(ConceptDetailsUtils.class);

	public ConceptDetailsUtils(String username, String password) {
		this.username = username;
		this.password = password;
	}

    public static void testMetadataUtils(int i, String username, String password) {
		String serviceUrl = (String) serviceUrl_vec.elementAt(i);
		new MetadataUtils(serviceUrl, username, password).dumpNameVersion2NamedGraphMap();
	}

    public static void testMetadataUtils(String username, String password) {
		testMetadataUtils(serviceUrl_vec, username, password);
    }

    public static void testMetadataUtils(Vector serviceUrl_vec, String username, String password) {
		Vector w = new Vector();
		Vector warning_vec = new Vector();
    	for (int i=0; i<serviceUrl_vec.size(); i++) {
			String serviceUrl = (String) serviceUrl_vec.elementAt(i);
			int j = i+1;
		    System.out.println("\n(" + j + ") " + serviceUrl);
		    try {
				if (serviceUrl.indexOf("?") != -1) {
					new MetadataUtils(serviceUrl).dumpNameVersion2NamedGraphMap();
					w.add(serviceUrl);
				} else {
					new MetadataUtils(serviceUrl, username, password).dumpNameVersion2NamedGraphMap();
					w.add(serviceUrl);
				}
			} catch (Exception ex) {
				System.out.println("\tWARNING: Exception: serviceUrl: " + serviceUrl);
				warning_vec.add(serviceUrl);
			}
		}
		System.out.println("\nTest Results:");
		Utils.dumpVector("Service URLs", serviceUrl_vec);

		System.out.println("\nThe following serviceURL return data successfully:");
		Utils.dumpVector("Service URLs", w);

		System.out.println("\nThe following serviceURL throw exceptions:");
		Utils.dumpVector("Service URLs", warning_vec);
	}


    public static boolean testMetadataUtils(String serviceUrl) {
		Vector w = new Vector();
		Vector warning_vec = new Vector();
	    System.out.println("\n" + serviceUrl);
		try {
			new MetadataUtils(serviceUrl).dumpNameVersion2NamedGraphMap();
			return true;
		} catch (Exception ex) {
			System.out.println("\tWARNING: Exception: serviceUrl: " + serviceUrl);
			return false;
		}
	}

    public static boolean testMetadataUtils(String serviceUrl, String username, String password) {
		Vector w = new Vector();
		Vector warning_vec = new Vector();
	    System.out.println("\n" + serviceUrl);
		try {
			new MetadataUtils(serviceUrl, username, password).dumpNameVersion2NamedGraphMap();
			return true;
		} catch (Exception ex) {
			System.out.println("\tWARNING: Exception: serviceUrl: " + serviceUrl);
			return false;
		}
	}

    public Vector searchAvailableServiceUrl(String codingScheme, String version) {
        Vector w = new Vector();
        String target = codingScheme + "|" + version;
    	for (int i=0; i<serviceUrl_vec.size(); i++) {
			String serviceUrl = (String) serviceUrl_vec.elementAt(i);
			int j = i+1;
		    try {
				MetadataUtils mdu = null;
				if (serviceUrl.indexOf("?") != -1) {
					mdu = new MetadataUtils(serviceUrl);
				} else {
					mdu = new MetadataUtils(serviceUrl, username, password);
				}
				HashMap hmap = mdu.getNameVersion2NamedGraphMap();
				if (hmap.containsKey(target)) {
					w.add(serviceUrl);
				}
			} catch (Exception ex) {

			}
		}
		return w;
	}


    public void getConceptDetailsUtils(String serviceUrl, String username, String password, String concept_code) {
		String named_graph = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl";
		OWLSPARQLUtils owlSPARQLUtils = null;
		ParserUtils parser = new ParserUtils();
		if (serviceUrl.indexOf("?") != -1) {
			owlSPARQLUtils = new OWLSPARQLUtils(serviceUrl, null, null);
		} else {
			owlSPARQLUtils = new OWLSPARQLUtils(serviceUrl, username, password);
		}

		Vector w = owlSPARQLUtils.getLabelByCode(named_graph, concept_code);
		w = new ParserUtils().getResponseValues(w);
		String label = (String) w.elementAt(0);
		System.out.println(label + " (" + concept_code + ")");
		HashMap prop_map = owlSPARQLUtils.getPropertyHashMapByCode(named_graph, concept_code);
		Utils.dumpMultiValuedHashMap("prop_map", prop_map);

		Vector axioms = owlSPARQLUtils.getAxiomsByCode(named_graph, concept_code, "FULL_SYN");
        Utils.dumpVector("axioms", axioms);

		Vector syns = parser.parseSynonymData2(axioms);
		for (int k=0; k<syns.size(); k++) {
			Synonym syn = (Synonym) syns.get(k);
			System.out.println(syn.toJson());
		}

		Vector superconcept_vec = owlSPARQLUtils.getSuperclassesByCode(named_graph, concept_code);
		superconcept_vec = parser.getResponseValues(superconcept_vec);
		if (superconcept_vec != null) Utils.dumpVector("superconcept_vec", superconcept_vec);
		Vector subconcept_vec = owlSPARQLUtils.getSubclassesByCode(named_graph, concept_code);
		subconcept_vec = parser.getResponseValues(subconcept_vec);
		if (subconcept_vec != null) Utils.dumpVector("subconcept_vec", subconcept_vec);
		Vector role_vec = owlSPARQLUtils.getOutboundRolesByCode(named_graph, concept_code);
		role_vec = parser.getResponseValues(role_vec);
		if (role_vec != null) Utils.dumpVector("role_vec", role_vec);
		Vector inv_role_vec = owlSPARQLUtils.getInboundRolesByCode(named_graph, concept_code);
		inv_role_vec = parser.getResponseValues(inv_role_vec);
		if (inv_role_vec != null) Utils.dumpVector("inv_role_vec", inv_role_vec);
		Vector asso_vec = owlSPARQLUtils.getAssociationsByCode(named_graph, concept_code);
		asso_vec = parser.getResponseValues(asso_vec);
		if (asso_vec != null) Utils.dumpVector("asso_vec", asso_vec);
		Vector inv_asso_vec = owlSPARQLUtils.getInverseAssociationsByCode(named_graph, concept_code);
		inv_asso_vec = parser.getResponseValues(inv_asso_vec);
		if (inv_asso_vec != null) Utils.dumpVector("inv_asso_vec", inv_asso_vec);
	}


    public void getConceptDetails(String codingScheme, String version, String concept_code) {
        Vector serviceUrl_vec = searchAvailableServiceUrl(codingScheme, version);
        if (serviceUrl_vec == null || serviceUrl_vec.size() == 0) return;
        String serviceUrl = (String) serviceUrl_vec.elementAt(0);
        getConceptDetailsUtils(serviceUrl, this.username, this.password, concept_code);
	}


    public void getConceptDetails(String serviceUrl, String named_graph, String username, String password, String concept_code) {
		OWLSPARQLUtils owlSPARQLUtils = null;
		ParserUtils parser = new ParserUtils();
		if (serviceUrl.indexOf("?") != -1) {
			owlSPARQLUtils = new OWLSPARQLUtils(serviceUrl, null, null);
		} else {
			owlSPARQLUtils = new OWLSPARQLUtils(serviceUrl, username, password);
		}
		owlSPARQLUtils.set_named_graph(named_graph);

		Vector w = owlSPARQLUtils.getLabelByCode(named_graph, concept_code);
		w = parser.getResponseValues(w);
		String label = (String) w.elementAt(0);
		System.out.println(label + " (" + concept_code + ")");
		HashMap prop_map = owlSPARQLUtils.getPropertyHashMapByCode(named_graph, concept_code);
		Utils.dumpMultiValuedHashMap("prop_map", prop_map);

		Vector axioms = owlSPARQLUtils.getAxiomsByCode(named_graph, concept_code, "FULL_SYN");
		Utils.dumpVector("axioms", axioms);

		Vector syns = parser.parseSynonymData2(axioms);
		for (int k=0; k<syns.size(); k++) {
			Synonym syn = (Synonym) syns.get(k);
			System.out.println(syn.toJson());
		}

		Vector superconcept_vec = owlSPARQLUtils.getSuperclassesByCode(named_graph, concept_code);
		if (superconcept_vec != null) Utils.dumpVector("superconcept_vec", superconcept_vec);
		Vector subconcept_vec = owlSPARQLUtils.getSubclassesByCode(named_graph, concept_code);
		if (subconcept_vec != null) Utils.dumpVector("subconcept_vec", subconcept_vec);

		Vector role_vec = owlSPARQLUtils.getOutboundRolesByCode(named_graph, concept_code);
		//role_vec = parser.getResponseValues(role_vec);
		if (role_vec != null) Utils.dumpVector("role_vec", role_vec);
		Vector inv_role_vec = owlSPARQLUtils.getInboundRolesByCode(named_graph, concept_code);
		//inv_role_vec = parser.getResponseValues(inv_role_vec);
		if (inv_role_vec != null) Utils.dumpVector("inv_role_vec", inv_role_vec);

		Vector asso_vec = owlSPARQLUtils.getAssociationsByCode(named_graph, concept_code);
		if (asso_vec != null) Utils.dumpVector("asso_vec", asso_vec);
		Vector inv_asso_vec = owlSPARQLUtils.getInverseAssociationsByCode(named_graph, concept_code);
		if (inv_asso_vec != null) Utils.dumpVector("inv_asso_vec", inv_asso_vec);
	}

    public static void main(String[] args) {
		String serviceUrl = args[0];
		String named_graph = args[1];
		String username = args[2];
		String password = args[3];
		String concept_code = args[4];
        if (named_graph.compareTo("null") == 0) named_graph = null;
		if (username.compareTo("null") == 0) username = null;
		if (password.compareTo("null") == 0) password = null;
/*
		ConceptDetailsUtils cdu = new ConceptDetailsUtils(username, password);
		String codingScheme = "NCI_Thesaurus";
		Vector v = cdu.searchAvailableServiceUrl(codingScheme, version);
		Utils.dumpVector("Service URLs supporting " + version, v);
		cdu.getConceptDetails(codingScheme, version, concept_code);
*/
//      public void getConceptDetails(String serviceUrl, String named_graph, String username, String password, String concept_code) {

		new ConceptDetailsUtils().getConceptDetails(serviceUrl, named_graph, username, password, concept_code);
	}
}
