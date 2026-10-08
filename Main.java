import gui.LoginFrame;
import service.AttendanceService;
import service.AttendanceServiceImpl;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Entry point of the Student Attendance Tracker.
 * <p>
 * The concrete service is created here and passed to the GUI, so the GUI
 * only depends on the {@code AttendanceService} interface.
 * </p>
 */
public class Main {

    public static void main(String[] args) {
        try {
            // Use the look and feel of the operating system
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            System.out.println("Could not load the system look and feel: " + ex.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            AttendanceService service = new AttendanceServiceImpl();
            new LoginFrame(service).setVisible(true);
        });
    }
}
