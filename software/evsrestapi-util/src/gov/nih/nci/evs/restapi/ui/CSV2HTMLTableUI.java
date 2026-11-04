package gov.nih.nci.evs.restapi.ui;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.config.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.text.*;

public class CSV2HTMLTableUI {
	public static String HYPERLINK_url = "https://evsexplore.semantics.cancer.gov/evsexplore/concept/ncit/";

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

	public static boolean isPasswordCorrect(char[] input) {
		String str = new String(input);
		if (str.compareTo(ConfigurationController.password)== 0) return true;
		return false;
	}


	public static String run(String serviceUrl, String named_graph, String username, String password, String csvfile) {
		int n = csvfile.lastIndexOf(".");
		String inputfile = csvfile.substring(0, n) + ".txt";
		int columnIndex = 0;//Integer.parseInt(columnIndexStr);
		File f = new File(inputfile);
		if (!f.exists()) {
			boolean skip_heading = false;
			String delim = "\t";
			Vector v = CSVFileReader.readCSV(csvfile, skip_heading, delim);
			String firstLine = (String) v.elementAt(0);
			Vector u = StringUtils.parseData(firstLine, '\t');
			for (int k=0; k<u.size(); k++) {
				String s = (String) u.elementAt(k);
				if (s.compareToIgnoreCase("Code") == 0) {
					columnIndex = k;
					break;
				}
			}
			Utils.saveToFile(inputfile, v);
		}
		String url = HYPERLINK_url;
		String outputfile = new HTMLTableDataConverter(serviceUrl, named_graph, username, password).convert(inputfile);
		System.out.println(outputfile + " generated.");
		Vector w = CSV2HTMLTable.addHyperlinks(inputfile, columnIndex, url, '\t');
		Utils.saveToFile(inputfile, w);
		Vector v = Utils.readFile(outputfile);
		outputfile = new HTMLTable().generate(v);
		System.out.println(outputfile + " generated.");
		return outputfile;
	}

    public static void main(String[] args) {
        // Create the main frame
        JFrame frame = new JFrame("Excel to HTML Converter UI");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 200);
        frame.setLayout(new FlowLayout());

        JLabel statusLabel = new JLabel("Status:");
        JLabel status = new JLabel("");

        JLabel label1 = new JLabel("Service URL:");

		Vector serviceUrl_vec = ConfigurationController.serviceUrl_vec;
		Utils.dumpVector("serviceUrl_vec", serviceUrl_vec);

		String[] serviceUrls = new String[serviceUrl_vec.size()];
        for (int i=0; i<serviceUrl_vec.size(); i++) {
			String url = (String) serviceUrl_vec.elementAt(i);
			serviceUrls[i] = url;
		}

		JComboBox urlList = new JComboBox(serviceUrls);
		urlList.setSelectedIndex(4);
		urlList.addActionListener(new ActionListener() {
		  public void actionPerformed(ActionEvent e) {
              String serviceUrl = (String) urlList.getSelectedItem();
		  }
		});

        JLabel label2 = new JLabel("Named Graph:");
		JTextField namedGraphTF = new JTextField();
		namedGraphTF.setPreferredSize(new Dimension(600, 30));
		namedGraphTF.setText(ConfigurationController.namedGraph);

        JLabel label3 = new JLabel("User Name:");
		JTextField usernameTF = new JTextField();
		usernameTF.setPreferredSize(new Dimension(600, 30));

        JLabel label4 = new JLabel("Password:");

		JPasswordField passwordTF = new JPasswordField(10);
		passwordTF.setPreferredSize(new Dimension(600, 30));

        JLabel label5 = new JLabel("Excel (CSV) File:");
		JTextField csvFileTF = new JTextField();
		csvFileTF.setPreferredSize(new Dimension(600, 30));
		csvFileTF.setEditable(true);

		csvFileTF.setText("");
        JTextPane jt = new JTextPane();
        jt.setPreferredSize(new Dimension(25, 200));

        JButton browseBtn = new JButton("Browse");

        browseBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Browse")) {
					jt.setText("");
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setDialogTitle("Select Query File");
					int userSelection = fileChooser.showOpenDialog(null);
					if (userSelection == JFileChooser.APPROVE_OPTION) {
						File fileToOpen = fileChooser.getSelectedFile();
						//System.out.println("Selected file: " + fileToOpen.getAbsolutePath());
						String inputfile = fileToOpen.getAbsolutePath();
						csvFileTF.setText(inputfile);
					}
				}
			}
        });

        JButton generateBtn = new JButton("Generate");
        generateBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Generate")) {
					status.setText("");
					String serviceUrl = (String) urlList.getSelectedItem();
					String namedGraph = namedGraphTF.getText();
					String username = usernameTF.getText();
					String password = passwordTF.getText();

					char[] input = passwordTF.getPassword();
					//String str = new String(input);
					if (isPasswordCorrect(input)) {
						String csvfile = csvFileTF.getText();
						try {
							String outputfile = run(serviceUrl, namedGraph, username, password, csvfile);
							status.setText(outputfile + " generated.");
						} catch (Exception ex) {
							status.setText("ERROR: Excel file generation failed.");
						}
					} else {
						status.setText("Invalid password.");
					}
				}
			}
        });

        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;

        ////////////////Service URL//////////////////

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(label1, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(urlList, gbc);


        ////////////////Named Graph//////////////////

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(label2, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(namedGraphTF, gbc);

       ////////////////User Name//////////////////

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(label3, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(usernameTF, gbc);

       ////////////////Password//////////////////

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(label4, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(passwordTF, gbc);

        /////////////////CSV File//////////////////////////

        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(label5, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(csvFileTF, gbc);

        gbc.gridx = 2;
        gbc.gridy = 4;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(browseBtn, gbc);

        gbc.gridx = 3;
        gbc.gridy = 4;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(generateBtn, gbc);

        /////////////////////////////////////////////////////////////////////
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(statusLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(status, gbc);

        /////////////////////////////////////////////////////////////////////
        frame.add(panel);
        // Make the frame visible
        frame.setVisible(true);
    }
}

