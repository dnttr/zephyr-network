package org.dnttr.zephyr.network.communication.api.controllers;

import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.network.communication.api.ISession;
import org.dnttr.zephyr.network.communication.api.authorization.ServerAuthorization;
import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.communication.core.channel.ChannelController;
import org.dnttr.zephyr.network.communication.core.flow.events.internal.channel.ConnectionEstablishedEvent;
import org.dnttr.zephyr.network.communication.core.managers.ObserverManager;
import org.dnttr.zephyr.network.communication.core.packet.processor.Transformer;
import org.dnttr.zephyr.network.protocol.packets.SessionStatePacket;
import org.dnttr.zephyr.network.protocol.packets.authorization.SessionNoncePacket;
import org.dnttr.zephyr.network.protocol.packets.authorization.SessionPrivatePacket;
import org.dnttr.zephyr.network.protocol.packets.authorization.SessionPublicPacket;
import org.jetbrains.annotations.NotNull;

/**
 * ServerChannelController is responsible for managing the server-side channel operations.
 * It extends the ChannelController and initializes with specific session packets.
 *
 * @author dnttr
 */
public final class ServerChannelController extends ChannelController {

    public ServerChannelController(ISession session, EventBus eventBus, ObserverManager observerManager, Transformer transformer) {
        super(eventBus, observerManager, transformer);


        this.addPackets(SessionStatePacket.class, SessionPrivatePacket.class, SessionPublicPacket.class, SessionNoncePacket.class);
        this.getEventBus().register(new ServerAuthorization(this.getEventBus(), this.getObserverManager()));
        this.getEventBus().register(this);
        this.getEventBus().register(session);
    }

    @Override
    public void fireActive(@NotNull ChannelContext context) {
        this.getEventBus().call(new ConnectionEstablishedEvent(context));
        super.fireActive(context);
    }
}