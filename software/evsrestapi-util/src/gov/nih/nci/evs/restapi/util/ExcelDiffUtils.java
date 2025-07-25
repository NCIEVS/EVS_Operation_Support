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
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;
import java.text.*;

/**
 * <!-- LICENSE_TEXT_START -->
 * Copyright 2020 MSC. This software was developed in conjunction
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
 *      "This product includes software developed by MSC and the National
 *      Cancer Institute."   If no such end-user documentation is to be
 *      included, this acknowledgment shall appear in the software itself,
 *      wherever such third-party acknowledgments normally appear.
 *   3. The names "The National Cancer Institute", "NCI" and "MSC" must
 *      not be used to endorse or promote products derived from this software.
 *   4. This license does not authorize the incorporation of this software
 *      into any third party proprietary programs. This license does not
 *      authorize the recipient to use any trademarks owned by either NCI
 *      or MSC
 *   5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED
 *      WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *      OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE
 *      DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
 *      MSC, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT,
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


public class ExcelDiffUtils {
    public String datafile1 = null;
    public String datafile2 = null;
    Vector headings = null;
    //boolean NORMALIZE = true;
    boolean NORMALIZE = false;
    static int HEADING_ROW = 0;

    public ExcelDiffUtils() {

	}

	public static void set_HEADING_ROW(int row) {
		HEADING_ROW = row;
	}


    public ExcelDiffUtils(String datafile1, String datafile2) {
		this.datafile1 = datafile1;
		this.datafile2 = datafile2;
	}

	public Vector split(String line) {
		return StringUtils.parseData(line, '\t');
	}

	public Vector split(String line, char delim) {
		return StringUtils.parseData(line, delim);
	}


	public void run(PrintWriter pw) {
		Vector v1 = Utils.readFile(datafile1);
		String heading = (String) v1.elementAt(HEADING_ROW);

		headings = split(heading);
		Utils.dumpVector(heading, headings);

		HashSet codes_1 = new HashSet();
		HashMap hmap_1 = new HashMap();
		for (int i=HEADING_ROW+1; i<v1.size(); i++) {
			String line = (String) v1.elementAt(i);
			line = line.trim();
			if (line.length() >  0) {
				Vector u = split(line, '\t');
				String code = (String) u.elementAt(2);
				if (!codes_1.contains(code)) {
					codes_1.add(code);
				}
				HashMap hmap = new HashMap();
				for (int j=0; j<u.size(); j++) {
					String value = (String) u.elementAt(j);
					hmap.put((String) headings.elementAt(j), value);
				}
				hmap_1.put(code, hmap);
			}
		}
		System.out.println("**hmap_1****" + hmap_1.keySet().size());
        System.out.println(datafile2);
		Vector v2 = Utils.readFile(datafile2);
		System.out.println(v2.size());
		heading = (String) v2.elementAt(HEADING_ROW);
		heading = heading.trim();
		headings = split(heading);

		HashSet codes_2 = new HashSet();
		HashMap hmap_2 = new HashMap();
		for (int i=HEADING_ROW+1; i<v2.size(); i++) {
			String line = (String) v2.elementAt(i);
			line = line.trim();
			if (line.length() > 0) {
				Vector u = split(line, '\t');
				String code = (String) u.elementAt(2);
				if (!codes_2.contains(code)) {
					codes_2.add(code);
				}
				HashMap hmap = new HashMap();
				for (int j=0; j<u.size(); j++) {
					String value = (String) u.elementAt(j);
					hmap.put((String) headings.elementAt(j), value);
				}
				hmap_2.put(code, hmap);
			}
		}
		System.out.println("**hmap_2****" + hmap_2.keySet().size());

        pw.println("\nOld file " + datafile1);
        pw.println("New file " + datafile2);
        pw.println("\n");

        pw.println("Edit action descriptions: ");
        pw.println("\tadded: data in " + datafile2 + " but not in " + datafile1);
        pw.println("\tdeleted: data in " + datafile1 + " but not in " + datafile2);

        dumpVector(pw, "\nHeadings", headings);
        pw.println("\nEdit actions:");
		compare(pw, hmap_1, hmap_2);
	}

	public void dumpVector(PrintWriter pw, String label, Vector v) {
	    pw.println(label);
	    for (int i=0; i<v.size(); i++) {
			int j = i+1;
			pw.println("(" + j + ") " + (String) v.elementAt(i));
		}
	}

    public void dumpHashMap(HashMap hmap) {
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String code = (String) it.next();
			System.out.println(code);
		    HashMap map = (HashMap) hmap.get(code);
			Iterator it1 = map.keySet().iterator();
			while (it1.hasNext()) {
				String key = (String) it1.next();
				String value = (String) map.get(key);
				System.out.println("\t" + key + " --> " + value);
			}
		}
	}

	public String normalize(String t) {
		if (t.indexOf("||") == -1) return t;
		t = t.replace(" || ", "|");
		Vector u = StringUtils.parseData(t, '|');
		u = new SortUtils().quickSort(u);
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<u.size(); i++) {
			String s = (String) u.elementAt(i);
			buf.append(s);
			if (i < u.size()-1) {
				buf.append(" || ");
			}
		}
		return buf.toString();
	}

	public void compareRowValues(PrintWriter pw, String code, String key, String value_1, String value_2) {
		String delimiter = " || ";
		Vector u1 = null;
		if (value_1.indexOf(delimiter) != -1) {
			value_1 = value_1.replace(delimiter, "$");
			u1 = StringUtils.parseData(value_1, '$');
			u1 = new SortUtils().quickSort(u1);
		}
		Vector u2 = null;
		if (value_2.indexOf(delimiter) != -1) {
			value_2 = value_2.replace(delimiter, "$");
			u2 = StringUtils.parseData(value_2, '$');
			u2 = new SortUtils().quickSort(u2);
		}
		for (int i=0; i<u1.size(); i++) {
			String value = (String) u1.elementAt(i);
			if (!u2.contains(value)) {
				pw.println("deleted|" + code + "|" + key + "|" + value);
			}
		}
		for (int i=0; i<u2.size(); i++) {
			String value = (String) u2.elementAt(i);
			if (!u1.contains(value)) {
				pw.println("added|" + code + "|" + key + "|" + value);
			}
		}
	}

	public void compareRow(PrintWriter pw, String code, HashMap hmap_1, HashMap hmap_2) {
		Iterator it1 = hmap_1.keySet().iterator();
		while (it1.hasNext()) {
			String key = (String) it1.next();
			String value_1 = (String) hmap_1.get(key);
			String value_2 = (String) hmap_2.get(key);

            if (NORMALIZE) {
				value_1 = normalize(value_1);
				value_2 = normalize(value_2);
			}

			if (value_1.compareTo(value_2) != 0) {
				value_1 = (String) hmap_1.get(key);
				value_2 = (String) hmap_2.get(key);

				System.out.println(value_1);
				System.out.println(value_2);

				// to be modified:
				//pw.println("added|" + code + "|" + key + "|" + value_1);
				//pw.println("deleted|" + code + "|" + key + "|" + value_2);
				compareRowValues(pw, code, key, value_1, value_2);
			}
		}
	}

	public void compare(PrintWriter pw, HashMap hmap_1, HashMap hmap_2) {
		int n1 = hmap_1.keySet().size();
		int n2 = hmap_2.keySet().size();

		//hmap_1: Old, hmap_2: New
		Iterator it1 = hmap_1.keySet().iterator();

		//In Old, but not in New
		while (it1.hasNext()) {
			String code = (String) it1.next();
			if (!hmap_2.containsKey(code)) {
				pw.println("deleted|" + code);
			}
		}
		//In Old, but not in New
        Iterator it2 = hmap_2.keySet().iterator();
		while (it2.hasNext()) {
			String code = (String) it2.next();
			if (!hmap_1.containsKey(code)) {
				pw.println("added|" + code);
			}
		}

		it2 = hmap_2.keySet().iterator();
		while (it2.hasNext()) {
			String code = (String) it2.next();
			////In both Old and New
			if (hmap_1.containsKey(code) && hmap_2.containsKey(code)) {
				HashMap map_1 = (HashMap) hmap_2.get(code);
				HashMap map_2 = (HashMap) hmap_1.get(code);
                compareRow(pw, code, map_1, map_2);
			}
		}
	}

	public static void dumpData(String filename) {
		Vector w = Utils.readFile(filename);
		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			Vector u = StringUtils.parseData(line, '\t');
			Utils.dumpVector(line, u);
		}
	}


    public static String exportExcelSheet(String excelfile) {
        int n = excelfile.lastIndexOf(".");
        String textfile = excelfile.substring(0, n) + ".txt";
        int sheet_num = 0;
		Vector w = ExcelReader.toDelimited(excelfile, sheet_num, '\t');
		Utils.saveToFile(textfile, w);
	    return textfile;
    }


	public static String exportXLSFile(String excelFilePath) {
		int n = excelFilePath.lastIndexOf(".");
		String textfile = excelFilePath.substring(0, n) + ".txt";
		try {
			Vector w = ExcelReadWriteUtils.readXLSFile(excelFilePath);
			Utils.saveToFile(textfile , w);
			System.out.println(textfile + " generated.");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return textfile;
	}

    public void run() {
		run(this.datafile1, this.datafile2);
	}

    public void run(String datafile1, String datafile2) {
		long ms = System.currentTimeMillis();
		/*
		if (datafile1.endsWith(".xls")) {
			datafile1 = exportXLSFile(datafile1);
		}
		if (datafile2.endsWith(".xls")) {
			datafile2 = exportXLSFile(datafile2);
		}
		*/
		PrintWriter pw = null;
		int n1 = datafile1.lastIndexOf(".");
		int n2 = datafile2.lastIndexOf(".");
		String outputfile = datafile1.substring(0, n1) + "_" + datafile2.substring(0, n2) + ".txt";
		try {
			pw = new PrintWriter(outputfile, "UTF-8");
            run(pw);

		} catch (Exception ex) {

		} finally {
			try {
				pw.close();
				System.out.println("Output file " + outputfile + " generated.");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

        System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}

    public static void main(String[] args) {
		long ms = System.currentTimeMillis();
		String datafile1 = args[0];
		String datafile2 = args[1];
		new ExcelDiffUtils(datafile1, datafile2).run();
        System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}
}