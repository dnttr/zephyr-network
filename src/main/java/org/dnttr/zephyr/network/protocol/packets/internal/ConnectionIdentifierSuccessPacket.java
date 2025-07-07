package org.dnttr.zephyr.network.protocol.packets.internal;

import lombok.Getter;
import org.dnttr.zephyr.network.protocol.Data;
import org.dnttr.zephyr.network.protocol.Packet;
import org.dnttr.zephyr.serializer.annotations.Serializable;

/**
 * @author dnttr
 */

@Getter
@Serializable
@Data(identity = -7, protocol = 1)
public final class ConnectionIdentifierSuccessPacket extends Packet {

}
