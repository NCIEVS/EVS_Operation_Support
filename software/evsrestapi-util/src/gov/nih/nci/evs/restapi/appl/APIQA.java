package gov.nih.nci.evs.restapi.appl;
import gov.nih.nci.evs.restapi.util.*;

import java.io.*;
import java.net.URI;
import java.text.*;
import java.util.*;
import java.nio.file.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author EVS Team
 * @version 1.0
 *
 * Modification history:
 *     Initial implementation kim.ong@nih.gov
 *
 */

public class APIQA {

    public static Vector listFileNames() {
		String currentDir = System.getProperty("user.dir");
		System.out.println("Current dir using System:" + currentDir);
		Vector filenames = listFileNames(currentDir);
		return filenames;
	}

    public static Vector listFileNames(String path) {
		Vector textfiles = FileUtils.listFiles(path);
		Vector filenames = new Vector();
		for (int i=0; i<textfiles.size(); i++) {
			File file = (File) textfiles.elementAt(i);
			String filename = file.getAbsolutePath();
			filename = filename.replace("\\", File.separator);
			filenames.add(filename);
		}
		return filenames;
	}


    public static Vector explore(final Object object) {
		String className = object.getClass().getSimpleName();
		final var objectClass = object.getClass();
        Field[] fields = objectClass.getDeclaredFields();
        Method[] methods = objectClass.getDeclaredMethods();
        Constructor<?>[] constructors = objectClass.getConstructors();

        Vector w = new Vector();
        for (final var f : fields) {
			w.add(className + "|Field|" + f.getName());
		}
		Vector methodNames = new Vector();
        for (final var f : methods) {
			methodNames.add(f.getName());
		}
		methodNames = new SortUtils().quickSort(methodNames);
		for (int i=0; i<methodNames.size(); i++) {
			String methodName = (String) methodNames.elementAt(i);
			w.add(className + "|Method|" + methodName);
		}
		return w;
    }

    public static Vector getForNames(Vector v) {
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			int n = line.lastIndexOf("src");
			line = line.substring(n+4, line.length());
			line = line.replace(".java", "");
			line = line.replace("\\", ".");
			w.add(line);
		}
		return w;
	}

    public static Vector explore(String forname) { //"gov.nih.nci.evs.restapi.util.OWLSPARQLUtils"
  		Vector w = new Vector();
		try {
			Class<?> clazz = Class.forName(forname);
			Object c = clazz.newInstance();
			w = explore(c);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return w;
	}

	public static void main(String[] args) {
		String dirName = args[0];
		System.out.println(dirName);
        Vector v = listFileNames(dirName);
        v = getForNames(v);
        Vector w = new Vector();

		int lcv = 1;
		int increment = 10;
		int total = v.size();

        for (int i=0; i<v.size(); i++) {
			String t = (String) v.elementAt(i);
			System.out.println(t);
			int j = i+1;
			if (lcv == increment) {
				System.out.println("" + j + " out of " + total + " completed.");
				lcv = 0;
			}
			lcv++;
			Vector w1 = explore(t);
			if (w1 != null) {
				w.addAll(w1);
			} else{
				System.out.println(t);
			}
		}
		System.out.println(total  + " out of " + total + " completed.");
        Utils.saveToFile("reflection.txt", w);
	}
}
