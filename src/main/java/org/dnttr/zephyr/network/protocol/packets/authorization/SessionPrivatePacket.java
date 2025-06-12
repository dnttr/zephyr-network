package org.dnttr.zephyr.network.protocol.packets.authorization;

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
@Data(identity = -0x4, protocol = Constants.VER_1)
public final class SessionPrivatePacket extends Packet {

    @Address(address = "privateKey")
    private final byte[] key;

    public SessionPrivatePacket(@Address(address = "privateKey") byte[] key) {
        this.key = key;
    }
}
