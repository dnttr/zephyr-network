package org.dnttr.zephyr.network.communication.core.channel;

import lombok.RequiredArgsConstructor;
import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.network.communication.core.flow.events.packet.PacketReceivedEvent;
import org.dnttr.zephyr.network.communication.core.flow.events.packet.PacketSentEvent;
import org.dnttr.zephyr.network.protocol.Packet;
import org.dnttr.zephyr.network.communication.core.packet.Carrier;
import org.dnttr.zephyr.network.communication.core.packet.processor.Direction;
import org.dnttr.zephyr.network.communication.core.utilities.PacketUtils;

/**
 * @author dnttr
 */

@RequiredArgsConstructor
public final class ChannelHandler extends ChannelAdapter<Packet, Carrier> {

    private final EventBus eventBus;
    private final ChannelController controller;

    @Override
    protected void channelRead(ChannelContext context, Carrier input) throws Exception {
        Packet packet = (Packet) this.controller.getTransformer().transform(Direction.INBOUND, input, context);

        if (packet == null) {
            context.restrict();
            return;
        }

        this.eventBus.call(new PacketReceivedEvent(packet, context));

        this.controller.fireRead(context, packet);
    }

    @Override
    protected void channelReadComplete(ChannelContext context) throws Exception {
        this.controller.fireReadComplete(context);
    }

    @Override
    protected void channelActive(ChannelContext context) throws Exception {
        this.controller.fireActive(context);
    }

    @Override
    protected void channelInactive(ChannelContext context) throws Exception {
        if (context.isRestricted()) {
            this.controller.fireRestriction(context);
        }

        this.controller.fireInactive(context);
    }

    @Override
    protected Carrier write(ChannelContext context, Packet input) throws Exception {
        if (!PacketUtils.isReserved(input.getData().identity())) {
            this.controller.fireWrite(context, input);
        }

        Carrier carrier = (Carrier) this.controller.getTransformer().transform(Direction.OUTBOUND, input, context);
        this.eventBus.call(new PacketSentEvent(input, context));

        return carrier;
    }

    @Override
    protected void onWriteComplete(ChannelContext context, Packet input, Carrier output) {
        if (PacketUtils.isReserved(input.getData().identity())) {
            return;
        }

        this.controller.fireWriteComplete(context, input);
    }
}
