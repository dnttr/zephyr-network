package org.dnttr.zephyr.network.communication.api.server.relay;

import lombok.RequiredArgsConstructor;
import org.dnttr.zephyr.network.communication.core.Consumer;

/**
 * @author dnttr
 */

@RequiredArgsConstructor
public final class Candidate {

    private final Consumer consumer;
    private final long uuid;
}
