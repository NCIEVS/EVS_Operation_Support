package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.appl.*;
import gov.nih.nci.evs.restapi.config.*;
import java.io.*;
import java.util.*;
import java.net.*;
import org.apache.commons.lang3.*;

public class HTMLReportExample {
    static String NCIT_OWL = ConfigurationController.owlfile;
	static HashMap code2LabelMap = new HashMap();
    static Vector annotationProperties = null;
    static Vector objectProperties = null;
	public static HashMap annotationPropertyCode2LabelMap = null;
	public static HashMap annotationPropertyLabel2CodeMap = null;
	public static HashMap objectPropertyCode2LabelMap = null;
	public static HashMap objectPropertyLabel2CodeMap = null;
    static gov.nih.nci.evs.restapi.appl.LogicalExpression LE = null;

 	static String serviceUrl = ConfigurationController.serviceUrl;
	static String named_graph =  ConfigurationController.namedGraph;
	static String username =  ConfigurationController.username;
	static String password =  ConfigurationController.password;

	static {
		System.out.println("NCIT_OWL: " + NCIT_OWL);
		code2LabelMap = createCode2LabelMap();
		System.out.println("code2LabelMap: " + code2LabelMap.keySet().size());
		//generateMetadata();
        LE = new gov.nih.nci.evs.restapi.appl.LogicalExpression(serviceUrl, named_graph, username, password);
	}

    public static void generateMetadata() {
		OWLScanner owlscanner = new OWLScanner(NCIT_OWL);
		annotationProperties = owlscanner.extractAnnotationProperties(owlscanner.get_owl_vec());
		annotationPropertyCode2LabelMap = new HashMap();
		annotationPropertyLabel2CodeMap = new HashMap();
		for (int i=0; i<annotationProperties.size(); i++) {
			String line = (String) annotationProperties.elementAt(i);
			Vector u = gov.nih.nci.evs.restapi.util.StringUtils.parseData(line, '|');
			annotationPropertyCode2LabelMap.put((String) u.elementAt(0),(String) u.elementAt(1));
			annotationPropertyLabel2CodeMap.put((String) u.elementAt(1),(String) u.elementAt(0));
		}

		objectProperties = owlscanner.extractObjectProperties(owlscanner.get_owl_vec());
		objectPropertyCode2LabelMap = new HashMap();
		objectPropertyLabel2CodeMap = new HashMap();
		for (int i=0; i<objectProperties.size(); i++) {
			String line = (String) objectProperties.elementAt(i);
			Vector u = gov.nih.nci.evs.restapi.util.StringUtils.parseData(line, '|');
			objectPropertyCode2LabelMap.put((String) u.elementAt(0),(String) u.elementAt(1));
			objectPropertyLabel2CodeMap.put((String) u.elementAt(1),(String) u.elementAt(0));
		}
	}

	public static HashMap createCode2LabelMap() {
		OWLScanner owlscanner = new OWLScanner(NCIT_OWL);
		Vector v = owlscanner.extractHierarchicalRelationships();
		HashMap hmap = new HashMap();
        for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = gov.nih.nci.evs.restapi.util.StringUtils.parseData(line, '|');
			hmap.put((String) u.elementAt(1), HTMLDecoder.decode((String) u.elementAt(0)));
            hmap.put((String) u.elementAt(3), HTMLDecoder.decode((String) u.elementAt(2)));
		}
		return hmap;
	}

    public static String toHyperLink(String code) {
		 return toHyperLink(code, code);
	}

    public static String toHyperLink(String code, String value) {
		value = HTMLDecoder.decode(value);
		return "<a href=\"#\" onclick=\"onValueSetNodeClicked('" + code + "');return false;\">" + value + "</a>";
	}

    public static String toTooltip(String code, String value) {
		//value = HTMLDecoder.decode(value);
		String t = "<div class=\"tooltip\">" + code + "<span class=\"tooltiptext\">" + value + "</span></div>";
		//return encode(t);
		return t;
	}

	public static boolean preCondition4NCItCode(char c) {
		if (c != ' ' && c != '#' && c != '(' && c != '[') return false;
		return true;
	}

	public static String getIndentation(String line) {
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<line.length(); i++) {
			char c = line.charAt(i);
			if (c == ' ') {
				buf.append("&nbsp;");
			} else {
				break;
			}
		}
		return buf.toString();
	}

    public static String hyperlinkNCItCodes(String line) {
		Vector codes = gov.nih.nci.evs.restapi.util.StringUtils.scanNCItCodes(line);
		if (codes.size() == 0) return line;
		for (int i=0; i<codes.size(); i++) {
			String code = (String) codes.elementAt(i);
			String s = toHyperLink(code);
			line = line.replace(code, toHyperLink(code));
		}
		return line;
	}

//<A8 rdf:resource="http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#(C193249)"/>

/*
    public static String toToolTip(String code, String label) {
		if (code == null || code.length() == 0) return null;
		if (label == null || label.length() == 0) return null;
		StringBuffer buf = new StringBuffer();
		buf.append("<div class=\"tooltip\">").append(code).append("<span class=\"tooltiptext\">").append(label).append("</span>").append("</div>");
        String t = buf.toString();
        return t;
	}
*/
    public static String text2HTML(String line) {
        Vector data_vec = gov.nih.nci.evs.restapi.util.StringUtils.parseData(line, '\n');
        return text2HTML(data_vec);
	}

    public static String text2HTML(Vector data_vec) {
		StringBuffer b = new StringBuffer();
		b.append("<ul>");
		for (int i=0; i<data_vec.size(); i++) {
			String line = (String) data_vec.elementAt(i);
			//line = hyperlinkCode(line);
			b.append("<li>");
			line = line.trim();
			//if (!line.startsWith("&nbsp;")) {
			//	b.append("<p></p>");
			//}
			b.append(line);
			b.append("</li>");
		}
		b.append("</ul>");
		return b.toString();
	}


	public static void run(PrintWriter out, String textfile) {
		out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
		out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
		out.println("	<head>");

		out.println("		<meta name=\"description\" content=\"A Tree from HTML Lists\">");
		out.println("		<meta http-equiv=\"pragma\" content=\"nocache\">");
		out.println("		<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
		int n = textfile.lastIndexOf(".");
		String title = (String) textfile.substring(0, n);
		out.println("		<title>" + title + "</title>");
		out.println("		<script>");
		out.println("            function onValueSetNodeClicked(node_id) {");
		out.println("				var url=\"https://evsexplore.semantics.cancer.gov/evsexplore/concept/ncit/\"+ node_id;");
		out.println("				window.open(url, \"\", \"alwaysRaised,dependent,status,scrollbars,resizable,width=800,height=600\");");
		out.println("			}");
		out.println("		</script>");

		out.println("<style>");
		out.println(".tooltip {");
		out.println("  position: relative;");
		out.println("  display: inline-block;");
		out.println("  border-bottom: 1px dotted black;");
		out.println("}");
		out.println("");
		out.println(".tooltip .tooltiptext {");
		out.println("  visibility: hidden;");
		out.println("  width: 3600px;");
		out.println("  background-color: #555;");
		out.println("  color: #fff;");
		out.println("  text-align: center;");
		out.println("  border-radius: 6px;");
		out.println("  padding: 5px 0;");
		out.println("  position: absolute;");
		out.println("  z-index: 1;");
		out.println("  bottom: 125%;");
		out.println("  left: 50%;");
		out.println("  margin-left: -60px;");
		out.println("  opacity: 0;");
		out.println("  transition: opacity 0.3s;");
		out.println("}");
		out.println("");
		out.println(".tooltip .tooltiptext::after {");
		out.println("  content: \"\";");
		out.println("  position: absolute;");
		out.println("  top: 100%;");
		out.println("  left: 50%;");
		out.println("  margin-left: -5px;");
		out.println("  border-width: 5px;");
		out.println("  border-style: solid;");
		out.println("  border-color: #555 transparent transparent transparent;");
		out.println("}");
		out.println("");
		out.println(".tooltip:hover .tooltiptext {");
		out.println("  visibility: visible;");
		out.println("  opacity: 1;");
		out.println("}");
		out.println("</style>");

		out.println("	</head>");
		out.println("	<body>");
		out.println("	    <font face=\"verdana\" size=2px>");
		out.println("	    <form>");


		Vector v = Utils.readFile(textfile);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			line = line.trim();
			String code = line;
			String indent = getIndentation(line);
			String label = (String) code2LabelMap.get(line);
			out.println(indent + label + " (" + hyperlinkNCItCodes(code) + ")");
			String expression = LE.run(named_graph, code, false);
			out.println("<pre>");
			out.println(expression);
			out.println("</pre>");
		}
		out.println("	    </form>");
		out.println("	</body>");
		out.println("</html>");
	}


    public static String encode(String unEscapedString) {
		return StringEscapeUtils.escapeHtml4(unEscapedString);
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

	public static String getPropertyLabel(String propCode) {
		if (annotationPropertyCode2LabelMap.containsKey(propCode)) {
			return (String) annotationPropertyCode2LabelMap.get(propCode);
		} else if (objectPropertyCode2LabelMap.containsKey(propCode)) {
			return (String) objectPropertyCode2LabelMap.get(propCode);
		}
		return null;
	}

	public static void run(String textfile) {
		PrintWriter pw = null;
		try {
			int n = textfile.lastIndexOf(".");
			String htmlfile = textfile.substring(0, n) + ".html";
			pw = new PrintWriter(htmlfile);
		    run(pw, textfile);
		} catch (Exception ex) {

		} finally {
			if (pw != null) {
				try {
 					pw.close();
				} catch (Exception ex) {

				}
			}
		}
	}

	public static void main(String[] args) {
		String filename = args[0];
		run(filename);
	}

}