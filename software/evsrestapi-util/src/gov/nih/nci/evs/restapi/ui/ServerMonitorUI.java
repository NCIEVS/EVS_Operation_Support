package gov.nih.nci.evs.restapi.ui;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.config.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.text.*;

public class ServerMonitorUI {

    public static void main(String[] args) {
        // Create the main frame
        JFrame frame = new JFrame("Server Monitor UI");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 200);
        frame.setLayout(new FlowLayout());
        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;
        JButton btn2 = new JButton("Execute");

        JTextArea ta1 = new JTextArea();
		ta1.setEditable(false);
        JLabel label1 = new JLabel("Output File");
        JTextField tf1 = new JTextField();
        tf1.setPreferredSize(new Dimension(600, 30));

        tf1.setText(System.getProperty("user.dir") + File.separator + ServerMonitor.SERVER_STATUS);

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
				} else {
				     // <Add action statements here.>
				}
			}
        });

        JLabel labela = new JLabel("Service URLs");

		Vector serviceUrl_vec = ConfigurationController.serviceUrl_vec;
		String[] serviceUrls = new String[serviceUrl_vec.size()];
        for (int i=0; i<serviceUrl_vec.size(); i++) {
			String url = (String) serviceUrl_vec.elementAt(i);
			serviceUrls[i] = url;
		}

		JComboBox urlList = new JComboBox(serviceUrls);
		urlList.setSelectedIndex(4);
		urlList.addActionListener(new ActionListener() {
		  public void actionPerformed(ActionEvent e) {
              String serviceUrl = (String) urlList.getSelectedItem();
		  }
		});


        JLabel label3 = new JLabel("");

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
				     // <Add action statements here.>
				     label3.setText("Processing -- please wait...");
				     ServerMonitor.run(ta1);
				     label3.setText("Done");
				}
			}
        });

        JLabel label2 = new JLabel("Status");


		ta1.setLineWrap(true); // Wrap lines instead of horizontal scrolling
		ta1.setWrapStyleWord(true);
		// Create a JScrollPane around the JTextArea
		JScrollPane scrollPane = new JScrollPane(ta1,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setPreferredSize(new Dimension(0, 600)); // fixed height
		scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 600)); // no more than 50px tall

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
        panel.add(labela, gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(urlList, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label2, gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(scrollPane, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label3, gbc);

        frame.add(panel);
        // Make the frame visible
        frame.setVisible(true);
    }
}
