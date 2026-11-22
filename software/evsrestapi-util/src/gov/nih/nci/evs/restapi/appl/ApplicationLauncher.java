package gov.nih.nci.evs.restapi.appl;
import gov.nih.nci.evs.restapi.util.*;
import java.io.IOException;
import java.io.*;
import java.util.*;
import java.awt.Desktop;
import java.net.URI;
import java.net.URL;

public class ApplicationLauncher {
    static String TEXTPAD_exe = "C:/Program Files/TextPad 8/TextPad.exe";

    public static Vector findApps() {
        String[] programFileDirs = {
            "C:\\Program Files",
            "C:\\Program Files (x86)"
        };
        return findApps(programFileDirs);
	}

    public static Vector findApps(String[] programFileDirs) {
        Vector v = new Vector();
        for (String dirPath : programFileDirs) {
            try {
                File dir = new File(dirPath);
                Vector w = listFilesRecursively(dir, "exe");
                if (w != null && w.size() > 0) {
                	v.addAll(w);
				}
            } catch (IOException e) {
                System.err.println("Error reading directory: " + dirPath);
            }
        }
        return v;
    }

    public static Vector findApps(String dirPath, String ext) {
		try {
			File dir = new File(dirPath);
			return listFilesRecursively(dir, ext);
		} catch (IOException e) {
			System.err.println("Error reading directory: " + dirPath);
		}
		return null;
    }

    private static Vector listFilesRecursively(File directory, String ext) throws IOException {
		Vector w = new Vector();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
				String pathname = file.getAbsolutePath();
                if (pathname.endsWith(".exe")) {
                	w.add(pathname);
				}
            }
        }
        if (directory.isDirectory()) {
			files = directory.listFiles();
			if (files != null) {
				for (File subDir : directory.listFiles()) {
					Vector w1 = listFilesRecursively(subDir, ext);
					w.addAll(w1);
				}
			}
        }
        return w;
    }

	public static URI toURI(String filepath) {
		File file = new File(filepath);
		URI uri = file.toURI();
		return uri;
	}

	public static URL toURL(String filepath) {
		File file = new File(filepath);
		try {
			java.net.URL url = file.toURI().toURL();
			return url;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

    public static void openHTML(String filepath) {
		URI uri = toURI(filepath);
		openWebPage(uri);
	}

    public static void openWebPage(URI uri) {
        try {
            if (Desktop.isDesktopSupported() &&
                Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(uri);
            } else {
                System.out.println("Desktop browsing is not supported on this platform.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void openWebPage(String url) {
        try {
            URI uri = new URI(url);
            if (Desktop.isDesktopSupported() &&
                Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(uri);
            } else {
                System.out.println("Desktop browsing is not supported on this platform.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void launchNotePad() {
        try {
            String command = "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe";
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec("notepad.exe"); // This will open Notepad on Windows
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


	public static void OpenExcel(String excelFilePath) {
		// Validate file existence
		File excelFile = new File(excelFilePath);
		if (!excelFile.exists()) {
			System.err.println("Error: File not found at " + excelFilePath);
			return;
		}

		// Check if Desktop API is supported
		if (!Desktop.isDesktopSupported()) {
			System.err.println("Error: Desktop API is not supported on this system.");
			return;
		}

		try {
			Desktop desktop = Desktop.getDesktop();
			// Ensure the file type is supported
			if (desktop.isSupported(Desktop.Action.OPEN)) {
				desktop.open(excelFile); // This will launch MS Excel if installed
				System.out.println("Excel file opened successfully.");
			} else {
				System.err.println("Error: OPEN action is not supported.");
			}
		} catch (IOException e) {
			System.err.println("Error opening Excel file: " + e.getMessage());
		} catch (SecurityException e) {
			System.err.println("Permission denied: " + e.getMessage());
		}
	}

	public static void launchTextPad(String filePath) {
		try {
			// Validate TextPad executable
			File textPadExe = new File(TEXTPAD_exe);
			if (!textPadExe.exists() || !textPadExe.isFile()) {
				System.err.println("Error: TextPad executable not found at: " + TEXTPAD_exe);
				return;
			}
			// Validate file to open
			File fileToOpen = new File(filePath);
			if (!fileToOpen.exists()) {
				System.err.println("Warning: File does not exist. It will be created when saved in TextPad.");
			}

			// Build command
			ProcessBuilder pb = new ProcessBuilder(TEXTPAD_exe, fileToOpen.getAbsolutePath());
			pb.redirectErrorStream(true); // Merge error output with standard output

			// Start process
			Process process = pb.start();
			System.out.println("TextPad launched successfully for file: " + filePath);
		} catch (IOException e) {
			System.err.println("Failed to launch TextPad: " + e.getMessage());
		}
	}

	public static void launch(String filepath) {
		if (filepath.endsWith(".html")) {
			openHTML(filepath);
		} else if (filepath.endsWith(".txt")) {
			launchTextPad(TEXTPAD_exe);
		} else if (filepath.endsWith(".xls") || filepath.endsWith(".xlsx")) {
			OpenExcel(filepath);
		} else {
			openWebPage(filepath);
		}
	}

	public static void main(String[] args) {
		try {
			String url = "www.google.com";
			String localhtml = "ADaM_Terminology.html";
			String filepath = "ADaM_Terminology.xls";
			filepath = "dom_test.txt";
			launch(localhtml);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

