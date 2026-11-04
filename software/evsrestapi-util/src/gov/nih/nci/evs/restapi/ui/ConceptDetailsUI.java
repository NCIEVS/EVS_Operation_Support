package gov.nih.nci.evs.restapi.ui;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.config.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.text.*;

public class ConceptDetailsUI {

	public static void generate(String terminology, String code) {
		ConceptDetailsPageGenerator.run(terminology, code);
	}

    public static void run(String jsonfile, String htmlfile) {
		Vector w = Utils.readFile(jsonfile);
		String json = (String) w.elementAt(0);
		JSONViewerTableGenerator.run(htmlfile, json);
	}

    public static void main(String[] args) {
        // Create the main frame
        JFrame frame = new JFrame("JSON Viewer Page Generation UI");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 200);
        frame.setLayout(new FlowLayout());
		JLabel statusLabel = new JLabel("Status:");
		JLabel status = new JLabel("");

///////////////////////////////////////////////////////////////////////////////
        JLabel label1 = new JLabel("Terminology:");
		JTextField terminologyTF = new JTextField();
		terminologyTF.setPreferredSize(new Dimension(600, 30));
		terminologyTF.setText("ncit");

        String[] terminologies = new String[]{"ncit"};
        JComboBox<String> terminologyCombo = new JComboBox<>(terminologies);

        terminologyCombo.setSelectedIndex(0);

        JLabel label2 = new JLabel("Code:");
		JTextField codeTF = new JTextField();
		codeTF.setPreferredSize(new Dimension(600, 30));
		codeTF.setText("C2394");

        JButton generateBtn = new JButton("Generate");
        generateBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				status.setText("");
				if (s.equals("Generate")) {
					String terminology = (String) terminologyCombo.getSelectedItem();
					String code = codeTF.getText();
					if (code.compareTo("") == 0) {
						status.setText("Please enter a concept code.");
					}
					try {
						generate(terminology, code);
						String jsonfile = "cd_" + code + ".json";
						String htmlfile = code + "_json.html";
						run(jsonfile, htmlfile);
						status.setText(htmlfile + " generated.");
					} catch (Exception ex) {
						status.setText("Error generating output files.");
					}
				}
			}
        });

        JPanel panel = new JPanel();

        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;

        //////////////////////////////////

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(label1, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(terminologyCombo, gbc);

        ///////////////////////////////////
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label2, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(codeTF, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(generateBtn, gbc);

        ///////////////////////////////////
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(statusLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(status, gbc);

        /////////////////////////////////////////////////////////////////////
        frame.add(panel);
        // Make the frame visible
        frame.setVisible(true);
    }
}

