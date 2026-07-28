package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.ui.*;
import java.io.*;
import java.util.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.nio.file.*;
import java.nio.file.Files;
import java.nio.charset.Charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import java.nio.IntBuffer;
import java.nio.FloatBuffer;
import java.lang.Character.UnicodeScript;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class CharacterSet {

    static String WIKIPEDIA_SITE = "https://en.wikipedia.org/wiki/List_of_Unicode_characters";
	static String WIKI_UNICODE_HTML = "wiki_List_of_Unicode_characters.html";
	static String UNICODE_LIST = "unicode_list.txt";

	public static void downloadUnicodePage() {
		DownloadPage.download(WIKIPEDIA_SITE, new File(WIKI_UNICODE_HTML));
	}

	public static void generateUnicodeTable() {
		Vector v = SpecialCharReadWrite.readFromFile(WIKI_UNICODE_HTML, true);
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, "</td>");
			if (u != null) {
				for (int j=0; j<u.size(); j++) {
					String line2 = (String) u.elementAt(j);
					Vector u2 = StringUtils.parseData(line2, "</span>");
				    w.addAll(u2);
				}
			}
		}

		Vector w0 = new Vector();
		w0.add("Unicode\tLabel\tSymbol");
		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			if (line.indexOf("td") != -1 && line.indexOf("title") != -1) {
				String t1 =(String) w.elementAt(i+1);
				if (t1.indexOf(">U+") != -1) {
					int n = t1.lastIndexOf(">");
					String s = t1.substring(n+1, t1.length());
					Vector u = StringUtils.parseData(s, ':');
					String unicode = (String) u.elementAt(0);
					String label = (String) u.elementAt(1);
					label = label.trim();
					String t6 =(String) w.elementAt(i+6);
					if (t6.indexOf("<span") != -1) {
						Vector v2 = ASCIITable.extractNonASCIIChars(t6);
						if (v2 != null && v2.size() == 1) {
							w0.add(unicode + "\t" + label + "\t" + (String) v2.elementAt(0));
						}
					}
				}
			}
		}
		SpecialCharReadWrite.saveToFile(UNICODE_LIST, w0);
		Text2Excel.generateExcel(UNICODE_LIST, '\t');
	}

	public void printCharBuffer(CharBuffer buffer) {
		buffer.flip(); // Prepare for reading
		while (buffer.hasRemaining()) {
			System.out.print(buffer.get());
		}
	}

	public static void CharBufferExample() {
		System.out.println("CharBufferExample ...");
		CharBuffer buffer = CharBuffer.allocate(10); // Allocate a buffer with capacity 10
		buffer.put('A'); // Add a character
		buffer.put("BCD"); // Add a string
		buffer.flip(); // Prepare for reading
		while (buffer.hasRemaining()) {
			System.out.print(buffer.get()); // Read and print characters
		}
	}

	public static void ByteBufferExample() {
		System.out.println("ByteBufferExample ...");

		// Allocate a new direct byte buffer with a capacity of 1024 bytes
		ByteBuffer directBuffer = ByteBuffer.allocateDirect(1024);

		// Wrap an existing byte array into a buffer
		byte[] byteArray = new byte[1024];
		ByteBuffer wrappedBuffer = ByteBuffer.wrap(byteArray);

		// Write data to the buffer
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		buffer.put((byte) 10);
		buffer.putInt(12345);
		buffer.putFloat(3.14f);

		// Read data from the buffer
		buffer.flip(); // Switch from writing to reading mode
		byte b = buffer.get();
		int i = buffer.getInt();
		float f = buffer.getFloat();

        System.out.println("byte b: " + b);
		System.out.println("int i: " + i);
		System.out.println("float f: " + f);

		// Create a view buffer as an int buffer
		IntBuffer intBuffer = buffer.asIntBuffer();

		// Create a view buffer as a float buffer
		FloatBuffer floatBuffer = buffer.asFloatBuffer();

	}

	public static void dumpSet(String label, Set<String> values) {
		System.out.println(label);
		Iterator it = values.iterator();
		while (it.hasNext()) {
			String t = (String) it.next();
			System.out.println(t);
		}
	}

    public static void characterSetExample() {
        System.out.println("characterSetExample ...");
		// Check if a charset is supported
		boolean isSupported = Charset.isSupported("UTF-8");
		System.out.println("If UTF-8 charset is supported? " + isSupported);

		// Get a Charset instance by name
		Charset utf8 = Charset.forName("UTF-8");

		// Get the default charset of the JVM
		Charset defaultCharset = Charset.defaultCharset();

		// Encode a string into bytes
		ByteBuffer encoded = utf8.encode("Hello, World!");

		// Decode bytes into a string
		CharBuffer decoded = utf8.decode(encoded);

		// Get aliases of a charset
		Set<String> aliases = utf8.aliases();
		dumpSet("utf8 aliases", aliases);
    }

	public static void CharBufferTest(String line) {
		byte[] bytes = line.getBytes();
		System.out.println(line);
		System.out.println("bytes length: " + bytes.length);
		CharBuffer buffer = CharBuffer.allocate(bytes.length);
		buffer.put(line); // Add a string
		buffer.flip(); // Prepare for reading
		Vector w = new Vector();
		while (buffer.hasRemaining()) {
			System.out.print(buffer.get()); // Read and print characters
			w.add(buffer.get());
		}
		Utils.saveToFile("test.txt", w);
	}

    public static boolean containsGreek(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        for (int i=0; i<text.length(); i++) {
			char c = text.charAt(i);
			if (isGreekLetter(c)) return true;
		}
        return false;
    }

	public static boolean isGreekLetter(char c) {
		return Character.UnicodeScript.of(c) == Character.UnicodeScript.GREEK;
	}

    public static Vector searchsGreekLetters(String filename) {
		Vector v = SpecialCharReadWrite.readFromFile(filename, true);
		System.out.println("v: " + v.size());
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			boolean bool = containsGreek(line);
			if (bool) {
				w.add(line + "\t" + bool);
			}
		}
		return w;
	}

	public static Vector readFile(String filename) {
		return SpecialCharReadWrite.readFromFile(filename, true);
	}

    public static String checkCodePoint(String text) {
		StringBuffer buf = new StringBuffer();
        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            UnicodeScript script = UnicodeScript.of(codePoint);
            String scriptName = script.name();
            buf.append(scriptName);
            if (i < text.length()-1) {
				buf.append("|");
			}
            i += Character.charCount(codePoint); // Move to next code point
        }
        return buf.toString();
	}

	public static void testReadWriteUnicodes(String filename) {
		String copyright = "Copyright \u00A9 2024";
		String heart = "I \u2764 Java!";
		System.out.println(copyright);
		System.out.println(heart);
		String game_symbol = " \u1f00 \u1f01 \u1f02 \u1f03 \u1f04 \u1f05 ";
		System.out.println(game_symbol);
		String s = " \u2551  \u2764";
		String s2 = " \u3280  \u3281  \u3282  \u3283  ";
		Vector w = new Vector();
		w.add(copyright);
		w.add(heart);
		w.add(game_symbol);
		w.add(s);
		w.add(s2);
		SpecialCharReadWrite.saveToFile(filename, w);
		Vector v = SpecialCharReadWrite.readFromFile(filename, true);
		SpecialCharReadWrite.saveToFile(filename, v);
	}

    public static int hex2Decimal(String hexInput) {
        int decimalResult = Integer.parseInt(hexInput, 16);
        return decimalResult;
    }

    public static String dec2HexConverter(int decimalInput) {
        String hexResult = Integer.toHexString(decimalInput);
        return hexResult;
    }

   public static void generateSpecialCharPage(PrintWriter out, int from, int to) {
		out.println("<!DOCTYPE html>");
		out.println("<html>");
		out.println("<style>");
		out.println("body {");
		out.println("  font-size: 20px;");
		out.println("}");
		out.println("</style>");
		out.println("<body>");
		out.println("");

		out.println("<center>");
		out.println("<h2>Hexadecimal to Decimal Conversion (" + from + " - "+ to + ") </h2>");
		out.println("<table style=\"width: 100%;\">");
		out.println("<tr>");
		out.println("<th style=\"width: 30%;\">Decimal</th>");
		out.println("<th style=\"width: 30%;\">Hexadecimal</th>");
		out.println("<th style=\"width: 40%;\">Symbol</th>");
		out.println("</tr>");
		for (int i=from; i<=to; i++) {
			out.println("<tr>");
			String hex = dec2HexConverter(i);
			out.println("<td style=\"text-align: center;\">" + i + "</td>");
			out.println("<td style=\"text-align: center;\">" + hex + "</td>");
			out.println("<td style=\"text-align: center;\"><span style='font-size:20px;'>&#" + i + ";</td>");
			out.println("</tr>");
		}
		out.println("</table>");
		out.println("</center>");
		out.println("");
		out.println("</body>");
		out.println("</html>");
   }

   public static void generateSpecialCharPage(String outputfile, int from, int to) {
	    PrintWriter pw = null;
		try {
			pw = new PrintWriter(outputfile, "UTF-8");
            generateSpecialCharPage(pw, from, to);
		} catch (Exception ex) {

		} finally {
			try {
				pw.close();
				System.out.println("Output file " + outputfile + " generated.");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

//The Unicode standard defines 1,114,112 valid code points (0x0000 to 0x10FFFF), excluding surrogate halves (U+D800–U+DFFF).
/*
Summary Table:
Code Point Range	Bytes in UTF-8	Binary Pattern (prefix bits)
U+0000 – U+007F	1 byte	0xxxxxxx
U+0080 – U+07FF	2 bytes	110xxxxx 10xxxxxx
U+0800 – U+FFFF	3 bytes	1110xxxx 10xxxxxx 10xxxxxx
U+10000 – U+10FFFF	4 bytes	11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
✅ Maximum UTF-8 encoding length per character: 4 bytes
✅ Maximum Unicode code point: U+10FFFF
*/

    public static String byteArray2String(byte[] bytes) {
		//byte[] bytes = {0x2B, 0x3C};
		return new String(bytes, StandardCharsets.UTF_8);
	}

    public static String byteArray2HexString(byte[] bytes) {
		HexFormat hexFormat = HexFormat.of().withPrefix("0x").withDelimiter(", ");
		String hexString = hexFormat.formatHex(bytes);
		return hexString;
	}

    public static byte[] hexString2byteArray(String hexString) {
		HexFormat hexFormat = HexFormat.of().withPrefix("0x").withDelimiter(", ");
		return hexFormat.parseHex(hexString);
	}

	public static String decode(String input) {
		Matcher m = Pattern.compile("\\\\u[0-9a-fA-F]{4}").matcher(input);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			char ch = (char) Integer.parseInt(m.group().substring(2), 16);
			m.appendReplacement(sb, Character.toString(ch));
		}
		m.appendTail(sb);
		return sb.toString();
	}

	public static void testDecodeHexString(String[] args) {
		System.out.println(decode("\\u0048\\u0065\\u006C\\u006C\\u006F World")); // Hello World
	}

	public static void appendUnicodeScriptName(String filename) {
		Vector w0 = new Vector();
		w0.add("Line Number\tNCIt Code\tValue\tSpecial Character(s)\tUnicode Script Name(s)");
		Vector v = readFile(filename);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '\t');
			String specChar = (String) u.elementAt(3);
			String scriptName = checkCodePoint(specChar);
			System.out.println(scriptName);
		}
	}

	public static String[][] generateHexTableData(int from, int to) {
		String[][] data = new String[to-from+1][3];
        for (int i=0; i<to-from+1; i++) {
			int k = from + i;
			String dec = "" + k;
			String hex = "\\u" + dec;
			String decoded = decode(hex);
			data[i][0] = dec;
			data[i][1] = hex;
			data[i][2] = decoded;
		}
		return data;
	}

	public static void generateHexTable(int from, int to) {
		String tableName = "Hex table";
		String[] columnNames = {"Dec", "Hex", "Symbol"};
		String[][] data = generateHexTableData(from, to);
		JTableMaker.run(tableName, columnNames, data);
	}

	public static void generateHexFile(int from, int to) {
		Vector w = new Vector();
		w.add("Dec\tHex\tSymbol");
        for (int i=0; i<to-from+1; i++) {
			int k = from + i;
			String dec = "" + k;
			String hex = "\\u" + dec;
			String decoded = decode(hex);
			w.add(dec + "\t" + hex + "\t" + decoded);
		}
        String filename = "hextable.txt";
		SpecialCharReadWrite.saveToFile(filename, w);
	}

	public static void main(String[] args) {
		//generateHexTable(3280, 3290);
		generateHexFile(3280, 3290);
	}

}

