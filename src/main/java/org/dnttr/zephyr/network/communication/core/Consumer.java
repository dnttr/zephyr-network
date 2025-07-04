package org.dnttr.zephyr.network.communication.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.dnttr.zephyr.network.protocol.Packet;

/**
 * @author dnttr
 */

@AllArgsConstructor
public abstract class Consumer {

    @Getter
    private final long uuid;

    public abstract void send(Packet packet);
}
