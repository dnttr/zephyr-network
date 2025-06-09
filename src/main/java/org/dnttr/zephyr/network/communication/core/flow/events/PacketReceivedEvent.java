package org.dnttr.zephyr.network.communication.core.flow.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dnttr.zephyr.event.Event;
import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.communication.core.packet.Packet;

/**
 * @author dnttr
 */

@Getter
@RequiredArgsConstructor
public final class PacketReceivedEvent extends Event {

    public final Packet packet;
    public final ChannelContext channelContext;
}
