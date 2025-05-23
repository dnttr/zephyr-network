package org.dnttr.zephyr.network.utilities;

/**
 * @author dnttr
 */

public class PacketUtils {

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
