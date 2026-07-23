package gov.nih.nci.evs.restapi.util;
import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

public class SpecialCharReadWrite {

    /**
     * Writes a string to a file using UTF-8 encoding.
     */
    public static boolean writeToFile(String filePath, String content) {
        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(filePath), StandardCharsets.UTF_8)) {
            writer.write(content);
            return true;
        } catch (IOException e) {
            System.err.println("Error writing file: " + e.getMessage());
            return false;
        }
    }

    public static boolean writeToFile(String filePath, Vector v) {
        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(filePath), StandardCharsets.UTF_8)) {
			for (int i=0; i<v.size(); i++) {
				String line = (String) v.elementAt(i);
            	writer.write(line + System.lineSeparator());
			}
            return true;
        } catch (IOException e) {
            System.err.println("Error writing file: " + e.getMessage());
            return false;
        }
    }

    public static boolean saveToFile(String filePath, Vector v) {
		return writeToFile(filePath, v);
	}

    /**
     * Reads a string from a file using UTF-8 encoding.
     */

    public static Vector readFromFile(String filePath, boolean utf8) {
		if (utf8) {
			return readFromFile(filePath);
		}
		return readFile(filePath);
	}

    public static Vector readFromFile(String filePath) {
		Vector w = new Vector();
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                w.add(line);
            }
            return w;
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return null;
        }
    }

	public static Vector readFile(String filename) {
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

}

