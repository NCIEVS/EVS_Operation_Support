package gov.nih.nci.evs.restapi.ui;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.config.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.text.*;

public class Delimited2HTMLUI {

	private final static String newline = "\n";

	public static boolean isPasswordCorrect(char[] input) {
		String str = new String(input);
		if (str.compareTo(ConfigurationController.password)== 0) return true;
		return false;
	}

    public static String run(String serviceUrl, String named_graph, String username, String password, String inputfile) {
		String outputfile = null;
		if (named_graph == null || named_graph.length() == 0) {
			outputfile = new HTMLTableDataConverter().convert(inputfile);
		} else {
			outputfile = new HTMLTableDataConverter(serviceUrl, named_graph, username, password).convert(inputfile);
		}
		System.out.println(outputfile + " generated.");
		Vector v = Utils.readFile(outputfile);
		outputfile = HTMLTable.generate(v);
		return outputfile;
	}


    public static void main(String[] args) {
        // Create the main frame
        JFrame frame = new JFrame("Delimited File to HTML UI");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 200);
        frame.setLayout(new FlowLayout());

        JLabel statusLabel = new JLabel("Status:");
        JLabel status = new JLabel("");

        JLabel label1 = new JLabel("Service URL:");

		Vector serviceUrl_vec = ConfigurationController.serviceUrl_vec;
		Utils.dumpVector("serviceUrl_vec", serviceUrl_vec);

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

        JLabel label2 = new JLabel("Named Graph:");
		JTextField namedGraphTF = new JTextField();
		namedGraphTF.setPreferredSize(new Dimension(600, 30));
		namedGraphTF.setText(ConfigurationController.namedGraph);

        JLabel label3 = new JLabel("User Name:");
		JTextField usernameTF = new JTextField();
		usernameTF.setPreferredSize(new Dimension(600, 30));

        JLabel label4 = new JLabel("Password:");
		JPasswordField passwordTF = new JPasswordField(10);
		passwordTF.setPreferredSize(new Dimension(600, 30));

        JLabel label5 = new JLabel("Delimited File:");
		JTextField delimitedFileTF = new JTextField();
		delimitedFileTF.setPreferredSize(new Dimension(600, 30));
		delimitedFileTF.setEditable(true);
		delimitedFileTF.setText("");

        JButton browseBtn = new JButton("Browse");
        browseBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Browse")) {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setDialogTitle("Select Hierarchy Data File");
					File defaultDirectory = new File(System.getProperty("user.dir"));
					fileChooser.setCurrentDirectory(defaultDirectory);

					int userSelection = fileChooser.showOpenDialog(null);
					if (userSelection == JFileChooser.APPROVE_OPTION) {
						File fileToOpen = fileChooser.getSelectedFile();
						String inputfile = fileToOpen.getAbsolutePath();
						delimitedFileTF.setText(inputfile);
					}
				}
			}
        });

        JButton generateBtn = new JButton("Generate");
        generateBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Generate")) {
					status.setText("");
					String serviceUrl = (String) urlList.getSelectedItem();
					String namedGraph = namedGraphTF.getText();
					String username = usernameTF.getText();
					String password = passwordTF.getText();

                    String inputfile = delimitedFileTF.getText();
					if (inputfile.compareTo("") == 0) {
						status.setText("Please specify an input file.");
						return;
					}
					boolean cont = true;
					if (usernameTF.getText().length() > 0 && passwordTF.getText().length() > 0) {
						char[] input = passwordTF.getPassword();
						String str = new String(input);
						if (!isPasswordCorrect(input)) {
							cont = false;
						}
					}
					if (cont) {
						try {
							String outputfile = run(serviceUrl, namedGraph, username, password, inputfile);
							status.setText(outputfile + " generated.");
						} catch (Exception ex) {
							ex.printStackTrace();
							status.setText("Error generating output file");
						}
					}
				}
			}
        });

        JButton resetBtn = new JButton("Reset");
        resetBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Reset")) {
					status.setText("");
					namedGraphTF.setText(ConfigurationController.namedGraph);
					usernameTF.setText("");
					passwordTF.setText("");
					delimitedFileTF.setText("");
				}
			}
        });

        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;

        ////////////////Service URL//////////////////

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(label1, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(urlList, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(resetBtn, gbc);

        ////////////////Named Graph//////////////////

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(label2, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(namedGraphTF, gbc);

       ////////////////User Name//////////////////

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(label3, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(usernameTF, gbc);

       ////////////////Password//////////////////

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(label4, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(passwordTF, gbc);

        /////////////////Delimitd File//////////////////////////

        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(label5, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(delimitedFileTF, gbc);

        gbc.gridx = 2;
        gbc.gridy = 4;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(browseBtn, gbc);

        /////////////////////////////////////////////////////////////////////
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(generateBtn, gbc);

        /////////////////////////////////////////////////////////////////////
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(statusLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(status, gbc);

        /////////////////////////////////////////////////////////////////////
        frame.add(panel);
        // Make the frame visible
        frame.setVisible(true);
    }
}

