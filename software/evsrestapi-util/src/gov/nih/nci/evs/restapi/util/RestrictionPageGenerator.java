package gov.nih.nci.evs.restapi.util;
import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

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
public class RestrictionPageGenerator extends HttpServlet {
	//String HYPERLINK = "https://nciterms65.nci.nih.gov/ncitbrowser/pages/concept_details.jsf?dictionary=NCI_Thesaurus&type=terminology&key=null&b=1&n=0&vse=null&code=";
    String HYPERLINK = "https://evsexplore-qa.semantics.cancer.gov/evsexplore/concept/ncit/";
	public RestrictionPageGenerator() {

	}

	public void setHYPERLINK(String HYPERLINK) {
		this.HYPERLINK = HYPERLINK;
	}

	public String getHyperLink(String code) {
		if (HYPERLINK != null) {
			return HYPERLINK + code;
		} else {
			return null;
		}
	}

	public void generate(String title, Vector restriction_vec, String outputfile) {
		long ms = System.currentTimeMillis();
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(outputfile, "UTF-8");
			generate(title, pw, restriction_vec);

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


	public void generate(String title, PrintWriter out, Vector restriction_vec) {
		out.println("<!doctype html>");
		out.println("<html lang=\"en\">");
		out.println("<head>");
		out.println("	<meta charset=\"utf-8\">");
		out.println("	<title>Biomarker Hierarchy (Last updated: 09-07-2020)</title>");
		out.println("	<link rel=\"stylesheet\" href=\"tree.css\">");
		out.println("	<script type=\"text/javascript\" src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.min.js\"></script>");
		out.println("	<script type=\"text/javascript\" src=\"js/jquery.sapling.min.js\"></script>");
		out.println("    <link rel=\"stylesheet\" type=\"text/css\" href=\"css/tree.css\">");
		out.println("	<script type=\"text/javascript\">");
		out.println("		$(document).ready(function() {");
		out.println("			$('#demoList').sapling();");
		out.println("		});");
		out.println("	</script>");
		out.println("</head>");
		out.println("<body>");
		out.println("	<h3>" + title + " (Last updated: " + StringUtils.getToday() + ")</h3>");
		out.println("	<hr>");
		out.println("	<ul id=\"demoList\">");
		out.println("		<ul>");

		for (int i=0; i<restriction_vec.size(); i++) {
			String line = (String) restriction_vec.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String source_label = (String) u.elementAt(0);
			String source_code = (String) u.elementAt(1);
			String restriction = (String) u.elementAt(2);
			String target_label = (String) u.elementAt(3);
			String target_code = (String) u.elementAt(4);

            String hyperlink1 = getHyperLink(source_code);
            String hyperlink2 = getHyperLink(target_code);
            out.println("<li>");
            out.println("[" + source_label + " (<a href=\"" + hyperlink1 + "\">" + source_code + "</a>" + ")] -- (" + restriction + ")"
				    + " --> [" + target_label + " (<a href=\"" + hyperlink2 + "\">" + target_code + "</a>" + ")] ");
    		out.println("</li>");
	    }

		out.println("		</ul>");
		out.println("	</li>");
		out.println("	</ul>");
		out.println("</body>");
		out.println("</html>");
	}

    public static void main(String[] args) {
		String inputfile = args[0];
		String outputfile = args[1];
		RestrictionPageGenerator generator = new RestrictionPageGenerator();
		long ms = System.currentTimeMillis();
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(outputfile, "UTF-8");
			Vector restriction_vec = Utils.readFile(inputfile);
			generator.generate("Restriction", pw, restriction_vec);

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

}
