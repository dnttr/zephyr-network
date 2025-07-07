package org.dnttr.zephyr.network.communication.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.protocol.Packet;

/**
 * @author dnttr
 */

@Getter
@RequiredArgsConstructor
public abstract class Consumer {

    private final long uuid;
    private final ChannelContext context;

    @Setter
    private boolean isFree;

    public abstract void send(Packet packet);
}
