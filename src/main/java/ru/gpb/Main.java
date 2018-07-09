package ru.gpb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.gpb.core.Mappers;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static ru.gpb.core.Constants.DATE_FORMAT;
import static ru.gpb.core.Constants.FILE_CHARSET;

/**
 * @author Danil Popov
 * BadPit@211.ru
 * on 06.07.18
 */
public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final String HELP = "Arguments:\n" +
            "the first - input file name which contains operations\n" +
            "the second - output file name which contains sums by date\n" +
            "the third - output file name which contains sums by office\n" +
            "\n" +
            "For example:\n" +
            "java -jar task2.jar operations.txt sums-by-dates.txt sums-by-offices.txt";

    public static void main(String[] args) {
        try {
            // validate args
            InputValidator.validate(args);

            Options options = Options.parseOptions(args);

            if (options.getIsHelp()) {
                System.out.println(HELP);
                return;
            }

            sumByOffice(options);

            sumByDate(options);

        } catch (IOException ioe) {
            System.out.println("An error occurred while FS working: " + ioe.getMessage());
            LOGGER.error("IO troubles", ioe);
        } catch (Exception e) {
            System.out.println("Something went wrong: " + e.getMessage());
            LOGGER.error("Unhandled exception", e);
        }
    }

    private static void sumByDate(Options options) throws IOException {
        Files.readAllLines(Paths.get(options.getInputFileName())).stream()
                .map(Mappers::stringToRow)
                // reduce to map K - date string, V - sum
                .reduce(
                        new HashMap<String, BigDecimal>(),
                        (map, row) -> {
                            String key = DATE_FORMAT.format(row.getOperationDate());
                            map.merge(key, row.getOperationSum(), BigDecimal::add);
                            return map;
                        },
                        (map, map1) -> map)
                .entrySet().stream()
                // sort by date string
                .sorted(Comparator.comparing(Map.Entry::getKey))
                // every entry to string
                .map(entity -> "" + entity.getKey() + " " + entity.getValue() + "\n")
                .forEach(a -> writeStrToFile(a, options.getSumsByDateFileName()));
    }

    private static void sumByOffice(Options options) throws IOException {
        Files.readAllLines(Paths.get(options.getInputFileName())).stream()
                .map(Mappers::stringToRow)
                // reduce to map K - sell point name, V - sum
                .reduce(
                        new HashMap<String, BigDecimal>(),
                        (map, row) -> {
                            map.merge(row.getSellPoint(), row.getOperationSum(), BigDecimal::add);
                            return map;
                        },
                        (map, map1) -> map)
                .entrySet().stream()
                // sort by sum, reverse order
                .sorted(Comparator.comparing((Map.Entry<String, BigDecimal> e1) -> e1.getValue())
                        .reversed())
                // every entry to string
                .map(entity -> "" + entity.getKey() + " " + entity.getValue() + "\n")
                .forEach(a -> writeStrToFile(a, options.getSumsByOfficeFileName()));
    }

    static void writeStrToFile(String rowString, String fileName) {
        try {
            Files.write(
                    Paths.get(fileName),
                    rowString.getBytes(FILE_CHARSET),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            LOGGER.error("An error occurred", e);
        }
    }
}
