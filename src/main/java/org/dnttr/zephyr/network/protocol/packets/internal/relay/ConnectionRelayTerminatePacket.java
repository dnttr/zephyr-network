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
@Data(identity = -11, protocol = 1)
public final class ConnectionRelayTerminatePacket extends Packet {

    @Address(address = "reason")
    private final String reason;

    public ConnectionRelayTerminatePacket(@Address(address = "reason") String reason) {
        this.reason = reason;
    }
}
