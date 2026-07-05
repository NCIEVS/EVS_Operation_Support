package gov.nih.nci.evs.restapi.appl;
import gov.nih.nci.evs.restapi.util.*;

import java.util.*;

public class GDCSetUp {
    static String GDC_CONFIGURATION_FILE = "gdc.config";

    public static void run() {
		long ms = System.currentTimeMillis();
		String versions = ReportSubmitter.searchForVersions();
		Vector u = StringUtils.parseData(versions, '|');
		String oldVersion = (String) u.elementAt(0);
		String newVersion = (String) u.elementAt(1);
		String oldValueFile = ReportSubmitter.searchXLSFile("Value", oldVersion);
		String newValueFile = ReportSubmitter.searchXLSFile("Value", newVersion);
		String oldPropertyFile = ReportSubmitter.searchXLSFile("Property", oldVersion);
		String newPropertyFile = ReportSubmitter.searchXLSFile("Property", newVersion);

		System.out.println("oldValueFile: " + oldValueFile);
		System.out.println("newValueFile: " + newValueFile);
		System.out.println("oldPropertyFile: " + oldPropertyFile);
		System.out.println("newPropertyFile: " + newPropertyFile);

		ExcelExporter.run(oldValueFile);
		ExcelExporter.run(newValueFile);
		ExcelExporter.run(oldPropertyFile);
		ExcelExporter.run(newPropertyFile);

		oldValueFile = oldValueFile.replace(".xls", ".txt");
		newValueFile = newValueFile.replace(".xls", ".txt");
		oldPropertyFile = oldPropertyFile.replace(".xls", ".txt");
		newPropertyFile = newPropertyFile.replace(".xls", ".txt");

		Vector w = new Vector();
		w.add(oldPropertyFile);
		w.add(newPropertyFile);
		w.add(oldValueFile);
		w.add(newValueFile);
		Utils.saveToFile(GDC_CONFIGURATION_FILE, w);
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}

	public static void main(String args[]) {
		run();
	}
}