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

public class ExcelFormatterUI {

    public static void main(String[] args) {
        // Create the main frame
        JFrame frame = new JFrame("Excel Formatter");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 200);
        frame.setLayout(new FlowLayout());
        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel label1 = new JLabel("Excel File (xls)");
        JTextField tf1 = new JTextField();
        tf1.setPreferredSize(new Dimension(600, 30));
        JButton btn1 = new JButton("Browse");
        btn1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Browse")) {
					tf1.setText("");
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setDialogTitle("Select File");
					int userSelection = fileChooser.showOpenDialog(null);
					if (userSelection == JFileChooser.APPROVE_OPTION) {
						File fileToOpen = fileChooser.getSelectedFile();
						String inputfile = fileToOpen.getAbsolutePath();
						tf1.setText(inputfile);
					}
				} else {
				     //
				}
			}
        });
        JLabel label2 = new JLabel("Sheet");
		String[] options1 = new String[]{
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
			"10",
			" "};
        JComboBox<String> combobox1 = new JComboBox<>(options1);
        combobox1.setSelectedIndex(0);
        JLabel label3 = new JLabel("Sheet Name");
        JTextField tf2 = new JTextField();
        tf2.setPreferredSize(new Dimension(600, 30));
        JButton btn2 = new JButton("Generate");
        JLabel label4 = new JLabel("Status");
        JLabel label5 = new JLabel("");

        btn2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Browse")) {
					tf2.setText("");
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setDialogTitle("Select File");
					int userSelection = fileChooser.showOpenDialog(null);
					if (userSelection == JFileChooser.APPROVE_OPTION) {
						File fileToOpen = fileChooser.getSelectedFile();
						String inputfile = fileToOpen.getAbsolutePath();
					}
				} else {
					String xlsfile = tf1.getText();
					CDISCExcelUtils.copyfile(xlsfile, "bak_" + xlsfile);
					String selected = (String) combobox1.getSelectedItem();
					int sheetIndex = Integer.parseInt(selected);
					String sheetName = tf2.getText();
					boolean status = CDISCExcelUtils.reformat(xlsfile, sheetIndex, sheetName);
					label5.setText("Reformat successful? " + status);
				     // <Add action statements here.>
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
        panel.add(tf2, gbc);
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btn2, gbc);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label4, gbc);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label5, gbc);
        frame.add(panel);
        // Make the frame visible
        frame.setVisible(true);
    }
}
