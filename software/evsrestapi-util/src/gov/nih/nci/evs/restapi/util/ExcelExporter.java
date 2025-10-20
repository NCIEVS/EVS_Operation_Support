package gov.nih.nci.evs.restapi.util;

import gov.nih.nci.evs.restapi.bean.*;
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
import java.text.*;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.*;

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

public class ExcelExporter {

	public ExcelExporter() {

	}

	public static String exportXLSFile(String excelFilePath) {
		int n = excelFilePath.lastIndexOf(".");
		String textfile = excelFilePath.substring(0, n) + ".txt";
		try {
			Vector w = ExcelReadWriteUtils.readXLSFile(excelFilePath);
			Utils.saveToFile(textfile , w);
			System.out.println(textfile + " generated.");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return textfile;
	}

    public static Vector getSheetNames(String excelfile) {
		try {
			return ExcelReader.getSheetNames(excelfile);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static int getSheetNumber(String excelfile, String sheetName) {
		Vector names = getSheetNames(excelfile);
		for (int i=0; i<names.size(); i++) {
			String nm = (String) names.elementAt(i);
			if (nm.compareTo(sheetName) == 0) {
				return i;
			}
		}
		return -1;
	}

	public static String getSheetName(String excelfile, int sheet_num) {
		Vector names = getSheetNames(excelfile);
        return (String) names.elementAt(sheet_num);
	}

    public static String export(String excelfile, String sheetName) throws IOException {
        int sheetNum = getSheetNumber(excelfile, sheetName);
        return export(excelfile, sheetNum);
    }

    public static String export(String excelfile, int sheetNum) throws IOException {
		int n = excelfile.lastIndexOf(".");
		String textfile = getSheetName(excelfile, sheetNum) + ".txt";
		textfile = textfile.replace(" ", "_");

        try {
			XSSFWorkbook workbook = new XSSFWorkbook(new File(excelfile));
			XSSFSheet sheet = workbook.getSheetAt(sheetNum); // Get the first sheet
			FileWriter writer = new FileWriter(textfile);
			for (Row row : sheet) {
				for (Cell cell : row) {
					String cellValue = cell.toString();
					writer.write(cellValue + "\t"); // Use tab as delimiter
				}
				writer.write("\n");
			}
			writer.close();
			workbook.close();
			return textfile;
    	} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
    }

    public static String export(String excelfile, int sheetNum, String outputdir) throws IOException {
		int n = excelfile.lastIndexOf(".");
		String textfile = ExcelExporter.getSheetName(excelfile, sheetNum) + ".txt";
		textfile = textfile.replace(" ", "_");
		textfile = outputdir + File.separator + textfile;

        try {
			XSSFWorkbook workbook = new XSSFWorkbook(new File(excelfile));
			XSSFSheet sheet = workbook.getSheetAt(sheetNum); // Get the first sheet
			FileWriter writer = new FileWriter(textfile);
			for (Row row : sheet) {
				for (Cell cell : row) {
					String cellValue = cell.toString();
					writer.write(cellValue + "\t"); // Use tab as delimiter
				}
				writer.write("\n");
			}
			writer.close();
			workbook.close();
			return textfile;
    	} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
    }

    public static String run(String excelfile) {
		return run(excelfile, 0);
    }

    public static String run(String excelfile, int sheet_num) {
		try {
			if (excelfile.endsWith("xls")) {
				int n = excelfile.lastIndexOf(".");
				String textfile = excelfile.substring(0, n) + ".txt";
				Vector data = ExcelUtils.excel2Text(excelfile, sheet_num);
				Utils.saveToFile(textfile, data);
                return textfile;
			} else {
				return export(excelfile, sheet_num);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
    }


/*
	public static void main(String[] args) {
		String excelfile = args[0];
		String sheetNumStr = args[1];
	    int sheet_number = Integer.parseInt(sheetNumStr);
		System.out.println("excelfile: " + excelfile);
		Vector data = excel2Text(excelfile, sheet_number);
        //generateTemplate(excelfile, 0);
        //write("template_" + excelfile, "v2_" + excelfile, 0, data, '\t');
        //String target_file = clone(excelfile);

	}


*/

    public static void exportExcelSheets(String excelfile, String outputDir) {
        Vector w = getSheetNames(excelfile);
        Utils.dumpVector(excelfile, w);
        File f = new File(outputDir);
        if (!f.exists()) {
			f.mkdir();
		}

		Vector textfiles = new Vector();
		for (int sheet_num=0; sheet_num<w.size(); sheet_num++) {
			try {
				String textfile = export(excelfile, sheet_num, outputDir);
				System.out.println(textfile + " generated.");
				textfiles.add(textfile);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
        String excelfile = args[0];
        String sheetStr = args[1];
        int sheet_num = Integer.parseInt(sheetStr);
        String textfile = run(excelfile, sheet_num);
        System.out.println(textfile + " generated.");
	}
}

