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
@Data(identity = 10, protocol = Constants.VER_1)
public final class ChatMessagePacket extends Packet {

    @Address(address = "message")
    private final String message;

    public ChatMessagePacket(@Address(address = "message") String message) {
        this.message = message;
    }
}
