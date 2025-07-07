package org.dnttr.zephyr.network.communication.core.flow.events.ipc.send;

import lombok.Getter;
import org.dnttr.zephyr.event.Event;

@Getter
public final class SendUserStatusCommand extends Event {

    private final int status;

    public SendUserStatusCommand(int status) {
        this.status = status;
    }
}