package gov.nih.nci.evs.restapi.util;

import java.io.*;
import java.util.*;

public class POJOGenerator
{
	public Vector import_statements = null;
	public Vector additional_methods = null;

	public POJOGenerator() {

	}

	public  void set_import_statements(Vector import_statements) {
		this.import_statements = import_statements;
	}

	public  void set_additional_methods(Vector additional_methods) {
		this.additional_methods = additional_methods;
	}

	public  PrintWriter openPrintWriter(String outputfile)
	{
		try {
			PrintWriter pw = new PrintWriter (new java.io.BufferedWriter(new java.io.FileWriter(outputfile)));
            return pw;
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
    }

	public  void closeWriter(PrintWriter pw)
	{
		if (pw == null)
		{
			pw.println("WARNING: closeWriter is not open.");
			return;
		}
		pw.close();
	}


    private static void writeToFile(PrintWriter pw, String s) {
        pw.println(s);
    }


    private static void writeToFile(FileOutputStream fileOut, String s) {
    	byte buf[] = s.getBytes();
		for (int i=0; i < buf.length; i++) {
			try {
				fileOut.write(buf[i]);
			} catch (Exception ex) {
                ex.printStackTrace();
			}
		}
		try {
			fileOut.write('\n');
		} catch (Exception ex) {
            ex.printStackTrace();
		}

    }

    private String makeFirstLetterLower(String s) {
		String firstChar = s.substring(0, 1);
		String remainderChars = s.substring(1, s.length());
		return firstChar.toLowerCase() + remainderChars;
	}

    private String makeFirstLetterUpper(String s) {
		String firstChar = s.substring(0, 1);
		String remainderChars = s.substring(1, s.length());
		return firstChar.toUpperCase() + remainderChars;
	}


    public void generate(String classname, Vector variable_types, Vector variable_names) {
		generate(null, classname, variable_types, variable_names);
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

    //classname|variablenames|outputfile
    public void generate(String packagename, String classname, Vector variable_types, Vector variable_names) {
        String outputfile = classname + ".java";
		PrintWriter pw = openPrintWriter(outputfile);

		set_additional_methods(get_additional_methods());
		set_import_statements(get_import_statements());

		if (packagename != null) {
			pw.println("package " + packagename + ";");
			pw.println("");
		}

		pw.println("import java.io.*;");
		pw.println("import java.util.*;");
		pw.println("import java.net.*;");

/*
		Vector import_vec = get_import_statements();
		for (k=0; k<import_vec.size(); k++) {
			pw.println((String) import_vec.elemetAt(k));
		}
*/
		pw.println("");

		if (import_statements != null) {
			for (int i=0; i<import_statements.size(); i++) {
				String t = (String) import_statements.elementAt(i);
				pw.println(t);
			}
		}
		pw.println("");


		pw.println("public class " + classname + " {");

		// variable declaration
		pw.println("\n// Variable declaration");
		for (int i=0; i<variable_types.size(); i++) {
			String type = (String) variable_types.elementAt(i);
			String name = (String) variable_names.elementAt(i);
			pw.println("\tprivate " + type + " " + makeFirstLetterLower(name) + ";");
		}

		// Default constructor
		pw.println("\n// Default constructor");
		pw.println("\tpublic " + classname + "() {");
		pw.println("\t}");


		// Constructor
		pw.println("\n// Constructor");
		pw.println("\tpublic " + classname + "(");
		for (int i=0; i<variable_types.size(); i++) {
			String type = (String) variable_types.elementAt(i);
			String name = (String) variable_names.elementAt(i);
			if (i < variable_types.size() -1) {
				pw.println("\t\t" + type + " " + makeFirstLetterLower(name) + ",");
			} else {
				pw.println("\t\t" + type + " " + makeFirstLetterLower(name) + ") {\n");
			}
		}
		for (int i=0; i<variable_types.size(); i++) {
			String type = (String) variable_types.elementAt(i);
			String name = (String) variable_names.elementAt(i);
			pw.println("\t\tthis." + makeFirstLetterLower(name) + " = " + makeFirstLetterLower(name) + ";");
		}

		pw.println("\t}");


		// set methods
		pw.println("\n// Set methods");
		for (int i=0; i<variable_types.size(); i++) {
			String type = (String) variable_types.elementAt(i);
			String name = (String) variable_names.elementAt(i);
			pw.println("\tpublic void set" + makeFirstLetterUpper(name) + "(" + type + " " + makeFirstLetterLower(name) + ") { " );
			pw.println("\t\tthis." + makeFirstLetterLower(name) + " = " + makeFirstLetterLower(name) + ";");
			pw.println("\t}\n");
		}

		// get methods
		pw.println("\n// Get methods");
		for (int i=0; i<variable_types.size(); i++) {
			String type = (String) variable_types.elementAt(i);
			String name = (String) variable_names.elementAt(i);
			pw.println("\tpublic " + type + " get" + makeFirstLetterUpper(name) + "() { " );
			pw.println("\t\treturn this." + makeFirstLetterLower(name) + ";");
			pw.println("\t}\n");
		}

		if (additional_methods != null) {
			for (int i=0; i<additional_methods.size(); i++) {
				String t = (String) additional_methods.elementAt(i);
				pw.println(t);
			}
		}
		pw.println("}");
		closeWriter(pw);
		System.out.println(outputfile + " generated.");
	}

    public  Vector<String> parseData(String line) {
        String tab = "|";
        return parseData(line, tab);
    }

    public  Vector<String> parseData(String line, String tab) {
        Vector data_vec = new Vector();
        StringTokenizer st = new StringTokenizer(line, tab);
        while (st.hasMoreTokens()) {
            String value = st.nextToken();
            if (value.compareTo("null") == 0)
                value = " ";
            data_vec.add(value);
        }
        return data_vec;
    }


    public static Vector generateInput2POJO(String packageName, String className, String heading, char delim) {
		Vector v = toCamelFormat(heading, delim);
		Vector w = new Vector();
		w.add(packageName);
		w.add(className);
		for (int k=0; k<v.size(); k++) {
			String t = (String) v.elementAt(k);
			w.add("String|" + t);
		}
		return w;
	}

    public static Vector toCamelFormat(String heading, char delim) {
		String heading_lc = heading.toLowerCase();
		heading_lc = heading_lc.replace("_", " ");
		heading_lc = heading_lc.replace("-", " ");
		heading_lc = heading_lc.replace(".", " ");
		heading_lc = heading_lc.replace("  ", " ");
		Vector u = StringUtils.parseData(heading_lc, delim);
		Vector w = new Vector();
		int knt = 0;
		for (int k=0; k<u.size(); k++) {
			String t = (String) u.elementAt(k);
			t = t.replace("_", " ");
			t = t.replace("-", " ");
			t = t.replace(".", " ");
			t = t.trim();
			if (t.length() == 0) {
				knt++;
				w.add("unspecified_" + knt);
			} else if (t.length() == 1) {
                w.add(t);
			} else {
				StringBuffer buf = new StringBuffer();
				Vector u2 = StringUtils.parseData(t, ' ');
				StringBuffer b = new StringBuffer();
				for (int i=0; i<u2.size(); i++) {
					String t2 = (String) u2.elementAt(i);
					if (i == 0) {
						b.append(t2);
					} else {
						String s2 = t2.substring(0, 1);
						s2 = s2.toUpperCase();
						if (t2.length() > 0) {
							s2 = s2 + t2.substring(1, t2.length());
						}
						b.append(s2);
					}
				}
				w.add(b.toString());
		    }
		}
		return w;
	}

	public void run(String datafile) {
		System.out.println("datafile: " + datafile);
		String classname = null;
		Vector variable_types = new Vector();
		Vector variable_names = new Vector();
	    try{
			FileInputStream fstream = new FileInputStream(datafile);
			DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			strLine = br.readLine();
			if (strLine != null) classname = strLine;
			while ((strLine = br.readLine()) != null) {
			    Vector<String> v = parseData(strLine);
			    String type = (String) v.elementAt(0);
			    String name = (String) v.elementAt(1);
			    variable_types.add(type);
			    variable_names.add(name);
			}
			in.close();
			generate(classname, variable_types, variable_names);
		}catch (Exception e){
		  System.err.println("Error: " + e.getMessage());
		}
    }

    public void runPOJO(String datafile) {
		Vector v = Utils.readFile(datafile);
		String packagename = (String) v.elementAt(0);
		String classname = (String) v.elementAt(1);
		Vector variable_types = new Vector();
		Vector variable_names = new Vector();
		for (int i=2; i<v.size(); i++) {
			String t = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(t, '|');
			String type = (String) u.elementAt(0);
			String name = (String) u.elementAt(1);
			variable_types.add(type);
			variable_names.add(name);
		}
        generate(packagename, classname, variable_types, variable_names);
	}

	public void heading2Java(String packageName, String className, String heading) {
        Vector w = generateInput2POJO(packageName, className, heading, '\t');
        String datafile = className.toLowerCase() + ".txt";
        Utils.dumpVector("heading", w);
        Utils.saveToFile(datafile, w);
        new POJOGenerator().runPOJO(datafile);
	}

	public static void main(String[] args) {
		String filename = args[0];
		Vector v = Utils.readFile(filename);
		String heading = (String) v.elementAt(0);
		String packageName = "gov.nih.nci.evs.restapi.util";
		String className = "DrugData";
		new POJOGenerator().heading2Java(packageName, className, heading);
	}
 }
