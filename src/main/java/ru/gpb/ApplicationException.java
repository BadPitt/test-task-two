package ru.gpb;

/**
 * Base application exception
 *
 * @author Danil Popov
 */
public class ApplicationException extends RuntimeException {
    public ApplicationException() {
        super("Please look at the help page(--help)");
    }
}
