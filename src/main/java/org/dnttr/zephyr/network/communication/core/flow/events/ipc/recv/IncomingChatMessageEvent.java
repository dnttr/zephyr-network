package org.dnttr.zephyr.network.communication.core.flow.events.ipc.recv;

import lombok.Getter;
import org.dnttr.zephyr.event.Event;

/**
 * @author dnttr
 */

@Getter
public final class IncomingChatMessageEvent extends Event {

    private final String message;

    public IncomingChatMessageEvent(String message) {
        this.message = message;
    }
}