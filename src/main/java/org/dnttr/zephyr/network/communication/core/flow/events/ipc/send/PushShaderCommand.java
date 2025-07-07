package org.dnttr.zephyr.network.communication.core.flow.events.ipc.send;

import lombok.Getter;
import org.dnttr.zephyr.event.Event;

/**
 * @author dnttr
 */

@Getter
public final class PushShaderCommand extends Event {

    private final String name;
    private final String source;

    public PushShaderCommand(String name, String source) {
        this.name = name;
        this.source = source;
    }
}