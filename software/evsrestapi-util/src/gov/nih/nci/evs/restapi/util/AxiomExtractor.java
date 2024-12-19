package gov.nih.nci.evs.restapi.util;

import java.io.*;
import java.text.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;
import java.nio.charset.Charset;

import java.util.regex.*;
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

public class AxiomExtractor {
	 static PorterStemmer stemmer = null;
	 static String AXIOM_FILE = "axiom_ThesaurusInferred_forTS.owl";
	 static String STOPWORD_FILE = "stop_words.txt";
     static HashSet STOP_WORDS = null;
    static {
   	   stemmer = new PorterStemmer();
   	   STOP_WORDS = createStopWordSet(STOPWORD_FILE);
    }

	public static HashSet createStopWordSet(String filename) {
		File f = new File(filename);
		HashSet hset = new HashSet();
		if(f.exists() && !f.isDirectory()) {
			Vector v = Utils.readFile(filename);
			for (int i=0; i<v.size(); i++) {
				String word = (String) v.elementAt(i);
				if (!hset.contains(word)) {
					hset.add(word);
				}
			}
		} else {
			System.out.println("WARNING: file " + filename + " does not exist.");
		}
		return hset;
	}

	public static boolean isFiller(String word) {
		return STOP_WORDS.contains(word);
	}

	 static boolean isDigitOrDash(String s) {
		 for (int i=0; i<s.length(); i++) {
			 char c = s.charAt(i);
			 if (!(Character.isDigit(c) || c == '-')) {
				 return false;
			 }
		 }
		 return true;
	 }

	public static String stemTerm(String term) {
		return stemmer.stem(term);
	}

     public static String removeSpecialCharacters(String t) {
		t = t.replace("(", " ");
		t = t.replace(")", " ");
		t = t.replace(",", " ");
		t = t.replace(";", " ");
		t = t.replace("'s", " ");
		t = t.replace("-", " ");
		t = t.replace("/", " ");
		t = t.replace("+", " ");
		t = t.replace("—", " ");
		t = t.replace(".", " ");

		//add &
		t = t.replace("&", " ");
		return t;
	 }

    public static Vector tokenize(String term) {
		term = term.toLowerCase();
		term = removeSpecialCharacters(term);
		Vector words = new Vector();
		StringTokenizer st = new StringTokenizer(term);
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (!isFiller(token)) {
				token = stemTerm(token);
				words.add(token);
			}
		}
		return words;
	}

    public static Vector tokenize(String term, boolean applyStem) {
		term = term.toLowerCase();
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

	 public static String run(String branchfile) {
		 Vector v = Utils.readFile(branchfile);
		 int n = branchfile.indexOf(".");
		 String root = branchfile.substring(0, n);
		 HashSet hset = Utils.vector2HashSet(v);
		 System.out.println(hset.size());
		 Vector w = Utils.readFile(AXIOM_FILE);
		 System.out.println(w.size());
		 Vector v1 = new Vector();
		 for (int i=0; i<w.size(); i++) {
			 String line = (String) w.elementAt(i);
			 Vector u = StringUtils.parseData(line, '|');
			 String code = (String) u.elementAt(1);
			 if (hset.contains(code)) {
				 System.out.println(line);
				 System.out.println(code);
				 String prop_code = (String) u.elementAt(2);
				 if (prop_code.compareTo("P90") == 0) {
					 String term = (String) u.elementAt(3);
					 System.out.println(term);
					 Vector w1 = tokenize(term);
					 for (int j=0; j<w1.size(); j++) {
						 String token = (String) w1.elementAt(j);
						 System.out.println(token);
						 if (token.indexOf("/") != -1) {
							 Vector u2 = StringUtils.parseData(token, '/');
							 for (int k=0; k<u2.size(); k++) {
								 String name = (String) u2.elementAt(k);
								 if (!v1.contains(name)) {
									 if (!isDigitOrDash(name)) {
									 	v1.add(name);
									 }
								 }
							 }
						 } else {
							 if (!v1.contains(token)) {
								 if (!isDigitOrDash(token)) {
									System.out.println("adding " + token);
									v1.add(token);
								 }
						     }
						 }
					 }
				 }
			 }
		 }

         v1 = new SortUtils().quickSort(v1);
         Utils.saveToFile("keyword_" + root + ".txt", v1);
         return "keyword_" + root + ".txt";
	 }
}
