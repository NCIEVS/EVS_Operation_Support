package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.appl.*;
import gov.nih.nci.evs.restapi.bean.*;
import gov.nih.nci.evs.restapi.common.*;
import gov.nih.nci.evs.restapi.config.*;
import java.io.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.*;
import java.util.*;
import java.util.*;
import java.util.regex.*;
import org.json.*;

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
public class DynamicTreePlus {
    static String NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile; //"ThesaurusInferred_forTS.owl";
	static String PARENT_CHILD_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile; // "parent_child.txt";

    static HierarchyHelper hh = null;

    static {
		if (new File(PARENT_CHILD_FILE).exists()) {
			Vector parent_child_vec = Utils.readFile(PARENT_CHILD_FILE);
			hh = new HierarchyHelper(parent_child_vec);
		}
	}

    public static String toTooltip(String code, String value) {
		value = HTMLDecoder.decode(value);
		String t = "<div class=\"tooltip\">" + code + "<span class=\"tooltiptext\">" + value + "</span></div>";
		return t;//OWL2HTML.encode(t);
	}

	public static HashMap vector2MultiValuedHashMap(Vector v) {
		char delim = '\t';
		return vector2MultiValuedHashMap(v, delim);
	}

	public static HashMap vector2MultiValuedHashMap(Vector v, char delim) {
		HashMap hmap = new HashMap();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, delim);
			String key = (String) u.elementAt(0);
			String value = (String) u.elementAt(2);
			Vector w = new Vector();
			if (hmap.containsKey(key)) {
				w = (Vector) hmap.get(key);
			}
			if (!w.contains(value)) {
				w.add(value);
			}
			hmap.put(key, w);
		}
		return hmap;
	}

	public static Vector getTooltipScript() {
		Vector w = new Vector();
		w.add("<style>");
		w.add(".tooltip {");
		w.add("  position: relative;");
		w.add("  display: inline-block;");
		w.add("  border-bottom: 1px dotted black;");
		w.add("}");
		w.add("");
		w.add(".tooltip .tooltiptext {");
		w.add("  visibility: hidden;");
		w.add("  width: 120px;");
		w.add("  background-color: #555;");
		w.add("  color: #fff;");
		w.add("  text-align: left;");
		w.add("  border-radius: 6px;");
		w.add("  padding: 5px 0;");
		w.add("  position: absolute;");
		w.add("  z-index: 1;");
		w.add("  bottom: 125%;");
		w.add("  left: 50%;");
		w.add("  margin-left: -60px;");
		w.add("  opacity: 0;");
		w.add("  transition: opacity 0.3s;");
		w.add("}");
		w.add("");
		w.add(".tooltip .tooltiptext::after {");
		w.add("  content: \"\";");
		w.add("  position: absolute;");
		w.add("  top: 100%;");
		w.add("  left: 50%;");
		w.add("  margin-left: -5px;");
		w.add("  border-width: 5px;");
		w.add("  border-style: solid;");
		w.add("  border-color: #555 transparent transparent transparent;");
		w.add("}");
		w.add("");
		w.add(".tooltip:hover .tooltiptext {");
		w.add("  visibility: visible;");
		w.add("  opacity: 1;");
		w.add("}");
		w.add("</style>");
		return w;
	}

    public static String generateDynamicHTMLTree(String root, int maxLevel) {
		Vector replace_vec = new Vector();
		Vector by_vec = new Vector();
		replace_vec.add("/ncitbrowser/js/");
		by_vec.add("");
		replace_vec.add("/ncitbrowser/images/");
		by_vec.add("");
		PolyHierarchy.generateDynamicHTMLTree(root, maxLevel, replace_vec, by_vec);
		return root + "_tree.html";
	}

	public static void run(String htmltreefile, String req, String tooltiptext) {
		Vector missing_value_vec = new Vector();
		try {
			Vector dataVec = new Vector();
			dataVec.add(req);
			DataRetrieval retrieval = new DataRetrieval(NCIT_OWL, dataVec);
			Vector w = retrieval.getData(req);
			HashMap hmap = vector2MultiValuedHashMap(w);
			int m = hmap.keySet().size();
			Vector v = Utils.readFile(htmltreefile);
			w.add(htmltreefile + ": " + v.size());
			Vector w1 = new Vector();
			for (int i=0; i<v.size(); i++) {
				String line = (String) v.elementAt(i);
				if (line.indexOf("/head") != -1) {
					w1.addAll(getTooltipScript());
				}
				if (line.startsWith("<img src=")) {
					w.add(line);
					int n = line.lastIndexOf("(");
					String t = line.substring(n+1, line.length());
					n = t.lastIndexOf(")");
					String code = t.substring(0, n);
					Vector values = (Vector) hmap.get(code);
					if (values != null) {
						String value = (String) values.elementAt(0);
						w.add(value);
						String s = "&nbsp;" + toTooltip("(" + tooltiptext + ")", value);
						line = line + s;
					} else {
						w.add("WARNING: " + tooltiptext + " not found for " + code);
						missing_value_vec.add(hh.getLabel(code) + "|" + code);
					}
				}
				w1.add(line);
			}
			Utils.saveToFile("v2_" + htmltreefile, w1);
	    } catch (Exception ex) {
			ex.printStackTrace();
		}
		Utils.saveToFile("missing_values.txt", missing_value_vec);
	}

//Disease or Disorder ( Code - C2991 )
	public static void main(String[] args) {
		String root = "C2991";
		int maxLevel = 30;
		generateDynamicHTMLTree(root, maxLevel);
		//output is root_tree
/*

		String htmltreefile = args[0];
		String req = args[1];//"P97|P378$NCI";
        run(htmltreefile, req);
*/

	}
}