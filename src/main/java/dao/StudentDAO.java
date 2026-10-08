package dao;

import model.Student;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access for the {@code students} table.
 */
public class StudentDAO {

    private static final String SQL_INSERT =
            "INSERT INTO students (roll_no, name, course, semester) VALUES (?, ?, ?, ?)";

    private static final String SQL_SELECT_ALL =
            "SELECT student_id, roll_no, name, course, semester FROM students ORDER BY roll_no";

    private static final String SQL_SEARCH =
            "SELECT student_id, roll_no, name, course, semester FROM students "
                    + "WHERE roll_no LIKE ? OR name LIKE ? OR course LIKE ? ORDER BY roll_no";

    private static final String SQL_COUNT_BY_ROLL_NO =
            "SELECT COUNT(*) FROM students WHERE roll_no = ?";

    /** Inserts a new student; the database rejects a duplicate roll_no. */
    public void insertStudent(Student student) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT)) {

            stmt.setString(1, student.getRollNo());
            stmt.setString(2, student.getName());
            stmt.setString(3, student.getCourse());
            stmt.setInt(4, student.getSemester());
            stmt.executeUpdate();
        }
    }

    /** @return true when the roll number is already taken. */
    public boolean rollNoExists(String rollNo) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_COUNT_BY_ROLL_NO)) {

            stmt.setString(1, rollNo);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /** @return every student, ordered by roll number. */
    public List<Student> findAll() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {

            return mapRows(rs);
        }
    }

    /** @return students whose roll number, name or course contains the keyword. */
    public List<Student> search(String keyword) throws SQLException {
        String pattern = "%" + keyword + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SEARCH)) {

            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);

            try (ResultSet rs = stmt.executeQuery()) {
                return mapRows(rs);
            }
        }
    }

    /** Converts a result set into a list of Student objects (generics + collections). */
    private List<Student> mapRows(ResultSet rs) throws SQLException {
        List<Student> students = new ArrayList<>();
        while (rs.next()) {
            Student student = new Student(
                    rs.getInt("student_id"),
                    rs.getString("roll_no"),
                    rs.getString("name"),
                    rs.getString("course"),
                    rs.getInt("semester"));
            students.add(student);
        }
        return students;
    }
}
