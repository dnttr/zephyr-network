package org.dnttr.zephyr.network.communication.api.server;

import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.network.communication.api.server.flow.ServerSessionOrchestrator;
import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.communication.core.channel.ChannelController;
import org.dnttr.zephyr.network.communication.core.flow.Relay;
import org.dnttr.zephyr.network.communication.core.flow.events.internal.channel.ConnectionEstablishedEvent;
import org.dnttr.zephyr.network.communication.core.managers.ObserverManager;
import org.dnttr.zephyr.network.communication.core.packet.transformer.TransformerFacade;
import org.jetbrains.annotations.NotNull;

/**
 * ServerChannelController is responsible for managing the server-side channel operations.
 * It extends the ChannelController and initializes with specific session packets.
 *
 * @author dnttr
 */
public final class ServerChannelController extends ChannelController {

    public ServerChannelController(@NotNull EventBus eventBus, @NotNull ObserverManager observerManager, @NotNull TransformerFacade transformerFacade) {
        super(eventBus, observerManager, transformerFacade);

        this.getEventBus().register(new ServerSessionOrchestrator(this.getEventBus(), this.getObserverManager()));
        this.getEventBus().register(new Relay());
        this.getEventBus().register(this);
    }

    @Override
    public void fireActive(@NotNull ChannelContext context) {
        this.getEventBus().call(new ConnectionEstablishedEvent(context));
        super.fireActive(context);
    }
}