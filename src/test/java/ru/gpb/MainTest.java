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
    private static final String OUTPUT_FILE_NAME_SP = "src/test/resources/test1.txt";
    private static final String OUTPUT_FILE_NAME_SP_REF = "src/test/resources/simpleOutputReffSellPoints.txt";
    private static final String OUTPUT_FILE_NAME_DATES = "src/test/resources/test2.txt";
    private static final String OUTPUT_FILE_NAME_DATES_REF = "src/test/resources/simpleOutputReffDates.txt";
    private static final String INPUT_FILE_NAME = "src/test/resources/simpleInput.txt";

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
        Main.main(new String[]{
                new File(INPUT_FILE_NAME).getPath(),
                new File(OUTPUT_FILE_NAME_DATES).getPath(),
                new File(OUTPUT_FILE_NAME_SP).getPath()
        });

        String sellPointsOutput = Files.readAllLines(Paths.get(OUTPUT_FILE_NAME_SP)).stream()
                .reduce((s1,s2)->""+s1+"\n"+s2)
                .get();
        String sellPointsOutputRef = Files.readAllLines(Paths.get(OUTPUT_FILE_NAME_SP_REF)).stream()
                .reduce((s1,s2)->""+s1+"\n"+s2)
                .get();
        assertEquals(sellPointsOutputRef, sellPointsOutput);

        String datesOutput = Files.readAllLines(Paths.get(OUTPUT_FILE_NAME_DATES)).stream()
                .reduce((s1,s2)->""+s1+"\n"+s2)
                .get();
        String datesOutputRef = Files.readAllLines(Paths.get(OUTPUT_FILE_NAME_DATES_REF)).stream()
                .reduce((s1,s2)->""+s1+"\n"+s2)
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
