package dao;

import model.Attendance;
import model.ReportRow;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access for the {@code attendance} table.
 */
public class AttendanceDAO {

    private static final String SQL_COUNT_BY_DATE =
            "SELECT COUNT(*) FROM attendance WHERE attendance_date = ?";

    private static final String SQL_DELETE_BY_DATE =
            "DELETE FROM attendance WHERE attendance_date = ?";

    private static final String SQL_INSERT =
            "INSERT INTO attendance (student_id, attendance_date, status) VALUES (?, ?, ?)";

    /**
     * Aggregate query used by the report: for every student it counts the
     * recorded classes and how many of them were present / absent.
     * The LEFT JOIN also returns students that have no attendance rows yet.
     */
    private static final String SQL_REPORT =
            "SELECT s.roll_no, s.name, "
                    + "COUNT(a.attendance_id) AS total_classes, "
                    + "SUM(CASE WHEN a.status = 'Present' THEN 1 ELSE 0 END) AS present_days, "
                    + "SUM(CASE WHEN a.status = 'Absent' THEN 1 ELSE 0 END) AS absent_days "
                    + "FROM students s "
                    + "LEFT JOIN attendance a ON a.student_id = s.student_id "
                    + "GROUP BY s.student_id, s.roll_no, s.name "
                    + "ORDER BY s.roll_no";

    /** @return true when attendance has already been saved for that date. */
    public boolean isMarked(LocalDate date) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_COUNT_BY_DATE)) {

            stmt.setDate(1, java.sql.Date.valueOf(date));

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /** Removes every attendance record of the given date (used before an overwrite). */
    public void deleteByDate(LocalDate date) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE_BY_DATE)) {

            stmt.setDate(1, java.sql.Date.valueOf(date));
            stmt.executeUpdate();
        }
    }

    /**
     * Saves a whole day of attendance in a single transaction:
     * either every record is stored, or none of them (rollback on failure).
     */
    public void insertAll(List<Attendance> records) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERT)) {
                for (Attendance record : records) {
                    stmt.setInt(1, record.getStudentId());
                    stmt.setDate(2, java.sql.Date.valueOf(record.getAttendanceDate()));
                    stmt.setString(3, record.getStatus());
                    stmt.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /** Reads the aggregated report data from the database. */
    public List<ReportRow> findReport() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_REPORT)) {

            List<ReportRow> rows = new ArrayList<>();
            while (rs.next()) {
                rows.add(new ReportRow(
                        rs.getString("roll_no"),
                        rs.getString("name"),
                        rs.getInt("total_classes"),
                        rs.getInt("present_days"),
                        rs.getInt("absent_days")));
            }
            return rows;
        }
    }
}
