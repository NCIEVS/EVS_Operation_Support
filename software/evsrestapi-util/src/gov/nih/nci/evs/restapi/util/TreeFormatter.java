package gov.nih.nci.evs.restapi.util;
import java.io.*;
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
public class TreeFormatter {

	HashMap code2NodeMap = new HashMap();
    HierarchyHelper hh = null;

	public TreeFormatter() {

	}

	public static String remoteTabs(String line) {
		while (line.startsWith("\t")) {
			line = line.substring(1, line.length());
		}
		return line;
	}

	public static String toDelimited(String line) {
		line = remoteTabs(line);
		int n = line.lastIndexOf("(");
		String label = line.substring(0, n-1);
		String code = line.substring(n+1, line.length()-1);
		System.out.println(label);
		System.out.println(code);
		String t = label + "|" + code;
		return t;
	}


	public static void flattenASCIITree(String asciitree) {
		Vector v = Utils.readFile(asciitree);
		int maxDepth = -1;
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			int n = 0;
			while (line.startsWith("\t")) {
				n++;
				line = line.substring(1, line.length());
			}
			if (n > maxDepth) {
				maxDepth = n;
			}
		}
		List<String> list = new ArrayList<String>();
		for (int i=0; i<=maxDepth; i++) {
			list.add("");
		}
        Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (line.length() > 0) {
				int n = 0;
				while (line.startsWith("\t")) {
					n++;
					line = line.substring(1, line.length());
				}
				line = toDelimited(line);
				list.set(n, line);
				if (n > 0) {
					String sup = list.get(n-1);
					w.add(sup + "|" + line);
				}
	    	}
		}
        Utils.saveToFile("parent_child_" + asciitree, w);
	}

	public static void main(String[] args) {
		String asciitree = args[0];
		flattenASCIITree(asciitree);
	}

}

