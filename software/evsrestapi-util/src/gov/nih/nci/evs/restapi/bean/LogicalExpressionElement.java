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
    private String range;
	private List<Restriction> roles;
	private List<RoleUnion> roleUnions;
	private List<RoleGroup> roleGroups;

// Default constructor
	public LogicalExpressionElement() {
	}

// Constructor
	public LogicalExpressionElement(
		String range,
		List<Restriction> roles,
		List<RoleUnion> roleUnions,
		List<RoleGroup> roleGroups
		) {
		this.range = range;
		this.roles = roles;
		this.roleUnions = roleUnions;
		this.roleGroups = roleGroups;
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


	public String toString() {
        StringBuffer buf = new StringBuffer();
        if (range.compareTo("[Range Unspecified]") == 0) {
			buf.append("").append("\n");
		} else {
        	buf.append(range).append("\n");
		}
        List<Restriction> roles = getRoles();
        for (int i=0; i<roles.size(); i++) {
			Restriction r = (Restriction) roles.get(i);
			buf.append("\t\t" + r.toString()).append("\n");
		}

        List<RoleUnion> roleUnions = getRoleUnions();
        if (roleUnions != null && roleUnions.size() > 0) {
			buf.append("\n");
			for (int i=0; i<roleUnions.size(); i++) {
				RoleUnion ru = (RoleUnion) roleUnions.get(i);
				roles = ru.getRoles();
				for (int j=0; j<roles.size(); j++) {
					Restriction r = (Restriction) roles.get(j);
					buf.append("\t\t" + r.toString()).append("\n");
					if (j<roles.size()-1) {
						buf.append("\tor").append("\n");
					}
				}
				buf.append("\n");
			}
		}

        List<RoleGroup> roleGroups = getRoleGroups();
        if (roleGroups != null && roleGroups.size() > 0) {
			buf.append("\n");
			for (int i=0; i<roleGroups.size(); i++) {
				RoleGroup rg = (RoleGroup) roleGroups.get(i);
                if (range.compareTo("[Range Unspecified]") == 0) {
					buf.append(rg2String(rg));
				} else {
					buf.append(rg.toString());
				}
			}
		}
		return buf.toString();
	}

    public String rg2String(RoleGroup rg) {
		StringBuffer buf = new StringBuffer();
		buf.append("Role Group").append("\n");
		List<RoleSet> roleSets = rg.getRoleSets();
		for (int j=0; j<roleSets.size(); j++) {
			RoleSet rs = (RoleSet) roleSets.get(j);
			List roles = rs.getRoles();
			for (int k=0; k<roles.size(); k++) {
				Restriction r = (Restriction) roles.get(k);
				buf.append("\t\t" + r.toString()).append("\n");
			}
			if (j<roleSets.size()-1) {
				buf.append("\tor");
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
