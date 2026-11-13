package gov.nih.nci.evs.restapi.ui;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.config.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.text.*;
import java.awt.GridLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class DataAppenderUI {
	static String NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile;
    public static Vector TABLE_CONTENT = null;

	public static Vector<Integer> delimitedInt2integers(String delim_columns, char delim) {
		Vector w = StringUtils.parseData(delim_columns, delim);
		Vector columns = new Vector();
		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			columns.add(Integer.valueOf(Integer.parseInt(line)));
		}
		return columns;
	}

    public static void main(String[] args) {
        // Create the main frame
        JFrame frame = new JFrame("Data Appender UI");
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
        JButton btn5 = new JButton("View");

        JLabel label1 = new JLabel("Data File");
        JTextField tf1 = new JTextField();
        tf1.setPreferredSize(new Dimension(600, 30));
        String filename = "";
        tf1.setText(filename);
        JButton btn1 = new JButton("Browse");
        btn1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Browse")) {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setDialogTitle("Select File");
					File defaultDirectory = new File(System.getProperty("user.dir"));
					fileChooser.setCurrentDirectory(defaultDirectory);

					int userSelection = fileChooser.showOpenDialog(null);
					if (userSelection == JFileChooser.APPROVE_OPTION) {
						File fileToOpen = fileChooser.getSelectedFile();
						String inputfile = fileToOpen.getAbsolutePath();
						tf1.setText(inputfile);
					}
				}
			}
		});

        JButton btn2 = new JButton("View");
        btn2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("View")) {
                    String datafile = tf1.getText();
                    boolean heading = true;
					// create a dialog Box
					JDialog d = new JDialog(frame, datafile);
					//d.setLocationRelativeTo(null);
					// create a label
					JTable table = JTableMaker.createJTable(datafile, heading);
                    JScrollPane scrollPane = JTableMaker.createJScrollPane(table);
                    scrollPane.setPreferredSize(new Dimension(1200, 600));
					scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 600));
					scrollPane.setMinimumSize(new Dimension(0, 600));
					d.add(scrollPane);
					// setsize of dialog
					d.setSize(1200, 600);
					// set visibility of dialog
					d.setVisible(true);
				}
			}
		});


        JLabel label2 = new JLabel("Appended Property Codes ('|' delimited)");
        JTextField tf2 = new JTextField();
        tf2.setPreferredSize(new Dimension(600, 30));
        tf2.setText("P106|P311|P322");

        JLabel label3 = new JLabel("Output File");
        JTextField tf3 = new JTextField();
        tf3.setPreferredSize(new Dimension(600, 30));
        btn5.setEnabled(false);

        JButton btn3 = new JButton("Browse");
        btn3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Browse")) {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setDialogTitle("Select File");
					File defaultDirectory = new File(System.getProperty("user.dir"));
					fileChooser.setCurrentDirectory(defaultDirectory);

					int userSelection = fileChooser.showOpenDialog(null);
					if (userSelection == JFileChooser.APPROVE_OPTION) {
						File fileToOpen = fileChooser.getSelectedFile();
						String inputfile = fileToOpen.getAbsolutePath();
						tf1.setText(inputfile);
					}
				}
			}
		});

        JButton btn4 = new JButton("Generate");
        btn5.setEnabled(false);
        btn4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Generate")) {
					String datafile = tf1.getText();
					String propertyCodeStr = tf2.getText();
					String outputfile = tf3.getText();
					System.out.println("datafile: " + datafile);
					char delim = DelimitedDataExtractor.findDelimiter(datafile);
					int codeCol = DelimitedDataExtractor.findCodeColumnNumberInFile(datafile, delim);
					if (codeCol == -1) {
						label5.setText("WARNING: Code column not found in " + datafile + ".");
					} else {
						try {
							DataAppender.run(datafile, propertyCodeStr, outputfile);
							label5.setText(outputfile + " generated.");
							System.out.println(outputfile + " generated.");
							btn5.setEnabled(true);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}
			}
		});


        btn5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("View")) {
                    String datafile = tf3.getText();
                    boolean heading = true;
					// create a dialog Box
					JDialog d = new JDialog(frame, datafile);
					//d.setLocationRelativeTo(null);
					// create a label
					JTable table = JTableMaker.createJTable(datafile, heading);
                    JScrollPane scrollPane = JTableMaker.createJScrollPane(table);
                    scrollPane.setPreferredSize(new Dimension(1200, 600));
					scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 600));
					scrollPane.setMinimumSize(new Dimension(0, 600));
					d.add(scrollPane);
					// setsize of dialog
					d.setSize(1200, 600);
					// set visibility of dialog
					d.setVisible(true);
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
        panel.add(btn2, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label2, gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(tf2, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label3, gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(tf3, gbc);
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btn3, gbc);
        gbc.gridx = 3;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btn4, gbc);
        gbc.gridx = 4;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btn5, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label4, gbc);
        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(label5, gbc);

        frame.add(panel);
        // Make the frame visible
        frame.setVisible(true);
    }
}

