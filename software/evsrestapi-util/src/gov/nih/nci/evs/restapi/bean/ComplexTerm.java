package gov.nih.nci.evs.restapi.bean;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
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
public class ComplexTerm implements java.io.Serializable
{

// Variable declaration
    @XStreamImplicit
	private String name = "";
	private String group = "";
	private String source = "";
	private String code = "";
	private String subsource_name = "";

// Default constructor
	public ComplexTerm() {
	}

// Constructor
	public ComplexTerm(
		String name,
		String group,
		String source,

		String code,

		String subsource_name) {

		this.name = name;
		this.group = group;
		this.source = source;
		this.code = code;
		this.subsource_name = subsource_name;
	}

// Set methods
	public void setName(String name) {
		this.name = name;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public void setSource(String source) {
		this.source = source;
	}


	public void setCode(String code) {
		this.code = code;
	}

	public void setsubsource_name(String subsource_name) {
		this.subsource_name = subsource_name;
	}
// Get methods
	public String getName() {
		return this.name;
	}

	public String getGroup() {
		return this.group;
	}

	public String getSource() {
		return this.source;
	}


	public String getCode() {
		return this.code;
	}

	public String getsubsource_name() {
		return this.subsource_name;
	}

	public String getDisplayForm() {
		StringBuffer buf = new StringBuffer();
		buf.append(name + " (group:" + group + ", " + "source: " + source);
		if (code != null && code.compareTo("") != 0) {
			buf.append(", " + "code: " + code);
		}
		if (subsource_name != null && subsource_name.compareTo("") != 0) {
			buf.append(", " + "subsource_name: " + subsource_name);
		}
		buf.append(")");
		return buf.toString();
	}

    @Override
    public String toString() {
        return "ComplexTerm [name=" + name + "]" + "[group=" + group + "]" + "[source=" + source + "]" + "[code=" + code + "]" + "[subsource_name=" + subsource_name + "]";
    }
}

/*
<ncicp:ComplexTerm xmlns:ncicp="http://ncicb.nci.nih.gov/xml/owl/EVS/ComplexProperties.xsd#"><ncicp:term-name>Acinar Cell</ncicp:term-name><ncicp:term-group>SY</ncicp:term-group><ncicp:term-source>NCI</ncicp:term-source><ncicp:source-code>TCGA</ncicp:source-code></ncicp:ComplexTerm>


<ncicp:ComplexTerm xmlns:ncicp="http://ncicb.nci.nih.gov/xml/owl/EVS/ComplexProp
erties.xsd#"><ncicp:term-name>Acinar Cell</ncicp:term-name><ncicp:term-group>SY<
/ncicp:term-group><ncicp:term-source>NCI</ncicp:term-source><ncicp:source-code>T
CGA</ncicp:source-code></ncicp:ComplexTerm>
<ncicp:ComplexTerm xmlns:ncicp="http://ncicb.nci.nih.gov/xml/owl/EVS/ComplexProp
erties.xsd#"><ncicp:term-name>Acinar Cell</ncicp:term-name><ncicp:term-group>SY<
/ncicp:term-group><ncicp:term-source>NCI</ncicp:term-source><ncicp:source-code>T
CGA</ncicp:source-code><ncicp:subsource_name-name>caDSR</ncicp:subsource_name-name></ncicp
:ComplexTerm>
<ncicp:ComplexTerm xmlns:ncicp="http://ncicb.nci.nih.gov/xml/owl/EVS/ComplexProperties.xsd#"><ncicp:term-name>Acinar Cell</ncicp:term-name><ncicp:term-group>SY</ncicp:term-group><ncicp:term-source>NCI</ncicp:term-source><ncicp:source-code>TCGA</ncicp:source-code><ncicp:subsource_name-name>caDSR</ncicp:subsource_name-name></ncicp:ComplexTerm>
*/
