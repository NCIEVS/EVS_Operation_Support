package gov.nih.nci.evs.restapi.bean;
import com.google.gson.*;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.XStream;
import gov.nih.nci.evs.restapi.common.*;
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
public class Paths
{
// Variable declaration
	private List paths;

// Default constructor
	public Paths() {
		paths = new ArrayList();
	}

// Constructor
	public Paths(
		List paths) {

		this.paths = paths;
	}

// Set methods
	public void setPath(List paths) {
		this.paths = paths;
	}


	public void add(Path path) {
		this.paths.add(path);
	}


// Get methods
	public List getPaths() {
		return this.paths;
	}


	public int getPathCount() {
		return this.paths.size();
	}


	public static String getIndentation(int n) {
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<n; i++) {
			buf.append("\t");
		}
		return buf.toString();
	}

	public static void printConcept(PrintWriter pw, Concept c) {
		int idx = c.getIdx();
		String code = c.getCode();
		String label = c.getLabel();
		pw.println(getIndentation(idx) + label + " (" + code + ")");
	}

	public static void printPath(PrintWriter pw, Path path) {
		List concepts = path.getConcepts();
		for (int i=0; i<concepts.size(); i++) {
			Concept c = (Concept) concepts.get(i);
			printConcept(pw, c);
		}
	}

	public static void printPaths(Paths paths) {
		PrintWriter pw = new PrintWriter(System.out, true);
		printPaths(pw, paths);
	}

	public static void printPaths(PrintWriter pw, Paths paths) {
		List list = paths.getPaths();
		for (int i=0; i<list.size(); i++) {
			Path path = (Path) list.get(i);
			printPath(pw, path);
		}
	}

	public String toXML() {
		XStream xstream_xml = new XStream(new DomDriver());
		String xml = xstream_xml.toXML(this);
		xml = StringUtils.escapeDoubleQuotes(xml);
		xml = Constants.XML_DECLARATION + "\n" + xml;
		xml = StringUtils.removePackageNames(Constants.EVSRESTAPI_BEAN, xml);
        return xml;
	}

	public String toJson() {
		//return new Gson().toJson(this);
		JsonParser parser = new JsonParser();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
	}

}
