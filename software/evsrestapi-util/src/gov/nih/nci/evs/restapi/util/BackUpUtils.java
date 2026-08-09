package gov.nih.nci.evs.restapi.util;

import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.ZoneId;
import java.nio.file.attribute.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;

public class BackUpUtils {
	static String DOCUMENTATION_FILE = "documentation.txt";

    public static Vector listFiles(String path) {
		Vector w = new Vector();
        File root = new File( path );
        File[] list = root.listFiles();
        if (list == null) return w;
        for (File f : list) {
            if (f.isDirectory()) {
                w.addAll(listFiles( f.getAbsolutePath()));
            } else {
                w.add(f.getAbsoluteFile());
            }
        }
        return w;
    }

    public static Vector listFileNames(String path) {
		Vector textfiles = listFiles(path);
		Vector filenames = new Vector();
		for (int i=0; i<textfiles.size(); i++) {
			File file = (File) textfiles.elementAt(i);
			String filename = file.getAbsolutePath();
			filename = filename.replace("\\", "/");
			filenames.add(filename);
		}
		return filenames;
	}

	public static void listPackageFolders() {
		Vector w = new Vector();
		String cwd = System.getProperty("user.dir");
		String srcDir = cwd + File.separator + "src";
		String libDir = cwd + File.separator + "lib";
		Vector v1 = listFileNames(srcDir);
		w.add("src folder");
		w.addAll(v1);
		w.add("\nlib folder");
		Vector v2 = listFileNames(libDir);
		w.addAll(v2);
		Vector v3 = Utils.readFile("build.properties");
		w.add("\nbuild.properties");
		w.addAll(v3);
		Vector v4 = Utils.readFile("build.xml");
		w.add("\nbuild.xml");
		w.addAll(v4);
		Utils.saveToFile(DOCUMENTATION_FILE, w);

	}

    public static void createSourceRepository() {
        OneJarUtils.createDirectory(OneJarUtils.SOURCE_REPOSITORY_DIR);
	}

	public static Vector getSourceFiles() {
		File f = new File(DOCUMENTATION_FILE);
		if (!f.exists()) {
			System.out.println("ERROR: " + DOCUMENTATION_FILE + " not found.");
			return null;
		}
		Vector v = Utils.readFile(DOCUMENTATION_FILE);
		Vector w = new Vector();
		boolean istart = false;
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (istart && line.length() > 0) {
				w.add(line);
			}
			if (!istart && line.startsWith("src folder")) {
				istart = true;
			}
			if (istart && line.length() == 0) {
				break;
			}
		}
		return w;
	}

	public static void create_build_properties() {
		File f = new File(DOCUMENTATION_FILE);
		if (!f.exists()) {
			System.out.println("ERROR: " + DOCUMENTATION_FILE + " not found.");
			return;
		}
		Vector v = Utils.readFile(DOCUMENTATION_FILE);
		Vector w = new Vector();
		boolean istart = false;
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (istart && line.length() > 0) {
				w.add(line);
			}
			if (!istart && line.startsWith("build.properties")) {
				istart = true;
			}
			if (istart && line.startsWith("build.xml")) {
				break;
			}
		}
		Utils.saveToFile("build.properties", w);
	}

	public static void create_build_xml() {
		File f = new File(DOCUMENTATION_FILE);
		if (!f.exists()) {
			System.out.println("ERROR: " + DOCUMENTATION_FILE + " not found.");
			return;
		}
		Vector v = Utils.readFile(DOCUMENTATION_FILE);
		Vector w = new Vector();
		boolean istart = false;
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (istart && line.length() > 0) {
				w.add(line);
			}
			if (!istart && line.startsWith("build.xml")) {
				istart = true;
			}
		}
		Utils.saveToFile("build.xml", w);
	}

	public static Vector getJarFiles() {
		File f = new File(DOCUMENTATION_FILE);
		if (!f.exists()) {
			System.out.println("ERROR: " + DOCUMENTATION_FILE + " not found.");
			return null;
		}
		Vector v = Utils.readFile(DOCUMENTATION_FILE);
		Vector w = new Vector();
		boolean istart = false;
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (istart && line.length() > 0) {
				w.add(line);
			}
			if (!istart && line.startsWith("lib folder")) {
				istart = true;
			}
			if (istart && line.length() == 0) {
				break;
			}
		}
		return w;
	}

	public static void copySourceFilesToSrcFolder() {
		OneJarUtils.createDirectory("src");
		String curr_dir = System.getProperty("user.dir");
	    Vector src_files = getSourceFiles();
		Vector target_files = new Vector();
		for (int i=0; i<src_files.size(); i++) {
			String src_file = (String) src_files.elementAt(i);
			File f = new File(src_file);
			target_files.add(curr_dir + File.separator + "src" + File.separator + f.getName());
		}
		FileUtils.copyFiles(src_files, target_files);
	}

	public static void copySourceFilesToRepository() {
		String curr_dir = System.getProperty("user.dir");
	    Vector src_files = getSourceFiles();
		Vector target_files = new Vector();
		for (int i=0; i<src_files.size(); i++) {
			String src_file = (String) src_files.elementAt(i);
			File f = new File(src_file);
			target_files.add(curr_dir + File.separator + OneJarUtils.SOURCE_REPOSITORY_DIR + File.separator + f.getName());
		}
		FileUtils.copyFiles(src_files, target_files);
	}

	public static void copyjarToLibFolder() {
		OneJarUtils.createDirectory("lib");
		String curr_dir = System.getProperty("user.dir");
	    Vector jar_files = getJarFiles();
		Vector target_files = new Vector();
		for (int i=0; i<jar_files.size(); i++) {
			String jar_file = (String) jar_files.elementAt(i);
			File f = new File(jar_file);
			target_files.add(curr_dir + File.separator + "lib" + File.separator + f.getName());
		}
		FileUtils.copyFiles(jar_files, target_files);
	}

	public static void restore() {
		copySourceFilesToSrcFolder();
		copyjarToLibFolder();
		create_build_properties();
		create_build_xml();
	}

	public static void main(String[] args) {
		restore();
	}
}