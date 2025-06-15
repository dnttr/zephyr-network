package org.dnttr.zephyr.network.communication.core.utilities;

/**
 * @author dnttr
 */

public final class PacketUtils {

    /**
     * Checks if the given packet is a reserved.
     *
     * @param identity the identity to check
     * @return true if the identity is reserved, false otherwise
     */
    public static boolean isReserved(int identity) {
        return identity < 0x0;
    }
}
