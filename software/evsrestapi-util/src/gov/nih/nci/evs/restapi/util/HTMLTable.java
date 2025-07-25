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
public class HTMLTable {
    //static String NCIT_URL = "https://nciterms.nci.nih.gov/ncitbrowser/pages/concept_details.jsf?dictionary=NCI_Thesaurus&&ns=ncit&code=";
    static String NCIT_URL = "https://evsexplore.semantics.cancer.gov/evsexplore/concept/ncit/";

    public static String hyperlink(String url, String code) {
		return "<a href=\"" + url + code + "\">" + code + "</a>";
	}

	public static String generate(Vector v) {
		char delim = searchDelimiter(v);
		return generate(v, delim);
	}

	public static String generate(Vector v, char delim) {
		String title = null;
		String table = null;
		String outputfile = null;
		PrintWriter pw = null;
		Vector th_vec = null;
		String th = null;
		Vector data_vec = new Vector();
		String footer = null;
		for (int i=0; i<v.size(); i++) {
			String t = (String) v.elementAt(i);
			t = t.trim();
			if (t.indexOf("<title>") != -1) {
				int n = t.indexOf("<title>");
				title = t.substring(n + "<title>".length(), t.length());
				outputfile = title + ".html";
				outputfile = outputfile.replace("/", "_");
				try {
					pw = new PrintWriter(outputfile, "UTF-8");
				} catch (Exception ex) {
				    ex.printStackTrace();
				}
                printHeader(pw, title);
				table = null;
			} else if (t.startsWith("<table>")) {
				table = t.substring("<table>".length(), t.length());
				th_vec = new Vector();
			} else if (t.startsWith("<th>")) {
				th = t.substring("<th>".length(), t.length());
				th_vec.add(th);
			} else if (t.startsWith("<data>")) {
				data_vec = new Vector();
			} else if (t.startsWith("</table>")) {
				printTable(pw, table, th_vec, data_vec, delim);
				table = null;
                th_vec = new Vector();
				data_vec = new Vector();
			} else if (t.startsWith("<footer>")) {
				footer = t.substring("<footer>".length(), t.length());
			} else {
				if (t.indexOf("</data>") == -1) {
					data_vec.add(t);
				}
			}
		}
		try {
			printFooter(pw, footer);
			pw.close();
			System.out.println("Output file " + outputfile + " generated.");
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return outputfile;
	}

	public static void printBanner(PrintWriter out) {
		out.println("<div>");
		out.println("  <img");
		//out.println("      src=\"https://nciterms.nci.nih.gov/ncitbrowser/images/evs-logo-swapped.gif\"");
		out.println("      src=\"https://www.cancer.gov//sites/g/files/xnrzdm211/files/ncids_header/logos/Logo_NCI.svg\"");
		out.println("      alt=\"EVS Logo\"");
		out.println("      width=\"100%\"");
		out.println("      height=\"26\"");
		out.println("      border=\"0\"");
		out.println("      usemap=\"#external-evs\"");
		out.println("  />");
		out.println("  <map id=\"external-evs\" name=\"external-evs\">");
		out.println("    <area");
		out.println("        shape=\"rect\"");
		out.println("        coords=\"0,0,140,26\"");
		out.println("        href=\"/ncitbrowser/start.jsf\"");
		out.println("        target=\"_self\"");
		out.println("        alt=\"NCI Term Browser\"");
		out.println("    />");
		out.println("    <area");
		out.println("        shape=\"rect\"");
		out.println("        coords=\"520,0,941,26\"");
		out.println("        href=\"http://evs.nci.nih.gov/\"");
		out.println("        target=\"_blank\"");
		out.println("        alt=\"Enterprise Vocabulary Services\"");
		out.println("    />");
		out.println("  </map>");
		out.println("</div>");
	}

    public static void printHeader(PrintWriter out, String pageTitle) {
		/*
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">");
		out.println("<html xmlns:c=\"http://java.sun.com/jsp/jstl/core\">");
		out.println("<head>");
		out.println("<title>" + pageTitle + "</title>");
		out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
		out.println("<style>");
		out.println("table {");
		out.println("    border-collapse: collapse;");
		out.println("}");
		out.println("table, td, th {");
		out.println("    border: 1px solid black;");
		out.println("}");
		out.println("</style>");
		out.println("</head>");
		out.println("");
		out.println("<body>");
		out.println("");
		out.println("");
		out.println(" <!-- nci banner<div class=\"ncibanner\"> -->");
		out.println("<div style='clear:both;margin-top:-5px;padding:8px;height:32px;color:white;background-color:#C31F40'>");
		out.println("  <a href=\"http://www.cancer.gov\" target=\"_blank\">");
		out.println("    <img");
		//out.println("        src=\"https://nciterms.nci.nih.gov/ncitbrowser/images/banner-red.png\"");
		//out.println("        src=\"https://nciterms.nci.nih.gov/ncitbrowser/images/nci-banner-1.gif\"");
		out.println("      src=\"https://www.cancer.gov//sites/g/files/xnrzdm211/files/ncids_header/logos/Logo_NCI.svg\"");
		out.println("        width=\"955\"");
		out.println("        height=\"39\"");
		out.println("        border=\"0\"");
		out.println("        alt=\"National Cancer Institute\"");
		out.println("    />");
		out.println("  </a>");
		out.println("</div>");
		out.println("<!-- end nci banner -->");
		out.println("");

		//printBanner(out);

		out.println("");
		out.println("");
		out.println("<center>");
		out.println("<h1>" + pageTitle + "</h1>");
		*/
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">");
		out.println("<html xmlns:c=\"http://java.sun.com/jsp/jstl/core\">");
		out.println("<head>");
		out.println("<title>test_insert_after</title>");
		out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
		out.println("<style>");
		out.println("table {");
		out.println("    border-collapse: collapse;");
		out.println("}");
		out.println("table, td, th {");
		out.println("    border: 1px solid black;");
		out.println("}");
		out.println("</style>");
		out.println("</head>");
		out.println("<body>");
		out.println("<div>");
		out.println("  <center>");
		out.println("  <img");
		out.println("      src=\"https://www.cancer.gov//sites/g/files/xnrzdm211/files/ncids_header/logos/Logo_NCI.svg\"");
		out.println("      alt=\"EVS Logo\"");
		out.println("      width=\"955\"");
		out.println("      height=\"39\"");
		out.println("      border=\"0\"");
		out.println("      alt=\"National Cancer Institute\"");
		out.println("      usemap=\"#external-evs\"");
		out.println("  />");
		out.println("  </center>");
		out.println("</div>");
		out.println("<center>");
		out.println("<h1>" + pageTitle + "</h1>");
		out.println("<p>");
		String today = StringUtils.getToday("MM-dd-yyyy");
		out.println("<h2>(Last modified: " + today + ")</h2>");
		out.println("</p>");
		out.println("</center>");
	}

	public static boolean isWideField(String th) {
		th = th.toLowerCase();
		if (th.indexOf("label") != -1 || th.indexOf("term") != -1
		   || th.indexOf("name") != -1 || th.indexOf("description") != -1
		   || th.indexOf("definition") != -1) {
			return true;
		}
		return false;
	}

	public static int calculateWideFieldWidth(int numFields, int numWideFields) {
		int total = 100;
		int width = 10;
		int remaining_width = 100 - 10 * (numFields - numWideFields);
		return (int) (remaining_width / numWideFields + 0.5);
	}

	public static int getWidth(int numFields, int numWideFields, String th) {
		if (!isWideField(th)) return 10;
		return calculateWideFieldWidth(numFields, numWideFields);
	}

	public static char searchDelimiter(Vector data_vec) {
		for (int i=0; i<data_vec.size(); i++) {
			String t = (String) data_vec.elementAt(i);
			if (t.indexOf("\t") != -1) {
				return '\t';
			} else if (t.indexOf("|") != -1) {
				return '|';
			}
		}
		return '\t';
	}

    public static void printTable(PrintWriter out,
        String tableLabel,
        Vector th_vec,
        Vector data_vec,
        char delim) {

		int num_wide_fields = 0;
		int num_fields = th_vec.size();
		for (int i=0; i<th_vec.size(); i++) {
			String th = (String) th_vec.elementAt(i);
		    th = th.toLowerCase();
		    if (th.indexOf("label") != -1 || th.indexOf("term") != -1
		       || th.indexOf("name") != -1 || th.indexOf("description") != -1
		       || th.indexOf("definition") != -1) {
				num_wide_fields++;
			}
		}

		if (out == null) {
			System.out.println("out is NULL???");
		}

		out.println("");
		out.println("<div>");
		out.println("<center>");
		out.println("<h3>" + tableLabel + "</h3>");
		out.println("<table>");
        out.println("<tr>");
		for (int i=0; i<th_vec.size(); i++) {
			String th = (String) th_vec.elementAt(i);
			out.println("<th>");
			out.println(th);
			out.println("</th>");
		}
		out.println("</tr>");

		for (int i=0; i<data_vec.size(); i++) {
			String data = (String) data_vec.elementAt(i);
			//System.out.println(data);

			out.println("<tr>");
			//Vector u = StringUtils.parseData(data, '\t');
			Vector u = StringUtils.parseData(data, delim);

			for (int j=0; j<u.size(); j++) {
				String value = (String) u.elementAt(j);
				int percent = getWidth(num_fields, num_wide_fields, (String) th_vec.elementAt(j));
				out.println("<td width=\"" + percent + "%\">");
				out.println(value);
				out.println("</td>");
			}
			out.println("</tr>");
		}
		out.println("</table>");
		out.println("</div>");
		out.println("");
	}


    public static void printFooter(PrintWriter out) {
		printFooter(out, null);
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


    public static void printFooter(PrintWriter out, String footer) {
		out.println("</div>");
        out.println("<br></br>");
        out.println("<br></br>");
        out.println("<center><b>");
		if(footer != null) {
			out.println(footer);
		}
		out.println("</b></center>");
		out.println("</body>");
		out.println("</html>");
    }

	public void generateHTMLPage(String filename) {
		String hyperlink_url = NCIT_URL;
		generateHTMLPage(filename, hyperlink_url);
	}

	public void generateHTMLPage(String filename, String hyperlink_url) {
        Vector w1 = Utils.readFile(filename);
        Vector w = new Vector();
		for (int i=1; i<w1.size(); i++) {
			String line = (String) w1.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			StringBuffer buf = new StringBuffer();
			for (int j=0; j<u.size(); j++) {
				String s = (String) u.elementAt(j);
				if (HTMLTableDataConverter.isCode(s)) {
					buf.append(hyperlink(hyperlink_url, s)).append("|");
				} else {
					buf.append(s).append("|");
				}
			}
			String t = buf.toString();
			t = t.substring(0, t.length()-1);
			w.add(t);
		}
        Vector w0 = new Vector();
        String heading = (String) w1.elementAt(0);
        Vector u = StringUtils.parseData(heading, '|');
        StringBuffer buf = new StringBuffer();
        for (int i=0; i<u.size(); i++) {
			String t = (String) u.elementAt(i);
			buf.append(t).append("|");
		}
		String t = buf.toString();
		t = t.substring(0, t.length()-1);
        w0.add(t);
        w0.addAll(w);
        int n = filename.lastIndexOf(".");
        String datafile = filename.substring(0, n) + "_" + StringUtils.getToday() + ".txt";
        Utils.saveToFile(datafile, w0);
        String outputfile = new HTMLTableDataConverter().convert(datafile);
		Vector v = Utils.readFile(outputfile);
		outputfile = new HTMLTable().generate(v, '\t');
		System.out.println(outputfile + " generated.");
	}

    public static void delimitedText2HTMLTable(String inputfile) {
        char delim = '\t';
        delimitedText2HTMLTable(inputfile, delim);
	}

    public static void delimitedText2HTMLTable(String inputfile, char delim) {
		String outputfile = new HTMLTableDataConverter().convert(inputfile);
		System.out.println(outputfile + " generated.");
		Vector v = Utils.readFile(outputfile);
		String htmlfile = new HTMLTable().generate(v, delim);
		System.out.println(htmlfile + " generated.");
	}

	public static void main(String[] args) {
		String datafile = args[0];
		Vector v = Utils.readFile(datafile);
		datafile = new HTMLTable().generate(v, '|');
		System.out.println(datafile + " generated.");
	}
}
