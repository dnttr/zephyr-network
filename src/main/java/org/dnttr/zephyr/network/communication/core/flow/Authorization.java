package org.dnttr.zephyr.network.communication.core.flow;

import lombok.AccessLevel;
import lombok.Getter;
import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.network.communication.core.managers.ObserverManager;

/**
 * @author dnttr
 */

public abstract class Authorization {

    @Getter(AccessLevel.PROTECTED)
    private final ObserverManager observerManager;

    @Getter(AccessLevel.PROTECTED)
    private final EventBus bus;

    public Authorization(final EventBus bus, ObserverManager manager) {
        bus.register(this);

        this.bus = bus;
        this.observerManager = manager;
    }
}
