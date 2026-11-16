package gov.nih.nci.evs.restapi.io;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.config.*;
import java.io.File;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

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

    public static JTree ceateHierarchyTree(String rootCode) {
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

    public static void main(String[] args) {
        JFrame frame = new JFrame("Directory Tree");
        JScrollPane scrollPane = createFileScrollPane();
        frame.add(scrollPane);
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

/*
    public static void main(String[] args) {
        JFrame frame = new JFrame("Tree");
        String root = args[0];
        JScrollPane scrollPane = ceateHierarchyScrollPane(root);
        frame.add(scrollPane);
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
*/
}
