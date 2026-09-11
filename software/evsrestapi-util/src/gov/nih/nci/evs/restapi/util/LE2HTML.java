package gov.nih.nci.evs.restapi.util;
import java.io.*;
import java.util.*;

class LE2HTML {

	public static void run(String textfile) {
		Vector raw_data_vec = Utils.readFile(textfile);
		HashMap roleName2RangeNameMap = NCItProperties.getRoleName2RangeNameMap();
        Vector data_vec = new Vector();
		HTMLTemplate test = null;
		try {
			test = new HTMLTemplate();
			test.setTooltipWidth(200);
			test.setTooltipHashMap(roleName2RangeNameMap);
			for (int i=0; i<raw_data_vec.size(); i++) {
				String line = (String) raw_data_vec.elementAt(i);
				if (line.indexOf("Logical expression of:") != -1) {
					data_vec.add("<hr></hr><h2><center>" + line + "</center></h2>");
				} else {
					Vector u = StringUtils.parseData(line, '\t');
					String indent = HTMLTemplate.getIndentation(line);
					for (int j=0; j<u.size(); j++) {
						String s = (String) u.elementAt(j);
						if (roleName2RangeNameMap.containsKey(s)) {
							String toolTipvalue = (String) roleName2RangeNameMap.get(s);
							line = line.replace(s, test.toTooltip(s));
						}
					}
					if (indent.length() == 0) {
						data_vec.add("<p></p>" + line);
					} else {
						data_vec.add(indent + line);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		int n = textfile.lastIndexOf(".");
		String outputfile = textfile.substring(0, n) + ".html";
		String heading = (String) data_vec.elementAt(0);
		heading = heading.trim();
		data_vec.remove(0);
		test.generate(outputfile, heading, data_vec);
	}


	public static void main(String[] args) {
		String textfile = args[0];
		run(textfile);
	}

}
