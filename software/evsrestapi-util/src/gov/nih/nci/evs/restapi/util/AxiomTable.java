package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.XStream;
import gov.nih.nci.evs.restapi.bean.*;
import gov.nih.nci.evs.restapi.common.*;
import java.io.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Base64;
import org.json.*;

public class AxiomTable {

    public static String run(int mode, String root, String prop_code) {
		boolean flatFormat = false;
		return run(mode, root, prop_code, flatFormat);
	}

	public static String toCamelCase(String t) {
		char firstChar = t.charAt(0);
		String s = "" + firstChar;
		return s.toUpperCase() + t.substring(1, t.length());
	}

    public static String run(int mode, String root, String prop_code, boolean flatFormat) {
		Vector roots = new Vector();
		roots.add(root);
        Vector w = AxiomFileTrimmer.retrieveAxioms(AxiomFileTrimmer.getCodes(mode, roots), prop_code);
        Utils.saveToFile("axiomfile_" + root + ".txt", w);
        Vector v0 = new Vector();

        String line = (String) w.elementAt(0);

		Object obj = AxiomParser.line2Object(line);
        ReflectionUtils utils = new ReflectionUtils(obj);

        String classname = utils.getClassName();
        System.out.println(classname);
        Vector fieldNames = utils.getFieldNames();

        String heading = toCamelCase((String) fieldNames.elementAt(1) + "\t" +
                         toCamelCase((String) fieldNames.elementAt(0)));
        for (int i=2; i<fieldNames.size(); i++) {
			heading = heading + "\t" + toCamelCase((String) fieldNames.elementAt(i));
		}

        v0.add(heading);
        Vector v = new Vector();
		for (int i=0; i<w.size(); i++) {
			line = (String) w.elementAt(i);
			obj = AxiomParser.line2Object(line);
			ReflectionUtils ru = new ReflectionUtils(obj);
			HashMap hmap = ru.getFieldValues();
			StringBuffer buf = new StringBuffer();
			buf.append((String) hmap.get((String) fieldNames.elementAt(1)) + "\t" +
							(String)hmap.get((String) fieldNames.elementAt(0)));
			for (int j=2; j<fieldNames.size(); j++) {
				buf.append("\t" + hmap.get((String) fieldNames.elementAt(j)));
			}
			String t = buf.toString();
            t = t.replace("null", "");
            v.add(t);
		}
		v = new SortUtils().quickSort(v);
		v0.addAll(v);
		String outputfile = root + ".txt";
		Utils.saveToFile(outputfile, v0);
		return outputfile;
	}

    public static void main(String[] args) {
		String modeStr = args[0];
		int mode = Integer.parseInt(modeStr);
		String root = args[1];
		String prop_code = args[2];
        run(mode, root, prop_code);
	}

}
