package gov.nih.nci.evs.restapi.ui;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.config.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.text.*;

public class HTMLEncoderUI {

	static String AXIOM_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.axiomfile;
	static String PARENT_CHILD_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile; // "parent_child.txt";

	static String A8_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.subsetfile;
	static HashMap a8Map = null;
	static HashMap inverse_a8Map = null;
	static HierarchyHelper hh = null;
	private final static String newline = "\n";

	public static HashMap createCode2ValuesMap(String inputfile, String req) {
		Vector v = Utils.readFile(inputfile);
		HashMap hmap = new HashMap();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
            Vector u2 = StringUtils.parseData(req, '|');
            boolean include = true;
            for (int j=0; j<u2.size(); j++) {
				String t = (String) u2.elementAt(j);
				if (!u.contains(t)) {
					include = false;
					break;
				}
			}
			if (include) {
				String code = (String) u.elementAt(0);
				Vector w = new Vector();
				if (hmap.containsKey(code)) {
					w = (Vector) hmap.get(code);
				}
				String value = (String) u.elementAt(2);
				if (!w.contains(value)) {
					w.add(value);
				}
				hmap.put(code, w);
			}
	    }
	    return hmap;
	}

    public static HashMap createTerm2CodesMap(Vector v) {
		boolean caseSensitive = false;
		HashMap hmap = new HashMap();
        for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String prop_code = (String) u.elementAt(1);
			if (prop_code.compareTo("P90") == 0) {
				String term = (String) u.elementAt(2);
				term = HTMLDecoder.decode(term);
				String term_lc = term.toLowerCase();
				if (!caseSensitive) {
					term = term_lc;
				}
				Vector w = new Vector();
				if (hmap.containsKey(term)) {
					w = (Vector) hmap.get(term_lc);
				}
				String code = (String) u.elementAt(0);
				if (!w.contains(code)) {
					w.add(code);
				}
				hmap.put(term_lc, w);
			}
		}
		return hmap;
	}

    public static HashMap createTerm2CodesMap(String filename) {
		Vector v = Utils.readFile(filename);
		return createTerm2CodesMap(v);
	}


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
        JFrame frame = new JFrame("HTML Encoder UI");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 200);
        frame.setLayout(new FlowLayout());

		JLabel label5 = new JLabel("Status:");

        JLabel label1 = new JLabel("Input File:");

		JTextField inputFileTF = new JTextField();
		inputFileTF.setPreferredSize(new Dimension(600, 30));
		inputFileTF.setText("termfile_C3367.txt");

/*
JPanel noWrapPanel = new JPanel( new BorderLayout() );
noWrapPanel.add( textPane );
*/

/*
        JTextArea jt = new JTextArea();
        jt.setPreferredSize(new Dimension(25, 500));
*/

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
					fileChooser.setDialogTitle("Select Input File");
					int userSelection = fileChooser.showOpenDialog(null);
					if (userSelection == JFileChooser.APPROVE_OPTION) {
						File fileToOpen = fileChooser.getSelectedFile();
						System.out.println("Selected file: " + fileToOpen.getAbsolutePath());
						String inputfile = fileToOpen.getAbsolutePath();
						inputFileTF.setText(inputfile);
					}
				}
			}
        });

        JLabel label2 = new JLabel("Output File:");
		JTextField outputFileTF = new JTextField();
		outputFileTF.setPreferredSize(new Dimension(600, 30));
		outputFileTF.setText("v2_" + inputFileTF.getText());

        JButton browseBtn2 = new JButton("Browse");
        browseBtn2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Browse")) {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setDialogTitle("Select Output File");
					int userSelection = fileChooser.showSaveDialog(null);
					if (userSelection == JFileChooser.APPROVE_OPTION) {
						File fileToSave = fileChooser.getSelectedFile();
						System.out.println("Selected file: " + fileToSave.getAbsolutePath());
						outputFileTF.setText(fileToSave.getAbsolutePath());
						label5.setText("");
					}
				}
			}
        });

        JButton saveBtn = new JButton("Save");
        saveBtn.setEnabled(false);
        saveBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Save")) {
					String inputfile = inputFileTF.getText();
					String outputfile = outputFileTF.getText();
					System.out.println("inputfile: " + inputfile);
					System.out.println("outputfile: " + outputfile);
					if (outputfile.compareTo("") == 0) {
						label5.setText("Please specify an output file.");
					} else {
						label5.setText("");
						String content = jt.getText();
						Vector w = new Vector();
						w.add(content);
						Utils.saveToFile(outputfile, w);
						label5.setText(outputfile + " generated.");
						saveBtn.setEnabled(false);
						jt.setPreferredSize(new Dimension(25, 500));
						frame.invalidate();
						frame.validate();
						frame.repaint();
					}
				}
			}
        });

        JButton encodeBtn = new JButton("Encode");
        encodeBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Encode")) {
					label5.setText("");
					String inputfile = inputFileTF.getText();
					String outputfile = outputFileTF.getText();
					System.out.println("inputfile: " + inputfile);
					System.out.println("outputfile: " + outputfile);
					if (outputfile.compareTo("") == 0) {
						label5.setText("Please specify an output file.");
					} else {
					    File f = new File(inputfile);
					    if (!f.exists()) {
							label5.setText(f.getName() + " does not exist");
						} else {
							//SimpleAttributeSet boldAttr = createSimpleAttributeSet(true, 12, Color.BLUE);
							//jt.setCharacterAttributes(boldAttr, true);
							jt.setText("");
							label5.setText("");
							Vector v = Utils.readFile(inputfile);
							for (int i=0; i<v.size(); i++) {
								String line = (String) v.elementAt(i);
								appendToPane(jt, HTMLDecoder.encode(line) + newline);
							}
							//label5.setText(outputfile + " generated.");
							saveBtn.setEnabled(true);
				    	}

					}
				}
			}
        });

        JButton decodeBtn = new JButton("Decode");
        decodeBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Decode")) {
					String inputfile = inputFileTF.getText();
					String outputfile = outputFileTF.getText();
					System.out.println("inputfile: " + inputfile);
					System.out.println("outputfile: " + outputfile);
					if (outputfile.compareTo("") == 0) {
						label5.setText("Please specify an output file.");
					} else {
					    File f = new File(inputfile);
					    if (!f.exists()) {
							label5.setText(f.getName() + " does not exist");
						} else {
							//SimpleAttributeSet boldAttr = createSimpleAttributeSet(true, 12, Color.BLUE);
							//jt.setCharacterAttributes(boldAttr, true);
							jt.setText("");
							label5.setText("");
							Vector v = Utils.readFile(inputfile);
							for (int i=0; i<v.size(); i++) {
								String line = (String) v.elementAt(i);
								appendToPane(jt, HTMLDecoder.decode(line) + newline);
							}
							//label5.setText(outputfile + " generated.");
							saveBtn.setEnabled(true);
				    	}

					}
				}

			}
        });



        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;

        ////////////////Input File//////////////////

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(label1, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(inputFileTF, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(browseBtn1, gbc);

        /////////////////Output File//////////////////////////

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(label2, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(outputFileTF, gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(browseBtn2, gbc);

        /////////////////////////////////////////////////////////////////////
        JPanel btnPanel = new JPanel();
        FlowLayout layout2 = new FlowLayout();
        btnPanel.setLayout(layout2);

        btnPanel.add(encodeBtn);
        btnPanel.add(decodeBtn);
        btnPanel.add(saveBtn);

        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(btnPanel, gbc);

        /////////////////////////////////////////////////////////////////////

        JLabel label4 = new JLabel("Results:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label4, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.insets = new Insets(3, 3, 3, 3);

		// Wrap it in a scroll pane
		//JPanel noWrapPanel = new JPanel( new BorderLayout() );
		//noWrapPanel.add( jt );

        JScrollPane scrollPane = new JScrollPane(jt);

        //JScrollPane scrollPane = new JScrollPane(noWrapPanel);

        // Set scroll policies
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        //scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        panel.add(scrollPane, gbc);

        /////////////////////////////////////////////////////////////////////

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label5, gbc);

        /////////////////////////////////////////////////////////////////////
        frame.add(panel);
        // Make the frame visible
        frame.setVisible(true);
    }
}

