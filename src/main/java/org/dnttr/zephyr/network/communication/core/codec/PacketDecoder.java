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

    private final int requiredBytes = 4 * Type.INT.getBytes();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> objects) {
        while (buffer.readableBytes() >= requiredBytes) {
            buffer.markReaderIndex();

            int version = buffer.readInt();

            if (version != Constants.VER_1) {
                //there is no other version in fact
                ctx.channel().disconnect();
                return;
            }

            int packetId = buffer.readInt();
            int hashSize = buffer.readInt();
            int contentSize = buffer.readInt();

            int totalPacketLength = hashSize + contentSize;

            DecoderUtils.validate(ctx, packetId, hashSize, contentSize);

            if (buffer.readableBytes() < totalPacketLength) {
                buffer.resetReaderIndex();
                return;
            }

            try {
                Carrier carrier = DecoderUtils.split(version, packetId, hashSize, contentSize, buffer);

                objects.add(carrier);
            } catch (Exception e) {
                ctx.channel().disconnect();
                return;
            }
        }
    }
}
