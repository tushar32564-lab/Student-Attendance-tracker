package gui;

import service.AttendanceService;
import util.ValidationException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;

/**
 * Login window - validates the username / password against the users table.
 */
public class LoginFrame extends JFrame {

    private static final Color HEADER_COLOR = new Color(33, 87, 122);

    private final AttendanceService service;
    private final JTextField usernameField = new JTextField(16);
    private final JPasswordField passwordField = new JPasswordField(16);

    public LoginFrame(AttendanceService service) {
        this.service = service;
        setTitle("Student Attendance Tracker - Login");
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        buildUi();
        pack();
        setLocationRelativeTo(null);
    }

    private void buildUi() {
        // --- title -----------------------------------------------------
        JLabel titleLabel = new JLabel("Student Attendance Tracker");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_COLOR);
        header.setBorder(BorderFactory.createEmptyBorder(14, 10, 14, 10));
        header.add(titleLabel, BorderLayout.CENTER);

        JLabel subtitle = new JLabel("Sign in to continue", JLabel.CENTER);
        subtitle.setFont(subtitle.getFont().deriveFont(Font.ITALIC, 12f));

        JPanel subtitlePanel = new JPanel(new BorderLayout());
        subtitlePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        subtitlePanel.add(subtitle, BorderLayout.CENTER);

        // --- form ------------------------------------------------------
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 12, 6, 12);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        form.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        form.add(passwordField, gbc);

        JPanel formPanel = new JPanel(new BorderLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        formPanel.add(form, BorderLayout.CENTER);
        formPanel.add(subtitlePanel, BorderLayout.NORTH);

        // --- buttons ---------------------------------------------------
        JButton loginButton = new JButton("Login");
        loginButton.setBackground(HEADER_COLOR);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);

        JButton exitButton = new JButton("Exit");
        exitButton.setFocusPainted(false);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        buttons.add(loginButton);
        buttons.add(exitButton);

        // --- layout ----------------------------------------------------
        JPanel content = new JPanel(new BorderLayout());
        content.add(formPanel, BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);
        content.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));

        add(header, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);

        loginButton.addActionListener(e -> doLogin());
        exitButton.addActionListener(e -> System.exit(0));

        // Pressing Enter inside the password field also logs in
        passwordField.addActionListener(e -> doLogin());
        getRootPane().setDefaultButton(loginButton);

        setMinimumSize(new Dimension(420, 230));
        usernameField.requestFocusInWindow();
    }

    /** Reads the fields, calls the service and opens the dashboard on success. */
    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        try {
            if (service.login(username, password)) {
                openDashboard();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid username or password. Please try again.",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
                passwordField.setText("");
                passwordField.requestFocusInWindow();
            }
        } catch (ValidationException ex) {
            showWarning(ex.getMessage());
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void openDashboard() {
        DashboardFrame dashboard = new DashboardFrame(service);
        dashboard.setVisible(true);
        dispose();
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error",
                JOptionPane.WARNING_MESSAGE);
    }

    private void showDatabaseError(SQLException ex) {
        JOptionPane.showMessageDialog(this,
                "Database error:\n" + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
    }
}
