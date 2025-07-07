package org.dnttr.zephyr.network.protocol.packets.internal;

import lombok.Getter;
import org.dnttr.zephyr.network.protocol.Constants;
import org.dnttr.zephyr.network.protocol.Data;
import org.dnttr.zephyr.network.protocol.Packet;
import org.dnttr.zephyr.serializer.annotations.Serializable;

/**
 * @author dnttr
 */

@Getter
@Data(identity = -14, protocol = Constants.VER_1)
@Serializable
public final class ConnectionGetUserListPacket extends Packet {
}
