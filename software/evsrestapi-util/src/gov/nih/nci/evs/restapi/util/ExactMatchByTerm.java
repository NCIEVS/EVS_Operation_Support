package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;
import java.io.*;
import java.text.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;
import java.nio.charset.Charset;

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


public class ExactMatchByTerm {
	static String AXIOM_FILE_NAME = "axiom_ThesaurusInferred_forTS.txt";
	static String HIER_FILE_NAME = "parent_child.txt";
	static String AXIOM_FILE = null;
	static String PROPERTY_FILE = null;

	static HashMap term2CodesMap = null;
	static HashMap NCIPTMap = null;
	static HashMap NCISYMap = null;
	static HashMap sourcePTMap = null;
	static HashMap sourceCode2NCItCodesMap = null;
	static HashMap code2FULLSYNMap = null;
    static boolean caseSensitive = false;
    static HashSet retiredConcepts = new HashSet();
    static String CONCEPT_STATUS_FILE = "P310.txt";
    static String sparqlRunnerDirectory = null;
    static HashSet branch = null;
    static HashMap sourceCode2LineMap = null;
    static String SYNONYM_EXT = null;
    static String termfile = null;

	static {
		AXIOM_FILE = ConfigurationController.reportGenerationDirectory + File.separator + AXIOM_FILE_NAME;
		PROPERTY_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.propertyfile;

		System.out.println(AXIOM_FILE);
		File file = new File(AXIOM_FILE);
		if (!file.exists()) {
			System.out.println("WARNING: " + AXIOM_FILE + " does not exist.");
			System.exit(0);
		} else {
			System.out.println("INFO: " + AXIOM_FILE + " exists.");
		}

		sourceCode2LineMap = createSourceCode2LineMap();

		retiredConcepts = createRetiredConceptSet();
		System.out.println("retired concepts: " + retiredConcepts.size());

		termfile = ConfigurationController.termfile;
		file = new File(termfile);
		if (!file.exists()) {
			System.out.println("INFO: term file does not exists, use AXIOM_FILE as default");
			termfile = AXIOM_FILE;
		} else {
			System.out.println("INFO: " + termfile + " exists.");
			System.out.println(termfile);
		}
		term2CodesMap = createTerm2CodesMap(termfile);

		SYNONYM_EXT = ConfigurationController.synonymExt;
		if (SYNONYM_EXT != null && SYNONYM_EXT.length() > 0) {
			file = new File(SYNONYM_EXT);
			if (file.exists()) {
				expandTerm2CodesMap(SYNONYM_EXT);
			}
		}

		NCIPTMap = createNCIPTMap();
		System.out.println("NCIPTMap: " + NCIPTMap.keySet().size());

		NCISYMap = createNCISYMap();
		System.out.println("NCISYMap: " + NCISYMap.keySet().size());

		code2FULLSYNMap = createCode2FULLSYNMap();
		System.out.println("code2FULLSYNMap: " + code2FULLSYNMap.keySet().size());

        branch = new HashSet();
        Vector codes = null;

        String restrictedcodefile = ConfigurationController.restrictedcodefile;
        if (restrictedcodefile != null && restrictedcodefile.compareTo("null") != 0) {
			 codes = Utils.readFile(restrictedcodefile);
			 branch = Utils.vector2HashSet(codes);

		} else {
			String branchfile = ConfigurationController.branchfile;
			if (branchfile != null && branchfile.compareTo("null") != 0) {
				file = new File(branchfile);
				if (!file.exists()) {
					System.out.println(branchfile + " does not exists.");
					String HIER_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile;
					Vector v = Utils.readFile(HIER_FILE);
					HierarchyHelper hh = new HierarchyHelper(v);
					int n = branchfile.indexOf(".");
					String root = branchfile.substring(0, n);//"C62634";
					codes = extractBranchCodes(root);
					Utils.saveToFile(branchfile, codes);
				} else {
					System.out.println(branchfile + " exists.");
					codes = Utils.readFile(branchfile);
				}
				branch = Utils.vector2HashSet(codes);
			}
		}
	}

	public static Vector extractBranchCodes(String root) {
		String HIER_FILE = ConfigurationController.reportGenerationDirectory + File.separator + HIER_FILE_NAME;
		System.out.println(HIER_FILE);
		File file = new File(HIER_FILE);
		if (!file.exists()) {
			System.out.println("WARNING: " + HIER_FILE + " does not exist.");
			System.exit(0);
		} else {
			System.out.println("INFO: " + HIER_FILE + " exists.");
		}
		Vector v = Utils.readFile(HIER_FILE);
		HierarchyHelper hh = new HierarchyHelper(v);
		Vector w = hh.get_transitive_closure_v3(root);
		return w;
	}

	public static HashMap createSourceCode2LineMap() {
		String AXIOM_FILE = ConfigurationController.reportGenerationDirectory + File.separator + AXIOM_FILE_NAME;
		System.out.println(AXIOM_FILE);
		File file = new File(AXIOM_FILE);
		if (!file.exists()) {
			System.out.println("WARNING: " + AXIOM_FILE + " does not exist.");
			System.exit(0);
		} else {
			System.out.println("INFO: " + AXIOM_FILE + " exists.");
		}
		Vector v = Utils.readFile(AXIOM_FILE);
		HashMap hmap = new HashMap();
		//Recombinant Amphiregulin|C1000|P90|AMPHIREGULIN|P383$PT|P384$FDA|P385$7MGE0HPM2H|P386$UNII
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			for (int j=0; j<u.size(); j++) {
				String t = (String) u.elementAt(j);
				if (t.startsWith("P385$")) {
					line = HTMLDecoder.decode(line);
					hmap.put(t, line);
				}
			}
		}
        //Utils.dumpHashMap("source_code.map", hmap);
        return hmap;
	}


	public static boolean is_retired(String code) {
		return retiredConcepts.contains(code);
	}

	public static Vector getMatchedCodes(String term) {
		if (!caseSensitive) {
			term = term.toLowerCase();
		}
		if (!term2CodesMap.containsKey(term)) return null;
		return (Vector) term2CodesMap.get(term);
	}

	public static void extractProperties(String prop_code) {
		long ms = System.currentTimeMillis();
		String reportGenerationDirectory = ConfigurationController.reportGenerationDirectory;
		String owlfile = reportGenerationDirectory + File.separator + ConfigurationController.owlfile;
		System.out.println(owlfile);
		OWLScanner scanner = new OWLScanner(owlfile);
		Vector w = scanner.extractProperties(scanner.get_owl_vec(), prop_code);
		String outputfile = prop_code + ".txt";
		Utils.saveToFile(outputfile, w);
		System.out.println(outputfile + " generated.");
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}

    public static HashSet createRetiredConceptSet() {
		HashSet hset = new HashSet();
		/*
		File file = new File(CONCEPT_STATUS_FILE);
		if (!file.exists()) {
			System.out.println("WARNING: " + CONCEPT_STATUS_FILE + " does not exist.");
			extractProperties("P310");
		}
		*/
		extractProperties("P310");
		Vector v = Utils.readFile(CONCEPT_STATUS_FILE);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String status = (String) u.elementAt(2);
			if (status.compareTo("Retired_Concept") == 0) {
				String code = (String) u.elementAt(0);
				hset.add(code);
			}
		}
		return hset;
	}


    public static HashMap createNCIPTMap() {
		return createSourcePTMap("NCI");
	}

    public static HashMap createNCISYMap() {
		return createSourceSYMap("NCI");
	}

    public static HashMap createHemOncPTMap() {
		return createSourcePTMap("HemOnc");
	}

    public static HashMap createSourcePTMap(String source) {
		HashMap hmap = new HashMap();
		Vector v = readFile(AXIOM_FILE);
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			if (u.contains("P383$PT") && u.contains("P384$" + source) ) {
				hmap.put((String) u.elementAt(1), (String) u.elementAt(3));
			}
		}
		return hmap;
	}

    //Recombinant Amphiregulin|C1000|P90|AR|P383$AB|P384$NCI
    public static HashMap createSourceSYMap(String source) {
		HashMap hmap = new HashMap();
		Vector v = readFile(AXIOM_FILE);
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String type = (String) u.elementAt(2);
			if (type.compareTo("P90") == 0) {
				if (u.contains("P383$SY") && u.contains("P384$" + source) ) {
					w = new Vector();
					String key = (String) u.elementAt(1);

					if (hmap.containsKey(key)) {
						w = (Vector) hmap.get(key);
					}
					String sy = (String) u.elementAt(3);
					sy = HTMLDecoder.decode(sy);
					if (!w.contains(sy)) {
						w.add(sy);
					}
					hmap.put(key, w);
				}
			}
		}
		return hmap;
	}

    public static HashMap createSourceCode2NCItCodesMap(String source) {
		HashMap sourceCode2NCItCodesMap = new HashMap();
		Vector v = readFile(AXIOM_FILE);
		String target = "P384$" + source;
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			if (u.contains(target)) {
				if (line.indexOf("P385") != -1) {
					for (int j=0; j<u.size(); j++) {
						String token = (String) u.elementAt(j);
						if (token.startsWith("P385")) {
							Vector u2 = StringUtils.parseData(token, '$');
							String sourceCode = (String) u2.elementAt(1);
							Vector w1 = new Vector();
							if (sourceCode2NCItCodesMap.containsKey(sourceCode)) {
								System.out.println("INFO: multiple sourceCode to NCItCode mapping encountered. " + sourceCode);
								w1 = (Vector) sourceCode2NCItCodesMap.get(sourceCode);
							}
							String ncitCode = (String) u.elementAt(1);
							if (!w1.contains(ncitCode)) {
								w1.add(ncitCode);
							}
							sourceCode2NCItCodesMap.put(sourceCode, w1);
							if (w1.size() > 1) {
								Utils.dumpVector(sourceCode, w1);
							}
						}
					}
				}
			}
		}
		return sourceCode2NCItCodesMap;
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
		//propertyName = propertyName.replace(" ", "+");
		String hyperlink = "https://aapmbdsc.azurewebsites.net/?KeyElementName=" + name + "&PropertyName=" + propertyName;
		//hyperlink = hyperlink.replace("�", "&ndash;");
		//hyperlink = encode(hyperlink);
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
//C192234	Operational Ontology for Radiation Oncology Prostate Cancer Terminology	C104495	Other Race

    public static void exploreContent(String filename, char delim) {
        Vector v = readFile(filename);
        String firstLine = (String) v.elementAt(0);
        Vector u = StringUtils.parseData(firstLine, delim);
        Utils.dumpVector(firstLine, u);
	}

	public static void mergeFiles(String sourcefile, int keyCol_1, String targetfile, int keyCol_2, String heading) {
		long ms = System.currentTimeMillis();
		//String codedTerms = "CodedTerms.txt";

        Vector v1 = readFile(sourcefile);
        HashMap hmap = new HashMap();
        for (int i=1; i<v1.size(); i++) {
			String line = (String) v1.elementAt(i);
			Vector u = StringUtils.parseData(line, '\t');
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
			Vector u = StringUtils.parseData(line, '\t');
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
			Vector u = StringUtils.parseData(line, delim);
			String value = (String) u.elementAt(columnIndex);
			w.add(value);
		}
		return w;
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

//Case sensitive match
    public static HashMap createTerm2CodesMap() {
		return createTerm2CodesMap(null);
	}

    public static HashMap createTerm2CodesMap(String filename) {
		HashMap hmap = new HashMap();
		if (filename == null) {
			filename = AXIOM_FILE;
		}
		Vector v = readFile(filename);
        for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String prop_code = (String) u.elementAt(2);
			if (prop_code.compareTo("P90") == 0) {
				String term = (String) u.elementAt(3);
				term = HTMLDecoder.decode(term);
				String term_lc = term.toLowerCase();
				if (!caseSensitive) {
					term = term_lc;
				}

				Vector w = new Vector();
				if (hmap.containsKey(term)) {
					w = (Vector) hmap.get(term);
				}
				String code = (String) u.elementAt(1);
				if (!w.contains(code)) {
					w.add(code);
				}
				hmap.put(term, w);
			}
		}
		return hmap;
	}

    public static HashMap createCode2FULLSYNMap() {
		String filename = AXIOM_FILE;
		HashMap hmap = new HashMap();
		Vector v = readFile(filename);
        for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(1);
			String prop_code = (String) u.elementAt(2);
			if (prop_code.compareTo("P90") == 0) {
				Vector w = new Vector();
				if (hmap.containsKey(code)) {
					w = (Vector) hmap.get(code);
				}
				String term = (String) u.elementAt(3);
				term = HTMLDecoder.decode(term);
				if (!w.contains(term)) {
					w.add(term);
				}
				hmap.put(code, w);
			}
		}
		return hmap;
	}

	public static String getMatchedData(String code) {
		Vector codes = new Vector();
		codes.add(code);
		return getMatchedData(codes);
	}

	public static String getMatchedData(String code, boolean ptOnly) {
		Vector codes = new Vector();
		codes.add(code);
		return getMatchedData(codes, ptOnly);
	}

	public static String getMatchedData(Vector codes, boolean ptOnly) {
		StringBuffer buf = new StringBuffer();
		StringBuffer buf_pt = new StringBuffer();
		StringBuffer all_syn_buf = new StringBuffer();
		String all_syn_str = "";
		for (int j=0; j<codes.size(); j++) {
			String code = (String) codes.elementAt(j);
			buf.append(code).append("|");
			buf_pt.append((String) NCIPTMap.get(code)).append("|");

			if (!ptOnly) {
				StringBuffer buf_syns = new StringBuffer();
				Vector fullSyns = (Vector) code2FULLSYNMap.get(code);
				for (int k=0; k<fullSyns.size(); k++) {
					String syn = (String) fullSyns.elementAt(k);
					buf_syns.append(syn).append("$");
				}
				String syn_str = buf_syns.toString();
				if (syn_str.length() > 0) {
					syn_str = syn_str.substring(0, syn_str.length()-1);
				}
				all_syn_buf.append(syn_str).append("|");
			}
		}
		String codes_str = buf.toString();
		String pts_str = buf_pt.toString();
		if (ptOnly) {
			codes_str = codes_str.substring(0, codes_str.length()-1);
			pts_str = pts_str.substring(0, pts_str.length()-1);
			return codes_str + "\t" + pts_str;
		} else {
			if (codes_str.length() > 0) {
				codes_str = codes_str.substring(0, codes_str.length()-1);
				pts_str = pts_str.substring(0, pts_str.length()-1);
			}
			all_syn_str = all_syn_buf.toString();
			if (all_syn_str.length() > 0) {
				all_syn_str = all_syn_str.substring(0, all_syn_str.length()-1);
			}
		}
		return codes_str + "\t" + pts_str + "\t" + all_syn_str;
	}

	public static String getMatchedData(Vector codes_0) {
		if (codes_0 == null || codes_0.size() == 0) return "";
		Vector codes = new Vector();
		for (int i=0; i<codes_0.size(); i++) {
			String code = (String) codes_0.elementAt(i);
			if (!is_retired(code)) {
				codes.add(code);
			}
		}
		StringBuffer buf = new StringBuffer();
		String s = null;
		//CODES
		if (codes != null && codes.size() > 0) {
			for (int i=0; i<codes.size(); i++) {
				String code = (String) codes.elementAt(i);
				buf.append(code).append("|");
			}
			s = buf.toString();
			s = s.substring(0, s.length()-1);
		}
		String code_str = s;

        // NCI PTs
        StringBuffer pt_buf = new StringBuffer();
        String pts = null;
		if (codes != null && codes.size() > 0) {
			for (int i=0; i<codes.size(); i++) {
				String code = (String) codes.elementAt(i);
				if (NCIPTMap.containsKey(code)) {
					String pt = (String) NCIPTMap.get(code);
					pt_buf.append(pt).append("|");
				}
			}
			pts = pt_buf.toString();
			if (pts.length() > 0) {
				pts = pts.substring(0, pts.length()-1);
			}
		}
		String pt_str = pts;

        // NCI SYs
        StringBuffer sy_buf = new StringBuffer();
        String syns_str = "";
		if (codes != null && codes.size() > 0) {
			for (int i=0; i<codes.size(); i++) {
				String code = (String) codes.elementAt(i);
				StringBuffer sb = new StringBuffer();
				if (NCISYMap.containsKey(code)) {
					Vector syns = (Vector) NCISYMap.get(code);
					for (int k=0; k<syns.size(); k++) {
						String sy = (String) syns.elementAt(k);
						sb.append(sy).append("$");
					}
				}
				s = sb.toString();
				if (s.length() > 0) {
					s = s.substring(0, s.length()-1);
				}
				sy_buf.append(s).append("|");
			}
			syns_str = sy_buf.toString();
			if (syns_str.length() > 0) {
				syns_str = syns_str.substring(0, syns_str.length()-1);
			}
		}
		return code_str + "\t" + pt_str + "\t" + syns_str;
	}

	public static String run(String datafile, String outputfile) {
		return run(datafile, outputfile, 0);
	}

	public static String run(String datafile, String outputfile, int colIndex) {

		System.out.println("datafile: " + datafile);
		System.out.println("outputfile: " + outputfile);
		System.out.println("colIndex: " + colIndex);

		int col = colIndex;
		long ms = System.currentTimeMillis();
		Vector no_matches = new Vector();
		Vector matches = new Vector();
        Vector v = Utils.readFile(datafile);
        Vector w = new Vector();
        String header = (String) v.elementAt(0);
        w.add(header + "\tMatched NCIt Code(s)\tNCI PT(s)\tNCI SY(s)");
        no_matches.add(header);
        matches.add(header + "\tMatched NCIt Code(s)\tNCI PT(s)\tNCI SY(s)");
		for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			line = line.trim();
			if (line.length() > 0) {
				Vector u = StringUtils.parseData(line, '\t');
				String term = (String) u.elementAt(col);
				String term_lc = term.toLowerCase();
				if (!caseSensitive) {
					term = term_lc;
				}
				boolean matched = false;
				if (term2CodesMap.containsKey(term)) {
					Vector codes_0 = (Vector) term2CodesMap.get(term);
					Vector codes = new Vector();
					for (int j=0; j<codes_0.size(); j++) {
						String code = (String) codes_0.elementAt(j);
						if (branch.size() > 0) {
							if (branch.contains(code) && !is_retired(code)) {
								codes.add(code);
							}
						} else {
							if (!is_retired(code)) {
								codes.add(code);
							}
						}
					}
					if (codes.size() > 0) {
						String matchData = getMatchedData(codes);
						w.add(line + "\t" + matchData);
						matches.add(line + "\t" + matchData);
						matched = true;
					} else {
						no_matches.add(line);
						w.add(line + "\tNo match");
					}

				} else {
					no_matches.add(line);
					w.add(line + "\tNo match");
				}
			}
		}
		Utils.saveToFile(outputfile, w);
		Utils.saveToFile("no_matches_" + outputfile, no_matches);
		Utils.saveToFile("matches_" + outputfile, matches);
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
		System.out.println(outputfile + " generated.");
		return outputfile;
	}


//////////////////////////////////////////////////////////////////////////////////////////////
	public static String matchByCode(String datafile, String outputfile) {
		return run(datafile, outputfile, 0);
	}

	public static void expandTerm2CodesMap(String filename) {
		Vector v = Utils.readFile(filename);
        for (int i=0; i<v.size(); i++) {
			String t = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(t, '|');
			String code = (String) u.elementAt(1);
			String term = (String) u.elementAt(0);
			term = term.toLowerCase();
			Vector w = new Vector();
			if (term2CodesMap.containsKey(term)) {
				w =	(Vector) term2CodesMap.get(term);
			}
			if (!w.contains(code)) {
				w.add(code);
			}
			term2CodesMap.put(term, w);
		}
	}

	public static String matchByCode(String datafile, String outputfile, int colIndex) {
		int col = colIndex;
		long ms = System.currentTimeMillis();
		Vector no_matches = new Vector();
		Vector matches = new Vector();
        Vector v = Utils.readFile(datafile);
        Vector w = new Vector();
        String header = (String) v.elementAt(0);
        Vector header_u = StringUtils.parseData(header, '\t');
        String hdr = (String) header_u.elementAt(0) + "\t" + (String) header_u.elementAt(col);
        w.add(hdr + "\tMatched NCIt Code(s)\tNCI PT(s)\tNCI SY(s)");
        no_matches.add(hdr);
        matches.add(hdr + "\tMatched NCIt Code(s)\tNCI PT(s)\tNCI SY(s)");
        int knt = 0;
		for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			line = line.trim();
			if (line.length() > 0) {
				Vector u = StringUtils.parseData(line, '\t');
				boolean matched = false;
				String coding_scheme = (String) u.elementAt(0);
				String source_code = (String) u.elementAt(col);
				String key = "P385$" + source_code;
				Vector codes = new Vector();
				if (sourceCode2LineMap.containsKey(key)) {
					knt++;
					String value = (String) sourceCode2LineMap.get(key);
					Vector value_u = StringUtils.parseData(value, '|');
					String code = (String) value_u.elementAt(1);
					if (branch.size() > 0) {
						if (branch.contains(code) && !is_retired(code)) {
							codes.add(code);
						}
					} else {
						if (!is_retired(code)) {
							codes.add(code);
						}
					}
				}
				if (codes.size() > 0) {
					String matchData = getMatchedData(codes);
					w.add(coding_scheme + "\t" + source_code + "\t" + matchData);
					matches.add(coding_scheme + "\t" + source_code + "\t" + matchData);
					matched = true;
				} else {
					no_matches.add(coding_scheme + "\t" + source_code);
					w.add(coding_scheme + "\t" + source_code + "\tNo match");
				}
			}
		}
		Utils.saveToFile(outputfile, w);
		Utils.saveToFile("no_matches_" + outputfile, no_matches);
		Utils.saveToFile("matches_" + outputfile, matches);
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
		System.out.println(outputfile + " generated.");

		System.out.println("matched codes: " + knt);
		return outputfile;
	}

	public static String matchByProperty(String datafile, int col, String propertyCode) { // P319 FDA_UNII_Code
		//C1000|P319|7MGE0HPM2H
		long ms = System.currentTimeMillis();
		int knt = 0;
		boolean matched = false;
		Vector no_matches = new Vector();
		Vector matches = new Vector();
		Vector w0 = new Vector();
		Vector v = Utils.readFile(PROPERTY_FILE);
		HashMap hmap = new HashMap();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(0);
			String propCode = (String) u.elementAt(1);
			String value = (String) u.elementAt(2);
			if (propCode.compareTo(propertyCode) == 0) {
				Vector w = new Vector();
				if (hmap.containsKey(value)) {
					w = (Vector) hmap.get(value);
				}
				if (!w.contains(code)) {
					w.add(code);
				}
				hmap.put(value, w);
			}
		}

		v = Utils.readFile(datafile);
        String header = (String) v.elementAt(0);
        Vector header_u = StringUtils.parseData(header, '\t');
        String hdr = (String) header_u.elementAt(0);
        w0.add(hdr + "\tMatched NCIt Code(s)\tNCI PT(s)\tNCI SY(s)");
		for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String value = (String) u.elementAt(col);
			if (hmap.containsKey(value)) {
				Vector w = (Vector) hmap.get(value);
				String matchData = getMatchedData(w);
				w0.add(line + "\t" + Utils.vector2Delimited(w, "|") + "\t" + matchData);
				matches.add(line + "\t" + Utils.vector2Delimited(w, "|") + "\t" + matchData);
				matched = true;
				knt++;
			} else {
				no_matches.add(line);
				w0.add(line + "\tNo match");
			}
		}
		String outputfile = "results_" + datafile;
		Utils.saveToFile(outputfile, w0);
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
		System.out.println(outputfile + " generated.");
		System.out.println("matched property values: " + knt);
		return outputfile;
	}


	public static Vector extractRowsFromAxiomFile(Vector req_vec) {
//Recombinant Amphiregulin|C1000|P90|Recombinant Amphiregulin|P383$PT|P384$NCI
		Vector w = new Vector();
		Vector v = Utils.readFile(AXIOM_FILE);
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

}
