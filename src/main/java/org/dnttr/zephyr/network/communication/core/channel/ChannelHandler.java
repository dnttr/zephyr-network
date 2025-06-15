package org.dnttr.zephyr.network.communication.core.channel;

import lombok.RequiredArgsConstructor;
import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.network.bridge.Security;
import org.dnttr.zephyr.network.communication.core.flow.events.packet.PacketReceivedEvent;
import org.dnttr.zephyr.network.communication.core.flow.events.packet.PacketSentEvent;
import org.dnttr.zephyr.network.communication.core.packet.Carrier;
import org.dnttr.zephyr.network.communication.core.packet.processor.Direction;
import org.dnttr.zephyr.network.communication.core.utilities.PacketUtils;
import org.dnttr.zephyr.network.protocol.Packet;
import org.dnttr.zephyr.network.protocol.packets.authorization.SessionNoncePacket;

import java.util.Optional;

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

        if (context.getEncryptionType() == Security.EncryptionMode.SYMMETRIC) {
            if (packet instanceof SessionNoncePacket noncePacket) {
                Security.setNonce(context.getUuid(), Security.EncryptionMode.SYMMETRIC, noncePacket.getNonce());
            }
        }

        this.controller.fireRead(context, packet);
        this.eventBus.call(new PacketReceivedEvent(packet, context));
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
        if (context.getEncryptionType() == Security.EncryptionMode.SYMMETRIC) {
            boolean isNonce = input instanceof SessionNoncePacket;

            if (!isNonce) {
                Security.buildNonce(context.getUuid(), Security.EncryptionMode.SYMMETRIC);
                Optional<byte[]> nonce = Security.getNonce(context.getUuid(), Security.EncryptionMode.SYMMETRIC);

                if (nonce.isPresent()) {
                    SessionNoncePacket noncePacket = new SessionNoncePacket(Security.EncryptionMode.SYMMETRIC.getValue(), nonce.get());
                    context.getChannel().writeAndFlush(noncePacket);
                }
            }
        }

        this.controller.fireWrite(context, input);

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
