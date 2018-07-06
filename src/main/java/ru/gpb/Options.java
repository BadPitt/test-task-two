package ru.gpb;

import lombok.Builder;
import lombok.Getter;

/**
 * Representation of application's arguments
 *
 * @author Danil Popov
 */
@Builder
public class Options {

    public static Options parseOptions(String[] args) {
        if ("--help".equals(args[0])) {
            return new OptionsBuilder()
                    .isHelp(true)
                    .build();
        } else {
            return new OptionsBuilder()
                    .inputFileName(args[0])
                    .sumsByDateFileName(args[1])
                    .sumsByOfficeFileName(args[2])
                    .isHelp(false)
                    .build();
        }
    }

    @Getter
    private String inputFileName;
    @Getter
    private String sumsByDateFileName;
    @Getter
    private String sumsByOfficeFileName;
    @Getter
    private Boolean isHelp;
}
