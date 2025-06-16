package org.dnttr.zephyr.network.protocol.packets.client;

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
@Data(identity = 0x1, protocol = Constants.VER_1)
public final class ClientAvailabilityPacket extends Packet {

    @Address(address = "available")
    private final boolean available;

    public ClientAvailabilityPacket(@Address(address = "available") boolean available) {
        this.available = available;
    }
}
