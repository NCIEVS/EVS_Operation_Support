package gov.nih.nci.evs.restapi.ui;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.config.*;
import java.io.File;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

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

    public static void main(String[] args) {
        JFrame frame = new JFrame("Tree");
        String root = args[0];
        JScrollPane scrollPane = ceateHierarchyScrollPane(root);
        frame.add(scrollPane);
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

/*
    public static void main(String[] args) {
        JFrame frame = new JFrame("Directory Tree");
        JScrollPane scrollPane = createFileScrollPane();
        frame.add(scrollPane);
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
*/

}
