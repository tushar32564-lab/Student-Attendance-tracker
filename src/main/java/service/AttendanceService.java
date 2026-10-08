package service;

import model.Attendance;
import model.ReportRow;
import model.Student;
import util.ValidationException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Business operations of the application.
 * <p>
 * The GUI talks to this interface only - the concrete class
 * ({@link AttendanceServiceImpl}) is injected by {@code Main},
 * which is a simple example of abstraction and polymorphism.
 * </p>
 */
public interface AttendanceService {

    /** Validates the login credentials against the users table. */
    boolean login(String username, String password) throws ValidationException, SQLException;

    /** Validates and stores a new student. */
    void addStudent(Student student) throws ValidationException, SQLException;

    /** @return all students ordered by roll number. */
    List<Student> getStudents() throws SQLException;

    /** @return students matching the given roll number / name / course. */
    List<Student> searchStudents(String keyword) throws SQLException;

    /** @return true when attendance was already saved for that date. */
    boolean isAttendanceMarked(LocalDate date) throws SQLException;

    /**
     * Stores one day of attendance.
     *
     * @param overwrite when true an already marked date is replaced
     */
    void saveAttendance(LocalDate date, List<Attendance> records, boolean overwrite)
            throws ValidationException, SQLException;

    /** Builds the attendance report (called from a background thread by the GUI). */
    List<ReportRow> generateReport() throws SQLException;
}
