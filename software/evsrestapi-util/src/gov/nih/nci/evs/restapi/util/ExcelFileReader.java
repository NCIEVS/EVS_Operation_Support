package gov.nih.nci.evs.restapi.util;
import java.io.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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
public class ExcelFileReader {
     static String CONFIGFILE = "config.txt";
	 public static void saveToFile(String outputfile, String t) {
		 Vector v = new Vector();
		 v.add(t);
		 saveToFile(outputfile, v);
	 }

	 public static void saveToFile(String outputfile, Vector v) {

		PrintWriter pw = null;
		try {
			pw = new PrintWriter(outputfile, "UTF-8");
			if (v != null && v.size() > 0) {
				for (int i=0; i<v.size(); i++) {
					String t = (String) v.elementAt(i);
					pw.println(t);
				}
		    }
		} catch (Exception ex) {

		} finally {
			try {
				pw.close();
				System.out.println("Output file " + outputfile + " generated.");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	 }

	public static void saveToFile(PrintWriter pw, Vector v) {
		if (v != null && v.size() > 0) {
			for (int i=0; i<v.size(); i++) {
				String t = (String) v.elementAt(i);
				pw.println(t);
			}
		}
	}

	public String getSheetName(String fileName, int sheet_number) throws IOException {
		File file = new File(fileName);
		FileInputStream inputStream = new FileInputStream(file);
		Workbook workbook = null;
		String fileExtensionName = fileName.substring(fileName.indexOf("."));
		if(fileExtensionName.equals(".xlsx")){
			workbook = new XSSFWorkbook(inputStream);
		} else if(fileExtensionName.equals(".xls")){
			workbook = new HSSFWorkbook(inputStream);
		}
		Sheet sheet = workbook.getSheetAt(sheet_number);
		return sheet.getSheetName();
	}

	public String getSheetName(String filePath, String fileName, int sheet_number) throws IOException {
		File file = new File(filePath+"\\"+fileName);
		FileInputStream inputStream = new FileInputStream(file);
		Workbook workbook = null;
		String fileExtensionName = fileName.substring(fileName.indexOf("."));
		if(fileExtensionName.equals(".xlsx")){
			workbook = new XSSFWorkbook(inputStream);
		} else if(fileExtensionName.equals(".xls")){
			workbook = new HSSFWorkbook(inputStream);
		}
		Sheet sheet = workbook.getSheetAt(sheet_number);
		return sheet.getSheetName();
	}

	public Vector readExcel(String filePath, String fileName, int sheet_number) throws IOException {
		String absolutePath = filePath +"\\" + fileName;
		return readExcel(absolutePath, sheet_number);
	}

	public Vector readExcel(String absolutePath, int sheet_number) throws IOException {
		Vector w = new Vector();
		File file = new File(absolutePath);

		FileInputStream inputStream = new FileInputStream(file);
		Workbook workbook = null;
		String fileExtensionName = absolutePath.substring(absolutePath.indexOf("."));
		if(fileExtensionName.equals(".xlsx")){
			workbook = new XSSFWorkbook(inputStream);
		} else if(fileExtensionName.equals(".xls")){
			workbook = new HSSFWorkbook(inputStream);
		}
		Sheet sheet = workbook.getSheetAt(sheet_number);
		int rowCount = sheet.getLastRowNum()-sheet.getFirstRowNum();
		for (int i = 0; i < rowCount+1; i++) {
			StringBuffer buf = new StringBuffer();
			Row row = sheet.getRow(i);
			if (row != null) {
				for (int j = 0; j < row.getLastCellNum(); j++) {
					try {
						String retstr = getCellValue(row.getCell(j));
						retstr = retstr.replace("\n", " ");
						buf.append(retstr);
						if (j < row.getLastCellNum()-1) {
							buf.append("\t");
						}
					} catch (Exception ex) {
						buf.append("\t");
					}
				}
				w.add(buf.toString());
			}
		}
		return w;
    }

    private static String getCellValue(Cell cell) {
		String retstr = null;
        switch (cell.getCellType()) {
            case BOOLEAN:
                retstr = "" + cell.getBooleanCellValue();
                break;
            case STRING:
                retstr = "" + cell.getRichStringCellValue().getString();
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    retstr = "" + cell.getDateCellValue();
                } else {
                    retstr = "" + cell.getNumericCellValue();
                }
                break;
            case FORMULA:
                retstr = "" + cell.getCellFormula();
                break;
            case BLANK:
                retstr = "";
                break;
            default:
                retstr = "";
        }
        return retstr;
    }

    public static void main(String[] args) throws IOException{
		ExcelFileReader test = new ExcelFileReader();
		String configfile = CONFIGFILE;
		String excelfile = args[0];
		String sheetIndex_str = args[1];
		int sheet_number = Integer.parseInt(sheetIndex_str);
		String filePath = System.getProperty("user.dir");
		Vector w = test.readExcel(filePath, excelfile, sheet_number);
		String sheetName = test.getSheetName(filePath, excelfile, sheet_number);
		saveToFile(sheetName + ".txt", w);
    }
}
