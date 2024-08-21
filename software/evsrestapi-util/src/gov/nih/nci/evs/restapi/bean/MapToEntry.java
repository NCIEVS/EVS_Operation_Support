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
public class MapToEntry
{

// Variable declaration
	private String code;
	private String preferredName;
	private String relationshipToTarget;
	private String targetCode;
	private String targetTerm;
	private String targetTermType;
	private String targetTerminology;
	private String targetTerminologyVersion;

// Default constructor
	public MapToEntry() {
	}

// Constructor
	public MapToEntry(
		String code,
		String preferredName,
		String relationshipToTarget,
		String targetCode,
		String targetTerm,
		String targetTermType,
		String targetTerminology,
		String targetTerminologyVersion) {

		this.code = code;
		this.preferredName = preferredName;
		this.relationshipToTarget = relationshipToTarget;
		this.targetCode = targetCode;
		this.targetTerm = targetTerm;
		this.targetTermType = targetTermType;
		this.targetTerminology = targetTerminology;
		this.targetTerminologyVersion = targetTerminologyVersion;
	}

// Set methods
	public void setCode(String code) {
		this.code = code;
	}

	public void setPreferredName(String preferredName) {
		this.preferredName = preferredName;
	}

	public void setRelationshipToTarget(String relationshipToTarget) {
		this.relationshipToTarget = relationshipToTarget;
	}

	public void setTargetCode(String targetCode) {
		this.targetCode = targetCode;
	}

	public void setTargetTerm(String targetTerm) {
		this.targetTerm = targetTerm;
	}

	public void setTargetTermType(String targetTermType) {
		this.targetTermType = targetTermType;
	}

	public void setTargetTerminology(String targetTerminology) {
		this.targetTerminology = targetTerminology;
	}

	public void setTargetTerminologyVersion(String targetTerminologyVersion) {
		this.targetTerminologyVersion = targetTerminologyVersion;
	}


// Get methods
	public String getCode() {
		return this.code;
	}

	public String getPreferredName() {
		return this.preferredName;
	}

	public String getRelationshipToTarget() {
		return this.relationshipToTarget;
	}

	public String getTargetCode() {
		return this.targetCode;
	}

	public String getTargetTerm() {
		return this.targetTerm;
	}

	public String getTargetTermType() {
		return this.targetTermType;
	}

	public String getTargetTerminology() {
		return this.targetTerminology;
	}

	public String getTargetTerminologyVersion() {
		return this.targetTerminologyVersion;
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

	public String toString() {
		return this.code + "|"
		     + this.preferredName + "|"
		     + "P375|"
		     + this.targetTerm + "|"
		     + "P393|"
		     + this.relationshipToTarget + "|"
		     + "P395|"
		     + this.targetCode + "|"
		     + "P394|"
		     + this.targetTermType + "|"
		     + "P397|"
		     + this.targetTerminology + "|"
		     + "P396|"
		     + this.targetTerminologyVersion;
	}
}
