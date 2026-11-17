package gov.nih.nci.evs.restapi.ui;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.config.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.text.*;
import javax.swing.tree.*;

public class NCIThesaurus {

	public static void generate(String terminology, String code) {
		ConceptDetailsPageGenerator.run(terminology, code);
	}

    public static void run(String jsonfile, String htmlfile) {
		Vector w = Utils.readFile(jsonfile);
		String json = (String) w.elementAt(0);
		JSONViewerTableGenerator.run(htmlfile, json);
	}

    private static DefaultMutableTreeNode searchNode(DefaultMutableTreeNode root, String nodeName) {
        @SuppressWarnings("unchecked")
        //Enumeration<DefaultMutableTreeNode> e = root.depthFirstEnumeration();
        Enumeration<?> e = root.depthFirstEnumeration();
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
            if (nodeName.equalsIgnoreCase(node.toString())) {
                return node;
            }
        }
        return null; // Not found
    }

    public static void main(String[] args) {
        // Create the main frame
        JFrame frame = new JFrame("NCItThesaurus UI");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 200);
        frame.setLayout(new FlowLayout());
        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel label4 = new JLabel("Status");
        JLabel label5 = new JLabel("");

        JLabel label1 = new JLabel("NCI Thesaurus");
        JTree tree = SwingComponents.createHierarchyTree();

        // Get the root node from the JTree
        TreeModel model = tree.getModel();
        Object rootNode = model.getRoot();
        final DefaultMutableTreeNode dmtnRoot = (DefaultMutableTreeNode) rootNode;

        JLabel label2 = new JLabel("Selected Concept");
        JTextField tf1 = new JTextField();
        tf1.setPreferredSize(new Dimension(600, 30));
        JButton Btn1 = new JButton("View");
        Btn1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				label5.setText("");
				if (s.equals("View")) {
					String terminology = "ncit";
					String displayName = tf1.getText();
					if (displayName.length() == 0) {
						label5.setText("Please select a concept from the tree.");
					} else {
						int n = displayName.lastIndexOf("(");
						String code = displayName.substring(n+1, displayName.length()-1);
						try {
							generate(terminology, code);
                            File f = new File(System.getProperty("user.dir"));
                            String htmlfile = f.getAbsolutePath() + File.separator + code + "_cd.html";
 							BrowserLauncher.launch(htmlfile);
							label5.setText(htmlfile + " generated.");
						} catch (Exception ex) {
							label5.setText("Error generating output files.");
						}
					}
				}
			}
        });

        JLabel label3 = new JLabel("Target");
        JTextField tf2 = new JTextField();
        tf2.setPreferredSize(new Dimension(600, 30));
        JButton Btn2 = new JButton("Search");
        Btn2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				label5.setText("");
				if (s.equals("Search")) {
					DefaultMutableTreeNode root = dmtnRoot;
					String nodeName = tf2.getText();
					DefaultMutableTreeNode foundNode = searchNode(root, nodeName);
					if (foundNode != null) {
						// Get path and expand
						TreePath path = new TreePath(foundNode.getPath());
						tree.expandPath(path);
						tree.setSelectionPath(path);
						tree.scrollPathToVisible(path);
						System.out.println("Found and expanded: " + nodeName);
					} else {
						System.out.println("Node not found: " + nodeName);
					}
				}
			}
        });

        SwingComponents.addTreeSelectionListener(tree, tf1);
        JScrollPane scrollPane = new JScrollPane(tree);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label1, gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(scrollPane, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label2, gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(tf1, gbc);
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(Btn1, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label3, gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(tf2, gbc);
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(Btn2, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label4, gbc);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label5, gbc);
        frame.add(panel);
        // Make the frame visible
        frame.setVisible(true);
    }
}
