package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;
import java.io.*;
import java.net.URI;
import java.text.*;
import java.util.*;

public class HTMLAppender {
	static String NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile; //"ThesaurusInferred_forTS.owl";

	static HierarchyHelper hh = null;
	static HashMap a8Map = null;
	static {
		hh = new HierarchyHelper(Utils.readFile("parent_child.txt"));
		a8Map = new HashMap();
		Vector v = Utils.readFile("A8.txt");
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String src = (String) u.elementAt(0);
			String target = (String) u.elementAt(2);
			Vector w = new Vector();
			if (a8Map.containsKey(target)) {
				w = (Vector) a8Map.get(target);
			}
			if (!w.contains(src)) {
				w.add(src);
			}
			a8Map.put(target, w);
		}
	}

    public static String getAppender(String code) {
		Vector members = (Vector) a8Map.get(code);
		if (members == null || members.size() == 0) {
			return "";
		} else {
			return " (count: " + members.size() + ")";
		}
	}

    public static void run(String htmlfile) {
		Vector v = Utils.readFile(htmlfile);
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (line.indexOf("data-jstree") != -1 || line.indexOf("show_hide") != -1) {
                 int n1 = line.lastIndexOf(">C");
                 int n2 = line.lastIndexOf("<");
                 String code = line.substring(n1+1, n2);
                 String appender = getAppender(code);
                 String s = line.substring(0, n1) + ">" + code + "</a>)" + appender;
                 w.add(s);
			} else {
				 w.add(line);
			}
		}
		Utils.saveToFile("v2_" + htmlfile, w);
	}

    public static HashMap getPropertyMap(String propCode) {
        Vector dataVec = new Vector();
		dataVec.add(propCode);
		PropertyRetrieval propertyRetrieval = new PropertyRetrieval(NCIT_OWL, dataVec);
		HashMap propertyMap = propertyRetrieval.getPropertyMap();
		return (HashMap) propertyMap.get(propCode);
	}

    public static String getPropertyValues(String propertyCode, String propertyValue) {
		return "(" + propertyCode + ": " + propertyValue + ")";
	}

	public static Vector appendTreeData(String treefile, String propCode) {
		HashMap hmap = getPropertyMap(propCode);
		Vector v = Utils.readFile(treefile);
		Vector w = new Vector();
		int knt = 0;
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (line.indexOf("data-jstree=") != -1 && line.indexOf("</a>") != -1 ) {
				int n1 = line.lastIndexOf("</a>");
				String t = line.substring(0, n1);
				int n2 = t.lastIndexOf(">");
				String code = line.substring(n2+1, t.length());
				if (StringUtils.isNCItCode(code)) {
					Vector values = (Vector) hmap.get(code);
					t = Utils.vector2DelimitedString(values, ';');
					line = line + "&nbsp;" + getPropertyValues("P106", t);
				}
			}
			w.add(line);
		}
		return w;
    }

	public static void main(String args[]) {
		String htmlfile = args[0];
		run(htmlfile);
	}
}

