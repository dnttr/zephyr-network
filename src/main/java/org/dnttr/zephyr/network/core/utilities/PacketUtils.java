package org.dnttr.zephyr.network.core.utilities;

import io.netty.buffer.ByteBuf;
import org.dnttr.zephyr.toolset.Pair;
import org.jetbrains.annotations.Nullable;

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

    @Nullable
    public static Pair<ByteBuf, ByteBuf> decompose() {
        return null;
    }
}
