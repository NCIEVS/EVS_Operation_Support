package gov.nih.nci.evs.restapi.ui;
import gov.nih.nci.evs.restapi.appl.*;
import gov.nih.nci.evs.restapi.util.*;
//import gov.nih.nci.evs.restapi.config.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.text.*;
import java.nio.file.*;
import java.nio.file.Paths;

public class FileFinder {
	static String EOL = "\n";
	public Vector find(File root, String target){
		Vector files_found = new Vector();
		try{
			if(root.isDirectory()){
				files_found.addAll(traverse(root, target));
			} else {
                boolean bool = searchSingleFile(root, target);
                if (bool) {
					files_found.add(root.getAbsolutePath());
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return files_found;
	}

	private Vector traverse(File currentRoot, String target) {
		Vector files_found = new Vector();
		File[] fileList = currentRoot.listFiles();
		int len=fileList.length;
		while(len > 0){
			len--;
			if(fileList[len].isDirectory()){
				files_found.addAll(traverse(fileList[len], target));
			}
			else{
				String name=fileList[len].getName();
				boolean bool = searchSingleFile(fileList[len], target);
				if (bool) {
					files_found.add(fileList[len].getAbsolutePath());
				}
			}
		}
		return files_found;
	}

	private boolean searchSingleFile(File currentFile, String target){
		String pathname = currentFile.getAbsolutePath();
		if (pathname.endsWith("svn-base")) return false;
		try{
			BufferedReader buff = new BufferedReader(new FileReader(currentFile));
			String line=buff.readLine();
			int line_num = 0;

			int knt = 0;
			Vector lines = new Vector();
			while(line != null){
				line_num++;
                if (line.indexOf(target) != -1) {
					buff.close();
					return true;
				}
				line=buff.readLine();
			}
            buff.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}

    /**
     * Appends a line of text to the JTextPane.
     * Handles newline insertion and scrolls to the bottom.
     */
    public static void appendLine(JTextPane textPane, String line) {
        StyledDocument doc = textPane.getStyledDocument();
        try {
            // Ensure newline if there is already content
            if (doc.getLength() > 0) {
                doc.insertString(doc.getLength(), "\n", null);
            }
            doc.insertString(doc.getLength(), line, null);

            // Auto-scroll to bottom
            textPane.setCaretPosition(doc.getLength());
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }

    public void run(String[] args) {
        // Create the main frame
        JFrame frame = new JFrame("FileFinder UI");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 200);
        frame.setLayout(new FlowLayout());
        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel label4 = new JLabel("Status");
        JLabel label5 = new JLabel("");

        JLabel label1 = new JLabel("Root Directory");
        JTextField tf1 = new JTextField();
        tf1.setPreferredSize(new Dimension(600, 30));
        JButton btn1 = new JButton("Browse");
        btn1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Browse")) {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					fileChooser.setDialogTitle("Select File");
					try {
						File defaultDirectory = new File(System.getProperty("user.dir"));
						fileChooser.setCurrentDirectory(defaultDirectory);
					} catch (Exception ex) {

					}
					int userSelection = fileChooser.showOpenDialog(null);
					if (userSelection == JFileChooser.APPROVE_OPTION) {
						File fileToOpen = fileChooser.getSelectedFile();
						String inputfile = fileToOpen.getAbsolutePath();
						tf1.setText(inputfile);
					}
				}
			}
        });
        JLabel label2 = new JLabel("Enter Keywords");
        JTextPane jt = new JTextPane();
        jt.setPreferredSize(new Dimension(600, 400));
        JScrollPane scrollPane = SwingComponents.createJScrollPane(jt, 12, Color.BLUE);

        JButton btn4 = new JButton("Reset");
        btn4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Reset")) {
					label5.setText("");
					jt.setText("");
				}
			}
        });

        JButton btn2 = new JButton("Find");
        btn2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Find")) {
				     // <Add action statements here.>
				     String rootDir = tf1.getText();
				     String t = jt.getText();
				     Vector u = StringUtils.parseData(t, '\n');
				     String target = (String) u.elementAt(0);
				     Vector file_found = find(new File(rootDir), target);
				     if (file_found.size() == 0) {
						 JOptionPane.showMessageDialog(frame,
							"No file found.",
							"No file found",
							JOptionPane.INFORMATION_MESSAGE);
						 label5.setText("No file found.");
						 return;
					 } else {
						 label5.setText("" + file_found.size() + " files found.");
						 jt.setText("");
					 }

				     Utils.dumpVector(rootDir, file_found);
				     appendLine(jt, target);
				     for (int i=0; i<file_found.size(); i++) {
						 String filepath = (String) file_found.elementAt(i);
						 appendLine(jt, filepath);
					 }
				}
			}
        });

        JButton btn3 = new JButton("View");
        btn3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("View")) {
					String selected = jt.getSelectedText();
					System.out.println("selected: " + selected);
				    ApplicationLauncher.launch(selected);
				}
			}
        });

        Vector<Component> components = new Vector();
        components.add(btn2);
        components.add(btn3);
        components.add(btn4);
        JPanel btnPanel = SwingComponents.createButtonPanel(components);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label1, gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(tf1, gbc);
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btn1, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label2, gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(scrollPane, gbc);
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btnPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label4, gbc);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label5, gbc);

        frame.add(panel);
        // Make the frame visible
        frame.setVisible(true);
    }

    public static void main(String[] args) {
		new FileFinder().run(args);
	}

}
