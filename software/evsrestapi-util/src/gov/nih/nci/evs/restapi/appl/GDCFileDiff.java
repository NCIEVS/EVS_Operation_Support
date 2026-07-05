package gov.nih.nci.evs.restapi.appl;
import gov.nih.nci.evs.restapi.util.*;

import java.io.*;
import java.net.URI;
import java.text.*;
import java.util.*;
import org.apache.commons.lang.StringEscapeUtils;

//https://www.javatips.net/api/java.text.normalizer

/**
 * <!-- LICENSE_TEXT_START -->
 * Copyright 2008-2017 NGIS. This software was developed in conjunction
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
 *      "This product includes software developed by NGIS and the National
 *      Cancer Institute."   If no such end-user documentation is to be
 *      included, this acknowledgment shall appear in the software itself,
 *      wherever such third-party acknowledgments normally appear.
 *   3. The names "The National Cancer Institute", "NCI" and "NGIS" must
 *      not be used to endorse or promote products derived from this software.
 *   4. This license does not authorize the incorporation of this software
 *      into any third party proprietary programs. This license does not
 *      authorize the recipient to use any trademarks owned by either NCI
 *      or NGIS
 *   5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED
 *      WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *      OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE
 *      DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
 *      NGIS, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT,
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
 *     Initial implementation kim.ong@ngc.com
 *
 */


public class GDCFileDiff {
	String filename1 = null;
	String filename2 = null;

	public GDCFileDiff(String filename1, String filename2) {
		this.filename1 = filename1;
		this.filename2 = filename2;
	}

	public static HashSet createHashSet(Vector v, int column, char delim) {
		HashSet hset = new HashSet();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, delim);
			String value = (String) u.elementAt(column);
			if (!hset.contains(value)) {
				hset.add(value);
			}
		}
		return hset;
	}

    // In v2 but not in v1
	public static Vector v1_v2(Vector v1, Vector v2) {
		Vector w = new Vector();
        HashSet hset = Utils.vector2HashSet(v2);
        for (int i=0; i<v1.size(); i++) {
			String t = (String) v1.elementAt(i);
			if (!hset.contains(t)) {
				w.add(t);
			}
		}
		return new SortUtils().quickSort(w);
	}

    public static Vector compareColumnValues(Vector w, Vector v1, Vector v2, int keyCol, int valueCol, String valueColLabel, char delim) {
		//System.out.println("Compare column " + valueCol);
        HashMap hmap1 = DelimitedDataExtractor.createHashMap(v1, 2, 3, delim);
        HashMap hmap2 = DelimitedDataExtractor.createHashMap(v2, 2, 3, delim);

        Iterator it1 = hmap1.keySet().iterator();
        while (it1.hasNext()) {
			String key1 = (String) it1.next();
			if (!hmap2.containsKey(key1)) {
				String line = "Delete" + "\t" + key1 + "\t";
				w.add(line);
			} else {
				String value1 = (String) hmap1.get(key1);
				String value2 = (String) hmap2.get(key1);
				if (value1.compareTo(value2) != 0) {
					String line = "Modify\t" + key1 + "\t" + valueColLabel + "\t" + value1 + "\t" + value2;
					w.add(line);
				}
			}
		}

        Iterator it2 = hmap1.keySet().iterator();
        while (it2.hasNext()) {
			String key2 = (String) it2.next();
			if (!hmap1.containsKey(key2)) {
				String line = "Add" + "\t" + key2 + "\t";
				w.add(line);
			}
		}
		return w;
	}

	public void run(String outputfile) {
		char delim = '\t';
		Vector v1 = Utils.readFile(filename1);
		Vector v2 = Utils.readFile(filename2);
		String header1 = (String) v1.remove(0);
		String header2 = (String) v2.remove(0);
		Vector header_vec = null;
		if (header1.compareTo(header2) != 0) {
			System.out.println("ERROR: headers are different.");
			System.out.println(header1);
			System.out.println(header2);
			System.exit(0);
		} else {
			header_vec = StringUtils.parseData(header1, delim);
		}

		Vector w = new Vector();
		w.add("Action\tConcept Id\tVariable Name\tOld Value\tNew Value");
        int keyCol = 2;
        String valueColLabel = null;
        for (int i=3; i<header_vec.size(); i++) {
			int valueCol = i;
			valueColLabel = (String) header_vec.elementAt(valueCol);
            w = compareColumnValues(w, v1, v2, keyCol, valueCol, valueColLabel, delim);
		}
		Utils.saveToFile(outputfile, w);
	}

	public static void text2Excel(String dir) {
		System.out.println("working dir: " + dir);
		String excelfile = dir + ".xlsx";
		System.out.println("excelfile: " + excelfile);
		char delim = '\t';
		System.out.println("Calling generateExcel...");
		Text2Excel.generateExcel(dir, excelfile, delim);
	}

	public static void submit(String outputDir) {
		String currentDir = System.getProperty("user.dir");
		Vector filenames = Utils.readFile("gdc.config");
		String filename1 = (String) filenames.elementAt(0);
		String filename2 = (String) filenames.elementAt(1);
		String dir = "gdcdiff";

		GDCFileDiff test = new GDCFileDiff(filename1, filename2);
		int n1 = filename1.lastIndexOf(".");
		int n2 = filename2.lastIndexOf(".");
		String outputfile = "GDC_Property_Terminology_diff.txt";
		test.run(outputfile);
		System.out.println("Copy ..." + outputfile);
		String targetFileName = dir + File.separator + outputfile;
		FileUtils.copyfile(outputfile, targetFileName);
 		filename1 = (String) filenames.elementAt(2);
		filename2 = (String) filenames.elementAt(3);
		test = new GDCFileDiff(filename1, filename2);
		n1 = filename1.lastIndexOf(".");
		n2 = filename2.lastIndexOf(".");
		outputfile = "GDC_Value_Terminology_diff.txt";
		test.run(outputfile);
        System.out.println("Copy ..." + currentDir + File.separator + outputfile);
        FileUtils.copyfile(currentDir + File.separator + outputfile, currentDir + File.separator + dir + File.separator + outputfile);

        text2Excel(dir);
	}

	public static void createDirectory(String dirName) {
		String currentDir = System.getProperty("user.dir");
		File folder = new File(currentDir + File.separator + dirName);
		if (!folder.exists()) {
			if (folder.mkdir()) {
				System.out.println("Folder created at: " + folder.getAbsolutePath());
			} else {
				System.out.println("Failed to create folder.");
			}
		}
	}

	public static void main(String[] args) {
		String outputDir = args[0];
		createDirectory(outputDir);
		submit(outputDir);
	}

}


