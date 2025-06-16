package org.dnttr.zephyr.network.communication.api.authorization;

import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.event.EventSubscriber;
import org.dnttr.zephyr.network.bridge.Security;
import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.communication.core.flow.Authorization;
import org.dnttr.zephyr.network.communication.core.flow.events.channel.*;
import org.dnttr.zephyr.network.communication.core.managers.ObserverManager;
import org.dnttr.zephyr.network.communication.core.packet.processor.Direction;
import org.dnttr.zephyr.network.protocol.packets.SessionStatePacket;
import org.dnttr.zephyr.network.protocol.packets.authorization.SessionNoncePacket;
import org.dnttr.zephyr.network.protocol.packets.authorization.SessionPrivatePacket;
import org.dnttr.zephyr.network.protocol.packets.authorization.SessionPublicPacket;

import static org.dnttr.zephyr.network.bridge.Security.EncryptionMode.SYMMETRIC;

/**
 * @author dnttr
 */

public class ClientAuthorization extends Authorization {

    public ClientAuthorization(EventBus bus,  ObserverManager observerManager) {
        super(bus, observerManager);
    }

    @EventSubscriber
    public void onEstablished(ConnectionEstablishedEvent event) {
        var context = event.getContext();

        context.getChannel().writeAndFlush(new SessionStatePacket(SessionStatePacket.State.REGISTER_REQUEST.getValue()));

        this.getObserverManager().observe(SessionPublicPacket.class, Direction.INBOUND, context).thenAccept(msg0 -> {
            var receivedMessage = (SessionPublicPacket) msg0;
            var responseKey = new SessionPublicPacket(this.getPublicKeyForAuth(context));

            Security.setPartnerPublicKey(context.getUuid(), receivedMessage.getPublicKey());
            context.getChannel().writeAndFlush(responseKey);

            this.getBus().call(new ConnectionInitialPublicKeyExchangedEvent(context));
        });
    }

    @EventSubscriber
    public void onInitialPublicKeyExchanged(ConnectionInitialPublicKeyExchangedEvent event) {
        var context = event.getContext();

        this.getObserverManager().observe(SessionPublicPacket.class, Direction.INBOUND, context).thenAccept(msg0 -> {
            var receivedMessage = (SessionPublicPacket) msg0;
            var baseSigningKey = Security.getBaseSigningKey(context.getUuid());

            Security.setSigningPublicKey(context.getUuid(), receivedMessage.getPublicKey());

            if (baseSigningKey.isEmpty()) {
                context.restrict("Unable to get public key for signing.");

                return;
            }

            var responseKey = new SessionPublicPacket(baseSigningKey.get());

            Security.deriveSigningKeyPair(context.getUuid(), Security.SideType.CLIENT);
            Security.finalizeSigningKeyPair(context.getUuid(), Security.SideType.CLIENT);

            context.getChannel().writeAndFlush(responseKey);
            this.getBus().call(new ConnectionSigningKeysExchangedEvent(context));
        });
    }

    @EventSubscriber
    public void onSigningKeysExchanged(ConnectionSigningKeysExchangedEvent event) {
        ChannelContext context = event.getContext();

        this.getObserverManager().observe(SessionPublicPacket.class, Direction.OUTBOUND, context).thenAccept(_ -> {
            context.setHash(true);

            this.getBus().call(new ConnectionIntegrityVerifiedEvent(context));
        });
    }

    @EventSubscriber
    public void onConnectionIntegrityVerified(ConnectionIntegrityVerifiedEvent event) {
        ChannelContext context = event.getContext();

        this.getObserverManager().observe(SessionNoncePacket.class, Direction.INBOUND, context).thenAccept(message -> {
            SessionNoncePacket response = (SessionNoncePacket) message;

            Security.setNonce(context.getUuid(), Security.EncryptionMode.ASYMMETRIC,  response.getNonce());

            this.getBus().call(new ConnectionHandshakeComplete(context));
        });
    }

    @EventSubscriber
    public void onHandshakeComplete(ConnectionHandshakeComplete event) {
        ChannelContext context = event.getContext();

        context.getChannel().writeAndFlush(new SessionStatePacket(SessionStatePacket.State.REGISTER_EXCHANGE.getValue()));

        this.getObserverManager().observe(SessionPrivatePacket.class, Direction.INBOUND, context).thenAccept(message -> {
            SessionPrivatePacket response = (SessionPrivatePacket) message;
            Security.processKeyExchange(context.getUuid(), response.getKey());

            context.setEncryptionType(SYMMETRIC);

            this.getObserverManager().observe(SessionStatePacket.class, Direction.OUTBOUND, context).thenAccept(_ -> {
                context.setReady(true);

                this.getBus().call(new ConnectionReadyEvent(context));
            });

            context.getChannel().writeAndFlush(new SessionStatePacket(SessionStatePacket.State.REGISTER_FINISH.getValue()));
        });
    }
}
