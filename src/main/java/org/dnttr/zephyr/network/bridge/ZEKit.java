package org.dnttr.zephyr.network.bridge;

import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;

/**
 * Direct use of this class is discouraged and should be avoided.
 * <br><br>
 * Tampering with these bindings may lead compromise the integrity of the vm and therefore undefined behavior.
 * This class is not intended for public use and is subject to change without notice.
 *
 * @implNote All parameters that are marked with _ptr are return buffers. While the native code will fill them with data, it is the caller's responsibility to ensure that these buffers are of sufficient size.
 *
 * @author dnttr
 * @since 1.0.0
 * @version 1.0.1-ZE
 */

@ApiStatus.Internal
public final class ZEKit {

    public enum Type {
        SYMMETRIC(0),
        ASYMMETRIC(1),
        NONE(2);

        @Getter
        private final int value;

        Type(int value) {
            this.value = value;
        }
    }

    /**
     * Creates a new session.
     */
    public static native long ffi_zm_open_session();

    /**
     * Closes an existing session.
     *
     * @param u unique identifier for the session
     */
    public static native int ffi_zm_close_session(long u);

    /**
     * Encrypts a message in symmetric mode.
     *
     * @param u unique identifier for the session
     * @param b1 buffer containing message to process
     * @param b2 buffer containing aead
     */
    public static native byte[] ffi_ze_encrypt_symmetric(long u, byte[] b1, byte[] b2);

    /**
     * Decrypts a message in symmetric mode.
     *
     * @param u unique identifier for the session
     * @param b1 buffer containing message to process
     * @param b2 buffer containing aead
     */
    public static native byte[] ffi_ze_decrypt_symmetric(long u, byte[] b1, byte[] b2);

    /**
     * Encrypts a message in asymmetric mode.
     *
     * @param u unique identifier for the session
     * @param b1 buffer containing message to process
     */
    public static native byte[] ffi_ze_encrypt_asymmetric(long u, byte[] b1);

    /**
     * Decrypts a message in asymmetric mode.
     *
     * @param u unique identifier for the session
     * @param b1 buffer containing message to process
     */
    public static native byte[] ffi_ze_decrypt_asymmetric(long u, byte[] b1);


    /**
     * Generates a nonce for the session.
     * It isn't necessary to call it every time you want to encrypt a message, however from security standpoint it is recommended to call it before each encryption operation.
     * The nonce itself is stored on the native side.
     * <br><br>
     * Modes of operation:
     * <br>
     * 0 - generate nonce for asymmetric encryption
     * <br>
     * 1 - generate nonce for symmetric encryption
     * <br>
     * @param u unique identifier for the session
     * @param m mode of operation
     */
    public static native void ffi_ze_nonce(long u, int m);

    public static native void ffi_ze_key(long u, int m);

    public static native void ffi_ze_set_asymmetric_key(long u, int m, byte[] k);

    public static native void ffi_ze_set_symmetric_key(long u, byte[] k);

    public static native void ffi_ze_set_nonce(long u, int m, byte[] n);

    public static native byte[] ffi_ze_get_asymmetric_key(long u, int m);

    public static native byte[] ffi_ze_get_symmetric_key(long u);

    public static native byte[] ffi_ze_get_nonce(long u, int m);

    public static native byte[] ffi_ze_build_hash(long u, byte[] b1);

    public static native boolean ffi_ze_compare_hash(long u, byte[] b1, byte[] b2);

    public static native void ffi_ze_build_derivable_key(long u);

    public static native void ffi_ze_derive_hash_key(long u);

    public static native void ffi_ze_derive_secret_key(long u, int m, byte[] k);

    public static native void ffi_ze_close();
}
