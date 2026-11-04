package gov.nih.nci.evs.restapi.ui;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.config.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.text.*;

public class Text2ExcelUI {

	private final static String newline = "\n";

    public static void run() {
        // Create the main frame
        JFrame frame = new JFrame("Text2Excel UI");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 200);
        frame.setLayout(new FlowLayout());
		JLabel statusLabel = new JLabel("Status:");
		JLabel status = new JLabel("");

        JLabel label1 = new JLabel("Text Data Directory:");
		JTextField textDataDirTF = new JTextField();
		textDataDirTF.setPreferredSize(new Dimension(600, 30));
		textDataDirTF.setText("");

		JTextField excelFileTF = new JTextField();
		excelFileTF.setPreferredSize(new Dimension(600, 30));
		excelFileTF.setText("");

        JButton browseBtn1 = new JButton("Browse");
        browseBtn1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Browse")) {
					JFileChooser chooser = new JFileChooser();
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); // Show only directories
					int option = chooser.showOpenDialog(frame);
					if (option == JFileChooser.APPROVE_OPTION) {
						File selectedDir = chooser.getSelectedFile();
						String pathName = selectedDir.getAbsolutePath();
						System.out.println("Selected directory: " + pathName);
						textDataDirTF.setText(pathName);
						String dirname = selectedDir.getName();
						System.out.println("Selected directory: " + dirname);
						excelFileTF.setText(dirname + ".xlsx");
					} else {
						System.out.println("Operation cancelled.");
					}
				}
			}
        });

///////////////////////////////////////////////////////////////////////////////

        JLabel delimiterLabel = new JLabel("Delimiter:");
        String[] formats = new String[] {"tab", "|"};
        JComboBox<String> delimiterCombo = new JComboBox<>(formats);
        delimiterCombo.setPreferredSize(new Dimension(30, 30));
        delimiterCombo.setSelectedIndex(0);

        JLabel label2 = new JLabel("Excel File (xlsx):");

        JButton generateBtn = new JButton("Generate");
        generateBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				status.setText("");
				if (s.equals("Generate")) {
					String dir = textDataDirTF.getText();
					String excelfile = excelFileTF.getText();
					char delim = '\t';
					String selected = (String) delimiterCombo.getSelectedItem();
					if (selected.compareTo("|") == 0) {
						delim = '|';
					}
					try {
						String pathName = textDataDirTF.getText();
                        File f = new File(pathName);
						String dirname = f.getName();
						Text2Excel.generateExcel(dirname, excelfile, delim);
						status.setText(excelfile + " generated.");
					} catch (Exception ex) {
						status.setText("Error generating " + excelfile + ".");
					}
				}
			}
        });


        JPanel panel = new JPanel();

        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(label1, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(textDataDirTF, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(browseBtn1, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(delimiterLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(delimiterCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(label2, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(excelFileTF, gbc);

        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(generateBtn, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(statusLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(status, gbc);

        /////////////////////////////////////////////////////////////////////
        frame.add(panel);
        // Make the frame visible
        frame.setVisible(true);
    }

    public static void main(String[] args) {
		run();
	}
}

