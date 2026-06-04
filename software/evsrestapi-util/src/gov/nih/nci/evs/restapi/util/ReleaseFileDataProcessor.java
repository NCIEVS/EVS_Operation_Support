package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;
import java.io.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.*;
import java.util.*;
import org.apache.poi.ss.usermodel.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

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
public class ReleaseFileDataProcessor {
	static String PARENT_CHILD_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile;
    static HierarchyHelper hh = null;
    static String VALUE_SET_REPORT_CONFIG = "value_set_report_config.txt";
    static String FILE_INFO_AT_FTP_SITE = "FileInfoAtFTPSite.txt";
    static {
		hh = new HierarchyHelper(Utils.readFile(PARENT_CHILD_FILE));
	}

    public static String getValueSetURI(String source, String subsource, String code) {
		if (subsource == null) {
			return "http://evs.nci.nih.gov/valueset/" + source + "/" + code;
		} else {
			return "http://evs.nci.nih.gov/valueset/" + source + "/" + subsource + "/" + code;
		}
	}

	public static void downLoad(String link, String outputfile) {
		File file = new File(outputfile);
		DownloadPage.download(link, file);
	}

	public static Vector findSubsetCodes(String url, String textfile, int sheetNum, String rootSubsetCode) {
        Vector v = Utils.readFile(textfile);
        String firstLine = (String) v.elementAt(0);
        Vector u = StringUtils.parseData(firstLine, '\t');
        int colNum = 0;
        if (u.contains("NCIt Code of Table")) {
			for (int i=0; i<u.size(); i++) {
				String col = (String) u.elementAt(i);
				if (col.compareTo("Subset Code") == 0 || col.compareTo("NCIt Subset Code") == 0) {
					colNum = i;
				}
			}
			int sheetNum1 = sheetNum + 1;
			int colNum1 = colNum + 1;
			Vector w = new Vector();
			String label = hh.getLabel(rootSubsetCode);
			w.add(label + "|" + rootSubsetCode + "|" + url + "|" + sheetNum1 + ":" + colNum1 + ":all");
			return w;
		} else if (u.contains("Codelist Code")) {
			Vector w = new Vector();
			int sheetNum1 = sheetNum + 1;
			String label = hh.getLabel(rootSubsetCode);
			w.add(label + "|" + rootSubsetCode + "|" + url + "|" + sheetNum1 + ":all");
		}

        for (int i=0; i<u.size(); i++) {
			String col = (String) u.elementAt(i);
			if (col.compareTo("Subset Code") == 0 || col.compareTo("NCIt Subset Code") == 0) {
				colNum = i;
				break;
			}
		}
		if (colNum == -1) return null;
		HashSet hset = new HashSet();
        for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
       		u = StringUtils.parseData(line, '\t');
			String subsetCode = (String) u.elementAt(colNum);
			if (!hset.contains(subsetCode)) {
				hset.add(subsetCode);
			}
		}
		Vector subsetCodes = Utils.hashSet2Vector(hset);
		subsetCodes = new SortUtils().quickSort(subsetCodes);

        Vector w = new Vector();
        int sheetNum1 = sheetNum + 1;
        int colNum1 = colNum + 1;
		if (subsetCodes.size() == 1) {
			String code = (String) subsetCodes.elementAt(0);
			String label = hh.getLabel(code);
			w.add(label + "|" + code + "|" + sheetNum1 + ":all");
		} else {
			String label = hh.getLabel(rootSubsetCode);
			w.add(label + "|" + rootSubsetCode + "|" + url + "|" + sheetNum1 + ":all");
			for (int i=0; i<subsetCodes.size(); i++) {
				String code = (String) subsetCodes.elementAt(i);
				label = hh.getLabel(code);
				w.add(label + "|" + code + "|" + url + "|" + sheetNum1 + ":" + colNum1 + ":" + code);
			}
		}
		return w;
	}

    public static int getNumberOfSheets(String excelfile) {
	    Workbook workbook = ExcelReader.openWorkbook(excelfile);
	    int numberOfSheets = workbook.getNumberOfSheets();
	    return numberOfSheets;
	}

	public static Vector searchSubsetCodes(String rootSubsetCode, String url) {
		int n = url.lastIndexOf("/");
		String excelfile = url.substring(n+1, url.length());
		File f = new File(excelfile);
		if (!f.exists()) {
			downLoad(url, excelfile);
		}
        int numberOfSheets = getNumberOfSheets(excelfile);
        System.out.println("Number of sheets: " + numberOfSheets);
        Vector w0 = new Vector();
        for (int i=0; i<numberOfSheets; i++) {
			try {
				String textfile = ExcelExporter.run(excelfile, i);
				System.out.println(textfile + " generated.");
				Vector v = Utils.readFile(textfile);
				if (v.size() > 0) {
					String firstLine = (String) v.elementAt(0);
					Vector u = StringUtils.parseData(firstLine, '\t');

					Utils.dumpVector(firstLine, u);

					Vector w = new Vector();
					if (u.contains("Subset Code") || u.contains("NCIt Subset Code") || u.contains("NCIt Code of Table")) {
						Vector vs_data = findSubsetCodes(url, textfile, i, rootSubsetCode);
						w0.addAll(vs_data);
					} else if (u.contains("Codelist Code")) {
						Vector vs_data = findSubsetCodes(url, textfile, i, rootSubsetCode);
						w0.addAll(vs_data);
					}
				}
				FileUtils.deleteFile(textfile);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return w0;
	}

    public static void generateValueSetHome(String valueSetName, String valueSetDescription, String reportURI, String extractionRule) {
		ValueSetHomeGenerator.run(valueSetName, valueSetDescription, reportURI, extractionRule);
	}

	public static void generate_partial_value_set_report_config(String rerportUrl, String rootSubsetCode) {
        int n = rerportUrl.lastIndexOf("/");
        String excelfile = rerportUrl.substring(n+1, rerportUrl.length());
        Vector w0 = searchSubsetCodes(rootSubsetCode, rerportUrl);
        Utils.dumpVector(excelfile, w0);
        Utils.saveToFile(rootSubsetCode + ".txt", w0);
	}

	public static void getFileInfoAtFTPSite() {
		Vector v = FTPCrawler.listFileInfoAtFTPSite(FTPCrawler.NCIT_FTP_SITE);
		Utils.saveToFile(FILE_INFO_AT_FTP_SITE, v);
	}

	public static Vector loadFileInfoAtFTPSite() {
		File f = new File(FILE_INFO_AT_FTP_SITE);
		if (!f.exists()) {
			getFileInfoAtFTPSite();
		}
		Vector v = Utils.readFile(FILE_INFO_AT_FTP_SITE);
        Vector w = new Vector();
        for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			line = line.trim();
			if (line.startsWith("(")) {
				int n = line.indexOf(")");
				line = line.substring(n+2, line.length());
				w.add(line);
			}
		}
		return v;
	}

	public static void dumpExcelFilesAtFTPSite() {
		Vector w = loadFileInfoAtFTPSite();
		Vector w0 = new Vector();
		System.out.println(w.size());
		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			if (line.endsWith(".xls")) {
				System.out.println(line);
				w0.add(line);
			}
		}

		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			if (line.endsWith(".xlsx")) {
				System.out.println(line);
				w0.add(line);
			}
		}
		Utils.saveToFile("excelfiles_" + FILE_INFO_AT_FTP_SITE, w0);
	}

	public static void excel2HTML(String excelfile) {
		int n = excelfile.lastIndexOf(".");
		String valueSetName = excelfile.substring(0, n);
		String valueSetDescription = valueSetName;
		String reportURI = "https://evs.nci.nih.gov/ftp1/" + excelfile;
		String extractionRule = "1:all";
		EmbedExcel2HTML.run(valueSetName, valueSetDescription, reportURI, extractionRule);
	}

	public static void main(String[] args) {

        //SPL Color Terminology|http://evs.nci.nih.gov/valueset/FDA/C54453|https://evs.nci.nih.gov/ftp1/FDA/SPL/FDA-SPL_NCIt_Subsets.xls|1:2:C54453
		String valueSetName = "SPL Color Terminology";
		String reportURI = "https://evs.nci.nih.gov/ftp1/FDA/SPL/FDA-SPL_NCIt_Subsets.xls";
		String valueSetDescription = "Terminology used for representation of the the framework of the Structured Product Labeling documents. SPL Terminology can be downloaded from this location SPL.";
		String extractionRule = "1:2:C54453";
		//EmbedExcel2HTML.run(valueSetName, valueSetDescription, reportURI, extractionRule);
        ValueSetHomeGenerator.run(valueSetName, valueSetDescription, reportURI, extractionRule);

        //String excelfile = "C201601.xlsx";
        //excel2HTML(excelfile);
	}
}

