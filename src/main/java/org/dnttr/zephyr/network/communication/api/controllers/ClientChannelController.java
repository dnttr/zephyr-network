package org.dnttr.zephyr.network.communication.api.controllers;

import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.network.communication.api.ISession;
import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.communication.core.channel.ChannelController;
import org.dnttr.zephyr.network.protocol.Packet;
import org.dnttr.zephyr.network.protocol.packets.SessionKeyPacket;
import org.dnttr.zephyr.network.protocol.packets.SessionSecretPacket;
import org.dnttr.zephyr.network.protocol.packets.SessionStatePacket;
import org.jetbrains.annotations.NotNull;

/**
 * ClientChannelController is responsible for managing the client-side channel operations.
 * It extends the ChannelController and initializes with specific session packets.
 *
 * @author dnttr
 */

public final class ClientChannelController extends ChannelController {

    public ClientChannelController(ISession session, EventBus eventBus) {
        super(session, eventBus);

        this.addPackets(SessionStatePacket.class, SessionKeyPacket.class, SessionSecretPacket.class);
    }

    @Override
    public void fireActive(@NotNull ChannelContext context) {
        System.out.println("fireActive");
        SessionStatePacket packet = new SessionStatePacket(SessionStatePacket.State.REGISTER_REQUEST.getValue());
        context.getChannel().writeAndFlush(packet).addListener(future -> {
            if (!future.isSuccess()) {
                System.err.println("Failed to send packet: " + future.cause());
            }
        });

        super.fireActive(context);
    }

    @Override
    public void fireRead(@NotNull ChannelContext context, @NotNull Packet msg) {
        System.out.println("fireRead");
        super.fireRead(context, msg);
    }
}