package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serial;

//username and password is 'admin'

public class LoginGUI extends JFrame implements ActionListener {
    @Serial
    private static final long serialVersionUID = 1L;

    private int maxVacationLeaves = 10;
    private int maxSickLeaves = 5;
    private int maxEmergencyLeaves = 5;

    private final JLabel userLabel;
    private final JLabel passLabel;
    private final JLabel messageLabel;
    private final JTextField userField;
    private final JPasswordField passField;
    private final JButton loginButton;

    private boolean isLoggedIn = false;
    private boolean isEmployeeSelected = false;

    private static final String LEAVE_APPLICATIONS_FILE = "leave_applications.csv";
    private static final String LEAVE_APPLICATIONS_ARCHIVE_FILE = "leave_applications_archive.csv";

    private final String[] columns = {
            "Employee Number", "Last Name", "First Name", "SSS No.", "PhilHealth No.", "TIN", "Pagibig No.", "Leave Dates"
    };

    private final String[][] data = {
            {"10001", "Crisostomo", "Jose", "49-1632020-8", "382189453145", "317-674-022-000", "441093369646", ""}
    };

    private DefaultTableModel tableModel;

    public LoginGUI() {
        setTitle("Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(350, 200);
        setLocationRelativeTo(null);

        userLabel = new JLabel("Username:");
        passLabel = new JLabel("Password:");
        messageLabel = new JLabel("");
        userField = new JTextField(10);
        passField = new JPasswordField(10);
        loginButton = new JButton("Login");

        loginButton.addActionListener(this);

        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(userLabel);
        panel.add(userField);
        panel.add(passLabel);
        panel.add(passField);
        panel.add(messageLabel);
        panel.add(loginButton);

        add(panel);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (!isLoggedIn) {
            handleLogin();
        } else {
            handleLoggedInActions(e);
        }
    }

    private void handleLogin() {
        String user = userField.getText();
        String pass = new String(passField.getPassword());

        if (user.equals("admin") && pass.equals("admin")) {
            isLoggedIn = true;
            messageLabel.setText("");
            userField.setText("");
            passField.setText("");
            updateMenu();
        } else {
            messageLabel.setText("Oops! Wrong username or password! ¯\\_(ツ)_/¯");
        }
    }

    private void handleLoggedInActions(ActionEvent e) {
        String actionCommand = e.getActionCommand();

        switch (actionCommand) {
            case "Employee 10001" -> {
                isEmployeeSelected = true;
                updateMenu();
            }
            case "Back" -> {
                isEmployeeSelected = false;
                updateMenu();
            }
            case "Update Employee" -> handleUpdateEmployee();
            case "Delete Part" -> handleDeletePart();
            case "Logout" -> {
                isLoggedIn = false;
                isEmployeeSelected = false;
                updateMenu();
            }
            case "Apply Leave" -> handleApplyLeave();
        }
    }

    private void handleUpdateEmployee() {
        String employeeNumber = JOptionPane.showInputDialog(this, "Enter employee number to update:");
        if (employeeNumber != null && !employeeNumber.isEmpty()) {
            EmployeeDataDialog dialog = new EmployeeDataDialog(this);
            if (dialog.showDialog()) {
                String[] updatedData = dialog.getEmployeeData();
                updateEmployeeData(employeeNumber, updatedData);
                JOptionPane.showMessageDialog(this, "Employee " + employeeNumber + " updated successfully.");
            }
        }
    }

    private void handleDeletePart() {
        String employeeNumber = JOptionPane.showInputDialog(this, "Enter employee number:");
        if (employeeNumber != null && !employeeNumber.isEmpty()) {
            int employeeIndex = findEmployeeIndex(employeeNumber);
            if (employeeIndex != -1) {
                String[] options = {"Last Name", "First Name", "SSS No.", "PhilHealth No.", "TIN", "Pagibig No."};
                String selectedPart = (String) JOptionPane.showInputDialog(this, "Select the part to delete:", "Delete Part",
                        JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

                if (selectedPart != null) {
                    int partIndex = getColumnIndex(selectedPart);
                    if (partIndex != -1) {
                        deletePart(employeeIndex, partIndex);
                        JOptionPane.showMessageDialog(this, "The selected part has been deleted for Employee " + employeeNumber);
                    } else {
                        JOptionPane.showMessageDialog(this, "Invalid part selected.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Employee not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleApplyLeave() {
        String employeeNumber = JOptionPane.showInputDialog(this, "Enter employee number:");
        if (employeeNumber != null && !employeeNumber.isEmpty()) {
            int employeeIndex = findEmployeeIndex(employeeNumber);
            if (employeeIndex != -1) {
                String[] leaveOptions = {"Vacation", "Sick", "Emergency"};
                String leaveType = (String) JOptionPane.showInputDialog(this, "Select the leave type:", "Apply Leave",
                        JOptionPane.PLAIN_MESSAGE, null, leaveOptions, leaveOptions[0]);

                if (leaveType != null) {
                    String leaveDates = JOptionPane.showInputDialog(this, "Enter leave dates (e.g., YYYY-MM-DD):");
                    if (leaveDates != null && !leaveDates.isEmpty()) {
                        saveLeaveApplication(employeeNumber, leaveType, leaveDates);
                        JOptionPane.showMessageDialog(this, "Leave application submitted successfully for Employee " + employeeNumber);
                    } else {
                        JOptionPane.showMessageDialog(this, "Invalid leave dates.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Employee not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveLeaveApplication(String employeeNumber, String leaveType, String leaveDates) {
        String[] leaveData = {employeeNumber, leaveType, leaveDates};


        switch (leaveType) {
            case "Vacation" -> maxVacationLeaves--;
            case "Sick" -> maxSickLeaves--;
            case "Emergency" -> maxEmergencyLeaves--;
        }

        try (FileWriter writer = new FileWriter(LEAVE_APPLICATIONS_FILE, true)) {
            StringBuilder sb = new StringBuilder();

            for (String data : leaveData) {
                sb.append(data);
                sb.append(",");
            }
            sb.append(System.lineSeparator());

            writer.write(sb.toString());
        } catch (IOException e) {
            System.err.println("An error occurred while saving leave application: " + e.getMessage());
        }


        try (FileWriter writer = new FileWriter(LEAVE_APPLICATIONS_ARCHIVE_FILE, true)) {
            StringBuilder sb = new StringBuilder();

            for (String data : leaveData) {
                sb.append(data);
                sb.append(",");
            }
            sb.append(System.lineSeparator());

            writer.write(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int findEmployeeIndex(String employeeNumber) {
        for (int i = 0; i < data.length; i++) {
            if (data[i][0].equals(employeeNumber)) {
                return i;
            }
        }
        return -1;
    }

    private int getColumnIndex(String columnName) {
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].equals(columnName)) {
                return i;
            }
        }
        return -1;
    }

    private void updateEmployeeData(String employeeNumber, String[] updatedData) {
        int employeeIndex = findEmployeeIndex(employeeNumber);
        if (employeeIndex != -1) {
            System.arraycopy(updatedData, 0, data[employeeIndex], 1, updatedData.length - 1);
            tableModel.fireTableDataChanged();
        }
    }


    private void deletePart(int employeeIndex, int partIndex) {
        data[employeeIndex][partIndex] = "";
        tableModel.fireTableDataChanged();
    }

    private void updateMenu() {
        getContentPane().removeAll();
        if (isLoggedIn) {
            if (isEmployeeSelected) {
                displayEmployeeTable();
            } else {
                displayMainMenu();
            }
        } else {
            displayLogin();
        }
        revalidate();
        repaint();
    }

    private void displayEmployeeTable() {
        tableModel = new DefaultTableModel(data, columns);
        JTable employeeTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(employeeTable);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(this);

        JLabel vacationLabel = new JLabel("Vacation Leaves Remaining: " + maxVacationLeaves);
        JLabel sickLabel = new JLabel("Sick Leaves Remaining: " + maxSickLeaves);
        JLabel emergencyLabel = new JLabel("Emergency Leaves Remaining: " + maxEmergencyLeaves);

        JPanel infoPanel = new JPanel(new GridLayout(3, 1));
        infoPanel.add(vacationLabel);
        infoPanel.add(sickLabel);
        infoPanel.add(emergencyLabel);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(backButton, BorderLayout.SOUTH);
        panel.add(infoPanel, BorderLayout.NORTH);

        getContentPane().add(panel);
    }

    private void displayMainMenu() {
        JPanel panel = new JPanel(new GridLayout(6, 1));

        JButton emp1Button = new JButton("Employee 10001");
        emp1Button.addActionListener(this);
        panel.add(emp1Button);

        JButton updateButton = new JButton("Update Employee");
        updateButton.addActionListener(this);
        panel.add(updateButton);

        JButton deleteButton = new JButton("Delete Part");
        deleteButton.addActionListener(this);
        panel.add(deleteButton);

        JButton applyLeaveButton = new JButton("Apply Leave");
        applyLeaveButton.addActionListener(this);
        panel.add(applyLeaveButton);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(this);
        panel.add(logoutButton);

        getContentPane().add(panel);
    }

    private void displayLogin() {
        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(userLabel);
        panel.add(userField);
        panel.add(passLabel);
        panel.add(passField);
        panel.add(messageLabel);
        panel.add(loginButton);

        getContentPane().add(panel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginGUI::new);
    }
}
