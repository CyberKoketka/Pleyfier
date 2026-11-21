package ru.playfair;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/** Общие файловые операции для обоих интерфейсов. */
public final class FileService {
    /** Ограничение памяти учебного приложения, 64 МиБ входного файла. */
    public static final long MAX_FILE_SIZE = 64L * 1024 * 1024;

    /** Логгер, не записывающий ключ и содержимое файлов. */
    private static final Logger LOGGER = LogManager.getLogger(FileService.class);

    /** Создаёт файловую службу. */
    public FileService() {}

    /**
     * Проверяет ввод, преобразует файл и сохраняет только в новый файл.
     *
     * @param input исходный путь
     * @param output новый выходной путь
     * @param key ключ длиной от трёх символов
     * @param encrypt true для шифрования
     * @return размер сохранённых данных
     * @throws IOException при ошибках файловой системы
     * @throws IllegalArgumentException при некорректных данных
     */
    public long process(Path input, Path output, String key, boolean encrypt) throws IOException {
        long limit = MAX_FILE_SIZE + (encrypt ? 0 : PlayfairCipher.HEADER_SIZE);
        validate(input, output, key, limit);
        byte[] data = Files.readAllBytes(input);
        if (data.length > limit) throw new IllegalArgumentException("Файл больше 64 МиБ");
        PlayfairCipher cipher = new PlayfairCipher(key.getBytes(StandardCharsets.UTF_8));
        byte[] result = encrypt ? cipher.encrypt(data) : cipher.decrypt(data);
        writeNew(output, result);
        LOGGER.info(
                "{} выполнено; сохранено {} байт",
                encrypt ? "Шифрование" : "Расшифрование",
                result.length);
        return result.length;
    }

    /**
     * Выполняет проверки до чтения и создания выходного файла.
     *
     * @param input исходный файл
     * @param output новый файл результата
     * @param key ключ пользователя
     * @param limit предел входного размера с учётом заголовка
     * @throws IOException при ошибке проверки путей
     */
    private void validate(Path input, Path output, String key, long limit) throws IOException {
        if (!InputValidator.isAcceptableKey(key))
            throw new IllegalArgumentException("Ключ должен быть не короче 3 символов");
        if (!InputValidator.isReadableFile(input))
            throw new IllegalArgumentException("Исходный файл не существует или недоступен");
        if (!InputValidator.areDifferent(input, output))
            throw new IllegalArgumentException("Путь сохранения совпадает с исходным файлом");
        if (!InputValidator.isWritablePath(output))
            throw new IllegalArgumentException(
                    "Выходной файл уже существует или каталог недоступен");
        if (Files.size(input) > limit) throw new IllegalArgumentException("Файл больше 64 МиБ");
    }

    /**
     * Создаёт файл без замены существующего; при ошибке записи удаляет свой неполный файл.
     *
     * @param output путь нового файла
     * @param result сохраняемые байты
     * @throws IOException при ошибке создания или записи
     */
    private void writeNew(Path output, byte[] result) throws IOException {
        var stream =
                Files.newOutputStream(
                        output, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        try (stream) {
            stream.write(result);
        } catch (IOException error) {
            try {
                Files.deleteIfExists(output);
            } catch (IOException cleanup) {
                error.addSuppressed(cleanup);
            }
            throw error;
        }
    }
}
