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

    public static native void ffi_ze_close();
    public static native long ffi_zm_open_session();
    public static native int ffi_zm_close_session(long uuid);
    public static native byte[] ffi_ze_encrypt_symmetric(long uuid, byte[] messageBuffer, byte[] aeadBuffer);
    public static native byte[] ffi_ze_decrypt_symmetric(long uuid, byte[] messageBuffer, byte[] aeadBuffer);
    public static native byte[] ffi_ze_encrypt_asymmetric(long uuid, byte[] messageBuffer);
    public static native byte[] ffi_ze_decrypt_asymmetric(long uuid, byte[] messageBuffer);
    public static native void ffi_ze_nonce(long uuid, int mode);
    public static native void ffi_ze_key(long uuid, int mode);
    public static native void ffi_ze_set_symmetric_key(long uuid, byte[] keyBuffer);
    public static native void ffi_ze_set_asymmetric_key(long uuid, int mode, byte[] keyBuffer);
    public static native void ffi_ze_set_nonce(long uuid, int mode, byte[] nonceBuffer);
    public static native byte[] ffi_ze_get_symmetric_key(long uuid);
    public static native byte[] ffi_ze_get_asymmetric_key(long uuid, int mode);
    public static native byte[] ffi_ze_get_nonce(long uuid, int mode);
    public static native byte[] ffi_ze_build_hash_sh0(long uuid, byte[] messageBuffer);
    public static native boolean ffi_ze_compare_hash_sh0(long uuid, byte[] hashBuffer, byte[] messageBuffer);
    public static native void ffi_ze_build_base_key_sh0(long uuid);
    public static native void ffi_ze_derive_keys_sh0(long uuid, int mode);
    public static native void ffi_ze_derive_final_key_sh0(long uuid, int mode);
    public static native byte[] ffi_ze_get_rv_public_key_sh0(long uuid);
    public static native void ffi_ze_set_rv_public_key_sh0(long uuid, byte[] keyBuffer);
    public static native byte[] ffi_ze_get_base_public_key_sh0(long uuid);
    public static native void ffi_ze_set_asymmetric_received_key(long uuid, byte[] keyBuffer);
    public static native byte[] ffi_ze_get_exchange_message(long uuid);
    public static native void ffi_ze_set_exchange_message(long uuid, byte[] messageBuffer);
}
