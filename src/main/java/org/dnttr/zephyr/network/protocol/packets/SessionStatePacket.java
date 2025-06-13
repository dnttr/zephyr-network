package org.dnttr.zephyr.network.protocol.packets;

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
@Data(identity = -0x1, protocol = Constants.VER_1)
public class SessionStatePacket extends Packet {

    @Address(address = "sessionState")
    private final int state;

    public SessionStatePacket(@Address(address = "sessionState") int state) {
        this.state = state;
    }


    @Getter
    @RequiredArgsConstructor
    public enum State {

        REGISTER_REQUEST(0x0),
        REGISTER_RESPONSE(0x1);

        private final int value;

        public static State from(int value) {
            return Arrays.stream(State.values()).filter(state -> state.value == value).findFirst().orElse(null);
        }
    }
}
