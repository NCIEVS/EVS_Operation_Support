package gov.nih.nci.evs.restapi.bean;
import com.google.gson.*;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.XStream;
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
public class MapEntry
{

// Variable declaration
	private String sourceCode;
	private String sourceName;
	private String sourceCodingScheme;
	private String sourceCodingSchemeVersion;
	private String sourceCodingSchemeNamespace;
	private String associationName;
	private String rel;
	private String mapRank;
	private String targetCode;
	private String targetName;
	private String targetCodingScheme;
	private String targetCodingSchemeVersion;
	private String targetCodingSchemeNamespace;

// Default constructor
	public MapEntry() {
	}

// Constructor
	public MapEntry(
		String sourceCode,
		String sourceName,
		String sourceCodingScheme,
		String sourceCodingSchemeVersion,
		String sourceCodingSchemeNamespace,
		String associationName,
		String rel,
		String mapRank,
		String targetCode,
		String targetName,
		String targetCodingScheme,
		String targetCodingSchemeVersion,
		String targetCodingSchemeNamespace) {

		this.sourceCode = sourceCode;
		this.sourceName = sourceName;
		this.sourceCodingScheme = sourceCodingScheme;
		this.sourceCodingSchemeVersion = sourceCodingSchemeVersion;
		this.sourceCodingSchemeNamespace = sourceCodingSchemeNamespace;
		this.associationName = associationName;
		this.rel = rel;
		this.mapRank = mapRank;
		this.targetCode = targetCode;
		this.targetName = targetName;
		this.targetCodingScheme = targetCodingScheme;
		this.targetCodingSchemeVersion = targetCodingSchemeVersion;
		this.targetCodingSchemeNamespace = targetCodingSchemeNamespace;
	}

// Set methods
	public void setSourceCode(String sourceCode) { 
		this.sourceCode = sourceCode;
	}

	public void setSourceName(String sourceName) { 
		this.sourceName = sourceName;
	}

	public void setSourceCodingScheme(String sourceCodingScheme) { 
		this.sourceCodingScheme = sourceCodingScheme;
	}

	public void setSourceCodingSchemeVersion(String sourceCodingSchemeVersion) { 
		this.sourceCodingSchemeVersion = sourceCodingSchemeVersion;
	}

	public void setSourceCodingSchemeNamespace(String sourceCodingSchemeNamespace) { 
		this.sourceCodingSchemeNamespace = sourceCodingSchemeNamespace;
	}

	public void setAssociationName(String associationName) { 
		this.associationName = associationName;
	}

	public void setRel(String rel) { 
		this.rel = rel;
	}

	public void setMapRank(String mapRank) { 
		this.mapRank = mapRank;
	}

	public void setTargetCode(String targetCode) { 
		this.targetCode = targetCode;
	}

	public void setTargetName(String targetName) { 
		this.targetName = targetName;
	}

	public void setTargetCodingScheme(String targetCodingScheme) { 
		this.targetCodingScheme = targetCodingScheme;
	}

	public void setTargetCodingSchemeVersion(String targetCodingSchemeVersion) { 
		this.targetCodingSchemeVersion = targetCodingSchemeVersion;
	}

	public void setTargetCodingSchemeNamespace(String targetCodingSchemeNamespace) { 
		this.targetCodingSchemeNamespace = targetCodingSchemeNamespace;
	}


// Get methods
	public String getSourceCode() { 
		return this.sourceCode;
	}

	public String getSourceName() { 
		return this.sourceName;
	}

	public String getSourceCodingScheme() { 
		return this.sourceCodingScheme;
	}

	public String getSourceCodingSchemeVersion() { 
		return this.sourceCodingSchemeVersion;
	}

	public String getSourceCodingSchemeNamespace() { 
		return this.sourceCodingSchemeNamespace;
	}

	public String getAssociationName() { 
		return this.associationName;
	}

	public String getRel() { 
		return this.rel;
	}

	public String getMapRank() { 
		return this.mapRank;
	}

	public String getTargetCode() { 
		return this.targetCode;
	}

	public String getTargetName() { 
		return this.targetName;
	}

	public String getTargetCodingScheme() { 
		return this.targetCodingScheme;
	}

	public String getTargetCodingSchemeVersion() { 
		return this.targetCodingSchemeVersion;
	}

	public String getTargetCodingSchemeNamespace() { 
		return this.targetCodingSchemeNamespace;
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
}
