package org.dnttr.zephyr.network.communication.core.packet.processor;

import org.dnttr.zephyr.network.bridge.Security;
import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.communication.core.packet.Carrier;
import org.jetbrains.annotations.NotNull;

/**
 * @author dnttr
 */

public final class Integrity {

    public boolean verify(@NotNull ChannelContext context, @NotNull Carrier carrier) {
        if (carrier.hash() == null || carrier.content() == null) {
            context.restrict("Hash or content is null.");

            return false;
        }

        boolean isPreserved = Security.verifySignature(context.getUuid(), carrier.hash(), carrier.content());

        if (!isPreserved) {
            context.restrict("Invalid signature.");

            return false;
        }

        return true;
    }

    public byte @NotNull [] build(@NotNull ChannelContext context, byte @NotNull [] packet) {
        if (context.isHash()) {
            var computedHash = Security.sign(context.getUuid(), packet);

            if (computedHash.isEmpty()) {
                throw new IllegalStateException("Couldn't compute hash");
            }

            return computedHash.get();
        }

        return new byte[] {};
    }
}
