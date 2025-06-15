/*
 * Part of the Zephyr-Encryption project
 * Copyright (c) 2025 dnttr
 *
 * This source code is licensed under the MIT license.
 */

package org.dnttr.zephyr.network.bridge;

import org.jetbrains.annotations.ApiStatus;

/**
 * <strong>Native cryptographic operations bridge for secure communications.</strong>
 * <p>
 * This class provides ZNB (JNI based) bindings to a native cryptographic library that implements
 * symmetric and asymmetric encryption and hashing.
 * It manages cryptographic sessions, key derivation, and secure message exchange.
 * </p><p>
 * <strong>Security Notice:</strong> Direct use of this class is strongly discouraged.
 * It should only be accessed through approved wrapper classes that implement proper
 * validation and security controls.
 * </p><p>
 * Improper use or tampering with these native bindings may compromise the integrity
 * of the virtual machine, potentially leading to memory corruption, information
 * disclosure, or other security vulnerabilities.
 * </p><p>
 * This class is internal API and subject to change without notice.
 * </p>
 *
 * @author dnttr
 * @since 1.0.0-ZE
 * @version 1.0.2-ZE
 */

@ApiStatus.Internal
public final class ZEKit {

    /**
     * Gracefully shuts down the native cryptographic library.
     * Releases all allocated resources and should be called when the application
     * is terminating to prevent memory leaks.
     */
    public static native void ffi_ze_close();

    /**
     * Opens a new cryptographic session in the native library.
     * Creates a native object that manages all keys and cryptographic state.
     *
     * @return UUID identifying the session for subsequent operations
     */
    public static native long ffi_zm_open_session();

    /**
     * Terminates a cryptographic session and releases associated resources.
     *
     * @param uuid Session identifier returned by {@link #ffi_zm_open_session()}
     * @return Status code indicating SUCCESS or FAILURE
     */
    public static native int ffi_zm_close_session(long uuid);

    /**
     * Performs symmetric encryption of the provided message.
     *
     * @param uuid Session identifier
     * @param messageBuffer Message data to encrypt
     * @param aeadBuffer Associated data for authenticated encryption
     * @return Encrypted data
     */
    public static native byte[] ffi_ze_encrypt_symmetric(long uuid, byte[] messageBuffer, byte[] aeadBuffer);

    /**
     * Performs symmetric decryption of the provided message.
     *
     * @param uuid Session identifier
     * @param messageBuffer Encrypted message data
     * @param aeadBuffer Associated data for authenticated decryption
     * @return Decrypted data
     */
    public static native byte[] ffi_ze_decrypt_symmetric(long uuid, byte[] messageBuffer, byte[] aeadBuffer);

    /**
     * Performs asymmetric encryption of the provided message.
     *
     * @param uuid Session identifier
     * @param messageBuffer Message data to encrypt
     * @return Encrypted data
     */
    public static native byte[] ffi_ze_encrypt_asymmetric(long uuid, byte[] messageBuffer);

    /**
     * Performs asymmetric decryption of the provided message.
     *
     * @param uuid Session identifier
     * @param messageBuffer Encrypted message data
     * @return Decrypted data
     */
    public static native byte[] ffi_ze_decrypt_asymmetric(long uuid, byte[] messageBuffer);

    /**
     * Builds a nonce for encryption/decryption operations.
     *
     * @param uuid Session identifier
     * @param mode Encryption type: 0 for SYMMETRIC, 1 for ASYMMETRIC
     */
    public static native void ffi_ze_nonce(long uuid, int mode);

    /**
     * Builds a key for encryption/decryption operations.
     *
     * @param uuid Session identifier
     * @param mode Encryption type: 0 for SYMMETRIC, 1 for ASYMMETRIC
     */
    public static native void ffi_ze_key(long uuid, int mode);

    /**
     * Sets the symmetric key for the specified session.
     *
     * @param uuid Session identifier
     * @param keyBuffer Symmetric key data
     */
    public static native void ffi_ze_set_symmetric_key(long uuid, byte[] keyBuffer);

    /**
     * Sets an asymmetric key for the specified session.
     *
     * @param uuid Session identifier
     * @param mode Key type: 0 for PUBLIC, 1 for PRIVATE
     * @param keyBuffer Key data
     */
    public static native void ffi_ze_set_asymmetric_key(long uuid, int mode, byte[] keyBuffer);

    /**
     * Sets the nonce for encryption/decryption operations.
     *
     * @param uuid Session identifier
     * @param mode Encryption type: 0 for SYMMETRIC, 1 for ASYMMETRIC
     * @param nonceBuffer Nonce data
     */
    public static native void ffi_ze_set_nonce(long uuid, int mode, byte[] nonceBuffer);

    /**
     * Retrieves the symmetric key for the specified session.
     *
     * @param uuid Session identifier
     * @return Symmetric key data
     */
    public static native byte[] ffi_ze_get_symmetric_key(long uuid);

    /**
     * Retrieves an asymmetric key for the specified session.
     *
     * @param uuid Session identifier
     * @param mode Key type: 0 for PUBLIC, 1 for PRIVATE
     * @return Requested asymmetric key
     */
    public static native byte[] ffi_ze_get_asymmetric_key(long uuid, int mode);

    /**
     * Retrieves the nonce for the specified encryption type.
     *
     * @param uuid Session identifier
     * @param mode Encryption type: 0 for SYMMETRIC, 1 for ASYMMETRIC
     * @return Nonce data
     */
    public static native byte[] ffi_ze_get_nonce(long uuid, int mode);

    /**
     * Generates a hash for the provided message.
     *
     * @param uuid Session identifier
     * @param messageBuffer Message to hash
     * @return Generated hash
     */
    public static native byte[] ffi_ze_build_hash_sh0(long uuid, byte[] messageBuffer);

    /**
     * Verifies if a hash matches the provided message.
     *
     * @param uuid Session identifier
     * @param hashBuffer Hash to verify
     * @param messageBuffer Message to verify against
     * @return true if hash matches the message, false otherwise
     */
    public static native boolean ffi_ze_compare_hash_sh0(long uuid, byte[] hashBuffer, byte[] messageBuffer);

    /**
     * Builds a base keypair for later key derivation.
     *
     * @param uuid Session identifier
     */
    public static native void ffi_ze_build_base_key_sh0(long uuid);

    /**
     * Derives RX and TX keys from the base keypair.
     *
     * @param uuid Session identifier
     * @param mode Connection type: 0 for SERVER, 1 for CLIENT
     */
    public static native void ffi_ze_derive_keys_sh0(long uuid, int mode);

    /**
     * Performs final hash key derivation from RX and TX keys.
     *
     * @param uuid Session identifier
     * @param mode Connection type: 0 for SERVER, 1 for CLIENT
     */
    public static native void ffi_ze_derive_final_key_sh0(long uuid, int mode);

    /**
     * Retrieves the received public key.
     *
     * @param uuid Session identifier
     * @return Received public key
     */
    public static native byte[] ffi_ze_get_rv_public_key_sh0(long uuid);

    /**
     * Sets the received public key from the other side of the connection.
     *
     * @param uuid Session identifier
     * @param keyBuffer Public key received from the other party
     */
    public static native void ffi_ze_set_rv_public_key_sh0(long uuid, byte[] keyBuffer);

    /**
     * Retrieves the generated base public key from the keypair.
     *
     * @param uuid Session identifier
     * @return Base public key
     */
    public static native byte[] ffi_ze_get_base_public_key_sh0(long uuid);

    /**
     * Sets the public key received from the other side for encryption/decryption.
     *
     * @param uuid Session identifier
     * @param keyBuffer Public key received from the other party
     */
    public static native void ffi_ze_set_asymmetric_received_key(long uuid, byte[] keyBuffer);

    /**
     * Returns an encrypted message containing the symmetric key.
     * Uses asymmetric encryption.
     *
     * @param uuid Session identifier
     * @return Encrypted exchange message
     */
    public static native byte[] ffi_ze_get_exchange_message(long uuid);

    /**
     * Processes an encrypted message containing a symmetric key.
     * Decrypts the message using asymmetric decryption and stores
     * the symmetric key for future operations.
     *
     * @param uuid Session identifier
     * @param messageBuffer Encrypted message containing symmetric key
     */
    public static native void ffi_ze_set_exchange_message(long uuid, byte[] messageBuffer);
}
