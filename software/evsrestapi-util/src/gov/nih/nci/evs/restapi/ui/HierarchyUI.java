package gov.nih.nci.evs.restapi.ui;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.config.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.text.*;

public class HierarchyUI {

	static String PARENT_CHILD_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile; // "parent_child.txt";
	static HierarchyHelper hh = null;
	private final static String newline = "\n";

    public static void appendToPane(JTextPane textPane, String msg) {
        StyledDocument doc = textPane.getStyledDocument();
        try {
            // Insert at the end of the document
            doc.insertString(doc.getLength(), msg, null);
        } catch (BadLocationException e) {
            System.err.println("Error appending text: " + e.getMessage());
        }
    }

    public static SimpleAttributeSet createSimpleAttributeSet(boolean bold, int size, Color color) {
        SimpleAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setBold(attr, bold);
        StyleConstants.setFontSize(attr, size);
        StyleConstants.setForeground(attr, color);
        return attr;
	}

    public static void main(String[] args) {
        // Create the main frame
        JFrame frame = new JFrame("Hierarchy Generation UI");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 200);
        frame.setLayout(new FlowLayout());
		JLabel statusLabel = new JLabel("Status:");

		JLabel status = new JLabel("");


///////////////////////////////////////////////////////////////////////////////
        JLabel label1 = new JLabel("NCIt Hierarchy Data File:");
		JTextField hierDataFileTF = new JTextField();
		hierDataFileTF.setPreferredSize(new Dimension(600, 30));
		hierDataFileTF.setText(PARENT_CHILD_FILE);

        JTextPane jt = new JTextPane();
        jt.setPreferredSize(new Dimension(25, 500));

        StyledDocument doc = jt.getStyledDocument();
		//SimpleAttributeSet attrs = new SimpleAttributeSet();
		SimpleAttributeSet attrs = createSimpleAttributeSet(false, 12, Color.BLUE);
		StyleConstants.setLineSpacing(attrs, 0);
		StyleConstants.setSpaceAbove(attrs, 0);
		StyleConstants.setSpaceBelow(attrs, 0);
		// Apply to the whole document
		doc.setParagraphAttributes(0, doc.getLength(), attrs, false);

        //SimpleAttributeSet boldAttr = createSimpleAttributeSet(true, 12, Color.BLUE);
        //jt.setCharacterAttributes(boldAttr, true);

        JButton browseBtn1 = new JButton("Browse");
        browseBtn1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Browse")) {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setDialogTitle("Select Hierarchy Data File");
					int userSelection = fileChooser.showOpenDialog(null);
					if (userSelection == JFileChooser.APPROVE_OPTION) {
						File fileToOpen = fileChooser.getSelectedFile();
						//System.out.println("Selected file: " + fileToOpen.getAbsolutePath());
						String inputfile = fileToOpen.getAbsolutePath();
						hierDataFileTF.setText(inputfile);
					}
				}
			}
        });

        JButton hierGeneBtn0 = new JButton("View");
        hierGeneBtn0.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("View")) {
					String hierdatafile = hierDataFileTF.getText();
					/*
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setDialogTitle("Select Hierarchy Output File");
					int userSelection = fileChooser.showSaveDialog(null);
					if (userSelection == JFileChooser.APPROVE_OPTION) {
						File fileToSave = fileChooser.getSelectedFile();
						System.out.println("Selected hierarchy output file: " + fileToSave.getAbsolutePath());
						hierFileTF1.setText(fileToSave.getAbsolutePath());
						statusLabel.setText("");
					}
					*/
					//HierarchyViewer.run(hierdatafile);

					JDialog treedialog = new TreeDialog(hierdatafile);
					//treedialog.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					treedialog.setSize(400, 300);
					treedialog.setVisible(true);
/*
				    JDialog dialog = new JDialog(treedialog, "Hierarchy", false);
					//dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					// Add the JFrame to the dialog's content pane
					dialog.getContentPane().add(treedialog, BorderLayout.CENTER);
					dialog.setVisible(true);
*/
				}
			}
        });


///////////////////////////////////////////////////////////////////////////////


        JLabel hierLabel1 = new JLabel("Branch Hierarchy Data File:");
		JTextField hierFileTF1 = new JTextField();
		hierFileTF1.setPreferredSize(new Dimension(600, 30));

        JButton hierBrowseBtn1 = new JButton("Browse");
        hierBrowseBtn1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Browse")) {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setDialogTitle("Select Hierarchy Data File");
					int userSelection = fileChooser.showSaveDialog(null);
					if (userSelection == JFileChooser.APPROVE_OPTION) {
						File fileToSave = fileChooser.getSelectedFile();
						hierFileTF1.setText(fileToSave.getAbsolutePath());
						status.setText("");
					}
				}
			}
        });

        JButton hierGeneBtn1 = new JButton("View");
        hierGeneBtn1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("View")) {
					String hierdatafile = hierFileTF1.getText();
					JDialog treedialog = new TreeDialog(hierdatafile);
					treedialog.setSize(400, 300);
					treedialog.setVisible(true);
				}
			}
        });

///////////////////////////////////////////////////////////////////////////////


        JLabel hierLabel2 = new JLabel("Hierarchy Report Format:");
		//JTextField hierFileTF2 = new JTextField();
		//hierFileTF2.setPreferredSize(new Dimension(600, 30));

        String[] formats = new String[]{"ASCII", "Default Mutable Tree Node", "MS Word", "HTML"};
        JComboBox<String> formatCombo = new JComboBox<>(formats);

        formatCombo.setSelectedIndex(0);
/*
        JButton hierBrowseBtn2 = new JButton("Browse");
        hierBrowseBtn1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Browse")) {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setDialogTitle("Select Word Hierarchy File");
					int userSelection = fileChooser.showSaveDialog(null);
					if (userSelection == JFileChooser.APPROVE_OPTION) {
						File fileToSave = fileChooser.getSelectedFile();
						System.out.println("Selected Word hierarchy file: " + fileToSave.getAbsolutePath());
						hierFileTF1.setText(fileToSave.getAbsolutePath());
						statusLabel.setText("");
					}
				}
			}
        });
*/

        JButton hierGeneBtn2 = new JButton("Generate");
        hierGeneBtn2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				status.setText("");
				if (s.equals("Generate")) {
					String hierdatafile = hierFileTF1.getText();
					String format = (String) formatCombo.getSelectedItem();
					//String[] formats = new String[]{"ASCII", "Default Mutable Tree Node", "MS Word", "Dynamic"};
					int n = hierdatafile.lastIndexOf(".");
					String root = hierdatafile.substring(0, n);
					if (format.compareTo("MS Word") == 0) {
						Vector w = Utils.readFile(hierdatafile);
						HierarchyHelper hh = new HierarchyHelper(w);
						hh.printTree(root);
						ASCIITree2Word.run(root + "_tree.txt");
						status.setText(root + "_tree.xdoc generated.");
					} else if (format.compareTo("Default Mutable Tree Node") == 0) {
						String title = root + " Hierarchy";
						Vector w = Utils.readFile(hierdatafile);
						HTMLHierarchy.run(w, title, root);
                        status.setText(root + "_Hierarchy.html generated.");
					} else if (format.compareTo("ASCII") == 0) {
						Vector w = Utils.readFile(hierdatafile);
						HierarchyHelper hh = new HierarchyHelper(w);
						hh.printTree(root);
						status.setText(root + "_tree.txt generated.");
					} else if (format.compareTo("HTML") == 0) {
						int maxLevel = 35;
                        DynamicTreePlus.generateDynamicHTMLTree(root, maxLevel);
                        status.setText(root + "_htmltree.html generated.");
					}
				}
			}
        });


        JPanel panel = new JPanel();

        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;

        ////////////////Hierarchy Data File//////////////////

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(label1, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(hierDataFileTF, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(browseBtn1, gbc);

        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(hierGeneBtn0, gbc);

        /////////////////Hierarchy File//////////////////////////

		hierFileTF1.setPreferredSize(new Dimension(600, 30));

        JButton hierfileBtn1 = new JButton("Browse");

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(hierLabel1, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(hierFileTF1, gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(hierBrowseBtn1, gbc);

        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(hierGeneBtn1, gbc);

        /////////////////Hierarchy File//////////////////////////

        JButton hierfileBtn2 = new JButton("Browse");

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(hierLabel2, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(formatCombo, gbc);

        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(hierGeneBtn2, gbc);

        /////////////////Hierarchy File//////////////////////////

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(statusLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(status, gbc);


        /////////////////////////////////////////////////////////////////////
        frame.add(panel);
        // Make the frame visible
        frame.setVisible(true);
    }
}

