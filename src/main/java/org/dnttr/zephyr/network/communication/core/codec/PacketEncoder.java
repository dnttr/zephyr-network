package org.dnttr.zephyr.network.communication.core.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.dnttr.zephyr.network.communication.core.packet.Carrier;

/**
 * @author dnttr
 */

public class PacketEncoder extends MessageToByteEncoder<Carrier> {

    @Override
    protected void encode(/* UNUSED */ ChannelHandlerContext ctx, Carrier carrier, ByteBuf buffer) {
        int version = carrier.version();

        buffer.writeInt(version);

        if (version == 0x1) {
            buffer.writeInt(carrier.identity());
            buffer.writeInt(carrier.hashSize());
            buffer.writeInt(carrier.contentSize());
            buffer.writeLong(carrier.timestamp());

            if (carrier.hashSize() != 0) {
                buffer.writeBytes(carrier.hash());
            }

            buffer.writeBytes(carrier.content());
        } else {
            throw new IllegalStateException("Unsupported version " + version);
        }
    }
}
