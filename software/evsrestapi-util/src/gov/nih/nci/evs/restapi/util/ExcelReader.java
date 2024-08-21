package gov.nih.nci.evs.restapi.util;
import java.io.*;
import java.io.File;
import java.text.*;
import java.util.*;
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
public class ExcelReader {
	public static DataFormatter dataFormatter = new DataFormatter();

    public static Vector csv2Text(String xlsxfile, String sheetName) throws Exception {
		DataFormatter dataFormatter  = new DataFormatter();
		File file = new File(xlsxfile);
        FileInputStream ip = new FileInputStream(file);
        Workbook wb = WorkbookFactory.create(ip);
        Sheet sheet = wb.getSheet(sheetName);
		Vector w = new Vector();
        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Iterator<Cell> cellIterator = row.cellIterator();
            StringBuffer buf = new StringBuffer();
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                String cellValue = dataFormatter.formatCellValue(cell);
                buf.append(cellValue + "\t");
            }
            String t = buf.toString();
            t = t.substring(0, t.length()-1);
            w.add(t);
        }
        ip.close();
        return w;
    }

    public static int getNumberOfSheets(String excelfile) {
		try {
			Workbook workbook = WorkbookFactory.create(new File(excelfile));
			return workbook.getNumberOfSheets();
		} catch (Exception ex) {
			//return -1;
		}
		return -1;
	}

    public static Workbook openWorkbook(String excelfile) {
		try {
			Workbook workbook = WorkbookFactory.create(new File(excelfile));
			return workbook;
		} catch (Exception ex) {
			//return -1;
		}
		return null;
	}

    public static int getNumberOfSheets(Workbook workbook) {
        return workbook.getNumberOfSheets();
	}

    public static Vector getSheetNames(String excelfile) {
		try {
			Workbook workbook = WorkbookFactory.create(new File(excelfile));
			return getSheetNames(workbook);
		} catch (Exception ex) {
			//return -1;
		}
		return null;
	}


    public static Vector getSheetNames(Workbook workbook) {
		Vector<String> v = new Vector();
        Iterator<Sheet> sheetIterator = workbook.sheetIterator();
        System.out.println("Retrieving Sheets using Iterator");
        while (sheetIterator.hasNext()) {
            Sheet sheet = sheetIterator.next();
            v.add(sheet.getSheetName());
        }
        return v;
	}

    public Sheet getSheet(Workbook workbook, int sheetNumber) {
		return workbook.getSheetAt(sheetNumber);
	}


    public Sheet getSheet(String excelfile, int sheetNumber) {
		try {
		     Workbook workbook = openWorkbook(excelfile);
 		     return getSheet(workbook, sheetNumber);
		} catch (Exception e) {

		}
		return null;
	}

    public static Vector toDelimited(String excelfile, char delim) {
		return toDelimited(excelfile, 0, delim);
	}


    private static void printCellValue(Cell cell) {
        //switch (cell.getCellTypeEnum()) {
		switch (cell.getCellType()) {
            case BOOLEAN:
                System.out.print(cell.getBooleanCellValue());
                break;
            case STRING:
                System.out.print(cell.getRichStringCellValue().getString());
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    System.out.print(cell.getDateCellValue());
                } else {
                    System.out.print(cell.getNumericCellValue());
                }
                break;
            case FORMULA:
                System.out.print(cell.getCellFormula());
                break;
            case BLANK:
                System.out.print("");
                break;
            default:
                System.out.print("");
        }
        System.out.print("\t");
    }


    public static Vector toDelimited(String excelfile, int sheetNumber, char delim) {
		Vector w = new Vector();
		Workbook workbook = openWorkbook(excelfile);
        Sheet sheet = workbook.getSheetAt(sheetNumber);
        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
			StringBuffer buf = new StringBuffer();
			Row row = rowIterator.next();
            List list = getRowData(row);
            for (int i=0; i<list.size(); i++) {
				String t = (String) list.get(i);
				buf.append(t).append(delim);
			}
			String s = buf.toString();
			s = s.substring(0, s.length()-1);
			w.add(s);
		}
        try {
        	workbook.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
        return w;
	}

/*
    public static List getRowData(Row row) {
		List list = new ArrayList();
		for (int i=0; i<row.getPhysicalNumberOfCells(); i++) {
			Cell cell=row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK );
			cell.setCellType(Cell.CELL_TYPE_STRING);
			list.add(cell.getStringCellValue());
		}
		return list;
	}
*/

    public static List getRowData(Row row) {
		DataFormatter fmt = new DataFormatter();
		List fetchedRow = new ArrayList();
		//List list = new ArrayList();
		for (int i=0; i<row.getPhysicalNumberOfCells(); i++) {
			Cell cell=row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK );
            boolean rowEmpty = true;
			switch (cell.getCellType()) {
				case NUMERIC:
					if (DateUtil.isCellDateFormatted(cell)) {
						Date date = cell.getDateCellValue();
						DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
						//DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
						fetchedRow.add(dateFormat.format(date));
					} else {
						fetchedRow.add(fmt.formatCellValue(cell));
					}
					rowEmpty = false;
					break;
				case STRING:
					//fetchedRow.add(cell.toString());
                    fetchedRow.add(fmt.formatCellValue(cell));
					rowEmpty = false;
					break;
				case BOOLEAN:
					fetchedRow.add(cell.toString());
					rowEmpty = false;
					break;
				case FORMULA:
					fetchedRow.add(Double.toString(cell.getNumericCellValue()));
					rowEmpty = false;
					break;
				case BLANK:
					//fetchedRow.add("");
					fetchedRow.add(fmt.formatCellValue(cell));
					rowEmpty = false;
					break;
				case ERROR:
					//fetchedRow.add("");
					fetchedRow.add(fmt.formatCellValue(cell));
					rowEmpty = false;
					break;
				default:
					//fetchedRow.add("");
					fetchedRow.add(fmt.formatCellValue(cell));
					rowEmpty = false;
					break;
			}
		}
		return fetchedRow;
	}

}
