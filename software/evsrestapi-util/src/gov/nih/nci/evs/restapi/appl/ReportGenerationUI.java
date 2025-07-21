package gov.nih.nci.evs.restapi.appl;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.config.*;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.ComponentOrientation;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import java.awt.GridLayout;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import java.io.*;
import java.util.*;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.Dimension;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.text.Element;
import javax.swing.event.*;

public class ReportGenerationUI {
	private JTree tree;
	private JLabel selectedLabel;

	static String HIER_FILE = "parent_child.txt";
	static String ROLE_FILE = "roles.txt";
	static String PROPERTY_FILE = "properties.txt";
	static String OBJECT_PROPERTY_FILE = "objectProperies.txt";
	static String SEMANTIC_TYPE_FILE = "P106.txt";
	static String NCIT_OWL_FILE = "ThesaurusInferred_forTS.owl";
	static String AXIOM_FILE = "axiom_ThesaurusInferred_forTS.txt";
	static String VS_FILE = "A8.txt";

	static String[] root_array = null;

	static HierarchyHelper hh = null;
	static Vector roots = null;
	static Vector displayLabels = null;
	static DefaultMutableTreeNode root = null;

	static String PRIMITIVE_CONCEPT_ROOTS = null;

	JFrame frame = null;
	private JPanel controlPanel;
	//JLabel axiomFileLabel;
	private JPanel controlPanel2;
	//JLabel conceptFileLabel;
	private JPanel controlPanel3;
	//JLabel dataFileLabel;
	private JPanel controlPanel4;
	JProgressBar pbar;
	private JPanel controlPanel5;
	JComboBox rootComboBox;
	JComboBox roleComboBox;
	JComboBox reportComboBox;
	private final static String newline = "\n";

	private JPanel controlPanel6;
	JTextArea textArea;

	JTextField tf = null;
	JTextField tf2 = null;
	JTextField tf3 = null;

	JLabel statusLabel = new JLabel("");
	JButton resetButton = new JButton("Reset");
	JButton resetButton2 = new JButton("Reset");
	//gov.nih.nci.evs.restapi.util.LexicalMatchRunner lmr = null;
	int numCompleted = 0;
	String outputfile = null;
	Vector semanticTypes = new Vector();
	Vector objectProperies = new Vector();


	OWLSPARQLUtils owlSPARQLUtils = null;
	String username = null;
	String password = null;
	String namedGraph = null;
	String prefixes = null;
	String serviceUrl = null;
	String[] object_properties = null;
	HashMap roleMap = null;
	HashMap roleCode2LabelMap = null;
	HashMap semanticTypeMap = null;

	boolean REPORT_GENERATION_MODE = true;
	OWLScanner owlscanner = null;
	Vector output_vec = null;
	private void createWindow() {
		frame = new JFrame("Report Generation");
		frame.setLayout(new GridLayout(10, 1));
		File f = new File(NCIT_OWL_FILE);
		if (!f.exists()) {
			System.out.println("WARNING: NCIT_OWL_FILE " + ConfigurationController.owlfile + " does not exist.");
		} else {
			System.out.println("INFO: NCIT_OWL_FILE " + ConfigurationController.owlfile + " exists.");
			owlscanner = new OWLScanner(NCIT_OWL_FILE);
		}

		frame.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent windowEvent){
			System.exit(0);
			}
		});

		serviceUrl = ConfigurationController.serviceUrl;
		namedGraph = ConfigurationController.namedGraph;
		username = ConfigurationController.username;
		password = ConfigurationController.password;
		PRIMITIVE_CONCEPT_ROOTS = ConfigurationController.primitiveConceptRoots;
/////////////////////////////////////////////////////////////////////////////////////////////////////////////

		JPanel ctrlPanel = new JPanel();
		ctrlPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;

		JLabel ncitOWLLabel = new JLabel("NCI Thesaurus OWL File: ", JLabel.LEFT);
		JTextField tf = new JTextField(80);
		tf.setText(NCIT_OWL_FILE);
		final JButton downloadButton = new JButton("Download");
		downloadButton.addActionListener(new ActionListener() {
		 public void actionPerformed(ActionEvent e) {
			 NCItDownload.download();
			 statusLabel.setText(NCIT_OWL_FILE + " dowloaded.");
		 }
		});

        int gridx = 0;
		c.gridx = gridx;
		c.gridy = 0;
		ctrlPanel.add(ncitOWLLabel, c);

		c.gridx = gridx;
		c.gridy = 1;
		ctrlPanel.add(tf, c);

		c.gridx = gridx;
		c.gridy = 2;
		ctrlPanel.add(downloadButton, c);
		gridx++;

//////////////////////////////////////////////////////////////////////////////////////

        JPanel controlPanel_2 = new JPanel();
		JLabel axiomLabel = new JLabel("Axiom File: ", JLabel.LEFT);
		JTextField tf2 = new JTextField(80);
		tf2.setText(AXIOM_FILE);
		final JButton generateAxiomButton = new JButton("Generate");
		generateAxiomButton.addActionListener(new ActionListener() {
		 public void actionPerformed(ActionEvent e) {
			 ReportSubmitter.generateAxiomFile();
			 statusLabel.setText(AXIOM_FILE + " generated.");
		 }
		});

		c.gridx = gridx;
		c.gridy = 0;
		ctrlPanel.add(axiomLabel, c);

		c.gridx = gridx;
		c.gridy = 1;
		ctrlPanel.add(tf2, c);

		c.gridx = gridx;
		c.gridy = 2;
		ctrlPanel.add(generateAxiomButton, c);
		gridx++;

//////////////////////////////////////////////////////////////////////////////////////
        JPanel controlPanel_3 = new JPanel();
		JLabel hierLabel = new JLabel("Hierarchy File: ", JLabel.LEFT);
		JTextField tf3 = new JTextField(80);
		tf3.setText(HIER_FILE);
		final JButton generateHierButton = new JButton("Generate");
		generateHierButton.addActionListener(new ActionListener() {
		 public void actionPerformed(ActionEvent e) {
			 ReportSubmitter.generateHierarchyFile();
			 statusLabel.setText(HIER_FILE + " generated.");
		 }
		});


		c.gridx = gridx;
		c.gridy = 0;
		ctrlPanel.add(hierLabel, c);

		c.gridx = gridx;
		c.gridy = 1;
		ctrlPanel.add(tf3, c);

		c.gridx = gridx;
		c.gridy = 2;
		ctrlPanel.add(generateHierButton, c);
		gridx++;

//////////////////////////////////////////////////////////////////////////////////////

        JPanel controlPanel_4 = new JPanel();
		JLabel roleLabel = new JLabel("Role/Restriction File: ", JLabel.LEFT);
		JTextField tf4 = new JTextField(80);
		tf4.setText(ROLE_FILE);
		final JButton generateRoleButton = new JButton("Generate");
		generateRoleButton.addActionListener(new ActionListener() {
		 public void actionPerformed(ActionEvent e) {
			 ReportSubmitter.generateRoleFile();
			 statusLabel.setText(ROLE_FILE + " generated.");
		 }
		});


		c.gridx = gridx;
		c.gridy = 0;
		ctrlPanel.add(roleLabel, c);

		c.gridx = gridx;
		c.gridy = 1;
		ctrlPanel.add(tf4, c);

		c.gridx = gridx;
		c.gridy = 2;
		ctrlPanel.add(generateRoleButton, c);
		gridx++;

//////////////////////////////////////////////////////////////////////////////////////
        JPanel controlPanel_5 = new JPanel();
		JLabel vsLabel = new JLabel("Value Set File: ", JLabel.LEFT);
		JTextField tf5 = new JTextField(80);
		tf5.setText(VS_FILE);
		final JButton generateVSButton = new JButton("Generate");
		generateVSButton.addActionListener(new ActionListener() {
		 public void actionPerformed(ActionEvent e) {
			 ReportSubmitter.generateValueSetFile();
			 statusLabel.setText(VS_FILE + " generated.");
		 }
		});


		c.gridx = gridx;
		c.gridy = 0;
		ctrlPanel.add(vsLabel, c);

		c.gridx = gridx;
		c.gridy = 1;
		ctrlPanel.add(tf5, c);

		c.gridx = gridx;
		c.gridy = 2;
		ctrlPanel.add(generateVSButton, c);
		gridx++;


//////////////////////////////////////////////////////////////////////////////////////
        JPanel controlPanel_6 = new JPanel();
		JLabel propertyLabel = new JLabel("Property File: ", JLabel.LEFT);
		JTextField tf6 = new JTextField(80);
		tf6.setText(PROPERTY_FILE);
		final JButton generatePropertyButton = new JButton("Generate");
		generatePropertyButton.addActionListener(new ActionListener() {
		 public void actionPerformed(ActionEvent e) {
			 ReportSubmitter.generatePropertyFile();
			 statusLabel.setText(PROPERTY_FILE + " generated.");
		 }
		});


		c.gridx = gridx;
		c.gridy = 0;
		ctrlPanel.add(propertyLabel, c);

		c.gridx = gridx;
		c.gridy = 1;
		ctrlPanel.add(tf6, c);

		c.gridx = gridx;
		c.gridy = 2;
		ctrlPanel.add(generatePropertyButton, c);
		gridx++;
//////////////////////////////////////////////////////////////////////////////////////


        ctrlPanel.setBorder(BorderFactory.createTitledBorder("Data Files"));
		frame.getContentPane().add(ctrlPanel, BorderLayout.WEST);

//////////////////////////////////////////////////////////////////////////////////////

	if (!REPORT_GENERATION_MODE) {
        JPanel controlPanel5 = new JPanel();

		hh = new HierarchyHelper(Utils.readFile(HIER_FILE));
		Vector roots = hh.getRoots();
		root_array = new String[roots.size()];
		root = new DefaultMutableTreeNode("Root");

		displayLabels = new Vector();
		for (int i=0; i<roots.size(); i++) {
			String root = (String) roots.elementAt(i);
			String label = hh.getLabel(root);
			displayLabels.add(label + " (" + root + ")");
		}

		displayLabels = new SortUtils().quickSort(displayLabels);

		for (int i=0; i<displayLabels.size(); i++) {
			String displayLabel = (String) displayLabels.elementAt(i);
			root_array[i] = displayLabel;
		}

		Utils.saveToFile("roots_" + HIER_FILE, displayLabels);
		for (int i=0; i<displayLabels.size(); i++) {
			String displayLabel = (String) displayLabels.elementAt(i);
			DefaultMutableTreeNode topNode = new DefaultMutableTreeNode(displayLabel);
			topNode = expandNode(topNode);
			root.add(topNode);
		}

		//create the tree by passing in the root node
		tree = new JTree(root);
		ImageIcon imageIcon = new ImageIcon(ReportGenerationUI.class.getResource("dot.gif"));
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		renderer.setLeafIcon(imageIcon);

		tree.setCellRenderer(renderer);
		tree.setShowsRootHandles(true);
		tree.setRootVisible(false);

		JScrollPane treeScrollPane = new JScrollPane(tree);
        treeScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        treeScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		treeScrollPane.setBounds(0,0,2000,1000);

		selectedLabel = new JLabel();

		tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
				selectedLabel.setText(selectedNode.getUserObject().toString());
				tf2.setText(selectedNode.getUserObject().toString());
			}
		});

		frame.getContentPane().add(treeScrollPane, BorderLayout.WEST);
	}
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		JLabel reportLabel = new JLabel("Select Report: ", JLabel.LEFT);
		//String[] reports = new String[]{"Maps To ICD0", "EVS Statistics", "GDC Diff", "FDA Count", "PrimitiveConcepts"};

		String[] reports = new String[]{"Maps To ICD0", "EVS Statistics", "GDC Diff", "FDA Count", "PrimitiveConcepts",
		"3D Pie Chart", "Linguistic Analysis", "Pediatric Subset Reports", "Is-A Hierarchy QA"};

		reportComboBox = new JComboBox(reports);
		JPanel controlPanel7 = new JPanel();

		final JButton reportButton = new JButton("Generate");
		reportButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			String report = (String) reportComboBox.getSelectedItem();
			statusLabel.setText("Generating " + report + " ...");
			ReportSubmitter.generateReport(report);
			statusLabel.setText("Reports " + report + " generated.");
		}
		});

		controlPanel7.add(reportLabel);
		controlPanel7.add( new JScrollPane(reportComboBox), BorderLayout.CENTER );
		//controlPanel7.add(tf7);
		controlPanel7.add(reportButton);
		//controlPanel.add(controlPanel7, BorderLayout.WEST);

		frame.getContentPane().add(controlPanel7, BorderLayout.WEST);

//////////////////////////////////////////////////////////////////////////////////////
		JPanel controlPanel6 = new JPanel();
		statusLabel = new JLabel("Status", JLabel.LEFT);
		//controlPanel6.setLayout(new FlowLayout());
		controlPanel6.add(statusLabel);
		frame.getContentPane().add(controlPanel6, BorderLayout.WEST);
//////////////////////////////////////////////////////////////////////////////////////

		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		frame.setSize(2400, 1600);
		frame.setVisible(true);
	}

	public String getCode(String displayName) {
		int n = displayName.lastIndexOf("(");
		if (n == -1) return displayName;
		return displayName.substring(n+1, displayName.length()-1);
	}

	public boolean isExpandable(DefaultMutableTreeNode node) {
        String displayName = (String) node.getUserObject();
        String code = getCode(displayName);
        Vector subs = (Vector) hh.getSubclassCodes(code);
        if (subs != null && subs.size() > 0) return true;
		return false;
	}

	public DefaultMutableTreeNode expandNode(DefaultMutableTreeNode node) {
		if (!isExpandable(node)) return node;
        String displayName = (String) node.getUserObject();
        String code = getCode(displayName);
        Vector subs = (Vector) hh.getSubclassCodes(code);
        if (subs != null && subs.size() > 0) {
			for (int i=0; i<subs.size(); i++) {
				String sub = (String) subs.elementAt(i);
				String label = hh.getLabel(sub);
				String name = label + " (" + sub + ")";
				DefaultMutableTreeNode childnode = new DefaultMutableTreeNode(name);
				childnode = expandNode(childnode);
				node.add(childnode);
			}
		}
		return node;
	}

	public static void main(String[] args) {
		ReportGenerationUI ui = new ReportGenerationUI();
		ui.createWindow();
	}
}

