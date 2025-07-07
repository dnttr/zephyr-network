package org.dnttr.zephyr.network.protocol.packets.internal.relay;

import lombok.Getter;
import org.dnttr.zephyr.network.protocol.Data;
import org.dnttr.zephyr.network.protocol.Packet;
import org.dnttr.zephyr.serializer.annotations.Address;
import org.dnttr.zephyr.serializer.annotations.Serializable;

/**
 * @author dnttr
 */

@Getter
@Serializable
@Data(identity = -9, protocol = 1)
public final class ConnectionRelayResponse extends Packet {

    @Address(address = "state")
    private final int state;

    public ConnectionRelayResponse(@Address(address = "state") int state) {
        this.state = state;
    }

    public enum State {
        SUCCESS(0x0),
        WAIT(0x1),
        REFUSED(0x2);

        @Getter
        private final int value;

        State(int value) {
            this.value = value;
        }
    }
}
