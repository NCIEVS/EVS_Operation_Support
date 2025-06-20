package gov.nih.nci.evs.restapi.util;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.text.similarity.*;

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

	String serviceUrl = null;
	String namedGraph = null;
	String username = null;
	String password = null;
	SPARQLSearchUtils searchUtils = null;

    public static HashSet STOP_WORDS = null;
    public static HashSet KEYWORDS = null;
    public static HashMap signatureMap = null;
    public static HashMap id2LabelMap = null;

    static {
		STOP_WORDS = LexicalMatching.STOP_WORDS;
		KEYWORDS = LexicalMatching.KEYWORDS;
		signatureMap = LexicalMatching.signatureMap;
		id2LabelMap = LexicalMatching.id2LabelMap;
		System.out.println("KEYWORDS: " + KEYWORDS.size());
		System.out.println("STOP_WORDS: " + STOP_WORDS.size());
	}

    public IndexUtils() {

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
				if (u != null && u.size() >0) {
					addAll(v, u);
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

	public boolean cooccurrent(String word1, String word2) {
		Vector words = new Vector();
		words.add(word1);
		words.add(word2);
		Vector w = matchBySignature(words);
		if (w != null && w.size() > 0) {
			return true;
		}
		return false;
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

	public Vector index_term(String term) {
		Vector w = new Vector();
		Vector words = term2Keywords(term);
		int n = words.size();
		int lcv = 0;
		StringBuffer buf = new StringBuffer();
		while (lcv < n) {
			String word = (String) words.elementAt(lcv);
			lcv++;
			if (isKeyword(word)) {
				buf.append(word).append(" ");
			} else {
				String t = buf.toString();
				if (t.length() > 0) {
					t = t.substring(0, t.length()-1);
					w.add(t);
				}
				buf = new StringBuffer();
			}
		}
		String t = buf.toString();
		if (t.length() > 0) {
			t = t.substring(0, t.length()-1);
			w.add(t);
		}
		Vector v = new Vector();
		for (int i=0; i<w.size(); i++) {
			t = (String) w.elementAt(i);
			Vector v1 = indexTerm(t);
			if (v1 != null && v1.size() > 0) {
				v.addAll(v1);
			}
		}
		return new SortUtils().quickSort(v);
	}

	public Vector indexTerm(String term) {
		Vector w = matchBySignature(term);
		Vector w1 = new Vector();
		if (w != null && w.size() > 0) {
			for (int i=0; i<w.size(); i++) {
				String code = (String) w.elementAt(i);
				String label = getLabel(code);
				w1.add(label + "|" + code);
			}
			return new SortUtils().quickSort(w1);
		}

		w = new Vector();
        Vector words = term2Keywords(term);
        words = removeFillers(words);
        Vector v0 = new Vector();
        if (words == null || words.size() == 0) return w;
        String vbt = "";
        Vector v = null;
        String prevword = "";
        String nextword = "";
        while (words.size() > 0) {
			nextword = (String) words.remove(0);
			boolean cooccur = true;
			if (prevword.length() > 0) {
				cooccur = cooccurrent(prevword, nextword);
			}
			if (cooccur) {
				vbt = vbt + " " + nextword;
			} else {
				if (v0 != null && v0.size() > 0) {
					w.addAll(v0);
					v0 = null;
			    }
				vbt = nextword;
			}
			v = matchBySignature(vbt);
			if (v != null && v.size() > 0) {
				v0 = v;
			}
			prevword = nextword;
		}
		if (v0 != null && v0.size() > 0) {
			w.addAll(v0);
			v0 = null;
		}

		if (w == null || w.size() == 0) {
			return w1;
		}
		for (int i=0; i<w.size(); i++) {
			String code = (String) w.elementAt(i);
			String label = getLabel(code);
			w1.add(label + "|" + code);
		}
		return new SortUtils().quickSort(w1);
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


