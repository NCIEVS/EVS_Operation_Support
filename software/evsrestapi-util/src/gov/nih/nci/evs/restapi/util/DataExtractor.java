package gov.nih.nci.evs.restapi.util;

import java.io.*;
import java.text.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;
import java.nio.charset.Charset;

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


public class DataExtractor {

	public static HashSet extractColumnData(String filename, int col, char delim) {
		Vector v = Utils.readFile(filename);
		HashSet hset = new HashSet();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, delim);
			String value = (String) u.elementAt(col);
			if (!hset.contains(value)) {
				hset.add(value);
			}
		}
		return hset;
	}

	public static String extractRowData(String line, Vector<Integer> col_vec, char delim) {
		Vector u = StringUtils.parseData(line, delim);
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<col_vec.size(); i++) {
			Integer int_obj = col_vec.elementAt(i);
			int k = Integer.parseInt(int_obj.toString());
			buf.append((String) u.elementAt(k)).append(delim);
		}
		String s = buf.toString();
		if (col_vec.size() > 0) {
			s = s.substring(0, s.length()-1);
		}
		return s;
	}

	public static void dumpHeading(String filename) {
		char delim = '\t';
		dumpHeading(filename, delim);
	}

	public static void dumpHeading(String filename, char delim) {
		Vector v = Utils.readFile(filename);
        String line = (String) v.elementAt(0);
        Vector u = StringUtils.parseData(line, delim);
        Utils.dumpVector(line, u);
	}

	public static Vector extractColumnData(String filename, Vector<Integer> col_vec, char delim) {
		Vector v = Utils.readFile(filename);
        Vector w = new Vector();
        String heading = extractRowData((String) v.elementAt(0), col_vec, delim);
        w.add(heading);
		for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			line = line.trim();
			if (line.length() > 0) {
				line = extractRowData((String) v.elementAt(i), col_vec, delim);
				w.add(line);
			}
		}
		return w;
	}

	public static Vector extractColumnData(String filename, String delimited_col_str, char delim) {
		Vector<Integer> col_vec = new Vector();
		Vector u = StringUtils.parseData(delimited_col_str, '|');
		for (int i=0; i<u.size(); i++) {
			String t = (String) u.elementAt(i);
			int col = Integer.parseInt(t);
		    col_vec.add(Integer.valueOf(col));
		}
	    return extractColumnData(filename, col_vec, delim);
	}

	public static Vector insertColumnData(Vector v, Vector values, int col) {
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '\t');
			String value = (String) values.elementAt(i);
			StringBuffer buf = new StringBuffer();
			for (int j=0; j<col; j++) {
				buf.append((String) u.elementAt(j)).append("\t");
			}
			buf.append(value).append("\t");
			for (int j=col; j<u.size(); j++) {
				buf.append((String) u.elementAt(j)).append("\t");
			}
			String s = buf.toString();
			s = s.substring(0, s.length()-1);
			w.add(s);
		}
		return w;
	}

    public static Vector rearrageColumnData(String filename, String col_str, char delim) {
		Vector v = Utils.readFile(filename);
		Vector w = new Vector();
		Vector col_indexes = StringUtils.parseData(col_str, '|');
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '\t');
			int num_cols = u.size();
			StringBuffer buf = new StringBuffer();
			for (int j=0; j<num_cols; j++) {
				int n = Integer.parseInt((String) col_indexes.elementAt(j));
				String value = (String) u.elementAt(n);
				buf.append(value).append("\t");
			}
			String s = buf.toString();
			s = s.substring(0, s.length()-1);
			w.add(s);
		}
		return w;
	}

	public static void main(String[] args) {
		long ms = System.currentTimeMillis();
		String filename = args[0];
		dumpHeading(filename);
		String col_str = args[1];
		char delim = '\t';
		Vector v = extractColumnData(filename, col_str, delim);
	    int n = filename.lastIndexOf(".");
	    String outputfile = "data_" + filename.substring(0, n) + ".txt";
		Utils.saveToFile(outputfile, v);
		System.out.println("Output file " + outputfile + " generated.");
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}
}

