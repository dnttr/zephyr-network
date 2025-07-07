package org.dnttr.zephyr.network.protocol.packets.internal;

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
@Data(identity = -13, protocol = Constants.VER_1)
public final class ConnectionUserListPacket extends Packet {

    @Address(address = "payload")
    private final String payload;

    public ConnectionUserListPacket(@Address(address = "payload") String payload) {
        this.payload = payload;
    }
}
