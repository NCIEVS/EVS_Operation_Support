package gov.nih.nci.evs.restapi.util;

import java.io.*;
import java.net.URI;
import java.text.*;
import java.util.*;
import java.nio.file.*;

/**
 * @author EVS Team
 * @version 1.0
 *
 * Modification history:
 *     Initial implementation kim.ong@nih.gov
 *
 */

public class OneJarUtils {

	static String SOURCE_REPOSITORY_DIR = "src_repository";

    public static void createDirectory(String directoryPath) {
		File directory = new File(directoryPath);
		if (!directory.exists()) {
			boolean created = directory.mkdirs();
			if (created) {
				System.out.println("Directory created at: " + directoryPath);
			} else {
				System.err.println("Failed to create directory at: " + directoryPath);
			}
		} else {
			System.out.println("Directory already exists at: " + directoryPath);
		}
	}

    public static Vector listFileNames(String path) {
		Vector textfiles = FileUtils.listFiles(path);
		Vector filenames = new Vector();
		for (int i=0; i<textfiles.size(); i++) {
			File file = (File) textfiles.elementAt(i);
			//String filename = file.getName();//file.getAbsolutePath();
			String filename = file.getAbsolutePath();
			filename = filename.replace("\\", File.separator);
			filenames.add(filename);
		}
		return filenames;
	}

    public static Vector listFileNames(String path, boolean nameOnly) {
		Vector textfiles = FileUtils.listFiles(path);
		Vector filenames = new Vector();
		for (int i=0; i<textfiles.size(); i++) {
			File file = (File) textfiles.elementAt(i);
			String filename = file.getName();//file.getAbsolutePath();
			filename = filename.replace("\\", File.separator);
			filenames.add(filename);
		}
		return filenames;
	}


	public static String findPackageName(String javasrc) {
		Vector v = Utils.readFile(javasrc);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			line = line.trim();
			if (line.startsWith("package ")) {
				int n = line.indexOf(" ");
				return line.substring(n+1, line.length()-1);
			}
		}
		return null;
	}

	public static void copyfiles(Vector src_files, String srcDir, String targetDir) {
		for (int i=0; i<src_files.size(); i++) {
			String srcFileName = (String) src_files.elementAt(i);
			String targetFileName = srcFileName.replace(srcDir + File.separator, targetDir + File.separator);
			FileUtils.copyfile(srcFileName, targetFileName);
		}
	}

	public static void copyfiles(Vector src_files, Vector target_files) {
		for (int i=0; i<src_files.size(); i++) {
			String srcFileName = (String) src_files.elementAt(i);
			String targetFileName = (String) target_files.elementAt(i);
			FileUtils.copyfile(srcFileName, targetFileName);
		}
	}

	public static void createFolder(String packageName) {
		Vector path = Utils.parseData(packageName, '.');
        ProcessBuilder processBuilder = new ProcessBuilder();
		String folderName = "";
		for (int i=0; i<path.size(); i++) {
			folderName = folderName + (String) path.elementAt(i);
			File f = new File(folderName);
			try {
				if (!f.exists()) {
					f.mkdir();
				}
				folderName = folderName + File.separator;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}


	public static String generateOneJarMainClassName(String filename) {
		Vector v = Utils.readFile(filename);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			line = line.trim();
			if (line.startsWith("public class ")) {
				Vector u = StringUtils.parseData(line, ' ');
				String name = (String) u.elementAt(2);
				name = name.toLowerCase();
				String className = ("" + name.charAt(0)).toUpperCase() + name.substring(1, name.length()) + "Main";
				return className;
			}
		}
		return null;
	}


	public static Vector findMainMethod(String folderName, String target) {
		Vector w = new Vector();
		String cwd = System.getProperty("user.dir");
		String srcDir = cwd + File.separator + "src";
		Vector v1 = listFileNames(srcDir);
		for (int i=0; i<v1.size(); i++) {
			String filename = (String) v1.elementAt(i);
			Vector v2 = Utils.readFile(filename);
			for (int j=0; j<v2.size(); j++) {
				String line = (String) v2.elementAt(j);
				if (line.indexOf(target) != -1) {
					w.add(filename);
					break;
				}
			}
		}
		return w;
	}

	public static boolean containsTargetString(String filename, String target) {
		Vector v2 = Utils.readFile(filename);
		for (int j=0; j<v2.size(); j++) {
			String line = (String) v2.elementAt(j);
			line = line.trim();
			Vector u = StringUtils.parseData(line, ' ');
			if (u.contains(target)) {
				return true;
			}
		}
		return false;
	}

	public static boolean checkDependency(String filename1, String filename2) {
		File f = new File(filename2);
		String t = f.getName();
		int n = t.lastIndexOf(".");
		String classname2 = t.substring(0, n);
		return containsTargetString(filename1, classname2);
	}

	public static HashMap dependencyAnalysis(Vector filenames) {
		HashMap hmap = new HashMap();
		for (int i=0; i<filenames.size(); i++) {
			String filename = (String) filenames.elementAt(i);
			hmap.put(filename, new Vector());
		}
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			for (int j=0; j<filenames.size(); j++) {
				String filename = (String) filenames.elementAt(j);
				if (key.compareTo(filename) != 0) {
					boolean bool = checkDependency(key, filename);
					if (bool) {
						Vector values = (Vector) hmap.get(key);
						values.add(filename);
						hmap.put(key, values);
					}
				}
			}
		}
		return hmap;
	}

	public static Vector findMainClasses(HashMap hmap) {
		Vector keys = new Vector();
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			keys.add(key);
		}

		Vector w = new Vector();
		for (int i=0; i<keys.size(); i++) {
			String filename = (String) keys.elementAt(i);
			boolean bool = true;
			it = hmap.keySet().iterator();
			while (it.hasNext()) {
				String key = (String) it.next();
				if (key.compareTo(filename) != 0) {
					Vector values = (Vector) hmap.get(key);
					if (values.contains(filename)) {
						bool = false;
						break;
					}
				}
			}
			if (bool) {
				w.add(filename);
			}
		}
       return w;
	}

	public static String getPackageStatement(String filename) {
		Vector v = Utils.readFile(filename);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			line = line.trim();
			if (line.startsWith("package ")) {
				return line;
			}
		}
		return null;
	}

	public static Vector getMainMethodStatement(String filename) {
		Vector w = new Vector();
		boolean istart = false;
		int openBracketCount = 0;
		int closeBracketCount = 0;
		Vector v = Utils.readFile(filename);
		for (int i=0; i<v.size(); i++) {
			String line0 = (String) v.elementAt(i);
			String line = line0.trim();
			if (line.indexOf("public static void main(") != -1) {
				istart = true;
			}
			if (istart) {
				w.add(line0);
			}

			if (istart && line.indexOf("{") != -1) {
				openBracketCount++;
			}
			if (istart && line.indexOf("}") != -1) {
				closeBracketCount++;
			}
			if (istart && openBracketCount > 0 && (openBracketCount == closeBracketCount)) {
				break;
			}
		}
		return w;
	}

    public static void generateOneJarMainClass(PrintWriter out, String className, String mainMethodPackageName, Vector mainMethodStmts) {
		out.println("/*");
		out.println(" * Copyright (c) 2004-2010, P. Simon Tuffs (simon@simontuffs.com)");
		out.println(" * All rights reserved.");
		out.println(" *");
		out.println(" * See the full license at http://one-jar.sourceforge.net/one-jar-license.html");
		out.println(" * This license is also included in the distributions of this software");
		out.println(" * under doc/one-jar-license.txt");
		out.println(" */");

		out.println(mainMethodPackageName.substring(0, mainMethodPackageName.length()-1) + ".main;");

		String t = mainMethodPackageName;
		t = t.replace("package", "");
		t = t.substring(0, t.length()-1);
		out.println("import " + t + ".*;");

		out.println("");
		out.println("import java.util.Arrays;");
		out.println("");
		out.println("public class " + className + " {");
		out.println("");
		for (int i=0; i<mainMethodStmts.size(); i++) {
			String stmt = (String) mainMethodStmts.elementAt(i);
			out.println(stmt);
		}
		out.println("}");
    }

    public static String generateOneJarMainClass(String className, String mainMethodPackageName, Vector mainMethodStmts) {
		long ms = System.currentTimeMillis();
		PrintWriter pw = null;
		String outputfile = className + ".java";
		try {
			pw = new PrintWriter(outputfile, "UTF-8");
			generateOneJarMainClass(pw, className, mainMethodPackageName, mainMethodStmts);

		} catch (Exception ex) {

		} finally {
			try {
				pw.close();
				System.out.println("Output file " + outputfile + " generated.");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
		return outputfile;
	}

	public static String generateOneJarMainClass(String projectName) {
		String folderName = "src";
		String target = "public static void main";
		Vector v = findMainMethod(folderName, target);
		HashMap hmap = dependencyAnalysis(v);
		v = findMainClasses(hmap);
		if (v.size() > 1) {
			System.out.println("Multiple main classes identified -- please resolve manually before proceeding further.");
			System.exit(0);
		}

		String projectName_upper = projectName.toUpperCase();
		char firstChar = projectName_upper.charAt(0);
		String mainclassfile = firstChar + projectName.substring(1, projectName.length()) + "Main";

		File f = new File(mainclassfile);
		String className = f.getName();

		String packageStmt = getPackageStatement((String) v.elementAt(0));
	    Vector mainStmts = getMainMethodStatement((String) v.elementAt(0));

        return generateOneJarMainClass(className, packageStmt, mainStmts);
	}

	public static void moveFiles(String source_dir, String target_dir) {
		Path sourceDir = Paths.get(source_dir);
		Path targetDir = Paths.get(target_dir);

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDir)) {
			for (Path entry : stream) {
				Path targetPath = targetDir.resolve(entry.getFileName());
				Files.move(entry, targetPath, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    public static void generateBuildXML(String projectName) {
		long ms = System.currentTimeMillis();
		PrintWriter pw = null;
		String outputfile = "build.xml";
		try {
			pw = new PrintWriter(outputfile, "UTF-8");
			generateBuildXML(pw, projectName);

		} catch (Exception ex) {

		} finally {
			try {
				pw.close();
				System.out.println("Output file " + outputfile + " generated.");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}

    public static void generateBuildXML(PrintWriter out, String projectName) {
        String projectName_uc = projectName.toUpperCase();
        char c = projectName_uc.charAt(0);
        String projectName2 = "" + c + projectName.substring(1, projectName.length());

		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		out.println("<project name=\"" + projectName + "\" basedir=\".\" default=\"build\">");
		out.println("");
		out.println("    <!-- Hook into One-Jar project -->");
		out.println("    <available property=\"one-jar.dir\" file=\"../one-jar/build.onejar.xml\" value=\"../one-jar\"/>");
		out.println("    <!-- Will be ignored if available above -->");
		out.println("	<property name=\"one-jar.dir\" location=\"one-jar\" />");
		out.println("    <import file=\"${one-jar.dir}/build.onejar.xml\"/>");
		out.println("    ");
		out.println("	<property name=\"eclipse.jar-in-jar.dir\" value=\"build-lib/eclipse\"/>");
		out.println("	<property name=\"temp.dir\" value=\"temp\" />	");
		out.println("    <path id=\"libraries\">");
		out.println("    	<fileset dir=\"lib\" includes=\"*.jar\"/>");
		out.println("    </path>");
		out.println("    ");
		out.println("    <target name=\"mkdirs\">");
		out.println("        <mkdir dir=\"${test.results.dir}\" />");
		out.println("        <mkdir dir=\"${test.reports.dir}\"/>");
		out.println("        <mkdir dir=\"classes/src\"/>");
		out.println("    	<mkdir dir=\"classes/test\"/>");
		out.println("        <mkdir dir=\"classes/junit\"/>");
		out.println("    	<mkdir dir=\"build\"/>");
		out.println("    </target>");
		out.println("");
		out.println("	<target name=\"build\" depends=\"clean, mkdirs, compile, build." + projectName + "\" />");
		out.println("");
		out.println("	<target name=\"clean\">");
		out.println("		<delete dir=\"classes\" />");
		out.println("	</target>");
		out.println("	");
		out.println("	<target name=\"distclean\" depends=\"clean\" description=\"clean distribution\">");
		out.println("	    <delete dir=\"build\"/>");
		out.println("		<delete dir=\"test-reports\"/>");
		out.println("	    <delete dir=\"test-results\"/>");
		out.println("	</target>");
		out.println("");
		out.println("	<target name=\"compile\" depends=\"mkdirs\">");
		out.println("        <javac destdir=\"classes/src\" includes=\"**/*.java\" debug=\"true\">");
		out.println("            <src path=\"src\" />");
		out.println("            <classpath refid=\"libraries\" />");
		out.println("            <classpath refid=\"one-jar.libraries\"/>");
		out.println("            <classpath path=\"${one-jar.ant.jar}\"/>");
		out.println("        </javac>");
		out.println("        <javac destdir=\"classes/test\" includes=\"**/*.java\" debug=\"true\">");
		out.println("            <src path=\"test\" />");
		out.println("            <classpath refid=\"libraries\" />");
		out.println("            <classpath refid=\"one-jar.libraries\"/>");
		out.println("            <classpath path=\"${one-jar.ant.jar}\"/>");
		out.println("        </javac>");
		out.println("        <javac destdir=\"classes/junit\" includes=\"**/*.java\" debug=\"true\">");
		out.println("            <src path=\"junit\" />");
		out.println("            <classpath refid=\"libraries\" />");
		out.println("            <classpath refid=\"one-jar.libraries\"/>");
		out.println("            <!-- Uses Invoker to set up a JUnit test bridge -->");
		out.println("        	<classpath path=\"classes/test\"/>");
		out.println("        </javac>");
		out.println("	</target>");
		out.println("");
		out.println("    <!-- Build the One-Jar -->");
		out.println("	<target name=\"build." + projectName + "\">");
		out.println("		<mkdir dir=\"build\"/>");
		out.println("		<one-jar destfile=\"build/" + projectName2 + ".jar\">");
		out.println("            <manifest>");
		out.println("                <attribute name=\"Main-Class\" value=\"gov.nih.nci.evs.restapi.appl.Boot\"/>");

		out.println("                <attribute name=\"One-Jar-Main-Class\" value=\"gov.nih.nci.evs.restapi.appl.main." + projectName2 + "Main\"/>");
		out.println("            </manifest>");
		out.println("            <main>");
		out.println("                <fileset dir=\"classes/src\"/>");
		out.println("            </main>");
		out.println("            <lib>");
		out.println("                <fileset dir=\"lib\" includes=\"*.jar\" />");
		out.println("            </lib>");
		out.println("		</one-jar>");
		out.println("		<one-jar destfile=\"build/test-" + projectName + ".jar\">");
		out.println("            <manifest>");
		out.println("                <attribute name=\"Main-Class\" value=\"gov.nih.nci.evs.restapi.appl.Boot\"/>");
		out.println("            	<attribute name=\"One-Jar-Main-Class\" value=\"gov.nih.nci.evs.restapi.appl.test.Test\"/>");
		out.println("            </manifest>");
		out.println("            <zipfileset src=\"${one-jar.ant.jar}\" includes=\"**/Testable.class\"/>");
		out.println("			<main>");
		out.println("				<fileset dir=\"classes/test\"/>");
		out.println("			</main>");
		out.println("			<lib>");
		out.println("				<fileset dir=\"lib\" includes=\"*.jar\" />");
		out.println("			</lib>");
		out.println("		</one-jar>");
		out.println("		<delete dir=\"${temp.dir}\" />");
		out.println("	</target>");
		out.println("");
		out.println("    <property name=\"formatter\" value=\"xml\" />");
		out.println("");
		out.println("    <!-- Test the distribution -->");
		out.println("    <target name=\"junit.report." + projectName + "\" depends=\"build\" description=\"JUnit test of " + projectName + "\">");
		out.println("        <antcall target=\"junit." + projectName + "\" />");
		out.println("        <antcall target=\"junit.report\"/>   ");
		out.println("    </target>");
		out.println("   ");
		out.println("    <target name=\"junit." + projectName + "\" depends=\"build\">");
		out.println("        <echo>junit-" + projectName + ".jar</echo>");
		out.println("        <junit fork=\"yes\" showoutput=\"no\" printsummary=\"yes\">");
		out.println("            <jvmarg value=\"-Done-jar.verbose=true\" />");
		out.println("            <jvmarg value=\"-Done-jar.jarname=${basedir}/build/test-" + projectName + ".jar\" />");
		out.println("            <classpath>");
		out.println("                <fileset file=\"${basedir}/build/test-" + projectName + ".jar\" />");
		out.println("                <path refid=\"one-jar.libraries\"/>");
		out.println("            	<path location=\"classes/junit\" />");
		out.println("            </classpath>");
		out.println("");
		out.println("            <!-- Uses Invoker to set up a JUnit test bridge -->");


		out.println("            <test name=\"gov.nih.nci.evs.restapi.appl.test." + projectName2 + "Suite\" todir=\"${test.results.dir}\" outfile=\"" + projectName2 + "Suite\" />");
		out.println("            <formatter type=\"${formatter}\" usefile=\"yes\" />");
		out.println("        </junit>");
		out.println("    </target>");
		out.println("");
		out.println("    <target name=\"run." + projectName + "\" depends=\"build\" description=\"Run a One-Jar " + projectName + "\">");
		out.println("        <java fork=\"true\" jar=\"build/" + projectName + ".jar\"/>");
		out.println("    </target>");
		out.println("");
		out.println("	<target name=\"test." + projectName + "\" depends=\"build\" description=\"Stand-alone test a One-Jar " + projectName + "\">");
		out.println("        <java fork=\"true\" jar=\"build/test-" + projectName + ".jar\"/>");
		out.println("    </target>");
		out.println("	");
		out.println("</project>");
    }

	public static void creatDirectory(String dirName) throws Exception {
		Path dirPath = Paths.get(dirName);
		if (!Files.exists(dirPath)) {
			Files.createDirectory(dirPath);
			System.out.println("Directory created: " + dirName);
		} else {
			System.out.println("Directory already exists: " + dirName);
		}
	}

    public static void setUpOneJarProject(String projectName) {
		String oneJarMainFile = generateOneJarMainClass(projectName);
		FileUtils.copyfile(oneJarMainFile, "src" + File.separator + oneJarMainFile);
		FileUtils.deleteFile(oneJarMainFile);

		generateBuildXML(projectName);
		try {
			creatDirectory("test");
			creatDirectory("junit");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	public static void main(String[] args) {
		String projectName = args[0];
		//String oneJarMainFile = generateOneJarMainClass(projectName);
		setUpOneJarProject(projectName);
	}

}
