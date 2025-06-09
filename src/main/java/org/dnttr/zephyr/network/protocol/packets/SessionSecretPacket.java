package org.dnttr.zephyr.network.protocol.packets;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.dnttr.zephyr.network.protocol.Constants;
import org.dnttr.zephyr.network.protocol.Data;
import org.dnttr.zephyr.network.protocol.Packet;
import org.dnttr.zephyr.serializer.annotations.Serializable;

/**
 * @author dnttr
 */

@Getter
@Serializable
@RequiredArgsConstructor
@Data(identity = -0x2, protocol = Constants.VER_1)
public final class SessionSecretPacket extends Packet {

    private final byte[] secret;
}
