package org.dnttr.zephyr.network.communication.core.flow.events.ipc.send;

import lombok.Getter;
import org.dnttr.zephyr.event.Event;

/**
 * @author dnttr
 */

@Getter
public final class PushFontCommand extends Event {

    private final String name;
    private final byte[] data;

    public PushFontCommand(String name, byte[] data) {
        this.name = name;
        this.data = data;
    }
}