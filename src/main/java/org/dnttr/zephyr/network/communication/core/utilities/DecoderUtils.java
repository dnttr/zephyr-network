package org.dnttr.zephyr.network.communication.core.utilities;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import org.dnttr.zephyr.network.communication.core.packet.Carrier;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author dnttr
 */

public final class DecoderUtils {

    /**
     * Validates packet data based on size constraints.
     *
     * @param ctx         The channel handler context for potential disconnection
     * @param packetId    The packet identifier
     * @param hashSize    Size of the hash data, must be positive except for special packets
     * @param contentSize Size of the content data, must be positive
     * @param timestamp   The time at which packet was sent
     * @throws NullPointerException if ctx is null
     */
    public static void validate(@NotNull ChannelHandlerContext ctx, int packetId, int hashSize, int contentSize, long timestamp) {
        Objects.requireNonNull(ctx);

        if (timestamp <= 0) {
            ctx.channel().disconnect();
            return;
        }

        if (contentSize <= 0 || hashSize < 0) {
            ctx.channel().disconnect();
            return;
        }

        if (hashSize == 0) {
            if (packetId == -1 || packetId == -2) {
                return;
            }

            ctx.channel().disconnect();
        }
    }

    /**
     * Splits a ByteBuf into hash and content components to create a Carrier.
     * <p>
     * IMPORTANT: The reading order matters - hash data must be read from the buffer before
     * content data to match the protocol's data layout.
     *
     * @param version Protocol version
     * @param packetId The packet identifier
     * @param hashSize Size of the hash data in bytes
     * @param contentSize Size of the content data in bytes
     * @param buffer ByteBuf containing the raw packet data
     * @return A new Carrier containing the extracted components
     * @throws NullPointerException if buffer is null
     */
    public static @NotNull Carrier split(int version, int packetId, int hashSize, int contentSize, long timestamp, @NotNull ByteBuf buffer) {
        Objects.requireNonNull(buffer);

        byte[] hashData = null;

        if (hashSize > 0) {
            ByteBuf temp = buffer.readBytes(hashSize);
            hashData = ByteBufUtil.getBytes(temp);
        }

        byte[] contentData = ByteBufUtil.getBytes(buffer.readBytes(contentSize));
        return new Carrier(version, packetId, hashSize, contentSize, timestamp, hashData, contentData);
    }
}
