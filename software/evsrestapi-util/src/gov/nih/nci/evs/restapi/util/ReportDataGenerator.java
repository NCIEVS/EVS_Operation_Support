package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.appl.*;

import java.io.*;
import java.util.*;

public class ReportDataGenerator {

	public static String NCIT_OWL_FILE = "ThesaurusInferred_forTS.owl";
	public static String AXIOM_FILE = "axiom_ThesaurusInferred_forTS.txt";
	public static String HIER_FILE = "parent_child.txt";
	public static String ROLE_FILE = "roles.txt";
	public static String SUBSET_FILE = "A8.txt";
	public static String LABEL_FILE = "label.txt";

	public static String reformatDate(String dateStr) {
		// from yyyy-MM-dd to MM/dd/yyyy
		Vector v = StringUtils.parseData(dateStr, '-');
		return (String) v.elementAt(1) + "/" + (String) v.elementAt(2) + "/" + (String) v.elementAt(0);
	}

	public static String getNamedGraphForDate(String dateStr) {
		return (String) date2NamedGraphMap.get(dateStr);
	}


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

    public static void generateLabelFile(String hierfile) {
		Vector v = Utils.readFile(hierfile);
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
		OWLScanner owlscanner = null;
		String currentDir = System.getProperty("user.dir");

		System.out.println("Downloading " + NCItDownload.NCIT_ZIP_FILE + "...");
		String zipFilePath = currentDir + File.separator + NCItDownload.NCIT_ZIP_FILE;
		NCItDownload.download(NCItDownload.NCIt_URI + NCItDownload.NCIT_ZIP_FILE, new File(currentDir + File.separator + NCItDownload.NCIT_ZIP_FILE));
		String targetDir = NCItDownload.NCIt_URI + NCItDownload.NCIT_ZIP_FILE;
		NCItDownload.unzip(zipFilePath, currentDir);
		System.out.println("NCI Thesaurus OWL dowloaded.");

	    owlscanner = new OWLScanner(currentDir + File.separator + NCIT_OWL_FILE);

		Vector w = owlscanner.extractAxiomData(null);
		Utils.saveToFile(currentDir + File.separator + AXIOM_FILE, w);
		System.out.println("owlscanner.extractAxiomData completed - " + w.size());

		w = owlscanner.extractHierarchicalRelationships(owlscanner.get_owl_vec());
		Utils.saveToFile(currentDir + File.separator + HIER_FILE, w);

		String hierfile = currentDir + File.separator + HIER_FILE;
		generateLabelFile(hierfile);
		System.out.println("owlscanner.extractHierarchicalRelationships completed - " + w.size());

		w = owlscanner.extractOWLRestrictions(owlscanner.get_owl_vec());
		Utils.saveToFile(currentDir + File.separator + ROLE_FILE, w);
		System.out.println("owlscanner.extractOWLRestrictions completed - " + w.size());

		w = owlscanner.extractAssociations(owlscanner.get_owl_vec(), "A8");
		Utils.saveToFile(currentDir + File.separator + SUBSET_FILE, w);
		System.out.println("owlscanner.extractAssociations completed - " + w.size());

   }

   public static void main(String[] args) {
	   run();
   }


}