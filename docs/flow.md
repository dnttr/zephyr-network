# Network flow

## Description

> This document outlines how the client and server talk to each other securely — including how they swap keys, switch to encrypted mode, and deal with nonces.

> **WARNING**: This is not fully finished and might change. There's probably something wrong with the way the nonce exchange is described — needs a recheck.

---

## Detailed Flow

### 1. Connection Establishment
- Client connects to the server.
- Fires a `ConnectionEstablishedEvent`.
- Sends a `REGISTER_REQUEST` (state packet).

### 2. Asymmetric Key Exchange (auth phase)
- Server gets the request.
- Generates a key pair (asymmetric) and some nonce.
- Sends public key to client.
- Client gets that, stores it, then makes its own key pair.
- Sends back its public key.
- Server stores client’s public key too.

### 3. Signing Key Exchange (integrity phase)
- Server sends signing key (not encrypted yet).
- Client gets it, responds with its own.
- Both sides:
    - Save the other's signing pubkey
    - Derive their signing keys (depending on CLIENT/SERVER mode)
    - Finalize and enable hash checking with `setHash(true)`

### 4. Nonce Exchange & Encryption Transition
- Server sends the nonce (in clear, not encrypted)
- Client stores the nonce
- Client sends `REGISTER_EXCHANGE` to continue
- Server gets it, creates symmetric encryption keys
- Packs the key exchange data in a `SessionPrivatePacket`
- Sends it to the client
- Client processes it and both switch to SYMMETRIC mode

### 5. Handshake Completion
- Client sends `REGISTER_FINISH`
- Server checks that everything looks good
- Both mark connection as ready
- Secure connection established

---

## Packet Structure

### General Packet Format

Every packet is structured like this:

- **Version**: Protocol version
- **Packet ID**: Type of the packet
- **Hash Size**: How big the hash data is
- **Content Size**: How big the actual payload is
- **Hash Data**: For integrity check (only if hash is enabled)
- **Content Data**: The payload

### Reading Order

When reading from `ByteBuf`:

1. Read header/constants first
2. Then the hash: `buffer.readBytes(hashSize)`
3. Then the content: `buffer.readBytes(contentSize)`

>️ **Important**: Do **not** mess this order up. Things break fast.

---

## Packet Types

### 1. `SessionStatePacket` *(subject to change)*
- Used to signal state changes (e.g. `REGISTER_REQUEST`, `REGISTER_EXCHANGE`, etc.)

### 2. `SessionPublicPacket`
- Used for public key exchange during auth/signing

### 3. `SessionNoncePacket`
- Sends the nonce (unencrypted, important!)
- Contains encryption mode + nonce bytes

### 4. `SessionPrivatePacket`
- Contains symmetric key exchange payload
- Sent encrypted

---

## Extra Notes

### Error Handling
- Add timeouts between phases or you’ll get stuck
- If a step fails (e.g. key mismatch), disconnect cleanly and maybe show a reason
- If protocol state is invalid, disconnect immediately

### Security Considerations
- Asymmetric is used initially for safety, then switch to symmetric for speed
- Signing ensures messages aren’t tampered with
- Nonce is sent unencrypted — yes, risky, but only happens once
- Don’t reuse key derivation modes — CLIENT/SERVER split avoids collisions
- Access to private keys must stay in native code — **no Java-side access**. This WILL get enforced later via removing native calls 

> Also don’t rely on this being exteremly secure. It’s just for fun

### Binary Format Stuff
- Validate all size fields to prevent reading garbage or worse
- Add max size limits — some sort of overflow attack might happen

---

## Testing

- Test reconnects to make sure you clean up state properly
- Try feeding it invalid packets to see if it explodes (and fix it if it does)
---