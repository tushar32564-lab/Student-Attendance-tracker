package dao;

import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data access for the {@code users} table (login).
 */
public class UserDAO {

    private static final String SQL_FIND_USER =
            "SELECT password FROM users WHERE username = ?";

    /**
     * Checks the given credentials against the database.
     *
     * @return true when the username exists and the password matches
     * @throws SQLException if the query cannot be executed
     */
    public boolean validateUser(String username, String password) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_USER)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    return storedPassword != null && storedPassword.equals(password);
                }
                return false;
            }
        }
    }
}
