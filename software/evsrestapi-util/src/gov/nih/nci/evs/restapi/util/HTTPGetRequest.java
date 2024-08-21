package gov.nih.nci.evs.restapi.util;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
public class HTTPGetRequest {

	private static final String USER_AGENT = "Mozilla/5.0";
	private static String ALPHABETIC = "abcdefghijklmnopqrstuvwxyz";

    private String GET_URL = "https://druginfo.nlm.nih.gov/drugportal/drug/names/";

    private int number_of_columns = 0;
    private Vector data_vec = new Vector();

    public HTTPGetRequest() {

	}

	public void setGET_URL(String GET_URL) {
		this.GET_URL = GET_URL;
	}

	public Vector extractRowData(Vector w) {
		Vector v = new Vector();
		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			line = line.trim();
			if (line.startsWith("<tr><td align=\"left\"><a href=")) {
				line = line.replace("/drugportal", "https://druginfo.nlm.nih.gov/drugportal");
				v.add(line);
			}
		}
		return v;
	}

	public Vector sendGET(String url_str) throws IOException {
		Vector w = new Vector();
		URL obj = new URL(url_str);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", USER_AGENT);
		int responseCode = con.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_OK) { // success
			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				w.add(inputLine);
			}
			in.close();
		} else {
			System.out.println("GET request failed.");
		}
		return w;
	}


	public Vector sendGET(char c) throws IOException {
		Vector w = new Vector();
		URL obj = new URL(GET_URL + c);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", USER_AGENT);
		int responseCode = con.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_OK) { // success
			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				w.add(inputLine);
			}
			in.close();
		} else {
			System.out.println("GET request failed.");
		}
		return w;
	}

	public String getAlphabeticListing() {
		StringBuffer buf = new StringBuffer();
	    for (int i=0; i<ALPHABETIC.length(); i++) {
			char ch = ALPHABETIC.charAt(i);
			String t = "" + ch;
			String t_upper = t.toUpperCase();
			//&nbsp;
			//https://druginfo.nlm.nih.gov/drugportal/drug/names
			buf.append("<a href=\"https://druginfo.nlm.nih.gov/drugportal/drug/names/" + t + "\">" + t_upper + "</a> &nbsp;&nbsp;");
		}
		return buf.toString();
	}

    public void generate() {
        Vector w = new Vector();
        w.add("<html>");
        w.add("<head>");
        w.add("</head>");
        w.add("<body style=\"background-color:LightGray;\">");
        w.add("<h2>");
        w.add("<center>FDA Approved Drugs</center>");
        w.add("<h4>");
        w.add("<center>(Last Updated on " + StringUtils.getToday() + ")</center>");
        w.add("<center>");
        w.add("<p></p>");
        w.add(getAlphabeticListing());
        w.add("<p></p>");
        w.add("<table border=\"0\">");
 	    w.add("<tr><th scope=\"col\">Drug Names</th></tr>");
	    for (int i=0; i<ALPHABETIC.length(); i++) {
			char ch = ALPHABETIC.charAt(i);
			try {
				Vector v = sendGET(ch);
				v = extractRowData(v);
				w.addAll(v);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		w.add("</table>");
		w.add("</center>");
		w.add("</body>");
        w.add("</html>");

        Utils.saveToFile("FDAApprovedDrugs.html", w);
	}

	public void extractStemTable(Vector v) {
        Vector w = new Vector();
        w.add("<html>");
        w.add("<head>");
        w.add("</head>");
        w.add("<body style=\"background-color:LightGray;\">");
        w.add("<h2>");
        w.add("<center>FDA Drug Name Stems</center>");
        w.add("<h4>");
        w.add("<center>(Last Updated on " + StringUtils.getToday() + ")</center>");
        w.add("<center>");
        w.add("<p></p>");
        w.add(getAlphabeticListing());
        w.add("<p></p>");

        boolean start = false;
	    for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			line = line.replace("/drugportal", "https://druginfo.nlm.nih.gov/drugportal");
			if (line.indexOf("<table class=\"stemTable\">") != -1) {
				start = true;
			}
			if (start) {
				w.add(line);
			}
			if (line.indexOf("</table>") != -1) {
				break;
			}
		}
		w.add("</center>");
		w.add("</body>");
        w.add("</html>");
        Utils.saveToFile("DrugNameStem.html", w);
	}


	public static void main1(String[] args) throws IOException {
		long ms = System.currentTimeMillis();
		HTTPGetRequest httpGetRequest = new HTTPGetRequest();
        httpGetRequest.generate();
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}

	public static void main(String[] args) throws IOException {
		long ms = System.currentTimeMillis();
		HTTPGetRequest httpGetRequest = new HTTPGetRequest();
		String url_str = "https://druginfo.nlm.nih.gov/drugportal/jsp/drugportal/DrugNameGenericStems.jsp";
        httpGetRequest.extractStemTable(httpGetRequest.sendGET(url_str));
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}
}


