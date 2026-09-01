package gov.nih.nci.evs.restapi.util;

import java.io.*;
import java.net.URI;
import java.text.*;
import java.util.*;


public class CommentLineUtils {

    public static Vector removeCommentLines(String foldername) {
		Vector v = Utils.readFile(foldername);
		return removeCommentLines(v);
	}

    public static Vector removeCommentLines(Vector v) {
		Vector w = new Vector();
		int i = 0;
		while (i < v.size()) {
			String line = (String) v.elementAt(i);
			String line0 = line;
			line = line.trim();
			if (line.startsWith("/*")) {
				boolean cont = true;
				while (cont) {
					if (line.endsWith("*/")) {
						cont = false;
						break;
					} else {
						i++;
						line = (String) v.elementAt(i);
					}
				}
			}
			if (!line.startsWith("//")) {
				if (!line.endsWith("*/")) {
					w.add(line0);
				}
			}
			i++;
		}
        return w;
	}

    public static Vector extractCommentLines(String filepath) {
		Vector v = Utils.readFile(filepath);
		return extractCommentLines(v);
	}

    public static Vector extractCommentLines(Vector v) {
		Vector w = new Vector();
		int i = 0;
		boolean isCommentLine = false;
		while (i < v.size()) {
			isCommentLine = false;
			String line = (String) v.elementAt(i);
			String line0 = line;
			line = line.trim();
			if (line.startsWith("/*")) {
				w.add(line0);
				isCommentLine = true;
				boolean cont = false;
				if (!line.endsWith("*/")) {
					cont = true;
				}
				while (cont) {
					i++;
					line = (String) v.elementAt(i);
					w.add(line);
					if (line.endsWith("*/")) {
						cont = false;
						isCommentLine = false;
						break;
					}
				}
			}
			if (line.startsWith("//")) {
				w.add(line0);
			}
			i++;
		}
        return w;
	}

	public static void removeCommentLinesInDirectory(String dirPath) {
		Vector v = FileUtils.getFilesInDirectory(dirPath);
		for (int i=0; i<v.size(); i++) {
			String filepath = (String) v.elementAt(i);
			Vector w = removeCommentLines(filepath);
			Utils.saveToFile(filepath, w);
		}
	}

	public static Vector extractCommentLinesInDirectory(String dirPath) {
		Vector w = new Vector();
		Vector v = FileUtils.getFilesInDirectory(dirPath);
		for (int i=0; i<v.size(); i++) {
			String filepath = (String) v.elementAt(i);
			w.add(filepath);
			Vector w1 = extractCommentLines(filepath);
			w.addAll(w1);
		}
		return w;
	}

	public static Vector getOnJarCopyRightStmts() {
		Vector w = new Vector();
		w.add("/*");
		w.add("Copyright (c) 2004-2010, P. Simon Tuffs (simon@simontuffs.com)");
		w.add("* All rights reserved.");
		w.add("*");
		w.add("* See the full license at http://one-jar.sourceforge.net/one-jar-license.html");
		w.add("* This license is also included in the distributions of this software");
		w.add("* under doc/one-jar-license.txt");
		w.add("*/");
		return w;
	}

	public static void addOnJarCopyRightStmts(String filename) {
		Vector w = getOnJarCopyRightStmts();
		Vector w1 = Utils.readFile(filename);
		w.addAll(w1);
		w.add("\n");
		Utils.saveToFile(filename, w);
	}

	public static String fileMainClassInPackage(String pathname) {
		Vector v = FileUtils.getFilesInDirectory(pathname);
		for (int i=0; i<v.size(); i++) {
			String filepath = (String) v.elementAt(i);
			Vector w = Utils.readFile(filepath);
			if (w.contains("package gov.nih.nci.evs.restapi.appl.main;")) {
				return filepath;
			}
		}
		return null;
	}

	public static boolean deleteDirectory(File file) {
		File[] allContents = file.listFiles();
		if (allContents != null) {
			for (File f : allContents) {
				deleteDirectory(f);
			}
		}
		return file.delete();
	}

	public static void run(String srcDir, String targetDir) {
		boolean oneJarAppl = false;
		run(srcDir, targetDir, oneJarAppl);
	}

	public static void run(String srcDir, String targetDir, boolean oneJarAppl) {
		String path1 = srcDir;
		Vector w = FileUtils.getFilesInDirectory(path1);
		Utils.dumpVector(path1, w);
		String path2 = targetDir;
		File f = new File(path2);
		if (f.exists()) {
			System.out.println("Delete " + path2);
			deleteDirectory(f);
		}

		try {
			System.out.println("Backing up " + path1 + "...");
			FileUtils.copyDirectory(path1, path2);

			System.out.println("removeCommentLinesInDirectory " + path2 + "...");
			CommentLineUtils.removeCommentLinesInDirectory(path2);

			if (oneJarAppl) {
				System.out.println("oneJarAppl? " + oneJarAppl);
				String mainClass = CommentLineUtils.fileMainClassInPackage(path2);
				System.out.println("main class: " + mainClass);
				CommentLineUtils.addOnJarCopyRightStmts(mainClass);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}



