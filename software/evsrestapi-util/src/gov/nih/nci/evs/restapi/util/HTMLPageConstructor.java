package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;
import java.io.*;
import java.util.*;

public class HTMLPageConstructor {
    static HashMap code2LabelMap = null;
    static Vector annotationProperties = null;
    static Vector objectProperties = null;
	public static HashMap annotationPropertyCode2LabelMap = null;
	public static HashMap annotationPropertyLabel2CodeMap = null;
	public static HashMap objectPropertyCode2LabelMap = null;
	public static HashMap objectPropertyLabel2CodeMap = null;

	public static HashMap baseTooltipHashMap = null;

    static String NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile;
    static String PARENT_CHILD_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile;

    static String RANGE_DATA_FILE = "role_range.txt";

	static {
		code2LabelMap = NCItProperties.getCode2LabelMap();
		annotationPropertyCode2LabelMap = NCItProperties.getAnnotationPropertyCode2LabelMap();
		annotationPropertyLabel2CodeMap = NCItProperties.getAnnotationPropertyLabel2CodeMap();
		objectPropertyCode2LabelMap = NCItProperties.getObjectPropertyCode2LabelMap();
		objectPropertyLabel2CodeMap = NCItProperties.getObjectPropertyLabel2CodeMap();

		baseTooltipHashMap = new HashMap();
		Iterator it = annotationPropertyCode2LabelMap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			String value = (String) annotationPropertyCode2LabelMap.get(key);
			baseTooltipHashMap.put(key, value);
		}
		it = objectPropertyCode2LabelMap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			String value = (String) objectPropertyCode2LabelMap.get(key);
			baseTooltipHashMap.put(key, value);
		}
		File f = new File(RANGE_DATA_FILE);
		if (!f.exists()) {
			Vector roleDomanAndRange_vec = NCItProperties.get_roleDomanAndRange_vec();
			Utils.saveToFile(RANGE_DATA_FILE, roleDomanAndRange_vec);
		}
	}

	public HTMLPageConstructor() {

	}

	public static String getTagName(String str) {
		str = str.trim();
		if (str.startsWith("<")) {
			str = str.substring(1, str.length());
			int n = str.indexOf(">");
			return str.substring(0, n);
		}
		return null;
	}

	public static String getTagValue(String str) {
		str = str.trim();
		String tagName = getTagName(str);
		if (tagName == null) {
			return null;
		}
		String s = "<" + tagName + ">";
		String t = str.substring(s.length(), str.length());
		int n = t.lastIndexOf("<");
		if (n != -1) {
			t = t.substring(0, n);
			return t;
	    }
	    return null;
	}

	public static String extractTagName(String line) {
		line = line.trim();
		if (line.length() == 0) return null;
		if (line.indexOf(":") != -1) return null;
		return getTagName(line);
	}

	public static Vector extractTagNames(Vector lines) {
		Vector w = new Vector();
		for (int i=0; i<lines.size(); i++) {
			String line = (String) lines.elementAt(i);
			String tagName = extractTagName(line);
			if (tagName != null) {
				w.add(tagName);
			}
		}
		return w;
	}

	public static String extractResourceName(String line) {
		line = line.trim();
		if (line.length() == 0) return null;
		if (line.indexOf("<!--") != -1) {
			return null;
		}
		//<owl:equivalentClass>
		if (line.indexOf(":") == -1 || line.indexOf("#") == -1) return null;
		int n1 = line.lastIndexOf("#");
		int n2 = line.lastIndexOf("\"");
		String t = line.substring(n1+1, n2);
		return t;
	}

	public static Vector extractResourceNames(Vector lines) {
		Vector w = new Vector();
		for (int i=0; i<lines.size(); i++) {
			String line = (String) lines.elementAt(i);
			String tagName = extractResourceName(line);
			if (tagName != null) {
				w.add(tagName);
			}
		}
		return w;
	}

	public static HashMap loadTooltipHashMap(String filename) {
		HashMap hmap = new HashMap();
		Vector v = Utils.readFile(filename);
		return loadTooltipHashMap(v, 0, 1, '|');
	}

	public static HashMap loadTooltipHashMap(Vector v, int keyCol, int valueCol, char delim) {
		HashMap hmap = new HashMap();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, delim);
			hmap.put((String) u.elementAt(keyCol), (String) u.elementAt(valueCol));
		}
		return hmap;
	}


	public static void main(String[] args) {
		String textfile = args[0];

		Vector raw_data_vec = Utils.readFile(textfile);
		HashMap tooltipHashMap = new HashMap();
		if (args.length > 1) {
			String tooltipDataFile = args[1];
			tooltipHashMap = loadTooltipHashMap(tooltipDataFile);
		}
		HTMLTemplate test = new HTMLTemplate();
		int tooltip_width = 200;
		test.setTooltipWidth(tooltip_width);
		test.setTooltipHashMap(tooltipHashMap);
		Vector data_vec = new Vector();
		for (int i=0; i<raw_data_vec.size(); i++) {
			String line = (String) raw_data_vec.elementAt(i);
			String indent = HTMLTemplate.getIndentation(line);
			line = line.trim();

			//<owl:onProperty rdf:resource="http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#R176"/>
            if (line.startsWith("<owl:onProperty") && line.indexOf("rdf:resource=") != -1) {
				int n1 = line.indexOf("#R");
				int n2 = line.lastIndexOf("\"");
				String tag = line.substring(n1+1, n2);
				String propLabel = OWL2HTML.getPropertyLabel(tag);
				String by = OWL2HTML.toToolTip(tag, propLabel);
				line = OWL2HTML.encode(line);
				line = OWL2HTML.hyperlinkNCItCodes(line);
				line = line.replace(tag, by);

            } else if (line.startsWith("<A") && line.indexOf("rdf:resource=") != -1) {
				int n1 = line.indexOf(" ");
				String tag = line.substring(1, n1);
				String propLabel = OWL2HTML.getPropertyLabel(tag);
				String by = OWL2HTML.toToolTip(tag, propLabel);
				line = OWL2HTML.encode(line);
				line = OWL2HTML.hyperlinkNCItCodes(line);
				line = line.replace(tag, by);
			} else if (line.startsWith("</")) {
				line = OWL2HTML.encode(line);
				//out.println(indent + line + "<p></p>");
			} else {
				String tag = null;
				String value = null;
				String propLabel = null;
				try {
					tag = OWL2HTML.getTagName(line);
					value = OWL2HTML.getTagValue(line);
					propLabel = OWL2HTML.getPropertyLabel(tag);
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				if (tag != null && tag.length() > 0 && propLabel != null && propLabel.length() > 0) {
					line = OWL2HTML.toToolTipText(tag, propLabel, value);
				} else {
					line = OWL2HTML.encode(line);
					line = OWL2HTML.hyperlinkNCItCodes(line);
				}
			}
			// customize data:
			Vector u = StringUtils.parseData(line, '\t');
			for (int j=0; j<u.size(); j++) {
				String s = (String) u.elementAt(j);
				if (tooltipHashMap.containsKey(s)) {
					String toolTipvalue = (String) tooltipHashMap.get(s);
					line = line.replace(s, test.toTooltip(s));
				}
			}
			if (indent.length() == 0) {
				data_vec.add("<p></p>" + line);
			} else {
				data_vec.add(indent + line);
			}
		}

		int n = textfile.lastIndexOf(".");
		String outputfile = textfile.substring(0, n) + ".html";
		String heading = (String) data_vec.elementAt(0);
		heading = heading.trim();
		data_vec.remove(0);
		test.generate(outputfile, heading, data_vec);
	}
}