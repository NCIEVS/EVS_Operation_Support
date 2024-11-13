package gov.nih.nci.evs.restapi.appl;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.bean.*;
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
 *     Initial implementation kim.ong@nih.gov
 *
 */

public class PediatricSubsetsGenerator {
    String named_graph = null;
    String prefixes = null;
    String serviceUrl = null;
    String username = null;
    String password = null;
    Vector role_source_data = null;

    //OWLSPARQLUtils basicQueryUtils = null;
    static HashSet retired_concepts = null;

    static HashMap subsetMap = null;
    static HashMap code2MabelMap = null;
    static String P310_DATAFILE = "P310_results.txt";
    static String P363_DATAFILE = "P363_results.txt";
    static String P97_DATAFILE = "P97_results.txt";
    static String P108_DATAFILE = "P108_results.txt";

    static String AXIOM_FILE = "axiom_ThesaurusInferred_forTS.txt";
    static String axiomfile = null;

	public HashMap term2CodeMap = new HashMap();
	public HashMap code2PTMap = new HashMap();
	public Vector term_vec = new Vector();

	public static HashMap synonymMap = null;
	public HashMap code2LabelMap = null;

	public HashMap definitionMap = null;
	public HashMap statusMap = null;
	public HashMap preferredTermMap = null;
	public static String HEADING = "NCI Subset Name\tNCI Subset Code\tNCI Code\tNCI Preferred Name\tNCI Synonyms\tNCI DEFINITION\tNeoplastic_Status";
    public String ncit_version = null;
    public Vector role_data = null;

    BasicQueryUtils basicQueryUtils = null;


    public PediatricSubsetsGenerator(String serviceUrl, String named_graph, String username, String password) {
		this.serviceUrl = serviceUrl;
    	this.named_graph = named_graph;
    	this.username = username;
    	this.password = password;
    	initialize();
    }

    public void initialize() {
		basicQueryUtils = new BasicQueryUtils(serviceUrl, named_graph, username, password);
		axiomfile = ConfigurationController.reportGenerationDirectory + File.separator + AXIOM_FILE;
		System.out.println(axiomfile);
		synonymMap = AxiomParser.loadSynonyms(axiomfile);
		System.out.println("synonymMap: " + synonymMap.keySet().size());
        Vector v = null;
        String line = null;
        Vector u = null;

        v = null;
        File f = new File(P310_DATAFILE);
        if (!f.exists()) {
			v = getProperty(named_graph, "P310");
			Utils.saveToFile(P310_DATAFILE, v);
		} else {
			v = Utils.readFile(P310_DATAFILE);
		}
		retired_concepts = new HashSet();
		for (int i=0; i<v.size(); i++) {
			line = (String) v.elementAt(i);
			u = StringUtils.parseData(line, '|');
			//1,2,5,6-Dibenzanthracene|C29789|Concept_Status|Retired_Concept
			String status = (String) u.elementAt(3);
			if (status.compareTo("Retired_Concept") == 0) {
				String code = (String) u.elementAt(1);
				retired_concepts.add(code);
			}
		}
		System.out.println("Retired concepts: " + retired_concepts.size());

        f = new File(P363_DATAFILE);
        if (!f.exists()) {
			v = getProperty(named_graph, "P363");
			Utils.saveToFile(P363_DATAFILE, v);
			System.out.println(P363_DATAFILE + " generated.");
		} else {
			System.out.println(P363_DATAFILE + " exists.");
		}

        f = new File(P310_DATAFILE);
        if (!f.exists()) {
			v = getProperty(named_graph, "P310");
			Utils.saveToFile(P310_DATAFILE, v);
			System.out.println(P310_DATAFILE + " generated.");
		} else {
			System.out.println(P310_DATAFILE + " exists.");
		}

        f = new File(P97_DATAFILE);
        if (!f.exists()) {
			v = getP97(named_graph);
			Utils.saveToFile(P97_DATAFILE, v);
			System.out.println(P97_DATAFILE + " generated.");
		} else {
			System.out.println(P97_DATAFILE + " exists.");
		}

        statusMap = createStatusMap();
        System.out.println("statusMap: " + statusMap.keySet().size());

        definitionMap = createDefinitionMap();
        System.out.println("definitionMap: " + definitionMap.keySet().size());

        role_source_data = getRoleSourceData();
        role_data = new Vector();
        for (int i=0; i<role_source_data.size(); i++) {
		    line = (String) role_source_data.elementAt(i);
		    u = StringUtils.parseData(line, '|');
		    //Stage M0 Childhood Ependymoma by Toronto Guidelines v2|C198489|Disease_Has_Finding|Childhood Lesion|C60644
            role_data.add((String) u.elementAt(1) + "|" + "R108" + "|" + (String) u.elementAt(4));
		}
        code2LabelMap = createCode2LabelMap(role_source_data);

        f = new File(P108_DATAFILE);
        if (!f.exists()) {
 			v = getProperty(named_graph, "P108");
 			Utils.saveToFile(P108_DATAFILE, v);
 			System.out.println(P108_DATAFILE + " generated.");
 		} else {
 			System.out.println(P108_DATAFILE + " exists.");
 		}
        preferredTermMap = createPreferredTermMap();
	}

	public HashMap createPreferredTermMap() {
		HashMap hmap = new HashMap();
		Vector v = Utils.readFile(P108_DATAFILE);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(1);
			String term = (String) u.elementAt(3);
			hmap.put(code, term);
		}
		return hmap;
	}

	public String get_ncit_version() {
        OWLSPARQLUtils owlSPQRALUtils = new OWLSPARQLUtils(serviceUrl, username, password);
		owlSPQRALUtils.set_named_graph(named_graph);
		Vector v = owlSPQRALUtils.get_ontology_info(named_graph);
		Utils.dumpVector("get_ncit_version", v);
		String line = (String) v.elementAt(0);
		Vector u = StringUtils.parseData(line, '|');
		String ncit_version = (String) u.elementAt(0);
		System.out.println(ncit_version);
		return ncit_version;
	}

	public static HashMap createStatusMap() {
		Vector status_vec = Utils.readFile(P363_DATAFILE);
		HashMap statusMap = new HashMap();
        for (int i=0; i<status_vec.size(); i++) {
			String line = (String) status_vec.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(1);
			String status = (String) u.elementAt(3);
			statusMap.put(code, status);
		}
		return statusMap;
	}

/*
    public static HashMap createCode2LabelMap() {
        Vector w = Utils.readFile(hierfile);
        HashMap code2LabelMap = new HashMap();
        for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			code2LabelMap.put((String) u.elementAt(1), (String) u.elementAt(0));
			code2LabelMap.put((String) u.elementAt(3), (String) u.elementAt(2));
		}
		return code2LabelMap;
	}
*/

    public HashMap createCode2LabelMap(Vector w) {
		//Advanced Childhood Malignant Solid Neoplasm|C187210|Disease_Has_Finding|Childhood Lesion|C60644
        //Vector w = Utils.readFile(hierfile);
        HashMap code2LabelMap = new HashMap();
        for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			code2LabelMap.put((String) u.elementAt(1), (String) u.elementAt(0));
			code2LabelMap.put((String) u.elementAt(4), (String) u.elementAt(3));
		}
		return code2LabelMap;
	}

	public boolean isRetired(String code) {
		return retired_concepts.contains(code);
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

	public static void createSourceFolders(String packageName) {
		Vector u = StringUtils.parseData("src." + packageName, '.');
		String currentDir = System.getProperty("user.dir");
		String path = "";
		for (int i=0; i<u.size(); i++) {
			String dir = (String) u.elementAt(i);
			path = path + dir;
			File f = new File(currentDir + "\\" + path);
			if (!f.exists()) {
				f.mkdirs();
				path = path + "\\";
			}
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

/*
	public static String encode(String toEncode) {
		try {
			return java.net.URLEncoder.encode(toEncode.trim(), "UTF-8");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
*/

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
    public static Vector extractColumnValues(String filename, int columnIndex, char delim) {
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

	public static HashMap createMultiValuedHashmap(Vector v) {
		return createMultiValuedHashmap(v, 0, 1, '\t');
	}

	public static HashMap createMultiValuedHashmap(Vector v, char delim) {
		return createMultiValuedHashmap(v, 0, 1, delim);
	}

	public static HashMap createMultiValuedHashmap(Vector v, int keyCol, int valueCol, char delim) {
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

	public static HashMap createMultiValuedHashmap(Vector v, int keyCol, int nameCol, int valueCol, char delim) {
		HashMap hmap = new HashMap();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, delim);
			String key = (String) u.elementAt(keyCol);
			String name = (String) u.elementAt(nameCol);
			String value = (String) u.elementAt(valueCol);
			Vector w = new Vector();
			if (hmap.containsKey(key)) {
				w = (Vector) hmap.get(key);
			}
			NameAndValue nv = new NameAndValue(name, value);
			if (!w.contains(nv)) {
				w.add(nv);
			}
			hmap.put(key, w);
	 	}
		return hmap;
	}

	public static HashMap createHashmap(Vector v, int keyCol, int valueCol, char delim) {
		HashMap hmap = new HashMap();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, delim);
			String key = (String) u.elementAt(keyCol);
			String value = (String) u.elementAt(valueCol);
			Vector w = new Vector();
			hmap.put(key, value);
	 	}
		return hmap;
	}

	public static Vector removeDuplicates(Vector v) {
		Vector w = new Vector();
		HashSet hset = new HashSet();
		for (int i=0; i<v.size(); i++) {
			String t = (String) v.elementAt(i);
			if (!hset.contains(t)) {
				hset.add(t);
				w.add(t);
			}
		}
		return new SortUtils().quickSort(w);
	}

    public static boolean setCurrentDirectory(String directory_name) {
        boolean result = false;  // Boolean indicating whether directory was set
        File directory;       // Desired current working directory
        directory = new File(directory_name).getAbsoluteFile();
        if (directory.exists() || directory.mkdirs())
        {
            result = (System.setProperty("user.dir", directory.getAbsolutePath()) != null);
        }
        return result;
    }

	public static Vector getColumnData(String filename, int col, char delim) {
		return getColumnData(filename, col, delim, true);
	}

	public static Vector getColumnData(String filename, int col, char delim, boolean hasHeader) {
		Vector v = readFile(filename);
		Vector w = new Vector();
		HashSet hset = new HashSet();
		int istart = 0;
		if (hasHeader) {
			istart = 1;
		}
		for (int i=istart; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, delim);
			String value = (String) u.elementAt(col);
			if (!hset.contains(value)) {
				hset.add(value);
				w.add(value);
			}
		}
		return new SortUtils().quickSort(w);
	}


	public static String findVersion(String datafile) {
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
				if (line.indexOf("<owl:versionInfo") != -1) {
					String version = extractTagValue(line, "owl:versionInfo");
					return version;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static void main5(String[] args) {
		long ms = System.currentTimeMillis();
		String uniifile = args[0];
		Vector v = readFile(uniifile);
		HashSet hset = vector2HashSet(v);
		System.out.println(hset.size());

		String colStr = args[1];
		int col = Integer.parseInt(colStr);
		String datafile = args[2];
		char delim = '\t';

		v = readFile(datafile);

		Vector w = new Vector();
		//UNII	Display Name	RN
		w.add("UNII	Display Name	RN");

		for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '\t');
			String unii = (String) u.elementAt(0);

			if (i<10) {
				System.out.println(unii);

			}
			if (hset.contains(unii)) {
				w.add((String) u.elementAt(0) + "\t"+ (String) u.elementAt(1) + "\t" + (String) u.elementAt(2));
			}
		}
		String outputfile = "rn_" + datafile;
		Utils.saveToFile(outputfile, w);
		int m = v.size()-1;
		System.out.println("datafile: " + m);
		int n = w.size()-1;
		System.out.println("hset.size(): " + hset.size());
		System.out.println("w: " + n);
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}

	public String construct_get_P363(String named_graph) {
        String prefixes = basicQueryUtils.getPrefixes();
        StringBuffer buf = new StringBuffer();
        buf.append(prefixes);
        buf.append("SELECT ?x_label ?x_code ?p_label ?p_value").append("\n");
        buf.append("{").append("\n");
        buf.append("from <" + named_graph + ">").append("\n");
        buf.append("{").append("\n");
        buf.append("        ?x a owl:Class .  ").append("\n");
        buf.append("    ?x :NHC0 ?x_code .").append("\n");
        buf.append("    ?x rdfs:label ?x_label . ").append("\n");
        buf.append("        ?p a owl:AnnotationProperty .       ").append("\n");
        buf.append("    ?p :NHC0 ?p_code .").append("\n");
        buf.append("        ?p :NHC0 \"P363\"^^xsd:string .").append("\n");
        buf.append("        ?p rdfs:label ?p_label . ").append("\n");
        buf.append("        ?x ?p ?p_value .").append("\n");
        buf.append("}").append("\n");
        buf.append("}").append("\n");
        return buf.toString();
	}


	public Vector getP363(String named_graph) {
        String query = construct_get_P363(named_graph);
        Vector v = basicQueryUtils.executeQuery(query);
        if (v == null) return null;
        if (v.size() == 0) return v;
        return new SortUtils().quickSort(v);
	}

	public String construct_get_subset(String named_graph) {
        String prefixes = basicQueryUtils.getPrefixes();
        StringBuffer buf = new StringBuffer();
        buf.append(prefixes);
        buf.append("SELECT ?x_label ?x_code ?p_label ?y_label ?y_code ").append("\n");
        buf.append("{").append("\n");
        buf.append("from <" + named_graph + ">").append("\n");
        buf.append("{").append("\n");
        buf.append("        ?x a owl:Class .  ").append("\n");
        buf.append("    ?x :NHC0 ?x_code .").append("\n");
        buf.append("    ?x rdfs:label ?x_label . ").append("\n");
        buf.append("        ?p a owl:AnnotationProperty .       ").append("\n");
        buf.append("        ?p :NHC0 \"A8\"^^xsd:string .").append("\n");
        buf.append("        ?p rdfs:label ?p_label . ").append("\n");
        buf.append("        ?y a owl:Class .  ").append("\n");
        buf.append("    ?y :NHC0 ?y_code .").append("\n");
        buf.append("    ?y rdfs:label ?y_label . ").append("\n");
        buf.append("        ?x ?p ?y").append("\n");
        buf.append("}").append("\n");
        buf.append("}").append("\n");
        return buf.toString();
	}

	public Vector getSubset(String named_graph) {
        String query = construct_get_subset(named_graph);
        Vector v = basicQueryUtils.executeQuery(query);
        if (v == null) return null;
        if (v.size() == 0) return v;
        return new SortUtils().quickSort(v);
	}


	public String construct_get_property(String named_graph, String prop_code) {
        String prefixes = basicQueryUtils.getPrefixes();
        StringBuffer buf = new StringBuffer();
        buf.append(prefixes);
        buf.append("SELECT ?x_label ?x_code ?p_label ?p_value").append("\n");
        buf.append("{").append("\n");
        buf.append("graph <"+ named_graph + ">").append("\n");
        buf.append("{").append("\n");
        buf.append("        ?x a owl:Class .  ").append("\n");
        buf.append("    ?x :NHC0 ?x_code .").append("\n");
        buf.append("    ?x rdfs:label ?x_label . ").append("\n");
        buf.append("        ?p a owl:AnnotationProperty .       ").append("\n");
        buf.append("    ?p :NHC0 ?p_code .").append("\n");
        buf.append("        ?p :NHC0 \"" + prop_code + "\"^^xsd:string .").append("\n");
        buf.append("        ?p rdfs:label ?p_label . ").append("\n");
        buf.append("        ?x ?p ?p_value .").append("\n");
        buf.append("}").append("\n");
        buf.append("}").append("\n");
        return buf.toString();
	}

	public Vector getProperty(String named_graph, String prop_code) {
        String query = construct_get_property(named_graph, prop_code);
        Vector v = basicQueryUtils.executeQuery(query);
        if (v == null) return null;
        if (v.size() == 0) return v;
        return new SortUtils().quickSort(v);
	}

	public String construct_get_P97(String named_graph) {
        String prefixes = basicQueryUtils.getPrefixes();
        StringBuffer buf = new StringBuffer();
        buf.append(prefixes);
        buf.append("select distinct ?x1_label ?x1_code ?a1_target ?q1_code ?q1_value").append("\n");
        buf.append("from <" + named_graph + ">").append("\n");
        buf.append("where  { ").append("\n");
        buf.append("                ?x1 a owl:Class .").append("\n");
        buf.append("                ?x1 :NHC0 ?x1_code .").append("\n");
        buf.append("                ?x1 rdfs:label ?x1_label .").append("\n");
        buf.append("                ?a1 a owl:Axiom .").append("\n");
        buf.append("                ?a1 owl:annotatedSource ?x1 .").append("\n");
        buf.append("                ?a1 owl:annotatedProperty ?p .").append("\n");
        buf.append("                ?a1 owl:annotatedTarget ?a1_target .").append("\n");
        buf.append("                ?p :NHC0 \"P97\"^^xsd:string .").append("\n");
        buf.append("                ?q1 :NHC0 ?q1_code .").append("\n");
        buf.append("                ?a1 ?q1 ?q1_value .").append("\n");
        buf.append("}").append("\n");
        return buf.toString();
	}

	public Vector getP97(String named_graph) {
        String query = construct_get_P97(named_graph);
        Vector v = basicQueryUtils.executeQuery(query);
        if (v == null) return null;
        if (v.size() == 0) return v;
        return new SortUtils().quickSort(v);
	}

	public String construct_get_axioms(String named_graph, String prop_code) {
        String prefixes = basicQueryUtils.getPrefixes();
        StringBuffer buf = new StringBuffer();
        buf.append(prefixes);
        buf.append("select distinct ?a1 ?x1_code ?x1_label ?p_code ?p_label ?a1_target ?q1_code ?q1_label ?q1_value").append("\n");
        buf.append("from <" + named_graph + ">").append("\n");
        buf.append("where  { ").append("\n");
        buf.append("                ?x1 a owl:Class .").append("\n");
        buf.append("                ?x1 :NHC0 ?x1_code .").append("\n");
        buf.append("                ?x1 rdfs:label ?x1_label .").append("\n");
        buf.append("                ?a1 a owl:Axiom .").append("\n");
        buf.append("                ?a1 owl:annotatedSource ?x1 .").append("\n");
        buf.append("                ?a1 owl:annotatedProperty ?p .").append("\n");
        buf.append("                ?a1 owl:annotatedTarget ?a1_target .").append("\n");
        buf.append("                ?p :NHC0 \"" + prop_code + "\"^^xsd:string .").append("\n");
        buf.append("                ?p :NHC0 ?p_code . ").append("\n");
        buf.append("                ?p rdfs:label ?p_label .").append("\n");
        buf.append("                ?q1 :NHC0 ?q1_code .").append("\n");
        buf.append("                ?q1 rdfs:label ?q1_label .").append("\n");
        buf.append("                ?a1 ?q1 ?q1_value .").append("\n");
        buf.append("}").append("\n");
        return buf.toString();
	}


	public Vector getAxioms(String named_graph, String prop_code) {
        String query = construct_get_axioms(named_graph, prop_code);
        Vector v = basicQueryUtils.executeQuery(query);
        if (v == null) return null;
        if (v.size() == 0) return v;
        return new SortUtils().quickSort(v);
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

	public static String searchFile(String prefix, String extension) {
		String currentDir = System.getProperty("user.dir");
		List<String> list = findFilesInDirectory(new File(currentDir));
		for (int i=0; i<list.size(); i++) {
			String t = (String) list.get(i);
			File f = new File(t);
			String name = f.getName();
			if (name.startsWith(prefix) && name.endsWith(extension)) {
				return name;
			}
		}
		return null;
	}

    public void constructChildhoodNeoplasmSubset() {
		// Childhood Neoplasm (Code C6283)
//1.	Pediatric Neoplasm should include all pediatric neoplasms defined as concepts
//      that have or inherit the role Disease_Has_Finding Childhood Lesion
//      (we need Nick to sign off on us adding the role where needed to encompass the handful of obsolete Childhood Neoplasms and Childhood Neoplasm in Remission findings)
//      Childhood Lesion (Code C60644)
//      Disease_Has_Finding 	R108
        Vector v = role_data;//Utils.readFile(rolefile); //C10000|R123|C405
		Vector w = new Vector();
		w.add(HEADING);
		Vector w0 = new Vector();
        for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (line.endsWith("R108|C60644")) {
				Vector u = StringUtils.parseData(line, '|');
				String memberCode = (String) u.elementAt(0);
				String memberLabel = (String) code2LabelMap.get(memberCode);
				//Therapeutic Estetrol|C68928|Concept_In_Subset|FDA Established Names and Unique Ingredient Identifier Codes Terminology|C63923
				//String data = memberLabel + "|" + memberCode + "|Concept_In_Subset|" + "Childhood Neoplasm|C6283";
				String data = appendData("C6283", memberCode);
				w0.add(data);

			}
		}
		//w0 = new SortUtils().quickSort(w0);
		w0 = sortByColumn(3, 2, w0);
		w.addAll(w0);
		Utils.saveToFile("ChildhoodNeoplasmSubset.txt", w);
	}

    public void constructChildhoodBenignNeoplasmSubset() {
		// Childhood Benign Neoplasm (Code C190573)
//2.	Benign Pediatric Neoplasm would only include terms that have or inherit the role Disease_Has_Finding Childhood Lesion AND has Neoplastic_Status = Benign
        Vector v = role_data;//Utils.readFile(rolefile); //C10000|R123|C405
		Vector status_vec = Utils.readFile(P363_DATAFILE);
		HashMap statusMap = new HashMap();
        for (int i=0; i<status_vec.size(); i++) {
			String line = (String) status_vec.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(1);
			String status = (String) u.elementAt(3);
			statusMap.put(code, status);
		}

		Vector w = new Vector();
		w.add(HEADING);
		Vector w0 = new Vector();
        for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (line.endsWith("R108|C60644")) {
				Vector u = StringUtils.parseData(line, '|');
				String memberCode = (String) u.elementAt(0);
				String status = (String) statusMap.get(memberCode);
				if (status == null) {
					String memberLabel = (String) code2LabelMap.get(memberCode);
					System.out.println("WARNING: Neoplastic Status not defined - " + memberLabel + " (" + memberCode + ")");
				} else {
					if (status.compareTo("Benign") == 0) {
						String memberLabel = (String) code2LabelMap.get(memberCode);
						//String data = memberLabel + "|" + memberCode + "|Concept_In_Subset|" + "Childhood Benign Neoplasm|C190573";
						String data = appendData("C190573", memberCode);
						w0.add(data);
					}
				}
			}
		}
		//w0 = new SortUtils().quickSort(w0);
		w0 = sortByColumn(3, 2, w0);
		w.addAll(w0);
		Utils.saveToFile("ChildhoodBenignNeoplasmSubset.txt", w);
	}

    public void constructChildhoodMalignantNeoplasmSubset() {
		// Childhood Malignant Neoplasm (Code C4005)
//3.	Malignant Pediatric Neoplasm would include terms that have or inherit the role Disease_Has_Finding Childhood Lesion AND has Neoplastic_Status = Malignant
        Vector v = role_data;//Utils.readFile(rolefile); //C10000|R123|C405

		Vector w = new Vector();
		w.add(HEADING);
		Vector w0 = new Vector();
        for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (line.endsWith("R108|C60644")) {
				Vector u = StringUtils.parseData(line, '|');
				String memberCode = (String) u.elementAt(0);
				String status = (String) statusMap.get(memberCode);
				if (status == null) {
					String memberLabel = (String) code2LabelMap.get(memberCode);
					System.out.println("WARNING: Neoplastic Status not defined - " + memberLabel + " (" + memberCode + ")");
				} else if (status.compareTo("Malignant") == 0) {
					String memberLabel = (String) code2LabelMap.get(memberCode);
					//String data = memberLabel + "|" + memberCode + "|Concept_In_Subset|" + "Childhood Malignant Neoplasm|C4005";
					String data = appendData("C4005", memberCode);
					w0.add(data);
				}
			}
		}
		//w0 = new SortUtils().quickSort(w0);
		w0 = sortByColumn(3, 2, w0);
		w.addAll(w0);
		Utils.saveToFile("ChildhoodMalignantNeoplasmSubset.txt", w);
	}

	//Percutaneous Coronary Intervention for ST Elevation Myocardial Infarction-Stable After Successful Full-Dose Thrombolytic Therapy|C100001|A percutaneous coronary intervention is necessary for a myocardial infarction that presents with ST segment elevation and the subject does not have recurrent or persistent symptoms, symptoms of heart failure or ventricular arrhythmia after a successful dose of thrombolytic therapy. (ACC)|P378|NCI
    public static HashMap createDefinitionMap() {
		Vector v = Utils.readFile(P97_DATAFILE);
		HashMap hmap = new HashMap();
        for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (line.endsWith("|P378|NCI")) {
				Vector u = StringUtils.parseData(line, '|');
				String code = (String) u.elementAt(1);
				String def = (String) u.elementAt(2);
				hmap.put(code, def);
			}
		}
		return hmap;
	}

	public static Vector sortByColumn(int col1, int col2, Vector v) {
		return sortByColumn('\t', col1, col2, v);
	}

	public static Vector sortByColumn(char delim, int col1, int col2, Vector v) {
		HashMap hmap = new HashMap();
		Vector w = new Vector();
		Vector keys = new Vector();
        for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, delim);
			String value1 = (String) u.elementAt(col1);
			String value2 = (String) u.elementAt(col2);
			String key = value1 + "$" + value2;
			key = key.toLowerCase();
            keys.add(key);
            hmap.put(key, line);
		}
		keys = new SortUtils().quickSort(keys);
        for (int i=0; i<keys.size(); i++) {
			String key = (String) keys.elementAt(i);
		    String line = (String) hmap.get(key);
		    w.add(line);
		}
		return w;
	}

	public String findSynonym(String code, String source, String termType) {
		List list = (List) synonymMap.get(code);
		StringBuffer b = new StringBuffer();
		for (int i=0; i<list.size(); i++) {
			Synonym syn = (Synonym) list.get(i);
			if (syn.getTermGroup().compareTo(termType) == 0 &&
                syn.getTermSource().compareTo(source) == 0) {
				b.append(syn.getTermName()).append("|");
			}
		}
		String term = b.toString();
		if (term.length() > 0) {
			term = term.substring(0, term.length()-1);
		}
		return term;
	}

	public String findSynonym(String code, String source, HashSet hset) {
		List list = (List) synonymMap.get(code);
		StringBuffer b = new StringBuffer();
		for (int i=0; i<list.size(); i++) {
			Synonym syn = (Synonym) list.get(i);
			if (hset.contains(syn.getTermGroup()) &&
                syn.getTermSource().compareTo(source) == 0) {
				b.append(syn.getTermName()).append("|");
			}
		}
		String term = b.toString();
		if (term.length() > 0) {
			term = term.substring(0, term.length()-1);
		}
		return term;
	}

	public String appendData(String subsetCode, String code) {
		StringBuffer buf = new StringBuffer();
		buf.append((String) code2LabelMap.get(subsetCode)).append("\t");
		buf.append(subsetCode).append("\t");
		buf.append(code).append("\t");

        //String pt = findSynonym(code, "NCI", "PT");
        String pt = (String) preferredTermMap.get(code);//(code, "NCI", "PT");
        if (pt.length() == 0) {
			String label = (String) code2LabelMap.get(code);
			System.out.println("Warning: NCI PT does not exist -- " + label + " (" + code + ").");
			//pt = findSynonym(code, "NCI", "HD");
		}
		buf.append(pt).append("\t");

        HashSet hset = new HashSet();
        hset.add("SY");
        hset.add("AQS");
        String syns = findSynonym(code, "NCI", hset);
		buf.append(syns).append("\t");

		buf.append((String) definitionMap.get(code)).append("\t");
		buf.append((String) statusMap.get(code)).append("\t");
		return buf.toString();
	}

	public String construct_get_role_source(String named_graph, String prop_code, String target_code) {
        String prefixes = basicQueryUtils.getPrefixes();
        StringBuffer buf = new StringBuffer();
        buf.append(prefixes);
        buf.append("select distinct ?x_label ?x_code ?p_label ?y_label ?y_code ").append("\n");
        buf.append("from <" + named_graph + ">").append("\n");
        buf.append("where").append("\n");
        buf.append("{").append("\n");
        buf.append("            ?x a owl:Class .").append("\n");
        buf.append("            ?x :NHC0 ?x_code .").append("\n");
        buf.append("            ?x rdfs:label ?x_label .").append("\n");
        buf.append("            ?x rdfs:subClassOf ?rs .").append("\n");
        buf.append("            ?rs a owl:Restriction .").append("\n");
        buf.append("            ?rs owl:onProperty ?p .").append("\n");
        buf.append("            ?p :NHC0 \"" + prop_code + "\"^^xsd:string .").append("\n");
        buf.append("            ?p rdfs:label ?p_label .").append("\n");
        buf.append("            ?rs owl:someValuesFrom ?y .").append("\n");
        buf.append("            ?y :NHC0 ?y_code .").append("\n");
        buf.append("            ?y :NHC0 \"" + target_code + "\"^^xsd:string .").append("\n");
        buf.append("            ?y rdfs:label ?y_label .").append("\n");
        buf.append("}").append("\n");
        return buf.toString();
	}

	public Vector getRoleSource(String named_graph, String prop_code, String target_code) {
        String query = construct_get_role_source(named_graph, prop_code, target_code);
        Vector v = basicQueryUtils.executeQuery(query);
        if (v == null) return null;
        if (v.size() == 0) return v;
        return new SortUtils().quickSort(v);
	}

	public String construct_get_role_source_part2(String named_graph, String prop_code, String target_code) {
        String prefixes = basicQueryUtils.getPrefixes();
        StringBuffer buf = new StringBuffer();
        buf.append(prefixes);
        buf.append("select distinct ?x_label ?x_code ?p_label ?y_label ?y_code ").append("\n");
        buf.append("from <" + named_graph + ">").append("\n");
        buf.append("where").append("\n");
        buf.append("{").append("\n");
        buf.append("            ?x a owl:Class .").append("\n");
        buf.append("            ?x :NHC0 ?x_code .").append("\n");
        buf.append("            ?x rdfs:label ?x_label .").append("\n");
        buf.append("            ?x owl:equivalentClass ?z0 .").append("\n");
        buf.append("            ?z0 a owl:Class .").append("\n");
        buf.append("            ?z0 owl:intersectionOf ?list .").append("\n");
        buf.append("            ?list rdf:rest*/rdf:first ?rs .").append("\n");
        buf.append("            ?rs a owl:Restriction .").append("\n");
        buf.append("            ?rs owl:onProperty ?p .").append("\n");
        buf.append("            ?p :NHC0 \"" + prop_code + "\"^^xsd:string .").append("\n");
        buf.append("            ?p rdfs:label ?p_label .").append("\n");
        buf.append("            ?rs owl:someValuesFrom ?y .").append("\n");
        buf.append("            ?y :NHC0 ?y_code .").append("\n");
        buf.append("            ?y :NHC0 \"" + target_code + "\"^^xsd:string .").append("\n");
        buf.append("            ?y rdfs:label ?y_label .").append("\n");
        buf.append("}").append("\n");
        return buf.toString();
	}


	public Vector getRoleSourcePart2(String named_graph, String prop_code, String target_code) {
        String query = construct_get_role_source_part2(named_graph, prop_code, target_code);
        Vector v = basicQueryUtils.executeQuery(query);
        if (v == null) return null;
        if (v.size() == 0) return v;
        return new SortUtils().quickSort(v);
	}

	public Vector getRoleSourceData() {
		String serviceUrl = ConfigurationController.serviceUrl;
		String named_graph = ConfigurationController.namedGraph;
		String username = ConfigurationController.username;
		String password = ConfigurationController.password;
		Vector v1 = getRoleSource(named_graph, "R108", "C60644");
		if (v1 != null) {
			System.out.println("v1: " + v1.size());
		} else {
			System.out.println("v1 == null ");
		}
		Vector v2 = getRoleSourcePart2(named_graph, "R108", "C60644");
		if (v2 != null) {
			System.out.println("v2: " + v2.size());
		} else {
			System.out.println("v2 == null ");
		}

		v1.addAll(v2);
		HashSet hset = new HashSet();
		for (int i=0; i<v1.size(); i++) {
		    String t = (String) v1.elementAt(i);
		    if (!hset.contains(t)) {
				hset.add(t);
			}
		}
		for (int i=0; i<v2.size(); i++) {
		    String t = (String) v1.elementAt(i);
		    if (!hset.contains(t)) {
				hset.add(t);
			}
		}
		v1 = new Vector();
		Iterator it = hset.iterator();
		while (it.hasNext()) {
			String line = (String) it.next();
			v1.add(line);
		}
		v1 = new SortUtils().quickSort(v1);
		/*
		String outputfile = "C60644.txt";
		Utils.saveToFile(outputfile, v1);
		System.out.println(outputfile + " generated.");
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
		*/
		return v1;
	}

	//ChildhoodNei=oplasmSubset.txt
	public Vector searchDistinctTermTypes(String filename) {
		Vector w = new Vector();
		HashSet hset = new HashSet();
		Vector v = Utils.readFile(filename);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '\t');
			String code = (String) u.elementAt(2);
			List list = (List) synonymMap.get(code);
			if (list != null) {
				for (int j=0; j<list.size(); j++) {
					Synonym syn = (Synonym) list.get(j);
					String termType = syn.getTermGroup();
					if (!hset.contains(termType)) {
						hset.add(termType);
						w.add(termType);
					}
				}
			}
		}
		w = new SortUtils().quickSort(w);
		return w;
	}

	public String construct_get_association(String named_graph, String asso_code) {
        String prefixes = basicQueryUtils.getPrefixes();
        StringBuffer buf = new StringBuffer();
        buf.append(prefixes);
        buf.append("SELECT ?x_label ?x_code ?p_label ?y_label ?y_code ").append("\n");
        buf.append("{").append("\n");
        buf.append("graph <" + named_graph + ">").append("\n");
        buf.append("{").append("\n");
        buf.append("        ?x a owl:Class .  ").append("\n");
        buf.append("    	?x :NHC0 ?x_code .").append("\n");
        buf.append("    	?x rdfs:label ?x_label . ").append("\n");
        buf.append("        ?p a owl:AnnotationProperty .       ").append("\n");
        buf.append("        ?p :NHC0 \"" + asso_code + "\"^^xsd:string .").append("\n");
        buf.append("        ?p rdfs:label ?p_label . ").append("\n");
        buf.append("        ?y a owl:Class .  ").append("\n");
        buf.append("    	?y :NHC0 ?y_code .").append("\n");
        buf.append("    	?y rdfs:label ?y_label . ").append("\n");
        buf.append("        ?x ?p ?y").append("\n");
        buf.append("}").append("\n");
        buf.append("}").append("\n");
        return buf.toString();
	}

	public Vector getAssociation(String named_graph, String asso_code) {
        String query = construct_get_association(named_graph, asso_code);
        Vector v = basicQueryUtils.executeQuery(query);
        if (v == null) return null;
        if (v.size() == 0) return v;
        return new SortUtils().quickSort(v);
	}

	public static String encode(String t) {
		t = t.replace("&apos;", "'");
		t = t.replace("&lt;", "<");
		t = t.replace("&gt;", ">");
		t = t.replace("&quot;", "\"");
		t = t.replace("&amp;", "&");
        return t;
	}

	public static Vector encode(Vector v) {
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String t = (String) v.elementAt(i);
			String t1 = encode(t);
			if (t1.compareTo(t) != 0) {
				System.out.println("\nReplace " + t);
				System.out.println("by      "  + t1);
			}
			w.add(t1);
		}
		return w;
	}

	public static void encodeFile(String filename) {
		Vector v = Utils.readFile(filename);
		v = encode(v);
		Utils.saveToFile(filename, v);
	}

    public void run() {
		constructChildhoodNeoplasmSubset();
		constructChildhoodBenignNeoplasmSubset();
		constructChildhoodMalignantNeoplasmSubset();

		encodeFile("ChildhoodNeoplasmSubset.txt");
		encodeFile("ChildhoodBenignNeoplasmSubset.txt");
		encodeFile("ChildhoodMalignantNeoplasmSubset.txt");

		Vector datafile_vec = new Vector();
		datafile_vec.add("ChildhoodNeoplasmSubset.txt");
		datafile_vec.add("ChildhoodBenignNeoplasmSubset.txt");
		datafile_vec.add("ChildhoodMalignantNeoplasmSubset.txt");

		Vector sheetLabel_vec = new Vector();
		sheetLabel_vec.add("Childhood Neoplasm");
		sheetLabel_vec.add("Childhood Benign Neoplasm");
		sheetLabel_vec.add("Childhood Malignant Neoplasm");
		String version = get_ncit_version();
		String label0 = "PediatricNeoplasmSubsets";
		char delim = '\t';
		String excelfile = label0 + "_(" + version + ")_" + StringUtils.getToday() + ".xlsx";
		new ExcelWriter().writeToXSSF(datafile_vec, excelfile, delim, sheetLabel_vec, null);
		System.out.println(excelfile + " generated.");
	}

	public static void main(String[] args) {
		long ms = System.currentTimeMillis();
		String serviceUrl = ConfigurationController.serviceUrl;
		String namedGraph = ConfigurationController.namedGraph;
		String username = ConfigurationController.username;
		String password = ConfigurationController.password;
		PediatricSubsetsGenerator utils = new PediatricSubsetsGenerator(serviceUrl, namedGraph, username, password);
		utils.run();
		System.out.println("Done");
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}
}

