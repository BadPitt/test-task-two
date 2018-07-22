package ru.gpb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.gpb.core.Mappers;
import ru.gpb.core.Row;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

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
    private static final Row EMPTY_ROW = new Row();

    public static void main(String[] args) {
        try {
            // validate args
            InputValidator.validate(args);

            Options options = Options.parseOptions(args);

            if (options.getIsHelp()) {
                System.out.println(HELP);
                return;
            }

            doInParallelAndWaitFinish(
                    () -> Main.sumByDate(options),
                    () -> Main.sumByOffice(options)
            );


        } catch (ExecutionException ee) {
            if (ee.getCause() instanceof IOException) {
                System.out.println("An error occurred while FS working: " + ee.getCause().getMessage());
                LOGGER.error("IO troubles", ee.getCause());
            } else {
                System.out.println("Something went wrong: " + ee.getMessage());
                LOGGER.error("Unhandled exception", ee);
            }
        } catch (Exception e) {
            System.out.println("Something went wrong: " + e.getMessage());
            LOGGER.error("Unhandled exception", e);
        }
    }

    static void doInParallelAndWaitFinish(Task... tasks) throws Exception {
        List<Callable<Object>> callables = new ArrayList<>();
        for (Task task : tasks) {
            callables.add(new TaskCallableAdapter<>(task, null));
        }
        ForkJoinPool pool = ForkJoinPool.commonPool();
        List<Future<Object>> results = pool.invokeAll(callables);

        for (Future<Object> result : results) {
            result.get();
        }
    }

    static void sumByDate(Options options) throws IOException {
        Files.lines(Paths.get(options.getInputFileName()))
                .map(str -> {
                    try {
                        return Mappers.stringToRow(str);
                    } catch (Exception e) {
                        LOGGER.error("An error occurs during mapping operation", e);
                    }
                    return EMPTY_ROW;
                })
                // reduce to map K - date string, V - sum
                .reduce(
                        new HashMap<String, BigDecimal>(),
                        (map, row) -> {
                            if (row.getOperationDate() != null && row.getOperationSum() != null) {
                                String key = DATE_FORMAT.format(row.getOperationDate());
                                map.merge(key, row.getOperationSum(), BigDecimal::add);
                            }
                            return map;
                        },
                        (map, map1) -> map)
                .entrySet().stream()
                // sort by date string
                .sorted(Map.Entry.comparingByKey())
                // every entry to string
                .map(entity -> "" + entity.getKey() + " " + entity.getValue() + "\n")
                .forEach(a -> writeStrToFile(a, options.getSumsByDateFileName()));
    }

    static void sumByOffice(Options options) throws IOException {
        Files.lines(Paths.get(options.getInputFileName()))
                .map(str -> {
                    try {
                        return Mappers.stringToRow(str);
                    } catch (Exception e) {
                        LOGGER.error("An error occurs during mapping operation", e);
                    }
                    return EMPTY_ROW;
                })
                // reduce to map K - sell point name, V - sum
                .reduce(
                        new HashMap<String, BigDecimal>(),
                        (map, row) -> {
                            if (row.getSellPoint() != null && row.getOperationSum() != null) {
                                map.merge(row.getSellPoint(), row.getOperationSum(), BigDecimal::add);
                            }
                            return map;
                        },
                        (map, map1) -> map)
                .entrySet().stream()
                // sort by sum, reverse order
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                // every entry to string
                .map(entity -> "" + entity.getKey() + " " + entity.getValue() + "\n")
                .forEach(a -> writeStrToFile(a, options.getSumsByOfficeFileName()));
    }

    static void sumAll(Options options) throws IOException {
        SumAllFilesApproach.sumAll(options);
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

    static void writeStrToFile(String rowString, String fileName, StandardOpenOption... openOptions) {
        try {
            Files.write(
                    Paths.get(fileName),
                    rowString.getBytes(FILE_CHARSET),
                    openOptions
            );
        } catch (IOException e) {
            LOGGER.error("An error occurred", e);
        }
    }

    interface Task {
        void run() throws Exception;
    }

    static class TaskCallableAdapter<T> implements Callable<T> {
        private final Task task;
        private final T result;

        public TaskCallableAdapter(Task task, T result) {
            this.task = task;
            this.result = result;
        }

        @Override
        public T call() throws Exception {
            task.run();
            return result;
        }
    }
}
