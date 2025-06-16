package org.dnttr.zephyr.network.communication.core.channel;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.network.communication.core.flow.events.packet.PacketInboundEvent;
import org.dnttr.zephyr.network.communication.core.flow.events.packet.PacketOutboundEvent;
import org.dnttr.zephyr.network.communication.core.flow.events.session.SessionRestrictedEvent;
import org.dnttr.zephyr.network.communication.core.flow.events.session.SessionTerminatedEvent;
import org.dnttr.zephyr.network.communication.core.managers.ObserverManager;
import org.dnttr.zephyr.network.communication.core.packet.processor.Transformer;
import org.dnttr.zephyr.network.protocol.Data;
import org.dnttr.zephyr.network.protocol.Packet;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author dnttr
 */

@ApiStatus.OverrideOnly
@RequiredArgsConstructor
public class ChannelController {

    @Getter(AccessLevel.PROTECTED)
    private final EventBus eventBus;

    @Getter(AccessLevel.PROTECTED)
    private final ObserverManager observerManager;

    @Getter(AccessLevel.PACKAGE)
    private final Transformer transformer;

    public final void addPackets(@NotNull List<Class<? extends Packet>> packetClasses) {
        packetClasses.stream().filter(klass -> klass.isAnnotationPresent(Data.class)).forEach(klass -> {
            Data data = klass.getDeclaredAnnotation(Data.class);

            this.getTransformer().getPackets().put(data.identity(), klass);
        });
    }

    public void fireRead(@NotNull ChannelContext context, @NotNull Packet msg) {
        if (!context.isReady()) {
            return;
        }

        this.eventBus.call(new PacketInboundEvent(context.getConsumer(), msg));
    }

    public void fireReadComplete(@NotNull ChannelContext context) {
        if (!context.isReady()) {
            return;
        }
    }

    public void fireActive(@NotNull ChannelContext context) {
        if (!context.isReady()) {
            return;
        }
    }

    public void fireInactive(@NotNull ChannelContext context) {
        if (!context.isReady()) {
            return;
        }

        this.eventBus.call(new SessionTerminatedEvent(context.getConsumer()));
    }

    public void fireWrite(@NotNull ChannelContext context, @NotNull Packet msg) {
        if (!context.isReady()) {
            return;
        }
    }

    public void fireWriteComplete(@NotNull ChannelContext context, @NotNull Packet msg) {
        if (!context.isReady()) {
            return;
        }

        this.eventBus.call(new PacketOutboundEvent(context.getConsumer(), msg));
    }

    public void fireRestriction(@NotNull ChannelContext context, String reason) {
        if (!context.isReady()) {
            return;
        }

        this.eventBus.call(new SessionRestrictedEvent(context.getConsumer(), reason));
    }
}
