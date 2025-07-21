package gov.nih.nci.evs.restapi.appl;
import gov.nih.nci.evs.restapi.config.*;
import gov.nih.nci.evs.restapi.util.*;
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
public class ApachePoiPieChartCSByOWL {
    static String NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile; //"ThesaurusInferred_forTS.owl";
	static String PARENT_CHILD_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile; // "parent_child.txt";
	static String RESTRICTION_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.rolefile; //"roles.txt";
	static String AXIOM_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.axiomfile;
	static OWLScanner scanner = null;
	static HashSet retired_concepts = null;
	static HashMap code2LabelMap = new HashMap();
	static HashMap contributingSourceMap = new HashMap();
	static Vector cs_data_vec = null;
	static String ncit_version = null;
	static {
		scanner = new OWLScanner(NCIT_OWL);
		ncit_version = get_ncit_version();
		//retired_concepts
		retired_concepts = new HashSet();
		String property_name = "Concept_Status";
		String property_value = "Retired_Concept";

		Vector w = scanner.extractPropertyData("P310");
		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String status = (String) u.elementAt(u.size()-1);
			if (status.compareTo(property_value) == 0) {
				String code = (String) u.elementAt(0);
				retired_concepts.add(code);
			}
		}
		System.out.println("Number of retired concepts: " + retired_concepts.size());

		String prop_code = "rdfs:label";
		w = scanner.extractPropertyData(prop_code);
		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(0);
			String label = (String) u.elementAt(1);
			label = HTMLDecoder.decode(label);
			code2LabelMap.put(code, label);
		}

		prop_code = "P322"; //Contributing_Source
		w = scanner.extractPropertyData(prop_code);
		Utils.saveToFile("cs_data_0.txt", w);

		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(0);
			Vector w1 = new Vector();
			if (contributingSourceMap.containsKey(code)) {
				w1 = (Vector) contributingSourceMap.get(code);
			}
			for (int j=1; j<u.size(); j++) {
				String cs = (String) u.elementAt(j);
				if (!w1.contains(cs)) {
					w1.add(cs);
				}
			}
			contributingSourceMap.put(code, w1);
		}
		cs_data_vec = createCSDataFile();
		Utils.saveToFile("cs_data.txt", cs_data_vec);
	}


//1,1-Dimethylhydrazine|C1072|Contributing_Source|FDA
    public static Vector createCSDataFile() {
		Vector w = new Vector();
		Iterator it = contributingSourceMap.keySet().iterator();
		while (it.hasNext()) {
			String code = (String) it.next();
			String label = (String) code2LabelMap.get(code);
			Vector w1 = (Vector) contributingSourceMap.get(code);
			for (int j=0; j<w1.size(); j++) {
				String cs = (String) w1.elementAt(j);
				w.add(label + "|" + code + "|Contributing_Source|" + cs);
			}
		}
		w = removedRetired(w);
		return new SortUtils().quickSort(w);
	}

//1,1-Dimethylhydrazine|C1072|Contributing_Source|FDA
	public static Vector removedRetired(Vector w) {
		Vector v = new Vector();
		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(1);
			if (!retired_concepts.contains(code)) {
				v.add(line);
			}
		}
		return v;
	}

	public static String get_ncit_version() {
		ncit_version = scanner.extractVersion();
		return ncit_version;
	}

	public static void generatePieChart(String version) {
		System.out.println("version: " + version);
		Vector w = cs_data_vec;

		HashMap hmap = new HashMap();
		HashMap concept_hmap = new HashMap();
		HashMap concept_count_hmap = new HashMap();
		for (int i=0; i<w.size(); i++) {
		    String line = (String) w.elementAt(i);
		    Vector u = StringUtils.parseData(line, '|');
		    //1, 25-Dihydroxyvitamin D Measurement|C92267|Contributing_Source|CDISC
		    String src = (String) u.elementAt(3);
		    String code = (String) u.elementAt(1);
		    Integer int_obj = Integer.valueOf(0);//new Integer(0);
		    if (hmap.containsKey(src)) {
				int_obj = (Integer) hmap.get(src);
			}
			//hmap.put(src, new Integer(Integer.valueOf(int_obj) + 1));
			hmap.put(src, Integer.valueOf(Integer.valueOf(int_obj) + 1));
		}

		Iterator it = hmap.keySet().iterator();
		Vector src_vec = new Vector();
		Vector count_vec = new Vector();
		while (it.hasNext()) {
			String src = (String) it.next();
			src_vec.add(src);
		}
		src_vec = new SortUtils().quickSort(src_vec);
		for (int i=0; i<src_vec.size(); i++) {
			String src = (String) src_vec.elementAt(i);
			Integer int_obj = (Integer) hmap.get(src);
			count_vec.add(int_obj);
		}
		try {
			pieChart(version, src_vec, count_vec);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static String formatVersion(String version) {
		String year = version.substring(0, 2);
		String month = version.substring(3, 5);
		year = "20" + year;
		if (month.charAt(0) == '0') {
			month = "" + month.charAt(1);
		}
		int month_number = Integer.parseInt(month);
		String month_name = new DateFormatSymbols().getMonths()[month_number-1];
		String s = month_name + " " + year;
		return s;
	}

	public static void pieChart(String version, Vector src_vec, Vector count_vec) throws FileNotFoundException, IOException {
		String month_year = formatVersion(version);
		try (XSSFWorkbook wb = new XSSFWorkbook()) {
			//XSSFSheet sheet = wb.createSheet("NCIt Concept Count By Contributing Sources");
			//XSSFSheet sheet = wb.createSheet("NCIt Concept Count By Contributing Sources");
			XSSFSheet sheet = wb.createSheet("NCIt Concept Count By Sources");

            Row row = sheet.createRow((short) 0);
            Cell cell = row.createCell((short) 0);
            cell.setCellValue("Contributing Source");
            cell = row.createCell((short) 1);
            cell.setCellValue("Concept Count");
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
			chart.setTitleText("NCIt Concepts from various contributing sources (" + version + " " + month_year +")");
			chart.setTitleOverlay(false);

			XDDFChartLegend legend = chart.getOrAddLegend();
			//legend.setPosition(LegendPosition.TOP_RIGHT);
			legend.setPosition(LegendPosition.BOTTOM);

			XDDFDataSource<String> sources = XDDFDataSourcesFactory.fromStringCellRange(sheet,
					new CellRangeAddress(1, src_vec.size(), 0, 0));

			XDDFNumericalDataSource<Double> values = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
					new CellRangeAddress(1, count_vec.size(), 1, 1));

			//XDDFChartData data = chart.createData(ChartTypes.PIE3D, null, null);
			XDDFChartData data = chart.createData(ChartTypes.PIE, null, null);

			data.setVaryColors(true);
			data.addSeries(sources, values);
			chart.plot(data);

			try (FileOutputStream fileOut = new FileOutputStream("NCIT_Concept_Stats_By_Contributing_Source.xlsx")) {
				wb.write(fileOut);
			}
		}
	}

	public static void main(String[] args) {
		generatePieChart(ApachePoiPieChartCSByOWL.ncit_version);
	}
}

