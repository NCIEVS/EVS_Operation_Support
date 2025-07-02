package gov.nih.nci.evs.restapi.test;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.config.*;
import gov.nih.nci.evs.restapi.bean.*;
import java.io.*;
import java.util.*;


public class ReportGeneratorTest {

    public static void test(String[] args) {
		Vector dataReq = new Vector();
		dataReq.add("P90|P383$PT|P384$NCI");
		dataReq.add("P90|P383$SY|P384$NCI");
		dataReq.add("P90|P383$PT|P384$SeroNet");
		dataReq.add("P97|P378$NCI");
		dataReq.add("A37");
        //(3) Instantiate ReportGenertor
		ReportGenerator test = new ReportGenerator(dataReq);
		Vector v = Utils.readFile("A37.txt");
		String prop_code = "A37";
		String source = "SeroNet";
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			System.out.println("\n" + line);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(0);
			String target = (String) u.elementAt(1);

			Vector w = test.getRelatedConceptCodes(code, prop_code);
			Utils.dumpVector("getRelatedConceptCodes "+ code,  w);
			String rel_codes = test.vector2Delimited(w, "|");
			System.out.println("rel_codes: " + rel_codes);

			w = test.getRelatedConceptSourcePTs(code, prop_code, source);
			Utils.dumpVector("getRelatedConceptSourcePTs "+ code, w);
			String rel_pts = test.vector2Delimited(w, "|");
			System.out.println("rel_pts: " + rel_pts);

			if (i == 10) break;
		}
	}

	public static void main(String[] args) {
		//(1) Optional step - explore template file.
		String templateFile = args[0];
		Vector w = ReportGenerator.exploreTemplateColumnLabels(templateFile);
		Utils.dumpVector(templateFile, w);
		//(2) Identify data requirements.
		Vector dataReq = new Vector();
		dataReq.add("P90|P383$PT|P384$NCI");
		dataReq.add("P90|P383$SY|P384$NCI");
		dataReq.add("P90|P383$PT|P384$SeroNet");
		dataReq.add("P97|P378$NCI");
		dataReq.add("A37");
        //(3) Instantiate ReportGenertor
		ReportGenerator test = new ReportGenerator(dataReq);
		TemplateLoader loader = new TemplateLoader();
		Template template = loader.load(templateFile);
		String rootConceptCode = template.getRootConceptCode();
		String root_ncipt = test.getPT(rootConceptCode, "NCI", "|");
        //(4) Execute ReportGenertor
		w = test.resolveValueSet(rootConceptCode);
		Vector v = new Vector();
		String header = test.generateReportHeader(templateFile);
		v.add(header);
		for (int i=0; i<w.size(); i++) {
			String code = (String) w.elementAt(i);
			String ncipt = test.getPT(code, "NCI", "|");
			String ncisy = test.getSY(code, "NCI", "|");
			String srcpt = test.getPT(code, "SeroNet", "|");
			String nciDef = test.getValues("P97|P378$NCI", code, "|");
			String rel_codes = test.vector2Delimited(test.getRelatedConceptCodes(code, "A37"), "|");
            String rel_pts = test.vector2Delimited(test.getRelatedConceptSourcePTs(code, "A37", "SeroNet"), "|");
			v.add(rootConceptCode + "\t" + root_ncipt + "\t" + code + "\t" + ncipt + "\t" + ncisy + "\t" + srcpt
			    + "\t" + nciDef + "\t" + rel_codes + "\t" + rel_pts);
		}
        //(5) Produce Excel Report
		String outputDir = "output";
		int n = templateFile.lastIndexOf(".");
		String outputfile = "test_" + templateFile.substring(0, n) + ".txt";
		ReportGenerator.saveData(outputDir, outputfile, v);

		String excelfile = "test_" + templateFile.substring(0, n) + ".xlsx";
		char delim = '\t';
		System.out.println("Generating excel file " + excelfile);
		test.generateExcel(outputDir, excelfile, delim);
	}
}
