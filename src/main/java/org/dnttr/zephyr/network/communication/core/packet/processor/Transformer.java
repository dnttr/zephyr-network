package org.dnttr.zephyr.network.communication.core.packet.processor;

import io.netty.buffer.Unpooled;
import lombok.Getter;
import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.communication.core.packet.Carrier;
import org.dnttr.zephyr.network.communication.core.packet.processor.impl.SecureProcessor;
import org.dnttr.zephyr.network.communication.core.packet.processor.impl.StandardProcessor;
import org.dnttr.zephyr.network.protocol.Data;
import org.dnttr.zephyr.network.protocol.Packet;
import org.dnttr.zephyr.network.protocol.packets.client.ClientAvailabilityPacket;
import org.dnttr.zephyr.network.protocol.packets.internal.ConnectionStatePacket;
import org.dnttr.zephyr.network.protocol.packets.internal.authorization.ConnectionNoncePacket;
import org.dnttr.zephyr.network.protocol.packets.internal.authorization.ConnectionPrivatePacket;
import org.dnttr.zephyr.network.protocol.packets.internal.authorization.ConnectionPublicPacket;
import org.dnttr.zephyr.serializer.Serializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * @author dnttr
 */

public class Transformer {

    @Getter
    private final HashMap<Integer, Class<?>> packets;

    private final SecureProcessor secureProcessor;
    private final StandardProcessor standardProcessor;

    private final Integrity integrity;

    public Transformer() {
        this.packets = new HashMap<>();

        List<Class<? extends Packet>> packetClasses = List.of(
                ConnectionStatePacket.class,
                ConnectionPrivatePacket.class,
                ConnectionPublicPacket.class,
                ConnectionNoncePacket.class,
                ClientAvailabilityPacket.class
        );

        packetClasses.stream().filter(klass -> klass.isAnnotationPresent(Data.class)).forEach(klass -> {
            Data data = klass.getDeclaredAnnotation(Data.class);

            this.packets.put(data.identity(), klass);
        });

        this.secureProcessor = new SecureProcessor();
        this.standardProcessor = new StandardProcessor();

        this.integrity = new Integrity();
    }

    public @Nullable Object transform(@NotNull Direction direction, @NotNull Object message, @NotNull ChannelContext context) throws Exception {
        Objects.requireNonNull(direction);
        Objects.requireNonNull(message);
        Objects.requireNonNull(context);

        switch (direction) {
            case INBOUND -> {
                if (message instanceof Carrier carrier) {
                    var klass = this.packets.get(carrier.identity());

                    if (!this.isTimestampValid(carrier)) {
                        return null;
                    }

                    byte[] result = process(context, Direction.INBOUND, carrier);

                    if (result == null) {
                        return null;
                    }

                    return Serializer.deserializeUsingBuffer(klass, Unpooled.wrappedBuffer(result));
                } else {
                    throw new IllegalArgumentException("Inbound processing requires a Carrier message, but received: " + message.getClass().getSimpleName());
                }
            }

            case OUTBOUND -> {
                if (message instanceof Packet packet) {
                    byte[] processedPacket = this.process(context, Direction.OUTBOUND, packet);

                    if  (processedPacket == null) {
                        return null;
                    }

                    int versionId = packet.getData().protocol();
                    int packetId = packet.getData().identity();
                    long timestamp = System.currentTimeMillis();


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

    private boolean isTimestampValid(@NotNull Carrier carrier) {
        return Math.abs(System.currentTimeMillis() - carrier.timestamp()) <= Duration.ofSeconds(6).toMillis();
    }

    private byte @Nullable [] process(ChannelContext context, Direction direction, Object object) throws Exception {
        IProcessor processor;

        switch (context.getEncryptionType()) {
            case NONE ->
                    processor = this.standardProcessor;
            case ASYMMETRIC, SYMMETRIC ->
                    processor = this.secureProcessor;
            default ->
                    throw new IllegalArgumentException("Unrecognized cipher type");
        }

        byte[] result = null;

        switch (direction) {
            case INBOUND -> {
                Carrier carrier = (Carrier) object;

                result = carrier.content();

                if (carrier.hashSize() != 0 && context.isHash()) {
                    boolean isVerified = this.integrity.verify(context, carrier.timestamp(), carrier);

                    if (!isVerified) {
                        return null;
                    }
                }

                if (carrier.identity() != -0x3) {
                    result = processor.processInbound(context, carrier.content());
                }
            }

            case OUTBOUND -> {
                Packet packet = (Packet) object;

                byte[] serializedPacket = Serializer.serializeToArray(packet.getClass(), packet);

                if (packet.getData().identity() != -0x3) {
                    result = processor.processOutbound(context, serializedPacket);
                } else {
                    result = serializedPacket;
                }
            }
        }

        return result;
    }
}
