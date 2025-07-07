package org.dnttr.zephyr.network.communication.api.server.relay;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dnttr.zephyr.network.communication.core.Consumer;
import org.jetbrains.annotations.NotNull;

/**
 * @author dnttr
 */

@RequiredArgsConstructor
@Getter
public final class Candidate {

    private final String name;

    @NotNull
    private final Consumer consumer;
}
