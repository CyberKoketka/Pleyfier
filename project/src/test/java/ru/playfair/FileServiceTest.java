package ru.playfair;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.*;

/** Проверки файлов, общие для консоли и JavaFX. */
class FileServiceTest {
    @TempDir Path directory;
    private final FileService service = new FileService();

    @Test
    void fileRoundTrip() throws Exception {
        Path src = directory.resolve("src.bin"),
                enc = directory.resolve("enc.pfc"),
                dst = directory.resolve("out.bin");
        byte[] data = {0, 7, (byte) 255, 0, 0};
        Files.write(src, data);
        service.process(src, enc, "secret", true);
        service.process(enc, dst, "secret", false);
        assertArrayEquals(data, Files.readAllBytes(dst));
    }

    @Test
    void emptyFileRoundTrip() throws Exception {
        Path src = Files.createFile(directory.resolve("empty")),
                enc = directory.resolve("enc"),
                dst = directory.resolve("dst");
        service.process(src, enc, "secret", true);
        service.process(enc, dst, "secret", false);
        assertEquals(0, Files.size(dst));
        // A maximum-size original produces a container 20 bytes larger.
        // A sparse, invalid container exercises size validation without encrypting 64 MiB.
        Path large = directory.resolve("large.pfc");
        try (var channel =
                Files.newByteChannel(
                        large, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            channel.position(FileService.MAX_FILE_SIZE + PlayfairCipher.HEADER_SIZE - 1);
            channel.write(java.nio.ByteBuffer.wrap(new byte[] {0}));
        }
        var formatError =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.process(
                                        large, directory.resolve("large.out"), "secret", false));
        assertTrue(formatError.getMessage().contains("Неверный формат"));
        var sizeError =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.process(
                                        large, directory.resolve("too-big.out"), "secret", true));
        assertTrue(sizeError.getMessage().contains("64 МиБ"));
    }

    @Test
    void preventsOverwrite() throws Exception {
        Path src = Files.write(directory.resolve("source"), new byte[] {1});
        Path dst = Files.write(directory.resolve("existing"), new byte[] {42});
        assertThrows(
                IllegalArgumentException.class, () -> service.process(src, dst, "secret", true));
        assertArrayEquals(new byte[] {42}, Files.readAllBytes(dst));
    }

    @Test
    void preventsSameFile() throws Exception {
        Path src = Files.write(directory.resolve("source"), new byte[] {1});
        assertThrows(
                IllegalArgumentException.class, () -> service.process(src, src, "secret", true));
    }

    @Test
    void preventsNormalizedSamePath() throws Exception {
        Path src = Files.write(directory.resolve("source"), new byte[] {1});
        assertFalse(InputValidator.areDifferent(src, directory.resolve("./source")));
    }

    @Test
    void rejectsBadKey() throws Exception {
        Path src = Files.write(directory.resolve("source"), new byte[] {1});
        assertThrows(
                IllegalArgumentException.class,
                () -> service.process(src, directory.resolve("out"), "a", true));
    }

    @Test
    void missingFile() {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.process(
                                directory.resolve("missing"),
                                directory.resolve("out"),
                                "secret",
                                true));
    }

    @Test
    void missingOutputDirectory() throws Exception {
        Path src = Files.write(directory.resolve("source"), new byte[] {1});
        assertThrows(
                IllegalArgumentException.class,
                () -> service.process(src, directory.resolve("missing/out"), "secret", true));
    }

    @Test
    void wrongKeyDoesNotCreateOutput() throws Exception {
        Path src = Files.write(directory.resolve("source"), new byte[] {1, 2, 3});
        Path enc = directory.resolve("enc"), out = directory.resolve("out");
        service.process(src, enc, "secret", true);
        assertThrows(
                IllegalArgumentException.class, () -> service.process(enc, out, "wrong", false));
        assertFalse(Files.exists(out));
    }

    @Test
    void validatorBoundaries() {
        assertFalse(InputValidator.isAcceptableKey(null));
        assertFalse(InputValidator.isAcceptableKey("  "));
        assertFalse(InputValidator.isAcceptableKey("ab"));
        assertTrue(InputValidator.isAcceptableKey("abc"));
        assertFalse(InputValidator.isValidMenuChoice(null));
        assertTrue(InputValidator.isValidMenuChoice("1"));
        assertFalse(InputValidator.isValidMenuChoice("4"));
        assertFalse(InputValidator.isReadableFile(directory));
        assertFalse(InputValidator.isWritablePath(null));
    }
}
