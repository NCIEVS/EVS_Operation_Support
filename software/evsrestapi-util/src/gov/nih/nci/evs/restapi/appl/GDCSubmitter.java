package gov.nih.nci.evs.restapi.appl;
import gov.nih.nci.evs.restapi.util.*;

import java.io.*;
import java.net.URI;
import java.text.*;
import java.util.*;

public class GDCSubmitter {
    static String OUTPUT_DIR = "gdcdiff";

    public static void run() {
		System.out.println("Setting up ...");
		GDCSetUp.run();
		System.out.println("Generating textual report ...");
		GDCFileDiff.createDirectory(OUTPUT_DIR);
		GDCFileDiff.submit(OUTPUT_DIR);
		System.out.println("Generating Excel report ...");
		copyFiles();
		generateExcelReport();
		System.out.println("Done");
	}

    public static void copyFiles() {
		String currentDir = System.getProperty("user.dir");
		String filename = "GDC_Property_Terminology_diff.txt";
		String srcfile = currentDir + File.separator + filename;
        System.out.println("Copy ..." + currentDir + File.separator + filename);
        FileUtils.copyfile(currentDir + File.separator + filename, currentDir + File.separator + OUTPUT_DIR + File.separator + filename);
 		filename = "GDC_Value_Terminology_diff.txt";
		srcfile = currentDir + File.separator + filename;
        System.out.println("Copy ..." + currentDir + File.separator + filename);
        FileUtils.copyfile(currentDir + File.separator + filename, currentDir + File.separator + OUTPUT_DIR + File.separator + filename);
	}

	public static void generateExcelReport() {
		String excelfile = OUTPUT_DIR + ".xlsx";
		System.out.println("excelfile: " + excelfile);
		char delim = '\t';
		System.out.println("Calling generateExcelReport...");
		Text2Excel.generateExcel(OUTPUT_DIR, excelfile, delim);
	}

	public static void main(String args[]) {
		run();
	}
}