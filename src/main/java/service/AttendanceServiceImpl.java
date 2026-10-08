package service;

import dao.AttendanceDAO;
import dao.StudentDAO;
import dao.UserDAO;
import model.Attendance;
import model.ReportRow;
import model.Student;
import util.ValidationException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Concrete implementation of {@link AttendanceService}.
 * <p>
 * All business rules (validation) live here, so the GUI classes never
 * contain SQL or validation logic. Database access is delegated to the
 * DAO layer.
 * </p>
 */
public class AttendanceServiceImpl implements AttendanceService {

    public static final int MIN_SEMESTER = 1;
    public static final int MAX_SEMESTER = 8;
    private static final int MAX_ROLL_NO_LENGTH = 20;
    private static final int MAX_NAME_LENGTH = 100;
    private static final String ROLL_NO_PATTERN = "[A-Za-z0-9\\-]+";

    private final UserDAO userDAO = new UserDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();

    /**
     * Guards the "check then insert" sequence so that two threads cannot
     * add the same roll number at the same time.
     */
    private final Object studentLock = new Object();

    /**
     * Guards the "check then replace" sequence so that two threads cannot
     * write attendance for the same date concurrently.
     */
    private final Object attendanceLock = new Object();

    // ------------------------------------------------------------------
    // Login
    // ------------------------------------------------------------------

    @Override
    public boolean login(String username, String password)
            throws ValidationException, SQLException {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Username cannot be empty.");
        }
        if (password == null || password.isEmpty()) {
            throw new ValidationException("Password cannot be empty.");
        }
        return userDAO.validateUser(username.trim(), password);
    }

    // ------------------------------------------------------------------
    // Students
    // ------------------------------------------------------------------

    @Override
    public void addStudent(Student student) throws ValidationException, SQLException {
        validateStudent(student);

        synchronized (studentLock) {
            if (studentDAO.rollNoExists(student.getRollNo())) {
                throw new ValidationException(
                        "Roll number \"" + student.getRollNo() + "\" already exists.");
            }
            try {
                studentDAO.insertStudent(student);
            } catch (SQLIntegrityConstraintViolationException e) {
                // Safety net if another thread inserted the same roll number.
                throw new ValidationException(
                        "Roll number \"" + student.getRollNo() + "\" already exists.", e);
            }
        }
    }

    /** Checks every field of a student and reports the first problem found. */
    private void validateStudent(Student student) throws ValidationException {
        String rollNo = trim(student.getRollNo());
        String name = trim(student.getName());
        String course = trim(student.getCourse());

        if (rollNo.isEmpty()) {
            throw new ValidationException("Roll number cannot be empty.");
        }
        if (rollNo.length() > MAX_ROLL_NO_LENGTH) {
            throw new ValidationException(
                    "Roll number must be at most " + MAX_ROLL_NO_LENGTH + " characters.");
        }
        if (!rollNo.matches(ROLL_NO_PATTERN)) {
            throw new ValidationException(
                    "Invalid roll number. Use only letters, digits and hyphens.");
        }
        if (name.isEmpty()) {
            throw new ValidationException("Student name cannot be empty.");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new ValidationException(
                    "Student name must be at most " + MAX_NAME_LENGTH + " characters.");
        }
        if (course.isEmpty()) {
            throw new ValidationException("Course cannot be empty.");
        }
        if (student.getSemester() < MIN_SEMESTER || student.getSemester() > MAX_SEMESTER) {
            throw new ValidationException("Semester must be between "
                    + MIN_SEMESTER + " and " + MAX_SEMESTER + ".");
        }

        // Normalise the values before they are stored
        student.setRollNo(rollNo);
        student.setName(name);
        student.setCourse(course);
    }

    @Override
    public List<Student> getStudents() throws SQLException {
        return studentDAO.findAll();
    }

    @Override
    public List<Student> searchStudents(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return studentDAO.findAll();
        }
        return studentDAO.search(keyword.trim());
    }

    // ------------------------------------------------------------------
    // Attendance
    // ------------------------------------------------------------------

    @Override
    public boolean isAttendanceMarked(LocalDate date) throws SQLException {
        return attendanceDAO.isMarked(date);
    }

    @Override
    public void saveAttendance(LocalDate date, List<Attendance> records, boolean overwrite)
            throws ValidationException, SQLException {

        if (date == null) {
            throw new ValidationException("Please select an attendance date.");
        }
        if (records == null || records.isEmpty()) {
            throw new ValidationException(
                    "No students to mark. Please add students first.");
        }
        for (Attendance record : records) {
            if (record.getStudentId() <= 0) {
                throw new ValidationException("Invalid student in the attendance list.");
            }
            if (!record.isStatusValid()) {
                throw new ValidationException(
                        "Invalid attendance status \"" + record.getStatus()
                                + "\". Only Present or Absent is allowed.");
            }
        }

        // Synchronised: check and write happen atomically even if two
        // threads try to save attendance for the same date.
        synchronized (attendanceLock) {
            if (attendanceDAO.isMarked(date)) {
                if (!overwrite) {
                    throw new ValidationException(
                            "Attendance is already marked for " + date + ".");
                }
                attendanceDAO.deleteByDate(date);
            }
            attendanceDAO.insertAll(records);
        }
    }

    @Override
    public List<ReportRow> generateReport() throws SQLException {
        return attendanceDAO.findReport();
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
