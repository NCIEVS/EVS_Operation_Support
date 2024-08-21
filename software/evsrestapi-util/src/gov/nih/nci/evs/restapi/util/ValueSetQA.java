package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.model.*;
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
public class ValueSetQA {
	HTTPUtils httpUtils = null;
	String serviceUrl = null;
	String username = null;
	String password = null;
	String namedGraph = null;
	String prefixes = null;
	OWLSPARQLUtils owlSPARQLUtils = null;
	gov.nih.nci.evs.restapi.util.ValueSetConditionValidator validator = null;
	boolean checkOutBoundConceptInSubset = false;
	ValueSetReportGenerator generator = null;

	public ValueSetQA(String serviceUrl, String namedGraph, String username, String password) {
		this.serviceUrl = serviceUrl;
		this.namedGraph = namedGraph;
		this.username = username;
		this.password = password;
		this.httpUtils = new HTTPUtils(serviceUrl,username, password);

		this.owlSPARQLUtils = new OWLSPARQLUtils(serviceUrl, username, password);
		this.owlSPARQLUtils.set_named_graph(namedGraph);

	}

	public void setCheckOutBoundConceptInSubset(boolean bool) {
		checkOutBoundConceptInSubset = bool;
	}

	public void setValueSetConditionValidator(gov.nih.nci.evs.restapi.util.ValueSetConditionValidator validator) {
		this.validator = validator;
	}

    public String getQuery(String query_file) {
	    return owlSPARQLUtils.getQuery(query_file);
	}

	public Vector execute(String query) {
		return owlSPARQLUtils.execute(query);
	}

	public Vector getConceptsWithPropertyAndQualifiersMatching(String named_graph, String code, String propertyLabel,
	              Vector qualifierCodes, Vector qualifierValues) {
	    return owlSPARQLUtils.getConceptsWithPropertyAndQualifiersMatching(named_graph, code, propertyLabel,
	              qualifierCodes, qualifierValues);
	}

	public void run(Vector conditions) {
		gov.nih.nci.evs.restapi.util.ValueSetConditionValidator validator = new gov.nih.nci.evs.restapi.util.ValueSetConditionValidator(
			  this.serviceUrl, this.namedGraph,this. username, this.password);
        validator.setConditions(conditions);
		String headerConceptCode = validator.getHeaderConceptCode();
		if (headerConceptCode == null) {
			System.out.println("WARNING: Value set header concept not set -- program abort.");
			System.exit(1);
		}
		System.out.println("Instantiatng valueSetQA ...");
		setValueSetConditionValidator(validator);
        System.out.println("Instantiatng ValueSetReportGenerator ...");
	    generator = new ValueSetReportGenerator(serviceUrl, namedGraph, username, password, validator);
        generator.set_condition_data(validator.getConditionData());
        generator.set_conditions(validator.getConditions());
        generator.setCheckOutBoundConceptInSubset(this.checkOutBoundConceptInSubset);
        System.out.println("Generating QA report, please wait...");
        generator.generate();
	}

	public Vector getWarnings() {
		return generator.getWarnings();
	}

	public Vector getMissings() {
		return generator.getMissings();
	}

}
