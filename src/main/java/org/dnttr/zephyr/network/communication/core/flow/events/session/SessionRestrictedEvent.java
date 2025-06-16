package org.dnttr.zephyr.network.communication.core.flow.events.session;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dnttr.zephyr.event.Event;
import org.dnttr.zephyr.network.communication.core.Consumer;

/**
 * @author dnttr
 */

@Getter
@RequiredArgsConstructor
public class SessionRestrictedEvent extends Event {

    private final Consumer consumer;
    private final String reason;
}

