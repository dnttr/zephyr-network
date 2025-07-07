package org.dnttr.zephyr.network.communication.core.flow.events.ipc.recv;

import lombok.Getter;
import org.dnttr.zephyr.event.Event;

/**
 * @author dnttr
 */

@Getter
public final class IncomingUserStatusEvent extends Event {
    private final int status;

    public IncomingUserStatusEvent(int status) {
        this.status = status;
    }
}