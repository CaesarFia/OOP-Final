package oop.project.library.argument;

public class ArgumentException extends Exception {

    public ArgumentException(String message) {
        super(message);
    }

    public ArgumentException(String message, Throwable cause) {
        super(message, cause);
    }

}
