package org.dnttr.zephyr.network.communication.api.client;

import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.network.communication.api.client.flow.ClientSessionOrchestrator;
import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.communication.core.channel.ChannelController;
import org.dnttr.zephyr.network.communication.core.flow.events.internal.channel.ConnectionEstablishedEvent;
import org.dnttr.zephyr.network.communication.core.managers.ObserverManager;
import org.dnttr.zephyr.network.communication.core.packet.transformer.TransformerFacade;
import org.dnttr.zephyr.network.protocol.packets.internal.ConnectionStatePacket;
import org.jetbrains.annotations.NotNull;

/**
 * ClientChannelController is responsible for managing the client-side channel operations.
 * It extends the ChannelController and initializes with specific session packets.
 *
 * @author dnttr
 */

public final class ClientChannelController extends ChannelController {

    public ClientChannelController(@NotNull EventBus eventBus, @NotNull ObserverManager observerManager, @NotNull TransformerFacade transformerFacade) {
        super(eventBus, observerManager, transformerFacade);

        this.getEventBus().register(new ClientSessionOrchestrator(this.getEventBus(), this.getObserverManager()));
        this.getEventBus().register(this);
    }

    @Override
    public void fireActive(@NotNull ChannelContext context) {
        this.getEventBus().call(new ConnectionEstablishedEvent(context));

        ConnectionStatePacket packet = new ConnectionStatePacket(ConnectionStatePacket.State.REGISTER_OPEN.getValue());
        context.getChannel().writeAndFlush(packet);

        super.fireActive(context);
    }
}