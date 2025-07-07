package org.dnttr.zephyr.network.communication.core.flow.events.ipc.send;

import lombok.Getter;
import org.dnttr.zephyr.event.Event;

/**
 * @author dnttr
 */

@Getter
public final class IdentifyCommand extends Event {

    private final String name;

    public IdentifyCommand(String name) {
        this.name = name;
    }
}