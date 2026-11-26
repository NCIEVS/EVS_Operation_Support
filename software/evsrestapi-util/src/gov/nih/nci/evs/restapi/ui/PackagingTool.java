package gov.nih.nci.evs.restapi.ui;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.config.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.text.*;

public class PackagingTool {

    public static void main(String[] args) {
        // Create the main frame
        JFrame frame = new JFrame("PackagingTool UI");
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

        JLabel label1 = new JLabel("Root Directory");
        JTextField tf1 = new JTextField();
        tf1.setPreferredSize(new Dimension(600, 30));

        JLabel label2 = new JLabel("Name a Zip File (.zip)");
        JTextField tf2 = new JTextField();
        tf2.setPreferredSize(new Dimension(600, 30));

        JButton btn1 = new JButton("Browse");
        btn1.addActionListener(new ActionListener() {
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
						tf1.setText(inputfile);

						String zipfilename = fileToOpen.getName() + ".zip";
						zipfilename = zipfilename.replace(" ", "_");
						tf2.setText(zipfilename);
					}
				}
			}
        });

        JButton btn2 = new JButton("Zip");
        btn2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Zip")) {
					label4.setText("");
					try {
						String zipfile = tf2.getText();
						String rootDir = tf1.getText();
						ZipUtils.zipFile(rootDir, zipfile);
						label4.setText("Files in the " + rootDir + " zipped to " + zipfile + ".");

JOptionPane.showMessageDialog(null, "Right click the zip file and select Extract All to unpack.", "Information", JOptionPane.INFORMATION_MESSAGE);

					} catch (Exception ex) {
						label4.setText("Something goes wrong - zip unsuccessful.");
					}
				}
			}
        });

        JButton btn4 = new JButton("Clear");
        btn4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Clear")) {
					label4.setText("");
					//tf2.setText("");
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
        panel.add(label4, gbc);
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btn4, gbc);
        frame.add(panel);
        // Make the frame visible
        frame.setVisible(true);
    }
}
//Note: Right click the zip file and select 'Extract All ...' to unpack.