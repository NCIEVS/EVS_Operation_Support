package gov.nih.nci.evs.restapi.ui;
import gov.nih.nci.evs.restapi.appl.*;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.config.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.text.*;

public class AxiomFileUI {

    public static void main(String[] args) {
        // Create the main frame
        JFrame frame = new JFrame("AxiomFile UI");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 200);
        frame.setLayout(new FlowLayout());
        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel label1 = new JLabel("Select Axiom File");
        JTextField tf1 = new JTextField();
        tf1.setPreferredSize(new Dimension(600, 30));
        tf1.setText("axiom_ThesaurusInferred_forTS.txt");

        String title = "Select Axiom File";
        Vector extensionFilters = new Vector();
        extensionFilters.add("OWL|owl");
        extensionFilters.add("XML|xml");
        extensionFilters.add("RDF|rdf");
        extensionFilters.add("Text|txt");
	    JButton btn1 = SwingComponents.createFileChooserButton(title, extensionFilters, tf1);

        JLabel label2 = new JLabel("Select Hierarchy File");
        JTextField tf2 = new JTextField();
        tf2.setPreferredSize(new Dimension(600, 30));
        tf2.setText("parent_child.txt");
        title = "Select Hierarchy File";
	    JButton btn2 = SwingComponents.createFileChooserButton(title, extensionFilters, tf2);

        JLabel label3 = new JLabel("Select Subset (A8) File");
        JTextField tf3 = new JTextField();
        tf3.setPreferredSize(new Dimension(600, 30));
        tf3.setText("A8.txt");
        title = "Select Subset (A8) File";
	    JButton btn3 = SwingComponents.createFileChooserButton(title, extensionFilters, tf3);

        JLabel label4 = new JLabel("Qualifiers");
        JTextField tf4 = new JTextField();
        tf4.setPreferredSize(new Dimension(600, 30));
        JButton btn_reset = new JButton("Reset");
        btn_reset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Reset")) {
				     // <Add action statements here.>
				     tf4.setText("");
				}
			}
        });
        JButton btn_default = new JButton("Default");
        btn_default.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Default")) {
				     // <Add action statements here.>
				     tf4.setText("P90|P383$PT|P384$NCI");
				}
			}
        });

        JLabel label_roots = new JLabel("Roots");
        JTextField tf_roots = new JTextField();

        JLabel selectedMode = new JLabel();
        JLabel label5 = new JLabel("Mode");
		JPanel btnGroupPanel1 = new JPanel();
		ButtonGroup btnGroup1 = new ButtonGroup();
		JRadioButton btn_1_0 = new JRadioButton("Default");
		btn_1_0.setSelected(true);
        btn_1_0.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedMode.setText("Default");
				String s = e.getActionCommand();
				if (s.equals("Default")) {
					tf_roots.setText("");
				    tf_roots.setEditable(false);
				}
			}
        });

		btnGroup1.add(btn_1_0);
		btnGroupPanel1.add(btn_1_0);
		JRadioButton btn_1_1 = new JRadioButton("Branch");
		btn_1_1 .addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			selectedMode.setText("Branch");
			tf_roots.setText("");
			tf_roots.setEditable(true);

		}
		});
		btnGroup1.add(btn_1_1);
		btnGroupPanel1.add(btn_1_1);
		JRadioButton btn_1_2 = new JRadioButton("Subset");
		btn_1_2 .addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			selectedMode.setText("Subset");
			tf_roots.setText("");
			tf_roots.setEditable(true);
		}
		});
		btnGroup1.add(btn_1_2);
		btnGroupPanel1.add(btn_1_2);

        tf_roots.setPreferredSize(new Dimension(600, 30));
        JButton btn_reset_roots = new JButton("Reset");
        btn_reset_roots.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Reset")) {
				     // <Add action statements here.>
				     tf_roots.setText("");
				}
			}
        });




        JLabel label_output = new JLabel("Select Output File");
        JTextField tf_output = new JTextField();
        tf_output.setPreferredSize(new Dimension(600, 30));
        tf_output.setText("termfile.txt");
        title = "Select Hierarchy File";
	    JButton btn_output = SwingComponents.createFileChooserButton(title, extensionFilters, tf_output);

        JButton btn_view = new JButton("View");
        btn_view.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("View")) {
				     // <Add action statements here.>
				     String outputfile = "";
				     ApplicationLauncher.launch(tf_output.getText());
				}
			}
        });

        JLabel label6 = new JLabel("Status");
        JLabel label7 = new JLabel("");

        JButton btn4 = new JButton("Generate");
        btn4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Generate")) {
				     // <Add action statements here.>
				     String axiomfile = tf1.getText();
				     axiomfile = axiomfile.trim();
				     if (axiomfile.length() == 0) {
				         JOptionPane.showMessageDialog(frame,
					         "Please specify an axiom file.",
					         "WARNING",
					         JOptionPane.WARNING_MESSAGE
                         );
					 }
				     File f = new File(axiomfile);
				     if (!f.exists()) {
				         JOptionPane.showMessageDialog(frame,
					         axiomfile + " does not exist.",
					         "WARNING",
					         JOptionPane.WARNING_MESSAGE
                         );
                         return;
					 }

					 String hierarchyFile = tf2.getText();
					 hierarchyFile = hierarchyFile.trim();
				     if (hierarchyFile.length() == 0) {
				         JOptionPane.showMessageDialog(frame,
					         "Please specify a hierarchy file.",
					         "WARNING",
					         JOptionPane.WARNING_MESSAGE
                         );
                         return;
					 }

					 String a8file = tf3.getText();
					 a8file = a8file.trim();
				     if (a8file.length() == 0) {
				         JOptionPane.showMessageDialog(frame,
					         "Please specify a hierarchy file.",
					         "WARNING",
					         JOptionPane.WARNING_MESSAGE
                         );
                         return;
					 }

					 int mode = ExactMatch.DEFAULT;
					 if (btn_1_1.isSelected()) {
					 	mode = ExactMatch.BRANCH;
					 } else if (btn_1_2.isSelected()) {
					 	mode = ExactMatch.SUBSET;
					 }
					 String rootStr = tf_roots.getText();
					 Vector roots = null;
					 rootStr = rootStr.trim();
					 if (mode != ExactMatch.DEFAULT && rootStr.length() == 0) {
				         JOptionPane.showMessageDialog(frame,
					         "Please specify at least one root ('|' delimited).",
					         "WARNING",
					         JOptionPane.WARNING_MESSAGE
                         );
						 return;
					 } else {
					     roots = StringUtils.parseData(rootStr, '|');
					 }
					 String qualifiers = tf4.getText();
					 Vector dataSpec = null;
					 if (qualifiers.length() > 0) {
					     dataSpec = StringUtils.parseData(qualifiers, '|');
					 }
					 String outputfile = tf_output.getText();
					 outputfile = outputfile.trim();
					 if (outputfile.length() == 0) {
				         JOptionPane.showMessageDialog(frame,
					         "Please specify a output file.",
					         "WARNING",
					         JOptionPane.WARNING_MESSAGE
                         );
						 return;
					 }
					 try {
					 	 AxiomFileTrimmer.run(axiomfile, hierarchyFile, a8file, mode, roots, dataSpec, outputfile);
					 	 label7.setText(outputfile + " generated.");
					 } catch (Exception ex) {
				         JOptionPane.showMessageDialog(frame,
					         "Something went wrong - output file not generated.",
					         "ERROR",
					         JOptionPane.WARNING_MESSAGE
                         );
					 }
				}
			}
        });

        JLabel label_datafile = new JLabel("Exact Match Data File");
        JTextField tf_datafile = new JTextField();
        tf_datafile.setPreferredSize(new Dimension(600, 30));
        tf_datafile.setText("");
        title = "Select Data File";
	    JButton btn_datafile = SwingComponents.createFileChooserButton(title, extensionFilters, tf_datafile);
        JButton btn_match = new JButton("Match");
        btn_match.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Match")) {
					long ms = System.currentTimeMillis();
					String axiomfile = tf1.getText();
					String datafile = tf_datafile.getText();
					int n = datafile.lastIndexOf(".");
					String outputfile = datafile.substring(0, n) + "_results.txt";
					System.out.println("outputfile: " + outputfile);

					int mode = ExactMatch.DEFAULT;
					if (btn_1_1.isSelected()) {
					 	mode = ExactMatch.BRANCH;
					} else if (btn_1_2.isSelected()) {
					 	mode = ExactMatch.SUBSET;
					}
					int colNum = 0;
					String termfile = tf_output.getText();
					if (mode == ExactMatch.DEFAULT) {
						new ExactMatch(axiomfile).run(datafile, outputfile, colNum);
					} else if (mode == ExactMatch.BRANCH) {
						new ExactMatch(termfile).run(datafile, outputfile, colNum);
					} else if (mode == ExactMatch.SUBSET) {
						new ExactMatch(termfile).run(datafile, outputfile, colNum);
					}
					//System.out.println("Appending data ...");
					//runner.appendData(outputfile);
					ExactMatch.toExcel(outputfile);
					long ms1 = System.currentTimeMillis();
					long timeElapsed = ms1 - ms;
					System.out.println("Total run time: " + timeElapsed + " (milli-seconds)");
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
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label2, gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(tf2, gbc);
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btn2, gbc);
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
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label4, gbc);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(tf4, gbc);
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btn_reset, gbc);
        gbc.gridx = 3;
        gbc.gridy = 3;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btn_default, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label5, gbc);
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btnGroupPanel1, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label_roots, gbc);
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(tf_roots, gbc);
        gbc.gridx = 2;
        gbc.gridy = 5;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btn_reset_roots, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label_output, gbc);
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(tf_output, gbc);
        gbc.gridx = 2;
        gbc.gridy = 6;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btn_output, gbc);

        gbc.gridx = 3;
        gbc.gridy = 6;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btn4, gbc);
        gbc.gridx = 4;
        gbc.gridy = 6;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btn_view, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label6, gbc);
        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label7, gbc);

        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label_datafile, gbc);
        gbc.gridx = 1;
        gbc.gridy = 8;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(tf_datafile, gbc);
        gbc.gridx = 2;
        gbc.gridy = 8;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btn_datafile, gbc);
        gbc.gridx = 3;
        gbc.gridy = 8;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btn_match, gbc);

        frame.add(panel);
        // Make the frame visible
        frame.setVisible(true);
    }
}
