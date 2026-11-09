package gov.nih.nci.evs.restapi.ui;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.config.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.text.*;

public class ValueSetHierarchyUI {

	static String NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile; //"ThesaurusInferred_forTS.owl";
	static String PARENT_CHILD_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile; // "parent_child.txt";
	static String RESTRICTION_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.rolefile; //"roles.txt";
	static String AXIOM_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.axiomfile;
	static String A8_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.subsetfile;
	static String PROPERTY_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.propertyfile;
	static String ASSOCIATION_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.associationfile;

	static HierarchyHelper hh = null;
	private final static String newline = "\n";
	HashMap subset_hmap = null;

	public ValueSetHierarchyUI() {
		initialize();
	}

	public void initialize() {
		hh = new HierarchyHelper(Utils.readFile(PARENT_CHILD_FILE));
		subset_hmap = create_subset_hmap();
	}

	public HashMap create_subset_hmap() {
		Vector association_vec = Utils.readFile(ASSOCIATION_FILE);
		HashMap hmap = new HashMap();
		for (int i=0; i<association_vec.size(); i++) {
			String line = (String) association_vec.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String member_code = (String) u.elementAt(0);
			String asso = (String) u.elementAt(1);
			if (asso.compareTo("A8") == 0) {
				String subset_code = (String) u.elementAt(2);
				Vector w = new Vector();
				if (hmap.containsKey(subset_code)) {
					w = (Vector) hmap.get(subset_code);
				}
				w.add(member_code);
				hmap.put(subset_code, w);
			}
		}
		return hmap;
	}

	public Vector resolve(String root) {
		Vector v = new Vector();
		Stack stack = new Stack();
        stack.push("@" + "|" + root);
        //v.add("NCIt Code of Table	Table PT	NCIt Concept Code	NCIt PT");
		while (!stack.isEmpty()) {
			String t = (String) stack.pop();
			Vector u = StringUtils.parseData(t, '|');
			String code = (String) u.elementAt(0);
			if (code.compareTo("@@") != 0) {
				String label = hh.getLabel(code);//(String) code2LabelMap.get(code);
				Vector memberCodes = (Vector) subset_hmap.get(code);
				if (memberCodes != null) {
					for (int i=0; i<memberCodes.size(); i++) {
						String memberCode = (String) memberCodes.elementAt(i);
						String memberLabel = hh.getLabel(memberCode);
						String data = label + "|" + code + "|" + memberLabel + "|" + memberCode;
						if (!v.contains(data)) {
							v.add(data);
						}
					}
				}

				String sub_code = (String) u.elementAt(1);
				Vector w = (Vector) hh.getSubclassCodes(sub_code);
				if (w == null) {
					stack.push(sub_code + "|@@");
				}
				if (w != null && w.size() > 0) {
					for (int i=0; i<w.size(); i++) {
						String s = (String) w.elementAt(i);
						stack.push(sub_code + "|" + s);
					}
				}
			}
		}
		System.out.println("Number of concepts in value set " + root + ": " + v.size());
		return v;
	}

    public void run() {
        // Create the main frame
        JFrame frame = new JFrame("Value Set Hierarchy UI");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 200);
        frame.setLayout(new FlowLayout());
		JLabel statusLabel = new JLabel("Status:");
		JLabel status = new JLabel("");
        JLabel hierLabel1 = new JLabel("Subset Concept Code:");
		JTextField rootTF1 = new JTextField();
		rootTF1.setPreferredSize(new Dimension(600, 30));


///////////////////////////////////////////////////////////////////////////////
        JLabel label1 = new JLabel("Hierarchy Data File:");
		JTextField hierDataFileTF = new JTextField();
		hierDataFileTF.setPreferredSize(new Dimension(600, 30));
		hierDataFileTF.setText("");

        JTextPane jt = new JTextPane();
        jt.setPreferredSize(new Dimension(25, 500));

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
					System.out.println("********************* hierdatafile: " + hierdatafile);
					String root = rootTF1.getText();
					JDialog treedialog = new TreeDialog(hierdatafile, root);
					treedialog.setSize(400, 300);
					treedialog.setVisible(true);
				}
			}
        });


///////////////////////////////////////////////////////////////////////////////

        JButton hierGeneBtn1 = new JButton("Generate");
        hierGeneBtn1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Generate")) {
					String root = rootTF1.getText();

					String hierdatafile = root + "_treedata.txt";
					System.out.println("hierdatafile: " + hierdatafile);

					Vector parent_child_vec = resolve(root);
					Utils.saveToFile(hierdatafile, parent_child_vec);
					hierDataFileTF.setText(hierdatafile);
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

        JButton hierGeneBtn2 = new JButton("Generate");
        hierGeneBtn2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				status.setText("");
				if (s.equals("Generate")) {
					String hierdatafile = rootTF1.getText();
					String format = (String) formatCombo.getSelectedItem();
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
						try {
							PrintWriter pw = null;
							n = hierdatafile.lastIndexOf(".");
							String asciitreefile = hierdatafile.substring(0, n) + "_tree.txt";
							try {
								pw = new PrintWriter(asciitreefile, "UTF-8");
								hh.printTree(pw);
							} catch (Exception ex) {

							} finally {
								try {
									pw.close();
									System.out.println("Output file " + asciitreefile + " generated.");
								} catch (Exception ex) {
									ex.printStackTrace();
								}
							}
							System.out.println(asciitreefile + " generated.");
							status.setText(asciitreefile + " generated.");
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					} else if (format.compareTo("HTML") == 0) {
						int maxLevel = 35;
                        DynamicTreePlus.generateDynamicHTMLTree(root, maxLevel);
                        status.setText(root + "_htmltree.html generated.");
                        String outputfile = HTMLHierarchy.run(hierdatafile, root, root);
                        Vector w = Utils.readFile(hierdatafile);
                        outputfile = JQueryHTMLTreeGenerator.run(w, root);
                        status.setText(outputfile + " generated.");
					}

				}
			}
        });


        JPanel panel = new JPanel();

        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;

		rootTF1.setPreferredSize(new Dimension(600, 30));
        JButton hierfileBtn1 = new JButton("Browse");

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(hierLabel1, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(rootTF1, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(hierGeneBtn1, gbc);

        ////////////////Hierarchy Data File//////////////////

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(label1, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(hierDataFileTF, gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(browseBtn1, gbc);

        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(hierGeneBtn0, gbc);

        /////////////////Hierarchy File//////////////////////////

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(statusLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(status, gbc);

        /////////////////////////////////////////////////////////////////////
        frame.add(panel);
        // Make the frame visible
        frame.setVisible(true);
    }

    public static void main(String[] args) {
		new ValueSetHierarchyUI().run();
	}
}

//Subset C173382 - COVID-19 Terminology
