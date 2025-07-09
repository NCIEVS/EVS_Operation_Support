package gov.nih.nci.evs.restapi.util;
import java.io.*;
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
public class HyperlinkHelper {
	private static String HYPER_LINK = "https://evsexplore.semantics.cancer.gov/evsexplore/concept/ncit/";

	public static String toHyperlink(String code) {
		if (isNCItCode(code)) {
			return toHyperlink(HYPER_LINK, code);
		} else {
			return code;
		}
	}


	public static boolean isNCItCode(String code) {
		char c = code.charAt(0);
		if (c != 'C') return false;
		String s = code.substring(1, code.length());
		try {
			int n = Integer.parseInt(s);
			return true;
		} catch(Exception ex) {

		}
		return false;
	}

	public static String toHyperlink(String hyperlink, String code) {
		if (hyperlink == null) return code;
		StringBuffer buf = new StringBuffer();
		buf.append("<a ");
		buf.append("href=");
		buf.append("\"");
		buf.append(hyperlink + code);
		buf.append("\">");
		buf.append(code);
		buf.append("</a>");
		return buf.toString();
	}

	public static void addHyperlinks(String filename, String hyperlinkfile) {
	    Vector v = Utils.readFile(filename);
	    Vector v2 = Utils.readFile(hyperlinkfile);
	    addHyperlinks(filename, v2);
	}

	public static void addHyperlinks(String filename, Vector v2) {
	    Vector v = Utils.readFile(filename);
	    String t = (String) v2.elementAt(0);
	    Vector hyperlinks = StringUtils.parseData(t, '|');
	    Vector w0 = new Vector();
	    w0.add((String) v.elementAt(0));
	    Vector w = new Vector();
	    for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			StringBuffer buf = new StringBuffer();
			Vector u = StringUtils.parseData(line, '|');
			for (int j=0; j<u.size(); j++) {
				buf.append(toHyperlink((String) hyperlinks.elementAt(j),
				                       (String) u.elementAt(j)));
				if (j < u.size()-1) {
				    buf.append("|");
				}
			}
			w.add(buf.toString());
		}
		w = new SortUtils().quickSort(w);
		w0.addAll(w);
		Utils.saveToFile(filename, w0);
	}

	public static void main(String[] args) {
		String code = "C12345";
		System.out.println(toHyperlink(code));
	}
}
