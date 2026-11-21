package ru.playfair;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.util.Scanner;

/** Проверки ошибочного ввода и завершения потока. */
class ConsoleUITest {
    @TempDir Path directory;

    @Test
    void eofTerminates() {
        assertTimeoutPreemptively(
                Duration.ofSeconds(3), () -> assertTrue(run("").contains("завершила")));
    }

    @Test
    void eofInsideOperationTerminates() {
        assertTimeoutPreemptively(
                Duration.ofSeconds(3), () -> assertTrue(run("1\n").contains("завершила")));
    }

    @Test
    void invalidMenuAndCancel() {
        String output = run("wrong\n1\nотмена\n3\n");
        assertTrue(output.contains("Допустимо"));
        assertTrue(output.contains("завершила"));
    }

    @Test
    void invalidPathDoesNotCrash() {
        String output = run("1\ninvalid\u0000path\nотмена\n3\n");
        assertTrue(output.contains("Ошибка"));
        assertTrue(output.contains("завершила"));
    }

    @Test
    void actualConsoleEncryption() throws Exception {
        Path src = Files.write(directory.resolve("source"), new byte[] {1, 2, 3});
        Path out = directory.resolve("out");
        String output = run("1\n" + src + "\nab\nsecret\n" + src + "\n" + out + "\n3\n");
        assertTrue(output.contains("Ключ должен"));
        assertTrue(output.contains("совпадает"));
        assertTrue(output.contains("Готово"));
        assertTrue(Files.exists(out));
    }

    private String run(String input) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        new ConsoleUI(new Scanner(input), new PrintStream(buffer, true, StandardCharsets.UTF_8))
                .run();
        return buffer.toString(StandardCharsets.UTF_8);
    }
}
