package gov.nih.nci.evs.restapi.ui;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class JTableMaker {

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

	public static void main(String[] args) {
		String tableName = "Simple Table Example";

		// Column names
		String[] columnNames = {"ID", "First Name", "Last Name"};

		// Sample data for the table
		String[][] data = {
		{"1", "John", "Doe"},
		{"2", "Jane", "Doe"},
		{"3", "Jack", "Smith"}
		};

		run(tableName, columnNames, data);

	}
}
