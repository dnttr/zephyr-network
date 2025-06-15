/*
 * Part of the Zephyr-Encryption project
 * Copyright (c) 2025 dnttr
 *
 * This source code is licensed under the MIT license.
 */
package org.dnttr.zephyr.network.bridge;

import lombok.Getter;
import org.dnttr.zephyr.network.bridge.internal.ZEKit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * High-level cryptographic API for secure communications.
 * <p>
 * This class provides a safe, idiomatic Java interface to the native cryptographic
 * library via JNI, supporting symmetric and asymmetric encryption, key management,
 * signing, and secure session handling.
 * </p>
 * <p>
 * All operations are session-based and require a valid session UUID, which is
 * managed internally by the native library. This class ensures parameter validation,
 * defensive copying, and safe error handling.
 * </p>
 * <p>
 * <strong>Usage:</strong> Always use this class instead of direct native calls.
 * </p>
 *
 * @author dnttr
 * @since 1.0.4
 * @version 1.0.4-ZE
 */
public final class Security {

    /**
     * Supported encryption modes.
     * @implNote Mode NONE isn't supported as a parameter,<br>it is the caller responsibility to take that into account
     */
    public enum EncryptionMode {
        /** No encryption. */
        NONE(-1),
        /** Symmetric encryption. */
        SYMMETRIC(0),
        /** Asymmetric encryption. */
        ASYMMETRIC(1);

        @Getter
        private final int value;

        EncryptionMode(int value) {
            this.value = value;
        }
    }

    /**
     * Key types for asymmetric cryptography.
     */
    public enum KeyType
    {
        /** Public key. */
        PUBLIC(0),
        /** Private key. */
        PRIVATE(1);

        @Getter
        private final int value;

        KeyType(int value) {
            this.value = value;
        }
    }

    /**
     * Connection side for key derivation.
     */
    public enum SideType {
        /** Server side. */
        SERVER(0),
        /** Client side. */
        CLIENT(1);

        @Getter
        private final int value;

        SideType(int value) {
            this.value = value;
        }
    }

    /**
     * Creates a new cryptographic session.
     *
     * @return UUID identifying the session
     * @throws IllegalStateException if session creation fails
     */
    public static long createSession() {
        long uuid = ZEKit.ffi_ze_create_session();

        if (uuid <= 0) {
            throw new IllegalStateException("Create session failed, invalid uuid has been returned");
        }

        return uuid;
    }

    /**
     * Deletes a cryptographic session and releases resources.
     *
     * @param uuid Session identifier
     * @return true if deletion succeeded, false otherwise
     * @throws IllegalArgumentException if uuid is invalid
     */
    public static boolean deleteSession(long uuid) {
        if (uuid <= 0) {
            throw new IllegalArgumentException("Delete session failed due to illegal uuid provided");
        }

        int code = ZEKit.ffi_ze_delete_session(uuid);
        return code == 0;
    }

    /**
     * Closes the native cryptographic library and releases all resources.
     */
    public static void closeLibrary() {
        ZEKit.ffi_ze_close_library();
    }

    /**
     * Encrypts data using the specified session and encryption mode.
     * <p>
     * For {@link EncryptionMode#SYMMETRIC}, uses the session's symmetric key and optional AEAD.
     * For {@link EncryptionMode#ASYMMETRIC}, uses the session's public key (AEAD is ignored).
     * </p>
     *
     * @param uuid   Session identifier
     * @param mode   Encryption mode (symmetric or asymmetric)
     * @param message Data to encrypt (not null)
     * @param aead   Optional associated data for AEAD (may or may not, be null, only used in symmetric mode)
     * @return Optional containing the encrypted data, or empty if encryption fails
     * @throws IllegalArgumentException if uuid is invalid or mode is NONE
     * @throws NullPointerException     if message or mode is null
     */
    public static Optional<byte[]> encrypt(long uuid, @NotNull EncryptionMode mode, byte @NotNull [] message, byte @Nullable [] aead) {
        Objects.requireNonNull(message);
        Objects.requireNonNull(mode);

        if (uuid <= 0) {
            throw new IllegalArgumentException("Invalid uuid provided");
        }

        if (message.length == 0) {
            return Optional.empty();
        }

        if (aead != null) {
            if (aead.length == 0) {
                throw new IllegalArgumentException("Aead provided is not empty");
            }
        }

        switch (mode) {
            case SYMMETRIC -> {
                return Optional.ofNullable(ZEKit.ffi_ze_encrypt_data(uuid, message, aead)).map(byte[]::clone);
            }

            case ASYMMETRIC -> {
                return Optional.ofNullable(ZEKit.ffi_ze_encrypt_with_public_key(uuid, message)).map(byte[]::clone);
            }

            case NONE -> {
                return Optional.of(message);
            }

            default -> throw new IllegalArgumentException("Invalid mode provided");
        }
    }

    /**
     * Decrypts data using the specified session and encryption mode.
     * <p>
     * For {@link EncryptionMode#SYMMETRIC}, uses the session's symmetric key and optional AEAD.
     * For {@link EncryptionMode#ASYMMETRIC}, uses the session's private key (AEAD is ignored).
     * </p>
     *
     * @param uuid   Session identifier
     * @param mode   Encryption mode (symmetric or asymmetric)
     * @param message Data to decrypt (not null)
     * @param aead   Optional associated data for AEAD (may, or may not, be null, only used in symmetric mode)
     * @return Optional containing the decrypted data, or empty if decryption fails
     * @throws IllegalArgumentException if uuid is invalid or mode is NONE
     * @throws NullPointerException     if message or mode is null
     */
    public static Optional<byte[]> decrypt(long uuid, @NotNull EncryptionMode mode, byte @NotNull [] message, byte @Nullable [] aead) {
        Objects.requireNonNull(message);
        Objects.requireNonNull(mode);

        if (uuid <= 0) {
            throw new IllegalArgumentException("Invalid uuid provided");
        }

        if (message.length == 0) {
            return Optional.empty();
        }

        if (aead != null) {
            if (aead.length == 0) {
                return Optional.empty();
            }
        }

        switch (mode) {
            case SYMMETRIC -> {
                return Optional.ofNullable(ZEKit.ffi_ze_decrypt_data(uuid, message, aead)).map(byte[]::clone);
            }

            case ASYMMETRIC -> {
                return Optional.ofNullable(ZEKit.ffi_ze_decrypt_with_private_key(uuid, message)).map(byte[]::clone);
            }

            case NONE -> {
                return Optional.of(message);
            }

            default -> throw new IllegalArgumentException("Invalid mode provided");
        }
    }

    /**
     * Generates a nonce for the specified encryption mode.
     *
     * @param uuid Session identifier
     * @param type Encryption mode
     * @return true if nonce generation succeeded, false otherwise
     * @throws IllegalArgumentException if uuid is invalid
     * @throws NullPointerException if type is null
     */
    public static boolean buildNonce(long uuid, @NotNull EncryptionMode type) {
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

    /**
     * Retrieves the nonce for the specified encryption mode.
     *
     * @param uuid Session identifier
     * @param type Encryption mode
     * @return Optional containing the nonce, or empty if not available
     * @throws IllegalArgumentException if uuid is invalid
     * @throws NullPointerException if type is null
     */
    public static Optional<byte[]> getNonce(long uuid, @NotNull EncryptionMode type) {
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

    /**
     * Sets the nonce for the specified encryption mode.
     *
     * @param uuid Session identifier
     * @param type Encryption mode
     * @param nonce Nonce data
     * @return true if nonce was set successfully, false otherwise
     * @throws IllegalArgumentException if uuid is invalid
     * @throws NullPointerException if type is null
     */
    public static boolean setNonce(long uuid, @NotNull EncryptionMode type, byte[] nonce) {
        Objects.requireNonNull(type);

        if (uuid <= 0) {
            throw new IllegalArgumentException("Invalid uuid provided");
        }

        if (type == EncryptionMode.NONE) {
            return false;
        }

        if (nonce.length == 0) {
            return false;
        }

        int mode = type.getValue();
        int code = ZEKit.ffi_ze_set_nonce(uuid, mode, nonce);

        return code == 0;
    }

    /**
     * Generates keys for the specified encryption mode.
     *
     * @param uuid Session identifier
     * @param type Encryption mode
     * @return true if key generation succeeded, false otherwise
     * @throws IllegalArgumentException if uuid is invalid
     * @throws NullPointerException if type is null
     */
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

    /**
     * Retrieves a keypair for the specified type.
     *
     * @param uuid Session identifier
     * @param type Key type (public/private)
     * @return Optional containing the key, or empty if not available
     * @throws IllegalArgumentException if uuid is invalid
     * @throws NullPointerException if type is null
     */
    public static Optional<byte[]> getKeyPair(long uuid,  @NotNull KeyType type) {
        Objects.requireNonNull(type);

        if (uuid <= 0) {
            throw new IllegalArgumentException("Invalid uuid provided");
        }

        int mode = type.getValue();
        return Optional.ofNullable(ZEKit.ffi_ze_get_keypair(uuid, mode)).map(byte[]::clone);
    }

    /**
     * Creates a digital signature for the given message.
     *
     * @param uuid Session identifier
     * @param message Message to sign
     * @return Optional containing the signature, or empty if signing fails
     * @throws IllegalArgumentException if uuid is invalid
     * @throws NullPointerException if message is null
     */
    public static Optional<byte[]> sign(long uuid, byte @NotNull [] message) {
        Objects.requireNonNull(message);

        if (uuid <= 0) {
            throw new IllegalArgumentException("Invalid uuid provided");
        }

        if (message.length == 0) {
            return Optional.empty();
        }

        return Optional.ofNullable(ZEKit.ffi_ze_create_signature(uuid, message)).map(byte[]::clone);
    }

    /**
     * Verifies a digital signature for the given message.
     *
     * @param uuid Session identifier
     * @param hash Signature to verify
     * @param message Message to verify against
     * @return true if signature is valid, false otherwise
     * @throws IllegalArgumentException if uuid is invalid
     * @throws NullPointerException if hash or message is null
     */
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

    /**
     * Sets the partner's public key for asymmetric encryption.
     *
     * @param uuid Session identifier
     * @param key Partner's public key
     * @return true if the key was set successfully, false otherwise
     * @throws IllegalArgumentException if uuid is invalid
     * @throws NullPointerException if key is null
     */
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

    /**
     * Generates a base signing keypair for the session.
     *
     * @param uuid Session identifier
     * @return true if keypair generation succeeded, false otherwise
     * @throws IllegalArgumentException if uuid is invalid
     */
    public static boolean generateSigningKeyPair(long uuid) {
        if (uuid <= 0) {
            throw new IllegalArgumentException("Invalid uuid provided");
        }

        int code = ZEKit.ffi_ze_generate_signing_keypair(uuid);
        return code == 0;
    }

    /**
     * Derives signing keys for the specified side.
     *
     * @param uuid Session identifier
     * @param side Connection side (server/client)
     * @return true if key derivation succeeded, false otherwise
     * @throws IllegalArgumentException if uuid is invalid
     * @throws NullPointerException if side is null
     */
    public static boolean deriveSigningKeyPair(long uuid, @NotNull SideType side) {
        Objects.requireNonNull(side);

        if (uuid <= 0) {
            throw new IllegalArgumentException("Invalid uuid provided");
        }

        int code = ZEKit.ffi_ze_derive_signing_keys(uuid, side.getValue());
        return code == 0;
    }

    /**
     * Finalizes signing keypair derivation for the specified side.
     *
     * @param uuid Session identifier
     * @param side Connection side (server/client)
     * @return true if finalization succeeded, false otherwise
     * @throws IllegalArgumentException if uuid is invalid
     * @throws NullPointerException if side is null
     */
    public static boolean finalizeSigningKeyPair(long uuid, @NotNull SideType side) {
        Objects.requireNonNull(side);

        if (uuid <= 0) {
            throw new IllegalArgumentException("Invalid uuid provided");
        }

        int code = ZEKit.ffi_ze_finalize_signing_key(uuid, side.getValue());
        return code == 0;
    }

    /**
     * Sets the partner's signing public key.
     *
     * @param uuid Session identifier
     * @param key Partner's signing public key
     * @return true if the key was set successfully, false otherwise
     * @throws IllegalArgumentException if uuid is invalid
     * @throws NullPointerException if key is null
     */
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

    /**
     * Retrieves the base signing public key for the session.
     *
     * @param uuid Session identifier
     * @return Optional containing the base signing key, or empty if not available
     * @throws IllegalArgumentException if uuid is invalid
     */
    public static Optional<byte[]> getBaseSigningKey(long uuid) {
        if (uuid <= 0) {
            throw new IllegalArgumentException("Invalid uuid provided");
        }

        return Optional.ofNullable(ZEKit.ffi_ze_get_base_signing_key(uuid)).map(byte[]::clone);
    }

    /**
     * Creates a key exchange message for the session.
     *
     * @param uuid Session identifier
     * @return Optional containing the key exchange message, or empty if not available
     * @throws IllegalArgumentException if uuid is invalid
     */
    public static Optional<byte[]> createKeyExchange(long uuid) {
        if (uuid <= 0) {
            throw new IllegalArgumentException("Invalid uuid provided");
        }

        return Optional.ofNullable(ZEKit.ffi_ze_create_key_exchange(uuid)).map(byte[]::clone);
    }

    /**
     * Processes a received key exchange message.
     *
     * @param uuid Session identifier
     * @param message Key exchange message to process
     * @return true if processing succeeded, false otherwise
     * @throws IllegalArgumentException if uuid is invalid
     * @throws NullPointerException if message is null
     */
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
