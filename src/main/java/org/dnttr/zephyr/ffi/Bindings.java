package org.dnttr.zephyr.ffi;

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
 */

@ApiStatus.Internal
public final class Bindings {

    /**
     * Creates a new session.
     *
     * @param b1_ptr buffer to store the session identifier
     * @return 0 on success, non-zero on failure
     */
    public native int ffi_zm_open_session(long[] b1_ptr);

    /**
     * Closes an existing session.
     *
     * @param u unique identifier for the session
     * @return 0 on success, non-zero on failure
     */
    public native int ffi_zm_close_session(long u);

    /**
     * Encrypts a message in symmetric mode.
     *
     * @param u unique identifier for the session
     * @param b1 buffer containing message to process
     * @param b2 buffer containing aead
     * @param b3_ptr buffer to store the encrypted message
     * @return 0 on success, non-zero on failure
     */
    public native int ffi_ze_encrypt_symmetric(long u, byte[] b1, byte[] b2, byte[] b3_ptr);

    /**
     * Decrypts a message in symmetric mode.
     *
     * @param u unique identifier for the session
     * @param b1 buffer containing message to process
     * @param b2 buffer containing aead
     * @param b3_ptr buffer to store the decrypted message
     * @return 0 on success, non-zero on failure
     */
    public native int ffi_ze_decrypt_symmetric(long u, byte[] b1, byte[] b2, byte[] b3_ptr);

    /**
     * Encrypts a message in asymmetric mode.
     *
     * @param u unique identifier for the session
     * @param b1 buffer containing message to process
     * @param b2_ptr buffer to store the encrypted message
     * @return 0 on success, non-zero on failure
     */
    public native int ffi_ze_encrypt_asymmetric(long u, byte[] b1, byte[] b2_ptr);

    /**
     * Decrypts a message in asymmetric mode.
     *
     * @param u unique identifier for the session
     * @param b1 buffer containing message to process
     * @param b2_ptr buffer to store the decrypted message
     * @return 0 on success, non-zero on failure
     */
    public native int ffi_ze_decrypt_asymmetric(long u, byte[] b1, byte[] b2_ptr);


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
     * @return 0 on success, non-zero on failure
     */
    public native int ffi_ze_nonce(long u, int m);
}
