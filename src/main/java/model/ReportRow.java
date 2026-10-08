package model;

/**
 * One row of the attendance report:
 * how many classes a student had, how many were attended,
 * and the resulting percentage.
 */
public class ReportRow {

    /** Minimum attendance required by the college. */
    public static final double MINIMUM_PERCENTAGE = 75.0;

    private final String rollNo;
    private final String name;
    private final int totalClasses;
    private final int presentDays;
    private final int absentDays;

    public ReportRow(String rollNo, String name, int totalClasses, int presentDays, int absentDays) {
        this.rollNo = rollNo;
        this.name = name;
        this.totalClasses = totalClasses;
        this.presentDays = presentDays;
        this.absentDays = absentDays;
    }

    public String getRollNo() {
        return rollNo;
    }

    public String getName() {
        return name;
    }

    public int getTotalClasses() {
        return totalClasses;
    }

    public int getPresentDays() {
        return presentDays;
    }

    public int getAbsentDays() {
        return absentDays;
    }

    /**
     * Attendance percentage rounded to two decimal places.
     * Returns 0.0 when no class has been marked yet.
     */
    public double getPercentage() {
        if (totalClasses == 0) {
            return 0.0;
        }
        double percentage = (presentDays * 100.0) / totalClasses;
        return Math.round(percentage * 100.0) / 100.0;
    }

    /** True when the student does not meet the 75% requirement. */
    public boolean isBelowMinimum() {
        return getPercentage() < MINIMUM_PERCENTAGE;
    }
}
