package org.dnttr.zephyr.network.communication.core.flow.events.ipc.send;

import lombok.Getter;
import org.dnttr.zephyr.event.Event;

/**
 * @author dnttr
 */

@Getter
public final class RequestRelayCommand extends Event {

    private final String targetName;

    public RequestRelayCommand(String targetName) {
        this.targetName = targetName;
    }
}