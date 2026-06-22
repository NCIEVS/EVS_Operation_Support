package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;

import java.io.*;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NCItUtils {
	public static String HIER_FILE = "parent_child.txt";
	public static String ROLE_FILE = "roles.txt";
	public static String PROPERTY_FILE = "properties.txt";
	public static String OBJECT_PROPERTY_FILE = "objectProperies.txt";
	public static String ANNOTATION_PROPERTIES_FILE = "supported_properties.txt";
	public static String SEMANTIC_TYPE_FILE = "P106.txt";
	public static String NCIT_OWL_FILE = "ThesaurusInferred_forTS.owl";
	public static String AXIOM_FILE = "axiom_ThesaurusInferred_forTS.txt";
	public static String ASSOCIATION_FILE = "associations.txt";
	public static String VS_FILE = "A8.txt";

	public static void generateReports() {
		String owlfile = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile;
		String outputDir = System.getProperty("user.dir");
		generateReports(owlfile, outputDir);
	}

	public static String getCurrentWorkingDirectory() {
		return System.getProperty("user.dir");
	}

	public static void generateReports(String owlfile) {
		String outputDir = getCurrentWorkingDirectory();
		generateReports(owlfile, outputDir);
	}

	private static long getFileSize(String filename) {
		File file = new File(filename);
	    long length = file.length();
	    return length;
    }

    public static void createDirectory(String directory) {
        Path path = Paths.get(directory);
        try {
            Files.createDirectories(path);
            System.out.println("Directory created successfully!");
        } catch (IOException e) {
            System.err.println("Failed to create directory: " + e.getMessage());
        }
    }

    public static void deleteFile(String filename) {
        Path path = Paths.get(filename);
        try {
            boolean result = Files.deleteIfExists(path);
            if (result) {
                System.out.println("File deleted successfully.");
            } else {
                System.out.println("File does not exist.");
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handles permission issues or non-empty directories
        }
    }

	public static void generateReports(String owlfile, String outputDir) {
		File f = new File(owlfile);
		if (!f.exists()) {
			System.out.println("The system cannot find " + owlfile);
			return;
		} else {
			System.out.println(owlfile +" exists.");
		}
		long ms = System.currentTimeMillis();
		deleteFile(outputDir);
		createDirectory(outputDir);

        OWLScanner owlscanner = new OWLScanner(owlfile);
        // Axiom file
		Vector w = owlscanner.extractAxiomData(null);
		String outputfile = outputDir + File.separator + AXIOM_FILE;
		Utils.saveToFile(outputfile, w);
		HTMLDecoder.run(outputfile);
		long size = getFileSize(outputfile);
		System.out.println(outputfile + " generated. (Number of lines: " + w.size() + ", File size: " + size + ")");

		// Hierarchy file
		w = owlscanner.extractHierarchicalRelationships(owlscanner.get_owl_vec());
		outputfile = outputDir + File.separator + HIER_FILE;
		Utils.saveToFile(outputfile, w);
		HTMLDecoder.run(outputfile);
		size = getFileSize(outputfile);
		System.out.println(outputfile + " generated. (Number of lines: " + w.size() + ", File size: " + size + ")");

		// Role file
		w = owlscanner.extractOWLRestrictions(owlscanner.get_owl_vec());
		outputfile = outputDir + File.separator + ROLE_FILE;
		Utils.saveToFile(outputfile, w);
		size = getFileSize(outputfile);
		System.out.println(outputfile + " generated. (Number of lines: " + w.size() + ", File size: " + size + ")");

		// Value set file
		w = owlscanner.extractAssociations(owlscanner.get_owl_vec(), "A8");
		outputfile = outputDir + File.separator + VS_FILE;
		Utils.saveToFile(outputfile, w);
		size = getFileSize(outputfile);
		System.out.println(outputfile + " generated. (Number of lines: " + w.size() + ", File size: " + size + ")");

		// Association file
		w = owlscanner.extractAssociations(owlscanner.get_owl_vec());
		outputfile = outputDir + File.separator + ASSOCIATION_FILE;
		Utils.saveToFile(outputfile, w);
		size = getFileSize(outputfile);
		System.out.println(outputfile + " generated. (Number of lines: " + w.size() + ", File size: " + size + ")");

		// Properety file
		w = owlscanner.extractProperties(owlscanner.get_owl_vec());
		outputfile = outputDir + File.separator + PROPERTY_FILE;
		Utils.saveToFile(outputfile, w);
		HTMLDecoder.run(outputfile);
		size = getFileSize(outputfile);
		System.out.println(outputfile + " generated. (Number of lines: " + w.size() + ", File size: " + size + ")");

        // Object property file
		w = owlscanner.extractObjectProperties(owlscanner.get_owl_vec());
		outputfile = outputDir + File.separator + OBJECT_PROPERTY_FILE;
		Utils.saveToFile(outputfile, w);
		HTMLDecoder.run(outputfile);
		size = getFileSize(outputfile);
		System.out.println(outputfile + " generated. (Number of lines: " + w.size() + ", File size: " + size + ")");

        // Annotation properties file
		w = owlscanner.getSupportedProperties();
		outputfile = outputDir + File.separator + ANNOTATION_PROPERTIES_FILE;
		Utils.saveToFile(outputfile, w);
		HTMLDecoder.run(outputfile);
		size = getFileSize(outputfile);
		System.out.println(outputfile + " generated. (Number of lines: " + w.size() + ", File size: " + size + ")");

        w.clear();
        owlscanner.get_owl_vec().clear();
        System.out.println("\tTotal run time (ms): " + (System.currentTimeMillis() - ms));
	}

	public static void main(String[] args) {
		String dir = null;
		if (args.length == 0) {
			dir = StringUtils.getToday();
		} else {
			dir = args[0];
		}
		String outputDir = getCurrentWorkingDirectory() + File.separator + dir;
		String owlfile = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile;
		generateReports(owlfile, outputDir);
	}
}

