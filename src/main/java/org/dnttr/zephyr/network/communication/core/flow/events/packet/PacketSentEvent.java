package org.dnttr.zephyr.network.communication.core.flow.events.packet;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dnttr.zephyr.event.Event;
import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.protocol.Packet;

/**
 * @author dnttr
 */

@Getter
@RequiredArgsConstructor
public final class PacketSentEvent extends Event {

    private final Packet packet;
    private final ChannelContext channelContext;
}
