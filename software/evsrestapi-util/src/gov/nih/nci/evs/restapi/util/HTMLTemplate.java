package gov.nih.nci.evs.restapi.util;
import java.io.*;
import java.util.*;

public class HTMLTemplate {
	public HashMap tooltipHashMap = null;
	int tooltip_width = 800;

	public HTMLTemplate() {

	}

	public void setTooltipHashMap(HashMap hmap) {
		this.tooltipHashMap = hmap;
	}

	public void setTooltipWidth(int width) {
		tooltip_width = width;
	}

	public static String getIndentation(String line) {
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<line.length(); i++) {
			char c = line.charAt(i);
			if (c == ' ' || c == '\t') {
				buf.append("&nbsp;&nbsp;&nbsp;&nbsp;");
			} else {
				break;
			}
		}
		return buf.toString();
	}

    public void run(PrintWriter out, String heading, Vector data_vec) {
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">");
		out.println("<html xmlns:c=\"http://java.sun.com/jsp/jstl/core\">");
		out.println("<head>");
		out.println("<title>Logical Expression</title>");
		out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");

		out.println("		<script>");
		out.println("            function onValueSetNodeClicked(node_id) {");
		out.println("				var url=\"https://evsexplore.semantics.cancer.gov/evsexplore/concept/ncit/\"+ node_id;");
		out.println("				window.open(url, \"\", \"alwaysRaised,dependent,status,scrollbars,resizable,width=800,height=600\");");
		out.println("			}");
		out.println("		</script>");

		out.println("<style>");

		out.println(".scroll-container {");
		out.println("    height: 100px;");
		out.println("    overflow: auto;");
		out.println("    position: relative;");
		out.println("}");

		out.println("ul");
		out.println("{");
		out.println("    list-style-type: none;");
		out.println("}");
		out.println(".tooltip {");
		out.println("  position: relative;");
		out.println("  display: inline-block;");

		out.println("  border-bottom: 1px dotted black;");
		out.println("  cursor: pointer;");
		out.println("}");
		out.println("");
		out.println(".tooltiptext {");
		out.println("  visibility: hidden;");
		out.println("  width: " + tooltip_width + "px;");
		out.println("  background-color: black;");
		out.println("  color: #fff;");
		out.println("  border-radius: 6px;");
		out.println("  padding: 5px 0;");
		out.println("  position: absolute;");
		out.println("  z-index: 1;");
		out.println("}");
		out.println("");
		out.println(".tooltip:hover .tooltiptext {");
		out.println("  visibility: visible;");
		out.println("}");

		out.println("table {");
		out.println("    border-collapse: collapse;");
		out.println("}");
		out.println("table, td, th {");
		out.println("    border: 1px solid black;");
		out.println("}");
		out.println(" li {list-style-type: none;}");
		out.println("</style>");
		out.println("</head>");
		out.println("");
		out.println("<body>");

        out.println("<div>");
		out.println("<a href=\"https://evs.nci.nih.gov/\">");
		out.println("  <img");
		out.println("      src=\"https://evs.nci.nih.gov/sites/default/files/evs-logo_1.png\" alt=\"Enterprise Vocabulary Services Home Page\"");
		out.println("  />");
		out.println("</a>");
		out.println("</div>");

		out.println("");
		out.println("");
		out.println("<center>");
		heading = hyperlinkCode(heading);
		out.println("<h2>" + heading + "</h2>");
		out.println("</center>");
		out.println("");
		out.println("<ul>");
		for (int i=0; i<data_vec.size(); i++) {
			String line = (String) data_vec.elementAt(i);
			line = hyperlinkCode(line);
			out.println("<li>");
			out.println(line);
			out.println("</li>");
		}
		out.println("</ul>");
		out.println("");
		out.println("");
		out.println("<br></br>");
		out.println("<br></br>");
		out.println("<center><b>");
		out.println("(Source; NCI Thesaurus, version )");
		out.println("</b></center>");
		out.println("</body>");
		out.println("</html>");
    }

    public static String hyperlinkCode(String line) {
		int n = line.lastIndexOf("(");
		if (n != -1) {
			String code = line.substring(n+1, line.length()-1);
			if (HTMLTableDataConverter.isCode(code)) {
				line = line.replace(code, HyperlinkHelper.toHyperlink(code));
				return line;
			}
		}
		return line;
	}

	public void generate(String outputfile, String title, Vector data_vec) {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(outputfile, "UTF-8");
            run(pw, title, data_vec);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				pw.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}


	public static String escape(String s) {
		StringBuilder builder = new StringBuilder();
		boolean previousWasASpace = false;
		for( char c : s.toCharArray() ) {
			if( c == ' ' ) {
				if( previousWasASpace ) {
					builder.append("&nbsp;");
					previousWasASpace = false;
					continue;
				}
				previousWasASpace = true;
			} else {
				previousWasASpace = false;
			}
			switch(c) {
				case '<': builder.append("&lt;"); break;
				case '>': builder.append("&gt;"); break;
				case '&': builder.append("&amp;"); break;
				case '"': builder.append("&quot;"); break;
				case '\n': builder.append("<br>"); break;
				// We need Tab support here, because we print StackTraces as HTML
				case '\t': builder.append("&nbsp; &nbsp; &nbsp;"); break;
				default:
					if( c < 128 ) {
						builder.append(c);
					} else {
						builder.append("&#").append((int)c).append(";");
					}
			}
		}
		return builder.toString();
	}

    public String toTooltip(String key) {
		String value = (String) tooltipHashMap.get(key);
		return toTooltip(key, value);
	}

    public String toTooltip(String key, String value) {
		StringBuffer buf = new StringBuffer();
		buf.append("<div class=\"tooltip\">").append(key);
		buf.append("  <span class=\"tooltiptext\">").append(value);
		buf.append("</div>");
		return buf.toString();
	}

	public static void main(String[] args) {
		String textfile = args[0];
		Vector raw_data_vec = Utils.readFile(textfile);
		HashMap roleName2RangeNameMap = NCItProperties.getRoleName2RangeNameMap();

		HTMLTemplate test = new HTMLTemplate();
		test.setTooltipWidth(200);
		test.setTooltipHashMap(roleName2RangeNameMap);
		Vector data_vec = new Vector();
		for (int i=0; i<raw_data_vec.size(); i++) {
			String line = (String) raw_data_vec.elementAt(i);
			String indent = getIndentation(line);
			Vector u = StringUtils.parseData(line, '\t');
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

		int n = textfile.lastIndexOf(".");
		String outputfile = textfile.substring(0, n) + ".html";
		String heading = (String) data_vec.elementAt(0);
		heading = heading.trim();
		data_vec.remove(0);
		test.generate(outputfile, heading, data_vec);
	}
}