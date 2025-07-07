package org.dnttr.zephyr.network.communication.core.flow.events.ipc.recv;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dnttr.zephyr.event.Event;

/**
 * @author dnttr
 */

@Getter
@RequiredArgsConstructor
public final class IncomingUserListEvent extends Event {

    private final String payload;
}
