package org.dnttr.zephyr.network.communication.core.flow.events.ipc.send;

import lombok.Getter;
import org.dnttr.zephyr.event.Event;

/**
 * @author dnttr
 */

@Getter
public final class AnswerRelayCommand extends Event {

    private final boolean accepted;

    public AnswerRelayCommand(boolean accepted) {
        this.accepted = accepted;
    }
}