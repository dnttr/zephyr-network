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
import java.util.*;

/**
 * Handles the transformation of messages between network-ready {@link Carrier} objects
 * and application-level {@link Packet} objects. It manages serialization, deserialization,
 * encryption, and decryption based on the current channel context.
 *
 * @author dnttr
 * @since 1.0.0
 */

public class Transformer {

    @Getter
    private final HashMap<Integer, Class<?>> packets;

    private final SecureProcessor secureProcessor;
    private final StandardProcessor standardProcessor;

    private final Integrity integrity;

    /**
     * Defines the time window in seconds for which a packet timestamp is considered valid.
     * This is used to mitigate simple replay attacks by rejecting packets that are too old.
     */
    private static final int CACHE_EXPIRATION_TIME = 6;

    /**
     * A set of packet identifiers that should be exempt from the standard encryption/decryption process.
     * This is necessary for handshake packets like nonce exchanges that must be sent in plaintext.
     */
    private static final Set<Integer> ENCRYPTION_EXEMPT_IDS = Set.of(-0x3);

    /**
     * Constructs a new Transformer, initializing packet mappings, processors, and integrity handlers.
     */
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

    /**
     * Main transformation method that processes inbound and outbound messages.
     *
     * @param direction The direction of the message flow (INBOUND or OUTBOUND).
     * @param message   The message to transform, either a {@link Carrier} for inbound or a {@link Packet} for outbound.
     * @param context   The channel context, containing session state like encryption settings.
     * @return A transformed object, e.g., a {@link Packet} for inbound or a {@link Carrier} for outbound, or {@code null} on failure.
     * @throws Exception if an error occurs during processing.
     */
    public @Nullable Object transform(@NotNull Direction direction, @NotNull Object message, @NotNull ChannelContext context) throws Exception {
        Objects.requireNonNull(direction);
        Objects.requireNonNull(message);
        Objects.requireNonNull(context);

        switch (direction) {
            case INBOUND -> {
                if (message instanceof Carrier carrier) {
                    var klass = this.packets.get(carrier.identity());

                    if (klass == null) {
                        return null;
                    }

                    if (!this.isTimestampValid(carrier)) {
                        return null;
                    }

                    byte[] data = this.process(context, Direction.INBOUND, carrier);

                    if (data == null) {
                        return null;
                    }

                    return Serializer.deserializeUsingBuffer(klass, Unpooled.wrappedBuffer(data));
                } else {
                    throw new IllegalArgumentException("Inbound processing requires a Carrier message, but received: " + message.getClass().getSimpleName());
                }
            }

            case OUTBOUND -> {
                if (message instanceof Packet packet) {
                    byte[] data = this.process(context, Direction.OUTBOUND, packet);

                    if (data == null) {
                        return null;
                    }

                    long timestamp = System.currentTimeMillis();
                    byte[] hash = this.integrity.build(context, timestamp, data);

                    if (hash == null) {
                        return null;
                    }

                    int versionId = packet.getData().protocol();
                    int packetId = packet.getData().identity();

                    return new Carrier(versionId, packetId, hash.length, data.length, timestamp, hash, data);
                } else {
                    throw new IllegalArgumentException("Outbound processing requires a Packet type, but received: " + message.getClass().getSimpleName());
                }
            }

            default -> throw new IllegalArgumentException("Unknown target type: " + direction);
        }
    }

    /**
     * Validates the timestamp of an incoming carrier to mitigate simple replay attacks.
     *
     * @param carrier The inbound carrier packet.
     * @return {@code true} if the timestamp is within the allowed time window, {@code false} otherwise.
     */
    private boolean isTimestampValid(@NotNull Carrier carrier) {
        return Math.abs(System.currentTimeMillis() - carrier.timestamp()) <= Duration.ofSeconds(CACHE_EXPIRATION_TIME).toMillis();
    }

    /**
     * Processes the core data of a message, applying encryption or decryption as needed.
     *
     * @param context   The channel context.
     * @param direction The direction of the message flow.
     * @param object    The message object, either a {@link Carrier} or a {@link Packet}.
     * @return The processed (e.g., decrypted or encrypted) byte array, or {@code null} on failure.
     * @throws Exception if an error occurs during processing.
     */
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

        byte[] buffer = null;

        switch (direction) {
            case INBOUND -> {
                Carrier carrier = (Carrier) object;

                buffer = carrier.content();

                if (carrier.hashSize() != 0 && context.isHash()) {
                    boolean isVerified = this.integrity.verify(context, carrier.timestamp(), carrier);

                    if (!isVerified) {
                        return null;
                    }
                }

                if (!ENCRYPTION_EXEMPT_IDS.contains(carrier.identity())) {
                    buffer = processor.processInbound(context, carrier.content());
                }
            }

            case OUTBOUND -> {
                Packet packet = (Packet) object;

                byte[] data = Serializer.serializeToArray(packet.getClass(), packet);

                if (!ENCRYPTION_EXEMPT_IDS.contains(packet.getData().identity())) {
                    buffer = processor.processOutbound(context, data);
                } else {
                    buffer = data;
                }
            }
        }

        return buffer;
    }
}
