package org.dnttr.zephyr.network.communication.core.flow.events.ipc.send;

import lombok.Getter;
import org.dnttr.zephyr.event.Event;

/**
 * @author dnttr
 */

@Getter
public final class PushTextureCommand extends Event {

    private final String name;
    private final byte[] data;
    private final int width;
    private final int height;

    public PushTextureCommand(String name, byte[] data, int width, int height) {
        this.name = name;
        this.data = data;
        this.width = width;
        this.height = height;
    }
}