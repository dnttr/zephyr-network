package org.dnttr.zephyr.network.communication.core.channel;

import org.dnttr.zephyr.bridge.Security;
import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.network.communication.core.flow.events.internal.observer.ObserverInboundPacketEvent;
import org.dnttr.zephyr.network.communication.core.flow.events.internal.observer.ObserverOutboundPacketEvent;
import org.dnttr.zephyr.network.communication.core.packet.Carrier;
import org.dnttr.zephyr.network.communication.core.packet.Direction;
import org.dnttr.zephyr.network.communication.core.utilities.PacketUtils;
import org.dnttr.zephyr.network.protocol.Packet;
import org.dnttr.zephyr.network.protocol.packets.internal.authorization.ConnectionNoncePacket;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

/**
 * @author dnttr
 */

public final class ChannelHandler extends ChannelAdapter<Packet, Carrier> {

    private final ChannelController controller;
    private final EventBus eventBus;

    public ChannelHandler(ChannelController controller, EventBus eventBus, boolean timeout) {
        super(timeout);
        this.controller = controller;
        this.eventBus = eventBus;
    }

    @Override
    protected void channelRead(ChannelContext context, Carrier input) throws Exception {
        Packet packet = (Packet) this.controller.getTransformerFacade().transform(Direction.INBOUND, input, context);

        if (packet == null) {
            context.restrict("Unable to transform packet.");
            return;
        }

        if (context.getEncryptionType() == Security.EncryptionMode.SYMMETRIC) {
            if (packet instanceof ConnectionNoncePacket noncePacket) {
                byte[] nonce = noncePacket.getNonce();

                if (this.isNoncePresent(context, nonce)) {
                    context.restrict("Such nonce is already in use.");
                    return;
                }

                this.recordNonce(context, nonce);

                Security.setNonce(context.getUuid(), Security.EncryptionMode.SYMMETRIC, nonce);

            }
        }

        if (input.identity() == -12) {
            return;
        }

        this.controller.fireRead(context, packet);
        this.eventBus.call(new ObserverInboundPacketEvent(packet, context));
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
            this.controller.fireRestriction(context, context.getRestrictionReason());
        }

        this.controller.fireInactive(context);
    }

    @Override
    protected @Nullable Carrier write(ChannelContext context, Packet input) throws Exception {
        if (context.getEncryptionType() == Security.EncryptionMode.SYMMETRIC) {
            boolean isNonce = input instanceof ConnectionNoncePacket;

            if (!isNonce) {
                Security.buildNonce(context.getUuid(), Security.EncryptionMode.SYMMETRIC);
                var nonceOpt = Security.getNonce(context.getUuid(), Security.EncryptionMode.SYMMETRIC);

                if (nonceOpt.isPresent()) {
                    byte[] nonce = nonceOpt.get();

                    if (this.isNoncePresent(context, nonce)) {
                        context.restrict("Such nonce is already in use.");
                        return null;
                    }

                    ConnectionNoncePacket noncePacket = new ConnectionNoncePacket(Security.EncryptionMode.SYMMETRIC.getValue(), nonce);
                    context.getChannel().writeAndFlush(noncePacket);
                } else {
                    context.restrict("Unable to get nonce.");
                }
            }
        }

        this.controller.fireWrite(context, input);

        Carrier carrier = (Carrier) this.controller.getTransformerFacade().transform(Direction.OUTBOUND, input, context);
        this.eventBus.call(new ObserverOutboundPacketEvent(input, context));

        return carrier;
    }

    @Override
    protected void onWriteComplete(ChannelContext context, Packet input, Carrier output) {
        if (PacketUtils.isReserved(input.getData().identity())) {
            return;
        }

        this.controller.fireWriteComplete(context, input);
    }

    private void recordNonce(ChannelContext context, byte[] nonce) {
        var key = ByteBuffer.wrap(nonce.clone());

        context.getNonces().synchronous().put(key, Boolean.TRUE);
    }

    private boolean isNoncePresent(ChannelContext context, byte[] nonce) {
        var key = ByteBuffer.wrap(nonce);

        return context.getNonces().synchronous().getIfPresent(key) != null;
    }
}
