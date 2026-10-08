package model;

/**
 * A student enrolled in a course.
 * Inherits {@code id} and {@code name} from {@link Person} (inheritance).
 */
public class Student extends Person {

    private String rollNo;
    private String course;
    private int semester;

    public Student() {
    }

    public Student(int id, String rollNo, String name, String course, int semester) {
        super(id, name);
        this.rollNo = rollNo;
        this.course = course;
        this.semester = semester;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    /** Overridden from Person - a simple example of runtime polymorphism. */
    @Override
    public String toString() {
        return "Student{rollNo='" + rollNo + "', name='" + getName()
                + "', course='" + course + "', semester=" + semester + "}";
    }
}
