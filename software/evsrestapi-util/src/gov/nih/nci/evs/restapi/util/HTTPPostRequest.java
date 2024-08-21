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
public class HTTPPostRequest {

	private static final String USER_AGENT = "Mozilla/5.0";

    private String GET_URL = "https://www.accessdata.fda.gov/scripts/cder/ob/search_product.cfm";
    private String POST_URL = "https://www.accessdata.fda.gov/scripts/cder/ob/search_product.cfm";

    private String POST_PARAMS = "discontinued=RX,OTC,DISCN&drugname=";

    private int number_of_columns = 0;
    private Vector data_vec = new Vector();

    public HTTPPostRequest() {

	}

	public void setGET_URL(String GET_URL) {
		this.GET_URL = GET_URL;
	}

	public void setPOST_URL(String POST_URL) {
		this.POST_URL = POST_URL;
	}

	public void setPOST_PARAMS(String POST_PARAMS) {
		this.POST_PARAMS = POST_PARAMS;
	}

    private String extractTable(String line) {
		int n = line.indexOf("<table");
		line = line.substring(n, line.length());
		n = line.lastIndexOf("</table>");
		line = line.substring(0, n + "</table>".length());
		return line;
	}

    private Vector extractTableHeadings(String line) {
		Vector th_vec = new Vector();
		int n = line.indexOf("</th>");
		while (n != -1) {
			String t = line.substring(0, n);
			int m = t.lastIndexOf(">");
			String s = t.substring(m+1, n);
			if (s.length() > 0) {
				if (!th_vec.contains(s)) {
					th_vec.add(s);
				} else {
					break;
				}
			} else {
				th_vec.add(s);
			}

			line = line.substring(n+1, line.length());
			n = line.indexOf("</th>");
		}
		return th_vec;
	}

    private Vector extractTableData(String line) {
		Vector td_vec = new Vector();
		int n = line.indexOf("</td>");
		while (n != -1) {
			String t = line.substring(0, n);
			int m = t.lastIndexOf(">");
			String s = t.substring(m+1, n);
			td_vec.add(s);
			line = line.substring(n+1, line.length());
			n = line.indexOf("</td>");
		}
		return td_vec;
	}


	private void sendGET() throws IOException {
		URL obj = new URL(GET_URL);
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
				response.append(inputLine);
			}
			in.close();

			// print result
			System.out.println(response.toString());
		} else {
			System.out.println("GET request not worked");
		}
	}

	private String toHeadingLine(Vector v) {
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<v.size(); i++) {
			String t = (String) v.elementAt(i);
			buf.append(t);
			if (i < v.size()-1) {
				buf.append("\t");
			}
		}
		return buf.toString();
	}

	private Vector toDataLines(Vector v) {
		Vector dt_vec = new Vector();
		StringBuffer buf = new StringBuffer();
		int j = 0;
		for (int i=0; i<v.size(); i++) {
			j++;
			String t = (String) v.elementAt(i);
			buf.append(t);
			if (j < number_of_columns) {
				buf.append("\t");
			} else if (j == number_of_columns) {
				dt_vec.add(buf.toString());
				buf = new StringBuffer();
				j = 0;
			}
		}
		return dt_vec;
	}

	private Vector sendPOST(Vector drugnames) throws IOException {
		Vector w = new Vector();
		int j = 0;
		data_vec = new Vector();
        for (int i=0; i<drugnames.size(); i++) {
			try {
				String drugname = (String) drugnames.elementAt(i);
				j++;
				System.out.println("(" + j + ") " + drugname);
				URL obj = new URL(POST_URL);
				HttpURLConnection con = (HttpURLConnection) obj.openConnection();
				con.setRequestMethod("POST");
				con.setRequestProperty("User-Agent", USER_AGENT);

				con.setDoOutput(true);
				OutputStream os = con.getOutputStream();
				String params = POST_PARAMS + drugname;
				os.write(params.getBytes());
				os.flush();
				os.close();

				int responseCode = con.getResponseCode();
				if (responseCode == HttpURLConnection.HTTP_OK) {
					BufferedReader in = new BufferedReader(new InputStreamReader(
							con.getInputStream()));
					String inputLine;
					StringBuffer response = new StringBuffer();

					while ((inputLine = in.readLine()) != null) {
						response.append(inputLine);
					}
					in.close();
					String s = response.toString();
					s = s.trim();
					s = extractTable(s);
					w.add(drugname + "|" + s);
                    if (i == 0) {
						Vector th_vec = extractTableHeadings(s);
						number_of_columns = th_vec.size();
						data_vec.add(toHeadingLine(th_vec));
					}
                    Vector td_vec = extractTableData(s);
                    data_vec.addAll(toDataLines(td_vec));

				} else {
					System.out.println("WARNING: POST request failed " + drugname);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return data_vec;
	}

	public static void main(String[] args) throws IOException {
		long ms = System.currentTimeMillis();
		HTTPPostRequest httpPostRequest = new HTTPPostRequest();
		String filename = args[0];
		Vector drugnames = Utils.readFile(filename);
		Vector w = httpPostRequest.sendPOST(drugnames);
		String textfile = "FDA_Approved_Drug_Data.txt";
		Utils.saveToFile(textfile, w);

		char delimiter = '\t';
		try {
			String csvfile = Text2CSV.toCSV(textfile, delimiter);
			System.out.println(csvfile + " generated.");
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}
}
