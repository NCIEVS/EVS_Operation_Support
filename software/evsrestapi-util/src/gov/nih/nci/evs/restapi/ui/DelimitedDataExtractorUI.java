package gov.nih.nci.evs.restapi.ui;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.config.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.text.*;

public class DelimitedDataExtractorUI {

	public static Vector extractByMatchingColumnValue(Vector w, char delim, int col2Match, HashSet hset) {
        Vector<Integer> columns = null;
        return extractByMatchingColumnValue(w, delim, null, col2Match, hset);
	}

	public static Vector extractByMatchingColumnValue(Vector w, char delim, Vector<Integer> columns, int col2Match, HashSet hset) {
		Vector v = new Vector();
		String firstLn = (String) w.elementAt(0);
		v.add(firstLn);
		for (int i=1; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			Vector u = StringUtils.parseData(line, delim);
			String t = (String) u.elementAt(col2Match);
            if (hset.contains(t)) {
				if (columns != null) {
					StringBuffer buf = new StringBuffer();
					for (int k=0; k<columns.size(); k++) {
						Integer column_obj = (Integer) columns.elementAt(k);
						int column_int = Integer.parseInt(column_obj.toString());
						String t2 = (String) u.elementAt(column_int);
						buf.append(t2).append(delim);
					}
					String s = buf.toString();
					s = s.substring(0, s.length()-1);
					v.add(s);
				} else {
					v.add(line);
				}
			}
		}
		return v;
	}

    public static void main(String[] args) {
        // Create the main frame
        JFrame frame = new JFrame("Delimited Data Extractor UI");
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
        JLabel label_b = new JLabel("Match Column (format: source col|target col)");
        JTextField tf_b = new JTextField();
        tf_b.setPreferredSize(new Dimension(600, 30));

        JLabel label1 = new JLabel("Delimited Data File");
        JTextField tf1 = new JTextField();
        tf1.setPreferredSize(new Dimension(600, 30));
        JButton btn1 = new JButton("Browse");
        btn1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Browse")) {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setDialogTitle("Select File");
					int userSelection = fileChooser.showOpenDialog(null);
					if (userSelection == JFileChooser.APPROVE_OPTION) {
						File fileToOpen = fileChooser.getSelectedFile();
						String inputfile = fileToOpen.getAbsolutePath();
						tf1.setText(inputfile);
					}
				} else {
				     // <Add action statements here.>
				}
			}
        });

        JButton btn_b = new JButton("HTML Table");
        btn_b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("HTML Table")) {
					String textfile = tf1.getText();
					HTMLTable.delimitedText2HTMLTable(textfile, '\t');
					int n = textfile.lastIndexOf(".");
					String outputfile = textfile.substring(0, n) + ".html";
                    label5.setText(outputfile + " generated.");
				}
			}
        });

        JLabel label2 = new JLabel("Delimiter");
		String[] options1 = new String[]{
			"tab",
			"bar"};
        JComboBox<String> combobox1 = new JComboBox<>(options1);
        combobox1.setSelectedIndex(0);
        JLabel label3 = new JLabel("Bar('|') Delimited Column Numbers");
        JTextField tf2 = new JTextField();
        tf2.setPreferredSize(new Dimension(600, 30));


        JButton btn2 = new JButton("Extract");
        btn2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Extract")) {
				     // <Add action statements here.>
				     String filename = tf1.getText();
				     Vector v = Utils.readFile(filename);
				     String firstLine = (String) v.elementAt(0);
                     String delimStr = (String) combobox1.getSelectedItem();
				     char delim = '\t';
				     if (delimStr.compareTo("bar") == 0) {
						 delim = '|';
					 }
				     Vector u = StringUtils.parseData(firstLine, delim);
				     Utils.dumpVector(firstLine, u);

				     String delim_columns = tf2.getText();
				     Vector w = DelimitedDataExtractor.extract(filename, delim_columns, delim);
				     int n = filename.lastIndexOf(".");
					 String t = filename.substring(0, n);
					 t = t + "_v2.txt";
				     Utils.saveToFile(t, w);
				     label5.setText(t + " generated.");
				}
			}
        });

        JButton btn_a = new JButton("Remove");
        btn_a.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Remove")) {
				     // <Add action statements here.>
				     String filename = tf1.getText();
				     Vector v = Utils.readFile(filename);
				     String firstLine = (String) v.elementAt(0);
                     String delimStr = (String) combobox1.getSelectedItem();
				     char delim = '\t';
				     if (delimStr.compareTo("bar") == 0) {
						 delim = '|';
					 }
				     Vector u = StringUtils.parseData(firstLine, delim);
				     Utils.dumpVector(firstLine, u);

				     String delim_columns = tf2.getText();
				     Vector<Integer> columns = DelimitedDataExtractor.delimitedInt2integers(delim_columns, '|');
				     Vector w = DelimitedDataExtractor.removeColumnData(v, columns, delim);
				     int n = filename.lastIndexOf(".");
					 String t = filename.substring(0, n);
					 t = t + "_v1.txt";
				     Utils.saveToFile(t, w);
				     label5.setText(t + " generated.");
				}
			}
        });

////////////////////////////////////////////////////////////////////////////////////////////
        JLabel label_a = new JLabel("Matched File");
        JTextField tf_a = new JTextField();
        tf_a.setPreferredSize(new Dimension(600, 30));

        JButton btn_c = new JButton("Browse");
        btn_c.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Browse")) {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setDialogTitle("Select File");
					int userSelection = fileChooser.showOpenDialog(null);
					if (userSelection == JFileChooser.APPROVE_OPTION) {
						File fileToOpen = fileChooser.getSelectedFile();
						String inputfile = fileToOpen.getAbsolutePath();
						tf_a.setText(inputfile);
					}
				} else {
				     // <Add action statements here.>
				}
			}
        });

        JButton btn_d = new JButton("Match");
        btn_d.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Match")) {
					String textfile = tf_a.getText();  //match file
					//hset
					String cols = tf_b.getText();
					Vector u = StringUtils.parseData(cols, '|');
					String col1 = (String) u.elementAt(0);
					String col2 = (String) u.elementAt(1);
					String delim_columns = col2;
                    String delimStr = (String) combobox1.getSelectedItem();
				    char delim = '\t';
				    if (delimStr.compareTo("bar") == 0) {
						delim = '|';
					}
					Vector w1 = DelimitedDataExtractor.extract(textfile, delim_columns, delim);
                    String removedElement = (String) w1.remove(0);
                    HashSet hset = Utils.vector2HashSet(w1);

                    String sourcefile = tf1.getText();
                    Vector w = Utils.readFile(sourcefile);
                    Vector<Integer> columns = null;
                    delim_columns = tf2.getText();
                    if (delim_columns.length() > 0) {
						columns = DelimitedDataExtractor.delimitedInt2integers(delim_columns, '|');
					}
                    int col2Match = Integer.parseInt(col1);
                    w = extractByMatchingColumnValue(w, delim, columns, col2Match, hset);
                    Utils.saveToFile("results_" + sourcefile, w);
                    label5.setText("results_" + sourcefile + " generated.");
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
        panel.add(btn_b, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label2, gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(combobox1, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label3, gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(tf2, gbc);
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btn2, gbc);

        gbc.gridx = 3;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btn_a, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label_a, gbc);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(tf_a, gbc);


        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label_b, gbc);
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(tf_b, gbc);
        gbc.gridx = 2;
        gbc.gridy = 4;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btn_d, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label4, gbc);
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label5, gbc);

        frame.add(panel);
        // Make the frame visible
        frame.setVisible(true);
    }
}
