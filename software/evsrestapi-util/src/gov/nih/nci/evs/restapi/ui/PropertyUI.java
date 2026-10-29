package gov.nih.nci.evs.restapi.ui;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.config.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class PropertyUI {

	static String NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile; //"ThesaurusInferred_forTS.owl";
	static String PARENT_CHILD_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile; // "parent_child.txt";
	static String RESTRICTION_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.rolefile; //"roles.txt";
	static String AXIOM_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.axiomfile;
	static String A8_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.subsetfile;
	static String PROPERTY_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.propertyfile;
	static String ASSOCIATION_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.associationfile;

    static HashMap code2LabelMap = null;

	public static String getDisplayName(String line) {
		Vector u = StringUtils.parseData(line, '|');
		return (String) u.elementAt(0) + " (" + (String) u.elementAt(1) + ")";
	}

	public static String[] properties = null;
	public static String[] associations = null;
	public static String[] roles = null;

/*
C1000|A8|C63923
C100000|A8|C101859
C100000|A8|C61410
C100000|A8|C66830
C100001|A8|C101859
C100001|A8|C61410
C100001|A8|C66830
C100002|A8|C101859
C100002|A8|C61410
*/
    static {
		code2LabelMap = new HashMap();
		//C1000|rdfs:label|Recombinant Amphiregulin
		Vector v = Utils.readFile(PROPERTY_FILE);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String propCode = (String) u.elementAt(1);
			if (propCode.compareTo("rdfs:label") == 0) {
				code2LabelMap.put((String) u.elementAt(0), (String) u.elementAt(2));
			}
		}

		v = Utils.readFile("supportedProperties.txt");
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			String displayName = getDisplayName(line);
			w.add(displayName);
		}
		properties = new String[w.size()];
		for (int k=0; k<w.size(); k++) {
			properties[k] = (String) w.elementAt(k);
		}

		v = Utils.readFile("supportedRoles.txt");
		w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			String displayName = getDisplayName(line);
			w.add(displayName);
		}
		roles = new String[w.size()];
		for (int k=0; k<w.size(); k++) {
			roles[k] = (String) w.elementAt(k);
		}

		v = Utils.readFile("supportedAssociations.txt");
		w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			String displayName = getDisplayName(line);
			w.add(displayName);
		}
		associations = new String[w.size()];
		for (int k=0; k<w.size(); k++) {
			associations[k] = (String) w.elementAt(k);
		}
	}

	public static String getLabel(String code) {
		return (String) code2LabelMap.get(code);
	}

	public String[] getTableHeadings(String datafile) {
		char delim = '|';
	    Vector v = Utils.readFile(datafile);
	    int m = datafile.lastIndexOf(".");
	    //title = datafile.substring(0, m);
	    String firstLine = (String) v.elementAt(0);
	    if (firstLine.indexOf("\t") == -1) {
			delim = '|';
		}
	    Vector u = StringUtils.parseData(firstLine, delim);
	    String[] headings = new String[u.size()];
	    for (int i=0; i<u.size(); i++) {
			String heading = (String) u.elementAt(i);
			headings[i] = heading;
		}
		return headings;
	}

	public static String getCode(String selectedItem) {
		int n = selectedItem.lastIndexOf("(");
		return selectedItem.substring(n+1, selectedItem.length()-1);
	}

	public static String getName(String selectedItem) {
		int n = selectedItem.lastIndexOf("(");
		return selectedItem.substring(0, n-1);
	}

//C1000|rdfs:label|Recombinant Amphiregulin
	public static String extractProperties(String filename, String selectedItem) {
        String propName = getName(selectedItem);
        String propCode = getCode(selectedItem);
        Vector v = Utils.readFile(filename);
        Vector w0 = new Vector();
        Vector w = new Vector();
        String assoCode = null;
        w0.add("Source Label|Source Code|Property Value");
        for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String propertyCode = (String) u.elementAt(1);
			if (propertyCode.compareTo(propCode) == 0) {
				String srcCode = (String) u.elementAt(0);
				String srcLabel = getLabel(srcCode);
				String propertyValue = (String) u.elementAt(2);
				w.add(srcLabel + "|" + srcCode + "|" + propertyValue);
			}
		}
		w0.addAll(new SortUtils().quickSort(w));
		String outputfile = propCode + ".txt";
		Utils.saveToFile(outputfile, w0);
		return outputfile;
	}

	public static String extractAssociations(String filename, String selectedItem) {
        String propName = getName(selectedItem);
        String propCode = getCode(selectedItem);
        Vector v = Utils.readFile(filename);
        Vector w0 = new Vector();
        Vector w = new Vector();
        String assoCode = null;
        w0.add("Source Label|Source Code|Target Label|Target Code");
        for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String propertyCode = (String) u.elementAt(1);
			if (propertyCode.compareTo(propCode) == 0) {
				String srcCode = (String) u.elementAt(0);
				String targetCode = (String) u.elementAt(2);
				String srcLabel = getLabel(srcCode);
				String targetLabel = getLabel(targetCode);
				w.add(srcLabel + "|" + srcCode + "|" + targetLabel + "|" + targetCode);
			}
		}
		w0.addAll(new SortUtils().quickSort(w));
		String outputfile = propCode + ".txt";
		Utils.saveToFile(outputfile, w0);
		return outputfile;
	}

////Severe Cardiac Valve Regurgitation|C100012|Disease_Has_Associated_Anatomic_Site|R100|Cardiovascular System|C12686
	public static String extractRestrictions(String filename, String selectedItem) {
        String associationName = getName(selectedItem);
        String associationCode = getCode(selectedItem);
        Vector v = Utils.readFile(filename);
        Vector w0 = new Vector();
        Vector w = new Vector();
        String assoCode = null;
        w0.add("Source Label|Source Code|Target Label|Target Code");
        for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			assoCode = (String) u.elementAt(3);
			if (assoCode.compareTo(associationCode) == 0) {
				String srcCode = (String) u.elementAt(1);
				String targetCode = (String) u.elementAt(5);
				String srcLabel = (String) u.elementAt(0);
				String targetLabel = (String) u.elementAt(4);
				w.add(srcLabel + "|" + srcCode + "|" + targetLabel + "|" + targetCode);
			}
		}
		w0.addAll(new SortUtils().quickSort(w));
		String outputfile = assoCode + ".txt";
		Utils.saveToFile(outputfile, w0);
		return outputfile;
	}

    public static void main(String[] args) {
        // Create the main frame
        JFrame frame = new JFrame("NCI Thesaurus Properties");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 200);
        frame.setLayout(new FlowLayout());

        JComboBox<String> comboBox1 = new JComboBox<>(properties);
        // Optional: make the combo box not editable
        comboBox1.setEditable(false);
        // Create a label to display the selected item
        JLabel label1 = new JLabel("Properties: ");
        // Add an ActionListener to handle selection changes

        comboBox1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selected = (String) comboBox1.getSelectedItem();
                //selectionLabel1.setText("Selected property: " + selected);
            }
        });

        JButton btn1 = new JButton("Data");
        btn1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Data")) {
					String title = (String) comboBox1.getSelectedItem();
					String selectedItem = (String) comboBox1.getSelectedItem();
					String datafile = extractProperties(PROPERTY_FILE, selectedItem);
					TableDialog d = new TableDialog(frame, title, datafile);
				}
			}
        });

         JComboBox<String> comboBox2 = new JComboBox<>(roles);
        // Optional: make the combo box not editable
        comboBox2.setEditable(false);
        // Create a label to display the selected item
        JLabel label2 = new JLabel("Roles: ");
        // Add an ActionListener to handle selection changes
        comboBox2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selected = (String) comboBox2.getSelectedItem();
                //selectionLabel.setText("Selected role: " + selected);
            }
        });

        JButton btn2 = new JButton("Data");
        btn2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Data")) {
					String title = (String) comboBox2.getSelectedItem();
					String selectedItem = (String) comboBox2.getSelectedItem();
					System.out.println(selectedItem);
					String datafile = extractRestrictions(RESTRICTION_FILE, selectedItem);
					TableDialog d = new TableDialog(frame, title, datafile);
				}
			}
        });

         JComboBox<String> comboBox3 = new JComboBox<>(associations);
        // Optional: make the combo box not editable
        comboBox3.setEditable(false);
        // Create a label to display the selected item
        JLabel label3 = new JLabel("Associations: ");
        // Add an ActionListener to handle selection changes
        comboBox3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selected = (String) comboBox3.getSelectedItem();
                //selectionLabel.setText("Selected role: " + selected);
            }
        });

        JButton btn3 = new JButton("Data");
        btn3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Data")) {
					String title = (String) comboBox3.getSelectedItem();
					String selectedItem = (String) comboBox3.getSelectedItem();
					String datafile = extractAssociations(ASSOCIATION_FILE, selectedItem);
					TableDialog d = new TableDialog(frame, title, datafile);
				}
			}
        });

        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);
        GridBagConstraints gbc = new GridBagConstraints();

        // Put constraints on different buttons
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(label1, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(comboBox1, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        panel.add(btn1, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(label2, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(comboBox2, gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        panel.add(btn2, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(label3, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(comboBox3, gbc);

        gbc.gridx = 2;
        gbc.gridy = 2;
        panel.add(btn3, gbc);

        //JTextArea jt = new JTextArea(20, 50);

		JPanel tbl_panel = new JPanel();
		tbl_panel.setPreferredSize(new Dimension(20, 50));
		JTable tbl = new JTable();
		tbl_panel.add(tbl);

		gbc.gridx = 1;
		gbc.gridy = 3;
		//panel.add(jt, gbc);
		panel.add(tbl_panel, gbc);
        //frame.repaint();

        frame.add(panel);

        // Make the frame visible
        frame.setVisible(true);
    }
}
