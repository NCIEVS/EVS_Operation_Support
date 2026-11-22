package gov.nih.nci.evs.restapi.ui;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.config.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.text.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.border.EmptyBorder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.XMLConstants;

import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import java.io.*;
import java.util.*;

public class NCItDomParser {
	static String NCIT_DOM = "dom.txt";
	static String NCIT_DOM_SER = "dom.ser";
	public static String NCIT_NS = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#";

	public static void printNode(StyledDocument doc, NodeList nodeList) {
		String line = null;
		for (int count = 0; count < nodeList.getLength(); count++) {
			Node tempNode = nodeList.item(count);
			// make sure it's element node.
			if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
				// get node name and value
				// tp.setText("\nNode Name=" + tempNode.getNodeName() + " [OPEN]");
				// tp.setText("\nNode Value=" + tempNode.getTextContent());
				line = "Node Name=" + tempNode.getNodeName() + " [OPEN]";
				try {
					doc.insertString(doc.getLength(), "\n" + line, null);
					line = "Node Value=" + tempNode.getTextContent();
					doc.insertString(doc.getLength(), "\n" + line, null);
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				if (tempNode.hasAttributes()) {
					// get attributes names and values
					NamedNodeMap nodeMap = tempNode.getAttributes();
					for (int i = 0; i < nodeMap.getLength(); i++) {
						Node node = nodeMap.item(i);
						//tp.setText("\nattr name: " + node.getNodeName());
						//tp.setText("\nattr value: " + node.getNodeValue());
                        try {
							line = "attr name: " + node.getNodeName();
							doc.insertString(doc.getLength(), "\n" + line, null);
							line = "attr value: " + node.getNodeValue();
							doc.insertString(doc.getLength(), "\n" + line, null);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}
				if (tempNode.hasChildNodes()) {
					// loop again if has child nodes
					printNode(doc, tempNode.getChildNodes());
				}
				line = "Node Name=" + tempNode.getNodeName() + " [CLOSE]";
				try {
					doc.insertString(doc.getLength(), "\n" + line, null);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

    public static String getAbbrevRootElementTagName(String rootElementTagName) {// example: owl:Class
        Vector u = StringUtils.parseData(rootElementTagName, ':');
        String name = (String) u.elementAt(1);
        name = name.toLowerCase();
        return name;//.substring(0, 5);
	}

    public static String getDOMOutputName(String rootElementTagName) {
	   return "dom_" + getAbbrevRootElementTagName(rootElementTagName) + ".txt";
    }


    public static String parse(String filename, String rootElementTagName) {// example: owl:Class
		PrintWriter pw = null;
		String outputfile = getDOMOutputName(rootElementTagName);
        try {
		    pw = new PrintWriter(outputfile, "UTF-8");
		    Document document = XmlDomParser.createDocument(filename);
            NodeList nodeList = XmlDomParser.getNodeListByTagName(document, rootElementTagName);
            System.out.println("Processsing ... - Please wait.");
            XmlDomParser.printNode(pw, nodeList);
            System.out.println("Done.");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
			if (pw != null) {
				try {
					pw.close();
					postProcess(outputfile);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		return outputfile;
    }

    public static void postProcess(String filename) {
		Vector w = new Vector();
		Vector v = Utils.readFile(filename);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (line.indexOf("|") != -1 && !line.startsWith(" ")) {
				w.add(line);
			}
		}
		Utils.saveToFile(filename, w);
	}

	public static Vector compileAxioms(String filename) { //"dom_axiom.txt"
		long ms = System.currentTimeMillis();
		Vector v = Utils.readFile(filename);
		int lcv = 1;
		int increment = 10000;
		int k = -1;

		String classId = null;
        Vector w = new Vector();
        StringBuffer buf = new StringBuffer();
        StringBuffer qual_buf = new StringBuffer();
        String annotatedSource = null;
        String annotatedProperty = null;
        String annotatedTarget = null;

		for (int i=0; i<v.size(); i++) {
			int j = i+1;
			if (lcv == increment) {
				k++;
				System.out.println("" + k*increment + " out of " + v.size() + " completed.");
				lcv = 0;
			}
			lcv++;
			String line = (String) v.elementAt(i);
			//C105863|P90|UDP Glucuronosyltransferase 1 Family, Polypeptide A9 wt Allele|P383$SY|P384$NCI
			if (line.compareTo("owl:Axiom|") == 0) {
                if (annotatedSource != null) {
					String quals = qual_buf.toString();
					if (quals.length() > 1) {
						quals = quals.substring(0, quals.length()-1);
					}
					String t = annotatedSource + "|" + annotatedProperty + "|" + annotatedTarget + "|" + quals;
					w.add(t);
				}
				qual_buf = new StringBuffer();
				annotatedSource = null;
				annotatedProperty = null;
				annotatedTarget = null;
			} else {
				Vector u = StringUtils.parseData(line, '|');
				String s = (String) u.elementAt(0);
				if (s.compareTo("owl:annotatedSource") == 0) {
					i++;
					line = (String) v.elementAt(i);
					u = StringUtils.parseData(line, '|');
					annotatedSource = (String) u.elementAt(1);
					int n = annotatedSource.lastIndexOf("#");
					annotatedSource = annotatedSource.substring(n+1, annotatedSource.length());
				} else if (s.compareTo("owl:annotatedProperty") == 0) {
					i++;
					line = (String) v.elementAt(i);
					u = StringUtils.parseData(line, '|');
					annotatedProperty = (String) u.elementAt(1);
					int n = annotatedProperty.lastIndexOf("#");
					annotatedProperty = annotatedProperty.substring(n+1, annotatedProperty.length());
				} else if (s.compareTo("owl:annotatedTarget") == 0) {
					annotatedTarget = (String) u.elementAt(1);
					int n = annotatedTarget.lastIndexOf("#");
					annotatedTarget = annotatedTarget.substring(n+1, annotatedTarget.length());
				} else { // qualifiers
					line = line.replace("|", "$");
					qual_buf.append(line).append("|");
				}
			}
		}
		if (annotatedSource != null) {
			String quals = qual_buf.toString();
			if (quals.length() > 1) {
				quals = quals.substring(0, quals.length()-1);
			}
			String t = annotatedSource + "|" + annotatedProperty + "|" + annotatedTarget + "|" + quals;
			w.add(t);
		}
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
		return w;
	}

	public static HashMap loadDomOutput(String filename, String tagName) { //"dom_class.txt"
		long ms = System.currentTimeMillis();
		Vector v = Utils.readFile(filename);
		String classId = null;

		int lcv = 1;
		int increment = 50000;
		int k = -1;

		HashMap map = new HashMap();
		for (int i=0; i<v.size(); i++) {
			int j = i+1;
			if (lcv == increment) {
				k++;
				System.out.println("" + k*increment + " out of " + v.size() + " completed.");
				lcv = 0;
			}
			lcv++;

			String line = (String) v.elementAt(i);
			if (line.compareTo(tagName + "|") == 0) {
				String tmp = (String) v.elementAt(i+1);
				Vector u = StringUtils.parseData(tmp, '|');
				String s = (String) u.elementAt(0);
				if (s.compareTo("rdf:about") != 0) {
					i++;
				} else {
					i++;
					line = (String) v.elementAt(i);
					int n = line.lastIndexOf("#");
					if (n != -1) {
						classId = line.substring(n+1, line.length());
						if (StringUtils.isNCItCode(classId)) {
							classId = line.substring(n+1, line.length());
						}
					}
				}

			} else if (line.compareTo("owl:Restriction|") == 0) {
				String key = "owl:Restriction";
				i++;
				i++;
				line = (String) v.elementAt(i);
				int n = line.lastIndexOf("#");
				String roleCode = line.substring(n+1, line.length());
				i++;
				i++;
				line = (String) v.elementAt(i);
				n = line.lastIndexOf("#");
				String targetCode = line.substring(n+1, line.length());
				String value = roleCode + "|" + targetCode;

				HashMap hmap = new HashMap();
				if (map.containsKey(classId)) {
					hmap = (HashMap) map.get(classId);
				}
				Vector w = new Vector();
				if (hmap.containsKey(key)) {
					w = (Vector) hmap.get(key);
				}
				if (!w.contains(value)) {
					w.add(value);
				}
				hmap.put(key, w);
				map.put(classId, hmap);

			} else {
				Vector u = StringUtils.parseData(line, '|');
				String key = (String) u.elementAt(0);
				if (key.compareTo("rdfs:subClassOf") == 0) {
					String nextLine = (String) v.elementAt(i+1);
					if (nextLine.compareTo("owl:Restriction|") == 0) {
						//load superclasses & restrictions

					} else {
						i++;
						line = (String) v.elementAt(i);
						String value = line;
						if (line.indexOf(NCIT_NS) != -1) {
							int n = line.lastIndexOf("#");
							value = line.substring(n+1, line.length());
						}
						HashMap hmap = new HashMap();
						if (map.containsKey(classId)) {
							hmap = (HashMap) map.get(classId);
						}
						Vector w = new Vector();
						if (hmap.containsKey(key)) {
							w = (Vector) hmap.get(key);
						}
						if (!w.contains(value)) {
							w.add(value);
						}
						hmap.put(key, w);
						map.put(classId, hmap);
					}
				} else {
					String value = line.replace((String) u.elementAt(0)+"|", "");
					if (value.length() == 0) {
						i++;
						line = (String) v.elementAt(i);
						if (line.indexOf(NCIT_NS) != -1) {
							int n = line.lastIndexOf("#");
							value = line.substring(n+1, line.length());
						}
					}
					HashMap hmap = new HashMap();
					if (map.containsKey(classId)) {
						hmap = (HashMap) map.get(classId);
					}
					Vector w = new Vector();
					if (hmap.containsKey(key)) {
						w = (Vector) hmap.get(key);
					}
					if (!w.contains(value)) {
						w.add(value);
					}
					hmap.put(key, w);
					map.put(classId, hmap);
				}
			}
		}
		System.out.println("" + v.size() + " out of " + v.size() + " completed.");
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
        return map;
	}

	public static HashMap loadClasses(String filename) {
	    return loadDomOutput(filename, "owl:Class");
	}

	public static HashMap loadObjectProperties(String filename) {
	    return loadDomOutput(filename, "owl:ObjectProperty");
	}

	public static HashMap loadAnnotatinProperties(String filename) {
	    return loadDomOutput(filename, "owl:AnnotationProperty");
	}


	public static Vector compileDataTypes(String filename) {
		long ms = System.currentTimeMillis();
		Vector v = Utils.readFile(filename);

		Vector w = new Vector();
		int lcv = 1;
		int increment = 10000;
		int k = -1;

		String datatypeId = null;
		Vector values = new Vector();
		for (int i=0; i<v.size(); i++) {
			int j = i+1;
			if (lcv == increment) {
				k++;
				System.out.println("" + k*increment + " out of " + v.size() + " completed.");
				lcv = 0;
			}
			lcv++;
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String t0 = (String) u.elementAt(0);
			if (t0.compareTo("rdf:about") == 0) {
				if (values.size() > 0) {
					values = new SortUtils().quickSort(values);
					String t = datatypeId + "|" + DelimitedDataExtractor.vector2DelimitedString(values, '$');
					w.add(t);
				}
				values = new Vector();
				datatypeId = (String) u.elementAt(1);
				int n = datatypeId.lastIndexOf("#");
				datatypeId = datatypeId.substring(n+1, datatypeId.length());
			} else if (t0.compareTo("rdf:first") == 0) {
				String t1 = (String) u.elementAt(1);
				if (t1.length() > 0 && t0.indexOf("#") == -1) {
					if (!values.contains(t1)) {
						values.add(t1);
					}
				}
			}
		}
		if (values.size() > 0) {
			values = new SortUtils().quickSort(values);
			String t = datatypeId + "|" + DelimitedDataExtractor.vector2DelimitedString(values, '$');
			w.add(t);
		}
		System.out.println("" + v.size() + " out of " + v.size() + " completed.");
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
		//Utils.saveToFile("datatypes.txt", w);
        return w;
    }



    public static void run(String[] args) {
        // Create the main frame
        JFrame frame = new JFrame("NCItDomParser UI");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 200);
        frame.setLayout(new FlowLayout());
        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int pageNumber = 0;
        int pageSize = 5000;
        JLabel pageNumberLabel = new JLabel(""+pageNumber);
        JLabel pageSizeLabel = new JLabel(""+pageSize);

        JLabel label_0 = new JLabel("");

        JLabel label_a = new JLabel("Root Element Tag Name");
		String[] tagnames = new String[3];
		tagnames[0] = "owl:Class";
        tagnames[1] = "owl:Axiom";
        tagnames[2] = "rdfs:Datatype";

		JComboBox tagNameList = new JComboBox(tagnames);
		tagNameList.setSelectedIndex(0);
		tagNameList.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
               //String serviceUrl = (String) urlList.getSelectedItem();
		    }
		});

        JTextField tf_a = new JTextField();
        tf_a.setPreferredSize(new Dimension(600, 30));

        JLabel label_b = new JLabel("Search Node");
        JTextField tf_b = new JTextField();
        tf_b.setPreferredSize(new Dimension(600, 30));
        JButton btn4 = new JButton("Search");
        btn4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Search")) {

				    // label4.setText(NCIT_DOM + " generated.");
				}
			}
        });

        int x_dim = 800;
        int y_dim = 500;
        int fontSize = 14;
        Color color = Color.BLACK;
        JTextPane textPane = SwingComponents.createJTextPane(x_dim, y_dim, fontSize, color);
        StyledDocument doc = textPane.getStyledDocument();

        JScrollPane scrollPane = SwingComponents.createJScrollPane4JTextPane(textPane);
        JLabel label3 = new JLabel("Status");
        JLabel label4 = new JLabel("");
        JLabel label1 = new JLabel("XML File");
        JTextField tf1 = new JTextField();
        tf1.setPreferredSize(new Dimension(600, 30));

        String title = "Select File";
        Vector extensionFilters = new Vector();
        extensionFilters.add("OWL|owl");
        extensionFilters.add("XML|xml");
        extensionFilters.add("RDF|rdf");
	    JButton btn1 = SwingComponents.createFileChooserButton(title, extensionFilters, tf1);
        JButton btn2 = new JButton("Parse");
        btn2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Parse")) {
					label4.setText("");
					String rootElementTagName = (String) tagNameList.getSelectedItem();
					label4.setText("Processing " + rootElementTagName + " -- please wait...");
					StyledDocument doc = textPane.getStyledDocument();
					// <Add action statements here.>
					System.out.println("Processing " + rootElementTagName);
					String outputfile = parse(tf1.getText(), rootElementTagName);
					label4.setText(outputfile + " generated.");
					label_0.setText(outputfile);
					if (rootElementTagName.compareTo("owl:Axiom") == 0) {
						Vector w = compileAxioms(outputfile);
						Utils.saveToFile("axiom_ThesaurusInferred_forTS.txt", w);
						label4.setText(outputfile + " and axiom_ThesaurusInferred_forTS.txt generated.");
					} else if (rootElementTagName.compareTo("rdfs:Datatype") == 0) {
						Vector w = compileDataTypes(outputfile);
						Utils.saveToFile("datatypes.txt", w);
						label4.setText(outputfile + " and datatypes.txt generated.");
					}
				}
			}
        });
        JLabel label2 = new JLabel("Results");
        JButton btn3 = new JButton("View");
        JButton btn3a = new JButton("Prev");
        JButton btn3b = new JButton("Next");

		btn3a.setEnabled(false);
		btn3b.setEnabled(false);


        btn3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				Vector v = null;
				if (s.equals("View")) {
					String outputfile = label_0.getText();
					File f = new File(outputfile);
					if (!f.exists()) {
						label4.setText(outputfile + " does not exist. Please press the Parse button to generate the file." );
						return;
				    }
				    v = Utils.readFile(outputfile);
					System.out.println("Number of lines: " + v.size());
					StyledDocument doc = textPane.getStyledDocument();
/*
					String pageNumberStr = pageNumberLabel.getText();
					int pageNumber = Integer.parseInt(pageNumberStr);

					String pageSizeStr = pageSizeLabel.getText();
					int pageSize = Integer.parseInt(pageSizeStr);
					int istart = pageNumber * pageSize;
					int iend = (pageNumber + 1) * pageSize - 1;
*/
                    int istart = 0;
                    int iend = v.size();
					for (int i=istart; i<iend; i++) {
						String line = (String) v.elementAt(i);
						// Append a new line at the end
						try {
							doc.insertString(doc.getLength(), "\n" + line, null);
							istart = iend + 1;
							iend = istart + pageSize;
						} catch (BadLocationException ex) {
							ex.printStackTrace();
						}
					}
                    btn3a.setEnabled(false);
                    btn3b.setEnabled(false);
				}
			}
        });

        btn3a.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Prev")) {
					Vector v = SerializationUtils.deserializeVector(NCIT_DOM_SER);

					String pageNumberStr = pageNumberLabel.getText();
					int pageNumber = Integer.parseInt(pageNumberStr);
					int istart = pageNumber * pageSize;
					int iend = (pageNumber + 1) * pageSize - 1;

					if (iend > v.size()) {
						iend = v.size();
					}

					System.out.println("pageNumber: " + pageNumber);
					System.out.println("istart: " + istart);
					System.out.println("iend: " + iend);

					for (int i=istart; i<iend; i++) {
						String line = (String) v.elementAt(i);
						// Append a new line at the end
						try {
							doc.insertString(doc.getLength(), "\n" + line, null);
							istart = iend + 1;
							iend = istart + pageSize;
						} catch (BadLocationException ex) {
							ex.printStackTrace();
						}
					}

					label4.setText("Page " + pageNumber + " displayed.");

					pageNumber--;
					pageNumberLabel.setText("" + pageNumber);


					/*
					try {
						Document doc = textPane.getDocument();
						for (int i=0; i<50000; i++) {
							String text = doc.getText(0, doc.getLength());
							int lastLineBreak = text.lastIndexOf('\n');
							if (lastLineBreak != -1) {
								doc.remove(lastLineBreak, doc.getLength() - lastLineBreak);
							}
						}
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
					*/
				}
			}
        });


        btn3b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Next")) {
					btn3b.setEnabled(false);
					Vector v = SerializationUtils.deserializeVector(NCIT_DOM_SER);

					String pageNumberStr = pageNumberLabel.getText();
					int pageNumber = Integer.parseInt(pageNumberStr);
					int istart = pageNumber * pageSize;
					int iend = (pageNumber + 1) * pageSize - 1;
					if (iend > v.size()) {
						iend = v.size();
					}

					System.out.println("pageNumber: " + pageNumber);
					System.out.println("istart: " + istart);
					System.out.println("iend: " + iend);

					for (int i=istart; i<iend; i++) {
						String line = (String) v.elementAt(i);
						// Append a new line at the end
						try {
							doc.insertString(doc.getLength(), "\n" + line, null);
							istart = iend + 1;
							iend = istart + pageSize;
						} catch (BadLocationException ex) {
							ex.printStackTrace();
						}
					}

					label4.setText("Page " + pageNumber + " displayed.");

					pageNumber++;
					pageNumberLabel.setText("" + pageNumber);
					btn3b.setEnabled(true);

		    	}
			}
        });

		int row = 0;
		int[] cols = new int[3];
		for (int i=0; i<cols.length; i++) {
			cols[i] = i;
		}
		Vector<JComponent> components = new Vector();
		components.add(label1);
		components.add(tf1);
		components.add(btn1);
	    SwingComponents.gridBagConstraintsLayout(gbc, row, cols, components, panel);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label_a, gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(tagNameList, gbc);
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btn2, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label2, gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(scrollPane, gbc);

 		Vector<JComponent> components_a = new Vector();
 		components_a.add(btn3);
 		components_a.add(btn3a);
 		components_a.add(btn3b);
 		JPanel panel_a = SwingComponents.boxVerticalLayout(components_a);
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(panel_a, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label_b, gbc);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(tf_b, gbc);
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btn4, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label3, gbc);
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label4, gbc);

        frame.add(panel);
        // Make the frame visible
        frame.setVisible(true);
    }

    public static void main(String[] args) {
		run(args);
	}

}

