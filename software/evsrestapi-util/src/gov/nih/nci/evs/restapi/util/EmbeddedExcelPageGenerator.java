package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.bean.*;

import java.io.*;
import java.text.*;
import java.util.*;

public class EmbeddedExcelPageGenerator {
	static String DEFAULT_URL = "https://evsexplore.semantics.cancer.gov/evsexplore/concept/ncit/";
    HashMap valueSetConfigHashMap = null;
    ValueSetConfig vsc = null;
    String valueSetName = null;
    String valueSetDescription = null;
	private String name;
	private String uri;
	private String reportURI;
	private String extractionRule;
	String excelfile = null;
	int sheet = 0;
	int col = 0;
	String code = null;
	boolean cdisc = false;

	public EmbeddedExcelPageGenerator(String valueSetName, String valueSetDescription) {
		this.valueSetName = valueSetName;
		this.valueSetDescription = valueSetDescription;
        initialize();
	}

	public void initialize() {
		valueSetConfigHashMap = ValueSetDefinitionConfig.getValueSetConfigHashMap();
		vsc = getValueSetConfig(valueSetName);
		if (vsc == null) {
			System.out.println(valueSetName + " not found.");
			return;
		}
		reportURI = vsc.getReportURI();
		System.out.println(reportURI);
		int n = reportURI.lastIndexOf("/");
		excelfile = reportURI.substring(n+1, reportURI.length());
		System.out.println(excelfile);
		File f = new File(excelfile);
		if (!f.exists()) {
			DownloadPage.download(reportURI, f);
		} else {
			System.out.println("Excel file " + excelfile + " exists.");
		}
		extractionRule = vsc.getExtractionRule();
		System.out.println("Data extraction rule: ");
		Vector u = StringUtils.parseData(extractionRule, ':');
		sheet = Integer.parseInt((String) u.elementAt(0)) - 1;
		col = Integer.parseInt((String) u.elementAt(1)) - 1;
        code = (String) u.elementAt(2);
		System.out.println("sheet: " + sheet);
		System.out.println("col: " + col);
		System.out.println("code: " + code);

		if (reportURI.indexOf("CDISC") != -1) {
			cdisc = true;
		}
	}

	public ValueSetConfig getValueSetConfig(String terminology) {
        Iterator it = valueSetConfigHashMap.keySet().iterator();
        while (it.hasNext()) {
			String uri = (String) it.next();
			ValueSetConfig vsc = (ValueSetConfig) valueSetConfigHashMap.get(uri);
			if (vsc.getName().compareTo(terminology) == 0) {
				return vsc;
			}
		}
		return null;
	}

    public Vector generatePageContent() {
		int startIndex = ExcelUtils.getExcelStartRow(excelfile, sheet, col, code);
		System.out.println("startIndex: " + startIndex);
		ResolvedValueSetIteratorHolder rvsi = null;
		try {
		    rvsi = new ResolvedValueSetIteratorHolder(excelfile, sheet, startIndex, col, code, DEFAULT_URL, cdisc);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		ListIterator iterator = rvsi.getResolvedValueSetIterator();
		Vector w = new Vector();
		while (iterator.hasNext()) {
			String t = (String) iterator.next();
			w.add(t);
		}
		return w;
	}

    public void run(PrintWriter out) {
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">");
		out.println("<html xmlns:c=\"http://java.sun.com/jsp/jstl/core\">");
		out.println("<head>");
		String uri = vsc.getUri();
		out.println("  <title>NCI Term Browser - Value Set " + uri + "</title>");
		out.println("  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
		out.println("<body>");

		out.println("<style type=\"text/css\">");
		out.println("    body, .mt {padding:0px;font-family: Tahoma, sans-serif;font-size: 0.9em;}");
		out.println("    body {margin:0px;}");
		out.println("    p {margin:30px 0px 30px 0px;}");
		out.println("    table.mt {border-width: 1px;border-spacing:0px ;border-style: solid;border-color: #cfcfcf;border-collapse: collapse;background-color: transparent;}");
		out.println("    table.mt th {border-width: 1px;padding: 1px;border-style: solid;border-color: #cfcfcf;white-space: nowrap; background-color: #afafaf;text-align:left;}");
		out.println("    table.mt td {border-width: 1px;padding: 1px;border-style: solid;border-color: #cfcfcf;text-align: left;vertical-align:top;}");
		out.println("    .frc {background: #efefef;}");
		out.println("</style>");

		out.println("<style>");
		out.println("html, body{ box-sizing:border-box}");
		out.println("*, *:before, *:after{ box-sizing:inherit}");
		out.println("");
		out.println(".table-scroll{");
		out.println("	position:relative;");
		out.println("	margin:auto;");
		out.println("	width:100%;");
		out.println("	overflow:auto;");
		out.println("	height:300px;");
		out.println("	overflow-y:scroll;");
		out.println("}");
		out.println(".table-wrap{");
		out.println("	margin-left:-9em;");
		out.println("	outline:1px solid;");
		out.println("	outline-offset:-1px;");
		out.println("	overflow:auto;");
		out.println("}");
		out.println("</style>");

		out.println("<table>");
		out.println("                    <tr><td colspan=\"2\" align=\"left\"><b>");
		out.println(valueSetName);
		out.println("                    </b></td></tr>");
		out.println("                    <tr><td colspan=\"2\" align=\"left\">");
		out.println("<p>" + valueSetDescription + "</p>");
		out.println("                    </td></tr>");
		out.println("</table>");
		out.println("");
		out.println("          <hr/>");

		out.println("<table border=\"0\" width=\"900\" role='presentation'>");
		out.println("	<tr><td>");
		out.println("	<div style=\"float:left;width:360px;\">");
		out.println("<table id=\"rvs_table\" width=\"900\" class=\"mt\">");

        System.out.println("GeneratePageContent ...");
		Vector w = generatePageContent();
		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			out.println(line);
		}

		out.println("</table>");
		out.println("");
		out.println("	</div>");
		out.println("	</td></tr>");
		out.println("</table>");
		out.println("</body>");
		out.println("</html>");
    }

    public static void main(String[] args) {
		String valueSetName = args[0];
		String valueSetDescription = args[1];
        EmbeddedExcelPageGenerator generator = new EmbeddedExcelPageGenerator(valueSetName, valueSetDescription);
		PrintWriter pw = null;
		String outputfile = valueSetName.replace(" ", "_") + ".html";
		try {
			pw = new PrintWriter(outputfile, "UTF-8");
            generator.run(pw);
		} catch (Exception ex) {

		} finally {
			try {
				pw.close();
				System.out.println("Output file " + outputfile + " generated.");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}

