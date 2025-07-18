package org.dnttr.zephyr.network.communication.core.flow.events.internal.channel;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dnttr.zephyr.event.Event;
import org.dnttr.zephyr.network.communication.core.channel.ChannelContext;

/**
 * @author dnttr
 */

@Getter
@RequiredArgsConstructor
public class ConnectionInitialPublicKeyExchangedEvent extends Event {

    private final ChannelContext context;
}
