package org.dnttr.zephyr.network.communication.core.flow.events.packet;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dnttr.zephyr.event.Event;
import org.dnttr.zephyr.network.communication.core.Consumer;
import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.protocol.Packet;

/**
 * @author dnttr
 */

@Getter
@RequiredArgsConstructor
public final class PacketInboundEvent extends Event {

    public final ChannelContext channelContext;
    public final Consumer consumer;
    public final Packet packet;
}
