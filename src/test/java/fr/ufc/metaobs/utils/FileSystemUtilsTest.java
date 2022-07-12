package fr.ufc.metaobs.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class FileSystemUtilsTest {

    private String testString = "Ceci" + System.lineSeparator() +
            "est un test" + System.lineSeparator() +
            "sur plusieurs" + System.lineSeparator() +
            "lines" + System.lineSeparator() +
            "avec plusieurs lines" + System.lineSeparator() +
            "qui doit remplacer" + System.lineSeparator() +
            "lines par lignes";

    private String expectedString = "Ceci" + System.lineSeparator() +
            "est un test" + System.lineSeparator() +
            "sur plusieurs" + System.lineSeparator() +
            "lignes" + System.lineSeparator() +
            "avec plusieurs lignes" + System.lineSeparator() +
            "qui doit remplacer" + System.lineSeparator() +
            "lignes par lignes";

    @BeforeEach
    void before() {
        File file = new File(getClass().getResource("test.txt").getFile());
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write(testString);
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testReplaceStrInFiles() {
        File file = new File(getClass().getResource("test.txt").getFile());
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String oldLines = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
            assertEquals(testString, oldLines);
            FileSystemUtils.replaceStrInFile(file, "lines", "lignes");
            BufferedReader bufferedReader1 = new BufferedReader(new FileReader(file));
            String newLines = bufferedReader1.lines().collect(Collectors.joining(System.lineSeparator()));
            assertEquals(expectedString, newLines);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}