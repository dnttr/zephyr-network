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
@Serializable
@Data(identity = 11, protocol = Constants.VER_1)
public final class UserStatusPacket extends Packet {

    @Address(address = "status")
    private final int status;

    //0=Offline, 1=Online, 2=Away

    public UserStatusPacket(@Address(address = "status") int status) {
        this.status = status;
    }
}
