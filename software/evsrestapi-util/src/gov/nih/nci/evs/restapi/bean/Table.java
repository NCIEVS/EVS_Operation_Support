package gov.nih.nci.evs.restapi.bean;
import com.google.gson.*;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.XStream;
import gov.nih.nci.evs.restapi.util.*;
import java.io.*;
import java.net.*;
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
public class Table
{

// Variable declaration
	private String label;
	private List<String> headings;
	private List<Row> rows;

// Default constructor
	public Table() {
	}

// Constructor
	public Table(
		String label,
		List<String> headings,
		List<Row> rows) {

		this.label = label;
		this.headings = headings;
		this.rows = rows;
	}


	public Table(
		String label,
		Vector heading_vec,
		Vector data_vec) {
		this.label = label;
		this.headings = new ArrayList();
		for (int i=0; i<heading_vec.size(); i++) {
			String heading = (String) heading_vec.elementAt(i);
		    this.headings.add(heading);
		}
		this.rows = new ArrayList();
		for (int i=0; i<data_vec.size(); i++) {
			String line = (String) data_vec.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			List list = new ArrayList();
			for (int j=0; j<u.size(); j++) {
				String cell_val = (String) u.elementAt(j);
				list.add(cell_val);
			}
			this.rows.add(new Row(list));
		}
	}


// Set methods
	public void setLabel(String label) {
		this.label = label;
	}

	public void setHeadings(List<String> headings) {
		this.headings = headings;
	}

	public void setRows(List<Row> rows) {
		this.rows = rows;
	}


// Get methods
	public String getLabel() {
		return this.label;
	}

	public List<String> getHeadings() {
		return this.headings;
	}

	public List<Row> getRows() {
		return this.rows;
	}

	public String toXML() {
		XStream xstream_xml = new XStream(new DomDriver());
		String xml = xstream_xml.toXML(this);
		xml = escapeDoubleQuotes(xml);
		StringBuffer buf = new StringBuffer();
		String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		buf.append(XML_DECLARATION).append("\n").append(xml);
		xml = buf.toString();
		return xml;
	}

	public String toJson() {
		JsonParser parser = new JsonParser();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
	}

	public String escapeDoubleQuotes(String inputStr) {
		char doubleQ = '"';
		StringBuffer buf = new StringBuffer();
		for (int i=0;  i<inputStr.length(); i++) {
			char c = inputStr.charAt(i);
			if (c == doubleQ) {
				buf.append(doubleQ).append(doubleQ);
			}
			buf.append(c);
		}
		return buf.toString();
	}


	public static String table2String(Table table) {
		StringBuffer buf = new StringBuffer();
		buf.append("<div>").append("\n");
		buf.append("<center>").append("\n");
		buf.append("<table>").append("\n");
		buf.append("<tr>").append("\n");
		List<String> headings = table.getHeadings();
		for (int i=0; i<headings.size(); i++) {
			String heading = headings.get(i);
			buf.append("<th>").append("\n");
			buf.append(heading).append("\n");
			buf.append("</th>").append("\n");
		}
		buf.append("<tr>").append("\n");
		List<Row> rows = table.getRows();
		for (int i=0; i<rows.size(); i++) {
			buf.append("<tr>").append("\n");
			Row row = rows.get(i);
			List cell_values = row.getrow();
			for (int j=0; j<cell_values.size(); j++) {
				String cell_value = (String) cell_values.get(j);
				buf.append("<td>").append("\n");
				buf.append(cell_value).append("\n");
				buf.append("</td>").append("\n");
			}
			buf.append("</tr>").append("\n");
		}
		buf.append("</table>").append("\n");
		buf.append("</div>").append("\n");
		return buf.toString();
	}


	public static Table construct_table(
		String label,
		Vector heading_vec,
		Vector data_vec) {
		List headings = new ArrayList();
		for (int i=0; i<heading_vec.size(); i++) {
			String heading = (String) heading_vec.elementAt(i);
			headings.add(heading);
		}
		List<Row> rows = new ArrayList();
		for (int i=0; i<data_vec.size(); i++) {
			List list = new ArrayList();
			String data = (String) data_vec.elementAt(i);
			Vector u = StringUtils.parseData(data, '|');
			List row_data = new ArrayList();
			for (int j=0; j<u.size(); j++) {
				String cell = (String) u.elementAt(j);
				row_data.add(cell);
			}
			Row row = new Row(row_data);
			rows.add(row);
		}
		return new Table(label, headings, rows);
	}
}
