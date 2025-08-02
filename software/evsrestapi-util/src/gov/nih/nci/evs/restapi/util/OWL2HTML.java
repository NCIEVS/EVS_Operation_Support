package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;
import gov.nih.nci.evs.restapi.bean.*;
import java.io.*;
import java.util.*;
import java.net.*;
import org.apache.commons.lang3.*;

public class OWL2HTML {
    static String NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile;
    static String PARENT_CHILD_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile;

	static HashMap code2LabelMap = new HashMap();
    static Vector annotationProperties = null;
    static Vector objectProperties = null;
	public static HashMap annotationPropertyCode2LabelMap = null;
	public static HashMap annotationPropertyLabel2CodeMap = null;
	public static HashMap objectPropertyCode2LabelMap = null;
	public static HashMap objectPropertyLabel2CodeMap = null;

	static {
		code2LabelMap = createCode2LabelMap();
		generateMetadata();
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
		Vector v = Utils.readFile(PARENT_CHILD_FILE);
		HashMap hmap = new HashMap();
        for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = gov.nih.nci.evs.restapi.util.StringUtils.parseData(line, '|');
			hmap.put((String) u.elementAt(1), HTMLDecoder.decode((String) u.elementAt(0)));
            hmap.put((String) u.elementAt(3), HTMLDecoder.decode((String) u.elementAt(2)));
            String child_label = HTMLDecoder.decode((String) u.elementAt(2));
            String child_code = HTMLDecoder.decode((String) u.elementAt(3));
		}
		return hmap;
	}

    public static String toHyperLink(String code) {
		//return "<a href=\"#\" onclick=\"onValueSetNodeClicked('" + code + "');return false;\">(" + code + ")</a>";
		 return toHyperLink(code, code);
	}

    public static String toHyperLink(String code, String value) {
		value = HTMLDecoder.decode(value);
		return "<a href=\"#\" onclick=\"onValueSetNodeClicked('" + code + "');return false;\">(" + value + ")</a>";
	}

    public static String toTooltip(String code, String value) {
		value = HTMLDecoder.decode(value);
		String t = "<div class=\"tooltip\">" + code + "<span class=\"tooltiptext\">" + value + "</span></div>";
		return encode(t);
	}

	public static boolean preCondition4NCItCode(char c) {
		if (c != ' ' && c != '#' && c != '(' && c != '[') return false;
		return true;
	}

	public static Vector scanNCItCodes(String line) {
		String str = "";
		String prev_code = "";
		Vector w = new Vector();
		int i = 0;
		boolean istart = false;
		while (i < line.length()) {
			char c = line.charAt(i);
			if (c != 'C') {
				if (istart) {
					str = str + c;
					if (gov.nih.nci.evs.restapi.util.StringUtils.isNCItCode(str)) {
						if (prev_code == "") {
							if (i>0 && preCondition4NCItCode(line.charAt(i-1))) {
								prev_code = str;
							}
						} else {
							if (str.substring(0, str.length()-1).compareTo(prev_code) == 0) {
								prev_code = str;
							}
						}
					} else {
						if (prev_code.length() > 0) {
							if (!w.contains(prev_code)) {
								w.add(prev_code);
								prev_code = "";
							}
						}
					}
				} else {
					str = "";
				}
			} else {
				if (gov.nih.nci.evs.restapi.util.StringUtils.isNCItCode(str)) {
					w.add(str);
				}
				istart = true;
				str = "C";
			}
			i++;
		}
		return w;
	}

    public static String hyperlinkNCItCodes(String line) {
		Vector codes = gov.nih.nci.evs.restapi.util.StringUtils.scanNCItCodes(line);
		if (codes.size() == 0) return line;
		for (int i=0; i<codes.size(); i++) {
			String code = (String) codes.elementAt(i);
			line = line.replace(code, toHyperLink(code));
		}
		return line;
	}

	public static void run(PrintWriter out, String textfile) {
		out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
		out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
		out.println("	<head>");

      out.println("		<meta name=\"description\" content=\"A Tree from HTML Lists\">");
      out.println("		<meta http-equiv=\"pragma\" content=\"nocache\">");
      out.println("		<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
      int n = textfile.lastIndexOf(",");
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
		out.println("  width: 120px;");
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
			if (line.startsWith("</")) {
				line = encode(line);
				out.println(line + "<p></p>");
			} else {
				String tag = null;
				String value = null;
				String propLabel = null;
				try {
					tag = getTagName(line);
					value = getTagValue(line);
					propLabel = getPropertyLabel(tag);
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				if (tag != null && tag.length() > 0 && propLabel != null && propLabel.length() > 0) {
					line = toToolTipText(tag, propLabel, value);
					out.println(line + "<p></p>");
				} else {
					line = encode(line);
					line = hyperlinkNCItCodes(line);
					out.println(line + "<p></p>");
				}
			}
		}
		out.println("	    </form>");
		out.println("	</body>");
		out.println("</html>");
	}

    public static String toToolTipText(String code, String label, String text) {
		if (code == null || code.length() == 0) return null;
		if (label == null || label.length() == 0) return null;

		StringBuffer buf = new StringBuffer();
		buf.append("&lt;<div class=\"tooltip\">").append(code).append("<span class=\"tooltiptext\">").append(label).append("</span>").append("</div>&gt;")
		.append(text)
		   .append("&lt;/<div class=\"tooltip\">").append(code).append("<span class=\"tooltiptext\">").append(label).append("</span>").append("</div>&gt;");
        return buf.toString();
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

		/*
		String t = toToolTipText("P108", "Preferred_Name", "Age in Days at Imaging");

		t = "<P106>Organism Attribute</P106>";
		String tag = getTagName(t);
		String value = getTagValue(t);
		String propLabel = getPropertyLabel(tag);

		System.out.println(t);
		System.out.println(tag);
		System.out.println(value);
		System.out.println(propLabel);

		String line = toToolTipText(tag, propLabel, value);
		System.out.println(line);
		*/
	}

}