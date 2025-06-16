package org.dnttr.zephyr.network.communication.core.packet.processor;

import io.netty.buffer.Unpooled;
import lombok.Getter;
import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.communication.core.packet.Carrier;
import org.dnttr.zephyr.network.communication.core.packet.processor.impl.SecureProcessor;
import org.dnttr.zephyr.network.communication.core.packet.processor.impl.StandardProcessor;
import org.dnttr.zephyr.network.protocol.Packet;
import org.dnttr.zephyr.serializer.Serializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Objects;

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

    public @Nullable Object transform(@NotNull Direction direction, @NotNull Object message, @NotNull ChannelContext context) throws Exception {
        Objects.requireNonNull(direction);
        Objects.requireNonNull(message);
        Objects.requireNonNull(context);

        var type = context.getEncryptionType();
        IProcessor processor;

        switch (type) {
            case NONE ->
                    processor = this.standardProcessor;
            case ASYMMETRIC, SYMMETRIC ->
                    processor = this.secureProcessor;
            default ->
                    throw new IllegalArgumentException("Unrecognized cipher type: " + type);
        }

        switch (direction) {
            case INBOUND -> {
                if (message instanceof Carrier carrier) {
                    int packetId = carrier.identity();
                    byte[] result = carrier.content();

                    if (carrier.hashSize() != 0 && context.isHash()) {
                        long timestamp = carrier.timestamp();

                        boolean isVerified = this.integrity.verify(context, timestamp, carrier);

                        if (!isVerified) {
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

                    long timestamp = System.currentTimeMillis();

                    if (packetId != -0x3) {
                        processedPacket = processor.processOutbound(context, serializedPacket);
                    } else {
                        processedPacket = serializedPacket;
                    }

                    if (processedPacket == null) {
                        return null;
                    }

                    byte[] computedHash = this.integrity.build(context, timestamp, processedPacket);

                    if (computedHash == null) {
                        return null;
                    }

                    return new Carrier(versionId, packetId, computedHash.length, processedPacket.length, timestamp, computedHash, processedPacket);
                } else {
                    throw new IllegalArgumentException("Outbound processing requires a Packet type, but received: " + message.getClass().getSimpleName());
                }
            }

            default -> throw new IllegalArgumentException("Unknown target type: " + direction);
        }
    }
}
