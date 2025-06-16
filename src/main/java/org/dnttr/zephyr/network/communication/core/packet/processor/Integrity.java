package org.dnttr.zephyr.network.communication.core.packet.processor;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.dnttr.zephyr.network.bridge.Security;
import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.communication.core.packet.Carrier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author dnttr
 */

public final class Integrity {

    public boolean verify(@NotNull ChannelContext context, long timestamp, @NotNull Carrier carrier) {
        if (carrier.hash() == null || carrier.content() == null) {
            context.restrict("Hash or content is null.");

            return false;
        }

        ByteBuf buffer = Unpooled.buffer();

        buffer.writeLong(timestamp);
        buffer.writeBytes(carrier.content());

        boolean isPreserved = Security.verifySignature(context.getUuid(), carrier.hash(), ByteBufUtil.getBytes(buffer));

        buffer.release();

        if (!isPreserved) {
            context.restrict("Invalid signature.");

            return false;
        }

        return true;
    }

    public byte @Nullable [] build(@NotNull ChannelContext context, long timestamp, byte @NotNull [] packet) {
        if (timestamp <= 0) {
            return null;
        }

        if (context.isHash()) {
            ByteBuf buffer = Unpooled.buffer();

            buffer.writeLong(timestamp);
            buffer.writeBytes(packet);

            var computedHash = Security.sign(context.getUuid(), ByteBufUtil.getBytes(buffer));

            buffer.release();

            if (computedHash.isEmpty()) {
                throw new IllegalStateException("Couldn't compute hash");
            }

            return computedHash.get();
        }

        return new byte[] {};
    }
}
