package gov.nih.nci.evs.restapi.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

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
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

public class XLSX2XLS {
	private static String getCellData(Cell cell) {
		try {
			return cell.getStringCellValue();
		} catch (Exception ex) {
			HSSFDataFormatter dataFormatter = new HSSFDataFormatter();
			String value = dataFormatter.formatCellValue(cell);
			return value;
		}
	}
    public static void run(String inpFn, String outFn) throws InvalidFormatException, IOException {
        InputStream in = new FileInputStream(inpFn);
        try {
            XSSFWorkbook wbIn = new XSSFWorkbook(in);
            File outF = new File(outFn);
            if (outF.exists()) {
                outF.delete();
            }

            Workbook wbOut = new HSSFWorkbook();
            int sheetCnt = wbIn.getNumberOfSheets();
            for (int i = 0; i < sheetCnt; i++) {
                Sheet sIn = wbIn.getSheetAt(i);
                Sheet sOut = wbOut.createSheet(sIn.getSheetName());
                System.out.println("Converting sheet " + sIn.getSheetName() + " ...");

                Iterator<Row> rowIt = sIn.rowIterator();
                while (rowIt.hasNext()) {
                    Row rowIn = rowIt.next();
                    Row rowOut = sOut.createRow(rowIn.getRowNum());

                    Iterator<Cell> cellIt = rowIn.cellIterator();
                    while (cellIt.hasNext()) {
                        Cell cellIn = cellIt.next();
                        //Cell cellOut = rowOut.createCell(cellIn.getColumnIndex(), cellIn.getCellType());
                        Cell cellOut = rowOut.createCell(cellIn.getColumnIndex(), CellType.STRING);
/*
                        switch (cellIn.getCellType()) {
                        case Cell.CELL_TYPE_BLANK: break;

                        case Cell.CELL_TYPE_BOOLEAN:
                            cellOut.setCellValue(cellIn.getBooleanCellValue());
                            break;

                        case Cell.CELL_TYPE_ERROR:
                            cellOut.setCellValue(cellIn.getErrorCellValue());
                            break;

                        case Cell.CELL_TYPE_FORMULA:
                            cellOut.setCellFormula(cellIn.getCellFormula());
                            break;

                        case Cell.CELL_TYPE_NUMERIC:
                            cellOut.setCellValue(cellIn.getNumericCellValue());
                            break;

                        case Cell.CELL_TYPE_STRING:
                            cellOut.setCellValue(cellIn.getStringCellValue());
                            break;
                        }
*/
						cellOut.setCellValue(getCellData(cellIn));
                        {
                            CellStyle styleIn = cellIn.getCellStyle();
                            CellStyle styleOut = cellOut.getCellStyle();
                            styleOut.setDataFormat(styleIn.getDataFormat());
                        }
                            cellOut.setCellComment(cellIn.getCellComment());

                        }
                }
            }
            OutputStream out = new BufferedOutputStream(new FileOutputStream(outF));
            try {
                wbOut.write(out);
                System.out.println(outFn + " generated.");
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }
}
