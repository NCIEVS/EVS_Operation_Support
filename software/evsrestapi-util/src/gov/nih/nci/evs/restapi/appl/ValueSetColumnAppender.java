package gov.nih.nci.evs.restapi.appl;
import gov.nih.nci.evs.restapi.util.*;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.*;
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
public class ValueSetColumnAppender {

	public static void run(String restURL, String named_graph, String username, String password, String flatfile) {
        FlatFileGenerator generator = new FlatFileGenerator(restURL, named_graph, username, password);
		//flatfile = generator.generate();
		Vector v = generator.getConceptMembership(named_graph);
		HashMap code2LabelMap = new HashMap();
        HashMap hmap = new HashMap();
		for (int i=0; i<v.size(); i++) {
			String t = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(t, '|');
			String label = (String) u.elementAt(0);
			String code = (String) u.elementAt(1);

			Vector w = new Vector();
			if (hmap.containsKey(code)) {
				w = (Vector) hmap.get(code);
			}
			String targetName = (String) u.elementAt(3);
			String targetCode = (String) u.elementAt(4);
			code2LabelMap.put(targetCode, targetName);
			if (!w.contains(targetCode)) {
				w.add(targetCode);
			}
			hmap.put(code, w);
		}

		v.clear();
		v = Utils.readFile(flatfile);
		Vector v2 = new Vector();
		HashSet hset = new HashSet();
		for (int i=0; i<v.size(); i++) {
			String t = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(t, '\t');
		    String code = (String) u.elementAt(0);
		    hset.add(code);
		    String cis = "";
		    if (hmap.containsKey(code)) {

				Vector labels = new Vector();
				Vector w = (Vector) hmap.get(code);
				for (int j=0; j<w.size(); j++) {
					String targetCode = (String) w.elementAt(j);
					String targetName = (String) code2LabelMap.get(targetCode);
					labels.add(targetName);
				}
				labels = new SortUtils().quickSort(labels);
				StringBuffer buf = new StringBuffer();
				for (int j=0; j<labels.size(); j++) {
					String targetName = (String) labels.elementAt(j);
					buf.append(targetName).append("|");
				}
				String s = buf.toString();
				cis = s.substring(0, s.length()-1);
			}
			t = t + "\t" + cis;
			v2.add(t);
		}

		System.out.println("Number of code in the " + flatfile + ": " + hset.size());
		int n = flatfile.lastIndexOf(".");
		String outputfile = flatfile.substring(0, n) + "_" + StringUtils.getToday() + ".txt";
		Utils.saveToFile(outputfile, v2);
		System.out.println("File generated as " + outputfile + ".");
	}

	public static void main(String[] args) {
		long ms = System.currentTimeMillis();
		String restURL = args[0];
		String named_graph = args[1];
		String username = args[2];
		String password = args[3];
		String flatfile = args[4];
		run(restURL, named_graph, username, password, flatfile);
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}
}

