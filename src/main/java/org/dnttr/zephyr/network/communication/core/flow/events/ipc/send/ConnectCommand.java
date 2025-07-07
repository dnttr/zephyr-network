package org.dnttr.zephyr.network.communication.core.flow.events.ipc.send;

import lombok.Getter;
import org.dnttr.zephyr.event.Event;

import java.net.InetSocketAddress;

/**
 * @author dnttr
 */

@Getter
public final class ConnectCommand extends Event {

    private final InetSocketAddress address;

    public ConnectCommand(String ip, int port) {
        this.address = new InetSocketAddress(ip, port);
    }
}