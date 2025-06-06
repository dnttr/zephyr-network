package org.dnttr.zephyr.network.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.dnttr.zephyr.protocol.packet.Carrier;

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
            buffer.writeInt(carrier.hash());
            buffer.writeInt(carrier.content());

            buffer.writeBytes(carrier.buffer());
        } else {
            throw new IllegalStateException("Unsupported version " + version);
        }

        carrier.buffer().release();
    }
}
