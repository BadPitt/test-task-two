package ru.gpb;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class MainTest {
    private static final String OUTPUT_FILE_NAME_SP = "src/test/resources/testSellPoints.txt";
    private static final String OUTPUT_FILE_NAME_SP_REF = "src/test/resources/simpleOutputReffSellPoints.txt";
    private static final String OUTPUT_FILE_NAME_DATES = "src/test/resources/testDates.txt";
    private static final String OUTPUT_FILE_NAME_DATES_REF = "src/test/resources/simpleOutputReffDates.txt";
    private static final String INPUT_FILE_NAME = "src/test/resources/simpleInput.txt";

    private static final String CORRUPTED_INPUT_FILE_NAME = "src/test/resources/corrupted/input.txt";
    private static final String CORRUPTED_OUTPUT_FILE_NAME_DATES = "src/test/resources/corruptedTestDates.txt";
    private static final String CORRUPTED_OUTPUT_FILE_NAME_SP = "src/test/resources/corruptedTestSellPoints.txt";
    private static final String CORRUPTED_OUTPUT_FILE_NAME_DATES_REF = "src/test/resources/corrupted/outputReffDates.txt";
    private static final String CORRUPTED_OUTPUT_FILE_NAME_SP_REF = "src/test/resources/corrupted/outputReffSellPoints.txt";

    @BeforeClass
    public static void prepare() {
        clear(OUTPUT_FILE_NAME_SP);
        clear(OUTPUT_FILE_NAME_DATES);
    }

    @AfterClass
    public static void rmRf() {
        clear(OUTPUT_FILE_NAME_SP);
        clear(OUTPUT_FILE_NAME_DATES);
    }

    private static void clear(String fileName) {
        File outputFile = new File(fileName);
        if (outputFile.exists()) {
            outputFile.delete();
        }
    }

    @Test
    public void execHelpTest() {
        Main.main(new String[]{"--help"});
    }

    @Test
    public void execMainTest() throws IOException {
        exec(
                INPUT_FILE_NAME,
                OUTPUT_FILE_NAME_DATES,
                OUTPUT_FILE_NAME_SP,
                OUTPUT_FILE_NAME_DATES_REF,
                OUTPUT_FILE_NAME_SP_REF
        );
    }

    @Test
    public void execMainWithCorruptedInputFileTest() throws IOException {
        exec(
                CORRUPTED_INPUT_FILE_NAME,
                CORRUPTED_OUTPUT_FILE_NAME_DATES,
                CORRUPTED_OUTPUT_FILE_NAME_SP,
                CORRUPTED_OUTPUT_FILE_NAME_DATES_REF,
                CORRUPTED_OUTPUT_FILE_NAME_SP_REF
        );
    }

    private void exec(String inputFileName,
                      String outputFileNameByDates,
                      String outputFileNameBySellPoint,
                      String outputByDatesReffFileName,
                      String outputBySellPointReffFileName) throws IOException {
        Main.main(new String[]{
                new File(inputFileName).getPath(),
                new File(outputFileNameByDates).getPath(),
                new File(outputFileNameBySellPoint).getPath()
        });

        String sellPointsOutput = Files.readAllLines(Paths.get(outputFileNameBySellPoint)).stream()
                .reduce((s1, s2) -> "" + s1 + "\n" + s2)
                .get();
        String sellPointsOutputRef = Files.readAllLines(Paths.get(outputBySellPointReffFileName)).stream()
                .reduce((s1, s2) -> "" + s1 + "\n" + s2)
                .get();
        assertEquals(sellPointsOutputRef, sellPointsOutput);

        String datesOutput = Files.readAllLines(Paths.get(outputFileNameByDates)).stream()
                .reduce((s1, s2) -> "" + s1 + "\n" + s2)
                .get();
        String datesOutputRef = Files.readAllLines(Paths.get(outputByDatesReffFileName)).stream()
                .reduce((s1, s2) -> "" + s1 + "\n" + s2)
                .get();
        assertEquals(datesOutputRef, datesOutput);
    }

    @Test
    public void execMainFileNotFoundTest() {
        Main.main(new String[]{
                new File("fileNotFound").getPath(),
                new File(OUTPUT_FILE_NAME_SP).getPath(),
                new File(OUTPUT_FILE_NAME_DATES).getPath()
        });
    }
}
