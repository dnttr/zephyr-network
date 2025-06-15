package org.dnttr.zephyr.network.communication.api.controllers;

import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.network.bridge.Security;
import org.dnttr.zephyr.network.communication.api.ISession;
import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.communication.core.channel.ChannelController;
import org.dnttr.zephyr.network.communication.core.flow.Observer;
import org.dnttr.zephyr.network.communication.core.packet.processor.Direction;
import org.dnttr.zephyr.network.protocol.Packet;
import org.dnttr.zephyr.network.protocol.packets.SessionStatePacket;
import org.dnttr.zephyr.network.protocol.packets.authorization.SessionNoncePacket;
import org.dnttr.zephyr.network.protocol.packets.authorization.SessionPrivatePacket;
import org.dnttr.zephyr.network.protocol.packets.authorization.SessionPublicPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static org.dnttr.zephyr.network.protocol.packets.SessionStatePacket.State;

/**
 * ServerChannelController is responsible for managing the server-side channel operations.
 * It extends the ChannelController and initializes with specific session packets.
 *
 * @author dnttr
 */
public final class ServerChannelController extends ChannelController {

    public ServerChannelController(ISession session, EventBus eventBus) {
        super(session, eventBus);

         this.addPackets(SessionStatePacket.class, SessionPrivatePacket.class, SessionPublicPacket.class, SessionNoncePacket.class);
    }

    @Override
    public void fireActive(@NotNull ChannelContext context) {
        Observer observer = this.getObserverManager().observe(SessionStatePacket.class, Direction.INBOUND, context);
        observer.thenAccept(message -> {
            SessionStatePacket packet = (SessionStatePacket) message;

            if (State.from(packet.getState()) == State.REGISTER_REQUEST) {
                Security.buildNonce(context.getUuid(), Security.EncryptionMode.ASYMMETRIC);
                Security.generateKeys(context.getUuid(), Security.EncryptionMode.ASYMMETRIC);
                Optional<byte[]> authPubKey = Security.getKeyPair(context.getUuid(), Security.KeyType.PUBLIC);

                if (authPubKey.isEmpty()) {
                    context.restrict();

                    return;
                }
                SessionPublicPacket publicAuthPacket = new SessionPublicPacket(authPubKey.get());
                context.getChannel().writeAndFlush(publicAuthPacket);

                Observer ox = this.getObserverManager().observe(SessionPublicPacket.class, Direction.INBOUND, context);

                ox.thenAccept(msg1 -> {
                    SessionPublicPacket packet1 = (SessionPublicPacket) msg1;
                    Security.setPartnerPublicKey(context.getUuid(), packet1.getPublicKey());

                    Observer hash = this.getObserverManager().observe(SessionPublicPacket.class, Direction.INBOUND, context);

                    Optional<byte[]> baseSigningKey = Security.getBaseSigningKey(context.getUuid());
                    if (baseSigningKey.isEmpty()) {
                        context.restrict();

                        return;
                    }

                    SessionPublicPacket publicHashPacket = new SessionPublicPacket(baseSigningKey.get());
                    context.getChannel().writeAndFlush(publicHashPacket);

                    hash.thenAccept(msg2 -> {
                        SessionPublicPacket packet2 = (SessionPublicPacket) msg2;

                        Security.setSigningPublicKey(context.getUuid(), packet2.getPublicKey());
                        Security.deriveSigningKeyPair(context.getUuid(), Security.SideType.SERVER);
                        Security.finalizeSigningKeyPair(context.getUuid(), Security.SideType.SERVER);

                        context.setHash(true);

                        Optional<byte[]> nonce = Security.getNonce(context.getUuid(), Security.EncryptionMode.ASYMMETRIC);

                        if (nonce.isEmpty()) {
                            context.restrict();

                            return;
                        }

                        SessionNoncePacket noncePacket = new SessionNoncePacket(Security.EncryptionMode.ASYMMETRIC.getValue(), nonce.get());
                        context.getChannel().writeAndFlush(noncePacket);

                        Observer xx = this.getObserverManager().observe(SessionStatePacket.class, Direction.INBOUND, context);
                        xx.thenAccept(msg3 -> {
                            SessionStatePacket packet3 = (SessionStatePacket) msg3;

                            if (State.from(packet3.getState()) == State.REGISTER_EXCHANGE) {
                                Security.generateKeys(context.getUuid(), Security.EncryptionMode.SYMMETRIC);
                                Optional<byte[]> keyExchange = Security.createKeyExchange(context.getUuid());

                                if (keyExchange.isEmpty()) {
                                    context.restrict();
                                    return;
                                }

                                SessionPrivatePacket privatePacket = new SessionPrivatePacket(keyExchange.get());
                                context.getChannel().writeAndFlush(privatePacket);

                                Observer kt  = this.getObserverManager().observe(SessionStatePacket.class, Direction.INBOUND, context);
                                kt.thenAccept(msg4 -> {
                                    SessionStatePacket packet4 = (SessionStatePacket) msg4;

                                    if (State.from(packet4.getState()) == State.REGISTER_FINISH) {
                                        context.setReady(true);
                                    }
                                });

                                context.setEncryptionType(Security.EncryptionMode.SYMMETRIC);
                            }
                        });

                    });
                });
            } else {
                context.restrict();
            }
        });
        super.fireActive(context);
    }

    @Override
    public void fireRead(@NotNull ChannelContext context, @NotNull Packet msg) {
        super.fireRead(context, msg);
    }

    @Override
    public void fireWrite(@NotNull ChannelContext context, @NotNull Packet msg) {
        super.fireWrite(context, msg);
    }

    @Override
    public void fireWriteComplete(@NotNull ChannelContext context, @NotNull Packet msg) {
        super.fireWriteComplete(context, msg);
    }
}