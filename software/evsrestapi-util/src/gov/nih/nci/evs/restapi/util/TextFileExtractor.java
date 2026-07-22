package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;
import java.io.*;
import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.ZoneId;

public class TextFileExtractor {
	public TextFileExtractor() {

	}

	public static int findLastOccurrenceLineNumber(Vector v, String target) {
		Vector w = new Vector();
		for (int i=0; i<v.size()-1; i++) {
			String line = (String) v.elementAt(i);
			i++;
			String nextLine = (String) v.elementAt(i);
			if (line.indexOf(target) != -1 && nextLine.indexOf(target) == -1) {
				return i;
			}
		}
		return -1;
	}


	public static int findLineNumber(Vector v, String target) {
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (line.indexOf(target) != -1) {
				return i;
			}
		}
		return -1;
	}

	public static int reverseFindLineNumber(Vector v, String target) {
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			int k = v.size() - i - 1;
			String line = (String) v.elementAt(k);
			if (line.indexOf(target) != -1) {
				return k;
			}
		}
		return -1;
	}

	public static Vector extractLines(Vector v, int istart, int iend) {
		Vector w = new Vector();
		for (int i=istart; i<iend; i++) {
			String line = (String) v.elementAt(i);
			w.add(line);
		}
		return w;
	}

	public static Vector extractLinesFromFile(String filename, int istart, int iend) {
		Vector v = Utils.readFile(filename);
		Vector w = new Vector();
		for (int i=istart; i<iend; i++) {
			String line = (String) v.elementAt(i);
			w.add(line);
		}
		return w;
	}

	public static void extractLinesFromFile(String filename, int istart, int iend, String outputfile) {
		Vector w = Utils.readFile(filename);
		extractLines(w, istart, iend, outputfile);
		Utils.saveToFile(outputfile, w);
	}

	public static void extractLines(Vector w, int istart, int iend, String outputfile) {
		w = extractLines(w, istart, iend);
		Utils.saveToFile(outputfile, w);
	}

	public static Vector extractDistinctColumnValues(String filename, int col) {
        return extractDistinctColumnValues(filename, col, '\t');
	}

	public static Vector extractDistinctColumnValues(String filename, int col, char delim) {
		HashSet hset = new HashSet();
		Vector v = Utils.readFile(filename);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, delim);
			String value = (String) u.elementAt(col);
			if (!hset.contains(value)) {
				hset.add(value);
			}
		}
		Vector w = Utils.hashSet2Vector(hset);
		return new SortUtils().quickSort(w);
	}

	public static int findLineNumber(String filename, String target) {
		Vector v = Utils.readFile(filename);
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (line.indexOf(target) != -1) {
				return i;
			}
		}
		return -1;
	}
}