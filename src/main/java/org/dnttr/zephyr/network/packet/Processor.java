package org.dnttr.zephyr.network.packet;

import java.util.IdentityHashMap;

/**
 * @author dnttr
 */

public class Processor {

    private final IdentityHashMap<Integer, Class<?>> packets;

    public Processor() {
        this.packets = new IdentityHashMap<>();
    }
}
