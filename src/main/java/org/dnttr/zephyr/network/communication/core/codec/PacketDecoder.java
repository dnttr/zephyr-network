package org.dnttr.zephyr.network.communication.core.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.dnttr.zephyr.network.communication.core.packet.Carrier;
import org.dnttr.zephyr.toolset.types.Type;

import java.util.List;

/**
 * @author dnttr
 */

public class PacketDecoder extends ByteToMessageDecoder {

    private final int requiredBytes = 5 * Type.INT.getBytes(); //shouldn't this be 4? whatever for now

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> objects) {
        while (buffer.readableBytes() >= requiredBytes) {
            buffer.markReaderIndex();

            int version = buffer.readInt();

            if (version != 0x1) {
                ctx.channel().disconnect();
                return;
            }


            int packetId = buffer.readInt();
            int hashSize = buffer.readInt();
            int contentSize = buffer.readInt();

            int totalPacketLength = hashSize + contentSize;

            this.validate(ctx, packetId, hashSize, contentSize);

            if (buffer.readableBytes() < totalPacketLength) {
                buffer.resetReaderIndex();
                return;
            }

            try {
                ByteBuf hashBuffer = hashSize > 0 ? buffer.readBytes(hashSize) : null;
                ByteBuf contentBuffer = buffer.readBytes(contentSize);

                byte[] hashData = hashBuffer != null ? ByteBufUtil.getBytes(hashBuffer) : null;
                byte[] contentData = ByteBufUtil.getBytes(contentBuffer);

                Carrier carrier = new Carrier(version, packetId, hashSize, contentSize, hashData,  contentData);

                objects.add(carrier);
            } catch (Exception e) {
                ctx.channel().disconnect();
                return;
            }
        }
    }

    private void validate(ChannelHandlerContext ctx, int packetId, int hashSize, int contentSize) {
        if (contentSize <= 0) {
            ctx.channel().disconnect();
            return;
        }

        if (hashSize <= 0) {
            if (packetId == -1 || packetId == -2) {
                return;
            }

            ctx.channel().disconnect();
        }
    }
}
