package org.dnttr.zephyr.network.communication.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dnttr.zephyr.network.protocol.Packet;

/**
 * @author dnttr
 */

@RequiredArgsConstructor
public abstract class Consumer {

    @Getter
    private final long uuid;

    public abstract void send(Packet packet);
}
