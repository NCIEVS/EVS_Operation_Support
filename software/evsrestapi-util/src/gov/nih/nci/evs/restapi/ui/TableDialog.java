package gov.nih.nci.evs.restapi.ui;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.config.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class TableDialog extends JDialog {

	public static String[] getTableColumnNames(String datafile) {
		char delim = '|';
		Vector v = Utils.readFile(datafile);
		String firstLine = (String) v.elementAt(0);
		Vector u0 = StringUtils.parseData(firstLine, delim);
		String[] columns = new String[u0.size()];
		for (int i=0; i<u0.size(); i++) {
			String t = (String) u0.elementAt(i);
			columns[i] = t;
		}
		return columns;
	}

	public static Object[][] getTableData(String datafile) {
		char delim = '|';
		//int num_cols = columns.length;
		Vector v = Utils.readFile(datafile);
		String firstLine = (String) v.elementAt(0);
		Vector u0 = StringUtils.parseData(firstLine, delim);
		int num_cols = u0.size();
		int num_rows = v.size()-1;
		Object[][] data = new Object[num_rows][num_cols];
		for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, delim);
			for (int j=0; j<u.size(); j++) {
				data[i-1][j] = u.elementAt(j);
			}
		}
		return data;
	}

    public static JTable createJTable(String datafile) {
		//char delim = '|';
        String[] columns = getTableColumnNames(datafile);
        Object[][] data = getTableData(datafile);
        JTable table = new JTable(data, columns);
        return table;
	}

    public TableDialog(JFrame parent, String title, String datafile) {
        super(parent);

        setTitle(title);
        setModal(true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        String[] columns = getTableColumnNames(datafile);
        JTable table = createJTable(datafile);

        JScrollPane scrollPane = new JScrollPane(table);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        pack();
        setVisible(true);
    }
}
