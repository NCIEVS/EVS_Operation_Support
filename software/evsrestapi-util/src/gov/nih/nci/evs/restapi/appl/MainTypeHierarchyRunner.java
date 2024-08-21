package gov.nih.nci.evs.restapi.appl;
import gov.nih.nci.evs.restapi.bean.*;
import gov.nih.nci.evs.restapi.common.*;
import gov.nih.nci.evs.restapi.util.*;
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
public class MainTypeHierarchyRunner {

	public static void countColumn(String filename, int column) {
		int knt = 0;

		Vector v = gov.nih.nci.evs.restapi.util.Utils.readFile(filename);
		int total = v.size()-1;

		String firstLine = (String) v.elementAt(0);
		Vector u = gov.nih.nci.evs.restapi.util.StringUtils.parseData(firstLine, '\t');
		String columnLabel = (String) u.elementAt(column);

		for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			u = gov.nih.nci.evs.restapi.util.StringUtils.parseData(line, '\t');
			String value = (String) u.elementAt(column);
			if (value.compareTo("true") == 0) {
				//System.out.println(line);
				knt++;
			}
		}
		System.out.println(columnLabel + " " + knt + " of " + total);
	}

	public static void main(String[] args) {
/*
		String serviceUrl = ConfigurationController.serviceUrl;
		String named_graph = ConfigurationController.namedGraph;
		String username = ConfigurationController.username;
		String password = ConfigurationController.password;
*/
		String serviceUrl = args[0];
		String named_graph = args[1];
		String username = args[2];
		String password = args[3];

		System.out.println(serviceUrl);
		MetadataUtils test = new MetadataUtils(serviceUrl, username, password);
		String codingScheme = "NCI_Thesaurus";
		long ms = System.currentTimeMillis();
		String version = test.getLatestVersion(codingScheme);
		System.out.println(codingScheme);
		System.out.println(version);
		//String named_graph = test.getNamedGraph(codingScheme);
		System.out.println(named_graph);

		MainTypeHierarchyData mthd = new MainTypeHierarchyData(serviceUrl, named_graph, username, password);

		String ncit_version = mthd.getVersion();
		System.out.println("version " + ncit_version);
		Vector broad_category_vec = mthd.get_broad_category_vec();
		//StringUtils.dumpVector("broad_category_vec", broad_category_vec);
		HashSet main_type_set = mthd.get_main_type_set();
		Vector<String> parent_child_vec = mthd.get_parent_child_vec(named_graph);

		System.out.println(	"parent_child_vec: " + parent_child_vec.size());

		Vector v1 = mthd.getDiseaseIsStageSourceCodes(named_graph);
		Vector v2 = mthd.getDiseaseIsGradeSourceCodes(named_graph);
        HashMap stageConceptHashMap = mthd.generateStageConceptHashMap(v1);
        HashMap gradeConceptHashMap = mthd.generateGradeConceptHashMap(v2);

	    HashSet ctrp_biomarker_set = mthd.get_ctrp_biomarker_set();
	    HashSet ctrp_reference_gene_set = mthd.get_ctrp_reference_gene_set();


System.out.println("====================== Instantiating MainTypeHierarchy ======================");

        MainTypeHierarchy mth = new MainTypeHierarchy(ncit_version, parent_child_vec, main_type_set, broad_category_vec,
             stageConceptHashMap, gradeConceptHashMap, ctrp_biomarker_set, ctrp_reference_gene_set);
System.out.println("main_type_set: " +  main_type_set.size());

        Vector mth_vec = mth.generate_main_type_hierarchy();
        Utils.saveToFile("MainTypeHierarchy_" + StringUtils.getToday() + ".txt", mth_vec);

        String outputfile = mth.DISEASES_AND_DISORDERS_CODE + ".txt";
        try {
			Vector codes = mth.getTransitiveClosure(mth.DISEASES_AND_DISORDERS_CODE);
			boolean flatFormat = true;
			mth.run(codes, outputfile, flatFormat);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}

}

