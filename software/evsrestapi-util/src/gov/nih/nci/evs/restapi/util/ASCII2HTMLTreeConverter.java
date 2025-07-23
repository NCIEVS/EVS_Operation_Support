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
public class ASCII2HTMLTreeConverter {

	public static Vector getEmbeddedHierarchy(Vector parent_child_vec, String rootCode, HashSet nodeSet) {
		HierarchyHelper hh = new HierarchyHelper(parent_child_vec);
        ParserUtils parser = new ParserUtils();
		Vector w = new Vector();
		HashSet visitedNodes = new HashSet();
		FirstInFirstOutQueue queue = new FirstInFirstOutQueue();
		Vector topNodes = hh.getSubclassCodes(rootCode);
		for (int i=0; i<topNodes.size(); i++) {
			String topNode = (String) topNodes.elementAt(i);
			queue.add(rootCode + "|" + topNode);
		}
		while (!queue.isEmpty()) {
			String line = (String) queue.remove();
			Vector u = StringUtils.parseData(line, '|');
			String parentCode = parser.getValue((String) u.elementAt(0));
			String code = parser.getValue((String) u.elementAt(1));
			if (!visitedNodes.contains(parentCode)) {
				visitedNodes.add(parentCode);
			}
			if (!visitedNodes.contains(code)) {
				visitedNodes.add(code);
			}
			if (nodeSet.contains(parentCode) && nodeSet.contains(code)) {
				String parentLabel = hh.getLabel(parentCode);
				String childLabel = hh.getLabel(code);
				String record = parentLabel + "|" + parentCode
				              + "|" + childLabel + "|" + code;
				if (!w.contains(record)) {
					w.add(record);
				}
				Vector v = hh.getSubclassCodes(code);
				if (v != null) {
					for (int j=0; j<v.size(); j++) {
						String childCode = (String) v.elementAt(j);
						queue.add(code + "|" + childCode);
					}
			    }
			} else if (nodeSet.contains(parentCode) && !nodeSet.contains(code)) {
				Vector v = hh.getSubclassCodes(code);
				if (v != null) {
					for (int j=0; j<v.size(); j++) {
						String childCode = (String) v.elementAt(j);
						queue.add(parentCode + "|" + childCode);
					}
				}
			} else if (!nodeSet.contains(parentCode)) {
				Vector v = hh.getSubclassCodes(code);
				if (v != null) {
					for (int j=0; j<v.size(); j++) {
						String childCode = (String) v.elementAt(j);
						queue.add(code + "|" + childCode);
					}
				}
			}
		}
		w = new SortUtils().quickSort(w);
        return w;
	}

	public static void modifyHTMLFile(String htmlfile) {
		Vector v = Utils.readFile(htmlfile);
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			line = line.replace("/ncitbrowser/images", "images");
			line = line.replace("/ncitbrowser/js", "js");
			w.add(line);
		}
		Utils.saveToFile(htmlfile, w);
	}

    public static void generateHTMLTree(String asciitreefile, String title, String root) {
		Vector parent_child_vec = Utils.readFile(asciitreefile);
		parent_child_vec = new ASCIITreeUtils().get_parent_child_vec(parent_child_vec);
		String datafile = "flattened_" + asciitreefile;
		Utils.saveToFile(datafile, parent_child_vec);
		HTMLHierarchy.run(datafile, title, root);
    }

    public static void generateDynamicHTMLTree(String parent_child_file, String htmlfile) {
		Vector parent_child_vec = Utils.readFile(parent_child_file);
        TreeItem ti = new ASCIITreeUtils().createTreeItem(parent_child_vec);
        HashMap hmap = new HashMap();
        hmap.put("<Root>", ti);
        String url = "https://evsexplore.semantics.cancer.gov/evsexplore/concept/ncit/";
        //String url = "https://nciterms.nci.nih.gov/ncitbrowser/ConceptReport.jsp?dictionary=NCI%20Thesaurus&code=";
        new SimpleTreeUtils().writeTree2HTML(hmap, url, htmlfile);
    }

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static int getMaxIndentationNumber(String filename, String indent) {
		int max = -1;
		Vector v = Utils.readFile(filename);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			int n = getIndentationNumber(line, indent);
			if (n > max) {
				max = n;
			}
		}
		return max;
	}

	public static int getIndentationNumber(String line, String indent) {
		int n = 0;
		if (line == null) return -1;
		while (line.startsWith(indent)) {
			line = line.substring(indent.length(), line.length());
			n++;
		}
		return n;
	}

	public static String removeIndentation(String line, String indent) {
		int n = 0;
		if (line == null) return null;
		while (line.startsWith(indent)) {
			line = line.substring(indent.length(), line.length());
		}
		return line;
	}

	public static Vector flattenAsciiTree(String asciiTreeFile, String indent) {
		boolean removeLeadingCloseBracket = true;
		return flattenAsciiTree(asciiTreeFile, indent, removeLeadingCloseBracket);
	}

	public static Vector flattenAsciiTree(String asciiTreeFile, String indent, boolean removeLeadingCloseBracket) {
		int max = getMaxIndentationNumber(asciiTreeFile, indent);
		String[] values = new String[max+1];
		Vector v = Utils.readFile(asciiTreeFile);
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			int numIndent = getIndentationNumber(line, indent);
			String t = removeIndentation(line, indent);
			t = displayName2BarDelimited(t);
			if (removeLeadingCloseBracket) {
				if (t.startsWith("[")) {
					int n = t.indexOf("]");
					if (n != -1) {
						t = t.substring(n+2, t.length());
					}
				}
			}
			values[numIndent] = t;
			if (numIndent > 0) {
				w.add(values[numIndent-1] + "|" + t);
			}
		}
		return w;
	}

	public static String displayName2BarDelimited(String line) {
		int n = line.lastIndexOf("(");
		String s1 = line.substring(0, n-1);
		String s2 = line.substring(n+1, line.length()-1);
		return s1 + "|" + s2;
	}


    public static void main(String[] args) {
        String parent_child_file = args[0];
        Vector parent_child_vec = Utils.readFile(parent_child_file);
        String nodefile = args[1];
        Vector v = Utils.readFile(nodefile);
        HashSet nodeSet = new HashSet();
        for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			if (line.endsWith("Yes")) {
				nodeSet.add((String) u.elementAt(0));
			}
		}

		System.out.println("parent_child_vec: " + parent_child_vec.size());
		System.out.println("nodeSet: " + nodeSet.size());
		String rootCode = "C54443";
		nodeSet.add(rootCode);
		Vector w = getEmbeddedHierarchy(parent_child_vec, rootCode, nodeSet);
		Utils.saveToFile(rootCode + ".txt", w);
		String htmlfile = rootCode + ".html";
		generateDynamicHTMLTree(rootCode + ".txt", htmlfile);
		modifyHTMLFile(htmlfile);
	}
}
