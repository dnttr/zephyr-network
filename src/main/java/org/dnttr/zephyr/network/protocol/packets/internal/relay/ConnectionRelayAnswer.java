package org.dnttr.zephyr.network.protocol.packets.internal.relay;

import lombok.Getter;
import org.dnttr.zephyr.network.protocol.Constants;
import org.dnttr.zephyr.network.protocol.Data;
import org.dnttr.zephyr.network.protocol.Packet;
import org.dnttr.zephyr.serializer.annotations.Address;
import org.dnttr.zephyr.serializer.annotations.Serializable;

/**
 * @author dnttr
 */

@Getter
@Serializable
@Data(identity = -10, protocol = Constants.VER_1)
public final class ConnectionRelayAnswer extends Packet {

    @Address(address = "state")
    private final int state;

    public ConnectionRelayAnswer(@Address(address = "state") int state) {
        this.state = state;
    }

    public enum Answer {

        ACCEPT(0x0),
        REFUSE(0x1);

        @Getter
        private final int value;

        Answer(int value) {
            this.value = value;
        }
    }
}
