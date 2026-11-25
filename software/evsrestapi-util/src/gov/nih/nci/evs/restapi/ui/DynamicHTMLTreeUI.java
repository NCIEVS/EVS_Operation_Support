package gov.nih.nci.evs.restapi.ui;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.config.*;
import gov.nih.nci.evs.restapi.bean.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.text.*;

public class DynamicHTMLTreeUI {

    public static void generateDynamicHTMLTree(String parent_child_file, String htmlfile) {
		Vector parent_child_vec = Utils.readFile(parent_child_file);
        TreeItem ti = new ASCIITreeUtils().createTreeItem(parent_child_vec);
        HashMap hmap = new HashMap();
        hmap.put("<Root>", ti);
        String url = "https://evsexplore.semantics.cancer.gov/evsexplore/concept/ncit/";
        gov.nih.nci.evs.restapi.util.SimpleTreeUtils tree = new gov.nih.nci.evs.restapi.util.SimpleTreeUtils();
        tree.setBasePath("web/");
        tree.writeTree2HTML(hmap, url, htmlfile);
    }

    public static void main(String[] args) {
        // Create the main frame
        JFrame frame = new JFrame("DynamicHTMLTree UI");
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

        JLabel label1 = new JLabel("Hierarchy File");
        JTextField tf1 = new JTextField();
        tf1.setPreferredSize(new Dimension(600, 30));
        String title = "Select Axiom File";
        Vector extensionFilters = new Vector();
        extensionFilters.add("OWL|owl");
        extensionFilters.add("XML|xml");
        extensionFilters.add("RDF|rdf");
        extensionFilters.add("Text|txt");
	    JButton btn1 = SwingComponents.createFileChooserButton(title, extensionFilters, tf1);

        JLabel label2 = new JLabel("Root");
        JTextField tf2 = new JTextField();
        tf2.setPreferredSize(new Dimension(600, 30));
        JButton btn2 = new JButton("Generate");
        btn2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Generate")) {
				     // <Add action statements here.>
				     HierarchyHelper hh = new HierarchyHelper(Utils.readFile(tf1.getText()));
				     String parent_child_file = tf2.getText() + ".txt";
				     Vector v = hh.get_transitive_closure_v4(tf2.getText());
				     Utils.saveToFile(parent_child_file, v);
				     String htmlfile = tf2.getText() + ".html";
				     generateDynamicHTMLTree(parent_child_file, htmlfile);
				     label4.setText(htmlfile + " generated.");
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
        frame.add(panel);
        // Make the frame visible
        frame.setVisible(true);
    }
}
