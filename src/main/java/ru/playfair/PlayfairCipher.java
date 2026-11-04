package ru.playfair;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

/**
 * Реализация симметричного алгоритма «шифр Плейфера» для 256 символов.
 */
public final class PlayfairCipher {

    private static final int N = MatrixBuilder.SIZE;

    private final byte[][] matrix;

    public PlayfairCipher(byte[] key) {
        Objects.requireNonNull(key, "Ключ не может быть null");
        if (key.length == 0) {
            throw new IllegalArgumentException("Ключ не может быть пустым");
        }
        this.matrix = MatrixBuilder.build(key);
    }

    public static byte[] prepare(byte[] data) {
        Objects.requireNonNull(data, "Данные не могут быть null");
        if ((data.length & 1) == 0) {
            return data;
        }
        byte[] result = new byte[data.length + 1];
        System.arraycopy(data, 0, result, 0, data.length);
        result[data.length] = 0;
        return result;
    }

    public byte[] encrypt(byte[] input) {
        Objects.requireNonNull(input, "Входные данные не могут быть null");
        byte[] data = prepare(input);
        ByteArrayOutputStream out = new ByteArrayOutputStream(data.length);

        for (int i = 0; i < data.length; i += 2) {
            int[] pa = MatrixBuilder.requirePosition(matrix, data[i]);
            int[] pb = MatrixBuilder.requirePosition(matrix, data[i + 1]);

            if (pa[0] == pb[0]) {
                out.write(matrix[pa[0]][(pa[1] + 1) % N]);
                out.write(matrix[pb[0]][(pb[1] + 1) % N]);
            } else if (pa[1] == pb[1]) {
                out.write(matrix[(pa[0] + 1) % N][pa[1]]);
                out.write(matrix[(pb[0] + 1) % N][pb[1]]);
            } else {
                out.write(matrix[pa[0]][pb[1]]);
                out.write(matrix[pb[0]][pa[1]]);
            }
        }
        return out.toByteArray();
    }

    public byte[] decrypt(byte[] input) {
        Objects.requireNonNull(input, "Шифротекст не может быть null");
        if ((input.length & 1) != 0) {
            throw new IllegalArgumentException(
                    "Длина шифротекста должна быть чётной, получено: " + input.length);
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream(input.length);

        for (int i = 0; i < input.length; i += 2) {
            int[] pa = MatrixBuilder.requirePosition(matrix, input[i]);
            int[] pb = MatrixBuilder.requirePosition(matrix, input[i + 1]);

            if (pa[0] == pb[0]) {
                out.write(matrix[pa[0]][(pa[1] - 1 + N) % N]);
                out.write(matrix[pb[0]][(pb[1] - 1 + N) % N]);
            } else if (pa[1] == pb[1]) {
                out.write(matrix[(pa[0] - 1 + N) % N][pa[1]]);
                out.write(matrix[(pb[0] - 1 + N) % N][pb[1]]);
            } else {
                out.write(matrix[pa[0]][pb[1]]);
                out.write(matrix[pb[0]][pa[1]]);
            }
        }
        return out.toByteArray();
    }
}