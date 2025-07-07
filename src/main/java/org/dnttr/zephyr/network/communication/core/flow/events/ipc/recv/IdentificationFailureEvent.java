package org.dnttr.zephyr.network.communication.core.flow.events.ipc.recv;

import lombok.Getter;
import org.dnttr.zephyr.event.Event;

/**
 * @author dnttr
 */

@Getter
public final class IdentificationFailureEvent extends Event {

    private final String reason;

    public IdentificationFailureEvent(String reason) {
        this.reason = reason;
    }
}