package org.dnttr.zephyr.network.communication.core.utilities;

import org.dnttr.zephyr.network.communication.core.packet.Carrier;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

import static org.dnttr.zephyr.network.protocol.Constants.CACHE_EXPIRATION_TIME;

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

    /**
     * Validates the timestamp of an incoming carrier to mitigate simple replay attacks.
     *
     * @param carrier The inbound carrier packet.
     * @return {@code true} if the timestamp is within the allowed time window, {@code false} otherwise.
     */
    public static boolean isTimestampValid(@NotNull Carrier carrier) {
        return Math.abs(System.currentTimeMillis() - carrier.timestamp()) <= Duration.ofSeconds(CACHE_EXPIRATION_TIME).toMillis();
    }

}
