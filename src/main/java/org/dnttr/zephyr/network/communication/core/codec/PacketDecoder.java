package org.dnttr.zephyr.network.communication.core.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.dnttr.zephyr.network.communication.core.packet.Carrier;
import org.dnttr.zephyr.toolset.types.Type;

import java.util.List;

/**
 * @author dnttr
 */

public class PacketDecoder extends ByteToMessageDecoder {

    private final int requiredBytes = 5 * Type.INT.getBytes();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> objects) {
        if (buffer.readableBytes() < requiredBytes) {
            return;
        }

        int version = buffer.readInt();

        if (version == 0x1) {
            int identity = buffer.readInt();
            int hash = buffer.readInt();
            int content = buffer.readInt();

            if (content <= 0) {
                ctx.channel().disconnect();
                return;
            }

            if (hash <= 0) {
                if (!(identity == -1 || identity == -2)) {
                    ctx.channel().disconnect();
                    return;
                }
            }

            buffer.markReaderIndex();

            if (buffer.readableBytes() < hash + content) {
                buffer.resetReaderIndex();
                return;
            }

            buffer.discardReadBytes(); //i think it should be discarded. TODO: check this

            try {
                objects.add(new Carrier(version, identity, hash, content, buffer.copy()));
            } catch (Exception _) {
                ctx.channel().disconnect();
            } finally {
                buffer.skipBytes(buffer.readableBytes());
            }
        } else {
            ctx.channel().disconnect();
        }
    }
}
