package gov.nih.nci.evs.restapi.util;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.*;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * <!-- LICENSE_TEXT_START -->
 * Copyright 2022 Guidehouse. This software was developed in conjunction
 * with the National Cancer Institute, and so to the extent government
 * employees are co-authors, any rights in such works shall be subject
 * to Title 17 of the United States Code, section 105.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the disclaimer of Article 3,
 *      below. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *   2. The end-user documentation included with the redistribution,
 *      if any, must include the following acknowledgment:
 *      "This product includes software developed by Guidehouse and the National
 *      Cancer Institute."   If no such end-user documentation is to be
 *      included, this acknowledgment shall appear in the software itself,
 *      wherever such third-party acknowledgments normally appear.
 *   3. The names "The National Cancer Institute", "NCI" and "Guidehouse" must
 *      not be used to endorse or promote products derived from this software.
 *   4. This license does not authorize the incorporation of this software
 *      into any third party proprietary programs. This license does not
 *      authorize the recipient to use any trademarks owned by either NCI
 *      or GUIDEHOUSE
 *   5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED
 *      WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *      OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE
 *      DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
 *      GUIDEHOUSE, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT,
 *      INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 *      BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *      LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *      CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 *      LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 *      ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *      POSSIBILITY OF SUCH DAMAGE.
 * <!-- LICENSE_TEXT_END -->
 */

/**
 * @author EVS Team
 * @version 1.0
 *
 * Modification history:
 *     Initial implementation kim.ong@nih.gov
 *
 */
public class HierarchyViewer {

	public static void run(String datafile) {
		JFrame frame = new TreeFrame(datafile);
		frame.show();
	}

	public static void run(String[] args) {
		String datafile = args[0];
		JFrame frame = new TreeFrame(datafile);
		frame.show();
	}

	public static void main(String[] args) {
		String datafile = args[0];
		JFrame frame = new TreeFrame(datafile);
		frame.show();
	}
}

class TreeFrame extends JFrame implements ActionListener {
	HashMap code2NodeMap = new HashMap();
    HierarchyHelper hh = null;

	public DefaultMutableTreeNode getNode(String code) {
		if (code2NodeMap.containsKey(code)) {
			return (DefaultMutableTreeNode) code2NodeMap.get(code);
		}
		String label = hh.getLabel(code);
		String display = label + " (" + code + ")";
		DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(display);
		code2NodeMap.put(code, newNode);
		return newNode;
	}

	public TreeFrame(String datafile) {
        String title = "NCI Thesaurus";

		Vector parent_child_vec = Utils.readFile(datafile);
		hh = new HierarchyHelper(parent_child_vec);
		Vector roots = hh.getRoots();
		if (roots.size() == 1) {
			String root = (String) roots.elementAt(0);
			title = hh.getLabel(root);
		}
		setTitle(title + " Hierarchy");
		setSize(2400, 1200);
		addWindowListener(new WindowAdapter() {
		  public void windowClosing(WindowEvent e) {
			System.exit(0);
		  }
		});

		TreeNode root = createTree(title + " Hierarchy");
		model = new DefaultTreeModel(root);
		tree = new JTree(model);

		tree.setEditable(false);
		Container contentPane = getContentPane();
		JScrollPane scrollPane = new JScrollPane(tree);
        contentPane.add(scrollPane, "Center");
	}

	public Vector sortByLabel(Vector codes) {
		HashMap label2CodeMap = new HashMap();
		Vector w = new Vector();
		Vector labels = new Vector();
		for (int i=0; i<codes.size(); i++) {
			String code = (String) codes.elementAt(i);
			String label = (String) hh.getLabel(code);
			labels.add(label);
		    label2CodeMap.put(label, code);
		}
		labels = new SortUtils().quickSort(labels);
		for (int i=0; i<labels.size(); i++) {
			int j = labels.size()-i-1;
			String label = (String) labels.elementAt(j);
			String code = (String) label2CodeMap.get(label);
			w.add(code);
		}
		return w;
	}

	public TreeNode createTree(String title) {
		String display = title;
		DefaultMutableTreeNode super_root = new DefaultMutableTreeNode(display);
		code2NodeMap.put("root", super_root);

		Stack stack = new Stack();
		Vector codes = hh.getRoots();
		codes = sortByLabel(codes);
		for (int i=0; i<codes.size(); i++) {
			int k = codes.size()-i-1;
			String code = (String) codes.elementAt(k);
			DefaultMutableTreeNode root = getNode(code);
			super_root.add(root);
    		Vector subs = hh.getSubclassCodes(code);
			if (subs != null) {
				subs = sortByLabel(subs);
				for (int j=0; j<subs.size(); j++) {
					String sub = (String) subs.elementAt(j);
					stack.push(code + "|" + sub);
				}
		    }
		}

		while (!stack.isEmpty()) {
            String line = (String) stack.pop();
            Vector u = StringUtils.parseData(line, '|');
            String sup = (String) u.elementAt(0);
            String sub = (String) u.elementAt(1);
            DefaultMutableTreeNode supNode = getNode(sup);
            DefaultMutableTreeNode subNode = getNode(sub);
            supNode.add(subNode);

			Vector subs = hh.getSubclassCodes(sub);
			if (subs != null) {
				subs = sortByLabel(subs);
				for (int j=0; j<subs.size(); j++) {
					String code = (String) subs.elementAt(j);
					stack.push(sub + "|" + code);
				}
		    }
		}
		return super_root;
	}

	public void actionPerformed(ActionEvent event) {
		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree
			.getLastSelectedPathComponent();

		if (selectedNode == null)
		  return;

		if (event.getSource().equals(deleteButton)) {
		  if (selectedNode.getParent() != null)
			model.removeNodeFromParent(selectedNode);
		  return;
		}

		DefaultMutableTreeNode newNode = new DefaultMutableTreeNode("New");

		if (event.getSource().equals(addSiblingButton)) {
		  DefaultMutableTreeNode parent = (DefaultMutableTreeNode) selectedNode
			  .getParent();

		  if (parent != null) {
			int selectedIndex = parent.getIndex(selectedNode);
			model.insertNodeInto(newNode, parent, selectedIndex + 1);
		  }
		} else if (event.getSource().equals(addChildButton)) {
		  model.insertNodeInto(newNode, selectedNode, selectedNode
			  .getChildCount());
		}

		TreeNode[] nodes = model.getPathToRoot(newNode);
		TreePath path = new TreePath(nodes);
		tree.scrollPathToVisible(path);
	}

	private DefaultTreeModel model;

	private JTree tree;

	private JButton addSiblingButton;

	private JButton addChildButton;

	private JButton deleteButton;

	private JButton editButton;
}
