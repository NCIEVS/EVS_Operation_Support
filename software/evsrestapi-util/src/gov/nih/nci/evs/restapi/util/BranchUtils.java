package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;
import gov.nih.nci.evs.restapi.bean.*;
import java.io.*;
import java.net.*;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.*;
import java.util.*;

public class BranchUtils {
	static String PARENT_CHILD_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile; // "parent_child.txt";
    static HierarchyHelper hh = null;

    static {
		hh = new HierarchyHelper(Utils.readFile(PARENT_CHILD_FILE));
	}

    public static Vector findRoots(String code, boolean codeOnly) {
        if (!HyperlinkHelper.isNCItCode(code)) {
			return new Vector();
		}
		Vector w = new Vector();
		Vector v = null;
		Stack stack = new Stack();
		stack.push(code);
		HashSet hset = new HashSet();
		while (!stack.isEmpty()) {
			String next_code = (String) stack.pop();
			v = hh.getSuperclassCodes(next_code);
			if (v == null) {
				if (!hset.contains(next_code)) {
					if (codeOnly) {
						w.add(next_code);
					} else {
						w.add(hh.getLabel(next_code) + "|" + next_code);
					}
					hset.add(next_code);
				}
			} else {
				for (int i=0; i<v.size(); i++) {
					String supclass_code = (String) v.elementAt(i);
					stack.push(supclass_code);
				}
			}
		}
		hset.clear();
		return new SortUtils().quickSort(w);
	}

    public static Vector findRoots(Vector codes, boolean codeOnly) {
		Vector w0 = new Vector();
		for (int i=0; i<codes.size(); i++) {
			String code = (String) codes.elementAt(i);
			Vector w = findRoots(code, codeOnly);
			w0.addAll(w);
		}
		return w0;
	}

	public static Vector extractColumnData(String filename, int col) {
		return extractColumnData(filename, col, '\t', true);
	}

	public static Vector extractColumnData(String filename, int col, char delim, boolean skipHeading) {
		Vector v = Utils.readFile(filename);
		Vector w = new Vector();
		int istart = 0;
		if (skipHeading) {
			istart = 1;
		}
		for (int i=istart; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, delim);
			if (u.size() > col) {
				String code = (String) u.elementAt(col);
				w.add(code);
			}
		}
		return w;
	}

	public static Vector appendRoots(String filename, int col, char delim, boolean skipHeading) {
		Vector v = Utils.readFile(filename);
		Vector w = new Vector();
		HashSet hset = new HashSet();
		Vector roots = new Vector();
		int istart = 0;
		if (skipHeading) {
			istart = 1;
			w.add((String) v.elementAt(0));
		}
		int lcv = 0;
		int i = 0;
		for (i=istart; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			lcv++;
			if (lcv == 100) {
				System.out.println(i + " out of " + v.size() + " completed.");
				lcv = 0;
			}
			Vector u = StringUtils.parseData(line, delim);
			if (u.size() > col) {
				String code = (String) u.elementAt(col);
				if (code.length() > 0 && code.indexOf("No match") == -1) {
					if (HyperlinkHelper.isNCItCode(code)) {
						Vector root_data = findRoots(code, false);
						String delimitedstr = Utils.vector2Delimited(root_data, "$");
						for (int j=0; j<root_data.size(); j++) {
							String root = (String) root_data.elementAt(j);
							if (!hset.contains(root)) {
								hset.add(root);
								roots.add(root);
							}
						}
						line = line + "\t" + delimitedstr;
						w.add(line);
					} else {
						w.add(line);
					}
				} else {
					w.add(line);
				}

			} else {
				w.add(line);
			}
		}
		System.out.println(i + " out of " + v.size() + " completed.");
		Utils.dumpVector("roots", roots);
		return w;
	}

	public static void main(String[] args) {
		long ms = System.currentTimeMillis();
		String filename = args[0];
		int col = 3;
		char delim = '\t';
		boolean skipHeading = true;
	    Vector w = appendRoots(filename, col, delim, skipHeading);
	    Utils.saveToFile("roots_" + filename, w);
	    long ms1 = System.currentTimeMillis();
	    long timeElapsed = ms1 - ms;
	    System.out.println("Total runtime " + timeElapsed + " (milli-seconds).");
	}


}

