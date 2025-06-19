package org.dnttr.zephyr.network.protocol.packets.internal.authorization;

import lombok.Getter;
import org.dnttr.zephyr.network.protocol.Data;
import org.dnttr.zephyr.network.protocol.Packet;
import org.dnttr.zephyr.serializer.annotations.Address;
import org.dnttr.zephyr.serializer.annotations.Serializable;

import static org.dnttr.zephyr.network.protocol.Constants.VER_1;

/**
 * @author dnttr
 */

@Getter
@Serializable
@Data(identity = -0x2, protocol = VER_1)
public final class ConnectionPublicPacket extends Packet {

    @Address(address = "publicKey")
    private final byte[] publicKey;

    public ConnectionPublicPacket(@Address(address = "publicKey") byte[] publicKey) {
        this.publicKey = publicKey;
    }
}
