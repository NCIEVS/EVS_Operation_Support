package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;

import java.io.*;
import java.util.*;

public class ReportDataGenerator {

    public static String REPORT_GENERATION_DIRECTORY = ConfigurationController.reportGenerationDirectory;
	public static String HIER_FILE = REPORT_GENERATION_DIRECTORY + File.separator + ConfigurationController.hierfile;
	public static String ROLE_FILE = REPORT_GENERATION_DIRECTORY + File.separator + ConfigurationController.rolefile;
	public static String PROPERTY_FILE = REPORT_GENERATION_DIRECTORY + File.separator + ConfigurationController.propertyfile;
	public static String NCIT_OWL_FILE = REPORT_GENERATION_DIRECTORY + File.separator + ConfigurationController.owlfile;
	public static String AXIOM_FILE = REPORT_GENERATION_DIRECTORY + File.separator + ConfigurationController.axiomfile;
	public static String SUBSET_FILE = REPORT_GENERATION_DIRECTORY + File.separator + ConfigurationController.subsetfile;
	public static String LABEL_FILE = REPORT_GENERATION_DIRECTORY + File.separator + "label.txt";

	static OWLScanner owlscanner = null;
	public static HashMap date2NamedGraphMap = null;

	static {
		date2NamedGraphMap = new HashMap();
		int currentYear = DateUtils.getCurrentYear();
		int currentMonth = DateUtils.getCurrentMonth();
        Vector w = DateUtils.getNCItReleaseSchedule(currentYear, false);
        for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			date2NamedGraphMap.put((String) u.elementAt(4), (String) u.elementAt(3));
		}
	}

	public static String reformatDate(String dateStr) {
		// from yyyy-MM-dd to MM/dd/yyyy
		Vector v = StringUtils.parseData(dateStr, '-');
		return (String) v.elementAt(1) + "/" + (String) v.elementAt(2) + "/" + (String) v.elementAt(0);
	}

	public static String getNamedGraphForDate(String dateStr) {
		return (String) date2NamedGraphMap.get(dateStr);
	}

    public static void generateLabelFile() {
		Vector v = Utils.readFile(HIER_FILE);
		Vector w = new Vector();
		HashSet hset = new HashSet();
		//EDQM Health Care Terminology|C148636|EDQM-HC Administrable Dose Form Terminology|C175439
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String s0 = (String) u.elementAt(0);
			String s1 = (String) u.elementAt(1);
			String s2 = (String) u.elementAt(2);
			String s3 = (String) u.elementAt(3);
			if (!hset.contains(s0 + "|" + s1)) {
				hset.add(s0 + "|" + s1);
				w.add(s0 + "|" + s1);
			}
			if (!hset.contains(s2 + "|" + s3)) {
				hset.add(s2 + "|" + s3);
				w.add(s2 + "|" + s3);
			}
		}
		w = new SortUtils().quickSort(w);
		Utils.saveToFile("label.txt", w);
	}

    public static void run() {
		long ms = System.currentTimeMillis();
		String currentWorkingDirectory = System.getProperty("user.dir");
		String url = NCItDownload.NCIt_URI + NCItDownload.NCIT_ZIP_FILE;
		String zipFilePath = REPORT_GENERATION_DIRECTORY + File.separator + NCItDownload.NCIT_ZIP_FILE;

		try {
			NCItDownload.download(url, zipFilePath);
			NCItDownload.unzip(zipFilePath, REPORT_GENERATION_DIRECTORY);
		} catch (Exception e) {
			e.printStackTrace();
		}

		OWLScanner owlscanner = new OWLScanner(NCIT_OWL_FILE);
		Vector w = owlscanner.extractAxiomData(null);
		Utils.saveToFile(AXIOM_FILE, w);
		HTMLDecoder.run(AXIOM_FILE);

		w = owlscanner.extractHierarchicalRelationships(owlscanner.get_owl_vec());
		Utils.saveToFile(HIER_FILE, w);
		HTMLDecoder.run(HIER_FILE);

		w = owlscanner.extractOWLRestrictions(owlscanner.get_owl_vec());
		Utils.saveToFile(ROLE_FILE, w);

		w = owlscanner.extractAssociations(owlscanner.get_owl_vec(), "A8");
		Utils.saveToFile(SUBSET_FILE, w);

		w = owlscanner.extractProperties(owlscanner.get_owl_vec());
		Utils.saveToFile(PROPERTY_FILE, w);
		HTMLDecoder.run(PROPERTY_FILE);

		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}

	public static void main(String[] args) {
		run();
	}
}

