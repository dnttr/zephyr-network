package org.dnttr.zephyr.network.communication.core.packet.transformer;

import lombok.AccessLevel;
import lombok.Getter;
import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.communication.core.packet.Direction;
import org.dnttr.zephyr.network.communication.core.packet.Integrity;
import org.dnttr.zephyr.network.communication.core.packet.processor.IProcessor;
import org.dnttr.zephyr.network.communication.core.packet.processor.impl.SecureProcessor;
import org.dnttr.zephyr.network.communication.core.packet.processor.impl.StandardProcessor;
import org.dnttr.zephyr.network.protocol.Data;
import org.dnttr.zephyr.network.protocol.Packet;
import org.dnttr.zephyr.network.protocol.packets.client.ClientAvailabilityPacket;
import org.dnttr.zephyr.network.protocol.packets.internal.ConnectionStatePacket;
import org.dnttr.zephyr.network.protocol.packets.internal.ConnectionIdentifierPacket;
import org.dnttr.zephyr.network.protocol.packets.internal.authorization.ConnectionNoncePacket;
import org.dnttr.zephyr.network.protocol.packets.internal.authorization.ConnectionPrivatePacket;
import org.dnttr.zephyr.network.protocol.packets.internal.authorization.ConnectionPublicPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

/**
 * @author dnttr
 */

public abstract class Transformer<I> {

    @Getter(AccessLevel.PROTECTED)
    private static final HashMap<Integer, Class<?>> packets;

    private static final SecureProcessor secureProcessor;
    private static final StandardProcessor standardProcessor;

    @Getter(AccessLevel.PROTECTED)
    private static final Integrity integrity;

    static {
        packets = new HashMap<>();

        List<Class<? extends Packet>> packetClasses = List.of(
                ConnectionStatePacket.class,
                ConnectionPrivatePacket.class,
                ConnectionPublicPacket.class,
                ConnectionNoncePacket.class,
                ConnectionIdentifierPacket.class,
                ClientAvailabilityPacket.class
        );

        packetClasses.stream().filter(klass -> klass.isAnnotationPresent(Data.class)).forEach(klass -> {
            Data data = klass.getDeclaredAnnotation(Data.class);

            packets.put(data.identity(), klass);
        });

        secureProcessor = new SecureProcessor();
        standardProcessor = new StandardProcessor();

        integrity = new Integrity();
    }

    public abstract @Nullable Object transform(@NotNull Direction direction, @NotNull I message, @NotNull ChannelContext context) throws Exception;

    protected abstract byte[] process(@NotNull ChannelContext context, @NotNull I message) throws Exception;

    protected static IProcessor getProcessor(@NotNull ChannelContext context) {
        switch (context.getEncryptionType()) {
            case NONE -> {
                return standardProcessor;
            }
            case ASYMMETRIC, SYMMETRIC -> {
                return secureProcessor;
            }

            default -> throw new IllegalArgumentException("Unrecognized cipher type");
        }

    }
}
