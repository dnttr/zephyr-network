package org.dnttr.zephyr.network.protocol;

import java.util.Set;

/**
 * @author dnttr
 */

public class Constants {

    public static final int VER_1 = 0x1;

    public static final int MAX_LENGTH = 1024;

    /**
     * Defines the time window in seconds for which a packet timestamp is considered valid.
     * This is used to mitigate simple replay attacks by rejecting packets that are too old.
     */
    public static final int CACHE_EXPIRATION_TIME = 6;

    /**
     * A set of packet identifiers that should be exempt from the standard encryption/decryption process.
     * This is necessary for handshake packets like nonce exchanges that must be sent in plaintext.
     */
    public static final Set<Integer> ENCRYPTION_EXEMPT_IDS = Set.of(-0x3);
}
