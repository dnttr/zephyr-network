package org.dnttr.zephyr.network.protocol;

/**
 * @author dnttr
 */

public abstract class Packet {

    public Data getData() {
        return this.getClass().getDeclaredAnnotation(Data.class);
    }

    @Override
    public String toString() {
        return "identity: " + getData().identity() + " version: " + getData().protocol();
    }
}
