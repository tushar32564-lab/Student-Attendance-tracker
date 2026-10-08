package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * The only place where the JDBC connection settings are kept.
 * <p>
 * Every DAO obtains its connection from {@link #getConnection()}.
 * </p>
 *
 * <p><b>Where the password comes from</b> (first source that has a value wins):</p>
 * <ol>
 *   <li>JVM system property {@code -Ddb.password=...}</li>
 *   <li>{@value #CONFIG_FILE} file in the folder the program was started from,
 *       next to the jar, or in the project folder - this file is listed in
 *       {@code .gitignore}, so the real password is never pushed to GitHub</li>
 *   <li>Environment variable {@code DB_PASSWORD}</li>
 *   <li>{@link #DB_PASSWORD_PLACEHOLDER} - placeholder, connection will fail
 *       until a real password is configured</li>
 * </ol>
 *
 * <p>The file contains a single line (create it once, edit the value):</p>
 * <pre>db.password=your_mysql_password</pre>
 */
public final class DatabaseConnection {

    public static final String DB_URL =
            "jdbc:mysql://localhost:3306/attendance_db"
                    + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    public static final String DB_USER = "attendance_user";

    /** Local configuration file name (kept out of git by .gitignore). */
    public static final String CONFIG_FILE = "db.properties";

    /** Key used inside {@link #CONFIG_FILE} and by the {@code -Ddb.password} property. */
    public static final String PASSWORD_KEY = "db.password";

    /** Fallback placeholder - replace it by creating {@link #CONFIG_FILE}. */
    public static final String DB_PASSWORD_PLACEHOLDER = "YOUR_PASSWORD";

    /** Utility class - cannot be instantiated. */
    private DatabaseConnection() {
    }

    /**
     * Opens a new database connection.
     * The caller must close it, preferably with try-with-resources.
     *
     * @return an open connection to attendance_db
     * @throws SQLException if MySQL is not reachable or the credentials are wrong
     */
    public static Connection getConnection() throws SQLException {
        Credentials credentials = resolveCredentials();
        try {
            return DriverManager.getConnection(DB_URL, DB_USER, credentials.password);
        } catch (SQLException e) {
            throw new SQLException(
                    "Could not connect to MySQL (" + DB_URL + ") as user '" + DB_USER + "'. "
                            + "Password source used: " + credentials.source + ". "
                            + "Put your MySQL password into " + CONFIG_FILE
                            + " (key '" + PASSWORD_KEY + "') or set the DB_PASSWORD "
                            + "environment variable. Details: " + e.getMessage(),
                    e.getSQLState(),
                    e.getErrorCode(),
                    e);
        }
    }

    /** Resolution order: system property, db.properties file, environment variable, placeholder. */
    private static Credentials resolveCredentials() {
        String value = System.getProperty(PASSWORD_KEY);
        if (isSet(value)) {
            return new Credentials(value, "JVM property -D" + PASSWORD_KEY);
        }

        File file = findConfigFile();
        if (file != null) {
            value = readPasswordFile(file);
            if (isSet(value)) {
                return new Credentials(value, CONFIG_FILE + " (" + file.getAbsolutePath() + ")");
            }
        }

        value = System.getenv("DB_PASSWORD");
        if (isSet(value)) {
            return new Credentials(value, "DB_PASSWORD environment variable");
        }

        return new Credentials(DB_PASSWORD_PLACEHOLDER,
                "built-in placeholder - no password configured");
    }

    /** Looks for the local config file in the working folder, beside the jar, or in the project folder. */
    private static File findConfigFile() {
        File[] candidates = new File[]{
                new File(CONFIG_FILE),          // folder the program was started from
                codeSourceFile(CONFIG_FILE),    // folder containing the classes / jar
                codeSourceParentFile(CONFIG_FILE) // project folder when running target/xxx.jar
        };
        for (File candidate : candidates) {
            if (candidate != null && candidate.isFile()) {
                return candidate;
            }
        }
        return null;
    }

    private static String readPasswordFile(File file) {
        Properties properties = new Properties();
        try (InputStream in = new FileInputStream(file)) {
            properties.load(in);
            return properties.getProperty(PASSWORD_KEY);
        } catch (IOException e) {
            System.err.println("Warning: could not read " + file.getAbsolutePath()
                    + " (" + e.getMessage() + ")");
            return null;
        }
    }

    private static File codeSourceFile(String name) {
        File directory = codeSourceDirectory();
        return directory == null ? null : new File(directory, name);
    }

    private static File codeSourceParentFile(String name) {
        File directory = codeSourceDirectory();
        if (directory == null || directory.getParentFile() == null) {
            return null;
        }
        return new File(directory.getParentFile(), name);
    }

    /** Directory of the running classes or jar (empty when it cannot be determined). */
    private static File codeSourceDirectory() {
        try {
            if (DatabaseConnection.class.getProtectionDomain().getCodeSource() == null) {
                return null;
            }
            Path location = Paths.get(DatabaseConnection.class
                    .getProtectionDomain().getCodeSource().getLocation().toURI());
            return Files.isDirectory(location) ? location.toFile() : location.toFile().getParentFile();
        } catch (URISyntaxException | RuntimeException e) {
            return null;
        }
    }

    private static boolean isSet(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /** Holds the resolved password together with a readable description of its origin. */
    private static final class Credentials {

        private final String password;
        private final String source;

        private Credentials(String password, String source) {
            this.password = password;
            this.source = source;
        }
    }
}
