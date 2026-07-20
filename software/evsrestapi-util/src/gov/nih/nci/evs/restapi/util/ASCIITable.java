package gov.nih.nci.evs.restapi.util;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.nio.file.*;
import java.nio.file.Files;

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

    public static void encoding(String str) {
         try {
            // Encode the string to UTF-8 bytes
            byte[] utf8Bytes = str.getBytes("UTF-8");
            System.out.println("UTF-8 bytes length: " + utf8Bytes.length);

            // Encode the string to ISO-8859-1 bytes
            byte[] isoBytes = str.getBytes("ISO-8859-1");
            System.out.println("ISO-8859-1 bytes length: " + isoBytes.length);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    //"UTF-8"
    //"ISO-8859-1"

    public static String encoding(String str, String standard) {
         try {
            // Encode the string to UTF-8 bytes
            byte[] bytes = str.getBytes(standard);
            System.out.println("bytes length: " + bytes.length);
            return bytes.toString();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
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

	public static Vector readfile(String filename) {
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

    public static Vector readFile(String filename) {
		Vector w = new Vector();
        java.nio.file.Path filePath = java.nio.file.Paths.get(filename);
        if (!Files.exists(filePath)) {
            System.err.println("Error: File not found - " + filePath.toAbsolutePath());
            return null;
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
			//ISO-8859-1
            String line;
            while ((line = reader.readLine()) != null) {
                w.add(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            e.printStackTrace();

            return null;
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

    public static void main(String[] args) {
		String filename = args[0];
		System.out.println(filename);
		Vector w = readfile(filename);
		System.out.println("w: " + w.size());
		w = extractNonASCIIChars(w);
		Utils.saveToFile("ncit_nonascii_chars.txt", w);
	}

}