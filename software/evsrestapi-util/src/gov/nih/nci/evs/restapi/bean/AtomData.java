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
public class AtomData
{

// Variable declaration
	private String aui;
	private String code;
	private String cui;
	private String ispref;
	private String lat;
	private String lui;
	private String sab;
	private String str;
	private String stt;
	private String suppress;
	private String tty;
	private int rank;

// Default constructor
	public AtomData() {
	}

// Constructor
	public AtomData(
		String aui,
		String code,
		String cui,
		String ispref,
		String lat,
		String lui,
		String sab,
		String str,
		String stt,
		String suppress,
		String tty,
		int rank) {

		this.aui = aui;
		this.code = code;
		this.cui = cui;
		this.ispref = ispref;
		this.lat = lat;
		this.lui = lui;
		this.sab = sab;
		this.str = str;
		this.stt = stt;
		this.suppress = suppress;
		this.tty = tty;
		this.rank = rank;
	}

// Set methods
	public void setAui(String aui) { 
		this.aui = aui;
	}

	public void setCode(String code) { 
		this.code = code;
	}

	public void setCui(String cui) { 
		this.cui = cui;
	}

	public void setIspref(String ispref) { 
		this.ispref = ispref;
	}

	public void setLat(String lat) { 
		this.lat = lat;
	}

	public void setLui(String lui) { 
		this.lui = lui;
	}

	public void setSab(String sab) { 
		this.sab = sab;
	}

	public void setStr(String str) { 
		this.str = str;
	}

	public void setStt(String stt) { 
		this.stt = stt;
	}

	public void setSuppress(String suppress) { 
		this.suppress = suppress;
	}

	public void setTty(String tty) { 
		this.tty = tty;
	}

	public void setRank(int rank) { 
		this.rank = rank;
	}


// Get methods
	public String getAui() { 
		return this.aui;
	}

	public String getCode() { 
		return this.code;
	}

	public String getCui() { 
		return this.cui;
	}

	public String getIspref() { 
		return this.ispref;
	}

	public String getLat() { 
		return this.lat;
	}

	public String getLui() { 
		return this.lui;
	}

	public String getSab() { 
		return this.sab;
	}

	public String getStr() { 
		return this.str;
	}

	public String getStt() { 
		return this.stt;
	}

	public String getSuppress() { 
		return this.suppress;
	}

	public String getTty() { 
		return this.tty;
	}

	public int getRank() { 
		return this.rank;
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
