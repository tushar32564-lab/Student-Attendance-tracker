package gui;

import model.ReportRow;
import service.AttendanceService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Shows the attendance report. The report is built in a background thread
 * ({@link SwingWorker}) so the window never freezes while MySQL aggregates
 * the data.
 */
public class ReportFrame extends JFrame {

    private static final String[] COLUMNS = {
            "Roll No", "Name", "Total Classes", "Present", "Absent",
            "Percentage", "Status"
    };
    private static final int COL_STATUS = 6;
    private static final Color SHORTAGE_COLOR = new Color(255, 205, 205);

    private final AttendanceService service;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JButton generateButton = new JButton("Generate Report");
    private final JButton backButton = new JButton("Back");
    private final JProgressBar progressBar = new JProgressBar();
    private final JLabel summaryLabel = new JLabel("Press \"Generate Report\" to load the report.");

    private ReportWorker currentWorker;

    public ReportFrame(AttendanceService service) {
        this.service = service;
        setTitle("Attendance Report");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(760, 460));

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        buildUi();
        setLocationRelativeTo(null);
        generateReport(); // load the report as soon as the window opens
    }

    private void buildUi() {
        JLabel titleLabel = new JLabel("Attendance Report", JLabel.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(12, 10, 4, 10));

        generateButton.setBackground(new Color(41, 128, 185));
        generateButton.setForeground(Color.WHITE);
        generateButton.setFocusPainted(false);
        generateButton.addActionListener(e -> generateReport());

        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 5));
        buttonPanel.add(generateButton);
        buttonPanel.add(backButton);

        JPanel north = new JPanel(new BorderLayout());
        north.add(titleLabel, BorderLayout.NORTH);
        north.add(buttonPanel, BorderLayout.CENTER);

        // --- table ------------------------------------------------------
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
        table.getTableHeader().setReorderingAllowed(false);
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(170);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(110);

        // Highlight every student below 75% in red
        DefaultTableCellRenderer rowRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    Object status = table.getModel().getValueAt(row, COL_STATUS);
                    boolean shortage = status != null
                            && status.toString().startsWith("BELOW");
                    c.setBackground(shortage ? SHORTAGE_COLOR : table.getBackground());
                }
                return c;
            }
        };
        for (int i = 0; i < COLUMNS.length; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(rowRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        // --- footer -----------------------------------------------------
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(160, 14));

        summaryLabel.setFont(summaryLabel.getFont().deriveFont(Font.BOLD, 12f));
        summaryLabel.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 5));

        JLabel legend = new JLabel("  Students below 75% attendance are highlighted in red.");
        legend.setFont(legend.getFont().deriveFont(Font.ITALIC, 11f));
        legend.setBorder(BorderFactory.createEmptyBorder(0, 12, 10, 12));

        JPanel south = new JPanel(new BorderLayout());
        south.add(progressBar, BorderLayout.NORTH);
        south.add(summaryLabel, BorderLayout.CENTER);
        south.add(legend, BorderLayout.SOUTH);

        add(north, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(generateButton);
    }

    /** Starts the background thread that reads the report from MySQL. */
    private void generateReport() {
        if (currentWorker != null && !currentWorker.isDone()) {
            return; // a report is already being generated
        }
        generateButton.setEnabled(false);
        progressBar.setVisible(true);
        summaryLabel.setText("Generating report, please wait...");

        currentWorker = new ReportWorker();
        currentWorker.execute();
    }

    /** Fills the table - always called on the Event Dispatch Thread. */
    private void showReport(List<ReportRow> rows) {
        tableModel.setRowCount(0);
        int shortageCount = 0;

        for (ReportRow row : rows) {
            boolean shortage = row.isBelowMinimum();
            if (shortage) {
                shortageCount++;
            }
            tableModel.addRow(new Object[]{
                    row.getRollNo(),
                    row.getName(),
                    row.getTotalClasses(),
                    row.getPresentDays(),
                    row.getAbsentDays(),
                    String.format("%.2f%%", row.getPercentage()),
                    shortage ? "BELOW 75%" : "OK"
            });
        }

        if (rows.isEmpty()) {
            summaryLabel.setForeground(Color.RED);
            summaryLabel.setText("No students found. Please add students first.");
        } else {
            summaryLabel.setForeground(shortageCount > 0 ? Color.RED : new Color(0, 128, 0));
            summaryLabel.setText(shortageCount + " of " + rows.size()
                    + " student(s) have attendance below 75%.");
        }
    }

    /**
     * Background worker: doInBackground() runs off the EDT, done() runs on the EDT.
     * This keeps the GUI responsive while the report is being calculated.
     */
    private class ReportWorker extends SwingWorker<List<ReportRow>, Void> {

        @Override
        protected List<ReportRow> doInBackground() throws Exception {
            return service.generateReport();
        }

        @Override
        protected void done() {
            try {
                showReport(get());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException ex) {
                Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                JOptionPane.showMessageDialog(ReportFrame.this,
                        "Could not generate the report:\n" + cause.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                summaryLabel.setForeground(Color.RED);
                summaryLabel.setText("Report could not be generated.");
            } finally {
                progressBar.setVisible(false);
                generateButton.setEnabled(true);
            }
        }
    }
}
