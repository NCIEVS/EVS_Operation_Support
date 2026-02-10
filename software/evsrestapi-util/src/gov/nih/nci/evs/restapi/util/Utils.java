package gov.nih.nci.evs.restapi.util;
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

/**
 * <!-- LICENSE_TEXT_START -->
 * Copyright 2022 Guidehouse. This software was developed in conjunction
 * with the National Cancer Institute, and so to the extent government
 * employees are co-authors, any rights in such works shall be subject
 * to Title 17 of the United States Code, section 105.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the disclaimer of Article 3,
 *      below. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *   2. The end-user documentation included with the redistribution,
 *      if any, must include the following acknowledgment:
 *      "This product includes software developed by Guidehouse and the National
 *      Cancer Institute."   If no such end-user documentation is to be
 *      included, this acknowledgment shall appear in the software itself,
 *      wherever such third-party acknowledgments normally appear.
 *   3. The names "The National Cancer Institute", "NCI" and "Guidehouse" must
 *      not be used to endorse or promote products derived from this software.
 *   4. This license does not authorize the incorporation of this software
 *      into any third party proprietary programs. This license does not
 *      authorize the recipient to use any trademarks owned by either NCI
 *      or GUIDEHOUSE
 *   5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED
 *      WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *      OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE
 *      DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
 *      GUIDEHOUSE, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT,
 *      INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 *      BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *      LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *      CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 *      LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 *      ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *      POSSIBILITY OF SUCH DAMAGE.
 * <!-- LICENSE_TEXT_END -->
 */

/**
 * @author EVS Team
 * @version 1.0
 *
 * Modification history:
 *     Initial implementation kim.ong@nih.gov
 *
 */
public class Utils {

    public static void dumpHashMap(String label, HashMap hmap) {
		System.out.println("\n" + label + ":");
		if (hmap == null) {
			System.out.println("\tNone");
			return;
		}
		Vector key_vec = new Vector();
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			key_vec.add(key);
		}
		key_vec = new SortUtils().quickSort(key_vec);
		for (int i=0; i<key_vec.size(); i++) {
			String key = (String) key_vec.elementAt(i);
			String value = (String) (String) hmap.get(key);
			System.out.println(key + " --> " + value);
		}
		System.out.println("\n");
	}

    public static void dumpMultiValuedHashMap(String label, HashMap hmap) {
   	    System.out.println("\n" + label + ":");
		if (hmap == null) {
			System.out.println("\tNone");
			return;
		}
		Vector key_vec = new Vector();
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String nv = (String) it.next();
			key_vec.add(nv);
		}
		key_vec = new SortUtils().quickSort(key_vec);
		for (int k=0; k<key_vec.size(); k++) {
			String nv = (String) key_vec.elementAt(k);
			System.out.println("\n");
			Vector v = (Vector) hmap.get(nv);
			for (int i=0; i<v.size(); i++) {
				String q = (String) v.elementAt(i);
				System.out.println(nv + " --> " + q);
			}
		}
		System.out.println("\n");
	}

    public static void dumpVector(String label, Vector v) {
		System.out.println("\n" + label + ":");
		if (v == null) return;
		if (v.size() == 0) {
			System.out.println("\tNone");
			return;
		}
        for (int i=0; i<v.size(); i++) {
			String t = (String) v.elementAt(i);
			int j = i+1;
			System.out.println("\t(" + j + ") " + t);
		}
		System.out.println("\n");
	}

    public static void dumpVector(String label, Vector v, boolean display_label, boolean display_index) {
		if (display_label) {
			System.out.println("\n" + label + ":");
		}
		if (v == null || v.size() == 0) {
			System.out.println("\tNone");
			return;
		}
        for (int i=0; i<v.size(); i++) {
			String t = (String) v.elementAt(i);
			int j = i+1;
			if (display_index) {
				System.out.println("\t(" + j + ") " + t);
			} else {
				System.out.println("\t" + t);
			}
		}
		System.out.println("\n");
	}

    public static void dumpArrayList(String label, ArrayList list) {
		System.out.println("\n" + label + ":");
		if (list == null || list.size() == 0) {
			System.out.println("\tNone");
			return;
		}
        for (int i=0; i<list.size(); i++) {
			String t = (String) list.get(i);
			int j = i+1;
			System.out.println("\t(" + j + ") " + t);
		}
		System.out.println("\n");
	}

    public static void dumpList(String label, List list) {
		System.out.println("\n" + label + ":");
		if (list == null || list.size() == 0) {
			System.out.println("\tNone");
			return;
		}
        for (int i=0; i<list.size(); i++) {
			String t = (String) list.get(i);
			int j = i+1;
			System.out.println("\t(" + j + ") " + t);
		}
		System.out.println("\n");
	}


	 public static String replaceFilename(String filename) {
	    return filename.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
	 }


	public static List<String> findFilesInDirectory(String directory) {
		return findFilesInDirectory(new File(directory));
	}

	public static List<String> findFilesInDirectory(File dir) {
		List<String> list = new ArrayList<String>();
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				List<String> list2 = findFilesInDirectory(file);
				list.addAll(list2);
			} else {
				list.add(file.getAbsolutePath());
			}
		}
		return list;
	}


	 public static void saveToFile(String outputfile, String t) {
		 Vector v = new Vector();
		 v.add(t);
		 saveToFile(outputfile, v);
	 }

	public static void saveToFile(PrintWriter pw, Vector v) {
		if (v != null && v.size() > 0) {
			for (int i=0; i<v.size(); i++) {
				String t = (String) v.elementAt(i);
				pw.println(t);
			}
		}
	}

    public static Vector parseData(String line, char delimiter) {
		if(line == null) return null;
		Vector w = new Vector();
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<line.length(); i++) {
			char c = line.charAt(i);
			if (c == delimiter) {
				w.add(buf.toString());
				buf = new StringBuffer();
			} else {
				buf.append(c);
			}
		}
		w.add(buf.toString());
		return w;
	}

	public static String createStaticVariable(Vector v, String variableName) {
		StringBuffer buf = new StringBuffer();
		buf.append("public static final String[] " + variableName + " = new String[] {").append("\n");
		for (int i=0; i<v.size(); i++) {
			int j = i+1;
			String t = (String) v.elementAt(i);
			buf.append("\"" + t + "\", ");
			if (j % 10 == 0) {
				buf.append("\n");
			}
		}
		String s = buf.toString();
		s = s.trim();
		s = s.substring(0, s.length()-1) + "};";
		return s;
	}

	public static String vector2Delimited(Vector v, String delim) {
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<v.size(); i++) {
			String value = (String) v.elementAt(i);
			buf.append(value);
			if (i < v.size()-1) {
				buf.append(delim);
			}
		}
		return buf.toString();
	}

	public static HashSet vector2HashSet(Vector v) {
		if (v == null) return null;
		HashSet hset = new HashSet();
		for (int i=0; i<v.size(); i++) {
			String t = (String) v.elementAt(i);
			hset.add(t);
		}
		return hset;
	}

	public static Vector array2Vector(String[] a) {
		Vector v = new Vector();
		for (int i=0; i<a.length; i++) {
			String t = (String) a[i];
			v.add(t);
		}
		return v;
	}

	public static Vector hashSet2Vector(HashSet hset) {
		if (hset == null) return null;
		Vector v = new Vector();
		Iterator it = hset.iterator();
		while (it.hasNext()) {
			String t = (String) it.next();
			v.add(t);
		}
		v = new SortUtils().quickSort(v);
		return v;
	}

    public static String changeFileExtension(String filename, String ext) {
		int n = filename.lastIndexOf(".");
		if (n != -1) {
			return filename.substring(0, n) + "." + ext;
		}
		return filename;
	}

	public static HashMap getInverseHashMap(HashMap hmap) {
		HashMap inv_hmap = new HashMap();
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			String value = (String) hmap.get(key);
			Vector v = new Vector();
			if (inv_hmap.containsKey(value)) {
				v = (Vector) inv_hmap.get(value);
			}
			v.add(key);
			inv_hmap.put(value, v);
		}
		return inv_hmap;
	}

    public static Vector listFiles(String directory) {
		Vector w = new Vector();
		Collection<File> c = listFileTree(new File(directory));
		int k = 0;
		Iterator it = c.iterator();
		while (it.hasNext()) {
			File t = (File) it.next();
			k++;
			w.add(t.getName());
		}
		w = new SortUtils().quickSort(w);
		return w;
	}


	public static Collection<File> listFileTree(File dir) {
		Set<File> fileTree = new HashSet<File>();
		if(dir==null||dir.listFiles()==null){
			return fileTree;
		}
		for (File entry : dir.listFiles()) {
			if (entry.isFile()) fileTree.add(entry);
			else fileTree.addAll(listFileTree(entry));
		}
		return fileTree;
	}

    public static boolean checkIfFileExists(String filename) {
		String currentDir = System.getProperty("user.dir");
		File f = new File(currentDir + "\\" + filename);
		if(f.exists() && !f.isDirectory()) {
			return true;
		} else {
			return false;
		}
	}

    public static HashMap createRelationshipHashMap(Vector v) {
		HashMap hmap = new HashMap();
		for (int i=0; i<v.size(); i++) {
			String t = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(t, '|');
			String src = (String) u.elementAt(0);
			String rel = (String) u.elementAt(1);
			String target = (String) u.elementAt(2);
			if (u.size() == 6) {
				src = (String) u.elementAt(1);
				rel = (String) u.elementAt(3);
				target = (String) u.elementAt(5);
			}
			HashMap sub_map = new HashMap();
			if (hmap.containsKey(src)) {
				sub_map = (HashMap) hmap.get(src);
			}
			Vector w = new Vector();
			if (sub_map.containsKey(rel)) {
				w = (Vector) sub_map.get(rel);
			}
			if (!w.contains(target)) {
				w.add(target);
			}
			sub_map.put(rel, w);
			hmap.put(src, sub_map);

		}
		return hmap;
	}

    public static HashMap createInverseRelationshipHashMap(Vector v) {
		HashMap hmap = new HashMap();
		for (int i=0; i<v.size(); i++) {
			String t = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(t, '|');
			String src = (String) u.elementAt(0);
			String rel = (String) u.elementAt(1);
			String target = (String) u.elementAt(2);
			if (u.size() == 6) {
				src = (String) u.elementAt(1);
				rel = (String) u.elementAt(3);
				target = (String) u.elementAt(5);
			}
			HashMap sub_map = new HashMap();
			if (hmap.containsKey(target)) {
				sub_map = (HashMap) hmap.get(target);
			}
			Vector w = new Vector();
			if (sub_map.containsKey(rel)) {
				w = (Vector) sub_map.get(rel);
			}
			if (!w.contains(src)) {
				w.add(src);
			}
			sub_map.put(rel, w);
			hmap.put(target, sub_map);
		}
		return hmap;
	}


    public static void generate_construct_statement(String method_name, String params, String filename) {
		Vector w = create_construct_statement(method_name, params, filename);
		StringUtils.dumpVector(w);
	}

    public static Vector create_construct_statement(String method_name, String params, String filename) {
		Vector u = readFile(filename);
		Vector w = new Vector();
		StringBuffer buf = new StringBuffer();
		w.add("public String " + method_name + "(" + params + ") {");
		//w.add("\tString prefixes = getPrefixes();");
		w.add("\tStringBuffer buf = new StringBuffer();");
		//w.add("\tbuf.append(prefixes);");
		for (int i=0; i<u.size(); i++) {
			String t = (String) u.elementAt(i);
			t = StringUtils.trimLeadingBlanksOrTabs(t);
			if (!t.startsWith("#")) {
				t = StringUtils.escapeDoubleQuotes(t);
				t = "\"" + t + "\"";
				t = "buf.append(" + t + ").append(\"\\n\");";
				w.add("\t" + t);
			}
		}
		w.add("\treturn buf.toString();");
		w.add("}");
		return w;
	}

    public static boolean deleteFile(String filename) {
        File file = new File(filename);
        if(file.delete())
        {
            return true;
        }
        else
        {
            return false;
        }
    }

/*
    public static Vector readFile(String filename) {
        Charset encoding = Charset.defaultCharset();
		File file = new File(filename);
		try {
			return handleFile(file, encoding);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
    }
*/

	public static Vector readFile(String filename) {
		Vector v = new Vector();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(filename));
			String line = reader.readLine();
			while (line != null) {
				if (line != null) {
					v.add(line);
				}
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return v;
	}


    private static Vector handleFile(File file, Charset encoding) throws IOException {
        try (InputStream in = new FileInputStream(file);
            Reader reader = new InputStreamReader(in, encoding);
            Reader buffer = new BufferedReader(reader)) {
            return handleCharacters(buffer);
        }
    }

    private static String removeNewLineCharacters(String s) {
		while (s.length() > 0 && s.endsWith("\n")) {
			s = s.substring(0, s.length()-1);
		}
		return s;
	}

    private static Vector handleCharacters(Reader reader)
            throws IOException {
		Vector w = new Vector();
        int r;
        String s = "";
        while ((r = reader.read()) != -1) {
            char ch = (char) r;
            //if (ch == 10) {
			if (ch == '\n') {
				//s = s.trim();
				w.add(s);
				s = "";
			} else {
				s = s + ch;
			}
        }
        //s = s.trim();
        if (s.length() > 0) {
			w.add(s);
		}
        return w;
    }

    public static void saveToFile(String target_file, Vector w) {
	    OutputStream outStream = null;
    	try{
    	    File bfile =new File(target_file);
    	    outStream = new FileOutputStream(bfile);
    	    for (int i=0; i<w.size(); i++) {
				String line = (String) w.elementAt(i) + "\n";
				byte[] buffer = line.getBytes();
    	    	outStream.write(buffer, 0, buffer.length);
    	    }
    	    outStream.close();
    	    System.out.println("File " + target_file + " generated.");
    	}catch(IOException e){
    		e.printStackTrace();
    	}
    }

    public static void copyFile(String filename) {
		Vector v = readFile(filename);
		saveToFile("copy_" + filename, v);
	}

	public static Table constructTable(String label, Vector heading_vec, Vector data_vec) {
        return Table.construct_table(label, heading_vec, data_vec);
	}

	////////////////////////////////////////////////////////////////////////
	public static HashSet extractColumnData(String filename, int col, char delim) {
		Vector v = readFile(filename);
		HashSet hset = new HashSet();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, delim);
			String value = (String) u.elementAt(col);
			if (!hset.contains(value)) {
				hset.add(value);
			}
		}
		return hset;
	}

	public static String extractRowData(String line, Vector<Integer> col_vec, char delim) {
		Vector u = StringUtils.parseData(line, delim);
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<col_vec.size(); i++) {
			Integer int_obj = col_vec.elementAt(i);
			int k = Integer.parseInt(int_obj.toString());
			buf.append((String) u.elementAt(k)).append(delim);
		}
		String s = buf.toString();
		if (col_vec.size() > 0) {
			s = s.substring(0, s.length()-1);
		}
		return s;
	}

	public static void dumpHeading(String filename) {
		char delim = '\t';
		dumpHeading(filename, delim);
	}

	public static void dumpHeading(String filename, char delim) {
		Vector v = readFile(filename);
        String line = (String) v.elementAt(0);
        Vector u = StringUtils.parseData(line, delim);
        Utils.dumpVector(line, u);
	}

	public static Vector extractColumnData(String filename, Vector<Integer> col_vec, char delim) {
		Vector v = readFile(filename);
        Vector w = new Vector();
        String heading = extractRowData((String) v.elementAt(0), col_vec, delim);
        w.add(heading);
		for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			line = line.trim();
			if (line.length() > 0) {
				line = extractRowData((String) v.elementAt(i), col_vec, delim);
				w.add(line);
			}
		}
		return w;
	}

	public static Vector extractColumnData(String filename, String delimitedString, char delim) {
		Vector<Integer> col_vec = new Vector();
		Vector u = StringUtils.parseData(delimitedString, '|');
		for (int i=0; i<u.size(); i++) {
			String int_str = (String) u.elementAt(i);
			int k = Integer.parseInt(int_str);
			Integer int_obj = Integer.valueOf(k);
			col_vec.add(int_obj);
		}
		return extractColumnData(filename, col_vec, delim);
	}

	public static Vector extractRowsFromFile(String filename, Vector req_vec) {
//Recombinant Amphiregulin|C1000|P90|Recombinant Amphiregulin|P383$PT|P384$NCI
		Vector w = new Vector();
		Vector v = readFile(filename);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			boolean match = true;
			for (int j=0; j<u.size(); j++) {
				for (int k=0; k<req_vec.size(); k++) {
					String req = (String) req_vec.elementAt(k);
					if (!u.contains(req)) {
						match = false;
						break;
					}
				}
			}
			if (match) {
				w.add(line);
			}
		}
		return new SortUtils().quickSort(w);
	}

/////////////////////////////////////////////////////////////////////////////////////////
	public static Vector removeNodeCodes(String htmlfile, String prefix) {
		Vector v = readFile(htmlfile);
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			int n = line.indexOf("(" + prefix);
			if (n != -1 && line.endsWith(")")) {
				String t = line.substring(0, n-1);
				w.add(t);
			} else {
				w.add(line);
			}
		}
		return w;
	}

    public static Vector toHierarchyFormat(String datafile, String prefix) {
		return toHierarchyFormat(readFile(datafile), prefix, "No match");
    }

    public static Vector toHierarchyFormat(Vector v, String prefix) {
		return toHierarchyFormat(v, prefix, "No match");
    }

    public static Vector toHierarchyFormat(Vector v, String prefix, String childNodeLabel) {
		Vector w = new Vector();
		String rootCode = null;
		String label = null;
		int knt = 0;
		int sub_knt = 0;
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			line = line.trim();
			if (line.startsWith("(")) {
				if (label != null && sub_knt == 0) {
					w.add(label + "|" + rootCode + "|" + childNodeLabel + "|NA");
				}
				int n = line.lastIndexOf(")");
				label = line.substring(n+1, line.length());
				knt++;
				rootCode = prefix + knt;
				sub_knt = 0;

			} else {
				if (line.indexOf("|") != -1) {
					w.add(label + "|" + rootCode + "|" + line);
					sub_knt++;
				}
			}
		}
		return w;
	}

	public static boolean isPureAscii(String v) {
		return Charset.forName("US-ASCII").newEncoder().canEncode(v);
	}

	public static Vector searchForNonASCII(String filename) {
	    Vector v = readFile(filename);
	    Vector w = new Vector();
	    for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (!isPureAscii(line)) {
				w.add(line);
			}
		}
		return w;
	}

    public static HashMap vector2MultiValuedHashMap(Vector v, char delim, int keyCol, int valueCol) {
		HashMap hmap = new HashMap();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, delim);
			String key = (String) u.elementAt(keyCol);
			String value = (String) u.elementAt(valueCol);
			Vector w = new Vector();
			if (hmap.containsKey(key)) {
				w = (Vector) hmap.get(key);
			}
			if (!w.contains(value)) {
				w.add(value);
			}
			hmap.put(key, w);
		}
		return hmap;
	}

    public static String[] vector2Array(Vector v) {
		String[] a = new String[v.size()];
		for (int i=0; i<v.size(); i++) {
			a[i] = (String) v.elementAt(i);
		}
		return a;
	}

	public static String insert(String file1, String file2, String locMarker) {
		Vector v1 = Utils.readFile(file1);
		Vector v2 = Utils.readFile(file2);
		Vector w = new Vector();

		for (int i=0; i<v1.size(); i++) {
			String line = (String) v1.elementAt(i);
			if (line.indexOf(locMarker) != -1) {
				for (int j=0; j<v2.size(); j++) {
					String line2 = (String) v2.elementAt(j);
					w.add(line2);
				}
			} else {
				w.add(line);
			}
		}
		int n = file2.lastIndexOf(".");
		String outputfile = file2.substring(0, n) + ".owl";
		Utils.saveToFile(outputfile, w);
		return outputfile;
	}

	static String INSERT_CONTENT_HERE = "INSERT CONTENT HERE";
	public static String insert(String file1, String file2) {
		return insert(file1, file2, INSERT_CONTENT_HERE);
	}

    public static String vector2DelimitedString(Vector v) {
		if (v == null) return "";
		v = new SortUtils().quickSort(v);
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<v.size(); i++) {
			String value = (String) v.elementAt(i);
			buf.append(value).append("|");
		}
		String t = buf.toString();
		if (t.endsWith("|")) {
			t = t.substring(0, t.length()-1);
		}
		String decoded = HTMLDecoder.decode(t);
		return decoded.trim();
	}

	public static HashMap createMultiValuedHashMap(Vector v, char delim, int keyCol, int valueCol, boolean skipFirstLine) {
		HashMap hmap = new HashMap();
        int istart = 0;
        if (skipFirstLine) {
			istart = 1;
		}
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			line = line.trim();
			if (line.length() > 0) {
				Vector u = StringUtils.parseData(line, delim);
				String key = (String) u.elementAt(keyCol);
				String value = (String) u.elementAt(valueCol);
				Vector w = new Vector();
				if (hmap.containsKey(key)) {
					w = (Vector) hmap.get(key);
				}
				if (!w.contains(value)) {
					w.add(value);
				}
				hmap.put(key, w);
			}
		}
		return hmap;
	}

	public static HashMap createMultiValuedHashMap(Vector v, char delim, int keyCol, int valueCol) {
		Vector<Integer> keyCols = new Vector();
		keyCols.add(Integer.valueOf(keyCol));
		boolean skipFirstLine = false;
		return createMultiValuedHashMap(v, delim, keyCols, valueCol, skipFirstLine);
	}

	public static HashMap createMultiValuedHashMap(Vector v, char delim, Vector<Integer> keyCols, int valueCol, boolean skipFirstLine) {
		HashMap hmap = new HashMap();
        int istart = 0;
        if (skipFirstLine) {
			istart = 1;
		}
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			line = line.trim();
			if (line.length() > 0) {
				Vector u = StringUtils.parseData(line, delim);
				String value = (String) u.elementAt(valueCol);
				StringBuffer buf = new StringBuffer();
				for (int j=0; j<keyCols.size(); j++) {
					Integer int_obj = (Integer) keyCols.elementAt(j);
					int k = int_obj.intValue();
					buf.append((String) u.elementAt(k)).append("" + delim);
				}
				String key = buf.toString();
				if (key.endsWith("" + delim)) {
					key = key.substring(0, key.length()-1);
				}
				Vector w = new Vector();
				if (hmap.containsKey(key)) {
					w = (Vector) hmap.get(key);
				}
				if (!w.contains(value)) {
					w.add(value);
				}
				hmap.put(key, w);
			}
		}
		return hmap;
	}

	public static HashMap createMultiValuedHashMap(String filename, char delim, int keyCol, int valueCol, boolean skipFirstLine) {
		HashMap hmap = new HashMap();
        int istart = 0;
        if (skipFirstLine) {
			istart = 1;
		}
		Vector v = Utils.readFile(filename);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			line = line.trim();
			if (line.length() > 0) {
				Vector u = StringUtils.parseData(line, delim);
				String key = (String) u.elementAt(keyCol);
				String value = (String) u.elementAt(valueCol);
				Vector w = new Vector();
				if (hmap.containsKey(key)) {
					w = (Vector) hmap.get(key);
				}
				if (!w.contains(value)) {
					w.add(value);
				}
				hmap.put(key, w);
			}
		}
		return hmap;
	}

	public static Vector compareMultivaluedHashMaps(String title, String keyLabel, String valueLabel, HashMap hmap1, HashMap hmap2) {
		boolean reportUnchanged = false;
		return compareMultivaluedHashMaps(title, keyLabel, valueLabel, hmap1, hmap2, reportUnchanged);
	}

	public static Vector compareMultivaluedHashMaps(String title, String keyLabel, String valueLabel, HashMap hmap1, HashMap hmap2, boolean reportUnchanged) {
		Vector w = new Vector();
		w.add(title + "\t\t\t");
		w.add(keyLabel + "\t" + "Action\tPrevious Value\tCurrent Value");
		Iterator it = hmap1.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			Vector w1 = (Vector) hmap1.get(key);
			String s1 = vector2DelimitedString(w1);
			if (hmap2.containsKey(key)) {
				Vector w2 = (Vector) hmap2.get(key);
				String s2 = vector2DelimitedString(w2);
				if (s1.compareTo(s2) != 0) {
					w.add(key + "\t" + "Modify" + "\t" + s1 + "\t" + s2);
				} else {
					if (reportUnchanged) {
					    w.add(key + "\t" + "Unchanged" + "\t" + s1 + "\t" + s2);
					}
				}

			} else {
				w.add(key + "\t" + "Delete" + "\t" + s1 + "\t" + "");
			}
		}

		it = hmap2.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			Vector w2 = (Vector) hmap2.get(key);
			String s2 = vector2DelimitedString(w2);
			if (!hmap1.containsKey(key)) {
				w.add(key + "\t" + "Add" + "\t" + s2 + "\t" + "");
			}
		}
		return w;
	}

}
