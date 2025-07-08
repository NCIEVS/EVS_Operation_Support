package gov.nih.nci.evs.restapi.util;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.text.similarity.*;
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
public class IndexUtils {

	public static String[] specialChars = new String[] {"{", "}", "(", ")", "'", ":", ";", ".", "," ,"\""};
	public static HashSet specialCharsHashSet = toHashSet(specialChars);
	static PorterStemmer stemmer = null;

	String serviceUrl = null;
	String namedGraph = null;
	String username = null;
	String password = null;
	SPARQLSearchUtils searchUtils = null;

    public static HashSet STOP_WORDS = null;
    public static HashSet KEYWORDS = null;
    public static HashMap signatureMap = null;
    public static HashMap id2LabelMap = null;
    static int MAX_MATCHES = 20;

    static {
		long ms = System.currentTimeMillis();
		System.out.println("Initializing IndexUtils ...");
		stemmer = new PorterStemmer();
		STOP_WORDS = LexicalMatching.STOP_WORDS;
		KEYWORDS = LexicalMatching.KEYWORDS;
		signatureMap = LexicalMatching.signatureMap;
		id2LabelMap = LexicalMatching.id2LabelMap;
		System.out.println("KEYWORDS: " + KEYWORDS.size());
		System.out.println("STOP_WORDS: " + STOP_WORDS.size());
		System.out.println("Completed initializing IndexUtils.");
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}

    public IndexUtils() {

    }

	public static String stemTerm(String term) {
		return LexicalMatching.stemTerm(term);
	}

    public IndexUtils(String serviceUrl, String namedGraph, String username, String password) {
		this.serviceUrl = serviceUrl;
		this.namedGraph = namedGraph;
        searchUtils = new SPARQLSearchUtils(serviceUrl, namedGraph, username, password);
	}

	public static HashSet toHashSet(Vector a) {
		HashSet hset = new HashSet();
		for (int i=0; i<a.size(); i++) {
			String t = (String) a.elementAt(i);
			hset.add(t);
		}
		return hset;
	}

	public static HashSet toHashSet(String[] a) {
		HashSet hset = new HashSet();
		for (int i=0; i<a.length; i++) {
			String t = a[i];
			hset.add(t);
		}
		return hset;
	}

    public Vector toSentences(String narrative) {
		Vector w = new Vector();
		Pattern re = Pattern.compile("[^.!?\\s][^.!?]*(?:[.!?](?!['\"]?\\s|$)[^.!?]*)*[.!?]?['\"]?(?=\\s|$)", Pattern.MULTILINE | Pattern.COMMENTS);
		Matcher reMatcher = re.matcher(narrative);
		while (reMatcher.find()) {
			w.add(reMatcher.group());
		}
		return w;
	}

	public Vector addAll(Vector v, Vector u) {
		for (int i=0; i<u.size(); i++) {
			String t = (String) u.elementAt(i);
			if (!v.contains(t)) {
				v.add(t);
			}
		}
		return v;
	}

	public Vector indexNarrative(String narrative) {
		Vector w = toSentences(narrative);
		Vector v = new Vector();
		for (int i=0; i<w.size(); i++) {
			String sentence = (String) w.elementAt(i);
			Vector segments = toSegments(sentence);
			for (int j=0; j<segments.size(); j++) {
				String segment = (String) segments.elementAt(j);
				Vector u = indexTerm(segment);
				if (u != null && u.size()>0 && u.size() <= MAX_MATCHES) {
					addAll(v, u);
				} else if (u != null && u.size()>0 && u.size() > MAX_MATCHES) {
					Vector u1 = new Vector();
					for (int k=0; k<MAX_MATCHES; k++) {
						String t = (String) u.elementAt(k);
						u1.add(t);
					}
					addAll(v, u1);
				}
			}
		}
		return v;
	}

    public static boolean isKeyword(String word) {
		return KEYWORDS.contains(word);
	}

    public static boolean isFiller(String word) {
		return STOP_WORDS.contains(word);
	}

	public Vector term2Keywords(String sentence) {
		char c = sentence.charAt(sentence.length()-1);
		while (!Character.isLetterOrDigit(c)) {
			sentence = sentence.substring(0, sentence.length()-1);
			c = sentence.charAt(sentence.length()-1);
		}
		return LexicalMatching.tokenize(sentence);
	}

	public Vector toSegments(String sentence) {
		Vector v = new Vector();
		Vector w = term2Keywords(sentence);
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<w.size(); i++) {
			String word = (String) w.elementAt(i);
			boolean isKeyword = isKeyword(word);
            boolean isFiller = isFiller(word);
            if (isKeyword && !isFiller) {
				buf.append(word).append(" ");
			} else if(!isKeyword) {
				String t = buf.toString();
				t = t.trim();
				v.add(t);
				buf = new StringBuffer();
			}
		}
		if (buf.length() > 0) {
			String t = buf.toString();
			t = t.trim();
			v.add(t);
		}
		return v;

	}

	public String getLabel(String code) {
		return (String) id2LabelMap.get(code);
	}

	public Vector getCodeBySignature(String signature) {
		if (signatureMap.containsKey(signature)) {
			return (Vector) signatureMap.get(signature);
		}
		return null;
	}

	public static Vector toKeywords(String term) {
		HashSet hset = new HashSet();
		term = term.trim();
		term = term.replace("\"", "");
		term = term.trim();
		if (term.length() > 0) {
			term = term.toLowerCase();
			term = term.replace("/", " ");
			String[] tokens = term.split(" ");
			for (int j=0; j<tokens.length; j++) {
				String token = tokens[j];
				if (token.length() > 1) {
					char firstChar = token.charAt(0);
					char lastChar = token.charAt(token.length()-1);
					if (specialCharsHashSet.contains("" + firstChar) &&
						specialCharsHashSet.contains("" + lastChar)) {
						token = token.substring(1, token.length()-1);
					} else if (specialCharsHashSet.contains("" + lastChar)) {
						token = token.substring(0, token.length()-1);
					} else if (specialCharsHashSet.contains("" + firstChar)) {
						token = token.substring(1, token.length());
					}
					if (token.length() > 0) {
						if (!hset.contains(token)) {
							hset.add(token);
						}
					}
				} else if (token.length() == 1) {
					if (!hset.contains(token)) {
						hset.add(token);
					}
				}
			}
		}
		return new SortUtils().quickSort(Utils.hashSet2Vector(hset));
	}

    public String getSignature(String term) {
		return LexicalMatching.getSignature(term);
	}

	public String words2Term(Vector words) {
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<words.size(); i++) {
			String word = (String) words.elementAt(i);
			buf.append(word);
			if (i < words.size()-1) {
				buf.append(" ");
			}
		}
		return buf.toString();
	}

	public Vector matchBySignature(Vector words) {
		String signature = getSignature(words2Term(words));
		return getCodeBySignature(signature);
	}

	public Vector matchBySignature(String term) {
		String signature = getSignature(term);
		return getCodeBySignature(signature);
	}

	public boolean checkCoocurrence(String word1, String word2) {
		return LexicalMatching.checkCoocurrence(word1, word2);
	}

	public Vector removeFillers(Vector words) {
		Vector w = new Vector();
		for (int i=0; i<words.size(); i++) {
			String word = (String) words.elementAt(i);
			if (!isFiller(word)) {
				w.add(word);
			}
		}
		return w;
	}

	public Vector findExactMatches(String t, Vector v) {
		Vector w = new Vector();
		if (v == null) {
			return w;
		}
		if (v == null || v.size() == 0) return w;
		for (int i=0; i<v.size(); i++) {
			String code = (String) v.elementAt(i);
			String label = getLabel(code);
			if (label.compareToIgnoreCase(t) == 0) {
				w.add(code);
			}
		}
		if (w.size() == 0) return v;
		return w;
	}

	public Vector indexTerm(String term) {
		boolean debug = false;
		if (debug) System.out.println("indexTerm: " + term);
		Vector w1 = new Vector();
		Vector w = matchBySignature(term);
		w = findExactMatches(term, w);
		if (w != null && w.size() > 0) {
			for (int i=0; i<w.size(); i++) {
				String code = (String) w.elementAt(i);
				String label = getLabel(code);
				w1.add(label + "|" + code);
			}
			return w1;
		}
		w = new Vector();
        Vector words = term2Keywords(term);
        words = removeFillers(words);
        Vector v0 = new Vector();
        if (words == null || words.size() == 0) {
			return w;
		}
        String prevword = null;
        String nextword = null;
        if (words.size() == 1) {
			prevword = (String) words.remove(0);
			w = matchBySignature(prevword);
			w = findExactMatches(prevword, w);
			if (w != null && w.size() > 0) {
				for (int i=0; i<w.size(); i++) {
					String code = (String) w.elementAt(i);
					String label = getLabel(code);
					w1.add(label + "|" + code);
				}
				return w1;
			}
		}
		boolean cooccur = true;
		StringBuffer buf = new StringBuffer();
		if (words.size() > 0) {
			prevword = (String) words.remove(0);
			if (debug) System.out.println("prevword: " + prevword);
			buf.append(prevword);
			while (words.size() > 0) {
				if (buf.length() > 0) {
					String t = buf.toString();
					if (debug) System.out.println("t: " + t);
					w = matchBySignature(t);
					w = findExactMatches(t, w);
					if (debug) Utils.dumpVector(t, w);
					if (w != null && w.size() > 0) {
						for (int i=0; i<w.size(); i++) {
							String code = (String) w.elementAt(i);
							String label = getLabel(code);
							if (!w1.contains(label + "|" + code)) {
								w1.add(label + "|" + code);
							}
						}
					}
				}

				nextword = (String) words.remove(0);
				w = matchBySignature(nextword);
				w = findExactMatches(nextword, w);
				if (w != null && w.size() > 0) {
					if (debug) Utils.dumpVector(nextword, w);

					for (int i=0; i<w.size(); i++) {
						String code = (String) w.elementAt(i);
						String label = getLabel(code);
						if (!w1.contains(label + "|" + code)) {
							w1.add(label + "|" + code);
						}
					}
				}

				cooccur = checkCoocurrence(prevword, nextword);
				if (debug) System.out.println("cooccur: " + cooccur);

				if (!cooccur) {
					String t = buf.toString();
					w = matchBySignature(t);
					w = findExactMatches(t, w);
					if (w != null && w.size() > 0) {
						if (debug) Utils.dumpVector(t, w);
						for (int i=0; i<w.size(); i++) {
							String code = (String) w.elementAt(i);
							String label = getLabel(code);
							if (!w1.contains(label + "|" + code)) {
								w1.add(label + "|" + code);
							}
						}
					}
					buf = new StringBuffer();
					buf.append(nextword);
					String s1 = buf.toString();
					if (debug) System.out.println("s1: " + s1);
				} else {
					//buf = new StringBuffer();
					buf.append(" ").append(nextword);
					String s2 = buf.toString();
					if (debug) System.out.println("s2: " + s2);
				}
			}
		}

		if (buf.length() > 0) {
			String t = buf.toString();
			if (debug) System.out.println("final: " + t);
			w = matchBySignature(t);
			w = findExactMatches(t, w);
			if (debug) Utils.dumpVector(t, w);
			if (w != null && w.size() > 0) {
				for (int i=0; i<w.size(); i++) {
					String code = (String) w.elementAt(i);
					String label = getLabel(code);
					if (!w1.contains(label + "|" + code)) {
						w1.add(label + "|" + code);
					}
				}
			} else {
				w = matchBySignature(nextword);
				w = findExactMatches(nextword, w);
				if (debug) Utils.dumpVector(nextword, w);
				if (w != null && w.size() > 0) {
					for (int i=0; i<w.size(); i++) {
						String code = (String) w.elementAt(i);
						String label = getLabel(code);
						if (!w1.contains(label + "|" + code)) {
							w1.add(label + "|" + code);
						}
					}
				}
			}
		}
		return w1;
	}

    public Vector appendLabels(Vector codes) {
        Vector w = new Vector();
        for (int i=0; i<codes.size(); i++) {
			String code = (String) codes.elementAt(i);
			w.add(getLabel(code) + " (" + code + ")");
		}
		w = new SortUtils().quickSort(w);
		return w;
	}

    public static Vector findAcronyms(String filename) {
		HashSet hset = new HashSet();
		Vector w = new Vector();
		Vector v = Utils.readFile(filename);
		int num = 0;
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (!line.startsWith("(") && line.indexOf("(") != -1) {
				Vector u = StringUtils.parseData(line, ' ');
				if (u.size() > 1) {
					int k = -1;
					for (k=0; k<u.size(); k++) {
						String word = (String) u.elementAt(k);
						int numChars = 0;
						String t0 = null;
						if (word.startsWith("(") && word.endsWith(")")) {
							numChars = word.length()-2;
							t0 = word.substring(1, word.length()-1);
							if (k-numChars > 0) {
								int lcv = k-numChars;
								boolean match = true;
								StringBuffer buf = new StringBuffer();

								int n = 0;
								for (int j=lcv; j<k; j++) {
									String token = (String) u.elementAt(j);
									char c = token.charAt(0);
									char c0 = t0.charAt(n);

									if (c != c0) {
										match = false;
										break;
									} else {

										String firstChar = "" + token.charAt(0);
										firstChar = firstChar.toUpperCase();
										token = firstChar + token.substring(1, token.length());
										buf.append(token).append(" ");
									}
									n++;
								}
								if (match) {
									String t = buf.toString();
									t = t.trim();
									String s = word.substring(1, word.length()-1);
									s = s.toUpperCase();
									if (!hset.contains(t + "|" + s)) {
										w.add(t + "|" + s);
										num++;
										hset.add(t + "|" + s);
									}
								}
							}
						}
					}
				}
			}
		}
		return new SortUtils().quickSort(w);
	}
}


