package gov.nih.nci.evs.restapi.ui;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.config.*;
import java.io.File;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import java.awt.*;
import java.awt.event.*;
import javax.swing.text.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class SwingComponents {
	static String PARENT_CHILD_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile; // "parent_child.txt";
    public static HierarchyHelper hh = null;

    static {
		hh = new HierarchyHelper(Utils.readFile(PARENT_CHILD_FILE));
	}

	public static void setHierarchyHelper(HierarchyHelper hh) {
		hh = hh;
	}

    private static void createFileNodes(DefaultMutableTreeNode node, File file) {
        for (File child : file.listFiles()) {
            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child.getName());
            node.add(childNode);
            if (child.isDirectory()) {
                createFileNodes(childNode, child);
            }
        }
    }

    public static JTree createFileTree(File f) {
		String filename = f.getName();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(filename);
        createFileNodes(root, f);
        JTree tree = new JTree(root);
        return tree;
	}

    public static JScrollPane createFileScrollPane(File f) {
		String filename = f.getName();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(filename);
        createFileNodes(root, f);
        JTree tree = new JTree(root);
        JScrollPane scrollPane = new JScrollPane(tree);
		return scrollPane;
	}

    public static JScrollPane createFileScrollPane() {
		File f = new File(System.getProperty("user.dir"));
		String filename = f.getName();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(filename);
        createFileNodes(root, new File(System.getProperty("user.dir")));
        JTree tree = new JTree(root);
        JScrollPane scrollPane = new JScrollPane(tree);
		return scrollPane;
	}

    private static DefaultMutableTreeNode createTreeNode(String code) {
		DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode();
		String label = hh.getLabel(code);
		dmtn.setUserObject(label + " (" + code + ")");
		Vector subs = hh.getSubclassCodes(code);
		if (subs != null) {
			for (int i=0; i<subs.size(); i++) {
				String sub = (String) subs.elementAt(i);
                DefaultMutableTreeNode child = createTreeNode(sub);
				dmtn.add(child);
			}
		}
		return dmtn;
	}

    public static JTree createHierarchyTree() {
		DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode();
		String label = "NCI Thesaurus";
		dmtn.setUserObject(label);
		Vector roots = hh.getRoots();
		Vector v = new Vector();
		for (int i=0; i<roots.size(); i++) {
			String root = (String) roots.elementAt(i);
			v.add(hh.getLabel(root) + "|" + root);
		}
		v = new SortUtils().quickSort(v);

		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			DefaultMutableTreeNode childNode = createTreeNode((String) u.elementAt(1));
			dmtn.add(childNode);
		}
        JTree tree = new JTree(dmtn);
		return tree;
	}

    public static JTree createHierarchyTree(String rootCode) {
		DefaultMutableTreeNode root = createTreeNode(rootCode);
        JTree tree = new JTree(root);
		return tree;
	}

    public static JScrollPane ceateHierarchyScrollPane(String rootCode) {
		DefaultMutableTreeNode root = createTreeNode(rootCode);
        JTree tree = new JTree(root);
        JScrollPane scrollPane = new JScrollPane(tree);
		return scrollPane;
	}

    public static void addTreeSelectionListener(JTree tree, Object component) {
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
				if (node != null) {
					if (component instanceof JTextField) {
						JTextField tf = (JTextField) component;
						tf.setText((String) node.getUserObject());
					} else if (component instanceof JLabel) {
						JLabel label = (JLabel) component;
						label.setText((String) node.getUserObject());
					}
					System.out.println("Node selected: " + node.getUserObject());
				}
			}
		});
	}

	public static JTextPane createJTextPane(int x_dim, int y_dim, int fontSize, Color color) {
		JTextPane textPane = new JTextPane();
        textPane.setPreferredSize(new Dimension(x_dim, y_dim));
        SimpleAttributeSet boldAttr = new SimpleAttributeSet();
        StyleConstants.setBold(boldAttr, true);
        StyleConstants.setFontSize(boldAttr, fontSize);
        StyleConstants.setForeground(boldAttr, color);
        textPane.setCharacterAttributes(boldAttr, false);
        textPane.setText("");

        SimpleAttributeSet normalAttr = new SimpleAttributeSet();
        StyleConstants.setBold(normalAttr, false);
        textPane.setCharacterAttributes(normalAttr, false);
        return textPane;
	}

    public static JScrollPane createJScrollPane4JTextPane(JTextPane textPane) {
		JScrollPane scrollPane = new JScrollPane(textPane,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		return scrollPane;
	}

	public static JButton createFileChooserButton(String title, Vector extensionFilters, JTextField tf1) {
        JButton btn1 = new JButton("Browse");
        btn1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Browse")) {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setDialogTitle(title);
					File defaultDirectory = new File(System.getProperty("user.dir"));
					fileChooser.setCurrentDirectory(defaultDirectory);

                    if (extensionFilters != null) {
						for (int i=0; i<extensionFilters.size(); i++) {
							String t = (String) extensionFilters.elementAt(i);
							Vector u = StringUtils.parseData(t, '|');
							fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(
								(String) u.elementAt(0),
								(String) u.elementAt(1)
								));
						}
					}

					int userSelection = fileChooser.showOpenDialog(null);
					if (userSelection == JFileChooser.APPROVE_OPTION) {
						File fileToOpen = fileChooser.getSelectedFile();
						String inputfile = fileToOpen.getAbsolutePath();
						tf1.setText(inputfile);
					}
				}
			}
        });
        return btn1;
	}

	public static void gridBagConstraintsLayout(GridBagConstraints gbc, int row,
	    int numCols, Vector<JComponent> components, JPanel panel) {
		for (int i=0; i<numCols; i++) {
	        gbc.gridx = i;
	        gbc.gridy = row;
	        gbc.insets = new Insets(3, 3, 3, 3);
	        panel.add(components.elementAt(i), gbc);
		}
	}

	public static void gridBagConstraintsLayout(GridBagConstraints gbc, int row,
	    int[] cols, Vector<JComponent> components, JPanel panel) {
		for (int i=0; i<cols.length; i++) {
	        gbc.gridx = cols[i];
	        gbc.gridy = row;
	        gbc.insets = new Insets(3, 3, 3, 3);
	        panel.add(components.elementAt(i), gbc);
		}
	}

	public static JPanel gridBagConstraintsLayout(GridBagConstraints gbc, int[] rows, int[] cols,
	    Vector<JComponent> components) {
		JPanel panel = new JPanel();
		for (int i=0; i<components.size(); i++) {
	        gbc.gridx = cols[i];
	        gbc.gridy = rows[i];
	        gbc.insets = new Insets(3, 3, 3, 3);
	        panel.add(components.elementAt(i), gbc);
		}
		return panel;
	}

	public static JPanel boxHorizontalLayout(Vector<JComponent> components) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		//verticalPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		for (int i=0; i<components.size(); i++) {
			Component c = (Component) components.elementAt(i);
			panel.add(c);
			panel.add(Box.createRigidArea(new Dimension(0, 5)));
		}
        return panel;
	}

	public static JPanel boxVerticalLayout(Vector<JComponent> components) {
		JPanel verticalPanel = new JPanel();
		verticalPanel.setLayout(new BoxLayout(verticalPanel, BoxLayout.Y_AXIS));
		for (int i=0; i<components.size(); i++) {
			Component c = (Component) components.elementAt(i);
			verticalPanel.add(c);
			verticalPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		}
        return verticalPanel;
	}

	public static JTextPane createJTextPane(int dim_x, int dim_y) {
        JTextPane jt = new JTextPane();
        jt.setPreferredSize(new Dimension(dim_y, dim_x));
        return jt;
	}

    public static SimpleAttributeSet createSimpleAttributeSet(boolean bold, int size, Color color) {
        SimpleAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setBold(attr, bold);
        StyleConstants.setFontSize(attr, size);
        StyleConstants.setForeground(attr, color);
        return attr;
	}

	public static JScrollPane createJScrollPane(JTextPane jt, int fontSize, Color color) {
        StyledDocument doc = jt.getStyledDocument();
		SimpleAttributeSet attrs = createSimpleAttributeSet(false, fontSize, color);
		StyleConstants.setLineSpacing(attrs, 0);
		StyleConstants.setSpaceAbove(attrs, 0);
		StyleConstants.setSpaceBelow(attrs, 0);
		doc.setParagraphAttributes(0, doc.getLength(), attrs, false);
        JScrollPane scrollPane = new JScrollPane(jt);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        return scrollPane;
	}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Tree");
        String root = args[0];
        JScrollPane scrollPane = ceateHierarchyScrollPane(root);
        frame.add(scrollPane);
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

}
