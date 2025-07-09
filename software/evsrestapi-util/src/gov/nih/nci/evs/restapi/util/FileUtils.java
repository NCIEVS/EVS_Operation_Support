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

    public static void main(String[] args) {
		String currentWorkingDir = getCurrentWorkingDirectory();
		System.out.println("getCurrentWorkingDirectory: " + currentWorkingDir);
		String today = getToday("MMddyy");
		System.out.println("getToday: " + today);
		boolean exists = directoryExists(today);
		System.out.println("directory exist? " + exists);
		String dirname = currentWorkingDir + File.separator + today + File.separator;
		if (!exists) {
			System.out.println(dirname);
    		boolean created = createDirectory(dirname);
    		System.out.println("directory created? " + created);
		}
		exists = directoryExists(dirname);
		System.out.println("directory exist? " + exists);
		Vector filesToCopy = new Vector();
		filesToCopy.add("cmd.exe");
		filesToCopy.add("run.bat");
		copyFile(currentWorkingDir, dirname, filesToCopy);
	}
}
