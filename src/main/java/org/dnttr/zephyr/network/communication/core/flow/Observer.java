package org.dnttr.zephyr.network.communication.core.flow;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;
import org.dnttr.zephyr.network.communication.core.packet.Direction;
import org.dnttr.zephyr.network.protocol.Packet;

import java.util.concurrent.CompletableFuture;

/**
 * @author dnttr
 */

@Getter
@RequiredArgsConstructor
public class Observer extends CompletableFuture<Packet> {

    @Getter(AccessLevel.NONE)
    private final Class<? extends Packet> klass;

    private final Direction direction;

    private final ChannelContext context;
}
