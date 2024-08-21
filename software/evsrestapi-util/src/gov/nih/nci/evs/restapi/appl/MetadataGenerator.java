package gov.nih.nci.evs.restapi.appl;
import gov.nih.nci.evs.restapi.util.*;
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
public class MetadataGenerator {
	OWLSPARQLUtils owlSPARQLUtils = null;
    MetadataUtils metadataUtils = null;
    String version = null;
    String serviceUrl = null;
    String named_graph = null;
    String username = null;
    String password = null;

    public MetadataGenerator(String serviceUrl, String named_graph, String username, String password) {
		this.serviceUrl = serviceUrl;
		this.named_graph = named_graph;
		this.username = username;
		this.password = password;

	    this.owlSPARQLUtils = new OWLSPARQLUtils(serviceUrl, username, password);
        this.owlSPARQLUtils.set_named_graph(named_graph);
	    this.metadataUtils = new MetadataUtils(serviceUrl, username, password);
	    this.version = metadataUtils.getLatestVersion("NCI_Thesaurus");
	    System.out.println(this.version);

	}

	public void generateHTML(String filename) {
		Vector v = Utils.readFile(filename);
		HTMLTable.generate(v);
	}

	public void generate(String outputfile) {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(outputfile, "UTF-8");
			pw.println("<title>NCIt_Properties_and_Relationships");
			pw.println("<table>Supported Properties");
			pw.println("<th>Name");
			pw.println("<th>Code");
			pw.println("<data>");
			Vector v = owlSPARQLUtils.getSupportedProperties(named_graph);
			for (int i=0; i<v.size(); i++) {
				String t = (String) v.elementAt(i);
				pw.println(t);
			}
			pw.println("</data>");
			pw.println("</table>");

			pw.println("<table>Supported Roles");
			pw.println("<th>Name");
			pw.println("<th>Code");
			pw.println("<data>");
			v = owlSPARQLUtils.getSupportedRoles(named_graph);
			for (int i=0; i<v.size(); i++) {
				String t = (String) v.elementAt(i);
				pw.println(t);
			}
			pw.println("</data>");
			pw.println("</table>");

			pw.println("<table>Supported Associations");
			pw.println("<th>Name");
			pw.println("<th>Code");
			pw.println("<data>");
			v = owlSPARQLUtils.getSupportedAssociations(named_graph);
			for (int i=0; i<v.size(); i++) {
				String t = (String) v.elementAt(i);
				pw.println(t);
			}
			pw.println("</data>");
			pw.println("</table>");

			pw.println("<table>Supported Property Qualifiers");
			pw.println("<th>Property Name");
			pw.println("<th>Property Code");
			pw.println("<th>Qualifier Name");
			pw.println("<th>QualifierCode");
			pw.println("<data>");
			v = owlSPARQLUtils.getSupportedPropertyQualifiers(named_graph);
			for (int i=0; i<v.size(); i++) {
				String t = (String) v.elementAt(i);
				pw.println(t);
			}
			pw.println("</data>");
			pw.println("</table>");

			pw.println("<footer>(Source: NCI Thesaurus, version " + this.version + ")");
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				pw.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		String serviceUrl = args[0];
		String named_graph = args[1];
		String username = args[2];
		String password = args[3];
        MetadataGenerator metadataGenerator = new MetadataGenerator(serviceUrl, named_graph, username, password);
        String filename = "metadata.txt";
        metadataGenerator.generate(filename);
        metadataGenerator.generateHTML(filename);
	}
}
