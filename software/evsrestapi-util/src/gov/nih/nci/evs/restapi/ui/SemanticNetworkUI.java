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

public class SemanticNetworkUI {

	public static boolean download() {
		try {
			//To be implemented:
			//https://www.nlm.nih.gov/research/umls/licensedcontent/umlsarchives04.html
			String link = "https://web/20241127183822/https://lhncbc.nlm.nih.gov/semanticnetwork/download/sn_archive/2023AA.tgz";
			link = "https://lhncbc.nlm.nih.gov/semanticnetwork/download/sn_archive/2023AA.tgz";
			File file = new File("2023AA.tgz");
			DownloadPage.download(link, file);
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

    public static void main(String[] args) {
        // Create the main frame
        JFrame frame = new JFrame("Semantic Network UI");
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

        JLabel label1 = new JLabel("Download");
        JTextField tf1 = new JTextField();

        tf1.setPreferredSize(new Dimension(600, 30));
        tf1.setText("2023AA");

        JButton btn1 = new JButton("Browse");
        btn1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Browse")) {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); // Allow directories only
					File defaultDirectory = new File(System.getProperty("user.dir"));
					fileChooser.setCurrentDirectory(defaultDirectory);

					fileChooser.setDialogTitle("Select Target Directory");
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
        JButton btn2 = new JButton("Download");
        btn2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
 				 if (s.equals("Download")) {
					 File directory = new File(tf1.getText());
					 if (directory.mkdirs()) {
						 System.out.println("Directory created successfully!");
					 } else {
						 System.out.println("Failed to create directory or it already exists.");
					 }
					 boolean bool = download();
					 if (bool) {
						 label4.setText("Download successful.");
					 } else {
						 label4.setText("Download failed.");
					 }
 				 }
			}
        });

        JLabel label2 = new JLabel("Hierarchy");
        JTextField tf2 = new JTextField();
        tf2.setPreferredSize(new Dimension(600, 30));
        JButton btn3 = new JButton("Browse");
        btn3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Browse")) {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setDialogTitle("Select File");
					int userSelection = fileChooser.showOpenDialog(null);
					if (userSelection == JFileChooser.APPROVE_OPTION) {
						File fileToOpen = fileChooser.getSelectedFile();
						String inputfile = fileToOpen.getAbsolutePath();
					}
				} else {
				     // <Add action statements here.>
				}
			}
        });
        JButton btn4 = new JButton("View");
        btn4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("View")) {
					String hierdatafile = tf2.getText();
					JDialog treedialog = new TreeDialog(hierdatafile, "Semantic Network");
					treedialog.setSize(400, 300);
					treedialog.setVisible(true);
				}
			}
        });

        JButton btn5 = new JButton("Execute");
        btn5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Execute")) {
					String dir = tf1.getText();
					try {
						SemanticNetwork.run(dir);
						label4.setText("Files generated: semantic_network_parent_child.txt, STY_2023AA.txt, sty_tree.txt");
						tf2.setText("semantic_network_parent_child.txt");

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
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btn2, gbc);
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btn5, gbc);

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
        panel.add(btn3, gbc);
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btn4, gbc);

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
