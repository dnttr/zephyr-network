package org.dnttr.zephyr.network.communication.core.managers;

import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.network.communication.core.flow.Observer;
import org.dnttr.zephyr.network.communication.core.packet.Packet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author dnttr
 */

public class ObserverManager {

    private final Map<Class<? extends Packet>, Observer> observers;

    public ObserverManager(EventBus bus) {
        this.observers = new ConcurrentHashMap<>();

        bus.register(this);
    }
}
