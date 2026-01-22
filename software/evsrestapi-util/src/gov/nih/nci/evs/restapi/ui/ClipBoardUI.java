package gov.nih.nci.evs.restapi.ui;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.config.*;

import javax.swing.*;
import java.awt.event.*;
import java.awt.AWTException;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;

import javax.swing.filechooser.FileSystemView;

import java.io.*;
import java.util.*;
import static java.awt.event.KeyEvent.*;

import java.awt.*;

public class ClipBoardUI {

    public String OUTPUT_DIR = "output";
    public String PREFIX = "F";
    JFrame frame = null;
    JTextArea textArea = null;
    JButton getButton = null;
    JButton saveButton = null;
    JButton clearButton = null;

    public ClipBoardUI() {

	}

    public ClipBoardUI(String outputDir, String prefix) {
		OUTPUT_DIR = outputDir;
		PREFIX = prefix;
	}

    public static Vector listFiles(String directory) {
		Vector w = new Vector();
		Collection<File> c = listFileTree(new File(directory));
		int k = 0;
		Iterator it = c.iterator();
		while (it.hasNext()) {
			File t = (File) it.next();
			k++;
			w.add(t.getName());
		}
		w = new SortUtils().quickSort(w);
		return w;
	}

	public static Collection<File> listFileTree(File dir) {
		Set<File> fileTree = new HashSet<File>();
		if(dir==null||dir.listFiles()==null){
			return fileTree;
		}
		for (File entry : dir.listFiles()) {
			if (entry.isFile()) fileTree.add(entry);
			else fileTree.addAll(listFileTree(entry));
		}
		return fileTree;
	}

    public static boolean checkIfFileExists(String filename) {
		String currentDir = System.getProperty("user.dir");
		File f = new File(currentDir + "\\" + filename);
		if(f.exists() && !f.isDirectory()) {
			return true;
		} else {
			return false;
		}
	}

	public String getNextFullPath() {
		System.out.println("getNextFullPath ...");
		try {
			Vector v = listFiles(OUTPUT_DIR);
			int n = v.size();
			n++;
			return new File(OUTPUT_DIR + File.separator + PREFIX + "_" + n + ".txt").getPath();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

    public void initialize() {
		try {
			String currentPath = new java.io.File(".").getCanonicalPath();
			System.out.println("Current dir:" + currentPath);
			File f = new File(currentPath + File.separator + OUTPUT_DIR);
			if (f.exists() && f.isDirectory()) {
			} else {
				f.mkdirs();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

        frame = new JFrame("CipBoard App");
        JPanel p = new JPanel();

        textArea = new JTextArea(50, 300);
		textArea.setEditable(true);
		JScrollPane scrollPane = new JScrollPane(textArea);

		JLabel label = new JLabel();
        label.setText("Clipboard Content");

        getButton = new JButton("Get");
		getButton.addActionListener(new ActionListener() {
			  public void actionPerformed(ActionEvent e) {

					try {
						String content = get();
						textArea.setText(content);
					} catch (Exception ex) {
						ex.printStackTrace();
					}

			  }
		} );

        clearButton = new JButton("Clear");
		clearButton.addActionListener(new ActionListener() {
			  public void actionPerformed(ActionEvent e) {

					try {
						textArea.setText("");
					} catch (Exception ex) {
						ex.printStackTrace();
					}

			  }
		} );

        saveButton = new JButton("Save");
		saveButton.addActionListener(new ActionListener() {
			  public void actionPerformed(ActionEvent e) {

					try {
						String content = textArea.getText();
						JFileChooser fc = new JFileChooser();//FileSystemView.getFileSystemView());
						fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
						fc.setApproveButtonText("Save");
						//fc.setCurrentDirectory(new File (System.getProperty("user.home") + System.getProperty("file.separator")+ OUTPUT_DIR));

						fc.setCurrentDirectory(new File(OUTPUT_DIR));

						String pathname = getNextFullPath();
						System.out.println(pathname);
                        fc.setSelectedFile(new File(pathname));
                        int actionDialog = fc.showSaveDialog(null);
						File file = new File("" + fc.getSelectedFile());
						try {
							if (file == null) {
								System.out.println("file == null");
								return;
							}
							BufferedWriter outFile = new BufferedWriter(new FileWriter(file));
							outFile.write(content);

							outFile.close();
						} catch (IOException ex) {
							ex.printStackTrace();
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
			  }
		} );

        p.add(getButton, BorderLayout.WEST);
        p.add(saveButton, BorderLayout.CENTER);
        p.add(clearButton, BorderLayout.EAST);

        //frame.add(area, BorderLayout.NORTH);
        frame.getContentPane().add(label, BorderLayout.NORTH);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.getContentPane().add(p, BorderLayout.SOUTH);

        frame.setSize(300, 600);

        frame.setVisible(true);
    }

    public void copy(String text)
    {
        Clipboard clipboard = getSystemClipboard();
        clipboard.setContents(new StringSelection(text), null);
    }

    public void paste() throws AWTException
    {
        Robot robot = new Robot();
        //int controlKey = IS_OS_MAC ? VK_META : VK_CONTROL;
        int controlKey = VK_CONTROL;
        robot.keyPress(controlKey);
        robot.keyPress(VK_V);
        robot.keyRelease(controlKey);
        robot.keyRelease(VK_V);
    }

    public String get() throws Exception {
        Clipboard systemClipboard = getSystemClipboard();
        DataFlavor dataFlavor = DataFlavor.stringFlavor;
        if (systemClipboard.isDataFlavorAvailable(dataFlavor))
        {
            Object text = systemClipboard.getData(dataFlavor);
            return (String) text;
        }
        return null;
    }

    private Clipboard getSystemClipboard()
    {
        Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
        return defaultToolkit.getSystemClipboard();
    }

    public static void run(String outputDir, String prefix) {
        ClipBoardUI ui = new ClipBoardUI(outputDir, prefix);
        ui.initialize();
	}

    public static void main(String args[]) {
        String outputDir = ConfigurationController.nccn_output_dir;
        String prefix = "F";
        ClipBoardUI ui = new ClipBoardUI(outputDir, prefix);
        ui.initialize();
	}
}