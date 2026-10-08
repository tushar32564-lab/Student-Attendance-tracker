package model;

import java.time.LocalDate;

/**
 * One attendance entry of a student for a particular date.
 */
public class Attendance {

    public static final String PRESENT = "Present";
    public static final String ABSENT = "Absent";

    private int attendanceId;
    private int studentId;
    private LocalDate attendanceDate;
    private String status;

    public Attendance() {
    }

    public Attendance(int studentId, LocalDate attendanceDate, String status) {
        this.studentId = studentId;
        this.attendanceDate = attendanceDate;
        this.status = status;
    }

    public int getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(int attendanceId) {
        this.attendanceId = attendanceId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /** Status must be exactly "Present" or "Absent". */
    public boolean isStatusValid() {
        return PRESENT.equals(status) || ABSENT.equals(status);
    }
}
