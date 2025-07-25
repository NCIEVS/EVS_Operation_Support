package gov.nih.nci.evs.restapi.util;
import java.io.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
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
public class DelimitedDataExtractor {

	public static HashMap extract(String filename, int keyCol, int valueCol, char delim) {
		Vector w = Utils.readFile(filename);
		HashMap hmap = new HashMap();
		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			Vector u = StringUtils.parseData(line, delim);
			String key = (String) u.elementAt(keyCol);
			String value = (String) u.elementAt(valueCol);
			hmap.put(key, value);
		}
		return hmap;
	}

	public static Vector extract(String filename, String delim_columns, char delim) {
		Vector w = StringUtils.parseData(delim_columns, delim);
		Vector columns = new Vector();
		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			columns.add(Integer.valueOf(Integer.parseInt(line)));
		}
		return extract(filename, columns, delim);
	}

	public static Vector extract(String filename, Vector<Integer> columns, char delim) {
		Vector w = Utils.readFile(filename);
		Vector v = new Vector();
		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			Vector u = StringUtils.parseData(line, delim);
			StringBuffer buf = new StringBuffer();
			for (int k=0; k<columns.size(); k++) {
				Integer column_obj = (Integer) columns.elementAt(k);
				int column_int = Integer.parseInt(column_obj.toString());
				String t = (String) u.elementAt(column_int);
				buf.append(t).append(delim);
			}
			String s = buf.toString();
			s = s.substring(0, s.length()-1);
			v.add(s);
		}
		return v;
	}

	public static Vector extract(Vector data_vec, String delim_columns, char delim) {
		Vector w = StringUtils.parseData(delim_columns, delim);
		Vector columns = new Vector();
		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			columns.add(Integer.valueOf(Integer.parseInt(line)));
		}
		return extract(data_vec, columns, delim);
	}

	public static Vector extract(Vector w, Vector<Integer> columns, char delim) {
		Vector v = new Vector();
		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			Vector u = StringUtils.parseData(line, delim);
			StringBuffer buf = new StringBuffer();
			for (int k=0; k<columns.size(); k++) {
				Integer column_obj = (Integer) columns.elementAt(k);
				int column_int = Integer.parseInt(column_obj.toString());
				String t = (String) u.elementAt(column_int);
				buf.append(t).append(delim);
			}
			String s = buf.toString();
			s = s.substring(0, s.length()-1);
			v.add(s);
		}
		return v;
	}

	public static void main(String[] args) throws Exception {
		String serviceUrl = args[0];
		String named_graph = args[1];
		System.out.println("serviceUrl: " + serviceUrl);
		System.out.println("named_graph: " + named_graph);
		OWLSPARQLUtils owlSPARQLUtils = new OWLSPARQLUtils(serviceUrl, null, null);
        owlSPARQLUtils.set_named_graph(named_graph);
        Vector v = owlSPARQLUtils.getDomainAndRangeData(named_graph);
        Vector trimmed_v = extract(v, "2|0|4", '|');
        Utils.saveToFile("trimmed_domain_range.txt", trimmed_v);
	}
}
