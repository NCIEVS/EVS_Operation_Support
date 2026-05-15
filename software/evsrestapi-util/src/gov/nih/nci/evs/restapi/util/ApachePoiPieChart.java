package gov.nih.nci.evs.restapi.util;

import java.io.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.*;
import java.util.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.LegendPosition;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFChartLegend;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.openxmlformats.schemas.drawingml.x2006.chart.*;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPlotArea;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBoolean;

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
public class ApachePoiPieChart {

	public static void pieChart(String excelfile, String chartTitle, String sheetLabel, String xLabel, String yLabel, Vector src_vec, Vector count_vec) throws FileNotFoundException, IOException {
		try (XSSFWorkbook wb = new XSSFWorkbook()) {
			XSSFSheet sheet = wb.createSheet(sheetLabel);

            Row row = sheet.createRow((short) 0);
            Cell cell = row.createCell((short) 0);
            cell.setCellValue(xLabel);
            cell = row.createCell((short) 1);
            cell.setCellValue(yLabel);
			for (int i=0; i<src_vec.size(); i++) {
				int j = i+1;
				row = sheet.createRow((short) j);
				cell = row.createCell((short) 0);
				String src = (String) src_vec.elementAt(i);
				cell.setCellValue(src);
				cell = row.createCell((short) 1);
				int count = (Integer) count_vec.elementAt(i);
				cell.setCellValue(Integer.valueOf(count));
			}

			XSSFDrawing drawing = sheet.createDrawingPatriarch();

			XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, src_vec.size()+1, 20, src_vec.size()+100);

			XSSFChart chart = drawing.createChart(anchor);
			chart.setTitleText(chartTitle);
			chart.setTitleOverlay(false);

			XDDFChartLegend legend = chart.getOrAddLegend();
			legend.setPosition(LegendPosition.BOTTOM);

			XDDFDataSource<String> sources = XDDFDataSourcesFactory.fromStringCellRange(sheet,
					new CellRangeAddress(1, src_vec.size(), 0, 0));

			XDDFNumericalDataSource<Double> values = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
					new CellRangeAddress(1, count_vec.size(), 1, 1));

			XDDFChartData data = chart.createData(ChartTypes.PIE, null, null);

			data.setVaryColors(true);
			data.addSeries(sources, values);

			CTPlotArea plotArea = chart.getCTChart().getPlotArea();
			if (!plotArea.getPieChartList().isEmpty()) {
				CTPieChart pieChart = plotArea.getPieChartArray(0);
				CTPieSer series = pieChart.getSerArray(0);
				CTDLbls labels = series.addNewDLbls();
				labels.addNewShowSerName().setVal(false);//Don't show series name
						labels.addNewShowVal().setVal(true);//show X value
						labels.addNewShowCatName().setVal(true);//show Y value
						labels.addNewShowPercent().setVal(false);
			}

			chart.plot(data);
			try (FileOutputStream fileOut = new FileOutputStream(excelfile)) {
				wb.write(fileOut);
				System.out.println(excelfile +  " generated.");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}






