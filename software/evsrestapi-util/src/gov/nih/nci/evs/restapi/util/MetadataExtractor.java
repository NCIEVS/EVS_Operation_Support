package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.util.*;
import java.io.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.*;
import org.apache.commons.codec.binary.Base64;

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
public class MetadataExtractor {

	public static String SOURCE_HELP_PAGE_URL = "https://nciterms65.nci.nih.gov/ncitbrowser/pages/source_help_info.jsf?dictionary=NCI_Thesaurus";
	public static String TERM_TYPE_HELP_PAGE_URL = "https://nciterms65.nci.nih.gov/ncitbrowser/pages/term_type_help_info.jsf?dictionary=NCI_Thesaurus";

	public static String TERM_BROWSER_HOME_URL = "https://nciterms65.nci.nih.gov";

	public MetadataExtractor() {

	}

	public static HashMap extractData(String url) {
		Vector w = DownloadPage.download(url);
		HashMap hmap = new HashMap();
		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			line = line.trim();
			if (line.length() > 0) {
				if (line.startsWith("<tr class=\"dataRow")) {
					String s = (String) w.elementAt(i+1);
					String t = (String) w.elementAt(i+2);
					int n = s.indexOf(">");
					s = s.substring(n+1, s.length());
					n = s.lastIndexOf("<");
					s = s.substring(0, n);

					n = t.lastIndexOf("<");
					t = t.substring(0, n);
					n = t.lastIndexOf(">");
					t = t.substring(n+1, t.length());

					//System.out.println(s + "-->" + t);
					if (!(s.compareTo("Name") == 0 && t.compareTo("Description") == 0)) {
						if (!(s.compareTo("Source") == 0 && t.compareTo("Description") == 0)) {
							s = s.replaceAll("&#45;", "-");
							hmap.put(s, t);
						}
					}
				}
			}
		}
		return hmap;
	}

	public static void dumpVector(String label, Vector v) {
		System.out.println(label);
		for (int i=0; i<v.size(); i++) {
			String t = (String) v.elementAt(i);
			System.out.println(t);
		}
	}

	public static void dumpHashMap(String label, HashMap hmap) {
		System.out.println(label);
		Vector v = new Vector();
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			v.add(key);
		}
		v = new SortUtils().quickSort(v);
		for (int i=0; i<v.size(); i++) {
			String key = (String) v.elementAt(i);
			String value = (String) hmap.get(key);
			System.out.println(key + " -> " + value);
		}
	}

    public static Vector download(String url, String filePath) {
		Vector w = new Vector();
		try {
			URL urlObj = new URL(url);
			URLConnection urlConnection = urlObj.openConnection();
			Charset charset = Charset.forName("UTF8");
			InputStreamReader stream = new InputStreamReader(urlConnection.getInputStream(), charset);
			BufferedReader reader = new BufferedReader(stream);
			StringBuffer responseBuffer = new StringBuffer();
			String line = null;
			while ((line = reader.readLine()) != null) {
				w.add(line);
			}
			reader.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return w;
	}

	public static void main(String[] args) {
		String url = SOURCE_HELP_PAGE_URL;
	    Vector v = null;
	    try {
			url = SOURCE_HELP_PAGE_URL;
			HashMap hmap = extractData(SOURCE_HELP_PAGE_URL);
			dumpHashMap("\nTerm Source", hmap);

			url = TERM_TYPE_HELP_PAGE_URL;
			hmap = extractData(TERM_TYPE_HELP_PAGE_URL);
			dumpHashMap("\nTerm Type", hmap);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
