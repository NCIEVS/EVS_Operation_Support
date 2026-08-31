package gov.nih.nci.evs.restapi.util;

import java.io.*;
import java.net.URI;
import java.text.*;
import java.util.*;


public class ClassNameFinder {
    static ArrayList<String> excluded = null;

    static {
		excluded = new ArrayList<String>();
		excluded.add("String");
		excluded.add("HashSet");
		excluded.add("Vector");
		excluded.add("HashMap");
		excluded.add("Stack");
		excluded.add("URL");
		excluded.add("Vector<String>");

		excluded.add("StringBuilder");
		excluded.add("PrintWriter");
		excluded.add("FileInputStream");
		excluded.add("FileReader");
		excluded.add("BufferedReader");
		excluded.add("StringBuilder");
		excluded.add("StringBuffer");
		excluded.add("java.io.File");
		excluded.add("ArrayList");
		excluded.add("InputStream");
		excluded.add("byte");
		excluded.add("BufferedInputStream");
		excluded.add("DataInputStream");
		excluded.add("InputStreamReader");
		excluded.add("OutputStreamWriter");
		excluded.add("StringEntity");
		excluded.add("StringWriter");

		excluded.add("File");
		excluded.add("FileOutputStream");
		excluded.add("FileWriter");
        excluded.add("StreamWriter");

		excluded.add("JComboBox");
		excluded.add("JFrame");
		excluded.add("JButton");

		excluded.add("JLabel");
		excluded.add("JProgressBar");

		excluded.add("JScrollPane");
		excluded.add("JTextArea");

		excluded.add("JTree");
		excluded.add("StringTokenizer");
		excluded.add("URLClassLoader");
		excluded.add("Comparator");
		excluded.add("HSSFWorkbook");
		excluded.add("Paths");
		excluded.add("GridBagConstraints");
		excluded.add("GridBagLayout");
		excluded.add("GridLayout");
		excluded.add("ImageIcon");
		excluded.add("JPanel");
		excluded.add("JTextField");
		excluded.add("TreeSelectionListener");
		excluded.add("WindowAdapter");
		excluded.add("DomDriver");
		excluded.add("GsonBuilder");
		excluded.add("JsonParser");
		excluded.add("XStream");
		excluded.add("JsonParser");
		excluded.add("DomDriver");
		excluded.add("JSONObject");

	}

    public static String removePackageName(String t) {
		int n = t.lastIndexOf(".");
		if (n != -1) {
			return t.substring(n+1, t.length());
		}
		return t;
	}

    public static boolean containsCaseSensitive(List<String> list, String target) {
        if (list == null || target == null) {
            return false;
        }
        return list.contains(target);
    }

    public static Vector searchClassNames(String filename) {
		HashSet hset = new HashSet();
		Vector v = Utils.readFile(filename);
		v = CommentLineUtils.removeCommentLines(v);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			line = line.trim();
			line = line.replace("final","");
			if (line.indexOf("new ") != -1 && line.indexOf("return") == -1 && line.indexOf("throw") == -1
			    && line.indexOf("//") == -1 && line.indexOf("List<") == -1 && line.indexOf("Vector<") == -1) {
				Vector u = StringUtils.parseData(line, ' ');
				String t = (String) u.elementAt(0);
				if (!containsCaseSensitive(excluded, t)) {
					t = (String) u.elementAt(1);
					Vector u2 = StringUtils.parseData(line, '=');
					for (int k=0; k<u2.size(); k++) {
						String t6 = (String) u2.elementAt(k);
						if (t6.indexOf("new ") != -1) {
							t6 = t6.replace("(", " ");
							Vector u7 = StringUtils.parseData(t6, ' ');
							for (int k1=0; k1<u7.size(); k1++) {
								String t7 = (String) u7.elementAt(k1);
								if (t7.compareTo("new") == 0) {
									String t8 = (String) u7.elementAt(k1+1);
									t8 = t8.replace("<", " ");
									Vector u5 = StringUtils.parseData(t8, ' ');
									t8 = (String) u5.elementAt(0);
									if (!containsCaseSensitive(excluded, t8)) {
										if (t8.indexOf("gov.nih.nci.evs.restapi") == -1
										&& t8.indexOf("[") == -1) {
											hset.add(t8);
										}
									}
								}
							}
						}
					}

					String t1 = (String) u2.elementAt(0);
					Vector u3 = StringUtils.parseData(t1, ' ');
					if (u3.size() == 2) {
						line = line.replace("(", " ");
						Vector u4 = StringUtils.parseData(line, ' ');
						for (int k=0; k<u4.size(); k++) {
							String t2 = (String) u4.elementAt(k);
							if (t2.compareTo("new") == 0) {
								String t3 = (String) u4.elementAt(k+1);
								t3 = t3.replace("<>", "");
								t3 = t3.replace("[]", "");
								if (t3.indexOf("[") != -1) {
									t3 = t3.substring(0, t3.indexOf("["));
								}
								if (!containsCaseSensitive(excluded, t3)) {
									t3 = removePackageName((String) u4.elementAt(k+1));
									t3 = t3.replace("<", " ");
									Vector u5 = StringUtils.parseData(t3, ' ');
									String t8 = (String) u5.elementAt(0);
									if (!containsCaseSensitive(excluded, t8)) {
										if (t8.indexOf("gov.nih.nci.evs.restapi") == -1) {
											hset.add(t8);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		Vector w = Utils.hashSet2Vector(hset);
		return new SortUtils().quickSort(w);
	}

    public static Vector run() {
		String foldername = null;
		return run(foldername);
	}

	public static Vector removeDuplicates(Vector v) {
		HashSet hset = new HashSet();
		for (int i=0; i<v.size(); i++) {
			String t = (String) v.get(i);
			if (!hset.contains(t)) {
				hset.add(t);
			}
		}
		Vector w = Utils.hashSet2Vector(hset);
		return new SortUtils().quickSort(w);

	}

    public static Vector run(String foldername) {
		if (foldername == null) {
			Vector srcfiles = new Vector();
			String currentDir = System.getProperty("user.dir");
			foldername = currentDir + File.separator + "src";
		}
		Vector srcfiles = new Vector();
		List list = FileUtils.listFilesInDirectory(foldername);
        Vector w = new Vector();
		for (int i=0; i<list.size(); i++) {
			String filepath = (String) list.get(i);
			if (filepath.endsWith("java")) {
				srcfiles.add(filepath);
				Vector v = searchClassNames(filepath);
				if (v.size() > 0) {
					w.addAll(v);
				}
			}
		}
        Utils.dumpVector("Java File", srcfiles);
        return w;
	}

    public static Vector getClassNames(String foldername) {
		if (foldername == null) {
			Vector srcfiles = new Vector();
			String currentDir = System.getProperty("user.dir");
			foldername = currentDir + File.separator + "src";
		}
		Vector srcfiles = new Vector();

		List list = FileUtils.listFilesInDirectory(foldername);
        Vector w = new Vector();
		for (int i=0; i<list.size(); i++) {
			String filepath = (String) list.get(i);
			if (filepath.endsWith("java")) {
				String className = getJavaClassName(filepath);
				w.add(className);
			}
		}
        return w;
	}

	public static String getJavaClassName(String filepath) {
		File f = new File(filepath);
		String t = f.getName();
		int n = t.lastIndexOf(File.separator);
		if (n != -1) {
			t = t.substring(n+1, t.length());
		}
		n = t.lastIndexOf(".");
		if (n != -1) {
			t = t.substring(0, n);
		}
		return t;

	}

	public static void main(String args[]) {
		String foldername = null;
		Vector w = new Vector();
		if (args.length == 0) {
			w = run();
		} else {
			foldername = args[0];
			w = run(foldername);
		}
		Vector w1 = getClassNames(foldername);
		w.addAll(w1);
		w = removeDuplicates(w);
		w = new SortUtils().quickSort(w);
		Utils.dumpVector("classNames", w);
	}
}

