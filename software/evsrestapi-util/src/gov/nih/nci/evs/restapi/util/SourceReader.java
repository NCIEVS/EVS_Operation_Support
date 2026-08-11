package gov.nih.nci.evs.restapi.util;

import java.io.*;
import java.net.URI;
import java.text.*;
import java.util.*;
import java.nio.file.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;


/**
 * @author EVS Team
 * @version 1.0
 *
 * Modification history:
 *     Initial implementation kim.ong@nih.gov
 *
 */

//java -Xms512m -Xmx4g -classpath %CLASSPATH% OneJarUtils ctcae2owl gov.nih.nci.evs.restapi.appl
public class SourceReader {

    public static Vector listFileNames() {
		String currentDir = System.getProperty("user.dir");
		System.out.println("Current dir using System:" + currentDir);
		Vector filenames = listFileNames(currentDir);
		return filenames;
	}

    public static Vector listFileNames(String path) {
		Vector textfiles = FileUtils.listFiles(path);
		Vector filenames = new Vector();
		for (int i=0; i<textfiles.size(); i++) {
			File file = (File) textfiles.elementAt(i);
			String filename = file.getAbsolutePath();
			filename = filename.replace("\\", File.separator);
			filenames.add(filename);
		}
		return filenames;
	}

    public static Vector listFileNames(String path, boolean nameOnly) {
		Vector textfiles = FileUtils.listFiles(path);
		Vector filenames = new Vector();
		for (int i=0; i<textfiles.size(); i++) {
			File file = (File) textfiles.elementAt(i);
			String filename = file.getName();//file.getAbsolutePath();
			filename = filename.replace("\\", File.separator);
			filenames.add(filename);
		}
		return filenames;
	}


	public static String findPackageName(String javasrc) {
		Vector v = Utils.readFile(javasrc);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			line = line.trim();
			if (line.startsWith("package ")) {
				int n = line.indexOf(" ");
				return line.substring(n+1, line.length()-1);
			}
		}
		return null;
	}


	public static void copyFiles(Vector src_files, String srcDir, String targetDir) {
		for (int i=0; i<src_files.size(); i++) {
			String srcFileName = (String) src_files.elementAt(i);
			String targetFileName = srcFileName.replace(srcDir + File.separator, targetDir + File.separator);
			FileUtils.copyfile(srcFileName, targetFileName);
		}
	}

	public static void copyFiles(String src_file, String srcDir, Vector targetDirs) {
		String srcFileName = srcDir + File.separator + src_file;
		for (int i=0; i<targetDirs.size(); i++) {
			String targetDir = (String) targetDirs.elementAt(i);
			String targetFileName = targetDir + File.separator + src_file;
			FileUtils.copyfile(srcFileName, targetFileName);
		}
	}

	public static void copyFiles(Vector src_files, Vector target_files) {
		for (int i=0; i<src_files.size(); i++) {
			String srcFileName = (String) src_files.elementAt(i);
			String targetFileName = (String) target_files.elementAt(i);
			FileUtils.copyfile(srcFileName, targetFileName);
		}
	}


/*
    public static String readRegularFiles(String startPathName, String ext) {
		List<String> result = FileUtils.listRegularFiles(startPathName, ext);
		File f = new File(startPathName);
		String srcfile = "src_" + f.getName() + ".txt";
		Vector w0 = new Vector();
		Vector importStmts = new Vector();
		int k = 0;
		for (int i=0; i<result.size(); i++) {
			k = i+1;
			String filename = result.get(i);
			w0.add("\n(" + k + ") " + filename);
			Vector w = Utils.readFile(filename);
			boolean istart = false;
			for (int j=0; j<w.size(); j++) {
				String line = (String) w.elementAt(j);
				if (line.startsWith("import ")) {
					istart = true;
				}
				if (istart && !line.startsWith("import ")) {
					istart = false;
					break;
				}
				if (istart) {
					importStmts.add(line);
				}
			}
			w0.addAll(w);
		}
		Utils.saveToFile("import_stmt_" + srcfile, importStmts);
		Utils.saveToFile(srcfile, w0);
		return srcfile;
    }
*/
	public static void createFolder(String packageName) {
		Vector path = Utils.parseData(packageName, '.');
        ProcessBuilder processBuilder = new ProcessBuilder();
		String folderName = "";
		for (int i=0; i<path.size(); i++) {
			folderName = folderName + (String) path.elementAt(i);
			File f = new File(folderName);
			try {
				if (!f.exists()) {
					f.mkdir();
				}
				folderName = folderName + File.separator;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}


	public static String generateOneJarMainClassName(String filename) {
		Vector v = Utils.readFile(filename);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			line = line.trim();
			if (line.startsWith("public class ")) {
				Vector u = StringUtils.parseData(line, ' ');
				String name = (String) u.elementAt(2);
				name = name.toLowerCase();
				String className = ("" + name.charAt(0)).toUpperCase() + name.substring(1, name.length()) + "Main";
				return className;
			}
		}
		return null;
	}

    public static void generateOnejarMainClass(String mainClassFileName, String oneJarMainClassPathName, String applPackageName) {
        String curr_dir = System.getProperty("user.dir");
        String repositoryDir = curr_dir + File.separator + "src_repository";
        String t = repositoryDir + File.separator + mainClassFileName;
		String oneJarMainClassName = generateOneJarMainClassName(t);
		PrintWriter out = null;
		mainClassFileName = mainClassFileName.replace(".java", "");
		try {
			out = new PrintWriter(oneJarMainClassPathName);
			out.println("/*");
			out.println(" * Copyright (c) 2004-2010, P. Simon Tuffs (simon@simontuffs.com)");
			out.println(" * All rights reserved.");
			out.println(" *");
			out.println(" * See the full license at http://one-jar.sourceforge.net/one-jar-license.html");
			out.println(" * This license is also included in the distributions of this software");
			out.println(" * under doc/one-jar-license.txt");
			out.println(" */");
			out.println("package " + applPackageName + ".main" + ";");
			out.println("import " + applPackageName + ".*;");
			out.println("");
			out.println("import java.util.Arrays;");
			out.println("");
			out.println("public class " + oneJarMainClassName + " {");
			out.println("");
			out.println("	public static void main(String[] args) {");
			out.println("		long ms = System.currentTimeMillis();");
			out.println("		String serviceUrl = args[0];");
			out.println("		String named_graph = args[1];");
			out.println("		String username = args[2];");
			out.println("		String password = args[3];");
			t = mainClassFileName.toLowerCase();
			out.println("		" + mainClassFileName + " " + t + " = new " + mainClassFileName + "(serviceUrl, named_graph, username, password);");
			out.println("		" + t + ".run();");
			out.println("		System.out.println(\"Total run time (ms): \" + (System.currentTimeMillis() - ms));");
			out.println("	}");
			out.println("}");
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

    public static String findOnJarMainClassName() {
		String oneJarMainClassName = null;
		String curr_dir = System.getProperty("user.dir");
		String repositoryDir = curr_dir + File.separator + "src_repository";
		Vector fileNames = listFileNames(repositoryDir, true);
		Vector srcFiles = new Vector();
		Vector targetFiles = new Vector();
		for (int i=0; i<fileNames.size(); i++) {
			String javasrc = (String) fileNames.elementAt(i);
			String srcPackageName = findPackageName(repositoryDir + File.separator + javasrc);
			String applPackageName = srcPackageName;
			srcPackageName = "src." + srcPackageName;
            String srcFile = repositoryDir + File.separator + javasrc;
			String targetFile = curr_dir + File.separator + srcPackageName;
			targetFile = targetFile.replace(".", File.separator);
			targetFile = targetFile + File.separator + javasrc;
			targetFile = curr_dir + File.separator + srcPackageName;
			targetFile = targetFile.replace(".", File.separator);
			oneJarMainClassName = generateOneJarMainClassName(srcFile);
			System.out.println("=============== oneJarMainClassName: " + oneJarMainClassName);
		}
		return oneJarMainClassName;
	}

	public static void main2(String[] args) {
		String curr_dir = System.getProperty("user.dir");

		String s = findOnJarMainClassName();

		String projectName = args[0];
		String packageName = "gov.nih.nci.evs.restapi.appl";
		if (args.length == 2) {
			packageName = args[1];
		}

		packageName = "src." + packageName;
		createFolder(packageName);

		packageName = "gov.nih.nci.evs.restapi.util";
		packageName = "src." + packageName;
		createFolder(packageName);

        String sourceDir = "src0";
        String targetDir = "src";

		Vector fileNames = listFileNames(sourceDir);
		copyFiles(fileNames, sourceDir, targetDir);

		Vector v = Utils.readFile("build0.xml");
		Vector w = new Vector();

		s = findOnJarMainClassName();
		s = s.replace("MainMain", "Main");
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (line.indexOf("One-Jar-Main-Class") != -1) {
				line = line.replace("<PROJECTNAME>", s);
			} else {
				line = line.replace("<PROJECTNAME>", projectName);
			}
			w.add(line);
		}
		Utils.saveToFile("build.xml", w);

		String repositoryDir = curr_dir + File.separator + "src_repository";
		fileNames = listFileNames(repositoryDir, true);
		Vector srcFiles = new Vector();
		Vector targetFiles = new Vector();
		for (int i=0; i<fileNames.size(); i++) {
			String javasrc = (String) fileNames.elementAt(i);
			String srcPackageName = findPackageName(repositoryDir + File.separator + javasrc);
			String applPackageName = srcPackageName;
			srcPackageName = "src." + srcPackageName;
			createFolder(srcPackageName);
			createFolder(srcPackageName + ".main");
            String srcFile = repositoryDir + File.separator + javasrc;

			srcFiles.add(srcFile);
			String targetFile = curr_dir + File.separator + srcPackageName;
			targetFile = targetFile.replace(".", File.separator);
			targetFile = targetFile + File.separator + javasrc;
			targetFiles.add(targetFile);

			targetFile = curr_dir + File.separator + srcPackageName;
			targetFile = targetFile.replace(".", File.separator);
			String oneJarMainClassName = generateOneJarMainClassName(srcFile);
			String onejarMain = targetFile + File.separator + "main" + File.separator + oneJarMainClassName + ".java";
			if (srcPackageName.endsWith(".appl")) {
				generateOnejarMainClass(javasrc, onejarMain, applPackageName);
			}
		}
		copyFiles(srcFiles, targetFiles);
	}

    public static Vector listSubdirectories() {
		String currentDir = System.getProperty("user.dir");
		File folder = new File(currentDir);
		File[] listOfFiles = folder.listFiles();
		Vector w = new Vector();
		if(listOfFiles != null) {
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
				} else if (listOfFiles[i].isDirectory()) {
					w.add(currentDir + File.separator + listOfFiles[i].getName());
				}
			}
		}
		return w;
	}

	public static void updateJar(String[] args) {
		Vector w = listSubdirectories();
		String src_file = args[0];
		String srcDir = args[1];
		String file2Copy = srcDir + File.separator + src_file;
		Vector targetDirs = new Vector();
		for (int i=0; i<w.size(); i++) {
			String t = (String) w.elementAt(i);
			String targetDir = t + File.separator + "lib";
			targetDirs.add(targetDir);
		}
		copyFiles(src_file, srcDir, targetDirs);
		updateTestProperties();
	}

	public static void deleteFiles(String filename) {
		Vector subdir = listSubdirectories();
		for (int i=0; i<subdir.size(); i++) {
			String sub = (String) subdir.elementAt(i);
			String targetFileName = sub + File.separator + "lib" + File.separator + filename;
			System.out.println("Delete " + targetFileName);
			FileUtils.deleteFile(targetFileName);
		}
	}

	public static void deleteDirectory(String dirName) {
		Vector subdir = listSubdirectories();
		for (int i=0; i<subdir.size(); i++) {
			String sub = (String) subdir.elementAt(i);
			String targetDir = sub + File.separator + dirName;
			System.out.println("Delete directory" + targetDir);
			deleteDir(targetDir);
		}
	}

	public static void deleteDir(String dir) { //"path/to/directory"
		Path pathToBeDeleted = Paths.get(dir);
		try (Stream<Path> walk = Files.walk(pathToBeDeleted)) {
			walk.sorted(Comparator.reverseOrder())
				.forEach(path -> {
					try {
						Files.delete(path);
					} catch (IOException e) {
						System.err.printf("Failed to delete %s: %s%n", path, e.getMessage());
					}
				});
			System.out.println("Directory " + dir + " deleted successfully!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void updateTestProperties() {
	    String srcDir = "C:/EVSFocus/USER_SUPPORT/REPORT_GENERATION/resources";
	    String src_file = "Test.properties";
	    String targetSubDir = "test/resources";
	    FileUtils.updateFile(srcDir, src_file, targetSubDir);
	}



	public static void main4(String[] args) {
		updateJar(args);
		Vector w = listSubdirectories();
		Utils.dumpVector("executable jars", w);
	}

	public static void main1(String[] args) {
		String filename = args[0];
		deleteFiles(filename);
	}


    public static String readRegularFiles(String startPathName, String ext) {
		List<String> result = FileUtils.listRegularFiles(startPathName, ext);
		File f = new File(startPathName);
		String srcfile = "src_" + f.getName() + ".txt";

		System.out.println(srcfile);

		Vector importStmts = new Vector();
		Vector w0 = new Vector();
		int k = 0;
		for (int i=0; i<result.size(); i++) {
			k = i+1;
			String filename = result.get(i);
			w0.add("\n(" + k + ") " + filename);
			Vector w = Utils.readFile(filename);
			boolean istart = false;
			for (int j=0; j<w.size(); j++) {
				String line = (String) w.elementAt(j);
				if (!istart && line.startsWith("import ")) {
					istart = true;
				}
				if (istart && !line.startsWith("import ")) {
					istart = false;
					break;
				}
				if (istart) {
					if (!importStmts.contains(line)) {
						importStmts.add(line);
					}
				}
			}
			w0.addAll(w);
		}
		importStmts = new SortUtils().quickSort(importStmts);
		Utils.saveToFile("import_stmts_" + srcfile, importStmts);
		Utils.saveToFile(srcfile, w0);
		return srcfile;
    }


	public static void main(String[] args) {
		String dirName = args[0];
		String ext = args[1];
        //deleteDirectory(dirName);
        //updateJar(args);

        //Vector v = listFileNames();
        //Utils.dumpVector("files", v);
        String outputfile = readRegularFiles(dirName, ext);
        System.out.println(outputfile + " generated.");


	}
}
