package org.dnttr.zephyr.network.communication.core.packet;

/**
 * @author dnttr
 */

public abstract class Packet {

    public Data getData() {
        return this.getClass().getDeclaredAnnotation(Data.class);
    }
}
