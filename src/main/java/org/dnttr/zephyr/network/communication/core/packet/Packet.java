package org.dnttr.zephyr.network.communication.core.packet;

import org.dnttr.zephyr.network.protocol.Data;

/**
 * @author dnttr
 */

public abstract class Packet {

    public Data getData() {
        return this.getClass().getDeclaredAnnotation(Data.class);
    }
}
