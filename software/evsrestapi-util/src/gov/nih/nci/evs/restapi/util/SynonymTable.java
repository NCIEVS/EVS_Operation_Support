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

public class SynonymTable {

	public static void run(int mode, String root) {
		boolean flatFormat = false;
		run(mode, root, flatFormat);
	}

    public static void run(int mode, String root, boolean flatFormat) {
		Vector roots = new Vector();
		roots.add(root);
        Vector w = TermFileGenerator.generateTermFile(mode, roots);
        Utils.saveToFile("termfile_" + root + ".txt", w);
        Vector v0 = new Vector();
        v0.add("Label\tCode\tTerm Name\tTerm Group\tTerm Source\tSource Code\tSubsource Name\tSubsource Code");
        Vector v = new Vector();
		HashMap synMap = new HashMap();
		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			Synonym syn = AxiomParser.line2Synonym(line);
			String key = syn.getLabel() + "\t" + syn.getCode();
			Vector w1 = new Vector();
			if (synMap.containsKey(key)) {
				w1 = (Vector) synMap.get(key);
			}
			String t = syn.getTermName() + "|"
			    + syn.getTermGroup() + "|"
			    + syn.getTermSource() + "|"
                + syn.getSourceCode() + "|"
 			    + syn.getSubSourceName() + "|"
                + syn.getSubSourceCode();

            t = t.replace("null", "");
            w1.add(t);
            synMap.put(key, w1);
		}
		//sort synMap
		Vector keys = new Vector();
		Iterator it = synMap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			keys.add(key);
		}
		keys = new SortUtils().quickSort(keys);
		for (int j=0; j<keys.size(); j++) {
			String key = (String) keys.elementAt(j);
			Vector u4 = StringUtils.parseData(key, '\t');
			String code = (String) u4.elementAt(1);
			String label = (String) u4.elementAt(0);
			w = (Vector) synMap.get(key);

// flatFomrat
            if (flatFormat) {
				Vector termName_vec = new Vector();
				Vector termGroup_vec = new Vector();
				Vector termSource_vec = new Vector();
				Vector sourceCode_vec = new Vector();
				Vector subSourceName_vec = new Vector();
				Vector subSourceCode_vec = new Vector();
				for (int k=0; k<w.size(); k++) {
					String s = (String) w.elementAt(k);
					Vector u = StringUtils.parseData(s, '|');
					String termName = (String) u.elementAt(0);
					String termGroup = (String) u.elementAt(1);
					String termSource = (String) u.elementAt(2);
					String sourceCode = (String) u.elementAt(3);
					String subSourceName = (String) u.elementAt(4);
					String subSourceCode = (String) u.elementAt(5);

					if (termName == null || termName.compareTo("null") == 0) termName = "";
					if (termGroup == null || termGroup.compareTo("null") == 0) termGroup = "";
					if (termSource == null || termSource.compareTo("null") == 0) termSource = "";
					if (sourceCode == null || sourceCode.compareTo("null") == 0) sourceCode = "";
					if (subSourceName == null || subSourceName.compareTo("null") == 0) subSourceName = "";
					if (subSourceCode == null || subSourceCode.compareTo("null") == 0) subSourceCode = "";

					termName_vec.add(termName);
					termGroup_vec.add(termGroup);
					termSource_vec.add(termSource);
					sourceCode_vec.add(sourceCode);
					subSourceName_vec.add(subSourceName);
					subSourceCode_vec.add(subSourceCode);
				}
				String line1 = key + "\t" + Utils.vector2Delimited(termName_vec, "|")
								   + "\t" + Utils.vector2Delimited(termGroup_vec, "|")
								   + "\t" + Utils.vector2Delimited(termSource_vec, "|")
								   + "\t" + Utils.vector2Delimited(sourceCode_vec, "|")
								   + "\t" + Utils.vector2Delimited(subSourceName_vec, "|")
								   + "\t" + Utils.vector2Delimited(subSourceCode_vec, "|");
				v.add(line1);
			} else {
				for (int k=0; k<w.size(); k++) {
					String s = (String) w.elementAt(k);
					Vector u = StringUtils.parseData(s, '|');
					String termName = (String) u.elementAt(0);
					String termGroup = (String) u.elementAt(1);
					String termSource = (String) u.elementAt(2);
					String sourceCode = (String) u.elementAt(3);
					String subSourceName = (String) u.elementAt(4);
					String subSourceCode = (String) u.elementAt(5);

					if (termName == null || termName.compareTo("null") == 0) termName = "";
					if (termGroup == null || termGroup.compareTo("null") == 0) termGroup = "";
					if (termSource == null || termSource.compareTo("null") == 0) termSource = "";
					if (sourceCode == null || sourceCode.compareTo("null") == 0) sourceCode = "";
					if (subSourceName == null || subSourceName.compareTo("null") == 0) subSourceName = "";
					if (subSourceCode == null || subSourceCode.compareTo("null") == 0) subSourceCode = "";

					String line1 = key + "\t" + termName
									   + "\t" + termGroup
									   + "\t" + termSource
									   + "\t" + sourceCode
									   + "\t" + subSourceName
									   + "\t" + subSourceCode;
					v.add(line1);
				}
			}
		}
		v = new SortUtils().quickSort(v);
		v0.addAll(v);
		Utils.saveToFile(root + ".txt", v0);
	}

    public static void run(int mode, String root, String source) {
		Vector roots = new Vector();
		roots.add(root);
        Vector w = TermFileGenerator.generateTermFile(mode, roots);
        Utils.saveToFile("termfile_" + root + ".txt", w);

        Vector v0 = new Vector();
        v0.add("Label\tCode\tNCI PT\t" + source + " Term(s)\t" + source + " Source Code(s)");
        Vector v = new Vector();
		HashMap synMap = new HashMap();
		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			Synonym syn = AxiomParser.line2Synonym(line);
			String key = syn.getLabel() + "\t" + syn.getCode();
			Vector w1 = new Vector();
			if (synMap.containsKey(key)) {
				w1 = (Vector) synMap.get(key);
			}
			String t = syn.getTermName() + "|"
			    + syn.getTermGroup() + "|"
			    + syn.getTermSource() + "|"
                + syn.getSourceCode() + "|"
 			    + syn.getSubSourceName() + "|"
                + syn.getSubSourceCode();

            t = t.replace("null", "");
            w1.add(t);
            synMap.put(key, w1);
		}
		//sort synMap
		Vector keys = new Vector();
		Iterator it = synMap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			keys.add(key);
		}
		keys = new SortUtils().quickSort(keys);

		for (int j=0; j<keys.size(); j++) {
			String key = (String) keys.elementAt(j);
			Vector u4 = StringUtils.parseData(key, '\t');
			String code = (String) u4.elementAt(1);
			String label = (String) u4.elementAt(0);
			w = (Vector) synMap.get(key);
			Vector source_terms = new Vector();
			Vector source_codes = new Vector();
			Vector ncipts = new Vector();

			for (int k=0; k<w.size(); k++) {
				String s = (String) w.elementAt(k);
				Vector u = StringUtils.parseData(s, '|');
				String source_code = (String) u.elementAt(3);
				String src = (String) u.elementAt(2);
				String group = (String) u.elementAt(1);
				String name = (String) u.elementAt(0);

				if (src != null && src.compareTo(source) == 0) {
					source_terms.add(name);
					source_codes.add(source_code);
				} else if (source.compareTo("NCI") == 0 && group.compareTo("PT") == 0) {
					ncipts.add(name);
				}
			}
			String line1 = key + "\t" + Utils.vector2Delimited(ncipts, "|")
			                   + "\t" + Utils.vector2Delimited(source_terms, "|")
			                   + "\t" + Utils.vector2Delimited(source_codes, "|");
			v.add(line1);
		}
		v = new SortUtils().quickSort(v);
		v0.addAll(v);
		Utils.saveToFile(root + ".txt", v0);
	}

    public static void main(String[] args) {
		String modeStr = args[0];
		int mode = Integer.parseInt(modeStr);
		String root = args[1];
        //String source = args[2];
        //run(mode, root, source);
        run(mode, root);
	}

}
