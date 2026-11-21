package ru.playfair;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Scanner;

/** Консольный интерфейс с повторным запросом ввода и обработкой конца потока. */
public final class ConsoleUI {
    /** Логгер сообщений об ошибках. */
    private static final Logger LOGGER = LogManager.getLogger(ConsoleUI.class);

    /** Источник пользовательского ввода. */
    private final Scanner scanner;

    /** Поток приглашений и результатов. */
    private final PrintStream output;

    /** Общая файловая служба. */
    private final FileService service = new FileService();

    /** Создаёт интерфейс со стандартными потоками. */
    public ConsoleUI() {
        this(new Scanner(System.in, StandardCharsets.UTF_8), System.out);
    }

    /**
     * Создаёт интерфейс с заменяемыми потоками для тестирования.
     *
     * @param scanner источник строк
     * @param output поток вывода
     */
    public ConsoleUI(Scanner scanner, PrintStream output) {
        this.scanner = scanner;
        this.output = output;
    }

    /** Выполняет меню до команды выхода или конца потока. */
    public void run() {
        output.println("=== PlayfairCipher 2.0 (Java 17+, Gradle) ===");
        while (true) {
            printMenu();
            String choice = readLine("Выберите опцию: ");
            if (choice == null || choice.trim().equals("3")) {
                output.println("Программа завершила работу.");
                return;
            }
            if (!InputValidator.isValidMenuChoice(choice.trim())) {
                reportError("Допустимо только 1, 2 или 3");
                continue;
            }
            processFile(choice.trim().equals("1"));
        }
    }

    /**
     * Запрашивает параметры и выполняет операцию с обработкой исключений.
     *
     * @param encrypt true для шифрования
     */
    private void processFile(boolean encrypt) {
        try {
            Path input = readPath("Введите путь к файлу (или 'отмена'): ", true, null);
            if (input == null) return;
            String key = readKey();
            if (key == null) return;
            Path target = readPath("Введите новый путь результата (или 'отмена'): ", false, input);
            if (target == null) return;
            long size = service.process(input, target, key, encrypt);
            output.println("Готово. Сохранено " + size + " байт: " + target);
        } catch (IOException | RuntimeException error) {
            reportError(error.getMessage());
        }
    }

    /**
     * Читает существующий входной либо новый выходной путь, повторяя запрос при ошибке.
     *
     * @param prompt приглашение
     * @param inputMode true для исходного файла
     * @param source путь исходника при выборе выхода
     * @return путь либо null при отмене или EOF
     */
    private Path readPath(String prompt, boolean inputMode, Path source) {
        while (true) {
            String text = readLine(prompt);
            if (text == null || text.trim().equalsIgnoreCase("отмена")) return null;
            try {
                if (text.isBlank()) throw new IllegalArgumentException("Путь не может быть пустым");
                Path path = Path.of(text.trim());
                if (inputMode && !InputValidator.isReadableFile(path)) {
                    throw new IllegalArgumentException("Файл не существует или недоступен");
                }
                if (!inputMode && !InputValidator.areDifferent(source, path)) {
                    throw new IllegalArgumentException("Путь совпадает с исходным файлом");
                }
                if (!inputMode && !InputValidator.isWritablePath(path)) {
                    throw new IllegalArgumentException(
                            "Выходной файл существует или каталог недоступен");
                }
                return path;
            } catch (IOException | RuntimeException error) {
                reportError(error.getMessage());
            }
        }
    }

    /**
     * Запрашивает ключ длиной от трёх символов.
     *
     * @return ключ без изменения его байтов либо null при отмене или EOF
     */
    private String readKey() {
        while (true) {
            String key = readLine("Введите ключ (мин. 3 символа, 'отмена'): ");
            if (key == null || key.equalsIgnoreCase("отмена")) return null;
            if (InputValidator.isAcceptableKey(key)) return key;
            reportError("Ключ должен быть не короче 3 символов");
        }
    }

    /**
     * Выводит приглашение и читает строку без зацикливания при EOF.
     *
     * @param prompt приглашение
     * @return строка либо null, если ввод закончился
     */
    private String readLine(String prompt) {
        output.print(prompt);
        return scanner.hasNextLine() ? scanner.nextLine() : null;
    }

    /**
     * Показывает ошибку и записывает её в журнал.
     *
     * @param message описание ошибки
     */
    private void reportError(String message) {
        output.println("Ошибка: " + message);
        LOGGER.warn("Ошибка ввода или обработки: {}", message);
    }

    /** Выводит доступные команды меню. */
    private void printMenu() {
        output.println("1 — Зашифровать\n2 — Расшифровать\n3 — Выйти");
    }
}
