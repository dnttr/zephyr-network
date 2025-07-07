package org.dnttr.zephyr.network.communication.core.flow.events.ipc.recv;

import lombok.Getter;
import org.dnttr.zephyr.event.Event;

/**
 * @author dnttr
 */

@Getter
public final class IncomingUserDescriptionEvent extends Event {
    private final String description;

    public IncomingUserDescriptionEvent(String description) {
        this.description = description;
    }
}