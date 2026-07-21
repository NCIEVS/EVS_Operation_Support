package gov.nih.nci.evs.restapi.util;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.nio.file.*;
import java.nio.file.Files;
import java.nio.charset.Charset;

public class ASCIITable {

	public static void backupFile(String filename) {
		SpecialCharHandler.backupFile(filename);
	}

    public static byte[] text2BytesInUTF8(String text) {
 	    try {
	    	return text.getBytes("UTF-8");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

    public static void displayBytes(byte[] bytesInUTF8) {
		for (int i=0; i<bytesInUTF8.length; i++) {
			byte b = bytesInUTF8[i];
			System.out.println("(" + i + ") " + b);
		}
	}

	public static boolean isASCII(char c) {
		if (c >= 0 && c <= 127) return true;
		return false;
	}

	public static Vector hset2Vector(HashSet hset) {
		Vector w = new Vector();
		Iterator it = hset.iterator();
		while (it.hasNext()) {
			String t = (String) it.next();
			w.add(t);
		}
		w = new SortUtils().quickSort(w);
		return w;
	}

	public static Vector extractNonASCIIChars(Vector lines) {
		HashSet hset = new HashSet();
		for (int i=0; i<lines.size(); i++) {
			String line = (String) lines.elementAt(i);
			Vector v = extractNonASCIIChars(line);
			for (int j=0; j<v.size(); j++) {
				String s = (String) v.elementAt(j);
				if (!hset.contains(s)) {
					hset.add(s);
				}
			}
		}
		Vector w = hset2Vector(hset);
		return w;
    }

    public static String encode(String str, String standard) {
        try {
            byte[] bytes = str.getBytes(standard);
            return bytes.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    //"UTF-8"
    //"ISO-8859-1"

    public static String decodeISO88597(String text) {
        // Example byte array representing "Ελλάδα" in ISO 8859-7
        byte[] bytes = text2BytesInUTF8(text);
        Charset iso88597 = Charset.forName("ISO-8859-1");
        String s = new String(bytes, iso88597);
        return s;
    }


	public static Vector extractNonASCIIChars(String text) {
		Vector w = new Vector();
		byte[] byteArray = text2BytesInUTF8(text);
		byte[] a = new byte[text.length()];
		int k = 0;
		for (int i=0; i<byteArray.length; i++) {
			byte b = byteArray[i];
			if (!(b >= 0 && b<=127)) {
				a[k] = b;
				k++;
			} else {
				if (k > 0) {
					String str = new String(a, StandardCharsets.UTF_8);
					str = str.trim();
					w.add(str);
					a = new byte[text.length()];
					k = 0;
				}
			}
		}
		if (k > 0) {
			String str = new String(a, StandardCharsets.UTF_8);
			str = str.trim();
			w.add(str);
		}
		return w;
	}

    public static void dumpASCIITable() {
        System.out.println("ASCII Table (0 - 127):");
        System.out.println("-----------------------");
        System.out.printf("%-10s %-10s %-10s%n", "Decimal", "Char", "Hex");
        for (int i = 0; i <= 127; i++) {
            char ch = (char) i;
            // For non-printable characters, show a placeholder
            String displayChar = (i >= 32 && i <= 126) ? String.valueOf(ch) : "NP"; // NP = Non-Printable
            System.out.printf("%-10d %-10s %-10s%n", i, displayChar, Integer.toHexString(i).toUpperCase());
        }
    }

    public static String fixEncoding(String wrong) {
        String fixed = new String(wrong.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        //String fixed = new String(wrong.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
        return fixed;
    }

    public static String decodeSymbol(String s) {
		byte[] bytes = text2BytesInUTF8(s);
		String t = decodeISO88597(s);
		return t;
	}

	public static Vector getLinesWithNonidentifiableChars(Vector lines) {
		Vector w = new Vector();
		for (int i=0; i<lines.size(); i++) {
			String line = (String) lines.elementAt(i);
			Vector v = extractNonASCIIChars(line);
			if (v != null && v.size() == 1) {
				w.add(line);
				w.addAll(v);
				/*
				String s = (String) v.elementAt(0);
				String t = decodeSymbol(s);
				System.out.println(s + " --> " + t);
				*/
			}
		}
		return w;
    }

 	public static Vector getLinesWithNonidentifiableChars(String filename) {
		Vector lines = Utils.readFile(filename);
		return getLinesWithNonidentifiableChars(lines);
	}

	public static String toASCII(String text) {
		byte[] bytes = text2BytesInUTF8(text);
		String asciiEncodedString = new String(bytes, StandardCharsets.US_ASCII);
		return asciiEncodedString;
	}
}