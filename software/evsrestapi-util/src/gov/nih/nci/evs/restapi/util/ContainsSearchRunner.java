package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;

import java.io.*;
import java.text.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;
import java.nio.charset.Charset;
import org.apache.poi.ss.usermodel.IndexedColors;

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


public class ContainsSearchRunner {
    static String NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile;

	public ContainsSearchRunner() {

	}

    public static String createTermFile(String filename, int codeCol, int termCol) {
		long ms = System.currentTimeMillis();
		DataExtractor.dumpHeading(filename);
		Vector<Integer> col_vec = new Vector();
		col_vec.add(Integer.valueOf(codeCol));
		col_vec.add(Integer.valueOf(termCol));
		char delim = '\t';
	    Vector v = DataExtractor.extractColumnData(filename, col_vec, delim);
	    int n = filename.lastIndexOf(".");
	    String outputfile = "data_" + filename.substring(0, n) + ".txt";
		Utils.saveToFile(outputfile, v);
		System.out.println("Output file " + outputfile + " generated.");
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
		return outputfile;
	}

//Disease Name	Indication(s)	NCIt Code	NCI PT	note
    public static String createTermFile(String filename, int codeCol) {
		long ms = System.currentTimeMillis();
	    Vector v = Utils.readFile(filename);
	    Vector w = new Vector();
	    w.add("NCIt Code" + "\t" + "NCIt Definition");
	    int n = filename.lastIndexOf(".");
	    String outputfile = "data_" + filename.substring(0, n) + ".txt";
	    for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '\t');
			String code = (String) u.elementAt(codeCol);
			String def = ContainsSearch.getDefinition(code);
			//termfile
			w.add(code + "\t" + def);
		}
		Utils.saveToFile(outputfile, w);
		System.out.println("Output file " + outputfile + " generated.");
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
		return outputfile;
	}

	public static void createTermFile(String outputfile) {
		String req = "P97|P378$NCI";
        Vector dataVec = new Vector();
		dataVec.add(req);
		DataRetrieval test = new DataRetrieval(NCIT_OWL, dataVec);
	    Vector w = test.getData(req);
	    for (int i=1; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(0);
			String def = (String) u.elementAt(2);
			w.add(code + "\t" + def);
		}
	    //return DataRetrieval.vector2MultiValuedHashMap(w);
	    Utils.saveToFile(outputfile, w);
	}

//NCIt Code	NCI PT	NCI DEFINITION	Disease Name

// termfile (terminology file) contains terminology data <key (code), value (DEFINITION)>
// data file contains verbatim terms need to be checked if value contains each verbatim term (e.g., NCCN disease name)

	public static String text2Excel(String textfile, char delim) {
		int n = textfile.lastIndexOf(".");
		String sheetName = textfile.substring(0, n);//label + " " + StringUtils.getToday("yyyy-MM-dd");
		String xlsfile = null;
		try {
			xlsfile = ExcelReadWriteUtils.writeXLSFile(textfile, delim, sheetName);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return xlsfile;
	}

	public static void search(String datafile, int disNameCol, int codeCol) {
		////Disease Name	Indication(s)	NCIt Code	NCI PT	note
		//String termfile = createTermFile(datafile, 2);
        String nciDefFile = "nciDefinition.txt";
        createTermFile(nciDefFile);
        String termfile = nciDefFile;
		//int codeCol = 2;
		//int disNameCol = 0;
        String outputfile = new ContainsSearch().run(termfile, datafile, codeCol, disNameCol);
        System.out.println(outputfile + " generated.");
        char delim = '\t';
        String xlsfile = text2Excel(outputfile, delim);
 		short firstRowColor = IndexedColors.LIGHT_GREEN.getIndex();
		ExcelFormatter.reformat(xlsfile, firstRowColor);
    }

	public static String run(String filename, int dis_col, int def_col) {
		ContainsSearch cs = new ContainsSearch();
		String outputfile = cs.run(filename, dis_col, def_col);
		return outputfile;
	}

	public static void main(String[] args) {
		////Disease Name	Indication(s)	NCIt Code	NCI PT	note
		String datafile = args[0];
		String disColStr = args[1];
		int dis_col = Integer.parseInt(disColStr);
		String defColStr = args[2];
		int def_col = Integer.parseInt(defColStr);
		run(datafile, dis_col, def_col);
    }
}

