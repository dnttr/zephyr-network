package org.dnttr.zephyr.network.communication.core.flow.events.internal.channel;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dnttr.zephyr.event.Event;

/**
 * @author dnttr
 */

@Getter
@RequiredArgsConstructor
public final class ConnectionFatalEvent extends Event {

    private final String reason;
}
