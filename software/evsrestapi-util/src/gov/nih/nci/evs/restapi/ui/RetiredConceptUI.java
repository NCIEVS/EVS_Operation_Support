package gov.nih.nci.evs.restapi.ui;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.config.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.text.*;

public class RetiredConceptUI {
    static String CONCEPT_STATUS_FILE = "P310.txt";

    public static void main(String[] args) {
        // Create the main frame
        JFrame frame = new JFrame("RetiredConcept UI");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 200);
        frame.setLayout(new FlowLayout());
        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel label1 = new JLabel("OWL File");
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

        JLabel label2 = new JLabel("Status");
        JLabel label3 = new JLabel("");

        JButton btn2 = new JButton("Run");
        btn2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Run")) {
					String owlfile = tf1.getText();
				    RetiredConcept test = new RetiredConcept(owlfile);
				    label3.setText(CONCEPT_STATUS_FILE + " generated.");
				}
			}
        });

        JLabel label_postmatch = new JLabel("Match Output File");
        JTextField tf_postmatch = new JTextField();
        tf_postmatch.setPreferredSize(new Dimension(600, 30));

        title = "Select File";
        extensionFilters = new Vector();
        extensionFilters.add("Text|txt");
 	    JButton btn_postmatch = SwingComponents.createFileChooserButton(title, extensionFilters, tf_postmatch);

        JButton btn_filter = new JButton("Filter");
        btn_filter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Filter")) {
					String matchfile = tf_postmatch.getText();
					String outputfile = filter(matchfile);
				    label3.setText(outputfile + " generated.");
				}
			}

			public String filter(String matchfile) {
				System.out.println("Processing " + matchfile);
				HashSet RETIRED_CONCEPTS = new HashSet();
				Vector v = Utils.readFile(CONCEPT_STATUS_FILE);
				for (int i=0; i<v.size(); i++) {
					String line = (String) v.elementAt(i);
					Vector u = StringUtils.parseData(line, '|');
					String status = (String) u.elementAt(2);
					if (status.compareTo("Retired_Concept") == 0) {
						RETIRED_CONCEPTS.add((String) u.elementAt(0));
					}
				}
				Vector w = new Vector();
				v = Utils.readFile(matchfile);
				String firstLn = (String) v.elementAt(0);
				Vector u1 = StringUtils.parseData(firstLn, '\t');
				int col = -1;
				for (int k=0; k<u1.size(); k++) {
					String t = (String) u1.elementAt(k);
					if (t.startsWith("Matched NCIt Code")) {
						col = k;
						break;
					}
				}
				w.add(firstLn);
				for (int i=1; i<v.size(); i++) {
					String line = (String) v.elementAt(i);
					Vector u2 = StringUtils.parseData(line, '\t');
					String codes = (String) u2.elementAt(col);
					if (codes.compareTo("No match") != 0) {
						Vector u3 = StringUtils.parseData(codes, '|');
						StringBuffer buf = new StringBuffer();
						for (int j=0; j<u3.size(); j++) {
							String code = (String) u3.elementAt(j);
							if (!RETIRED_CONCEPTS.contains(code)) {
								buf.append(code).append("|");
							} else {
								System.out.println(code + " is retired.");
							}
						}
						String t = buf.toString();
						if (t.length() > 0) {
							t = t.substring(0, t.length()-1);
						}
						if (t.length() > 0) {
							line = line.replace(codes, t);
							w.add(line);
						} else {
							line = line.replace(codes, "No match");
						}
						w.add(line);
					} else {
						w.add(line);
					}
				}
				int n = matchfile.lastIndexOf(".");
				String outputfile = matchfile.substring(0, n) + "_v2.txt";
				Utils.saveToFile(outputfile, w);

				Vector diff = DiffUtils.run("Filter Retired", matchfile, outputfile);
				Utils.saveToFile("diff.txt", diff);


				return outputfile;
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
        panel.add(btn2, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label_postmatch, gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(tf_postmatch, gbc);
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btn_postmatch, gbc);
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btn_filter, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label2, gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label3, gbc);
        frame.add(panel);
        // Make the frame visible
        frame.setVisible(true);
    }
}
