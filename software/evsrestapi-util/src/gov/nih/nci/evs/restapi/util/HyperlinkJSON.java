package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.bean.*;
import com.google.gson.Gson;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.URI;
import java.text.*;
import java.util.*;


public class HyperlinkJSON {
	private static String HYPER_LINK = "https://evsexplore.semantics.cancer.gov/evsexplore/concept/ncit/";

    public static boolean containsCCode(String line) {
		Vector v = StringUtils.parseData(line, '\"');
		for (int i=0; i<v.size(); i++) {
			String t = (String) v.elementAt(i);
			if (StringUtils.isNCItCode(t)) return true;
		}
		return false;
	}

    public static LogicalExpression toObject(String json) {
        Gson gson = new Gson();
        LogicalExpression le = gson.fromJson(json, LogicalExpression.class);
        if (le == null) {
			System.out.println("le is null???");
		}
        return le;
    }

    public static Vector hyperlinkJSON(Vector unflattened_json_vec) {
		Vector w = new Vector();
		for (int i=0; i<unflattened_json_vec.size(); i++) {
			String line = (String) unflattened_json_vec.elementAt(i);
			boolean bool = containsCCode(line);
			if (bool) {
				String code = extractCCode(line);
				String hyperlink = HyperlinkHelper.toHyperlink(HYPER_LINK, code);
				line = line.replace(code, hyperlink);
				w.add(line);
			} else {
				w.add(line);
			}
		}
		return w;
	}

    public static String extractCCode(String line) {
		Vector v = StringUtils.parseData(line, ':');
		String s = (String) v.elementAt(1);
		s = s.trim();
		int n = s.lastIndexOf("\"");
		return s.substring(1, n);
	}

	public static void run(PrintWriter out, String title, Vector json_lines) {
		out.println("<!doctype HTML>");
		out.println("<html>");
		out.println("  <head>");
		out.println("    <title>" + title + "</title>");
		out.println("    <meta charset=\"utf-8\" />");
    	out.println("  </head>");
		out.println("  <body>");
		out.println("<h2><center>" + title + "</center></h2>");
		out.println("  <pre>");
		for (int i=0; i<json_lines.size(); i++) {
			String line = (String) json_lines.elementAt(i);
			out.println(line);
		}
		out.println("  </pre>");
		out.println("  </body>");
		out.println("</html>");
	}


    public static void run(String title, String htmlfile, Vector json_lines) {
        long ms = System.currentTimeMillis();
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(htmlfile, "UTF-8");
            run(pw, title, json_lines);
		} catch (Exception ex) {

		} finally {
			try {
				pw.close();
				System.out.println("Output file " + htmlfile + " generated.");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
    }

    public static void generateHTML(String title, String jsonfile) {
		int n = jsonfile.lastIndexOf(".");
		String htmlfile = jsonfile.substring(0, n) + ".html";
		Vector unflattened_json_vec = Utils.readFile(jsonfile);
		Vector w = hyperlinkJSON(unflattened_json_vec);
		run(title, htmlfile, w);
	}

	public static void main2(String args[]) {
		String jsonfile = args[0];
		Vector json_lines = Utils.readFile(jsonfile);
		String title = null;
		try {
			String json = JSONParser.flatten(json_lines);
			LogicalExpression le = toObject(json);
			String code = le.getCode();
			String label = le.getLabel();
			title = label + " (" + code + ")";
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		generateHTML(title, jsonfile);
	}

    public static void runBatch(String htmlfile, Vector json_lines) {
        long ms = System.currentTimeMillis();
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(htmlfile, "UTF-8");
            run(pw, json_lines);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				pw.close();
				System.out.println("Output file " + htmlfile + " generated.");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
    }



	public static String prettify(String jsonString) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();

		// Convert JSON string to a formatted (pretty-printed) string
		String prettyJson = objectMapper
		.writerWithDefaultPrettyPrinter()
		.writeValueAsString(objectMapper.readValue(jsonString, Object.class));

		return prettyJson;
	}


	public static void run(PrintWriter out, Vector json_lines) {
		out.println("<!doctype HTML>");
		out.println("<html>");
		out.println("  <head>");
		out.println("    <title>" + "Logical Expression" + "</title>");
		out.println("    <meta charset=\"utf-8\" />");
    	out.println("  </head>");
		out.println("  <body>");
		for (int i=0; i<json_lines.size(); i++) {
			String json_line = (String) json_lines.elementAt(i);
		    Vector u = StringUtils.parseData(json_line, '|');
		    String label = (String) u.elementAt(0);
		    String code = (String) u.elementAt(1);
		    String json = (String) u.elementAt(2);
		    String title = label + " (" + code + ")";
		    int j = i+1;
		    System.out.println("(" + j + ") " + title);

			out.println("<p></p><h2><center>" + title + "</center></h2>");
			out.println("<pre>");
			String str = json;
            try {
				str = prettify(json);
				//Vector hyperlinkJSON(Vector unflattened_json_vec)
				u = StringUtils.parseData(str, '\n');
				u = hyperlinkJSON(u);
				for (int k=0; k<u.size(); k++) {
					String s = (String) u.elementAt(k);
			        out.println(s);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			out.println("</pre>");
		}
		out.println("  </body>");
		out.println("</html>");
	}

	public static void main(String args[]) {
		String jsonfile = args[0];
		Vector json_lines = Utils.readFile(jsonfile);
		String title = null;
		Vector lines = new Vector();
		try {
			for (int i=0; i<json_lines.size(); i++) {
				String json = (String) json_lines.elementAt(i);
				LogicalExpression le = toObject(json);
				String code = le.getCode();
				String label = le.getLabel();
				lines.add(label + "|" + code + "|" + json);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		int n = jsonfile.lastIndexOf(".");
		String htmlfile = jsonfile.substring(0, n) + ".html";
		runBatch(htmlfile, lines);
	}

}


