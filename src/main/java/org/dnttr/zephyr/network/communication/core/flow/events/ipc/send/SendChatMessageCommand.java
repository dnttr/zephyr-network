package org.dnttr.zephyr.network.communication.core.flow.events.ipc.send;

import lombok.Getter;
import org.dnttr.zephyr.event.Event;

@Getter
public final class SendChatMessageCommand extends Event {

    private final String message;

    public SendChatMessageCommand(String message) {
        this.message = message;
    }
}