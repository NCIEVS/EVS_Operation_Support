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

public class NCItCompareUtils {
	String observedDir = null;
	String expectedDir = null;

	public NCItCompareUtils(String observedDir, String expectedDir) {
		this.observedDir = observedDir;
		this.expectedDir = expectedDir;
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


    public void run() {
		Vector w = new Vector();
		String path = FileUtils.getCurrentWorkingDirectory() + File.separator + observedDir;
		Vector filenames = FileUtils.listFileNames(path);
		Vector files = FileUtils.listFiles(path);
		for (int i=0; i<files.size(); i++) {
			File f1 = (File) files.elementAt(i);
			String path1 = FileUtils.getCurrentWorkingDirectory() + File.separator + observedDir + File.separator + f1.getName();
			String path2 = FileUtils.getCurrentWorkingDirectory() + File.separator + expectedDir + File.separator + f1.getName();
			File f2 = new File(path2);
			if (f2.exists()) {
				w.add("\n" + observedDir + " " + f1.getName() + " (length: " + getFileLength(path1) + " size: " + getFileSize(path1) + ")");
				w.add(expectedDir + " " + f2.getName() + " (length: " + getFileLength(path2) + " size: " + getFileSize(path2) + ")");
			}
		}
		Utils.saveToFile(observedDir + "_vs_" + expectedDir + ".txt", w);
	}

    // In v2 but not in v1
	public static Vector v1_v2(Vector v1, Vector v2) {
		Vector w = new Vector();
        HashSet hset = Utils.vector2HashSet(v2);
        for (int i=0; i<v1.size(); i++) {
			String t = (String) v1.elementAt(i);
			if (!hset.contains(t)) {
				w.add(t);
			}
		}
		return new SortUtils().quickSort(w);
	}

	public void runDiff() {
		String dir1 = observedDir;
		String dir2 = expectedDir;
		runDiff(dir1, dir2);
	}

	public static void runDiff(String dir1, String dir2) {
		List list1 = listFilesInDirectory(dir1);
		int lcv = 0;
		Vector v1_not_in_v2 = new Vector();
		Vector v2_not_in_v1 = new Vector();
		for (int i=0; i<list1.size(); i++) {
			String t1 = (String) list1.get(i);
			File f1 = new File(t1);
			if (!f1.isDirectory()) {
				String t2 = t1.replace(dir1, dir2);
				File f2 = new File(t2);
				if (!f1.exists()) {
					System.out.println("File " + f1.getAbsolutePath() + " does not exist.");
				} else if (!f2.exists()) {
					System.out.println("File " + f2.getAbsolutePath() + " does not exist.");
				} else {
					lcv++;
					Vector v1 = Utils.readFile(f1.getAbsolutePath());
					Vector v2 = Utils.readFile(f2.getAbsolutePath());
					v1_not_in_v2.add("\n(" + lcv + ") Lines in " + t1 + " but not in " + t2);
					v1_not_in_v2.addAll(v1_v2(v1, v2));

					v2_not_in_v1.add("\n(" + lcv + ") Lines in " + t2 + " but not in " + t1);
					v2_not_in_v1.addAll(v1_v2(v2, v1));
				}
			}
		}
		Utils.saveToFile(dir1 + "_not_in_" + dir2 + ".txt", v1_not_in_v2);
		Utils.saveToFile(dir2 + "_not_in_" + dir1 + ".txt", v2_not_in_v1);
	}

	public static void main(String[] args) {
		String observedDir = args[0];
		String expectedDir = args[1];
		NCItCompareUtils test = new NCItCompareUtils(observedDir, expectedDir);
		test.run();
	}
}

