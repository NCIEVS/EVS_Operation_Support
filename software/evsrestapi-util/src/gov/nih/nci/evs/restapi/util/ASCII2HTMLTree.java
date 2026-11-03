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
public class ASCII2HTMLTree {

    public static Vector get_parent_child_vec(Vector ascii_tree_vec) {
		return new ASCIITreeUtils().get_parent_child_vec(ascii_tree_vec);
	}

	public static Vector getRoots(Vector parent_child_vec) {
		HierarchyHelper hh = new HierarchyHelper(parent_child_vec);
		Vector roots = hh.getRoots();
		Vector w = new Vector();
		for (int i=0; i<roots.size(); i++) {
			String code = (String) roots.elementAt(i);
			String label = hh.getLabel(code);
			w.add(label + "|" + code);
		}
		return w;
	}

    public static void generateHTMLtree(String asciitreedatafile, String title, String root) {
		Vector parent_child_vec = Utils.readFile(asciitreedatafile);
		Vector roots = getRoots(parent_child_vec);
		Vector w = new Vector();
		if (roots.size() > 1) {
			for (int i=0; i<roots.size(); i++) {
				String line = (String) roots.elementAt(i);
				String t = title + "|" + "<root>|" + line;
				w.add(t);
			}
			root = "<root>";
		}
		w.addAll(parent_child_vec);
		Utils.saveToFile("mod_" + asciitreedatafile, w);

		int n = asciitreedatafile.lastIndexOf(".");
		String htmlfile = asciitreedatafile.substring(0, n) + "_tree.html";

		ASCII2HTMLTreeConverter.generateDynamicHTMLTree("mod_" + asciitreedatafile, htmlfile);
		Vector v = Utils.readFile(htmlfile);
		w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			line = line.replace("/ncitbrowser", "web");
			w.add(line);
		}
		Utils.saveToFile(htmlfile, w);
		System.out.println(htmlfile + " generated.");

		String htmlfile2 = HTMLHierarchy.run("mod_" + asciitreedatafile, title, root);
		System.out.println(htmlfile2 + " generated.");
	}

    public static void main(String[] args) {
		String asciitreedatafile = args[0];
		String title = "UMLS Semantic Network";
		String root = "<root>";
		generateHTMLtree(asciitreedatafile, title, root);
    }

}