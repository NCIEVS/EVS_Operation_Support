package gov.nih.nci.evs.restapi.util;

import java.io.*;
import java.net.URI;
import java.text.*;
import java.util.*;


public class ClassNameFinder {
    static ArrayList<String> JAVA_PACKAGES = null;
    static ArrayList<String> excluded = null;

    static {
		JAVA_PACKAGES = new ArrayList<String>();
		JAVA_PACKAGES.add("java.lang");
		JAVA_PACKAGES.add("java.io");
		JAVA_PACKAGES.add("java.util");
		JAVA_PACKAGES.add("java.net");
		JAVA_PACKAGES.add("java.text");
		JAVA_PACKAGES.add("java.URI");
		JAVA_PACKAGES.add("java.swing");
		JAVA_PACKAGES.add("java.awt");
		JAVA_PACKAGES.add("java.event");
		JAVA_PACKAGES.add("java.util.Scanner");
		JAVA_PACKAGES.add("java.awt.event");
		JAVA_PACKAGES.add("javax.swing");

		excluded = new ArrayList<String>();
        excluded.add("StreamWriter");
		excluded.add("URLClassLoader");
		excluded.add("Comparator");
		excluded.add("HSSFWorkbook");
		excluded.add("TreeSelectionListener");
		excluded.add("DomDriver");
		excluded.add("GsonBuilder");
		excluded.add("JsonParser");
		excluded.add("XStream");
		excluded.add("JsonParser");
		excluded.add("DomDriver");
		excluded.add("JSONObject");
        excluded.add("Runtime");

		excluded.add("ActionListener");
		excluded.add("AsciiToExcelFormatter");
		excluded.add("BorderStyle");
		excluded.add("ByteBuffer");
		excluded.add("CellRangeAddress");
		excluded.add("CharBuffer");
		excluded.add("Charset");
		excluded.add("CodeGenTest");
		excluded.add("CSVReader");
		excluded.add("DataFlavor");
		excluded.add("DataFormatter");
		excluded.add("DatePicker");
		excluded.add("DatePickerSettings");
		excluded.add("DateTimeFormatter");
		excluded.add("DefaultMutableTreeNode");
		excluded.add("DefaultTreeCellRenderer");
		excluded.add("DefaultTreeModel");
		excluded.add("DocumentBuilderFactory");
		excluded.add("Duration");
		excluded.add("EVSRESTAPIConfig");
		excluded.add("Files");
		excluded.add("FillPatternType");
		excluded.add("HostnameVerifier");
		excluded.add("HSSFColorPredefined");
		excluded.add("HSSFDataFormatter");
		excluded.add("HSSFFont");
		excluded.add("HSSFShape");
		excluded.add("HttpClients");
		excluded.add("HttpEntity");
		excluded.add("HttpHeaders");
		excluded.add("HttpPost");
		excluded.add("ImageIO");
		excluded.add("IndexedColors");
		excluded.add("JFileChooser");
		excluded.add("JSONValue");
		excluded.add("JTable");
		excluded.add("LevenshteinDistance");
		excluded.add("LexItem");
		excluded.add("LinkedMultiValueMap");
		excluded.add("LocalDate");
		excluded.add("LoggerFactory");
		excluded.add("LogManager");
		excluded.add("LvgApi");
		excluded.add("MediaType");
		excluded.add("Pattern");
		excluded.add("PdfReader");
		excluded.add("PdfReaderContentParser");
		excluded.add("PorterStemmer");
		excluded.add("RestTemplateBuilder");
		excluded.add("SecureRandom");
		excluded.add("SimpleTextExtractionStrategy");
		excluded.add("SSLContext");
		excluded.add("StringHttpMessageConverter");
		excluded.add("StringSelection");
		excluded.add("StringSubstitutor");
		excluded.add("ToAsciiApi");
		excluded.add("ToInflection");
		excluded.add("ToLowerCase");
		excluded.add("TreeFrame");
		excluded.add("TreePath");
		excluded.add("TrustManager");
		excluded.add("UnicodeScript");
		excluded.add("WorkbookFactory");
		excluded.add("X509TrustManager");
		excluded.add("XDDFDataSourcesFactory");
		excluded.add("XMLHttpRequest");
		excluded.add("XSSFFont");
		excluded.add("XSSFWorkbook");
		excluded.add("XWPFDocument");
		excluded.add("XWPFHyperlinkRun");
		excluded.add("YearMonth");
		excluded.add("ZipEntry");
		excluded.add("ZipInputStream");
		excluded.add("ZipOutputStream");

	}

    public static boolean isInJavaIO(String className) {
        try {
            Class<?> clazz = Class.forName("java.io." + className);
            /*
            return clazz.getPackage() != null &&
                   "java.io".equals(clazz.getPackage().getName());
            */
            return true;

        } catch (ClassNotFoundException e) {
            return false;
        } catch (Exception e) {
             System.err.println("Error checking class: " + e.getMessage());
            return false;
        }
    }

    public static boolean isInJavaPackage(String packageName, String className) {
        try {
            Class<?> clazz = Class.forName(packageName + "." + className);
            return true;

        } catch (ClassNotFoundException e) {
            return false;
        } catch (Exception e) {
            System.err.println("Error checking class: " + e.getMessage());
            return false;
        }
	}

    public static boolean isInJavaPackages(String className) {
		for (int i=0; i<JAVA_PACKAGES.size(); i++) {
			String packageName = (String) JAVA_PACKAGES.get(i);
			boolean bool = isInJavaPackage(packageName, className);
			if (bool) return bool;
		}
		return false;
	}

    public static boolean isInJavaUtil(String className) {
		return isInJavaPackage("java.util", className);
		/*
        try {
            Class<?> clazz = Class.forName("java.util." + className);
            return true;

        } catch (ClassNotFoundException e) {
            return false;
        } catch (Exception e) {
            System.err.println("Error checking class: " + e.getMessage());
            return false;
        }
        */
    }

    public static boolean isInJavaIOorUtil(String className) {
		return isInJavaIO(className) || isInJavaUtil(className);
	}

    public static boolean allUpperCases(String str) {
		String str_uc = str.toUpperCase();
		if (str_uc.equals(str)) return true;
		return false;
	}

    public static boolean startsWithLowerCase(String str) {
	    char c = str.charAt(0);
	    String s = "" + c;
	    return !allUpperCases(s);
	}

	public static boolean startsWithLowerCaseOrAllUpperCases(String str) {
		return allUpperCases(str) || startsWithLowerCase(str);
	}

	public static boolean isAlphaNumeric(String str) {
		if (str == null || str.isEmpty()) return false;
		for (char c : str.toCharArray()) {
			if (!Character.isLetterOrDigit(c)) {
				return false;
			}
		}
		return true;
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
			if (line.indexOf("=") != -1) {
				line = line.trim();
				int n = line.indexOf("=");
				String s0 = line.substring(n+1, line.length());
				if (line.indexOf(".") != -1) {
					Vector u0 = StringUtils.parseData(s0, '.');
					s0 = (String) u0.elementAt(0);
					s0 = s0.trim();
					if (s0.indexOf(" ") == -1) {
						char c = s0.charAt(0);
						if (Character.isAlphabetic(c)) {
							String firstChar = "" + s0.charAt(0);
							String uc_firstChar = firstChar.toUpperCase();
							if (firstChar.equals(uc_firstChar)) {
								if (!containsCaseSensitive(excluded, s0)) {
									if (!startsWithLowerCaseOrAllUpperCases(s0)) {
										if (s0.length() > 0 && isAlphaNumeric(s0) && !isInJavaPackages(s0)) {
											hset.add(s0);
										}
								    }
								}
							}
						}
					}
				}
			}

			if (line.indexOf("new ") != -1) {
				line = line.trim();
				int n = line.indexOf("(");
				if (n != -1) {
					String s0 = line.substring(0, n);
					Vector u0 = StringUtils.parseData(s0, ' ');
					s0 = (String) u0.elementAt(u0.size()-1);
					if (!startsWithLowerCaseOrAllUpperCases(s0)) {
						if (!containsCaseSensitive(excluded, s0) && isAlphaNumeric(s0) && !isInJavaPackages(s0)) {
							hset.add(s0);
						}
					}
				} else {
					n = line.indexOf("[");
					if (n != -1) {
						String s0 = line.substring(0, n);
						Vector u0 = StringUtils.parseData(s0, ' ');
						s0 = (String) u0.elementAt(u0.size()-1);
						if (!startsWithLowerCaseOrAllUpperCases(s0)) {
							if (!containsCaseSensitive(excluded, s0) && isAlphaNumeric(s0) && !isInJavaPackages(s0)) {
								hset.add(s0);
							}
						}
					}
				}
			}

			line = line.trim();
			line = line.replace("final","");
			if (line.indexOf("new ") != -1 && line.indexOf("return") == -1 && line.indexOf("throw") == -1
			    && line.indexOf("//") == -1 && line.indexOf("List<") == -1 && line.indexOf("Vector<") == -1) {

				String s1 = line.replace("(", " ");
				Vector u0 = StringUtils.parseData(s1, ' ');
				for (int k=0; k<u0.size(); k++) {
					String t0 = (String) u0.elementAt(k);
					if (t0.equals("new")) {
						String t1 = (String) u0.elementAt(k+1);
						if (!containsCaseSensitive(excluded, t1)) {
							if (t0.indexOf("gov.nih.nci.evs.restapi") == -1
							    && t0.indexOf("[") == -1 && t0.compareTo("new") != 0) {
								if (!startsWithLowerCaseOrAllUpperCases(t0) && isAlphaNumeric(t0) && !isInJavaPackages(t0)) {
									hset.add(t0);
								}
							}
						}
					}
				}

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
											if (!startsWithLowerCaseOrAllUpperCases(t8) && isAlphaNumeric(t8) && !isInJavaPackages(t8)) {
												hset.add(t8);
											}
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
											if (!startsWithLowerCaseOrAllUpperCases(t8) && isAlphaNumeric(t8) && !isInJavaPackages(t8)) {
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
        return w;
	}

    public static Vector getSourceFilePathNames(String foldername) {
		if (foldername == null) {
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
			}
		}
		srcfiles = new SortUtils().quickSort(srcfiles);
        return srcfiles;
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

	public static Vector findClassNames(String foldername) {
		Vector w = new Vector();
		if (foldername == null) {
			w = run();
		} else {
			w = run(foldername);
		}
		Vector w1 = getClassNames(foldername);
		w.removeElement("");
		w.addAll(w1);
		w = removeDuplicates(w);
		w = new SortUtils().quickSort(w);
		return w;
	}

	public static void main(String args[]) {
		String foldername = null;
        if (args.length > 0) {
			foldername = args[0];
		}
		Vector w = findClassNames(foldername);
		w = removeDuplicates(w);
		w = new SortUtils().quickSort(w);
		Utils.dumpVector("classNames", w);
	}
}

