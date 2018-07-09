package ru.gpb;

import ru.gpb.core.ApplicationException;

/**
 * Validator for input arguments
 *
 * @author Danil Popov
 */
public class InputValidator {
    public static void validate(String[] args) {
        if (args == null) {
            throw new ApplicationException();
        }
        if (!(isValidArgs(args) || isHelp(args))) {
            throw new ApplicationException();
        }
    }

    private static boolean isValidArgs(String[] args) {
        return args.length == 3;
    }

    private static boolean isHelp(String[] args) {
        return (args.length == 1 && "--help".equals(args[0]));
    }
}
