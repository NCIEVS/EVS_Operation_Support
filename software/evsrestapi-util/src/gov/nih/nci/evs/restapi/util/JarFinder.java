package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;

import java.io.*;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Comparator;
import java.util.stream.Stream;
import java.util.stream.*;
import java.nio.file.*;

public class JarFinder {

	public JarFinder() {

	}

	private static long getFileSize(String filename) {
		File file = new File(filename);
	    long length = file.length();
	    return length;
    }

	private static int getFileLength(String filename) {
		Vector v = Utils.readFile(filename);
		return v.size();
    }

    public static List listFilesInDirectory(String dir) {
		List<String> result = null;
		try {
			String currentPath = new java.io.File(".").getCanonicalPath();
			Path startPath = Paths.get(currentPath + File.separator + dir);
			try (Stream<Path> stream = Files.walk(startPath)) {
				result = stream
					.map(Path::toString)
					.collect(Collectors.toList());
			} catch (IOException e) {
				e.printStackTrace();
			}
    	} catch (Exception ex) {

		}
		return result;
    }


	public static boolean createDirectory(String dir) {
		Path path = Paths.get(dir);
		try {
			Files.createDirectory(path);
			System.out.println("Directory created successfully");
			return true;
		} catch (IOException e) {
			System.err.println("Failed to create directory: " + e.getMessage());
			return false;
		}
	}

    public static void copyFiles(Vector<String> w, String OUTPUT_DIR) {
		File f = new File(OUTPUT_DIR);
		if (!f.exists()) {
			createDirectory(OUTPUT_DIR);
		}
		String currentDir = System.getProperty("user.dir");
		for (int i=0; i<w.size(); i++) {
			String srcfile = (String) w.elementAt(i);
			System.out.println("Copy ..." + srcfile);
			f = new File(srcfile);
			String filename = f.getName();
			//FileUtils.copyfile(srcfile, currentDir + File.separator + OUTPUT_DIR + File.separator + filename);
			FileUtils.copyfile(srcfile, OUTPUT_DIR + File.separator + filename);
		}
		System.out.println("Done.");
	}

	public static Vector findJarFilesInFolder(String folder) {
		List list = listFilesInDirectory(folder);
		Vector w = new Vector();
		for (int i=0; i<list.size(); i++) {
			String t = (String) list.get(i);
			if (t.endsWith(".jar")) {
				w.add(t);
			}
		}
		return w;
	}

	public static void run(String sourceDir, String targetDir) {
		Vector w = findJarFilesInFolder(sourceDir);
		copyFiles(w, targetDir);
	}

	public static void main(String[] args) {
		String sourceDir = args[0];
		String targetDir = args[1];
		run(sourceDir, targetDir);
	}
}