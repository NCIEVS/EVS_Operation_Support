package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;

import java.io.*;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

	public static void main(String[] args) {
		String observedDir = args[0];
		String expectedDir = args[1];
		NCItCompareUtils test = new NCItCompareUtils(observedDir, expectedDir);
		test.run();
	}
}

