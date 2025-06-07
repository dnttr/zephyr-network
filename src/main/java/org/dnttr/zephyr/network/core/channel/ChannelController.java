package org.dnttr.zephyr.network.core.channel;

import org.dnttr.zephyr.network.api.ISession;
import org.dnttr.zephyr.network.core.packet.Data;
import org.dnttr.zephyr.protocol.packet.Packet;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * @author dnttr
 */

@ApiStatus.OverrideOnly
public final class ChannelController {

    private final ISession session;

    public ChannelController(ISession session) {
        this.session = session;

        //todo: add processor
    }

    @SafeVarargs
    public final void addPackets(@NotNull Class<? extends Packet>... packets) {
        Arrays.stream(packets).takeWhile(packet -> packet.isAnnotationPresent(Data.class)).forEach(packet -> {
            Data data = packet.getAnnotation(Data.class);

            //todo: add em to processor
        });
    }

    public void fireRead(@NotNull ChannelContext context, @NotNull Packet msg) {
        if (!context.isReady()) {
            return;
        }

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
