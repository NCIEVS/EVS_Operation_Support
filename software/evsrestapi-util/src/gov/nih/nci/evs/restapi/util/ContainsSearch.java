package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;
import java.io.*;
import java.text.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;
import java.nio.charset.Charset;
import opennlp.tools.stemmer.PorterStemmer;

/**
 * <!-- LICENSE_TEXT_START -->
 * Copyright 2008-2017 NGIS. This software was developed in conjunction
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
 *      "This product includes software developed by NGIS and the National
 *      Cancer Institute."   If no such end-user documentation is to be
 *      included, this acknowledgment shall appear in the software itself,
 *      wherever such third-party acknowledgments normally appear.
 *   3. The names "The National Cancer Institute", "NCI" and "NGIS" must
 *      not be used to endorse or promote products derived from this software.
 *   4. This license does not authorize the incorporation of this software
 *      into any third party proprietary programs. This license does not
 *      authorize the recipient to use any trademarks owned by either NCI
 *      or NGIS
 *   5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED
 *      WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *      OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE
 *      DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
 *      NGIS, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT,
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
 *     Initial implementation kim.ong@ngc.com
 *
 */


public class ContainsSearch {
	static PorterStemmer stemmer = null;
	public static HashMap SYNONYM_MAP = null;
	public static HashMap SYNONYM_MAP2 = null;

	public static HashMap SYNONYMOUS_PHRASE_MAP = null;


	public static HashSet DISCARDED_KEYWORD_SET = null;
	public static String DISCARDED_KEYWORDS = null;
	public static HashSet STOP_WORD_SET = null;

	public static Vector SYNONYM_VEC = new Vector();

	public HashMap code2TermMap = new HashMap();
	public HashMap code2TokenizedTermMap = new HashMap();

	public static String SYNONYM_FILE = "synonym.txt";
	public static String DISCARDED_WORD_FILE = "discarded.txt";
	public static String DISCARDED_DIS_FILE = "discardedDis.txt";
	public static String STOP_WORD_FILE = "stop_words.txt";

	public static Vector discardedDis_vec = new Vector();
	static HashMap definitionMap = null;
    static String AXIOM_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.axiomfile;

    public String named_graph = null;
    String serviceUrl = null;
    String username = null;
    String password = null;
    public OWLSPARQLUtils owlSPARQLUtils = null;

	static {
		//AXIOM_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.axiomfile;
		stemmer = new PorterStemmer();
		//System.out.println(AXIOM_FILE);
		File file = new File(AXIOM_FILE);
		definitionMap = new HashMap();
		if (!file.exists()) {
			System.out.println("WARNING: " + AXIOM_FILE + " does not exist.");
			System.exit(0);
		} else {
			System.out.println("INFO: " + AXIOM_FILE + " exists.");
			createDefinitionMap();
		}
	}



    public static void createDefinitionMap() {
		Vector v = Utils.readFile(AXIOM_FILE);
		for (int i=0; i<v.size(); i++) {
			String t = (String) v.elementAt(i);
			Vector u = parseData(t, '|');
			String code = (String) u.elementAt(1);
			String prop_code = (String) u.elementAt(2);
			if (prop_code.compareTo("P97") == 0) {
				String def = (String) u.elementAt(3);
				definitionMap.put(code, def);
			}
		}
	}

	public static String getDefinition(String code) {
		return (String) definitionMap.get(code);
	}

	public ContainsSearch() {

	}

	public ContainsSearch(String serviceUrl, String named_graph, String username, String password) {
		this.serviceUrl = serviceUrl;
		this.named_graph = named_graph;
		this.username = username;
		this.password = password;

		owlSPARQLUtils = new OWLSPARQLUtils(serviceUrl, username, password);
		owlSPARQLUtils.set_named_graph(named_graph);

	}

	public ContainsSearch(String termfile) {
        createCode2TokenizedTermMap(termfile);
	}


	static public boolean in_discardedDis_vec(String t) {
	    return discardedDis_vec.contains(t.toLowerCase());
	}

	static {
		File file = new File(DISCARDED_DIS_FILE);
		if (file.exists()) {
			Vector w = Utils.readFile(DISCARDED_DIS_FILE);
			for (int i=0; i<w.size(); i++) {
				String dis = (String) w.elementAt(i);
				discardedDis_vec.add(dis.toLowerCase());
			}
	    }
		loadSynonyms();
		loadDiscardedWords();
		loadStopWords();
        Vector w = new Vector();
        String wd1 = null;
        String wd2 = null;
		SYNONYM_MAP = new HashMap();
		SYNONYM_MAP2 = new HashMap();
		SYNONYMOUS_PHRASE_MAP = new HashMap();

		for (int i=0; i<SYNONYM_VEC.size(); i++) {
			String wd_pair = (String) SYNONYM_VEC.elementAt(i);
			wd_pair = wd_pair.toLowerCase();
			Vector u = parseData(wd_pair, '|');
			wd1 = (String) u.elementAt(0);
			wd2 = (String) u.elementAt(1);

			wd1 = wd1.trim();
			wd2 = wd2.trim();

			if (wd1.indexOf(" ") != -1 || wd2.indexOf(" ") != -1) {
				w = new Vector();
				if (SYNONYMOUS_PHRASE_MAP.containsKey(wd1)) {
					w = (Vector) SYNONYMOUS_PHRASE_MAP.get(wd1);
				}
				if (!w.contains(wd2)) {
					w.add(wd2);
				}
				SYNONYMOUS_PHRASE_MAP.put(wd1, w);

				w = new Vector();
				if (SYNONYMOUS_PHRASE_MAP.containsKey(wd2)) {
					w = (Vector) SYNONYMOUS_PHRASE_MAP.get(wd2);
				}
				if (!w.contains(wd1)) {
					w.add(wd1);
				}
				SYNONYMOUS_PHRASE_MAP.put(wd2, w);

			} else {

				wd1 = stemTerm(wd1);
				wd2 = stemTerm(wd2);
				w = new Vector();
				if (SYNONYM_MAP.containsKey(wd1)) {
					w = (Vector) SYNONYM_MAP.get(wd1);
				}
				if (!w.contains(wd2)) {
					w.add(wd2);
				}
				SYNONYM_MAP.put(wd1, w);

				w = new Vector();
				if (SYNONYM_MAP.containsKey(wd2)) {
					w = (Vector) SYNONYM_MAP.get(wd2);
				}
				if (!w.contains(wd1)) {
					w.add(wd1);
				}
				SYNONYM_MAP.put(wd2, w);

				///////////////////////////////////////////////////////////
				wd1 = (String) u.elementAt(0);
				wd2 = (String) u.elementAt(1);
				w = new Vector();
				if (SYNONYM_MAP2.containsKey(wd1)) {
					w = (Vector) SYNONYM_MAP2.get(wd1);
				}
				if (!w.contains(wd2)) {
					w.add(wd2);
				}
				SYNONYM_MAP2.put(wd1, w);

				w = new Vector();
				if (SYNONYM_MAP2.containsKey(wd2)) {
					w = (Vector) SYNONYM_MAP2.get(wd2);
				}
				if (!w.contains(wd1)) {
					w.add(wd1);
				}
				SYNONYM_MAP2.put(wd2, w);
			}
		}
		//dumpMultiValuedHashMap("SYNONYM_MAP", SYNONYM_MAP);
		System.out.println("Initialization of ContainsSearch completed.");
	}

    public static Vector removeFillers(Vector v) {
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String wd = (String) v.elementAt(i);
			if (!isFiller(wd)) {
				w.add(wd);
			}
		}
		return w;
	}

	public static void loadSynonyms() {
		boolean exists = checkIfFileExists(SYNONYM_FILE);
		if (!exists) {
			System.out.println("WARNING: file " + SYNONYM_FILE + " does not exists.");
		}
        SYNONYM_VEC = Utils.readFile(SYNONYM_FILE);
	}

	public static boolean isFiller(String wd) {
		return STOP_WORD_SET.contains(wd);
	}

	public static void loadStopWords() {
		boolean exists = checkIfFileExists(STOP_WORD_FILE);
		if (!exists) {
			System.out.println("WARNING: file " + STOP_WORD_FILE + " does not exists.");
		}
        Vector w = Utils.readFile(STOP_WORD_FILE);
        STOP_WORD_SET = Utils.vector2HashSet(w);
	}

	public static void loadDiscardedWords() {
		boolean exists = checkIfFileExists(DISCARDED_WORD_FILE);
		if (!exists) {
			System.out.println("WARNING: file " + SYNONYM_FILE + " does not exists.");
		}
        Vector w = Utils.readFile(DISCARDED_WORD_FILE);
        DISCARDED_KEYWORD_SET = Utils.vector2HashSet(w);
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

    public static Vector addSynonymStems(Vector v) {
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String wd = (String) v.elementAt(i);
			if (SYNONYM_MAP.containsKey(wd)) {
				Vector v2 = (Vector) SYNONYM_MAP.get(wd);
				for (int i2=0; i2<v2.size(); i2++) {
					String wd2 = (String) v2.elementAt(i2);
					w.add(wd2);
				}
			}
			w.add(wd);
		}
		return w;
	}

    public static Vector parseData(String line, char delimiter) {
		if(line == null) return null;
		Vector w = new Vector();
		String t = line;
		int n = t.indexOf("" + delimiter);
		while (n != -1) {
			String s = t.substring(0, n);
			w.add(s);
			t = t.substring(n+1, t.length());
			n = t.indexOf("" + delimiter);
		}
		w.add(t);
		return w;
	}

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

	public static Vector readFile(String datafile) {
		Vector v = new Vector();
        try {
			File file = new File(datafile);
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			BufferedReader br = null;
			try {
				br = new BufferedReader(new InputStreamReader(bis));
			} catch (Exception ex) {
				return null;
			}

            while (true) {
                String line = br.readLine();
				if (line == null) {
					break;
				}
				v.add(line);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return v;
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


	public static void copyDirectory(String sourceDirectoryLocation, String destinationDirectoryLocation)
	  throws IOException {
	    Files.walk(java.nio.file.Paths.get(sourceDirectoryLocation))
	      .forEach(source -> {
		  java.nio.file.Path destination = java.nio.file.Paths.get(destinationDirectoryLocation, source.toString()
		    .substring(sourceDirectoryLocation.length()));
		  try {
		      Files.copy(source, destination);
		  } catch (IOException e) {
		      e.printStackTrace();
		  }
	      });
	}


	public static String encode(String toEncode) {
		try {
			return java.net.URLEncoder.encode(toEncode.trim(), "UTF-8");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

    public static String generateHyperlink(String name, String propertyName) {
		String propertyName_0 = propertyName;
		if (propertyName.compareTo("Relationships") == 0) {
			propertyName = "list_relationships";
		}
		String hyperlink = "https://aapmbdsc.azurewebsites.net/?KeyElementName=" + name + "&PropertyName=" + propertyName;
		hyperlink = "<a href=\"" + hyperlink + "\">" + propertyName_0 + "</a>";
		return hyperlink;
	}


	public static boolean isPureAscii(String v) {
		return Charset.forName("US-ASCII").newEncoder().canEncode(v);
	}

	public String xmlEscapeText(String t) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < t.length(); i++){
			char c = t.charAt(i);
			switch(c){
				case '<': sb.append("&lt;"); break;
				case '>': sb.append("&gt;"); break;
				case '\"': sb.append("&quot;"); break;
				case '&': sb.append("&amp;"); break;
				case '\'': sb.append("&apos;"); break;
				default:
					if (c>0x7e) {
						sb.append("&#"+((int)c)+";");
					} else {
						sb.append(c);
					}
			}
		}
		return sb.toString();
	}

	public static Vector loadFileLineByLine(String filename) {
		Vector w = new Vector();
		StringBuffer buf = new StringBuffer();
		try {
			InputStream input = new FileInputStream(filename);
			int data = input.read();
			while(data != -1) {
				char c = (char) data;
				if (c == '\n') {
					String s = buf.toString();
					s = s.trim();
					w.add(s);
					buf = new StringBuffer();
				} else {
					buf.append("" + c);
				}
				data = input.read();
			}
			String s = buf.toString();
			s = s.trim();
			w.add(s);
			input.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return w;
	}

    public static void saveToFile(String outputfile, Vector v) {
        try {
            FileOutputStream output = new FileOutputStream(outputfile);
            for (int i=0; i<v.size(); i++) {
				String data = (String) v.elementAt(i);
				if (i < v.size()) {
					data = data + "\n";
				}
				byte[] array = data.getBytes();
				output.write(array);
			}
            output.close();
        } catch(Exception e) {
            e.getStackTrace();
        }
    }

	public static String extractTagValue(String line, String tag) {
		String t0 = line;
		String t = t0.trim();
		int n = t.indexOf(tag);
		if (n == -1) return null;
		t = t.substring(n+tag.length(), t.length());
		n = t.indexOf("<");
		if (n == -1) return null;
		t = t.substring(1, n);
		t = t.trim();
		return t;
	}

	public static Vector extractColumnData(String filename, Vector<Integer> col_vec, char delim) {
		Vector v = readFile(filename);
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = parseData(line, delim);
			StringBuffer buf = new StringBuffer();
			for (int j=0; j<u.size(); j++) {
				Integer int_obj = Integer.valueOf(j);
				if (col_vec.contains(int_obj)) {
					String value = (String) u.elementAt(j);
					buf.append(value).append("" + delim);
				}
			}
			String s = buf.toString();
			s = s.substring(0, s.length()-1);
			w.add(s);
		}
		return w;
	}

	public static HashSet extractColumnData(String filename, int col, char delim) {
		Vector v = readFile(filename);
		HashSet hset = new HashSet();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = parseData(line, delim);
			String value = (String) u.elementAt(col);
			if (!hset.contains(value)) {
				hset.add(value);
			}
		}
		return hset;
	}

    public static void exploreContent(String filename, char delim) {
        Vector v = readFile(filename);
        String firstLine = (String) v.elementAt(0);
        Vector u = parseData(firstLine, delim);
        Utils.dumpVector(firstLine, u);
	}

	public static void mergeFiles(String sourcefile, int keyCol_1, String targetfile, int keyCol_2, String heading) {
		long ms = System.currentTimeMillis();
		//String codedTerms = "CodedTerms.txt";

        Vector v1 = readFile(sourcefile);
        HashMap hmap = new HashMap();
        for (int i=1; i<v1.size(); i++) {
			String line = (String) v1.elementAt(i);
			Vector u = parseData(line, '\t');
			String t =(String) u.elementAt(keyCol_1);
			hmap.put(t, line);
		}
        Vector v2 = readFile(targetfile);
        Vector w = new Vector();
        w.add(heading);//"Term\tNCIt PT\tNCIt Code");
        int knt1 = 0;
        int knt2 = 0;
        for (int i=0; i<v2.size(); i++) {
			String line = (String) v2.elementAt(i);
			Vector u = parseData(line, '\t');
			String t = (String) u.elementAt(keyCol_2);
			if (hmap.containsKey(t)) {
				w.add(t + "\t" + (String) hmap.get(t));
				knt1++;
			} else {
				w.add(t + "\tNot found\tNot found");
				knt2++;
			}
		}
		Utils.saveToFile("mod_" + targetfile, w);
		System.out.println("No match: " + knt2);
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}

////////////////////////////////////////////////////////////////////////////////////////////////////
    public Vector extractColumnValues(String filename, int columnIndex, char delim) {
		Vector v = readFile(filename);
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = parseData(line, delim);
			String value = (String) u.elementAt(columnIndex);
			w.add(value);
		}
		return w;
	}

    public static Vector removeSpecialCharacters(Vector v) {
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			line = removeSpecialCharacters(line);
			line = line.trim();
			//line = line.replace("'s", "");
			w.add(line);
		}
		return w;
	}


     public static String removeSpecialCharacters(String t) {
		t = t.replace("(", " ");
		t = t.replace(")", " ");
		t = t.replace(",", " ");
		t = t.replace(";", " ");
		t = t.replace("'s", " ");
		t = t.replace("-", " ");
		t = t.replace("+", " ");
		t = t.replace("—", " ");
		t = t.replace(".", " ");
		t = t.replace(":", " ");
		//t = t.replace("/", " ");
		//add &
		t = t.replace("&", " ");
		return t;
	 }

	public static String stemTerm(String term) {
		return stemmer.stem(term);
	}


    public static Vector tokenizeTerm(String t) {
		t = removeSpecialCharacters(t);
		//Vector tokens = tokenize(t, false);
		Vector tokens = tokenize(t, false);

        Vector w = new Vector();
		for (int i=0; i<tokens.size(); i++) {
			String token = (String) tokens.elementAt(i);
			token = token.toLowerCase();
			String wd = stemTerm(token);
			if (!w.contains(wd)) {
				w.add(wd);
			}
			Iterator it = DISCARDED_KEYWORD_SET.iterator();
			while (it.hasNext()) {
				String s = (String) it.next();
				if (!tokens.contains(s)) {
					s = stemTerm(s);
					//////////////////////////////////
					//s = removeSpecialCharacters(s);
					if (!w.contains(s)) {
						w.add(s);
					}
				}
			}
			if (SYNONYM_MAP.containsKey(wd)) {
				Vector syns = (Vector) SYNONYM_MAP.get(wd);
				for (int i2=0; i2<syns.size(); i2++) {
					String syn = (String) syns.elementAt(i2);
					syn = stemTerm(syn);
					if (!w.contains(syn)) {
						w.add(syn);
					}
				}
			}
		}
		return w;
	}

	//Ovarian Cancer/Fallopian Tube Cancer/Primary Peritoneal Cancer
	public static Vector reconstructTerms(String t) {
		Vector w = new Vector();
		if (t.indexOf("/") == -1) {
			w.add(t);
			return w;
		}
		w = tokenize(t);
		Vector v = new Vector();
		for (int i=0; i<w.size(); i++) {
			String token = (String) w.elementAt(i);
			Vector u = parseData(token, '/');
			//Utils.dumpVector(token, u);
			if (v.size() == 0) {
				for (int j=0; j<u.size(); j++) {
					String wd = (String) u.elementAt(j);
					v.add(wd);
				}
			} else {
				Vector vec = new Vector();
				for (int j=0; j<v.size(); j++) {
					String s = (String) v.elementAt(j);
					for (int k=0; k<u.size(); k++) {
						String wd = (String) u.elementAt(k);
						String s2 = s + " " + wd;
						vec.add(s2);
					}
				}
				v = (Vector) vec.clone();
			}
		}
		return v;
	}

	public String getTerms(Vector codes) {
		String s = "";
		for (int i=0; i<codes.size(); i++) {
			String code = (String) codes.elementAt(i);
			String t = getTerms(code);
			if (i == 0) {
				s = t;
			} else {
				s = s + "|" + t;
			}
		}
		return s;
	}


	public String getTerms(String code) {
		if (!code2TermMap.containsKey(code)) {
			return "";
		}
		Vector terms = (Vector) code2TermMap.get(code);
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<terms.size(); i++) {
			String term = (String) terms.elementAt(i);
			buf.append(term).append("$");
		}
		String t = buf.toString();
		return t.substring(0, t.length()-1);
	}


	public void createCode2TokenizedTermMap(String filename) {
        code2TermMap = createCode2TermMap(filename);
        code2TokenizedTermMap = createCode2TokenizedTermMap(code2TermMap);
	}

    public HashMap createCode2TokenizedTermMap(HashMap code2TermMap) {
		HashMap hmap = new HashMap();
		HashSet term_set = new HashSet();
		Iterator it = code2TermMap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			List list = new ArrayList();
			Vector v = (Vector) code2TermMap.get(key);
			for (int i=0; i<v.size(); i++) {
				String term = (String) v.elementAt(i);
				if (!term_set.contains(term)) {
					term_set.add(term);
					Vector v2 = tokenizeTerm(term);
					v2 = new SortUtils().quickSort(v2);
					list.add(v2);
				}
			}
			hmap.put(key, list);
		}
		return hmap;
	}

    public HashMap createCode2TermMap(String filename) {
		Vector v = Utils.readFile(filename);
		HashMap hmap = new HashMap();
		for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = parseData(line, '\t');
			String key = (String) u.elementAt(0);
			Vector w = new Vector();
			if (hmap.containsKey(key)) {
				w = (Vector) hmap.get(key);
			}
			String value = (String) u.elementAt(1);
			if (value.length() > 0) {
				if (!w.contains(value)) {
					w.add(value);
				}
			}
			hmap.put(key, w);
		}
        return hmap;
	}

	public Vector search(String term) {
		Vector w = new Vector();
		Vector v1 = tokenize(term);
		Utils.dumpVector(term, v1);

		Iterator it = code2TokenizedTermMap.keySet().iterator();
		while (it.hasNext()) {
            String key = (String) it.next();
            List list = (List) code2TokenizedTermMap.get(key);
            String term_lc = term.toLowerCase();
            for (int i=0; i<list.size(); i++) {
				Vector v2 = (Vector) list.get(i);
				boolean contains = true;
				for (int j=0; j<v1.size(); j++) {
					String token = (String) v1.elementAt(j);
					if (!v2.contains(token)) {
						contains = false;
						break;
					}
				}
				if (contains) {
					if (!w.contains(key)) {
						w.add(key);
					}
				}
			}
		}
		return w;
	}

	public boolean contains(String code, String dis) {
		Vector v1 = tokenize(dis);
		Utils.dumpVector(dis, v1);
		if (!code2TokenizedTermMap.containsKey(code)) {
			System.out.println("Key not found in code2TokenizedTermMap " + code);
			return false;
		}

		List list = (List) code2TokenizedTermMap.get(code);
		if (list == null || list.size() == 0) {
			System.out.println("code2TokenizedTermMap returns empty list");
			return false;
		}
		for (int i=0; i<list.size(); i++) {
			Vector v2 = (Vector) list.get(i);
			Utils.dumpVector("v2", v2);
			boolean contains = true;
			for (int j=0; j<v1.size(); j++) {
				String token = (String) v1.elementAt(j);
				if (!v2.contains(token)) {
					contains = false;
					break;
				}
			}
			if (contains) {
				return true;
			}
		}
		return false;
	}

    public static String vector2Delimited(Vector v, char delim) {
		if (v == null || v.size() == 0) return "";
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<v.size(); i++) {
			String wd = (String) v.elementAt(i);
			buf.append(wd).append(delim);
		}
		String t = buf.toString();
		t = t.substring(0, t.length()-1);
		if (t.endsWith("" + delim)) {
			t = t.substring(0, t.length()-1);
		}
		return t;
	}

	public static String vector2String(Vector v, char delim) {
		if (v == null || v.size() == 0) return "";
		StringBuffer buf = new StringBuffer();
        for (int i=0; i<v.size(); i++) {
			String t = (String) v.elementAt(i);
			buf.append(t).append("" + delim);
		}
		String t = buf.toString();
		return t.substring(0, t.length()-1);
	}

//Melanoma: Cutaneous|A regimen consisting of carboplatin and nab-paclitaxel, an albumin-bound formulation of paclitaxel, that is used for the treatment of non-small cell lung cancer (NSCLC), breast, ovarian, fallopian tube and primary peritoneal cancers, and cutaneous melanoma.|
    public void test() {
		String dis_term = "Melanoma: Cutaneous";
		Vector res_vec = search(dis_term);
		System.out.println("search returns: " + res_vec.size());
		dumpVector(dis_term, res_vec);
	}

	public String run(String termfile, String datafile, int codeCol, int vbtCol) {
        createCode2TokenizedTermMap(termfile);
        Vector w = new Vector();
        Vector v = Utils.readFile(datafile);
        String heading = (String) v.elementAt(0);
        Vector nomatches = new Vector();
        nomatches.add(heading);
        int knt = 0;
        w.add(heading + "\tNCIt DEFINITION" + "\tContains(*)");
        for (int i=1; i<v.size(); i++) {
			knt++;
			String line = (String) v.elementAt(i);
			Vector u = parseData(line, '\t');
			String code = (String) u.elementAt(codeCol);
			String dis = (String) u.elementAt(vbtCol);
			Vector dis_vec = reconstructTerms(dis);
			dis_vec = removeFillers(dis_vec);
			boolean bool = false;
			for (int j=0; j<dis_vec.size(); j++) {
			    String dis_term = (String) dis_vec.elementAt(j);
			    if (!in_discardedDis_vec(dis_term)) {
					Vector res_vec = search(dis_term);
					if (res_vec.contains(code)) {
						w.add(line + "\t" + (String) getDefinition(code) + "\t" + "(*)");
						bool = true;
						break;
					}
				}
			}
			if (!bool) {
				w.add(line + "\t" + (String) getDefinition(code) + "\t");
				nomatches.add(line);
			}
		}
		String outputfile = "results_" + datafile;
        Utils.saveToFile(outputfile, w);
        Utils.saveToFile("nomatch_" + datafile, nomatches);
        System.out.println("Number of records: " + knt);
        System.out.println("Number of nomatches: " + nomatches.size());
        return outputfile;
	}

    public static Vector addSynonyms(Vector v) {
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String wd = (String) v.elementAt(i);
			if (SYNONYM_MAP2.containsKey(wd)) {
				Vector v2 = (Vector) SYNONYM_MAP2.get(wd);
				for (int i2=0; i2<v2.size(); i2++) {
					String wd2 = (String) v2.elementAt(i2);
					w.add(wd2);
				}
			}
			w.add(wd);
		}
		return w;
	}

    public static String addSynonymousPhrases(String def) {
		def = def.toLowerCase();
		Iterator it = SYNONYMOUS_PHRASE_MAP.keySet().iterator();
		while (it.hasNext()) {
			String phrase = (String) it.next();
			if (def.indexOf(phrase) != -1) {
				Vector v = (Vector) SYNONYMOUS_PHRASE_MAP.get(phrase);
				for (int j=0; j<v.size(); j++) {
					String wd2 = (String) v.elementAt(j);
				    def = def + " " + wd2;
				}
			}
		}
		return def;
	}

/*
Row	NCIt code	NCIt DEFINITION	NCCN Regimen Name	NCCN Disease Name
2	C188903	A regimen consisting of polatuzumab vedotin, bendamustine and rituximab that may be used in the treatment of diffuse large B-cell lymphoma (DLBCL).	Bendamustine/Polatuzumab vedotin-piiq + RiTUXimab	Diffuse Large B-Cell Lymphoma
*/
	public static Vector run(String filename, int dis_col, int def_col) {
		System.out.println("filename: " + filename);
		System.out.println("dis_col: " + dis_col);
		System.out.println("def_col: " + def_col);

		//Vector debug_vec = new Vector();
		Vector missing_word_vec = new Vector();
        Vector w = new Vector();
        Vector v = Utils.readFile(filename);
        w.add((String) v.elementAt(0) + "\tContains (*)");
        int knt1 = 0;
        int knt2 = 0;
        int knt3 = 0;
        for (int i=1; i<v.size(); i++) {
			knt1++;
			String line = (String) v.elementAt(i);
			System.out.println("(" + i + ") " + line);

			Vector u = parseData(line, '\t');
			String dis = (String) u.elementAt(dis_col);
			String def = (String) u.elementAt(def_col);

			dis = removeBrackets(dis);

			String def0 = def.toLowerCase();
			System.out.println(def0);

			def = addSynonymousPhrases(def0);
			if (def.compareTo(def0) != 0) {
				System.out.println("(*) Synonymous phrases added");
				System.out.println(def);
			}

			//Vector dis_vec = reconstructTerms(dis);
			Vector dis_vec = tokenize(dis, false);
			dis_vec = removeSpecialCharacters(dis_vec);

			Vector def_vec = tokenize(def, false);
			def_vec = addSynonyms(def_vec);
			def_vec = removeSpecialCharacters(def_vec);


			boolean contains = true;
			for (int i3=0; i3<dis_vec.size(); i3++) {
				String word = (String) dis_vec.elementAt(i3);
				if (!def_vec.contains(word)) {
					contains = false;
					knt3++;
					//debug_vec.add("\n(" + knt3 + ") " + line);
					//debug_vec.add("\tDisease Name: " + dis);
					//debug_vec.add("\tword not found: " + word);
					if (!missing_word_vec.contains(word)) {
						missing_word_vec.add(word);
					}
					break;
				}
			}
            if (contains) {
				knt2++;
			}
			System.out.println("Contains? " + contains);
			if (contains) {
				w.add(line + "\t(*)");
			} else {
				w.add(line + "\t");
			}
		}
        System.out.println("Total: " + knt1);
        System.out.println("Contains: " + knt2);
        System.out.println("No match: " + knt3);
        //Utils.saveToFile("reason_" + filename, debug_vec);

        missing_word_vec = new SortUtils().quickSort(missing_word_vec);
        Utils.saveToFile("missing_word_" + filename, missing_word_vec);
        return w;
	}


	public static String removeBrackets(String t) {
		int n1 = t.lastIndexOf("(");
		if (n1 != -1 && t.endsWith(")")) {
			return t.substring(0, n1-1);
		}
		return t;

	}

    public static Vector tokenize(String term, boolean applyStem) {
		term = term.toLowerCase();
		term = removeSpecialCharacters(term);
		Vector words = new Vector();
		Vector w = parseData(term, ' ');
        for (int i=0; i<w.size(); i++) {
			String token = (String) w.elementAt(i);
			if (token.length() > 0) {
				if (!isFiller(token)) {
					if (applyStem) {
						token = stemTerm(token);
					}
					words.add(token);
				}
			}
		}
		return words;
	}

    public static Vector tokenize(String term) {
		term = term.toLowerCase();
		Vector words = parseData(term, ' ');
		return words;
	}

	public static Vector runAnalysis(String line, String dis,  String def) {
		Vector w = new Vector();
		dis = removeBrackets(dis);
		dis = dis.replace("/", " ");

		Vector dis_vec = parseData(dis, ' ');
		dis_vec = removeSpecialCharacters(dis_vec);
		Utils.dumpVector(dis, dis_vec);

        def = def.replace("/", " ");
		String def0 = def.toLowerCase();
		def = addSynonymousPhrases(def0);
		Vector def_vec = tokenize(def, false);
		def_vec = addSynonyms(def_vec);
		Utils.dumpVector(def, def_vec);

		for (int i3=0; i3<dis_vec.size(); i3++) {
			String word = (String) dis_vec.elementAt(i3);
			if (!def_vec.contains(word)) {
				w.add(word);
			}
		}
        return w;
	}


	public static void runAnalysis(String matchfile, String nomatchfile, int dis_col, int def_col) {
		Vector v = Utils.readFile(matchfile);
		Vector w = new Vector();
		HashMap lineMap = new HashMap();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Integer int_obj = Integer.valueOf(i);
			lineMap.put(int_obj, line);
		}

		w.add((String) v.elementAt(0));
 		for (int i=1; i<v.size(); i++) {
			Integer int_obj = Integer.valueOf(i);
            String line = (String) lineMap.get(int_obj);

			String line1 = line;
			if (line1.indexOf("(*)") == -1) {

				w.add(line);

//////////////////////////////////////////////////////////////////////////////////////
				Vector w1 = new Vector();
				Vector u = parseData(line1, '\t');
				String dis = (String) u.elementAt(dis_col);
				String def = (String) u.elementAt(def_col);
				w1.add("\tDisease Name: " + dis);
				w1.add("\tDefinition: " + def);
				Vector missing_words = runAnalysis(line, dis,  def);
				w1.add("\tMissing words: ");
				for (int j=0; j<missing_words.size(); j++) {
					String wd = (String) missing_words.elementAt(j);
					w1.add("\t\t" + wd);
				}
				w.addAll(w1);
//////////////////////////////////////////////////////////////////////////////////////
			}
		}
        Utils.saveToFile("nomatch_" + matchfile, w);
	}


	public String construct_get_contains(String named_graph, String target) {
        String prefixes = owlSPARQLUtils.getPrefixes();
        StringBuffer buf = new StringBuffer();
        buf.append(prefixes);
        buf.append("select distinct ?x_label ?x_code ?z_target ").append("\n");
        buf.append("from <" + named_graph + ">").append("\n");
        buf.append("where  { ").append("\n");
        buf.append("            ?x a owl:Class .").append("\n");
        buf.append("            ?x :NHC0 ?x_code .").append("\n");
        buf.append("            ?x rdfs:label ?x_label .").append("\n");
        buf.append("            ?z_axiom a owl:Axiom  .").append("\n");
        buf.append("            ?z_axiom owl:annotatedSource ?x .").append("\n");
        buf.append("            ?z_axiom owl:annotatedProperty ?p .").append("\n");
        buf.append("            ?p rdfs:label ?p_label .").append("\n");
        buf.append("            ?p rdfs:label \"FULL_SYN\"^^xsd:string .").append("\n");
        buf.append("            ?z_axiom owl:annotatedTarget ?z_target .").append("\n");
        buf.append("            FILTER (contains(lcase(str(?z_target)), \"" + target + "\"))").append("\n");
        buf.append("}").append("\n");
        buf.append("").append("\n");
        buf.append("").append("\n");
        return buf.toString();
	}

	public Vector getContains(String named_graph, String target) {
        String query = construct_get_contains(named_graph, target);
        System.out.println(query);

        Vector v = owlSPARQLUtils.executeQuery(query);
        if (v == null) return null;
        if (v.size() == 0) return v;
        return new SortUtils().quickSort(v);
	}

	public static void run(String serviceUrl,
	                       String namedGraph,
	                       String username,
	                       String password,
	                       String target,
	                       String outputfile) {
		long ms = System.currentTimeMillis();
	    ContainsSearch cs = new ContainsSearch(serviceUrl, namedGraph, username, password);
	    Vector v = cs.getContains(namedGraph, target);
	    Utils.saveToFile(outputfile, v);
	    System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}

	public static void main(String[] args) {
		long ms = System.currentTimeMillis();
		String serviceUrl = ConfigurationController.serviceUrl;
		String namedGraph = ConfigurationController.namedGraph;
		String username = ConfigurationController.username;
		String password = ConfigurationController.password;
	    String target = args[0];
	    String outputfile = args[1];
	    run(serviceUrl, namedGraph, username, password, target, outputfile);
	}
}
