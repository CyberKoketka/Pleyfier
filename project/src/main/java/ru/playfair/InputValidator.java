package ru.playfair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.stream.Stream;

/** Общие проверки для консоли и графического интерфейса. */
public final class InputValidator {
    /** Запрещает создание экземпляров. */
    private InputValidator() {}

    /**
     * Проверяет номер пункта меню с помощью Stream API.
     *
     * @param choice введённая строка
     * @return true для 1, 2 или 3
     */
    public static boolean isValidMenuChoice(String choice) {
        return Stream.of("1", "2", "3").anyMatch(s -> s.equals(choice));
    }

    /**
     * Проверяет ключ интерфейса приложения.
     *
     * @param key ключевая фраза
     * @return true, если после удаления крайних пробелов есть хотя бы 3 символа
     */
    public static boolean isAcceptableKey(String key) {
        return key != null && key.trim().length() >= 3;
    }

    /**
     * Проверяет возможность чтения обычного файла.
     *
     * @param path входной путь
     * @return true для существующего читаемого файла
     */
    public static boolean isReadableFile(Path path) {
        return path != null && Files.isRegularFile(path) && Files.isReadable(path);
    }

    /**
     * Проверяет, что выходной файл ещё не существует, а каталог доступен. Символьные ссылки, в том
     * числе повреждённые, не перезаписываются.
     *
     * @param path выходной путь
     * @return true для нового файла в доступном каталоге
     */
    public static boolean isWritablePath(Path path) {
        if (path == null || Files.exists(path, LinkOption.NOFOLLOW_LINKS)) return false;
        Path parent = path.toAbsolutePath().normalize().getParent();
        return parent != null && Files.isDirectory(parent) && Files.isWritable(parent);
    }

    /**
     * Сравнивает нормализованные пути и, при наличии, реальные файлы.
     *
     * @param input исходный путь
     * @param output выходной путь
     * @return true, если пути различаются
     * @throws IOException если реальный путь не может быть проверен
     */
    public static boolean areDifferent(Path input, Path output) throws IOException {
        if (input == null || output == null) return false;
        if (input.toAbsolutePath().normalize().equals(output.toAbsolutePath().normalize())) {
            return false;
        }
        return !Files.exists(output) || !Files.isSameFile(input, output);
    }
}
