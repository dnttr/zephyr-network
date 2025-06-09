package org.dnttr.zephyr.network.communication.api.controllers;

import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.network.communication.api.ISession;
import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.communication.core.channel.ChannelController;
import org.dnttr.zephyr.network.protocol.packets.SessionKeyPacket;
import org.dnttr.zephyr.network.protocol.packets.SessionSecretPacket;
import org.dnttr.zephyr.network.protocol.packets.SessionStatePacket;
import org.jetbrains.annotations.NotNull;

/**
 * ServerChannelController is responsible for managing the server-side channel operations.
 * It extends the ChannelController and initializes with specific session packets.
 *
 * @author dnttr
 */
public final class ServerChannelController extends ChannelController {

    public ServerChannelController(ISession session, EventBus eventBus) {
        super(session, eventBus);

         this.addPackets(SessionStatePacket.class, SessionKeyPacket.class, SessionSecretPacket.class);
    }

    @Override
    public void fireActive(@NotNull ChannelContext context) {
        super.fireActive(context);

        byte[] x = new byte[2];

        x[0] = 1;
        x[1] = 1;

        context.getChannel().writeAndFlush(new SessionKeyPacket(x));
    }
}