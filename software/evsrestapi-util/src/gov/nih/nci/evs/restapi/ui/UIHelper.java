import gov.nih.nci.evs.restapi.appl.*;
import gov.nih.nci.evs.restapi.ui.*;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.config.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.text.*;
import java.nio.file.*;
import java.nio.file.Paths;

public class UIHelper {

    public static void printBtnActionListners(PrintWriter pw, HashMap btnId2ActionMap) {
		Vector btnId_vec = new Vector();
		Iterator it = btnId2ActionMap.keySet().iterator();
		while (it.hasNext()) {
			String btnId = (String) it.next();
			btnId_vec.add(btnId);
		}
		btnId_vec = new SortUtils().quickSort(btnId_vec);
		for (int i=0; i<btnId_vec.size(); i++) {
			String btnId = (String) btnId_vec.elementAt(i);
			printBtnListener(pw, btnId, (String) btnId2ActionMap.get(btnId));
		}
	}

	public static void generate(PrintWriter out, String ui_design) {
		HashMap btnId2ActionMap = new HashMap();
		UIHelperPlus helper = new UIHelperPlus(ui_design);
		Vector design_vec = Utils.readFile(ui_design);
		String className = (String) design_vec.elementAt(0);
		String outputfile = className + ".java";
		int y = 0;
		int x = 0;
		int component_count = 0;
		int label_count = 0;
		int tf_count = 0;
		int ta_count = 0;
		int tp_count = 0;
		int scrollpane_count = 0;
		int combobox_count = 0;
		int radiobutton_count = 0;
		int checkbox_count = 0;
		int btn_count = 0;
		int buttonpanel_count = 0;

		String labelId = null;
		String tfId = null;
		String taId = null;
		String tpId = null;
		String scrollpaneId = null;
		String comboboxId = null;
		String radiobuttonId = null;
		String checkboxId = null;
		String btnId = null;
		String buttonpanelId = null;

		Vector v = Utils.readFile(ui_design);
		for (int i=1; i<v.size(); i++) {
			y = i-1;
			String line = (String) v.elementAt(i);
			System.out.println(line);
			out.println("//" + line);
			Vector u = StringUtils.parseData(line, '|');
			for (int j=0; j<u.size(); j++) {
				x = j;
				String t = (String) u.elementAt(j);
				Vector u2 = StringUtils.parseData(t, '$');
				String s = (String) u2.elementAt(0);
				if (s.compareTo("JLabel") == 0) {
					label_count++;
					labelId = "label_" + label_count;
					out.println("        JLabel " + labelId + " = new JLabel(" + "\"" + (String) u2.elementAt(1) + "\");");
					out.println("        gbc.gridx = " + x + ";");
					out.println("        gbc.gridy = " + y + ";");
					out.println("        gbc.insets = new Insets(3, 3, 3, 3);");
					out.println("        panel.add(" + labelId + ", gbc);");
				} else if (s.compareTo("JButton") == 0) {
					String s1 = (String) u2.elementAt(1);
					if (s1.compareTo("Browse") == 0) {
						btn_count++;
						btnId = "btn_" + btn_count;
						String title = "Select File";
						String filterExtensionStr = "OWL|owl;XML|xml;RDF|rdf;Text|txt;Java|java";
						out.println("        JButton " + btnId + " = SwingComponents.createFileChooserButton(\"" + title + "\", \"" + filterExtensionStr + "\", " + tfId + ");");
					} else {
						btn_count++;
						btnId = "btn_" + btn_count;
						out.println("        JButton " + btnId + " = new JButton(" + "\"" + (String) u2.elementAt(1) + "\");");
					}

					btnId2ActionMap.put(btnId, s1);
					out.println("        gbc.gridx = " + x + ";");
					out.println("        gbc.gridy = " + y + ";");
					out.println("        gbc.insets = new Insets(3, 3, 3, 3);");
					out.println("        panel.add(" + btnId + ", gbc);");
				} else if (s.compareTo("JTextField") == 0) {
					tf_count++;
					tfId = "tf_" + tf_count;
					out.println("        JTextField " + tfId + " = new JTextField();");
					out.println("        " + tfId + ".setPreferredSize(new Dimension(600, 30));");
					out.println("        gbc.gridx = " + x + ";");
					out.println("        gbc.gridy = " + y + ";");
					out.println("        gbc.insets = new Insets(3, 3, 3, 3);");
					out.println("        panel.add(" + tfId + ", gbc);");
				} else if (s.compareTo("JTextArea") == 0) {
					ta_count++;
					taId = "ta_" + ta_count;
					out.println("        JTextArea " + taId + " = new JTextArea();");
					out.println("        " + taId + ".setPreferredSize(new Dimension(600, 400));");
					out.println("        gbc.gridx = " + x + ";");
					out.println("        gbc.gridy = " + y + ";");
					out.println("        gbc.insets = new Insets(3, 3, 3, 3);");
					out.println("        panel.add(" + taId + ", gbc);");

				} else if (s.compareTo("JScrollPane") == 0) {
					scrollpane_count++;
					scrollpaneId = "scrollpan_" + scrollpane_count;
                    tp_count++;
                    tpId = "tp_" + tp_count;
                    out.println("        JTextPane " + tpId + " = new JTextPane();");
                    out.println("        " + tpId + ".setPreferredSize(new Dimension(600, 400));");
                    out.println("        JScrollPane " + scrollpaneId + " = SwingComponents.createJScrollPane(" + tpId + ", 12, Color.BLUE);");
					out.println("        gbc.gridx = " + x + ";");
					out.println("        gbc.gridy = " + y + ";");
					out.println("        gbc.insets = new Insets(3, 3, 3, 3);");
					out.println("        panel.add(" + scrollpaneId + ", gbc);");

				} else if (s.compareTo("JComboBox") == 0) {
					combobox_count++;
					comboboxId = "combobox_" + combobox_count;
					String valueStr = (String) u2.elementAt(1);
					String t2 = (String) u2.elementAt(1);
					Vector values = StringUtils.parseData(valueStr, ';');
					out.println("        String valueStr = " + "\"" + t2 + "\";");
					out.println("        Vector values = StringUtils.parseData(valueStr, ';');");
					out.println("        String[] options = new String[values.size()];");
					out.println("        for (int k=0; k<values.size(); k++) {");
					out.println("        	options[k] = (String) values.elementAt(k);");
					out.println("        }");
					out.println("        JComboBox<String> " + comboboxId + " = new JComboBox<>(options);");

					out.println("        gbc.gridx = " + x + ";");
					out.println("        gbc.gridy = " + y + ";");
					out.println("        gbc.insets = new Insets(3, 3, 3, 3);");
					out.println("        panel.add(" + comboboxId + ", gbc);");

				} else if (s.compareTo("JRadioButton") == 0) {
					radiobutton_count++;
					radiobuttonId = "radiobutton_" + ta_count;
					out.println("        JRadioButton " + radiobuttonId + " = new JRadioButton(" + "\"" + (String) u2.elementAt(1) + "\");");
					out.println("        gbc.gridx = " + x + ";");
					out.println("        gbc.gridy = " + y + ";");
					out.println("        gbc.insets = new Insets(3, 3, 3, 3);");
					out.println("        panel.add(" + radiobuttonId + ", gbc);");
				} else if (s.compareTo("JCheckBox") == 0) {
					checkbox_count++;
					checkboxId = "checkbox_" + ta_count;
					out.println("        JCheckBox " + checkboxId + " = new JRadioButton(" + "\"" + (String) u2.elementAt(1) + "\");");
					out.println("        gbc.gridx = " + x + ";");
					out.println("        gbc.gridy = " + y + ";");
					out.println("        gbc.insets = new Insets(3, 3, 3, 3);");
					out.println("        panel.add(" + checkboxId + ", gbc);");

				} else if (s.compareTo("ButtonPanel") == 0) {
					buttonpanel_count++;
					buttonpanelId = "buttonpanel_" + buttonpanel_count;
					String s2 = (String) u2.elementAt(1);
					Vector labels = StringUtils.parseData(s2, ';');
					out.println("        Vector<Component> components = new Vector();");
					for (int k=0; k<labels.size(); k++) {
						String label = (String) labels.elementAt(k);
						btn_count++;
						btnId = "btn_" + btn_count;
						btnId2ActionMap.put(btnId, label);
						out.println("        JButton " + btnId + " = new JButton(" + "\"" + label + "\");");
						out.println("        components.add(" + btnId + ");");
					}
					out.println("        JPanel " + buttonpanelId + " = SwingComponents.createButtonPanel(components);");
					out.println("        gbc.gridx = " + x + ";");
					out.println("        gbc.gridy = " + y + ";");
					out.println("        gbc.insets = new Insets(3, 3, 3, 3);");
					out.println("        panel.add(" + buttonpanelId + ", gbc);");
				}
			}
		}
		printBtnActionListners(out, btnId2ActionMap);

		out.println("        ");
		out.println("        frame.add(panel);");
		out.println("        // Make the frame visible");
		out.println("        frame.setVisible(true);");
		out.println("    }        ");
		out.println("}        ");
	}

	public static void generate(String ui_design) {
		PrintWriter pw = null;
		try {
			UIHelperPlus helper = new UIHelperPlus(ui_design);
			Vector design_vec = Utils.readFile(ui_design);
			String className = (String) design_vec.elementAt(0);
			String outputfile = className + ".java";
			pw = new PrintWriter(outputfile, "UTF-8");

			helper.printClassStmts(pw, className);

			generate(pw, ui_design);

			//helper.printFooter(pw);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (pw != null) {
				try {
					pw.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	public static void printBtnListener(PrintWriter out, String btnId, String action) {
		out.println("        " + btnId + ".addActionListener(new ActionListener() {");
		out.println("			public void actionPerformed(ActionEvent e) {");
		out.println("				String s = e.getActionCommand();");
		out.println("				if (s.equals(\"" + action + "\")) {");
		out.println("					try {");
		out.println("						");
		out.println("					} catch (Exception ex) {");
		out.println("						");
		out.println("					}");
		out.println("				}");
		out.println("			}");
		out.println("        });");
	}

	public static void main(String[] args) {
		//String outputfile = args[0];
		String ui_design = args[0];
		generate(ui_design);
	}


}