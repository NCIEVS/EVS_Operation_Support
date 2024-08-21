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

	public static void run(String[] args) throws IOException {
	    Workbook workbook = new XSSFWorkbook();
	    Sheet sheet = workbook.createSheet("Color Test");
	    Row row = sheet.createRow(0);

	    CellStyle style = workbook.createCellStyle();
	    style.setFillForegroundColor(IndexedColors.GREEN.getIndex());
	    Font font = workbook.createFont();
		font.setColor(IndexedColors.RED.getIndex());
		style.setFont(font);

	    Cell cell1 = row.createCell(0);
	    cell1.setCellValue("ID");
	    cell1.setCellStyle(style);

	    Cell cell2 = row.createCell(1);
	    cell2.setCellValue("NAME");
	    cell2.setCellStyle(style);

	    FileOutputStream fos =new FileOutputStream(new File("D:/xlsx/cp.xlsx"));
	    workbook.write(fos);
	    fos.close();
	    System.out.println("Done");
	}

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


/*
	private static String getCellData(Cell cell) {
		String value = null;
		if (cell == null) {
			return null;
		}
		switch (cell.getCellType()) {
			case HSSFCell.CELL_TYPE_STRING:
				value = cell.getStringCellValue();
				break;
			case HSSFCell.CELL_TYPE_FORMULA:
				value = cell.getCellFormula();
				break;
			case HSSFCell.CELL_TYPE_NUMERIC:
				HSSFDataFormatter dataFormatter = new HSSFDataFormatter();
				value = dataFormatter.formatCellValue(cell);
				break;
			case HSSFCell.CELL_TYPE_BLANK:
				value = null;
				break;
			case HSSFCell.CELL_TYPE_ERROR:
				value = "#ERROR#";
				break;
		}
		return value;
	}
*/

	public static String reformat(String xlsfile) {
		String outputfile = "modified_" + xlsfile;
		System.out.println(xlsfile);
		int sheetIndex = 0;
		boolean status = false;
		FileOutputStream fileOut = null;
		try {
			InputStream inp = new FileInputStream(xlsfile);
			Workbook wb = WorkbookFactory.create(inp);
			//HSSFSheet sheet = wb.getSheetAt(sheetIndex);
			Sheet sheet = wb.getSheetAt(sheetIndex);
			HSSFRow row;
			HSSFCell cell;
			sheet.setColumnWidth(0, 8 * 256);   //A
			sheet.setColumnWidth(1, 8 * 256);   //B
			sheet.setColumnWidth(2, 12 * 256);  //C
			sheet.setColumnWidth(3, 35 * 256);  //D
			sheet.setColumnWidth(4, 35 * 256);  //E
			sheet.setColumnWidth(5, 35 * 256);  //F
			sheet.setColumnWidth(6, 64 * 256);  //G
			sheet.setColumnWidth(7, 35 * 256);  //H

            int n = xlsfile.lastIndexOf(".");
			String sheetName = xlsfile.substring(0,n);
			sheetName = sheetName.replace("_", " ");
			sheetName = sheetName + " " + StringUtils.getToday();
			wb.setSheetName(sheetIndex, sheetName);

			Font font= wb.createFont();
			font.setFontName("Arial");
			font.setBold(false);
			font.setItalic(false);

			Iterator rows = sheet.rowIterator();
			int row_num = 0;
			while (rows.hasNext())
			{
				StringBuffer buf = new StringBuffer();
				row = (HSSFRow) rows.next();
				row.setHeight((short)-1);

				cell = row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
				boolean costlistRow = false;
				String costlistCode = getCellData(cell);

				if (costlistCode == null || costlistCode.compareTo("null") == 0 || costlistCode.compareTo("") == 0) {
					costlistRow = true;
				}

				for(int i=0; i<row.getLastCellNum(); i++) {
                    cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
 				    CellStyle cellStyle = cell.getCellStyle();
					cellStyle.setWrapText(true);   //Wrapping text
					if (row_num == 0) {
						cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
					} else {
						cellStyle.setVerticalAlignment(VerticalAlignment.TOP);
					}
					if (costlistRow) {
						cellStyle.setFillBackgroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
						cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
						//cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						//System.out.println("Row: " + row_num + " Col: " + i + " color: " + IndexedColors.LIGHT_TURQUOISE.getIndex());
						cellStyle.setFont(font);
				    }

					cellStyle.setBorderTop(BorderStyle.THIN);
					cellStyle.setBorderBottom(BorderStyle.THIN);
					cellStyle.setBorderLeft(BorderStyle.THIN);
					cellStyle.setBorderRight(BorderStyle.THIN);
					cell.setCellStyle(cellStyle);
				}
				row_num++;
			}
            outputfile = "modified_" + xlsfile;
			fileOut = new FileOutputStream(outputfile);
			wb.write(fileOut);
			status = true;
			System.out.println(xlsfile + " is modified and saved as " + outputfile);

		} catch (Exception ex) {
			ex.printStackTrace();

		} finally {
			try {
				fileOut.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}
}

