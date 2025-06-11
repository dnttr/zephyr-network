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

         this.addPackets(SessionStatePacket.class, SessionPrivatePacket.class, SessionPublicPacket.class);
    }

    @Override
    public void fireActive(@NotNull ChannelContext context) {
        Observer observer = this.getObserverManager().observe(SessionStatePacket.class, Direction.INBOUND, context);
        observer.accept(message -> {
            SessionStatePacket packet = (SessionStatePacket) message;

            if (State.from(packet.getState()) == State.REGISTER_REQUEST) {
                ZEKit.ffi_ze_key(context.getUuid(), ZEKit.Type.ASYMMETRIC.getValue());
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
    public void fireWriteComplete(@NotNull ChannelContext context, @NotNull Packet msg) {
        super.fireWriteComplete(context, msg);
    }
}