Network Flow Protocol

## Description

This document describes the secure communication flow between client and server, including key exchange, encryption mode transitions, and nonce handling.

#### WARNING: The documentation is incomplete and therefore is a subject to change. There might be inconsistencies with nonce's exchange

## Detailed Flow

1. **Session Initialization**
    - **Both** *client* and *server* open a session (`ffi_zm_open_session`)
    - Both sides build a keypair of base keys (`ffi_ze_build_derivable_key`) for later derivation
    - Both sides generate initial nonces to ensure session freshness

2. **Registration Request**
    - *Client* sends SessionStatePacket with `REGISTER_REQUEST(0x0)` including its nonce
    - *Server* receives SessionStatePacket, checks whether it is `REGISTER_REQUEST`
        - If **not**: Server restricts the connection
        - If **yes**: Server proceeds to build asymmetric keypair (`ffi_ze_key`, set to ASYMMETRIC) for authorization

3. **Key Exchange**
    - *Server* generates its own nonce
    - *Server* sends to *client*:
        - Public key designated for authorization
        - Public key designated for hashing
        - Server nonce
    - *Server* sets the encryption mode to ASYMMETRIC
    - *Client* receives the public key, server nonce, and builds a symmetric key (`ffi_ze_key`, set to SYMMETRIC)
    - Both sides derive the hashing key (`ffi_ze_derive_secret_key` and `ffi_ze_derive_hash_key`) using:
        - Previously exchanged public key (hashing)
    - Both sides enable the hash mode
    - *Client* sets the encryption mode to ASYMMETRIC (using received public key)
    - *Client* sends the shared symmetric key over to the *server* (encrypted with server's public key)

4. **Secure Channel Establishment**
    - *Server* receives the key, decrypts it
    - *Server* sends the confirmation packet with updated nonce
    - *Server* sets the mode to SYMMETRIC
    - *Client* receives the confirmation packet and verifies nonce
    - *Client* sets the mode to SYMMETRIC

5. **Secure Communication**
    - All subsequent messages use the established symmetric encryption
    - Nonces are updated with each message to prevent replay attacks