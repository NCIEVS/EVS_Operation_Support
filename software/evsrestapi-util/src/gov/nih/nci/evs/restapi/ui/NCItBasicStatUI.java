package gov.nih.nci.evs.restapi.ui;
import gov.nih.nci.evs.restapi.appl.*;
import gov.nih.nci.evs.restapi.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.text.*;

public class NCItBasicStatUI {
    static HashMap filenameMap = null;
    static String RETIRED_FILE = "P310.txt";
    static HashSet RETIRED_CONCEPTS = null;
    static String LABEL_FILE = "labels.txt";
    static {
		filenameMap = new HashMap();
		filenameMap.put("Concept File", "labels.txt");
		filenameMap.put("Axiom File", "axiom_ThesaurusInferred_forTS.txt");
		filenameMap.put("Hierarchy File", "parent_child.txt");
		filenameMap.put("Restriction File", "roles.txt");
		filenameMap.put("Association File", "associations.txt");
		filenameMap.put("Property File", "properties.txt");

        createConceptStatusFile();

		RETIRED_CONCEPTS = new HashSet();
		Vector v = Utils.readFile(RETIRED_FILE);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String status = (String) u.elementAt(2);
			if (status.compareTo("Retired_Concept") == 0) {
				RETIRED_CONCEPTS.add((String) u.elementAt(0));
			}
		}
		System.out.println("Number of retired concepts: " + RETIRED_CONCEPTS.size());

		createLabelFile();
	}

    public static void createConceptStatusFile() {
		Vector v = Utils.readFile("properties.txt");
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (line.indexOf("|P310|") != -1) {
				w.add(line);
			}
		}
		Utils.saveToFile(RETIRED_FILE, w);
	}

    public static void createLabelFile() {
		Vector v = Utils.readFile("parent_child.txt");
		HashSet hset = new HashSet();
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String parentLabel = (String) u.elementAt(0);
			String parentCode = (String) u.elementAt(1);
			String childLabel = (String) u.elementAt(2);
			String childCode = (String) u.elementAt(3);
			String t = parentLabel + "|" + parentCode;
			if (!hset.contains(t)) {
				hset.add(t);
				w.add(t);
			}
			t = childLabel + "|" + childCode;
			if (!hset.contains(t)) {
				hset.add(t);
				w.add(t);
			}
		}
		w = new SortUtils().quickSort(w);
		Utils.saveToFile(LABEL_FILE, w);
		System.out.println("Number of concepts: " + w.size());
	}

	public static Vector match(String selected, String pattern, boolean excludeRetired) {
		String filename = (String) filenameMap.get(selected);
		Vector w = new Vector();
		if (pattern.isEmpty()) {
			Vector v = Utils.readFile(filename);
			if (!excludeRetired) {
				//return v.size();
				return v;
			} else {
				int count = 0;
				for (int i=0; i<v.size(); i++) {
					String line = (String) v.elementAt(i);
					if (!containsRetiredCode(line)) {
						w.add(line);
						count++;
					}
				}
				return w;//count;
			}
		} else {
			System.out.println("Reading " + filename);
			Vector v = Utils.readFile(filename);
			System.out.println("v " + v.size());
			return match(v, pattern, excludeRetired);
		}
	}

	public static boolean containsRetiredCode(String line) {
		Vector u = StringUtils.parseData(line, '|');
		for (int i=0; i<u.size(); i++) {
			String t = (String) u.elementAt(i);
			boolean bool = StringUtils.isNCItCode(t);
			if (bool && RETIRED_CONCEPTS.contains(t)) {
				return true;
			}
		}
		return false;
	}

	public static Vector match(Vector v, String pattern, boolean excludeRetired) {
		if (pattern.compareTo("") == 0) return v;
		Vector w = new Vector();
		Vector u0 = StringUtils.parseData(pattern, '|');
		Vector u1 = new Vector();
		if (u0.size() > 0) {
			for (int i=0; i<u0.size(); i++) {
				String t = (String) u0.elementAt(i);
				if (t.compareTo("*") != 0) {
					u1.add(t);
				}
			}
		}
		if (u1.size() == 0) {
			return v;//.size();
		}
		int count = 0;
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			if (excludeRetired && NCItBasicStatUI.containsRetiredCode(line)) {
				//System.out.println("SKIP");
			} else {
				Vector u2 = StringUtils.parseData(line, '|');
				boolean bool = true;
				for (int j=0; j<u1.size(); j++) {
					String t = (String) u1.elementAt(j);
					if (!u2.contains(t)) {
						bool = false;
						break;
					}
				}
				if (bool) {
					// rare exception C171325|P90|P97
					if (line.contains("|P90|P97") && pattern.compareTo("P97") == 0) {
						//skip
					} else {
						count++;
						w.add(line);
					}
				}
			}
		}
		return w;
	}

    public static void main(String[] args) {
        // Create the main frame
        JFrame frame = new JFrame("NCItBasicStat UI");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 200);
        frame.setLayout(new FlowLayout());
        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel label4 = new JLabel("Count Stat");
        JTextField tf2 = new JTextField();
        tf2.setPreferredSize(new Dimension(600, 30));
        JLabel label5 = new JLabel("Status");
        JLabel label6 = new JLabel("");
        JLabel label2 = new JLabel("Match Pattern (*: wild)");
        JTextField tf1 = new JTextField();
        tf1.setPreferredSize(new Dimension(600, 30));

        JLabel label1 = new JLabel("NCIt File");
		String[] options1 = new String[] {
			"Concept File",
			"Hierarchy File",
			"Axiom File",
			"Restriction File",
			"Association File",
			"Property File"};
        JComboBox<String> combobox1 = new JComboBox<>(options1);
        combobox1.setSelectedIndex(0);
        JButton btn0 = new JButton("View");
        btn0.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("View")) {
					String selected = (String) combobox1.getSelectedItem();
					String filename = (String) filenameMap.get(selected);
					ApplicationLauncher.launch(filename);
				}
			}
        });

        JButton btn3 = new JButton("Clear");
        btn3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Clear")) {
				     tf1.setText("");
				     tf2.setText("");
				}
			}
        });
        JLabel label3 = new JLabel("Retired Concept");
		JPanel btnGroupPanel1 = new JPanel();
		ButtonGroup btnGroup1 = new ButtonGroup();
		JRadioButton btn_1_0 = new JRadioButton("Include");
		btn_1_0.setSelected(false);

		btnGroup1.add(btn_1_0);
		btnGroupPanel1.add(btn_1_0);
		JRadioButton btn_1_1 = new JRadioButton("Exclude");

		btn_1_0 .addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			btn_1_1.setSelected(false);
		}
		});

		btn_1_1 .addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			btn_1_0.setSelected(false);
		}
		});
		btnGroup1.add(btn_1_1);
		btn_1_1.setSelected(true);
		btnGroupPanel1.add(btn_1_1);

        JButton btn2 = new JButton("Match");
        btn2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Match")) {
					 String selected = (String) combobox1.getSelectedItem();
					 String pattern = tf1.getText();
					 System.out.println(selected);
					 System.out.println(pattern);
					 boolean excludeRetired = btn_1_1.isSelected();
					 System.out.println("excludeRetired? " + excludeRetired);
					 Vector w = match(selected, pattern, excludeRetired);
					 int count = w.size();
					 tf2.setText("" + count);
					 Utils.saveToFile("match_results.txt", w);
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
        panel.add(combobox1, gbc);
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btn0, gbc);

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
        panel.add(btn2, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label3, gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btnGroupPanel1, gbc);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label4, gbc);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(tf2, gbc);
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btn3, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label5, gbc);
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label6, gbc);
        frame.add(panel);
        // Make the frame visible
        frame.setVisible(true);
    }
}


