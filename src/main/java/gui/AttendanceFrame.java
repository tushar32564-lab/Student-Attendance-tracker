package gui;

import model.Attendance;
import model.Student;
import service.AttendanceService;
import util.ValidationException;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Lets the teacher pick a date and mark every student Present or Absent.
 */
public class AttendanceFrame extends JFrame {

    private static final int COL_ROLL_NO = 0;
    private static final int COL_NAME = 1;
    private static final int COL_STATUS = 2;
    private static final String[] COLUMNS = {"Roll No", "Name", "Status"};

    private static final Color PRESENT_COLOR = new Color(0, 128, 0);
    private static final Color ABSENT_COLOR = new Color(178, 34, 34);

    private final AttendanceService service;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField dateField = new JTextField(12);
    private final JLabel hintLabel = new JLabel(" ");

    /** Kept in the same order as the table rows so rows map back to students. */
    private List<Student> students = new ArrayList<>();

    public AttendanceFrame(AttendanceService service) {
        this.service = service;
        setTitle("Mark Attendance");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(620, 440));

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == COL_STATUS;
            }
        };
        table = new JTable(tableModel);
        buildUi();
        setLocationRelativeTo(null);
        loadStudents();
    }

    private void buildUi() {
        // --- title ------------------------------------------------------
        JLabel titleLabel = new JLabel("Mark Attendance", JLabel.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(12, 10, 4, 10));

        // --- date bar ---------------------------------------------------
        dateField.setText(LocalDate.now().toString());
        JButton todayButton = new JButton("Today");
        todayButton.addActionListener(e -> dateField.setText(LocalDate.now().toString()));

        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        datePanel.add(new JLabel("Date (YYYY-MM-DD):"));
        datePanel.add(dateField);
        datePanel.add(todayButton);

        JPanel north = new JPanel(new BorderLayout());
        north.add(titleLabel, BorderLayout.NORTH);
        north.add(datePanel, BorderLayout.CENTER);

        // --- table ------------------------------------------------------
        table.setFillsViewportHeight(true);
        table.setRowHeight(26);
        table.getTableHeader().setReorderingAllowed(false);
        table.getColumnModel().getColumn(COL_ROLL_NO).setPreferredWidth(90);
        table.getColumnModel().getColumn(COL_NAME).setPreferredWidth(200);
        table.getColumnModel().getColumn(COL_STATUS).setPreferredWidth(90);

        // Status column uses a combo box: Present or Absent
        JComboBox<String> statusCombo = new JComboBox<>(
                new String[]{Attendance.PRESENT, Attendance.ABSENT});
        table.getColumnModel().getColumn(COL_STATUS)
                .setCellEditor(new DefaultCellEditor(statusCombo));

        // Colour the status text: green = Present, red = Absent
        DefaultTableCellRenderer statusRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setForeground(Attendance.PRESENT.equals(value)
                            ? PRESENT_COLOR : ABSENT_COLOR);
                }
                return c;
            }
        };
        table.getColumnModel().getColumn(COL_STATUS).setCellRenderer(statusRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        // --- buttons ----------------------------------------------------
        JButton saveButton = new JButton("Save Attendance");
        saveButton.setBackground(new Color(41, 128, 185));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.addActionListener(e -> saveAttendance());

        JButton backButton = new JButton("Back");
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> dispose());

        hintLabel.setFont(hintLabel.getFont().deriveFont(Font.ITALIC, 12f));
        hintLabel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        JPanel south = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        buttonPanel.add(saveButton);
        buttonPanel.add(backButton);
        south.add(buttonPanel, BorderLayout.CENTER);
        south.add(hintLabel, BorderLayout.SOUTH);

        add(north, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(saveButton);
    }

    /** Loads every student and pre-fills all rows with "Present". */
    private void loadStudents() {
        try {
            students = service.getStudents();
            tableModel.setRowCount(0);
            for (Student student : students) {
                tableModel.addRow(new Object[]{
                        student.getRollNo(), student.getName(), Attendance.PRESENT});
            }
            if (students.isEmpty()) {
                hintLabel.setForeground(ABSENT_COLOR);
                hintLabel.setText("No students found. Please add students first.");
            } else {
                hintLabel.setForeground(new Color(40, 40, 40));
                hintLabel.setText(students.size()
                        + " student(s) loaded. Click a Status cell to change Present/Absent.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not load students:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveAttendance() {
        try {
            LocalDate date = parseDate();
            List<Attendance> records = buildRecords(date);

            boolean overwrite = false;
            if (service.isAttendanceMarked(date)) {
                int choice = JOptionPane.showConfirmDialog(this,
                        "Attendance is already marked for " + date + ".\nDo you want to replace it?",
                        "Already Marked", JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (choice != JOptionPane.YES_OPTION) {
                    return;
                }
                overwrite = true;
            }

            service.saveAttendance(date, records, overwrite);

            JOptionPane.showMessageDialog(this,
                    "Attendance saved successfully for " + date + ".",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not save attendance:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Reads the date field and checks that it is a real, non-future date. */
    private LocalDate parseDate() throws ValidationException {
        String text = dateField.getText().trim();
        if (text.isEmpty()) {
            throw new ValidationException("Please enter the attendance date.");
        }
        LocalDate date;
        try {
            date = LocalDate.parse(text);
        } catch (DateTimeParseException ex) {
            throw new ValidationException(
                    "Invalid date \"" + text + "\". Use the format YYYY-MM-DD.", ex);
        }
        if (date.isAfter(LocalDate.now())) {
            throw new ValidationException("Attendance date cannot be in the future.");
        }
        return date;
    }

    /** Converts the table rows into attendance records for the service. */
    private List<Attendance> buildRecords(LocalDate date) throws ValidationException {
        if (students.isEmpty()) {
            throw new ValidationException("There are no students to mark.");
        }
        List<Attendance> records = new ArrayList<>();
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            Student student = students.get(row);
            String status = String.valueOf(tableModel.getValueAt(row, COL_STATUS)).trim();
            records.add(new Attendance(student.getId(), date, status));
        }
        return records;
    }
}
