package ru.playfair;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Класс валидации пользовательского ввода.
 * Содержит все проверки «защиты от дурака».
 */
public final class InputValidator {

    private InputValidator() {}

    /**
     * Проверяет корректность выбора пункта меню.
     * Допустимые значения: 1, 2, 3.
     */
    public static boolean isValidMenuChoice(String choice) {
        return Stream.of("1", "2", "3").anyMatch(s -> s.equals(choice));
    }

    /**
     * Проверяет существование и читаемость файла.
     */
    public static boolean isReadableFile(Path path) {
        return path != null
                && Files.exists(path)
                && Files.isRegularFile(path)
                && Files.isReadable(path);
    }

    /**
     * Проверяет, что путь пригоден для записи (родительская папка существует
     * и доступна для записи).
     */
    public static boolean isWritablePath(Path path) {
        if (path == null) return false;
        Path parent = path.toAbsolutePath().getParent();
        return parent != null && Files.exists(parent) && Files.isWritable(parent);
    }

    /**
     * Проверяет, что ключ не слишком короткий.
     */
    public static boolean isAcceptableKey(String key) {
        return key != null && key.trim().length() >= 3;
    }

    /**
     * Проверяет, что входной и выходной пути не совпадают
     * (защита от затирания исходного файла).
     */
    public static boolean areDifferent(Path input, Path output) {
        if (input == null || output == null) return false;
        try {
            return !Files.isSameFile(input, output);
        } catch (Exception e) {
            return true;
        }
    }
}