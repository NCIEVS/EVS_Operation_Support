package gov.nih.nci.evs.restapi.ui;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.config.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.text.*;

public class ResolvedValueSetIteratorHolderUI {

    public static void main(String[] args) {
        // Create the main frame
        JFrame frame = new JFrame("Resolved Value Set IteratorHolder UI");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 200);
        frame.setLayout(new FlowLayout());
        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel label7 = new JLabel("Status");
        JLabel label8 = new JLabel("");
        JTextField tf4 = new JTextField();
        tf4.setPreferredSize(new Dimension(600, 30));
        JLabel label1 = new JLabel("Excel File");
        JTextField tf1 = new JTextField();

        tf1.setText("ADaM_Terminology.xls");
        tf4.setText("ADaM_Terminology.html");

        tf1.setPreferredSize(new Dimension(600, 30));
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
						int n = inputfile.lastIndexOf(".");
						String outputfile = inputfile.substring(0, n) + ".html";
						tf4.setText(outputfile);
					}
				} else {
				     // <Add action statements here.>
				}
			}
        });
        JLabel label2 = new JLabel("CDISC?");
		String[] options1 = new String[]{
			"true",
			"false"};
        JComboBox<String> combobox1 = new JComboBox<>(options1);
        combobox1.setSelectedIndex(1);
        JLabel label3 = new JLabel("Sheet Number");
		String[] options2 = new String[]{
			"0",
			"1",
			"2",
			"3",
			"4",
			"5",
			"6",
			"7",
			"8",
			"9",
			"10"};
        JComboBox<String> combobox2 = new JComboBox<>(options2);
        combobox2.setSelectedIndex(1);
        JLabel label4 = new JLabel("Column Number");


		String[] options3 = new String[]{
			"0",
			"1",
			"2",
			"3",
			"4",
			"5",
			"6",
			"7",
			"8",
			"9",
			"10"};
        JComboBox<String> combobox3 = new JComboBox<>(options3);
        combobox3.setSelectedIndex(0);
        //JTextField tf2 = new JTextField();
        //tf2.setPreferredSize(new Dimension(600, 30));
        JLabel label5 = new JLabel("Code");
        JTextField tf3 = new JTextField();
        tf3.setText("C81223");
        tf3.setPreferredSize(new Dimension(600, 30));
        JLabel label6 = new JLabel("Output File");

        JButton btn2 = new JButton("Generate");
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
						tf4.setText(inputfile);
					}
				} else {
					label8.setText("");
					String excelfile = tf1.getText();
					String sheetStr = (String) combobox2.getSelectedItem();
					int sheet = Integer.parseInt(sheetStr);

					String colStr = (String) combobox3.getSelectedItem();
					int col = Integer.parseInt(colStr);

					String code = tf3.getText();
					boolean cdisc = false;
					String cdiscStr = (String) combobox1.getSelectedItem();
					if (cdiscStr.compareTo("true") == 0) {
						cdisc = true;
					}
					String url = ResolvedValueSetIteratorHolder.DEFAULT_URL;
					String htmlfile = tf4.getText();
                    String str = ResolvedValueSetIteratorHolder.run(excelfile, sheet, col, code, cdisc, url, htmlfile);
                    label8.setText(htmlfile + " generated.");
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
        panel.add(combobox1, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label3, gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(combobox2, gbc);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label4, gbc);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(combobox3, gbc);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label5, gbc);
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(tf3, gbc);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label6, gbc);
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(tf4, gbc);
        gbc.gridx = 2;
        gbc.gridy = 5;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btn2, gbc);
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label7, gbc);
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label8, gbc);
        frame.add(panel);
        // Make the frame visible
        frame.setVisible(true);
    }
}
