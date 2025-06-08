package org.dnttr.zephyr.network.communication.api.controllers;

import org.dnttr.zephyr.network.communication.api.ISession;
import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.communication.core.channel.ChannelController;
import org.dnttr.zephyr.protocol.packet.impl.SessionKeyPacket;
import org.dnttr.zephyr.protocol.packet.impl.SessionSecretPacket;
import org.dnttr.zephyr.protocol.packet.impl.SessionStatePacket;
import org.jetbrains.annotations.NotNull;

/**
 * ClientChannelController is responsible for managing the client-side channel operations.
 * It extends the ChannelController and initializes with specific session packets.
 *
 * @author dnttr
 */

public final class ClientChannelController extends ChannelController {

    public ClientChannelController(ISession session) {
        super(session);

        this.addPackets(SessionStatePacket.class, SessionKeyPacket.class, SessionSecretPacket.class);
    }

    @Override
    public void fireActive(@NotNull ChannelContext context) {
        System.out.println(context.getEncryptionType());
        System.out.println("fired");
        super.fireActive(context);
    }
}