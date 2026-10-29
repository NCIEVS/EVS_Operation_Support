package gov.nih.nci.evs.restapi.ui;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.config.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class HierarchyDataUI {

	static String PARENT_CHILD_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile; // "parent_child.txt";
    static HierarchyHelper hh = null;

    static {
		hh = new HierarchyHelper(Utils.readFile(PARENT_CHILD_FILE));
	}

	public static String getLabel(String code) {
		return (String) hh.getLabel(code);
	}

    public static void main(String[] args) {
        // Create the main frame
        JFrame frame = new JFrame("NCI Thesaurus Hierarchy Data");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new FlowLayout());

        JLabel label1 = new JLabel("Hierarchy Data: ");
        Vector v = Utils.readFile(PARENT_CHILD_FILE);
        Vector w0 = new Vector();
        w0.add("Parent Label|Parent Code|Child Label|Child Code");
        w0.addAll(v);
        Utils.saveToFile("hier.txt", w0);
        JTable tbl = TableDialog.createJTable("hier.txt");

		// Disable auto-resize to allow horizontal scroll
		tbl.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		// Wrap in scroll pane
		JScrollPane scrollPane = new JScrollPane(tbl);
		scrollPane.setPreferredSize(new Dimension(600, 300));
		scrollPane.setMinimumSize(new Dimension(600, 300));

        JLabel label2 = new JLabel("Root concept code: ");
        JTextField textField = new JTextField();
        JButton btn1 = new JButton("Generate");
        JLabel statusLabel = new JLabel("");

        btn1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Generate")) {
					String code = textField.getText();
					code = code.trim();
					if (code.length() == 0) {
						statusLabel.setText("Please specify a root concept code.");
					} else {
						if (hh.getLabel(code) == null) {
							statusLabel.setText(code + " not found.");
						} else {
							Vector parent_child_vec = hh.get_transitive_closure_v4(code);
							Utils.saveToFile(code + ".txt", parent_child_vec);
							System.out.println(code + ".txt" + " generated.");
							statusLabel.setText(code + ".txt" + " generated.");
							textField.setText("");
						}
					}
				}
			}
        });

        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);
        GridBagConstraints gbc = new GridBagConstraints();

        // Put constraints on different buttons
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(label1, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;

        panel.add(scrollPane, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(label2, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;


        panel.add(textField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(btn1, gbc);


        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(statusLabel, gbc);

        frame.add(panel);

        // Make the frame visible
        frame.setVisible(true);
    }
}
