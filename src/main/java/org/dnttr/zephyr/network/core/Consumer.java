package org.dnttr.zephyr.network.core;

import org.dnttr.zephyr.protocol.packet.Packet;

/**
 * @author dnttr
 */

public abstract class Consumer {

    public abstract void send(Packet packet);
}
