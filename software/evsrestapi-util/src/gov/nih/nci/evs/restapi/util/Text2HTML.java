package gov.nih.nci.evs.restapi.util;
import java.io.*;
import java.util.*;

public class Text2HTML {

	public Text2HTML() {

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

    public void run(PrintWriter out, String heading, Vector data_vec) {
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">");
		out.println("<html xmlns:c=\"http://java.sun.com/jsp/jstl/core\">");
		out.println("<head>");
		out.println("<title>Logical Expression</title>");
		out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
		out.println("<style>");
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
		out.println("");
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
			line = line.trim();
			if (!line.startsWith("&nbsp;")) {
				out.println("<p></p>");
			}
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

/*
							boolean bool = isCode(s2);
							if (bool) {
								s2 = HyperlinkHelper.toHyperlink(s2);
							}
*/
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


	public static void main(String[] args) {
		String textfile = args[0];
		Vector v = Utils.readFile(textfile);
		Vector data_vec = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			line = escape(line);
			data_vec.add(line);
		}
		Text2HTML test = new Text2HTML();
		int n = textfile.lastIndexOf(".");

		String outputfile = textfile.substring(0, n) + ".html";
		String heading = (String) data_vec.elementAt(0);
		heading = heading.trim();
		data_vec.remove(0);
		test.generate(outputfile, heading, data_vec);

	}

}