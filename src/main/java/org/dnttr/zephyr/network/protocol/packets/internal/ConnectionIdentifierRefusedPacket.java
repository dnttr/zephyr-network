package org.dnttr.zephyr.network.protocol.packets.internal;

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
@Data(identity = -6, protocol = 1)
public final class ConnectionIdentifierRefusedPacket extends Packet {

    @Address(address = "reason")
    private final String reason;

    public ConnectionIdentifierRefusedPacket(@Address(address = "reason") String reason) {
        this.reason = reason;
    }
}
