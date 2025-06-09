package org.dnttr.zephyr.network.protocol.packets;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dnttr.zephyr.network.protocol.Constants;
import org.dnttr.zephyr.network.protocol.Data;
import org.dnttr.zephyr.network.protocol.Packet;

/**
 * @author dnttr
 */

@Getter
@RequiredArgsConstructor
@Data(identity = -0x3, protocol = Constants.VER_1)
public final class SessionStatePacket extends Packet {

    private final int state;

    @Getter
    @RequiredArgsConstructor
    public enum State {

        AVAILABLE(0x0),
        NOT_AVAILABLE(0x1),
        READY(0x2);

        private final int value;
    }
}
