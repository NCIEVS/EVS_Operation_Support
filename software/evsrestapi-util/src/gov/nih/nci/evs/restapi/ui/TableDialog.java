package gov.nih.nci.evs.restapi.ui;
import gov.nih.nci.evs.restapi.util.*;

import java.io.*;
import java.util.*;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


public class TableDialog extends JDialog implements ActionListener {

	public TableDialog(String filename, boolean heading) {
        String title = "NCI Thesaurus";
        setUp(filename, title, heading);
	}

	public TableDialog(String filename, String title, boolean heading) {
        setUp(filename, title, heading);
	}

    public void setUp(String filename, String title, boolean heading) {
		setTitle(title);
		setSize(2400, 1200);

		addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent e) {

		    }
		});

		String[] columnNames = JTableMaker.getColumnNames(filename, heading);
		String[][] data = JTableMaker.loadTableData(filename, heading);
		JTable table = new JTable(data, columnNames);
		Container contentPane = getContentPane();
		JScrollPane scrollPane = new JScrollPane(table);
        contentPane.add(scrollPane, "Center");
	}

	public void actionPerformed(ActionEvent event) {

	}

    public static void main(String[] args) {
        String filename = args[0];
        String tableName = "Test JTableMaker";
        boolean heading = false;

		JDialog tableDialog = new TableDialog(filename, "Test Tree Dialog", heading);
		tableDialog.setSize(400, 300);
		tableDialog.setVisible(true);
	}
}
