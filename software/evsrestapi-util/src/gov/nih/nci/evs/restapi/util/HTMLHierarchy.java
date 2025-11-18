package gov.nih.nci.evs.restapi.util;
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
public class HTMLHierarchy {
	HashMap code2NodeMap = new HashMap();
    HierarchyHelper hh = null;

    public String title = "NCI Thesaurus";

    String datafile = null;

    public HTMLHierarchy(String datafile) {
        this.datafile = datafile;
        initialize();
	}

    public HTMLHierarchy(Vector parent_child_vec) {
        this.datafile = null;
        hh = new HierarchyHelper(parent_child_vec);
	}

    public void initialize() {
		Vector parent_child_vec = Utils.readFile(datafile);
		System.out.println("parent_child_vec: " + parent_child_vec.size());
		hh = new HierarchyHelper(parent_child_vec);
	}

	public Vector sortByLabel(Vector codes) {
		Vector w = new Vector();
		for (int i=0; i<codes.size(); i++) {
			String code = (String) codes.elementAt(i);
			String label = hh.getLabel(code);
			if (label != null && label.length() > 0) {
				w.add(label + "|" + code);
			}
		}
		w = new SortUtils().quickSort(w);
		Vector v = new Vector();
		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			Vector u = StringUtils.parseData(line);
			v.add((String) u.elementAt(1));
		}
		return v;
	}


    public void setTitle(String title) {
		this.title = title;
	}

    public void writeData(PrintWriter out) {
		out.println("	<h1>" + title + "</h1>");
		out.println("	<div id=\"html\" class=\"demo\">");


		out.println("		<ul>");
		out.println("			<li data-jstree='{ \"opened\" : true }'>NCI Thesaurus");
		out.println("				<ul>");
		out.println("					<li data-jstree='{ \"selected\" : true }'>Child node 1</li>");
		out.println("					<li>Child node 2</li>");
		out.println("				</ul>");
		out.println("			</li>");
		out.println("		</ul>");


		out.println("	</div>");
	}


	public void writeNode(PrintWriter out, String code) {
		String label = (String) hh.getLabel(code);
		String link_code = HyperlinkHelper.toHyperlink(code);
		String display = label + " (" + link_code + ")";
		out.println("		<ul>");
		out.println("			<li data-jstree='{ \"opened\" : false }'>" + display);

		Vector subs = hh.getSubclassCodes(code);
		if (subs != null) {
			for (int j=0; j<subs.size(); j++) {
				String sub = (String) subs.elementAt(j);
				writeNode(out, sub);
			}
		}
		out.println("			</li>");
		out.println("		</ul>");
	}

	public void write_data(PrintWriter out, String title, String root) {
		Stack stack = new Stack();
		if (root != null) {
			stack.push(root);
		} else {
			Vector codes = hh.getRoots();
			codes = sortByLabel(codes);
			for (int i=0; i<codes.size(); i++) {
				int k = codes.size()-i-1;
				String code = (String) codes.elementAt(k);
				stack.push(code);
			}
	    }
		while (!stack.isEmpty()) {
            String code = (String) stack.pop();
            writeNode(out, code);
		}
	}

    public void run(PrintWriter out, String title, String root) {
		out.println("<!DOCTYPE html>");
		out.println("<html lang=\"en\">");
		out.println("<head>");
		out.println("	<meta charset=\"UTF-8\">");
		out.println("	<title>" + title + "</title>");
		out.println("	<style>");
		out.println("	html { margin:0; padding:0; font-size:62.5%; }");
		out.println("	body { max-width:800px; min-width:300px; margin:0 auto; padding:20px 10px; font-size:14px; font-size:1.4em; }");
		out.println("	h1 { font-size:1.8em; }");
		out.println("	.demo { overflow:auto; border:1px solid silver; min-height:100px; }");
		out.println("	</style>");
		out.println("	<link rel=\"stylesheet\" href=\"style.min.css\" />");
		out.println("");
		out.println("</head>");
		out.println("<body>");

		write_data(out, title, root);

		out.println("");
		out.println("");
		out.println("	<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js\"></script>");
		out.println("	<script src=\"jstree.min.js\"></script>");
		out.println("");
		out.println("	<script>");
		out.println("	// html demo");
		out.println("	$('#html').jstree();");
		out.println("");
		out.println("	</script>");
		out.println("</body>");
		out.println("</html>");
    }

    public static String run(String datafile, String title, String root) {
        long ms = System.currentTimeMillis();
		PrintWriter pw = null;
		int n = datafile.lastIndexOf(".");
		String outputfile = datafile.substring(0, n) + ".html";
		try {
			pw = new PrintWriter(outputfile, "UTF-8");
            HTMLHierarchy htmlHierarchy = new HTMLHierarchy(datafile);
            htmlHierarchy.run(pw, title, root);

		} catch (Exception ex) {

		} finally {
			try {
				pw.close();
				System.out.println("Output file " + outputfile + " generated.");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
		return outputfile;
	}

    public static String run(Vector parent_child_vec, String title, String root) {
        long ms = System.currentTimeMillis();
		PrintWriter pw = null;
		String outputfile = title.toLowerCase();
		outputfile = outputfile.replace(" ", "_");
		outputfile = outputfile + ".html";

		try {
			pw = new PrintWriter(outputfile, "UTF-8");
            HTMLHierarchy htmlHierarchy = new HTMLHierarchy(parent_child_vec);
            htmlHierarchy.run(pw, title, root);

		} catch (Exception ex) {

		} finally {
			try {
				pw.close();
				System.out.println("Output file " + outputfile + " generated.");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
		return outputfile;
	}


    public static HashMap createSourcePTMap(String axiomfile, String source) {
		HashMap hmap = new HashMap();
		Vector v = Utils.readFile(axiomfile);
		int lcv = 0;
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			if (u.contains("P383$PT") && u.contains("P384$" + source) ) {
				lcv++;
				if (lcv < 100) {
					//System.out.println((String) u.elementAt(1) + " --> " + (String) u.elementAt(3));
				}
				hmap.put((String) u.elementAt(1), (String) u.elementAt(3));
			}
		}
		System.out.println(hmap.keySet().size());
		return hmap;
	}

	public static void generateHierarchyFiles(Vector parent_child_vec, String axiomfile, String root) {
		HierarchyHelper hh = new HierarchyHelper(parent_child_vec);
		Vector lines = hh.get_transitive_closure_v4(root);
		Utils.saveToFile(root + "_codes.txt", lines);

		Vector w = new Vector();
		if (axiomfile == null) {
			for (int i=0; i<lines.size(); i++) {
				String line = (String) lines.elementAt(i);
				w.add(line);
			}
		} else {
			HashMap NCIPTMap = createSourcePTMap(axiomfile, "NCI");
			for (int i=0; i<lines.size(); i++) {
				String line = (String) lines.elementAt(i);
				Vector u = StringUtils.parseData(line, '|');
				String s0 = (String) u.elementAt(0);
				String s1 = (String) u.elementAt(1);
				String s2 = (String) u.elementAt(2);
				String s3 = (String) u.elementAt(3);
				String t0 = (String) NCIPTMap.get(s1);
				String t2 = (String) NCIPTMap.get(s3);
				w.add(t0 + "|" + s1 + "|" + t2 + "|" + s3);
			}
		}
		w = new SortUtils().quickSort(w);
        Utils.saveToFile(root + ".txt", w);
        String title = root + " Hierarchy";
        run(w, title, root);
        hh = new HierarchyHelper(w);
        hh.printTree(root);
	}


    public static void main(String[] args) {
		String datafile = args[0];
		String title = args[1];
		String root = args[2];
		HTMLHierarchy.run(datafile, title, root);
	}
}
