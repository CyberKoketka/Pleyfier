package ru.playfair;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;
import java.util.zip.CRC32;

/**
 * Учебная байтовая модификация Плейфера 16 на 16. Контейнер PFC2 хранит длину и CRC32 исходных
 * данных. CRC32 обнаруживает случайное повреждение, но не является криптографической
 * аутентификацией.
 */
public final class PlayfairCipher {
    /** Размер стороны матрицы. */
    private static final int N = MatrixBuilder.SIZE;

    /** Сигнатура формата, не совместимого с прежним сырым шифротекстом. */
    private static final int MAGIC = 0x50464332;

    /** Размер заголовка: сигнатура, длина long и две CRC32 int. */
    public static final int HEADER_SIZE = 20;

    /** Матрица, построенная один раз для выбранного ключа. */
    private final byte[][] matrix;

    /**
     * Создаёт шифр с указанным ключом.
     *
     * @param key непустой ключ
     * @throws IllegalArgumentException если ключ пустой или равен null
     */
    public PlayfairCipher(byte[] key) {
        matrix = MatrixBuilder.build(key);
    }

    /**
     * Дополняет нечётный массив одним нулём, сохраняя исходный массив. Одинаковые байты не
     * разделяются: это обратимая байтовая модификация.
     *
     * @param data исходные байты
     * @return массив чётной длины
     */
    public static byte[] prepare(byte[] data) {
        Objects.requireNonNull(data, "Данные не могут быть null");
        return Arrays.copyOf(data, data.length + (data.length & 1));
    }

    /**
     * Шифрует данные и формирует контейнер с длиной и контрольной суммой.
     *
     * @param input исходные байты, включая пустой массив
     * @return контейнер PFC2
     */
    public byte[] encrypt(byte[] input) {
        Objects.requireNonNull(input, "Входные данные не могут быть null");
        byte[] encrypted = transform(prepare(input), 1);
        return ByteBuffer.allocate(HEADER_SIZE + encrypted.length)
                .putInt(MAGIC)
                .putLong(input.length)
                .putInt(matrixChecksum())
                .putInt(checksum(input))
                .put(encrypted)
                .array();
    }

    /**
     * Восстанавливает ровно исходное число байтов, включая конечные нули.
     *
     * @param input контейнер PFC2
     * @return исходные данные
     * @throws IllegalArgumentException при неверном формате, ключе или повреждении
     */
    public byte[] decrypt(byte[] input) {
        Objects.requireNonNull(input, "Шифротекст не может быть null");
        if (input.length < HEADER_SIZE) {
            throw new IllegalArgumentException("Файл слишком короткий: требуется контейнер PFC2");
        }
        ByteBuffer header = ByteBuffer.wrap(input);
        if (header.getInt() != MAGIC) {
            throw new IllegalArgumentException(
                    "Неверный формат PFC2; старые файлы не поддерживаются");
        }
        long length = header.getLong();
        if (header.getInt() != matrixChecksum()) {
            throw new IllegalArgumentException("Неверный ключ (матрица не совпадает)");
        }
        int expectedCrc = header.getInt();
        int payloadSize = input.length - HEADER_SIZE;
        if (length < 0
                || length > Integer.MAX_VALUE
                || (payloadSize & 1) != 0
                || length + (length & 1) != payloadSize) {
            throw new IllegalArgumentException("Повреждён заголовок или длина файла");
        }
        byte[] restored = transform(Arrays.copyOfRange(input, HEADER_SIZE, input.length), -1);
        if ((length & 1) != 0 && restored[restored.length - 1] != 0) {
            throw new IllegalArgumentException("Неверный ключ или повреждённый файл");
        }
        byte[] result = Arrays.copyOf(restored, (int) length);
        if (checksum(result) != expectedCrc) {
            throw new IllegalArgumentException("Неверный ключ или повреждённый файл (CRC32)");
        }
        return result;
    }

    /**
     * Применяет общее прямое либо обратное преобразование биграмм. Для одинаковых байтов действует
     * правило одной строки.
     *
     * @param data чётное число байтов
     * @param direction 1 для шифрования, -1 для расшифрования
     * @return преобразованные байты
     */
    private byte[] transform(byte[] data, int direction) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i += 2) {
            int[] a = MatrixBuilder.requirePosition(matrix, data[i]);
            int[] b = MatrixBuilder.requirePosition(matrix, data[i + 1]);
            if (a[0] == b[0]) {
                result[i] = matrix[a[0]][Math.floorMod(a[1] + direction, N)];
                result[i + 1] = matrix[b[0]][Math.floorMod(b[1] + direction, N)];
            } else if (a[1] == b[1]) {
                result[i] = matrix[Math.floorMod(a[0] + direction, N)][a[1]];
                result[i + 1] = matrix[Math.floorMod(b[0] + direction, N)][b[1]];
            } else {
                result[i] = matrix[a[0]][b[1]];
                result[i + 1] = matrix[b[0]][a[1]];
            }
        }
        return result;
    }

    /**
     * Вычисляет незащищённый идентификатор матрицы для выявления ошибки ключа.
     *
     * @return CRC32 последовательности всех ячеек матрицы
     */
    private int matrixChecksum() {
        byte[] flat = new byte[N * N];
        for (int row = 0; row < N; row++) {
            System.arraycopy(matrix[row], 0, flat, row * N, N);
        }
        return checksum(flat);
    }

    /**
     * Вычисляет контрольную сумму данных.
     *
     * @param data проверяемые байты
     * @return младшие 32 бита CRC32
     */
    private static int checksum(byte[] data) {
        CRC32 crc = new CRC32();
        crc.update(data);
        return (int) crc.getValue();
    }
}
