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
@Serializable
@Data(identity = -12, protocol = Constants.VER_1)
public final class ConnectionKeepAlivePacket extends Packet {
}
