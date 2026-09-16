package gov.nih.nci.evs.restapi.ui;
import gov.nih.nci.evs.restapi.appl.*;
import gov.nih.nci.evs.restapi.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/*
download|0
Maps To ICDO|0
EVS Statistics|0
GDC Diff|0
FDA Count|1
PrimitiveConcepts|0
Linguistic Analysis|0
3D Pie Chart|0
Pediatric Subset Reports|0
Is-A Hierarchy QA|0
*/

public class ReportSubmitterUI extends JFrame implements ItemListener {
   JCheckBox cb1, cb2, cb3, cb4, cb5, cb6, cb7, cb8, cb9, cb10;
   JLabel lbl1, lbl2, lbl3, lbl4, lbl5, lbl6, lbl7, lbl8, lbl9, lbl10;

   boolean cb1_selected = false;
   boolean cb2_selected = false;
   boolean cb3_selected = false;
   boolean cb4_selected = false;
   boolean cb5_selected = false;
   boolean cb6_selected = false;
   boolean cb7_selected = false;
   boolean cb8_selected = false;
   boolean cb9_selected = false;
   boolean cb10_selected = false;

   JButton executeButton = null;
   JButton closeButton = null;

   public ReportSubmitterUI() {
		setTitle("ReportSubmitter UI");
		setLayout(new FlowLayout());

		cb1 = new JCheckBox("Download");
		cb2 = new JCheckBox("Maps To ICDO", true); // pre-selected
		cb3 = new JCheckBox("EVS Statistics");
		cb4 = new JCheckBox("GDC Diff");
		cb5 = new JCheckBox("FDA Count");
		cb6 = new JCheckBox("PrimitiveConcepts");
		cb7 = new JCheckBox("Linguistic Analysis");
		cb8 = new JCheckBox("3D Pie Chart");
		cb9 = new JCheckBox("Pediatric Subset Reports");
		cb10 = new JCheckBox("Is-A Hierarchy QA");

		cb1.addItemListener(this);
		cb2.addItemListener(this);
		cb3.addItemListener(this);
		cb4.addItemListener(this);
		cb5.addItemListener(this);
		cb6.addItemListener(this);
		cb7.addItemListener(this);
		cb8.addItemListener(this);
		cb9.addItemListener(this);
		cb10.addItemListener(this);

		//lbl1 = new JLabel("Option 1 not selected");
		//lbl2 = new JLabel("Option 2 selected");

		add(cb1);
		add(cb2);
		add(cb3);
		add(cb4);
		add(cb5);
		add(cb6);
		add(cb7);
		add(cb8);
		add(cb9);
		add(cb10);

		executeButton = new JButton("Generate");
		executeButton.addActionListener((ActionEvent e) -> {
			if (cb1.isSelected()) {
				ReportSubmitter.generateReport("download");
			}
			if (cb2.isSelected()) {
				ReportSubmitter.generateReport("Maps To ICDO");
			}
			if (cb3.isSelected()) {
				ReportSubmitter.generateReport("EVS Statistics");
			}
			if (cb4.isSelected()) {
				ReportSubmitter.generateReport("GDC Diff");
			}
			if (cb5.isSelected()) {
				ReportSubmitter.generateReport("FDA Count");
			}
			if (cb6.isSelected()) {
				ReportSubmitter.generateReport("PrimitiveConcepts");
			}
			if (cb7.isSelected()) {
				ReportSubmitter.generateReport("Linguistic Analysis");
			}
			if (cb8.isSelected()) {
				ReportSubmitter.generateReport("3D Pie Chart");
			}
			if (cb9.isSelected()) {
				ReportSubmitter.generateReport("Pediatric Subset Reports");
			}
			if (cb10.isSelected()) {
				ReportSubmitter.generateReport("Is-A Hierarchy QA");
			}
		});

		closeButton = new JButton("Exit");
		closeButton.addActionListener((ActionEvent e) -> {
            this.dispose();
		});

		add(executeButton);
		add(closeButton);

		setSize(800, 120);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
   }

   @Override
   public void itemStateChanged(ItemEvent e) {
       if (e.getSource() == cb1) {
		   cb1_selected = false;
		   if (cb1.isSelected()) {
			   cb1_selected = true;
		   }
       } else if (e.getSource() == cb2) {
		   cb2_selected = false;
		   if (cb2.isSelected()) {
			   cb2_selected = true;
		   }
       } else if (e.getSource() == cb3) {
		   cb3_selected = false;
		   if (cb3.isSelected()) {
			   cb3_selected = true;
		   }
       } else if (e.getSource() == cb4) {
		   cb4_selected = false;
		   if (cb4.isSelected()) {
			   cb4_selected = true;
		   }
       } else if (e.getSource() == cb5) {
		   cb5_selected = false;
		   if (cb5.isSelected()) {
			   cb5_selected = true;
		   }
       } else if (e.getSource() == cb6) {
		   cb6_selected = false;
		   if (cb6.isSelected()) {
			   cb6_selected = true;
		   }
       } else if (e.getSource() == cb7) {
		   cb7_selected = false;
		   if (cb7.isSelected()) {
			   cb7_selected = true;
		   }
       } else if (e.getSource() == cb8) {
		   cb8_selected = false;
		   if (cb8.isSelected()) {
			   cb8_selected = true;
		   }
       } else if (e.getSource() == cb9) {
		   cb9_selected = false;
		   if (cb9.isSelected()) {
			   cb9_selected = true;
		   }
       } else if (e.getSource() == cb10) {
		   cb10_selected = false;
		   if (cb10.isSelected()) {
			   cb10_selected = true;
		   }
       }
   }

   public static void main(String[] args) {
       SwingUtilities.invokeLater(ReportSubmitterUI::new);
   }
}