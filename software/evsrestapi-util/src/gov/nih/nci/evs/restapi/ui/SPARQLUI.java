package gov.nih.nci.evs.restapi.ui;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.config.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.text.*;

public class SPARQLUI {

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

	public static boolean isPasswordCorrect(char[] input) {
		String str = new String(input);
		if (str.compareTo(ConfigurationController.password)== 0) return true;
		return false;
	}

    public static void main(String[] args) {
        // Create the main frame
        JFrame frame = new JFrame("SPARQL QUery UI");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 200);
        frame.setLayout(new FlowLayout());

        JLabel statusLabel = new JLabel("Status:");
        JLabel status = new JLabel("");

        JLabel label1 = new JLabel("Service URL:");

		Vector serviceUrl_vec = ConfigurationController.serviceUrl_vec;
		Utils.dumpVector("serviceUrl_vec", serviceUrl_vec);

//serviceUrls=http://ncias-d2175-c.nci.nih.gov:3030/NCIT2/query|http://ncidb-q294-c.nci.nih.gov:3030/NCIT2/query|http://ncidb-s308-c.nci.nih.gov:3030/NCIT2/query|http://ncidb-p335-c.nci.nih.gov:3030/NCIT2/query|http://ncias-d2175-c.nci.nih.gov:3030/CTRP/query|http://ncidb-q294-c.nci.nih.gov:3030/CTRP/query|http://ncias-q1761-v.nci.nih.gov:3030/NCIEVS/query|http://ncidb-s308-c.nci.nih.gov:3030/CTRP/query|http://ncidb-p335-c.nci.nih.gov:3030/CTRP/query


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
		//usernameTF.setText(ConfigurationController.username);

        JLabel label4 = new JLabel("Password:");
        /*
		JTextField passwordTF = new JTextField();
		passwordTF.setPreferredSize(new Dimension(600, 30));
		passwordTF.setText(ConfigurationController.password);
		*/
		JPasswordField passwordTF = new JPasswordField(10);
		passwordTF.setPreferredSize(new Dimension(600, 30));

        JLabel label5 = new JLabel("Query File:");
		JTextField queryFileTF = new JTextField();
		queryFileTF.setPreferredSize(new Dimension(600, 30));
		queryFileTF.setEditable(true);

		//queryFileTF.setText("C:/EVSFocus/USER_SUPPORT/SPARQL_RUNNER/label_query.txt");
		queryFileTF.setText("");
        JTextPane jt = new JTextPane();
        jt.setPreferredSize(new Dimension(25, 200));

        JButton browseBtn1 = new JButton("Browse");

        JLabel label6 = new JLabel("SPARQL:");
        JLabel sparqlLabel = new JLabel("SPARQL:");

        StyledDocument doc = jt.getStyledDocument();
		SimpleAttributeSet attrs = createSimpleAttributeSet(false, 12, Color.BLUE);
		StyleConstants.setLineSpacing(attrs, 0);
		StyleConstants.setSpaceAbove(attrs, 0);
		StyleConstants.setSpaceBelow(attrs, 0);
		// Apply to the whole document
		doc.setParagraphAttributes(0, doc.getLength(), attrs, false);

        JButton executeBtn = new JButton("Execute");
        browseBtn1.addActionListener(new ActionListener() {
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
						queryFileTF.setText(inputfile);
						//Populate jt
						status.setText("");
						Vector v = Utils.readFile(inputfile);
						for (int i=0; i<v.size(); i++) {
							String line = (String) v.elementAt(i);
							appendToPane(jt, line + newline);
						}
						executeBtn.setEnabled(true);
					}
				}
			}
        });

        JLabel label7 = new JLabel("SPARQL Response:");
        JTextPane jt2 = new JTextPane();
        jt2.setPreferredSize(new Dimension(25, 200));

        JButton saveBtn = new JButton("Save");
        saveBtn.setEnabled(false);
        JButton resetBtn = new JButton("Reset");
        resetBtn.setEnabled(false);

        JPanel panel2 = new JPanel();
        //panel2.setLayout(new FlowLayout());
        panel2.add(saveBtn);
        panel2.add(resetBtn);

		executeBtn.addActionListener(new ActionListener() {
		  public void actionPerformed(ActionEvent e) {
			  status.setText("");
			  String serviceUrl = (String) urlList.getSelectedItem();
			  String namedGraph = namedGraphTF.getText();
			  String username = usernameTF.getText();
			  String password = passwordTF.getText();
			  String query = jt.getText();

			  if (namedGraph.compareTo("") == 0) {
				  status.setText("Please specify a named graph.");
				  return;
			  }
			  if (username.compareTo("") == 0) {
				  status.setText("Please enter a username.");
				  return;
			  }
			  if (password.compareTo("") == 0) {
				  status.setText("Please enter a password.");
				  return;
			  }
			  if (query.compareTo("") == 0) {
				  String inputfile = queryFileTF.getText();
				  File f = new File(inputfile);
				  if (f.exists()) {
						Vector v = Utils.readFile(inputfile);
						for (int i=0; i<v.size(); i++) {
							String line = (String) v.elementAt(i);
							appendToPane(jt, line + newline);
						}
				  }
			  }
			  query = jt.getText();
			  if (query.compareTo("") == 0) {
				  status.setText("Please select or enter a SPARQL query.");
				  return;
			  }
			  String s = e.getActionCommand();
			  if (s.equals("Execute")) {
					char[] input = passwordTF.getPassword();
					String str = new String(input);
					if (isPasswordCorrect(input)) {
						OWLSPARQLUtils owlsparqlutils = new OWLSPARQLUtils(serviceUrl, username, password);
						owlsparqlutils.set_named_graph(namedGraph);
						query = jt.getText();
						Vector w = owlsparqlutils.executeQuery(query);
						//String queryfile = queryFileTF.getText();
						//System.out.println(queryfile);
						//Vector w = null;//owlsparqlutils.execute(queryfile);
						try {
							//w = SPARQLRunner.run(serviceUrl, namedGraph, username, password, queryfile);
							w = owlsparqlutils.executeQuery(query);
						} catch (Exception ex) {
							ex.printStackTrace();
						}

						if (w != null) {
							StringBuffer buf = new StringBuffer();
							for (int i=0; i<w.size(); i++) {
								String line = (String) w.elementAt(i);
								buf.append(line).append("\n");
							}
							jt2.setText(buf.toString());
							saveBtn.setEnabled(true);
							resetBtn.setEnabled(true);
						} else {
						    status.setText("No response.");
						    saveBtn.setEnabled(false);
						}

					} else {
						System.out.println("No response.");
						JOptionPane.showMessageDialog(frame,
							"Invalid password. Try again.",
							"Error Message",
							JOptionPane.ERROR_MESSAGE);
						status.setText("Invalid password.");
					}

					//Zero out the possible password, for security.
					//Arrays.fill(input, '0');
					//passwordTF.selectAll();
			   }

		  }
		});

        StyledDocument doc2 = jt2.getStyledDocument();
		//SimpleAttributeSet attrs = new SimpleAttributeSet();
		SimpleAttributeSet attrs2 = createSimpleAttributeSet(false, 12, Color.BLUE);
		StyleConstants.setLineSpacing(attrs, 0);
		StyleConstants.setSpaceAbove(attrs, 0);
		StyleConstants.setSpaceBelow(attrs, 0);
		// Apply to the whole document
		doc.setParagraphAttributes(0, doc2.getLength(), attrs2, false);

        saveBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Save")) {
					String queryfile = queryFileTF.getText();
					int n = queryfile.lastIndexOf("/");
                    if (n == -1) {
						n = queryfile.lastIndexOf("\\");
					}
					if (n == -1) {
						n = 0;
					}
                    String t = queryfile.substring(n+1, queryfile.length());
                    String outputfile = "results_" + t;
                     String t1 = jt2.getText();
                    Vector w = new Vector();
                    w.add(t1);
                    Utils.saveToFile(outputfile, w);
                    status.setText("File saved as " + outputfile);
 				}
			}
        });

        resetBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Reset")) {
					queryFileTF.setText("");
					jt.setText("");
					jt2.setText("");
					status.setText("");
					executeBtn.setEnabled(true);
					saveBtn.setEnabled(false);
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

        /////////////////Query File//////////////////////////

        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(label5, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(queryFileTF, gbc);

        gbc.gridx = 2;
        gbc.gridy = 4;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(browseBtn1, gbc);

        /////////////////Query Results //////////////////////////

        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(sparqlLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.insets = new Insets(3, 3, 3, 3);

        JScrollPane scrollPane = new JScrollPane(jt);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        //scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        panel.add(scrollPane, gbc);

        gbc.gridx = 2;
        gbc.gridy = 5;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(executeBtn, gbc);

        /////////////////Query Results //////////////////////////

        gbc.gridx = 0;
        gbc.gridy = 6;
        panel.add(label7, gbc);

        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.insets = new Insets(3, 3, 3, 3);
        JScrollPane scrollPane2 = new JScrollPane(jt2);
        scrollPane2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        //scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        panel.add(scrollPane2, gbc);

        gbc.gridx = 2;
        gbc.gridy = 6;
        gbc.insets = new Insets(3, 3, 3, 3);
        //panel.add(saveBtn, gbc);
        panel.add(panel2, gbc);

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

