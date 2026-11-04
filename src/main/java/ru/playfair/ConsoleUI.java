package ru.playfair;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

/**
 * Консольный интерфейс с защитой от дурака.
 * Все методы возвращают результат или повторяют запрос при ошибке.
 */
public final class ConsoleUI {

    private final Scanner scanner;

    public ConsoleUI() {
        this.scanner = new Scanner(System.in, StandardCharsets.UTF_8);
    }

    /** Главный цикл приложения. */
    public void run() {
        printBanner();
        while (true) {
            printMenu();
            String choice = readLine("Выберите опцию: ").trim();
            if (!InputValidator.isValidMenuChoice(choice)) {
                System.err.println("Ошибка: допустимо только 1, 2 или 3.");
                continue;
            }
            switch (choice) {
                case "1" -> processFile(true);
                case "2" -> processFile(false);
                case "3" -> {
                    System.out.println("Программа завершила работу.");
                    return;
                }
            }
        }
    }

    private void processFile(boolean encryptMode) {
        Path input = readExistingFile();
        if (input == null) return;

        String key = readAcceptableKey();
        if (key == null) return;

        Path output = readWritablePath(input);
        if (output == null) return;

        try {
            byte[] data = Files.readAllBytes(input);
            if (data.length == 0) {
                System.err.println("Ошибка: файл пуст.");
                return;
            }

            PlayfairCipher cipher = new PlayfairCipher(key.getBytes(StandardCharsets.UTF_8));
            byte[] result = encryptMode ? cipher.encrypt(data) : cipher.decrypt(data);

            Files.write(output, result,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            System.out.printf("%s закончено. Размер: %d байт.%n",
                    encryptMode ? "Зашифрование" : "Расшифрование",
                    result.length);
        } catch (IOException e) {
            System.err.println("Ошибка ввода-вывода: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка параметров: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.err.println("Внутренняя ошибка: " + e.getMessage());
        }
    }

    /** Читает путь к существующему файлу, повторяя запрос при ошибке. */
    private Path readExistingFile() {
        while (true) {
            String line = readLine("Введите путь к файлу (или 'отмена'): ").trim();
            if (line.equalsIgnoreCase("отмена")) return null;
            if (line.isEmpty()) {
                System.err.println("Ошибка: путь не может быть пустым.");
                continue;
            }
            Path p = Path.of(line);
            if (!InputValidator.isReadableFile(p)) {
                System.err.println("Ошибка: файла не существует или он недоступен.");
                continue;
            }
            return p;
        }
    }

    /** Читает и проверяет ключ. */
    private String readAcceptableKey() {
        while (true) {
            String key = readLine("Введите ключ (мин. 3 символа, 'отмена'): ");
            if (key.equalsIgnoreCase("отмена")) return null;
            if (!InputValidator.isAcceptableKey(key)) {
                System.err.println("Ошибка: ключ должен быть не короче 3 символов.");
                continue;
            }
            return key;
        }
    }

    /** Читает путь для сохранения и проверяет, что он не совпадает с входным. */
    private Path readWritablePath(Path input) {
        while (true) {
            String line = readLine("Введите путь для сохранения (или 'отмена'): ").trim();
            if (line.equalsIgnoreCase("отмена")) return null;
            if (line.isEmpty()) {
                System.err.println("Ошибка: путь не может быть пустым.");
                continue;
            }
            Path p = Path.of(line);
            if (!InputValidator.areDifferent(input, p)) {
                System.err.println("Ошибка: путь сохранения совпадает с исходным файлом!");
                continue;
            }
            if (!InputValidator.isWritablePath(p)) {
                System.err.println("Ошибка: папка для сохранения недоступна.");
                continue;
            }
            return p;
        }
    }

    /** Безопасное чтение строки (никогда не возвращает null). */
    private String readLine(String prompt) {
        System.out.print(prompt);
        String line = scanner.hasNextLine() ? scanner.nextLine() : "";
        return line == null ? "" : line;
    }

    private void printBanner() {
        System.out.println("=== PlayfairCipher v1.0 (Java 16, Gradle) ===");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("|_____ Меню: _____|");
        System.out.println("| (1) Зашифровать   |");
        System.out.println("| (2) Расшифровать  |");
        System.out.println("| (3) Выйти         |");
    }
}