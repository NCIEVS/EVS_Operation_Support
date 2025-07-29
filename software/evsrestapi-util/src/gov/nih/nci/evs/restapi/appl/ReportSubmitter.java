package gov.nih.nci.evs.restapi.appl;
import gov.nih.nci.evs.restapi.config.*;
import gov.nih.nci.evs.restapi.util.*;

import java.io.*;
import java.util.*;

public class ReportSubmitter {
	public static String HIER_FILE = "parent_child.txt";
	public static String ROLE_FILE = "roles.txt";
	public static String PROPERTY_FILE = "properties.txt";
	public static String OBJECT_PROPERTY_FILE = "objectProperies.txt";
	public static String SEMANTIC_TYPE_FILE = "P106.txt";
	public static String NCIT_OWL_FILE = "ThesaurusInferred_forTS.owl";
	public static String AXIOM_FILE = "axiom_ThesaurusInferred_forTS.txt";
	public static String ASSOCIATION_FILE = "association.txt";
	public static String VS_FILE = "A8.txt";

	static String serviceUrl = ConfigurationController.serviceUrl;
	static String namedGraph = ConfigurationController.namedGraph;
	static String username = ConfigurationController.username;
    static String password = ConfigurationController.password;

	static String[] root_array = null;

	static HierarchyHelper hh = null;
	static Vector roots = null;
	static Vector displayLabels = null;

	static String PRIMITIVE_CONCEPT_ROOTS = null;

	int numCompleted = 0;
	String outputfile = null;
	Vector semanticTypes = new Vector();
	Vector objectProperies = new Vector();

	String[] object_properties = null;
	HashMap roleMap = null;
	HashMap roleCode2LabelMap = null;
	HashMap semanticTypeMap = null;

	boolean REPORT_GENERATION_MODE = true;
	static String NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile;
	static OWLScanner owlscanner = null;
	Vector output_vec = null;

	public static OWLScanner getOWLScanner() {
		return owlscanner;
	}

	public static void clearOWLScanner() {
		if (owlscanner != null) {
			owlscanner.clear();
		}
	}

	public static void setOWLScanner(OWLScanner owl_scanner) {
		owlscanner.clear();
		owlscanner = owl_scanner;
	}

	public static void reportGenerationSetUp() {
		CurrentUser.reportGenerationSetUp();
	}

	public static void deletReportGenerationData() {
		CurrentUser.deletReportGenerationData();
	}

	public static void download() {
		NCItDownload.download();
	}

    static {
		owlscanner = new OWLScanner(NCIT_OWL);
		PRIMITIVE_CONCEPT_ROOTS = ConfigurationController.primitiveConceptRoots;
	}


	public static void generateAxiomFile() {
		 Vector w = owlscanner.extractAxiomData(null);
		 Utils.saveToFile(AXIOM_FILE, w);
		 HTMLDecoder.run(AXIOM_FILE);
		 System.out.println(AXIOM_FILE + " generated.");
	 }

	 public static void generateHierarchyFile() {
		 Vector w = owlscanner.extractHierarchicalRelationships(owlscanner.get_owl_vec());
		 Utils.saveToFile(HIER_FILE, w);
		 HTMLDecoder.run(HIER_FILE);
		 System.out.println(HIER_FILE + " generated.");
	 }

	 public static void generateRoleFile() {
		 Vector w = owlscanner.extractOWLRestrictions(owlscanner.get_owl_vec());
		 Utils.saveToFile(ROLE_FILE, w);
		 System.out.println(ROLE_FILE + " generated.");
	 }

	 public static void generateValueSetFile() {
		 Vector w = owlscanner.extractAssociations(owlscanner.get_owl_vec(), "A8");
		 Utils.saveToFile(VS_FILE, w);
		 System.out.println(VS_FILE + " generated.");
	 }

	 public static void generateAssociationFile() {
		 Vector w = owlscanner.extractAssociations(owlscanner.get_owl_vec());
		 Utils.saveToFile(ASSOCIATION_FILE, w);
		 System.out.println(ASSOCIATION_FILE + " generated.");
	 }

	 public static void generatePropertyFile() {
		 Vector w = owlscanner.extractProperties(owlscanner.get_owl_vec());
		 Utils.saveToFile(PROPERTY_FILE, w);
		 HTMLDecoder.run(PROPERTY_FILE);
		 System.out.println(PROPERTY_FILE + " generated.");
	 }

	 public static void generateReport(String report) {
		 if (report.compareTo("Maps To ICD0") == 0) {
			long ms = System.currentTimeMillis();
			MapsToReportWriter mapsToReportWriter = new MapsToReportWriter(serviceUrl, namedGraph, username, password);

			String terminology_name = "ICDO3";
			String terminology_version = "3.1";
			mapsToReportWriter.run(terminology_name, terminology_version);

			terminology_version = "3.2";
			mapsToReportWriter.run(terminology_name, terminology_version);

			System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
			System.out.println("Reports " + report + " generated.");
		} else if (report.compareTo("EVS Statistics") == 0) {
			long ms = System.currentTimeMillis();
			String NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile; //"ThesaurusInferred_forTS.owl";
			EVSStatisticsByOWL evsStatistics = new EVSStatisticsByOWL(NCIT_OWL);
			evsStatistics.generate();
			//evsStatistics.generate();
			System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
			System.out.println("Report " + report + " generated.");

		} else if (report.compareTo("GDC Diff") == 0) {
			long ms = System.currentTimeMillis();
			String versions = searchForVersions();
			Vector u = StringUtils.parseData(versions, '|');
			String oldVersion = (String) u.elementAt(0);
			String newVersion = (String) u.elementAt(1);
			String oldValueFile = searchXLSFile("Value", oldVersion);
			String newValueFile = searchXLSFile("Value", newVersion);
			String oldPropertyFile = searchXLSFile("Property", oldVersion);
			String newPropertyFile = searchXLSFile("Property", newVersion);

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

			ExcelDiffUtils util = new ExcelDiffUtils(oldValueFile, newValueFile);
			util.run(oldValueFile, newValueFile);
			util = new ExcelDiffUtils(oldPropertyFile, newPropertyFile);
			util.run(oldPropertyFile, newPropertyFile);

			System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
			System.out.println("Reports " + report + " generated.");

		} else if (report.compareTo("FDA Count") == 0) {
			long ms = System.currentTimeMillis();
			MemberConceptCount.run("EDQM-HC");
			MemberConceptCount.run("FDA");
			MemberConceptCount.generateCounts("count_EDQM-HC.txt", "count_FDA.txt", "C131123");
			MemberConceptCount.generateCounts("count_EDQM-HC.txt", "count_FDA.txt", "C148636");
			System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
			System.out.println("Reports " + report + " generated.");

		} else if (report.compareTo("PrimitiveConcepts") == 0) {
			long ms = System.currentTimeMillis();
			PrimitiveConceptFinder.run(PRIMITIVE_CONCEPT_ROOTS);
			System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
			System.out.println("Reports " + report + " generated.");

		} else if (report.compareTo("Linguistic Analysis") == 0) { //NLM work
			long ms = System.currentTimeMillis();
			LinguisticAnalyzer.run();
			System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
			System.out.println("Reports " + report + " generated.");

		} else if (report.compareTo("3D Pie Chart") == 0) {
			long ms = System.currentTimeMillis();
			ApachePoiPieChartCSByOWL.generatePieChart(ApachePoiPieChartCSByOWL.ncit_version);
			System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
			System.out.println("Reports " + report + " generated.");

		} else if (report.compareTo("Pediatric Subset Reports") == 0) {
			long ms = System.currentTimeMillis();
			new PediatricSubsetsGeneratorByOWL(NCIT_OWL).run();
			System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
			System.out.println("Reports " + report + " generated.");

		} else if (report.compareTo("Is-A Hierarchy QA") == 0) {
			long ms = System.currentTimeMillis();
			String serviceUrl = ConfigurationController.serviceUrl;
			String namedGraph = ConfigurationController.namedGraph;
			String username = ConfigurationController.username;
			String password = ConfigurationController.password;
			IsAHierarchyQA generator = new IsAHierarchyQA(serviceUrl, namedGraph, username, password);
			generator.run();
			System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
			System.out.println("Reports " + report + " generated.");
		} else {
			System.out.println("Report " + report + " is not sspported.");
		}


	}

    public static Vector listFilesInDirectory() {
		String dirName = null;
		return listFilesInDirectory(dirName);
	}

    public static Vector listFilesInDirectory(String dirName) {
		Vector v = new Vector();
		if (dirName == null) {
			dirName = System.getProperty("user.dir");;
		}
        File f = new File(dirName);
        String[] pathnames = f.list();
        for (String pathname : pathnames) {
            v.add(pathname);
        }
        return v;
	}

	public static String searchXLSFile(String type, String version) {
		Vector v = listFilesInDirectory();
		for (int i=0; i<v.size(); i++) {
			String t = (String) v.elementAt(i);
			if (t.endsWith(".xls")) {
				String t1 = type + "_Terminology";
				int n = t.indexOf(t1);
				if (n != -1 && t.indexOf(version) != -1) {
					return t;
				}

			}
		}
		return null;
	}

    public static String searchForVersions() {
		Vector v = listFilesInDirectory();
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String t = (String) v.elementAt(i);
			if (t.endsWith(".xls")) {
				int n = t.indexOf("Terminology");
				String s = t.substring(n+ "Terminology".length()+1, t.length());
				n = s.lastIndexOf(".");
				s = s.substring(0, n);
                if (!w.contains(s)) {
					w.add(s);
				}
			}
		}
		w = new SortUtils().quickSort(w);
		Utils.dumpVector("w", w);

		String oldVersion = (String) w.elementAt(0);
		String newVersion = (String) w.elementAt(1);
		return oldVersion + "|" + newVersion;
	}

	public static void main(String[] args) {

	}
}

