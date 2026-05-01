package oop.project.library.input;

/**
 * Signals invalid low-level command text before typed command parsing begins.
 */
public final class InputException extends Exception {

    public InputException(String message) {
        super(message);
    }

    public InputException(String message, Throwable cause) {
        super(message, cause);
    }
}
