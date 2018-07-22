package ru.gpb;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.gpb.core.Constants;
import ru.gpb.core.Mappers;
import ru.gpb.core.Row;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Danil Popov
 * BadPit@211.ru
 * on 22.07.18
 */
public class SumAllFilesApproach {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static final String END_OF_TEMP_FILE_NAME_SP = ".tmpsp";
    private static final String END_OF_TEMP_FILE_NAME_DATE = ".tmpdate";

    private static final ReentrantLock SELL_POINT_LOCK = new ReentrantLock();
    private static final ReentrantLock DATE_LOCK = new ReentrantLock();

    public static void sumAll(Options options) throws IOException {
        try {
            Path outputSp = Paths.get(options.getSumsByOfficeFileName());
            Path outputDate = Paths.get(options.getSumsByDateFileName());
            Files.lines(Paths.get(options.getInputFileName()))
                    .parallel()
                    .forEach(str -> {
                        // маппим на Row
                        Row row = Mappers.stringToRow(str);
                        try {
                            handleBySellPoint(row);
                            handleByDate(row);
                        } catch (Exception e) {
                            LOGGER.error("Unhandled exception", e);
                        }
                    });

            sumAllTempFilesBySellPoint(outputSp);
            sumAllTempFilesByDate(outputDate);

        } finally {
            deleteTempFiles();
        }
    }

    private static void deleteTempFiles() throws IOException {
        Files.find(Paths.get("."), 1, (path, basicFileAttributes) -> path.toString().endsWith(END_OF_TEMP_FILE_NAME_SP))
                .forEach(path -> path.toFile().delete());
        Files.find(Paths.get("."), 1, (path, basicFileAttributes) -> path.toString().endsWith(END_OF_TEMP_FILE_NAME_DATE))
                .forEach(path -> path.toFile().delete());
    }

    private static void sumAllTempFilesBySellPoint(Path outputSp) throws IOException {
        // проходимся по всем временным файлам и складываем данные в результирующий файл
        // не работает параллельно
        Files.find(Paths.get("."), 1, (path, basicFileAttributes) -> path.toString().endsWith(END_OF_TEMP_FILE_NAME_SP))
                .map(path -> {
                    try {
                        return Files.lines(path)
                                .map(s -> {
                                    String[] strings = s.split(" ");
                                    return new SumBySellPoint(strings[0], new BigDecimal(strings[1]));
                                })
                                .reduce((sumBySellPoint, sumBySellPoint2) -> sumBySellPoint)
                                .orElse(new SumBySellPoint(null, null));
                    } catch (IOException e) {
                        LOGGER.error("IO troubles", e);
                    }
                    return new SumBySellPoint(null, null);
                })
                // sort by sum
                .sorted(Comparator.comparing(SumBySellPoint::getResult, Comparator.reverseOrder()))
                .forEach(result -> {
                    Main.writeStrToFile(
                            "" + result.getSellPoint() + " " + result.getResult() + "\n",
                            outputSp.toString()
                    );
                });
    }

    private static void sumAllTempFilesByDate(Path outputDate) throws IOException {
        // проходимся по всем временным файлам и складываем данные в результирующий файл
        // не работает параллельно
        Files.find(Paths.get("."), 1, (path, basicFileAttributes) -> path.toString().endsWith(END_OF_TEMP_FILE_NAME_DATE))
                .map(path -> {
                    try {
                        return Files.lines(path)
                                .map(s -> {
                                    String[] strings = s.split(" ");
                                    return new SumByDate(strings[0], new BigDecimal(strings[1]));
                                })
                                // подразумеваем, что будет лишь одна строка в файле
                                .reduce((sumBySellPoint, sumBySellPoint2) -> sumBySellPoint)
                                .orElse(new SumByDate(null, null));
                    } catch (IOException e) {
                        LOGGER.error("IO troubles", e);
                    }
                    return new SumByDate(null, null);
                })
                // sort by sum
                .sorted(Comparator.comparing(SumByDate::getDate))
                .forEach(result -> {
                    Main.writeStrToFile(
                            "" + result.getDate() + " " + result.getResult() + "\n",
                            outputDate.toString()
                    );
                });
    }

    private static void handleBySellPoint(Row row) throws Exception {
        // читаем из временного файла с именем как ключ
        Path path = Paths.get(row.getSellPoint() + END_OF_TEMP_FILE_NAME_SP);
        lock(SELL_POINT_LOCK, () -> {
            if (Files.exists(path)) {
                Files.lines(path)
                        .filter(s -> !s.isEmpty())
                        .map(s -> {
                            String[] strings = s.split(" ");
                            return new SumBySellPoint(strings[0], new BigDecimal(strings[1]));
                        })
                        .filter(sum -> Objects.equals(row.getSellPoint(), sum.getSellPoint()))
                        .map(sum -> new SumBySellPoint(sum.getSellPoint(), row.getOperationSum().add(sum.getResult())))
                        .map(sum -> "" + sum.getSellPoint() + " " + sum.getResult() + "\n")
                        // пишем во временный файл, увеличиваем сумму
                        .forEach(strSum -> Main.writeStrToFile(path.toString(), strSum, StandardOpenOption.CREATE));
            } else {
                // пишем во временный файл, увеличиваем сумму
                Main.writeStrToFile(
                        path.toString(),
                        "" + row.getSellPoint() + " " + row.getOperationSum() + "\n",
                        StandardOpenOption.CREATE);
            }
        });
    }

    private static void handleByDate(Row row) throws Exception {
        // читаем из временного файла с именем как ключ
        Path path = Paths.get(Constants.DATE_FORMAT.format(row.getOperationDate()) + END_OF_TEMP_FILE_NAME_DATE);
        lock(DATE_LOCK, () -> {
            if (Files.exists(path)) {
                Files.lines(path)
                        .filter(s -> !s.isEmpty())
                        .map(s -> {
                            String[] strings = s.split(" ");
                            return new SumByDate(strings[0], new BigDecimal(strings[1]));
                        })
                        .filter(sum -> Objects.equals(Constants.DATE_FORMAT.format(row.getOperationDate()), sum.getDate()))
                        .map(sum -> new SumByDate(sum.getDate(), row.getOperationSum().add(sum.getResult())))
                        .map(sum -> "" + sum.getDate() + " " + sum.getResult() + "\n")
                        // пишем во временный файл, увеличиваем сумму
                        .forEach(strSum -> Main.writeStrToFile(path.toString(), strSum, StandardOpenOption.CREATE));
            } else {
                // пишем во временный файл, увеличиваем сумму
                Main.writeStrToFile(
                        path.toString(),
                        "" + Constants.DATE_FORMAT.format(row.getOperationDate()) + " " + row.getOperationSum() + "\n",
                        StandardOpenOption.CREATE);
            }
        });
    }

    private static void lock(Lock lock, Main.Task task) throws Exception {
        try {
            while (true) {
                if (!lock.tryLock()) {
                    continue;
                }
                task.run();
                break;
            }
        } finally {
            lock.unlock();
        }
    }

    @Data
    static class SumBySellPoint {
        private final String sellPoint;
        private final BigDecimal result;

        SumBySellPoint(String sellPoint, BigDecimal result) {
            this.sellPoint = sellPoint;
            this.result = result;
        }
    }

    @Data
    static class SumByDate {
        private final String date;
        private final BigDecimal result;

        SumByDate(String date, BigDecimal result) {
            this.date = date;
            this.result = result;
        }
    }
}
