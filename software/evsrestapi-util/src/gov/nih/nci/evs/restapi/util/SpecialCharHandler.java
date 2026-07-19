package gov.nih.nci.evs.restapi.util;
import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.nio.file.*;
import java.nio.file.Files;

public class SpecialCharHandler {

	public static Vector search(Vector v, String target) {
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			int j = i+1;
			String line = (String) v.elementAt(i);
			if (line.indexOf(target) != -1) {
				w.add("LINE " + j + ": " + line);
			}
		}
		return w;
	}

	public static String backupFile(String filename) {
		String outputfile = "copy_of_" + filename;
		FileUtils.copyfile(filename, outputfile);
		return outputfile;
	}

	public static String copyFile(String filename) {
		String outputfile = "copy_of_" + filename;
		Vector v = Utils.readFile(filename);
		Utils.saveToFile(outputfile, v);
		return outputfile;
	}

    public static int getByteLength(String text) {
 	    try {
	    	byte[] bytesInUTF8 = text.getBytes("UTF-8");
            return bytesInUTF8.length;

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return -1;
	}

    public static void displayBytes(String text) {
 	    try {
	    	byte[] bytesInUTF8 = text.getBytes("UTF-8");
	    	for (int i=0; i<bytesInUTF8.length; i++) {
				byte b = bytesInUTF8[i];
				System.out.println("(" + i + ") " + b);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

    public static Vector substituteSpecialCharInFile(String filename, String replace, String by) {
		Vector lines = Utils.readFile(filename);
		return substituteSpecialChar(lines, replace, by);
	}

    public static Vector substituteSpecialChar(Vector lines, String replace, String by) {
	    Vector w = new Vector();
	    for (int i=0; i<lines.size(); i++) {
		    String line = (String) lines.elementAt(i);
		    w.add(substituteSpecialChar(line, replace, by));
	    }
	    return w;
    }

    public static String substituteSpecialChar(String text, String replace, String by) {
		//displayBytes(text);
		int num_text_bytes = getByteLength(text);
		int num_replace_bytes = getByteLength(replace);
		int num_by_bytes = getByteLength(by);
		int len = num_text_bytes - num_replace_bytes + num_by_bytes;
		/*
		System.out.println("num_text_bytes: " + num_text_bytes);
		System.out.println("num_replace_bytes: " + num_replace_bytes);
		System.out.println("num_by_bytes: " + num_by_bytes);
		System.out.println("len: " + len);
		*/
		byte[] byteArray = new byte[len];
        String s = null;
        try {
			byte[] bytesInUTF8_text = text.getBytes("UTF-8");
			byte[] bytesInUTF8_replace = replace.getBytes("UTF-8");
			byte[] bytesInUTF8_by = by.getBytes("UTF-8");
			/*
			String str_text = new String(bytesInUTF8_text, StandardCharsets.UTF_8);
			String str_replace = new String(bytesInUTF8_replace, StandardCharsets.UTF_8);
			String str_to = new String(bytesInUTF8_by, StandardCharsets.UTF_8);

			System.out.println("str_text: " + str_text);
			System.out.println("str_replace: " + str_replace);
			System.out.println("str_to: " + str_to);

			byte[] bytesInUTF8_text2 = str_text.getBytes("UTF-8");
			byte[] bytesInUTF8_replace2 = str_replace.getBytes("UTF-8");
			byte[] bytesInUTF8_by2 = str_to.getBytes("UTF-8");

			int num_text_bytes2 = getByteLength(str_text);
			int num_replace_bytes2 = getByteLength(str_replace);
			int num_by_bytes2 = getByteLength(str_to);
			int len2 = num_text_bytes2 - num_replace_bytes2 + num_by_bytes2;

			System.out.println("num_text_bytes2: " + num_text_bytes2);
			System.out.println("num_replace_bytes2: " + num_replace_bytes2);
			System.out.println("num_by_bytes2: " + num_by_bytes2);
			System.out.println("len2: " + len2);
			*/
			int n = text.indexOf(replace);
			if (n == -1) return text;
        	//System.out.println("n: " + n);
            int m = 0;
			for (int i=0; i<n; i++) {
				byteArray[i] = bytesInUTF8_text[i];
				System.out.println("(" + i + ") " + byteArray[i]);
				m++;
			}
			int j = 0;
			for (int i=n; i<n+num_by_bytes; i++) {
   			    byteArray[i] = bytesInUTF8_by[j];
   			    System.out.println("(" + i + ") " + byteArray[i]);
   			    j++;
   			    m++;
			}
			for (int i=m; i<len; i++) {
				byteArray[i] = bytesInUTF8_text[i+num_replace_bytes-num_by_bytes];
				System.out.println("(" + i + ") " + byteArray[i]);
			}
			s = new String(byteArray, StandardCharsets.UTF_8);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return s;
	}
}