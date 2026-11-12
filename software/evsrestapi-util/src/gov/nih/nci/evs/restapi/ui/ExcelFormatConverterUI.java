package gov.nih.nci.evs.restapi.ui;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.config.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.text.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ExcelFormatConverterUI extends JFrame {

    private static void showOpenFileDialog(JTextField tf1) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        File defaultDirectory = new File(System.getProperty("user.dir"));
        fileChooser.setCurrentDirectory(defaultDirectory);

        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Microsoft Excel 97-2003 Worksheet", "xls"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Microsoft Excel Workbook", "xlsx"));

        fileChooser.setAcceptAllFileFilterUsed(true);
        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            System.out.println("Selected file: " + selectedFile.getAbsolutePath());
            tf1.setText(selectedFile.getAbsolutePath());
        }
    }

    public static void main(String[] args) {
        // Create the main frame
        JFrame frame = new JFrame("ExcelFormatConverter UI");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 200);
        frame.setLayout(new FlowLayout());
        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel label1 = new JLabel("Excel File");
        JTextField tf1 = new JTextField();
        tf1.setPreferredSize(new Dimension(600, 30));
        JButton btn1 = new JButton("Browse");

        btn1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                showOpenFileDialog(tf1);
            }
        });

        JLabel label2 = new JLabel("Status");
        JLabel label3 = new JLabel("");

        JButton btn2 = new JButton("Convert");
        btn2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Browse")) {
					JFileChooser fileChooser = new JFileChooser();
					File defaultDirectory = new File(System.getProperty("user.dir"));
					fileChooser.setCurrentDirectory(defaultDirectory);
					fileChooser.setDialogTitle("Select File");
					int userSelection = fileChooser.showOpenDialog(null);
					if (userSelection == JFileChooser.APPROVE_OPTION) {
						File fileToOpen = fileChooser.getSelectedFile();
						String inputfile = fileToOpen.getAbsolutePath();
						tf1.setText(inputfile);
						label3.setText("");
					}
				} else {
				     // <Add action statements here.>
				     String inputfile = tf1.getText();
				     if (inputfile.endsWith(".xls")) {
						 int n = inputfile.lastIndexOf(".");
						 String outputfile = inputfile.substring(0, n) + ".xlsx";
						 XLS2XLSX.run(inputfile, outputfile);
						 label3.setText(outputfile + " generated");
					 } else {
						 int n = inputfile.lastIndexOf(".");
						 String outputfile = inputfile.substring(0, n) + ".xls";
						 try {
						 	XLSX2XLS.run(inputfile, outputfile);
						 	label3.setText(outputfile + " generated");
						 } catch (Exception ex) {
							ex.printStackTrace();
							label3.setText("EROR: something went wrong -- " + outputfile + " generation failed.");
						 }
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
        panel.add(label3, gbc);
        frame.add(panel);
        // Make the frame visible
        frame.setVisible(true);
    }

}
