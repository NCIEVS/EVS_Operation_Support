package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.bean.*;
import gov.nih.nci.evs.restapi.config.*;

import java.io.*;
import java.text.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;
import java.nio.charset.Charset;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

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


public class TestUtils {

	public static String SPARQL_QUERY_PROPERTIES = "sparql-queries.properties";
	public static String TESTCASE_PROPERTIES = "testcase.properties";

	public static HashMap load_property_file(String property_file) {
        HashMap hmap = new HashMap();
        try (InputStream input = new FileInputStream(property_file)) {
            Properties prop = new Properties();
            // load a properties file
            prop.load(input);
            prop.forEach((key, value) ->
                hmap.put(key, value)
            );
        } catch (IOException io) {
            io.printStackTrace();
        }
		return hmap;
	}

	public static HashMap reformat_sparql_queries(HashMap hmap) {
		HashMap map = new HashMap();
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			String value = (String) hmap.get(key);
			value = value.replace("#{", "${");
			map.put(key, value);
		}
		return map;
	}

    public static Vector extractQueryParameters(String query) {
		Vector w = new Vector();
		String t = query;
		int n = t.indexOf("${");
		while (n != -1) {
			t = t.substring(n, t.length());
			int m = t.indexOf("}");
			String parameter = t.substring(2, m);
			if (!w.contains(parameter)) {
				w.add(parameter);
			}
			t = t.substring(m+1, t.length());
			n = t.indexOf("${");
		}
		return w;
	}

    public static Vector extractAllQueryParameters(String query_properties) {
		HashMap hmap = load_property_file(query_properties);
		hmap = reformat_sparql_queries(hmap);
		Vector w = new Vector();
		Iterator it = hmap.keySet().iterator();
		int lcv = 0;
		while (it.hasNext()) {
			lcv++;
			String key = (String) it.next();
			String query = (String) hmap.get(key);
			Vector v = extractQueryParameters(query);


			Utils.dumpVector("(" + lcv + ") " + key + ":\n" + query, v);


			for (int i=0; i<v.size(); i++) {
				String parameter = (String) v.elementAt(i);
				if (!w.contains(parameter)) {
					w.add(parameter);
				}
			}
		}
		w = new SortUtils().quickSort(w);
		Vector w1 = new Vector();
		for (int i=0; i<w.size(); i++) {
			String t = (String) w.elementAt(i);
			w1.add(t + "=");
		}
		return w1;
	}

	public static Vector generateStringSubstitutionCodes(Vector parameters, Vector values) {
		return null;
	}

	public static void createTestProperties(String query_properties) {
		long ms = System.currentTimeMillis();
        Vector w1 = extractAllQueryParameters(query_properties);
        Utils.saveToFile("testcase.properties", w1);
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}

	public static HashMap generateSampleQueries() {
		HashMap hmap = new HashMap();
        HashMap query_hmap = load_property_file(SPARQL_QUERY_PROPERTIES);
        HashMap testcase_hmap = load_property_file(TESTCASE_PROPERTIES);


        return hmap;
	}

	public static void main(String[] args) {
		long ms = System.currentTimeMillis();
		Vector w = extractAllQueryParameters(SPARQL_QUERY_PROPERTIES);
		Utils.dumpVector(TESTCASE_PROPERTIES, w);


		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}
}

