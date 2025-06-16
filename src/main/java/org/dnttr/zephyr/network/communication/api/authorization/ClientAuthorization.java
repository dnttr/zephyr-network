package org.dnttr.zephyr.network.communication.api.authorization;

import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.event.EventSubscriber;
import org.dnttr.zephyr.network.bridge.Security;
import org.dnttr.zephyr.network.communication.core.flow.Authorization;
import org.dnttr.zephyr.network.communication.core.flow.events.internal.channel.*;
import org.dnttr.zephyr.network.communication.core.flow.events.session.SessionEstablishedEvent;
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

public final class ClientAuthorization extends Authorization {

    public ClientAuthorization(EventBus bus,  ObserverManager observerManager) {
        super(bus, observerManager);
    }

    @EventSubscriber
    public void onEstablished(final ConnectionEstablishedEvent event) {
        var context = event.getContext();
        var packet = new SessionStatePacket(SessionStatePacket.State.REGISTER_REQUEST.getValue());

        context.getChannel().writeAndFlush(packet);

        this.getObserverManager().observe(SessionPublicPacket.class, Direction.INBOUND, context).thenAccept(msg0 -> {
            var receivedMessage = (SessionPublicPacket) msg0;
            var responseKey = new SessionPublicPacket(this.getPublicKeyForAuth(context));

            boolean isPartnerPublicKeySet = Security.setPartnerPublicKey(context.getUuid(), receivedMessage.getPublicKey());

            if (!isPartnerPublicKeySet) {
                context.restrict("Unable to set partner public key");
                return;
            }

            this.getBus().call(new ConnectionInitialPublicKeyExchangedEvent(context));
            context.getChannel().writeAndFlush(responseKey);
        });
    }

    @EventSubscriber
    public void onInitialPublicKeyExchanged(final ConnectionInitialPublicKeyExchangedEvent event) {
        var context = event.getContext();

        this.getObserverManager().observe(SessionPublicPacket.class, Direction.INBOUND, context).thenAccept(msg0 -> {
            var receivedMessage = (SessionPublicPacket) msg0;
            var baseSigningKey = Security.getBaseSigningKey(context.getUuid());

            boolean isSigningPublicKeySet = Security.setSigningPublicKey(context.getUuid(), receivedMessage.getPublicKey());

            if (!isSigningPublicKeySet) {
                context.restrict("Unable to set signing public key");
                return;
            }

            if (baseSigningKey.isEmpty()) {
                context.restrict("Unable to get public key for signing.");

                return;
            }

            var responseKey = new SessionPublicPacket(baseSigningKey.get());
            this.getBus().call(new ConnectionSigningKeysExchangedEvent(context));

            boolean isSigningKeyPairDerived = Security.deriveSigningKeyPair(context.getUuid(), Security.SideType.CLIENT);

            if (!isSigningKeyPairDerived) {
                context.restrict("Unable to derive key pair for signing.");
                return;
            }

            boolean isSigningKeyPairFinalized = Security.finalizeSigningKeyPair(context.getUuid(), Security.SideType.CLIENT);

            if (!isSigningKeyPairFinalized) {
                context.restrict("Unable to finalize signing key pair for signing.");
                return;
            }

            context.getChannel().writeAndFlush(responseKey);
        });
    }

    @EventSubscriber
    public void onSigningKeysExchanged(final ConnectionSigningKeysExchangedEvent event) {
        var context = event.getContext();

        this.getObserverManager().observe(SessionPublicPacket.class, Direction.OUTBOUND, context).thenAccept(_ -> {
            context.setHash(true);

            this.getBus().call(new ConnectionIntegrityVerifiedEvent(context));

        });
    }

    @EventSubscriber
    public void onConnectionIntegrityVerified(final ConnectionIntegrityVerifiedEvent event) {
        var context = event.getContext();

        this.getObserverManager().observe(SessionNoncePacket.class, Direction.INBOUND, context).thenAccept(msg0 -> {
            var receivedMessage = (SessionNoncePacket) msg0;

            Security.setNonce(context.getUuid(), Security.EncryptionMode.ASYMMETRIC,  receivedMessage.getNonce());

            this.getBus().call(new ConnectionHandshakeComplete(context));
        });
    }

    @EventSubscriber
    public void onHandshakeComplete(final ConnectionHandshakeComplete event) {
        var context = event.getContext();

        var packet = new SessionStatePacket(SessionStatePacket.State.REGISTER_EXCHANGE.getValue());
        context.getChannel().writeAndFlush(packet);

        this.getObserverManager().observe(SessionPrivatePacket.class, Direction.INBOUND, context).thenAccept(msg0 -> {
            var receivedMessage = (SessionPrivatePacket) msg0;
            var responseState = new SessionStatePacket(SessionStatePacket.State.REGISTER_FINISH.getValue());

            boolean isMessageProcessed = Security.processKeyExchange(context.getUuid(), receivedMessage.getKey());

            if (!isMessageProcessed) {
                context.restrict("Unable to process key exchange");
                return;
            }

            context.setEncryptionType(SYMMETRIC);

            this.getObserverManager().observe(SessionStatePacket.class, Direction.OUTBOUND, context).thenAccept(_ -> {
                context.setReady(true);

                this.getBus().call(new SessionEstablishedEvent(context.getConsumer()));
            });
            context.getChannel().writeAndFlush(responseState);
        });
    }
}
