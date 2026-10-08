package gui;

import model.Student;
import service.AttendanceService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.SQLException;
import java.util.List;

/**
 * Shows all students in a table and allows searching by roll number,
 * name or course.
 */
public class StudentListFrame extends JFrame {

    private static final String[] COLUMNS = {"Roll No", "Name", "Course", "Semester"};

    private final AttendanceService service;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField searchField = new JTextField(16);
    private final JLabel statusLabel = new JLabel(" ");

    public StudentListFrame(AttendanceService service) {
        this.service = service;
        setTitle("View Students");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(640, 420));

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // the table is read-only
            }
        };
        table = new JTable(tableModel);
        buildUi();
        setLocationRelativeTo(null);
        loadStudents(null);
    }

    private void buildUi() {
        // --- search bar ------------------------------------------------
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> search());
        JButton showAllButton = new JButton("Show All");
        showAllButton.addActionListener(e -> {
            searchField.setText("");
            search();
        });
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadStudents(searchField.getText()));
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> dispose());

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(showAllButton);
        searchPanel.add(refreshButton);
        searchPanel.add(backButton);

        // Enter key starts the search
        searchField.addActionListener(e -> search());

        // --- table -----------------------------------------------------
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(70);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

        // --- footer ----------------------------------------------------
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.ITALIC, 12f));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void search() {
        loadStudents(searchField.getText());
    }

    /** Fills the table from MySQL; runs on the EDT because queries are fast. */
    private void loadStudents(String keyword) {
        try {
            List<Student> students = keyword == null || keyword.trim().isEmpty()
                    ? service.getStudents()
                    : service.searchStudents(keyword);

            tableModel.setRowCount(0);
            for (Student student : students) {
                tableModel.addRow(new Object[]{
                        student.getRollNo(),
                        student.getName(),
                        student.getCourse(),
                        student.getSemester()
                });
            }

            statusLabel.setForeground(new Color(40, 40, 40));
            statusLabel.setText("  " + students.size() + " student(s) found.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not read students from the database:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
