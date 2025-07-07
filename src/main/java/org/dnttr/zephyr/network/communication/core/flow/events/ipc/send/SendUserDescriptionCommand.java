package org.dnttr.zephyr.network.communication.core.flow.events.ipc.send;

import lombok.Getter;
import org.dnttr.zephyr.event.Event;

@Getter
public final class SendUserDescriptionCommand extends Event {

    private final String description;

    public SendUserDescriptionCommand(String description) {
        this.description = description;
    }
}