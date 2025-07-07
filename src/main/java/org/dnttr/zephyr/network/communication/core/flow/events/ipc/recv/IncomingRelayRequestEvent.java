package org.dnttr.zephyr.network.communication.core.flow.events.ipc.recv;

import lombok.Getter;
import org.dnttr.zephyr.event.Event;

/**
 * @author dnttr
 */

@Getter
public final class IncomingRelayRequestEvent extends Event {
    private final String senderName;

    public IncomingRelayRequestEvent(String senderName) {
        this.senderName = senderName;
    }
}