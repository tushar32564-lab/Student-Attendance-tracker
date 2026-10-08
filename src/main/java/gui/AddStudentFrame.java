package gui;

import model.Student;
import service.AttendanceService;
import service.AttendanceServiceImpl;
import util.ValidationException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;

/**
 * Form used to register a new student in the database.
 */
public class AddStudentFrame extends JFrame {

    private static final String[] COURSES = {
            "B.Tech CSE", "B.Tech ECE", "B.Tech ME", "B.Tech CE", "BCA", "BBA", "B.Sc"
    };

    private final AttendanceService service;

    private final JTextField rollNoField = new JTextField(18);
    private final JTextField nameField = new JTextField(18);
    private final JComboBox<String> courseCombo = new JComboBox<>(COURSES);
    private final JTextField semesterField = new JTextField(18);

    public AddStudentFrame(AttendanceService service) {
        this.service = service;
        setTitle("Add Student");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(480, 360));
        buildUi();
        setLocationRelativeTo(null);
    }

    private void buildUi() {
        JLabel titleLabel = new JLabel("Add New Student", JLabel.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 12, 7, 12);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;

        addRow(form, gbc, 0, "Roll Number:", rollNoField);
        addRow(form, gbc, 1, "Student Name:", nameField);
        addRow(form, gbc, 2, "Course:", courseCombo);
        addRow(form, gbc, 3, "Semester (1-8):", semesterField);

        JPanel formPanel = new JPanel(new BorderLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        formPanel.add(form, BorderLayout.CENTER);

        JButton saveButton = new JButton("Save Student");
        saveButton.setBackground(new Color(41, 128, 185));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.addActionListener(e -> saveStudent());

        JButton clearButton = new JButton("Clear");
        clearButton.setFocusPainted(false);
        clearButton.addActionListener(e -> clearForm());

        JButton backButton = new JButton("Back");
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new BorderLayout());
        buttons.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));

        JPanel buttonRow = new JPanel();
        buttonRow.add(saveButton);
        buttonRow.add(clearButton);
        buttonRow.add(backButton);

        JLabel hint = new JLabel(
                "Example roll number: 21CS001", JLabel.CENTER);
        hint.setFont(hint.getFont().deriveFont(Font.ITALIC, 11f));
        hint.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        buttons.add(buttonRow, BorderLayout.CENTER);
        buttons.add(hint, BorderLayout.SOUTH);

        add(titleLabel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(saveButton);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row,
                        String label, java.awt.Component field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);
    }

    private void saveStudent() {
        try {
            Student student = new Student();
            student.setRollNo(rollNoField.getText());
            student.setName(nameField.getText());
            student.setCourse(String.valueOf(courseCombo.getSelectedItem()));
            student.setSemester(parseSemester());

            service.addStudent(student);

            JOptionPane.showMessageDialog(this,
                    "Student \"" + student.getName() + "\" added successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            rollNoField.requestFocusInWindow();
        } catch (ValidationException ex) {
            showError(ex.getMessage());
        } catch (SQLException ex) {
            showError("Database error:\n" + ex.getMessage());
        }
    }

    private int parseSemester() throws ValidationException {
        String text = semesterField.getText().trim();
        if (text.isEmpty()) {
            throw new ValidationException("Semester cannot be empty.");
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            throw new ValidationException(
                    "Invalid semester \"" + text + "\". Enter a number between "
                            + AttendanceServiceImpl.MIN_SEMESTER + " and "
                            + AttendanceServiceImpl.MAX_SEMESTER + ".", ex);
        }
    }

    private void clearForm() {
        rollNoField.setText("");
        nameField.setText("");
        courseCombo.setSelectedIndex(0);
        semesterField.setText("");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error",
                JOptionPane.ERROR_MESSAGE);
    }
}
