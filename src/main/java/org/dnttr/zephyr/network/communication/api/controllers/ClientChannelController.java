package org.dnttr.zephyr.network.communication.api.controllers;

import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.network.communication.api.Parent;
import org.dnttr.zephyr.network.communication.api.authorization.ClientAuthorization;
import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.communication.core.channel.ChannelController;
import org.dnttr.zephyr.network.communication.core.flow.events.internal.channel.ConnectionEstablishedEvent;
import org.dnttr.zephyr.network.communication.core.managers.ObserverManager;
import org.dnttr.zephyr.network.communication.core.packet.processor.Transformer;
import org.dnttr.zephyr.network.protocol.packets.SessionStatePacket;
import org.jetbrains.annotations.NotNull;

/**
 * ClientChannelController is responsible for managing the client-side channel operations.
 * It extends the ChannelController and initializes with specific session packets.
 *
 * @author dnttr
 */

public final class ClientChannelController extends ChannelController {

    public ClientChannelController(@NotNull Parent session, @NotNull EventBus eventBus, @NotNull ObserverManager observerManager, @NotNull Transformer transformer) {
        super(eventBus, observerManager, transformer);

        this.getEventBus().register(new ClientAuthorization(this.getEventBus(), this.getObserverManager()));
        this.getEventBus().register(this);
    }

    @Override
    public void fireActive(@NotNull ChannelContext context) {
        this.getEventBus().call(new ConnectionEstablishedEvent(context));

        SessionStatePacket packet = new SessionStatePacket(SessionStatePacket.State.REGISTER_REQUEST.getValue());
        context.getChannel().writeAndFlush(packet);

        super.fireActive(context);
    }
}