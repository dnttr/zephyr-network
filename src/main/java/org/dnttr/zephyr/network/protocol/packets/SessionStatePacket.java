package org.dnttr.zephyr.network.protocol.packets;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dnttr.zephyr.network.protocol.Constants;
import org.dnttr.zephyr.network.protocol.Data;
import org.dnttr.zephyr.network.protocol.Packet;
import org.dnttr.zephyr.serializer.annotations.Map;
import org.dnttr.zephyr.serializer.annotations.Serializable;

/**
 * @author dnttr
 */

@Getter
@Serializable
@RequiredArgsConstructor
@Data(identity = -0x3, protocol = Constants.VER_1)
public final class SessionStatePacket extends Packet {

    @Map(address = "state")
    private final int state;

    @Getter
    @RequiredArgsConstructor
    public enum State {

        REGISTER_REQUEST(0x0),
        NOT_AVAILABLE(0x1),
        READY(0x2);

        private final int value;
    }
}
