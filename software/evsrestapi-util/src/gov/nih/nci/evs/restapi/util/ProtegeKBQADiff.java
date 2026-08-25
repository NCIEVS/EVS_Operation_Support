package gov.nih.nci.evs.restapi.util;
import java.io.*;
import java.text.*;
import java.util.*;


public class ProtegeKBQADiff {

	 public static Vector extractCountStatistics(String qafile) {
		File f1 = new File(qafile);
		if (!f1.exists()) {
			System.out.println(qafile + " does not exists.");
			return null;
		}
		Vector v1 = Utils.readFile(qafile);
		System.out.println("v1: " + v1.size());
        Vector w = new Vector();
		for (int i=0; i<v1.size(); i++) {
			String line = (String) v1.elementAt(i);
			if (line.indexOf("=") == -1) {
				line = line.replace(": ", ":");
				Vector u = StringUtils.parseData(line, ':');
				if (u.size() == 2) {
					line = line.replace(":", ": ");
					if (line.startsWith("*")) {
						line = line.substring(1, line.length());
						line = line.trim();
					}
					w.add(line);
				}
			}
		}
		w = new SortUtils().quickSort(w);
		return w;
	}

	public static Vector format(String qafile1, Vector w1, String qafile2, Vector w2) {
		Vector w = new Vector();
		w.add("Item\t" + qafile1 + "\t" + qafile2);
		for (int i=0; i<w1.size(); i++) {
			String line1 = (String) w1.elementAt(i);
			line1 = line1.replace(": ", ":");
			Vector u1 = StringUtils.parseData(line1, ':');

			String line2 = (String) w2.elementAt(i);
			line2 = line2.replace(": ", ":");
			Vector u2 = StringUtils.parseData(line2, ':');

			String item = (String) u1.elementAt(0);
			String s1 = (String) u1.elementAt(1);
			String s2 = (String) u2.elementAt(1);

			w.add(item + "\t" + s1 + "\t" + s2);
		}
		return w;
	}

    public static void run(String qafile1, String qafile2) {
		Vector w1 = extractCountStatistics(qafile1);
		Vector w2 = extractCountStatistics(qafile2);
		Utils.dumpVector(qafile1, w1);
        Utils.dumpVector(qafile2, w2);
        Vector w = format(qafile1, w1, qafile2, w2);
        System.out.println("\n");
        Utils.saveToFile("SummaryData.txt", w);
	}

	public static void main(String args[]) {
		String qafile1 = args[0];
		String qafile2 = args[1];
		run(qafile1, qafile2);
	}
}