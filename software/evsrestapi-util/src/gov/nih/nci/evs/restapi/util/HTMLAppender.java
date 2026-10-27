package gov.nih.nci.evs.restapi.util;

import java.io.*;
import java.net.URI;
import java.text.*;
import java.util.*;

public class HTMLAppender {
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
			if (line.indexOf("data-jstree") != -1) {
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

	public static void main(String args[]) {
		String htmlfile = args[0];
		run(htmlfile);
	}
}

