package ru.gpb;

import ru.gpb.core.Mappers;
import ru.gpb.core.Row;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.gpb.core.Constants.DATE_FORMAT;
import static ru.gpb.core.Constants.FILE_CHARSET;

/**
 * @author Danil Popov
 * BadPit@211.ru
 * on 06.07.18
 */
public class Main {
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
            InputValidator validator = new InputValidator();
            validator.validate(args);

            Options options = Options.parseOptions(args);

            if (options.getIsHelp()) {
                System.out.println(HELP);
                return;
            }

            List<Row> rows = getAllRows(options.getInputFileName());

            String sumsByOffice = getSumsByOffice(rows);

            String sumsByDate = getSumsByDate(rows);

            writeToOutput(options, sumsByOffice, sumsByDate);

        } catch (IOException ioe) {
            System.out.println("An error occurred while FS working: " + ioe.getMessage());
        } catch (Exception e) {
            System.out.println("Something went wrong: " + e.getMessage());
        }
    }

    static void writeToOutput(Options options, String sumsByOffice, String sumsByDate) throws IOException {
        Files.write(
                Paths.get(options.getSumsByOfficeFileName()),
                sumsByOffice.getBytes(Charset.forName(FILE_CHARSET)),
                StandardOpenOption.CREATE
        );
        Files.write(
                Paths.get(options.getSumsByDateFileName()),
                sumsByDate.getBytes(Charset.forName(FILE_CHARSET)),
                StandardOpenOption.CREATE
        );
    }

    static List<Row> getAllRows(String inputFileName) throws IOException {
        return Files.readAllLines(Paths.get(inputFileName)).stream()
                .map(Mappers::stringToRow)
                .collect(Collectors.toList());
    }

    static String getSumsByDate(List<Row> rows) {
        return rows.stream()
                // reduce to map K - date string, V - sum
                .reduce(
                        new HashMap<String, BigDecimal>(),
                        (map, row) -> {
                            String key = DATE_FORMAT.format(row.getOperationDate());
                            if (map.containsKey(key)) {
                                map.put(key, map.get(key).add(row.getOperationSum()));
                            } else {
                                map.put(key, row.getOperationSum());
                            }
                            return map;
                        },
                        (map, map1) -> map)
                .entrySet().stream()
                // sort by date string
                .sorted(Comparator.comparing(Map.Entry::getKey))
                // every entry to string
                .map(entity -> "" + entity.getKey() + " " + entity.getValue())
                // every string to one string
                .reduce((str1, str2) -> str1 + "\n" + str2)
                .get();
    }

    static String getSumsByOffice(List<Row> rows) {
        return rows.stream()
                // reduce to map K - sell point name, V - sum
                .reduce(
                        new HashMap<String, BigDecimal>(),
                        (map, row) -> {
                            if (map.containsKey(row.getSellPoint())) {
                                map.put(row.getSellPoint(), map.get(row.getSellPoint()).add(row.getOperationSum()));
                            } else {
                                map.put(row.getSellPoint(), row.getOperationSum());
                            }
                            return map;
                        },
                        (map, map1) -> map)
                .entrySet().stream()
                // sort by sum, reverse order
                .sorted(Comparator.comparing((Map.Entry<String, BigDecimal> e1) -> e1.getValue())
                        .reversed())
                // every entry to string
                .map(entity -> "" + entity.getKey() + " " + entity.getValue())
                // every string to one string
                .reduce((str1, str2) -> str1 + "\n" + str2)
                .get();
    }
}
