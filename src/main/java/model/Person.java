package model;

/**
 * Base class for every person stored by the system.
 * <p>
 * Marked {@code abstract} because a person is never created directly -
 * only concrete types such as {@link Student} are used. This also
 * demonstrates abstraction together with the {@code AttendanceService} interface.
 * </p>
 */
public abstract class Person {

    private int id;
    private String name;

    public Person() {
    }

    public Person(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Person{id=" + id + ", name='" + name + "'}";
    }
}
