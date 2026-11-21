package ru.playfair;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

/** Проверки обратимости, структуры контейнера и повреждений. */
class PlayfairCipherTest {
    private final byte[] key = "секретный ключ".getBytes(StandardCharsets.UTF_8);

    @Test
    void emptyFile() {
        roundTrip(new byte[0]);
    }

    @Test
    void oddLength() {
        roundTrip(new byte[] {1, 2, 3});
    }

    @Test
    void evenLength() {
        roundTrip(new byte[] {1, 2, 3, 4});
    }

    @Test
    void trailingZeros() {
        roundTrip(new byte[] {7, 0, 0});
    }

    @Test
    void repeatedBytes() {
        roundTrip(new byte[101]);
    }

    @Test
    void unicodeText() {
        roundTrip("Привет, мир! 日本語".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void allBytes() {
        byte[] bytes = new byte[256];
        for (int i = 0; i < bytes.length; i++) bytes[i] = (byte) i;
        roundTrip(bytes);
    }

    @Test
    void everyBigramIsReversible() {
        byte[] pairs = new byte[65536 * 2];
        for (int i = 0; i < 65536; i++) {
            pairs[2 * i] = (byte) (i >>> 8);
            pairs[2 * i + 1] = (byte) i;
        }
        roundTrip(pairs);
    }

    @Test
    void manyLengths() {
        Random random = new Random(28);
        for (int n = 0; n < 257; n++) {
            byte[] bytes = new byte[n];
            random.nextBytes(bytes);
            roundTrip(bytes);
        }
    }

    @Test
    void inputNotModified() {
        byte[] bytes = {1, 2, 3};
        byte[] copy = bytes.clone();
        new PlayfairCipher(key).encrypt(bytes);
        assertArrayEquals(copy, bytes);
    }

    @Test
    void preparePreservesZeros() {
        assertArrayEquals(new byte[] {1, 0, 0, 0}, PlayfairCipher.prepare(new byte[] {1, 0, 0}));
    }

    @Test
    void headerContainsLength() {
        byte[] bytes = new PlayfairCipher(key).encrypt(new byte[] {1, 2, 3});
        assertEquals(24, bytes.length);
        assertEquals(3, ByteBuffer.wrap(bytes).getLong(4));
    }

    @Test
    void wrongKey() {
        byte[] enc =
                new PlayfairCipher(key)
                        .encrypt("Проверка неверного ключа".getBytes(StandardCharsets.UTF_8));
        assertThrows(
                IllegalArgumentException.class,
                () -> new PlayfairCipher(new byte[] {1, 3, 8}).decrypt(enc));
    }

    @Test
    void corruptPayload() {
        PlayfairCipher cipher = new PlayfairCipher(key);
        byte[] enc = cipher.encrypt(new byte[] {1, 2, 3, 4});
        enc[20] ^= 1;
        assertThrows(IllegalArgumentException.class, () -> cipher.decrypt(enc));
    }

    @Test
    void invalidSignature() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PlayfairCipher(key).decrypt(new byte[16]));
    }

    @Test
    void truncatedContainer() {
        PlayfairCipher cipher = new PlayfairCipher(key);
        byte[] enc = cipher.encrypt(new byte[] {1, 2, 3});
        assertThrows(
                IllegalArgumentException.class,
                () -> cipher.decrypt(Arrays.copyOf(enc, enc.length - 1)));
    }

    @Test
    void negativeLength() {
        PlayfairCipher cipher = new PlayfairCipher(key);
        byte[] enc = cipher.encrypt(new byte[] {1, 2});
        ByteBuffer.wrap(enc).putLong(4, -1);
        assertThrows(IllegalArgumentException.class, () -> cipher.decrypt(enc));
    }

    @Test
    void emptyKeyRejected() {
        assertThrows(IllegalArgumentException.class, () -> new PlayfairCipher(new byte[0]));
        assertThrows(IllegalArgumentException.class, () -> new PlayfairCipher(null));
    }

    @Test
    void nullInputRejected() {
        PlayfairCipher cipher = new PlayfairCipher(key);
        assertThrows(NullPointerException.class, () -> cipher.encrypt(null));
        assertThrows(NullPointerException.class, () -> cipher.decrypt(null));
    }

    private void roundTrip(byte[] bytes) {
        PlayfairCipher cipher = new PlayfairCipher(key);
        assertArrayEquals(bytes, cipher.decrypt(cipher.encrypt(bytes)));
    }
}
