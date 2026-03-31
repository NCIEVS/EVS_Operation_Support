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

	public static Vector<Integer> delimitedInt2integers(String delim_columns, char delim) {
		Vector w = StringUtils.parseData(delim_columns, delim);
		Vector columns = new Vector();
		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			columns.add(Integer.valueOf(Integer.parseInt(line)));
		}
		return columns;
	}

	public static Vector extract(String filename, String delim_columns, char delim) {
		return extract(filename, delimitedInt2integers(delim_columns, delim), delim);
	}

	public static Vector extract(String filename, Vector<Integer> columns, char delim) {
		Vector data_vec = Utils.readFile(filename);
		return extract(data_vec, columns, delim);
	}

	public static Vector extract(Vector data_vec, String delim_columns, char delim) {
		return extract(data_vec, delimitedInt2integers(delim_columns, delim), delim);
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

	public static HashMap createHashMap(String filename, int keyCol, int valueCol, char delim) {
		Vector data_vec = Utils.readFile(filename);
		return createHashMap(data_vec, keyCol, valueCol, delim);
	}

	public static HashMap createHashMap(Vector data_vec, int keyCol, int valueCol, char delim) {
		HashMap hmap = new HashMap();
		for (int i=0; i<data_vec.size(); i++) {
			String line = (String) data_vec.elementAt(i);
			Vector u = StringUtils.parseData(line, delim);
			String key = (String) u.elementAt(keyCol);
			String value = (String) u.elementAt(valueCol);
			hmap.put(key, value);
		}
		return hmap;
	}

	public static HashMap createHashMap(Vector data_vec, String delim_column_key, String delim_column_value, char delim) {
		Vector<Integer> key_cols = delimitedInt2integers(delim_column_key, delim);
		Vector<Integer> value_cols = delimitedInt2integers(delim_column_value, delim);
		HashMap hmap = new HashMap();
		for (int i=0; i<data_vec.size(); i++) {
			String line = (String) data_vec.elementAt(i);
			Vector u = StringUtils.parseData(line, delim);
			StringBuffer key_buf = new StringBuffer();
			StringBuffer value_buf = new StringBuffer();
			for (int j=0; j<u.size(); j++) {
				Integer int_obj = Integer.valueOf(j);
				if (key_cols.contains(int_obj)) {
					key_buf.append((String) u.elementAt(j)).append(delim);
				} else if (value_cols.contains(int_obj)) {
					value_buf.append((String) u.elementAt(j)).append(delim);
				}
			}
			String t_key = key_buf.toString();
			if (t_key.endsWith("" + delim)) {
				t_key = t_key.substring(0, t_key.length()-1);
			}
			String t_value = value_buf.toString();
			if (t_value.endsWith("" + delim)) {
				t_value = t_value.substring(0, t_value.length()-1);
			}
			Vector w = new Vector();
			if (hmap.containsKey(t_key)) {
				w = (Vector) hmap.get(t_key);
			}
			w.add(t_value);
			hmap.put(t_key, w);
		}
		return hmap;
	}

}
