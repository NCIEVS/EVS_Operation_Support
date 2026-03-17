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
 * Copyright 2008-2017 NGIS. This software was developed in conjunction
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
 *      "This product includes software developed by NGIS and the National
 *      Cancer Institute."   If no such end-user documentation is to be
 *      included, this acknowledgment shall appear in the software itself,
 *      wherever such third-party acknowledgments normally appear.
 *   3. The names "The National Cancer Institute", "NCI" and "NGIS" must
 *      not be used to endorse or promote products derived from this software.
 *   4. This license does not authorize the incorporation of this software
 *      into any third party proprietary programs. This license does not
 *      authorize the recipient to use any trademarks owned by either NCI
 *      or NGIS
 *   5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED
 *      WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *      OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE
 *      DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
 *      NGIS, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT,
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
 *     Initial implementation kim.ong@ngc.com
 *
 */

public class OWL2LogicalExpression {

    OWLScanner owlScanner = null;

	static String NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile; //"ThesaurusInferred_forTS.owl";
	static String PARENT_CHILD_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile; // "parent_child.txt";
	static String RESTRICTION_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.rolefile; //"roles.txt";
	static String AXIOM_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.axiomfile;
	static Vector ROLE_DATA = null;

	public LogicalExpressionGenerator generator = null;

	static String CONCEPT_FILE = "concepts_with_rolegroups.txt";

	static {

	}

    public OWL2LogicalExpression() {
		owlScanner = new OWLScanner(NCIT_OWL);
		generator = new LogicalExpressionGenerator();
    }

	public Vector getOWLClassDataByCode(String code) {
		return owlScanner.getOWLClassDataByCode(code);
	}

	//<owl:onProperty rdf:resource="http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#R114"/>
	public String findRoleCode(String line) {
		if (line.indexOf("owl:onProperty") == -1) return null;
		int n = line.lastIndexOf("#");
		String t = line.substring(n+1, line.length()-3);
		return t;
	}

	public Vector getCodes() {
		Vector w = new Vector();
		Vector v = Utils.readFile(CONCEPT_FILE);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(0);
			if (!w.contains(code)) {
				w.add(code);
			}
		}
		return w;
	}

    public Vector appendRoleRange(String code) {
		String owlfile = code + ".owl";
		Vector v = null;
		File f = new File(owlfile);
		if (!f.exists()) {
			v = getOWLClassDataByCode(code);
			Utils.saveToFile(owlfile, v);
		} else {
			v = Utils.readFile(owlfile);
		}

        Vector w = new Vector();
        for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			String roleCode = findRoleCode(line);
			if (roleCode != null) {
				String roleRange = (String) generator.getRangeNameByRoleCode(roleCode);
				w.add(line + " (" + roleCode + " range: " + roleRange + ")");
			} else {
				w.add(line);
			}
		}
		return w;
	}

	public String getLabel(String code) {
		return generator.getLabel(code);
	}

    public static void main(String[] args) {
		long ms = System.currentTimeMillis();
		OWL2LogicalExpression test = new OWL2LogicalExpression();
		long ms0 = System.currentTimeMillis();
		Vector w0 = new Vector();
		Vector codes = null;
		codes = test.getCodes();
		if (args.length > 0) {
			String codefile = args[0];
			codes = Utils.readFile(codefile);
		}

		Vector display_name_vec = new Vector();

		for (int i=0; i<codes.size(); i++) {
			String code = (String) codes.elementAt(i);
			String label = test.getLabel(code);
			System.out.println(label + " (" + code + ")");
			String displayName = label + " (" + code + ")";
			System.out.println(displayName);
			display_name_vec.add(displayName);
			Vector w = test.appendRoleRange(code);
			w0.add("\n");
			w0.addAll(w);
			ms = System.currentTimeMillis();
			System.out.println("Time elapsed (ms): " + (ms - ms0));
		}
		Utils.saveToFile("range_" + CONCEPT_FILE, w0);
		ms = System.currentTimeMillis();
        System.out.println("Total run time (ms): " + (ms - ms0));
        Utils.dumpVector("Concepts processed", display_name_vec);
    }

}
