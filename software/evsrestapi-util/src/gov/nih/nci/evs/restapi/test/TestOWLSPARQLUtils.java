package gov.nih.nci.evs.restapi.test;
import gov.nih.nci.evs.restapi.bean.*;
import gov.nih.nci.evs.restapi.common.*;
import gov.nih.nci.evs.restapi.util.*;
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
public class TestOWLSPARQLUtils {
	OWLSPARQLUtils owlSPARQLUtils = null;
	String serviceUrl = null;
	String version = null;
	String named_graph = null;

    public TestOWLSPARQLUtils(String serviceUrl, String named_graph) {
		if (!serviceUrl.endsWith("?query=")) {
			serviceUrl = serviceUrl + "?query=";
		}
		owlSPARQLUtils = new OWLSPARQLUtils(serviceUrl, null, null);
        this.named_graph = named_graph;
        owlSPARQLUtils.set_named_graph(this.named_graph);

		try {
			new MetadataUtils(serviceUrl).dumpNameVersion2NamedGraphMap();
		} catch (Exception ex) {
			System.out.println("\tWARNING: Exception: serviceUrl: " + serviceUrl);
		}

		version = owlSPARQLUtils.get_ontology_version(named_graph);

    }

    public OWLSPARQLUtils getOWLSPARQLUtils() {
		return owlSPARQLUtils;
	}


	public static void main(String[] args) {
		String serviceUrl = args[0];
		System.out.println(serviceUrl);
		MetadataUtils test = new MetadataUtils(serviceUrl);
		String codingScheme = "NCI_Thesaurus";
		long ms = System.currentTimeMillis();
		String version = test.getLatestVersion(codingScheme);
		System.out.println(codingScheme);
		System.out.println(version);
		String named_graph = args[1]; //test.getNamedGraph(codingScheme);
		System.out.println(named_graph);
		ParserUtils parser = new ParserUtils();

		TestOWLSPARQLUtils analyzer = new TestOWLSPARQLUtils(serviceUrl, named_graph);

		OWLSPARQLUtils owlSPARQLUtils = analyzer.getOWLSPARQLUtils();
		String inputfile = args[2];
		Vector codes = Utils.readFile(inputfile);
		for (int i=0; i<codes.size(); i++) {
			String concept_code = (String) codes.elementAt(i);
			Vector w = owlSPARQLUtils.getLabelByCode(named_graph, concept_code);
			w = new ParserUtils().getResponseValues(w);
			String label = (String) w.elementAt(0);
			int j = i+1;
			System.out.println("(" + j + ") " + label + " (" + concept_code + ")");
			HashMap prop_map = owlSPARQLUtils.getPropertyHashMapByCode(named_graph, concept_code);
			Utils.dumpMultiValuedHashMap("prop_map", prop_map);

//	public Vector getAxiomsByCode(String named_graph, String code, String propertyName) {


			Vector axioms = owlSPARQLUtils.getAxiomsByCode(named_graph, concept_code, "FULL_SYN");
			//Utils.dumpVector("axioms", axioms);
            System.out.println("\nFULL_SYN");
			Vector syns = parser.parseSynonymData(axioms);
			for (int k=0; k<syns.size(); k++) {
				Synonym syn = (Synonym) syns.get(k);
				System.out.println(syn.toJson());
			}

            System.out.println("\nDEFINITION");
			Vector def_vec = owlSPARQLUtils.getDefinitions(named_graph, concept_code, "DEFINITION");
			for (int k2=0; k2<def_vec.size(); k2++) {
				Definition def = (Definition) def_vec.get(k2);
				System.out.println(def.toJson());
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
	}
}
