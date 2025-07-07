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
@Data(identity = -8, protocol = Constants.VER_1)
@Serializable
public final class ConnectionRelayRequest extends Packet {

    @Address(address = "name")
    private final String name;

    public ConnectionRelayRequest(@Address(address = "name") String name) {
        this.name = name;
    }
}
