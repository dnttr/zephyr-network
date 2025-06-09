package org.dnttr.zephyr.network.communication.core.channel;

import lombok.AccessLevel;
import lombok.Getter;
import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.network.communication.api.ISession;
import org.dnttr.zephyr.network.communication.core.flow.events.packet.PacketReceivedEvent;
import org.dnttr.zephyr.network.communication.core.flow.events.packet.PacketSentEvent;
import org.dnttr.zephyr.network.communication.core.managers.ObserverManager;
import org.dnttr.zephyr.network.protocol.Packet;
import org.dnttr.zephyr.network.protocol.Data;
import org.dnttr.zephyr.network.communication.core.packet.processor.Transformer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * @author dnttr
 */

@ApiStatus.OverrideOnly
public class ChannelController {

    private final ISession session;

    @Getter(AccessLevel.PROTECTED)
    private final EventBus eventBus;

    @Getter(AccessLevel.PROTECTED)
    private final ObserverManager observerManager;

    @Getter(AccessLevel.PACKAGE)
    private final Transformer transformer;

    public ChannelController(ISession session, EventBus eventBus) {
        this.session = session;
        this.eventBus = eventBus;
        this.observerManager = new ObserverManager(eventBus);

        this.transformer = new Transformer();
    }

    @SafeVarargs
    public final void addPackets(@NotNull Class<? extends Packet>... packets) {
        Arrays.stream(packets).takeWhile(packet -> packet.isAnnotationPresent(Data.class)).forEach(packet -> {
            Data data = packet.getDeclaredAnnotation(Data.class);
            this.getTransformer().getPackets().put(data.identity(), packet);
        });
    }

    public void fireRead(@NotNull ChannelContext context, @NotNull Packet msg) {
        if (!context.isReady()) {
            return;
        }

        this.eventBus.call(new PacketReceivedEvent(msg, context));
        this.session.onRead(context.getConsumer(), msg);
    }

    public void fireReadComplete(@NotNull ChannelContext context) {
        if (!context.isReady()) {
            return;
        }

        this.session.onReadComplete(context.getConsumer());
    }

    public void fireActive(@NotNull ChannelContext context) {
        if (!context.isReady()) {
            return;
        }

        this.session.onActive(context.getConsumer());
    }

    public void fireInactive(@NotNull ChannelContext context) {
        if (!context.isReady()) {
            return;
        }

        this.session.onInactive(context.getConsumer());
    }

    public void fireWrite(@NotNull ChannelContext context, @NotNull Packet msg) {
        if (!context.isReady()) {
            return;
        }

        this.eventBus.call(new PacketSentEvent(msg, context));
        this.session.onWrite(context.getConsumer(), msg);
    }

    public void fireWriteComplete(@NotNull ChannelContext context, @NotNull Packet msg) {
        if (!context.isReady()) {
            return;
        }

        this.session.onWriteComplete(context.getConsumer(), msg);
    }

    public void fireRestriction(@NotNull ChannelContext context) {
        this.session.onRestriction(context.getConsumer());
    }
}
