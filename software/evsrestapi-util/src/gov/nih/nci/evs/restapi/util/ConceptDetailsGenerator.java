package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.bean.*;
import gov.nih.nci.evs.restapi.common.*;
import java.io.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.Arrays;
import java.util.Map.Entry;

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
public class ConceptDetailsGenerator {
	HierarchyHelper hh = null;
	Vector parent_child_vec = null;
	String TREE_VARIABLE_NAME = "demoList";
	String HYPERLINK = "https://nciterms65.nci.nih.gov/ncitbrowser/pages/concept_details.jsf?dictionary=NCI_Thesaurus&type=terminology&key=null&b=1&n=0&vse=null&code=";

	HashMap propertyHashMap = null;

	public ConceptDetailsGenerator() {
		this.propertyHashMap = null;
	}

	public ConceptDetailsGenerator(HashMap propertyHashMap) {
		this.propertyHashMap = propertyHashMap;
	}

	public void setHYPERLINK(String hyperlinkUrl) {
		this.HYPERLINK = hyperlinkUrl;
	}

	public String getHyperLink(String code) {
		if (HYPERLINK != null) {
			return HYPERLINK + code;
		} else {
			return null;
		}
	}

    public String encode(String t) {
		if (t == null) return null;
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<t.length(); i++) {
			char c = t.charAt(i);
			if (c > 126) {
				buf.append(" ");
			} else {
				String s = "" + c;
				if (s.compareTo("'") == 0) {
					buf.append("\\'");
				} else {
					buf.append(s);
				}
			}
		}
		return buf.toString();
	}

    public void writeHeader(PrintWriter out, String title) {
      out.println("<!doctype html>");
      out.println("<html lang=\"en\">");
      out.println("<head>");
      out.println("	<meta charset=\"utf-8\">");
      out.println("	<title>" + title + "</title>");
      out.println("	<link rel=\"stylesheet\" href=\"tree.css\">");
      writeFunction(out);
      out.println("</head>");
	}


    public void writeFunction(PrintWriter out) {
		out.println("	<script type=\"text/javascript\" src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.min.js\"></script>");
		out.println("	<script type=\"text/javascript\" src=\"js/jquery.sapling.min.js\"></script>");
		out.println("    <link rel=\"stylesheet\" type=\"text/css\" href=\"css/styleSheet.css\">");
		out.println("	<script type=\"text/javascript\">");
		out.println("		$(document).ready(function() {");
		out.println("			$('#demoList').sapling();");
		out.println("		});");
		out.println("	</script>");
    }


	public String getIndentation(int level) {
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<level; i++) {
			buf.append("\t");
		}
		return buf.toString();
	}

	public static Boolean isEven (Integer i) {
		return (i % 2) == 0;
	}


	public String propertyHashMap2HTML(HashMap propertyHashMap) {
		StringBuffer buf = new StringBuffer();
        buf.append("<table class=\"datatable_960\" border=\"0\" width=\"100%\" >").append("\n");
		buf.append("<tr>").append("\n");
		buf.append("   <th class=\"dataCellText\" scope=\"col\" align=\"left\">Source Code</th>").append("\n");
		buf.append("   <th class=\"dataCellText\" scope=\"col\" align=\"left\">Source Term</th>").append("\n");
		buf.append("   <th class=\"dataCellText\" scope=\"col\" align=\"left\">Target Code</th>").append("\n");
		buf.append("   <th class=\"dataCellText\" scope=\"col\" align=\"left\">Target Term</th>").append("\n");
		buf.append("   <th class=\"dataCellText\" scope=\"col\" align=\"left\">Property</th>").append("\n");
		buf.append("   <th class=\"dataCellText\" scope=\"col\" align=\"left\">Property Value</th>").append("\n");
		buf.append("</tr>").append("\n");
		buf.append("   <col width=\"10%\">").append("\n");
		buf.append("   <col width=\"20%\">").append("\n");
		buf.append("   <col width=\"10%\">").append("\n");
		buf.append("   <col width=\"20%\">").append("\n");
		buf.append("   <col width=\"10%\">").append("\n");
		buf.append("   <col width=\"30%\">").append("\n");

		Iterator it = propertyHashMap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			String line = CSVFileReader.csv2Delimited(key, "|");
			Vector u = StringUtils.parseData(line, '|');
			String source_code = (String) u.elementAt(0);
			String source_term = (String) u.elementAt(1);
			String target_code = (String) u.elementAt(2);
			String target_term = (String) u.elementAt(3);
			StringBuffer property_buf = new StringBuffer();
			HashMap hmap = (HashMap) propertyHashMap.get(key);
			Vector properties = new Vector();
			Iterator it2 = hmap.keySet().iterator();
			while (it2.hasNext()) {
				String key2 = (String) it2.next();
				properties.add(key2);
			}
			properties = new gov.nih.nci.evs.restapi.util.SortUtils().quickSort(properties);
			int line_num = 0;
			for (int k=0; k<properties.size(); k++) {
				StringBuffer line_buf = new StringBuffer();
				String property = (String) properties.elementAt(k);
				Vector values = (Vector) hmap.get(property);
				values = new gov.nih.nci.evs.restapi.util.SortUtils().quickSort(values);
			    for (int k2=0; k2<values.size(); k2++) {
					buf.append("<tr>").append("\n");
					if (line_num == 0) {
						line_buf.append("<td class=\"textbody\">").append(source_code).append("</td>").append("\n");
						line_buf.append("<td class=\"textbody\">").append(source_term).append("</td>").append("\n");
						line_buf.append("<td class=\"textbody\">").append("<a href='").append(getHyperLink(target_code)).append("'>").append(target_code).append("</a>").append("</td>").append("\n");
						line_buf.append("<td class=\"textbody\">").append(target_term).append("</td>").append("\n");

					} else {
						line_buf.append("<td class=\"textbody\">").append("").append("</td>").append("\n");
						line_buf.append("<td class=\"textbody\">").append("").append("</td>").append("\n");
						line_buf.append("<td class=\"textbody\">").append("").append("</td>").append("\n");
						line_buf.append("<td class=\"textbody\">").append("").append("</td>").append("\n");
					}
					String value = (String) values.elementAt(k2);
					String col1 = property;
					String col2 = value;
					if (k2 != 0) {
						col1 = "";
					}
					line_buf.append("<td class=\"textbody\">").append(col1).append("</td>").append("\n");
					line_buf.append("<td class=\"textbody\">").append(col2).append("</td>").append("\n");

					buf.append(line_buf.toString()).append("\n");
					buf.append("</tr>").append("\n");
					line_num++;
					line_buf = new StringBuffer();
				}

			}

		}
		buf.append("</table>").append("\n");
		return buf.toString();
	}

	public void writeBody(PrintWriter out, String title) {
		out.println("<body>");
		out.println("	<center><h3>" + title + "</h3></center>");
		out.println("	<hr>");
		String content = null;
		try {
			content = propertyHashMap2HTML(this.propertyHashMap);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		out.println(content);
		out.println("</body>");
	}

	public void writeFooter(PrintWriter out) {
		out.println("</html>");
	}

	public Vector sortByLabel(Vector codes) {
		if (codes == null || codes.size()<=1) return codes;
		Vector w = new Vector();
		HashMap hmap = new HashMap();
		for (int i=0; i<codes.size(); i++) {
			String code = (String) codes.elementAt(i);
			String label = hh.getLabel(code);
			hmap.put(label, code);
			w.add(label);
		}
		w = new SortUtils().quickSort(w);
		Vector v = new Vector();
		for (int i=0; i<w.size(); i++) {
			String label = (String) w.elementAt(i);
			String code = (String) hmap.get(label);
			v.add(code);
		}
		return v;
	}

    public void generate(PrintWriter out, String title) {
        writeHeader(out, title);
        writeBody(out, title);
        writeFooter(out);
	}

	public void generate(String outputfile, String title) {
		long ms = System.currentTimeMillis();
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(outputfile, "UTF-8");
			generate(pw, title);

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

	public static Vector filterRoots(Vector parent_child_vec) {
		Vector w = new Vector();
		for (int i=0; i<parent_child_vec.size(); i++) {
			String line = (String) parent_child_vec.elementAt(i);
			if (line.indexOf("|<Root>|") == -1) {
				w.add(line);
			}
		}
		return w;
	}


    public HashMap appendPropertiesToMappingEntries(String serviceUrl, String namedGraph, Vector mapping_entries) {
		HashMap propertyHashMap = new HashMap();
		OWLSPARQLUtils owlSPARQLUtils = new OWLSPARQLUtils(serviceUrl);
		ParserUtils parserUtils = new ParserUtils();
        Vector v = mapping_entries;
        for (int i=1; i<mapping_entries.size(); i++) {
			String line = (String) mapping_entries.elementAt(i);
			String line1 = line;
			if (line.indexOf("|") == -1) {
				line1 = CSVFileReader.csv2Delimited(line, "|");
			}

			Vector u = StringUtils.parseData(line1, '|');
			String source_code = (String) u.elementAt(0);
			String source_term = (String) u.elementAt(1);
			String target_code = (String) u.elementAt(2);
			if (target_code != null && target_code.length() > 0) {
				String query = owlSPARQLUtils.construct_get_properties_by_code(namedGraph, target_code);
				Vector w = owlSPARQLUtils.getPropertiesByCode(namedGraph, target_code);
				if (w == null || w.size() == 0) {
					//System.out.println("\tgetPropertiesByCode returns null???");
				} else {
					w = parserUtils.getResponseValues(w);
					HashMap hmap = createPropertyHashMap(w);
					propertyHashMap.put(line, hmap);
				}
			}
		}
		return propertyHashMap;
	}

	public void dumpPropertyHashMap(HashMap propertyHashMap) {
		Iterator it = propertyHashMap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();

			HashMap hmap = (HashMap) propertyHashMap.get(key);
			System.out.println(key);
			Vector properties = new Vector();
			Iterator it2 = hmap.keySet().iterator();
			while (it2.hasNext()) {
				String key2 = (String) it2.next();
				properties.add(key2);
			}
			properties = new gov.nih.nci.evs.restapi.util.SortUtils().quickSort(properties);
			for (int k=0; k<properties.size(); k++) {
				String property = (String) properties.elementAt(k);
				System.out.println(property);
				Vector values = (Vector) hmap.get(property);
				values = new gov.nih.nci.evs.restapi.util.SortUtils().quickSort(values);
			    for (int k2=0; k2<values.size(); k2++) {
					String value = (String) values.elementAt(k2);
					System.out.println("\t" + value);
				}
			}
		}
	}

	public HashMap createPropertyHashMap(Vector w) {
		HashMap hmap = null;
    	if (w == null || w.size() == 0) {
			return hmap;
		}
		hmap = new HashMap();
		for (int j=0; j<w.size(); j++) {
			String t = (String) w.elementAt(j);
			Vector u = StringUtils.parseData(t, '|');
			//Secondary Malignant Neoplasm|code|C4968
			String property_name = (String) u.elementAt(1);
			String property_value = (String) u.elementAt(2);
			Vector v = new Vector();
			if (hmap.containsKey(property_name)) {
				v = (Vector) hmap.get(property_name);
			}
			v.add(property_value);
			hmap.put(property_name, v);
		}
		return hmap;
	}

    public String toString() {
 		StringWriter out = new StringWriter();
		PrintWriter writer = new PrintWriter(out);
		String content = propertyHashMap2HTML(this.propertyHashMap);
        writer.print(content);
		return writer.toString();
	}

	public static void main(String[] args) {
		String serviceUrl = args[0];
		String namedGraph = args[1];
		String mappingfile = args[2];
		System.out.println("serviceUrl: " + serviceUrl);
		System.out.println("namedGraph: " + namedGraph);
		System.out.println("mappingfile: " + mappingfile);

		System.out.println("Generating property hashmap...");
		Vector mapping_entries = Utils.readFile(mappingfile);

		HashMap propertyHashMap = new ConceptDetailsGenerator().appendPropertiesToMappingEntries(serviceUrl, namedGraph, mapping_entries);
		ConceptDetailsGenerator generator = new ConceptDetailsGenerator(propertyHashMap);
		String hyperlinkUrl = "https://ncimappingtool-dev.nci.nih.gov/ncimappingtool/pages/concept_details.jsf?ng=http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl&code=";
        generator.setHYPERLINK(hyperlinkUrl);
        String content = generator.propertyHashMap2HTML(propertyHashMap);
        /*
	    String outputfile = "mapping.html";
	    String title = "Concept Details";
		generator.generate(outputfile, title);
		*/
		System.out.println(content);
	}

}




