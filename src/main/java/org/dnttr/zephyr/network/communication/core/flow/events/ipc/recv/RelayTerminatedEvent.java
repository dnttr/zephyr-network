package org.dnttr.zephyr.network.communication.core.flow.events.ipc.recv;

import lombok.Getter;
import org.dnttr.zephyr.event.Event;

/**
 * @author dnttr
 */

@Getter
public final class RelayTerminatedEvent extends Event {
    private final String reason;

    public RelayTerminatedEvent(String reason) {
        this.reason = reason;
    }
}