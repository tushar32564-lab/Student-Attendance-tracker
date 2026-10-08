package gui;

import service.AttendanceService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

/**
 * Main menu of the application. Every button opens one feature window.
 */
public class DashboardFrame extends JFrame {

    private static final Color HEADER_COLOR = new Color(33, 87, 122);

    private final AttendanceService service;

    public DashboardFrame(AttendanceService service) {
        this.service = service;
        setTitle("Student Attendance Tracker - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(520, 420));
        buildUi();
        setLocationRelativeTo(null);
    }

    private void buildUi() {
        // --- header ----------------------------------------------------
        JLabel titleLabel = new JLabel("Attendance Dashboard", JLabel.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 20f));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(18, 10, 18, 10));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_COLOR);
        header.add(titleLabel, BorderLayout.CENTER);

        // --- menu buttons ---------------------------------------------
        JButton addStudentButton = createMenuButton("Add Student");
        JButton viewStudentsButton = createMenuButton("View Students");
        JButton markAttendanceButton = createMenuButton("Mark Attendance");
        JButton reportButton = createMenuButton("Attendance Report");

        JPanel menu = new JPanel(new GridLayout(4, 1, 0, 12));
        menu.setBorder(BorderFactory.createEmptyBorder(30, 60, 20, 60));
        menu.add(addStudentButton);
        menu.add(viewStudentsButton);
        menu.add(markAttendanceButton);
        menu.add(reportButton);

        // --- logout ----------------------------------------------------
        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(new Color(178, 34, 34));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(e -> logout());

        JPanel footer = new JPanel(new GridLayout(1, 1, 0, 0));
        footer.setBorder(BorderFactory.createEmptyBorder(10, 60, 25, 60));
        footer.add(logoutButton);

        add(header, BorderLayout.NORTH);
        add(menu, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        addStudentButton.addActionListener(e -> new AddStudentFrame(service).setVisible(true));
        viewStudentsButton.addActionListener(e -> new StudentListFrame(service).setVisible(true));
        markAttendanceButton.addActionListener(e -> new AttendanceFrame(service).setVisible(true));
        reportButton.addActionListener(e -> new ReportFrame(service).setVisible(true));
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 15f));
        button.setFocusPainted(false);
        button.setBackground(new Color(41, 128, 185));
        button.setForeground(Color.WHITE);
        return button;
    }

    private void logout() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Do you want to logout?", "Logout",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            new LoginFrame(service).setVisible(true);
            dispose();
        }
    }
}
