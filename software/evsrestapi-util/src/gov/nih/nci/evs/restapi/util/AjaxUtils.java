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
public class AjaxUtils {

	public AjaxUtils() {

	}

	//https://nciterms65.nci.nih.gov/ncitbrowser/ajax?action=build_tree&ontology_display_name=NCI_Thesaurus
	public void run(PrintWriter out, String title, String url) {
		out.println("<!DOCTYPE html>");
		out.println("<html lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\">");
		out.println("<head>");
		out.println("    <meta charset=\"utf-8\" />");
		out.println("    <title>" + title + "</title>");
		out.println("    <script>");
		out.println("    function get() {");
		out.println("		var ajax = new XMLHttpRequest();");
		out.println("		ajax.open(\"GET\", \"" + url + "\", true);");
		out.println("		ajax.send();");
		out.println("		ajax.onreadystatechange = function() {");
		out.println("			if (ajax.readyState == 4 && ajax.status == 200) {");
		out.println("				var data = ajax.responseText;");
		out.println("				alert(data);");
		out.println("			}");
		out.println("		}");
		out.println("    }");
		out.println("    </script>");
		out.println("</head>");
		out.println("<body onload=\"javascript:get()\">");
		out.println("    <div id=\"" + title + "\"></div>");
		out.println("</body>");
		out.println("</html>");
	}

	public void run(String title, String url, String outputfile) {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(outputfile, "UTF-8");
			run(pw, title, url);

		} catch (Exception ex) {

		} finally {
			try {
				pw.close();
				System.out.println("Output file " + outputfile + " generated.");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
        long ms = System.currentTimeMillis();
        String title = args[0];
        String url = args[1];
        AjaxUtils ajaxUtils = new AjaxUtils();
        String outputfile = title + ".html";
        outputfile = outputfile.replace(" ", "_");
        ajaxUtils.run(title, url, outputfile);
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}

}
