package gov.nih.nci.evs.restapi.ui;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.config.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class ExactMatchUI {
	static String AXIOM_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.axiomfile;

	public static HashMap createCode2ValuesMap(String axiomfile, String req) {
		Vector v = Utils.readFile(axiomfile);
		HashMap hmap = new HashMap();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
            Vector u2 = StringUtils.parseData(req, '|');
            boolean include = true;
            for (int j=0; j<u2.size(); j++) {
				String t = (String) u2.elementAt(j);
				if (!u.contains(t)) {
					include = false;
					break;
				}
			}
			if (include) {
				String code = (String) u.elementAt(0);
				Vector w = new Vector();
				if (hmap.containsKey(code)) {
					w = (Vector) hmap.get(code);
				}
				String value = (String) u.elementAt(2);
				if (!w.contains(value)) {
					w.add(value);
				}
				hmap.put(code, w);
			}
	    }
	    return hmap;
	}

    public static HashMap createTerm2CodesMap(Vector v) {
		boolean caseSensitive = false;
		HashMap hmap = new HashMap();
        for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String prop_code = (String) u.elementAt(1);
			if (prop_code.compareTo("P90") == 0) {
				String term = (String) u.elementAt(2);
				term = HTMLDecoder.decode(term);
				String term_lc = term.toLowerCase();
				if (!caseSensitive) {
					term = term_lc;
				}
				Vector w = new Vector();
				if (hmap.containsKey(term)) {
					w = (Vector) hmap.get(term_lc);
				}
				String code = (String) u.elementAt(0);
				if (!w.contains(code)) {
					w.add(code);
				}
				hmap.put(term_lc, w);
			}
		}
		return hmap;
	}

    public static HashMap createTerm2CodesMap(String filename) {
		Vector v = Utils.readFile(filename);
		return createTerm2CodesMap(v);
	}

	public static String setupAndRunExactMatch(String axiomfile, String mappingTarget, String datafile, int col) {
		Vector dataVec = new Vector();
		dataVec.add("P90|P384$NCI|P383$PT");
		dataVec.add("P90|P384$NCI|P383$SY");
		System.out.println("Create ptMap ...");
		HashMap ptMap = createCode2ValuesMap(axiomfile, "P90|P384$NCI|P383$PT");
		System.out.println("Create syMap ...");
		HashMap syMap = createCode2ValuesMap(axiomfile, "P90|P384$NCI|P383$SY");
		HashMap term2CodesMap = null;

		System.out.println("Create term2CodesMap ...");
		if (mappingTarget == null || mappingTarget.length() == 0) {
			term2CodesMap = createTerm2CodesMap(axiomfile);
		} else {
			String reqs = mappingTarget;//"P90|P383$PT|P384$NCI";
			Vector w = DataRetrieval.extractRowsFromAxiomFile(reqs);
			term2CodesMap = createTerm2CodesMap(w);
		}
		Vector w = new Vector();
		Vector v = Utils.readFile(datafile);
		System.out.println("v: " + v.size());
		String line = (String) v.elementAt(0);
		char delim = '\t';
		if (line.indexOf("|") != -1) {
			delim = '|';
		}
		Vector u = StringUtils.parseData(line, delim);
		Utils.dumpVector(line, u);
		System.out.println("col: " + col);

		String heading = (String) u.elementAt(col);
		w.add(heading + "\t" + "Matched NCIt Code(s)	NCI PT(s)	NCI SY(s)");

		int lcv = 1;
		int increment = 1000;
		int k = -1;

		System.out.println("Start processing ...");
		for (int i=1; i<v.size(); i++) {
			if (lcv == increment) {
				k++;
				int k1 = k*increment;
				System.out.println("" + k1 + " out of "  + v.size() + " processed.");
				lcv = 0;
			}
			lcv++;
			line = (String) v.elementAt(i);
			u = StringUtils.parseData(line, delim);
			String term = (String) u.elementAt(col);
			String term0 = term;
			term = term.toLowerCase();
			if (term2CodesMap.containsKey(term)) {
				Vector codes = (Vector) term2CodesMap.get(term);
				String codeStr = Utils.vector2Delimited(codes, "|");
				StringBuffer ptBuf = new StringBuffer();
				StringBuffer syBuf = new StringBuffer();
				for (int j=0; j<codes.size(); j++) {
					String code = (String) codes.elementAt(j);
					if (ptMap.containsKey(code)) {
						ptBuf.append(Utils.vector2Delimited((Vector) ptMap.get(code), "|")).append("$");
					}
					if (syMap.containsKey(code)) {
						syBuf.append(Utils.vector2Delimited((Vector) syMap.get(code), "|")).append("$");
					}
				}
				String s1 = ptBuf.toString();
				if (s1.length() > 0) {
					s1 = s1.substring(0, s1.length()-1);
				}
				String s2 = syBuf.toString();
				if (s2.length() > 0) {
					s2 = s2.substring(0, s2.length()-1);
				}
				w.add(term0 + "\t" + codeStr + "\t" + s1 + "\t" + s2);
			} else {
				w.add(term0 + "\tNo match");
			}
		}
		System.out.println("" + v.size() + " out of "  + v.size() + " processed.");
		File f = new File(datafile);
		String filename = f.getName();
		String outputfile = "results_" + filename;
        Utils.saveToFile(outputfile, w);
        System.out.println(outputfile + " generated.");
        return outputfile;
	}

    public static void main(String[] args) {
        // Create the main frame
        JFrame frame = new JFrame("NCI Thesaurus Exact Match");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 200);
        frame.setLayout(new FlowLayout());

        JLabel label1 = new JLabel("Axiom File:");
		JTextField axiomFileTF = new JTextField();
		axiomFileTF.setPreferredSize(new Dimension(600, 30));
		axiomFileTF.setText(AXIOM_FILE);
        JButton browseBtn1 = new JButton("Browse");
        browseBtn1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Browse")) {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setDialogTitle("Select Axiom File");
					int userSelection = fileChooser.showOpenDialog(null);
					if (userSelection == JFileChooser.APPROVE_OPTION) {
						File fileToOpen = fileChooser.getSelectedFile();
						System.out.println("Selected file: " + fileToOpen.getAbsolutePath());
						String axiomfile = fileToOpen.getAbsolutePath();
						axiomFileTF.setText(axiomfile);
					}
				}
			}
        });

        JLabel label2 = new JLabel("Data File:");

		JTextField dataFileTF = new JTextField();
		dataFileTF.setPreferredSize(new Dimension(600, 30));
		dataFileTF.setText("");
		JLabel label5 = new JLabel("");

        JButton browseBtn2 = new JButton("Browse");
        browseBtn2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Browse")) {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setDialogTitle("Select Data File");
					int userSelection = fileChooser.showOpenDialog(null);
					if (userSelection == JFileChooser.APPROVE_OPTION) {
						File fileToOpen = fileChooser.getSelectedFile();
						System.out.println("Selected file: " + fileToOpen.getAbsolutePath());
						dataFileTF.setText(fileToOpen.getAbsolutePath());
						label5.setText("");
					}
				}
			}
        });

        JLabel label3 = new JLabel("Maps To:");
		JTextField mapsToTF = new JTextField();
		mapsToTF.setText("");
		mapsToTF.setPreferredSize(new Dimension(200, 30));

        String[] columns = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        JComboBox<String> columnCombo = new JComboBox<>(columns);

        columnCombo.setSelectedIndex(0);
        JLabel label4 = new JLabel("Term Column:");

        JLabel label7 = new JLabel("Example: P90|P383$PT|P384$NCI");

        JButton generateBtn = new JButton("Generate");
        generateBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Generate")) {
					label5.setText("");
					String axiomfile = axiomFileTF.getText();
					String mapsTo = mapsToTF.getText();
					String datafile = dataFileTF.getText();
					int col = Integer.parseInt((String) columnCombo.getSelectedItem());
					System.out.println("axiomfile: " + axiomfile);
					System.out.println("datafile: " + datafile);
					if (datafile.compareTo("") == 0) {
						label5.setText("Please specify a data file.");
					} else {
					    File f = new File(datafile);
					    if (!f.exists()) {
							label5.setText(f.getName() + " does not exist");
						} else {
							label5.setText("");
							System.out.println("mapsTo: " + mapsTo);
							System.out.println("col: " + col);
							String outputfile = setupAndRunExactMatch(axiomfile, mapsTo, datafile, col);
							label5.setText(outputfile + " generated.");
				    	}
					}
				}
			}
        });

        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(label1, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(axiomFileTF, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(browseBtn1, gbc);

        ///////////////////////////////////////////

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(label2, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(dataFileTF, gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(browseBtn2, gbc);

        ///////////////////////////////////////////

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(label3, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(mapsToTF, gbc);

        gbc.gridx = 2;
        gbc.gridy = 2;
        panel.add(label7, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(label4, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(columnCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(generateBtn, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        panel.add(label5, gbc);

        frame.add(panel);

        // Make the frame visible
        frame.setVisible(true);
    }
}
