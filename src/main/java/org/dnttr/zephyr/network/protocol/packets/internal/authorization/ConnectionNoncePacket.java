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
@Data(identity = -3, protocol = VER_1)
public final class ConnectionNoncePacket extends Packet {

    @Address(address = "nonceMode")
    private final int mode;

    @Address(address = "nonce")
    private final byte[] nonce;

    public ConnectionNoncePacket(@Address(address = "nonceMode") int mode, @Address(address = "nonce") byte[] nonce) {
        this.mode = mode;
        this.nonce = nonce;
    }
}
