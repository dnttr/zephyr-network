package org.dnttr.zephyr.network.communication.core.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.dnttr.zephyr.network.communication.core.packet.Carrier;
import org.dnttr.zephyr.network.communication.core.utilities.DecoderUtils;
import org.dnttr.zephyr.network.protocol.Constants;
import org.dnttr.zephyr.toolset.types.Type;

import java.util.List;

/**
 * @author dnttr
 */

public class PacketDecoder extends ByteToMessageDecoder {

    public static final int HEADER_SIZE = (4 * Type.INT.getBytes()) + Type.LONG.getBytes();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> objects) {
        while (buffer.readableBytes() >= HEADER_SIZE) {
            buffer.markReaderIndex();

            int version = buffer.readInt();
            if (version != Constants.VER_1) {
                ctx.channel().disconnect();
                return;
            }

            int packetId = buffer.readInt();
            int hashSize = buffer.readInt();
            int contentSize = buffer.readInt();
            long timestamp = buffer.readLong();

            int totalPayloadLength = hashSize + contentSize;

            if (totalPayloadLength > Constants.MAX_LENGTH) {
                ctx.channel().disconnect();
                return;
            }

            DecoderUtils.validate(ctx, packetId, hashSize, contentSize, timestamp);

            if (buffer.readableBytes() < totalPayloadLength) {
                buffer.resetReaderIndex();
                return;
            }

            try {
                Carrier carrier = DecoderUtils.split(version, packetId, hashSize, contentSize, timestamp, buffer);
                objects.add(carrier);
            } catch (Exception e) {
                System.err.println("Error during packet splitting: " + e.getMessage());
                e.printStackTrace();
                ctx.channel().disconnect();
                return;
            }
        }
    }
}
