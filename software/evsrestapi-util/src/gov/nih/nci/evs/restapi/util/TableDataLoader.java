package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.bean.*;

import java.io.*;
import java.net.URI;
import java.text.*;
import java.util.*;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;

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


public class TableDataLoader {

	 public static void dumpTableData(HashMap map) {
		 String title = (String) map.get("title");
		 System.out.println(title);
		 Vector table_label_vec = (Vector) map.get("table_label_vec");
		 for (int i=0; i<table_label_vec.size(); i++) {
			 String table_label = (String) table_label_vec.elementAt(i);
			 System.out.println(table_label);
             HashMap hmap = (HashMap) map.get(table_label);
             Vector th_vec = (Vector) hmap.get("th_vec");
             HashMap data_map = (HashMap) hmap.get("data_hmap");
             if (data_map != null) {
				 Vector data_map_key_vec = (Vector) hmap.get("data_map_key_vec");
				 for (int k=0; k<data_map_key_vec.size(); k++) {
					 String key = (String) data_map_key_vec.get(k);
					 String value = (String) data_map.get(key);
					 System.out.println(value);
				 }
			 }
		 }
		 String footer = (String) map.get("footer");
		 System.out.println(footer);
	 }

	 public static void saveTableData(HashMap map, String outputfile) {
		 Vector w = new Vector();
		 String title = (String) map.get("title");
		 w.add("<title>" + title);
		 Vector table_label_vec = (Vector) map.get("table_label_vec");
		 for (int i=0; i<table_label_vec.size(); i++) {
			 String table_label = (String) table_label_vec.elementAt(i);
			 w.add("<table>" + table_label);
             HashMap hmap = (HashMap) map.get(table_label);
             Vector th_vec = (Vector) hmap.get("th_vec");
             for (int j=0; j<th_vec.size(); j++) {
				 String th = (String) th_vec.elementAt(j);
				 w.add("<th>" + th);
			 }
			 w.add("<data>");
             HashMap data_map = (HashMap) hmap.get("data_hmap");
             if (data_map != null) {
				 Vector data_map_key_vec = (Vector) hmap.get("data_map_key_vec");
				 for (int k=0; k<data_map_key_vec.size(); k++) {
					 String key = (String) data_map_key_vec.get(k);
					 String value = (String) data_map.get(key);
					 w.add(value);
				 }
			 }
			 w.add("</data>");
			 w.add("</table>");
		 }
		 String footer = (String) map.get("footer");
		 w.add("<footer>" + footer);
		 Utils.saveToFile(outputfile, w);
	 }

	 public static HashMap loadTableData(String filename) {
		 HashMap map = new HashMap();
		 HashMap hmap = new HashMap();
		 HashMap data_hmap = new HashMap();
		 Vector th_vec = new Vector();
		 Vector v = Utils.readFile(filename);
		 int i = -1;
		 String table_label = null;
		 Vector table_label_vec = new Vector();
		 Vector data_map_key_vec = new Vector();
		 while (i < v.size()) {
			 i++;
			 String line = (String) v.elementAt(i);
			 if (line.startsWith("<title>")) {
				 int n = line.indexOf(">");
				 String title = line.substring(n+1, line.length());
			     map.put("title", title);
			 } else if (line.startsWith("<footer>")) {
				 int n = line.indexOf(">");
				 String footer = line.substring(n+1, line.length());
			     map.put("footer", footer);
			 } else if (line.startsWith("<table>")) {
				 hmap = new HashMap();
				 th_vec = new Vector();
				 int n = line.indexOf(">");
				 table_label = line.substring(n+1, line.length());
				 table_label_vec.add(table_label);
			 } else if (line.startsWith("</table>")) {
				 map.put(table_label, hmap);
			 } else if (line.startsWith("</data>")) {

			 } else if (line.startsWith("<th>")) {
				 int n = line.indexOf(">");
				 String th = line.substring(n+1, line.length());
				 th_vec.add(th);
			 } else if (line.startsWith("</th>")) {

			 } else if (line.startsWith("<data>")) {
				 hmap.put("th_vec", th_vec);
				 i++;
				 data_hmap = new HashMap();
				 data_map_key_vec = new Vector();
				 boolean flag = true;
				 while (flag) {
					 line = (String) v.elementAt(i);
					 if (line.compareTo("</data>") == 0) {
						 hmap.put("data_map_key_vec", data_map_key_vec);
						 hmap.put("data_hmap", data_hmap);
						 flag = false;
						 break;
					 }
					 Vector u = StringUtils.parseData(line, '|');
					 String key = (String) u.elementAt(0) + "|" + (String) u.elementAt(1);
					 data_map_key_vec.add(key);
					 data_hmap.put(key, line);
					 i++;
				 }
			 }
			 if (i == v.size()-1) break;
		 }
		 map.put("table_label_vec", table_label_vec);
		 return map;
	 }

	 public static void qa(String filename) {
		 String outputfile = "bak_" + filename;
		 Vector v0 = Utils.readFile(filename);
		 Vector v1 = Utils.readFile(outputfile);

		 int n0 = v0.size();
		 int n1 = v1.size();

		 System.out.println("v0: " + n0);
		 System.out.println("v1: " + n1);

		 int n = n0;
		 if (n1 < n0) {
			 n = n1;
		 }
		 System.out.println("n: " + n);
		 for (int i=0; i<n; i++) {
			 String t0 = (String) v0.elementAt(i);
			 String t1 = (String) v1.elementAt(i);
			 if (t0.compareTo(t1) != 0) {
				 System.out.println(filename + " (" + i + ") " + t0);
				 System.out.println(outputfile + " (" + i + ") " + t1);
				 break;
			 }
		 }
	 }

	 public static void tabledata2HTML(String datafile) {
		Vector v = Utils.readFile(datafile);
		String htmlfile = new HTMLTable().generate(v, '|');
		System.out.println(htmlfile + " generated.");
	 }

     public static void tableData2Excel(String datafile) {
		int n = datafile.indexOf(".");
		String xlsxfile = datafile.substring(0, n) + ".xlsx";
		Vector v = Utils.readFile(datafile);
		int rowNum = 0;
		int colNum = 0;
		boolean th_start = false;
		org.apache.poi.ss.usermodel.Row row = null;
        try (Workbook workbook = new XSSFWorkbook()) {
            HashMap map = loadTableData(datafile);
            String title = (String) map.get("title");
            Sheet sheet = workbook.createSheet(title);
			row = sheet.createRow(rowNum);
			Cell cell = row.createCell(colNum);
            cell.setCellValue(title);
			CellStyle cellStyle = workbook.createCellStyle();
			cellStyle.setAlignment(HorizontalAlignment.CENTER);
			cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			cellStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
			cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			cell.setCellStyle(cellStyle);

			CellRangeAddress mergedRegion = new CellRangeAddress(rowNum, rowNum, colNum, colNum+7);
			sheet.addMergedRegion(mergedRegion);
			colNum = 0;
			Vector table_label_vec = (Vector) map.get("table_label_vec");
			for (int i=0; i<table_label_vec.size(); i++) {
				rowNum++;
				rowNum++;
				String table_label = (String) table_label_vec.elementAt(i);
				HashMap hmap = (HashMap) map.get(table_label);
				Vector th_vec = (Vector) hmap.get("th_vec");
				row = sheet.createRow(rowNum);
				cell = row.createCell(colNum);
				String table = table_label;
				cell.setCellValue(table);

				cellStyle = workbook.createCellStyle();
				cellStyle.setAlignment(HorizontalAlignment.CENTER);
				cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

				cellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
				cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				cell.setCellStyle(cellStyle);
				mergedRegion = new CellRangeAddress(rowNum, rowNum, colNum, colNum+7);//th_vec.size());
				sheet.addMergedRegion(mergedRegion);
				rowNum++;
				row = sheet.createRow(rowNum);
				for (int j=0; j<th_vec.size(); j++) {
					String th = (String) th_vec.elementAt(j);
					cell = row.createCell(j); // Column B (0-indexed)
					cell.setCellValue(th);
					cellStyle = workbook.createCellStyle();
					cellStyle.setAlignment(HorizontalAlignment.LEFT);
					cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
					cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
					cell.setCellStyle(cellStyle);
				}
				Vector data_map_key_vec = (Vector) hmap.get("data_map_key_vec");
				HashMap data_hmap = (HashMap) hmap.get("data_hmap");
				for (int j=0; j<data_map_key_vec.size(); j++) {
					String data_map_key = (String) data_map_key_vec.elementAt(j);
					String value = (String) data_hmap.get(data_map_key);
					Vector u = StringUtils.parseData(value, '|');
					rowNum++;
					row = sheet.createRow(rowNum);
					for (int k=0; k<u.size(); k++) {
						String t = (String) u.elementAt(k);
						cell = row.createCell(k); // Column B (0-indexed)
						cell.setCellValue(t);
						cellStyle = workbook.createCellStyle();
						cellStyle.setAlignment(HorizontalAlignment.LEFT);
						cell.setCellStyle(cellStyle);
					}
				}
			}
			try (FileOutputStream fileOut = new FileOutputStream(xlsxfile)) {
				workbook.write(fileOut);
				System.out.println("Excel file created successfully.");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
     }


	 public static void main(String[] args) {
		 String filename = args[0];
		 HashMap map = loadTableData(filename);
		 String outputfile = "v2_" + filename;
		 saveTableData(map, outputfile);
		 //qa(filename);
		 //tabledata2HTML(outputfile);
		 tableData2Excel(outputfile);
	 }
 }

