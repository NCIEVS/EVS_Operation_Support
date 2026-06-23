package gov.nih.nci.evs.restapi.util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.text.*;
import java.util.*;

import java.io.IOException;
import java.nio.file.attribute.*;
import java.nio.file.*;
import java.util.Comparator;
import java.util.stream.Stream;
import java.util.stream.*;



/**
 * <!-- LICENSE_TEXT_START -->
 * Copyright 2022 Guidehouse. This software was developed in conjunction
 * with the National Cancer Institute, and so to the extent government
 * employees are co-authors, any rights in such works shall be subject
 * to Title 17 of the United States Code, section 105.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the disclaimer of Article 3,
 *      below. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *   2. The end-user documentation included with the redistribution,
 *      if any, must include the following acknowledgment:
 *      "This product includes software developed by Guidehouse and the National
 *      Cancer Institute."   If no such end-user documentation is to be
 *      included, this acknowledgment shall appear in the software itself,
 *      wherever such third-party acknowledgments normally appear.
 *   3. The names "The National Cancer Institute", "NCI" and "Guidehouse" must
 *      not be used to endorse or promote products derived from this software.
 *   4. This license does not authorize the incorporation of this software
 *      into any third party proprietary programs. This license does not
 *      authorize the recipient to use any trademarks owned by either NCI
 *      or GUIDEHOUSE
 *   5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED
 *      WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *      OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE
 *      DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
 *      GUIDEHOUSE, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT,
 *      INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 *      BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *      LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *      CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 *      LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 *      ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *      POSSIBILITY OF SUCH DAMAGE.
 * <!-- LICENSE_TEXT_END -->
 */

/**
 * @author EVS Team
 * @version 1.0
 *
 * Modification history:
 *     Initial implementation kim.ong@nih.gov
 *
 */
public class FileUtils
{
    public static void copyfile(String src_file, String target_file) {
    	InputStream inStream = null;
	    OutputStream outStream = null;
    	try{
    	    File afile =new File(src_file);
    	    File bfile =new File(target_file);
    	    inStream = new FileInputStream(afile);
    	    outStream = new FileOutputStream(bfile);
    	    byte[] buffer = new byte[1024];
    	    int length;
    	    while ((length = inStream.read(buffer)) > 0){
    	    	outStream.write(buffer, 0, length);
    	    }
    	    inStream.close();
    	    outStream.close();
    	    System.out.println("File copied from " + src_file + " to " + target_file);
    	}catch(IOException e){
    		e.printStackTrace();
    	}
    }

    public static boolean fileExists(String filename) {
		File f = new File(filename);
		if(f.exists() && !f.isDirectory()) {
            return true;
		}
		return false;
	}

    public static boolean directoryExists(String filename) {
		File f = new File(filename);
		if(f.exists()) {
            return true;
		}
		return false;
	}

	public static String getCurrentWorkingDirectory() {
		return System.getProperty("user.dir");
	}

	public static String getToday() {
		return getToday("MM-dd-yyyy");
	}

	public static String getToday(String format) {
		java.util.Date date = Calendar.getInstance().getTime();
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}

    public static boolean createDirectory(String pathname) {
		Path path = Paths.get(pathname);
		try {
			Files.createDirectory(path);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

    public static void copyFile(String sourceDir, String targetDir, Vector filesToCopy) {
		for (int i=0; i<filesToCopy.size(); i++) {
			String filename = (String) filesToCopy.elementAt(i);
			String from = sourceDir + File.separator + filename;
			String to = targetDir + File.separator + filename;
			copyfile(from, to);
		}
	}

    public static Vector listFiles(String path) {
		Vector w = new Vector();
        File root = new File( path );
        File[] list = root.listFiles();
        if (list == null) return w;
        for (File f : list) {
            if (f.isDirectory()) {
                w.addAll(listFiles( f.getAbsolutePath()));
            } else {
                w.add(f.getAbsoluteFile());
            }
        }
        return w;
    }

    public static Vector listFileNames(String path) {
		Vector textfiles = listFiles(path);
		Vector filenames = new Vector();
		for (int i=0; i<textfiles.size(); i++) {
			File file = (File) textfiles.elementAt(i);
			String filename = file.getAbsolutePath();
			filename = filename.replace("\\", "/");
			filenames.add(filename);
		}
		return filenames;
	}


	public static void copyDirectory(String sourceDirectoryLocation, String destinationDirectoryLocation)
		throws IOException {
		File target_dir = new File(destinationDirectoryLocation);

		Files.walk(Paths.get(sourceDirectoryLocation)).forEach(source -> {
		Path destination = Paths.get(destinationDirectoryLocation, source.toString().substring(sourceDirectoryLocation.length()));
		try {
			Files.copy(source, destination);
		} catch (IOException e) {
			e.printStackTrace();
		}
		});
	}

	public static boolean fileDiff(String filename1, String filename2) {
		System.out.println("First file: " + filename1);
		System.out.println("Second file: " + filename2);
		Vector v1 = Utils.readFile(filename1);
		Vector v2 = Utils.readFile(filename2);
		if (v1.size() != v2.size())  return false;
		for (int i=0; i<v1.size(); i++) {
			String line1 = (String) v1.elementAt(i);
			String line2 = (String) v2.elementAt(i);
			if (line1.compareTo(line2) != 0) {
				System.out.println(line1);
				System.out.println(line2);
				return false;
			}
		}
		return true;
	}

    public static void deleteFile(String filename) {
        Path path = Paths.get(filename);
        try {
            boolean result = Files.deleteIfExists(path);
            if (result) {
                System.out.println("File deleted successfully.");
            } else {
                System.out.println("File does not exist.");
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handles permission issues or non-empty directories
        }
    }

    public static Vector listSubdirectories() {
		String currentDir = System.getProperty("user.dir");
		File folder = new File(currentDir);
		File[] listOfFiles = folder.listFiles();
		Vector w = new Vector();
		if(listOfFiles != null) {
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
				} else if (listOfFiles[i].isDirectory()) {
					w.add(currentDir + File.separator + listOfFiles[i].getName());
				}
			}
		}
		return w;
	}

	public static void updateFile(String srcDir, String src_file, String targetSubDir) {
		Vector w = listSubdirectories();
		String file2Copy = srcDir + File.separator + src_file;
		Vector targetDirs = new Vector();
		for (int i=0; i<w.size(); i++) {
			String t = (String) w.elementAt(i);
			String targetDir = t + File.separator + targetSubDir;
			File f = new File(targetDir);
			if (f.exists()) {
				targetDirs.add(targetDir);
			}
		}
		copyFiles(src_file, srcDir, targetDirs);
	}

	public static void deleteFles(String filename) {
		Vector subdir = listSubdirectories();
		for (int i=0; i<subdir.size(); i++) {
			String sub = (String) subdir.elementAt(i);
			String targetFileName = sub + File.separator + "lib" + File.separator + filename;
			System.out.println("Delete " + targetFileName);
			FileUtils.deleteFile(targetFileName);
		}
	}

	public static void deleteDirectory(String dirName) {
		Vector subdir = listSubdirectories();
		for (int i=0; i<subdir.size(); i++) {
			String sub = (String) subdir.elementAt(i);
			String targetDir = sub + File.separator + dirName;
			System.out.println("Delete directory" + targetDir);
			deleteDir(targetDir);
		}
	}

	public static void deleteDir(String dir) { //"path/to/directory"
		Path pathToBeDeleted = Paths.get(dir);
		try (Stream<Path> walk = Files.walk(pathToBeDeleted)) {
			walk.sorted(Comparator.reverseOrder())
				.forEach(path -> {
					try {
						Files.delete(path);
					} catch (IOException e) {
						System.err.printf("Failed to delete %s: %s%n", path, e.getMessage());
					}
				});
			System.out.println("Directory " + dir + " deleted successfully!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void copyFiles(Vector src_files, String srcDir, String targetDir) {
		for (int i=0; i<src_files.size(); i++) {
			String srcFileName = (String) src_files.elementAt(i);
			String targetFileName = srcFileName.replace(srcDir + File.separator, targetDir + File.separator);
			FileUtils.copyfile(srcFileName, targetFileName);
		}
	}

	public static void copyFiles(String src_file, String srcDir, Vector targetDirs) {
		String srcFileName = srcDir + File.separator + src_file;
		for (int i=0; i<targetDirs.size(); i++) {
			String targetDir = (String) targetDirs.elementAt(i);
			String targetFileName = targetDir + File.separator + src_file;
			FileUtils.copyfile(srcFileName, targetFileName);
		}
	}

	public static void copyFiles(Vector src_files, Vector target_files) {
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


///////////////////////////////////////////////////////////////////////////////////////////
    public static List listRegularFiles(String startPathName, String ext) {
		List<String> result = null;
		Path startPath = null;
		if (startPathName == null) {
			startPath = Paths.get(System.getProperty("user.dir"));
		} else {
			startPath = Paths.get(startPathName);
		}
		try (Stream<Path> stream = Files.walk(startPath)) {
			result = stream
				.filter(Files::isRegularFile)
				.map(Path::toString)
				.filter(path -> path.endsWith("." + ext))
				.collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
    }

    public static List listRegularFiles(String startPathName) {
		List<String> result = null;
		Path startPath = null;
		if (startPathName == null) {
			startPath = Paths.get(System.getProperty("user.dir"));
		} else {
			startPath = Paths.get(startPathName);
		}
		try (Stream<Path> stream = Files.walk(startPath)) {
			result = stream
				.filter(Files::isRegularFile)
				.map(Path::toString)
				.collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
    }

    public static List listDirectories(String startPathName) {
		List<String> result = null;
		Path startPath = null;
		if (startPathName == null) {
			startPath = Paths.get(System.getProperty("user.dir"));
		} else {
			startPath = Paths.get(startPathName);
		}
		try (Stream<Path> stream = Files.walk(startPath)) {
			result = stream
				.filter(Files::isDirectory)
				.map(Path::toString)
				.collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
    }

    public static List listFilesInDirectory(String startPathName) {
		List<String> result = null;
		Path startPath = null;
		if (startPathName == null) {
			startPath = Paths.get(System.getProperty("user.dir"));
		} else {
			startPath = Paths.get(startPathName);
		}
		try (Stream<Path> stream = Files.walk(startPath)) {
			result = stream
				.map(Path::toString)
				.collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
    }

    public static List listRegularFiles() {
		List<String> result = null;
        Path startPath = Paths.get(System.getProperty("user.dir"));
		try (Stream<Path> stream = Files.walk(startPath)) {
			result = stream
				.filter(Files::isRegularFile)
				.map(Path::toString)
				.collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
    }

    public static List listDirectories() {
		List<String> result = null;
        Path startPath = Paths.get(System.getProperty("user.dir"));
		try (Stream<Path> stream = Files.walk(startPath)) {
			result = stream
				.filter(Files::isDirectory)
				.map(Path::toString)
				.collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
    }

    public static List listFilesInDirectory() {
		List<String> result = null;
        Path startPath = Paths.get(System.getProperty("user.dir"));
		try (Stream<Path> stream = Files.walk(startPath)) {
			result = stream
				.map(Path::toString)
				.collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
    }

    public static String readRegularFiles(String startPathName, String ext) {
		List<String> result = listRegularFiles(startPathName, ext);
		File f = new File(startPathName);
		String srcfile = "src_" + f.getName() + ".txt";
		Vector importStmts = new Vector();
		Vector w0 = new Vector();
		int k = 0;
		for (int i=0; i<result.size(); i++) {
			k = i+1;
			String filename = result.get(i);
			w0.add("\n(" + k + ") " + filename);
			Vector w = Utils.readFile(filename);
			boolean istart = false;
			for (int j=0; j<w.size(); j++) {
				String line = (String) w.elementAt(j);
				line = line.trim();
				if (line.length() > 0) {
					if (!istart && line.startsWith("import ")) {
						istart = true;
					}
					if (istart && !line.startsWith("import ")) {
						istart = false;
						break;
					}
					if (istart && line.startsWith("import ")) {
						if (!importStmts.contains(line)) {
							importStmts.add(line);
						}
					}
				}
			}
			w0.addAll(w);
		}
		importStmts = new SortUtils().quickSort(importStmts);
		Utils.saveToFile("import_stmts_" + srcfile, importStmts);
		Utils.saveToFile(srcfile, w0);
		return srcfile;
    }

	public static Vector getFileAttributes(File dir) {
		Vector w = new Vector();
		//File dir = new File(dirName);
		for (File child : dir.listFiles()) {
			if (!child.isDirectory()) {
				String t = getFileAttributes(child.getPath());
				w.add(child.getPath() + "|" + t);
			}
		}
		return w;
	}

	public static String getFileAttributes(String filename) {
		Path path = Paths.get(filename);
		try {
			BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
			return attr.creationTime() + "|" + attr.lastAccessTime() + "|" + attr.lastModifiedTime();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static String getFileCreationTime(String filename) {
		Path path = Paths.get(filename);
		try {
			BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
			return attr.creationTime().toString();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static String getFileLastAccessTime(String filename) {
		Path path = Paths.get(filename);
		try {
			BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
			return attr.lastAccessTime().toString();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static String getFileLastModifiedTime(String filename) {
		Path path = Paths.get(filename);
		try {
			BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
			return attr.lastModifiedTime().toString();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}


    public static long toActualTimestamp(String timestampString) {
		Vector u1 = StringUtils.parseData(timestampString, 'T');
		Vector u2 = StringUtils.parseData((String) u1.elementAt(1), '.');
		String s = (String) u1.elementAt(0) + " " + (String) u2.elementAt(0);
		System.out.println(s);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date date = sdf.parse(s);
			String specifiedDateString = sdf.format(date);
			long actualTimestamp = sdf.parse(specifiedDateString).getTime();
			return actualTimestamp;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return -1;
	}
}
