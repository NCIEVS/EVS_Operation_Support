package gov.nih.nci.evs.restapi.util;

import java.io.*;
import java.util.*;

public class POJOGeneratorRunner {

    public POJOGeneratorRunner() {

    }

	public static Vector readFile(String filename)
	{
		Vector v = new Vector();
		try {
			BufferedReader in = new BufferedReader(
			   new InputStreamReader(
						  new FileInputStream(filename), "UTF8"));
			String str;
			while ((str = in.readLine()) != null) {
				v.add(str);
			}
            in.close();
		} catch (Exception ex) {
            ex.printStackTrace();
		}
		return v;
	}

    public static Vector<String> parseData(String line, String tab) {
		if (line == null) return null;
		if (tab == null) return null;
        Vector data_vec = new Vector();
        StringTokenizer st = new StringTokenizer(line, tab);
        while (st.hasMoreTokens()) {
            String value = st.nextToken();
            value = value.trim();
            data_vec.add(value);
        }
        return data_vec;
    }

	public static void generate(String filepath) {
		BufferedReader br = null;
		try {
		   File fileDir = new File(filepath);
		   br = new BufferedReader(
				new InputStreamReader(
					  new FileInputStream(fileDir), "UTF-8"));

           StringBuffer buf = new StringBuffer();
           String line;

		   String packagename = null;
		   String classname = null;
		   Vector variables_types = new Vector();
		   Vector var_names = new Vector();

		   packagename = br.readLine();
		   while ((line = br.readLine()) != null)
		   {
			   if (line.length() == 0) {
				   if (classname != null) {
					   new POJOGenerator().generate(packagename, classname, variables_types, var_names);
					   classname = null;
					   variables_types = new Vector();
					   var_names = new Vector();
				   }
			   } else {
				   int n = line.indexOf("|");
				   if (n == -1) {
					   classname = line.trim();
				   } else {
					   Vector u = parseData(line, "|");
					   variables_types.add((String) u.elementAt(0));
					   var_names.add((String) u.elementAt(1));
				   }
			   }
     	   }
		   new POJOGenerator().generate(packagename, classname, variables_types, var_names);

		} catch (FileNotFoundException e) {
			System.out.println("Error: '" + e.getMessage() + "'.");
		} catch (Exception e) {
			System.out.println("Error: '" + e.getMessage() + "'.");
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (Exception e) {
					//System.out.println("Error: '" + e.getMessage() + "'.");
				}
			}
		}
	}

	public void generate(Vector v) {
	   if (v == null) return;

	   POJOGenerator generator = new POJOGenerator();


	   String line = null;
	   String packagename = null;
	   String classname = null;
	   Vector variables_types = new Vector();
	   Vector var_names = new Vector();
	   int lcv = 0;
	   packagename = (String) v.elementAt(lcv);
	   lcv++;
	   while (lcv < v.size())
	   {
		   line = (String) v.elementAt(lcv);
		   if (line.length() == 0) {
			   if (classname != null) {
				   generator.generate(packagename, classname, variables_types, var_names);
				   classname = null;
				   variables_types = new Vector();
				   var_names = new Vector();
			   }
		   } else {
			   int n = line.indexOf("|");
			   if (n == -1) {
				   if (classname != null) {
					   generator.generate(packagename, classname, variables_types, var_names);
				   }
				   classname = line.trim();
				   variables_types = new Vector();
				   var_names = new Vector();

			   } else {
				   Vector u = parseData(line, "|");
				   variables_types.add((String) u.elementAt(0));
				   var_names.add((String) u.elementAt(1));
			   }
		   }
		   lcv++;
	   }

	   generator.set_import_statements(get_import_statements());
	   generator.set_additional_methods(get_additional_methods());
	   generator.generate(packagename, classname, variables_types, var_names);
	}

    public Vector get_import_statements() {
		Vector v = new Vector();
		v.add("import com.google.gson.*;");
		v.add("import com.google.gson.GsonBuilder;");
		v.add("import com.google.gson.reflect.TypeToken;");
		v.add("import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;");
		v.add("import com.thoughtworks.xstream.io.xml.DomDriver;");
		v.add("import com.thoughtworks.xstream.XStream;");
		return v;
	}

    public Vector get_additional_methods() {
		Vector v = new Vector();
		v.add("	public String toXML() {");
		v.add("		XStream xstream_xml = new XStream(new DomDriver());");
		v.add("		String xml = xstream_xml.toXML(this);");
		v.add("		xml = escapeDoubleQuotes(xml);");

		v.add("		StringBuffer buf = new StringBuffer();");

		v.add("		String XML_DECLARATION = \"<?xml version=\\\"1.0\\\" encoding=\\\"UTF-8\\\"?>\";");

		v.add("		buf.append(XML_DECLARATION).append(\"\\n\").append(xml);");
		v.add("		xml = buf.toString();");

		v.add("		return xml;");
		v.add("	}");
		v.add("");
		v.add("	public String toJson() {");
		v.add("		JsonParser parser = new JsonParser();");
		v.add("		Gson gson = new GsonBuilder().setPrettyPrinting().create();");
		v.add("        return gson.toJson(this);");
		v.add("	}");
        v.add("");
/*
		v.add("	public String toCSV() {");
		v.add("        return " + "\"" + csv_str + "\";" );
		v.add("	}");
        v.add("");
*/
        v.add("	public String escapeDoubleQuotes(String inputStr) {");
		v.add("		char doubleQ = '\"';");
		v.add("		StringBuffer buf = new StringBuffer();");
		v.add("		for (int i=0;  i<inputStr.length(); i++) {");
		v.add("			char c = inputStr.charAt(i);");
		v.add("			if (c == doubleQ) {");
		v.add("				buf.append(doubleQ).append(doubleQ);");
		v.add("			}");
		v.add("			buf.append(c);");
		v.add("		}");
		v.add("		return buf.toString();");
		v.add("	}");
		return v;
	}

	public static void main(String [] args) {
		String datafile = args[0];
		System.out.println(datafile);

		new POJOGeneratorRunner().generate(readFile(datafile));
		//generate(datafile);
	}
}