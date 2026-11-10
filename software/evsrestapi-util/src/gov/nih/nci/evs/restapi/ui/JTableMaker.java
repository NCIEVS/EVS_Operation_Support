package gov.nih.nci.evs.restapi.ui;
import gov.nih.nci.evs.restapi.util.*;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.util.*;

public class JTableMaker {
	static String ALPHABET = null;
	static {
        StringBuffer buf = new StringBuffer();
		for (int i=65; i<91; i++) {
			char c = (char) i;
			buf.append(c);
		}
		ALPHABET = buf.toString();
	}


/*
JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
JScrollPane.VERTICAL_SCROLLBAR_NEVER
JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
*/
    public static char identifyDelimiter(String line) {
		char delim = '|';
		Vector u = StringUtils.parseData(line, delim);
		if (u.size() == 1) {
			delim = '\t';
		}
		return delim;
	}

    public static String[] getColumnNames(String filename) {
		boolean heading = true;
		return getColumnNames(filename, heading);
	}

    public static String[] getColumnNames(String filename, boolean heading) {
		Vector v = Utils.readFile(filename);
		String firstLn = (String) v.elementAt(0);
		char delim = identifyDelimiter(firstLn);
		Vector u = StringUtils.parseData(firstLn, delim);
		String[] columns = new String[u.size()];
		if (heading) {
			for (int i=0; i<u.size(); i++) {
				columns[i] = (String) u.elementAt(i);
			}
		} else {
			for (int i=0; i<u.size(); i++) {
				columns[i] = "Column " + ALPHABET.charAt(i);
			}
		}
		return columns;
	}

    public static String[][] loadTableData(String filename) {
		boolean heading = true;
		return loadTableData(filename, heading);
	}

    public static String[][] loadTableData(String filename, boolean heading) {
		Vector v = Utils.readFile(filename);
		String firstLn = (String) v.elementAt(0);
		char delim = identifyDelimiter(firstLn);
		Vector u = StringUtils.parseData(firstLn, delim);
		int numRows = 0;
		int numColumns = u.size();
		if (heading) {
			numRows = v.size();
		} else {
			numRows = v.size() + 1;
		}
		String[][] data = new String[numRows][numColumns];
		if (heading) {
			//data = new String[numRows-1][numColumns];
			for (int i=1; i<v.size(); i++) {
				String line = (String) v.elementAt(i);
				u = StringUtils.parseData(line, delim);
				for (int j=0; j<u.size(); j++) {
					String t = (String) u.elementAt(j);
					data[i-1][j] = t;
				}
			}
		} else {
			String[] columns = getColumnNames(filename, heading);
			for (int i=0; i<v.size(); i++) {
				String line = (String) v.elementAt(i);
				u = StringUtils.parseData(line, delim);
				for (int j=0; j<u.size(); j++) {
					String t = (String) u.elementAt(j);
					data[i][j] = t;
				}
			}
		}
		return data;
	}

	public static JTable createJTable(String datafile) {
		boolean heading = true;
		String[] columnNames = getColumnNames(datafile, heading);
		String[][] data = loadTableData(datafile, heading);
		JTable table = new JTable(data, columnNames);
		return table;
	}

	public static JTable createJTable(String[] columnNames, String[][] data) {
		JTable table = new JTable(data, columnNames);
		return table;
	}

	public static JScrollPane createJTableScrollPane(String[] columnNames, String[][] data) {
		JTable table = new JTable(data, columnNames);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		return scrollPane;
	}

	public static void run(String tableName, String[] columnNames, String[][] data) {
		// Creating the JTable
		JTable table = new JTable(data, columnNames);
		// Adding the table to a scroll pane
		JScrollPane scrollPane = new JScrollPane(table);
		// Creating a frame to display the table
		JFrame frame = new JFrame(tableName);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(scrollPane);
		frame.setSize(300, 200);
		frame.setVisible(true);
	}

	public static void run(String tableName, String filename, boolean heading) {
		// Column names
		System.out.println("With heading? " + heading);
		String[] columnNames = getColumnNames(filename, heading);
		for (int i=0; i<columnNames.length; i++) {
			String columnName = columnNames[i];
			System.out.println("(" + i + ") " + columnName);
		}

		String[][] data = loadTableData(filename, heading);
		run(tableName, columnNames, data);
	}

	public static void main(String[] args) {
		String filename = args[0];
		String tableName = "Test JTableMaker";
		boolean heading = false;
		if (filename.indexOf("v2") != -1) {
			heading = true;
		}
        run(tableName, filename, heading);
    }
}
