package org.dnttr.zephyr.network.communication.core.channel;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
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
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Optional;

/**
 * @author dnttr
 */

@RequiredArgsConstructor
public final class ChannelHandler extends ChannelAdapter<Packet, Carrier> {

    private final AsyncCache<ByteBuffer, Boolean> nonces = Caffeine
            .newBuilder()
            .expireAfterWrite(Duration.ofSeconds(25))
            .buildAsync();

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

                if (this.isNoncePresent(nonce)) {
                    context.restrict("Such nonce is already in use.");
                    return;
                }

                this.recordNonce(nonce);

                Security.setNonce(context.getUuid(), Security.EncryptionMode.SYMMETRIC, nonce);
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
    protected @Nullable Carrier write(ChannelContext context, Packet input) throws Exception {
        if (context.getEncryptionType() == Security.EncryptionMode.SYMMETRIC) {
            boolean isNonce = input instanceof SessionNoncePacket;

            if (!isNonce) {
                Security.buildNonce(context.getUuid(), Security.EncryptionMode.SYMMETRIC);
                Optional<byte[]> nonceOpt = Security.getNonce(context.getUuid(), Security.EncryptionMode.SYMMETRIC);

                if (nonceOpt.isPresent()) {
                    byte[] nonce = nonceOpt.get();

                    if (this.isNoncePresent(nonce)) {
                        context.restrict("Such nonce is already in use.");
                        return null;
                    }

                    SessionNoncePacket noncePacket = new SessionNoncePacket(Security.EncryptionMode.SYMMETRIC.getValue(), nonce);
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

    private void recordNonce(byte[] nonce) {
        ByteBuffer key = ByteBuffer.wrap(nonce.clone());

        this.nonces.synchronous().put(key, Boolean.TRUE);
    }

    private boolean isNoncePresent(byte[] nonce) {
        ByteBuffer key = ByteBuffer.wrap(nonce);

        return nonces.synchronous().getIfPresent(key) != null;
    }
}
