package gov.nih.nci.evs.restapi.ui;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;
import java.util.Date;

public class DatePickerExample {
/*
    public static void main(String[] args) {
        SwingUtilities.invokeLater(DatePickerExample::createAndShowGUI);
    }
*/
    public static JPanel createDatePanel(JTextField tf1, JLabel label) {
        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel label1 = new JLabel("Date");
        UtilDateModel model = new UtilDateModel();
        model.setSelected(true); // Pre-select today's date

        // Properties for date formatting
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");

        // Create date panel and picker
        JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
        JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, null);
        JLabel label2 = new JLabel("Data");
        JButton btn1 = new JButton("Select");
        btn1.addActionListener(e -> {
            Date selectedDate = (Date) datePicker.getModel().getValue();
            if (selectedDate != null) {
				tf1.setText(selectedDate.toString());
            } else {
				label.setText("Error - No date selected");
				/*
                JOptionPane.showMessageDialog(frame,
                        "No date selected!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                */
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
        panel.add(btn1, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label2, gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);

        panel.add(datePicker, gbc);
        return panel;
	}

    //private static void createAndShowGUI() {
	public static void main(String[] args) {
        JFrame frame = new JFrame("Date Picker Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLayout(new FlowLayout());

        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField tf1 = new JTextField();
        JLabel label1 = new JLabel("Status");
        JLabel label2 = new JLabel("");
        JPanel panel1 = createDatePanel(tf1, label2);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(panel1, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label1, gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        panel.add(label2, gbc);

        frame.add(panel);
        frame.setVisible(true);
    }
}

