package gov.nih.nci.evs.restapi.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Map;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.apache.poi.ss.usermodel.DataFormatter;

public class ExcelParser {
	Vector<String> headers = new Vector<String>();

	public ExcelParser() {

	}

    public static boolean isFloat(String str) {
		try {
			if (str.indexOf(".") == -1) return false;
			Float.parseFloat(str);
		} catch (Exception ex) {
			return false;
		}
		return true;
	}

	public static String formatFloat(String str) {
		double d = Double.parseDouble(str);
		return String.format("%.1f", d);
	}

	private TreeMap<String, String> parseRow(XSSFRow row) {
		XSSFCell cell;
		Iterator cells = row.cellIterator();
		TreeMap<String, String> cellValues = new TreeMap<String, String>();
		Iterator<String> headerIt = headers.iterator();
		while (cells.hasNext()) {
			cell = (XSSFCell) cells.next();
			cell.setCellType(CellType.STRING);
			String header = headerIt.next();
			String cellValue = cell.getStringCellValue();
			if (isFloat(cellValue)) {
				cellValue = formatFloat(cellValue);
			}
			cellValues.put(header, cellValue);
		}
		return cellValues;
	}

	private void parseHeader(XSSFRow headerRow) {
		// TODO Auto-generated method stub
		int i = 0;
		Iterator cells = headerRow.cellIterator();
		while (cells.hasNext()) {
			XSSFCell cell = (XSSFCell) cells.next();
			cell.setCellType(CellType.STRING);
			if (cell.getStringCellValue() != null) {
				headers.add(cell.getStringCellValue());
				i++;
			}
		}
	}


    public TreeMap parse(File file, int sheetNumber, String keyLabel) throws Exception {
		TreeMap<String, TreeMap<String, String>> dataMap = new TreeMap<String, TreeMap<String, String>>();
		try {
			InputStream inputStream = new FileInputStream(file);
			XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
			XSSFSheet sheet = workbook.getSheetAt(sheetNumber);
			System.out.println("Parsing sheet: " + workbook.getSheetName(sheetNumber));
			Iterator rows = sheet.rowIterator();
			XSSFRow row;

			// take the header row
			parseHeader((XSSFRow) rows.next());

			while (rows.hasNext()) {
				TreeMap<String, String> singleRow = parseRow((XSSFRow) rows.next());
				String rowName = singleRow.get(keyLabel);
				dataMap.put(rowName, singleRow);
			}

			inputStream.close();
			workbook.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		return dataMap;
	}

	public static void dumpTreeMap(TreeMap treeMap) {
        Iterator it = treeMap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			TreeMap map = (TreeMap) treeMap.get(key);
			Iterator it2 = map.keySet().iterator();
			while (it2.hasNext()) {
				String colName = (String) it2.next();
                String colValue = (String) map.get(colName);
			    System.out.println(colName + " --> " + colValue);
			}
		}
	}

    public static void main(String[] args) {
		String excelfile = args[0];
		String sheetNumberStr = args[1];
		int sheetNumber = Integer.parseInt(sheetNumberStr);
		String keyColLabel = args[2];
        ExcelParser parser = new ExcelParser();
        try {
			TreeMap treeMap = parser.parse(new File(excelfile), sheetNumber, keyColLabel);
			dumpTreeMap(treeMap);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
