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

public class LogicalExpressionElement {

// Variable declaration
	private List<Restriction> roles;
	private List<RoleUnion> roleUnions;
	private List<RoleGroup> roleGroups;
	private String range;

// Default constructor
	public LogicalExpressionElement() {
	}

// Constructor
	public LogicalExpressionElement(
		List<Restriction> roles,
		List<RoleUnion> roleUnions,
		List<RoleGroup> roleGroups,
		String range) {

		this.roles = roles;
		this.roleUnions = roleUnions;
		this.roleGroups = roleGroups;
		this.range = range;
	}

// Set methods
	public void setRoles(List<Restriction> roles) { 
		this.roles = roles;
	}

	public void setRoleUnions(List<RoleUnion> roleUnions) { 
		this.roleUnions = roleUnions;
	}

	public void setRoleGroups(List<RoleGroup> roleGroups) { 
		this.roleGroups = roleGroups;
	}

	public void setRange(String range) { 
		this.range = range;
	}


// Get methods
	public List<Restriction> getRoles() { 
		return this.roles;
	}

	public List<RoleUnion> getRoleUnions() { 
		return this.roleUnions;
	}

	public List<RoleGroup> getRoleGroups() { 
		return this.roleGroups;
	}

	public String getRange() { 
		return this.range;
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
