package org.dnttr.zephyr.network.communication.api.controllers;

import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.network.bridge.Security;
import org.dnttr.zephyr.network.communication.api.ISession;
import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.communication.core.channel.ChannelController;
import org.dnttr.zephyr.network.communication.core.flow.Observer;
import org.dnttr.zephyr.network.communication.core.flow.events.channel.ConnectionEstablishedEvent;
import org.dnttr.zephyr.network.communication.core.packet.processor.Direction;
import org.dnttr.zephyr.network.protocol.Packet;
import org.dnttr.zephyr.network.protocol.packets.SessionStatePacket;
import org.dnttr.zephyr.network.protocol.packets.authorization.SessionNoncePacket;
import org.dnttr.zephyr.network.protocol.packets.authorization.SessionPrivatePacket;
import org.dnttr.zephyr.network.protocol.packets.authorization.SessionPublicPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static org.dnttr.zephyr.network.bridge.Security.EncryptionMode.SYMMETRIC;

/**
 * ClientChannelController is responsible for managing the client-side channel operations.
 * It extends the ChannelController and initializes with specific session packets.
 *
 * @author dnttr
 */

public final class ClientChannelController extends ChannelController {

    public ClientChannelController(ISession session, EventBus eventBus) {
        super(session, eventBus);

        this.addPackets(SessionStatePacket.class, SessionPrivatePacket.class, SessionPublicPacket.class, SessionNoncePacket.class);

        this.getEventBus().register(this);
    }

    @Override
    public void fireActive(@NotNull ChannelContext context) {
        this.getEventBus().call(new ConnectionEstablishedEvent(context));

        Observer auth = this.getObserverManager().observe(SessionPublicPacket.class, Direction.INBOUND, context);
        auth.thenAccept(msg0 -> {
            SessionPublicPacket serverAuthKey = (SessionPublicPacket) msg0;

            Security.generateKeys(context.getUuid(), Security.EncryptionMode.ASYMMETRIC);
            Security.setPartnerPublicKey(context.getUuid(), serverAuthKey.getPublicKey());

            Optional<byte[]> clientAuthKey = Security.getKeyPair(context.getUuid(), Security.KeyType.PUBLIC);

            if (clientAuthKey.isEmpty()) {
                context.restrict("Unable to get public key for auth.");
                return;
            }

            SessionPublicPacket clientAuthKeyPacket = new SessionPublicPacket(clientAuthKey.get());
            context.getChannel().writeAndFlush(clientAuthKeyPacket);

            Observer hash = this.getObserverManager().observe(SessionPublicPacket.class, Direction.INBOUND, context);

            hash.thenAccept(msg1 -> {
                SessionPublicPacket serverHashKey = (SessionPublicPacket) msg1;
                Security.setSigningPublicKey(context.getUuid(), serverHashKey.getPublicKey());

                Optional<byte[]> clientKey = Security.getBaseSigningKey(context.getUuid());

                if (clientKey.isEmpty()) {
                    context.restrict("Unable to get public key for signing.");

                    return;
                }

                Security.deriveSigningKeyPair(context.getUuid(), Security.SideType.CLIENT);
                Security.finalizeSigningKeyPair(context.getUuid(), Security.SideType.CLIENT);

                Observer otx = this.getObserverManager().observe(SessionPublicPacket.class, Direction.OUTBOUND, context);
                otx.thenAccept(_ -> {
                    context.setHash(true);

                    Observer noncePacket = this.getObserverManager().observe(SessionNoncePacket.class, Direction.INBOUND, context);
                    noncePacket.thenAccept(packet -> {
                        SessionNoncePacket sessionNoncePacket = (SessionNoncePacket) packet;

                        Security.setNonce(context.getUuid(), Security.EncryptionMode.ASYMMETRIC,  sessionNoncePacket.getNonce());

                        Observer ott =  this.getObserverManager().observe(SessionPrivatePacket.class, Direction.INBOUND, context);
                        ott.thenAccept(msg4 -> {
                            SessionPrivatePacket sessionPrivatePacket = (SessionPrivatePacket) msg4;
                            Security.processKeyExchange(context.getUuid(), sessionPrivatePacket.getKey());

                            context.setEncryptionType(SYMMETRIC);
                            Observer obx = this.getObserverManager().observe(SessionStatePacket.class, Direction.OUTBOUND, context);
                            obx.thenAccept(msg5 -> context.setReady(true));

                            context.getChannel().writeAndFlush(new SessionStatePacket(SessionStatePacket.State.REGISTER_FINISH.getValue()));
                        });

                        context.getChannel().writeAndFlush(new SessionStatePacket(SessionStatePacket.State.REGISTER_EXCHANGE.getValue()));
                    });
                });

                context.getChannel().writeAndFlush(new SessionPublicPacket(clientKey.get()));
            });
        });

        SessionStatePacket packet = new SessionStatePacket(SessionStatePacket.State.REGISTER_REQUEST.getValue());
        context.getChannel().writeAndFlush(packet);

        super.fireActive(context);
    }

    @Override
    public void fireRead(@NotNull ChannelContext context, @NotNull Packet msg) {
        super.fireRead(context, msg);
    }
}