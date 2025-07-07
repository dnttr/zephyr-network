package org.dnttr.zephyr.network.protocol.packets.internal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dnttr.zephyr.network.protocol.Constants;
import org.dnttr.zephyr.network.protocol.Data;
import org.dnttr.zephyr.network.protocol.Packet;
import org.dnttr.zephyr.serializer.annotations.Address;
import org.dnttr.zephyr.serializer.annotations.Serializable;

import java.util.Arrays;

/**
 * @author dnttr
 */

@Getter
@Serializable
@Data(identity = -1, protocol = Constants.VER_1)
public class ConnectionStatePacket extends Packet {

    @Address(address = "sessionState")
    private final int state;

    public ConnectionStatePacket(@Address(address = "sessionState") int state) {
        this.state = state;
    }

    @Getter
    @RequiredArgsConstructor
    public enum State {

        REGISTER_OPEN(0x0),
        REGISTER_KEYS(0x1),
        REGISTER_CLOSE(0x2),
        REGISTER_CONFIRM(0x3);

        private final int value;

        public static State from(int value) {
            return Arrays.stream(State.values()).filter(state -> state.value == value).findFirst().orElse(null);
        }
    }
}
