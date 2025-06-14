package org.dnttr.zephyr.network.communication.api.controllers;

import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.network.bridge.ZEKit;
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

import java.util.Arrays;

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
            ZEKit.ffi_ze_nonce(context.getUuid(), ZEKit.Type.ASYMMETRIC.getValue());
            ZEKit.ffi_ze_key(context.getUuid(), ZEKit.Type.ASYMMETRIC.getValue());
            ZEKit.ffi_ze_set_asymmetric_received_key(context.getUuid(), serverAuthKey.getPublicKey());

            byte[] clientAuthKey = ZEKit.ffi_ze_get_asymmetric_key(context.getUuid(), 0);
            SessionPublicPacket clientAuthKeyPacket = new SessionPublicPacket(clientAuthKey);
            context.getChannel().writeAndFlush(clientAuthKeyPacket);

            Observer hash = this.getObserverManager().observe(SessionPublicPacket.class, Direction.INBOUND, context);
            hash.thenAccept(msg1 -> {
                SessionPublicPacket serverHashKey = (SessionPublicPacket) msg1;
                ZEKit.ffi_ze_set_rv_public_key_sh0(context.getUuid(), serverHashKey.getPublicKey());

                byte[] clientKey = ZEKit.ffi_ze_get_base_public_key_sh0(context.getUuid());

                ZEKit.ffi_ze_derive_keys_sh0(context.getUuid(), 1);
                ZEKit.ffi_ze_derive_final_key_sh0(context.getUuid(), 1);

                Observer otx = this.getObserverManager().observe(SessionPublicPacket.class, Direction.OUTBOUND, context);
                otx.thenAccept(_ -> {
                    context.setHash(true);

                    Observer privatePacket = this.getObserverManager().observe(SessionPrivatePacket.class, Direction.INBOUND, context);
                    privatePacket.thenAccept(packet -> {
                        SessionPrivatePacket sessionPrivatePacket = (SessionPrivatePacket) packet;
                        System.out.println(Arrays.toString(sessionPrivatePacket.getKey()));
                    });
                });

                context.getChannel().writeAndFlush(new SessionPublicPacket(clientKey));
            });
        });

        SessionStatePacket packet = new SessionStatePacket(SessionStatePacket.State.REGISTER_REQUEST.getValue());
        context.getChannel().writeAndFlush(packet);

        super.fireActive(context);
    }

    private void continueAuth(@NotNull ChannelContext context) {
    }

    @Override
    public void fireRead(@NotNull ChannelContext context, @NotNull Packet msg) {
        super.fireRead(context, msg);
    }
}