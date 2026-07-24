package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.appl.*;
import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.nio.file.Files;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import java.util.Base64;
import java.nio.file.*;
import java.nio.file.Files;
import java.nio.charset.Charset;

public class SpecialCharFileDiff {
	public static void run(String filename1, String filename2) {
		File f1 = new File(filename1);
		if (!f1.exists()) {
			System.out.println("File " + filename1 + " not found - program aborts.");
		}
		File f2 = new File(filename2);
		if (!f2.exists()) {
			System.out.println("File " + filename2 + " not found - program aborts.");
		}
		Vector v1 = SpecialCharReadWrite.readFromFile(filename1, false);
		Object obj = v1.remove(0);
		System.out.println((String) obj.toString());
		v1 = DelimitedDataExtractor.removeColumnData(v1, 0, '\t');
		System.out.println("v1: " + ": " + v1.size());

		Vector v2 = SpecialCharReadWrite.readFromFile(filename2, false);
		obj = v2.remove(0);
		System.out.println((String) obj.toString());
		v2 = DelimitedDataExtractor.removeColumnData(v2, 0, '\t');
		System.out.println("v2: " + ": " + v2.size());

		HashSet hset = Utils.vector2HashSet(v2);
		Vector w = new Vector();
		w.add("Line Number\tNCIt Code\tValue\tSpecial Character(s)");
		for (int i=0; i<v1.size(); i++) {
			String line = (String) v1.elementAt(i);
			if (!hset.contains(line)) {
				w.add(line);
			}
		}
		String outputfile = "diff_" + StringUtils.getToday() + ".txt";
		SpecialCharReadWrite.saveToFile(outputfile, w);
		Text2Excel.generateExcel(outputfile, '\t');
	}

	public static void run(String filename) {
		Vector v1 = SpecialCharReadWrite.readFromFile(filename, false);
		SpecialCharDetector.saveToFile("test_" + filename, v1);
	}

	public static void main(String[] args) {
		String filename1 = args[0]; //nonascii_Thesaurus-260526-26.05d.original_v2
		String filename2 = args[1]; //nonascii_Thesaurus-260526-26.05d_v2
		run(filename1, filename2);
	}
}

