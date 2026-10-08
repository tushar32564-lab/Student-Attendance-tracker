# Student Attendance Tracker

A simple desktop application built with **Java Swing**, **MySQL** and **JDBC** that helps
college teachers manage student attendance without registers or spreadsheets.

---

## 1. Project Description

The **Student Attendance Tracker** is a GUI-based Java desktop application. A teacher can log in,
add students, mark attendance for a date, and instantly see an attendance report with percentages
and a clear warning for students who fall below the 75% attendance requirement.

The project follows a clean layered architecture:

```
GUI (Swing)  -->  Service (business logic)  -->  DAO (JDBC)  -->  MySQL
```

It is intentionally kept simple and uses **only Java, Swing, MySQL, JDBC and Maven**, so the whole
codebase can be understood and explained in a viva.

---

## 2. Problem Statement

Manual attendance management is time-consuming and makes it difficult to maintain records and
calculate attendance percentages. Registers are hard to search, attendance percentages have to be
calculated by hand, and students below the required attendance are noticed too late.

This application solves that by storing attendance in a database and generating the report
(percentage, present/absent days, shortage list) automatically.

---

## 3. Features

| # | Feature | Description |
|---|---------|-------------|
| 1 | **Login** | Username and password validated against the `users` table |
| 2 | **Dashboard** | Add Student, View Students, Mark Attendance, Attendance Report, Logout |
| 3 | **Add Student** | Roll number, name, course, semester - saved to MySQL with validation |
| 4 | **View Students** | All students in a `JTable` with search by roll no / name / course |
| 5 | **Mark Attendance** | Pick a date, mark each student Present or Absent, save to MySQL |
| 6 | **Attendance Report** | Total classes, present, absent, percentage; students below 75% are highlighted in red |
| 7 | **Validation** | Empty fields, invalid roll number, invalid semester, duplicate roll number, invalid date, invalid login, invalid status, database errors - all shown with clear messages |
| 8 | **Re-mark a day** | If attendance already exists for a date the user is asked before replacing it |

---

## 4. Technologies Used

| Technology | Purpose |
|------------|---------|
| **Java 17+** | Core language (built and tested on JDK 17+) |
| **Java Swing** | Desktop GUI (`JFrame`, `JPanel`, `JTable`, `JOptionPane`, layout managers) |
| **MySQL 8** | Relational database (`attendance_db`) |
| **JDBC** | `Connection`, `PreparedStatement`, `ResultSet`, `SQLException` |
| **Maven** | Dependency management, build and packaging |
| **MySQL Connector/J** | MySQL JDBC driver (`com.mysql:mysql-connector-j`) |

No Spring Boot, no web framework, no JavaScript - only the above.

---

## 5. Project Structure

```
StudentAttendanceTracker/
├── pom.xml                      # Maven configuration + MySQL driver dependency
├── database.sql                 # Database, tables and the default admin user
├── README.md
├── .gitignore
└── src/main/java/
    ├── Main.java                # Entry point (starts the login window)
    ├── model/                   # Data / entity classes
    │   ├── Person.java          # abstract base class      -> Abstraction, Inheritance
    │   ├── Student.java         # extends Person           -> Encapsulation, Inheritance
    │   ├── Attendance.java      # one attendance record
    │   └── ReportRow.java       # one report row + percentage logic
    ├── dao/                     # JDBC code only (no UI logic)
    │   ├── UserDAO.java         # login query
    │   ├── StudentDAO.java      # insert / list / search students
    │   └── AttendanceDAO.java   # insert / delete / report query
    ├── service/                 # business rules + validation
    │   ├── AttendanceService.java        -> Interface (Abstraction, Polymorphism)
    │   └── AttendanceServiceImpl.java    -> Implementation + synchronization
    ├── gui/                     # Swing windows
    │   ├── LoginFrame.java
    │   ├── DashboardFrame.java
    │   ├── AddStudentFrame.java
    │   ├── StudentListFrame.java
    │   ├── AttendanceFrame.java
    │   └── ReportFrame.java
    └── util/
        ├── DatabaseConnection.java       # DB URL, user, password in ONE place
        └── ValidationException.java      # custom checked exception
```

**Separation of concerns:** GUI classes never contain SQL, DAO classes never contain UI code,
and business rules live in the service layer.

---

## 6. Database Setup

### Step 1 - Create the database

Open **MySQL Workbench** (or a terminal) as a user that can create databases and run the script:

```bash
mysql -u root -p < database.sql
```

Or in MySQL Workbench: *File → Open SQL Script → database.sql → Execute (lightning icon)*.

The script creates:

| Table | Columns |
|-------|---------|
| `users` | `user_id` INT PK AI, `username` VARCHAR(50) UNIQUE, `password` VARCHAR(100) |
| `students` | `student_id` INT PK AI, `roll_no` VARCHAR(20) UNIQUE, `name` VARCHAR(100), `course` VARCHAR(50), `semester` INT |
| `attendance` | `attendance_id` INT PK AI, `student_id` INT (FK → students), `attendance_date` DATE, `status` VARCHAR(10) |

and inserts the default login user (`admin` / `admin123`).

### Step 2 - Create the application database account (one time)

The application connects as `attendance_user`. Run once (change the password yourself):

```sql
CREATE USER IF NOT EXISTS 'attendance_user'@'localhost' IDENTIFIED BY 'YOUR_PASSWORD';
GRANT ALL PRIVILEGES ON attendance_db.* TO 'attendance_user'@'localhost';
FLUSH PRIVILEGES;
```

> The same statements are available commented-out at the bottom of `database.sql`.

---

## 7. JDBC Configuration

All connection settings are kept in **one class**: `src/main/java/util/DatabaseConnection.java`

```java
public static final String DB_URL    = "jdbc:mysql://localhost:3306/attendance_db?...";
public static final String DB_USER   = "attendance_user";
public static final String CONFIG_FILE = "db.properties";       // local password file
public static final String DB_PASSWORD_PLACEHOLDER = "YOUR_PASSWORD";
```

**The password is never written in Java code and never committed to GitHub.**
Create a file named `db.properties` in the project folder (it is already listed in `.gitignore`):

```properties
db.password=your_mysql_password
```

Resolution order - the first source that has a value wins:

| # | Source | Example |
|---|--------|---------|
| 1 | JVM system property | `java -Ddb.password=... -jar app.jar` |
| 2 | **`db.properties`** (project folder / next to the jar) | `db.password=...` |
| 3 | Environment variable | `DB_PASSWORD=...` |
| 4 | Built-in placeholder | connection fails and the error tells you which source was used |

The error dialog always reports **which source supplied the password**, so a wrong or missing
configuration is easy to spot.

Alternative (environment variable instead of the file):

```powershell
# Windows PowerShell
$env:DB_PASSWORD = "your_mysql_password"
mvn compile exec:java
```

```bash
# Linux / macOS
export DB_PASSWORD=your_mysql_password
mvn compile exec:java
```

> Do **not** put `db.properties` inside `src/main/resources` - it would be packed into the jar.

Every DAO uses `try-with-resources`, `PreparedStatement` (no string concatenation of SQL,
so no SQL injection) and proper `SQLException` handling. The insert of a full day of attendance
runs inside a **transaction** with rollback on failure.

---

## 8. How to Run the Project

### Prerequisites

* JDK 17 or newer
* Maven 3.8+
* MySQL Server 8 running on `localhost:3306`

### Steps

```bash
# 1. Get the source
git clone <your-repo-url>
cd StudentAttendanceTracker

# 2. Create the database (section 6)
mysql -u root -p < database.sql

# 3. Tell the app your MySQL password - create db.properties in this folder:
#    db.password=your_mysql_password      (this file is ignored by git)
#    Alternative: set the DB_PASSWORD environment variable (section 7)

# 4. Compile and run
mvn compile exec:java
```

**Using IntelliJ IDEA:** open the folder as a Maven project → wait for Maven import →
run `Main.java`. The password is read from `db.properties` in the project folder, so no
run configuration is required (you can still use the `DB_PASSWORD` environment variable in
*Run → Edit Configurations* instead).

**Packaged jar** (already contains the MySQL driver):

```bash
mvn package
java -jar target/student-attendance-tracker-1.0.0-jar-with-dependencies.jar
```

Run it from the project folder (or keep `db.properties` next to the jar).

---

## 9. Default Login Credentials

| Field | Value |
|-------|-------|
| **Username** | `admin` |
| **Password** | `admin123` |

Database account used by the application:

| Field | Value |
|-------|-------|
| **User** | `attendance_user` |
| **Password** | whatever you chose in `CREATE USER` (kept out of the repository) |

---

## 10. Screens / Features

1. **Login window** - username, password, *Login* / *Exit*; invalid credentials show an error
   dialog and clear the password field. *Enter* key also logs in.
2. **Dashboard** - four large menu buttons plus *Logout* (with confirmation).
3. **Add Student** - form with course combo box and semester field; success message and the form
   is cleared after saving.
4. **View Students** - read-only table with search box, *Search / Show All / Refresh / Back* and a
   "n student(s) found" status label.
5. **Mark Attendance** - date field (`YYYY-MM-DD`, defaults to today, *Today* button), all students
   listed with a **Present/Absent combo box per row** (green/red text). Duplicate dates ask for
   confirmation before replacing.
6. **Attendance Report** - generated in a **background thread** with a progress bar so the window
   never freezes; shows total classes, present, absent, percentage and status. Rows **below 75%
   are highlighted in red** and the summary counts them.

---

## 11. OOP, Collections and Multithreading (viva notes)

| Rubric item | Where it is used |
|-------------|------------------|
| **Encapsulation** | Private fields with getters/setters in `Person`, `Student`, `Attendance`, `ReportRow` |
| **Inheritance** | `Student extends Person` |
| **Abstraction** | Abstract class `Person`, interface `AttendanceService` |
| **Polymorphism** | `AttendanceService service = new AttendanceServiceImpl()`; overridden `toString()` |
| **Interfaces** | `AttendanceService` implemented by `AttendanceServiceImpl` |
| **Exception handling** | Custom checked `ValidationException`, `SQLException` handling, try-with-resources, rollback |
| **Collections & generics** | `List<Student>`, `ArrayList<Student>`, `List<Attendance>`, `List<ReportRow>` |
| **Multithreading** | `SwingWorker` builds the report off the EDT (GUI stays responsive) |
| **Synchronization** | `synchronized` blocks in `AttendanceServiceImpl` make "check then insert/replace" atomic for students and attendance |

---

## 12. Future Improvements

* Hash the passwords (BCrypt) instead of plain text
* Add / edit / delete students from the GUI
* Filter the report by course, semester and date range
* Export the report to PDF or Excel
* Per-subject attendance instead of a single overall percentage
* Date picker component instead of typing the date
* Connection pooling (HikariCP) and an external `db.properties` file
* Unit tests with JUnit

---

## License

This project is provided for academic / educational use.
