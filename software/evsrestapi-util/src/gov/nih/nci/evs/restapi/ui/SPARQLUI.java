package gov.nih.nci.evs.restapi.ui;
import com.github.lgooddatepicker.components.*;
import gov.nih.nci.evs.restapi.config.*;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.appl.*;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.ComponentOrientation;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

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
import javax.swing.filechooser.*;

import java.io.*;
import java.util.*;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;

import java.awt.Dimension;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.text.Element;
import javax.swing.event.*;
import javax.swing.border.LineBorder;
import java.time.format.*;

public class SPARQLUI {
	public static HashMap date2NamedGraphMap = null;
	static {
		date2NamedGraphMap = new HashMap();
		int currentYear = DateUtils.getCurrentYear();
		int currentMonth = DateUtils.getCurrentMonth();
        Vector w = DateUtils.getNCItReleaseSchedule(currentYear, false);
        for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			date2NamedGraphMap.put((String) u.elementAt(4), (String) u.elementAt(3));
		}
	}

	private JTree tree;
	private JLabel selectedLabel;

	//static String HIER_FILE = "parent_child.txt";
	static String ROLE_FILE = "roles.txt";
	static String PROPERTY_FILE = "properties.txt";
	static String OBJECT_PROPERTY_FILE = "objectProperies.txt";
	static String SEMANTIC_TYPE_FILE = "P106.txt";
	//static String NCIT_OWL_FILE = "ThesaurusInferred_forTS.owl";
	//static String AXIOM_FILE = "axiom_ThesaurusInferred_forTS.txt";
	static String VS_FILE = "A8.txt";

	static String reportGenerationDirectory = ConfigurationController.reportGenerationDirectory;
	static String NCIT_OWL_FILE = reportGenerationDirectory + File.separator + ConfigurationController.owlfile;
	static String AXIOM_FILE = reportGenerationDirectory + File.separator + ConfigurationController.axiomfile;
	static String HIER_FILE = reportGenerationDirectory + File.separator + ConfigurationController.hierfile;

	static String[] root_array = null;

	static HierarchyHelper hh = null;
	//static Vector roots = null;
	static Vector displayLabels = null;
	//static DefaultMutableTreeNode root = null;

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

	HashSet branch = null;

	JTextField tf = null;
	JTextField tf2 = null;
	JTextField tf3 = null;

	JLabel statusLabel = new JLabel("");
	JButton resetButton = new JButton("Reset");
	JButton resetButton2 = new JButton("Reset");
	//LexicalMatchRunner lmr = null;
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

	public static String getNamedGraphForDate(String dateStr) {
		return (String) date2NamedGraphMap.get(dateStr);
	}

	private void createWindow() {
		frame = new JFrame("SPARQL UI");
		frame.setLayout(new GridLayout(10, 1));

		frame.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent windowEvent){
			System.exit(0);
			}
		});

//////////////////////////////////////////////////////////////////////////////////////////////////////////////
        JTextArea textarea = new JTextArea(100, 50);

        DatePicker datePicker;
        DatePickerSettings dateSettings;

        // Create a CalendarPanel: With highlight policy.
        dateSettings = new DatePickerSettings();
        datePicker = new DatePicker(dateSettings);
        dateSettings.setVisibleDateTextField(false);
        dateSettings.setGapBeforeButtonPixels(0);
        datePicker.setBorder(LineBorder.createBlackLineBorder());

        JLabel datePickerLabel = new JLabel("Date picker: ", JLabel.LEFT);
        JTextField tf01 = new JTextField(20);
        JTextField tf11 = new JTextField(80);

		final JButton refreshButton = new JButton("Refresh Selected Date");
		refreshButton.addActionListener(new ActionListener() {
		  public void actionPerformed(ActionEvent e) {
              tf01.setText(reformatDate(datePicker.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE)));
              String dateStr = reformatDate(datePicker.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
              String ng = getNamedGraphForDate(dateStr);
              tf11.setText(ng);
              String t = updateQuery(textarea.getText(), ng);
              textarea.setText(t);
		  }
		});

		JPanel panel = new JPanel();
        panel.add(datePickerLabel);
        panel.add(tf01);
        panel.add(datePicker);
        panel.add(refreshButton);
        frame.getContentPane().add(panel, BorderLayout.WEST);

		serviceUrl = ConfigurationController.serviceUrl;
		namedGraph = ConfigurationController.namedGraph;
		username = ConfigurationController.username;
		password = ConfigurationController.password;

		Vector serviceUrl_vec = ConfigurationController.serviceUrl_vec;
		String[] serviceUrls = new String[serviceUrl_vec.size()];
        for (int i=0; i<serviceUrl_vec.size(); i++) {
			String url = (String) serviceUrl_vec.elementAt(i);
			serviceUrls[i] = url;
		}
		JComboBox urlList = new JComboBox(serviceUrls);
		urlList.setSelectedIndex(4);
		urlList.addActionListener(new ActionListener() {
		  public void actionPerformed(ActionEvent e) {
              serviceUrl = (String) urlList.getSelectedItem();
		  }
		});

        // button to open open dialog
        JLabel serviceURLLabel = new JLabel("Service URL: ", JLabel.LEFT);

        JPanel p1 = new JPanel();
        p1.add(serviceURLLabel);
        p1.add(urlList);
        frame.getContentPane().add(p1, BorderLayout.WEST);

        JLabel namedGraphLabel = new JLabel("Named Graph: ", JLabel.LEFT);

        tf11.setText(ConfigurationController.namedGraph);
        JPanel p2 = new JPanel();
        p2.add(namedGraphLabel);
        p2.add(tf11);
        frame.getContentPane().add(p2, BorderLayout.WEST);

        JLabel userLabel = new JLabel("User Name: ", JLabel.LEFT);
        JTextField tf12 = new JTextField(80);
        tf12.setText(ConfigurationController.username);
        JPanel p3 = new JPanel();
        p3.add(userLabel);
        p3.add(tf12);
        frame.getContentPane().add(p3, BorderLayout.WEST);

        JLabel passwordLabel = new JLabel("Password: ", JLabel.LEFT);
        JTextField tf13 = new JTextField(80);
        tf13.setText(ConfigurationController.password);
        JPanel p4 = new JPanel();
        p4.add(passwordLabel);
        p4.add(tf13);
        frame.getContentPane().add(p4, BorderLayout.WEST);

        OWLSPARQLUtils owlsparqlutils = new OWLSPARQLUtils(serviceUrl, tf12.getText(), tf13.getText());
        owlsparqlutils.set_named_graph(tf11.getText());


        textarea.setLineWrap(true);
		final JButton loadButton = new JButton("Load SPARQL");
		loadButton.addActionListener(new ActionListener() {
		  public void actionPerformed(ActionEvent e) {
				JFileChooser f1 = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
				int r = f1.showSaveDialog(null);
				if (r == JFileChooser.APPROVE_OPTION) {
					String query = owlsparqlutils.getQuery(f1.getSelectedFile().getAbsolutePath());
					textarea.setText(query);
				}
		  }
		});
		/*
		JPanel loadPanel = new JPanel();
		loadPanel.add(loadButton);
		frame.getContentPane().add(loadPanel, BorderLayout.WEST);
		*/

        textarea.setEditable(true);
        JScrollPane scrollPane1 = new JScrollPane(textarea,
			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        frame.add(scrollPane1);

		final JButton execButton = new JButton("Execute");
		JTextArea textarea2 = new JTextArea(100, 50);
		execButton.addActionListener(new ActionListener() {
		  public void actionPerformed(ActionEvent e) {
			  System.out.println(textarea.getText());
              Vector w = owlsparqlutils.executeQuery(textarea.getText());
              if (w != null) {
				  StringBuffer buf = new StringBuffer();
				  for (int i=0; i<w.size(); i++) {
					  String line = (String) w.elementAt(i);
					  buf.append(line).append("\n");
				  }
				  textarea2.setText(buf.toString());
		      } else {
				  System.out.println("w is null.");
			  }
		  }
		});
		JPanel execPanel = new JPanel();
		execPanel.add(loadButton);
		execPanel.add(execButton);
		frame.getContentPane().add(execPanel, BorderLayout.WEST);

        textarea2.setEditable(false);
        JScrollPane scrollPane12 = new JScrollPane(textarea2,
			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        frame.add(scrollPane12);

		final JButton saveButton = new JButton("Save");
		saveButton.addActionListener(new ActionListener() {
		  public void actionPerformed(ActionEvent e) {
				JFileChooser f1 = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
				int r = f1.showSaveDialog(null);
				if (r == JFileChooser.APPROVE_OPTION) {
					String outputfile = f1.getSelectedFile().getAbsolutePath();
					String content = textarea2.getText();
					Vector w = new Vector();
					w.add(content);
                    Utils.saveToFile(outputfile, w);
				}
		  }
		});

		final JButton resetButton = new JButton("Reset");
		resetButton.addActionListener(new ActionListener() {
		  public void actionPerformed(ActionEvent e) {
              textarea.setText("");
              textarea2.setText("");
		  }
		});

		JPanel resetPanel = new JPanel();
		resetPanel.add(saveButton);
		resetPanel.add(resetButton);
		frame.getContentPane().add(resetPanel, BorderLayout.WEST);

        frame.setSize(2000, 1000);
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.setVisible(true);
	}

	public String getCode(String displayName) {
		int n = displayName.lastIndexOf("(");
		if (n == -1) return displayName;
		return displayName.substring(n+1, displayName.length()-1);
	}

	public static String updateQuery(String query, String ng) {
		if (query.length() == 0) return query;
		Vector u = StringUtils.parseData(query, '\n');
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<u.size(); i++) {
			String line = (String) u.elementAt(i);
			line = line.trim();
			if (line.startsWith("from ")) {
				buf.append("from " + "<" + ng + ">").append("\n");
			} else {
				buf.append(line).append("\n");
			}
		}
		return buf.toString();
	}

    public static Vector listFilesInDirectory() {
		String dirName = null;
		return listFilesInDirectory(dirName);
	}

    public static Vector listFilesInDirectory(String dirName) {
		Vector v = new Vector();
		if (dirName == null) {
			dirName = System.getProperty("user.dir");;
		}
        File f = new File(dirName);
        String[] pathnames = f.list();
        for (String pathname : pathnames) {
            //System.out.println(pathname);
            v.add(pathname);
        }
        return v;
	}

	public static String reformatDate(String dateStr) {
		// from yyyy-MM-dd to MM/dd/yyyy
		Vector v = StringUtils.parseData(dateStr, '-');
		return (String) v.elementAt(1) + "/" + (String) v.elementAt(2) + "/" + (String) v.elementAt(0);
	}

	public static void main(String[] args) {
		SPARQLUI ui = new SPARQLUI();
		ui.createWindow();
	}
}

