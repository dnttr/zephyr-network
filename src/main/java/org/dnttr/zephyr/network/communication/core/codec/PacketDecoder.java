package org.dnttr.zephyr.network.communication.core.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.dnttr.zephyr.network.communication.core.packet.Carrier;
import org.dnttr.zephyr.toolset.types.Type;
import org.jetbrains.annotations.NotNull;

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
                Carrier carrier = this.split(version, packetId, hashSize, contentSize, buffer);
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

    private @NotNull Carrier split(int version, int packetId, int hashSize, int contentSize, ByteBuf buffer) {
        byte[] hashData = new byte[hashSize];
        byte[] contentData = ByteBufUtil.getBytes(buffer.readBytes(contentSize));

        if (hashSize > 0) {
            ByteBuf temp = buffer.readBytes(hashSize);
            hashData = ByteBufUtil.getBytes(temp);
        }

        return new Carrier(version, packetId, hashSize, contentSize, hashData, contentData);
    }
}
