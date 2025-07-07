package org.dnttr.zephyr.network.communication.api;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.dnttr.zephyr.network.communication.core.managers.ObserverManager;
import org.dnttr.zephyr.network.loader.core.Worker;

/**
 * @author dnttr
 */

@RequiredArgsConstructor
public abstract class Endpoint {

    @Getter(AccessLevel.PROTECTED)
    private final ObserverManager observerManager;

    @Getter
    @Setter
    private Worker<?> parent;
}
