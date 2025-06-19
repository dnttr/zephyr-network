package org.dnttr.zephyr.network.communication.core.packet.transformer.impl;

import io.netty.buffer.Unpooled;
import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.communication.core.packet.Carrier;
import org.dnttr.zephyr.network.communication.core.packet.Direction;
import org.dnttr.zephyr.network.communication.core.packet.transformer.Transformer;
import org.dnttr.zephyr.network.communication.core.utilities.PacketUtils;
import org.dnttr.zephyr.serializer.Serializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.dnttr.zephyr.network.protocol.Constants.ENCRYPTION_EXEMPT_IDS;

/**
 * @author dnttr
 */

public class InboundTransformer extends Transformer<Carrier> {

    @Override
    public @Nullable Object transform(@NotNull Direction direction, @NotNull Carrier carrier, @NotNull ChannelContext context) throws Exception {
        var klass = getPackets().get(carrier.identity());

        if (klass == null) {
            return null;
        }

        if (!PacketUtils.isTimestampValid(carrier)) {
            return null;
        }

        byte[] data = this.process(context,carrier);

        if (data == null) {
            return null;
        }

        return Serializer.deserializeUsingBuffer(klass, Unpooled.wrappedBuffer(data));
    }

    @Override
    protected byte[] process(@NotNull ChannelContext context, @NotNull Carrier message) throws Exception {
        byte[] buffer = message.content();

        if (message.hashSize() != 0 && context.isHash()) {
            boolean isVerified = getIntegrity().verify(context, message.timestamp(), message);

            if (!isVerified) {
                return null;
            }
        }

        if (!ENCRYPTION_EXEMPT_IDS.contains(message.identity())) {
            buffer = getProcessor(context).processInbound(context, message.content());
        }

        return buffer;
    }
}
