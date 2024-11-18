package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;
import gov.nih.nci.evs.restapi.bean.*;

import java.io.*;
import java.text.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;
import java.nio.charset.Charset;
import opennlp.tools.stemmer.PorterStemmer;

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


public class Normalizer {
	public static HashSet STOP_WORD_SET = null;
	public static String SYNONYM_FILE = "synonym.txt";
	public static String DISCARDED_WORD_FILE = "discarded.txt";
	public static String STOP_WORD_FILE = "stop_words.txt";
    static PorterStemmer stemmer = null;

    static {
		stemmer = new PorterStemmer();
		loadStopWords();
	}

	public static String stemTerm(String term) {
		return stemmer.stem(term);
	}

	public static void parseMatchData(String line) {
		Vector v1 = StringUtils.parseData(line, '\t');
		Utils.dumpVector(line, v1);
		for (int i=0; i<v1.size(); i++) {
			String t1 = (String) v1.elementAt(i);
			Vector v2 = StringUtils.parseData(t1, '|');
			Utils.dumpVector(t1, v2);
			for (int j=0; j<v2.size(); j++) {
				String t2 = (String) v2.elementAt(j);
			    Vector v3 = StringUtils.parseData(t2, '$');
			    Utils.dumpVector(t2, v3);
			}
		}
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
     public static String removeSpecialCharacters(String t) {
		t = t.replace("(", " ");
		t = t.replace(")", " ");
		t = t.replace(",", " ");
		t = t.replace(";", " ");
		t = t.replace("'s", " ");
		//t = t.replace("-", " ");
		t = t.replace("+", " ");
		t = t.replace("/", " ");
		//t = t.replace("—", " ");
		t = t.replace(".", " ");
		t = t.replace(":", " ");
		//t = t.replace("/", " ");
		//add &
		t = t.replace("&", " ");
		return t;
	 }

    public static boolean isLowerCase(String t) {
		if (t.length() > 1) {
			t = t.substring(1, t.length());
		}
		String s = t.toLowerCase();
		if (s.compareTo(t) == 0) return true;
		return false;
	}

    public static Vector tokenize(String term, boolean applyStem) {
		//term = term.toLowerCase();
		term = removeSpecialCharacters(term);
		Vector words = new Vector();
		StringTokenizer st = new StringTokenizer(term);
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (!isFiller(token)) {
				if (applyStem) {
					token = stemTerm(token);
				}
				words.add(token);
			}
		}
		return words;
	}

	public static Vector trimVec(Vector v) {
		int j = v.size()-1;
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			j = v.size()-1-i;
			String s = (String) v.elementAt(j);
			s = s.trim();
			if (s.length() > 0) {
				break;
			}
		}
		w = new Vector();
		for (int i=0; i<=j; i++) {
			String s = (String) v.elementAt(i);
			w.add(s);
		}
		return w;
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

	public static Vector normalize(String filename, int col) {
        Vector v = Utils.readFile(filename);
        Vector w2 = new Vector();
        boolean applyStem = false;
        Vector terms = new Vector();
        for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);

			Vector u = StringUtils.parseData(line, '\t');
			u = trimVec(u);

			if (u.size() > col) {
				String s2 = (String) u.elementAt(col);
				terms.add(s2);

				Vector w = tokenize(s2, applyStem);
				//Utils.dumpVector(s2, w);
				//ALK and ALK gene fusions

				Vector w1 = new Vector();
				for (int k=0; k<w.size(); k++) {
					String t = (String) w.elementAt(k);
					if (!isLowerCase(t)) {
						w1.add(t);
					}
				}
				w2.addAll(w1);
			}
		}
		w2 = removeDuplicates(w2);
		w2 = new SortUtils().quickSort(w2);
		return w2;
	}

	public static Vector normalize(String filename, int col0, int col) {
        Vector v = Utils.readFile(filename);
        Vector w2 = new Vector();
        boolean applyStem = false;
        Vector terms = new Vector();
        for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);

			Vector u = StringUtils.parseData(line, '\t');
			u = trimVec(u);

			if (u.size() > col) {
				String s0 = (String) u.elementAt(col0);
				String s2 = (String) u.elementAt(col);
				terms.add(s2);

				Vector w = tokenize(s2, applyStem);

				Vector w1 = new Vector();
				for (int k=0; k<w.size(); k++) {
					String t = (String) w.elementAt(k);
					if (!isLowerCase(t)) {
						w1.add(s0 + "|" + t);
					}
				}
				w2.addAll(w1);
			}
		}
		w2 = removeDuplicates(w2);
		w2 = new SortUtils().quickSort(w2);
		return w2;
	}

	public static boolean isCompoundWord(String term) {
		Vector u = StringUtils.parseData(term, '-');
		if (u.size() == 1) return false;
		boolean retval = true;
		for (int j=0; j<u.size(); j++) {
			String s = (String) u.elementAt(j);
			if (!isLowerCase(s)) {
				retval = false;
				break;
			}
		}
		return retval;
	}

	public static boolean isUpperCaseOrDigit(String term) {
		term = term.trim();
		Vector u = StringUtils.parseData(term, '-');
		for (int i=0; i<u.size(); i++) {
			String token = (String) u.elementAt(i);
			for (int j=0; j<token.length(); j++) {
				char c = token.charAt(j);
				if (!Character.isDigit(c)) {
					String t = "" + c;
					if (t.toLowerCase().compareTo(t) == 0) {
						return false;
					}
				}
			}
		}
		return true;
	}

	public static Vector findCompoundWords(String filename) {
		Vector v = Utils.readFile(filename);
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '\t');
			String term = (String) u.elementAt(0);
			if (isCompoundWord(term)) {
				boolean retval = isUpperCaseOrDigit(term);
				if (!retval) {
					w.add(term);
				}
			}
		}
		return w;
	}

/*
	public static void main(String[] args) {
		long ms = System.currentTimeMillis();
		Vector w1 = normalize("RelevantMolecularTargets.txt", 1);
		Vector w2 = normalize("RelevantMolecularTargets.txt", 2);
		w1.addAll(w2);
		w1 = removeDuplicates(w1);
		w1 = new SortUtils().quickSort(w1);
        Utils.saveToFile("tokens.txt", w1);
        System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}
*/
	public static void main(String[] args) {
		long ms = System.currentTimeMillis();
		Vector w1 = normalize("RelevantMolecularTargets.txt", 0, 1);
		Vector w2 = normalize("RelevantMolecularTargets.txt", 0, 2);
		w1.addAll(w2);
		w1 = removeDuplicates(w1);
		w1 = new SortUtils().quickSort(w1);
        Utils.saveToFile("tokens_v2.txt", w1);
        System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}

/*
	public static void main(String[] args) {
		String filename = args[0];
		Vector v = readFile(filename);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			parseMatchData(line);
		}
	}
*/
}

