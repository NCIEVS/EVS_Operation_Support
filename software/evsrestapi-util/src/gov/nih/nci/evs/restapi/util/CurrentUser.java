package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;
import java.io.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.*;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.text.*;

import javax.net.ssl.*;
import java.security.cert.*;
import java.security.*;

import org.apache.commons.text.StringSubstitutor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.nio.file.StandardCopyOption;

import java.io.FilenameFilter;

public class CurrentUser {

	public CurrentUser() {

	}

	public static void moveFiles(String targetDir, String fileExt) {
	    String sourceDir = FileUtils.getCurrentWorkingDirectory();
	    moveFiles(sourceDir, targetDir, fileExt);
	}

	public static void moveFiles(String sourceDir, String targetDir, String fileExt) {
		File dir = new File(targetDir);
		if (!dir.exists()) {
			dir.mkdir();
			System.out.println("Directory " + targetDir + " created.");
		} else {
			System.out.println("Directory " + targetDir + " already exists.");
		}

		List<String> files = findFilesInDirectory(new File(sourceDir));
		Vector filesToCopy = new Vector();
		Vector w = new Vector();
		for (int i=0; i<files.size(); i++) {
			String f = (String) files.get(i);
			if (f.endsWith("." + fileExt)) {
				filesToCopy.add(new File(f).getName());
				System.out.println(new File(f).getName());
			}
		}

		System.out.println(sourceDir);
		System.out.println(targetDir);
		System.out.println(filesToCopy.size());
	    FileUtils.copyFile(sourceDir, targetDir, filesToCopy);
        int knt = 0;
		for (int i=0; i<files.size(); i++) {
			String f = (String) files.get(i);
			if (f.endsWith("." + fileExt)) {
				new File(f).delete();
				knt++;
			}
		}
		System.out.println("Number of files moved: " + knt);
	}

	public static List<String> findFilesInCurrDirectory() {
		return findFilesInCurrDirectory(true);
	}


	public static List<String> findFilesInCurrDirectory(boolean filesOnly) {
		String currDir = FileUtils.getCurrentWorkingDirectory();
		File curr_dir = new File(currDir);
		return findFilesInDirectory(curr_dir, filesOnly);
	}

	public static List<String> findFilesInDirectory(File dir) {
		return findFilesInDirectory(dir, true);
	}


	public static List<String> findFilesInDirectory(File dir, boolean filesOnly) {
		List<String> list = new ArrayList<String>();
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				if (!filesOnly) {
					List<String> list2 = findFilesInDirectory(file);
					list.addAll(list2);
				}
			} else {
				list.add(file.getAbsolutePath());
			}
		}
		return list;
	}

    public static String getFileCreationTime(String filename) {
		File file = new File(filename);
		try {
			BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
			//System.out.println("creationTime: " + attr.creationTime());
			//System.out.println("lastAccessTime: " + attr.lastAccessTime());
			//System.out.println("lastModifiedTime: " + attr.lastModifiedTime());
			return attr.creationTime().toString();
		} catch (Exception e) {

		}
		return null;
	}

	public static String getUserDownloadDir() {
		String username = System.getProperty("user.name");
		System.out.println("Current user: " + username);

		String userDownloadDir = ConfigurationController.userDownloadDir;
		System.out.println(userDownloadDir);

		Map<String, Object> textParameters = new HashMap<>();
		textParameters.put("user", username);

		StringSubstitutor substitutor = new StringSubstitutor(textParameters);
		String replacedUserDownloadDir = substitutor.replace(userDownloadDir);
		System.out.println(replacedUserDownloadDir);
		return replacedUserDownloadDir;
	}

	public static List searchDownloadFiles(String timestamp) {
		List file_list = new ArrayList();
		String replacedUserDownloadDir = getUserDownloadDir();
		List<String> list = findFilesInDirectory(new File(replacedUserDownloadDir));
		for (int i=0; i<list.size(); i++) {
			String t = (String) list.get(i);
			String s = getFileCreationTime(t);
			System.out.println(s);
			if (s.startsWith(timestamp)) {
				file_list.add(t);
			}
		}
		System.out.println(replacedUserDownloadDir + ": " + list.size());
		return file_list;
	}

	public static String getToday() {
		return getToday("MM-dd-yyyy");
	}

	public static String getToday(String format) {
		java.util.Date date = Calendar.getInstance().getTime();
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}

	public static boolean copyFile(String source, String target) {
		try {
			Files.copy(new File(source).toPath(),
				(new File(target)).toPath(),
				StandardCopyOption.REPLACE_EXISTING);
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

	public static String searchFile(List list, String prefix) {
		for (int i=0; i<list.size(); i++) {
			String t = (String) list.get(i);
			File f = new File(t);
			String name = f.getName();
			if (name.startsWith(prefix)) {
				return name;
			}
		}
		return null;
	}

	public static String searchFile(List list, String prefix, String extension) {
		for (int i=0; i<list.size(); i++) {
			String t = (String) list.get(i);
			File f = new File(t);
			String name = f.getName();
			if (name.startsWith(prefix) && name.endsWith(extension)) {
				return name;
			}
		}
		return null;
	}

    public static void unzip(String zipFilePath, String destDir) {
        File dir = new File(destDir);
        // create output directory if it doesn't exist
        if(!dir.exists()) dir.mkdirs();
        FileInputStream fis;
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
            fis = new FileInputStream(zipFilePath);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            while(ze != null){
                String fileName = ze.getName();
                File newFile = new File(destDir + File.separator + fileName);
                System.out.println("Unzipping to "+newFile.getAbsolutePath());
                //create directories for sub directories in zip
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
                }
                fos.close();
                //close this ZipEntry
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
            zis.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	public static String getMainClassAndArgs() {
		return System.getProperty("sun.java.command");
	}

	public static String getMainClassFullPathName() {
		String mainClass = getMainClassAndArgs();
		return System.getProperty("user.dir") + File.separator + mainClass + ".class";
	}

	public static long getMainClassLastModified() {
		String filename = getMainClassFullPathName();
		File f = new File(filename);
		return f.lastModified();
	}

	public static long findLatestClassLastModified() {
		List<String> filenames = CurrentUser.findFilesInCurrDirectory();
		long latest = (long) 0.0;
		for (int i=0; i<filenames.size(); i++) {
			String filename = (String) filenames.get(i);
			if (filename.endsWith(".class")) {
				long lastModified = new File(filename).lastModified();
				if (lastModified > latest) {
					latest = lastModified;
				}
			}
		}
		return latest;
	}

	public static List getFilesCreatedAfterASpecificTime(long time) {
		List<String> filenames = CurrentUser.findFilesInCurrDirectory();
		ArrayList list = new ArrayList();
		for (int i=0; i<filenames.size(); i++) {
			String filename = (String) filenames.get(i);
			long lastModified = new File(filename).lastModified();
			if (time < lastModified){
				list.add(filename);
			}
		}
		return list;
	}

	public static List getNewlyCreatedFiles() {
		return getFilesCreatedAfterASpecificTime(findLatestClassLastModified());
	}

	public static void main(String[] args) {
		String today = getToday("yyyy-MM-dd");
        List list = searchDownloadFiles(today);
        System.out.println("searchDownloadFiles: " + today);
		for (int i=0; i<list.size(); i++) {
			String t = (String) list.get(i);
			System.out.println(t);
		}
        String name = searchFile(list, "UNII_Data");
        System.out.println("searchFile: " + name);
        String dir = getUserDownloadDir();
        String source = dir + File.separator + name;
        String currentDir = System.getProperty("user.dir");
        String target = currentDir + File.separator + name;
        System.out.println(source);
        System.out.println(target);
        boolean bool = copyFile(source, target);
        if (bool) {
			System.out.println("File " + target + " created.");
			System.out.println("Unzip " + target);
			unzip(target, currentDir);
			System.out.println("Unzip done.");
		}
	}
}