package org.dnttr.zephyr.network.communication.api.controllers;

import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.network.bridge.ZEKit;
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
        observer.accept(message -> {
            SessionStatePacket packet = (SessionStatePacket) message;

            if (State.from(packet.getState()) == State.REGISTER_REQUEST) {
                ZEKit.ffi_ze_nonce(context.getUuid(), ZEKit.Type.ASYMMETRIC.getValue());
                ZEKit.ffi_ze_key(context.getUuid(), ZEKit.Type.ASYMMETRIC.getValue());

                SessionPublicPacket publicAuthPacket = new SessionPublicPacket(ZEKit.ffi_ze_get_asymmetric_key(context.getUuid(), 0)); //pub

                context.getChannel().writeAndFlush(publicAuthPacket);
                Observer ox = this.getObserverManager().observe(SessionPublicPacket.class, Direction.INBOUND, context);
                ox.accept(msg1 -> {
                    SessionPublicPacket packet1 = (SessionPublicPacket) msg1;
                    ZEKit.ffi_ze_set_asymmetric_received_key(context.getUuid(), packet1.getPublicKey());

                    Observer hash = this.getObserverManager().observe(SessionPublicPacket.class, Direction.INBOUND, context);

                    SessionPublicPacket publicHashPacket = new SessionPublicPacket(ZEKit.ffi_ze_get_base_public_key_sh0(context.getUuid()));
                    context.getChannel().writeAndFlush(publicHashPacket);

                    hash.accept(msg2 -> {
                        SessionPublicPacket packet2 = (SessionPublicPacket) msg2;

                        ZEKit.ffi_ze_set_rv_public_key_sh0(context.getUuid(), packet2.getPublicKey());
                        ZEKit.ffi_ze_derive_keys_sh0(context.getUuid(), 0);
                        ZEKit.ffi_ze_derive_final_key_sh0(context.getUuid());

                        SessionStatePacket packet3 = new SessionStatePacket(State.REGISTER_RESPONSE.getValue());
                        context.getChannel().writeAndFlush(packet3);

                        Observer xt = this.getObserverManager().observe(SessionStatePacket.class, Direction.INBOUND, context);
                        xt.accept(msg3 -> {
                            SessionStatePacket packet4 = (SessionStatePacket) msg3;

                            if (State.from(packet4.getState()) == State.REGISTER_RESPONSE) {
                                context.setHash(true);
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

    /*
     *    byte[] nonce = ZEKit.ffi_ze_get_nonce(context.getUuid(), ZEKit.Type.ASYMMETRIC.getValue());

                SessionNoncePacket noncePacket = new SessionNoncePacket(ZEKit.Type.ASYMMETRIC.getValue(), nonce);
                context.getChannel().writeAndFlush(noncePacket);

                byte[] authKey = ZEKit.ffi_ze_get_asymmetric_key(context.getUuid(), 0);
                SessionPublicPacket publicPacket = new SessionPublicPacket(authKey);
     */

    @Override
    public void fireRead(@NotNull ChannelContext context, @NotNull Packet msg) {
        super.fireRead(context, msg);
    }

    @Override
    public void fireWriteComplete(@NotNull ChannelContext context, @NotNull Packet msg) {
        super.fireWriteComplete(context, msg);
    }
}