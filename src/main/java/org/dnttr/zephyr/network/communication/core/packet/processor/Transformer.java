package org.dnttr.zephyr.network.communication.core.packet.processor;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import org.dnttr.zephyr.network.bridge.ZEKit;
import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.communication.core.packet.Carrier;
import org.dnttr.zephyr.network.communication.core.packet.processor.impl.SecureProcessor;
import org.dnttr.zephyr.network.communication.core.packet.processor.impl.StandardProcessor;
import org.dnttr.zephyr.network.communication.core.utilities.PacketUtils;
import org.dnttr.zephyr.network.protocol.Packet;
import org.dnttr.zephyr.serializer.Serializer;

import java.util.IdentityHashMap;

/**
 * @author dnttr
 */

public class Transformer {

    @Getter
    private final IdentityHashMap<Integer, Class<?>> packets;

    private final SecureProcessor secureProcessor;
    private final StandardProcessor standardProcessor;

    public Transformer() {
        this.packets = new IdentityHashMap<>();

        this.secureProcessor = new SecureProcessor();
        this.standardProcessor = new StandardProcessor();
    }

    public Object transform(Direction direction, Object message, ChannelContext context) throws Exception {
        IProcessor processor;

        switch (context.getEncryptionType()) {
            case NONE -> processor = this.standardProcessor;
            case ASYMMETRIC, SYMMETRIC -> processor = this.secureProcessor;
            default -> throw new IllegalArgumentException("Unrecognized cipher type: " + context.getEncryptionType());
        }

        switch (direction) {
            case INBOUND -> {
                if (message instanceof Carrier carrier) {
                    ByteBuf content;

                    if (carrier.hashSize() != 0) {
                        var buffer = PacketUtils.decompose(carrier, carrier.hashSize());

                        if (buffer == null) {
                            return null;
                        }

                        content = buffer.value();

                        if (content == null) {
                            context.restrict();
                            return null;
                        }
                    } else {
                        content = carrier.buffer();
                    }

                    byte[] bytes = ByteBufUtil.getBytes(content);

                    ByteBuf result = processor.processInbound(carrier, context, bytes);

                    var klass = this.packets.get(carrier.identity());

                    if (klass == null) {
                        return null;
                    }

                    return Serializer.deserializeUsingBuffer(klass, result);
                } else {
                    throw new IllegalArgumentException("Inbound processing requires a Carrier message, but received: " + message.getClass().getSimpleName());
                }
            }

            case OUTBOUND -> {
                if (message instanceof Packet packet) {
                    ByteBuf content = Serializer.serializeToBuffer(packet.getClass(), packet);
                    byte[] bytes = ByteBufUtil.getBytes(content);

                    ByteBuf result = processor.processOutbound(packet, context, bytes);

                    int hashSize = 0, contentSize = result.readableBytes();

                    int versionId = packet.getData().protocol();
                    int packetId = packet.getData().identity();

                    if (context.isHash()) {
                        byte[] contentBytes = ByteBufUtil.getBytes(result);

                        byte[] hashOut = ZEKit.ffi_ze_build_hash_sh0(context.getUuid(), contentBytes);
                        hashSize = hashOut.length;
                        ByteBuf hash = Unpooled.buffer(hashSize);
                        hash.writeBytes(hashOut);

                        ByteBuf buffer = Unpooled.buffer();
                        buffer.writeBytes(contentBytes);

                        contentSize = contentBytes.length;

                        return new Carrier(versionId, packetId, hashSize, contentSize, hash, buffer);
                    }

                    ByteBuf buffer = Unpooled.buffer();

                    buffer.writeBytes(result);

                    content.release();

                    return new Carrier(versionId, packetId, hashSize, contentSize, null, buffer);
                } else {
                    throw new IllegalArgumentException("Outbound processing requires a Packet type, but received: " + message.getClass().getSimpleName());
                }
            }
            default -> throw new IllegalArgumentException("Unknown target type: " + direction);
        }
    }
}
