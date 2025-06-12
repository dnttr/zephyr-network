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
        while (buffer.readableBytes() >= requiredBytes) {
            buffer.markReaderIndex();

            int version = buffer.readInt();

            if (version != 0x1) {
                ctx.channel().disconnect();
                return;
            }


            int identity = buffer.readInt();
            int hash = buffer.readInt();
            int content = buffer.readInt();

            int totalPacketLength = hash + content;

            if (content <= 0) {
                ctx.channel().disconnect();
                return;
            }

            if (hash <= 0 && !(identity == -1 || identity == -2)) {
                ctx.channel().disconnect();
                return;
            }

            if (buffer.readableBytes() < totalPacketLength) {
                buffer.resetReaderIndex();
                return;
            }

            try {
                ByteBuf hashData = hash > 0 ? buffer.readBytes(hash) : null;
                ByteBuf contentData = buffer.readBytes(content);

                Carrier carrier = new Carrier(version, identity, hash, content, hashData, contentData);

                if (hashData != null) {
                    hashData.release();
                }

                objects.add(carrier);
            } catch (Exception e) {
                ctx.channel().disconnect();
                return;
            }
        }
    }
}
