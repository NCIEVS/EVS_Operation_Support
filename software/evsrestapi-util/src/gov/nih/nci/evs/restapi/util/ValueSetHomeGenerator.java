package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.bean.*;
import java.io.*;
import java.text.*;
import java.util.*;

/**
 * <!-- LICENSE_TEXT_START -->
 * Copyright 2022 Guidehouse. This software was developed in conjunction
 * with the National Cancer Institute, and so to the extent government
 * employees are co-authors, any rights in such works shall be subject
 * to Title 17 of the United States Code, section 105.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the disclaimer of Article 3,
 *      below. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *   2. The end-user documentation included with the redistribution,
 *      if any, must include the following acknowledgment:
 *      "This product includes software developed by Guidehouse and the National
 *      Cancer Institute."   If no such end-user documentation is to be
 *      included, this acknowledgment shall appear in the software itself,
 *      wherever such third-party acknowledgments normally appear.
 *   3. The names "The National Cancer Institute", "NCI" and "Guidehouse" must
 *      not be used to endorse or promote products derived from this software.
 *   4. This license does not authorize the incorporation of this software
 *      into any third party proprietary programs. This license does not
 *      authorize the recipient to use any trademarks owned by either NCI
 *      or GUIDEHOUSE
 *   5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED
 *      WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *      OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE
 *      DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
 *      GUIDEHOUSE, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT,
 *      INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 *      BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *      LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *      CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 *      LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 *      ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *      POSSIBILITY OF SUCH DAMAGE.
 * <!-- LICENSE_TEXT_END -->
 */

/**
 * @author EVS Team
 * @version 1.0
 *
 * Modification history:
 *     Initial implementation kim.ong@nih.gov
 *
 */
public class ValueSetHomeGenerator {
	static String DEFAULT_URL = "https://nciterms65.nci.nih.gov/ncitbrowser/ConceptReport.jsp?dictionary=NCI_Thesaurus&ns=ncit";
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

	public ValueSetHomeGenerator(String valueSetName, String valueSetDescription) {
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
		//System.out.println(vsc.toString());
		//SPL Color Terminology|http://evs.nci.nih.gov/valueset/FDA/C54453|https://evs.nci.nih.gov/ftp1/FDA/SPL/FDA-SPL_NCIt_Subsets.xls|1:2:C54453

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
		out.println("  <title>NCI Term Browser - Value Set http://evs.nci.nih.gov/valueset/FDA/C54453 </title>");
		out.println("  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
		out.println("");
		out.println("<style type=\"text/css\">");
		out.println("/*margin and padding on body element");
		out.println("  can introduce errors in determining");
		out.println("  element position and are not recommended;");
		out.println("  we turn them off as a foundation for YUI");
		out.println("  CSS treatments. */");
		out.println("body {");
		out.println("	margin:0;");
		out.println("	padding:0;");
		out.println("}");
		out.println("</style>");
		out.println("");
		out.println("");
		out.println("<script type=\"text/javascript\" src=\"ncitbrowser/js/event_simulate.js\"></script>");
		out.println("<script type=\"text/javascript\" src=\"ncitbrowser/js/value_set_tree_navigation.js\"></script>");
		out.println("<!--Additional custom style rules for this example:-->");
		out.println("<style type=\"text/css\">");
		out.println("");
		out.println("");
		out.println(".ygtvcheck0 { background: url(ncitbrowser/images/yui/treeview/check0.gif) 0 0 no-repeat; width:16px; height:20px; float:left; cursor:pointer; }");
		out.println(".ygtvcheck1 { background: url(ncitbrowser/images/yui/treeview/check1.gif) 0 0 no-repeat; width:16px; height:20px; float:left; cursor:pointer; }");
		out.println(".ygtvcheck2 { background: url(ncitbrowser/images/yui/treeview/check2.gif) 0 0 no-repeat; width:16px; height:20px; float:left; cursor:pointer; }");
		out.println("");
		out.println("");
		out.println(".ygtv-edit-TaskNode  {	width: 190px;}");
		out.println(".ygtv-edit-TaskNode .ygtvcancel, .ygtv-edit-TextNode .ygtvok  {	border:none;}");
		out.println(".ygtv-edit-TaskNode .ygtv-button-container { float: right;}");
		out.println(".ygtv-edit-TaskNode .ygtv-input  input{	width: 140px;}");
		out.println(".whitebg {");
		out.println("	background-color:white;");
		out.println("}");
		out.println("</style>");
		out.println("");
		out.println("  <link rel=\"stylesheet\" type=\"text/css\" href=\"ncitbrowser/css/styleSheet.css\" />");
		out.println("  <link rel=\"shortcut icon\" href=\"ncitbrowser/favicon.ico\" type=\"image/x-icon\" />");
		out.println("");
		out.println("  <script type=\"text/javascript\" src=\"ncitbrowser/js/script.js\"></script>");
		out.println("  <script type=\"text/javascript\" src=\"ncitbrowser/js/tasknode.js\"></script>");
		out.println("  <script type=\"text/javascript\" src=\"ncitbrowser/js/search.js\"></script>");
		out.println("  <script type=\"text/javascript\" src=\"ncitbrowser/js/dropdown.js\"></script>");
		out.println("  <script type=\"text/javascript\">");
		out.println("");
		out.println("    function refresh() {");
		out.println("");
		out.println("      var selectValueSetSearchOptionObj = document.forms[\"valueSetSearchForm\"].selectValueSetSearchOption;");
		out.println("");
		out.println("      for (var i=0; i<selectValueSetSearchOptionObj.length; i++) {");
		out.println("        if (selectValueSetSearchOptionObj[i].checked) {");
		out.println("            selectValueSetSearchOption = selectValueSetSearchOptionObj[i].value;");
		out.println("        }");
		out.println("      }");
		out.println("");
		out.println("");
		out.println("      window.location.href=\"ncitbrowser/pages/value_set_source_view.jsf?refresh=1\"");
		out.println("          + \"&nav_type=valuesets\" + \"&opt=\"+ selectValueSetSearchOption;");
		out.println("");
		out.println("    }");
		out.println("  </script>");
		out.println("");
		out.println("");
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
		out.println("<script async src=\"https://www.googletagmanager.com/gtag/js?id=G-21QRTJ0WQS\"></script>");
		out.println("<script>");
		out.println("	window.dataLayer = window.dataLayer || [];");
		out.println("	function gtag(){dataLayer.push(arguments);}");
		out.println("	gtag('js', new Date());");
		out.println("	gtag('config', 'G-21QRTJ0WQS');");
		out.println("</script>");
		out.println("</head>");
		out.println("");
		out.println("<style>");
		out.println(".outer {position:relative}");
		out.println(".inner {");
		out.println("  overflow-x:scroll;");
		out.println("  overflow-y:scroll;");
		out.println("  width:900px;");
		out.println("  margin-left:5px;");
		out.println("  height:500px;");
		out.println("}");
		out.println("</style>");
		out.println("");
		out.println("");
		out.println("<style type=\"text/css\">");
		out.println("    body, .mt {padding:0px;font-family: Tahoma, sans-serif;font-size: 0.9em;}");
		out.println("    body {margin:0px;}");
		out.println("    p {margin:30px 0px 30px 0px;}");
		out.println("    table.mt {border-width: 1px;border-spacing:0px ;border-style: solid;border-color: #cfcfcf;border-collapse: collapse;background-color: transparent;}");
		out.println("    table.mt th {border-width: 1px;padding: 1px;border-style: solid;border-color: #cfcfcf;white-space: nowrap; background-color: #afafaf;text-align:left;}");
		out.println("    table.mt td {border-width: 1px;padding: 1px;border-style: solid;border-color: #cfcfcf;text-align: left;vertical-align:top;}");
		out.println("    .frc {background: #efefef;}");
		out.println("</style>");
		out.println("");
		out.println("<body onLoad=\"collapse_all();\">");
		out.println("  <script type=\"text/javascript\" src=\"ncitbrowser/js/wz_tooltip.js\"></script>");
		out.println("  <script type=\"text/javascript\" src=\"ncitbrowser/js/tip_centerwindow.js\"></script>");
		out.println("  <script type=\"text/javascript\" src=\"ncitbrowser/js/tip_followscroll.js\"></script>");
		out.println("  <!-- Begin Skip Top Navigation -->");
		out.println("  <a href=\"#evs-content\" class=\"hideLink\" accesskey=\"1\" title=\"Skip repetitive navigation links\">skip navigation links</A>");
		out.println("  <!-- End Skip Top Navigation -->");
		out.println("");
		out.println("<!-- nci banner -->");
		out.println("<div style='clear:both;margin-top:-5px;padding:8px;height:47px;color:white;background-color:#C31F40'>");
		out.println("  <a href=\"https://www.cancer.gov\" target=\"_blank\">");
		out.println("    <img src=\"ncitbrowser/images/banner-red.png\"");
		out.println("      width=\"955\" height=\"39\" border=\"0\"");
		out.println("      alt=\"National Cancer Institute\"/>");
		out.println("  </a>");
		out.println("</div>");
		out.println("<!-- end nci banner -->");
		out.println("");
		out.println("  <div class=\"center-page_960\">");
		out.println("    <!-- EVS Logo -->");
		out.println("<div>");
		out.println("  <img src=\"ncitbrowser/images/evs-logo-swapped.gif\" alt=\"EVS Logo\"");
		out.println("       width=\"941\" height=\"26\" border=\"0\"");
		out.println("       usemap=\"#external-evs\" />");
		out.println("  <map id=\"external-evs\" name=\"external-evs\">");
		out.println("    <area shape=\"rect\" coords=\"0,0,140,26\"");
		out.println("      href=\"ncitbrowser/start.jsf\" target=\"_self\"");
		out.println("      alt=\"NCI Term Browser\" />");
		out.println("    <area shape=\"rect\" coords=\"520,0,745,26\"");
		out.println("      href=\"https://evs.nci.nih.gov/\" target=\"_blank\"");
		out.println("      alt=\"Enterprise Vocabulary Services\" />");
		out.println("  </map>");
		out.println("</div>");
		out.println("");
		out.println("");
		out.println("<table cellspacing=\"0\" cellpadding=\"0\" border=\"0\" role='presentation'>");
		out.println("  <tr>");
		out.println("    <td width=\"5\"></td>");
		out.println("    <td><a href=\"ncitbrowser/pages/multiple_search.jsf?nav_type=terminologies\">");
		out.println("      <img name=\"tab_terms\" src=\"ncitbrowser/images/tab_terms.gif\"");
		out.println("        border=\"0\" alt=\"Terminologies\" title=\"Terminologies\" /></a></td>");
		out.println("    <td><a href=\"ncitbrowser/ajax?action=create_src_vs_tree\">");
		out.println("      <img name=\"tab_valuesets\" src=\"ncitbrowser/images/tab_valuesets_clicked.gif\"");
		out.println("        border=\"0\" alt=\"Value Sets\" title=\"ValueSets\" /></a></td>");
		out.println("    <td><a href=\"ncitbrowser/pages/mapping_search.jsf?nav_type=mappings\">");
		out.println("      <img name=\"tab_map\" src=\"ncitbrowser/images/tab_map.gif\"");
		out.println("        border=\"0\" alt=\"Mappings\" title=\"Mappings\" /></a></td>");
		out.println("  </tr>");
		out.println("</table>");
		out.println("");
		out.println("<div class=\"mainbox-top\"><img src=\"ncitbrowser/images/mainbox-top.gif\" width=\"945\" height=\"5\" alt=\"\"/></div>");
		out.println("<!-- end EVS Logo -->");
		out.println("    <!-- Main box -->");
		out.println("    <div id=\"main-area_960\">");
		out.println("");
		out.println("      <!-- Thesaurus, banner search area -->");
		out.println("      <div class=\"bannerarea_960\">");
		out.println("        <a class=\"vocabularynamebanner\" href=\"ncitbrowser/ajax?action=create_src_vs_tree&vsd_uri=http://evs.nci.nih.gov/valueset/FDA/C54453\">");
		out.println("	             <div class=\"vocabularynamebanner\">");
		out.println("                <div class=\"vocabularynameshort\" STYLE=\"font-size: 22px; font-family : Arial\">");
		out.println(valueSetName);
		out.println("                </div>");
		out.println("              </div>");
		out.println("	       </a>");
		out.println("        <div class=\"search-globalnav_960\">");
		out.println("          <!-- Search box -->");
		out.println("          <div class=\"searchbox-top\"><img src=\"ncitbrowser/images/searchbox-top.gif\" width=\"352\" height=\"2\" alt=\"SearchBox Top\" /></div>");
		out.println("          <div class=\"searchbox\">");
		out.println("");
		out.println("");
		out.println("<form id=\"valueSetSearchForm\" name=\"valueSetSearchForm\" method=\"post\" action=\"ncitbrowser/ajax?action=search_value_set\" class=\"search-form-main-area\" enctype=\"application/x-www-form-urlencoded;charset=UTF-8\">");
		out.println("<input type=\"hidden\" name=\"valueSetSearchForm\" value=\"valueSetSearchForm\" />");
		out.println("<input type=\"hidden\" name=\"view\" value=\"1\" />");
		out.println("            <input type=\"hidden\" id=\"checked_vocabularies\" name=\"checked_vocabularies\" value=\"\" />");
		out.println("            <input type=\"hidden\" id=\"partial_checked_vocabularies\" name=\"partial_checked_vocabularies\" value=\"\" />");
		out.println("            <input type=\"hidden\" id=\"value_set_home\" name=\"value_set_home\" value=\"true\" />");
		out.println("            <input type=\"hidden\" name=\"vsd_uri\" value=\"http://evs.nci.nih.gov/valueset/FDA/C54453\" />");
		out.println("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"margin: 2px\" role='presentation'>");
		out.println("  <tr valign=\"top\" align=\"left\">");
		out.println("    <td align=\"left\" class=\"textbody\">");
		out.println("");
		out.println("                  <input CLASS=\"searchbox-input-2\"");
		out.println("                    name=\"matchText\"");
		out.println("                    value=\"\"");
		out.println("                    onFocus=\"active = true\"");
		out.println("                    onBlur=\"active = false\"");
		out.println("                    onkeypress=\"return submitEnter('valueSetSearchForm:valueset_search',event)\"");
		out.println("                    tabindex=\"1\"/>");
		out.println("");
		out.println("");
		out.println("                <input id=\"valueSetSearchForm:valueset_search\" type=\"image\" src=\"ncitbrowser/images/search.gif\" name=\"valueSetSearchForm:valueset_search\" alt=\"Search value sets containing matched concepts\" tabindex=\"2\" class=\"searchbox-btn\" /><a href=\"ncitbrowser/pages/help.jsf#searchhelp\" tabindex=\"3\"><img src=\"ncitbrowser/images/search-help.gif\" alt=\"Search Help\" style=\"border-width:0;\" class=\"searchbox-btn\" /></a>");
		out.println("");
		out.println("");
		out.println("    </td>");
		out.println("  </tr>");
		out.println("");
		out.println("  <tr valign=\"top\" align=\"left\">");
		out.println("    <td>");
		out.println("      <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"margin: 0px\" role='presentation'>");
		out.println("");
		out.println("        <tr valign=\"top\" align=\"left\">");
		out.println("        <td align=\"left\" class=\"textbody\">");
		out.println("                     <input type=\"radio\" name=\"valueset_search_algorithm\" value=\"contains\" alt=\"Contains\" checked tabindex=\"3\"  onclick=\"onVSAlgorithmChanged();\">Contains");
		out.println("                     <input type=\"radio\" name=\"valueset_search_algorithm\" value=\"exactMatch\" alt=\"Exact Match\"  tabindex=\"3\">Exact Match&nbsp;");
		out.println("                     <input type=\"radio\" name=\"valueset_search_algorithm\" value=\"startsWith\" alt=\"Begins With\"  tabindex=\"3\"  onclick=\"onVSAlgorithmChanged();\">Begins With&nbsp;");
		out.println("        </td>");
		out.println("        </tr>");
		out.println("");
		out.println("        <tr align=\"left\">");
		out.println("            <td height=\"1px\" bgcolor=\"#2F2F5F\" align=\"left\"></td>");
		out.println("        </tr>");
		out.println("        <tr valign=\"top\" align=\"left\">");
		out.println("          <td align=\"left\" class=\"textbody\">");
		out.println("                <input type=\"radio\" id=\"selectValueSetSearchOption\" name=\"selectValueSetSearchOption\" value=\"Name\" checked alt=\"Name\" checked tabindex=\"4\"  >Name&nbsp;");
		out.println("                <input type=\"radio\" id=\"selectValueSetSearchOption\" name=\"selectValueSetSearchOption\" value=\"Code\"  alt=\"Code\" tabindex=\"4\" onclick=\"onVSCodeButtonPressed();\">Code&nbsp;");
		out.println("          </td>");
		out.println("        </tr>");
		out.println("      </table>");
		out.println("    </td>");
		out.println("  </tr>");
		out.println("</table>");
		out.println("                <input type=\"hidden\" id=\"nav_type\" name=\"nav_type\" value=\"valuesets\" />");
		out.println("                <input type=\"hidden\" id=\"view\" name=\"view\" value=\"source\" />");
		out.println("          </div> <!-- searchbox -->");
		out.println("");
		out.println("          <div class=\"searchbox-bottom\"><img src=\"ncitbrowser/images/searchbox-bottom.gif\" width=\"352\" height=\"2\" alt=\"SearchBox Bottom\" /></div>");
		out.println("          <!-- end Search box -->");
		out.println("          <!-- Global Navigation -->");
		out.println("");
		out.println("<table class=\"global-nav\" border=\"0\" width=\"100%\" height=\"37px\" cellpadding=\"0\" cellspacing=\"0\" role='presentation'>");
		out.println("  <tr>");
		out.println("    <td align=\"left\" valign=\"bottom\">");
		out.println("      <a href=\"#\" onclick=\"javascript:window.open('ncitbrowser/pages/source_help_info-termbrowser.jsf',");
		out.println("        '_blank','top=100, left=100, height=740, width=780, status=no, menubar=no, resizable=yes, scrollbars=yes, toolbar=no, location=no, directories=no');\" tabindex=\"13\">");
		out.println("        Sources</a>");
		out.println("");
		out.println("");
		out.println("");
		out.println("");
		out.println("");
		out.println("");
		out.println("    </td>");
		out.println("    <td align=\"right\" valign=\"bottom\">");
		out.println("      <a href=\"");
		out.println("ncitbrowser/pages/help.jsf\" tabindex=\"16\">Help</a>");
		out.println("");
		out.println("    </td>");
		out.println("");
		out.println("    <td width=\"7\"></td>");
		out.println("");
		out.println("  </tr>");
		out.println("");
		out.println("</table>");
		out.println("          <!-- end Global Navigation -->");
		out.println("");
		out.println("        </div> <!-- search-globalnav -->");
		out.println("      </div> <!-- bannerarea -->");
		out.println("");
		out.println("      <!-- end Thesaurus, banner search area -->");
		out.println("      <!-- Quick links bar -->");
		out.println("");
		out.println("<div class=\"bluebar\">");
		out.println("  <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" role='presentation'>");
		out.println("  <tr>");
		out.println("    <td><div class=\"quicklink-status\">&nbsp;</div></td>");
		out.println("    <td>");
		out.println("");
		out.println("  <div id=\"quicklinksholder\">");
		out.println("      <ul id=\"quicklinks\"");
		out.println("        onmouseover=\"document.quicklinksimg.src='ncitbrowser/images/quicklinks-active.gif';\"");
		out.println("        onmouseout=\"document.quicklinksimg.src='ncitbrowser/images/quicklinks-inactive.gif';\">");
		out.println("        <li>");
		out.println("          <a href=\"#\" tabindex=\"-1\"><img src=\"ncitbrowser/images/quicklinks-inactive.gif\" width=\"162\"");
		out.println("            height=\"18\" border=\"0\" name=\"quicklinksimg\" alt=\"Quick Links\" />");
		out.println("          </a>");
		out.println("          <ul>");
		out.println("            <li><a href=\"https://evs.nci.nih.gov/\" tabindex=\"-1\" target=\"_blank\"");
		out.println("              alt=\"Enterprise Vocabulary Services\">EVS Home</a></li>");
		out.println("            <li><a href=\"https://ncim.nci.nih.gov/ncimbrowser\" tabindex=\"-1\" target=\"_blank\"");
		out.println("              alt=\"NCI Metathesaurus\">NCI Metathesaurus Browser</a></li>");
		out.println("");
		out.println("");
		out.println("");
		out.println("            <li><a href=\"ncitbrowser/index.jsp\" tabindex=\"-1\"");
		out.println("              alt=\"NCI Thesaurus Browser\">NCI Thesaurus Browser</a></li>");
		out.println("");
		out.println("");
		out.println("");
		out.println("            <li>");
		out.println("              <a href=\"ncitbrowser/termbrowser.jsf\" tabindex=\"-1\" alt=\"NCI Term Browser\">NCI Term Browser</a>");
		out.println("            </li>");
		out.println("");
		out.println("            <li><a href=\"https://www.cancer.gov/cancertopics/terminologyresources\" tabindex=\"-1\" target=\"_blank\"");
		out.println("              alt=\"NCI Terminology Resources\">NCI Terminology Resources</a></li>");
		out.println("");
		out.println("");
		out.println("          </ul>");
		out.println("        </li>");
		out.println("      </ul>");
		out.println("  </div>");
		out.println("");
		out.println("      </td>");
		out.println("    </tr>");
		out.println("  </table>");
		out.println("");
		out.println("</div>");
		out.println("      <!-- end Quick links bar -->");
		out.println("");
		out.println("      <!-- Page content -->");
		out.println("      <div class=\"pagecontent\">");
		out.println("");
		out.println("      <a href=\"");
		out.println("/ncitbrowser");
		out.println("        <div id=\"popupContentArea\">");
		out.println("          <a name=\"evs-content\" id=\"evs-content\"></a>");
		out.println("");
		out.println("      <table class=\"datatableValueSet_960\" summary=\"\" cellpadding=\"3\" cellspacing=\"0\" border=\"0\" width=\"100%\">");
		out.println("            <tr class=\"textbody\">");
		out.println("                      <td>");
		out.println("                         <div class=\"texttitle-blue\">Welcome</div>");
		out.println("                      </td>");
		out.println("");
		out.println("                      <td>");
		out.println("&nbsp;");
		out.println("                      </td>");
		out.println("");
		out.println("                      <td>");
		out.println("<table role='presentation'>");
		out.println("<tr><td>");
		out.println("<a href=\"ncitbrowser/ajax?action=values&vsd_uri=http://evs.nci.nih.gov/valueset/FDA/C54453\"><img src=\"ncitbrowser/images/values.gif\" alt=\"Values\" border=\"0\" tabindex=\"2\"></a>");
		out.println("&nbsp;");
		out.println("<a href=\"ncitbrowser/ajax?action=versions&vsd_uri=http://evs.nci.nih.gov/valueset/FDA/C54453\"><img src=\"ncitbrowser/images/versions.gif\" alt=\"Versions\" border=\"0\" tabindex=\"2\"></a>");
		out.println("&nbsp;");
		out.println("<a href=\"ncitbrowser/ajax?action=xmldefinitions&vsd_uri=http://evs.nci.nih.gov/valueset/FDA/C54453\"><img src=\"ncitbrowser/images/xmldefinitions.gif\" alt=\"XML Definition\" border=\"0\" tabindex=\"2\"></a>");
		out.println("</td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("                      </td>");
		out.println("            </tr>");
		out.println("                    <tr><td colspan=\"2\" align=\"left\"><b>");
		out.println(valueSetName);
		out.println("                    </b></td></tr>");
		out.println("                    <tr><td colspan=\"2\" align=\"left\">");
		//String valueSetDescription = "&lt;Populate value set description here.&gt;";
		out.println("<p>" + valueSetDescription + "</p>");
		//Terminology used for representation of the the framework of the Structured Product Labeling documents. </p><p>SPL Terminology can be downloaded from this location <a href=\"https://evs.nci.nih.gov/ftp1/FDA/SPL\">SPL</a>.
		//</p>");
		out.println("                    </td></tr>");
		out.println("          </table>");
		out.println("");
		out.println("          <hr/>");
		out.println("");
		out.println("");
		out.println("");
		out.println("<style>");
		out.println("#expandcontractdiv {border:1px solid #336600; background-color:#FFFFCC; margin:0 0 .5em 0; padding:0.2em;}");
		out.println("#treecontainer { background: #fff }");
		out.println("</style>");
		out.println("");
		out.println("");
		out.println("<font face=\"verdana\" size=\"1\">");
		out.println("<div id=\"expandcontractdiv\">");
		out.println("<a href=\"#\" onclick=\"expand_all();\" tabindex=\"1\" >Expand all</a>");
		out.println("&nbsp;");
		out.println("Expand&nbsp;");
		out.println("<select id=\"level\" onchange=\"expand_tree();\">");
		out.println("    <option value=\"0\" selected>0</option>");
		out.println("    <option value=\"1\">1</option>");
		out.println("    <option value=\"2\">2</option>");
		out.println("    <option value=\"3\">3</option>");
		out.println("    <option value=\"4\">4</option>");
		out.println("    <option value=\"5\">5</option>");
		out.println("</select>");
		out.println("&nbsp;");
		out.println("Levels");
		out.println("&nbsp;");
		out.println("<a href=\"#\" onclick=\"collapse_all();\" tabindex=\"2\">Collapse all</a>");
		out.println("&nbsp;");
		out.println("<a href=\"#\" onclick=\"select_all();\" tabindex=\"1\">Check all</a>");
		out.println("&nbsp;");
		out.println("<a href=\"#\" onclick=\"select_none();\" tabindex=\"2\">Uncheck all</a>");
		out.println("</div>");
		out.println("<font>");
		out.println("");
		out.println("");
		out.println("");
		out.println("          <!-- Tree content -->");
		out.println("");
		out.println("<div>");
		out.println("	    <font face=\"verdana\" size=1.7px>");
		out.println("	    <form>");
		out.println("<ul>");
		out.println("<li>");
		out.println("<input type=\"checkbox\" aria-labelledby=\"N_1\" id=\"N_1\" name=\"http://evs.nci.nih.gov/valueset/FDA/C54453\"  onclick=\"updateCheckbox('N_1'); return false; \" tabindex=\"3\" checked>SPL Color Terminology");
		out.println("</li>");
		out.println("</ul>");
		out.println("	    </form>");
		out.println("	    </font>");
		out.println("");
		out.println("          </div> <!-- popupContentArea -->");
		out.println("");
		out.println("            <div class=\"pagecontent\">");
		out.println("               <a name=\"evs-content\" id=\"evs-content\"></a>");
		out.println("                  <div class=\"tabTableContentContainer\">");
		out.println("");
		out.println("                  <div class=\"table-scroll\">");
		out.println("                     <table border=\"0\" role='presentation'>");
		out.println("                        <tr>");
		out.println("                           <td>");
		out.println("                              <table border=\"0\" width=\"870\" role='presentation'>");
		out.println("                                 <tr>");
		out.println("                                    <td align=\"right\">");
		out.println("<a href=\"ncitbrowser/ajax?action=export_to_excel&from_download=true&vsd_uri=http://evs.nci.nih.gov/valueset/FDA/C54453\"><font face=\"verdana\" size=\"1\">Export Excel</font></a>");
		out.println("<a title=\"Download Plugin Microsoft Excel Viewer\" href=\"https://products.office.com/en-US/excel?legRedir=true&CorrelationId=1229dc2e-5ff3-4e3b-adc8-2b6f59e21be4\" target=\"_blank\"><img");
		out.println("     src=\"ncitbrowser/images/link_xls.gif\" width=\"16\"");
		out.println("     height=\"16\" border=\"0\"");
		out.println("alt=\"Download Plugin Microsoft Excel Viewer\" /></a>");
		out.println("<a href=\"ncitbrowser/ajax?action=export_to_csv&from_download=true&vsd_uri=http://evs.nci.nih.gov/valueset/FDA/C54453\"><font face=\"verdana\" size=\"1\">Export CSV</font></a>");
		out.println("                                    </td>");
		out.println("                                 </tr>");
		out.println("                              </table>");
		out.println("                           </td>");
		out.println("                        </tr>");
		out.println("                     </table>");
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
		out.println("</div></div>");
		out.println("");
		out.println("<div class=\"textbody\">");
		out.println("<!-- footer -->");
		out.println("<div class=\"footer\" style=\"width:920px\">");
		out.println("  <ul>");
		out.println("    <li><a href=\"https://www.cancer.gov\" target=\"_blank\" alt=\"National Cancer Institute\">NCI Home</a> |</li>");
		out.println("    <li><a href=\"ncitbrowser/pages/contact_us.jsf\">Contact Us</a> |</li>");
		out.println("    <li><a href=\"https://www.cancer.gov/policies\" target=\"_blank\" alt=\"National Cancer Institute Policies\">Policies</a> |</li>");
		out.println("    <li><a href=\"https://www.cancer.gov/policies/page3\" target=\"_blank\" alt=\"National Cancer Institute Accessibility\">Accessibility</a> |</li>");
		out.println("    <li><a href=\"https://www.cancer.gov/policies/page6\" target=\"_blank\" alt=\"National Cancer Institute FOIA\">FOIA</a></li>");
		out.println("  </ul>");
		out.println("<center>");
		out.println("<a href=\"https://www.hhs.gov/\" alt=\"U.S. Department of Health and Human Services\">");
		out.println("U.S. Department of Health and Human Services");
		out.println("</a>");
		out.println("&nbsp;|&nbsp;");
		out.println("<a href=\"https://www.nih.gov/about-nih\" alt=\"National Institutes of Health\">");
		out.println("National Institutes of Health");
		out.println("</a>");
		out.println("&nbsp;|&nbsp;");
		out.println("<a href=\"https://www.cancer.gov/\" alt=\"National Cancer Institute\">");
		out.println("National Cancer Institute");
		out.println("</a>");
		out.println("&nbsp;|&nbsp;");
		out.println("<a href=\"https://www.usa.gov/\" alt=\"USA.gov\">");
		out.println("USA.gov");
		out.println("</a>");
		out.println("</center>");
		out.println("</div>");
		out.println("<!-- end footer -->");
		out.println("</div>");
		out.println("");
		out.println("");
		out.println("      </div> <!-- pagecontent -->");
		out.println("    </div> <!--  main-area -->");
		out.println("    <div class=\"mainbox-bottom\"><img src=\"ncitbrowser/images/mainbox-bottom.gif\" width=\"945\" height=\"5\" alt=\"Mainbox Bottom\" /></div>");
		out.println("");
		out.println("  </div> <!-- center-page -->");
		out.println("");
		out.println("   <form id=\"hidden_form\" enctype=\"application/x-www-form-urlencoded;charset=UTF-8\">");
		out.println("      <input type=\"hidden\" id=\"checkedNodes\" name=\"checkedNodes\" value=\"\" />");
		out.println("      <input type=\"hidden\" id=\"partialCheckedNodes\" name=\"partialCheckedNodes\" value=\"null\" />");
		out.println("   </form>");
		out.println("");
		out.println("        <script type=\"text/javascript\">");
		out.println("            fxheaderInit('rvs_table',300,1,0);");
		out.println("            fxheader();");
		out.println("        </script>");
		out.println("</body>");
		out.println("</html>");
		out.println("");
		out.println("<script id=\"f5_cspm\">(function(){var f5_cspm={f5_p:'HKBPMAPPLKJGJMDKKCFGGPPBPMCDKJOMMNBNILHHLHACAFKCNLHKAJJHJOJODDGLJMCBMPIHBEONKAHAALBAKBMCAKPECOBAEPJANKJLHHNHNHFDODKKFNMDDLCGOJNK',setCharAt:function(str,index,chr){if(index>str.length-1)return str;return str.substr(0,index)+chr+str.substr(index+1);},get_byte:function(str,i){var s=(i/16)|0;i=(i&15);s=s*32;return((str.charCodeAt(i+16+s)-65)<<4)|(str.charCodeAt(i+s)-65);},set_byte:function(str,i,b){var s=(i/16)|0;i=(i&15);s=s*32;str=f5_cspm.setCharAt(str,(i+16+s),String.fromCharCode((b>>4)+65));str=f5_cspm.setCharAt(str,(i+s),String.fromCharCode((b&15)+65));return str;},set_latency:function(str,latency){latency=latency&0xffff;str=f5_cspm.set_byte(str,40,(latency>>8));str=f5_cspm.set_byte(str,41,(latency&0xff));str=f5_cspm.set_byte(str,35,2);return str;},wait_perf_data:function(){try{var wp=window.performance.timing;if(wp.loadEventEnd>0){var res=wp.loadEventEnd-wp.navigationStart;if(res<60001){var cookie_val=f5_cspm.set_latency(f5_cspm.f5_p,res);window.document");
		out.println(".cookie='f5avr1069532048aaaaaaaaaaaaaaaa_cspm_='+encodeURIComponent(cookie_val)+';path=/;'+'';}");
		out.println("return;}}");
		out.println("catch(err){return;}");
		out.println("setTimeout(f5_cspm.wait_perf_data,100);return;},go:function(){var chunk=window.document.cookie.split(/\s*;\s*/);for(var i=0;i<chunk.length;++i){var pair=chunk[i].split(/\s*=\s*/);if(pair[0]=='f5_cspm'&&pair[1]=='1234')");
		out.println("{var d=new Date();d.setTime(d.getTime()-1000);window.document.cookie='f5_cspm=;expires='+d.toUTCString()+';path=/;'+';';setTimeout(f5_cspm.wait_perf_data,100);}}}}");
		out.println("f5_cspm.go();}());</script>");
    }

    public static void main(String[] args) {
		String valueSetName = args[0];
		String valueSetDescription = args[1];
        ValueSetHomeGenerator valueSetHomeGenerator = new ValueSetHomeGenerator(valueSetName, valueSetDescription);
		PrintWriter pw = null;
		String outputfile = valueSetName.replace(" ", "_") + ".html";
		try {
			pw = new PrintWriter(outputfile, "UTF-8");
            valueSetHomeGenerator.run(pw);
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

//SPL Color Terminology|http://evs.nci.nih.gov/valueset/FDA/C54453|https://evs.nci.nih.gov/ftp1/FDA/SPL/FDA-SPL_NCIt_Subsets.xls|1:2:C54453
