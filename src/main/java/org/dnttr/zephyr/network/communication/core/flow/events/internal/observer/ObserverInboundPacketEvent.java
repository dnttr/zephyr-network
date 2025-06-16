package org.dnttr.zephyr.network.communication.core.flow.events.internal.observer;

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
public final class ObserverInboundPacketEvent extends Event {

    public final Packet packet;
    public final ChannelContext channelContext;
}
