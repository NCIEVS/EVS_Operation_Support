package gov.nih.nci.evs.restapi.util;
import gov.nih.nlm.nls.lvg.Api.*;
import gov.nih.nlm.nls.lvg.Lib.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
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
public class NCItAsciiApiUtils {

    static String NAMESPACE = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#";
    static String NAMESPACE_TARGET = "<!-- " + NAMESPACE;
    static String OWL_CLS_TARGET = NAMESPACE_TARGET + "C";

    public static String extractClassId(String line) {
        String t = line;
        String classId = null;
		if (t.indexOf(NAMESPACE_TARGET) != -1 && t.endsWith("-->")) {
			int n = t.lastIndexOf("#");
			t = t.substring(n, t.length());
			n = t.lastIndexOf(" ");
			classId = t.substring(1, n);
		}
		return classId;
	}

	public static Vector getLinesWithSpecialChars(Vector lines) {
		Vector w = new Vector();
		for (int i=0; i<lines.size(); i++) {
			String line = (String) lines.elementAt(i);
			try {
				byte[] utf8Bytes = line.getBytes("UTF-8");
				for (int j=0; j<utf8Bytes.length; j++) {
					Byte b = utf8Bytes[j];
					if (!(b >=0 && b<=127)) {
						w.add(line);
						break;
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return w;
	}

    public static Vector scan(Vector v) {
		Vector w = new Vector();
		w.add("Line Number\tNCIt Code\tValue");
		String classId = null;
		String label = null;
		for (int i=0; i<v.size(); i++) {
			String t = (String) v.elementAt(i);
			String line = t.trim();

			if (t.indexOf(NAMESPACE_TARGET) != -1 && t.endsWith("-->")) {
				classId = extractClassId(t);
			}
			String t3 = t.trim();
            boolean bool = false;
			try {
				byte[] utf8Bytes = line.getBytes("UTF-8");
				for (int j=0; j<utf8Bytes.length; j++) {
					Byte b = utf8Bytes[j];
					if (!(b >=0 && b<=127)) {
						int k = i+1;
						w.add("" + k + "\t" + classId + "\t" + t3);
						break;
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return w;
	}

	public static Vector getLinesWithNonidentifiableChars(String filename) {
		return ASCIITable.getLinesWithNonidentifiableChars(filename);
	}

	public static Vector getLinesWithNonidentifiableChars(Vector v) {
		return ASCIITable.getLinesWithNonidentifiableChars(v);
	}

    public static Vector findLinesWithNonidentifiableChars(String filename) {
		File f = new File(filename);
		if (!f.exists()) {
			System.out.println("File " + filename + " not found.");
			return null;
		}
		Vector w = new Vector();
		w.add("Line Number\tNCIt Code\tValue");
		Vector v = SpecialCharDetector.readFile(filename, false);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (line.startsWith("?")) {
				String t = (String) v.elementAt(i-1);
				w.add(t);
			}
		}
		return w;
	}

    public static void generateReports(String owlfile) {
		Vector w = scan(SpecialCharDetector.readFile(owlfile));
		int n = owlfile.lastIndexOf(".");
		String outputfile = "nonascii_" + owlfile.substring(0, n) + "_v1.txt";
		Utils.saveToFile(outputfile, w);
		w = getLinesWithNonidentifiableChars(w);
		outputfile = "nonascii_" + owlfile.substring(0, n) + "_v2.txt";
		Utils.saveToFile(outputfile, w);
		w = findLinesWithNonidentifiableChars(outputfile);
		outputfile = "nonascii_" + owlfile.substring(0, n) + "_v3.txt";
		Utils.saveToFile(outputfile, w);
		Text2Excel.generateExcel(outputfile, '\t');
	}

    public static void main(String[] args) {
		String owlfile = args[0];
		generateReports(owlfile);
    }
}