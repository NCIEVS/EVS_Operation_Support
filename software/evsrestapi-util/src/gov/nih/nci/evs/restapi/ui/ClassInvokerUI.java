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

public class ClassInvokerUI {
    public static Vector TABLE_CONTENT = null;
    public static void main(String[] args) {
        // Create the main frame
        JFrame frame = new JFrame("Subset Home UI");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 200);
        frame.setLayout(new FlowLayout());
        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel label1 = new JLabel("Configuration File");
        JTextField tf1 = new JTextField();
        tf1.setPreferredSize(new Dimension(600, 30));
        String filename = "value_set_report_config.txt";
        tf1.setText(filename);
        JButton btn1 = new JButton("Browse");
        JLabel label2 = new JLabel("Selected Table Item");
        JTextField tf2 = new JTextField();
        tf2.setPreferredSize(new Dimension(600, 30));
        JLabel label6 = new JLabel("Execution Class");
        JTextField tf3 = new JTextField();
        tf3.setPreferredSize(new Dimension(600, 30));
        ///////////////////////////////////////////////////////////////////////
        tf3.setText("EmbeddedExcelPageGenerator");

        JLabel label3 = new JLabel("Select Row");
        boolean heading = false;
        JLabel label4 = new JLabel("Status");
        JLabel label5 = new JLabel("");

        JTable table = JTableMaker.createJTable(filename, heading);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				if (!event.getValueIsAdjusting()) {
					int selectedRow = table.getSelectedRow();
					System.out.println("Selected row: " + selectedRow);
					if (TABLE_CONTENT == null) {
						TABLE_CONTENT = Utils.readFile(tf1.getText());
					}
					tf2.setText((String) TABLE_CONTENT.elementAt(selectedRow));
					label5.setText("");
				}
			}
		});

        JScrollPane scrollPane = JTableMaker.createJScrollPane(table);
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
						TABLE_CONTENT = Utils.readFile(inputfile);
					}
				} else {
				     // <Add action statements here.>
				}
			}
        });


        JButton btn2 = new JButton("Execute");
        btn2.addActionListener(new ActionListener() {
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
					}
				} else {
					System.out.println("Execute Button pressed...");
				    // <Add action statements here.>
				    String className = tf3.getText();
				    label5.setText("Exeuting " + className);
                    try {
						System.out.println("instantiating " + className);
						Class<?> clazz = Class.forName(className);
						System.out.println(clazz.getName() + " instantiated.");
						Method method = clazz.getDeclaredMethod("invoke", String.class);
						method.invoke(null, tf2.getText());
					} catch (Exception ex) {
						ex.printStackTrace();
					}
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
        panel.add(label3, gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(scrollPane, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label6, gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(tf3, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label2, gbc);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(tf2, gbc);
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btn2, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label4, gbc);
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label5, gbc);

        frame.add(panel);
        // Make the frame visible
        frame.setVisible(true);
    }
}

