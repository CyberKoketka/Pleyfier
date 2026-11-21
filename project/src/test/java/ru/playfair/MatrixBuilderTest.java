package ru.playfair;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

/** Проверки матрицы и поиска. */
class MatrixBuilderTest {
    @Test
    void permutationContainsAllBytes() {
        byte[][] matrix = MatrixBuilder.build(new byte[] {3, 3, 7});
        Set<Byte> unique = new HashSet<>();
        for (byte[] row : matrix) for (byte value : row) unique.add(value);
        assertEquals(256, unique.size());
        assertEquals(3, matrix[0][0]);
        assertEquals(7, matrix[0][1]);
    }

    @Test
    void findsUnsignedByte() {
        byte[][] matrix = MatrixBuilder.build(new byte[] {(byte) 255});
        assertArrayEquals(new int[] {0, 0}, MatrixBuilder.requirePosition(matrix, (byte) 255));
    }

    @Test
    void absentByteRejected() {
        assertThrows(
                IllegalStateException.class,
                () -> MatrixBuilder.requirePosition(new byte[16][16], (byte) 1));
    }
}
