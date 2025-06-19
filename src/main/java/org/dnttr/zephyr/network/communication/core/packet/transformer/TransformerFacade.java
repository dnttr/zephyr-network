package org.dnttr.zephyr.network.communication.core.packet.transformer;

import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.communication.core.packet.Carrier;
import org.dnttr.zephyr.network.communication.core.packet.Direction;
import org.dnttr.zephyr.network.communication.core.packet.transformer.impl.InboundTransformer;
import org.dnttr.zephyr.network.communication.core.packet.transformer.impl.OutboundTransformer;
import org.dnttr.zephyr.network.protocol.Packet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Acts as a facade for the packet transformation subsystem, providing a single entry point
 * for processing both inbound and outbound messages. It delegates the actual transformation
 * logic to specialized {@link InboundTransformer} and {@link OutboundTransformer} instances
 * based on the specified message direction.
 *
 * @author dnttr
 */
public class TransformerFacade {

    private final InboundTransformer inboundTransformer;
    private final OutboundTransformer outboundTransformer;

    /**
     * Constructs a new TransformerFacade, initializing the underlying
     * inbound and outbound transformers.
     */
    public TransformerFacade() {
        this.inboundTransformer = new InboundTransformer();
        this.outboundTransformer = new OutboundTransformer();
    }

    /**
     * Transforms a message by delegating it to the appropriate directional transformer.
     *
     * @param direction The direction of the message flow (INBOUND or OUTBOUND).
     * @param message   The message to transform, which must be a {@link Carrier} for INBOUND
     *                  or a {@link Packet} for OUTBOUND.
     * @param context   The channel context associated with the message.
     * @return The transformed object (e.g., a {@link Packet} for inbound, a {@link Carrier} for outbound),
     *         or {@code null} if transformation fails.
     * @throws Exception if an error occurs during the transformation process.
     * @throws IllegalArgumentException if the message type does not match the specified direction.
     */
    public @Nullable Object transform(@NotNull Direction direction, @NotNull Object message, @NotNull ChannelContext context) throws Exception {
        Objects.requireNonNull(direction);
        Objects.requireNonNull(message);
        Objects.requireNonNull(context);

        return switch (direction) {
            case INBOUND -> {
                if (message instanceof Carrier carrier) {
                    yield this.inboundTransformer.transform(direction, carrier, context);
                }

                throw new IllegalArgumentException("Inbound processing requires a Carrier, but received: " + message.getClass().getSimpleName());
            }
            case OUTBOUND -> {
                if (message instanceof Packet packet) {
                    yield this.outboundTransformer.transform(direction, packet, context);
                }

                throw new IllegalArgumentException("Outbound processing requires a Packet, but received: " + message.getClass().getSimpleName());
            }
        };
    }
}
