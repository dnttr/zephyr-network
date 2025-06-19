package org.dnttr.zephyr.network.communication.api.authorization;

import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.event.EventSubscriber;
import org.dnttr.zephyr.network.bridge.Security;
import org.dnttr.zephyr.network.communication.core.flow.Authorization;
import org.dnttr.zephyr.network.communication.core.flow.events.internal.channel.*;
import org.dnttr.zephyr.network.communication.core.flow.events.session.SessionEstablishedEvent;
import org.dnttr.zephyr.network.communication.core.managers.ObserverManager;
import org.dnttr.zephyr.network.communication.core.packet.processor.Direction;
import org.dnttr.zephyr.network.protocol.packets.internal.ConnectionStatePacket;
import org.dnttr.zephyr.network.protocol.packets.internal.authorization.ConnectionNoncePacket;
import org.dnttr.zephyr.network.protocol.packets.internal.authorization.ConnectionPrivatePacket;
import org.dnttr.zephyr.network.protocol.packets.internal.authorization.ConnectionPublicPacket;

/**
 * @author dnttr
 */

public final class ServerAuthorization extends Authorization {

    public ServerAuthorization(EventBus bus, ObserverManager observerManager) {
        super(bus, observerManager);
    }

    @EventSubscriber
    public void onEstablished(final ConnectionEstablishedEvent event) {
        var context = event.getContext();

        this.getObserverManager().observe(ConnectionStatePacket.class, Direction.INBOUND, context).thenAccept(msg0 -> {
            var receivedMessage = (ConnectionStatePacket) msg0;

            if (ConnectionStatePacket.State.from(receivedMessage.getState()) == ConnectionStatePacket.State.REGISTER_OPEN) {
                boolean isNonceBuilt = Security.buildNonce(context.getUuid(), Security.EncryptionMode.ASYMMETRIC);

                if (!isNonceBuilt) {
                    context.restrict("Nonce could not be built");
                    return;
                }

                var responseKey = new ConnectionPublicPacket(this.getPublicKeyForAuth(context));
                context.getChannel().writeAndFlush(responseKey);

                this.getBus().call(new ConnectionInitialPublicKeyExchangedEvent(context));
            } else {
                context.restrict("Invalid session state.");
            }
        });
    }

    @EventSubscriber
    public void onInitialPublicKeyExchanged(final ConnectionInitialPublicKeyExchangedEvent event) {
        var context = event.getContext();

        this.getObserverManager().observe(ConnectionPublicPacket.class, Direction.INBOUND, context).thenAccept(msg0 -> {
            var receivedMessage = (ConnectionPublicPacket) msg0;

            boolean isPartnerPublicKeyBuilt = Security.setPartnerPublicKey(context.getUuid(), receivedMessage.getPublicKey());

            if (!isPartnerPublicKeyBuilt) {
                context.restrict("Partner key could not be built");
                return;
            }

            this.getBus().call(new ConnectionSigningKeysExchangedEvent(context));

            var baseSigningKey = Security.getBaseSigningKey(context.getUuid());

            if (baseSigningKey.isEmpty()) {
                context.restrict("Unable to get public key for signing.");

                return;
            }

            var responseKey = new ConnectionPublicPacket(baseSigningKey.get());
            context.getChannel().writeAndFlush(responseKey);
        });
    }

    @EventSubscriber
    public void onSigningKeysExchanged(final ConnectionSigningKeysExchangedEvent event) {
        var context = event.getContext();

        this.getObserverManager().observe(ConnectionPublicPacket.class, Direction.INBOUND, context).thenAccept(msg0 -> {
            var receivedMessage = (ConnectionPublicPacket) msg0;

            boolean isSigningPublicKeySet = Security.setSigningPublicKey(context.getUuid(), receivedMessage.getPublicKey());

            if (!isSigningPublicKeySet) {
                context.restrict("Unable to set public key for signing.");
                return;
            }

            boolean isSigningKeyPairDerived = Security.deriveSigningKeyPair(context.getUuid(), Security.SideType.SERVER);

            if (!isSigningKeyPairDerived) {
                context.restrict("Unable to derive signing key.");
                return;
            }

            boolean isSigningKeyPairFinalized = Security.finalizeSigningKeyPair(context.getUuid(), Security.SideType.SERVER);

            if (!isSigningKeyPairFinalized) {
                context.restrict("Unable to finalize signing key.");
                return;
            }

            context.setHash(true);

            var nonce = Security.getNonce(context.getUuid(), Security.EncryptionMode.ASYMMETRIC);

            if (nonce.isEmpty()) {
                context.restrict("Unable to get nonce in asymmetric mode.");

                return;
            }

            var responseNonce = new ConnectionNoncePacket(Security.EncryptionMode.ASYMMETRIC.getValue(), nonce.get());
            context.getChannel().writeAndFlush(responseNonce);

            this.getBus().call(new ConnectionIntegrityVerifiedEvent(context));
        });
    }

    @EventSubscriber
    public void onConnectionIntegrityVerified(final ConnectionIntegrityVerifiedEvent event) {
        var context = event.getContext();

        this.getObserverManager().observe(ConnectionStatePacket.class, Direction.INBOUND, context).thenAccept(msg0 -> {
            var receivedMessage = (ConnectionStatePacket) msg0;

            if (ConnectionStatePacket.State.from(receivedMessage.getState()) == ConnectionStatePacket.State.REGISTER_KEYS) {
                boolean isKeyBuilt = Security.generateKeys(context.getUuid(), Security.EncryptionMode.SYMMETRIC);

                if (!isKeyBuilt) {
                    context.restrict("Unable to generate keys for signing.");
                    return;
                }

                var keyExchange = Security.createKeyExchange(context.getUuid());

                if (keyExchange.isEmpty()) {
                    context.restrict("Unable to create exchange message.");
                    return;
                }

                this.getBus().call(new ConnectionHandshakeComplete(context));

                ConnectionPrivatePacket responseKey = new ConnectionPrivatePacket(keyExchange.get());

                context.getChannel().writeAndFlush(responseKey);
            } else {
                context.restrict("Invalid session state.");
            }
        });
    }

    @EventSubscriber
    public void onHandshakeComplete(final ConnectionHandshakeComplete event) {
        var context = event.getContext();

        this.getObserverManager().observe(ConnectionStatePacket.class, Direction.INBOUND, context).thenAccept(msg0 -> {
            var receivedStateMessage = (ConnectionStatePacket) msg0;

            if (ConnectionStatePacket.State.from(receivedStateMessage.getState()) == ConnectionStatePacket.State.REGISTER_CLOSE) {
                context.setEncryptionType(Security.EncryptionMode.SYMMETRIC);

                var responseConfirmationMessage = new ConnectionStatePacket(ConnectionStatePacket.State.REGISTER_CONFIRM.getValue());

                context.getChannel().writeAndFlush(responseConfirmationMessage).addListener(future -> {
                    if (future.isSuccess()) {
                        context.setReady(true);

                        this.getBus().call(new SessionEstablishedEvent(context.getConsumer()));
                    } else {
                        context.restrict("Failed to send confirmation packet.");
                    }
                });
            } else {
                context.restrict("Invalid session state, expected REGISTER_FINISH.");
            }
        });
    }
}
