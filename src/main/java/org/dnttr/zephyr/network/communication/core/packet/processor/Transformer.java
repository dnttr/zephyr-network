package org.dnttr.zephyr.network.communication.core.packet.processor;

import io.netty.buffer.Unpooled;
import lombok.Getter;
import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.communication.core.packet.Carrier;
import org.dnttr.zephyr.network.communication.core.packet.processor.impl.SecureProcessor;
import org.dnttr.zephyr.network.communication.core.packet.processor.impl.StandardProcessor;
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

    private final Integrity integrity;

    public Transformer() {
        this.packets = new IdentityHashMap<>();

        this.secureProcessor = new SecureProcessor();
        this.standardProcessor = new StandardProcessor();

        this.integrity = new Integrity();
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
                    int packetId = carrier.identity();
                    byte[] result = carrier.content();

                    if (carrier.hashSize() != 0 && context.isHash()) {
                        if (!this.integrity.verify(context, carrier)) {
                            return null;
                        }
                    }

                    if (packetId != -0x3) {
                        result = processor.processInbound(context, carrier.content());

                        if (result == null) {
                            return null;
                        }
                    }

                    var klass = this.packets.get(carrier.identity());

                    if (result == null || klass == null) {
                        return null;
                    }

                    return Serializer.deserializeUsingBuffer(klass, Unpooled.wrappedBuffer(result));
                } else {
                    throw new IllegalArgumentException("Inbound processing requires a Carrier message, but received: " + message.getClass().getSimpleName());
                }
            }

            case OUTBOUND -> {
                if (message instanceof Packet packet) {
                    byte[] serializedPacket = Serializer.serializeToArray(packet.getClass(), packet);
                    byte[] processedPacket;

                    int versionId = packet.getData().protocol();
                    int packetId = packet.getData().identity();

                    if (packetId != -0x3) {
                        processedPacket = processor.processOutbound(context, serializedPacket);
                    } else {
                        processedPacket = serializedPacket;
                    }

                    if (processedPacket == null) {
                        return null;
                    }

                    byte[] computedHash = this.integrity.build(context, processedPacket);

                    return new Carrier(versionId, packetId, computedHash.length, processedPacket.length, computedHash, processedPacket);
                } else {
                    throw new IllegalArgumentException("Outbound processing requires a Packet type, but received: " + message.getClass().getSimpleName());
                }
            }
            default -> throw new IllegalArgumentException("Unknown target type: " + direction);
        }
    }
}
