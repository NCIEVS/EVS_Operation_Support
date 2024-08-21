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
public class CSV2HTMLTable {

    public static Vector addHyperlinks(String datafile, int columnIndex, String url) {
		Vector w = new Vector();
		Vector v = Utils.readFile(datafile);
		w.add((String) v.elementAt(0));

		for (int i=1; i<v.size(); i++) {
			String t = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(t, '|');
			StringBuffer buf = new StringBuffer();
			for (int j=0; j<u.size(); j++) {
				String value = (String) u.elementAt(j);
				if (j == columnIndex) {
					value = ExternalLinkPageGenerator.hyperlink(url, value);
				}
				buf.append(value).append("|");
			}
			String s = buf.toString();
			s = s.substring(0, s.length()-1);
			w.add(s);
		}
		return w;
	}

	public static void main(String[] args) {
		String serviceUrl = args[0];
		String named_graph = args[1];
		String username = args[2];
		String password = args[3];
		String csvfile = args[4];

		boolean skip_heading = false;
		String delim = "|";
        Vector v = CSVFileReader.readCSV(csvfile, skip_heading, delim);
        int n = csvfile.lastIndexOf(".");
        String inputfile = csvfile.substring(0, n) + ".txt";

		if (args.length > 5) {
			String columnIndexStr = args[5];
			int columnIndex = Integer.parseInt(columnIndexStr);
			String url = args[6];
			Vector w = addHyperlinks(inputfile, columnIndex, url);
			Utils.saveToFile(inputfile, w);
		}

        Utils.saveToFile(inputfile, v);
		String outputfile = new HTMLTableDataConverter(serviceUrl, named_graph, username, password).convert(inputfile);
		System.out.println(outputfile + " generated.");
		v = Utils.readFile(outputfile);
		outputfile = new HTMLTable().generate(v);
		System.out.println(outputfile + " generated.");
	}
}
