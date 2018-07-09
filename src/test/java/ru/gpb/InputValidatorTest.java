package ru.gpb;

import org.junit.Test;
import ru.gpb.core.ApplicationException;

/**
 * @author Danil Popov
 *         BadPit@211.ru
 */
public class InputValidatorTest {

    private InputValidator validator = new InputValidator();

    @Test
    public void testMainScenario3Args() {
        String[] args = new String[]{"inputFileName", "sumsByOfficeFileName", "sumsByDateFileName"};
        validator.validate(args);
    }

    @Test
    public void testMainScenario1Args() {
        String[] args = new String[]{"--help"};
        validator.validate(args);
    }

    @Test(expected = ApplicationException.class)
    public void testErrorScenarioEmptyArgs() {
        String[] args = new String[]{};
        validator.validate(args);
    }

    @Test(expected = ApplicationException.class)
    public void testErrorScenarioNullArgs() {
        validator.validate(null);
    }

}
