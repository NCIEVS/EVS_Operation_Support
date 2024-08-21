package gov.nih.nci.evs.restapi.util;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.text.*;
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
public class XMLEditor {

	public static void saveToFile(PrintWriter pw, Vector v) {
		if (v != null && v.size() > 0) {
			for (int i=0; i<v.size(); i++) {
				String t = (String) v.elementAt(i);
				pw.println(t);
			}
		}
	}

	public static Vector readFile(String datafile) {
		Vector v = new Vector();
        try {
			File file = new File(datafile);
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			BufferedReader br = null;
			try {
				br = br = new BufferedReader(new InputStreamReader(bis));
			} catch (Exception ex) {
				return null;
			}

            while (true) {
                String line = br.readLine();
				if (line == null) {
					break;
				}
				v.add(line);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return v;
	}

    public static void saveToFile(String outputfile, Vector v) {
        try {
            FileOutputStream output = new FileOutputStream(outputfile);
            for (int i=0; i<v.size(); i++) {
				String data = (String) v.elementAt(i);
				if (i < v.size()) {
					data = data + "\n";
				}
				byte[] array = data.getBytes();
				output.write(array);
			}
            output.close();
        } catch(Exception e) {
            e.getStackTrace();
        }
    }

	public static String extractTagValue(String line, String tag, String close_tag) {
		int n1 = line.indexOf(tag);
		int n2 = line.indexOf(close_tag);
		if (n1 == -1 || n2 == -1) return null;
		return line.substring(n1 + tag.length(), n2);
	}

    public static String getTag(String line) {
		String t = line;
		t = t.trim();
		if (t.startsWith("<") && t.endsWith(">")) {
			int n = t.indexOf(">");
			String s = t.substring(1, n);
			return s;
		}
		return null;
	}

	public static String replace(String t) {
		t = t.replace(">=", "&gt;=");
		t = t.replace("<=", "&lt;=");
		t = t.replace("<", "&lt;");
		t = t.replace(">", "&gt;");
		return t;
	}

	public static void run(String xmlfile, String outputfile) {
		Vector v = readFile(xmlfile);
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			String s = getTag(line);
			String tag = "<" + s + ">";
			String close_tag = "</" + s + ">";
			if (s != null) {
				if (line.endsWith(close_tag)) {
					int indent = line.indexOf(tag);
					String value = extractTagValue(line, tag, close_tag);
					if (value != null) {
						w.add(line.substring(0, indent) + tag + replace(value) + close_tag);
					}
				} else {
					w.add(line);
				}
			} else {
				w.add(line);
			}
		}
		saveToFile(outputfile, w);
		System.out.println("Outputfile " + outputfile + " generated.");
	}

	public static void main(String[] args) {
		long ms = System.currentTimeMillis();
		String xmlfile = args[0];
		String outputfile = args[1];
		run(xmlfile, outputfile);
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}
}

