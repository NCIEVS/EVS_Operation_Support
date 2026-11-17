package gov.nih.nci.evs.restapi.ui;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.config.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.text.*;

public class ReportTemplate {
	static HashMap annotationPropertyCode2LabelMap = null;
	static HashMap annotationPropertyLabel2CodeMap = null;
	static HashMap objectPropertyCode2LabelMap = null;
	static HashMap objectPropertyLabel2CodeMap = null;
	static String[] annotationPropertyLabels = null;
	static int conceptInSubsetIndex = -1;
	static HashMap templateDataMap = new HashMap();

	static {
		annotationPropertyCode2LabelMap = NCItProperties.getAnnotationPropertyCode2LabelMap();
        annotationPropertyLabel2CodeMap = NCItProperties.getAnnotationPropertyLabel2CodeMap();
        int n = annotationPropertyLabel2CodeMap.keySet().size();
        annotationPropertyLabels = new String[n];
        Vector v = new Vector();
        Iterator it = annotationPropertyLabel2CodeMap.keySet().iterator();
        while (it.hasNext()) {
			String key = (String) it.next();
			v.add(key);
		}
		v = new SortUtils().quickSort(v);
        for (int i=0; i<v.size(); i++) {
			String key = (String) v.elementAt(i);
			if (key.compareTo("Concept_In_Subset") == 0) {
				conceptInSubsetIndex = i;
			}
			annotationPropertyLabels[i] = key;
		}

        objectPropertyCode2LabelMap = NCItProperties.getObjectPropertyCode2LabelMap();
        objectPropertyLabel2CodeMap = NCItProperties.getObjectPropertyLabel2CodeMap();
	}

    public static String reformatTemplateData(String filename) {
		Vector w = new Vector();
		Vector v = Utils.readFile(filename);
		String firstLn = (String) v.elementAt(0);
		Vector u = StringUtils.parseData(firstLn, '|');
		String name = (String) u.elementAt(0);
		String outputfile = name.replace(" ", "_") + "_v0.template";
		w.add("name:" + (String) u.elementAt(0) +
		      "|type:" + (String) u.elementAt(1) +
		      "|rootConceptCode:" + (String) u.elementAt(2) +
		      "|association:" + (String) u.elementAt(3) +
		      "|level:" + (String) u.elementAt(4) +
		      "|sortColumn:" + (String) u.elementAt(5));
		for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			u = StringUtils.parseData(line, '|');
			w.add("label:" + (String) u.elementAt(0) +
				  "|display:" + (String) u.elementAt(1) +
				  "|propertyType:" + (String) u.elementAt(2) +
				  "|property:" + (String) u.elementAt(3) +
				  "|source:" + (String) u.elementAt(4) +
				  "|group:" + (String) u.elementAt(5) +
				  "|attr:" + (String) u.elementAt(6));
		}
		Utils.saveToFile(outputfile, w);
		return outputfile;
	}

    public static String run(String inputfile) {
		inputfile = reformatTemplateData(inputfile);
		String outputfile = inputfile.replace("_v0", "");
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(outputfile, "UTF-8");
			String indent = "    ";
			Vector v = Utils.readFile(inputfile);
			String heading = (String) v.elementAt(0);
			Vector u = StringUtils.parseData(heading, '|');
			for (int i=0; i<u.size(); i++) {
				String t = (String) u.elementAt(i);
				t = t.replace(":", ": ");
				pw.println(t);
			}
			pw.println("columns:");
			for (int i=1; i<v.size(); i++) {
				pw.println("  - columnNumber: " + i);
				String t = (String) v.elementAt(i);
				t = t.trim();
				u = StringUtils.parseData(t, '|');
				for (int j=0; j<u.size(); j++) {
					String s = (String) u.elementAt(j);
					s = s.replace(":", ": ");
					pw.println(indent + s);
				}
				if (i<v.size()-1) pw.print("\n");
			}
		} catch (Exception ex) {

		} finally {
			try {
				pw.close();
				System.out.println("Template file " + outputfile + " generated.");
				return outputfile;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}


    public static void main(String[] args) {
        // Create the main frame
        JFrame frame = new JFrame("Report Template UI");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 200);
        frame.setLayout(new FlowLayout());
        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel label15 = new JLabel("Status");
        JLabel label16 = new JLabel("");

        JPanel paginationPanel = new JPanel();
        layout = new GridBagLayout();
        paginationPanel.setLayout(layout);
        GridBagConstraints gbc_a = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel page_label = new JLabel("Column 0");
        JButton btn_a1 = new JButton(" < ");
        btn_a1.setEnabled(false);
        JButton btn_a2 = new JButton(" > ");
        page_label.setPreferredSize(new Dimension(80, 30));

        gbc_a.gridx = 0;
        gbc_a.gridy = 0;
        gbc_a.insets = new Insets(3, 3, 3, 3);
        paginationPanel.add(page_label, gbc);

        gbc_a.gridx = 1;
        gbc_a.gridy = 0;
        gbc_a.insets = new Insets(3, 3, 3, 3);

        paginationPanel.add(btn_a1, gbc);

        gbc_a.gridx = 2;
        gbc_a.gridy = 0;
        gbc_a.insets = new Insets(3, 3, 3, 3);

        paginationPanel.add(btn_a2, gbc);

        JButton btn_a = new JButton("Save");

        JLabel label1 = new JLabel("Name");
        JTextField tf1 = new JTextField();
        tf1.setPreferredSize(new Dimension(600, 30));
        tf1.setText("");
        //tf1.setText("EDQM Characteristics Prototype");

        JLabel label2 = new JLabel("Type");
        JTextField tf2 = new JTextField();
        tf2.setPreferredSize(new Dimension(600, 30));
        tf2.setText("Association");

        JLabel label3 = new JLabel("Root Concept Code");
        JTextField tf3 = new JTextField();
        tf3.setPreferredSize(new Dimension(600, 30));
        //tf3.setText("C150103");
        tf3.setText("");
        JLabel label4 = new JLabel("Association");
		String[] options1 = annotationPropertyLabels;
        JComboBox<String> combobox1 = new JComboBox<>(options1);
        combobox1.setSelectedIndex(conceptInSubsetIndex);
        JLabel label5 = new JLabel("Level");
		String[] options2 = new String[]{
			"0",
			"1",
			"2",
			"3",
			"4",
			"5",
			"6",
			"7",
			"8",
			"9",
			"10"};
        JComboBox<String> combobox2 = new JComboBox<>(options2);
        combobox2.setSelectedIndex(1);
        JLabel label6 = new JLabel("Sort Column");
		String[] options3 = new String[]{
			"0",
			"1",
			"2",
			"3",
			"4",
			"5",
			"6",
			"7",
			"8",
			"9",
			"10"};
        JComboBox<String> combobox3 = new JComboBox<>(options3);
        combobox3.setSelectedIndex(2);
        JLabel label7 = new JLabel("");
        JLabel label8 = new JLabel("Enter Report Column Data");
        JLabel label9 = new JLabel("Label");
        JTextField tf4 = new JTextField();
        tf4.setPreferredSize(new Dimension(600, 30));
        JLabel label10 = new JLabel("Display");
		String[] options0 = new String[]{
			"code", "property", "subsource_code"};
        JComboBox<String> combobox4a = new JComboBox<>(options0);
        JLabel label11 = new JLabel("Property Type");
		String[] options4 = new String[] {
			"",
			"ALT_DEFINITION",
			"Associated Concept Code",
			"Associated Concept Property",
			"code",
			"DEFINITION",
			"Disease_May_Have_Cytogenetic_Abnormality",
			"FULL_SYN",
			"FULL_SYN Axiom",
			"Has OORO_PC_VALUE",
			"Has_CTDC_Value",
			"Has_DIPG_DMG_Authorized_Value",
			"Has_GDC_Value",
			"Has_ICDC_Value",
			"Has_PCDC_ALL_Authorized_Value",
			"Has_PCDC_AML_Authorized_Value",
			"Has_PCDC_Data_Type",
			"Has_PCDC_EWS_Authorized_Value",
			"Has_PCDC_GCT_Authorized_Value",
			"Has_PCDC_OS_Authorized_Value",
			"Has_Ped-PRO-CTCAE_Attribute",
			"Has_Ped-PRO-CTCAE_Attribute_Value",
			"Has_Ped-PRO-CTCAE_Parent",
			"Has_Pharmaceutical_Administration_Method",
			"Has_Pharmaceutical_Basic_Administrable_Dose_Form",
			"Has_Pharmaceutical_Basic_Dose_Form",
			"Has_Pharmaceutical_Intended_Site",
			"Has_Pharmaceutical_Release_Characteristics",
			"Has_Pharmaceutical_State_Of_Matter",
			"Has_Pharmaceutical_Transformation",
			"Has_PRO-CTCAE_Attribute",
			"Has_PRO-CTCAE_Attribute_Value",
			"Has_PRO-CTCAE_Parent",
			"Has_SeroNet_Authorized_Value",
			"Is_PCDC_ALL_Authorized_Value_for_Variable",
			"Is_PCDC_AML_Authorized_Value_For_Variable",
			"Is_PCDC_EWS_Authorized_Value_For_Variable",
			"Is_PCDC_GCT_Authorized_Value_for_Variable",
			"Is_Value_For_GDC_Property",
			"Maps_To Axiom",
			"Molecular_Abnormality_Involves_Gene",
			"NCI_Drug_Dictionary_ID",
			"property",
			"Related_To_Genetic_Biomarker",
			"Value_Set_Is_Paired_With"};

        JComboBox<String> combobox4 = new JComboBox<>(options4);
        combobox4.setSelectedIndex(0);
        JButton btn1 = new JButton("Save");

        JLabel label12 = new JLabel("Property Target");
		String[] options5 = new String[] {
			"",
			"Accepted_Therapeutic_Use_For(P302)",
			"ALT_DEFINITION(P325)",
			"attr(P381)",
			"BioCarta_ID(P216)",
			"CAS_Registry(P210)",
			"CHEBI_ID(P368)",
			"Chemical_Formula(P350)",
			"code(NHC0)",
			"Concept_In_Subset(A8)",
			"Concept_Status(P310)",
			"Contributing_Source(P322)",
			"def-definition(P377)",
			"def-source(P378)",
			"Def_Curator(P318)",
			"DEFINITION(P97)",
			"Definition_Review_Date(P380)",
			"Definition_Reviewer_Name(P379)",
			"DesignNote(P98)",
			"Disease_May_Have_Cytogenetic_Abnormality(R114)",
			"Display_Name(P107)",
			"Editor_Note(P95)",
			"EntrezGene_ID(P321)",
			"Essential_Amino_Acid(P356)",
			"Essential_Fatty_Acid(P357)",
			"Extensible_List(P361)",
			"FDA_Table(P317)",
			"FDA_UNII_Code(P319)",
			"FULL_SYN(P90)",
			"GenBank_Accession_Number(P102)",
			"Gene_Encodes_Product(P96)",
			"go-evi(P389)",
			"go-id(P387)",
			"go-source(P390)",
			"go-term(P388)",
			"GO_Annotation(P211)",
			"Has OORO_PC_VALUE(A42)",
			"Has_CDRH_Parent(A10)",
			"Has_Data_Element(A12)",
			"Has_DIPG_DMG_Authorized_Value(A40)",
			"Has_Free_Acid_Or_Base_Form(A6)",
			"Has_ICDC_Value(A30)",
			"Has_NICHD_Parent(A11)",
			"Has_PCDC_ALL_Authorized_Value(A36)",
			"Has_PCDC_AML_Authorized_Value(A26)",
			"Has_PCDC_Data_Type(A23)",
			"Has_PCDC_EWS_Authorized_Value(A29)",
			"Has_PCDC_GCT_Authorized_Value(A34)",
			"Has_PCDC_HL_Authorized_Value(A39)",
			"Has_PCDC_OS_Authorized_Value(A38)",
			"Has_Ped-PRO-CTCAE_Attribute(A50)",
			"Has_Ped-PRO-CTCAE_Attribute_Value(A52)",
			"Has_Ped-PRO-CTCAE_Parent(A54)",
			"Has_Pharmaceutical_Administration_Method(A19)",
			"Has_Pharmaceutical_Basic_Administrable_Dose_Form(A41)",
			"Has_Pharmaceutical_Basic_Dose_Form(A18)",
			"Has_Pharmaceutical_Intended_Site(A20)",
			"Has_Pharmaceutical_Release_Characteristics(A21)",
			"Has_Pharmaceutical_State_Of_Matter(A17)",
			"Has_Pharmaceutical_Transformation(A22)",
			"Has_PRO-CTCAE_Attribute(A49)",
			"Has_PRO-CTCAE_Attribute_Value(A51)",
			"Has_PRO-CTCAE_Parent(A53)",
			"Has_Salt_Form(A5)",
			"Has_SeroNet_Authorized_Value(A37)",
			"Has_Target(A7)",
			"hasDbXref(hasDbXref)",
			"HGNC_ID(P369)",
			"Homologous_Gene(P101)",
			"ICD-O-3_Code(P334)",
			"Image_Link(P167)",
			"In_Clinical_Trial_For(P303)",
			"IND_Code(P304)",
			"INFOODS(P353)",
			"Is_PCDC_ALL_Authorized_Value_for_Variable(A35)",
			"Is_PCDC_AML_Authorized_Value_For_Variable(A24)",
			"Is_PCDC_EWS_Authorized_Value_For_Variable(A28)",
			"Is_PCDC_GCT_Authorized_Value_for_Variable(A33)",
			"Is_Related_To_Endogenous_Product(A9)",
			"KEGG_ID(P215)",
			"Legacy_Concept_Name(P366)",
			"Macronutrient(P360)",
			"Maps_To(P375)",
			"Merge_Into(NHC3)",
			"Merge_Source(P327)",
			"Merge_Target(P328)",
			"MGI_Accession_ID(P332)",
			"Micronutrient(P359)",
			"miRBase_ID(P362)",
			"Molecular_Abnormality_Involves_Gene(R177)",
			"NCBI_Taxon_ID(P331)",
			"NCI_Drug_Dictionary_ID(P399)",
			"NCI_META_CUI(P208)",
			"Neoplastic_Status(P363)",
			"NICHD_Hierarchy_Term(P371)",
			"NSC_Code(P175)",
			"Nutrient(P358)",
			"OID(P320)",
			"OLD_ASSOCIATION(P364)",
			"OLD_CHILD(P201)",
			"OLD_KIND(P203)",
			"OLD_PARENT(P200)",
			"OLD_ROLE(P204)",
			"OLD_SOURCE_ASSOCIATION(P365)",
			"OLD_SOURCE_ROLE(P202)",
			"OLD_STATE(P205)",
			"OMIM_Number(P100)",
			"PDQ_Closed_Trial_Search_ID(P330)",
			"PDQ_Open_Trial_Search_ID(P329)",
			"PID_ID(P367)",
			"Preferred_Name(P108)",
			"Project_Name(P370)",
			"Publish_Value_Set(P372)",
			"PubMedID_Primary_Reference(P171)",
			"Qualifier_Applies_To(A4)",
			"Related_To_Genetic_Biomarker(A13)",
			"Relationship_to_Target(P393)",
			"Relative_Enzyme_Activity(P316)",
			"Role_Has_Domain(A1)",
			"Role_Has_Parent(A3)",
			"Role_Has_Range(A2)",
			"Semantic_Type(P106)",
			"SNP_ID(P315)",
			"source-code(P385)",
			"source-date(P391)",
			"Split_From(NHC4)",
			"Subsource(P92)",
			"subsource-name(P386)",
			"Swiss_Prot(P93)",
			"Target_Code(P395)",
			"Target_Term(P392)",
			"Target_Term_Type(P394)",
			"Target_Terminology(P396)",
			"term-group(P383)",
			"term-name(P382)",
			"term-source(P384)",
			"Term_Browser_Value_Set_Description(P376)",
			"Tolerable_Level(P352)",
			"TVS_Location(P373)",
			"UMLS_CUI(P207)",
			"Unit(P355)",
			"US_Recommended_Intake(P351)",
			"USDA_ID(P354)",
			"Use_For(P333)",
			"Value_Set_Location(P374)"
			};
        JComboBox<String> combobox5 = new JComboBox<>(options5);
        combobox5.setSelectedIndex(0);
        JButton btn2 = new JButton("View");
        btn2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Browse")) {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setDialogTitle("Select File");
					File defaultDirectory = new File(System.getProperty("user.dir"));
					fileChooser.setCurrentDirectory(defaultDirectory);
					int userSelection = fileChooser.showOpenDialog(null);
					if (userSelection == JFileChooser.APPROVE_OPTION) {
						File fileToOpen = fileChooser.getSelectedFile();
						String inputfile = fileToOpen.getAbsolutePath();
					}
				} else {
				     // <Add action statements here.>
				}
			}
        });
        JLabel label13 = new JLabel("Source");
		String[] options6 = new String[] {
			"",
			"ACC/AHA",
			"BIOCARTA",
			"BLOODPAC",
			"BRIDG",
			"BRIDG 3.0.3",
			"BRIDG 5.3",
			"caDSR",
			"CareLex",
			"CBDD",
			"CCPS",
			"CDC",
			"CDISC",
			"CDISC-GLOSS",
			"Cellosaurus",
			"CPTAC",
			"CRCH",
			"CTCAE",
			"CTCAE 3.0",
			"CTCAE 5.0",
			"CTCAE 6.0",
			"CTDC",
			"CTEP",
			"CTRM",
			"CTRP",
			"DCP",
			"DICOM",
			"DIPG/DMG",
			"DTP",
			"EDQM-HC",
			"FDA",
			"GAIA",
			"GCT",
			"GDC",
			"GENC",
			"HemOnc",
			"HGNC",
			"HL7",
			"ICD10 2016",
			"ICD10CM 2017",
			"ICD9CM 2014",
			"ICDC",
			"ICDO3",
			"ICH",
			"IMDRF",
			"INC",
			"JAX",
			"KEGG",
			"mCode",
			"MedDRA",
			"NCI",
			"NCI-GLOSS",
			"NCICB",
			"NCPDP",
			"NICHD",
			"OORO",
			"PCDC",
			"PI-RADS",
			"PID",
			"PQCMC",
			"PRO-CTCAE",
			"RENI",
			"SEER",
			"Seronet",
			"SPL",
			"UBERON",
			"UCUM",
			"Unified Code for Units of Measure",
			"ZFin",
			"None"};
        JComboBox<String> combobox6 = new JComboBox<>(options6);
        combobox6.setSelectedIndex(0);
        JButton btn3 = new JButton("Reset");


/////////////////////////////////////////////////////////////////////////
        JLabel label13a = new JLabel("Group");
		String[] options6a = new String[] {
			"",
			"AB",
			"AD",
			"AQ",
			"AQS",
			"BR",
			"CA2",
			"CA3",
			"CN",
			"CNU",
			"CS",
			"DN",
			"FB",
			"HD",
			"PT",
			"SN",
			"SY",
			"None"
			};

        JComboBox<String> combobox6a = new JComboBox<>(options6a);
        combobox6a.setSelectedIndex(0);

        JLabel label14 = new JLabel("subsource");
		String[] options7 = new String[] {
			"",
			"ALL",
			"AML",
			"caDSR",
			"Canine Glioma",
			"CDRH",
			"CDRH-GUDID",
			"CTRP",
			"Dosage Form (C-DRG-00201)",
			"eCTD",
			"eManu",
			"EWS",
			"GCT",
			"HL7",
			"HL7 CDA",
			"ICD-10 CM",
			"ICDC",
			"ICSR",
			"ICSR-CVM",
			"LOINC",
			"Marketing Category (C-DRG-0935)",
			"mCode",
			"OS",
			"Package Type (C-DRG-00907)",
			"PC",
			"PCC",
			"Ped",
			"pFDA",
			"Plant Part (C-DRG-00924)",
			"Potency (C-DRG-00501)",
			"PQCMC",
			"PRECINCT",
			"Pro",
			"PRO-CTCAE",
			"QM",
			"Route of Administration (C-DRG-00301)",
			"RPS",
			"SARS2",
			"SDC",
			"SNOMED CT",
			"SPL",
			"Stability",
			"TCGA",
			"Unified Code for Units of Measure",
			"UNII",
			"None"};

        JComboBox<String> combobox7 = new JComboBox<>(options7);
        combobox7.setSelectedIndex(0);

        JLabel label14a = new JLabel("Attr");
		String[] options7a = new String[] {
			"",
			"CDRH",
			"CDRH-GUDID",
			"eCTD",
			"eManu",
			"ICSR",
			"PCC",
			"PQCMC",
			"SARS2",
			"Stability",
			"None"};
        JComboBox<String> combobox7a = new JComboBox<>(options7a);
        combobox7a.setSelectedIndex(0);

        JButton btn4 = new JButton("Generate Report Template");
        btn4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				label16.setText("");
				int numRows = templateDataMap.keySet().size();
				if (numRows < 2) {
					label16.setText("Please complete data entries. Report template must contains at least one column.");
				}
				Vector w = new Vector();
				String s = e.getActionCommand();
				if (s.equals("Generate Report Template")) {
				     int n = templateDataMap.keySet().size();
				     for (int i=0; i<n; i++) {
						 Integer int_obj = Integer.valueOf(i);
						 String line = (String) templateDataMap.get(int_obj);
						 w.add(line);
					 }
				}
				String rptLine = (String) w.elementAt(0);
				Vector u = StringUtils.parseData(rptLine, '|');
				String name = (String) u.elementAt(0);
				String outputfile = name.replace(" ", "_") + "_template.txt";
				Utils.saveToFile(outputfile, w);
				String templatefile = run(outputfile);
				System.out.println("Template file " + templatefile + " generated.");
				label16.setText("Template file " + templatefile + " generated.");
			}
        });


        btn_a.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Save")) {
					String name = tf1.getText();
					if (name.compareTo("") == 0) {
						label16.setText("Please specify a template name.");
					} else {
						Integer int_obj = Integer.valueOf(0);
						String line = tf1.getText() + "|" + tf2.getText() + "|" + tf3.getText()
													+ "|" + (String) combobox1.getSelectedItem()
													+ "|" + (String) combobox2.getSelectedItem()
													+ "|" + (String) combobox3.getSelectedItem();
						templateDataMap.put(int_obj, line);
						System.out.println(line);
					}
				}
			}
        });

        btn_a1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals(" < ")) {
					String t = page_label.getText();
					Vector u = StringUtils.parseData(t, ' ');
					s = (String) u.elementAt(1);
					int col_num = Integer.parseInt(s);
					col_num--;
					page_label.setText("Column " + col_num);
					if (col_num == 0) {
						btn_a1.setEnabled(false);
					} else {
						btn_a1.setEnabled(true);
					}
					btn_a2.setEnabled(true);
				}
			}
        });

        btn1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Save")) {
					label16.setText("");
					String columnLabel = tf4.getText();
					if (columnLabel.length() == 0) {
						label16.setText("No column label is specified.");
					} else {
						String t = page_label.getText();
						Vector u = StringUtils.parseData(t, ' ');
						s = (String) u.elementAt(1);
						int col_num = Integer.parseInt(s);
						col_num++;
						String line = tf4.getText() + "|" + (String) combobox4a.getSelectedItem()
													+ "|" + (String) combobox4.getSelectedItem()
													+ "|" + (String) combobox5.getSelectedItem()
													+ "|" + (String) combobox6.getSelectedItem()
													+ "|" + (String) combobox6a.getSelectedItem()
													+ "|" + (String) combobox7.getSelectedItem()
													+ "|" + (String) combobox7a.getSelectedItem();
						templateDataMap.put(Integer.valueOf(col_num), line);
						System.out.println(line);
						btn_a1.setEnabled(true);
					}
				}
			}
        });


        btn_a2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals(" > ")) {
					String t = page_label.getText();
					Vector u = StringUtils.parseData(t, ' ');
					s = (String) u.elementAt(1);
					int col_num = Integer.parseInt(s);
					col_num++;
					int n = templateDataMap.keySet().size();
					int numberOfColumns = n-1;
					page_label.setText("Column " + col_num);
					if (col_num == numberOfColumns-1) {
						btn_a2.setEnabled(false);
					} else {
						btn_a2.setEnabled(true);
					}
					btn_a1.setEnabled(true);
				}
			}
        });

        btn3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = e.getActionCommand();
				if (s.equals("Reset")) {
				     // <Add action statements here.>
				    tf4.setText("");
				    combobox4a.setSelectedIndex(0);
				    combobox4.setSelectedIndex(0);
				    combobox4a.setSelectedIndex(0);
				    combobox5.setSelectedIndex(0);
				    combobox6.setSelectedIndex(0);
				    combobox6a.setSelectedIndex(0);
				    combobox7.setSelectedIndex(0);
				    combobox7a.setSelectedIndex(0);
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
        panel.add(tf1, gbc);
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btn_a, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label2, gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(tf2, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label3, gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(tf3, gbc);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label4, gbc);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(combobox1, gbc);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label5, gbc);
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(combobox2, gbc);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label6, gbc);
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(combobox3, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label7, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label8, gbc);
        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(paginationPanel, gbc);
        gbc.gridx = 2;
        gbc.gridy = 7;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btn1, gbc);

        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label9, gbc);
        gbc.gridx = 1;
        gbc.gridy = 8;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(tf4, gbc);

        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label10, gbc);
        gbc.gridx = 1;
        gbc.gridy = 9;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(combobox4a, gbc);

        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label12, gbc);
        gbc.gridx = 1;
        gbc.gridy = 10;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(combobox5, gbc);

        gbc.gridx = 0;
        gbc.gridy = 11;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label11, gbc);
        gbc.gridx = 1;
        gbc.gridy = 11;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(combobox4, gbc);

        gbc.gridx = 0;
        gbc.gridy = 12;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label12, gbc);
        gbc.gridx = 1;
        gbc.gridy = 12;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(combobox5, gbc);

        /*
        gbc.gridx = 2;
        gbc.gridy = 12;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btn2, gbc);
        */

        gbc.gridx = 0;
        gbc.gridy = 13;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label13, gbc);
        gbc.gridx = 1;
        gbc.gridy = 13;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(combobox6, gbc);
        gbc.gridx = 2;
        gbc.gridy = 13;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btn3, gbc);

        gbc.gridx = 0;
        gbc.gridy = 14;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label13, gbc);
        gbc.gridx = 1;
        gbc.gridy = 14;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(combobox6, gbc);


        gbc.gridx = 0;
        gbc.gridy = 15;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label13a, gbc);
        gbc.gridx = 1;
        gbc.gridy = 15;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(combobox6a, gbc);

        gbc.gridx = 0;
        gbc.gridy = 16;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label14a, gbc);
        gbc.gridx = 1;
        gbc.gridy = 16;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(combobox7a, gbc);
        gbc.gridx = 2;
        gbc.gridy = 16;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btn3, gbc);

        gbc.gridx = 1;
        gbc.gridy = 17;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(btn4, gbc);

        gbc.gridx = 0;
        gbc.gridy = 18;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label15, gbc);
        gbc.gridx = 1;
        gbc.gridy = 18;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label16, gbc);

        frame.add(panel);
        // Make the frame visible
        frame.setVisible(true);
    }
}
