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
public class MetaTreeNode
{

// Variable declaration
	public String _cui;
	public String _text;
	public String _sab;
	public String _aui;
	public String _code;
	public Iterator children;
	public boolean _expandable;
	public HashMap asso2ChildNodeMap;

// Default constructor
	public MetaTreeNode() {
		asso2ChildNodeMap = new HashMap();
	}

// Constructor
	public MetaTreeNode(
		String cui,
		String name,
		Iterator children,
		boolean expanded) {

		this._cui = cui;
		this._text = name;
		this.children = children;
		this._expandable = expanded;
		this.asso2ChildNodeMap = new HashMap();
	}

	public MetaTreeNode(
		String cui,
		String name) {
		this._cui = cui;
		this._text = name;
		this.asso2ChildNodeMap = new HashMap();
	}

// Set methods
	public void setCui(String cui) {
		this._cui = cui;
	}


	public void setCode(String code) {
		this._code = code;
	}

	public void setSab(String sab) {
		this._sab = sab;
	}

	public void setAui(String aui) {
		this._aui = aui;
	}

	public void setText(String name) {
		this._text = name;
	}

	public void setChildren(Iterator children) {
		this.children = children;
	}

	public void setExpanded(boolean expanded) {
		this._expandable = expanded;
	}


    public void addChild(String assoc, MetaTreeNode childItem) {
        Vector v = (Vector) asso2ChildNodeMap.get(assoc);
        v.add(childItem);
        asso2ChildNodeMap.put(assoc, v);
	}


// Get methods
	public String getCui() {
		return this._cui;
	}


	public String getCode() {
		return this._code;
	}

	public String getSab() {
		return this._sab;
	}

	public String getAui() {
		return this._aui;
	}

	public String getText() {
		return this._text;
	}

	public Iterator getChildren() {
		return this.children;
	}

	public boolean getExpanded() {
		return this._expandable;
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
