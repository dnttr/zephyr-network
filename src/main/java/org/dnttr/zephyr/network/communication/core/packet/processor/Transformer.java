package org.dnttr.zephyr.network.communication.core.packet.processor;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.communication.core.packet.Carrier;
import org.dnttr.zephyr.network.communication.core.packet.Packet;
import org.dnttr.zephyr.network.communication.core.packet.processor.impl.SecureProcessor;
import org.dnttr.zephyr.network.communication.core.packet.processor.impl.StandardProcessor;
import org.dnttr.zephyr.network.communication.core.security.JSecurity;
import org.dnttr.zephyr.network.communication.core.utilities.PacketUtils;
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

    private final JSecurity security;

    public Transformer() {
        this.packets = new IdentityHashMap<>();

        this.secureProcessor = new SecureProcessor();
        this.standardProcessor = new StandardProcessor();

        this.security = new JSecurity();
    }

    public Object transform(Type target, Object message, ChannelContext context) throws Exception {
        final boolean isProviderAvailable = this.security.isIntegrityProviderAvailable(context.getSecret());

        IProcessor processor;

        switch (context.getEncryptionType()) {
            case NONE -> processor = this.standardProcessor;
            case ASYMMETRIC, SYMMETRIC -> processor = this.secureProcessor;
            default -> throw new IllegalArgumentException("Unrecognized encryption type: " + context.getEncryptionType());
        }

        switch (target) {
            case INBOUND -> {
                if (message instanceof Carrier carrier) {
                    ByteBuf content = carrier.buffer();

                    if (isProviderAvailable) {
                        var buffer = PacketUtils.decompose(carrier, carrier.hashSize());

                        if (buffer == null) {
                            return null;
                        }

                        if (!this.security.isIntegrityPreserved(context.getSecret(), buffer)) {
                            return null;
                        }

                        content = buffer.value();
                    } else if (carrier.hashSize() > 0) {
                        return null;
                    }

                    if (content == null) {
                        context.restrict();
                        return null;
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
                    ByteBuf buffer = Unpooled.buffer();

                    if (isProviderAvailable) {
                        byte[] mark = this.security.getMark(context.getSecret(), result);

                        buffer.writeBytes(mark);
                        hashSize = mark.length;
                    }

                    buffer.writeBytes(result);
                    content.release();

                    int versionId = packet.getData().protocol();
                    int packetId = packet.getData().identity();

                    return new Carrier(versionId, packetId, hashSize, contentSize, buffer);
                } else {
                    throw new IllegalArgumentException("Outbound processing requires a Packet type, but received: " + message.getClass().getSimpleName());
                }
            }
            default -> throw new IllegalArgumentException("Unknown target type: " + target);
        }
    }

    public enum Type {
        INBOUND,
        OUTBOUND
    }
}
