import org.dnttr.zephyr.network.bridge.ZEKit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class ZEKitTest {

    private static long sessionId;

    @BeforeAll
    static void setUp() {
        String path = System.getenv("EXTERNAL_LIBS");

        if (path == null || path.isEmpty()) {
            System.err.println("Environment variable EXTERNAL_LIBS is not set or empty.");
            System.exit(1337);
        }

        File file = new File(path + "libze.dylib");
        System.load(file.getPath());

        sessionId = ZEKit.ffi_zm_open_session();
        assertTrue(sessionId != 0, "Session should be created");
    }

    @Test
    void testSymmetricEncryptionDecryption() {
        ZEKit.ffi_ze_nonce(sessionId, ZEKit.Type.SYMMETRIC.getValue());
        ZEKit.ffi_ze_key(sessionId, ZEKit.Type.SYMMETRIC.getValue());

        byte[] message = "hello".getBytes();
        byte[] aead = "aead".getBytes();

        byte[] encrypted = ZEKit.ffi_ze_encrypt_symmetric(sessionId, message, aead);
        assertNotNull(encrypted);

        byte[] decrypted = ZEKit.ffi_ze_decrypt_symmetric(sessionId, encrypted, aead);

        assertNotNull(decrypted);
        assertArrayEquals(message, decrypted);
    }

    @Test
    void testAsymmetricEncryptionDecryption() {
        ZEKit.ffi_ze_nonce(sessionId, ZEKit.Type.ASYMMETRIC.getValue());
        ZEKit.ffi_ze_key(sessionId, ZEKit.Type.ASYMMETRIC.getValue());

        byte[] message = "world".getBytes();

        byte[] encrypted = ZEKit.ffi_ze_encrypt_asymmetric(sessionId, message);
        byte[] decrypted = ZEKit.ffi_ze_decrypt_asymmetric(sessionId, encrypted);

        assertNotNull(decrypted);
        assertArrayEquals(message, decrypted);
    }

    @AfterAll
    static void tearDown() {
        int closeResult = ZEKit.ffi_zm_close_session(sessionId);
        assertEquals(0, closeResult);
        ZEKit.ffi_ze_close();
    }
}