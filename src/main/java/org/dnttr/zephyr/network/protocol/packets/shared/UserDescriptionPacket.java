package org.dnttr.zephyr.network.protocol.packets.shared;

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
@Data(identity = 12, protocol = Constants.VER_1)
@Serializable
public final class UserDescriptionPacket extends Packet {

    @Address(address = "description")
    private final String description;

    public UserDescriptionPacket(@Address(address = "description") String description) {
        this.description = description;
    }
}
