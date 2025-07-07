package gov.nih.nci.evs.restapi.appl;
import gov.nih.nci.evs.restapi.util.*;

import java.io.*;
import java.text.*;
import java.util.*;


public class ReportGeneratorCodeGen {
	Vector dataReq = null;

	public ReportGeneratorCodeGen(String templateFile) {
		dataReq = Utils.array2Vector(ReportGenerator.header2DataReq(templateFile));
		Utils.dumpVector("dataReq", dataReq);
	}

	public ReportGeneratorCodeGen(Vector dataReq) {
		this.dataReq = dataReq;
		Utils.dumpVector("dataReq", dataReq);
	}

	public void run(PrintWriter out, String className) {
		out.println("import gov.nih.nci.evs.restapi.util.*;");
		out.println("import gov.nih.nci.evs.restapi.config.*;");
		out.println("import gov.nih.nci.evs.restapi.bean.*;");
		out.println("import java.io.*;");
		out.println("import java.util.*;");
		out.println("");
		out.println("\npublic class " + className + " {");
        out.println("	String templateFile = null;");
        out.println("	Vector dataReq = null;");
        out.println("	ReportGenerator generator = null;");
        out.println("	String header = null;");
		out.println("\n\tpublic " + className + "() {");
		out.println("	}");
		out.println("");
		out.println("\tpublic " + className + "(String templateFile) {");
		out.println("		this.templateFile = templateFile;");
		out.println("		dataReq = Utils.array2Vector(ReportGenerator.header2DataReq(templateFile));");
		out.println("		Utils.dumpVector(\"dataReq\", dataReq);");
		out.println("		generator = new ReportGenerator(dataReq);");
		out.println("		header = ReportGenerator.generateReportHeader(templateFile);");
		out.println("	}");
        out.println("");
		out.println("\tpublic " + className + "(Vector dataReq) {");
		out.println("		this.templateFile = null;");
		out.println("		Utils.dumpVector(\"dataReq\", dataReq);");
		out.println("		generator = new ReportGenerator(dataReq);");
		out.println("		header = ReportGenerator.dataReq2Header(dataReq);");
		out.println("		System.out.println(header);");
		out.println("	}");
        out.println("");
		out.println("	public void setDataReq(Vector dataReq) {");
		out.println("		this.dataReq = dataReq;");
		out.println("		generator = new ReportGenerator(dataReq);");
		out.println("	}");
		out.println("");
		out.println("	public void setHeader(String header) {");
		out.println("		this.header = header;");
		out.println("	}");
        out.println("");
		out.println("	public void run(Vector subset_codes, Vector codes) {");
		out.println("		Vector v = new Vector();");
		out.println("		v.add(header);");
		out.println("		for (int i=0; i<codes.size(); i++) {");
		out.println("			String code = (String) codes.elementAt(i);");
		out.println("			String subsetcode = (String) subset_codes.elementAt(i);");
		out.println("			String subsetPT = generator.getPT(subsetcode, \"|\");");

        StringBuffer buf = new StringBuffer();
		for (int k=0; k<dataReq.size(); k++) {
			String s = (String) dataReq.elementAt(k);
			out.println("			//" + (String) dataReq.elementAt(k));
			String method = ReportGenerator.getMethod4RetrievingRelatedConceptData(s);
			if (method != null) {
				out.println("			String col_" + k + " = " + method + ";");
			} else {
				if (s.compareTo("NCIt Concept Code") == 0) {
					out.println("			String col_" + k + " = code;");
				} else if (s.compareTo("NCIt Code of Table") == 0) {
					out.println("			String col_" + k + " = subsetcode;");
				} else if (s.endsWith("Table PT")) {
					out.println("			String col_" + k + " = subsetPT;");
				} else {
					if (ReportGenerator.dataReqTarget2SrcHashMap.containsKey(s)) {
						out.println("			String col_" + k + " = generator.getValues(\"" + s + "\", code, \"|\");");
					} else if (ReportGenerator.dataReqSrc2TargetHashMap.containsKey(s)) {
						String s1 = (String) ReportGenerator.dataReqSrc2TargetHashMap.get(s);
						out.println("			String col_" + k + " = generator.getValues(\"" + s1 + "\", code, \"|\");");
					} else {
						s = s.replace("NCIt", "NCI");
						if (ReportGenerator.dataReqSrc2TargetHashMap.containsKey(s)) {
							String s1 = (String) ReportGenerator.dataReqSrc2TargetHashMap.get(s);
							out.println("			String col_" + k + " = generator.getValues(\"" + s1 + "\", code, \"|\");");
						} else {
							out.println("			String col_" + k + " = (String) dataReq.elementAt(" + k + ");");
						}
					}
				}
			}
			buf.append("col_" + k);
			if (k < dataReq.size()-1) {
				buf.append("+" + "\"\t\"+");
			}
		}
		String t = buf.toString();
		out.println("			v.add(" + t + ");");
		out.println("		}");
		out.println("		//(5) Produce Excel Report");
		out.println("		String outputDir = \"output\";");
		out.println("		int n = templateFile.lastIndexOf(\".\");");
		out.println("		String outputfile = templateFile.substring(0, n) + \".txt\";");
		out.println("		ReportGenerator.saveData(outputDir, outputfile, v);");
		out.println("		String excelfile = templateFile.substring(0, n) + \".xlsx\";");
		out.println("		char delim = '\t';");
		out.println("		System.out.println(\"Generating excel file \" + excelfile);");
		out.println("		generator.generateExcel(outputDir, excelfile, delim);");
		out.println("	}");
		out.println("");
		out.println("	public static void main(String[] args) {");
		out.println("		CodeGenTest test = null;");
		out.println("		String rootConceptCode = null;");
		out.println("		String templateFile = args[0];");
		out.println("		if (templateFile.endsWith(\".template\")) {");
		out.println("			TemplateLoader loader = new TemplateLoader();");
		out.println("			Template template = loader.load(templateFile);");
		out.println("			rootConceptCode = template.getRootConceptCode();");
		out.println("			test = new CodeGenTest(templateFile);");
		out.println("		} else {");
		out.println("			Vector dataReq = Utils.readFile(templateFile);");
		out.println("			rootConceptCode = args[1];");
		out.println("			System.out.println(\"rootConceptCode: \" + rootConceptCode);");
		out.println("			test = new CodeGenTest(dataReq);");
		out.println("		}");
		out.println("		System.out.println(\"rootConceptCode: \" + rootConceptCode);");
		out.println("		Vector codes = ReportGenerator.resolveValueSet(rootConceptCode);");
		out.println("		Vector subset_codes = new Vector();");
		out.println("		for (int i=0; i<codes.size(); i++) {");
		out.println("			subset_codes.add(rootConceptCode);");
		out.println("		}");
		out.println("		test.run(subset_codes, codes);");
		out.println("	}");
		out.println("}");
	}

	public static void main(String[] args) {
		PrintWriter pw = null;
        String javafile = null;
		String templateFile = args[0];
		String className = null;
		if (args.length == 1) {
			int n = templateFile.lastIndexOf(".");
			className = templateFile.substring(0, n);
			className = className.replace("_", "") + ".java";
		} else {
			javafile = args[1];
			int n = javafile.lastIndexOf(".");
			className = javafile.substring(0, n);
		}
		ReportGeneratorCodeGen generator = null;
		if (templateFile.endsWith(".template")) {
		    generator = new ReportGeneratorCodeGen(templateFile);
		} else {
			Vector dataReq = Utils.readFile(templateFile);
			generator = new ReportGeneratorCodeGen(dataReq);
		}
		try {
			pw = new PrintWriter(javafile);
			generator.run(pw, className);
		}
		catch (FileNotFoundException e)	{
			e.printStackTrace();
		}
		finally {
			if (pw != null) {
				pw.close();
			}
		}
	}
}