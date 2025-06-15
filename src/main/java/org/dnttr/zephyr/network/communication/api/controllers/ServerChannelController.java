package org.dnttr.zephyr.network.communication.api.controllers;

import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.network.bridge.internal.ZEKit;
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
                ZEKit.ffi_ze_nonce(context.getUuid(), ZEKit.Type.ASYMMETRIC.getValue());
                ZEKit.ffi_ze_key(context.getUuid(), ZEKit.Type.ASYMMETRIC.getValue());

                SessionPublicPacket publicAuthPacket = new SessionPublicPacket(ZEKit.ffi_ze_get_asymmetric_key(context.getUuid(), 0)); //pub

                context.getChannel().writeAndFlush(publicAuthPacket);
                Observer ox = this.getObserverManager().observe(SessionPublicPacket.class, Direction.INBOUND, context);
                ox.thenAccept(msg1 -> {
                    SessionPublicPacket packet1 = (SessionPublicPacket) msg1;
                    ZEKit.ffi_ze_set_asymmetric_received_key(context.getUuid(), packet1.getPublicKey());

                    Observer hash = this.getObserverManager().observe(SessionPublicPacket.class, Direction.INBOUND, context);

                    SessionPublicPacket publicHashPacket = new SessionPublicPacket(ZEKit.ffi_ze_get_base_public_key_sh0(context.getUuid()));
                    context.getChannel().writeAndFlush(publicHashPacket);

                    hash.thenAccept(msg2 -> {
                        SessionPublicPacket packet2 = (SessionPublicPacket) msg2;

                        ZEKit.ffi_ze_set_rv_public_key_sh0(context.getUuid(), packet2.getPublicKey());
                        ZEKit.ffi_ze_derive_keys_sh0(context.getUuid(), 0);
                        ZEKit.ffi_ze_derive_final_key_sh0(context.getUuid(), 0);

                        context.setHash(true);

                        byte[] nonce = ZEKit.ffi_ze_get_nonce(context.getUuid(), ZEKit.Type.ASYMMETRIC.getValue());
                        SessionNoncePacket noncePacket = new SessionNoncePacket(ZEKit.Type.ASYMMETRIC.getValue(), nonce);
                        Observer xx = this.getObserverManager().observe(SessionStatePacket.class, Direction.INBOUND, context);
                        xx.thenAccept(msg3 -> {
                            SessionStatePacket packet3 = (SessionStatePacket) msg3;

                            if (State.from(packet3.getState()) == State.REGISTER_EXCHANGE) {
                                ZEKit.ffi_ze_key(context.getUuid(), ZEKit.Type.SYMMETRIC.getValue());
                                byte[] bytes = ZEKit.ffi_ze_get_exchange_message(context.getUuid());
                                SessionPrivatePacket privatePacket = new SessionPrivatePacket(bytes);

                                context.getChannel().writeAndFlush(privatePacket);

                                Observer kt  = this.getObserverManager().observe(SessionStatePacket.class, Direction.INBOUND, context);
                                kt.thenAccept(msg4 -> {
                                    SessionStatePacket packet4 = (SessionStatePacket) msg4;

                                    if (State.from(packet4.getState()) == State.REGISTER_FINISH) {
                                        context.setReady(true);
                                    }
                                });
                                context.setEncryptionType(ZEKit.Type.SYMMETRIC);
                            }
                        });
                        context.getChannel().writeAndFlush(noncePacket);
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