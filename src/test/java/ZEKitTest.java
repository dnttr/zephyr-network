import org.dnttr.zephyr.bridge.internal.ZEKit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "EXTERNAL_LIBS", matches = ".+")
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

        sessionId = ZEKit.ffi_ze_create_session();
        assertTrue(sessionId != 0, "Session should be created");
    }

    @Test
    void testSymmetricEncryptionDecryption() {
        ZEKit.ffi_ze_generate_nonce(sessionId, 0);
        ZEKit.ffi_ze_generate_keys(sessionId, 0);

        byte[] message = "hello".getBytes();
        byte[] aead = "aead".getBytes();

        byte[] encrypted = ZEKit.ffi_ze_encrypt_data(sessionId, message, aead);
        assertNotNull(encrypted);

        byte[] decrypted = ZEKit.ffi_ze_decrypt_data(sessionId, encrypted, aead);

        assertNotNull(decrypted);
        assertArrayEquals(message, decrypted);
    }

    @Test
    void testAsymmetricEncryptionDecryption() {
        ZEKit.ffi_ze_generate_nonce(sessionId, 1);
        ZEKit.ffi_ze_generate_keys(sessionId, 1);

        byte[] message = "world".getBytes();

        byte[] encrypted = ZEKit.ffi_ze_encrypt_with_public_key(sessionId, message);
        byte[] decrypted = ZEKit.ffi_ze_decrypt_with_private_key(sessionId, encrypted);

        assertNotNull(decrypted);
        assertArrayEquals(message, decrypted);
    }

    @AfterAll
    static void tearDown() {
        int closeResult = ZEKit.ffi_ze_delete_session(sessionId);
        assertEquals(0, closeResult);
        ZEKit.ffi_ze_close_library();
    }
}