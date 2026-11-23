package gov.nih.nci.evs.restapi.appl;
import gov.nih.nci.evs.restapi.ui.*;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.config.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.text.*;

public class NCItDataViewer {
    private final static String newline = "\n";
    static String DOM_CLASS_FILE = "dom_class.txt";
    static String DOM_AXIOM_FILE = "dom_axiom.txt";
    static String DOM_DATATYPE_FILE = "dom_datatype.txt";
    static String DOM_OBJECTPROPERTY_FILE = "dom_objectproperty.txt";
    static String DOM_ANNOTATIONPROPERTY_FILE = "dom_annotationproperty.txt";

    static String AXIOM_FILE = "axiom_ThesaurusInferred_forTS.txt";
    static String HIERARCHY_FILE = "parent_child.txt";
    static String A8_FILE = "A8.txt";
    static String ASSOCIATION_FILE = "association.txt";
    static String PROPERTY_FILE = "property.txt";
    static String ROLE_FILE = "roles.txt";

    public static void appendToPane(JTextPane textPane, String msg) {
        StyledDocument doc = textPane.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), msg, null);
        } catch (BadLocationException e) {
            System.err.println("Error appending text: " + e.getMessage());
        }
    }

    public static JComboBox createJComboBox(String[] items) {
		JComboBox combobox = new JComboBox(items);
		combobox.setSelectedIndex(0);
		return combobox;
	}

    public static void main(String[] args) {
        // Create the main frame
        JFrame frame = new JFrame("NCItDataViewer UI");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 200);
        frame.setLayout(new FlowLayout());
        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel label3 = new JLabel("Status");
        JLabel label4 = new JLabel("");
        JLabel label1 = new JLabel("Terminology File");
        JTextField tf1 = new JTextField();
        tf1.setPreferredSize(new Dimension(600, 30));

        String title = "Select File";
        Vector extensionFilters = new Vector();
        extensionFilters.add("OWL|owl");
        extensionFilters.add("XML|xml");
        extensionFilters.add("RDF|rdf");
        extensionFilters.add("Text|txt");
        extensionFilters.add("JSON|json");
	    JButton btn1 = SwingComponents.createFileChooserButton(title, extensionFilters, tf1);

        JButton runBtn = new JButton("Run");
        runBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Run")) {
					String owlfile = tf1.getText();
                    String rootElementTagName = "owl:Class";
                    String outputfile = NCItDomParser.parse(owlfile, rootElementTagName);
                    label4.setText(outputfile + " generated.");
                    System.out.println(outputfile + " generated.");

                    rootElementTagName = "owl:Axiom";
                    outputfile = NCItDomParser.parse(owlfile, rootElementTagName);
                    label4.setText(outputfile + " generated.");
                    System.out.println(outputfile + " generated.");

                    rootElementTagName = "rdfs:Datatype";
                    outputfile = NCItDomParser.parse(owlfile, rootElementTagName);
                    label4.setText(outputfile + " generated.");
                    System.out.println(outputfile + " generated.");

                    rootElementTagName = "owl:ObjectProperty";
                    outputfile = NCItDomParser.parse(owlfile, rootElementTagName);
                    label4.setText(outputfile + " generated.");
                    System.out.println(outputfile + " generated.");

                    rootElementTagName = "owl:AnnotationProperty";
                    outputfile = NCItDomParser.parse(owlfile, rootElementTagName);
                    label4.setText(outputfile + " generated.");
                    System.out.println(outputfile + " generated.");

                    HashMap map = DomPostProcessor.loadClasses(DOM_CLASS_FILE);
                    HashMap code2LabelMap = DomPostProcessor.generateCode2LabelMap(map);
                    // axiom file
                    Vector w = NCItDomParser.compileAxioms(DOM_AXIOM_FILE);
                    Utils.saveToFile(AXIOM_FILE, w);
                    label4.setText(AXIOM_FILE + " generated.");
                    System.out.println(AXIOM_FILE + " generated.");

                    w = DomPostProcessor.generateHierarchyDataFile(map, code2LabelMap);
                    Utils.saveToFile(HIERARCHY_FILE, w);
                    label4.setText(HIERARCHY_FILE + " generated.");
                    System.out.println(HIERARCHY_FILE + " generated.");

					Vector a8_vec = DomPostProcessor.generateA8File(map);
					Utils.saveToFile(A8_FILE, a8_vec);
					label4.setText(A8_FILE + " generated.");
					System.out.println(A8_FILE + " generated.");

					Vector prop_vec = DomPostProcessor.generatePropertyFile(map);
					Utils.saveToFile(PROPERTY_FILE, prop_vec);
					label4.setText(PROPERTY_FILE + " generated.");

	                Vector asso_vec = DomPostProcessor.generateAssociationFile(map, code2LabelMap);
	                Utils.saveToFile(ASSOCIATION_FILE, asso_vec);
	                label4.setText(ASSOCIATION_FILE + " generated.");
	                System.out.println(ASSOCIATION_FILE + " generated.");

		            Vector role_vec = DomPostProcessor.generateRestrictionFile(map);
					Utils.saveToFile(ROLE_FILE, role_vec);
					label4.setText(ROLE_FILE + " generated.");
					System.out.println(ROLE_FILE + " generated.");
					label4.setText("All files generated.");
					System.out.println("All files generated.");
				}
			}
        });

        JLabel label2 = new JLabel("Content");
        JTextPane jt = SwingComponents.createJTextPane(500, 25);
        JScrollPane scrollPane = SwingComponents.createJScrollPane(jt, 12, Color.BLUE);
        JButton btn2 = new JButton("View");
        btn2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("View")) {
				    String inputfile = tf1.getText();
					Vector v = Utils.readFile(inputfile);
					for (int i=0; i<v.size(); i++) {
						String line = (String) v.elementAt(i);
						appendToPane(jt, line + newline);
					}
				}
			}
        });

        JLabel labela = new JLabel("NCIt DOM Output File");
        JLabel labelb = new JLabel();
        String options = DOM_CLASS_FILE + "|" + DOM_AXIOM_FILE + "|" + DOM_DATATYPE_FILE + "|" + DOM_OBJECTPROPERTY_FILE + "|" + DOM_ANNOTATIONPROPERTY_FILE
             + AXIOM_FILE + "|" + HIERARCHY_FILE + "|" + A8_FILE + "|" + ASSOCIATION_FILE + "|" + PROPERTY_FILE + "|" + ROLE_FILE;

        Vector u = StringUtils.parseData(options, '|');
        String[] items = new String[u.size()];
        for (int i=0; i<u.size(); i++) {
			items[i] = (String) u.elementAt(i);
		}
        JComboBox dom_fileCombo = createJComboBox(items);
        JButton viewBtn = new JButton("View");
        viewBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("View")) {
				    String filepath = (String) dom_fileCombo.getSelectedItem();
				    filepath = filepath.replace("\\", "//");
                    ApplicationLauncher.launch(filepath);
				}
			}
        });

        JButton btn_c = new JButton("Launch");
        btn_c.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Launch")) {
				    String filepath = tf1.getText();
				    filepath = filepath.replace("\\", "//");
                    ApplicationLauncher.launch(filepath);
				}
			}
        });



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
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(runBtn, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(labela, gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(dom_fileCombo, gbc);
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(viewBtn, gbc);
/*
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label2, gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(scrollPane, gbc);
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btn2, gbc);
*/
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label3, gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label4, gbc);
        frame.add(panel);
        // Make the frame visible
        frame.setVisible(true);
    }
}
