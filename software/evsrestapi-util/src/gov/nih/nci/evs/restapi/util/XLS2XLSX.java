package gov.nih.nci.evs.restapi.util;

import java.io.*;
import java.util.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFCreationHelper;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFHyperlink;
import org.apache.poi.hssf.usermodel.HSSFHyperlink;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFPicture;
import org.apache.poi.hssf.usermodel.HSSFPictureData;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFShape;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;

public class XLS2XLSX {

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


    public static void run(String inpFn, String outFn) {
        InputStream in = null;

        try {
			in = new BufferedInputStream(new FileInputStream(inpFn));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
        try {
            Workbook wbIn = new HSSFWorkbook(in);
            File outF = new File(outFn);
            if (outF.exists())
                outF.delete();

            Workbook wbOut = new XSSFWorkbook();
            int sheetCnt = wbIn.getNumberOfSheets();
            for (int i = 0; i < sheetCnt; i++) {
                Sheet sIn = wbIn.getSheetAt(0);
                Sheet sOut = wbOut.createSheet(sIn.getSheetName());
                Iterator<Row> rowIt = sIn.rowIterator();
                while (rowIt.hasNext()) {
                    Row rowIn = rowIt.next();
                    Row rowOut = sOut.createRow(rowIn.getRowNum());

                    Iterator<Cell> cellIt = rowIn.cellIterator();
                    while (cellIt.hasNext()) {
                        Cell cellIn = cellIt.next();
                        Cell cellOut = rowOut.createCell(
                                cellIn.getColumnIndex(), cellIn.getCellType());

                        String data = getCellData(cellIn);
                        cellOut.setCellValue(data);
						CellStyle styleIn = cellIn.getCellStyle();
						CellStyle styleOut = cellOut.getCellStyle();
						styleOut.setDataFormat(styleIn.getDataFormat());
                         cellOut.setCellComment(cellIn.getCellComment());
                    }
                }
            }
            OutputStream out = new BufferedOutputStream(new FileOutputStream(outF));
            try {
                wbOut.write(out);
            } finally {
                out.close();
            }
        } catch (FileNotFoundException ex) {
			ex.printStackTrace();
        } catch (Exception ex) {
			ex.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}