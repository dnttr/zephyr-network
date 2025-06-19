package org.dnttr.zephyr.network.communication.core.channel;

import lombok.RequiredArgsConstructor;
import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.network.bridge.Security;
import org.dnttr.zephyr.network.communication.core.flow.events.internal.observer.ObserverInboundPacketEvent;
import org.dnttr.zephyr.network.communication.core.flow.events.internal.observer.ObserverOutboundPacketEvent;
import org.dnttr.zephyr.network.communication.core.packet.Carrier;
import org.dnttr.zephyr.network.communication.core.packet.processor.Direction;
import org.dnttr.zephyr.network.communication.core.utilities.PacketUtils;
import org.dnttr.zephyr.network.protocol.Packet;
import org.dnttr.zephyr.network.protocol.packets.authorization.SessionNoncePacket;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

/**
 * @author dnttr
 */

@RequiredArgsConstructor
public final class ChannelHandler extends ChannelAdapter<Packet, Carrier> {

    private final ChannelController controller;
    private final EventBus eventBus;

    @Override
    protected void channelRead(ChannelContext context, Carrier input) throws Exception {
        Packet packet = (Packet) this.controller.getTransformer().transform(Direction.INBOUND, input, context);

        if (packet == null) {
            context.restrict("Unable to transform packet.");
            return;
        }

        if (context.getEncryptionType() == Security.EncryptionMode.SYMMETRIC) {
            if (packet instanceof SessionNoncePacket noncePacket) {
                byte[] nonce = noncePacket.getNonce();

                if (this.isNoncePresent(context, nonce)) {
                    context.restrict("Such nonce is already in use.");
                    return;
                }

                this.recordNonce(context, nonce);

                Security.setNonce(context.getUuid(), Security.EncryptionMode.SYMMETRIC, nonce);

                System.out.println("ENC/INC");
                incrementNonce(context);
            }
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
            boolean isNonce = input instanceof SessionNoncePacket;

            if (!isNonce) {
                Security.buildNonce(context.getUuid(), Security.EncryptionMode.SYMMETRIC);
                var nonceOpt = Security.getNonce(context.getUuid(), Security.EncryptionMode.SYMMETRIC);

                if (nonceOpt.isPresent()) {
                    System.out.println("DEC/INC" + input);

                    byte[] nonce = nonceOpt.get();
                    incrementNonce(context);

                    if (this.isNoncePresent(context, nonce)) {
                        context.restrict("Such nonce is already in use.");
                        return null;
                    }

                    System.out.println("OK");

                    SessionNoncePacket noncePacket = new SessionNoncePacket(Security.EncryptionMode.SYMMETRIC.getValue(), nonce);
                    context.getChannel().writeAndFlush(noncePacket);
                } else {
                    context.restrict("Unable to get nonce.");
                }
            }
        }

        this.controller.fireWrite(context, input);

        Carrier carrier = (Carrier) this.controller.getTransformer().transform(Direction.OUTBOUND, input, context);

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

    private void incrementNonce(ChannelContext context) {
        Security.getNonce(context.getUuid(), Security.EncryptionMode.SYMMETRIC).ifPresent(nonce -> {
            for (int i = nonce.length - 1; i >= 0; i--) {
                nonce[i]++;
                if (nonce[i] != 0) break;
            }

            Security.setNonce(context.getUuid(), Security.EncryptionMode.SYMMETRIC, nonce);
        });
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
