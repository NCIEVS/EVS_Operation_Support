package gov.nih.nci.evs.restapi.util;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
public class FTPSiteInventory {
	static String EVS_FTP_URL = "https://evs.nci.nih.gov/ftp1/";
    static String currentWorkingDirectory;
    static {
		currentWorkingDirectory = System.getProperty("user.dir");
	}
	public static String RAW_FTP_DATA = "FTP_TABLE_DATA.txt";
	public static String FTP_TABLE_DATA = "ftp.txt";
	public static String FTP_HIER_DATA = "ftp_hier_data.txt";

	public static void download(String url, String outputfile) {
		try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
		    FileOutputStream fileOutputStream = new FileOutputStream(outputfile)) {
			byte dataBuffer[] = new byte[1024];
			int bytesRead;
			while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
				fileOutputStream.write(dataBuffer, 0, bytesRead);
			}
		} catch (IOException e) {
			// handle exception
		}
	}

	public static Vector getURLPageContent(String url) {
		String htmlfile = "root.html";
		String url0 = url;

		System.out.println(url);
		System.out.println(EVS_FTP_URL);

		if (url.compareTo(EVS_FTP_URL) != 0) {
     		String line = url;
			url = url.replace(EVS_FTP_URL, "");
			url = url.replace("/", "");
			String href = url.trim();
			htmlfile = href + ".html";
		}
		System.out.println("****************** Downloading from " + url0);
		System.out.println("download url0: " + url0);
		download(url0, htmlfile);
		System.out.println("download url0: " + url0 + " done.");
		System.out.println("parseHTML: " + htmlfile);
        Vector w = parseHTML(htmlfile);
        FileUtils.deleteFile(htmlfile);
        return w;
	}


	public static Vector parseHTML(String htmlfile) {
		Vector v = Utils.readFile(htmlfile);
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (line.indexOf("<td class=") != -1) {
				Vector u = StringUtils.parseData(line, "<td");

				String t0 = (String) u.elementAt(2);
				int n = t0.lastIndexOf("</a>");
				String t1 = t0.substring(0, n);
				int m = t1.lastIndexOf(">");
				String s1 = t1.substring(m+1, n);

				t0 = (String) u.elementAt(3);
				n = t0.lastIndexOf("</td>");
				t1 = t0.substring(0, n);
				m = t1.lastIndexOf(">");
				String s2 = t1.substring(m+1, n);
				s2 = s2.trim();

				t0 = (String) u.elementAt(4);
				n = t0.lastIndexOf("</td>");
				t1 = t0.substring(0, n);
				m = t1.lastIndexOf(">");
				String s3 = t1.substring(m+1, n);
				s3 = s3.trim();

                if (s1.endsWith("//")) {
					s1 = EVS_FTP_URL + s1;
				}
				w.add(s1 + "|" + s2 + "|" + s3);
			}
		}
		return w;
	}

/*
    public static Vector run() {
		String url = EVS_FTP_URL;
		Vector w0 = getURLPageContent(url);
		Vector w = new Vector();
		w.add("URL|File Name|Last Modified Date|Size");
		Stack stack = new Stack();
		for (int i=0; i<w0.size(); i++) {
			String line = (String) w0.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String href = (String) u.elementAt(0);
			System.out.println(href);
			if (href.endsWith("/")) {
				stack.push(EVS_FTP_URL + href);
			} else {
				String href2 = href.replace(" ", "%20");
				line = line.replace("&nbsp;", "");
				w.add(EVS_FTP_URL + href2 + "|" + line);
			}
		}
		HashSet hset = new HashSet();
		while (!stack.isEmpty()) {
			url = (String) stack.pop();
			Vector v = getURLPageContent(url);
			for (int i=0; i<v.size(); i++) {
				String line = (String) v.elementAt(i);
				Vector u = StringUtils.parseData(line, '|');
				String href = (String) u.elementAt(0);
				if (href.endsWith("/")) {
					stack.push(url + href);
				} else {
					String href2 = href.replace(" ", "%20");
					line = line.replace("&nbsp;", "");
					w.add(url + href2 + "|" + line);
				}
			}
		}
        return w;
	}
*/
/*
    public static Vector run() {
		return null;
	}
*/
	public static void generate(String filename) {
        generate(filename, false);
	}

	public static void generate(String filename, boolean showURL) {
		int n = filename.lastIndexOf(".");
		String htmlfile = filename.substring(0, n) + ".html";
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(htmlfile);
			generate(pw, filename, showURL);
		} catch (Exception ex) {

		} finally {
			try {
				pw.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public static void generate(PrintWriter out, String filename) {
		generate(out, filename, false);
	}


	public static void generate(PrintWriter out, String filename, boolean showURL) {
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">");
		out.println("<html xmlns:c=\"http://java.sun.com/jsp/jstl/core\">");
		out.println("<head>");
		out.println("<title>test_insert_after</title>");
		out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
		out.println("<style>");
		out.println("table {");
		out.println("    border-collapse: collapse;");
		out.println("}");
		out.println("table, td, th {");
		out.println("    border: 1px solid black;");
		out.println("}");
		out.println("</style>");
		out.println("</head>");
		out.println("<body>");
		out.println("<div>");
		out.println("  <center>");
		out.println("  <img");
		out.println("      src=\"https://www.cancer.gov//sites/g/files/xnrzdm211/files/ncids_header/logos/Logo_NCI.svg\"");
		out.println("      alt=\"EVS Logo\"");
		out.println("      width=\"955\"");
		out.println("      height=\"39\"");
		out.println("      border=\"0\"");
		out.println("      alt=\"National Cancer Institute\"");
		out.println("      usemap=\"#external-evs\"");
		out.println("  />");
		out.println("  </center>");
		out.println("</div>");
		out.println("<center>");
		out.println("<h1>Files Published at the NCI FTP Site</h1>");
		out.println("<p>");
		String timestamp = StringUtils.getToday();
		out.println("<h2>(Last modified: " + timestamp + ")</h2>");
		out.println("</p>");
		out.println("</center>");
		out.println("");
		out.println("<div>");
		out.println("<center>");
		out.println("<h3>Published Files</h3>");
		out.println("<table>");
		out.println("<tr>");
		if (showURL) {
			out.println("<th>");
			out.println("URL");
			out.println("</th>");
		}
		out.println("<th>");
		out.println("Name");
		out.println("</th>");
		out.println("<th>");
		out.println("Last Modified Date");
		out.println("</th>");
		out.println("<th>");
		out.println("Size");
		out.println("</th>");
		Vector v = Utils.readFile(filename);
		for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '\t');
			String t0 = (String) u.elementAt(0);
			String t1 = (String) u.elementAt(1);
			String t2 = (String) u.elementAt(2);
			String t3 = (String) u.elementAt(3);

			String s1 = toHyperLink(t0 + File.separator + t1, t1);
			out.println("<tr>");

			if (showURL) {
				out.println("<td width=\"45%\">");
				out.println(t0);
				out.println("</td>");

				out.println("<td width=\"30%\">");
				out.println(s1);
				out.println("</td>");

				out.println("<td width=\"15%\">");
				out.println(t2);
				out.println("</td>");

			} else {

				out.println("<td width=\"60%\">");
				out.println(s1);
				out.println("</td>");

				out.println("<td width=\"30%\">");
				out.println(t2);
				out.println("</td>");
			}
			out.println("<td width=\"10%\">");
			out.println(t3);
			out.println("</td>");
			out.println("</tr>");
		}
		out.println("</table>");
		out.println("</div>");
		out.println("");
		out.println("<br></br>");
		out.println("<br></br>");
		out.println("<center><b>");
		out.println("(Source: https://evs.nci.nih.gov/ftp1/)");
		out.println("</b></center>");
		out.println("</body>");
		out.println("</html>");
	}

    public static String toHyperLink(String url, String value) {
		return "<a href=\"" + url + "\">" + value + "</a>";
	}

    public static void run(String outputfile) {
		//run(outputfile, false);
		run(outputfile, true);
	}

    public static void run(String outputfile, boolean showURL) {
		long ms = System.currentTimeMillis();
		/*
        File f = new File(outputfile);
		Vector w = run();
		Utils.dumpVector(EVS_FTP_URL, w);
		Utils.saveToFile(outputfile, w);
        generate(outputfile, showURL);
        */
        generate(outputfile, showURL);
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}

	public static Vector findFTPSubs(HashMap hmap, String url) {
		Vector w = new Vector();
	    Set set = hmap.keySet();
	    Iterator it = set.iterator();
	    while (it.hasNext()) {
			String t = (String) it.next();
			if (t.compareTo(url) != 0 && t.startsWith(url)) {
				int n = t.lastIndexOf("/");
				String s = t.substring(n+1, t.length());
				Vector u1 = StringUtils.parseData(s, "/");
				if (u1.size() == 1) {
					w.add(t);
				}
			}
		}
	    return w;
	}


	public static Vector generateFTPHierData(HashMap hmap) {
		String EVS_FTP = "https://evs.nci.nih.gov/ftp1";
		Vector w = new Vector();
		Stack stack = new Stack();
		stack.push(EVS_FTP);
		HashMap label2CodeMap = new HashMap();
		int count = 0;
		while (!stack.isEmpty()) {
			String url = (String) stack.pop();
			if (!label2CodeMap.containsKey(url)) {
				count++;
				String code = "URL_" + count;
				label2CodeMap.put(url, code);
			}
            String parentCode = (String) label2CodeMap.get(url);
			Vector subs = findFTPSubs(hmap, url);
			if (subs.size() > 0) {
				for (int i=0; i<subs.size(); i++) {
					String sub = (String) subs.elementAt(i);

					if (!label2CodeMap.containsKey(sub)) {
						count++;
						String code = "URL_" + count;
						label2CodeMap.put(sub, code);
					}
					String childCode = (String) label2CodeMap.get(sub);
					w.add(url + "|" + parentCode + "|" + sub + "|" + childCode);
					stack.push(sub);
				}
			}
		}
        return new SortUtils().quickSort(w);
	}

	public static Vector formatFPTData(HashMap hmap) {
		Vector w = new Vector();
		w.add("URL/tName/tLast modified/tSize/tDescription");
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String url = (String) it.next();
			Vector v = (Vector) hmap.get(url);
			for (int i=0; i<v.size(); i++) {
				String line = (String) v.elementAt(i);
				w.add(url + "\t" + line);
			}
		}
		return w;
	}


	public static HashMap loadFTPSiteData(String filename) {
		HashMap hmap = new HashMap();
		String url = null;
		Vector data_vec = null;
		Vector v = Utils.readFile(filename);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (line.startsWith("http")) {
				if (data_vec != null) {
					hmap.put(url, data_vec);
				}
				url = line;
				data_vec = new Vector();
			} else {
				Vector u = StringUtils.parseData(line, '\t');
				if (u.size() == 4) {
					data_vec.add(line);
				}
			}
		}
		if (data_vec != null) {
			hmap.put(url, data_vec);
		}
		return hmap;
	}

	public static String generateFTPHierData() {
		String filename = RAW_FTP_DATA;
		File f = new File(filename);
		if (!f.exists()) {
			System.out.println(filename + " does not exist.");
			return null;
		}
		HashMap hmap = loadFTPSiteData(filename);
		Vector v = formatFPTData(hmap);
		Utils.saveToFile(FTP_TABLE_DATA, v);
		Vector w = generateFTPHierData(hmap);
		Utils.saveToFile(FTP_HIER_DATA, w);
		return FTP_HIER_DATA;
	}

    public static void main0(String[] args) {
    	String hier_data = generateFTPHierData();
    	if (hier_data != null) {
			System.out.println(hier_data + " generated.");
		}
	}

    public static void main(String[] args) {
		String outputfile = FTP_TABLE_DATA;
		run(outputfile);
	}
}

