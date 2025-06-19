package org.dnttr.zephyr.network.communication.api.client.flow;

import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.event.EventSubscriber;
import org.dnttr.zephyr.bridge.Security;
import org.dnttr.zephyr.network.communication.core.flow.Orchestrator;
import org.dnttr.zephyr.network.communication.core.flow.events.internal.channel.*;
import org.dnttr.zephyr.network.communication.core.flow.events.session.SessionEstablishedEvent;
import org.dnttr.zephyr.network.communication.core.managers.ObserverManager;
import org.dnttr.zephyr.network.communication.core.packet.Direction;
import org.dnttr.zephyr.network.protocol.packets.internal.ConnectionStatePacket;
import org.dnttr.zephyr.network.protocol.packets.internal.authorization.ConnectionNoncePacket;
import org.dnttr.zephyr.network.protocol.packets.internal.authorization.ConnectionPrivatePacket;
import org.dnttr.zephyr.network.protocol.packets.internal.authorization.ConnectionPublicPacket;

import static org.dnttr.zephyr.bridge.Security.EncryptionMode.SYMMETRIC;

/**
 * @author dnttr
 */

public final class ClientSessionOrchestrator extends Orchestrator {

    public ClientSessionOrchestrator(EventBus bus, ObserverManager observerManager) {
        super(bus, observerManager);
    }

    @EventSubscriber
    public void onEstablished(final ConnectionEstablishedEvent event) {
        var context = event.getContext();

        this.getObserverManager().observe(ConnectionPublicPacket.class, Direction.INBOUND, context).thenAccept(msg0 -> {
            var receivedMessage = (ConnectionPublicPacket) msg0;
            var responseKey = new ConnectionPublicPacket(this.getPublicKeyForAuth(context));

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

        this.getObserverManager().observe(ConnectionPublicPacket.class, Direction.INBOUND, context).thenAccept(msg0 -> {
            var receivedMessage = (ConnectionPublicPacket) msg0;
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

            var responseKey = new ConnectionPublicPacket(baseSigningKey.get());
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

        this.getObserverManager().observe(ConnectionPublicPacket.class, Direction.OUTBOUND, context).thenAccept(_ -> {
            context.setHash(true);

            this.getBus().call(new ConnectionIntegrityVerifiedEvent(context));

        });
    }

    @EventSubscriber
    public void onConnectionIntegrityVerified(final ConnectionIntegrityVerifiedEvent event) {
        var context = event.getContext();

        this.getObserverManager().observe(ConnectionNoncePacket.class, Direction.INBOUND, context).thenAccept(msg0 -> {
            var receivedMessage = (ConnectionNoncePacket) msg0;

            Security.setNonce(context.getUuid(), Security.EncryptionMode.ASYMMETRIC,  receivedMessage.getNonce());

            this.getBus().call(new ConnectionHandshakeComplete(context));
        });
    }


    @EventSubscriber
    public void onHandshakeComplete(final ConnectionHandshakeComplete event) {
        var context = event.getContext();

        var packet = new ConnectionStatePacket(ConnectionStatePacket.State.REGISTER_KEYS.getValue());
        context.getChannel().writeAndFlush(packet);

        this.getObserverManager().observe(ConnectionPrivatePacket.class, Direction.INBOUND, context).thenAccept(msg0 -> {
            var receivedMessage = (ConnectionPrivatePacket) msg0;
            var responseFinishPacket = new ConnectionStatePacket(ConnectionStatePacket.State.REGISTER_CLOSE.getValue());

            boolean isMessageProcessed = Security.processKeyExchange(context.getUuid(), receivedMessage.getKey());

            if (!isMessageProcessed) {
                context.restrict("Unable to process key exchange");
                return;
            }

            context.getChannel().writeAndFlush(responseFinishPacket).addListener(future -> {
                if (future.isSuccess()) {
                    context.setEncryptionType(SYMMETRIC);

                    this.getObserverManager().observe(ConnectionStatePacket.class, Direction.INBOUND, context).thenAccept(msg1 -> {
                        var responseConfirmationMessage = ConnectionStatePacket.State.from(((ConnectionStatePacket) msg1).getState());

                        if (responseConfirmationMessage == ConnectionStatePacket.State.REGISTER_CONFIRM) {
                            context.setReady(true);

                            this.getBus().call(new SessionEstablishedEvent(context.getConsumer()));
                        } else {
                            context.restrict("Invalid confirmation packet from server.");
                        }
                    });
                } else {
                    context.restrict("Failed to send REGISTER_FINISH packet.");
                }
            });
        });
    }
}
