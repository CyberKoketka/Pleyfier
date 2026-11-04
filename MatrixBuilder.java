package ru.playfair;

import java.util.stream.IntStream;

/**
 * Утилитарный класс для построения ключевой матрицы шифра Плейфера
 * размером 16×16 и поиска в ней символов.
 * Оптимизация: поиск реализован через {@link IntStream} по «плоскому»
 * представлению матрицы — без вложенных циклов и лишних аллокаций.
 */
public final class MatrixBuilder {

    public static final int SIZE = 16;
    public static final int TOTAL = SIZE * SIZE;

    private MatrixBuilder() {
        throw new UnsupportedOperationException("Утилитарный класс");
    }

    /**
     * Строит матрицу 16×16 на основе ключа.
     * Ключ записывается первым (без повторов), оставшиеся байты 0..255
     * дописываются в порядке возрастания.
     */
    public static byte[][] build(byte[] key) {
        if (key == null || key.length == 0) {
            throw new IllegalArgumentException("Ключ не может быть пустым");
        }

        boolean[] used = new boolean[TOTAL];
        byte[] flat = new byte[TOTAL];
        int[] index = {0};

        // 1. Уникальные символы ключа
        IntStream.range(0, key.length)
                .map(i -> key[i] & 0xFF)
                .filter(v -> !used[v])
                .forEach(v -> {
                    used[v] = true;
                    flat[index[0]++] = (byte) v;
                });

        // 2. Оставшиеся байты 0..255
        IntStream.range(0, TOTAL)
                .filter(v -> !used[v])
                .forEach(v -> flat[index[0]++] = (byte) v);

        // 3. Разворачиваем в двумерный массив
        byte[][] matrix = new byte[SIZE][SIZE];
        IntStream.range(0, TOTAL).forEach(i ->
                matrix[i / SIZE][i % SIZE] = flat[i]);
        return matrix;
    }

    /**
     * Ищет координаты байта в матрице.
     *
     * @return массив {@code [row, col]} или {@code [-1, -1]}, если не найден
     */
    public static int[] findPosition(byte[][] matrix, byte ch) {
        int target = ch & 0xFF;
        return IntStream.range(0, TOTAL)
                .filter(i -> (matrix[i / SIZE][i % SIZE] & 0xFF) == target)
                .mapToObj(i -> new int[]{i / SIZE, i % SIZE})
                .findFirst()
                .orElse(new int[]{-1, -1});
    }

    /**
     * Безопасный поиск: бросает исключение, если символ не найден.
     * Используется в {@link PlayfairCipher} как «защита от дурака».
     */
    public static int[] requirePosition(byte[][] matrix, byte ch) {
        int[] pos = findPosition(matrix, ch);
        if (pos[0] == -1) {
            throw new IllegalStateException(
                    String.format("Символ 0x%02X отсутствует в матрице", ch & 0xFF));
        }
        return pos;
    }
}