package gov.nih.nci.evs.restapi.appl;
import gov.nih.nci.evs.restapi.util.*;
import java.io.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFHyperlink;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFPicture;
import org.apache.poi.hssf.usermodel.HSSFPictureData;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFShape;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
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
public class CDISCExcelUtils {

	private static String getCellData(Cell cell) {
		String value = null;
		if (cell == null) {
			return null;
		}
		switch (cell.getCellType()) {
			case STRING:
				value = cell.getStringCellValue();
				break;
			case FORMULA:
				value = cell.getCellFormula();
				break;
			case NUMERIC:
				HSSFDataFormatter dataFormatter = new HSSFDataFormatter();
				value = dataFormatter.formatCellValue(cell);
				break;
			case BLANK:
				value = null;
				break;
			case ERROR:
				value = "#ERROR#";
				break;
		}
		return value;
	}


	public static boolean reformat(String xlsfile, int sheetIndex, Vector<Integer> rowNumbers, short colorIndex, String outputfile) {
		FileOutputStream fileOut = null;
		try {
			InputStream inp = new FileInputStream(xlsfile);
			Workbook wb = WorkbookFactory.create(inp);
			Sheet sheet = wb.getSheetAt(sheetIndex);

			for (int i=0; i<rowNumbers.size(); i++) {
				int rowNumber = rowNumbers.elementAt(i).intValue();
				Row row = sheet.getRow(rowNumber);
				System.out.println("row number " + rowNumber + " selected.");
				row.setHeight((short)-1);

				CellStyle cellStyle = wb.createCellStyle();
				Font font= wb.createFont();
				font.setFontName("Arial");
				font.setBold(false);
				font.setItalic(false);

				cellStyle.setWrapText(true);
				cellStyle.setBorderTop(BorderStyle.THIN);
				cellStyle.setBorderBottom(BorderStyle.THIN);
				cellStyle.setBorderLeft(BorderStyle.THIN);
				cellStyle.setBorderRight(BorderStyle.THIN);
				cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				cellStyle.setFillBackgroundColor(colorIndex);
				cellStyle.setFillForegroundColor(colorIndex);
				cellStyle.setFont(font);

				for (int k = 0; k < row.getLastCellNum(); k++) {
				   row.getCell(k).setCellStyle(cellStyle);
				}
			}

			fileOut = new FileOutputStream(outputfile);
			wb.write(fileOut);
			System.out.println(xlsfile + " is modified and saved as " + outputfile);
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;

		} finally {
			try {
				fileOut.close();
			} catch (Exception ex) {
				ex.printStackTrace();
				return false;
			}
		}
		return true;
	}

	public static Vector<Integer> findCodeListRows(String xlsfile) {
		int sheetIndex = 0;
		return findCodeListRows(xlsfile, sheetIndex);
	}

	public static Vector<Integer> findCodeListRows(String xlsfile, int sheetIndex) {
		boolean status = false;
		Vector rowNumbers = new Vector();
		try {
			InputStream inp = new FileInputStream(xlsfile);
			Workbook wb = WorkbookFactory.create(inp);
			Sheet sheet = wb.getSheetAt(sheetIndex);
			Iterator rows = sheet.rowIterator();
			int row_num = 0;
			while (rows.hasNext()) {
				HSSFRow row = (HSSFRow) rows.next();
				Cell cell = row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
				String codelistCode = getCellData(cell);
				if (codelistCode == null || codelistCode.compareTo("null") == 0 || codelistCode.length() == 0 || codelistCode.compareTo("") == 0) {
					rowNumbers.add(Integer.valueOf(row_num));
				}
				row_num++;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		return rowNumbers;
	}

    public static boolean hightlightFirstRow(String excelfile) {
		int sheetIndex = 0;
		return hightlightFirstRow(excelfile, sheetIndex);
	}

    public static boolean hightlightFirstRow(String excelfile, int sheetIndex) {
		String outputfile = excelfile;
		return hightlightFirstRow(excelfile, sheetIndex, outputfile);
	}

    public static boolean hightlightFirstRow(String excelfile, String outputfile) {
	     int sheetIndex = 0;
	     return hightlightFirstRow(excelfile, sheetIndex, outputfile);
	}

    public static boolean hightlightFirstRow(String excelfile, int sheetIndex, String outputfile) {
		try {
			FileInputStream inputStream = new FileInputStream(new File(excelfile));
			HSSFWorkbook resultWorkbook = new HSSFWorkbook(inputStream);
			HSSFSheet resultSheet = resultWorkbook.getSheetAt(sheetIndex);

			resultSheet.setColumnWidth(0, 8 * 256);   //A
			resultSheet.setColumnWidth(1, 10 * 256);  //B
			resultSheet.setColumnWidth(2, 12 * 256);  //C
			resultSheet.setColumnWidth(3, 35 * 256);  //D
			resultSheet.setColumnWidth(4, 35 * 256);  //E
			resultSheet.setColumnWidth(5, 35 * 256);  //F
			resultSheet.setColumnWidth(6, 64 * 256);  //G
			resultSheet.setColumnWidth(7, 64 * 256);  //H

			HSSFRow sheetrow = resultSheet.getRow(0);
			Font font= resultWorkbook.createFont();
			font.setFontName("Arial");
			font.setBold(true);
			font.setItalic(false);
			font.setFontHeightInPoints((short) 12);

			HSSFCellStyle style = resultWorkbook.createCellStyle();
			style.setFont(font);

			style.setWrapText(true);   //Wrapping text
			style.setVerticalAlignment(VerticalAlignment.CENTER);
			style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
			style.setFillBackgroundColor(IndexedColors.YELLOW.getIndex());
			style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			style.setBorderTop(BorderStyle.THIN);
			style.setBorderBottom(BorderStyle.THIN);
			style.setBorderLeft(BorderStyle.THIN);
			style.setBorderRight(BorderStyle.THIN);
			style.setVerticalAlignment(VerticalAlignment.CENTER);
			style.setAlignment(HorizontalAlignment.CENTER);

			for(int i=0; i<sheetrow.getLastCellNum(); i++) {
			    sheetrow.getCell(i).setCellStyle(style);//Cell number
			}
			FileOutputStream outFile =new FileOutputStream(new File(outputfile));
			resultWorkbook.write(outFile);
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

	public static boolean assignSheetName(String xlsfile, int sheetIndex, String sheetName) {
		System.out.println(xlsfile);
		boolean status = false;
		FileOutputStream fileOut = null;
		try {
			InputStream inp = new FileInputStream(xlsfile);
			Workbook wb = WorkbookFactory.create(inp);
			wb.setSheetName(sheetIndex, sheetName);
			FileOutputStream outFile =new FileOutputStream(new File(xlsfile));
			wb.write(outFile);
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

	public static boolean reformat(String xlsfile, int sheetIndex, String sheetName) {
		boolean status = reformat(xlsfile, sheetIndex);
		if (sheetName == null) {
			String timestamp = StringUtils.getToday("yyyy-mm-dd");
			sheetName = sheetName + " " + timestamp;
		}
		status = assignSheetName(xlsfile, sheetIndex, sheetName);
		return status;
	}

	public static boolean reformat(String xlsfile, int sheetIndex) {
		String outputfile = xlsfile;
        hightlightFirstRow(xlsfile, 0, outputfile);
        short colorIndex = IndexedColors.LIGHT_TURQUOISE.getIndex();
	    boolean status = reformat(outputfile, sheetIndex, findCodeListRows(xlsfile), colorIndex, outputfile);
	    if (status) {
			status = setTextWrap(outputfile, sheetIndex, outputfile);
			System.out.println(outputfile + " generated.");
			return true;
		} else {
			System.out.println("Generation of " + outputfile + " is NOT successful.");
			return false;
		}
	}

	public static boolean setTextWrap(String xlsfile, int sheetIndex, String outputfile) {
		System.out.println(xlsfile);
		boolean status = false;
		FileOutputStream fileOut = null;
		try {
			InputStream inp = new FileInputStream(xlsfile);
			Workbook wb = WorkbookFactory.create(inp);
			Sheet sheet = wb.getSheetAt(sheetIndex);
			HSSFRow row;
			HSSFCell cell;

            int n = xlsfile.lastIndexOf(".");
			String sheetName = xlsfile.substring(0, n);
			sheetName = sheetName.replace("_", " ");
			String timestamp = StringUtils.getToday("yyyy-mm-dd");
			sheetName = sheetName + " " + timestamp;
			wb.setSheetName(sheetIndex, sheetName);

			Iterator rows = sheet.rowIterator();
			int row_num = 0;
			while (rows.hasNext()) {
				row = (HSSFRow) rows.next();
				row.setHeight((short)-1);
                CellStyle cellStyle = null;
				for(int i=0; i<row.getLastCellNum(); i++) {
                    cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
 				    cellStyle = cell.getCellStyle();
					cellStyle.setWrapText(true);
					cellStyle.setBorderTop(BorderStyle.THIN);
					cellStyle.setBorderBottom(BorderStyle.THIN);
					cellStyle.setBorderLeft(BorderStyle.THIN);
					cellStyle.setBorderRight(BorderStyle.THIN);
					cell.setCellStyle(cellStyle);
				}
			}
			fileOut = new FileOutputStream(outputfile);
			wb.write(fileOut);
			status = true;
			System.out.println(xlsfile + " is modified and saved as " + outputfile);

		} catch (Exception ex) {
			ex.printStackTrace();
			status = false;

		} finally {
			try {
				fileOut.close();
			} catch (Exception ex) {
				ex.printStackTrace();
				status = false;
			}
		}
		return status;
	}

	public static void copyfile(String src_file, String target_file) {
	    FileUtils.copyfile(src_file, target_file);
    }

	public static void main(String[] args) {
		String xlsfile = args[0];
		copyfile(xlsfile, "bak_" + xlsfile);
		int sheetIndex = 0;
		String sheetName = "ICHM11 Terminology " + StringUtils.getToday("yyyy-mm-dd");
		boolean status = reformat(xlsfile, sheetIndex, sheetName);
	}
}

