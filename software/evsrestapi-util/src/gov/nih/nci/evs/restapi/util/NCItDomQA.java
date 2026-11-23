package gov.nih.nci.evs.restapi.util;
//import gov.nih.nci.evs.restapi.ui.*;
import gov.nih.nci.evs.restapi.config.*;
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


public class NCItDomQA {
	static String PARENT_CHILD_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile; // "parent_child.txt";
	static String RESTRICTION_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.rolefile; //"roles.txt";
	static String AXIOM_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.axiomfile;
	static String A8_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.subsetfile;
	static String PROPERTY_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.propertyfile;
	static String ASSOCIATION_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.associationfile;

    static String LOCAL_AXIOM_FILE = "axiom_ThesaurusInferred_forTS.txt";
    static String LOCAL_HIERARCHY_FILE = "parent_child.txt";
    static String LOCAL_A8_FILE = "A8.txt";
    static String LOCAL_ASSOCIATION_FILE = "association.txt";
    static String LOCAL_PROPERTY_FILE = "property.txt";
    static String LOCAL_ROLE_FILE = "roles.txt";

	public static String decode(String line) {
		line = line.replace("&quot;" ,"\"");
		line = line.replace("&amp;" ,"&");
		line = line.replace("&gt;" ,">");
		line = line.replace("&lt;" ,"<");
		line = line.replace("&apos;" ,"'");
    	return line;
	}

    public static void sort(String filename) {
		Vector v = Utils.readFile(filename);
		v = new SortUtils().quickSort(v);
		Utils.saveToFile(filename, v);
	}

	public static void decodeFile(String filename) {
		Vector w = new Vector();
		Vector v = Utils.readFile(filename);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			w.add(decode(line));
		}
		Utils.saveToFile(filename, w);
	}

	public static void qa(String srcFileName, String domGeneratedFile) {
		try {
			File f = new File(domGeneratedFile);
			if (!f.exists()) {
				System.out.println(domGeneratedFile + " does not exist. -- please generate the file first.");
				return;
			}
			System.out.println("srcFileName: " + srcFileName);
			int n = srcFileName.lastIndexOf(File.separator);
			String filename = srcFileName.substring(n+1, srcFileName.length());
			System.out.println("filename: " + filename);
			String targetFileName = System.getProperty("user.dir") + File.separator + "v0_" + filename;
			System.out.println("targetFileName: " + targetFileName);
			FileUtils.copyfile(srcFileName, targetFileName);
			System.out.println("copyfile: from " + srcFileName + " to " + targetFileName);
			System.out.println("decode " + targetFileName);
			decodeFile(targetFileName);
			System.out.println("sort " + targetFileName);
			sort(targetFileName);
			String label = "dom_diff";
			Vector diff_vec = DiffUtils.run(label, targetFileName, domGeneratedFile);
			Utils.saveToFile("diff_" + domGeneratedFile, diff_vec);
		} catch (Exception ex) {
		    ex.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {

			String srcFileName = RESTRICTION_FILE;
			String domGeneratedFile = LOCAL_ROLE_FILE;
			qa(srcFileName, domGeneratedFile);

			srcFileName = A8_FILE;
			domGeneratedFile = LOCAL_A8_FILE;
			qa(srcFileName, domGeneratedFile);

			srcFileName = ASSOCIATION_FILE;
			domGeneratedFile = LOCAL_ASSOCIATION_FILE;
			qa(srcFileName, domGeneratedFile);

			srcFileName = AXIOM_FILE;
			domGeneratedFile = LOCAL_AXIOM_FILE;
			qa(srcFileName, domGeneratedFile);

			srcFileName = PARENT_CHILD_FILE;
			domGeneratedFile = LOCAL_HIERARCHY_FILE;
			qa(srcFileName, domGeneratedFile);

			srcFileName = PROPERTY_FILE;
			domGeneratedFile = LOCAL_PROPERTY_FILE;
			qa(srcFileName, domGeneratedFile);

		} catch (Exception ex) {
		    ex.printStackTrace();
		}
	}
}

