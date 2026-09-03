package gov.nih.nci.evs.restapi.bean;
import gov.nih.nci.evs.restapi.util.*;
import java.io.*;
import java.util.*;
import java.net.*;

import com.google.gson.*;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.XStream;

public class LogicalExpression {

// Variable declaration
	private String code;
	private String label;
	private List<Concept> parents;
	private List<LogicalExpressionElement> elements;
	private String expression;

// Default constructor
	public LogicalExpression() {
	}

// Constructor
	public LogicalExpression(
		String code,
		String label,
		List<Concept> parents,
		List<LogicalExpressionElement> elements,
		String expression) {

		this.code = code;
		this.label = label;

		this.parents = new ArrayList();
		if (parents != null && parents.size() > 0) {
			for (int i=0; i<parents.size(); i++) {
				int idx = i;
				Concept concept = (Concept) parents.get(i);
				Concept c = new Concept(i, concept.getLabel(), concept.getCode());
				this.parents.add(c);
			}
		}

		this.elements = elements;
		this.expression = expression;
	}

// Set methods
	public void setCode(String code) {
		this.code = code;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setParents(List<Concept> parents) {
		this.parents = parents;
	}

	public void setElements(List<LogicalExpressionElement> elements) {
		this.elements = elements;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}


// Get methods
	public String getCode() {
		return this.code;
	}

	public String getLabel() {
		return this.label;
	}

	public List<Concept> getParents() {
		return this.parents;
	}

	public List<LogicalExpressionElement> getElements() {
		return this.elements;
	}

	public String getExpression() {
		return this.expression;
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
        buf.append("Logical expression of: ").append(getLabel()).append(" (" + getCode() + ")").append("\n\n");

        List parents = getParents();
        Vector parent_vec = new Vector();
        Vector w = new Vector();

        if (parents != null && parents.size() > 0) {
			buf.append("Parent(s)").append("\n");
			for (int i=0; i<parents.size(); i++) {
				Concept c = (Concept) parents.get(i);;
				String line = c.getLabel() + " (" + c.getCode() + ")";
				parent_vec.add("\t\t" + line);
			}
		}
		parent_vec = new SortUtils().quickSort(parent_vec);
		for (int i=0; i<parent_vec.size(); i++) {
			String line = (String) parent_vec.elementAt(i);
			buf.append(line).append("\n");
		}
		buf.append("\n");

		Vector ranges = new Vector();
		HashMap range2LEEMap = new HashMap();
		List<LogicalExpressionElement> elements = getElements();
		for (int i=0; i<elements.size(); i++) {
			LogicalExpressionElement element = (LogicalExpressionElement) elements.get(i);
			ranges.add(element.getRange());
			range2LEEMap.put(element.getRange(), element);
		}
		ranges = new SortUtils().quickSort(ranges);
		for (int i=0; i<ranges.size(); i++) {
			String range = (String) ranges.elementAt(i);
			LogicalExpressionElement element = (LogicalExpressionElement) range2LEEMap.get(range);
            buf.append(element.toString()).append("\n");
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
