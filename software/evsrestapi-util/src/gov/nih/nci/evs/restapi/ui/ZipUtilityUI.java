package gov.nih.nci.evs.restapi.ui;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.config.*;

import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.text.*;

public class ZipUtilityUI {

    public static void main(String[] args) {
        // Create the main frame
        JFrame frame = new JFrame("Zip Utility UI");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 200);
        frame.setLayout(new FlowLayout());
        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel label3 = new JLabel("Status");
        JLabel label4 = new JLabel("");

        JLabel label1 = new JLabel("Zip File");
        JTextField tf1 = new JTextField();
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

					fileChooser.setDialogTitle("Select an image");
					fileChooser.setAcceptAllFileFilterUsed(false);
					FileNameExtensionFilter filter = new FileNameExtensionFilter("Zipped File", "zip");
					fileChooser.addChoosableFileFilter(filter);

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
        JLabel label2 = new JLabel("Destination Folder");
        JTextField tf2 = new JTextField();
        tf2.setPreferredSize(new Dimension(600, 30));
        JButton btn2 = new JButton("Browse");
        btn2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Browse")) {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					fileChooser.setDialogTitle("Select File");
					File defaultDirectory = new File(System.getProperty("user.dir"));
					fileChooser.setCurrentDirectory(defaultDirectory);
					int userSelection = fileChooser.showOpenDialog(null);
					if (userSelection == JFileChooser.APPROVE_OPTION) {
						File fileToOpen = fileChooser.getSelectedFile();
						String inputfile = fileToOpen.getAbsolutePath();
						tf2.setText(inputfile);
					}
				} else {
				     // <Add action statements here.>
				}
			}
        });
        JButton btn3 = new JButton("Unzip");
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
					}
				} else {
					label4.setText("");
					try {
						String zipfile = tf1.getText();
						String destDir = tf2.getText();
						ZipUtils.unzip(zipfile, destDir);
						int n = zipfile.lastIndexOf(File.separator);
						String foldername = zipfile.substring(n, zipfile.length());
						label4.setText(foldername + " unzipped to folder " + tf2.getText());
					} catch (Exception ex) {
						label4.setText("Something goes wrong - unzip unsuccessful.");
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
        panel.add(label2, gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(tf2, gbc);
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btn2, gbc);
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btn3, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label3, gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label4, gbc);
        frame.add(panel);
        // Make the frame visible
        frame.setVisible(true);
    }
}
