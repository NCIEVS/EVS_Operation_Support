package gov.nih.nci.evs.restapi.util;

import java.io.*;
import java.text.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;
import java.nio.charset.Charset;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

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


public class Text2Excel {

    public static Vector listFilesInDirectory() {
		String dirName = null;
		return listFilesInDirectory(dirName);
	}


    public static Vector listFilesInDirectory(String dirName) {
		Vector v = new Vector();
		if (dirName == null) {
			dirName = System.getProperty("user.dir");;
		}
        File f = new File(dirName);
        String[] pathnames = f.list();
        for (String pathname : pathnames) {
            v.add(pathname);
        }
        return v;
	}

    public static Vector getSheetNames(String excelfile) {
		Vector sheetLabel_vec = new Vector();
	    Workbook workbook = ExcelReader.openWorkbook(excelfile);
	    int numberOfSheets = workbook.getNumberOfSheets();
	    System.out.println("numberOfSheets: " + numberOfSheets);
	    for (int i=0; i<numberOfSheets; i++) {
		    Sheet sheet = workbook.getSheetAt(i);
		    String sheetName = sheet.getSheetName();
		    System.out.println("sheetName: " + sheetName);
		    sheetLabel_vec.add(sheetName);
		}
		return sheetLabel_vec;
	}

    public static void generateExcel(Vector datafile_vec, String excelfile, char delim, Vector sheetLabel_vec) {
		ExcelWriter writer = new ExcelWriter();
		if (excelfile.endsWith(".xls")) {
			writer.writeToHSSF(datafile_vec, excelfile, delim, sheetLabel_vec);
		} else {
			writer.writeToXSSF(datafile_vec, excelfile, delim, sheetLabel_vec);
		}
		System.out.println(excelfile + " generated.");
	}

    public static void generateExcel(String dir, String excelfile, char delim) {
		Vector files = listFilesInDirectory(dir);
		String currentPath = null;
		try {
			currentPath = new java.io.File(".").getCanonicalPath();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		Vector datafile_vec = new Vector();
		Vector sheetLabel_vec = new Vector();
		String label0 = null;
		for (int i=0; i<files.size(); i++) {
			String file = (String) files.elementAt(i);
			int n = file.lastIndexOf(".");
			String sheetLabel = file.substring(0, n);
			sheetLabel_vec.add(sheetLabel);
			datafile_vec.add(currentPath + File.separator + dir + File.separator + file);
		}
		Utils.dumpVector("datafile_vec", datafile_vec);
		Utils.dumpVector("sheetLabel_vec", sheetLabel_vec);
		generateExcel(datafile_vec, excelfile, delim, sheetLabel_vec);
		System.out.println(excelfile + " generated.");
	}

	public static void main(String[] args) {
		String dir = args[0];
		System.out.println("working dir: " + dir);
		String excelfile = dir + ".xlsx";//args[1];//"NCIt_release_schedule.xlsx";
		System.out.println("excelfile: " + excelfile);
		char delim = '\t';
		System.out.println("Calling generateExcel...");
		generateExcel(dir, excelfile, delim);
	}

}