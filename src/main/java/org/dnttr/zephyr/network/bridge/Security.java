package org.dnttr.zephyr.network.bridge;

import lombok.Getter;
import org.dnttr.zephyr.network.bridge.internal.ZEKit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * @author dnttr
 */

public final class Security {

    public enum EncryptionMode {
        NONE(-1),
        SYMMETRIC(0),
        ASYMMETRIC(1);

        @Getter
        private final int value;

        EncryptionMode(int value) {
            this.value = value;
        }
    }

    public enum KeyType
    {
        PUBLIC(0),
        PRIVATE(1);

        @Getter
        private final int value;

        KeyType(int value) {
            this.value = value;
        }
    }

    public enum SideType {
        SERVER(0),
        CLIENT(1);

        @Getter
        private final int value;

        SideType(int value) {
            this.value = value;
        }
    }

    public static long createSession() {
        long uuid = ZEKit.ffi_ze_create_session();

        if (uuid <= 0) {
            throw new IllegalStateException("Create session failed, invalid uuid has been returned");
        }

        return uuid;
    }

    public static boolean deleteSession(long uuid) {
        if (uuid <= 0) {
            throw new IllegalArgumentException("Delete session failed due to illegal uuid provided");
        }

        int code = ZEKit.ffi_ze_delete_session(uuid);
        return code == 0;
    }

    public static void closeLibrary() {
        ZEKit.ffi_ze_close_library();
    }

    public static Optional<byte[]> encryptData(long uuid, byte @NotNull [] message, byte @Nullable [] aead) {
        Objects.requireNonNull(message);

        if (uuid <= 0) {
            throw new IllegalArgumentException("Invalid uuid provided");
        }

        return Optional.ofNullable(ZEKit.ffi_ze_encrypt_data(uuid, message, aead)).map(byte[]::clone);
    }

    public static Optional<byte[]> decryptData(long uuid, byte @NotNull [] message, byte @Nullable [] aead) {
        Objects.requireNonNull(message);

        if (uuid <= 0) {
            throw new IllegalArgumentException("Invalid uuid provided");
        }

        return Optional.ofNullable(ZEKit.ffi_ze_decrypt_data(uuid, message, aead)).map(byte[]::clone);
    }

    public static Optional<byte[]> encryptDataWithPublicKey(long uuid, byte @NotNull [] message) {
        Objects.requireNonNull(message);

        if (uuid <= 0) {
            throw new IllegalArgumentException("Invalid uuid provided");
        }

        return Optional.ofNullable(ZEKit.ffi_ze_encrypt_with_public_key(uuid, message)).map(byte[]::clone);
    }

    public static Optional<byte[]> decryptDataWithPrivateKey(long uuid, byte @NotNull [] message) {
        Objects.requireNonNull(message);

        if (uuid <= 0) {
            throw new IllegalArgumentException("Invalid uuid provided");
        }

        return Optional.ofNullable(ZEKit.ffi_ze_decrypt_with_private_key(uuid, message)).map(byte[]::clone);
    }

    public static boolean generateNonce(long uuid, @NotNull EncryptionMode type) {
        Objects.requireNonNull(type);

        if (uuid <= 0) {
            throw new IllegalArgumentException("Invalid uuid provided");
        }

        if (type == EncryptionMode.NONE) {
            return false;
        }

        int mode = type.getValue();
        int code = ZEKit.ffi_ze_generate_nonce(uuid, mode);

        return code == 0;
    }

    public static Optional<byte[]> getNonce(long uuid,  @NotNull EncryptionMode type) {
        Objects.requireNonNull(type);

        if (uuid <= 0) {
            throw new IllegalArgumentException("Invalid uuid provided");
        }

        if (type == EncryptionMode.NONE) {
            return Optional.empty();
        }

        int mode = type.getValue();
        return Optional.ofNullable(ZEKit.ffi_ze_get_nonce(uuid, mode)).map(byte[]::clone);
    }

    public static boolean setNonce(long uuid, @NotNull EncryptionMode type, byte[] nonce) {
        Objects.requireNonNull(type);

        if (uuid <= 0) {
            throw new IllegalArgumentException("Invalid uuid provided");
        }

        if (type == EncryptionMode.NONE) {
            return false;
        }

        int mode = type.getValue();
        int code = ZEKit.ffi_ze_set_nonce(uuid, mode, nonce);

        return code == 0;
    }

    public static boolean generateKeys(long uuid, @NotNull EncryptionMode type) {
        Objects.requireNonNull(type);

        if (uuid <= 0) {
            throw new IllegalArgumentException("Invalid uuid provided");
        }

        if (type == EncryptionMode.NONE) {
            return false;
        }

        int mode = type.getValue();
        int code = ZEKit.ffi_ze_generate_keys(uuid, mode);

        return code == 0;
    }

    public static Optional<byte[]> getKeypair(long uuid,  @NotNull KeyType type) {
        Objects.requireNonNull(type);

        if (uuid <= 0) {
            throw new IllegalArgumentException("Invalid uuid provided");
        }

        int mode = type.getValue();
        return Optional.ofNullable(ZEKit.ffi_ze_get_keypair(uuid, mode)).map(byte[]::clone);
    }

    public static Optional<byte[]> createSignature(long uuid, byte @NotNull [] message) {
        Objects.requireNonNull(message);

        if (uuid <= 0) {
            throw new IllegalArgumentException("Invalid uuid provided");
        }

        if (message.length == 0) {
            return Optional.empty();
        }

        return Optional.ofNullable(ZEKit.ffi_ze_create_signature(uuid, message)).map(byte[]::clone);
    }

    public static boolean verifySignature(long uuid, byte @NotNull [] hash, byte @Nullable [] message) {
        Objects.requireNonNull(message);
        Objects.requireNonNull(hash);

        if (uuid <= 0) {
            throw new IllegalArgumentException("Invalid uuid provided");
        }

        if (message.length == 0 || hash.length == 0) {
            return false;
        }

        return ZEKit.ffi_ze_verify_signature(uuid, hash, message);
    }

    public static boolean setPartnerPublicKey(long uuid, byte @NotNull [] key) {
        Objects.requireNonNull(key);

        if (uuid <= 0) {
            throw new IllegalArgumentException("Invalid uuid provided");
        }

        if (key.length == 0) {
            return false;
        }

        int code = ZEKit.ffi_ze_set_partner_public_key(uuid, key);
        return code == 0;
    }

    public static boolean generateSigningKeypair(long uuid) {
        if (uuid <= 0) {
            throw new IllegalArgumentException("Invalid uuid provided");
        }

        int code = ZEKit.ffi_ze_generate_signing_keypair(uuid);
        return code == 0;
    }

    public static boolean deriveSigningKeypair(long uuid, @NotNull SideType side) {
        Objects.requireNonNull(side);

        if (uuid <= 0) {
            throw new IllegalArgumentException("Invalid uuid provided");
        }

        int code = ZEKit.ffi_ze_derive_signing_keys(uuid, side.getValue());
        return code == 0;
    }

    public static boolean finalizeSigningKeypair(long uuid, @NotNull SideType side) {
        Objects.requireNonNull(side);

        if (uuid <= 0) {
            throw new IllegalArgumentException("Invalid uuid provided");
        }

        int code = ZEKit.ffi_ze_finalize_signing_key(uuid, side.getValue());
        return code == 0;
    }

    public static boolean setSigningPublicKey(long uuid, byte @NotNull [] key) {
        Objects.requireNonNull(key);

        if (uuid <= 0) {
            throw new IllegalArgumentException("Invalid uuid provided");
        }

        if (key.length == 0) {
            return false;
        }

        int code = ZEKit.ffi_ze_set_signing_public_key(uuid, key);
        return code == 0;
    }

    public static Optional<byte[]> getBaseSigningKey(long uuid) {
        if (uuid <= 0) {
            throw new IllegalArgumentException("Invalid uuid provided");
        }

        return Optional.ofNullable(ZEKit.ffi_ze_get_base_signing_key(uuid)).map(byte[]::clone);
    }

    public static Optional<byte[]> createKeyExchange(long uuid) {
        if (uuid <= 0) {
            throw new IllegalArgumentException("Invalid uuid provided");
        }

        return Optional.ofNullable(ZEKit.ffi_ze_create_key_exchange(uuid)).map(byte[]::clone);
    }

    public static boolean processKeyExchange(long uuid, byte[] message) {
        Objects.requireNonNull(message);

        if (uuid <= 0) {
            throw new IllegalArgumentException("Invalid uuid provided");
        }

        if (message.length == 0) {
            return false;
        }

        int code = ZEKit.ffi_ze_process_key_exchange(uuid, message);
        return code == 0;
    }
}
