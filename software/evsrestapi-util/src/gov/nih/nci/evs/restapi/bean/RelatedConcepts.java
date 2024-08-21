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
public class RelatedConcepts
{

// Variable declaration
	private List<Superconcept> superconcepts;
	private List<Subconcept> subconcepts;
	private List<Association> associations;
	private List<InverseAssociation> inverseAssoications;
	private List<Role> roles;
	private List<InverseRole> inverseRoles;

// Default constructor
	public RelatedConcepts() {
	}

// Constructor
	public RelatedConcepts(
		List<Superconcept> superconcepts,
		List<Subconcept> subconcepts,
		List<Association> associations,
		List<InverseAssociation> inverseAssoications,
		List<Role> roles,
		List<InverseRole> inverseRoles) {

		this.superconcepts = superconcepts;
		this.subconcepts = subconcepts;
		this.associations = associations;
		this.inverseAssoications = inverseAssoications;
		this.roles = roles;
		this.inverseRoles = inverseRoles;
	}

// Set methods
	public void setSuperconcepts(List<Superconcept> superconcepts) {
		this.superconcepts = superconcepts;
	}

	public void setSubconcepts(List<Subconcept> subconcepts) {
		this.subconcepts = subconcepts;
	}

	public void setAssociations(List<Association> associations) {
		this.associations = associations;
	}

	public void setInverseAssoications(List<InverseAssociation> inverseAssoications) {
		this.inverseAssoications = inverseAssoications;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}

	public void setInverseRoles(List<InverseRole> inverseRoles) {
		this.inverseRoles = inverseRoles;
	}


// Get methods
	public List<Superconcept> getSuperconcepts() {
		return this.superconcepts;
	}

	public List<Subconcept> getSubconcepts() {
		return this.subconcepts;
	}

	public List<Association> getAssociations() {
		return this.associations;
	}

	public List<InverseAssociation> getInverseAssoications() {
		return this.inverseAssoications;
	}

	public List<Role> getRoles() {
		return this.roles;
	}

	public List<InverseRole> getInverseRoles() {
		return this.inverseRoles;
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
		JsonParser parser = new JsonParser();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
	}
}
