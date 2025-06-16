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

import java.util.Optional;

/**
 * @author dnttr
 */

public class ServerAuthorization extends Authorization {

    public ServerAuthorization(EventBus bus, ObserverManager observerManager) {
        super(bus, observerManager);
    }

    @EventSubscriber
    public void onConnectionEstablished(ConnectionEstablishedEvent event) {
        ChannelContext context = event.getContext();

        this.getObserverManager().observe(SessionStatePacket.class, Direction.INBOUND, context).thenAccept(message -> {
            SessionStatePacket packet = (SessionStatePacket) message;

            if (SessionStatePacket.State.from(packet.getState()) == SessionStatePacket.State.REGISTER_REQUEST) {
                Security.buildNonce(context.getUuid(), Security.EncryptionMode.ASYMMETRIC);
                Security.generateKeys(context.getUuid(), Security.EncryptionMode.ASYMMETRIC);
                Optional<byte[]> authPubKey = Security.getKeyPair(context.getUuid(), Security.KeyType.PUBLIC);

                if (authPubKey.isEmpty()) {
                    context.restrict("Unable to get public key for auth.");

                    return;
                }
                SessionPublicPacket publicAuthPacket = new SessionPublicPacket(authPubKey.get());
                context.getChannel().writeAndFlush(publicAuthPacket);

                this.getBus().call(new ConnectionSecondStageEvent(context));
            } else {
                context.restrict("Invalid session state.");
            }
        });
    }

    @EventSubscriber
    public void onConnectionSecondStage(ConnectionSecondStageEvent event) {
        ChannelContext context = event.getContext();

        this.getObserverManager().observe(SessionPublicPacket.class, Direction.INBOUND, context).thenAccept(message -> {
            SessionPublicPacket response = (SessionPublicPacket) message;
            Security.setPartnerPublicKey(context.getUuid(), response.getPublicKey());

            Optional<byte[]> baseSigningKey = Security.getBaseSigningKey(context.getUuid());
            if (baseSigningKey.isEmpty()) {
                context.restrict("Unable to get public key for signing.");

                return;
            }

            SessionPublicPacket publicHashPacket = new SessionPublicPacket(baseSigningKey.get());
            context.getChannel().writeAndFlush(publicHashPacket);

            this.getBus().call(new ConnectionThirdStageEvent(context));
        });
    }

    @EventSubscriber
    public void onConnectionThirdStage(ConnectionThirdStageEvent event) {
        ChannelContext context = event.getContext();

        this.getObserverManager().observe(SessionPublicPacket.class, Direction.INBOUND, context).thenAccept(msg2 -> {
            SessionPublicPacket packet2 = (SessionPublicPacket) msg2;

            Security.setSigningPublicKey(context.getUuid(), packet2.getPublicKey());
            Security.deriveSigningKeyPair(context.getUuid(), Security.SideType.SERVER);
            Security.finalizeSigningKeyPair(context.getUuid(), Security.SideType.SERVER);

            context.setHash(true);

            Optional<byte[]> nonce = Security.getNonce(context.getUuid(), Security.EncryptionMode.ASYMMETRIC);

            if (nonce.isEmpty()) {
                context.restrict("Unable to get nonce in asymmetric mode.");

                return;
            }

            SessionNoncePacket noncePacket = new SessionNoncePacket(Security.EncryptionMode.ASYMMETRIC.getValue(), nonce.get());
            context.getChannel().writeAndFlush(noncePacket);

            this.getBus().call(new ConnectionFourthStageEvent(context));
        });
    }

    @EventSubscriber
    public void onConnectionFourthStage(ConnectionFourthStageEvent event) {
        ChannelContext context = event.getContext();

        this.getObserverManager().observe(SessionStatePacket.class, Direction.INBOUND, context).thenAccept(msg3 -> {
            SessionStatePacket packet3 = (SessionStatePacket) msg3;

            if (SessionStatePacket.State.from(packet3.getState()) == SessionStatePacket.State.REGISTER_EXCHANGE) {
                Security.generateKeys(context.getUuid(), Security.EncryptionMode.SYMMETRIC);
                Optional<byte[]> keyExchange = Security.createKeyExchange(context.getUuid());

                if (keyExchange.isEmpty()) {
                    context.restrict("Unable to create exchange message.");
                    return;
                }

                SessionPrivatePacket privatePacket = new SessionPrivatePacket(keyExchange.get());
                context.getChannel().writeAndFlush(privatePacket);
                context.setEncryptionType(Security.EncryptionMode.SYMMETRIC);

                this.getBus().call(new ConnectionFifthStageEvent(context));
            }
        });
    }

    @EventSubscriber
    public void onConnectionFifthStage(ConnectionFifthStageEvent event) {
        ChannelContext context = event.getContext();

        this.getObserverManager().observe(SessionStatePacket.class, Direction.INBOUND, context).thenAccept(msg4 -> {
            SessionStatePacket packet4 = (SessionStatePacket) msg4;

            if (SessionStatePacket.State.from(packet4.getState()) == SessionStatePacket.State.REGISTER_FINISH) {
                context.setReady(true);
            }
        });
    }
}
