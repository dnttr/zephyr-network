package org.dnttr.zephyr.network.communication.core.packet.transformer.impl;

import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.communication.core.packet.Carrier;
import org.dnttr.zephyr.network.communication.core.packet.Direction;
import org.dnttr.zephyr.network.communication.core.packet.transformer.Transformer;
import org.dnttr.zephyr.network.protocol.Packet;
import org.dnttr.zephyr.serializer.Serializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.dnttr.zephyr.network.protocol.Constants.ENCRYPTION_EXEMPT_IDS;

/**
 * @author dnttr
 */

public class OutboundTransformer extends Transformer<Packet> {

    @Override
    public @Nullable Object transform(@NotNull Direction direction, @NotNull Packet packet, @NotNull ChannelContext context) throws Exception {
        byte[] data = this.process(context, packet);

        if (data == null) {
            return null;
        }

        long timestamp = System.currentTimeMillis();
        byte[] hash = getIntegrity().build(context, timestamp, data);

        if (hash == null) {
            return null;
        }

        int versionId = packet.getData().protocol();
        int packetId = packet.getData().identity();

        return new Carrier(versionId, packetId, hash.length, data.length, timestamp, hash, data);
    }

    @Override
    protected byte[] process(@NotNull ChannelContext context, @NotNull Packet message) throws Exception {
        byte[] data = Serializer.serializeToArray(message.getClass(), message);

        if (!ENCRYPTION_EXEMPT_IDS.contains(message.getData().identity())) {
            data = getProcessor(context).processOutbound(context, data);
        }

        return data;
    }
}
