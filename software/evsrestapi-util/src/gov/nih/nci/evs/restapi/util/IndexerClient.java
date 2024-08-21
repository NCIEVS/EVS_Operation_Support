package gov.nih.nci.evs.restapi.util;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class IndexerClient {
	String serviceUrl = null;
	String namedGraph = null;
	String username = null;
	String password = null;
    private SPARQLSearchUtils searchUtils = null;
    private IndexUtils indexUtils = null;
	private int THRESHOLD = 100;

	public IndexerClient(String serviceUrl, String namedGraph, String username, String password) {
		this.serviceUrl = serviceUrl;
		this.namedGraph = namedGraph;
        searchUtils = new SPARQLSearchUtils(serviceUrl, namedGraph, username, password);
        indexUtils = new IndexUtils(serviceUrl, namedGraph, username, password);
	}

	public void set_THRESHOLD(int threshold) {
		this.THRESHOLD = threshold;
	}

	public void dumpHashMap(String label, Vector keys, HashMap hmap) {
		System.out.println(label);
		for (int i=0; i<keys.size(); i++) {
			String key = (String) keys.elementAt(i);
			Vector values = (Vector) hmap.get(key);
			Utils.dumpVector(key, values);
		}
	}

	public HashMap run(String vbtfile) {
		Vector w = Utils.readFile(vbtfile);
		return run(w, "unmatched.txt");
	}

	public HashMap run(Vector vbt_vec, String unmatched_file) {
		Vector unmatched = new Vector();
		HashMap hmap = new HashMap();
		for (int i=0; i<vbt_vec.size(); i++) {
			String term = (String) vbt_vec.elementAt(i);
			Vector v = search(namedGraph, term);
			if (v == null || v.size() > THRESHOLD) {
				unmatched.add(term);
			}
			hmap.put(term, v);
		}
		Utils.saveToFile(unmatched_file, unmatched);
		return hmap;
	}

	public Vector search(String named_graph, String term) {
		term = term.trim();
		if (term.length() == 0) return null;
		Vector w = searchUtils.search(named_graph, term, SPARQLSearchUtils.EXACT_MATCH);
		if (w != null && w.size() > 0) return w;
		w = getCodeBySignature(term);
		if (w != null && w.size() > 0) return w;
		w = indexUtils.indexNarrative(term);
		if (w != null && w.size() > 0) return w;
		return null;
	}

	public Vector getCodeBySignature(String term) {
		String signature = searchUtils.getSignature(term);
		Vector v = searchUtils.getCodeBySignature(signature);
		if (v == null) {
			return null;
		}
		Vector w1 = new Vector();
		if (v == null || v.size() == 0) return w1;
		for (int i=0; i<v.size(); i++) {
			String code = (String) v.elementAt(i);
			String label = searchUtils.get_label(code);
			w1.add(label + "|" + code);
		}
		return new SortUtils().quickSort(w1);
	}
}
