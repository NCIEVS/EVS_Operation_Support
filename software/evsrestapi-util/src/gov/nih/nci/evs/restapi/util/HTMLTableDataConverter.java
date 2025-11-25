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
public class HTMLTableDataConverter {
	String serviceUrl = null;
	String named_graph = null;
	String username = null;
	String password = null;
	String ncit_version = null;

	static String NC_THESAURUS = "NCI_Thesaurus";
	MetadataUtils metadataUtils = null;

	public HTMLTableDataConverter() {

	}

	public HTMLTableDataConverter(String serviceUrl, String named_graph, String username, String password) {
		this.serviceUrl = serviceUrl;
		this.named_graph = named_graph;
		this.username = username;
		this.password = password;
		metadataUtils = new MetadataUtils(serviceUrl, username, password);
		this.ncit_version = metadataUtils.getVocabularyVersion(named_graph);
		System.out.println(this.ncit_version);
	}

	public static boolean isCode(String t) {
		if (t == null) return false;
		if (t.length() == 0) return false;
		char c = t.charAt(0);
		if (c != 'C') return false;
		String s = t.substring(1, t.length());
		try {
			int value = Integer.parseInt(s);
		} catch (Exception ex) {
			return false;
		}
		return true;
	}

	public String convert(String inputfile, String outputfile, String title, String table,
	    Vector th_vec, char delimiter) {
		Vector v = Utils.readFile(inputfile);

		PrintWriter pw = null;
		try {
			pw = new PrintWriter(outputfile, "UTF-8");
			pw.println("<title>" + title);
			pw.println("<table>" + table);
			for (int i=0; i<th_vec.size(); i++) {
				String th = (String) th_vec.elementAt(i);
				pw.println("<th>" + th);
			}
			pw.println("<data>");
			for (int i=1; i<v.size(); i++) {
				String t = (String) v.elementAt(i);
				Vector u = StringUtils.parseData(t, delimiter);
				StringBuffer buf = new StringBuffer();
				for (int j=0; j<u.size(); j++) {
				    String s = (String) u.elementAt(j);
					if (s.indexOf("|") != -1) {
						StringBuffer buf2 = new StringBuffer();
						Vector u2 = StringUtils.parseData(s, '|');
						for (int k=0; k<u2.size(); k++) {
							String s2 = (String) u2.elementAt(k);
							boolean bool = isCode(s2);
							if (bool) {
								s2 = HyperlinkHelper.toHyperlink(s2);
							}
							buf2.append(s2);
							if (k < u2.size()-1) {
								buf2.append("|");
							}
						}
						buf.append(buf2.toString() + delimiter);

					} else {
						boolean bool = isCode(s);
						if (bool) {
							s = HyperlinkHelper.toHyperlink(s);
						}
						buf = buf.append(s);
						if (j < u.size()-1) {
							buf.append("" + delimiter);
						}
					}
				}
				pw.println(buf.toString());
			}
			pw.println("</data>");
			pw.println("</table>");

			pw.println("<footer>(Source: NCI Thesaurus, version " + ncit_version + ")");
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				pw.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return outputfile;
	}

	public String convert(String inputfile) {
		int n = inputfile.lastIndexOf(".");
		String title = inputfile.substring(0, n);
		String table = title;
		Vector v = Utils.readFile(inputfile);
		String firstLine = (String) v.elementAt(0);
		char delimiter = '\t';
		if (firstLine.indexOf("\t") == -1) {
			delimiter = '|';
		}
		Vector u = StringUtils.parseData(firstLine, delimiter);
		int numberOfColumns = u.size();
		Vector th_vec = new Vector();
		for (int i=0; i<numberOfColumns; i++) {
			th_vec.add((String) u.elementAt(i));
		}
		String outputfile = title + "_" + StringUtils.getToday() + ".txt";
	    return convert(inputfile, outputfile,
			title, table,
			th_vec, delimiter);
	}

	public static void main(String[] args) {
		String serviceUrl = args[0];
		String named_graph = args[1];
		String username = args[2];
		String password = args[3];
		String inputfile = args[4];
		String outputfile = new HTMLTableDataConverter(serviceUrl, named_graph, username, password).convert(inputfile);
		System.out.println(outputfile + " generated.");
	}
}
