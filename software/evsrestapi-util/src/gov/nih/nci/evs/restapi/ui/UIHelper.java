package gov.nih.nci.evs.restapi.ui;
import gov.nih.nci.evs.restapi.util.*;
import java.io.*;
import java.text.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;

public class UIHelper {
	static String DESIGN_FILE = "ui_design.txt";
	public int labelCount = 0;
	public int btnCount = 0;
	public int jtCount = 0;
	public int comboboxCount = 0;
	public String className = null;
	public Vector design_vec = null;

	public UIHelper() {
		initialize();
	}

	public UIHelper(String designFile) {
		DESIGN_FILE = designFile;
		initialize();
	}

	public void initialize() {
		File f = new File(DESIGN_FILE);
		if (!f.exists()) {
			System.out.println("File " + DESIGN_FILE + " not found. Please privide a design file.");
			System.exit(0);
		}
		design_vec = Utils.readFile(DESIGN_FILE);
		className = (String) design_vec.elementAt(0);
	}

    public void run() {
		PrintWriter pw = null;
		String outputfile = className + ".java";
		try {
			pw = new PrintWriter(outputfile, "UTF-8");
			printClassStmts(pw, className);

			pw.println("///////////////////////////////////////////////////////////////////////////");

			String tf_i = null;
			String btn_1 = null;
			String btn_2 = null;
			String combo_i = null;
			int btn_count = 0;
			int combo_count = 0;
			int label_count = 0;
			for (int i=1; i<design_vec.size(); i++) {
				String label1 = null;
				String label2 = null;
				String btn1 = null;
				String btn2 = null;
				String btn1_label = null;
				String btn2_label = null;

				String line = (String) design_vec.elementAt(i);
				pw.println("\t\t//" + line);
				Vector u = StringUtils.parseData(line, '|');
				String t0 = (String) u.elementAt(0);
				Vector u0 = StringUtils.parseData(t0, '$');

				label1 = (String) (String) u0.elementAt(1);

				String t1 = (String) u.elementAt(1);
				Vector u1 = StringUtils.parseData(t1, '$');

				String component = (String) (String) u1.elementAt(0);
				if (component.compareTo("JTextField") == 0) {
					String tf = (String) (String) u1.elementAt(0);
					tf_i = "tf" + i;
					if (u.size() > 2) {
						String t2 = (String) u.elementAt(2);
						Vector u2 = StringUtils.parseData(t2, '$');
						btn1 = (String) (String) u2.elementAt(1);
						btn1_label = (String) (String) u2.elementAt(1);
						btn_count++;
						btn_1 = "btn" + btn_count;
						btn2 = null;
						if (u.size() > 3) {
							String t3 = (String) u.elementAt(3);
							Vector u3 = StringUtils.parseData(t3, '$');
							btn2 = (String) (String) u3.elementAt(1);
							btn2_label = (String) (String) u3.elementAt(1);
							btn_count++;
							btn_2 = "btn" + btn_count;
						}
					}
					printUIComponent(pw, label1, label2, tf_i, btn1_label, btn2_label);
				} else if (component.compareTo("JComboBox") == 0) {
					combo_count++;
					combo_i = "combo" + i;
					String valueStr = (String) (String) u1.elementAt(1);
					Vector values = StringUtils.parseData(valueStr, ';');
					String[] options = new String[values.size()];
					for (int j=0; j<values.size(); j++) {
						options[j] = (String) values.elementAt(j);
					}

					String t2 = (String) u.elementAt(2);
					Vector u2 = StringUtils.parseData(t2, '$');
					btn1 = (String) (String) u2.elementAt(1);
					btn1_label = (String) (String) u2.elementAt(1);
                    printUIComponent(pw, label1, label2, combo_i, options, btn1_label, btn2_label);
				} else if (component.compareTo("JLabel") == 0) {
					label2 = (String) (String) u1.elementAt(1);
					printUIComponent(pw, label1, label2, null, null, null);
				}
			}
			pw.println("///////////////////////////////////////////////////////////////////////////");

			printLayOutStmts(pw);

			printFooter(pw);


		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				pw.close();
				System.out.println("Output file " + outputfile + " generated.");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public void printFooter(PrintWriter out) {
		out.println("        frame.add(panel);");
		out.println("        // Make the frame visible");
		out.println("        frame.setVisible(true);");
		out.println("    }");
		out.println("}");
	}


	public void printClassStmts(PrintWriter out, String className) {
		out.println("import gov.nih.nci.evs.restapi.ui.*;");
		out.println("import gov.nih.nci.evs.restapi.util.*;");
		out.println("import gov.nih.nci.evs.restapi.config.*;");
		out.println("");
		out.println("import javax.swing.*;");
		out.println("import java.awt.*;");
		out.println("import java.awt.event.*;");
		out.println("import java.io.*;");
		out.println("import java.util.*;");
		out.println("import javax.swing.text.*;");
		out.println("");
		out.println("public class " + className + " {");

		out.println("");
		out.println("    public static void main(String[] args) {");
		out.println("        // Create the main frame");
		out.println("        JFrame frame = new JFrame(\"" + className + " UI\");");
		out.println("        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);");
		out.println("        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);");
		out.println("        frame.setSize(800, 200);");
		out.println("        frame.setLayout(new FlowLayout());");

		out.println("        JPanel panel = new JPanel();");
		out.println("        GridBagLayout layout = new GridBagLayout();");
		out.println("        panel.setLayout(layout);");
		out.println("        GridBagConstraints gbc = new GridBagConstraints();");
		out.println("");
		out.println("        gbc.fill = GridBagConstraints.HORIZONTAL;");
		out.println("");
	}

	public void printLayOutStmts(PrintWriter out) {
		int row = 0;
		int col = 0;
		String tf_i = null;
		String btn_1 = null;
		String btn_2 = null;
		int btn_count = 0;
		int label_count = 0;
		int combo_count = 0;
		String combo_i = null;
		String label_i = null;
		String label_j = null;

		for (int i=1; i<design_vec.size(); i++) {
			String btn1 = null;
			String btn2 = null;
			tf_i = null;
			combo_i = null;

			String line = (String) design_vec.elementAt(i);
			System.out.println("\n" + line);
			Vector u = StringUtils.parseData(line, '|');

			String t0 = (String) u.elementAt(0);
			Vector u0 = StringUtils.parseData(t0, '$');
			String label = (String) (String) u0.elementAt(1);
			label_count++;
			label_i = "label" + label_count;

			String t1 = (String) u.elementAt(1);
			Vector u1 = StringUtils.parseData(t1, '$');

			String component = (String) (String) u1.elementAt(0);

			System.out.println("component: " + component);

			if (component.compareTo("JLabel") == 0) {
				label = (String) (String) u1.elementAt(1);
				label_count++;
				label_j = "label" + label_count;

			} else if (component.compareTo("JTextField") == 0) {
				String tf = (String) (String) u1.elementAt(0);
				tf_i = "tf" + i;
				if (u.size() > 2) {
					String t2 = (String) u.elementAt(2);
					Vector u2 = StringUtils.parseData(t2, '$');
					btn1 = (String) (String) u2.elementAt(1);
					btn_count++;
					btn_1 = "btn" + btn_count;
					btn2 = null;
					if (u.size() > 3) {
						String t3 = (String) u.elementAt(3);
						Vector u3 = StringUtils.parseData(t3, '$');
						btn2 = (String) (String) u3.elementAt(1);
						btn_count++;
						btn_2 = "btn" + btn_count;
					}
				}

			} else if (component.compareTo("JComboBox") == 0) {
				combo_count++;
				combo_i = "combobox" + combo_count;
				String t2 = (String) u.elementAt(2);
				Vector u2 = StringUtils.parseData(t2, '$');
				btn1 = (String) (String) u2.elementAt(1);
				btn_count++;
				btn_1 = "btn" + btn_count;
				btn2 = null;
				if (u.size() > 3) {
					String t3 = (String) u.elementAt(3);
					Vector u3 = StringUtils.parseData(t3, '$');
					btn2 = (String) (String) u3.elementAt(1);
					btn_count++;
					btn_2 = "btn" + btn_count;
				}
			}

			System.out.println("label_i: " + label_i);
			System.out.println("label_j: " + label_j);
			System.out.println("tf_i: " + tf_i);
			System.out.println("combo_i: " + combo_i);
			System.out.println("btn_1: " + btn_1);
			System.out.println("btn_2: " + btn_2);

			out.println("        gbc.gridx = " + col + ";");
			out.println("        gbc.gridy = " + row + ";");
			out.println("        panel.add(" + label_i + ", gbc" + ");");
			out.println("");
			col++;
			out.println("        gbc.gridx = " + col + ";");
			out.println("        gbc.gridy = " + row + ";");
			out.println("        gbc.insets = new Insets(3, 3, 3, 3);");

			if (component.compareTo("JTextField") == 0) {
				out.println("        panel.add(" + tf_i + ", gbc);");
			} else if (component.compareTo("JComboBox") == 0) {
				out.println("        panel.add(" + combo_i + ", gbc);");
			} else if (component.compareTo("JLabel") == 0) {
				out.println("        panel.add(" + label_j + ", gbc);");
			}
			out.println("");

			if (btn_1 != null) {
				System.out.println("btn_1 != null");
				col++;
				out.println("        gbc.gridx = " + col + ";");
				out.println("        gbc.gridy = " + row + ";");
				out.println("        gbc.insets = new Insets(3, 3, 3, 3);");
				out.println("        panel.add(" + btn_1 + ", gbc);");
				out.println("");
		    }

			if (btn_2 != null) {
				System.out.println("btn_2 != null");
				col++;
				out.println("        gbc.gridx = " + col + ";");
				out.println("        gbc.gridy = " + row + ";");
				out.println("        gbc.insets = new Insets(3, 3, 3, 3);");
				out.println("        panel.add(" + btn_2 + ", gbc);");
				out.println("");
		    }

		    row++;
		    col = 0;
			btn_1 = null;
			btn_2 = null;
			label_i = null;
			label_j = null;
			tf_i = null;
			combo_i = null;
			out.println("\t\t/////////////////////////////////////////////////");
		}
	}

	public void printBtnActionListener(PrintWriter out, String tf, String btn, String btnLabel) {
		out.println("        " + btn + ".addActionListener(new ActionListener() {");
		out.println("			public void actionPerformed(ActionEvent e) {");
		out.println("				String s = e.getActionCommand();");
		out.println("				if (s.equals(\"Browse\")) {");
		out.println("					" + tf + ".setText(\"\");");
		out.println("					JFileChooser fileChooser = new JFileChooser();");
		out.println("					fileChooser.setDialogTitle(\"Select Query File\");");
		out.println("					int userSelection = fileChooser.showOpenDialog(null);");
		out.println("					if (userSelection == JFileChooser.APPROVE_OPTION) {");
		out.println("						File fileToOpen = fileChooser.getSelectedFile();");
		out.println("						String inputfile = fileToOpen.getAbsolutePath();");

		out.println("					}");
		out.println("				} else if (s.equals(\"" + btnLabel + "\")) {");
		out.println("				     // <Add action statements here.>");
		out.println("				}");
		out.println("			}");
		out.println("        });");
	}

	public void printUIComponent(PrintWriter out, String label1, String label2, String combo, String[] options, String btnLabel1, String btnLabel2) {
		if (label1 != null) {
			labelCount++;
			out.println("        JLabel label" + labelCount + " = new JLabel(\"" + label1 + ":\");");
		}

		if (label2 != null) {
			labelCount++;
			out.println("        JLabel label" + labelCount + " = new JLabel(\"" + label2 + "\");");
		}

        if (combo != null) {
			comboboxCount++;
			out.println("\t\tString[] options" + comboboxCount + " = new String[]{");

			String t = "";
			for (int i=0; i<options.length-1; i++) {
				t = (String) options[i];
				out.println("\t\t\t\"" + t + "\",");
			}
			t = (String) options[options.length-1];
			out.println("\t\t\t\"" + t + "\"};");

			out.println("        JComboBox<String> combobox" + comboboxCount + " = new JComboBox<>(" + "options" + comboboxCount + ");");
			out.println("        " + "combobox" + comboboxCount + ".setSelectedIndex(0);");
		}

		if (btnLabel1 != null) {
			btnCount++;
			out.println("        JButton btn" + btnCount + " = new JButton(\"" + btnLabel1 + "\");");
		}

		if (btnLabel2 != null) {
			btnCount++;
			out.println("        JButton btn" + btnCount + " = new JButton(\"" + btnLabel2 + "\");");
			printBtnActionListener(out, null, "btn" + btnCount, "" + btnLabel2 + "");
		}
	}

	public void printUIComponent(PrintWriter out, String label1, String label2, String tf, String btnLabel1, String btnLabel2) {
		if (label1 != null) {
			labelCount++;
			out.println("        JLabel label" + labelCount + " = new JLabel(\"" + label1 + ":\");");
		}

		if (label2 != null) {
			labelCount++;
			out.println("        JLabel label" + labelCount + " = new JLabel(\"" + label2 + "\");");
		}

		if (tf != null) {
			jtCount++;
			out.println("        JTextField " + tf + " = new JTextField();");
			out.println("        " + tf + ".setPreferredSize(new Dimension(600, 30));");
		}

		if (btnLabel1 != null) {
			btnCount++;
			out.println("        JButton btn" + btnCount + " = new JButton(\"" + btnLabel1 + "\");");
			printBtnActionListener(out, tf, "btn" + btnCount, "" + btnLabel1 + "");
		}

		if (btnLabel2 != null) {
			btnCount++;
			out.println("        JButton btn" + btnCount + " = new JButton(\"" + btnLabel2 + "\");");
			printBtnActionListener(out, tf, "btn" + btnCount, "" + btnLabel2 + "");
		}
	}

	public static void main(String[] args) {
		String designFile = DESIGN_FILE;
		if (args.length == 1) {
			designFile = args[0];
		}
		new UIHelper(designFile).run();
	}

}

/* Sample ui_design.txt:
Test
JLabel$Hierarchy Data File|JTextField|JButton$Browse|JButton$View
JLabel$Branch Hierarchy Data File|JTextField|JButton$Browse|JButton$View
JLabel$Report Format|JComboBox$ASCII;Default Mutable Tree Node;MS Word;HTML|JButton$Generate
JLabel$Status|JLabel$
*/

