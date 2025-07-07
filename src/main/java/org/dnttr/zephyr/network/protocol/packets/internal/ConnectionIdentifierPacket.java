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
@Data(identity = -5, protocol = Constants.VER_1)
public final class ConnectionIdentifierPacket extends Packet {

    @Address(address = "name")
    private final String name;

    public ConnectionIdentifierPacket(@Address(address = "name") String name) {
        this.name = name;
    }
}
