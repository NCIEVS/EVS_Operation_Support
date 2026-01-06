package gov.nih.nci.evs.restapi.util;
import java.io.*;

public class JarExtractor {
    public static void deleteDirectory(File file)
    {
        for (File subfile : file.listFiles()) {
            if (subfile.isDirectory()) {
                deleteDirectory(subfile);
            }
            subfile.delete();
        }
    }

    public static void main(String[] args) {
		String jarFile = args[0];
		String destDir = args[1];
		File f = new File(destDir);
		if (f.exists()) {
		    deleteDirectory(f);
		}
		f.mkdir();
		try {
			java.util.jar.JarFile jar = new java.util.jar.JarFile(jarFile);
			java.util.Enumeration enumEntries = jar.entries();
			while (enumEntries.hasMoreElements()) {
				java.util.jar.JarEntry file = (java.util.jar.JarEntry) enumEntries.nextElement();
				f = new java.io.File(destDir + java.io.File.separator + file.getName());
				if (file.isDirectory()) { // if its a directory, create it
					f.mkdir();
					continue;
				}
				java.io.InputStream is = jar.getInputStream(file); // get the input stream
				java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
				while (is.available() > 0) {  // write contents of 'is' to 'fos'
					fos.write(is.read());
				}
				fos.close();
				is.close();
			}
			jar.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}