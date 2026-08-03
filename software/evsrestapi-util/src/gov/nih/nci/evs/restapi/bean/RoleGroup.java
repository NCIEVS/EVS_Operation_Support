package gov.nih.nci.evs.restapi.bean;

import java.io.*;
import java.util.*;
import java.net.*;

import com.google.gson.*;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.XStream;

public class RoleGroup {

// Variable declaration
	private List<RoleSet> roleSets;

// Default constructor
	public RoleGroup() {
	}

// Constructor
	public RoleGroup(
		List<RoleSet> roleSets) {

		this.roleSets = roleSets;
	}

// Set methods
	public void setRoleSets(List<RoleSet> roleSets) {
		this.roleSets = roleSets;
	}


// Get methods
	public List<RoleSet> getRoleSets() {
		return this.roleSets;
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

    public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("\tRole Group").append("\n");
		for (int j=0; j<roleSets.size(); j++) {
			RoleSet rs = (RoleSet) roleSets.get(j);
			List roles = rs.getRoles();
			for (int k=0; k<roles.size()/2; k++) {
				Restriction r1 = (Restriction) roles.get(k*2);
				Restriction r2 = (Restriction) roles.get(k*2+1);
				buf.append("\t\t" + r1.toString()).append("\n");
				buf.append("\t\t" + r2.toString()).append("\n");
				if (j<roleSets.size()-1) {
					buf.append("\tor");
				}
			}
			buf.append("\n");
		}
		return buf.toString();
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
