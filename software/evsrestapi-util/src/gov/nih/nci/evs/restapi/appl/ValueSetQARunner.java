package gov.nih.nci.evs.restapi.appl;
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
public class ValueSetQARunner {
	String serviceUrl = null;
	String namedGraph = null;
	String username = null;
	String password = null;
    ValueSetQA valueSetQA = null;
	OWLSPARQLUtils owlSPARQLUtils = null;
	HashMap conditionHashMap = null;

	public ValueSetQARunner(String serviceUrl, String namedGraph, String username, String password) {
		this.serviceUrl = serviceUrl;
		this.namedGraph = namedGraph;
		this.username = username;
		this.password = password;
		this.owlSPARQLUtils = new OWLSPARQLUtils(serviceUrl, username, password);
		this.owlSPARQLUtils.set_named_graph(namedGraph);
		this.valueSetQA = new ValueSetQA(serviceUrl, namedGraph, username, password);
	}

    public void loadConditionHashMap(String filename) {
		conditionHashMap = new HashMap();
		Vector w = Utils.readFile(filename);
		String key = null;
		Vector values = null;
		for (int i=0; i<w.size(); i++) {
			String t = (String) w.elementAt(i);
			if (t.indexOf("$") != -1) {
				if (key != null) {
					conditionHashMap.put(key, values);
				}
				key = t;
				values = new Vector();
			} else {
				values.add(t);
			}
		}
		conditionHashMap.put(key, values);
	}

    public void dumpConditionHashMap() {
		if (conditionHashMap == null) return;
		Iterator it = conditionHashMap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			Vector values = (Vector) conditionHashMap.get(key);
			Vector u = StringUtils.parseData(key, '$');
			String headerConceptCode = (String) u.elementAt(0);
			boolean boolval = false;
			if(u.size() == 2) {
				String boolstr = (String) u.elementAt(1);
				Boolean boolObj = Boolean.parseBoolean(boolstr);
				boolval = Boolean.valueOf(boolObj);
			}
			System.out.println("\nheaderConceptCode: " + headerConceptCode);
			System.out.println("boolval: " + boolval);
			Utils.dumpVector(key, values);
		}
	}

	public Vector appendChar(Vector v, char c) {
		Vector w = new Vector();
		for (int i=0; i<v.size();i++) {
			String t = (String) v.elementAt(i);
			t = t + "|" + c;
			w.add(t);
		}
		return w;
	}

	public void run() {
		if (conditionHashMap == null) return;
		String outputfile = "ValuSetQA_" + StringUtils.getToday() + ".log";
		Vector warning_vec = new Vector();
		Iterator it = conditionHashMap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			Vector values = (Vector) conditionHashMap.get(key);
			Vector u = StringUtils.parseData(key, '$');
			String headerConceptCode = (String) u.elementAt(0);
			boolean boolval = false;
			if(u.size() == 2) {
				String boolstr = (String) u.elementAt(1);
				Boolean boolObj = Boolean.parseBoolean(boolstr);
				boolval = Boolean.valueOf(boolObj);
			}
			valueSetQA.setCheckOutBoundConceptInSubset(boolval);
			valueSetQA.run(values);
			Vector v = new Vector();
			v.add(headerConceptCode);
			v.addAll(appendChar(valueSetQA.getWarnings(), '1'));
			v.addAll(appendChar(valueSetQA.getMissings(), '2'));
			warning_vec.addAll(v);
		}
		Utils.saveToFile(outputfile, warning_vec);
	}

	public static void main(String[] args) {
		long ms = System.currentTimeMillis();
		String serviceUrl = args[0];
		String namedGraph = args[1];
		String username = args[2];
		String password = args[3];
		String datafile = args[4];
		ValueSetQARunner test = new ValueSetQARunner(serviceUrl, namedGraph, username, password);
		test.loadConditionHashMap(datafile);
		test.run();
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}
}

