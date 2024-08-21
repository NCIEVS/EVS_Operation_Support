package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.bean.*;
import gov.nih.nci.evs.restapi.common.*;
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
import java.util.*;
import java.util.regex.*;

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
public class TreeTraversalUtils {
    OWLSPARQLUtils owlSPARQLUtils = null;
    HierarchyHelper hh = null;
    Vector main_types = null;
    String named_graph = null;

    public TreeTraversalUtils(Vector parent_child_vec, Vector main_types) {
		this.main_types = main_types;
		this.hh = new HierarchyHelper(parent_child_vec);
    }

    public TreeTraversalUtils(String serviceUrl, String named_graph, String username, String password, Vector parent_child_vec, Vector main_types) {
		this.main_types = main_types;
		this.hh = new HierarchyHelper(parent_child_vec);

		this.named_graph = named_graph;
		owlSPARQLUtils = new OWLSPARQLUtils(serviceUrl, username, password);
		if (named_graph != null) {
			owlSPARQLUtils.set_named_graph(named_graph);
		}
	}

	public Vector formatRoles(String label, String code, Vector roles, boolean outbound) {
		Vector v = new Vector();
		for (int i=0; i<roles.size(); i++) {
			String line = (String) roles.elementAt(i);

			if (outbound) {
				line = label + "|" + code + "|" + line;
			} else {
				//Stage I Choroidal and Ciliary Body Melanoma AJCC v8|C140660|Disease_Has_Primary_Anatomic_Site
				//System.out.println(line);
				line = line + "|" + label + "|" + code;
			}
			line = StringUtils.formatAssociation(line);
			v.add(line);
		}
		return new SortUtils().quickSort(v);
	}


	public Vector getRolesByCode(String code) {
		String label = hh.getLabel(code);
		Vector w = new  Vector();
		boolean outbound = true;
		Vector v = owlSPARQLUtils.getInboundRolesByCode(named_graph, code);
		v = new ParserUtils().getResponseValues(v);
		if (v != null && v.size() > 0) {
			v = formatRoles(label, code, v, false);
			v = new SortUtils().quickSort(v);
			w.addAll(v);
		}

		v = owlSPARQLUtils.getOutboundRolesByCode(named_graph, code);
		v = new ParserUtils().getResponseValues(v);
		if (v != null && v.size() > 0) {
			v = formatRoles(label, code, v, true);
			v = new SortUtils().quickSort(v);
			w.addAll(v);
		}
		return w;
	}

	public void dumpVector(String indentation, Vector v) {
		for (int i=0; i<v.size(); i++) {
			String t = (String) v.elementAt(i);
			t = indentation + t;
			System.out.println(t);
		}
	}


    public String getIndentation(int level) {
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<level; i++) {
			buf.append("\t");
		}
		return buf.toString();
	}

/*
	public Vector getLocality(String code) {
		Vector w = new  Vector();
		boolean outbound = true;
		Vector v = owlSPARQLUtils.getAssociatedConcepts(named_graph, code, null, outbound);
		v = new ParserUtils().getResponseValues(v);
		if (v != null && v.size() > 0) {
			w.addAll(v);
		}
		outbound = false;
		v = owlSPARQLUtils.getAssociatedConcepts(named_graph, code, null, outbound);
		v = new ParserUtils().getResponseValues(v);
		if (v != null && v.size() > 0) {
			w.addAll(v);
		}
		return w;
	}
*/

	public void run(String root) {
		run(root, false);
	}

    public void run(String root, boolean show_locality) {
		Stack stack = new Stack();
		int level = 0;
		stack.push(root + "|" + level);
		while (!stack.isEmpty()) {
			String line = (String) stack.pop();
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(0);
			String level_str = (String) u.elementAt(1);
			int next_level = Integer.parseInt(level_str);
			String label = hh.getLabel(code);
			String indentation = getIndentation(next_level);
			if (main_types != null && main_types.contains(code)) {
				System.out.println(indentation + label + " (" + code + ") (*)");
			} else {
				System.out.println(indentation + label + " (" + code + ")");
			}

			if (show_locality) {
				Vector v = getRolesByCode(code);
                dumpVector(indentation, v);
			}

			next_level = next_level + 1;

			Vector superclass_codes = hh.getSuperclassCodes(code);
			if (superclass_codes != null) {
				for (int k=0; k<superclass_codes.size(); k++) {
					String child_code = (String) superclass_codes.elementAt(k);
					stack.push(child_code + "|" + next_level);
				}
			}
		}
	}


	public static void main(String[] args) {
		long ms = System.currentTimeMillis();
		String serviceUrl = args[0];
		String named_graph = args[1];
		String username = args[2];
		String password = args[3];

		String parent_child_file = args[4];
		Vector main_types = null;

		String code = args[5];
		Vector parent_child_vec = Utils.readFile(parent_child_file);
		//Vector main_types = Utils.readFile("main_types.txt");

		new TreeTraversalUtils(serviceUrl, named_graph, username, password, parent_child_vec, main_types).run(code, true);

		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}
}

