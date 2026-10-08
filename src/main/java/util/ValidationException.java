package util;

/**
 * Custom checked exception raised when user input does not satisfy
 * the business rules (empty fields, invalid roll number, duplicate student,
 * invalid semester, invalid attendance status, ...).
 * <p>
 * Being a checked exception, every caller is forced to handle it,
 * which keeps validation errors visible in the GUI instead of crashing
 * the application.
 * </p>
 */
public class ValidationException extends Exception {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
